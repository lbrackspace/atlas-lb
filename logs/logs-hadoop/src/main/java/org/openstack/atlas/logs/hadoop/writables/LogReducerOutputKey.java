package org.openstack.atlas.logs.hadoop.writables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;

public class LogReducerOutputKey implements WritableComparable<LogReducerOutputKey> {

    private int accountId;
    private int loadbalancerId;

    @Override
    public String toString() {
        return "LogReducerKey{" + "accountId=" + accountId + "loadbalancerId=" + loadbalancerId + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LogReducerOutputKey other = (LogReducerOutputKey) obj;
        if (this.accountId != other.accountId) {
            return false;
        }
        if (this.loadbalancerId != other.loadbalancerId) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.accountId;
        hash = 53 * hash + this.loadbalancerId;
        return hash;
    }

    @Override
    public void write(DataOutput d) throws IOException {
        d.writeInt(accountId);
        d.writeInt(loadbalancerId);
    }

    @Override
    public void readFields(DataInput di) throws IOException {
        accountId = di.readInt();
        loadbalancerId = di.readInt();
    }

    @Override
    public int compareTo(LogReducerOutputKey o) {
        int oAccountId = o.getAccountId();
        int oLoadbalancerId = o.getLoadbalancerId();
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
}
