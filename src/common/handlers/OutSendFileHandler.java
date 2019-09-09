package common.handlers;

import common.CommandMessage;
import common.FileMessage;
import io.netty.channel.*;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class OutSendFileHandler extends ChannelOutboundHandlerAdapter {

    private String pathFile;
    private final int bufSize = 10485760;

    public OutSendFileHandler(String pathFile) {
        this.pathFile = pathFile;
    }

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof CommandMessage) {
            CommandMessage message = (CommandMessage) msg;
            File file = new File(pathFile);

            int partsCount = (new Long(file.length() / (long)bufSize)).intValue();
            if (file.length() % (long)bufSize != 0L) {
                ++partsCount;
            }

            FileMessage fmOut = new FileMessage(pathFile, -1, partsCount, new byte[bufSize]);
            RandomAccessFile inRnd = new RandomAccessFile(file, "r");
            int i = message.getIdx();
            while (i < partsCount) {
                if (partsCount == 1 || i == partsCount-1) {
                    int readedBytes = inRnd.read(fmOut.data);
                    if (readedBytes < bufSize) {
                        fmOut.data = Arrays.copyOfRange(fmOut.data, 0, readedBytes);
                    }
                }else if (i==0 && partsCount>1) {
                    inRnd.readFully(fmOut.data);
                }else {
                    inRnd.seek((long)bufSize*i);
                    inRnd.readFully(fmOut.data);
                }
                fmOut.partNumber = i + 1;
                ctx.writeAndFlush(fmOut);
                i++;
                if (i%100==0L) {break;}
            }
            inRnd.close();
        }else {
            System.out.println("It is not FILE_DOWNLOAD_NEXT_PART");
        }
    }
}
