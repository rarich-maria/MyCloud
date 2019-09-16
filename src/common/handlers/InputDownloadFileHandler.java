package common.handlers;

import client.controller.impl.ClientEventController;
import common.StatusFile;
import common.TempFileClass;
import common.message.CommandMessage;
import common.message.FileMessage;
import common.message.InfoFileClass;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import swing.MainWindow;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class InputDownloadFileHandler extends ChannelInboundHandlerAdapter {

    private String pathFile;
    private String fileName;
    private String userName;
    private InfoFileClass fileData;
    private BufferedOutputStream out;
    private ClientEventController eventController;
    private final int PART_STOP = 100;

    public InputDownloadFileHandler(String pathFile, String userName, InfoFileClass fileData, ClientEventController eventController) {
        this.pathFile = pathFile;
        this.userName = userName;
        this.fileData = fileData;
        this.fileName = fileData.getFileName();
        this.eventController = eventController;
        createFileTemp();
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FileMessage message = (FileMessage) msg;
        out = new BufferedOutputStream(new FileOutputStream(pathFile + userName + "/" + fileName, true));
        out.write(message.data);
        out.flush();

        if (message.partNumber % PART_STOP == 0L) {
            ctx.writeAndFlush(new CommandMessage(CommandMessage.Command.FILE_DOWNLOAD_NEXT_PART, message.partNumber));
        }
        if (message.partNumber == message.partsCount) {
            out.close();
            if (eventController == null) {
                ctx.writeAndFlush(new CommandMessage(CommandMessage.Command.FILE_UPLOAD_COMPLETED));
                deleteTempFile();
            } else {
                eventController.fileUploadCompleted();
            }
            ctx.close();
        }
        out.close();
    }

    private void createFileTemp () {
        if (eventController==null) {
            TempFileClass tmp = new TempFileClass(fileData, userName);
            try {
                tmp.createTmp();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteTempFile () {
        TempFileClass tmp = new TempFileClass(fileData, userName);
        tmp.deleteTmp();
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        out.close();
        ctx.close();
        cause.printStackTrace();
    }
}
