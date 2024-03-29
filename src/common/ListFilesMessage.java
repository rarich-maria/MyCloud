package common;

import java.io.File;

public class ListFilesMessage extends AbstractMessage {

    private String [][] arr;

    public ListFilesMessage (String pathFile) {
        arr = data(pathFile);
    }

    private String [][] data (String pathFile) {
        File myFiles = new File("server_storage/"+pathFile);
        if (!myFiles.exists()) {
            myFiles.mkdir();
        }
        File[] files = myFiles.listFiles();
        if (files.length!=0) {
            String arr [][] = new String[files.length][2];

            for (int i=0; i<files.length; i++){
                arr[i][0]=files[i].getName();
                arr[i][1]=files[i].length() + " byte";
                System.out.println(arr[i][0] + "  " + arr[i][1]);
            }
            return arr;
        }else {
            arr = null;
            System.out.println("файлов нет");
        }


        return arr;
    }

    public String[][] getArr() {
        return arr;
    }
}
