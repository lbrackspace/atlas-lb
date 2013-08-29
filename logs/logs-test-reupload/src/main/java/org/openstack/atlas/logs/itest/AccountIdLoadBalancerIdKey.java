package org.openstack.atlas.logs.itest;

public class AccountIdLoadBalancerIdKey {

    private int accountId;
    private int loadbalancerId;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AccountIdLoadBalancerIdKey other = (AccountIdLoadBalancerIdKey) obj;
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
        int hash = 3;
        hash = 79 * hash + this.accountId;
        hash = 79 * hash + this.loadbalancerId;
        return hash;
    }

    @Override
    public String toString() {
        return "{accountId=" + accountId
                + ", loadbalancerId=" + loadbalancerId
                + "}";
    }

    public AccountIdLoadBalancerIdKey() {
        accountId = 0;
        loadbalancerId = 0;
    }

    public AccountIdLoadBalancerIdKey(int accountId, int loadbalancerId) {
        this.accountId = accountId;
        this.loadbalancerId = loadbalancerId;
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
