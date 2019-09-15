package common.message;

import common.StatusFile;

import java.io.Serializable;

public class InfoFileClass implements Serializable {

    private String path;
    private String fileName;
    private long size;
    private Long currentSize;
    private StatusFile status;
    private InfoFileClass infoFile;

    public InfoFileClass (String path, String fileName, long size, StatusFile status) {
        this.path = path;
        this.fileName = fileName;
        this.size = size;
        this.status = status;
    }

    public InfoFileClass (InfoFileClass infoFile, Long currentSize) {
        this.infoFile = infoFile;
        this.currentSize = currentSize;
    }

    public long getSize() {
        return size;
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }

    public StatusFile getStatus() {
        return status;
    }

    public Long getCurrentSize() {
        return currentSize;
    }

    public InfoFileClass getInfoFile() {
        return infoFile;
    }
}
