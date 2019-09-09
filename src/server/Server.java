package server;


import client.auth.AuthServiceImpl;
import common.*;
import common.handlers.InputDownloadFileHandler;
import common.handlers.OutSendFileHandler;
import io.netty.bootstrap.ServerBootstrap;
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

    private final String SERVER_STORAGE = "server_storage/";
    private final int maxObjectSize = 104857600;

    public Server() {
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            ((ServerBootstrap)b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)).childHandler(new ChannelInitializer<SocketChannel>() {
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ChannelHandler[]{new ObjectDecoder(maxObjectSize, ClassResolvers.cacheDisabled((ClassLoader) null)), new ObjectEncoder(), Server.this.new AuthHandler()});

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
            AbstractMessage mes = (AbstractMessage)msg;
            if (mes instanceof NewChanelForSendFileMessage) {
                this.authOk = true;
                this.userName = ((NewChanelForSendFileMessage) mes).getUserName();
            }

            if (this.authOk) {
                if (mes instanceof CommandMessage) {
                    CommandMessage command = (CommandMessage) mes;
                    if (command.getCommand() == CommandMessage.Command.DELETE) {
                        File file = new File(SERVER_STORAGE + userName+"/"+command.getPath());
                        Integer idx = command.getIdx();
                        boolean result = false;
                        while (file.exists()) {
                            try {
                                result = file.delete();
                                System.out.println("File delete result "+ result);
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (idx != null) {
                            if(result) {
                                ctx.writeAndFlush(new CommandMessage(CommandMessage.Command.DELETE, true, idx));
                            }else{
                                ctx.writeAndFlush(new CommandMessage(CommandMessage.Command.DELETE, false, idx));
                            }
                        }
                    }else if (command.getCommand() == CommandMessage.Command.ADD) {
                        File file = new File (SERVER_STORAGE + userName+"/"+command.getPath());
                        if (file.exists()){
                            ctx.writeAndFlush(new CommandMessage(CommandMessage.Command.FILE_EXIST_TRUE));
                        }else {
                            ctx.writeAndFlush(new CommandMessage(CommandMessage.Command.FILE_EXIST_FALSE));
                            ctx.pipeline().addLast(new ChannelHandler[]{new InputDownloadFileHandler(SERVER_STORAGE, command.getPath(), userName, command.getSize(), null)});
                        }
                    }else if (command.getCommand() == CommandMessage.Command.DOWNLOAD) {
                        ctx.pipeline().addLast(new ChannelHandler[]{new OutSendFileHandler(SERVER_STORAGE + userName+"/"+command.getPath())});
                        ctx.channel().writeAndFlush(new CommandMessage(CommandMessage.Command.FILE_DOWNLOAD_NEXT_PART, 0));
                    } else if (command.getCommand() == CommandMessage.Command.STOP) {
                        ctx.close();
                    }else if (command.getCommand() == CommandMessage.Command.FILE_DOWNLOAD_NEXT_PART) {
                        ctx.channel().writeAndFlush(command);
                    }

                }else if (mes instanceof FileMessage) {
                    FileMessage message = (FileMessage) mes;
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
                            ctx.writeAndFlush(new ListFilesMessage (userName));
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

