package org.openstack.atlas.usagerefactor.collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.snmp.StingrayUsageClient;
import org.openstack.atlas.usagerefactor.snmp.StingrayUsageClientImpl;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpObjectNotFoundException;

import java.util.concurrent.Callable;

public class SnmpUsageCollector implements Callable<SnmpUsage> {
    private final Log LOG = LogFactory.getLog(SnmpUsageCollector.class);

    public StingrayUsageClient stingrayUsageClient;
    public Host host;
    public LoadBalancer loadbalancer;

    public SnmpUsageCollector(Host host, LoadBalancer lb) {
        stingrayUsageClient = new StingrayUsageClientImpl();
        this.host = host;
        this.loadbalancer = lb;
    }

    public SnmpUsageCollector() {
    }

    @Override
    public SnmpUsage call(){
        SnmpUsage snmpusage = null;
        try {
            snmpusage = stingrayUsageClient.getVirtualServerUsage(host, loadbalancer);
            snmpusage = handleNullUsageResponse(snmpusage);
        } catch (StingraySnmpObjectNotFoundException ex) {
            String retString = String.format("Request for host %s usage from SNMP server failed. SnmpUsage Object" +
                    " is Not found for host", host.getName());
            LOG.info(retString);
            snmpusage = handleNullUsageResponse(snmpusage);
        } catch (StingraySnmpGeneralException eg) {
            String retString = String.format("Request for host %s usage from SNMP server failed. A default SnmpUsage will be created.", host.getName());
            LOG.info(retString);
            snmpusage = handleNullUsageResponse(snmpusage);
        } catch (Exception e) {
            String retString = String.format("Request for host %s usage from SNMP server failed.", host.getName());
            LOG.error(retString, e);
            snmpusage = handleNullUsageResponse(snmpusage);
        }
        if(snmpusage != null) {
            LOG.info(String.format("Stored snmp usage: %s", snmpusage.toString()));
        }
        return snmpusage;
    }

    private SnmpUsage handleNullUsageResponse(SnmpUsage snmpusage){
        if (snmpusage == null){
            SnmpUsage blankSnmpUsage = new SnmpUsage();
            blankSnmpUsage.setHostId(host.getId());
            blankSnmpUsage.setLoadbalancerId(loadbalancer.getId());
            return blankSnmpUsage;
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