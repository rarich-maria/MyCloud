package swing;
import client.controller.impl.ImplClientController;
import common.message.InfoFileClass;
import common.StatusFile;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class MainWindow extends JFrame {
    private final String CLIENT_STORAGE = "client_storage/";
    private JButton downloadFile;
    private JButton addNewFileOnServer;
    private JButton deleteFile;
    private JButton myStorage;
    private JFileChooser fileChooserForDownloadOnServer;
    private JPanel buttonPanel;
    private ModelListFile table;
    private ImplClientController clientController;
    private LoginDialog loginDialog;
    private String userName;
    private MessageDialogWindow dialogWindow;

    public MainWindow() {
        setTitle("Облачное хранилище");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setBounds(200, 200, 500, 500);
        setLayout(new BorderLayout());
        buttonPanel = new JPanel();
        Box contents = new Box(BoxLayout.Y_AXIS);
        table = new ModelListFile();
        contents.add(new JScrollPane(table.getTable()));
        add(contents, BorderLayout.CENTER);

        downloadFile = new JButton("Скачать");
        addNewFileOnServer = new JButton("Загрузить");
        deleteFile = new JButton("Удалить");
        myStorage = new JButton("Загрузки");
        fileChooserForDownloadOnServer = new JFileChooser();

        GridLayout layout = new GridLayout(0, 4, 5, 12);
        buttonPanel.setLayout(layout);
        buttonPanel.add(downloadFile);
        buttonPanel.add(addNewFileOnServer);
        buttonPanel.add(deleteFile);
        buttonPanel.add(myStorage);
        add(buttonPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (clientController.stop()) {
                        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                        super.windowClosing(e);
                    } else {
                        System.out.println("продолжить загрузку");
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });

        setVisible(true);
        dialogWindow = new MessageDialogWindow(this);
        clientController = new ImplClientController(this);
        clientController.start();
        loginDialog = new LoginDialog(this, clientController);
        loginDialog.setVisible(true);

        if (!loginDialog.isConnected()) {
            System.exit(0);
        }

        downloadFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int idx = table.getTable().getSelectedRow();
                if (idx == -1) {
                    dialogWindow.messageDialogRowNotSelected("Выберите файл для скачивания");
                } else {
                    checkDirectory();
                    String fileName = (String) table.getTable().getValueAt(idx, 0);
                    long size = table.getFileSize(idx);
                    File localFileExist = new File(CLIENT_STORAGE + userName + "/" + fileName);
                    if (localFileExist.exists()) {
                        tryToChangeExistingFile(fileName, size, localFileExist);
                    } else {
                        newNetworkForDownloadFile(fileName, size);
                    }
                }
            }
        });

        deleteFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Integer idx = table.getTable().getSelectedRow();
                if (idx == -1) {
                    dialogWindow.messageDialogRowNotSelected("Выберите файл для удаления");
                } else {
                    clientController.deleteFile((String) table.getTable().getValueAt(idx, 0), idx);
                }
            }
        });

        addNewFileOnServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fileChooserForDownloadOnServer.setDialogTitle("Выбор файла");
                fileChooserForDownloadOnServer.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fileChooserForDownloadOnServer.showOpenDialog(MainWindow.this);
                if (fileChooserForDownloadOnServer.getSelectedFile() != null) {
                    InfoFileClass fileData = new InfoFileClass(fileChooserForDownloadOnServer.getSelectedFile().getPath(),
                              fileChooserForDownloadOnServer.getSelectedFile().getName(),
                              fileChooserForDownloadOnServer.getSelectedFile().length(),
                              StatusFile.SEND);
                    clientController.startFileNetwork(fileData);
                }
            }
        });

        myStorage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(
                          chooser.getFileSystemView().getParentDirectory(
                                    new File(CLIENT_STORAGE + userName)));
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.showDialog(myStorage, "Open file");
                try {
                    File file = new File (chooser.getSelectedFile().getPath());
                    openFile(file);
                }catch (NullPointerException ex) {
                    System.out.println("Файл не выбран");
                    ex.printStackTrace();
                }
            }
        });
    }

    private void openFile(File file) {
        Desktop desktop = null;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }
        try {
            desktop.open(file);
        } catch (Exception ioe) {
            System.out.println("Exception Desktop");
            ioe.printStackTrace();
        }
    }

    private void tryToChangeExistingFile(String fileName, long size, File fileExist) {
        if (dialogWindow.showMessageForLocalStorage(fileName)) {
            if (fileExist.delete()) {
                newNetworkForDownloadFile(fileName, size);
            }
        } else {
            System.out.println("Выбрана отмена");
        }
    }

    private void checkDirectory() {
        File dir = new File(CLIENT_STORAGE + userName);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public int searchEqualsFileName(String fileName) {
        return table.searchEqualsFileName(fileName);
    }

    public LoginDialog getLoginDialog() {
        return loginDialog;
    }

    public ModelListFile getTableFiles() {
        return table;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String name) {
        this.userName = name;
    }

    private void newNetworkForDownloadFile(String fileName, long size) {
        InfoFileClass infoFile = new InfoFileClass(CLIENT_STORAGE + userName + "/" + fileName, fileName, size, StatusFile.DOWNLOAD);
        clientController.startFileNetwork(infoFile);
    }

    public MessageDialogWindow getDialogWindow() {
        return dialogWindow;
    }
}
