package common;

import io.netty.buffer.ByteBuf;

public class FileMessage extends AbstractMessage {
    public String filename;
    public int partNumber;
    public int partsCount;
    public ByteBuf buf;

    public FileMessage(String filename, int partNumber, int partsCount, ByteBuf buf) {
        this.filename = filename;
        this.partNumber = partNumber;
        this.partsCount = partsCount;
        this.buf = buf;
    }


}
