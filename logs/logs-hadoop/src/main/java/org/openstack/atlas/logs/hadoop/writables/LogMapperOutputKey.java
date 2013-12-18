package org.openstack.atlas.logs.hadoop.writables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;

public class LogMapperOutputKey implements WritableComparable<LogMapperOutputKey> {

    private int accountId;
    private int loadbalancerId;
    private long date;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LogMapperOutputKey other = (LogMapperOutputKey) obj;
        if (this.accountId != other.accountId) {
            return false;
        }
        if (this.loadbalancerId != other.loadbalancerId) {
            return false;
        }
        if (this.date != other.date) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + this.accountId;
        hash = 61 * hash + this.loadbalancerId;
        hash = 61 * hash + (int) (this.date ^ (this.date >>> 32));
        return hash;
    }

    @Override
    public void write(DataOutput d) throws IOException {
        d.writeInt(accountId);
        d.writeInt(loadbalancerId);
        d.writeLong(date);
    }

    @Override
    public void readFields(DataInput di) throws IOException {
        accountId = di.readInt();
        loadbalancerId = di.readInt();
        date = di.readLong();
    }

    @Override
    public int compareTo(LogMapperOutputKey o) {
        int oAccountId = o.getAccountId();
        int oLoadbalancerId = o.getLoadbalancerId();
        long oDate = o.getDate();
        if (accountId > oAccountId) {
            return 1;
        }
        if (accountId < oAccountId) {
            return -1;
        }
        if (loadbalancerId > oLoadbalancerId) {
            return 1;
        }
        if (loadbalancerId < oLoadbalancerId) {
            return -1;
        }
        if (date > oDate) {
            return 1;
        }
        if (date < oDate) {
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

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
