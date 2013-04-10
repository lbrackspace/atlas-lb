package org.openstack.atlas.usagerefactor.helpers;

public class HostIdLoadbalancerIdKey {

    private int hostId;
    private int loadBalancerId;

    public HostIdLoadbalancerIdKey(){}

    public HostIdLoadbalancerIdKey(int hostId, int loadBalancerId){
        setHostId(hostId);
        setLoadBalancerId(loadBalancerId);
    }

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public int getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(int loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HostIdLoadbalancerIdKey)) return false;

        HostIdLoadbalancerIdKey that = (HostIdLoadbalancerIdKey) o;

        if (hostId != that.hostId) return false;
        if (loadBalancerId != that.loadBalancerId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = hostId;
        result = 31 * result + loadBalancerId;
        return result;
    }
}
