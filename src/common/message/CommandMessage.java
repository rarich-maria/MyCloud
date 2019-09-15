package common.message;

public class CommandMessage extends AbstractMessage {
    public enum Command {
        DELETE, ADD, DOWNLOAD, FILE_EXIST_TRUE, FILE_EXIST_FALSE, STOP, FILE_DOWNLOAD_NEXT_PART,
        FILE_UPLOAD_COMPLETED, DELETE_ALL_TEMP_FILES, RELOADING_FILE
    }

    private Command command;
    private String path;
    private boolean result;
    private Integer idx;
    private long size;
    private Long currentSize;
    private InfoFileClass fileData;

    public CommandMessage (Command command) {
        this.command = command;
    }

    public CommandMessage (Command command, String path, Integer idx) {
        this.command = command;
        this.path = path;
        this.idx = idx;
    }

    public CommandMessage (Command command, Integer idx) {
        this.command = command;
        this.idx = idx;
    }

    public CommandMessage (Command command, InfoFileClass fileData) {
        this.command = command;
        this.fileData = fileData;
    }
    public CommandMessage (Command command, Integer idx, Long currentSize) {
        this.command = command;
        this.idx = idx;
        this.currentSize = currentSize;
    }

    public CommandMessage (Command command, String path, long size) {
        this.command = command;
        this.path = path;
        this.size = size;
    }

    public CommandMessage (Command command, boolean result, Integer idx) {
        this.command = command;
        this.result = result;
        this.idx = idx;
    }

    public String getPath() {
        return path;
    }

    public Command getCommand() {
        return command;
    }

    public boolean isResult() {
        return result;
    }

    public Integer getIdx() {
        return idx;
    }

    public long getSize() {
        return size;
    }

    public Long getCurrentSize() {
        return currentSize;
    }

    public InfoFileClass getFileData() {
        return fileData;
    }
}
