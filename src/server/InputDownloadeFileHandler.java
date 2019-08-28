package server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.*;

public class InputDownloadeFileHandler extends ChannelInboundHandlerAdapter {
    private String pathFile;
    private String fileName;
    private String userName;
    private long size;
    private BufferedOutputStream out;

    public InputDownloadeFileHandler(String pathFile, String fileName, String userName, long size) {

        this.fileName = fileName;
        this.pathFile = pathFile;
        this.userName = userName;
        this.size = size;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //System.out.println("InputDownloadeFileHandler get file " + msg.getClass() );

        ByteBuf buf = (ByteBuf)msg;
        try {
            out = new BufferedOutputStream(new FileOutputStream(pathFile+"/"+userName+"/"+fileName, true));
            Throwable var5 = null;

            try {
                while(buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                }
            } catch (Throwable var15) {
                System.out.println("var5 = var15");
                var5 = var15;
                throw var15;
            } finally {

                if (out != null) {
                    if (var5 != null) {
                        try {
                            out.close();
                        } catch (Throwable var14) {
                            System.out.println("var5.addSuppressed(var14)");
                            var5.addSuppressed(var14);
                        }
                    } else {
                        out.close();
                    }
                }

            }
        } catch (IOException var17) {
            out.close();
            ctx.close();
            System.out.println("var17.printStackTrace()");
            var17.printStackTrace();
        }

        buf.release();
        long fileSize = new File(pathFile+"/"+userName+"/"+fileName).length();

        if (fileSize == size) {
            System.out.println("file last byte " + fileSize);
        }

    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            System.out.println("=================================n" );
            System.out.println("==== IT is IOException ====" );
        }
        if (cause instanceof FileNotFoundException) {
            System.out.println("Передача файла на сервер не завершена ");
            System.out.println("==== IT is FileNotFoundException ====" );
        }
        out.close();
        ctx.close();
        cause.printStackTrace();
    }
}
