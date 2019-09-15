package common.message;
import common.TempFileClass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListTempFilesMessage extends AbstractMessage {
    private final String SERVER_STORAGE = "server_storage/";
    private final String SERVER_TMP = "tmp/";
    private List <InfoFileClass> listTemp;
    private String path;

    public ListTempFilesMessage (String userName) throws IOException {
        path = SERVER_STORAGE + userName +"/" + SERVER_TMP;
        listTemp = new ArrayList<>();
        listTemp = getInfoTemp();
    }

    private List<InfoFileClass> getInfoTemp () throws IOException {
        File myFiles = new File(path);
        if (!myFiles.exists()) {
            System.out.println("Файлы не загружались на сервер");
            return listTemp;
        }
        File[] tmpFiles = myFiles.listFiles();
        if (tmpFiles.length!=0) {
            for (int i=0; i< tmpFiles.length; i++) {
                listTemp.add(new TempFileClass(tmpFiles[i].getPath()).readFileDataFromTmp());
            }
        }else {
            return listTemp;
        }
        return listTemp;
    }

    public List<InfoFileClass> getListTemp () {
        return listTemp;
    }
}
