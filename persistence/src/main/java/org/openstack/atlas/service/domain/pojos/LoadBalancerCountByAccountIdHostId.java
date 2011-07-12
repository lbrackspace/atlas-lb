package org.openstack.atlas.service.domain.pojos;

public class LoadBalancerCountByAccountIdHostId {

    private final static long serialVersionUID = 532512316L;
    private Integer accountId;
    private Integer hostId;
    private Long loadBalancerCount;

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getHostId() {
        return hostId;
    }

    public void setHostId(Integer clusterId) {
        this.hostId = clusterId;
    }

    public Long getLoadBalancerCount() {
        return loadBalancerCount;
    }

    public void setLoadBalancerCount(Long loadBalancerCount) {
        this.loadBalancerCount = loadBalancerCount;
    }
}
