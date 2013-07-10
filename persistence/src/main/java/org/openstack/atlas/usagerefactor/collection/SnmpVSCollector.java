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
    public LoadBalancer loadbalancer;

    public SnmpVSCollector(Host host, LoadBalancer lb) {
        stingrayUsageClient = new StingrayUsageClientImpl();
        this.host = host;
        this.loadbalancer = lb;
    }

    public SnmpVSCollector() {
    }

    @Override
    public SnmpUsage call() throws UsageEventCollectionException {
        SnmpUsage snmpusage = null;
        try {
            snmpusage = stingrayUsageClient.getVirtualServerUsage(host, loadbalancer);
        } catch (StingraySnmpObjectNotFoundException ex) {
            String retString = String.format("Request for host %s usage from SNMP server failed. SnmpUsage Object" +
                    " is Not found for host", host.getName());
            LOG.info(retString);
        } catch (StingraySnmpGeneralException eg) {
            String retString = String.format("Request for host %s usage from SNMP server failed. SnmpUsage is Null", host.getName());
            LOG.info(retString);
        } catch (Exception e) {
            String retString = String.format("Request for host %s usage from SNMP server failed.", host.getName());
            LOG.error(retString, e);
            throw new UsageEventCollectionException(retString, e);
        }
        if(snmpusage != null) {
            LOG.info(String.format("Received snmp usage: %s", snmpusage.toString()));
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

    public LoadBalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(LoadBalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
    }
}