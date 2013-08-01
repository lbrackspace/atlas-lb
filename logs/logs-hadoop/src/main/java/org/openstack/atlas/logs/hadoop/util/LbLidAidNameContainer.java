package org.openstack.atlas.logs.hadoop.util;

// An embasy object so that Hadoop logs doesn't need the atlas persistence module.
import java.util.Comparator;

public class LbLidAidNameContainer {

    private int accountId;
    private int loadbalancerId;
    private String name;

    @Override
    public String toString() {
        return "{accountId=" + accountId
                + ", loadbalancerId=" + loadbalancerId
                + ", name=" + name
                + "}";
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
