package org.openstack.atlas.adapter.vtm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.adapter.helpers.ZeusNodePriorityContainer;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.adapter.zxtm.ZxtmConversionUtils;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.util.ca.StringUtils;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;
import org.openstack.atlas.util.constants.ConnectionThrottleDefaultConstants;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.rackspace.vtm.client.bandwidth.Bandwidth;
import org.rackspace.vtm.client.bandwidth.BandwidthBasic;
import org.rackspace.vtm.client.bandwidth.BandwidthProperties;
import org.rackspace.vtm.client.monitor.Monitor;
import org.rackspace.vtm.client.monitor.MonitorBasic;
import org.rackspace.vtm.client.monitor.MonitorHttp;
import org.rackspace.vtm.client.monitor.MonitorProperties;
import org.rackspace.vtm.client.pool.*;
import org.rackspace.vtm.client.protection.*;
import org.rackspace.vtm.client.ssl.keypair.Keypair;
import org.rackspace.vtm.client.ssl.keypair.KeypairBasic;
import org.rackspace.vtm.client.ssl.keypair.KeypairProperties;
import org.rackspace.vtm.client.traffic.ip.TrafficIp;
import org.rackspace.vtm.client.traffic.ip.TrafficIpBasic;
import org.rackspace.vtm.client.traffic.ip.TrafficIpProperties;
import org.rackspace.vtm.client.util.EnumFactory;
import org.rackspace.vtm.client.virtualserver.*;

import java.io.IOException;
import java.util.*;

public class VTMResourceTranslator {
    public Pool cPool;
    public Monitor cMonitor;
    public Map<String, TrafficIp> cTrafficIpGroups;
    public VirtualServer cVServer;
    public VirtualServer cRedirectVServer;
    public Protection cProtection;
    public Bandwidth cBandwidth;
    public Keypair cKeypair;
    public Map<String, Keypair> cKeypairMappings;
    protected static final ZeusUtils zeusUtil;

    static {
        zeusUtil = new ZeusUtils();
    }

    public static VTMResourceTranslator getNewResourceTranslator() {
        return new VTMResourceTranslator();
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
        if (loadBalancer.getCertificateMappings() != null) translateKeypairMappingsResource(loadBalancer, careAboutCert);
        if ((loadBalancer.getAccessLists() != null && !loadBalancer.getAccessLists().isEmpty()) || loadBalancer.getConnectionLimit() != null)
            translateProtectionResource(loadBalancer);

        translatePoolResource(vsName, loadBalancer, queLb);
        translateVirtualServerResource(config, vsName, loadBalancer);
        if (loadBalancer.getHttpsRedirect() != null && loadBalancer.getHttpsRedirect()) {
            translateRedirectVirtualServerResource(config, vsName, loadBalancer);
        }
    }

    //This could probably be trimmed down a bit
    public VirtualServer translateRedirectVirtualServerResource(LoadBalancerEndpointConfiguration config, String vsName, LoadBalancer loadBalancer) throws InsufficientRequestException {
        VirtualServerBasic basic = new VirtualServerBasic();
        VirtualServerSsl ssl = new VirtualServerSsl();
        VirtualServerProperties properties = new VirtualServerProperties();
        VirtualServerConnectionErrors ce = new VirtualServerConnectionErrors();
        VirtualServerLog log;
        List<String> rules = new ArrayList<String>();

        properties.setBasic(basic);
        cRedirectVServer = new VirtualServer();
        cRedirectVServer.setProperties(properties);

        basic.setEnabled(true);

        //protection class settings
        if ((loadBalancer.getAccessLists() != null && !loadBalancer.getAccessLists().isEmpty()) || loadBalancer.getConnectionLimit() != null) {
            basic.setProtectionClass(ZxtmNameBuilder.genVSName(loadBalancer));
        }

        // Redirection specific
        basic.setPort(80);
        basic.setPool("discard");
        basic.setProtocol(VirtualServerBasic.Protocol.HTTP);

        log = new VirtualServerLog();
        log.setEnabled(false);
        properties.setLog(log);

        //error file settings
        if (loadBalancer.getUserPages() != null && loadBalancer.getUserPages().getErrorpage() != null) {
            ce.setErrorFile(ZxtmNameBuilder.generateErrorPageName(ZxtmNameBuilder.genRedirectVSName(loadBalancer)));
        } else {
            ce.setErrorFile("Default");
        }
        properties.setConnectionErrors(ce);

        //trafficscript or rule settings
        rules.add(VTMConstants.HTTPS_REDIRECT);
        basic.setRequestRules(rules);

        //trafficIpGroup settings
        basic.setListenOnAny(false);
        basic.setListenOnTrafficIps(genGroupNameSet(loadBalancer));

        //ssl settings
        ssl.setServerCertDefault(vsName);

        return cRedirectVServer;
    }

    public VirtualServer translateVirtualServerResource(LoadBalancerEndpointConfiguration config,
                                                        String vsName, LoadBalancer loadBalancer) throws InsufficientRequestException {
        VirtualServerBasic basic = new VirtualServerBasic();
        VirtualServerSsl ssl = new VirtualServerSsl();
        VirtualServerProperties properties = new VirtualServerProperties();
        VirtualServerConnectionErrors ce = new VirtualServerConnectionErrors();
        VirtualServerTcp tcp = new VirtualServerTcp();
        VirtualServerLog log;
        List<String> rules = new ArrayList<String>();

        properties.setBasic(basic);

        cVServer = new VirtualServer();
        cVServer.setProperties(properties);

        //basic virtual server settings
        if (vsName.equals(ZxtmNameBuilder.genSslVSName(loadBalancer))) {
            basic.setPort(loadBalancer.getSslTermination().getSecurePort());
            basic.setSslDecrypt(true);
            basic.setEnabled(loadBalancer.isUsingSsl());

            // Set cipher profiles
            SslCipherProfile cipherProfile = loadBalancer.getSslTermination().getCipherProfile();
            if(cipherProfile != null) {
                ssl.setCipherSuites(cipherProfile.getCiphers());
            }

            // Set TLS version enable/disable
            ssl.setSupportTls1(loadBalancer.getSslTermination().isTls10Enabled() ? VirtualServerSsl.SupportTls1.ENABLED : VirtualServerSsl.SupportTls1.DISABLED);
            ssl.setSupportTls11(loadBalancer.getSslTermination().isTls11Enabled() ? VirtualServerSsl.SupportTls11.ENABLED : VirtualServerSsl.SupportTls11.DISABLED);

            if (loadBalancer.getProtocol() == LoadBalancerProtocol.HTTP) {
                ssl.setAddHttpHeaders(true);
                VirtualServerHttp virtualServerHttp = new VirtualServerHttp();
                virtualServerHttp.setLocationRewrite(VirtualServerHttp.LocationRewrite.NEVER);
                properties.setHttp(virtualServerHttp);
            }

            // certificate mappings, related certificate/key pairs should be imported by now
            List<VirtualServerServerCertHostMapping> cmappings = new ArrayList<>();
            if (loadBalancer.getCertificateMappings() != null ) {
                Set<CertificateMapping> certMappings = loadBalancer.getCertificateMappings();
                for (CertificateMapping cm : certMappings) {
                    VirtualServerServerCertHostMapping vcm = new VirtualServerServerCertHostMapping();
                    vcm.setHost(cm.getHostName());
                    vcm.setCertificate(ZxtmNameBuilder.generateCertificateName(loadBalancer.getId(),
                            loadBalancer.getAccountId(), cm.getId()));
                    cmappings.add(vcm);
                }
                ssl.setServerCertHostMapping(cmappings);
            }
            properties.setSsl(ssl);
        } else {
            basic.setPort(loadBalancer.getPort());
            if (loadBalancer.hasSsl()) {
                basic.setEnabled(!loadBalancer.isSecureOnly());
            } else {
                basic.setEnabled(true);
            }
        }

        basic.setPool(ZxtmNameBuilder.genVSName(loadBalancer));
        basic.setProtocol(VirtualServerBasic.Protocol.fromValue(ZxtmConversionUtils.mapProtocol(loadBalancer.getProtocol()).getValue()));

        //protection class settings
        if ((loadBalancer.getAccessLists() != null && !loadBalancer.getAccessLists().isEmpty()) || loadBalancer.getConnectionLimit() != null) {
            basic.setProtectionClass(ZxtmNameBuilder.genVSName(loadBalancer));
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
        // Default
        VirtualServerWebCache cache = new VirtualServerWebCache();
        cache.setEnabled(false);
        properties.setWebCache(cache);
        if (loadBalancer.isContentCaching() != null) {
            if (loadBalancer.isContentCaching()) {
                cache.setEnabled(true);
                properties.setWebCache(cache);
                rules.add(VTMConstants.CONTENT_CACHING);
            }
        }

        //error file settings
        if (loadBalancer.getUserPages() != null && loadBalancer.getUserPages().getErrorpage() != null) {
            ce.setErrorFile(ZxtmNameBuilder.generateErrorPageName(vsName));
        } else {
            ce.setErrorFile("Default");
        }
        properties.setConnectionErrors(ce);

        //X_forwarded headers
        if (loadBalancer.getProtocol() == LoadBalancerProtocol.HTTP) {
            VirtualServerHttp vshttp = new VirtualServerHttp();
            vshttp.setAddXForwardedFor(true);
            vshttp.setAddXForwardedProto(true);
            rules.add(VTMConstants.XFPORT);
            properties.setHttp(vshttp);
        }

        //trafficscript or rule settings
        basic.setRequestRules(rules);

        //Half closed proxy settings
        tcp.setProxyClose(loadBalancer.getHalfClosed());
        properties.setTcp(tcp);

        //trafficIpGroup settings
        basic.setListenOnAny(false);
        basic.setListenOnTrafficIps(genGroupNameSet(loadBalancer));

        //ssl settings
        ssl.setServerCertDefault(vsName);

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

        basic.setHashSourcePort(false);
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
        List<PoolNodesTable> poolNodesTable = new ArrayList<>();
        PoolLoadbalancing poollb = new PoolLoadbalancing();

        Set<Node> nodes = loadBalancer.getNodes();

        basic.setPassiveMonitoring(false);

        ZeusNodePriorityContainer znpc = new ZeusNodePriorityContainer(nodes);
        poollb.setPriorityEnabled(znpc.hasSecondary());

        String lbAlgo = loadBalancer.getAlgorithm().name().toLowerCase();

        for (Node node : nodes) {
            PoolNodesTable pnt = new PoolNodesTable();
            pnt.setNode(IpHelper.createZeusIpString(node.getIpAddress(), node.getPort()));
            pnt.setPriority(node.getType() == NodeType.PRIMARY ? 2 : 1);
            String condition = node.getCondition().toString().equals("ENABLED") ? "active" : node.getCondition().toString().toLowerCase();
            pnt.setState(PoolNodesTable.State.fromValue(condition));
            if (lbAlgo.equals(EnumFactory.AcceptFrom.WEIGHTED_ROUND_ROBIN.toString())
                    || lbAlgo.equals(EnumFactory.AcceptFrom.WEIGHTED_LEAST_CONNECTIONS.toString())) {
                pnt.setWeight(node.getWeight());
            }
            poolNodesTable.add(pnt);
//            pnt.setSourceIp();
        }

        poollb.setAlgorithm(PoolLoadbalancing.Algorithm.fromValue(lbAlgo));

        PoolConnection connection = null;
        if (loadBalancer.getTimeout() != null) {
            connection = new PoolConnection();
            connection.setMaxReplyTime(loadBalancer.getTimeout());
        }

        if (loadBalancer.getHealthMonitor() != null)
            basic.setMonitors(new HashSet<String>(Arrays.asList(vsName)));

        if ((loadBalancer.getSessionPersistence() != null) && !(loadBalancer.getSessionPersistence().name().equals(SessionPersistence.NONE.name()))) {
            basic.setPersistenceClass(loadBalancer.getSessionPersistence().name());
        } else {
            basic.setPersistenceClass("");
        }

        if (loadBalancer.getRateLimit() != null) {
            basic.setBandwidthClass(vsName);
        } else {
            basic.setBandwidthClass(null);
        }

        basic.setNodesTable(poolNodesTable);
        properties.setBasic(basic);
        properties.setLoadBalancing(poollb);
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
            basic.setType(MonitorBasic.Type.CONNECT);
        } else if (hm.getType().equals(HealthMonitorType.HTTP) || hm.getType().equals(HealthMonitorType.HTTPS)) {
            basic.setType(MonitorBasic.Type.HTTP);
            http = new MonitorHttp();
            http.setPath(hm.getPath());
            http.setStatusRegex(hm.getStatusRegex());
            http.setBodyRegex(hm.getBodyRegex());
            http.setHostHeader(hm.getHostHeader());
            if (hm.getType().equals(HealthMonitorType.HTTPS)) {
                basic.setUseSsl(true);
            } else {
                basic.setUseSsl(false);
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
        properties.setAccessRestriction(pac);

        ProtectionConcurrentConnections protectionConcurrentConnections = new ProtectionConcurrentConnections();
        ProtectionConnectionRate limiting = new ProtectionConnectionRate();
        if (limits != null) {
            Integer maxConnections = limits.getMaxConnections();
            if (maxConnections == null) maxConnections = 0;
            protectionConcurrentConnections.setMax1Connections(maxConnections);

            /* Zeus bug requires us to set per-process to false and ignore most of these settings */
            protectionConcurrentConnections.setPerProcessConnectionCount(false);
//            #NOTE: These values are  currently deprecated
            protectionConcurrentConnections.setMinConnections(ConnectionThrottleDefaultConstants.getMinConnections());
            limiting.setRateTimer(ConnectionThrottleDefaultConstants.getRateInterval());
            limiting.setMaxConnectionRate(ConnectionThrottleDefaultConstants.getMaxConnectionRate());
            // We wont be using this, but it must be set to 0 as our default
            protectionConcurrentConnections.setMax10Connections(0);
        } else {
            protectionConcurrentConnections.setMax1Connections(0);

            /* Zeus bug requires us to set per-process to false */
            protectionConcurrentConnections.setPerProcessConnectionCount(false);
            protectionConcurrentConnections.setMinConnections(0);
            limiting.setRateTimer(1);
            limiting.setMaxConnectionRate(0);
            protectionConcurrentConnections.setMax10Connections(0);
        }
        properties.setConnectionRate(limiting);
        properties.setConcurrentConnections(protectionConcurrentConnections);

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

    public Map<String, Keypair> translateKeypairMappingsResource(LoadBalancer loadBalancer,
                                                    boolean careAboutCert) throws InsufficientRequestException {
        Integer lbId = loadBalancer.getId();
        Integer accountId = loadBalancer.getAccountId();

        // Ensure we're using a fresh map
        cKeypairMappings = new HashMap<>();

        for (CertificateMapping cm : loadBalancer.getCertificateMappings()) {

            String certificateName = ZxtmNameBuilder.generateCertificateName(lbId, accountId, cm.getId());

            ZeusCrtFile zeusCertFile = zeusUtil.buildZeusCrtFileLbassValidation(cm.getPrivateKey(),
                    cm.getCertificate(), cm.getIntermediateCertificate());
            if (zeusCertFile.hasFatalErrors()) {
                String fmt = "StingrayCertFile generation Failure: %s";
                String errors = StringUtils.joinString(zeusCertFile.getErrors(), ",");
                String msg = String.format(fmt, errors);

                if (careAboutCert)
                    throw new InsufficientRequestException(msg);
                else
                    return null;
            }

            Keypair kp = new Keypair();
            KeypairProperties keypairProperties = new KeypairProperties();
            KeypairBasic keypairBasic = new KeypairBasic();
            keypairBasic.setPrivate(zeusCertFile.getPrivate_key());
            keypairBasic.setPublic(zeusCertFile.getPublic_cert());
            keypairProperties.setBasic(keypairBasic);
            kp.setProperties(keypairProperties);
            cKeypairMappings.put(certificateName, kp);
        }
        return cKeypairMappings;
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

    public Map<String, Keypair> getcKeypairMappings() {
        return cKeypairMappings;
    }

    public void setcKeypairMappings(Map<String, Keypair> cKeypairMappings) {
        this.cKeypairMappings = cKeypairMappings;
    }
}
