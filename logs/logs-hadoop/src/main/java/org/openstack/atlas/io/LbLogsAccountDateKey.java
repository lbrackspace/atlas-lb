package org.openstack.atlas.io;

import org.openstack.atlas.hadoop.deprecated.DateTime;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LbLogsAccountDateKey implements WritableComparable<LbLogsAccountDateKey> {
    private static Log LOG = LogFactory.getLog(LbLogsAccountDateKey.class);
    
    private String accountId;
    private String loadBalancerId;
    private String date;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(String loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    @Override
    public int compareTo(LbLogsAccountDateKey o) {
        int i = Long.valueOf(accountId).compareTo(Long.valueOf(o.getAccountId()));
        if(i != 0) return i;

        int j = Long.valueOf(loadBalancerId).compareTo(Long.valueOf(o.getLoadBalancerId()));
        if(j != 0) return j;

        return smartDateComparison(o);
    }

    private int smartDateComparison(LbLogsAccountDateKey o) {
        DateTime current, other;
        try {
            current = new DateTime(date, DateTime.ISO);
        } catch (Exception e) {
            LOG.error(e);
            return -1;
        }
        try {
            other = new DateTime(o.getDate(), DateTime.ISO);
        } catch (Exception e) {
            LOG.error(e);
            return 1;
        }
//        return other.getCalendar().compareTo(current.getCalendar());
        return current.getCalendar().compareTo(other.getCalendar());
    }

    public void write(DataOutput out) throws IOException {
        out.writeUTF(accountId);
        out.writeUTF(loadBalancerId);
        out.writeUTF(date);
    }

    public void readFields(DataInput in) throws IOException {
        accountId = in.readUTF();
        loadBalancerId = in.readUTF();
        date = in.readUTF();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LbLogsAccountDateKey) {
            LbLogsAccountDateKey other = (LbLogsAccountDateKey) obj;
            return compareTo(other) == 0;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return accountId + ":" + loadBalancerId + ":" + date;
    }
}