package org.openstack.atlas.usagerefactor.collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.usagerefactor.SnmpStats;
import org.openstack.atlas.usagerefactor.snmp.StingrayUsageClient;
import org.openstack.atlas.usagerefactor.snmp.StingrayUsageClientImpl;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpObjectNotFoundException;

import java.util.concurrent.Callable;

public class SnmpStatsCollector implements Callable<SnmpStats> {
    private final Log LOG = LogFactory.getLog(SnmpStatsCollector.class);

    public StingrayUsageClient stingrayUsageClient;
    public Host host;
    public LoadBalancer loadbalancer;

    public SnmpStatsCollector(Host host, LoadBalancer lb) {
        stingrayUsageClient = new StingrayUsageClientImpl();
        this.host = host;
        this.loadbalancer = lb;
    }

    public SnmpStatsCollector() {
    }

    @Override
    public SnmpStats call(){
        SnmpStats snmpStats = null;

        try {
            snmpStats = stingrayUsageClient.getVirtualServerStats(host, loadbalancer);
            snmpStats = handleNullStatsResponse(snmpStats);
        } catch (StingraySnmpObjectNotFoundException ex) {
            String retString = String.format("Request for host %s stats from SNMP server failed. SnmpStats object" +
                    " is not found for host", host.getName());
            LOG.info(retString);
            snmpStats = handleNullStatsResponse(snmpStats);
        } catch (StingraySnmpGeneralException eg) {
            String retString = String.format("Request for host %s stats from SNMP server failed. A default SnmpStats will be created.", host.getName());
            LOG.info(retString);
            snmpStats = handleNullStatsResponse(snmpStats);
        } catch (Exception e) {
            String retString = String.format("Request for host %s stas from SNMP server failed.", host.getName());
            LOG.error(retString, e);
            snmpStats = handleNullStatsResponse(snmpStats);
        }

        if(snmpStats != null) {
            LOG.info(String.format("Stored snmp stats: %s", snmpStats.toString()));
        }

        return snmpStats;
    }

    private SnmpStats handleNullStatsResponse(SnmpStats snmpStats){
        if (snmpStats == null){
            SnmpStats blankSnmpStats = new SnmpStats();
            blankSnmpStats.setHostId(host.getId());
            blankSnmpStats.setLoadbalancerId(loadbalancer.getId());
            return blankSnmpStats;
        }

        return snmpStats;
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
