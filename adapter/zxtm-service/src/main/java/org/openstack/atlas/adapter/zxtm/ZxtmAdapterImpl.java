package org.openstack.atlas.adapter.zxtm;

import com.zxtm.service.client.*;
import org.apache.axis.AxisFault;
import org.apache.axis.types.UnsignedInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.*;
import org.openstack.atlas.adapter.helpers.*;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.*;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.util.ca.StringUtils;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;
import org.openstack.atlas.util.converters.StringConverter;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.springframework.stereotype.Component;
import com.zxtm.service.client.VirtualServerSSLSupportTLS1;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;
import org.openstack.atlas.util.constants.ConnectionThrottleDefaultConstants;
import static org.openstack.atlas.service.domain.entities.SessionPersistence.NONE;

import static org.apache.commons.lang.StringUtils.EMPTY;


@Component
public class ZxtmAdapterImpl implements ReverseProxyLoadBalancerAdapter {

    public static Log LOG = LogFactory.getLog(ZxtmAdapterImpl.class.getName());
    public static final LoadBalancerAlgorithm DEFAULT_ALGORITHM = LoadBalancerAlgorithm.RANDOM;
    public static final String SOURCE_IP = "SOURCE_IP";
    public static final String HTTP_COOKIE = "HTTP_COOKIE";
    public static final String SSL_ID = "SSL_ID";
    public static final String RATE_LIMIT_HTTP = "rate_limit_http";
    public static final String RATE_LIMIT_NON_HTTP = "rate_limit_nonhttp";
    public static final String CONTENT_CACHING = "content_caching";
    public static final String XFF = "add_x_forwarded_for_header";
    public static final String XFP = "add_x_forwarded_proto";
    public static final String XFPORT = "add_x_forwarded_port";
    public static final String HTTPS_REDIRECT = "force_https_redirect";
    public static final VirtualServerRule ruleRateLimitHttp = new VirtualServerRule(RATE_LIMIT_HTTP, true, VirtualServerRuleRunFlag.run_every);
    public static final VirtualServerRule ruleRateLimitNonHttp = new VirtualServerRule(RATE_LIMIT_NON_HTTP, true, VirtualServerRuleRunFlag.run_every);
    public static final VirtualServerRule ruleXForwardedPort = new VirtualServerRule(XFPORT, true, VirtualServerRuleRunFlag.run_every);
    public static final VirtualServerRule ruleXForwardedFor = new VirtualServerRule(XFF, true, VirtualServerRuleRunFlag.run_every);
    public static final VirtualServerRule ruleXForwardedProto = new VirtualServerRule(XFP, true, VirtualServerRuleRunFlag.run_every);
    public static final VirtualServerRule ruleContentCaching = new VirtualServerRule(CONTENT_CACHING, true, VirtualServerRuleRunFlag.run_every);
    public static final VirtualServerRule ruleForceHttpsRedirect = new VirtualServerRule(HTTPS_REDIRECT, true, VirtualServerRuleRunFlag.run_every);
    protected static final ZeusUtils zeusUtil;
    public static final String NON_HTTP_LOG_FORMAT = "%v %t %h %A:%p %n %B %b %T";
    public static final String HTTP_LOG_FORMAT = "%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\" %n";

    static {
        zeusUtil = new ZeusUtils();
    }

    @Override
    public ZxtmServiceStubs getServiceStubs(LoadBalancerEndpointConfiguration config) throws AxisFault {
        return ZxtmServiceStubs.getServiceStubs(config.getEndpointUrl(), config.getUsername(), config.getPassword());
    }

    @Override
    public ZxtmServiceStubs getStatsStubs(URL endpoint, LoadBalancerEndpointConfiguration config) throws AxisFault {
        return ZxtmServiceStubs.getServiceStubs(endpoint, config.getUsername(), config.getPassword());
    }

    @Override
    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {

        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String virtualServerName = ZxtmNameBuilder.genVSName(lb);

        final String poolName = ZxtmNameBuilder.genVSName(lb.getId(), lb.getAccountId());
        final VirtualServerBasicInfo vsInfo;

        LoadBalancerAlgorithm algorithm = lb.getAlgorithm() == null ? DEFAULT_ALGORITHM : lb.getAlgorithm();
        final String rollBackMessage = "Create load balancer request canceled.";

        LOG.debug(String.format("Creating load balancer '%s'...", virtualServerName));

        try {
            createNodePool(config, lb.getId(), lb.getAccountId(), lb.getNodes(), algorithm);
            setNodesPriorities(config, poolName, lb);
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            deleteNodePool(serviceStubs, poolName);
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        try {
            LOG.debug(String.format("Adding virtual server '%s'...", virtualServerName));
            vsInfo = new VirtualServerBasicInfo(lb.getPort(), ZxtmConversionUtils.mapProtocol(lb.getProtocol()), poolName);
            serviceStubs.getVirtualServerBinding().addVirtualServer(new String[]{virtualServerName}, new VirtualServerBasicInfo[]{vsInfo});
            serviceStubs.getVirtualServerBinding().setAddXForwardedForHeader(new String[]{virtualServerName}, new boolean[]{true});
            serviceStubs.getVirtualServerBinding().setAddXForwardedProtoHeader(new String[]{virtualServerName}, new boolean[]{true});
            LOG.info(String.format("Virtual server '%s' successfully added.", virtualServerName));
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                throw new ObjectAlreadyExists();
            }
            LOG.error(StringConverter.getExtendedStackTrace(e));
            deleteVirtualServer(serviceStubs, virtualServerName);
            deleteNodePool(serviceStubs, poolName);
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        try {
            addVirtualIps(config, lb, virtualServerName);
            //Verify that the server is not listening to all addresses, zeus does this by default and is an unwanted behaviour.
            isVSListeningOnAllAddresses(serviceStubs, virtualServerName, poolName);
            serviceStubs.getVirtualServerBinding().setEnabled(new String[]{virtualServerName}, new boolean[]{true});

            updateLoadBalancerAttributes(config, serviceStubs, lb, virtualServerName);


            if (lb.getTimeout() != null) {
                updateTimeout(config, lb);
            }

            //Added rules for HTTP LB
            if (lb.getProtocol().equals(LoadBalancerProtocol.HTTP)) {
                TrafficScriptHelper.addXForwardedPortScriptIfNeeded(serviceStubs);
                attachXFPORTRuleToVirtualServer(serviceStubs, virtualServerName);
                serviceStubs.getVirtualServerBinding().setAddXForwardedForHeader(new String[]{virtualServerName}, new boolean[]{true});
                serviceStubs.getVirtualServerBinding().setAddXForwardedProtoHeader(new String[]{virtualServerName}, new boolean[]{true});

                setDefaultErrorFile(config, lb);
            }
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            deleteLoadBalancer(config, lb);
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        LOG.info(String.format("Load balancer '%s' successfully created.", virtualServerName));
    }

    @Override
    public void updateLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, StmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String name = ZxtmNameBuilder.genVSName(loadBalancer);
        final String rollBackMessage = "Sync load balancer request canceled.";

        if (!Arrays.asList(serviceStubs.getPoolBinding().getPoolNames()).contains(name)) {
            LOG.debug(String.format("Adding node pool '%s'...", name));
            createNodePool(config, loadBalancer.getId(), loadBalancer.getAccountId(), loadBalancer.getNodes(), loadBalancer.getAlgorithm());
            LOG.info(String.format("Node pool '%s' successfully added.", name));
        } else {
            // Create a HM if it exists first, because the pool needs it in the next step
            // We ran into this weird scenario because of CR7 migration using the REST API (possibly?)
            if (loadBalancer.getHealthMonitor() != null) {
                // If the Health Monitor is HTTPS Type, remove and recreate it in case it is broken per the REST Create bug
                if (loadBalancer.getHealthMonitor().getType().equals(HealthMonitorType.HTTPS)) {
                    removeHealthMonitor(config, loadBalancer);
                }
                updateHealthMonitor(config, loadBalancer);
            }

            setLoadBalancingAlgorithm(config, loadBalancer.getId(), loadBalancer.getAccountId(), loadBalancer.getAlgorithm());
            setNodes(config, loadBalancer);
            serviceStubs.getPoolBinding().setPassiveMonitoring(new String[]{name}, new boolean[]{false});
        }

        if (!(loadBalancer.isHttpsRedirect() != null && loadBalancer.isHttpsRedirect() && loadBalancer.hasSsl())
                && !Arrays.asList(serviceStubs.getVirtualServerBinding().getVirtualServerNames()).contains(name)) {
            LOG.debug(String.format("Adding virtual server '%s'...", name));
            VirtualServerBasicInfo vsInfo = new VirtualServerBasicInfo(loadBalancer.getPort(), ZxtmConversionUtils.mapProtocol(loadBalancer.getProtocol()), name);
            serviceStubs.getVirtualServerBinding().addVirtualServer(new String[]{name}, new VirtualServerBasicInfo[]{vsInfo});
            LOG.info(String.format("Virtual server '%s' successfully added.", name));
        }

        if (Arrays.asList(serviceStubs.getVirtualServerBinding().getVirtualServerNames()).contains(name)) {
            serviceStubs.getVirtualServerBinding().setAddXForwardedForHeader(new String[]{name}, new boolean[]{true});
            serviceStubs.getVirtualServerBinding().setAddXForwardedProtoHeader(new String[]{name}, new boolean[]{true});
            if (!loadBalancer.getProtocol().equals(LoadBalancerProtocol.HTTP)) {
                try {
                    serviceStubs.getVirtualServerBinding().removeRules(new String[]{name}, new String[][]{{ZxtmAdapterImpl.ruleXForwardedPort.getName()}});
                } catch (InvalidInput e) {
                    LOG.info(String.format("X-Forwarded-Port rule doesn't exist on virtual server '%s': ignoring....", name));
                }
            }
            serviceStubs.getVirtualServerBinding().setProtocol(new String[]{name}, new VirtualServerProtocol[]{ZxtmConversionUtils.mapProtocol(loadBalancer.getProtocol())});
            serviceStubs.getVirtualServerBinding().setPort(new String[]{name}, new UnsignedInt[]{new UnsignedInt(loadBalancer.getPort())});
            addVirtualIps(config, loadBalancer, name);
            try {
                isVSListeningOnAllAddresses(serviceStubs, name, name);
            } catch (Exception e) {
                LOG.error(StringConverter.getExtendedStackTrace(e));
                throw new ZxtmRollBackException(rollBackMessage, e);
            }
            serviceStubs.getVirtualServerBinding().setEnabled(new String[]{name}, new boolean[]{true});
            if (loadBalancer.getProtocol().equals(LoadBalancerProtocol.HTTP)) {
                TrafficScriptHelper.addXForwardedPortScriptIfNeeded(serviceStubs);
                attachXFPORTRuleToVirtualServer(serviceStubs, name);
            }
        }

        // Attempt to pre-fix cert-mappings if they exist, since updating the SSL Term + CertMappings + AttributeSync is a chicken+egg problem
        boolean certMappingsFixed = false;
        try {
            if (loadBalancer.getSslTermination() != null && loadBalancer.isUsingSsl() && loadBalancer.getCertificateMappings() != null) {
                // Remove any cert mappings FIRST just in case
                removeAllCertificateMappings(config, loadBalancer.getId(), loadBalancer.getAccountId());
                for (CertificateMapping certificateMapping : loadBalancer.getCertificateMappings()) {
                    updateCertificateMapping(config, loadBalancer.getId(), loadBalancer.getAccountId(), certificateMapping);
                }
            }
            certMappingsFixed = true;
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
        }

        syncLoadBalancerAttributes(config, serviceStubs, loadBalancer, name); // This creates the RedirectVS as a side-effect if it needs to exist

        if (loadBalancer.getTimeout() != null) {
            updateTimeout(config, loadBalancer);
        }

        if (loadBalancer.getSslTermination() != null && loadBalancer.isUsingSsl()) {
            ZeusSslTermination sslTerm = new ZeusSslTermination();
            sslTerm.setCertIntermediateCert(loadBalancer.getSslTermination().getCertificate());
            sslTerm.setSslTermination(loadBalancer.getSslTermination());
            updateSslTermination(config, loadBalancer, sslTerm);

            if (!certMappingsFixed) {
                for (CertificateMapping certificateMapping : loadBalancer.getCertificateMappings()) {
                    updateCertificateMapping(config, loadBalancer.getId(), loadBalancer.getAccountId(), certificateMapping);
                }
            }
        }

        if (loadBalancer.getUserPages() != null && loadBalancer.getUserPages().getErrorpage() != null) {
            setErrorFile(config, loadBalancer, loadBalancer.getUserPages().getErrorpage());
        } else {
            setDefaultErrorFile(config, loadBalancer);
        }

        LOG.info(String.format("Load balancer '%s' successfully synced.", name));
    }

    private void createRedirectVirtualServer(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {

        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String virtualServerName = ZxtmNameBuilder.genRedirectVSName(lb);
        final String poolName = "discard";
        final VirtualServerBasicInfo vsInfo;

        final String rollBackMessage = "Create load balancer request canceled.";

        LOG.debug(String.format("Creating load balancer '%s'...", virtualServerName));

        try {
            LOG.debug(String.format("Adding virtual server '%s'...", virtualServerName));
            vsInfo = new VirtualServerBasicInfo(80, VirtualServerProtocol.http, poolName);
            serviceStubs.getVirtualServerBinding().addVirtualServer(new String[]{virtualServerName}, new VirtualServerBasicInfo[]{vsInfo});
            LOG.info(String.format("Virtual server '%s' successfully added.", virtualServerName));
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                throw new ObjectAlreadyExists();
            }
            LOG.error(StringConverter.getExtendedStackTrace(e));
            deleteVirtualServer(serviceStubs, virtualServerName);
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        try {
            addVirtualIps(config, lb, virtualServerName);
            //Verify that the server is not listening to all addresses, zeus does this by default and is an unwanted behaviour.
            isVSListeningOnAllAddresses(serviceStubs, virtualServerName, poolName);
            TrafficScriptHelper.addForceHttpsRedirectScriptIfNeeded(serviceStubs);
            attachForceHttpsRedirectRuleToVirtualServer(serviceStubs, virtualServerName);
            serviceStubs.getVirtualServerBinding().setEnabled(new String[]{virtualServerName}, new boolean[]{true});
            updateLoadBalancerAttributes(config, serviceStubs, lb, virtualServerName);

            if (lb.getTimeout() != null) {
                updateTimeout(config, lb);
            }
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            deleteVirtualServer(serviceStubs, virtualServerName);
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        LOG.info(String.format("Load balancer '%s' successfully created.", virtualServerName));
    }

    private void createSecureVirtualServer(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        final String rollBackMessage = "Create load balancer request canceled.";
        final String secureVsName;
        final String vsName;
        final String poolName;
        final VirtualServerBasicInfo vsInfo;

        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        secureVsName = ZxtmNameBuilder.genSslVSName(lb.getId(), lb.getAccountId());
        vsName = ZxtmNameBuilder.genVSName(lb.getId(), lb.getAccountId());
        //Same name, but differentiate for clarity
        poolName = vsName;
        String vsRedirectName = ZxtmNameBuilder.genRedirectVSName(vsName);

        boolean isRedirectServer = lb.isHttpsRedirect() != null && lb.isHttpsRedirect() == true;
        String vsNameAltered = isRedirectServer ? vsRedirectName : vsName;

        LOG.debug(String.format("Adding virtual server '%s'...", secureVsName));
        vsInfo = new VirtualServerBasicInfo(lb.getSslTermination().getSecurePort(), ZxtmConversionUtils.mapProtocol(lb.getProtocol()), poolName);
        try {
            serviceStubs.getVirtualServerBinding().addVirtualServer(new String[]{secureVsName}, new VirtualServerBasicInfo[]{vsInfo});
            LOG.info(String.format("Virtual server '%s' successfully added.", secureVsName));
        } catch (ObjectAlreadyExists oae) {
            LOG.info(String.format("Virtual server '%s' already exists, moving on.", secureVsName));
        }

        try {
            addVirtualIps(config, lb, secureVsName);

            //Verify that the server is not listening to all addresses, zeus does this by default and is an unwanted behaviour.
            isVSListeningOnAllAddresses(serviceStubs, secureVsName, poolName);
            serviceStubs.getVirtualServerBinding().setEnabled(new String[]{secureVsName}, new boolean[]{true});

            updateLoadBalancerAttributes(config, serviceStubs, lb, secureVsName);

            if (lb.getRateLimit() != null) {
                setRateLimit(config, secureVsName, lb.getRateLimit());
            }

            //Added rules for HTTP LB
            if (lb.getProtocol().equals(LoadBalancerProtocol.HTTP)) {
                TrafficScriptHelper.addXForwardedPortScriptIfNeeded(serviceStubs);
                attachXFPORTRuleToVirtualServer(serviceStubs, secureVsName);
                serviceStubs.getVirtualServerBinding().setAddXForwardedForHeader(new String[]{secureVsName}, new boolean[]{true});
                serviceStubs.getVirtualServerBinding().setAddXForwardedProtoHeader(new String[]{secureVsName}, new boolean[]{true});

//
//                TrafficScriptHelper.addXForwardedProtoScriptIfNeeded(serviceStubs);
//                attachXFPRuleToVirtualServer(serviceStubs, secureVsName);

                LOG.info(String.format("Enabling SSL Headers for virtual server: %s", secureVsName));
                serviceStubs.getVirtualServerBinding().setSSLHeaders(new String[]{secureVsName}, new boolean[]{true});

                LOG.info(String.format("Disabling HOST Header rewrite on secure virtual server: %s", secureVsName));
                VirtualServerLocationDefaultRewriteMode never = VirtualServerLocationDefaultRewriteMode.never;
                serviceStubs.getVirtualServerBinding().setLocationDefaultRewriteMode(new String[]{secureVsName}, new VirtualServerLocationDefaultRewriteMode[]{never});
                LOG.info(String.format("Succesfully disabled HOST Header rewrite on secure virtual server: %s", secureVsName));


                String[] errorFile;
                errorFile = serviceStubs.getVirtualServerBinding().getErrorFile(new String[]{vsNameAltered});
//                efContents = serviceStubs.getZxtmConfExtraBinding().getFile(new String[]{poolName});
                if (errorFile[0].equals("Default") || errorFile[0].equals(Constants.DEFAULT_ERRORFILE)) {
                    setDefaultErrorFile(config, secureVsName);
                } else {
                    setErrorFile(config, secureVsName, lb.getUserPages().getErrorpage());
                }
            }

        } catch (Exception e) {
            if (e instanceof ObjectAlreadyExists) {
                throw new ObjectAlreadyExists();
            } else {
                LOG.error(StringConverter.getExtendedStackTrace(e));
                deleteVirtualServer(serviceStubs, secureVsName);
                removeSslTermination(config, lb);
                throw new ZxtmRollBackException(rollBackMessage, e);
            }
        }
        LOG.info(String.format("Secure virtual server '%s' successfully created.", secureVsName));
    }

    private void repurposeVirtualServerForHttpsRedirect(LoadBalancer lb, ZxtmServiceStubs serviceStubs)
            throws InsufficientRequestException {
        if (lb.getSslTermination().isSecureTrafficOnly() != true) {
            return;
        }
        String virtualServerName = ZxtmNameBuilder.genVSName(lb);
        String virtualServerRedirectName = ZxtmNameBuilder.genRedirectVSName(lb);

        try {
            TrafficScriptHelper.addForceHttpsRedirectScriptIfNeeded(serviceStubs);
            attachForceHttpsRedirectRuleToVirtualServer(serviceStubs, virtualServerName);
            removeXFPORTRuleFromVirtualServer(serviceStubs, virtualServerName);
            serviceStubs.getVirtualServerBinding().renameVirtualServer(new String[]{virtualServerName}, new String[]{virtualServerRedirectName});
            serviceStubs.getVirtualServerBinding().setEnabled(new String[]{virtualServerRedirectName}, new boolean[]{true});
        } catch (RemoteException e) {
            e.printStackTrace();
            LOG.error(StringConverter.getExtendedStackTrace(e));
            //throw rollback?
        }
    }

    private void restoreVirtualServerFromHttpsRedirect(LoadBalancer lb, ZxtmServiceStubs serviceStubs)
            throws InsufficientRequestException {
        String virtualServerName = ZxtmNameBuilder.genVSName(lb);
        String virtualServerRedirectName = ZxtmNameBuilder.genRedirectVSName(lb);

        try {
            serviceStubs.getVirtualServerBinding().setEnabled(new String[]{virtualServerRedirectName}, new boolean[]{false});
            serviceStubs.getVirtualServerBinding().renameVirtualServer(new String[]{virtualServerRedirectName}, new String[]{virtualServerName});
            removeForceHttpsRedirectRuleFromVirtualServer(serviceStubs, virtualServerName);
            attachXFPORTRuleToVirtualServer(serviceStubs, virtualServerName);
        } catch (RemoteException e) {
            LOG.info(String.format("Couldn't restore VS '%s' to '%s' because it doesn't exist, possibly it was already done.", virtualServerRedirectName, virtualServerName));
            LOG.error(StringConverter.getExtendedStackTrace(e));
            //throw rollback?
        }
    }

    private void syncLoadBalancerAttributes(LoadBalancerEndpointConfiguration config, ZxtmServiceStubs serviceStubs, LoadBalancer lb, String virtualServerName)
            throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        String redirectName = ZxtmNameBuilder.genRedirectVSName(lb);

        if (lb.getSessionPersistence() != null && !lb.getSessionPersistence().equals(NONE)) {
            setSessionPersistence(config, lb.getId(), lb.getAccountId(), lb.getSessionPersistence());
        } else if ((lb.getSessionPersistence() == null || lb.getSessionPersistence().equals(NONE) || lb.hasSsl())
                && serviceStubs.getPoolBinding().getPersistence(new String[]{virtualServerName}).length != 0) {
            removeSessionPersistence(config, lb.getId(), lb.getAccountId());
        }

        if (lb.getHealthMonitor() != null) {
            updateHealthMonitor(config, lb);
        } else if (lb.getHealthMonitor() == null && Arrays.asList(serviceStubs.getMonitorBinding().getCustomMonitorNames()).contains(virtualServerName)) {
            removeHealthMonitor(config, lb);
        }

        if (lb.getConnectionLimit() != null) {
            updateConnectionThrottle(config, lb);
        } else if (Arrays.asList(serviceStubs.getProtectionBinding().getProtectionNames()).contains(virtualServerName)) {
            deleteConnectionThrottle(config, lb);
        }

        if (lb.isConnectionLogging() == null) {
            lb.setConnectionLogging(false);
        }
        updateConnectionLogging(config, lb);

        if (lb.isContentCaching() == null) {
            lb.setContentCaching(false);
        }
        updateContentCaching(config, lb);

        if (lb.getAccessLists() != null && !lb.getAccessLists().isEmpty()) {
            updateAccessList(config, lb);
        } else if (Arrays.asList(serviceStubs.getProtectionBinding().getProtectionNames()).contains(virtualServerName)) {
            deleteAccessList(config, virtualServerName);
        }

        if (lb.isHalfClosed() == null) {
            lb.setHalfClosed(false);
        }
        updateHalfClosed(config, lb);

        if ((lb.isHttpsRedirect() != null && !virtualServerName.equals(redirectName))
                || (lb.isHttpsRedirect() == null && Arrays.asList(
                serviceStubs.getVirtualServerBinding().getVirtualServerNames()).contains(redirectName))) {
            updateHttpsRedirect(config, lb);
        }
    }

    private void updateLoadBalancerAttributes(LoadBalancerEndpointConfiguration config, ZxtmServiceStubs serviceStubs, LoadBalancer lb, String virtualServerName) throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        /* UPDATE REST OF LOADBALANCER CONFIG */
        if (lb.getSessionPersistence() != null && !lb.getSessionPersistence().equals(NONE) && !lb.hasSsl()) {
            //TODO: refactor to take lb only, right now its a pool item and only updates in one place...
            //sessionPersistence is a pool item
            setSessionPersistence(config, lb.getId(), lb.getAccountId(), lb.getSessionPersistence());
        }

        if (lb.getHealthMonitor() != null && !lb.hasSsl()) {
            updateHealthMonitor(config, lb);
        }

        //VirtualServer items
        if (lb.getConnectionLimit() != null) {
            updateConnectionThrottle(config, lb);
        }

        if (lb.isConnectionLogging() != null && lb.isConnectionLogging()) {
            updateConnectionLogging(config, lb);
        }

        if (lb.isContentCaching() != null && lb.isContentCaching()) {
            updateContentCaching(config, lb);
        }

        if (lb.getAccessLists() != null && !lb.getAccessLists().isEmpty()) {
            updateAccessList(config, lb);
        }

        if (lb.isHalfClosed() != null) {
            updateHalfClosed(config, lb);
        }

        if (lb.isHttpsRedirect() != null && !virtualServerName.equals(ZxtmNameBuilder.genRedirectVSName(lb))) {
            updateHttpsRedirect(config, lb);
        }
    }

    private void isVSListeningOnAllAddresses(ZxtmServiceStubs serviceStubs, String virtualServerName, String poolName) throws RemoteException, VirtualServerListeningOnAllAddressesException {
        boolean[] isListening = serviceStubs.getVirtualServerBinding().getListenOnAllAddresses(new String[]{virtualServerName});

        //If The VS is listening on all addresses, rollback pools and VS, Log an exception...
        if (isListening[0]) {
            deleteNodePool(serviceStubs, poolName);
            deleteVirtualServer(serviceStubs, virtualServerName);
            throw new VirtualServerListeningOnAllAddressesException(String.format("The Virtual Server %s was found to be listening on all IP addresses.", virtualServerName));
        }
    }

    @Override
    public void setNodesPriorities(LoadBalancerEndpointConfiguration config, String poolName, LoadBalancer lb) throws RemoteException {
        Set<Node> nodes = lb.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        ZeusNodePriorityContainer znpc = new ZeusNodePriorityContainer(nodes);
        String[] poolNames = new String[]{poolName};

        LOG.debug(String.format("setNodePriority for pool %s priority=%s Starting....", poolNames[0], znpc));
        ZxtmServiceStubs stubs = getServiceStubs(config);
        PoolPriorityValueDefinition[][] priorities = znpc.getPriorityValues();
        try {
            if (priorities != null) {
                stubs.getPoolBinding().setNodesPriorityValue(poolNames, znpc.getPriorityValues());
                if (znpc.hasSecondary()) {
                    boolean[] setTrue = new boolean[]{true};
                    stubs.getPoolBinding().setPriorityEnabled(poolNames, setTrue);
                } else {
                    boolean[] setFalse = new boolean[]{false};
                    stubs.getPoolBinding().setPriorityEnabled(poolNames, setFalse);
                }
            }
        } catch (InvalidInput ex) {
            LOG.warn(String.format("Nodes %s not updated as it no longer exist...", poolNames[0]));
        }
        LOG.debug(String.format("setNodePriority for pool %s priority=%s Finished....", poolNames[0], znpc));
    }

    private void removeAllCertificateMappings(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId) throws RemoteException, InsufficientRequestException {
        final String virtualServerNameSecure = ZxtmNameBuilder.genSslVSName(lbId, accountId);

        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        VirtualServerBindingStub virtualServerService = serviceStubs.getVirtualServerBinding();
        CatalogSSLCertificatesBindingStub certificateCatalogService = serviceStubs.getZxtmCatalogSSLCertificatesBinding();

        try {
            LOG.debug(String.format("Removing certificate mappings for load balancer '%d'...", lbId));
            VirtualServerSSLSite[][] sslSites = virtualServerService.getSSLSites(new String[]{virtualServerNameSecure});
            List<String> siteIpsArray = new ArrayList<String>();
            List<String> siteCertsArray = new ArrayList<String>();

            for (VirtualServerSSLSite sslSite : sslSites[0]) {
                LOG.debug(String.format("Marking SSLSite with host name '%s' for removal for load balancer '%d'...", sslSite.getDest_address(), lbId));
                siteIpsArray.add(sslSite.getDest_address());
                siteCertsArray.add(sslSite.getCertificate());
            }
            try {
                String[] siteIps = new String[siteIpsArray.size()];
                siteIpsArray.toArray(siteIps);
                virtualServerService.deleteSSLSites(new String[]{virtualServerNameSecure}, new String[][]{siteIps});
                for (String certificateName : siteCertsArray) {
                    try {
                        certificateCatalogService.deleteCertificate(new String[]{certificateName});
                    } catch (AxisFault af) {
                        if (af.getFaultString().contains("does not exist")) {
                            LOG.debug(String.format("Failed to delete certificate '%s' for load balancer '%d', because it doesn't exist. Ignoring...", certificateName, lbId));
                        } else {
                            throw af;
                        }
                    }
                }
                LOG.debug(String.format("Successfully removed certificate mappings for load balancer '%d'.", lbId));
            } catch (AxisFault af) {
                LOG.debug(String.format("Cannot remove certificate mappings for load balancer '%d'.", lbId));
                LOG.error(StringConverter.getExtendedStackTrace(af));
            }
        } catch (ObjectDoesNotExist odne) {
            LOG.debug(String.format("Cannot remove certificate mappings for load balancer '%d' as the virtual server does not exist. Ignoring...", lbId));
        }
    }

    @Override
    public void updateCertificateMapping(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, CertificateMapping certMappingToUpdate) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        final String virtualServerNameSecure = ZxtmNameBuilder.genSslVSName(lbId, accountId);
        final String certificateName = ZxtmNameBuilder.generateCertificateName(lbId, accountId, certMappingToUpdate.getId());

        String userKey = certMappingToUpdate.getPrivateKey();
        String userCrt = certMappingToUpdate.getCertificate();
        String imdCrt = certMappingToUpdate.getIntermediateCertificate();
        ZeusCrtFile zeusCrtFile = zeusUtil.buildZeusCrtFileLbassValidation(userKey, userCrt, imdCrt);

        if (zeusCrtFile.hasFatalErrors()) {
            String errors = StringUtils.joinString(zeusCrtFile.getFatalErrorList(), ",");
            String msg = String.format("ZeusCrtFile generation failure: %s", errors);
            throw new InsufficientRequestException(msg);
        }

        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        VirtualServerBindingStub virtualServerService = serviceStubs.getVirtualServerBinding();
        CatalogSSLCertificatesBindingStub certificateCatalogService = serviceStubs.getZxtmCatalogSSLCertificatesBinding();

        try {
            LOG.debug(String.format("Removing certificate mappings for load balancer '%d' that use certificate '%s'...", lbId, certificateName));
            VirtualServerSSLSite[][] sslSites = virtualServerService.getSSLSites(new String[]{virtualServerNameSecure});

            for (VirtualServerSSLSite sslSite : sslSites[0]) {
                if (sslSite.getCertificate().equals(certificateName)) {
                    LOG.debug(String.format("Marking SSLSite with host name '%s' for removal for load balancer '%d' that uses certificate '%s'...", sslSite.getDest_address(), lbId, certificateName));
                    virtualServerService.deleteSSLSites(new String[]{virtualServerNameSecure}, new String[][]{{sslSite.getDest_address()}});
                }
            }

            LOG.debug(String.format("Successfully removed certificate mappings for load balancer '%d' that used certificate '%s'.", lbId, certificateName));
        } catch (ObjectDoesNotExist odne) {
            LOG.debug(String.format("Cannot remove certificate mappings for load balancer '%d' that use certificate '%s' as the virtual server does not exist. Ignoring...", lbId, certificateName));
        } catch (AxisFault axisFault) {
            LOG.error(StringConverter.getExtendedStackTrace(axisFault));
            // Stingray should have returned a proper error, but doesn't sadly. Hence, why we parse the error message.
            if (axisFault.getFaultString().contains("could not be found") || axisFault.getFaultString().contains("Could not find")) {
                LOG.debug(String.format("Cannot remove certificate mappings for load balancer '%d' that use certificate '%s' as at least one mapping does not exist. Ignoring...", lbId, certificateName));
            } else {
                throw axisFault;
            }
        }

        try {
            LOG.debug(String.format("Removing certificate '%s' for certificate mapping '%d' for load balancer '%d'...", certificateName, certMappingToUpdate.getId(), lbId));
            certificateCatalogService.deleteCertificate(new String[]{certificateName});
            LOG.debug(String.format("Successfully removed certificate mapping '%d' for load balancer '%d'.", certMappingToUpdate.getId(), lbId));
        } catch (ObjectDoesNotExist odne) {
            LOG.debug(String.format("Cannot remove certificate '%s' as it does not exist for certificate mapping '%d' for load balancer '%d'. Ignoring...", certificateName, certMappingToUpdate.getId(), lbId));
        }

        try {
            LOG.debug(String.format("Importing certificate '%s' for certificate mapping '%d' for load balancer '%d'...", certificateName, certMappingToUpdate.getId(), lbId));
            CertificateFiles certificateFiles = new CertificateFiles(zeusCrtFile.getPublic_cert(), zeusCrtFile.getPrivate_key());
            certificateCatalogService.importCertificate(new String[]{certificateName}, new CertificateFiles[]{certificateFiles});
            LOG.debug(String.format("Successfully imported certificate '%s' for certificate mapping '%d' for load balancer '%d'.", certificateName, certMappingToUpdate.getId(), lbId));

            VirtualServerSSLSite sslSite = new VirtualServerSSLSite(certMappingToUpdate.getHostName(), certificateName);

            LOG.debug(String.format("Attaching certificate mapping '%d' (certificate '%s') to load balancer %d (virtual server '%s')...", certMappingToUpdate.getId(), certificateName, lbId, virtualServerNameSecure));
            virtualServerService.addSSLSites(new String[]{virtualServerNameSecure}, new VirtualServerSSLSite[][]{new VirtualServerSSLSite[]{sslSite}});
            LOG.debug(String.format("Successfully attached certificate mapping '%d' (certificate '%s') to load balancer %d (virtual server '%s').", certMappingToUpdate.getId(), certificateName, lbId, virtualServerNameSecure));
        } catch (AxisFault af) {
            String message = String.format("Error updating certificate mapping '%d' (certificate '%s') for load balancer %d (virtual server '%s').", certMappingToUpdate.getId(), certificateName, lbId, virtualServerNameSecure);
            LOG.error(message);
            LOG.error(StringConverter.getExtendedStackTrace(af));
            throw new ZxtmRollBackException(message, af);
        }
    }

    @Override
    public void deleteCertificateMapping(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, CertificateMapping certMappingToDelete) throws RemoteException, InsufficientRequestException, RollBackException {
        final String virtualServerNameSecure = ZxtmNameBuilder.genSslVSName(lbId, accountId);
        final String certificateName = ZxtmNameBuilder.generateCertificateName(lbId, accountId, certMappingToDelete.getId());

        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        VirtualServerBindingStub virtualServerService = serviceStubs.getVirtualServerBinding();
        CatalogSSLCertificatesBindingStub certificateCatalogService = serviceStubs.getZxtmCatalogSSLCertificatesBinding();

        try {
            LOG.debug(String.format("Removing certificate mapping with host name '%s' and id '%d' for load balancer '%d'...", certMappingToDelete.getHostName(), certMappingToDelete.getId(), lbId));
            virtualServerService.deleteSSLSites(new String[]{virtualServerNameSecure}, new String[][]{new String[]{certMappingToDelete.getHostName()}});
        } catch (ObjectDoesNotExist odne) {
            LOG.debug(String.format("Cannot remove certificate with host name '%s' and id '%d' as it does not exist for load balancer '%d'. Ignoring...", certMappingToDelete.getHostName(), certMappingToDelete.getId(), lbId));
        } catch (AxisFault axisFault) {
            LOG.error(StringConverter.getExtendedStackTrace(axisFault));
            // Stingray should have returned a proper error, but doesn't sadly. Hence, why we parse the error message.
            if (axisFault.getFaultString().contains("could not be found")) {
                LOG.debug(String.format("Cannot remove certificate with host name '%s' and id '%d' as it does not exist for load balancer '%d'. Ignoring...", certMappingToDelete.getHostName(), certMappingToDelete.getId(), lbId));
            } else {
                throw axisFault;
            }
        }

        try {
            LOG.debug(String.format("Removing certificate '%s' for certificate mapping '%d' for load balancer '%d'...", certificateName, certMappingToDelete.getId(), lbId));
            certificateCatalogService.deleteCertificate(new String[]{certificateName});
            LOG.debug(String.format("Successfully removed certificate mapping '%d' for load balancer '%d'.", certMappingToDelete.getId(), lbId));
        } catch (ObjectDoesNotExist odne) {
            LOG.debug(String.format("Cannot remove certificate '%s' as it does not exist for certificate mapping '%d' for load balancer '%d'. Ignoring...", certificateName, certMappingToDelete.getId(), lbId));
        }
    }

    @Override
    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String virtualServerName = ZxtmNameBuilder.genVSName(loadBalancer);
        final String virtualSecureServerName = ZxtmNameBuilder.genSslVSName(loadBalancer.getId(), loadBalancer.getAccountId());
        final String virtualRedirectServerName = ZxtmNameBuilder.genRedirectVSName(loadBalancer.getId(), loadBalancer.getAccountId());
        final String poolName = virtualServerName;

        LOG.debug(String.format("Deleting load balancer '%s'", virtualServerName));

        if (loadBalancer.hasSsl() && loadBalancer.getCertificateMappings() != null) {
            // Remove any cert mappings FIRST just in case
            removeAllCertificateMappings(config, loadBalancer.getId(), loadBalancer.getAccountId());
        }

        removeAndSetDefaultErrorFile(config, loadBalancer);
        //If present, remove the secure virtual server (SSL Termination)
        boolean isSecureServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualSecureServerName);
        if (isSecureServer) {
            removeSslTermination(config, loadBalancer);
        }

        deleteRateLimit(config, loadBalancer);
        deleteVirtualServer(serviceStubs, virtualServerName);
        deleteNodePool(serviceStubs, poolName);

        boolean isRedirectServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualRedirectServerName);
        if (isRedirectServer) {
            deleteVirtualServer(serviceStubs, virtualRedirectServerName);
        }
        deleteProtectionCatalog(serviceStubs, poolName);
        removeHealthMonitor(config, loadBalancer);
        deleteTrafficIpGroups(serviceStubs, loadBalancer);

        LOG.info(String.format("Successfully deleted load balancer '%s'.", virtualServerName));
    }

    private void deleteVirtualServer(ZxtmServiceStubs serviceStubs, String virtualServerName) throws RemoteException {
        try {
            LOG.debug(String.format("Deleting virtual server '%s'...", virtualServerName));
            serviceStubs.getVirtualServerBinding().deleteVirtualServer(new String[]{virtualServerName});
            LOG.debug(String.format("Virtual server '%s' successfully deleted.", virtualServerName));
        } catch (ObjectDoesNotExist odne) {
            LOG.debug(String.format("Virtual server '%s' already deleted.", virtualServerName));
        }
    }

    private void deleteNodePool(ZxtmServiceStubs serviceStubs, String poolName) throws RemoteException {
        try {
            LOG.debug(String.format("Deleting pool '%s'...", poolName));
            serviceStubs.getPoolBinding().deletePool(new String[]{poolName});
            LOG.info(String.format("Pool '%s' successfully deleted.", poolName));
        } catch (ObjectDoesNotExist odne) {
            LOG.debug(String.format("Pool '%s' already deleted. Ignoring...", poolName));
        } catch (ObjectInUse oiu) {
            LOG.error(String.format("Pool '%s' is currently in use. Cannot delete.", poolName));
        }
    }

    private void deleteProtectionCatalog(ZxtmServiceStubs serviceStubs, String poolName) throws RemoteException {
        try {
            LOG.info(String.format("Removing protection catalog from virtual server for: '%s'.", poolName));
            serviceStubs.getVirtualServerBinding().setProtection(new String[]{poolName}, new String[]{""});
            LOG.debug(String.format("Removed protection catalog from virtual server for: '%s'.", poolName));
        } catch (ObjectDoesNotExist odne) {
            LOG.debug(String.format("Virtual server '%s' already deleted. not updating protection catalog on server, ignoring...", poolName));
        }
        try {
            LOG.debug(String.format("Deleting service protection catalog '%s'...", poolName));
            serviceStubs.getProtectionBinding().deleteProtection(new String[]{poolName});
            LOG.info(String.format("Service protection catalog '%s' successfully deleted.", poolName));
        } catch (ObjectDoesNotExist odne) {
            LOG.debug(String.format("Service protection catalog '%s' already deleted. Ignoring...", poolName));
        } catch (ObjectInUse oiu) {
            LOG.error(String.format("Service protection catalog '%s' currently in use. Cannot delete.", poolName));
        }
    }

    private void deleteTrafficIpGroups(ZxtmServiceStubs serviceStubs, LoadBalancer lb) throws RemoteException, InsufficientRequestException {
        for (LoadBalancerJoinVip loadBalancerJoinVip : lb.getLoadBalancerJoinVipSet()) {
            LOG.info(String.format("About to remove traffic ip group for load balancer: %d on account: %d", lb.getId(), lb.getAccountId()));
            String trafficIpGroupName = ZxtmNameBuilder.generateTrafficIpGroupName(lb, loadBalancerJoinVip.getVirtualIp());
            deleteTrafficIpGroup(serviceStubs, trafficIpGroupName);
            LOG.info(String.format("Traffic ip group: %s for load balancer: %d was removed..", trafficIpGroupName, lb.getId()));
        }

        for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : lb.getLoadBalancerJoinVip6Set()) {
            LOG.info(String.format("About to remove traffic ip group for load balancer: %d on account: %d", lb.getId(), lb.getAccountId()));
            String trafficIpGroupName = ZxtmNameBuilder.generateTrafficIpGroupName(lb, loadBalancerJoinVip6.getVirtualIp());
            deleteTrafficIpGroup(serviceStubs, trafficIpGroupName);
            LOG.info(String.format("Traffic ip group: %s for load balancer: %d was removed..", trafficIpGroupName, lb.getId()));
        }
    }

    private void deleteTrafficIpGroupsX(ZxtmServiceStubs serviceStubs, List<String> trafficIpGroups) throws RemoteException {
        for (String trafficIpGroupName : trafficIpGroups) {
            deleteTrafficIpGroup(serviceStubs, trafficIpGroupName);
        }
    }

    private void deleteTrafficIpGroup(ZxtmServiceStubs serviceStubs, String trafficIpGroupName) throws RemoteException, InvalidOperation {
        try {
            LOG.debug(String.format("Deleting traffic ip group '%s'...", trafficIpGroupName));
            serviceStubs.getTrafficIpGroupBinding().deleteTrafficIPGroup(new String[]{trafficIpGroupName});
            LOG.info(String.format("Successfully deleted traffic ip group '%s'...", trafficIpGroupName));

        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.debug(String.format("Traffic ip group '%s' already deleted. Ignoring...", trafficIpGroupName));
            } else if (e instanceof ObjectInUse) {
                LOG.debug(String.format("Traffic ip group '%s' is in use (i.e. shared). Skipping...", trafficIpGroupName));
            } else if (!(e instanceof ObjectDoesNotExist) && !(e instanceof ObjectInUse)) {
                LOG.debug(String.format("There was an unknown issues deleting traffic ip group: %s", trafficIpGroupName) + e.getMessage());
            }
            LOG.error(StringConverter.getExtendedStackTrace(e));
            LOG.debug(String.format("There was an error removing traffic ip group: %s Message: %s Stack-Trace: %s", trafficIpGroupName, e.getMessage(), Arrays.toString(e.getStackTrace())));
        }
    }

    @Override
    public void updateProtocol(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        Integer lbId = lb.getId();
        Integer accountId = lb.getAccountId();
        LoadBalancerProtocol protocol = lb.getProtocol();
        String[] vsNames;
        boolean[] enablesXF;
        boolean[] disablesXF;

        boolean connectionLogging;

        if (lb.isConnectionLogging() == null) {
            connectionLogging = false;
            lb.setConnectionLogging(Boolean.FALSE);
        } else {
            //Keep a record
            connectionLogging = lb.isConnectionLogging();
            lb.setConnectionLogging(Boolean.FALSE);
        }

        String virtualServerName = ZxtmNameBuilder.genVSName(lbId, accountId);
        String virtualSecureServerName = ZxtmNameBuilder.genSslVSName(lbId, accountId);
        String virtualRedirectServerName = ZxtmNameBuilder.genRedirectVSName(lbId, accountId);

        boolean isSecureServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualSecureServerName);
        boolean isRedirectServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualRedirectServerName);

        enablesXF = new boolean[]{true, true};
        disablesXF = new boolean[]{false, false};
        if (isSecureServer && isRedirectServer) { //don't make any changes to the Redirect server here
            vsNames = new String[1];
            vsNames[0] = virtualSecureServerName;
            enablesXF = new boolean[]{true};
            disablesXF = new boolean[]{false};
        } else if (isSecureServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualSecureServerName;
        } else if (isRedirectServer) { //don't make any changes to the Redirect server here
            vsNames = new String[1];
            vsNames[0] = virtualServerName;
            enablesXF = new boolean[]{true};
            disablesXF = new boolean[]{false};
        } else {
            vsNames = new String[1];
            vsNames[0] = virtualServerName;
            enablesXF = new boolean[]{true};
            disablesXF = new boolean[]{false};
        }

        try {
            if (protocol.equals(LoadBalancerProtocol.HTTP)) {
                if (SessionPersistence.NONE.equals(lb.getSessionPersistence()) || SessionPersistence.SSL_ID.equals(lb.getSessionPersistence())) {
                    removeSessionPersistence(config, lbId, accountId);
                }

                serviceStubs.getVirtualServerBinding().setAddXForwardedForHeader(vsNames, enablesXF);
                serviceStubs.getVirtualServerBinding().setAddXForwardedProtoHeader(vsNames, enablesXF);
                attachXFPORTRuleToVirtualServers(serviceStubs, vsNames);
            } else {
                if (SessionPersistence.NONE.equals(lb.getSessionPersistence()) || SessionPersistence.HTTP_COOKIE.equals(lb.getSessionPersistence())) {
                    removeSessionPersistence(config, lbId, accountId);
                }

                for (String vname : vsNames) {
                    serviceStubs.getVirtualServerBinding().setRules(new String[]{(vname)}, new VirtualServerRule[][]{{}});
                }
                updateContentCaching(config, lb);
                serviceStubs.getVirtualServerBinding().setAddXForwardedForHeader(vsNames, disablesXF);
                serviceStubs.getVirtualServerBinding().setAddXForwardedProtoHeader(vsNames, disablesXF);

            }
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            throw new ZxtmRollBackException(String.format("Update protocol request canceled for %s ", virtualServerName), e);
        }


        try {
            // Drop rate-limit Rule if it exists
            boolean rateLimitExists = false;
            String[] rateNames = serviceStubs.getZxtmRateCatalogService().getRateNames();
            for (String vsName : vsNames) {
                for (String rateName : rateNames) {
                    if (rateName.equals(vsName)) {
                        rateLimitExists = true;
                    }
                }
            }

            if (rateLimitExists) {
                removeRateLimitRulesFromVirtualServers(serviceStubs, vsNames);
            }

            // Disable logging for protocol switch (keeping trevors commit)
            updateConnectionLogging(config, lb);

            LOG.debug(String.format("Updating protocol to '%s' for virtual server '%s'...", protocol.name(), virtualServerName));
            serviceStubs.getVirtualServerBinding().setProtocol(new String[]{vsNames[0]}, new VirtualServerProtocol[]{ZxtmConversionUtils.mapProtocol(protocol)});
            LOG.info(String.format("Successfully updated protocol for virtual server '%s'.", virtualServerName));

            try {
                if (protocol.equals(LoadBalancerProtocol.HTTP)) {
                    TrafficScriptHelper.addXForwardedPortScriptIfNeeded(serviceStubs);
                    attachXFPORTRuleToVirtualServers(serviceStubs, vsNames);
                    serviceStubs.getVirtualServerBinding().setAddXForwardedForHeader(vsNames, enablesXF);
                    serviceStubs.getVirtualServerBinding().setAddXForwardedProtoHeader(vsNames, enablesXF);
                }
            } catch (Exception ex) {
                LOG.error(StringConverter.getExtendedStackTrace(e));
                throw new ZxtmRollBackException("Update protocol request canceled.", ex);
            }
            // Re-add rate-limit Rule
            if (rateLimitExists) {
                attachRateLimitRulesToVirtualServers(serviceStubs, vsNames);
            }
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot update protocol for virtual server '%s' as it does not exist.", virtualServerName), e);
            }
            throw new ZxtmRollBackException("Update protocol request canceled.", e);
        }

        try {
            // Update log format to match protocol
            if (connectionLogging) {
                lb.setConnectionLogging(true);
            }
            updateConnectionLogging(config, lb);
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            throw new ZxtmRollBackException("Update protocol request canceled.", e);
        }

    }

    private void updateSecureServerWithTempPool(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        String poolName = ZxtmNameBuilder.genSslVSName(loadBalancer.getId(), loadBalancer.getAccountId());
        String tempPoolName = poolName + "_T";
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        LOG.debug(String.format("Creating temporary pool '%s' and setting nodes...", tempPoolName));
        try {
            serviceStubs.getPoolBinding().addPool(new String[]{tempPoolName}, NodeHelper.getIpAddressesFromNodes(loadBalancer.getNodes()));
        } catch (ObjectAlreadyExists oae) {
            LOG.info("Temporary pool already exists, ignoring...");
        }
        serviceStubs.getVirtualServerBinding().setDefaultPool(new String[]{poolName}, new String[]{tempPoolName});
    }

    private void reattachSecureServerWithTempPool(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        String poolName = ZxtmNameBuilder.genSslVSName(loadBalancer.getId(), loadBalancer.getAccountId());
        String nonSecurePoolName = ZxtmNameBuilder.genVSName(loadBalancer);
        String tempPoolName = poolName + "_T";
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        LOG.debug(String.format("Removing temporary pool '%s' ...", tempPoolName));
//        serviceStubs.getPoolBinding().addPool(new String[]{tempPoolName}, NodeHelper.getIpAddressesFromNodes(loadBalancer.getNodes()));
        serviceStubs.getVirtualServerBinding().setDefaultPool(new String[]{poolName}, new String[]{nonSecurePoolName});
        serviceStubs.getPoolBinding().deletePool(new String[]{tempPoolName});
    }

    @Override
    public void updatePort(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Integer port)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        String virtualServerName = ZxtmNameBuilder.genVSName(lbId, accountId);
        String virtualSecureServerName = ZxtmNameBuilder.genSslVSName(lbId, accountId);
        String virtualRedirectServerName = ZxtmNameBuilder.genRedirectVSName(lbId, accountId);

        boolean isSecureServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualSecureServerName);
        boolean isRedirectServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualRedirectServerName);
        if (isRedirectServer && isSecureServer) {
            virtualServerName = virtualRedirectServerName;
        }

        try {
            LOG.debug(String.format("Updating port to '%d' for virtual server '%s'...", port, virtualServerName));
            serviceStubs.getVirtualServerBinding().setPort(new String[]{virtualServerName}, new UnsignedInt[]{new UnsignedInt(port)});
            LOG.info(String.format("Successfully updated port for virtual server '%s'.", virtualServerName));
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot update port for virtual server '%s' as it does not exist.", virtualServerName), e);
            }
            throw new ZxtmRollBackException("Update port request canceled.", e);
        }
    }

    private void updatePort(LoadBalancerEndpointConfiguration config, String virtualServerName, Integer port) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        try {
            LOG.debug(String.format("Updating port to '%d' for virtual server '%s'...", port, virtualServerName));
            serviceStubs.getVirtualServerBinding().setPort(new String[]{virtualServerName}, new UnsignedInt[]{new UnsignedInt(port)});
            LOG.info(String.format("Successfully updated port for virtual server '%s'.", virtualServerName));
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot update port for virtual server '%s' as it does not exist.", virtualServerName), e);
            } else {
                throw new ZxtmRollBackException("Update port request canceled as there was an unexpected error...", e);
            }
        }
    }

    private void suspendUnsuspendVirtualServer(LoadBalancerEndpointConfiguration config, String virtualServerName, boolean isSuspended) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        boolean isEnabled = !isSuspended;

        try {
            LOG.debug(String.format("Updating suspension to '%s' for virtual server '%s'...", isSuspended, virtualServerName));
            serviceStubs.getVirtualServerBinding().setEnabled(new String[]{virtualServerName}, new boolean[]{isEnabled});
//            LOG.info(String.format("Successfully updated suspension for virtual server '%s'.", virtualServerName));
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot suspend/unsuspend virtual server '%s' as it does not exist.", virtualServerName), e);
            } else {
                throw new ZxtmRollBackException("Suspend/unsuspend request canceled.", e);
            }
        }
    }

    @Override
    public void setLoadBalancingAlgorithm(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, LoadBalancerAlgorithm algorithm)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.genVSName(lbId, accountId);

        try {
            LOG.debug(String.format("Setting load balancing algorithm to '%s' for node pool '%s'...", algorithm.name(), poolName));
            serviceStubs.getPoolBinding().setLoadBalancingAlgorithm(new String[]{poolName}, new PoolLoadBalancingAlgorithm[]{ZxtmConversionUtils.mapAlgorithm(algorithm)});
            LOG.info(String.format("Load balancing algorithm successfully set for node pool '%s'...", poolName));
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot update algorithm for node pool '%s' as it does not exist.", poolName), e);
            }
            throw new ZxtmRollBackException("Update algorithm request canceled.", e);
        }
    }

    @Override
    public void addVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        addVirtualIps(config, lb, ZxtmNameBuilder.genVSName(lb));
        if (lb.hasSsl()) {
            addVirtualIps(config, lb, ZxtmNameBuilder.genSslVSName(lb));
        }
    }

    public void addVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer lb, String vsName)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);


        String[] failoverTrafficManagers = config.getFailoverTrafficManagerNames().toArray(new String[config.getFailoverTrafficManagerNames().size()]);
        final String rollBackMessage = "Add virtual ips request canceled.";
        String[][] currentTrafficIpGroups;
        Set<String> updatedTrafficIpGroups = new HashSet<String>();
        List<String> newTrafficIpGroups = new ArrayList<String>();

        LOG.debug(String.format("Adding virtual ips for virtual server '%s'...", vsName));

        try {
            // Obtain traffic groups currently associated with the virtual server
            currentTrafficIpGroups = serviceStubs.getVirtualServerBinding().getListenTrafficIPGroups(new String[]{vsName});
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot add virtual ips to virtual server %s as it does not exist. %s", vsName, e));
            }
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        // Add current traffic ip groups to traffic ip group list
        if (currentTrafficIpGroups != null) {
            updatedTrafficIpGroups.addAll(Arrays.asList(currentTrafficIpGroups[0]));
        }

        // Add new traffic ip groups for IPv4 vips
        for (LoadBalancerJoinVip loadBalancerJoinVipToAdd : lb.getLoadBalancerJoinVipSet()) {
            String newTrafficIpGroup = ZxtmNameBuilder.generateTrafficIpGroupName(lb, loadBalancerJoinVipToAdd.getVirtualIp());
            newTrafficIpGroups.add(newTrafficIpGroup);
            updatedTrafficIpGroups.add(newTrafficIpGroup);
            createTrafficIpGroup(config, serviceStubs, loadBalancerJoinVipToAdd.getVirtualIp().getIpAddress(), newTrafficIpGroup);
        }

        // Add new traffic ip groups for IPv6 vips
        for (LoadBalancerJoinVip6 loadBalancerJoinVip6ToAdd : lb.getLoadBalancerJoinVip6Set()) {
            String newTrafficIpGroup = ZxtmNameBuilder.generateTrafficIpGroupName(lb, loadBalancerJoinVip6ToAdd.getVirtualIp());
            newTrafficIpGroups.add(newTrafficIpGroup);
            updatedTrafficIpGroups.add(newTrafficIpGroup);
            try {
                createTrafficIpGroup(config, serviceStubs, loadBalancerJoinVip6ToAdd.getVirtualIp().getDerivedIpString(), newTrafficIpGroup);
            } catch (IPStringConversionException e) {
                LOG.error("Rolling back newly created traffic ip groups...", e);
                deleteTrafficIpGroupsX(serviceStubs, newTrafficIpGroups);
                throw new ZxtmRollBackException("Cannot derive name from IPv6 virtual ip.", e);
            }
        }

        try {
            // Define the virtual server to listen on for every traffic ip group
            serviceStubs.getVirtualServerBinding().setListenTrafficIPGroups(new String[]{vsName}, new String[][]{Arrays.copyOf(updatedTrafficIpGroups.toArray(), updatedTrafficIpGroups.size(), String[].class)});
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            if (e instanceof ObjectDoesNotExist) {
                LOG.error("Cannot add virtual ips to virtual server as it does not exist.", e);
            }
            LOG.error("Rolling back newly created traffic ip groups...", e);
            deleteTrafficIpGroupsX(serviceStubs, newTrafficIpGroups);
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        // TODO: Refactor and handle exceptions properly
        // Enable and set failover traffic managers for traffic ip groups
        for (String trafficIpGroup : updatedTrafficIpGroups) {
            try {
                serviceStubs.getTrafficIpGroupBinding().setEnabled(new String[]{trafficIpGroup}, new boolean[]{true});
                serviceStubs.getTrafficIpGroupBinding().addTrafficManager(new String[]{trafficIpGroup}, new String[][]{failoverTrafficManagers});
                serviceStubs.getTrafficIpGroupBinding().setPassiveMachine(new String[]{trafficIpGroup}, new String[][]{failoverTrafficManagers});
            } catch (ObjectDoesNotExist e) {
                LOG.warn(String.format("Traffic ip group '%s' does not exist. Continuing...", trafficIpGroup));
            }
        }

        LOG.info(String.format("Virtual ips successfully added for virtual server '%s'...", vsName));
    }

    /*
     *  A traffic ip group consists of only one virtual ip at this time.
     */
    private void createTrafficIpGroup(LoadBalancerEndpointConfiguration config, ZxtmServiceStubs serviceStubs, String ipAddress, String newTrafficIpGroup) throws RemoteException {
        final TrafficIPGroupsDetails details = new TrafficIPGroupsDetails(new String[]{ipAddress}, new String[]{config.getTrafficManagerName()});

        try {
            LOG.debug(String.format("Adding traffic ip group '%s'...", newTrafficIpGroup));
            serviceStubs.getTrafficIpGroupBinding().addTrafficIPGroup(new String[]{newTrafficIpGroup}, new TrafficIPGroupsDetails[]{details});
            LOG.info(String.format("Traffic ip group '%s' successfully added.", newTrafficIpGroup));
        } catch (ObjectAlreadyExists oae) {
            LOG.debug(String.format("Traffic ip group '%s' already exists. Ignoring...", newTrafficIpGroup));
        }
    }

    @Override
    public void deleteVirtualIp(LoadBalancerEndpointConfiguration config, LoadBalancer lb, Integer vipId)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {

        List<Integer> vipIds = new ArrayList<Integer>();
        vipIds.add(vipId);

        deleteVirtualIps(config, lb, vipIds);
    }

    @Override
    public void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer lb, List<Integer> vipIds)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String virtualServerName = ZxtmNameBuilder.genVSName(lb.getId(), lb.getAccountId());
        String[][] currentTrafficIpGroups;
        List<String> updatedTrafficIpGroupList = new ArrayList<String>();
        String trafficIpGroupToDelete;
        final String rollBackMessage = "Delete virtual ip request canceled.";

        try {
            currentTrafficIpGroups = serviceStubs.getVirtualServerBinding().getListenTrafficIPGroups(new String[]{virtualServerName});
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error("Cannot delete virtual ip from virtual server as the virtual server does not exist.", e);
            }
            LOG.error(rollBackMessage + "Rolling back changes...", e);
            LOG.error(StringConverter.getExtendedStackTrace(e));
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        // Convert current traffic groups to array
        List<String> trafficIpGroupNames = new ArrayList<String>();
        for (String[] currentTrafficGroup : currentTrafficIpGroups) {
            trafficIpGroupNames.addAll(Arrays.asList(currentTrafficGroup));
        }

        // Get traffic ip group to delete
        List<String> trafficIpGroupNamesToDelete = new ArrayList<String>();
        for (Integer vipIdToDelete : vipIds) {
            trafficIpGroupNamesToDelete.add(ZxtmNameBuilder.generateTrafficIpGroupName(lb, vipIdToDelete));
        }

        // Exclude the traffic ip group to delete
        for (String trafficIpGroupName : trafficIpGroupNames) {
            if (!trafficIpGroupNamesToDelete.contains(trafficIpGroupName)) {
                updatedTrafficIpGroupList.add(trafficIpGroupName);
                serviceStubs.getTrafficIpGroupBinding().setEnabled(new String[]{trafficIpGroupName}, new boolean[]{true});
            }
        }

        try {
            // Update the virtual server to listen on the updated traffic ip groups
            serviceStubs.getVirtualServerBinding().setListenTrafficIPGroups(new String[]{virtualServerName}, new String[][]{Arrays.copyOf(updatedTrafficIpGroupList.toArray(), updatedTrafficIpGroupList.size(), String[].class)});
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error("Cannot set traffic ip groups to virtual server as it does not exist.", e);
            }
            LOG.error(StringConverter.getExtendedStackTrace(e));
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        if (!trafficIpGroupNamesToDelete.isEmpty()) {
            try {
                deleteTrafficIpGroupsX(serviceStubs, trafficIpGroupNamesToDelete);
            } catch (RemoteException re) {
                LOG.error(rollBackMessage + "Rolling back changes...", re);
                serviceStubs.getVirtualServerBinding().setListenTrafficIPGroups(new String[]{virtualServerName}, new String[][]{Arrays.copyOf(trafficIpGroupNamesToDelete.toArray(), trafficIpGroupNamesToDelete.size(), String[].class)});
                serviceStubs.getTrafficIpGroupBinding().setEnabled(trafficIpGroupNames.toArray(new String[trafficIpGroupNames.size()]), generateBooleanArray(trafficIpGroupNames.size(), true));
                throw new ZxtmRollBackException(rollBackMessage, re);
            }
        }
    }

    @Override
    public void setRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        setRateLimit(config, ZxtmNameBuilder.genVSName(loadBalancer), rateLimit);
        if (loadBalancer.hasSsl()) {
            setRateLimit(config, ZxtmNameBuilder.genSslVSName(loadBalancer), rateLimit);
        }
    }

    public void setRateLimit(LoadBalancerEndpointConfiguration config, String vsName, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        try {
            LOG.debug(String.format("Adding a rate limit to load balancer...'%s'...", vsName));
            serviceStubs.getZxtmRateCatalogService().addRate(new String[]{vsName});
            serviceStubs.getZxtmRateCatalogService().setMaxRatePerSecond(new String[]{vsName}, new UnsignedInt[]{new UnsignedInt(rateLimit.getMaxRequestsPerSecond())});
            LOG.info("Successfully added a rate limit to the rate limit pool.");

            TrafficScriptHelper.addRateLimitScriptsIfNeeded(serviceStubs);
            attachRateLimitRulesToVirtualServers(serviceStubs, new String[]{vsName});

        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot add rate limit for virtual server '%s' as it does not exist.", vsName));
            }
            throw new ZxtmRollBackException("Add rate limit request canceled.", e);
        }
    }

    private void attachRateLimitRulesToVirtualServer(ZxtmServiceStubs serviceStubs, String virtualServerName) throws RemoteException {
        LOG.debug("Attach rules and enable them on the virtual server.");
        VirtualServerRule rateLimitRule;

        if (serviceStubs.getVirtualServerBinding().getProtocol(new String[]{virtualServerName})[0].equals(VirtualServerProtocol.http)) {
            rateLimitRule = ZxtmAdapterImpl.ruleRateLimitHttp;
        } else {
            rateLimitRule = ZxtmAdapterImpl.ruleRateLimitNonHttp;
        }

        serviceStubs.getVirtualServerBinding().addRules(new String[]{virtualServerName}, new VirtualServerRule[][]{{rateLimitRule}});
        LOG.info("Rules attached to the VS, add rate limit successfully completed.");
    }

    private void attachRateLimitRulesToVirtualServers(ZxtmServiceStubs serviceStubs, String[] virtualServerNames) throws RemoteException {
        for (String vsName : virtualServerNames) {
            attachRateLimitRulesToVirtualServer(serviceStubs, vsName);
        }
    }

    private void removeRateLimitRulesFromVirtualServer(ZxtmServiceStubs serviceStubs, String virtualServerName) throws RemoteException {
        LOG.debug(String.format("Removing rate-limit rules from load balancer '%s'...", virtualServerName));
        VirtualServerRule[][] virtualServerRules = serviceStubs.getVirtualServerBinding().getRules(new String[]{virtualServerName});
        if (virtualServerRules.length > 0) {
            for (VirtualServerRule virtualServerRule : virtualServerRules[0]) {
                if (virtualServerRule.getName().equals(ZxtmAdapterImpl.ruleRateLimitHttp.getName())) {
                    serviceStubs.getVirtualServerBinding().removeRules(new String[]{virtualServerName}, new String[][]{{ZxtmAdapterImpl.ruleRateLimitHttp.getName()}});
                }
                if (virtualServerRule.getName().equals(ZxtmAdapterImpl.ruleRateLimitNonHttp.getName())) {
                    serviceStubs.getVirtualServerBinding().removeRules(new String[]{virtualServerName}, new String[][]{{ZxtmAdapterImpl.ruleRateLimitNonHttp.getName()}});
                }
            }
        }
        LOG.debug(String.format("Rate-limit rules successfully removed from load balancer '%s'.", virtualServerName));
    }

    private void removeRateLimitRulesFromVirtualServers(ZxtmServiceStubs serviceStubs, String[] virtualServerNames) throws RemoteException {
        for (String vsName : virtualServerNames) {
            removeRateLimitRulesFromVirtualServer(serviceStubs, vsName);
        }
    }

    private void attachXFFRuleToVirtualServer(ZxtmServiceStubs serviceStubs, String virtualServerName) throws RemoteException {
        if (serviceStubs.getVirtualServerBinding().getProtocol(new String[]{virtualServerName})[0].equals(VirtualServerProtocol.http)) {
            LOG.debug(String.format("Attaching the XFF rule and enabling it on load balancer '%s'...", virtualServerName));
            serviceStubs.getVirtualServerBinding().addRules(new String[]{virtualServerName}, new VirtualServerRule[][]{{ZxtmAdapterImpl.ruleXForwardedFor}});
            LOG.debug(String.format("XFF rule successfully enabled on load balancer '%s'.", virtualServerName));
        }
    }

    private void attachXFPORTRuleToVirtualServer(ZxtmServiceStubs serviceStubs, String virtualServerName) throws RemoteException {
        if (serviceStubs.getVirtualServerBinding().getProtocol(new String[]{virtualServerName})[0].equals(VirtualServerProtocol.http)) {
            LOG.debug(String.format("Attaching the XFPORT rule and enabling it on load balancer '%s'...", virtualServerName));
            serviceStubs.getVirtualServerBinding().addRules(new String[]{virtualServerName}, new VirtualServerRule[][]{{ZxtmAdapterImpl.ruleXForwardedPort}});
            LOG.debug(String.format("XFPORT rule successfully enabled on load balancer '%s'.", virtualServerName));
        }
    }

    private void attachXFPRuleToVirtualServer(ZxtmServiceStubs serviceStubs, String virtualServerName) throws RemoteException {
        if (serviceStubs.getVirtualServerBinding().getProtocol(new String[]{virtualServerName})[0].equals(VirtualServerProtocol.http)) {
            LOG.debug(String.format("Attaching the XFP rule and enabling it on load balancer '%s'...", virtualServerName));
            serviceStubs.getVirtualServerBinding().addRules(new String[]{virtualServerName}, new VirtualServerRule[][]{{ZxtmAdapterImpl.ruleXForwardedProto}});
            LOG.debug(String.format("XFP rule successfully enabled on load balancer '%s'.", virtualServerName));
        }
    }

    private void attachForceHttpsRedirectRuleToVirtualServer(ZxtmServiceStubs serviceStubs, String virtualServerName) throws RemoteException {
        if (serviceStubs.getVirtualServerBinding().getProtocol(new String[]{virtualServerName})[0].equals(VirtualServerProtocol.http)) {
            LOG.debug(String.format("Attaching the Https Redirect rule and enabling it on load balancer '%s'...", virtualServerName));
            serviceStubs.getVirtualServerBinding().addRules(new String[]{virtualServerName}, new VirtualServerRule[][]{{ZxtmAdapterImpl.ruleForceHttpsRedirect}});
            LOG.debug(String.format("Https Redirect rule successfully enabled on load balancer '%s'.", virtualServerName));
        }
    }

    private void attachXFFRuleToVirtualServers(ZxtmServiceStubs serviceStubs, String[] virtualServerNames) throws RemoteException {
        for (String vsName : virtualServerNames) {
            attachXFFRuleToVirtualServer(serviceStubs, vsName);
        }
    }

    private void attachXFPRuleToVirtualServers(ZxtmServiceStubs serviceStubs, String[] virtualServerNames) throws RemoteException {
        for (String vsName : virtualServerNames) {
            attachXFPRuleToVirtualServer(serviceStubs, vsName);
        }
    }

    private void attachXFPORTRuleToVirtualServers(ZxtmServiceStubs serviceStubs, String[] virtualServerNames) throws RemoteException {
        for (String vsName : virtualServerNames) {
            attachXFPORTRuleToVirtualServer(serviceStubs, vsName);
        }
    }

    private void attachXFFRuleToVirtualServerForced(ZxtmServiceStubs serviceStubs, String virtualServerName) throws RemoteException {
        LOG.debug(String.format("Attaching the XFF rule and enabling it on load balancer '%s'...", virtualServerName));
        serviceStubs.getVirtualServerBinding().addRules(new String[]{virtualServerName}, new VirtualServerRule[][]{{ZxtmAdapterImpl.ruleXForwardedFor}});
        LOG.debug(String.format("XFF rule successfully enabled on load balancer '%s'.", virtualServerName));
    }

    private void removeXFFRuleFromVirtualServer(ZxtmServiceStubs serviceStubs, String virtualServerName) throws RemoteException {
        LOG.debug(String.format("Removing the XFF rule from load balancer '%s'...", virtualServerName));
        VirtualServerRule[][] virtualServerRules = serviceStubs.getVirtualServerBinding().getRules(new String[]{virtualServerName});
        if (virtualServerRules.length > 0) {
            for (VirtualServerRule virtualServerRule : virtualServerRules[0]) {
                if (virtualServerRule.getName().equals(ZxtmAdapterImpl.ruleXForwardedFor.getName())) {
                    serviceStubs.getVirtualServerBinding().removeRules(new String[]{virtualServerName}, new String[][]{{ZxtmAdapterImpl.ruleXForwardedFor.getName()}});
                }
            }
        }
        LOG.debug(String.format("XFF rule successfully removed from load balancer '%s'.", virtualServerName));
    }

    private void removeXFPRuleFromVirtualServer(ZxtmServiceStubs serviceStubs, String virtualServerName) throws RemoteException {
        LOG.debug(String.format("Removing the XFP rule from load balancer '%s'...", virtualServerName));
        VirtualServerRule[][] virtualServerRules = serviceStubs.getVirtualServerBinding().getRules(new String[]{virtualServerName});
        if (virtualServerRules.length > 0) {
            for (VirtualServerRule virtualServerRule : virtualServerRules[0]) {
                if (virtualServerRule.getName().equals(ZxtmAdapterImpl.ruleXForwardedProto.getName())) {
                    serviceStubs.getVirtualServerBinding().removeRules(new String[]{virtualServerName}, new String[][]{{ZxtmAdapterImpl.ruleXForwardedProto.getName()}});
                }
            }
        }
        LOG.debug(String.format("XFP rule successfully removed from load balancer '%s'.", virtualServerName));
    }

    private void removeXFPORTRuleFromVirtualServer(ZxtmServiceStubs serviceStubs, String virtualServerName) throws RemoteException {
        LOG.debug(String.format("Removing the XFPORT rule from load balancer '%s'...", virtualServerName));
        VirtualServerRule[][] virtualServerRules = serviceStubs.getVirtualServerBinding().getRules(new String[]{virtualServerName});
        if (virtualServerRules.length > 0) {
            for (VirtualServerRule virtualServerRule : virtualServerRules[0]) {
                if (virtualServerRule.getName().equals(ZxtmAdapterImpl.ruleXForwardedPort.getName())) {
                    serviceStubs.getVirtualServerBinding().removeRules(new String[]{virtualServerName}, new String[][]{{ZxtmAdapterImpl.ruleXForwardedPort.getName()}});
                }
            }
        }
        LOG.debug(String.format("XFPORT rule successfully removed from load balancer '%s'.", virtualServerName));
    }

    private void removeForceHttpsRedirectRuleFromVirtualServer(ZxtmServiceStubs serviceStubs, String virtualServerName) throws RemoteException {
        LOG.debug(String.format("Removing the Https Redirect rule from load balancer '%s'...", virtualServerName));
        VirtualServerRule[][] virtualServerRules = serviceStubs.getVirtualServerBinding().getRules(new String[]{virtualServerName});
        if (virtualServerRules.length > 0) {
            for (VirtualServerRule virtualServerRule : virtualServerRules[0]) {
                if (virtualServerRule.getName().equals(ZxtmAdapterImpl.ruleForceHttpsRedirect.getName())) {
                    serviceStubs.getVirtualServerBinding().removeRules(new String[]{virtualServerName}, new String[][]{{ZxtmAdapterImpl.ruleForceHttpsRedirect.getName()}});
                }
            }
        }
        LOG.debug(String.format("Https Redirect rule successfully removed from load balancer '%s'.", virtualServerName));
    }

    private void removeXFFRuleFromVirtualServers(ZxtmServiceStubs serviceStubs, String[] virtualServerNames) throws RemoteException {
        for (String vsName : virtualServerNames) {
            removeXFFRuleFromVirtualServer(serviceStubs, vsName);
        }
    }

    private void removeXFPRuleFromVirtualServers(ZxtmServiceStubs serviceStubs, String[] virtualServerNames) throws RemoteException {
        for (String vsName : virtualServerNames) {
            removeXFPRuleFromVirtualServer(serviceStubs, vsName);
        }
    }

    private void removeXFPORTRuleFromVirtualServers(ZxtmServiceStubs serviceStubs, String[] virtualServerNames) throws RemoteException {
        for (String vsName : virtualServerNames) {
            removeXFPORTRuleFromVirtualServer(serviceStubs, vsName);
        }
    }

    @Override
    public void updateSslTermination(LoadBalancerEndpointConfiguration conf, LoadBalancer loadBalancer, ZeusSslTermination zeusSslTermination) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        final String sslVirtualServerName = ZxtmNameBuilder.genSslVSName(loadBalancer.getId(), loadBalancer.getAccountId());
        final String virtualServerNameNonSecure = ZxtmNameBuilder.genVSName(loadBalancer);

        String userKey = zeusSslTermination.getSslTermination().getPrivatekey();
        String userCrt = zeusSslTermination.getSslTermination().getCertificate();
        String imdCrt = zeusSslTermination.getSslTermination().getIntermediateCertificate();
        ZeusCrtFile zeusCrtFile = zeusUtil.buildZeusCrtFileLbassValidation(userKey, userCrt, imdCrt);
        if (zeusCrtFile.hasFatalErrors()) {
            String fmt = "ZeusCrtFile generation Failure: %s";
            String errors = StringUtils.joinString(zeusCrtFile.getFatalErrorList(), ",");
            String msg = String.format(fmt, errors);
            throw new InsufficientRequestException(msg);
        }

        ZxtmServiceStubs serviceStubs = getServiceStubs(conf);
        VirtualServerBindingStub virtualServerService = serviceStubs.getVirtualServerBinding();
        CatalogSSLCertificatesBindingStub certificateCatalogService = serviceStubs.getZxtmCatalogSSLCertificatesBinding();

        try {
            LOG.info(String.format("Creating ssl termination load balancer %s in zeus... ", sslVirtualServerName));
            createSecureVirtualServer(conf, loadBalancer);
        } catch (Exception af) {
            LOG.error(StringConverter.getExtendedStackTrace(af));
            if (af instanceof ObjectAlreadyExists) {
                LOG.warn(String.format("Secure virtual server '%s' already exists, ignoring....", sslVirtualServerName));
            }
        }
        // disable tls v1.0 if they disabled it
        enableDisableTLS_10(conf, loadBalancer, zeusSslTermination.getSslTermination().isTls10Enabled());

        if (!virtualServerService.getPort(new String[]{sslVirtualServerName})[0].equals(zeusSslTermination.getSslTermination().getSecurePort())) {
            LOG.info(String.format("Updating secure servers port for ssl termination load balancer  %s in zeus...", sslVirtualServerName));
            updatePort(conf, sslVirtualServerName, zeusSslTermination.getSslTermination().getSecurePort());
            LOG.debug(String.format("Successfully updated secure servers port for ssl termination load balancer %s in zeus...", sslVirtualServerName));
        }

        try {
            if (zeusSslTermination.getCertIntermediateCert() != null) {
                if (certificateCatalogService.getCertificateInfo(new String[]{sslVirtualServerName}) != null) {
                    LOG.info(String.format("Certificate already exists, removing it for loadbalancer: %s", loadBalancer.getId()));
                    enableDisableSslTermination(conf, loadBalancer, false);
                    certificateCatalogService.deleteCertificate(new String[]{sslVirtualServerName});
                    LOG.debug(String.format("Removed existing certificate for loadbalancer: %s", loadBalancer.getId()));
                }
            }
        } catch (ObjectDoesNotExist odne) {
            LOG.debug(String.format("The certificate does not exist, ignoring... loadbalancer: %s", loadBalancer.getId()));
        }

        try {

            if (zeusSslTermination.getCertIntermediateCert() != null) {
                LOG.info(String.format("Importing certificate for load balancer: %s", loadBalancer.getId()));
                CertificateFiles certificateFiles = new CertificateFiles();
                String privKey = zeusCrtFile.getPrivate_key();
                String pubCrt = zeusCrtFile.getPublic_cert();
                certificateFiles.setPrivate_key(privKey);
                certificateFiles.setPublic_cert(pubCrt);
                certificateCatalogService.importCertificate(new String[]{sslVirtualServerName}, new CertificateFiles[]{certificateFiles});
                LOG.debug(String.format("Successfully imported certificate for load balancer: %s", loadBalancer.getId()));
            }

            LOG.info(String.format("Attaching certificate and key, load balancer: %s ", loadBalancer.getId()));
            virtualServerService.setSSLCertificate(new String[]{sslVirtualServerName}, new String[]{sslVirtualServerName});
            LOG.debug(String.format("Succesfullly attached certificate and key for load balancer: %s", loadBalancer.getId()));

            LOG.info(String.format("Ssl termination virtual server will be enabled:'%s' for load balancer: %s", zeusSslTermination.getSslTermination().isEnabled(), loadBalancer.getId()));
            enableDisableSslTermination(conf, loadBalancer, zeusSslTermination.getSslTermination().isEnabled());
            LOG.debug(String.format("Successfully enabled:'%s' load balancer: %s ssl termination", zeusSslTermination.getSslTermination().isEnabled(), loadBalancer.getId()));

            boolean isRedirectServer = loadBalancer.isHttpsRedirect() != null && loadBalancer.isHttpsRedirect();
            if (!isRedirectServer) {
                LOG.info(String.format("Non-secure virtual server will be enabled:'%s' load balancer: %s", zeusSslTermination.getSslTermination().isEnabled(), loadBalancer.getId()));
                suspendUnsuspendVirtualServer(conf, virtualServerNameNonSecure, zeusSslTermination.getSslTermination().isSecureTrafficOnly());
                LOG.debug(String.format("Successfully enabled:'%s' non-secure server for load balancer: %s", zeusSslTermination.getSslTermination().isEnabled(), loadBalancer.getId()));
            }
        } catch (AxisFault af) {
            LOG.error("there was a error setting ssl termination in zxtm adapter for load balancer " + loadBalancer.getId());
            LOG.error(StringConverter.getExtendedStackTrace(e));

            //TODO: handle errors better...
            throw new ZxtmRollBackException("there was a error setting ssl termination in zxtm adapter for load balancer " + loadBalancer.getId(), af);
        }

        // SSLTermination VS is configured, update the ciphers.
        String cipherString = EMPTY;
        SslCipherProfile cipherProfile = zeusSslTermination.getSslTermination().getCipherProfile();
        if(cipherProfile != null) {
            cipherString = cipherProfile.getCiphers();
        }

        try {
            LOG.debug(String.format("ciphers to be updated in Zeus: %s",cipherString));
            setSslCiphersByVhost(conf, loadBalancer.getAccountId(), loadBalancer.getId(), cipherString);
            LOG.debug("Successfully updated a load balancer ssl termination in Zeus.");
        } catch (EntityNotFoundException e) {
            LOG.error("there was a error setting ssl termination ciphers in zxtm adapter for load balancer " + loadBalancer.getId());
            LOG.error(StringConverter.getExtendedStackTrace(e));

            //TODO: handle errors better...
            throw new ZxtmRollBackException("there was a error setting ssl termination ciphers in zxtm adapter for load balancer " + loadBalancer.getId(), e);
        }
    }

    @Override
    public void removeSslTermination(LoadBalancerEndpointConfiguration conf, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        String virtualServerName = ZxtmNameBuilder.genSslVSName(loadBalancer.getId(), loadBalancer.getAccountId());
        String virtualServerNameNonSecure = ZxtmNameBuilder.genVSName(loadBalancer);
        ZxtmServiceStubs serviceStubs = getServiceStubs(conf);

        try {
            //Detach and remove ssl termination (shadow server)
            LOG.info(String.format("Detaching and disabling certificate for load balancer: '%s' virtual server name: %s", loadBalancer.getId(), virtualServerNameNonSecure));
            enableDisableSslTermination(conf, loadBalancer, false);

            try {
                serviceStubs.getVirtualServerBinding().setSSLCertificate(new String[]{virtualServerName}, new String[]{""});
                serviceStubs.getZxtmCatalogSSLCertificatesBinding().deleteCertificate(new String[]{virtualServerName});
            } catch (ObjectDoesNotExist ex) {
                LOG.info(String.format("Certificates or key does not exist for load balancer: %d ignoring... Exception: %s", loadBalancer.getId(), ex.getErrmsg()));
            }

            // Remove all certificates used for certificate mappings
            for (CertificateMapping certificateMapping : loadBalancer.getCertificateMappings()) {
                String certificateName = ZxtmNameBuilder.generateCertificateName(loadBalancer.getId(), loadBalancer.getAccountId(), certificateMapping.getId());
                try {
                    LOG.info(String.format("Removing certificate '%s' for load balancer %d...", certificateName, loadBalancer.getId()));
                    serviceStubs.getZxtmCatalogSSLCertificatesBinding().deleteCertificate(new String[]{certificateName});
                    LOG.info(String.format("Successfully removed certificate '%s' for load balancer %d.", certificateName, loadBalancer.getId()));
                } catch (ObjectDoesNotExist odne) {
                    LOG.info(String.format("Certificate '%s' does not exist for load balancer %d. Ignoring...", certificateName, loadBalancer.getId()));
                }
            }

            try {
                //Removing rateLimit from shadow server
                LOG.info(String.format("Removing a rate limit from load balancer...'%s'...", loadBalancer.getId()));
                removeRateLimitRulesFromVirtualServer(serviceStubs, virtualServerName);
                serviceStubs.getZxtmRateCatalogService().deleteRate(new String[]{virtualServerName});
                LOG.debug(String.format("Rules detached and deleted from the ssl terminated virtual server, for loadbalancer: '%s' ", loadBalancer.getId()));
            } catch (ObjectDoesNotExist e) {
                LOG.warn(String.format("There was an warning removing rate limit from the shadow server as it does not exist for load balancer: '%s' ", loadBalancer.getId()));
            } catch (ObjectInUse e) {
                LOG.warn(String.format("There was an warning removing rate limit from the shadow server as it is already in use for load balancer: '%s' ", loadBalancer.getId()));
            } catch (RemoteException af) {
                LOG.error(StringConverter.getExtendedStackTrace(af));
                LOG.error(String.format("There was an unexpected exception while removing the secure virtual servers ratelimit for virtual server: %s", virtualServerName));

            }

            //Removing connectionThrottle from shadow server
            LOG.info(String.format("Removing connectionThrottle from load balancer...'%s'...", loadBalancer.getId()));
            zeroOutConnectionThrottleConfig(conf, loadBalancer);
            updateConnectionThrottle(conf, loadBalancer, virtualServerName);
            LOG.debug(String.format("Remove connectionThrottle from the ssl terminated virtual server, for loadbalancer: '%s' ", loadBalancer.getId()));

            //Removing connectionLogging from shadow server
            LOG.info(String.format("Removing connectionLogging from load balancer...'%s'...", loadBalancer.getId()));
            loadBalancer.setConnectionLogging(false);
            updateConnectionLogging(conf, loadBalancer, virtualServerName);
            LOG.debug(String.format("Remove connectionLogging from the ssl terminated virtual server, for loadbalancer: '%s' ", loadBalancer.getId()));

            //Removing accessList from shadow server
            LOG.info(String.format("Removing accessList from load balancer...'%s'...", loadBalancer.getId()));
            deleteAccessList(conf, virtualServerName);
            LOG.debug(String.format("Remove accessList from the ssl terminated virtual server, for loadbalancer: '%s' ", loadBalancer.getId()));

            //Removing error file from shadow server
            LOG.info(String.format("Removing error file from load balancer...'%s' for ssl termination...", loadBalancer.getId()));
            deleteErrorFile(conf, virtualServerName);
            LOG.debug(String.format("Remove error file from the ssl terminated virtual server, for loadbalancer: '%s' ", loadBalancer.getId()));

            //Removing protectionCatalog from shadow server
            LOG.info(String.format("Removing protection catalog from load balancer...'%s'...", loadBalancer.getId()));
            deleteProtectionCatalog(serviceStubs, virtualServerName);
            LOG.debug(String.format("Removed protection catalog from the ssl terminated virtual server, for loadbalancer: '%s' ", loadBalancer.getId()));

            //Removing the secure VS
            try {
                LOG.info(String.format(String.format("Removing the secure virtual server: %s", virtualServerName)));
                deleteVirtualServer(serviceStubs, virtualServerName);
                LOG.debug(String.format(String.format("Successfully removed the secure virtual server: %s", virtualServerName)));
            } catch (ObjectDoesNotExist dne) {
                //Only happens when deleteLoadBalancer calls us
                LOG.info(String.format("Virtual server %s was not found for %s removeSslTermination, ignoring...", virtualServerName, virtualServerNameNonSecure));
            }

            //Returning the non-secure VS to default state if HTTPS Redirect was turned on
            if (loadBalancer.isHttpsRedirect() != null && loadBalancer.isHttpsRedirect() == true) {
                restoreVirtualServerFromHttpsRedirect(loadBalancer, serviceStubs);
            }

            //Un-suspending non-secure VS
            try {
                LOG.info("Suspending/disabling non-secure virtual server for load balancer: " + loadBalancer.getId());
                suspendUnsuspendVirtualServer(conf, virtualServerNameNonSecure, false);
                LOG.debug("Successfully suspended/disabled non-secure virtual server for load balancer: " + loadBalancer.getId());
            } catch (ObjectDoesNotExist dne) {
                //Only happens when deleteLoadBalancer calls us
                LOG.info(String.format("Virtual server %s was not found for %s to suspend or enable, ignoring...", virtualServerName, virtualServerNameNonSecure));
            }

        } catch (Exception af) {
            LOG.error(String.format("there was a error removing ssl termination in zxtm adapter for load balancer: '%s'", loadBalancer.getId()));
            LOG.error(StringConverter.getExtendedStackTrace(e));
            throw new ZxtmRollBackException("There was an error removing ssl termination from load balancer: " + loadBalancer.getId(), af);
        }
    }

    @Override
    public void enableDisableTLS_10(LoadBalancerEndpointConfiguration conf, LoadBalancer loadBalancer, boolean isEnabled) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        String[] virtualServerNameArry = new String[]{ZxtmNameBuilder.genSslVSName(loadBalancer.getId(), loadBalancer.getAccountId())};
        VirtualServerSSLSupportTLS1[] tlsValue;
        if (isEnabled) {
            tlsValue = new VirtualServerSSLSupportTLS1[]{VirtualServerSSLSupportTLS1.enabled};
        } else {
            tlsValue = new VirtualServerSSLSupportTLS1[]{VirtualServerSSLSupportTLS1.disabled};
        }
        try {
            VirtualServerBindingStub vsBinding = getServiceStubs(conf).getVirtualServerBinding();
            vsBinding.setSSLSupportTLS1(virtualServerNameArry, tlsValue);
        } catch (RemoteException af) {
            String msg = String.format("There was a error setting TLS 1.0 support to %s for loadbalancer %d", isEnabled, loadBalancer.getId());
            LOG.error(msg);
            LOG.error(StringConverter.getExtendedStackTrace(af));
            throw new ZxtmRollBackException(msg, af);
        }
    }

    @Override
    public void enableDisableSslTermination(LoadBalancerEndpointConfiguration conf, LoadBalancer loadBalancer, boolean isSslTermination) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        final String virtualServerName = ZxtmNameBuilder.genSslVSName(loadBalancer.getId(), loadBalancer.getAccountId());
        ZxtmServiceStubs serviceStubs = getServiceStubs(conf);

        try {
            serviceStubs.getVirtualServerBinding().setSSLDecrypt(new String[]{virtualServerName}, new boolean[]{isSslTermination});

            boolean[] isVSEnabled = new boolean[]{loadBalancer.isUsingSsl()};
            serviceStubs.getVirtualServerBinding().setEnabled(new String[]{virtualServerName}, isVSEnabled);
        } catch (RemoteException af) {
            String msg = String.format("There was a error enabling/disabling ssl termination for loadbalancer: %d", loadBalancer.getId());
            LOG.error(msg);
            LOG.error(StringConverter.getExtendedStackTrace(af));
            throw new ZxtmRollBackException(msg, af);
        }

    }

    // upload the file then set the Errorpage.
    @Override
    public void setErrorFile(LoadBalancerEndpointConfiguration conf, LoadBalancer loadbalancer, String content) throws RemoteException, InsufficientRequestException {
        Integer lbId = loadbalancer.getId();
        Integer accountId = loadbalancer.getAccountId();
        ZxtmServiceStubs serviceStubs = getServiceStubs(conf);

        String virtualServerName = ZxtmNameBuilder.genVSName(lbId, accountId);
        String virtualSecureServerName = ZxtmNameBuilder.genSslVSName(lbId, accountId);
        String virtualRedirectServerName = ZxtmNameBuilder.genRedirectVSName(lbId, accountId);
        String[] vsNames;

        boolean isSecureServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualSecureServerName);
        boolean isRedirectServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualRedirectServerName);

        if (isSecureServer && isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualRedirectServerName;
            vsNames[1] = virtualSecureServerName;
        } else if (isSecureServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualSecureServerName;
        } else if (isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualRedirectServerName;
        } else {
            vsNames = new String[1];
            vsNames[0] = virtualServerName;
        }

        for (String vsName : vsNames) {
            setErrorFile(conf, vsName, content);
        }
    }

    public void setErrorFile(LoadBalancerEndpointConfiguration conf, String vsName, String content) throws RemoteException {
        String[] vsNames = new String[1];
        String[] errorFiles = new String[1];

        ZxtmServiceStubs serviceStubs = getServiceStubs(conf);
        ConfExtraBindingStub extraService = serviceStubs.getZxtmConfExtraBinding();
        VirtualServerBindingStub virtualServerService = serviceStubs.getVirtualServerBinding();

        String errorFileName = getErrorFileName(vsName);
        try {
            LOG.debug(String.format("Attempting to upload the error file: %s for: %s", errorFileName, vsName));
            extraService.uploadFile(errorFileName, content.getBytes());
            LOG.info(String.format("Successfully uploaded the error file: %s for: %s...", errorFileName, vsName));

            vsNames[0] = String.format("%s", vsName);
            errorFiles[0] = errorFileName;

            LOG.debug("Attempting to set the error file...");
            virtualServerService.setErrorFile(vsNames, errorFiles);
            LOG.info(String.format("Successfully set the error file for: %s...", vsName));
        } catch (InvalidInput ip) {
            LOG.error(StringConverter.getExtendedStackTrace(ip));
            //Couldn't find a custom 'default' error file...
            LOG.error(String.format("The Error file: %s could not be set for: %s", errorFileName, vsName));
            virtualServerService.setErrorFile(vsNames, new String[]{"Default"});

        }
    }

    @Override
    public void removeAndSetDefaultErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws InsufficientRequestException, RemoteException {
        deleteErrorFile(config, loadBalancer);
        setDefaultErrorFile(config, loadBalancer);
    }

    @Override
    public void uploadDefaultErrorFile(LoadBalancerEndpointConfiguration config, String content) throws InsufficientRequestException, RemoteException {
        ZxtmServiceStubs serviceStubs = null;
        serviceStubs = getServiceStubs(config);
        ConfExtraBindingStub extraService = null;
        LOG.debug("Attempting to upload the default error file...");
        extraService = serviceStubs.getZxtmConfExtraBinding();
        if (extraService != null) {
            extraService.uploadFile(Constants.DEFAULT_ERRORFILE, content.getBytes());
            LOG.info("Successfully uploaded the default error file...");
        }
    }

    @Override
    public void setDefaultErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws InsufficientRequestException, RemoteException {
        Integer lbId = loadBalancer.getId();
        Integer accountId = loadBalancer.getAccountId();
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        String virtualServerName = ZxtmNameBuilder.genVSName(lbId, accountId);
        String virtualSecureServerName = ZxtmNameBuilder.genSslVSName(lbId, accountId);
        String virtualRedirectServerName = ZxtmNameBuilder.genRedirectVSName(lbId, accountId);
        String[] vsNames;

        boolean isSecureServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualSecureServerName);
        boolean isRedirectServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualRedirectServerName);

        if (isSecureServer && isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualRedirectServerName;
            vsNames[1] = virtualSecureServerName;
        } else if (isSecureServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualSecureServerName;
        } else if (isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualRedirectServerName;
        } else {
            vsNames = new String[1];
            vsNames[0] = virtualServerName;
        }

        for (String vsName : vsNames) {
            setDefaultErrorFile(config, vsName);
        }
    }

    public void setDefaultErrorFile(LoadBalancerEndpointConfiguration config, String vsName) throws InsufficientRequestException, RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        LOG.debug(String.format("Attempting to set the default error file for: %s", vsName));
        //TODO: uncomment when zeus performance issues are resolved... (VERSION 1) TK-12805
        try {
//        serviceStubs.getVirtualServerBinding().setErrorFile(new String[]{vsName}, new String[]{Constants.DEFAULT_ERRORFILE});
            serviceStubs.getVirtualServerBinding().setErrorFile(new String[]{vsName}, new String[]{"Default"});
            LOG.info(String.format("Successfully set the default error file for: %s", vsName));
        } catch (ObjectDoesNotExist odne) {
            LOG.warn(String.format("Virtual server %s does not exist, ignoring...", vsName));
        }
    }

    @Override
    public void deleteErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws AxisFault, InsufficientRequestException {
        Integer lbId = loadBalancer.getId();
        Integer accountId = loadBalancer.getAccountId();
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        String virtualServerName = ZxtmNameBuilder.genVSName(lbId, accountId);
        String virtualSecureServerName = ZxtmNameBuilder.genSslVSName(lbId, accountId);
        String virtualRedirectServerName = ZxtmNameBuilder.genRedirectVSName(lbId, accountId);
        String[] vsNames;

        try {
            boolean isSecureServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualSecureServerName);
            boolean isRedirectServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualRedirectServerName);

            if (isSecureServer && isRedirectServer) {
                vsNames = new String[2];
                vsNames[0] = virtualRedirectServerName;
                vsNames[1] = virtualSecureServerName;
            } else if (isSecureServer) {
                vsNames = new String[2];
                vsNames[0] = virtualServerName;
                vsNames[1] = virtualSecureServerName;
            } else if (isRedirectServer) {
                vsNames = new String[2];
                vsNames[0] = virtualServerName;
                vsNames[1] = virtualRedirectServerName;
            } else {
                vsNames = new String[1];
                vsNames[0] = virtualServerName;
            }

            for (String vsName : vsNames) {
                deleteErrorFile(config, vsName);
            }
        } catch (RemoteException e) {
            //TODO what should we do here?
        }
    }

    public void deleteErrorFile(LoadBalancerEndpointConfiguration config, String vsName) throws AxisFault, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        String fileToDelete = getErrorFileName(vsName);
        try {
            LOG.debug(String.format("Attempting to delete a custom error file for: %s", vsName));
            serviceStubs.getVirtualServerBinding().setErrorFile(new String[]{vsName}, new String[]{"Default"});
            serviceStubs.getZxtmConfExtraBinding().deleteFile(new String[]{fileToDelete});
            LOG.info(String.format("Successfully deleted a custom error file for: %s", vsName));
        } catch (RemoteException e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.warn(String.format("Cannot delete custom error page as, %s, it does not exist. Ignoring...", fileToDelete));
            }
        } catch (Exception ex) {
            LOG.error(String.format("There was a unexpected error deleting the error file for: %s Exception: %s", vsName, ex.getMessage()));
        }
    }

    @Override
    public void deleteRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException {
        deleteRateLimit(config, ZxtmNameBuilder.genVSName(loadBalancer));
        if (loadBalancer.hasSsl()) {
            deleteRateLimit(config, ZxtmNameBuilder.genSslVSName(loadBalancer));
        }
    }

    public void deleteRateLimit(LoadBalancerEndpointConfiguration config, String vsName) throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        try {
            removeRateLimitRulesFromVirtualServers(serviceStubs, new String[]{vsName});

            LOG.debug(String.format("Removing a rate limit from load balancer...'%s'...", vsName));
            serviceStubs.getZxtmRateCatalogService().deleteRate(new String[]{vsName});
            LOG.info("Successfully removed a rate limit from the rate limit pool.");
            LOG.info("Rules detached from the VS, delete rate limit sucessfully completed.");

        } catch (RemoteException e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.warn(String.format("Cannot delete rate limit for virtual server '%s' as it does not exist. Ignoring...", vsName));
            }
            if (e instanceof ObjectInUse) {
                LOG.warn(String.format("Cannot delete rate limit for virtual server '%s' as it is in use. Ignoring...", vsName));
            }
        }
    }

    @Override
    public void updateRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        updateRateLimit(config, ZxtmNameBuilder.genVSName(loadBalancer), rateLimit);
        if (loadBalancer.hasSsl()) {
            updateRateLimit(config, ZxtmNameBuilder.genSslVSName(loadBalancer), rateLimit);
        }
    }

    @Override
    public void updateTimeout(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        final String poolName = ZxtmNameBuilder.genVSName(loadBalancer.getId(), loadBalancer.getAccountId());
        if (loadBalancer.getTimeout() != null) {
            serviceStubs.getPoolBinding().setMaxReplyTime(new String[]{poolName}, new UnsignedInt[]{new UnsignedInt(loadBalancer.getTimeout())});
        }
    }

    public void updateRateLimit(LoadBalancerEndpointConfiguration config, String vsName, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {

        try {
            LOG.debug(String.format("Removing the current rate limit from load balancer...'%s'...", vsName));
            deleteRateLimit(config, vsName);
            LOG.info("Successfully removed a rate limit from the rate limit pool.");
            LOG.debug("Attaching new rate limit to the virtual server.");
            setRateLimit(config, vsName, rateLimit);
            LOG.info("Rules attached to the VS, update rate limit sucessfully completed.");

        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot update rate limit for virtual server '%s' as it does not exist.", vsName));
            }
            throw new ZxtmRollBackException("Update rate limit request canceled.", e);
        }
    }

    @Override
    public void changeHostForLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb, Host newHost) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        final String trafficManagerName = newHost == null ? config.getTrafficManagerName() : newHost.getTrafficManagerName();
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        List<String> tManagersList = config.getFailoverTrafficManagerNames();
        tManagersList.add(trafficManagerName);
        String[] allTrafficManagers = tManagersList.toArray(new String[config.getFailoverTrafficManagerNames().size()]);

        // Redefine Traffic IP Groups for new HOST
        for (LoadBalancerJoinVip loadBalancerJoinVip : lb.getLoadBalancerJoinVipSet()) {
            String trafficIpGroupName = ZxtmNameBuilder.generateTrafficIpGroupName(lb, loadBalancerJoinVip.getVirtualIp());
            serviceStubs.getTrafficIpGroupBinding().setTrafficManager(new String[]{trafficIpGroupName}, new String[][]{allTrafficManagers});
        }

        for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : lb.getLoadBalancerJoinVip6Set()) {
            String trafficIpGroupName = ZxtmNameBuilder.generateTrafficIpGroupName(lb, loadBalancerJoinVip6.getVirtualIp());
            serviceStubs.getTrafficIpGroupBinding().setTrafficManager(new String[]{trafficIpGroupName}, new String[][]{allTrafficManagers});
        }
    }

    @Override
    public void setNodes(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        Integer lbId = lb.getId();
        Integer accountId = lb.getAccountId();
        Set<Node> nodes = lb.getNodes();
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.genVSName(lbId, accountId);
        final String rollBackMessage = "Set nodes request canceled.";
        final String[][] enabledNodesBackup;
        final String[][] disabledNodesBackup;
        final String[][] drainingNodesBackup;

        try {
            LOG.debug(String.format("Backing up nodes for existing pool '%s'", poolName));
            enabledNodesBackup = serviceStubs.getPoolBinding().getNodes(new String[]{poolName});
            disabledNodesBackup = serviceStubs.getPoolBinding().getDisabledNodes(new String[]{poolName});
            drainingNodesBackup = serviceStubs.getPoolBinding().getDrainingNodes(new String[]{poolName});
            LOG.debug(String.format("Backup for existing pool '%s' created.", poolName));

            LOG.debug(String.format("Setting nodes for existing pool '%s'", poolName));
            serviceStubs.getPoolBinding().setNodes(new String[]{poolName}, NodeHelper.getIpAddressesFromNodes(nodes));
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot set nodes for pool '%s' as it does not exist.", poolName), e);
            }
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        try {
            setDisabledNodes(config, poolName, getNodesWithCondition(nodes, NodeCondition.DISABLED));
            setDrainingNodes(config, poolName, getNodesWithCondition(nodes, NodeCondition.DRAINING));
            setNodeWeights(config, lbId, accountId, nodes);
            setNodesPriorities(config, poolName, lb);
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            if (e instanceof InvalidInput) {
                LOG.error(String.format("Error setting node conditions for pool '%s'. All nodes cannot be disabled.", poolName), e);
            }

            LOG.debug(String.format("Restoring pool '%s' with backup...", poolName));
            serviceStubs.getPoolBinding().setNodes(new String[]{poolName}, enabledNodesBackup);
            serviceStubs.getPoolBinding().setDisabledNodes(new String[]{poolName}, disabledNodesBackup);
            serviceStubs.getPoolBinding().setDrainingNodes(new String[]{poolName}, drainingNodesBackup);
            LOG.debug(String.format("Backup successfully restored for pool '%s'.", poolName));

            throw new ZxtmRollBackException(rollBackMessage, e);
        }
    }

    private void setDisabledNodes(LoadBalancerEndpointConfiguration config, String poolName, List<Node> nodesToDisable) throws RemoteException {
        if (nodesToDisable == null || nodesToDisable.isEmpty()) {
            return;
        }

        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        LOG.debug(String.format("Setting disabled nodes for pool '%s'", poolName));
        serviceStubs.getPoolBinding().setDisabledNodes(new String[]{poolName}, NodeHelper.getIpAddressesFromNodes(nodesToDisable));
        LOG.debug(String.format("Successfully set disabled nodes for pool '%s'", poolName));
    }

    private void setDrainingNodes(LoadBalancerEndpointConfiguration config, String poolName, List<Node> nodesToDrain) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        String[][] currentDrainingNodes = serviceStubs.getPoolBinding().getDrainingNodes(new String[]{poolName});

        if (nodesToDrain != null && !nodesToDrain.isEmpty()) {
            LOG.debug(String.format("Setting draining nodes for pool '%s'", poolName));
            serviceStubs.getPoolBinding().setDrainingNodes(new String[]{poolName}, NodeHelper.getIpAddressesFromNodes(nodesToDrain));
            LOG.debug(String.format("Successfully set draining nodes for pool '%s'", poolName));
        } else if (currentDrainingNodes[0].length > 0) {
            LOG.debug(String.format("Removing draining nodes for pool '%s'", poolName));
            serviceStubs.getPoolBinding().removeDrainingNodes(new String[]{poolName}, currentDrainingNodes);
            LOG.debug(String.format("Successfully removed draining nodes for pool '%s'", poolName));
        }
    }

    @Override
    public void removeNodes(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes)
            throws AxisFault, InsufficientRequestException, ZxtmRollBackException {

        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.genVSName(lbId, accountId);
        final String rollBackMessage = "Remove node request canceled.";

        try {
            String[][] ipAndPorts = NodeHelper.getIpAddressesFromNodes(nodes);
            serviceStubs.getPoolBinding().removeNodes(new String[]{poolName}, ipAndPorts);
        } catch (ObjectDoesNotExist odne) {
            LOG.warn(String.format("Node pool '%s' for nodes %s does not exist.", poolName, NodeHelper.getNodeIdsStr(nodes)));
            LOG.error(StringConverter.getExtendedStackTrace(odne));
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            throw new ZxtmRollBackException(rollBackMessage, e);
        }
    }

    @Override
    public void removeNode(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, String ipAddress, Integer port)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {


        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.genVSName(lbId, accountId);
        final String rollBackMessage = "Remove node request canceled.";

        try {
            serviceStubs.getPoolBinding().removeNodes(new String[]{poolName}, NodeHelper.buildNodeInfo(ipAddress, port));
        } catch (ObjectDoesNotExist odne) {
            LOG.warn(String.format("Node pool '%s' for node '%s:%d' does not exist.", poolName, ipAddress, port));
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            throw new ZxtmRollBackException(rollBackMessage, e);
        }
    }

    @Override
    public void setNodeWeights(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.genVSName(lbId, accountId);
        final String rollBackMessage = "Update node weights request canceled.";

        try {
            final PoolLoadBalancingAlgorithm[] loadBalancingAlgorithm = serviceStubs.getPoolBinding().getLoadBalancingAlgorithm(new String[]{poolName});
            if (loadBalancingAlgorithm[0].equals(PoolLoadBalancingAlgorithm.wroundrobin) || loadBalancingAlgorithm[0].equals(PoolLoadBalancingAlgorithm.wconnections)) {
                LOG.debug(String.format("Setting node weights for pool '%s'...", poolName));
                serviceStubs.getPoolBinding().setNodesWeightings(new String[]{poolName}, buildPoolWeightingsDefinition(nodes));
                LOG.info(String.format("Node weights successfully set for pool '%s'.", poolName));
            }
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Node pool '%s' does not exist. Cannot update node weights", poolName), e);
            }
            if (e instanceof InvalidInput) {
                LOG.error(String.format("Node weights are out of range for node pool '%s'. Cannot update node weights", poolName), e);
            }
            LOG.error(StringConverter.getExtendedStackTrace(e));
            throw new ZxtmRollBackException(rollBackMessage, e);
        }
    }

    public void setSessionPersistence(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
    }

    public void removeSessionPersistence(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
    }

    @Override
    public void setSessionPersistence(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, SessionPersistence mode)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.genVSName(lbId, accountId);
        boolean httpCookieClassConfigured = false;
        boolean sourceIpClassConfigured = false;
        boolean sslIdClassConfigured = false;
        final String rollBackMessage = "Update session persistence request canceled.";

        LOG.debug(String.format("Setting session persistence for node pool '%s'...", poolName));

        String[] persistenceClasses = serviceStubs.getPersistenceBinding().getPersistenceNames();

        // Iterate through all persistence classes to determine if the
        // cookie and source IP class exist. If they exist, then it is
        // assumed they are configured correctly.
        if (persistenceClasses != null) {
            for (String persistenceClass : persistenceClasses) {
                if (persistenceClass.equals(HTTP_COOKIE)) {
                    httpCookieClassConfigured = true;
                }
                if (persistenceClass.equals(SOURCE_IP)) {
                    sourceIpClassConfigured = true;
                }
                if (persistenceClass.equals(SSL_ID)) {
                    sslIdClassConfigured = true;
                }
            }
        }

        // Create the HTTP cookie class if it is not yet configured.
        if (!httpCookieClassConfigured) {
            serviceStubs.getPersistenceBinding().addPersistence(new String[]{HTTP_COOKIE});
            serviceStubs.getPersistenceBinding().setType(new String[]{HTTP_COOKIE}, new CatalogPersistenceType[]{CatalogPersistenceType.value4});
            serviceStubs.getPersistenceBinding().setFailureMode(new String[]{HTTP_COOKIE}, new CatalogPersistenceFailureMode[]{CatalogPersistenceFailureMode.newnode});
        }

        // Create the source IP class if it is not yet configured.
        if (!sourceIpClassConfigured) {
            serviceStubs.getPersistenceBinding().addPersistence(new String[]{SOURCE_IP});
            serviceStubs.getPersistenceBinding().setType(new String[]{SOURCE_IP}, new CatalogPersistenceType[]{CatalogPersistenceType.value1});
            serviceStubs.getPersistenceBinding().setFailureMode(new String[]{SOURCE_IP}, new CatalogPersistenceFailureMode[]{CatalogPersistenceFailureMode.newnode});
        }

        // Create the SSL_ID class if it is not yet configured
        if (!sslIdClassConfigured) {
            serviceStubs.getPersistenceBinding().addPersistence(new String[]{SSL_ID});
            serviceStubs.getPersistenceBinding().setType(new String[]{SSL_ID}, new CatalogPersistenceType[]{CatalogPersistenceType.value9});
            serviceStubs.getPersistenceBinding().setFailureMode(new String[]{SSL_ID}, new CatalogPersistenceFailureMode[]{CatalogPersistenceFailureMode.newnode});
        }

        try {
            // Set the session persistence mode for the pool.
            serviceStubs.getPoolBinding().setPersistence(new String[]{poolName}, getPersistenceMode(mode));
        } catch (Exception e) {

            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Node pool '%s' does not exist. Cannot update session persistence.", poolName));
            }
            LOG.error(StringConverter.getExtendedStackTrace(e));
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        LOG.info(String.format("Session persistence successfully set for node pool '%s'.", poolName));
    }

    @Override
    public void removeSessionPersistence(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.genVSName(lbId, accountId);
        final String rollBackMessage = "Remove session persistence request canceled.";

        try {
            LOG.debug(String.format("Removing session persistence from node pool '%s'...", poolName));
            serviceStubs.getPoolBinding().setPersistence(new String[]{poolName}, new String[]{""});
            LOG.info(String.format("Session persistence successfully removed from node pool '%s'.", poolName));
        } catch (ObjectDoesNotExist odne) {
            LOG.warn(String.format("Node pool '%s' does not exist. No session persistence to remove.", poolName));
        } catch (Exception e) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            throw new ZxtmRollBackException(rollBackMessage, e);
        }
    }

    @Override
    public void updateConnectionLogging(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {

        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        Integer lbId = loadBalancer.getId();
        Integer accountId = loadBalancer.getAccountId();

        String virtualServerName = ZxtmNameBuilder.genVSName(lbId, accountId);
        String virtualSecureServerName = ZxtmNameBuilder.genSslVSName(lbId, accountId);
        String virtualRedirectServerName = ZxtmNameBuilder.genRedirectVSName(lbId, accountId);
        String[] vsNames;

        boolean isSecureServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualSecureServerName);
        boolean isRedirectServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualRedirectServerName);

        if (isSecureServer && isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualRedirectServerName;
            vsNames[1] = virtualSecureServerName;
        } else if (isSecureServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualSecureServerName;
        } else if (isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualRedirectServerName;
        } else {
            vsNames = new String[1];
            vsNames[0] = virtualServerName;
        }

        for (String vsName : vsNames) {
            updateConnectionLogging(config, loadBalancer, vsName);
        }
    }

    private void updateConnectionLogging(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String virtualServerName)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {

        boolean isConnectionLogging = loadBalancer.isConnectionLogging();
        LoadBalancerProtocol protocol = loadBalancer.getProtocol();

        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        final String rollBackMessage = "Update connection logging request canceled.";

        if (isConnectionLogging) {
            LOG.debug(String.format("ENABLING logging for virtual server '%s'...", virtualServerName));
        } else {
            LOG.debug(String.format("DISABLING logging for virtual server '%s'...", virtualServerName));
        }

        try {
            if (isConnectionLogging) {
                if (protocol != LoadBalancerProtocol.HTTP) {
                    serviceStubs.getVirtualServerBinding().setLogFormat(new String[]{virtualServerName}, new String[]{NON_HTTP_LOG_FORMAT});
                } else if (protocol == LoadBalancerProtocol.HTTP) {
                    serviceStubs.getVirtualServerBinding().setLogFormat(new String[]{virtualServerName}, new String[]{HTTP_LOG_FORMAT});
                }

                serviceStubs.getVirtualServerBinding().setLogFilename(new String[]{virtualServerName}, new String[]{config.getLogFileLocation()});
            }
            serviceStubs.getVirtualServerBinding().setLogEnabled(new String[]{virtualServerName}, new boolean[]{isConnectionLogging});

        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Virtual server '%s' does not exist. Cannot update connection logging.", virtualServerName));
            }
            LOG.error(StringConverter.getExtendedStackTrace(e));
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        LOG.info(String.format("Successfully updated connection logging for virtual server '%s'...", virtualServerName));
    }

    @Override
    public void updateContentCaching(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        Integer lbId = loadBalancer.getId();
        Integer accountId = loadBalancer.getAccountId();

        String virtualServerName = ZxtmNameBuilder.genVSName(lbId, accountId);
        String virtualSecureServerName = ZxtmNameBuilder.genSslVSName(lbId, accountId);
        String virtualRedirectServerName = ZxtmNameBuilder.genRedirectVSName(lbId, accountId);
        String[] vsNames;

        boolean isSecureServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualSecureServerName);
        boolean isRedirectServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualRedirectServerName);

        if (isSecureServer && isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualRedirectServerName;
            vsNames[1] = virtualSecureServerName;
        } else if (isSecureServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualSecureServerName;
        } else if (isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualRedirectServerName;
        } else {
            vsNames = new String[1];
            vsNames[0] = virtualServerName;
        }

        for (String vsName : vsNames) {
            updateContentCaching(config, loadBalancer, vsName);
        }
    }

    private void updateContentCaching(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String virtualServerName)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        boolean isContentCaching = false;

        if (loadBalancer.isContentCaching() != null) {
            isContentCaching = loadBalancer.isContentCaching();
        }

        LoadBalancerProtocol protocol = loadBalancer.getProtocol();
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String rollBackMessage = "Update content caching request canceled.";

        if (isContentCaching) {
            LOG.debug(String.format("ENABLING content caching for virtual server '%s'...", virtualServerName));
        } else {
            LOG.debug(String.format("DISABLING content caching for virtual server '%s'...", virtualServerName));
        }

        try {
            VirtualServerRule contentCachingRule = ZxtmAdapterImpl.ruleContentCaching;
            if (isContentCaching) {

                LOG.debug("Attach content caching rule and enable on the virtual server.");

                if (protocol.equals(LoadBalancerProtocol.HTTP)) {
                    serviceStubs.getVirtualServerBinding().addRules(new String[]{virtualServerName}, new VirtualServerRule[][]{{contentCachingRule}});
                    serviceStubs.getVirtualServerBinding().setWebcacheEnabled(new String[]{virtualServerName}, new boolean[]{isContentCaching});
                    LOG.info("Rules attached to the VS, update content caching successfully completed.");
                } else {
                    LOG.info("Content caching rule not set because loadbalancer protocol is not HTTP.");
                    serviceStubs.getVirtualServerBinding().setWebcacheEnabled(new String[]{virtualServerName}, new boolean[]{false});
                    serviceStubs.getVirtualServerBinding().removeRules(new String[]{virtualServerName}, new String[][]{{contentCachingRule.getName()}});
                }
            } else {
                LOG.info("Removing content caching rule from virtualserver: " + virtualServerName);
                serviceStubs.getVirtualServerBinding().setWebcacheEnabled(new String[]{virtualServerName}, new boolean[]{false});
                serviceStubs.getVirtualServerBinding().removeRules(new String[]{virtualServerName}, new String[][]{{contentCachingRule.getName()}});
            }

        } catch (ObjectDoesNotExist obdne) {
            LOG.error("Virtual server not found, ignoring this request......" + obdne);
            LOG.error(StringConverter.getExtendedStackTrace(obdne));
        } catch (DeploymentError e) {
            LOG.error("Error updating content caching..." + e);
            LOG.error(StringConverter.getExtendedStackTrace(e));
            throw new ZxtmRollBackException(rollBackMessage, e);
        } catch (InvalidInput e) {
            LOG.error("Content caching not found, ignoring..." + e);
            LOG.error(StringConverter.getExtendedStackTrace(e));
        } catch (RemoteException e) {
            LOG.error("Error updating content caching..." + e);
            LOG.error(StringConverter.getExtendedStackTrace(e));
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        LOG.info(String.format("Successfully updated content caching for virtual server '%s'...", virtualServerName));
    }

    @Override
    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        Integer lbId = loadBalancer.getId();
        Integer accountId = loadBalancer.getAccountId();

        String virtualServerName = ZxtmNameBuilder.genVSName(lbId, accountId);
        String virtualSecureServerName = ZxtmNameBuilder.genSslVSName(lbId, accountId);
        String virtualRedirectServerName = ZxtmNameBuilder.genRedirectVSName(lbId, accountId);
        String[] vsNames;

        boolean isSecureServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualSecureServerName);
        boolean isRedirectServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualRedirectServerName);

        if (isSecureServer && isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualRedirectServerName;
            vsNames[1] = virtualSecureServerName;
        } else if (isSecureServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualSecureServerName;
        } else if (isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualRedirectServerName;
        } else {
            vsNames = new String[1];
            vsNames[0] = virtualServerName;
        }

        for (String vsName : vsNames) {
            updateConnectionThrottle(config, loadBalancer, vsName);
        }
    }

    private void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String virtualServerName) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ConnectionLimit throttle = loadBalancer.getConnectionLimit();
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        String protectionClassName = ZxtmNameBuilder.genVSName(loadBalancer);
        String rollBackMessage = "Update connection throttle request canceled.";

        addProtectionClass(config, protectionClassName);

        try {
            if (throttle != null) {
                // This request is "non-negotiable" until Stingray can fix a bug that makes the remaining fields invalid.
                serviceStubs.getProtectionBinding().setPerProcessConnectionCount(new String[]{protectionClassName}, new boolean[]{false});

                if (throttle.getMinConnections() != null) {
                    // Set the minimum connections that will be allowed from any single IP before beginning to apply restrictions.
                    serviceStubs.getProtectionBinding().setMinConnections(new String[]{protectionClassName}, new UnsignedInt[]{new UnsignedInt(ConnectionThrottleDefaultConstants.getMinConnections())});
                }

                if (throttle.getRateInterval() != null) {
                    // Set the rate interval for the rates
                    serviceStubs.getProtectionBinding().setRateTimer(new String[]{protectionClassName}, new UnsignedInt[]{new UnsignedInt(ConnectionThrottleDefaultConstants.getRateInterval())});
                }

                //Odd issue in 9.5 where rateInterval must be set before max rate...

                if (throttle.getMaxConnectionRate() != null) {
                    // Set the maximum connection rate + rate interval
                    serviceStubs.getProtectionBinding().setMaxConnectionRate(new String[]{protectionClassName}, new UnsignedInt[]{new UnsignedInt(ConnectionThrottleDefaultConstants.getMaxConnectionRate())});
                }


                if (throttle.getMaxConnections() != null) {
                    // Set the maximum connections permitted from a single IP address
                    serviceStubs.getProtectionBinding().setMax1Connections(new String[]{protectionClassName}, new UnsignedInt[]{new UnsignedInt(throttle.getMaxConnections())});
                }

                // We wont be using this, but it must be set to 0 as our default
                serviceStubs.getProtectionBinding().setMax10Connections(new String[]{protectionClassName}, new UnsignedInt[]{new UnsignedInt(0)});

                // Apply the service protection to the virtual server.
                serviceStubs.getVirtualServerBinding().setProtection(new String[]{virtualServerName}, new String[]{protectionClassName});
            }
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Protection class '%s' does not exist. Cannot update connection throttling.", protectionClassName));
            }
            LOG.error(StringConverter.getExtendedStackTrace(e));
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        LOG.info(String.format("Successfully updated connection throttle for virtual server '%s'.", virtualServerName));
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        deleteConnectionThrottle(config, loadBalancer, ZxtmNameBuilder.genVSName(loadBalancer));
    }

    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String vsName)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {

        final String rollBackMessage = "Delete connection throttle request canceled.";

        LOG.debug(String.format("Deleting connection throttle for loadbalancer '%s'...", vsName));

        try {
            zeroOutConnectionThrottleConfig(config, loadBalancer);
            updateConnectionThrottle(config, loadBalancer);
            LOG.info(String.format("Successfully zeroed out connection throttle settings for protection class '%s'.", vsName));
        } catch (ZxtmRollBackException zre) {
            LOG.error(StringConverter.getExtendedStackTrace(zre));
            throw new ZxtmRollBackException(rollBackMessage, zre);
        }

        LOG.info(String.format("Successfully deleted connection throttle for loadbalancer '%s'.", vsName));
    }

    private void zeroOutConnectionThrottleConfig(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        zeroOutConnectionThrottleConfig(config, loadBalancer, ZxtmNameBuilder.genVSName(loadBalancer));
        if (loadBalancer.hasSsl()) {
            zeroOutConnectionThrottleConfig(config, loadBalancer, ZxtmNameBuilder.genSslVSName(loadBalancer));
        }
    }

    private void zeroOutConnectionThrottleConfig(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String vsName) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        LOG.debug(String.format("Zeroing out connection throttle settings for protection class '%s'.", vsName));

        ConnectionLimit throttle = new ConnectionLimit();
        throttle.setMaxConnectionRate(0);
        throttle.setMaxConnections(0);
        throttle.setMinConnections(0);
        throttle.setRateInterval(1);
        loadBalancer.setConnectionLimit(throttle);
    }

    @Override
    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        int lbId = loadBalancer.getId();
        int accountId = loadBalancer.getAccountId();
        HealthMonitor healthMonitor = loadBalancer.getHealthMonitor();

        final String poolName = ZxtmNameBuilder.genVSName(lbId, accountId);
        final String monitorName = poolName;

        LOG.debug(String.format("Updating health monitor for node pool '%s'.", poolName));


        addMonitorClass(config, monitorName);

        // Set the properties on the monitor class that apply to all configurations.
        serviceStubs.getMonitorBinding().setDelay(new String[]{monitorName}, new UnsignedInt[]{new UnsignedInt(healthMonitor.getDelay())});
        serviceStubs.getMonitorBinding().setTimeout(new String[]{monitorName}, new UnsignedInt[]{new UnsignedInt(healthMonitor.getTimeout())});
        serviceStubs.getMonitorBinding().setFailures(new String[]{monitorName}, new UnsignedInt[]{new UnsignedInt(healthMonitor.getAttemptsBeforeDeactivation())});

        if (healthMonitor.getType().equals(HealthMonitorType.CONNECT)) {
            serviceStubs.getMonitorBinding().setType(new String[]{monitorName}, new CatalogMonitorType[]{CatalogMonitorType.connect});
        } else if (healthMonitor.getType().equals(HealthMonitorType.HTTP) || healthMonitor.getType().equals(HealthMonitorType.HTTPS)) {
            serviceStubs.getMonitorBinding().setType(new String[]{monitorName}, new CatalogMonitorType[]{CatalogMonitorType.http});
            serviceStubs.getMonitorBinding().setPath(new String[]{monitorName}, new String[]{healthMonitor.getPath()});
            serviceStubs.getMonitorBinding().setStatusRegex(new String[]{monitorName}, new String[]{healthMonitor.getStatusRegex()});
            serviceStubs.getMonitorBinding().setBodyRegex(new String[]{monitorName}, new String[]{healthMonitor.getBodyRegex()});
            serviceStubs.getMonitorBinding().setHostHeader(new String[]{monitorName}, new String[]{healthMonitor.getHostHeader()});
            if (healthMonitor.getType().equals(HealthMonitorType.HTTPS)) {
                serviceStubs.getMonitorBinding().setUseSSL(new String[]{monitorName}, new boolean[]{true});
            }
        } else {
            throw new InsufficientRequestException(String.format("Unsupported monitor type: %s", healthMonitor.getType().name()));
        }

        // Assign monitor to the node pool
        String[][] monitors = new String[1][1];
        monitors[0][0] = monitorName;
        serviceStubs.getPoolBinding().setMonitors(new String[]{poolName}, monitors);

        LOG.info(String.format("Health monitor successfully updated for node pool '%s'.", poolName));
    }

    @Override
    public void removeHealthMonitor(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.genVSName(loadBalancer);
        final String monitorName = poolName;

        String[][] monitors = new String[1][1];
        monitors[0][0] = monitorName;

        try {
            LOG.debug(String.format("Removing health monitor for node pool '%s'...", poolName));
            serviceStubs.getPoolBinding().removeMonitors(new String[]{monitorName}, new String[][]{new String[]{monitorName}});
            LOG.info(String.format("Health monitor successfully removed for node pool '%s'.", poolName));
        } catch (ObjectDoesNotExist odne) {
            LOG.warn(String.format("Node pool '%s' does not exist. Ignoring...", poolName));
        } catch (InvalidInput ii) {
            LOG.warn(String.format("Health monitor for node pool '%s' does not exist. Ignoring.", poolName));
        }

        deleteMonitorClass(serviceStubs, monitorName);
    }

    private void deleteMonitorClass(ZxtmServiceStubs serviceStubs, String monitorName) throws RemoteException {
        try {
            LOG.debug(String.format("Removing monitor class '%s'...", monitorName));
            serviceStubs.getMonitorBinding().deleteMonitors(new String[]{monitorName});
            LOG.info(String.format("Monitor class '%s' successfully removed.", monitorName));
        } catch (ObjectDoesNotExist odne) {
            LOG.warn(String.format("Monitor class '%s' does not exist. Ignoring...", monitorName));
        } catch (ObjectInUse oiu) {
            LOG.error(String.format("Monitor class '%s' is currently in use. Cannot delete.", monitorName));
        }
    }

    @Override
    public void updateHalfClosed(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException {
        String virtualServerName = ZxtmNameBuilder.genVSName(loadBalancer.getId(), loadBalancer.getAccountId());
        String virtualSecureServerName = ZxtmNameBuilder.genSslVSName(loadBalancer.getId(), loadBalancer.getAccountId());
        String virtualRedirectServerName = ZxtmNameBuilder.genRedirectVSName(loadBalancer.getId(), loadBalancer.getAccountId());
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        boolean isRedirectServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualRedirectServerName);
        if (isRedirectServer && loadBalancer.hasSsl()) {
            virtualServerName = virtualRedirectServerName;
        } else if (isRedirectServer) {
            try {
                LOG.debug(String.format("Updating half close support for virtual server '%s': Value: '%s'...", virtualRedirectServerName, loadBalancer.isHalfClosed()));
                serviceStubs.getVirtualServerBinding().setProxyClose(new String[]{virtualRedirectServerName}, new boolean[]{loadBalancer.isHalfClosed()});
            } catch (Exception e) {
                LOG.error("Could not update half close support for virtual server: " + virtualRedirectServerName);
                LOG.error(StringConverter.getExtendedStackTrace(e));
            }
        }
        try {
            LOG.debug(String.format("Updating half close support for virtual server '%s': Value: '%s'...", virtualServerName, loadBalancer.isHalfClosed()));
            serviceStubs.getVirtualServerBinding().setProxyClose(new String[]{virtualServerName}, new boolean[]{loadBalancer.isHalfClosed()});
        } catch (Exception e) {
            LOG.error("Could not update half close support for virtual server: " + virtualServerName);
            LOG.error(StringConverter.getExtendedStackTrace(e));

        }
        if (loadBalancer.hasSsl()) {
            try {
                LOG.debug(String.format("Updating half close support for virtual server '%s': Value: '%s'...", virtualSecureServerName, loadBalancer.isHalfClosed()));
                serviceStubs.getVirtualServerBinding().setProxyClose(new String[]{virtualSecureServerName}, new boolean[]{loadBalancer.isHalfClosed()});
            } catch (Exception e) {
                LOG.error("Could not update half close support for virtual server: " + virtualSecureServerName);
                LOG.error(StringConverter.getExtendedStackTrace(e));
            }
        }
    }

    @Override
    public void updateHttpsRedirect(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException {
        String vsName = ZxtmNameBuilder.genVSName(loadBalancer);
        String vsRedirectName = ZxtmNameBuilder.genRedirectVSName(loadBalancer);

        try {
            LOG.debug(String.format("Updating HTTPS Redirect for virtual server '%s': Value: '%s'...", vsName, loadBalancer.isHttpsRedirect()));
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);

            boolean redirectExists = Arrays.asList(serviceStubs.getVirtualServerBinding().getVirtualServerNames()).contains(vsRedirectName);

            if (!redirectExists && loadBalancer.isHttpsRedirect() != null && loadBalancer.isHttpsRedirect()) {
                if (loadBalancer.hasSsl() == true) {
                    repurposeVirtualServerForHttpsRedirect(loadBalancer, serviceStubs);
                } else {
                    createRedirectVirtualServer(config, loadBalancer);
                }
            } else if (redirectExists && loadBalancer.isHttpsRedirect() == null || !loadBalancer.isHttpsRedirect()) {
                if (loadBalancer.hasSsl()) {
                    restoreVirtualServerFromHttpsRedirect(loadBalancer, serviceStubs);
                } else {
                    deleteVirtualServer(serviceStubs, vsRedirectName);
                }
            }
        } catch (Exception e) {
            LOG.error("Could not update HTTPS Redirect for virtual server: " + vsName);
            LOG.error(StringConverter.getExtendedStackTrace(e));
        }
    }

    @Override
    public void updateAccessList(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException {
        Collection<AccessList> accessListItems = loadBalancer.getAccessLists();
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        String protectionClassName = ZxtmNameBuilder.genVSName(loadBalancer);

        LOG.debug(String.format("Updating access list for protection class '%s'...", protectionClassName));

        if (addProtectionClass(config, protectionClassName)) {
            try {
                zeroOutConnectionThrottleConfig(config, loadBalancer);
            } catch (ZxtmRollBackException zre) {
                LOG.warn("Could not zero out connection throttle settings. Continuing...", zre);
            }
            try {
                updateConnectionThrottle(config, loadBalancer);
            } catch (Exception e) {
                LOG.warn("Could not update connection throttle settings. Continuing...", e);
                LOG.error(StringConverter.getExtendedStackTrace(e));
            }
        }

        LOG.info("Removing the old access list...");
        //remove the current access list...
        deleteAccessList(config, protectionClassName);

        LOG.debug("adding the new access list...");
        //add the new access list...
        serviceStubs.getProtectionBinding().setAllowedAddresses(new String[]{protectionClassName}, buildAccessListItems(accessListItems, AccessListType.ALLOW));
        serviceStubs.getProtectionBinding().setBannedAddresses(new String[]{protectionClassName}, buildAccessListItems(accessListItems, AccessListType.DENY));

        //Not especially efficient, but we need to *make sure* we have added the protection class to all virtual servers (even though they probably are ok already)
        String virtualServerName = ZxtmNameBuilder.genVSName(loadBalancer);
        String virtualSecureServerName = ZxtmNameBuilder.genSslVSName(loadBalancer);
        String virtualRedirectServerName = ZxtmNameBuilder.genRedirectVSName(loadBalancer);
        String[] vsNames;

        boolean isSecureServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualSecureServerName);
        boolean isRedirectServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualRedirectServerName);

        if (isSecureServer && isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualRedirectServerName;
            vsNames[1] = virtualSecureServerName;
        } else if (isSecureServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualSecureServerName;
        } else if (isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualRedirectServerName;
        } else {
            vsNames = new String[1];
            vsNames[0] = virtualServerName;
        }

        for (String vsName : vsNames) {
            // Apply the service protection to the virtual server.
            serviceStubs.getVirtualServerBinding().setProtection(new String[]{vsName}, new String[]{protectionClassName});
        }

        LOG.info(String.format("Successfully updated access list for protection class '%s'...", protectionClassName));
    }

    @Override
    public void deleteAccessList(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException {
        final String protectionClassName = ZxtmNameBuilder.genVSName(lbId, accountId);
        final String secureProtectionClassName = ZxtmNameBuilder.genSslVSName(lbId, accountId);

        deleteAccessList(config, protectionClassName);
        deleteAccessList(config, secureProtectionClassName);
    }

    @Override
    public void deleteAccessList(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException {
    }

    private void deleteAccessList(LoadBalancerEndpointConfiguration config, String protectionClassName)
            throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        try {
            String[][] allowList = serviceStubs.getProtectionBinding().getAllowedAddresses(new String[]{protectionClassName});
            String[][] bannedList = serviceStubs.getProtectionBinding().getBannedAddresses(new String[]{protectionClassName});
            serviceStubs.getProtectionBinding().removeAllowedAddresses(new String[]{protectionClassName}, allowList);
            serviceStubs.getProtectionBinding().removeBannedAddresses(new String[]{protectionClassName}, bannedList);
            //Shouldnt delete this until server is going away, connection throttle needs it, and removing it before theres a connection throttle means
            //updateAccessListFails... should delete when removing the virtual server leave the protectionClass in place until then...
//            serviceStubs.getProtectionBinding().deleteProtection(new String[]{poolName});
        } catch (ObjectDoesNotExist odne) {
            LOG.warn(String.format("Protection class '%s' already deleted.", protectionClassName));
        } catch (ObjectInUse oiu) {
            LOG.warn(String.format("Protection class '%s' is currently in use. Cannot delete.", protectionClassName));
            LOG.error(StringConverter.getExtendedStackTrace(oiu));

        }
    }

    @Override
    public void suspendLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        Integer lbId = lb.getId(), accountId = lb.getAccountId();
        String virtualServerName = ZxtmNameBuilder.genVSName(lbId, accountId);
        String virtualSecureServerName = ZxtmNameBuilder.genSslVSName(lbId, accountId);
        String virtualRedirectServerName = ZxtmNameBuilder.genRedirectVSName(lbId, accountId);
        String[] vsNames;
        boolean[] isEnabled;

        // boolean isSecureServer = lb.isUsingSsl();
        // boolean isRedirectServer = lb.isHttpsRedirect();
        // Why was I doing this? >_> The above should work fine... But since I can't remember, I'll stick with the old inefficient way.
        boolean isSecureServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualSecureServerName);
        boolean isRedirectServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualRedirectServerName);

        if (isSecureServer && isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualRedirectServerName;
            vsNames[1] = virtualSecureServerName;
            isEnabled = new boolean[]{false, false};
        } else if (isSecureServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualSecureServerName;
            isEnabled = new boolean[]{false, false};
        } else if (isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualRedirectServerName;
            isEnabled = new boolean[]{false, false};
        } else {
            vsNames = new String[1];
            vsNames[0] = virtualServerName;
            isEnabled = new boolean[]{false};
        }
        // Disable the virtual servers
        serviceStubs.getVirtualServerBinding().setEnabled(vsNames, isEnabled);

        // Disable the traffic ip groups
        LOG.info(String.format("Grabbing all TIGs related to VS... %s", vsNames));
        for (LoadBalancerJoinVip loadBalancerJoinVip : lb.getLoadBalancerJoinVipSet()) {
            String trafficIpGroupName = ZxtmNameBuilder.generateTrafficIpGroupName(lb, loadBalancerJoinVip.getVirtualIp());
            LOG.info(String.format("Disabling TIG " + trafficIpGroupName + " related to VS: %s", vsNames));
            serviceStubs.getTrafficIpGroupBinding().setEnabled(new String[]{trafficIpGroupName}, new boolean[]{false});
            LOG.info(String.format("Successfully disabled TIG '%s' on VS: %s", trafficIpGroupName, vsNames));
        }

        for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : lb.getLoadBalancerJoinVip6Set()) {
            String trafficIpGroupName = ZxtmNameBuilder.generateTrafficIpGroupName(lb, loadBalancerJoinVip6.getVirtualIp());
            LOG.info(String.format("Disabling TIG " + trafficIpGroupName + " related to VS: %s", vsNames));
            serviceStubs.getTrafficIpGroupBinding().setEnabled(new String[]{trafficIpGroupName}, new boolean[]{false});
            LOG.info(String.format("Successfully disabled TIG '%s' on VS: %s", trafficIpGroupName, vsNames));
        }
    }

    @Override
    public void removeSuspension(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        Integer lbId = lb.getId(), accountId = lb.getAccountId();
        String virtualServerName = ZxtmNameBuilder.genVSName(lbId, accountId);
        String virtualSecureServerName = ZxtmNameBuilder.genSslVSName(lbId, accountId);
        String virtualRedirectServerName = ZxtmNameBuilder.genRedirectVSName(lbId, accountId);
        String[] vsNames;
        boolean[] isEnabled;

        boolean isSecureServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualSecureServerName);
        boolean isRedirectServer = arrayElementSearch(serviceStubs.getVirtualServerBinding().getVirtualServerNames(), virtualRedirectServerName);

        if (isSecureServer && isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualRedirectServerName;
            vsNames[1] = virtualSecureServerName;
            isEnabled = new boolean[]{true, true};
        } else if (isSecureServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualSecureServerName;
            isEnabled = new boolean[]{true, true};
            if (lb.isUsingSsl() && lb.getSslTermination().isSecureTrafficOnly()) {
                isEnabled[0] = false;
            }
        } else if (isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualRedirectServerName;
            isEnabled = new boolean[]{true, true};
        } else {
            vsNames = new String[1];
            vsNames[0] = virtualServerName;
            isEnabled = new boolean[]{true};
        }

        // Disable the traffic ip groups
        LOG.info(String.format("Grabbing all TIGs related to VS... %s", vsNames));
        for (LoadBalancerJoinVip loadBalancerJoinVip : lb.getLoadBalancerJoinVipSet()) {
            String trafficIpGroupName = ZxtmNameBuilder.generateTrafficIpGroupName(lb, loadBalancerJoinVip.getVirtualIp());
            LOG.info(String.format("Enabling TIG " + trafficIpGroupName + " related to VS: %s", vsNames));
            serviceStubs.getTrafficIpGroupBinding().setEnabled(new String[]{trafficIpGroupName}, new boolean[]{true});
            LOG.info(String.format("Successfully enabled TIG '%s' on VS: %s", trafficIpGroupName, vsNames));
        }

        for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : lb.getLoadBalancerJoinVip6Set()) {
            String trafficIpGroupName = ZxtmNameBuilder.generateTrafficIpGroupName(lb, loadBalancerJoinVip6.getVirtualIp());
            LOG.info(String.format("Enabling TIG " + trafficIpGroupName + " related to VS: %s", vsNames));
            serviceStubs.getTrafficIpGroupBinding().setEnabled(new String[]{trafficIpGroupName}, new boolean[]{true});
            LOG.info(String.format("Successfully enabled TIG '%s' on VS: %s", trafficIpGroupName, vsNames));
        }

        // Enable the virtual servers
        serviceStubs.getVirtualServerBinding().setEnabled(vsNames, isEnabled);
    }

    @Override
    public void createHostBackup(LoadBalancerEndpointConfiguration config, String backupName) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        serviceStubs.getSystemBackupsBinding().createBackup(backupName, null);
    }

    @Override
    public void deleteHostBackup(LoadBalancerEndpointConfiguration config, String backupName) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        try {
            serviceStubs.getSystemBackupsBinding().deleteBackups(new String[]{backupName});
        } catch (ObjectDoesNotExist odne) {
            LOG.warn(String.format("Backup '%s' does not exist. Ignoring...", backupName));
        }
    }

    @Override
    public void restoreHostBackup(LoadBalancerEndpointConfiguration config, String backupName) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        serviceStubs.getSystemBackupsBinding().restoreBackup(backupName);
    }

    @Override
    public void setSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        String[] hosts = new String[]{hostssubnet.getHostsubnets().get(0).getName()};

        TrafficIPGroupsSubnetMappingPerHost[] oldMap = serviceStubs.getTrafficIpGroupBinding().getSubnetMappings(hosts);
        Hostssubnet oldHostssubnet = adapter2domainSubnetMapping(oldMap);
        List<Hostssubnet> mergeMaps = new ArrayList<Hostssubnet>();
        mergeMaps.add(hostssubnet);
        mergeMaps.add(oldHostssubnet);
        Hostssubnet newHostssubnet = domainUnionSubnetMapping(mergeMaps);

        TrafficIPGroupsSubnetMappingPerHost[] zeusMap = domain2adaptorSubnetMapping(newHostssubnet);
        serviceStubs.getTrafficIpGroupBinding().setSubnetMappings(zeusMap);
    }

    @Override
    public void deleteSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        String[] hosts = new String[]{hostssubnet.getHostsubnets().get(0).getName()};
        TrafficIPGroupsSubnetMappingPerHost[] oldMap = serviceStubs.getTrafficIpGroupBinding().getSubnetMappings(hosts);
        Hostssubnet oldSubnet = adapter2domainSubnetMapping(oldMap);
        Hostssubnet newSubnet = domainSubnetMappingRemove(oldSubnet, hostssubnet);
        TrafficIPGroupsSubnetMappingPerHost[] newMap = domain2adaptorSubnetMapping(newSubnet);
        serviceStubs.getTrafficIpGroupBinding().setSubnetMappings(newMap); // Sinze zues delete clobbers all mappings
        // Use this bizare cherry pick method instead.
    }

    @Override
    public Hostssubnet getSubnetMappings(LoadBalancerEndpointConfiguration config, String host) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        String[] hosts = new String[]{host};
        TrafficIPGroupsSubnetMappingPerHost[] zeusMap = serviceStubs.getTrafficIpGroupBinding().getSubnetMappings(hosts);
        return adapter2domainSubnetMapping(zeusMap);
    }

    @Override
    public List<String> getStatsSystemLoadBalancerNames(LoadBalancerEndpointConfiguration config) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        List<String> loadBalancerNames = new ArrayList<String>();
        loadBalancerNames.addAll(Arrays.asList(serviceStubs.getSystemStatsBinding().getVirtualservers()));
        return loadBalancerNames;
    }

    @Override
    public Map<String, Integer> getLoadBalancerCurrentConnections(LoadBalancerEndpointConfiguration config, List<String> names)
            throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        Map<String, Integer> currentConnectionMap = new HashMap<String, Integer>();
        int[] currentConnections = serviceStubs.getSystemStatsBinding().getVirtualserverCurrentConn(names.toArray(new String[names.size()]));

        for (int i = 0; i < names.size(); i++) {
            currentConnectionMap.put(names.get(i), currentConnections[i]);
        }

        return currentConnectionMap;
    }

    @Override
    public Integer getLoadBalancerCurrentConnections(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadBalancerId, boolean isSsl) throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        String virtualServerName = isSsl ? ZxtmNameBuilder.genSslVSName(loadBalancerId, accountId) : ZxtmNameBuilder.genVSName(loadBalancerId, accountId);
        String virtualServerNames[] = {virtualServerName};
        int[] ccsArray = serviceStubs.getSystemStatsBinding().getVirtualserverCurrentConn(virtualServerNames);
        return ccsArray[0];
    }

    @Override
    public int getTotalCurrentConnectionsForHost(LoadBalancerEndpointConfiguration config) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        return serviceStubs.getSystemStatsBinding().getTotalCurrentConn();
    }

    @Override
    public Stats getLoadBalancerStats(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException {
        List<ZxtmServiceStubs> allStubs = new ArrayList<ZxtmServiceStubs>();
        for (URI endpoint : config.getSoapStatsEndpoints()) {
            try {
                allStubs.add(getStatsStubs(endpoint.toURL(), config));
            } catch (MalformedURLException e) {
                LOG.error(String.format("Creating stats bindings for endpoint %s failed.", endpoint), e);
                LOG.error(StringConverter.getExtendedStackTrace(e));

            }
        }
//        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        Stats stats = new Stats();
        String virtualServerName;
        if (loadBalancer.isSecureOnly() && loadBalancer.isHttpsRedirect() != null && loadBalancer.isHttpsRedirect()) {
            virtualServerName = ZxtmNameBuilder.genRedirectVSName(loadBalancer);
        } else {
            virtualServerName = ZxtmNameBuilder.genVSName(loadBalancer);
        }
        final String sslVirtualServerName = ZxtmNameBuilder.genSslVSName(loadBalancer);
        int[] connectionTimedOut = new int[1];
        int[] connectionError = new int[1];
        int[] connectionFailure = new int[1];
        int[] dataTimedOut = new int[1];
        int[] keepaliveTimedOut = new int[1];
        int[] maxConnections = new int[]{0};
        int[] currentConnections = new int[1];
        int[] connectionTimedOutSsl = new int[1];
        int[] connectionErrorSsl = new int[1];
        int[] connectionFailureSsl = new int[1];
        int[] dataTimedOutSsl = new int[1];
        int[] keepaliveTimedOutSsl = new int[1];
        int[] maxConnectionsSsl = new int[1];
        int[] currentConnectionsSsl = new int[1];

        if (!loadBalancer.isSecureOnly() && (loadBalancer.isHttpsRedirect() == null || !loadBalancer.isHttpsRedirect())) {
            for (ZxtmServiceStubs serviceStubs : allStubs) {
                connectionTimedOut[0] += serviceStubs.getSystemStatsBinding().getVirtualserverConnectTimedOut(new String[]{virtualServerName})[0];
                connectionError[0] += serviceStubs.getSystemStatsBinding().getVirtualserverConnectionErrors(new String[]{virtualServerName})[0];
                connectionFailure[0] += serviceStubs.getSystemStatsBinding().getVirtualserverConnectionFailures(new String[]{virtualServerName})[0];
                dataTimedOut[0] += serviceStubs.getSystemStatsBinding().getVirtualserverDataTimedOut(new String[]{virtualServerName})[0];
                keepaliveTimedOut[0] += serviceStubs.getSystemStatsBinding().getVirtualserverKeepaliveTimedOut(new String[]{virtualServerName})[0];
                currentConnections[0] += serviceStubs.getSystemStatsBinding().getVirtualserverCurrentConn(new String[]{virtualServerName})[0];
                int max = serviceStubs.getSystemStatsBinding().getVirtualserverMaxConn(new String[]{virtualServerName})[0];
                if (max > maxConnections[0]) {
                    maxConnections[0] = max;
                }
            }
            stats.setConnectTimeOut(connectionTimedOut);
            stats.setConnectError(connectionError);
            stats.setConnectFailure(connectionFailure);
            stats.setDataTimedOut(dataTimedOut);
            stats.setKeepAliveTimedOut(keepaliveTimedOut);
            stats.setMaxConn(maxConnections);
            stats.setCurrentConn(currentConnections);
        } else {
            stats.setConnectTimeOut(new int[]{0});
            stats.setConnectError(new int[]{0});
            stats.setConnectFailure(new int[]{0});
            stats.setDataTimedOut(new int[]{0});
            stats.setKeepAliveTimedOut(new int[]{0});
            stats.setMaxConn(new int[]{0});
            stats.setCurrentConn(new int[]{0});
        }

        if (loadBalancer.isUsingSsl()) {
            for (ZxtmServiceStubs serviceStubs : allStubs) {
                connectionTimedOutSsl[0] += serviceStubs.getSystemStatsBinding().getVirtualserverConnectTimedOut(new String[]{sslVirtualServerName})[0];
                connectionErrorSsl[0] += serviceStubs.getSystemStatsBinding().getVirtualserverConnectionErrors(new String[]{sslVirtualServerName})[0];
                connectionFailureSsl[0] += serviceStubs.getSystemStatsBinding().getVirtualserverConnectionFailures(new String[]{sslVirtualServerName})[0];
                dataTimedOutSsl[0] += serviceStubs.getSystemStatsBinding().getVirtualserverDataTimedOut(new String[]{sslVirtualServerName})[0];
                keepaliveTimedOutSsl[0] += serviceStubs.getSystemStatsBinding().getVirtualserverKeepaliveTimedOut(new String[]{sslVirtualServerName})[0];
                currentConnectionsSsl[0] += serviceStubs.getSystemStatsBinding().getVirtualserverCurrentConn(new String[]{sslVirtualServerName})[0];
                int maxSsl = serviceStubs.getSystemStatsBinding().getVirtualserverMaxConn(new String[]{sslVirtualServerName})[0];
                if (maxSsl > maxConnectionsSsl[0]) {
                    maxConnectionsSsl[0] = maxSsl;
                }
                //            stats.setConnectTimeOut(serviceStubs.getSystemStatsBinding().getVirtualserverConnectTimedOut(new String[]{virtualServerName}));
                //            stats.setConnectError(serviceStubs.getSystemStatsBinding().getVirtualserverConnectionErrors(new String[]{virtualServerName}));
                //            stats.setConnectFailure(serviceStubs.getSystemStatsBinding().getVirtualserverConnectionFailures(new String[]{virtualServerName}));
                //            stats.setDataTimedOut(serviceStubs.getSystemStatsBinding().getVirtualserverDataTimedOut(new String[]{virtualServerName}));
                //            stats.setKeepAliveTimedOut(serviceStubs.getSystemStatsBinding().getVirtualserverKeepaliveTimedOut(new String[]{virtualServerName}));
                //            stats.setMaxConn(serviceStubs.getSystemStatsBinding().getVirtualserverMaxConn(new String[]{virtualServerName}));
                //            stats.setCurrentConn(serviceStubs.getSystemStatsBinding().getVirtualserverCurrentConn(new String[]{virtualServerName}));
                //            stats.setConnectTimeOutSsl(serviceStubs.getSystemStatsBinding().getVirtualserverConnectTimedOut(new String[]{sslVirtualServerName}));
                //            stats.setConnectErrorSsl(serviceStubs.getSystemStatsBinding().getVirtualserverConnectionErrors(new String[]{sslVirtualServerName}));
                //            stats.setConnectFailureSsl(serviceStubs.getSystemStatsBinding().getVirtualserverConnectionFailures(new String[]{sslVirtualServerName}));
                //            stats.setDataTimedOutSsl(serviceStubs.getSystemStatsBinding().getVirtualserverDataTimedOut(new String[]{sslVirtualServerName}));
                //            stats.setKeepAliveTimedOutSsl(serviceStubs.getSystemStatsBinding().getVirtualserverKeepaliveTimedOut(new String[]{sslVirtualServerName}));
                //            stats.setMaxConnSsl(serviceStubs.getSystemStatsBinding().getVirtualserverMaxConn(new String[]{sslVirtualServerName}));
                //            stats.setCurrentConnSsl(serviceStubs.getSystemStatsBinding().getVirtualserverCurrentConn(new String[]{sslVirtualServerName}));
            }
            stats.setConnectTimeOutSsl(connectionTimedOutSsl);
            stats.setConnectErrorSsl(connectionErrorSsl);
            stats.setConnectFailureSsl(connectionFailureSsl);
            stats.setDataTimedOutSsl(dataTimedOutSsl);
            stats.setKeepAliveTimedOutSsl(keepaliveTimedOutSsl);
            stats.setMaxConnSsl(maxConnectionsSsl);
            stats.setCurrentConnSsl(currentConnectionsSsl);
        } else {
            stats.setConnectTimeOutSsl(new int[]{0});
            stats.setConnectErrorSsl(new int[]{0});
            stats.setConnectFailureSsl(new int[]{0});
            stats.setDataTimedOutSsl(new int[]{0});
            stats.setKeepAliveTimedOutSsl(new int[]{0});
            stats.setMaxConnSsl(new int[]{0});
            stats.setCurrentConnSsl(new int[]{0});
        }
        return stats;
    }

    @Override
    public Map<String, Long> getLoadBalancerBytesIn(LoadBalancerEndpointConfiguration config, List<String> names) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        Map<String, Long> bytesInMap = new HashMap<String, Long>();

        long[] bytesIn = serviceStubs.getSystemStatsBinding().getVirtualserverBytesIn(
                names.toArray(new String[names.size()]));

        for (int i = 0; i < names.size(); i++) {
            bytesInMap.put(names.get(i), bytesIn[i]);
        }

        return bytesInMap;
    }

    @Override
    public Long getLoadBalancerBytesIn(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadBalancerId, boolean isSsl) throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        String virtualServerName = isSsl ? ZxtmNameBuilder.genSslVSName(loadBalancerId, accountId) : ZxtmNameBuilder.genVSName(loadBalancerId, accountId);
        String virtualServerNames[] = {virtualServerName};
        long[] bytesInArray = serviceStubs.getSystemStatsBinding().getVirtualserverBytesIn(virtualServerNames);
        return bytesInArray[0];
    }

    @Override
    public Map<String, Long> getLoadBalancerBytesOut(LoadBalancerEndpointConfiguration config, List<String> names) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        Map<String, Long> bytesOutMap = new HashMap<String, Long>();

        long[] bytesOut = serviceStubs.getSystemStatsBinding().getVirtualserverBytesOut(
                names.toArray(new String[names.size()]));

        for (int i = 0; i < names.size(); i++) {
            bytesOutMap.put(names.get(i), bytesOut[i]);
        }

        return bytesOutMap;
    }

    @Override
    public Long getLoadBalancerBytesOut(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadBalancerId, boolean isSsl) throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        String virtualServerName = isSsl ? ZxtmNameBuilder.genSslVSName(loadBalancerId, accountId) : ZxtmNameBuilder.genVSName(loadBalancerId, accountId);
        String virtualServerNames[] = {virtualServerName};
        long[] bytesOutArray = serviceStubs.getSystemStatsBinding().getVirtualserverBytesOut(virtualServerNames);
        return bytesOutArray[0];
    }

    @Override
    public Long getHostBytesIn(LoadBalancerEndpointConfiguration config) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        return serviceStubs.getSystemStatsBinding().getTotalBytesIn();
    }

    @Override
    public Long getHostBytesOut(LoadBalancerEndpointConfiguration config) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        return serviceStubs.getSystemStatsBinding().getTotalBytesOut();
    }

    @Override
    public boolean isEndPointWorking(LoadBalancerEndpointConfiguration config) throws RemoteException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            serviceStubs.getPoolBinding().getPoolNames();
            return true;
        } catch (AxisFault af) {
            LOG.error(StringConverter.getExtendedStackTrace(e));
            if (IpHelper.isNetworkConnectionException(af)) {
                return false;
            }
            throw af;
        }

    }

    private boolean[] generateBooleanArray(int size, boolean value) {
        boolean[] array = new boolean[size];
        for (boolean b : array) {
            b = value;
        }
        return array;
    }

    private void createNodePool(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> allNodes, LoadBalancerAlgorithm algorithm) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.genVSName(loadBalancerId, accountId);

        LOG.debug(String.format("Creating pool '%s' and setting nodes...", poolName));
        serviceStubs.getPoolBinding().addPool(new String[]{poolName}, NodeHelper.getIpAddressesFromNodes(allNodes));

        setLoadBalancingAlgorithm(config, loadBalancerId, accountId, algorithm);

        setDisabledNodes(config, poolName, getNodesWithCondition(allNodes, NodeCondition.DISABLED));
        setDrainingNodes(config, poolName, getNodesWithCondition(allNodes, NodeCondition.DRAINING));
        setNodeWeights(config, loadBalancerId, accountId, allNodes);
        serviceStubs.getPoolBinding().setPassiveMonitoring(new String[]{poolName}, new boolean[]{false});
    }

    private List<Node> getNodesWithCondition(Collection<Node> nodes, NodeCondition nodeCondition) {
        List<Node> nodesWithCondition = new ArrayList<Node>();
        for (Node node : nodes) {
            if (node.getCondition().equals(nodeCondition)) {
                nodesWithCondition.add(node);
            }
        }
        return nodesWithCondition;
    }

    /*
     *  Returns true is the protection class is brand new. Returns false if it already exists.
     */
    private boolean addProtectionClass(LoadBalancerEndpointConfiguration config, String poolName) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        boolean isNewProtectionClass = true;

        try {
            LOG.debug(String.format("Adding protection class '%s'...", poolName));
            serviceStubs.getProtectionBinding().addProtection(new String[]{poolName});
            LOG.info(String.format("Protection class '%s' successfully added.", poolName));
        } catch (ObjectAlreadyExists oae) {
            LOG.debug(String.format("Protection class '%s' already exists. Ignoring...", poolName));
            isNewProtectionClass = false;
        }

        return isNewProtectionClass;
    }

    private void addMonitorClass(LoadBalancerEndpointConfiguration config, String monitorName)
            throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        try {
            LOG.debug(String.format("Adding monitor class '%s'...", monitorName));
            serviceStubs.getMonitorBinding().addMonitors(new String[]{monitorName});
            LOG.info(String.format("Monitor class '%s' successfully added.", monitorName));
        } catch (ObjectAlreadyExists oae) {
            LOG.debug(String.format("Monitor class '%s' already exists. Ignoring...", monitorName));
        }
    }

    // Translates from the transfer object "PersistenceMode" to an array of
    // strings, which is used for Zeus.
    private String[] getPersistenceMode(SessionPersistence mode) throws InsufficientRequestException {
        if (mode == SessionPersistence.HTTP_COOKIE) {
            return new String[]{HTTP_COOKIE};
        } else if (mode == SessionPersistence.SOURCE_IP) {
            return new String[]{SOURCE_IP};
        } else if (mode == SessionPersistence.SSL_ID) {
            return new String[]{SSL_ID};
        } else {
            throw new InsufficientRequestException("Unrecognized persistence mode.");
        }
    }

    private PoolWeightingsDefinition[][] buildPoolWeightingsDefinition(Collection<Node> nodes) {
        final PoolWeightingsDefinition[][] poolWeightings = new PoolWeightingsDefinition[1][nodes.size()];
        final Integer DEFAULT_NODE_WEIGHT = 1;

        int i = 0;
        for (Node node : nodes) {
            Integer nodeWeight = node.getWeight() == null ? DEFAULT_NODE_WEIGHT : node.getWeight();
            poolWeightings[0][i] = new PoolWeightingsDefinition(IpHelper.createZeusIpString(node.getIpAddress(), node.getPort()), nodeWeight);
            i++;
        }

        return poolWeightings;
    }

    private String[][] buildAccessListItems(Collection<AccessList> accessListItems, AccessListType type) throws InsufficientRequestException {
        String[][] list;

        if (type == AccessListType.ALLOW) {
            List<AccessList> accessList = getFilteredList(accessListItems, AccessListType.ALLOW);
            list = new String[1][accessList.size()];
            for (int i = 0; i < accessList.size(); i++) {
                list[0][i] = accessList.get(i).getIpAddress();
            }
        } else if (type == AccessListType.DENY) {
            List<AccessList> accessList = getFilteredList(accessListItems, AccessListType.DENY);
            list = new String[1][accessList.size()];
            for (int i = 0; i < accessList.size(); i++) {
                list[0][i] = accessList.get(i).getIpAddress();
            }
        } else {
            throw new InsufficientRequestException(String.format("Unsupported rule type '%s' found when building item list", type));
        }

        return list;
    }

    private List<AccessList> getFilteredList(Collection<AccessList> accessListItems, AccessListType type) {
        List<AccessList> filteredItems = new ArrayList<AccessList>();

        for (AccessList item : accessListItems) {
            if (item.getType() == type) {
                filteredItems.add(item);
            }
        }

        return filteredItems;
    }

    public static Hostssubnet domainSubnetMappingRemove(Hostssubnet oldNet, Hostssubnet removeItems) {
        Hostssubnet out = new Hostssubnet();
        int i;

        Map<String, Map<String, Set<String>>> hmap = new HashMap<String, Map<String, Set<String>>>();
        for (Hostsubnet hsubnet : oldNet.getHostsubnets()) {
            String host = hsubnet.getName();
            if (!hmap.containsKey(host)) {
                hmap.put(host, new HashMap<String, Set<String>>());
            }
            for (NetInterface netInterface : hsubnet.getNetInterfaces()) {
                String eth = netInterface.getName();
                if (!hmap.get(host).containsKey(eth)) {
                    hmap.get(host).put(eth, new HashSet<String>());
                }
                for (Cidr cidr : netInterface.getCidrs()) {
                    String block = cidr.getBlock();
                    hmap.get(host).get(eth).add(cidr.getBlock());
                }
            }
        }

        // Trim oldNetHashMap
        for (Hostsubnet hsubnet : removeItems.getHostsubnets()) {
            String host = hsubnet.getName();
            for (NetInterface netInterface : hsubnet.getNetInterfaces()) {
                String eth = netInterface.getName();
                for (Cidr cidr : netInterface.getCidrs()) {
                    String block = cidr.getBlock();
                    if (hmap.containsKey(host) && hmap.get(host).containsKey(eth)) {
                        hmap.get(host).get(eth).remove((String) block);
                        if (hmap.get(host).get(eth).isEmpty()) {
                            hmap.get(host).remove(eth);
                        }
                        if (hmap.get(host).isEmpty()) {
                            hmap.remove(host);
                        }
                    }
                }
            }
        }

        // copy surviving items
        out = new Hostssubnet();
        for (String host : hmap.keySet()) {
            Hostsubnet hostsubnet = new Hostsubnet();
            hostsubnet.setName(host);
            for (String eth : hmap.get(host).keySet()) {
                NetInterface netInterface = new NetInterface();
                netInterface.setName(eth);
                Object[] cidrObjs = hmap.get(host).get(eth).toArray();
                for (i = 0; i < cidrObjs.length; i++) {
                    String cidrStr = (String) cidrObjs[i];
                    netInterface.getCidrs().add(new Cidr(cidrStr));
                }
                hostsubnet.getNetInterfaces().add(netInterface);
            }
            out.getHostsubnets().add(hostsubnet);
        }

        return out;
    }

    public static Hostssubnet domainUnionSubnetMapping(List<Hostssubnet> hostssubnets) {
        int i;
        Hostssubnet out = new Hostssubnet();
        Map<String, Map<String, Set<String>>> hmap = new HashMap<String, Map<String, Set<String>>>();
        for (Hostssubnet hostssubnet : hostssubnets) {
            for (Hostsubnet hsubnet : hostssubnet.getHostsubnets()) {
                String host = hsubnet.getName();
                if (!hmap.containsKey(host)) {
                    hmap.put(host, new HashMap<String, Set<String>>());
                }
                for (NetInterface netInterface : hsubnet.getNetInterfaces()) {
                    String eth = netInterface.getName();
                    if (!hmap.get(host).containsKey(eth)) {
                        hmap.get(host).put(eth, new HashSet<String>());
                    }
                    for (Cidr cidr : netInterface.getCidrs()) {
                        String block = cidr.getBlock();
                        hmap.get(host).get(eth).add(cidr.getBlock());
                    }
                }
            }
        }

        out = new Hostssubnet();
        for (String host : hmap.keySet()) {
            Hostsubnet hostsubnet = new Hostsubnet();
            hostsubnet.setName(host);
            for (String eth : hmap.get(host).keySet()) {
                NetInterface netInterface = new NetInterface();
                netInterface.setName(eth);
                Object[] cidrObjs = hmap.get(host).get(eth).toArray();
                for (i = 0; i < cidrObjs.length; i++) {
                    String cidrStr = (String) cidrObjs[i];
                    netInterface.getCidrs().add(new Cidr(cidrStr));
                }
                hostsubnet.getNetInterfaces().add(netInterface);
            }
            out.getHostsubnets().add(hostsubnet);
        }

        return out;
    }

    private static Hostssubnet adapter2domainSubnetMapping(TrafficIPGroupsSubnetMappingPerHost[] zeusMap) {
        int i;
        int j;
        int k;

        Hostssubnet hostssubnet = new Hostssubnet();
        for (i = 0; i < zeusMap.length; i++) {
            Hostsubnet hostsubnet = new Hostsubnet();
            String hostName = zeusMap[i].getHostname();
            hostsubnet.setName(hostName);
            TrafficIPGroupsSubnetMapping[] zsubnetMappings = zeusMap[i].getSubnetmappings();
            for (j = 0; j < zsubnetMappings.length; j++) {
                NetInterface iface = new NetInterface();

                String interfaceName = zsubnetMappings[j].get_interface();
                iface.setName(interfaceName);
                String[] subNets = zsubnetMappings[j].getSubnets();
                for (k = 0; k < subNets.length; k++) {
                    Cidr cidr = new Cidr();
                    cidr.setBlock(subNets[k]);
                    iface.getCidrs().add(cidr);
                }
                hostsubnet.getNetInterfaces().add(iface);
            }
            hostssubnet.getHostsubnets().add(hostsubnet);
        }
        return hostssubnet;
    }

    public static TrafficIPGroupsSubnetMappingPerHost[] domain2adaptorSubnetMapping(Hostssubnet hostssubnet) {
        TrafficIPGroupsSubnetMappingPerHost[] zeusMap;
        int i;
        int j;
        int k;
        int smSize;
        int cSize;
        zeusMap = new TrafficIPGroupsSubnetMappingPerHost[hostssubnet.getHostsubnets().size()];
        for (i = 0; i < hostssubnet.getHostsubnets().size(); i++) {
            Hostsubnet hostsubnet = hostssubnet.getHostsubnets().get(i);
            String hostName = hostsubnet.getName();
            zeusMap[i] = new TrafficIPGroupsSubnetMappingPerHost();
            zeusMap[i].setHostname(hostName);
            smSize = hostsubnet.getNetInterfaces().size();
            TrafficIPGroupsSubnetMapping[] zsubnetMappings;
            zsubnetMappings = new TrafficIPGroupsSubnetMapping[smSize];
            for (j = 0; j < hostsubnet.getNetInterfaces().size(); j++) {
                NetInterface iface = hostsubnet.getNetInterfaces().get(j);
                zsubnetMappings[j] = new TrafficIPGroupsSubnetMapping();
                zsubnetMappings[j].set_interface(iface.getName());
                cSize = iface.getCidrs().size();
                String[] cidrs = new String[cSize];
                for (k = 0; k < iface.getCidrs().size(); k++) {
                    cidrs[k] = iface.getCidrs().get(k).getBlock();
                }
                zsubnetMappings[j].setSubnets(cidrs);
            }
            zeusMap[i].setSubnetmappings(zsubnetMappings);
        }
        return zeusMap;
    }

    private String getErrorFileName(String vsName) {
        String msg = String.format("%s_error.html", vsName);
        return msg;
    }

    private boolean arrayElementSearch(String[] namesArray, String searchName) {
        for (int n = 0; n < namesArray.length; n++) {
            if (namesArray[n].equals(searchName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getSslCiphersByVhost(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadbalancerId) throws RemoteException, EntityNotFoundException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        String vsName = String.format("%d_%d_S", accountId, loadbalancerId);
        String[] soapVSNames = new String[]{vsName};
        String[] ciphers = serviceStubs.getVirtualServerBinding().getSSLCiphers(soapVSNames);
        if (ciphers == null || ciphers.length < 1) {
            String errorMsg = String.format("no ciphers found for virtual server  %d_%d_s", accountId, loadbalancerId);
            throw new EntityNotFoundException(errorMsg);
        }
        return ciphers[0];
    }

    @Override
    public void setSslCiphersByVhost(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadbalancerId, String ciphers) throws RemoteException, EntityNotFoundException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        String vsName = String.format("%d_%d_S", accountId, loadbalancerId);
        String[] soapVSName = new String[]{vsName};
        String[] ciphersStrAry = new String[]{ciphers};
        serviceStubs.getVirtualServerBinding().setSSLCiphers(soapVSName, ciphersStrAry);
    }

    @Override
    public String getSsl3Ciphers(LoadBalancerEndpointConfiguration config) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        String ciphers = serviceStubs.getGlobalSettingsBinding().getSSL3Ciphers();


        return ciphers;

    }
}
