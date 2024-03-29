package server;


import client.auth.AuthServiceImpl;
import common.*;
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
            System.out.println("server get msg");
            AbstractMessage mes = (AbstractMessage)msg;
            if (mes instanceof NewChanelForSendFileMessage) {
                this.authOk = true;
                this.userName = ((NewChanelForSendFileMessage) mes).getUserName();
                System.out.println("server get NewChanelForSendFileMessag, username " + userName);
            }


            if (this.authOk) {
                System.out.println("authOk true");
                if (mes instanceof CommandMessage) {
                    System.out.println("server get CommandMessage");
                    CommandMessage command = (CommandMessage) mes;
                    if (command.getCommand() == CommandMessage.Command.DELETE) {
                        File file = new File("server_storage/"+userName+"/"+command.getPath());
                        System.out.println("path file for delete " + "server_storage/"+userName+"/"+command.getPath());
                        Integer idx = command.getIdx();

                        // Ошибка касается только удаления файла на сервере
                        // При удалении недозагруженного файла из основного потока после закрытия канала передачи файла здесь возможны 4 ситуации:
                        // ошибка, файл занят другим потоком 1 раз было
                        // никакой ошибки нет, но результат удаления false
                        // файл удаляется основным потоком, но InputDownloadFileHandler снова его создаёт и догружает из буфера несколько Кб
                        // всё отлично удаляется

                        // Даже если отказаться от удаления недокаченных фалов при выходе из приложения
                        // пользователь может нажать удалить файл, который находится в процессе загрузки
                        // тогда генерируется окно с предупреждением и предложением остановить загрузку или продолжить
                        // в случае остановки загрузки, недокаченный файл в любом случае нужно удалить, поэтому возникает та же ситуация

                        boolean result = false;
                        while (file.exists()) {
                            try {
                                result = file.delete();
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
                            ctx.pipeline().addLast(new ChannelHandler[]{new InputDownloadeFileHandler("server_storage", command.getPath(), userName, command.getSize())});
                            ctx.pipeline().remove(Server.AuthHandler.class);
                            ctx.pipeline().remove(ObjectDecoder.class);

                        }


                    }else if (command.getCommand() == CommandMessage.Command.DOWNLOAD) {

                        ctx.pipeline().addFirst(new ChannelHandler[]{new OutFileSendHandler("server_storage/"+userName+"/"+command.getPath(), null, null)});
                        ctx.pipeline().remove(Server.AuthHandler.class);
                        ctx.pipeline().remove(ObjectEncoder.class);
                        ctx.writeAndFlush("File start");
                    }

                }else if (mes instanceof FileMessage) {
                    System.out.println("It is FileMessage");
                    FileMessage message = (FileMessage) mes;
                    if (message.partNumber == message.partsCount) {
                        System.out.println("It is last part file");
                    }
                    ctx.fireChannelRead(message.buf);

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

