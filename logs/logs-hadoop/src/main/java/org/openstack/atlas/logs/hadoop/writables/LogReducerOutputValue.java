package org.openstack.atlas.logs.hadoop.writables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;

public class LogReducerOutputValue implements WritableComparable<LogReducerOutputValue> {

    private int accountId;
    private int loadbalancerId;
    private int nLines;
    private long crc;
    private long fileSize;
    private String logFile;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LogReducerOutputValue other = (LogReducerOutputValue) obj;
        if (this.accountId != other.accountId) {
            return false;
        }
        if (this.loadbalancerId != other.loadbalancerId) {
            return false;
        }
        if (this.nLines != other.nLines) {
            return false;
        }
        if (this.crc != other.crc) {
            return false;
        }
        if (this.fileSize != other.fileSize) {
            return false;
        }
        if ((this.logFile == null) ? (other.logFile != null) : !this.logFile.equals(other.logFile)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.accountId;
        hash = 89 * hash + this.loadbalancerId;
        hash = 89 * hash + this.nLines;
        hash = 89 * hash + (int) (this.crc ^ (this.crc >>> 32));
        hash = 89 * hash + (int) (this.fileSize ^ (this.fileSize >>> 32));
        hash = 89 * hash + (this.logFile != null ? this.logFile.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "LogReducerValue{accountId=" + accountId
                + ", loadbalancerId=" + loadbalancerId
                + ", nLines=" + nLines
                + ", crc=" + crc
                + ", fileSize=" + fileSize
                + ", logFile=" + logFile + '}';
    }

    @Override
    public void write(DataOutput d) throws IOException {
        int nullFlags = ((logFile == null) ? 0 : 1);
        d.writeByte(nullFlags);
        d.writeInt(accountId);
        d.writeInt(loadbalancerId);
        d.writeInt(nLines);
        d.writeLong(crc);
        d.writeLong(fileSize);

        if ((nullFlags & 1) > 0) {
            d.writeUTF(logFile);
        }

    }

    @Override
    public void readFields(DataInput di) throws IOException {
        int nullFlags = di.readByte();

        accountId = di.readInt();
        loadbalancerId = di.readInt();
        nLines = di.readInt();
        crc = di.readLong();
        fileSize = di.readLong();

        if ((nullFlags & 1) > 0) {
            logFile = di.readUTF();
        } else {
            logFile = null;
        }


    }

    @Override
    public int compareTo(LogReducerOutputValue o) {
        long oFileSize = o.getFileSize();
        if (fileSize > oFileSize) {
            return 1;
        }
        if (fileSize < oFileSize) {
            return -1;
        }
        return 0;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getLoadbalancerId() {
        return loadbalancerId;
    }

    public void setLoadbalancerId(int loadbalancerId) {
        this.loadbalancerId = loadbalancerId;
    }

    public int getnLines() {
        return nLines;
    }

    public void setnLines(int nLines) {
        this.nLines = nLines;
    }

    public long getCrc() {
        return crc;
    }

    public void setCrc(long crc) {
        this.crc = crc;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
