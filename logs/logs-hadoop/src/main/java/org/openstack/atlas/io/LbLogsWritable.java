package org.openstack.atlas.io;

import org.openstack.atlas.util.DateTime;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Calendar;

public class LbLogsWritable implements WritableComparable<LbLogsWritable> {

    private Integer accountId;
    private Integer loadBalancerId;
    private String loadbalancerName;
    private Calendar date;

    private String sourceIp;

    private String logline;

    public LbLogsWritable() {
        super();
    }

    public LbLogsWritable(Integer accountId, String sourceIp, String loadbalancerName, Integer loadBalancerId, Calendar date, String logline) {
        this.accountId = accountId;
        this.sourceIp = sourceIp;
        this.loadbalancerName = loadbalancerName;
        this.loadBalancerId = loadBalancerId;
        this.date = date;
        this.logline = logline;
    }

    public String getLogline() {
        return logline;
    }

    public void setLogline(String logline) {
        this.logline = logline;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getLoadbalancerName() {
        return loadbalancerName;
    }

    public void setLoadbalancerName(String loadbalancerName) {
        this.loadbalancerName = loadbalancerName;
    }

    public Integer getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Override
    public int compareTo(LbLogsWritable o) {
        Calendar c1;
        Calendar c2;

        try {
            c1 = getDate();
        } catch (Exception e) {
            return -1;
        }
        try {
            c2 = o.getDate();
        } catch (Exception e) {
            return 1;
        }
        return c1.compareTo(c2);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(getAccountId());
        out.writeUTF(getSourceIp());
                out.writeUTF(getLoadbalancerName());
        out.writeInt(getLoadBalancerId());
        out.writeUTF(new DateTime(getDate()).getIso());
        out.writeUTF(getLogline());

    }

    @Override
    public void readFields(DataInput in) throws IOException {
        accountId = in.readInt();
        sourceIp = in.readUTF();
        loadbalancerName = in.readUTF();
        loadBalancerId = in.readInt();
        date = new DateTime(in.readUTF(), DateTime.ISO).getCalendar();
        logline = in.readUTF();
    }

    public String toString() {
        return getAccountId() + ":" + getLoadBalancerId() + ":" + getLoadbalancerName() + ":" + getDate();
    }
}