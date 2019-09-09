package common;

import java.io.File;

public class ListFilesMessage extends AbstractMessage {

    private String [][] arr;
    private final String SERVER_STORAGE = "server_storage/";

    public ListFilesMessage (String pathFile) {
        arr = data(pathFile);
    }

    private String [][] data (String pathFile) {
        File myFiles = new File(SERVER_STORAGE+pathFile);
        if (!myFiles.exists()) {
            myFiles.mkdir();
        }
        File[] files = myFiles.listFiles();

        if (files.length!=0) {
            arr = new String[files.length][2];
            for (int i=0; i<files.length; i++){
                arr[i][0]=files[i].getName();
                arr[i][1]=files[i].length() + " byte";
            }
            return arr;
        } else {
            arr = null;
        }
        return arr;
    }

    public String[][] getArr() {
        return arr;
    }
}
