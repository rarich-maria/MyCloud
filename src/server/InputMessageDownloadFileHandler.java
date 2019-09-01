package server;

import common.CommandMessage;
import common.FileMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import swing.MainWindow;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class InputMessageDownloadFileHandler extends ChannelInboundHandlerAdapter {

    private String pathFile;
    private String fileName;
    private String userName;
    private long size;
    private BufferedOutputStream out;
    private MainWindow parent;

    public InputMessageDownloadFileHandler(String pathFile, String fileName, String userName, long size, MainWindow parent) {

        this.fileName = fileName;
        this.pathFile = pathFile;
        this.userName = userName;
        this.size = size;
        this.parent = parent;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        FileMessage message = (FileMessage) msg;
        System.out.println("It is FileMessage InputMessageDownloadFileHandler message.partNumber / message.partsCount " +
                  message.partNumber + "  /  " + message.partsCount);

        out = new BufferedOutputStream(new FileOutputStream(pathFile+"/"+userName+"/"+fileName, true));

        out.write(message.data);
        out.flush();

        if (message.partNumber%100 == 0L) {

            ctx.writeAndFlush(new CommandMessage(CommandMessage.Command.FILE_DOWNLOAD_NEXT_PART, message.partNumber));
            System.out.println("Next part " + message.partNumber);
            out.close();
        }

        if (message.partNumber == message.partsCount) {
            out.close();
            if (parent == null){
                ctx.writeAndFlush(new CommandMessage(CommandMessage.Command.FILE_UPLOAD_COMPLETED));
                System.out.println("Command.FILE_UPLOAD_COMPLETED");
            }else {
                parent.showMessageDownloadComplited(fileName);
            }

            ctx.close();
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
