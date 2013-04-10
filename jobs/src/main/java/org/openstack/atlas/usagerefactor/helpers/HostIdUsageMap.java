package org.openstack.atlas.usagerefactor.helpers;

import org.openstack.atlas.usagerefactor.SnmpUsage;

import java.util.Map;

public class HostIdUsageMap {
    public Integer hostId;
    public Map<Integer, SnmpUsage> map;

    public HostIdUsageMap(Integer hostId, Map<Integer, SnmpUsage> map) {
        this.hostId = hostId;
        this.map = map;
    }

    public Integer getHostId() {
        return hostId;
    }

    public void setHostId(Integer hostId) {
        this.hostId = hostId;
    }

    public Map<Integer, SnmpUsage> getMap() {
        return map;
    }

    public void setMap(Map<Integer, SnmpUsage> map) {
        this.map = map;
    }
}