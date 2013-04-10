package org.openstack.atlas.api.usage.processor.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.usagerefactor.StingrayUsageClient;
import org.openstack.atlas.usagerefactor.StingrayUsageClientImpl;

public class SNMPUtil {
    private final Log LOG = LogFactory.getLog(SNMPUtil.class);

    public void retrieveSNMPUsage(LoadBalancer loadBalancer) {
        StingrayUsageClient snmpClient = new StingrayUsageClientImpl();
//        snmpClient.getVirtualServerUsage()

    }
}
