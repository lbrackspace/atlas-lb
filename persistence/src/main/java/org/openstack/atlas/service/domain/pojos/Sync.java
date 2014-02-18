package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;

public class Sync implements Serializable {
    private final static long serialVersionUID = 532512316L;
    private Integer loadBalancerId;
    private Integer accountId;
    private SyncLocation locationToSyncFrom;

    public Integer getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public SyncLocation getLocationToSyncFrom() {
        return locationToSyncFrom;
    }

    public void setLocationToSyncFrom(SyncLocation locationToSyncFrom) {
        this.locationToSyncFrom = locationToSyncFrom;
    }
}
