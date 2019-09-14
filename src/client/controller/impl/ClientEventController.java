package client.controller.impl;
import client.Network;
import client.auth.AuthException;
import common.StatusFile;
import common.message.*;
import swing.MainWindow;

public class ClientEventController {
    private MainWindow parent;
    private Network network;
    private InfoFileClass fileData;
    private ImplClientController implClientController;

    public ClientEventController (ImplClientController implClientController) {
        this.implClientController = implClientController;
        this.parent = implClientController.getParent();
        this.network = new Network(this);
    }

    public ClientEventController (ImplClientController implClientController, InfoFileClass fileData) {
        this.implClientController = implClientController;
        this.fileData = fileData;
        this.parent = implClientController.getParent();
        this.network = new Network(this);
        selectModeNetwork();
    }

    public void sendMessage (AbstractMessage message) {
        network.sendMessage(message);
    }

    private void selectModeNetwork () {
        if (fileData.getStatus() == StatusFile.SEND) {
            sendFileOnServer();
        }else if (fileData.getStatus() == StatusFile.DOWNLOAD) {
            downloadFileFromServer();
        }
    }

    private void sendFileOnServer () {
        network.changePipeline();
        network.sendMessage(new NewChanelForSendFileMessage(parent.getUserName()));
        network.sendMessage(new CommandMessage(CommandMessage.Command.ADD, fileData.getFileName(), fileData.getSize()));
    }

    private void downloadFileFromServer () {
        network.changePipeline();
        network.sendMessage(new NewChanelForSendFileMessage(parent.getUserName()));
        network.sendMessage(new CommandMessage(CommandMessage.Command.DOWNLOAD, fileData.getFileName(), fileData.getSize()));
        network.changeHandlerForDownloadFile(parent.getUserName(), fileData.getFileName(), fileData.getSize());
    }

    public void stop () {
        network.stop();
        try {
            network.getThread().join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public InfoFileClass getFileData () {
        return fileData;
    }

    public void tryRemoveEventControllerFromListDownload () {
        if (fileData!=null) {
            implClientController.removeEventControllerFromListDownload(fileData.getFileName());
        }
    }

    public void authorizationIsFailed () {
        parent.getLoginDialog().setConnected(false);
        parent.getLoginDialog().tryCloseLoginDialog(new AuthException());
    }

    public void authorizationIsPass (String userName) {
        parent.getLoginDialog().setConnected(true);
        parent.getLoginDialog().tryCloseLoginDialog(null);
        parent.setUserName(userName);
    }

    public void updateListFiles (ListFilesMessage listFilesMessage) {
        parent.getTableFiles().updateTable(listFilesMessage.getArr());
    }

    public void initDownloadFileOnTable () {
        String[][] arr = {{fileData.getFileName(), "Download..."}};
        parent.getTableFiles().updateTable(arr);
    }

    public void updateListFilesAfterDownload () {
        System.out.println("Загрузка файла завершена");
        parent.getTableFiles().removeFile(parent.searchEqualsFileName(fileData.getFileName()));
        String[][] arr = {{fileData.getFileName(), fileData.getSize() + " byte"}};
        parent.getTableFiles().updateTable(arr);
    }

    public void tryToChangeFile () {
        Integer idx = parent.getDialogWindow().showMessageFileExist(fileData.getFileName());
        if (idx != null) {
            sendMessage(new CommandMessage(CommandMessage.Command.DELETE, fileData.getFileName(), idx));
        } else {
            stop();
        }
    }

    public void deleteFileFromFilesList (Integer idx) {
        parent.getTableFiles().removeFile(idx);
    }

    public void showErrorMessage (Throwable cause) {
        parent.getDialogWindow().messageError(cause);
    }

    public void fileUploadCompleted () {
        parent.getDialogWindow().showMessageDownloadCompleted(fileData.getFileName());
    }

}
