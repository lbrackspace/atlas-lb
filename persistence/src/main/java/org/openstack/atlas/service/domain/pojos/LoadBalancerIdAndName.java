package org.openstack.atlas.service.domain.pojos;

public class LoadBalancerIdAndName implements Comparable<LoadBalancerIdAndName>{


    private int loadbalancerId;
    private int accountId;
    private String name;

    @Override
    public String toString() {
        return "LoadBalancerIdsAndNames{loadbalancerId=" + loadbalancerId
                + ", accoundId=" + accountId
                + ", name=" + name
                + "}";
    }


    public LoadBalancerIdAndName() {
    }

    public int getLoadbalancerId() {
        return loadbalancerId;
    }

    public void setLoadbalancerId(int loadbalancerId) {
        this.loadbalancerId = loadbalancerId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(LoadBalancerIdAndName o) {
        int oLoadBalancerId = o.getLoadbalancerId();
        int oAccountId = o.getAccountId();

        if(loadbalancerId<oLoadBalancerId){
            return -1;
        }
        if(loadbalancerId>oLoadBalancerId){
            return 1;
        }
        if(accountId<oAccountId){
            return -1;
        }
        if(accountId>oAccountId){
            return 1;
        }
        return 0;
    }


    // Only compare the ids. This is for use as a key.
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LoadBalancerIdAndName other = (LoadBalancerIdAndName) obj;
        if (this.loadbalancerId != other.loadbalancerId) {
            return false;
        }
        if (this.accountId != other.accountId) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + this.loadbalancerId;
        hash = 17 * hash + this.accountId;
        hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
