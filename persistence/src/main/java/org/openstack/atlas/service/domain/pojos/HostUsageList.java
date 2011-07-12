package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HostUsageList implements Serializable {
    private final static long serialVersionUID = 532512316L;
    protected List<HostUsageRecord> hostUsageRecords;

    public List<HostUsageRecord> getHostUsageRecords() {
        if (hostUsageRecords == null) return new ArrayList<HostUsageRecord>();
        return hostUsageRecords;
    }

    public void setHostUsageRecords(List<HostUsageRecord> hostUsageRecords) {
        this.hostUsageRecords = hostUsageRecords;
    }
}

