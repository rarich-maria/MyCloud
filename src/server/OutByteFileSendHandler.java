package server;

import client.Network;
import common.FileMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import swing.MainWindow;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

public class OutByteFileSendHandler extends ChannelOutboundHandlerAdapter {

    private String pathFile;
    private MainWindow parent;
    private Network network;

    public OutByteFileSendHandler(String pathFile, MainWindow parent, Network network) {

        this.pathFile = pathFile;
        this.parent = parent;
        this.network = network;
    }

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("OutByteFileSendHandler start");
        System.out.println(msg);
        System.out.println(pathFile);
        File file = new File(pathFile);


        int bufSize = 10485760;
        int partsCount = (new Long(file.length() / (long)bufSize)).intValue();
        if (file.length() % (long)bufSize != 0L) {
            ++partsCount;
        }
        System.out.println("partsCount " + partsCount);
        ByteBuf buf = ctx.alloc().buffer(bufSize);


        FileMessage fmOut = new FileMessage(pathFile, -1, partsCount, buf);
        FileInputStream in = new FileInputStream(file);
        byte [] arr = new byte[bufSize];

        for(int i = 0; i < partsCount; ++i) {
            int readedBytes = in.read(arr);
            fmOut.partNumber = i + 1;

            if (readedBytes < bufSize) {
                arr = Arrays.copyOfRange(arr, 0, readedBytes);
            }

            System.out.println("arr.length " + arr.length +" bufSize "+ bufSize);

            fmOut.buf.writeBytes(arr);

            ctx.writeAndFlush(fmOut);
            fmOut.buf.release();
            System.out.println("Отправлена часть #" + (i + 1));
        }

        in.close();
        System.out.println("in.close()");
    }
}
