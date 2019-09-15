package common;

import common.message.InfoFileClass;

import java.io.*;

public class TempFileClass {
    private final String SERVER_STORAGE = "server_storage/";
    private final String SERVER_TMP = "tmp/";
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
        String path = SERVER_STORAGE+ userName +"/" + SERVER_TMP + fileData.getFileName() + ".mytmp";
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
            result = (InfoFileClass) objectinputstream.readObject();
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
        String path = SERVER_STORAGE+ userName +"/" + SERVER_TMP + fileData.getFileName() + ".mytmp";
        File file = new File (path);
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
}
