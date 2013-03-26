package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.util.snmp.OIDConstants;

import java.util.Map;

public class StingrayUsageClientImpl implements StingrayUsageClient {

    @Override
    public Map<String, Long> getConcurrentConnections(Host host) {
        return null;
    }

    @Override
    public Map<String, Long> getTotalBandwidthIn(Host host) {
        return null;
    }

    @Override
    public Map<String, Long> getTotalBandwidthOut(Host host) {
        return null;
    }

    @Override
    public Long getConcurrentConnections(Host host, String virtualServerName) {
        String oid = OIDConstants.VS_CURRENT_CONNECTIONS;
        return null;
    }

    @Override
    public Long getTotalBandwidthIn(Host host, String virtualServerName) {
        String oid = OIDConstants.VS_BYTES_IN_LO;
        return null;
    }

    @Override
    public Long getTotalBandwidthOut(Host host, String virtualServerName) {
        String oid = OIDConstants.VS_BYTES_OUT_LO;
        return null;
    }
}