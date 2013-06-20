package org.openstack.atlas.adapter.helpers;

import com.zxtm.service.client.PoolLoadBalancingAlgorithm;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.stm.StmAdapterImpl;
import org.openstack.atlas.service.domain.entities.*;
import org.rackspace.stingray.client.bandwidth.Bandwidth;
import org.rackspace.stingray.client.bandwidth.BandwidthBasic;
import org.rackspace.stingray.client.bandwidth.BandwidthProperties;
import org.rackspace.stingray.client.monitor.Monitor;
import org.rackspace.stingray.client.monitor.MonitorBasic;
import org.rackspace.stingray.client.monitor.MonitorHttp;
import org.rackspace.stingray.client.monitor.MonitorProperties;
import org.rackspace.stingray.client.persistence.Persistence;
import org.rackspace.stingray.client.persistence.PersistenceBasic;
import org.rackspace.stingray.client.persistence.PersistenceProperties;
import org.rackspace.stingray.client.pool.*;
import org.rackspace.stingray.client.protection.*;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.traffic.ip.TrafficIpBasic;
import org.rackspace.stingray.client.traffic.ip.TrafficIpProperties;
import org.rackspace.stingray.client.util.EnumFactory;
import org.rackspace.stingray.client.virtualserver.*;

import java.util.*;

public class ResourceTranslator {
    public Pool cPool;
    public Monitor cMonitor;
    public TrafficIp cTrafficIpGroup;
    public VirtualServer cVServer;
    public Protection cProtection;
    public Persistence cPersistence;
    public Bandwidth cBandwidth;


    public void translateLoadBalancerResource(LoadBalancerEndpointConfiguration config,
                                              String vsName, LoadBalancer loadBalancer) throws InsufficientRequestException {

        translatePersistenceResource(vsName, loadBalancer);
        translateMonitorResource(loadBalancer);
        translateBandwidthResource(loadBalancer);
        translatePoolResource(vsName, loadBalancer);
        translateVirtualServerResource(config, vsName, loadBalancer);
    }

    public VirtualServer translateVirtualServerResource(LoadBalancerEndpointConfiguration config,
                                                        String vsName, LoadBalancer loadBalancer) throws InsufficientRequestException {
        VirtualServer virtualServer = new VirtualServer();
        VirtualServerBasic basic = new VirtualServerBasic();
        VirtualServerProperties properties = new VirtualServerProperties();
        VirtualServerConnectionError ce = new VirtualServerConnectionError();
        VirtualServerTcp tcp = new VirtualServerTcp();
        VirtualServerLog log = null;

        //basic virtual server settings
        basic.setProtocol(loadBalancer.getProtocol().name());
        basic.setPort(loadBalancer.getPort());
        basic.setPool(vsName);

        //protection class settings
        if (loadBalancer.getAccessLists() != null || loadBalancer.getConnectionLimit() != null) {
            basic.setProtection_class(vsName);
        }

        //connection log settings
        if (loadBalancer.isConnectionLogging() != null && loadBalancer.isConnectionLogging()) {
            log = new VirtualServerLog();
            final String nonHttpLogFormat = "%v %t %h %A:%p %n %B %b %T";
            final String httpLogFormat = "%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\" %n";

            if (loadBalancer.getProtocol() != LoadBalancerProtocol.HTTP) {
                log.setFormat(nonHttpLogFormat);
            } else if (loadBalancer.getProtocol() == LoadBalancerProtocol.HTTP) {
                log.setFormat(httpLogFormat);
            }

            log.setEnabled(loadBalancer.isConnectionLogging());
            log.setFilename(config.getLogFileLocation());
            properties.setLog(log);
        }

        //error file settings
        UserPages userPages = loadBalancer.getUserPages();
        String ep = null;
        if (userPages != null) { // if userPages is null, just leave the ce object alone and it should use the default page
            ep = userPages.getErrorpage();
            ce.setError_file(ep);
        }
        properties.setConnection_errors(ce);

        //trafficscript or rule settings
        List<String> rules = Arrays.asList(StmAdapterImpl.XFF, StmAdapterImpl.XFP);
        basic.setRequest_rules(rules);

        //Half closed proxy settings
        tcp.setProxy_close(loadBalancer.isHalfClosed());
        properties.setTcp(tcp);

        //trafficIpGroup settings
        basic.setListen_on_any(false);
//        basic.setListen_on_traffic_ips();

        properties.setBasic(basic);
        virtualServer.setProperties(properties);

        cVServer = virtualServer;
        return cVServer;
    }

    public Bandwidth translateBandwidthResource(LoadBalancer loadBalancer) throws InsufficientRequestException {
        Bandwidth bandwidth = new Bandwidth();
        BandwidthProperties properties = new BandwidthProperties();
        BandwidthBasic basic = new BandwidthBasic();

        loadBalancer.getConnectionLimit();
        basic.setMaximum(loadBalancer.getRateLimit().getMaxRequestsPerSecond());

        properties.setBasic(basic);
        bandwidth.setProperties(properties);

        cBandwidth = bandwidth;
        return cBandwidth;
    }

    public TrafficIp translateTrafficIpResource(LoadBalancerEndpointConfiguration config,
                                                String tigName, LoadBalancer loadBalancer) {
        TrafficIp tig = new TrafficIp();
        TrafficIpProperties properties = new TrafficIpProperties();
        TrafficIpBasic basic = new TrafficIpBasic();

        basic.setEnabled(true);
//        basic.setIpaddresses();
//        basic.set


        return null;
    }

    public Pool translatePoolResource(String vsName, LoadBalancer loadBalancer) throws InsufficientRequestException {
        Set<Node> nodes = loadBalancer.getNodes();

        Pool pool = new Pool();
        PoolProperties properties = new PoolProperties();
        PoolBasic basic = new PoolBasic();
        List<PoolNodeWeight> weights = new ArrayList<PoolNodeWeight>();
        PoolLoadbalancing poollb = new PoolLoadbalancing();
        PoolConnection connection = new PoolConnection();

        basic.setDraining(getNodesWithCondition(nodes, NodeCondition.DRAINING));
        basic.setDisabled(getNodesWithCondition(nodes, NodeCondition.DISABLED));
        basic.setPassive_monitoring(false);

        if (loadBalancer.getAlgorithm().name().equals(PoolLoadBalancingAlgorithm.wroundrobin.getValue())
                || loadBalancer.getAlgorithm().name().equals(PoolLoadBalancingAlgorithm.wconnections.getValue())) {
            PoolNodeWeight nw;
            for (Node n : nodes) {
                nw = new PoolNodeWeight();
                nw.setNode(n.getIpAddress());
                nw.setWeight(n.getWeight());
                weights.add(nw);
            }
            poollb.setNode_weighting(weights);
        }

        ZeusNodePriorityContainer znpc = new ZeusNodePriorityContainer(loadBalancer.getNodes());
        poollb.setPriority_enabled(znpc.hasSecondary());
        poollb.setPriority_values(znpc.getPriorityValuesSet());
        poollb.setAlgorithm(loadBalancer.getAlgorithm().name());

        connection.setMax_reply_time(loadBalancer.getTimeout());

        if (loadBalancer.getHealthMonitor() != null)
            basic.setMonitors(new HashSet<String>(Arrays.asList(vsName)));


        if (loadBalancer.getSessionPersistence() != null)
            basic.setPersistence_class(loadBalancer.getSessionPersistence()
                    .getSessionPersistence().getPersistenceType().name());

        properties.setBasic(basic);
        properties.setLoad_balancing(poollb);
        properties.setConnection(connection);
        pool.setProperties(properties);

        cPool = pool;
        return cPool;
    }

    public Monitor translateMonitorResource(LoadBalancer loadBalancer) {
        Monitor monitor = new Monitor();
        MonitorProperties properties = new MonitorProperties();
        MonitorBasic basic = new MonitorBasic();
        MonitorHttp http;

        HealthMonitor hm = loadBalancer.getHealthMonitor();

        basic.setDelay(hm.getDelay());
        basic.setTimeout(hm.getTimeout());
        basic.setFailures(hm.getAttemptsBeforeDeactivation());

        if (hm.getType().equals(HealthMonitorType.CONNECT)) {
            basic.setType(EnumFactory.Accept_from.CONNECT.name());
        } else if (hm.getType().equals(HealthMonitorType.HTTP) || hm.getType().equals(HealthMonitorType.HTTPS)) {
            basic.setType(EnumFactory.Accept_from.HTTP.name());
            http = new MonitorHttp();
            http.setPath(hm.getPath());
            http.setStatus_regex(hm.getStatusRegex());
            http.setBody_regex(hm.getBodyRegex());
            http.setHost_header(hm.getHostHeader());
            if (hm.getType().equals(HealthMonitorType.HTTPS)) {
                basic.setUse_ssl(true);
            }
            properties.setHttp(http);
        }

        properties.setBasic(basic);
        monitor.setProperties(properties);

        cMonitor = monitor;
        return cMonitor;
    }

    public Protection translateProtectionResource(String vsName, LoadBalancer loadBalancer) {
        Protection protection = new Protection();
        ProtectionBasic basic = new ProtectionBasic();
        ProtectionProperties properties = new ProtectionProperties();

        ConnectionLimit limits = loadBalancer.getConnectionLimit();
        Set<AccessList> accessList = loadBalancer.getAccessLists();

        ProtectionAccessRestiction pac = new ProtectionAccessRestiction();
//        pac.setAllowed("allowedaddys");
        ProtectionConnectionLimiting limiting = new ProtectionConnectionLimiting();

        properties.setBasic(basic);
        protection.setProperties(properties);

        cProtection = protection;
        return protection;
    }

    public Persistence translatePersistenceResource(String vsName, LoadBalancer loadBalancer) {
        Persistence persistence = new Persistence();
        PersistenceProperties properties = new PersistenceProperties();
        PersistenceBasic basic = new PersistenceBasic();

        basic.setType(loadBalancer.getSessionPersistence().getSessionPersistence().getPersistenceType().value());
        properties.setBasic(basic);
        persistence.setProperties(properties);
        cPersistence = persistence;
        return cPersistence;

    }

    private Set<String> getNodesWithCondition(Collection<Node> nodes, NodeCondition nodeCondition) {
        Set<String> nodesWithCondition = new HashSet<String>();
        for (Node node : nodes) {
            if (node.getCondition().equals(nodeCondition)) {
                nodesWithCondition.add(String.format("%s:%d", node.getIpAddress(), node.getPort()));
            }
        }
        return nodesWithCondition;
    }

    public Pool getcPool() {
        return cPool;
    }

    public void setcPool(Pool cPool) {
        this.cPool = cPool;
    }

    public Monitor getcMonitor() {
        return cMonitor;
    }

    public void setcMonitor(Monitor cMonitor) {
        this.cMonitor = cMonitor;
    }

    public VirtualServer getcVServer() {
        return cVServer;
    }

    public void setcVServer(VirtualServer cVServer) {
        this.cVServer = cVServer;
    }

    public Protection getcProtection() {
        return cProtection;
    }

    public void setcProtection(Protection cProtection) {
        this.cProtection = cProtection;
    }

    public Persistence getcPersistence() {
        return cPersistence;
    }

    public void setcPersistence(Persistence cPersistence) {
        this.cPersistence = cPersistence;
    }

    public Bandwidth getcBandwidth() {
        return cBandwidth;
    }

    public void setcBandwidth(Bandwidth cBandwidth) {
        this.cBandwidth = cBandwidth;
    }
}
