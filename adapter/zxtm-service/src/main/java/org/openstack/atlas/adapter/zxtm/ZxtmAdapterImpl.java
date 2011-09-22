package org.openstack.atlas.adapter.zxtm;

import com.zxtm.service.client.*;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.VirtualServerListeningOnAllAddressesException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.adapter.helpers.NodeHelper;
import org.openstack.atlas.adapter.helpers.TrafficScriptHelper;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.Cidr;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Hostsubnet;
import org.openstack.atlas.service.domain.pojos.NetInterface;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.converters.StringConverter;
import org.apache.axis.AxisFault;
import org.apache.axis.types.UnsignedInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import sun.nio.cs.ext.ISO_8859_11;

import java.rmi.RemoteException;
import java.util.*;

import static org.openstack.atlas.service.domain.entities.SessionPersistence.NONE;

public class ZxtmAdapterImpl implements ReverseProxyLoadBalancerAdapter {

    public static Log LOG = LogFactory.getLog(ZxtmAdapterImpl.class.getName());
    public static final LoadBalancerAlgorithm DEFAULT_ALGORITHM = LoadBalancerAlgorithm.RANDOM;
    public static final String SOURCE_IP = "SOURCE_IP";
    public static final String HTTP_COOKIE = "HTTP_COOKIE";
    public static final String RATE_LIMIT_HTTP = "rate_limit_http";
    public static final String RATE_LIMIT_NON_HTTP = "rate_limit_nonhttp";
    public static final String XFF = "add_x_forwarded_for_header";
    public static final VirtualServerRule ruleRateLimitHttp = new VirtualServerRule(RATE_LIMIT_HTTP, true, VirtualServerRuleRunFlag.run_every);
    public static final VirtualServerRule ruleRateLimitNonHttp = new VirtualServerRule(RATE_LIMIT_NON_HTTP, true, VirtualServerRuleRunFlag.run_every);
    public static final VirtualServerRule ruleXForwardedFor = new VirtualServerRule(XFF, true, VirtualServerRuleRunFlag.run_every);

    static {
        //Security.addProvider(new NaiveTrustProvider());
        //Security.setProperty("ssl.TrustManagerFactory.algorithm", NaiveTrustProvider.TRUST_PROVIDER_ALG);
    }

    @Override
    public ZxtmServiceStubs getServiceStubs(LoadBalancerEndpointConfiguration config) throws AxisFault {
        return ZxtmServiceStubs.getServiceStubs(config.getEndpointUrl(), config.getUsername(), config.getPassword());
    }

    @Override
    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String virtualServerName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lb);
        final String poolName = virtualServerName;
        LoadBalancerAlgorithm algorithm = lb.getAlgorithm() == null ? DEFAULT_ALGORITHM : lb.getAlgorithm();
        final String rollBackMessage = "Create load balancer request canceled.";

        LOG.debug(String.format("Creating load balancer '%s'...", virtualServerName));

        try {
            createNodePool(config, lb.getId(), lb.getAccountId(), lb.getNodes());
        } catch (Exception e) {
            deleteNodePool(serviceStubs, poolName);
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        try {
            LOG.debug(String.format("Adding virtual server '%s'...", virtualServerName));
            final VirtualServerBasicInfo vsInfo = new VirtualServerBasicInfo(lb.getPort(), ZxtmConversionUtils.mapProtocol(lb.getProtocol()), poolName);
            serviceStubs.getVirtualServerBinding().addVirtualServer(new String[]{virtualServerName}, new VirtualServerBasicInfo[]{vsInfo});
            LOG.info(String.format("Virtual server '%s' successfully added.", virtualServerName));
        } catch (Exception e) {
            deleteVirtualServer(serviceStubs, virtualServerName);
            deleteNodePool(serviceStubs, poolName);
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        try {
            addVirtualIps(config, lb);
            //Verify that the server is not listening to all addresses, zeus does this by default and is an unwanted behaviour.
            isVSListeningOnAllAddresses(serviceStubs, virtualServerName, poolName);
            serviceStubs.getVirtualServerBinding().setEnabled(new String[]{virtualServerName}, new boolean[]{true});

            /* UPDATE REST OF LOADBALANCER CONFIG */

            setLoadBalancingAlgorithm(config, lb.getId(), lb.getAccountId(), algorithm);

            if (lb.getSessionPersistence() != null && !lb.getSessionPersistence().equals(NONE)) {
                setSessionPersistence(config, lb.getId(), lb.getAccountId(), lb.getSessionPersistence());
            }

            if (lb.getHealthMonitor() != null) {
                updateHealthMonitor(config, lb.getId(), lb.getAccountId(), lb.getHealthMonitor());
            }

            if (lb.getConnectionLimit() != null) {
                updateConnectionThrottle(config, lb.getId(), lb.getAccountId(), lb.getConnectionLimit());
            }

            if (lb.isConnectionLogging() != null && lb.isConnectionLogging()) {
                updateConnectionLogging(config, lb.getId(), lb.getAccountId(), lb.isConnectionLogging(), lb.getProtocol());
            }

            if (lb.getAccessLists() != null && !lb.getAccessLists().isEmpty()) {
                updateAccessList(config, lb.getId(), lb.getAccountId(), lb.getAccessLists());
            }

            if (lb.getProtocol().equals(LoadBalancerProtocol.HTTP)) {
                TrafficScriptHelper.addXForwardedForScriptIfNeeded(serviceStubs);
                attachXFFRuleToVirtualServer(serviceStubs, virtualServerName);
                setDefaultErrorFile(config, lb.getId(), lb.getAccountId());
            }
        } catch (Exception e) {
            deleteLoadBalancer(config, lb);
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        LOG.info(String.format("Load balancer '%s' successfully created.", virtualServerName));
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
    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String virtualServerName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lb);
        final String poolName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lb);

        LOG.debug(String.format("Deleting load balancer '%s'...", virtualServerName));

        removeAndSetDefaultErrorFile(config, lb.getId(), lb.getAccountId());
        removeHealthMonitor(config, lb.getId(), lb.getAccountId());
        deleteRateLimit(config, lb.getId(), lb.getAccountId());
        deleteVirtualServer(serviceStubs, virtualServerName);
        deleteNodePool(serviceStubs, poolName);
        deleteProtectionCatalog(serviceStubs, poolName);
        deleteTrafficIpGroups(serviceStubs, lb);

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
            String trafficIpGroupName = ZxtmNameBuilder.generateTrafficIpGroupName(lb, loadBalancerJoinVip.getVirtualIp());
            deleteTrafficIpGroup(serviceStubs, trafficIpGroupName);
        }

        for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : lb.getLoadBalancerJoinVip6Set()) {
            String trafficIpGroupName = ZxtmNameBuilder.generateTrafficIpGroupName(lb, loadBalancerJoinVip6.getVirtualIp());
            deleteTrafficIpGroup(serviceStubs, trafficIpGroupName);
        }
    }

    private void deleteTrafficIpGroupsX(ZxtmServiceStubs serviceStubs, List<String> trafficIpGroups) throws RemoteException {
        for (String trafficIpGroupName : trafficIpGroups) {
            deleteTrafficIpGroup(serviceStubs, trafficIpGroupName);
        }
    }

    private void deleteTrafficIpGroup(ZxtmServiceStubs serviceStubs, String trafficIpGroupName) throws RemoteException {
        try {
            LOG.debug(String.format("Deleting traffic ip group '%s'...", trafficIpGroupName));
            serviceStubs.getTrafficIpGroupBinding().deleteTrafficIPGroup(new String[]{trafficIpGroupName});
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.debug(String.format("Traffic ip group '%s' already deleted. Ignoring...", trafficIpGroupName));
            }
            if (e instanceof ObjectInUse) {
                LOG.debug(String.format("Traffic ip group '%s' is in use (i.e. shared). Skipping...", trafficIpGroupName));
            }
            if (!(e instanceof ObjectDoesNotExist) && !(e instanceof ObjectInUse)) {
                LOG.debug(String.format("There was an unknown issues deleting traffic ip group: %s", trafficIpGroupName) + e.getMessage());
            }
        }

        try {
            //Verify the TIG was in fact deleted...
            String[][] tig = serviceStubs.getTrafficIpGroupBinding().getTrafficManager(new String[]{trafficIpGroupName});
            if (tig != null) throw new ObjectInUse();
        } catch (ObjectDoesNotExist odne) {
            LOG.debug(String.format(String.format("Traffic ip group '%s' successfully deleted.", trafficIpGroupName)));
        }
    }

    @Override
    public void updateProtocol(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, LoadBalancerProtocol protocol)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String virtualServerName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);

        try {
            if (!protocol.equals(LoadBalancerProtocol.HTTP)) {
                removeSessionPersistence(config, lbId, accountId); // We currently only support HTTP session persistence
                removeXFFRuleFromVirtualServer(serviceStubs, virtualServerName); // XFF is only for the HTTP protocol
            }
        } catch (Exception e) {
            throw new ZxtmRollBackException("Update protocol request canceled.", e);
        }

        try {
            // Update log format to match protocol
            if (serviceStubs.getVirtualServerBinding().getLogEnabled(new String[]{virtualServerName})[0]) {
                updateConnectionLogging(config, lbId, accountId, true, protocol);
            }
        } catch (Exception e) {
            throw new ZxtmRollBackException("Update protocol request canceled.", e);
        }

        try {
            // Drop rate-limit Rule if it exists
            boolean rateLimitExists = false;
            String[] rateNames = serviceStubs.getZxtmRateCatalogService().getRateNames();
            for (String rateName : rateNames) {
                if (rateName.equals(virtualServerName)) {
                    rateLimitExists = true;
                }
            }
            if (rateLimitExists) {
                removeRateLimitRulesFromVirtualServer(serviceStubs, virtualServerName);
            }

            LOG.debug(String.format("Updating protocol for virtual server '%s'...", virtualServerName));
            serviceStubs.getVirtualServerBinding().setProtocol(new String[]{virtualServerName}, new VirtualServerProtocol[]{ZxtmConversionUtils.mapProtocol(protocol)});
            LOG.info(String.format("Successfully updated protocol for virtual server '%s'.", virtualServerName));

            try {
                if (protocol.equals(LoadBalancerProtocol.HTTP)) {
                    TrafficScriptHelper.addXForwardedForScriptIfNeeded(serviceStubs);
                    attachXFFRuleToVirtualServer(serviceStubs, virtualServerName);
                }
            } catch (Exception ex) {
                throw new ZxtmRollBackException("Update protocol request canceled.", ex);
            }

            // Re-add rate-limit Rule
            if (rateLimitExists) {
                attachRateLimitRulesToVirtualServer(serviceStubs, virtualServerName);
            }
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot update protocol for virtual server '%s' as it does not exist.", virtualServerName), e);
            }
            throw new ZxtmRollBackException("Update protocol request canceled.", e);
        }
    }

    @Override
    public void updatePort(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Integer port)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String virtualServerName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);

        try {
            LOG.debug(String.format("Updating port for virtual server '%s'...", virtualServerName));
            serviceStubs.getVirtualServerBinding().setPort(new String[]{virtualServerName}, new UnsignedInt[]{new UnsignedInt(port)});
            LOG.info(String.format("Successfully updated port for virtual server '%s'.", virtualServerName));
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot update port for virtual server '%s' as it does not exist.", virtualServerName), e);
            }
            throw new ZxtmRollBackException("Update port request canceled.", e);
        }
    }

    @Override
    public void setLoadBalancingAlgorithm(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, LoadBalancerAlgorithm algorithm)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);

        try {
            LOG.debug(String.format("Setting load balancing algorithm for node pool '%s'...", poolName));
            serviceStubs.getPoolBinding().setLoadBalancingAlgorithm(new String[]{poolName}, new PoolLoadBalancingAlgorithm[]{ZxtmConversionUtils.mapAlgorithm(algorithm)});
            LOG.info(String.format("Load balancing algorithm successfully set for node pool '%s'...", poolName));
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot update algorithm for node pool '%s' as it does not exist.", poolName), e);
            }
            throw new ZxtmRollBackException("Update algorithm request canceled.", e);
        }
    }

    @Override
    public void addVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String virtualServerName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lb.getId(), lb.getAccountId());
        String[] failoverTrafficManagers = config.getFailoverTrafficManagerNames().toArray(new String[config.getFailoverTrafficManagerNames().size()]);
        final String rollBackMessage = "Add virtual ips request canceled.";
        String[][] currentTrafficIpGroups;
        List<String> updatedTrafficIpGroups = new ArrayList<String>();
        List<String> newTrafficIpGroups = new ArrayList<String>();

        LOG.debug(String.format("Adding virtual ips for virtual server '%s'...", virtualServerName));

        try {
            // Obtain traffic groups currently associated with the virtual server
            currentTrafficIpGroups = serviceStubs.getVirtualServerBinding().getListenTrafficIPGroups(new String[]{virtualServerName});
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error("Cannot add virtual ips to virtual server as it does not exist.", e);
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
            serviceStubs.getVirtualServerBinding().setListenTrafficIPGroups(new String[]{virtualServerName},
                    new String[][]{Arrays.copyOf(updatedTrafficIpGroups.toArray(), updatedTrafficIpGroups.size(), String[].class)});
        } catch (Exception e) {
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
                LOG.warn(String.format("Traffic ip group '%s' does not exist. It looks like it got deleted. Continuing...", trafficIpGroup));
            }
        }

        LOG.info(String.format("Virtual ips successfully added for virtual server '%s'...", virtualServerName));
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
        final String virtualServerName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lb.getId(), lb.getAccountId());
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
    public void setRateLimit(LoadBalancerEndpointConfiguration config, int id, int accountId, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String virtualServerName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(id, accountId);

        try {
            LOG.debug(String.format("Adding a rate limit to load balancer...'%s'...", id));
            serviceStubs.getZxtmRateCatalogService().addRate(new String[]{virtualServerName});
            serviceStubs.getZxtmRateCatalogService().setMaxRatePerSecond(new String[]{virtualServerName}, new UnsignedInt[]{new UnsignedInt(rateLimit.getMaxRequestsPerSecond())});
            LOG.info("Successfully added a rate limit to the rate limit pool.");

            TrafficScriptHelper.addRateLimitScriptsIfNeeded(serviceStubs);
            attachRateLimitRulesToVirtualServer(serviceStubs, virtualServerName);

        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot add rate limit for virtual server '%s' as it does not exist.", virtualServerName));
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

    private void attachXFFRuleToVirtualServer(ZxtmServiceStubs serviceStubs, String virtualServerName) throws RemoteException {
        if (serviceStubs.getVirtualServerBinding().getProtocol(new String[]{virtualServerName})[0].equals(VirtualServerProtocol.http)) {
            LOG.debug(String.format("Attaching the XFF rule and enabling it on load balancer '%s'...", virtualServerName));
            serviceStubs.getVirtualServerBinding().addRules(new String[]{virtualServerName}, new VirtualServerRule[][]{{ZxtmAdapterImpl.ruleXForwardedFor}});
            LOG.debug(String.format("XFF rule successfully enabled on load balancer '%s'.", virtualServerName));
        }
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

    @Override
    public void updateRateLimit(LoadBalancerEndpointConfiguration config, int id, int accountId, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        final String virtualServerName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(id, accountId);

        try {
            LOG.debug(String.format("Removing the current rate limit from load balancer...'%s'...", id));
            deleteRateLimit(config, id, accountId);
            LOG.info("Successfully removed a rate limit from the rate limit pool.");
            LOG.debug("Attaching new rate limit to the virtual server.");
            setRateLimit(config, id, accountId, rateLimit);
            LOG.info("Rules attached to the VS, update rate limit sucessfully completed.");

        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot update rate limit for virtual server '%s' as it does not exist.", virtualServerName));
            }
            throw new ZxtmRollBackException("Update rate limit request canceled.", e);
        }
    }


   // upload the file then set the Errorpage.
    @Override
    public void setErrorFile(LoadBalancerEndpointConfiguration conf, Integer loadbalancerId, Integer accountId, String content) throws RemoteException {
        String[] vsNames = new String[1];
        String[] errorFiles = new String[1];

        ZxtmServiceStubs serviceStubs = getServiceStubs(conf);
        ConfExtraBindingStub extraService = serviceStubs.getZxtmConfExtraBinding();
        VirtualServerBindingStub virtualServerService = serviceStubs.getVirtualServerBinding();

        try {
            String errorFileName = getErrorFileName(loadbalancerId, accountId);

            LOG.debug("Attempting to upload the error file...");
            extraService.uploadFile(errorFileName, content.getBytes());
            LOG.info(String.format("Successfully uploaded the error file for: %s_%s...", accountId, loadbalancerId));

            vsNames[0] = String.format("%d_%d", accountId, loadbalancerId);
            errorFiles[0] = errorFileName;

            LOG.debug("Attempting to set the error file...");
            virtualServerService.setErrorFile(vsNames, errorFiles);
            LOG.info(String.format("Successfully set the error file for: %s_%s...", accountId, loadbalancerId));
        } catch (AxisFault af) {
            if (af instanceof InvalidInput) {
                //Couldn't find a custom 'default' error file...
                errorFiles[1] = "Default";
                virtualServerService.setErrorFile(vsNames, errorFiles);

            }
        }
    }

    @Override
    public void removeAndSetDefaultErrorFile(LoadBalancerEndpointConfiguration config, Integer loadbalancerId, Integer accountId) throws InsufficientRequestException, RemoteException {
        deleteErrorFile(config, loadbalancerId,accountId);
        setDefaultErrorFile(config, loadbalancerId, accountId);
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
    public void setDefaultErrorFile(LoadBalancerEndpointConfiguration config, Integer loadbalancerId, Integer accountid) throws InsufficientRequestException, RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String virtualServerName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(loadbalancerId, accountid);
        LOG.debug(String.format("Attempting to set the default error file for: %s_%s", accountid, loadbalancerId));
        serviceStubs.getVirtualServerBinding().setErrorFile(new String[]{virtualServerName}, new String[]{Constants.DEFAULT_ERRORFILE});
        LOG.info(String.format("Successfully set the default error file for: %s_%s", accountid, loadbalancerId));

    }

    @Override
    public void deleteErrorFile(LoadBalancerEndpointConfiguration config, Integer loadbalancerId,Integer accountId) throws AxisFault {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        String fileToDelete = getErrorFileName(loadbalancerId, accountId);
           try {
            LOG.debug(String.format("Attempting to delete a custom error file for: %s%s",accountId,loadbalancerId));
            serviceStubs.getZxtmConfExtraBinding().deleteFile(new String[]{fileToDelete});
            LOG.info(String.format("Successfully deleted a custom error file for: %s%s",accountId,loadbalancerId));
        } catch (RemoteException e) {
               if (e instanceof ObjectDoesNotExist) {
                LOG.warn(String.format("Cannot delete custom error page as, %s, it does not exist. Ignoring...", fileToDelete));
            }
        }catch(Exception ex){
            LOG.error(String.format("Exception: ",ex));
        }
    }

    @Override
    public void deleteRateLimit(LoadBalancerEndpointConfiguration config, int id, int accountId) throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String virtualServerName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(id, accountId);

        try {
            removeRateLimitRulesFromVirtualServer(serviceStubs, virtualServerName);

            LOG.debug(String.format("Removing a rate limit from load balancer...'%s'...", id));
            serviceStubs.getZxtmRateCatalogService().deleteRate(new String[]{virtualServerName});
            LOG.info("Successfully removed a rate limit from the rate limit pool.");
            LOG.info("Rules detached from the VS, delete rate limit sucessfully completed.");

        } catch (RemoteException e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.warn(String.format("Cannot delete rate limit for virtual server '%s' as it does not exist. Ignoring...", virtualServerName));
            }
            if (e instanceof ObjectInUse) {
                LOG.warn(String.format("Cannot delete rate limit for virtual server '%s' as it is in use. Ignoring...", virtualServerName));
            }
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
    public void setNodes(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
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
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot set nodes for pool '%s' as it does not exist.", poolName), e);
            }
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        try {
            setDisabledNodes(config, poolName, getNodesWithCondition(nodes, NodeCondition.DISABLED));
            setDrainingNodes(config, poolName, getNodesWithCondition(nodes, NodeCondition.DRAINING));
            setNodeWeights(config, lbId, accountId, nodes);
        } catch (Exception e) {
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
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        LOG.debug(String.format("Setting disabled nodes for pool '%s'", poolName));
        serviceStubs.getPoolBinding().setDisabledNodes(new String[]{poolName}, NodeHelper.getIpAddressesFromNodes(nodesToDisable));
    }

    private void setDrainingNodes(LoadBalancerEndpointConfiguration config, String poolName, List<Node> nodesToDrain) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        LOG.debug(String.format("Setting draining nodes for pool '%s'", poolName));
        serviceStubs.getPoolBinding().setDrainingNodes(new String[]{poolName}, NodeHelper.getIpAddressesFromNodes(nodesToDrain));
    }

    @Override
    public void removeNodes(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes)
            throws AxisFault, InsufficientRequestException, ZxtmRollBackException {

        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
        final String rollBackMessage = "Remove node request canceled.";

        try {
            String[][] ipAndPorts = NodeHelper.getIpAddressesFromNodes(nodes);
            serviceStubs.getPoolBinding().removeNodes(new String[]{poolName}, ipAndPorts);
        } catch (ObjectDoesNotExist odne) {
            LOG.warn(String.format("Node pool '%s' for nodes %s does not exist.", poolName, NodeHelper.getNodeIdsStr(nodes)));
            LOG.warn(StringConverter.getExtendedStackTrace(odne));
        } catch (Exception e) {
            throw new ZxtmRollBackException(rollBackMessage, e);
        }
    }

    @Override
    public void removeNode(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, String ipAddress, Integer port)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {


        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
        final String rollBackMessage = "Remove node request canceled.";

        try {
            serviceStubs.getPoolBinding().removeNodes(new String[]{poolName}, NodeHelper.buildNodeInfo(ipAddress, port));
        } catch (ObjectDoesNotExist odne) {
            LOG.warn(String.format("Node pool '%s' for node '%s:%d' does not exist.", poolName, ipAddress, port));
        } catch (Exception e) {
            throw new ZxtmRollBackException(rollBackMessage, e);
        }
    }

    @Override
    public void setNodeWeights(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
        final String rollBackMessage = "Update node weights request canceled.";

        try {
            LOG.debug(String.format("Setting node weights for pool '%s'...", poolName));
            serviceStubs.getPoolBinding().setNodesWeightings(new String[]{poolName}, buildPoolWeightingsDefinition(nodes));
            LOG.info(String.format("Node weights successfully set for pool '%s'.", poolName));
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Node pool '%s' does not exist. Cannot update node weights", poolName), e);
            }
            if (e instanceof InvalidInput) {
                LOG.error(String.format("Node weights are out of range for node pool '%s'. Cannot update node weights", poolName), e);
            }
            throw new ZxtmRollBackException(rollBackMessage, e);
        }
    }

    @Override
    public void setSessionPersistence(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, SessionPersistence mode)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
        boolean httpCookieClassConfigured = false;
        boolean sourceIpClassConfigured = false;
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

        try {
            // Set the session persistence mode for the pool.
            serviceStubs.getPoolBinding().setPersistence(new String[]{poolName}, getPersistenceMode(mode));
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Node pool '%s' does not exist. Cannot update session persistence.", poolName));
            }
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        LOG.info(String.format("Session persistence successfully set for node pool '%s'.", poolName));
    }

    @Override
    public void removeSessionPersistence(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
        final String rollBackMessage = "Remove session persistence request canceled.";

        try {
            LOG.debug(String.format("Removing session persistence from node pool '%s'...", poolName));
            serviceStubs.getPoolBinding().setPersistence(new String[]{poolName}, new String[]{""});
            LOG.info(String.format("Session persistence successfully removed from node pool '%s'.", poolName));
        } catch (ObjectDoesNotExist odne) {
            LOG.warn(String.format("Node pool '%s' does not exist. No session persistence to remove.", poolName));
        } catch (Exception e) {
            throw new ZxtmRollBackException(rollBackMessage, e);
        }
    }

    @Override
    public void updateConnectionLogging(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, boolean isConnectionLogging, LoadBalancerProtocol protocol)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String virtualServerName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
        final String rollBackMessage = "Update connection logging request canceled.";
        final String nonHttpLogFormat = "%v %t %h %A:%p %n %B %b %T";
        final String httpLogFormat = "%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\"";

        if (isConnectionLogging) {
            LOG.debug(String.format("ENABLING logging for virtual server '%s'...", virtualServerName));
        } else {
            LOG.debug(String.format("DISABLING logging for virtual server '%s'...", virtualServerName));
        }

        try {
            if (protocol != LoadBalancerProtocol.HTTP) {
                serviceStubs.getVirtualServerBinding().setLogFormat(new String[]{virtualServerName}, new String[]{nonHttpLogFormat});
            } else if (protocol == LoadBalancerProtocol.HTTP) {
                serviceStubs.getVirtualServerBinding().setLogFormat(new String[]{virtualServerName}, new String[]{httpLogFormat});
            }
            serviceStubs.getVirtualServerBinding().setLogFilename(new String[]{virtualServerName}, new String[]{config.getLogFileLocation()});
            serviceStubs.getVirtualServerBinding().setLogEnabled(new String[]{virtualServerName}, new boolean[]{isConnectionLogging});
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Virtual server '%s' does not exist. Cannot update connection logging.", virtualServerName));
            }
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        LOG.info(String.format("Successfully updated connection logging for virtual server '%s'...", virtualServerName));
    }

    @Override
    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, ConnectionLimit throttle)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String virtualServerName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
        final String protectionClassName = virtualServerName;
        final String rollBackMessage = "Update connection throttle request canceled.";

        LOG.debug(String.format("Updating connection throttle for virtual server '%s'...", virtualServerName));

        addProtectionClass(config, protectionClassName);

        try {
            if (throttle.getMinConnections() != null) {
                // Set the minimum connections that will be allowed from any single IP before beginning to apply restrictions.
                serviceStubs.getProtectionBinding().setMinConnections(new String[]{protectionClassName}, new UnsignedInt[]{new UnsignedInt(throttle.getMinConnections())});
            }

            if (throttle.getMaxConnectionRate() != null) {
                // Set the maximum connection rate + rate interval
                serviceStubs.getProtectionBinding().setMaxConnectionRate(new String[]{protectionClassName}, new UnsignedInt[]{new UnsignedInt(throttle.getMaxConnectionRate())});
            }

            if (throttle.getRateInterval() != null) {
                // Set the rate interval for the rates
                serviceStubs.getProtectionBinding().setRateTimer(new String[]{protectionClassName}, new UnsignedInt[]{new UnsignedInt(throttle.getRateInterval())});
            }

            if (throttle.getMaxConnections() != null) {
                // Set the maximum connections permitted from a single IP address
                serviceStubs.getProtectionBinding().setMax1Connections(new String[]{protectionClassName}, new UnsignedInt[]{new UnsignedInt(throttle.getMaxConnections())});
            }

            // We wont be using this, but it must be set to 0 as our default
            serviceStubs.getProtectionBinding().setMax10Connections(new String[]{protectionClassName}, new UnsignedInt[]{new UnsignedInt(0)});

            // Apply the service protection to the virtual server.
            serviceStubs.getVirtualServerBinding().setProtection(new String[]{virtualServerName}, new String[]{protectionClassName});
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Protection class '%s' does not exist. Cannot update connection throttling.", protectionClassName));
            }
            throw new ZxtmRollBackException(rollBackMessage, e);
        }

        LOG.info(String.format("Successfully updated connection throttle for virtual server '%s'.", virtualServerName));
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
        final String protectionClassName = poolName;
        final String rollBackMessage = "Delete connection throttle request canceled.";

        LOG.debug(String.format("Deleting connection throttle for node pool '%s'...", poolName));

        try {
            zeroOutConnectionThrottleConfig(config, lbId, accountId);
        } catch (ZxtmRollBackException zre) {
            throw new ZxtmRollBackException(rollBackMessage, zre);
        }

        LOG.info(String.format("Successfully deleted connection throttle for node pool '%s'.", poolName));
    }

    private void zeroOutConnectionThrottleConfig(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        final String protectionClassName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(loadBalancerId, accountId);

        LOG.debug(String.format("Zeroing out connection throttle settings for protection class '%s'.", protectionClassName));

        ConnectionLimit throttle = new ConnectionLimit();
        throttle.setMaxConnectionRate(0);
        throttle.setMaxConnections(0);
        throttle.setMinConnections(0);
        throttle.setRateInterval(0);

        updateConnectionThrottle(config, loadBalancerId, accountId, throttle);

        LOG.info(String.format("Successfully zeroed out connection throttle settings for protection class '%s'.", protectionClassName));
    }

    // TODO: Rollback properly
    @Override
    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, HealthMonitor healthMonitor)
            throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
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
            if (healthMonitor.getType().equals(HealthMonitorType.HTTPS)) {
                serviceStubs.getMonitorBinding().setUseSSL(new String[]{monitorName}, new boolean[]{true});
            }
        } else {
            throw new InsufficientRequestException(String.format("Unsupported monitor type: %s", healthMonitor));
        }

        // Assign monitor to the node pool
        String[][] monitors = new String[1][1];
        monitors[0][0] = monitorName;
        serviceStubs.getPoolBinding().setMonitors(new String[]{poolName}, monitors);

        LOG.info(String.format("Health monitor successfully updated for node pool '%s'.", poolName));
    }

    @Override
    public void removeHealthMonitor(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
        final String monitorName = poolName;

        String[][] monitors = new String[1][1];
        monitors[0][0] = monitorName;

        try {
            LOG.debug(String.format("Removing health monitor for node pool '%s'...", poolName));
            serviceStubs.getPoolBinding().removeMonitors(new String[]{poolName}, monitors);
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
    public void updateAccessList(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<AccessList> accessListItems)
            throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String protectionClassName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);

        LOG.debug(String.format("Updating access list for protection class '%s'...", protectionClassName));

        if (addProtectionClass(config, protectionClassName)) {
            try {
                zeroOutConnectionThrottleConfig(config, lbId, accountId);
            } catch (ZxtmRollBackException zre) {
                LOG.warn("Could not zero out connection throttle settings. Continuing...", zre);
            }
        }
        LOG.info("Removing the old access list...");
        //remove the current access list...
        deleteAccessList(config, lbId, accountId);

        LOG.debug("adding the new access list...");
        //add the new access list...
        serviceStubs.getProtectionBinding().setAllowedAddresses(new String[]{protectionClassName}, buildAccessListItems(accessListItems, AccessListType.ALLOW));
        serviceStubs.getProtectionBinding().setBannedAddresses(new String[]{protectionClassName}, buildAccessListItems(accessListItems, AccessListType.DENY));

        LOG.info(String.format("Successfully updated access list for protection class '%s'...", protectionClassName));
    }

    @Override
    public void deleteAccessList(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);

        try {
            // TODO: Do we really need to remove addresses first or can we just call deleteProtection()?
            String[][] allowList = serviceStubs.getProtectionBinding().getAllowedAddresses(new String[]{poolName});
            String[][] bannedList = serviceStubs.getProtectionBinding().getBannedAddresses(new String[]{poolName});
            serviceStubs.getProtectionBinding().removeAllowedAddresses(new String[]{poolName}, allowList);
            serviceStubs.getProtectionBinding().removeBannedAddresses(new String[]{poolName}, bannedList);
            serviceStubs.getProtectionBinding().deleteProtection(new String[]{poolName});
        } catch (ObjectDoesNotExist odne) {
            LOG.warn(String.format("Protection class '%s' already deleted.", poolName));
        } catch (ObjectInUse oiu) {
            LOG.warn(String.format("Protection class '%s' is currently in use. Cannot delete.", poolName));
        }
    }

    @Override
    public void suspendLoadBalancer(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);

        // Disable the virtual server
        serviceStubs.getVirtualServerBinding().setEnabled(new String[]{poolName}, new boolean[]{false});
    }

    @Override
    public void removeSuspension(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);

        // Enable the virtual server
        serviceStubs.getVirtualServerBinding().setEnabled(new String[]{poolName}, new boolean[]{true});
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
    public int getTotalCurrentConnectionsForHost(LoadBalancerEndpointConfiguration config) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        return serviceStubs.getSystemStatsBinding().getTotalCurrentConn();
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
            if (isConnectionExcept(af)) {
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

    private void createNodePool(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> allNodes) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(loadBalancerId, accountId);

        LOG.debug(String.format("Creating pool '%s' and setting nodes...", poolName));
        serviceStubs.getPoolBinding().addPool(new String[]{poolName}, NodeHelper.getIpAddressesFromNodes(allNodes));

        setDisabledNodes(config, poolName, getNodesWithCondition(allNodes, NodeCondition.DISABLED));
        setDrainingNodes(config, poolName, getNodesWithCondition(allNodes, NodeCondition.DRAINING));
        setNodeWeights(config, loadBalancerId, accountId, allNodes);
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

    private boolean isConnectionExcept(AxisFault af) {
        String faultString = af.getFaultString();
        if (faultString == null) {
            return false;
        }
        if (faultString.split(":")[0].equals("java.net.ConnectException")) {
            return true;
        }
        return false;
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

    private String getErrorFileName(Integer loadbalancerId, Integer accountId) {
        String msg = String.format("%d_%d_error.html", accountId, loadbalancerId);
        return msg;
    }
}
