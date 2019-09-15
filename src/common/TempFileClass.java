package common;

import common.message.InfoFileClass;

import java.io.*;

public class TempFileClass {
    private final String SERVER_STORAGE = "server_storage/";
    private final String SERVER_TMP = "tmp/";
    private final String EXTENSION = ".my.tmp";
    private InfoFileClass fileData;
    private String userName;
    private String path;

    public TempFileClass (InfoFileClass fileData, String userName) {
        this.fileData = fileData;
        this.userName = userName;
    }

    public TempFileClass (String path) {
        this.path = path;
    }

    public void createTmp () throws IOException {
        checkDirectory();
        String path = SERVER_STORAGE+ userName +"/" + SERVER_TMP + fileData.getFileName() + EXTENSION;
        File file = new File(path);
        if (!file.exists()){
            ObjectOutputStream oos = null;
            try{
                FileOutputStream fout = new FileOutputStream(path, true);
                oos = new ObjectOutputStream(fout);
                oos.writeObject(fileData);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if(oos != null){
                    oos.close();
                }
            }
        }else {
            System.out.println("Temp file exist");
        }
    }

    public InfoFileClass readFileDataFromTmp () throws IOException {
        InfoFileClass result = null;
        ObjectInputStream objectinputstream = null;
        try {
            FileInputStream streamIn = new FileInputStream(path);
            objectinputstream = new ObjectInputStream(streamIn);
            InfoFileClass fileInfo = (InfoFileClass) objectinputstream.readObject();
            System.out.println("readFileDataFromTmp fileInfo.getPath()"+ fileInfo.getPath());
            long currentFileSize = getCurrentFileSize(fileInfo.getFileName());
            if (currentFileSize != fileInfo.getSize()) {
                result = new InfoFileClass (fileInfo, currentFileSize);
                System.out.println("result InfoFileClass " + result.getCurrentSize());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(objectinputstream != null){
                objectinputstream.close();
            }
        }
        return result;
    }

    public void deleteTmp () {
        String path = SERVER_STORAGE+ userName +"/" + SERVER_TMP + fileData.getFileName() + EXTENSION;
        File file = new File (path);
        if (file.delete()) {
            System.out.println("Delete tmpFile true");
        }else {
            System.out.println("Delete tmpFile false");
        }
    }

    public void deleteTmp (String fileName) {
        String tempPath = path + "/" + SERVER_TMP + fileName + EXTENSION;
        File file = new File (tempPath);
        if (file.delete()) {
            System.out.println("Delete tmpFile true");
        }else {
            System.out.println("Delete tmpFile false");
        }
    }

    private void checkDirectory() {
        File dir = new File(SERVER_STORAGE + userName+"/" + SERVER_TMP);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    private long getCurrentFileSize (String fileName) {
        String filePath = delimiterPath()+fileName;
        return new File(filePath).length();
    }

    private String delimiterPath () {
        File f = new File(path);
        for (int i=0; i< 2; i++) {
            f = f.getParentFile();
            System.out.println("Parent=" + f.getName());
        }
        System.out.println("f.getPath() " + f.getPath());
        return f.getPath()+"\\";
    }

    public void deleteAllTempFiles () {
        File myFiles = new File(path + "/" + SERVER_TMP);
        File[] tmpFiles = myFiles.listFiles();
        for (int i=0; i< tmpFiles.length; i++) {
            System.out.println(tmpFiles[i].delete());
        }
    }
}
