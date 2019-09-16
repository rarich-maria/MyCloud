package client.handlers;
import client.controller.impl.ClientEventController;
import common.handlers.OutSendFileHandler;
import common.message.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientReadMessageHandler extends ChannelInboundHandlerAdapter {
    private ClientEventController eventController;
    private InfoFileClass fileData;

    public ClientReadMessageHandler(ClientEventController eventController) {
        this.eventController = eventController;
        this.fileData = eventController.getFileData();
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        AbstractMessage mes = (AbstractMessage) msg;
        if (mes instanceof ListFilesMessage) {
            ListFilesMessage listFilesMessage = (ListFilesMessage) mes;
            eventController.updateListFiles(listFilesMessage);
        } else if (mes instanceof CommandMessage) {
            CommandMessage command = (CommandMessage) mes;
            readCommandMessage(ctx, command);
        } else if (mes instanceof FileMessage) {
            FileMessage message = (FileMessage) mes;
            ctx.fireChannelRead(message);
        } else if (mes instanceof ListTempFilesMessage) {
            ListTempFilesMessage message = (ListTempFilesMessage) mes;
            eventController.sendListUnloadedFiles(message.getListTemp());
        }
    }

    private void readCommandMessage(ChannelHandlerContext ctx, CommandMessage command) {
        if (command.getCommand() == CommandMessage.Command.DELETE) {
            tryDeleteFile(ctx, command);
        } else if (command.getCommand() == CommandMessage.Command.FILE_EXIST_TRUE) {
            eventController.tryToChangeFile();
        } else if (command.getCommand() == CommandMessage.Command.FILE_EXIST_FALSE) {
            eventController.initDownloadFileOnTable ();
            changeHandlerForSendFile(ctx, fileData.getPath());
        } else if (command.getCommand() == CommandMessage.Command.FILE_DOWNLOAD_NEXT_PART) {
            ctx.channel().writeAndFlush(command);
        } else if (command.getCommand() == CommandMessage.Command.FILE_UPLOAD_COMPLETED) {
            fileUploadCompleted(ctx);
        }
    }

    private void fileUploadCompleted(ChannelHandlerContext ctx) {
        eventController.updateListFilesAfterDownload ();
        ctx.channel().close();
    }

    private void tryDeleteFile(ChannelHandlerContext ctx, CommandMessage command) {
        if (command.isResult()) {
            if (command.getIdx() != null) {
                eventController.deleteFileFromFilesList(command.getIdx());
            }
            if (fileData != null) {
                ctx.writeAndFlush(new CommandMessage(CommandMessage.Command.ADD, fileData));
            }
        } else {
            System.out.println("File delete false");
        }
    }

    public void changeHandlerForSendFile(ChannelHandlerContext ctx, String path) {
        ctx.channel().pipeline().addLast(new ChannelHandler[]{new OutSendFileHandler(path)});
        ctx.channel().writeAndFlush(new CommandMessage(CommandMessage.Command.FILE_DOWNLOAD_NEXT_PART, 0));
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        eventController.showErrorMessage(cause);
        ctx.close();
        cause.printStackTrace();
    }
}
