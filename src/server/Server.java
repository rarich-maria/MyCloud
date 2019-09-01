package server;


import client.auth.AuthServiceImpl;
import common.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import java.io.File;


public class Server {
    public Server() {
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            ((ServerBootstrap)b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)).childHandler(new ChannelInitializer<SocketChannel>() {
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ChannelHandler[]{new ObjectDecoder(104857600, ClassResolvers.cacheDisabled((ClassLoader) null)), new ObjectEncoder(), Server.this.new AuthHandler()});

                }
            }).childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(8188).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) throws Exception {
        (new Server()).run();
    }



    private class AuthHandler extends ChannelInboundHandlerAdapter {
        private boolean authOk;
        private String userName;

        private AuthHandler() {
            this.authOk = false;
            this.userName = null;
        }

        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //System.out.println("server get msg");
            AbstractMessage mes = (AbstractMessage)msg;
            if (mes instanceof NewChanelForSendFileMessage) {
                this.authOk = true;
                this.userName = ((NewChanelForSendFileMessage) mes).getUserName();
                System.out.println("server get NewChanelForSendFileMessag, username " + userName);
            }

            if (this.authOk) {

                if (mes instanceof CommandMessage) {

                    CommandMessage command = (CommandMessage) mes;
                    System.out.println("server get CommandMessage " + command.getCommand());
                    if (command.getCommand() == CommandMessage.Command.DELETE) {
                        File file = new File("server_storage/"+userName+"/"+command.getPath());
                        System.out.println("path file for delete " + "server_storage/"+userName+"/"+command.getPath());
                        Integer idx = command.getIdx();

                        boolean result = false;
                        while (file.exists()) {
                            try {
                                result = file.delete();
                                System.out.println("File delete result "+ result);
                            }catch (Exception e) {
                                System.out.println("Exception delete file");
                                e.printStackTrace();
                            }
                        }

                        System.out.println("File delete result "+ result +" idx " + idx);
                        if (idx != null) {

                            if(result) {
                                System.out.println("file delete true");
                                ctx.writeAndFlush(new CommandMessage(CommandMessage.Command.DELETE, true, idx));
                            }else{
                                System.out.println("file delete false");
                                ctx.writeAndFlush(new CommandMessage(CommandMessage.Command.DELETE, false, idx));
                            }
                        }else {
                            System.out.println("File idx " + idx);
                        }

                    }else if (command.getCommand() == CommandMessage.Command.ADD) {
                        File file = new File ("server_storage/"+userName+"/"+command.getPath());
                        if (file.exists()){
                            System.out.println("file exist");
                            ctx.writeAndFlush(new CommandMessage(CommandMessage.Command.FILE_EXIST_TRUE));
                        }else {
                            System.out.println("file dont exist");
                            ctx.writeAndFlush(new CommandMessage(CommandMessage.Command.FILE_EXIST_FALSE));
                            ctx.pipeline().addLast(new ChannelHandler[]{new InputMessageDownloadFileHandler("server_storage", command.getPath(), userName, command.getSize(), null)});
                            //ctx.pipeline().addLast(new ChannelHandler[]{new InputDownloadeFileHandler("server_storage", command.getPath(), userName, command.getSize())});
                            //ctx.pipeline().remove(Server.AuthHandler.class);
                            //ctx.pipeline().remove(ObjectDecoder.class);

                        }


                    }else if (command.getCommand() == CommandMessage.Command.DOWNLOAD) {

                        ctx.pipeline().addLast(new ChannelHandler[]{new OutByteFileSendHandler("server_storage/"+userName+"/"+command.getPath())});
                        //ctx.pipeline().remove(Server.AuthHandler.class);
                        //ctx.pipeline().remove(ObjectEncoder.class);
                        ctx.channel().writeAndFlush(new CommandMessage(CommandMessage.Command.FILE_DOWNLOAD_NEXT_PART, 0));
                        //ctx.writeAndFlush(new CommandMessage(CommandMessage.Command.FILE_DOWNLOAD_NEXT_PART, 0));
                    } else if (command.getCommand() == CommandMessage.Command.STOP) {
                        ctx.close();
                        System.out.println("SERVER get Command.STOP");
                    }else if (command.getCommand() == CommandMessage.Command.FILE_DOWNLOAD_NEXT_PART) {
                        System.out.println("FILE_DOWNLOAD_NEXT_PART getIdx" + command.getIdx());
                        ctx.channel().writeAndFlush(command);
                    }

                }else if (mes instanceof FileMessage) {
                    FileMessage message = (FileMessage) mes;
                    System.out.println("It is FileMessage message.partNumber / message.partsCount " +
                              message.partNumber + "  /  " + message.partsCount);

                    ctx.fireChannelRead(message);

                }

            } else {
                if (mes instanceof AuthMessage) {
                    AuthMessage authMsg = (AuthMessage) mes;
                    AuthServiceImpl authService = new AuthServiceImpl ();
                    if (authMsg.getCommand().equals("/auth")){

                        if (authService.authUser(authMsg.getUserName(), authMsg.getPassword())) {
                            userName = authMsg.getUserName();
                            ctx.writeAndFlush(new AuthMessage(true, userName));
                            this.authOk = true;
                            System.out.println("auth on server is true");
                            ctx.writeAndFlush(new ListFilesMessage (userName));
                            System.out.println("username " + userName);
                        }else {
                            ctx.writeAndFlush(new AuthMessage(false, null));
                        }

                    }
                }


            }
        }

        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

                ctx.close();
                cause.printStackTrace();
        }

    }
}

