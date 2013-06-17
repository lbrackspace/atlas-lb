package org.openstack.atlas.logs.hadoop.writables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

public class LogMapperOutputValue implements WritableComparable<LogMapperOutputValue> {

    @Override
    public String toString() {
        return "LogMapperOutputValue{accountId=" + accountId
                + ", loadbalancerId=" + loadbalancerId
                + ", date=" + date
                + ", loadbalancerName=" + loadbalancerName
                + ", sourceIp=" + sourceIp
                + ", logLine=" + logLine + '}';
    }
    private int accountId;
    private int loadbalancerId;
    private long date;
    private String loadbalancerName;
    private String sourceIp;
    private String logLine;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LogMapperOutputValue other = (LogMapperOutputValue) obj;
        if (this.accountId != other.accountId) {
            return false;
        }
        if (this.loadbalancerId != other.loadbalancerId) {
            return false;
        }
        if ((this.loadbalancerName == null) ? (other.loadbalancerName != null) : !this.loadbalancerName.equals(other.loadbalancerName)) {
            return false;
        }
        if ((this.sourceIp == null) ? (other.sourceIp != null) : !this.sourceIp.equals(other.sourceIp)) {
            return false;
        }
        if ((this.logLine == null) ? (other.logLine != null) : !this.logLine.equals(other.logLine)) {
            return false;
        }
        if (this.date != other.date) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + this.accountId;
        hash = 43 * hash + this.loadbalancerId;
        hash = 43 * hash + (this.loadbalancerName != null ? this.loadbalancerName.hashCode() : 0);
        hash = 43 * hash + (this.sourceIp != null ? this.sourceIp.hashCode() : 0);
        hash = 43 * hash + (this.logLine != null ? this.logLine.hashCode() : 0);
        hash = 43 * hash + (int) (this.date ^ (this.date >>> 32));
        return hash;
    }

    @Override
    public void write(DataOutput d) throws IOException {
        int nullFlags = ((loadbalancerName == null) ? 0 : 1) + ((sourceIp == null) ? 0 : 2) + ((logLine == null) ? 0 : 4);

        d.writeByte(nullFlags);

        d.writeInt(accountId);
        d.writeInt(loadbalancerId);
        d.writeLong(date);

        if ((nullFlags & 1) > 0) {
            d.writeUTF(loadbalancerName);
        }
        if ((nullFlags & 2) > 0) {
            d.writeUTF(sourceIp);
        }
        if ((nullFlags & 4) > 0) {
            d.writeUTF(logLine);
        }
    }

    @Override
    public void readFields(DataInput di) throws IOException {
        int nullFlags = di.readByte();

        accountId = di.readInt();
        loadbalancerId = di.readInt();
        date = di.readLong();

        if ((nullFlags & 1) > 0) {
            loadbalancerName = di.readUTF();
        } else {
            loadbalancerName = null;
        }
        if ((nullFlags & 2) > 0) {
            sourceIp = di.readUTF();
        } else {
            sourceIp = null;
        }
        if ((nullFlags & 4) > 0) {
            logLine = di.readUTF();
        } else {
            logLine = null;
        }

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

    public String getLoadbalancerName() {
        return loadbalancerName;
    }

    public void setLoadbalancerName(String loadbalancerName) {
        this.loadbalancerName = loadbalancerName;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getLogLine() {
        return logLine;
    }

    public void setLogLine(String logLine) {
        this.logLine = logLine;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public int compareTo(LogMapperOutputValue o) {
        long oDate = o.getDate();
        if (date > oDate) {
            return 1;
        }
        if (date < oDate) {
            return -1;
        }
        return 0;
    }
}
