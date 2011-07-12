package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;
import java.util.List;


public class HostUsageRecord implements Serializable
{

    private final static long serialVersionUID = 532512316L;
    protected List<HostUsage> hostUsages;
    protected Integer hostId;

    public List<HostUsage> getHostUsages() {
        return hostUsages;
    }

    public void setHostUsages(List<HostUsage> hostUsages) {
        this.hostUsages = hostUsages;
    }

    public Integer getHostId() {
        return hostId;
    }

    public void setHostId(Integer hostId) {
        this.hostId = hostId;
    }
}


