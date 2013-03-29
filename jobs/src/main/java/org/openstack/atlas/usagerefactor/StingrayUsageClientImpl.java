package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.util.snmp.OIDConstants;
import org.openstack.atlas.util.snmp.StingraySnmpClient;
import org.openstack.atlas.util.snmp.StingraySnmpConstants;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;
import org.snmp4j.mp.SnmpConstants;

import java.util.Map;

public class StingrayUsageClientImpl implements StingrayUsageClient {

    StingraySnmpClient client = new StingraySnmpClient();

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
        client.setAddress(host.getManagementIp());
        client.setPort(StingraySnmpConstants.PORT);
        client.setVersion(SnmpConstants.version2c);
        try {
            return client.getValueForServerOnHost(host.getManagementIp(), virtualServerName,
                OIDConstants.VS_CURRENT_CONNECTIONS);
        } catch(StingraySnmpGeneralException ssge) {
            return (-1L);
        }
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