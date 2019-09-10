package client;


import client.auth.AuthException;
import common.message.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import common.handlers.InputDownloadFileHandler;
import common.handlers.OutSendFileHandler;
import swing.MainWindow;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

public class Network {

    private Channel currentChannel;
    private MainWindow parent;

    private String userName;
    private InfoFileClass fileData;

    private Thread thread;
    private final String CLIENT_STORAGE = "client_storage/";
    private final int maxObjectSize = 104857600;

    public Network(MainWindow parent) {
        this.parent = parent;
        this.userName = null;
        networkThreadStart();
    }

    public Network(MainWindow parent, InfoFileClass fileData) {
        this.parent = parent;
        this.userName = null;
        this.fileData = fileData;
        networkThreadStart();
    }

    private void networkThreadStart () {
        CountDownLatch networkStarter = new CountDownLatch(1);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Сетевое подключение открыто");
                start(networkStarter);
                if (fileData!=null) {
                    parent.getListDownload().remove(fileData.getFileName());
                    System.out.println("remove network from listDownload");
                }
                System.out.println("Сетевое подключение закрыто");
            }
        });

        thread.start();

        try {
            networkStarter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public InfoFileClass getFileData () {
        return fileData;
    }

    public Thread getThread  () {
        return thread;
    }

    public Channel getCurrentChannel() {
        return this.currentChannel;
    }

    public void start(CountDownLatch countDownLatch) {
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress("localhost", 8188));
            clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new ChannelHandler[]{new ObjectDecoder(maxObjectSize, ClassResolvers.cacheDisabled((ClassLoader) null)),
                              new ObjectEncoder(),
                              Network.this.new ReadMessageHandler(parent)});
                    Network.this.currentChannel = socketChannel;
                }
            });
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            countDownLatch.countDown();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception var13) {
            var13.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException var12) {
                var12.printStackTrace();
            }
        }
    }

    public void stop() {
        getCurrentChannel().writeAndFlush(new CommandMessage(CommandMessage.Command.STOP));
        this.currentChannel.close();

    }

    public void changeHandlerForSendFile(String path) {
        this.currentChannel.pipeline().addLast(new ChannelHandler[]{new OutSendFileHandler(path)});
        getCurrentChannel().writeAndFlush(new CommandMessage(CommandMessage.Command.FILE_DOWNLOAD_NEXT_PART, 0));

    }

    public void changeHandlerForDownloadFile(String userName, String path, long size) {
        this.currentChannel.pipeline().addLast(new ChannelHandler[]{new InputDownloadFileHandler(CLIENT_STORAGE, path, userName, size, parent)});

    }

    public void sendMessage (AbstractMessage message) {
        getCurrentChannel().writeAndFlush(message);
    }

    private class ReadMessageHandler extends ChannelInboundHandlerAdapter {
        private boolean authOk;
        private MainWindow parent;

        private ReadMessageHandler(MainWindow parent) {
            this.authOk = false;
            this.parent = parent;

        }

        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            AbstractMessage mes = (AbstractMessage)msg;
            if (mes instanceof AuthMessage) {
                AuthMessage authMsg = (AuthMessage) mes;
                if (!authMsg.isAuthSuccessfull()) {
                    parent.getLoginDialog().setConnected(false);
                    parent.getLoginDialog().tryCloseLoginDialog(new AuthException());

                }else {
                    parent.getLoginDialog().setConnected(true);
                    parent.getLoginDialog().tryCloseLoginDialog(null);
                    userName = authMsg.getUserName();
                    parent.setUserName(userName);
                }
            }else if (mes instanceof ListFilesMessage) {
                ListFilesMessage listFilesMessage = (ListFilesMessage) mes;
                parent.getTableFiles().updateTable(listFilesMessage.getArr());
            }else if (mes instanceof CommandMessage) {
                CommandMessage command = (CommandMessage) mes;
                if(command.getCommand() == CommandMessage.Command.DELETE) {
                    if (command.isResult()) {
                        if (command.getIdx() != null){
                            parent.getTableFiles().removeFile(command.getIdx());
                        }
                        if (fileData!=null) {
                            sendMessage(new CommandMessage(CommandMessage.Command.ADD, fileData.getFileName(), fileData.getSize()));
                        }
                    }else {
                        System.out.println("File delete false");
                    }
                }else if (command.getCommand() == CommandMessage.Command.FILE_EXIST_TRUE) {
                    parent.showMessage(Network.this);
                }else if (command.getCommand() == CommandMessage.Command.FILE_EXIST_FALSE) {
                    String [][] arr = {{fileData.getFileName(),"Download..." }};
                    parent.getTableFiles().updateTable(arr);
                    changeHandlerForSendFile(fileData.getPath());
                }else if (command.getCommand() == CommandMessage.Command.FILE_DOWNLOAD_NEXT_PART) {
                    getCurrentChannel().writeAndFlush(command);
                }else if (command.getCommand() == CommandMessage.Command.FILE_UPLOAD_COMPLETED) {
                    System.out.println("Загрузка файла завершена");
                    parent.getTableFiles().removeFile(parent.searchEqualsFileName(fileData.getFileName()));
                    String [][] arr = {{fileData.getFileName(),fileData.getSize()+ " byte" }};
                    parent.getTableFiles().updateTable(arr);
                    ctx.close();
                }
            }else if (mes instanceof FileMessage) {
                FileMessage message = (FileMessage) mes;
                ctx.fireChannelRead(message);
            }
        }

        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            if (cause instanceof AuthException) {
                throw new AuthException();
            }else {
                parent.getLoginDialog().tryCloseLoginDialog(new Exception());
                ctx.close();
                cause.printStackTrace();
            }
        }
    }
}

