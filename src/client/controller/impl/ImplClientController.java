package client.controller.impl;
import client.controller.ClientController;
import common.StatusFile;
import common.message.AuthMessage;
import common.message.CommandMessage;
import common.message.InfoFileClass;
import swing.MainWindow;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImplClientController implements ClientController {
    private final String CLIENT_STORAGE = "client_storage/";
    private MainWindow parent;
    private Map<String, ClientEventController> listDownload = new HashMap();
    private Map<String, StatusFile> listForDeleteFile = new HashMap();
    private ClientEventController eventController;

    public ImplClientController (MainWindow parent){
        this.parent = parent;
    }

    @Override
    public void start() {
        eventController = new ClientEventController(this);
    }

    @Override
    public boolean stop () throws InterruptedException {
        if (listDownload.isEmpty()){
            eventController.stop();
            return true;
        }else {
            if (parent.getDialogWindow().showMessageForCloseWindow()) {
                exitAndDeleteFile();
                eventController.stop();
                return true;
            }else {
                System.out.println("Отмена выхода из приложения");
            }
        }
        return false;
    }

    public void startFileNetwork (InfoFileClass fileData) {
        ClientEventController fileEventController = new ClientEventController(this, fileData);
        listDownload.put(fileData.getFileName(), fileEventController);
    }

    public void startReloadingFileNetwork (InfoFileClass fileData, Long currentSize) {
        ClientEventController fileEventController = new ClientEventController(this, fileData, currentSize);
        listDownload.put(fileData.getFileName(), fileEventController);
    }

    public void tryAuthorization (String userName, String password) {
        eventController.sendMessage(new AuthMessage(userName, password));
    }

    private void exitAndDeleteFile () {
        for (ClientEventController controller : listDownload.values()) {
            listForDeleteFile.put(controller.getFileData().getFileName(), controller.getFileData().getStatus());
            controller.stop();
        }
        deleteNotCompletedFiles();
        System.out.println("Выход из программы и удаление файлов");
    }

    private void deleteNotCompletedFiles() {
        if (listDownload.isEmpty()) {
            for (Map.Entry st : listForDeleteFile.entrySet()) {
                if (st.getValue() == StatusFile.SEND) {
                    eventController.sendMessage(new CommandMessage(CommandMessage.Command.DELETE, st.getKey().toString(), null));
                } else if (st.getValue() == StatusFile.DOWNLOAD) {
                    File localFileExist = new File(CLIENT_STORAGE + parent.getUserName() + "/" + st.getKey().toString());
                    deleteFileOnClientStorage(localFileExist);
                }
            }
        }
    }

    private void deleteFileOnClientStorage(File localFileExist) {
        if (localFileExist.exists()) {
            if (localFileExist.delete()) {
                System.out.println("File delete on client");
            }
        } else {
            System.out.println("file dont exist");
        }
    }

    public void deleteFile (String fileName, Integer idx) {
        if (!listDownload.containsKey(fileName)) {
            eventController.sendMessage(new CommandMessage(CommandMessage.Command.DELETE, fileName, idx));
        }else {
            if (parent.getDialogWindow().showMessageDeleteDownloadFile(fileName)) {
                listDownload.get(fileName).stop();
                eventController.sendMessage(new CommandMessage(CommandMessage.Command.DELETE, fileName, idx));
            }else {
                System.out.println("Выбрана отмена удаления загружаемого фала");
            }
        }
    }

    public void startReloadingFiles (List<InfoFileClass> listUnloadedFiles) {
        for (InfoFileClass info: listUnloadedFiles) {
            startReloadingFileNetwork(info.getInfoFile(), info.getCurrentSize());
        }
    }

    public void removeEventControllerFromListDownload (String fileName) {
        listDownload.remove(fileName);
    }

    public MainWindow getParent() {
        return parent;
    }
}
