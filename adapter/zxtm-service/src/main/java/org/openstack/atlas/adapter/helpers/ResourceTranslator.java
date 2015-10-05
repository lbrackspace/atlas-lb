package org.openstack.atlas.adapter.helpers;

import com.zxtm.service.client.VirtualServerProtocol;
import org.codehaus.jackson.map.ObjectMapper;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.zxtm.ZxtmConversionUtils;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.util.ca.StringUtils;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;
import org.openstack.atlas.util.ip.IPUtils;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.rackspace.stingray.client.bandwidth.Bandwidth;
import org.rackspace.stingray.client.bandwidth.BandwidthBasic;
import org.rackspace.stingray.client.bandwidth.BandwidthProperties;
import org.rackspace.stingray.client.monitor.Monitor;
import org.rackspace.stingray.client.monitor.MonitorBasic;
import org.rackspace.stingray.client.monitor.MonitorHttp;
import org.rackspace.stingray.client.monitor.MonitorProperties;
import org.rackspace.stingray.client.pool.*;
import org.rackspace.stingray.client.protection.*;
import org.rackspace.stingray.client.ssl.keypair.Keypair;
import org.rackspace.stingray.client.ssl.keypair.KeypairBasic;
import org.rackspace.stingray.client.ssl.keypair.KeypairProperties;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.traffic.ip.TrafficIpBasic;
import org.rackspace.stingray.client.traffic.ip.TrafficIpProperties;
import org.rackspace.stingray.client.util.EnumFactory;
import org.rackspace.stingray.client.virtualserver.*;

import java.io.IOException;
import java.util.*;

public class ResourceTranslator {
    public Pool cPool;
    public Monitor cMonitor;
    public Map<String, TrafficIp> cTrafficIpGroups;
    public VirtualServer cVServer;
    public VirtualServer cRedirectVServer;
    public Protection cProtection;
    public Bandwidth cBandwidth;
    public Keypair cKeypair;
    protected static final ZeusUtils zeusUtil;

    static {
        zeusUtil = new ZeusUtils();
    }

    public static ResourceTranslator getNewResourceTranslator() {
        return new ResourceTranslator();
    }

    public void translateLoadBalancerResource(LoadBalancerEndpointConfiguration config,
                                              String vsName, LoadBalancer loadBalancer, LoadBalancer queLb) throws InsufficientRequestException {
        translateLoadBalancerResource(config, vsName, loadBalancer, queLb, true, true);
    }

    public void translateLoadBalancerResource(LoadBalancerEndpointConfiguration config,
                                              String vsName, LoadBalancer loadBalancer, LoadBalancer queLb, boolean careAboutCert, boolean vipsEnabled)
            throws InsufficientRequestException {
        //Order matters when translating the entire entity.
        if (loadBalancer.getHealthMonitor() != null) translateMonitorResource(loadBalancer);
        if (loadBalancer.getRateLimit() != null) translateBandwidthResource(loadBalancer);

        translateTrafficIpGroupsResource(config, loadBalancer, vipsEnabled);

        if (loadBalancer.getSslTermination() != null) translateKeypairResource(loadBalancer, careAboutCert);
        if ((loadBalancer.getAccessLists() != null && !loadBalancer.getAccessLists().isEmpty()) || loadBalancer.getConnectionLimit() != null)
            translateProtectionResource(loadBalancer);

        translatePoolResource(vsName, loadBalancer, queLb);
        translateVirtualServerResource(config, vsName, loadBalancer);
        if (loadBalancer.isHttpsRedirect() != null && loadBalancer.isHttpsRedirect()) {
            translateRedirectVirtualServerResource(config, vsName, loadBalancer);
        }
    }

    //This could probably be trimmed down a bit
    public VirtualServer translateRedirectVirtualServerResource(LoadBalancerEndpointConfiguration config, String vsName, LoadBalancer loadBalancer) throws InsufficientRequestException {
        VirtualServerBasic basic = new VirtualServerBasic();
        VirtualServerSsl ssl = new VirtualServerSsl();
        VirtualServerProperties properties = new VirtualServerProperties();
        VirtualServerConnectionError ce = new VirtualServerConnectionError();
        VirtualServerLog log;
        List<String> rules = new ArrayList<String>();

        properties.setBasic(basic);
        properties.setSsl(ssl);
        cRedirectVServer = new VirtualServer();
        cRedirectVServer.setProperties(properties);

        basic.setEnabled(true);

        //protection class settings
        if ((loadBalancer.getAccessLists() != null && !loadBalancer.getAccessLists().isEmpty()) || loadBalancer.getConnectionLimit() != null) {
            basic.setProtection_class(ZxtmNameBuilder.genVSName(loadBalancer));
        }

        // Redirection specific
        basic.setPort(80);
        basic.setPool("discard");
        basic.setProtocol(VirtualServerProtocol.http.getValue());

        log = new VirtualServerLog();
        log.setEnabled(false);
        properties.setLog(log);

        //error file settings
        if (loadBalancer.getUserPages() != null && loadBalancer.getUserPages().getErrorpage() != null) {
            ce.setError_file(ZxtmNameBuilder.generateErrorPageName(ZxtmNameBuilder.genRedirectVSName(loadBalancer)));
        } else {
            ce.setError_file("Default");
        }
        properties.setConnection_errors(ce);

        //trafficscript or rule settings
        rules.add(StmConstants.HTTPS_REDIRECT);
        basic.setRequest_rules(rules);

        //trafficIpGroup settings
        basic.setListen_on_any(false);
        basic.setListen_on_traffic_ips(genGroupNameSet(loadBalancer));

        //ssl settings
        ssl.setServer_cert_default(vsName);

        return cRedirectVServer;
    }

    public VirtualServer translateVirtualServerResource(LoadBalancerEndpointConfiguration config,
                                                        String vsName, LoadBalancer loadBalancer) throws InsufficientRequestException {
        VirtualServerBasic basic = new VirtualServerBasic();
        VirtualServerSsl ssl = new VirtualServerSsl();
        VirtualServerProperties properties = new VirtualServerProperties();
        VirtualServerConnectionError ce = new VirtualServerConnectionError();
        VirtualServerTcp tcp = new VirtualServerTcp();
        VirtualServerLog log;
        List<String> rules = new ArrayList<String>();

        properties.setBasic(basic);
        properties.setSsl(ssl);
        cVServer = new VirtualServer();
        cVServer.setProperties(properties);

        //basic virtual server settings
        if (vsName.equals(ZxtmNameBuilder.genSslVSName(loadBalancer))) {
            basic.setPort(loadBalancer.getSslTermination().getSecurePort());
            basic.setSsl_decrypt(true);
            basic.setEnabled(loadBalancer.isUsingSsl());
            if (loadBalancer.getProtocol() == LoadBalancerProtocol.HTTP) {
                ssl.setAdd_http_headers(true);
                VirtualServerHttp virtualServerHttp = new VirtualServerHttp();
                virtualServerHttp.setLocation_rewrite("never");
                properties.setHttp(virtualServerHttp);
            }
        } else {
            basic.setPort(loadBalancer.getPort());
            if (loadBalancer.hasSsl()) {
                basic.setEnabled(!loadBalancer.isSecureOnly());
            } else {
                basic.setEnabled(true);
            }
        }

        basic.setPool(ZxtmNameBuilder.genVSName(loadBalancer));
        basic.setProtocol(ZxtmConversionUtils.mapProtocol(loadBalancer.getProtocol()).getValue());

        //protection class settings
        if ((loadBalancer.getAccessLists() != null && !loadBalancer.getAccessLists().isEmpty()) || loadBalancer.getConnectionLimit() != null) {
            basic.setProtection_class(ZxtmNameBuilder.genVSName(loadBalancer));
        }
        // Dumbing this down for SOAP compatibility, this isn't deleted now
        // else {
        //     basic.setProtection_class("");
        // }

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
        } else if (loadBalancer.isConnectionLogging() != null && !loadBalancer.isConnectionLogging()) {
            log = new VirtualServerLog();
            log.setEnabled(false);
            properties.setLog(log);
        }

        //webcache settings
        if (loadBalancer.isContentCaching() != null && loadBalancer.isContentCaching()) {
            VirtualServerWebcache cache = new VirtualServerWebcache();
            cache.setEnabled(true);
            properties.setWeb_cache(cache);
            rules.add(StmConstants.CONTENT_CACHING);
        }

        //error file settings
        if (loadBalancer.getUserPages() != null && loadBalancer.getUserPages().getErrorpage() != null) {
            ce.setError_file(ZxtmNameBuilder.generateErrorPageName(vsName));
        } else {
            ce.setError_file("Default");
        }
        properties.setConnection_errors(ce);

        //X_forwarded headers
        if (loadBalancer.getProtocol() == LoadBalancerProtocol.HTTP) {
            basic.setAdd_x_forwarded_for(true);
            basic.setAdd_x_forwarded_proto(true);
            rules.add(StmConstants.XFPORT);
        }

        //trafficscript or rule settings
        basic.setRequest_rules(rules);

        //Half closed proxy settings
        tcp.setProxy_close(loadBalancer.isHalfClosed());
        properties.setTcp(tcp);

        //trafficIpGroup settings
        basic.setListen_on_any(false);
        basic.setListen_on_traffic_ips(genGroupNameSet(loadBalancer));

        //ssl settings
        ssl.setServer_cert_default(vsName);

        return cVServer;
    }

    public Map<String, TrafficIp> translateTrafficIpGroupsResource(LoadBalancerEndpointConfiguration config,
                                                                   LoadBalancer loadBalancer, boolean isEnabled) throws InsufficientRequestException {
        Map<String, TrafficIp> nameandgroup = new HashMap<String, TrafficIp>();

        // Add new traffic ip groups for IPv4 vips
        for (LoadBalancerJoinVip loadBalancerJoinVipToAdd : loadBalancer.getLoadBalancerJoinVipSet()) {
            nameandgroup.put(ZxtmNameBuilder.generateTrafficIpGroupName(loadBalancer, loadBalancerJoinVipToAdd.getVirtualIp()),
                    translateTrafficIpGroupResource(config, loadBalancerJoinVipToAdd.getVirtualIp().getIpAddress(), isEnabled));
        }

        // Add new traffic ip groups for IPv6 vips
        for (LoadBalancerJoinVip6 loadBalancerJoinVip6ToAdd : loadBalancer.getLoadBalancerJoinVip6Set()) {
            try {
                VirtualIpv6 virtualIpv6 = loadBalancerJoinVip6ToAdd.getVirtualIp();
                virtualIpv6.setCluster(config.getTrafficManagerHost().getCluster());
                nameandgroup.put(ZxtmNameBuilder.generateTrafficIpGroupName(loadBalancer, loadBalancerJoinVip6ToAdd.getVirtualIp()),
                        translateTrafficIpGroupResource(config, virtualIpv6.getDerivedIpString(), isEnabled));
            } catch (IPStringConversionException e) {
                //Generally means there is a missing value, wrap up the exception into general IRE;
                throw new InsufficientRequestException(e);
            }
        }

        cTrafficIpGroups = nameandgroup;
        return nameandgroup;
    }

    private TrafficIp translateTrafficIpGroupResource(LoadBalancerEndpointConfiguration config, String ipaddress, boolean isEnabled) {
        TrafficIp tig = new TrafficIp();
        TrafficIpProperties properties = new TrafficIpProperties();
        TrafficIpBasic basic = new TrafficIpBasic();

//        TrafficIpIpMapping mapping = new TrafficIpIpMapping();
//        List<TrafficIpIpMapping> mappings = new ArrayList<TrafficIpIpMapping>(Arrays.asList(mapping));
//        basic.setIp_mapping(mappings);

        basic.setHash_source_port(false);
        basic.setKeeptogether(false);

        basic.setEnabled(isEnabled);
        basic.setIpaddresses(new HashSet<String>(Arrays.asList(ipaddress)));

        Set<String> machines = new HashSet<String>();
        machines.add(config.getTrafficManagerName());
        machines.addAll(config.getFailoverTrafficManagerNames());
        basic.setMachines(machines);
        basic.setSlaves(new HashSet<String>(config.getFailoverTrafficManagerNames()));

        properties.setBasic(basic);
        tig.setProperties(properties);
        return tig;
    }

    public Set<String> genGroupNameSet(LoadBalancer loadBalancer) throws InsufficientRequestException {
        Set<String> groupSet = new HashSet<String>();

        int acctId = loadBalancer.getAccountId();
        for (LoadBalancerJoinVip loadBalancerJoinVipToAdd : loadBalancer.getLoadBalancerJoinVipSet()) {
            groupSet.add(ZxtmNameBuilder.generateTrafficIpGroupName(acctId, loadBalancerJoinVipToAdd.getVirtualIp().getId().toString()));
        }

        for (LoadBalancerJoinVip6 loadBalancerJoinVip6ToAdd : loadBalancer.getLoadBalancerJoinVip6Set()) {
            groupSet.add(ZxtmNameBuilder.generateTrafficIpGroupName(acctId, loadBalancerJoinVip6ToAdd.getVirtualIp().getId().toString()));
        }

        return groupSet;
    }

    public Pool translatePoolResource(String vsName, LoadBalancer loadBalancer, LoadBalancer queLb) throws InsufficientRequestException {
        cPool = new Pool();
        PoolProperties properties = new PoolProperties();
        PoolBasic basic = new PoolBasic();
        List<PoolNodeWeight> weights = new ArrayList<PoolNodeWeight>();
        PoolLoadbalancing poollb = new PoolLoadbalancing();

        Set<Node> nodes = loadBalancer.getNodes();
        Set<Node> enabledNodes = new HashSet<Node>();


        enabledNodes.addAll(NodeHelper.getNodesWithCondition(nodes, NodeCondition.ENABLED));
        enabledNodes.addAll(NodeHelper.getNodesWithCondition(nodes, NodeCondition.DRAINING));

        basic.setNodes(NodeHelper.getNodeStrValue(enabledNodes));
        basic.setDraining(NodeHelper.getNodeStrValue(NodeHelper.getNodesWithCondition(nodes, NodeCondition.DRAINING)));
        basic.setDisabled(NodeHelper.getNodeStrValue(NodeHelper.getNodesWithCondition(nodes, NodeCondition.DISABLED)));
        basic.setPassive_monitoring(false);


        String lbAlgo = loadBalancer.getAlgorithm().name().toLowerCase();

        if (queLb.getNodes() != null && !queLb.getNodes().isEmpty()) {
            if (lbAlgo.equals(EnumFactory.Accept_from.WEIGHTED_ROUND_ROBIN.toString())
                    || lbAlgo.equals(EnumFactory.Accept_from.WEIGHTED_LEAST_CONNECTIONS.toString())) {
                PoolNodeWeight nw;
                for (Node n : nodes) {
                    nw = new PoolNodeWeight();
                    nw.setNode(IpHelper.createZeusIpString(n.getIpAddress(), n.getPort()));
                    nw.setWeight(n.getWeight());
                    weights.add(nw);
                }
                poollb.setNode_weighting(weights);
            }
        }

        ZeusNodePriorityContainer znpc = new ZeusNodePriorityContainer(nodes);
        poollb.setPriority_enabled(znpc.hasSecondary());
        poollb.setPriority_values(znpc.getPriorityValuesSet());
        poollb.setAlgorithm(lbAlgo);

        PoolConnection connection = null;
        if (queLb.getTimeout() != null) {
            connection = new PoolConnection();
            connection.setMax_reply_time(loadBalancer.getTimeout());
        }

        if (queLb.getHealthMonitor() != null)
            basic.setMonitors(new HashSet<String>(Arrays.asList(vsName)));

        if ((queLb.getSessionPersistence() != null) && !(queLb.getSessionPersistence().name().equals(SessionPersistence.NONE.name()))) {
            basic.setPersistence_class(loadBalancer.getSessionPersistence().name());
        } else {
            basic.setPersistence_class("");
        }

        if (queLb.getRateLimit() != null) {
            basic.setBandwidth_class(vsName);
        } else {
            basic.setBandwidth_class(null);
        }

        properties.setBasic(basic);
        properties.setLoad_balancing(poollb);
        properties.setConnection(connection);
        cPool.setProperties(properties);

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
            basic.setType(EnumFactory.Accept_from.CONNECT.toString());
        } else if (hm.getType().equals(HealthMonitorType.HTTP) || hm.getType().equals(HealthMonitorType.HTTPS)) {
            basic.setType(EnumFactory.Accept_from.HTTP.toString());
            http = new MonitorHttp();
            http.setPath(hm.getPath());
            http.setStatus_regex(hm.getStatusRegex());
            http.setBody_regex(hm.getBodyRegex());
            http.setHost_header(hm.getHostHeader());
            if (hm.getType().equals(HealthMonitorType.HTTPS)) {
                basic.setUse_ssl(true);
            } else {
                basic.setUse_ssl(false);
            }
            properties.setHttp(http);
        }

        properties.setBasic(basic);
        monitor.setProperties(properties);

        cMonitor = monitor;
        return cMonitor;
    }

    public Protection translateProtectionResource(LoadBalancer loadBalancer) {
        cProtection = new Protection();
        ProtectionBasic basic = new ProtectionBasic();
        ProtectionProperties properties = new ProtectionProperties();

        ConnectionLimit limits = loadBalancer.getConnectionLimit();
        Set<AccessList> accessList = loadBalancer.getAccessLists();

        ProtectionAccessRestriction pac = new ProtectionAccessRestriction();
        if (accessList != null && !accessList.isEmpty()) {
            Set<String> allowed = new HashSet<String>();
            Set<String> banned = new HashSet<String>();
            for (AccessList item : accessList) {
                if (item.getType().equals(AccessListType.ALLOW)) {
                    allowed.add(item.getIpAddress());
                } else {
                    banned.add(item.getIpAddress());
                }
            }
            pac.setAllowed(allowed);
            pac.setBanned(banned);
        }
        properties.setAccess_restriction(pac);

        ProtectionConnectionLimiting limiting = new ProtectionConnectionLimiting();
        if (limits != null) {
            Integer maxConnections = limits.getMaxConnections();
            if (maxConnections == null) maxConnections = 0;
            limiting.setMax_1_connections(maxConnections);

            /* Zeus bug requires us to set per-process to false and ignore most of these settings */
            basic.setPer_process_connection_count(false);
            limiting.setMin_connections(0);
            limiting.setRate_timer(1);
            limiting.setMax_connection_rate(0);
            limiting.setMax_10_connections(0);
            //limiting.setMin_connections(limits.getMinConnections());
            //limiting.setRate_timer(limits.getRateInterval());
            //limiting.setMax_connection_rate(limits.getMaxConnectionRate());
            //limiting.setMax_10_connections(maxConnections * 10);
        } else {
            limiting.setMax_1_connections(0);

            /* Zeus bug requires us to set per-process to false */
            basic.setPer_process_connection_count(false);
            limiting.setMin_connections(0);
            limiting.setRate_timer(1);
            limiting.setMax_connection_rate(0);
            limiting.setMax_10_connections(0);
        }
        properties.setConnection_limiting(limiting);

        properties.setBasic(basic);
        cProtection.setProperties(properties);

        return cProtection;
    }

    public Keypair translateKeypairResource(LoadBalancer loadBalancer, boolean careAboutCert)
            throws InsufficientRequestException {
        ZeusCrtFile zeusCertFile = zeusUtil.buildZeusCrtFileLbassValidation(loadBalancer.getSslTermination().getPrivatekey(),
                loadBalancer.getSslTermination().getCertificate(), loadBalancer.getSslTermination().getIntermediateCertificate());
        if (zeusCertFile.hasFatalErrors()) {
            String fmt = "StingrayCertFile generation Failure: %s";
            String errors = StringUtils.joinString(zeusCertFile.getErrors(), ",");
            String msg = String.format(fmt, errors);

            if (careAboutCert)
                throw new InsufficientRequestException(msg);
            else
                return null;
        }

        cKeypair = new Keypair();
        KeypairProperties keypairProperties = new KeypairProperties();
        KeypairBasic keypairBasic = new KeypairBasic();
        keypairBasic.setPrivate(zeusCertFile.getPrivate_key());
        keypairBasic.setPublic(zeusCertFile.getPublic_cert());
        keypairProperties.setBasic(keypairBasic);
        cKeypair.setProperties(keypairProperties);
        return cKeypair;
    }

    public Bandwidth translateBandwidthResource(LoadBalancer loadBalancer) throws InsufficientRequestException {
        Bandwidth bandwidth = new Bandwidth();
        BandwidthProperties properties = new BandwidthProperties();
        BandwidthBasic basic = new BandwidthBasic();

        RateLimit rateLimit = loadBalancer.getRateLimit();

        if (rateLimit != null) {
            Ticket ticket = rateLimit.getTicket();
            basic.setMaximum(rateLimit.getMaxRequestsPerSecond());

            if (ticket != null)
                basic.setNote(ticket.getComment());
        }

        properties.setBasic(basic);
        bandwidth.setProperties(properties);

        cBandwidth = bandwidth;
        return cBandwidth;
    }

    public <T> String objectToString(Object obj, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Object myObject = mapper.writeValueAsString(obj);
//        System.out.println(myObject.toString());
        return myObject.toString();
    }

    public <T> T stringToObject(String str, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Object myObject = mapper.readValue(str, clazz);
        return (T) myObject;
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

    public Map<String, TrafficIp> getcTrafficIpGroups() {
        return cTrafficIpGroups;
    }

    public void setcTrafficIpGroups(Map<String, TrafficIp> cTrafficIpGroups) {
        this.cTrafficIpGroups = cTrafficIpGroups;
    }

    public VirtualServer getcVServer() {
        return cVServer;
    }

    public void setcVServer(VirtualServer cVServer) {
        this.cVServer = cVServer;
    }

    public VirtualServer getcRedirectVServer() {
        return cRedirectVServer;
    }

    public void setcRedirectVServer(VirtualServer cVServer) {
        this.cRedirectVServer = cVServer;
    }

    public Protection getcProtection() {
        return cProtection;
    }

    public void setcProtection(Protection cProtection) {
        this.cProtection = cProtection;
    }

    public Bandwidth getcBandwidth() {
        return cBandwidth;
    }

    public void setcBandwidth(Bandwidth cBandwidth) {
        this.cBandwidth = cBandwidth;
    }

    public Keypair getcKeypair() {
        return cKeypair;
    }

    public void setcKeypair(Keypair keypair) {
        this.cKeypair = keypair;
    }
}
