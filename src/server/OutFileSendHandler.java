package server;

import client.Network;
import common.CommandMessage;
import io.netty.channel.*;
import swing.MainWindow;

import java.io.File;
import java.io.FileInputStream;

public class OutFileSendHandler extends ChannelOutboundHandlerAdapter {

    private String pathFile;
    private MainWindow parent;
    private Network network;

    public OutFileSendHandler(String pathFile, MainWindow parent, Network network) {

        this.pathFile = pathFile;
        this.parent = parent;
        this.network = network;
    }

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("OutFileSendHandler start");
        System.out.println(msg);
        System.out.println(pathFile);
        File file = new File(pathFile);
        FileInputStream in = new FileInputStream(file);
        FileRegion region = new DefaultFileRegion(file, 0L, file.length());
        ChannelFuture transferOperationFuture = ctx.writeAndFlush(region);
        transferOperationFuture.addListener((future) -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }

            if (future.isSuccess()) {
                System.out.println("Файл успешно передан ");
                if (parent!=null) {
                    parent.getTableFiles().removeFile(parent.searchEqualsFileName(file.getName()));
                    String [][] arr = {{file.getName(),file.length()+ " byte" }};
                    parent.getTableFiles().updataTable(arr);

                }

                in.close();

                if (network!=null) {
                    ctx.close();
                    network.stop();
                }else {
                    String [][] arr = {{file.getName(),file.length()+ " byte" }};

                    ctx.close();
                }

            }

        });
    }


}
