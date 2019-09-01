package server;

import client.Network;
import common.CommandMessage;
import common.FileMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import swing.MainWindow;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class OutByteFileSendHandler extends ChannelOutboundHandlerAdapter {

    private String pathFile;


    public OutByteFileSendHandler(String pathFile) {

        this.pathFile = pathFile;
    }

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("OutByteFileSendHandler start");
        System.out.println(msg);
        if (msg instanceof CommandMessage) {
            CommandMessage message = (CommandMessage) msg;
            int startPart = message.getIdx();

            System.out.println(pathFile);
            File file = new File(pathFile);
            int bufSize = 10485760;
            int partsCount = (new Long(file.length() / (long)bufSize)).intValue();
            if (file.length() % (long)bufSize != 0L) {
                ++partsCount;
            }
            System.out.println("partsCount " + partsCount);

            FileMessage fmOut = new FileMessage(pathFile, -1, partsCount, new byte[bufSize]);

            RandomAccessFile inRnd = new RandomAccessFile(file, "r");

            int i = startPart;
            while (i < partsCount) {

                if (partsCount == 1 || i == partsCount-1) {
                    int readedBytes = inRnd.read(fmOut.data);
                    if (readedBytes < bufSize) {
                        fmOut.data = Arrays.copyOfRange(fmOut.data, 0, readedBytes);
                    }
                }else if (i==0 && partsCount>1) {
                    System.out.println("inRnd.getFilePointer() " + inRnd.getFilePointer());
                    inRnd.readFully(fmOut.data);
                }else {
                    inRnd.seek((long)bufSize*i);
                    System.out.println("inRnd.getFilePointer() " + inRnd.getFilePointer());
                    inRnd.readFully(fmOut.data);

                }
                fmOut.partNumber = i + 1;
                ctx.writeAndFlush(fmOut);
                System.out.println("Отправлена часть #" + (i + 1) + " fmOut.data.length " + fmOut.data.length);
                i++;
                if (i%100==0L) {break;}

            }

            inRnd.close();
            System.out.println("in.close()");


        }else {
            System.out.println("It is not FILE_DOWNLOAD_NEXT_PART");
        }


    }
}
