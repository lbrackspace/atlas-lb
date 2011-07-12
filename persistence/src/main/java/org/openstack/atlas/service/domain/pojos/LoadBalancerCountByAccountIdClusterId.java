package org.openstack.atlas.service.domain.pojos;
import java.io.Serializable;

public class LoadBalancerCountByAccountIdClusterId implements Serializable{
    private final static long serialVersionUID = 532512316L;
    private Integer accountId;
    private Integer clusterId;
    private Long loadBalancerCount;

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }

    public Long getLoadBalancerCount() {
        return loadBalancerCount;
    }

    public void setLoadBalancerCount(Long loadBalancerCount) {
        this.loadBalancerCount = loadBalancerCount;
    }

}
