package org.openstack.atlas.usagerefactor.collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.snmp.StingrayUsageClient;
import org.openstack.atlas.usagerefactor.snmp.StingrayUsageClientImpl;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpObjectNotFoundException;

import java.util.concurrent.Callable;

public class SnmpVSCollector implements Callable<SnmpUsage> {
    private final Log LOG = LogFactory.getLog(SnmpVSCollector.class);

    public StingrayUsageClient stingrayUsageClient;
    public Host host;
    public LoadBalancer lb;

    public SnmpVSCollector(Host host, LoadBalancer lb) {
        stingrayUsageClient = new StingrayUsageClientImpl();
        this.host = host;
        this.lb = lb;
    }

    public SnmpVSCollector() {
    }

    @Override
    public SnmpUsage call() throws UsageEventCollectionException {
        SnmpUsage snmpusage;
        try {
            snmpusage = stingrayUsageClient.getVirtualServerUsage(host, lb);
        } catch (StingraySnmpObjectNotFoundException ex) {
            //Set host ID so we can still process the usage event for this host...
            snmpusage = new SnmpUsage();
            snmpusage.setHostId(host.getId());
            String retString = String.format("Request for host %s usage from SNMP server failed. SnmpUsage Object" +
                    "is Not foud for host", host.getName());
            LOG.error(retString, ex);
        } catch (StingraySnmpGeneralException eg) {
            //Set host ID so we can still process the usage event for this host...
            snmpusage = new SnmpUsage();
            snmpusage.setHostId(host.getId());
            String retString = String.format("Request for host %s usage from SNMP server failed. SnmpUsage is Null", host.getName());
            LOG.error(retString, eg);
        } catch (Exception e) {
            String retString = String.format("Request for host %s usage from SNMP server failed.", host.getName());
            LOG.error(retString, e);
            throw new UsageEventCollectionException(retString, e);
        }
        return snmpusage;
    }

    public StingrayUsageClient getStingrayUsageClient() {
        return stingrayUsageClient;
    }

    public void setStingrayUsageClient(StingrayUsageClient stingrayUsageClient) {
        this.stingrayUsageClient = stingrayUsageClient;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public LoadBalancer getLb() {
        return lb;
    }

    public void setLb(LoadBalancer lb) {
        this.lb = lb;
    }
}