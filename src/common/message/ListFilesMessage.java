package common.message;
import java.io.File;
import java.util.Arrays;

public class ListFilesMessage extends AbstractMessage {
    private final String SERVER_STORAGE = "server_storage/";
    private final String BYTE = " byte";
    private String [][] arr;

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
                if (files[i].isFile()) {
                    arr[i][0]=files[i].getName();
                    arr[i][1]=files[i].length() + BYTE;
                }else if (files[i].isDirectory()) {
                    arr = Arrays.copyOfRange(arr, 0, files.length-1);
                }
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
