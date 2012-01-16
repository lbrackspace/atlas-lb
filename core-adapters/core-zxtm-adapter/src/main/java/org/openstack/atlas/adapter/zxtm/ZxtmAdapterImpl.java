package org.openstack.atlas.adapter.zxtm;

import com.zxtm.service.client.*;
import org.apache.axis.AxisFault;
import org.apache.axis.types.UnsignedInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerAdapter;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.adapter.exception.BadRequestException;
import org.openstack.atlas.adapter.exception.RollbackException;
import org.openstack.atlas.adapter.zxtm.helper.*;
import org.openstack.atlas.adapter.zxtm.service.ZxtmServiceStubs;
import org.openstack.atlas.common.converters.StringConverter;
import org.openstack.atlas.common.ip.exception.IPStringConversionException1;
import org.openstack.atlas.datamodel.CoreAlgorithmType;
import org.openstack.atlas.datamodel.CoreHealthMonitorType;
import org.openstack.atlas.datamodel.CorePersistenceType;
import org.openstack.atlas.datamodel.CoreProtocolType;
import org.openstack.atlas.service.domain.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;

import java.rmi.RemoteException;
import java.util.*;

@Service
public class ZxtmAdapterImpl implements LoadBalancerAdapter {

    private static Log LOG = LogFactory.getLog(ZxtmAdapterImpl.class.getName());
    public static final String DEFAULT_ALGORITHM = CoreAlgorithmType.ROUND_ROBIN;
    public static final String SOURCE_IP = "SOURCE_IP";
    public static final String HTTP_COOKIE = "HTTP_COOKIE";
    public static final String RATE_LIMIT_HTTP = "rate_limit_http";
    public static final String RATE_LIMIT_NON_HTTP = "rate_limit_nonhttp";
    public static final String XFF = "add_x_forwarded_for_header";
    public static final VirtualServerRule ruleRateLimitHttp = new VirtualServerRule(RATE_LIMIT_HTTP, true, VirtualServerRuleRunFlag.run_every);
    public static final VirtualServerRule ruleRateLimitNonHttp = new VirtualServerRule(RATE_LIMIT_NON_HTTP, true, VirtualServerRuleRunFlag.run_every);
    public static final VirtualServerRule ruleXForwardedFor = new VirtualServerRule(XFF, true, VirtualServerRuleRunFlag.run_every);

    @Override
    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            final String virtualServerName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lb);
            final String poolName = virtualServerName;
            String algorithm = lb.getAlgorithm() == null ? DEFAULT_ALGORITHM : lb.getAlgorithm();
            final String rollBackMessage = "Create load balancer request canceled.";

            LOG.debug(String.format("Creating load balancer '%s'...", virtualServerName));

            try {
                createNodePool(config, lb.getId(), lb.getAccountId(), lb.getNodes());
            } catch (Exception e) {
                deleteNodePool(serviceStubs, poolName);
                throw new RollbackException(rollBackMessage, e);
            }

            try {
                LOG.debug(String.format("Adding virtual server '%s'...", virtualServerName));
                final VirtualServerBasicInfo vsInfo = new VirtualServerBasicInfo(lb.getPort(), ZxtmConversionUtils.mapProtocol(lb.getProtocol()), poolName);
                serviceStubs.getVirtualServerBinding().addVirtualServer(new String[]{virtualServerName}, new VirtualServerBasicInfo[]{vsInfo});
                LOG.info(String.format("Virtual server '%s' successfully added.", virtualServerName));
            } catch (Exception e) {
                deleteVirtualServer(serviceStubs, virtualServerName);
                deleteNodePool(serviceStubs, poolName);
                throw new RollbackException(rollBackMessage, e);
            }

            try {
                addVirtualIps(config, lb);
                serviceStubs.getVirtualServerBinding().setEnabled(new String[]{virtualServerName}, new boolean[]{true});

                /* UPDATE REST OF LOADBALANCER CONFIG */

                setLoadBalancingAlgorithm(serviceStubs, lb.getAccountId(), lb.getId(), algorithm);

                if (lb.getSessionPersistence() != null && lb.getSessionPersistence().getPersistenceType() != null) {
                    setSessionPersistence(config, lb.getId(), lb.getAccountId(), lb.getSessionPersistence());
                }

                if (lb.getHealthMonitor() != null) {
                    updateHealthMonitor(config, lb.getId(), lb.getAccountId(), lb.getHealthMonitor());
                }

                if (lb.getConnectionThrottle() != null) {
                    updateConnectionThrottle(config, lb.getId(), lb.getAccountId(), lb.getConnectionThrottle());
                }

                if (lb.getProtocol().equals(CoreProtocolType.HTTP)) {
                    TrafficScriptHelper.addXForwardedForScriptIfNeeded(serviceStubs);
                    attachXFFRuleToVirtualServer(serviceStubs, virtualServerName);
                }
                afterLoadBalancerCreate(config, lb);
            } catch (Exception e) {
                deleteLoadBalancer(config, lb);
                throw new RollbackException(rollBackMessage, e);
            }

            LOG.info(String.format("Load balancer '%s' successfully created.", virtualServerName));
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * Override this method if you want to do extra stuffs in the extension
     */
    protected void afterLoadBalancerCreate(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws AdapterException {

    }

    /**
     * Override this method if you want to do extra stuffs in the extension
     */
    protected void afterLoadBalancerDelete(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws AdapterException {

    }

    @Override
    public void updateLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            // Core spec only allows modification of algorithm attribute for now
            setLoadBalancingAlgorithm(serviceStubs, lb.getAccountId(), lb.getId(), lb.getAlgorithm());
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            final String virtualServerName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lb.getId(), lb.getAccountId());
            final String poolName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lb.getId(), lb.getAccountId());
            final String[][] trafficIpGroups = serviceStubs.getVirtualServerBinding().getListenTrafficIPGroups(new String[]{virtualServerName});

            LOG.debug(String.format("Deleting load balancer '%s'...", virtualServerName));

            deleteHealthMonitor(config, lb.getId(), lb.getAccountId());
            deleteVirtualServer(serviceStubs, virtualServerName);
            deleteNodePool(serviceStubs, poolName);
            deleteProtectionCatalog(serviceStubs, poolName);
            deleteTrafficIpGroups(serviceStubs, trafficIpGroups[0]);

            afterLoadBalancerDelete(config, lb);

            LOG.info(String.format("Successfully deleted load balancer '%s'.", virtualServerName));
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public void createNodes(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            final String poolName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
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
                List<String> mergedNodes = NodeHelper.getMergedIpAddresses(nodes, enabledNodesBackup[0], disabledNodesBackup[0], drainingNodesBackup[0]);
                String[][] zeusMergedNodes = new String[1][];
                zeusMergedNodes[0] = mergedNodes.toArray(new String[mergedNodes.size()]);
                serviceStubs.getPoolBinding().setNodes(new String[]{poolName}, zeusMergedNodes);
            } catch (RemoteException e) {
                if (e instanceof ObjectDoesNotExist) {
                    LOG.error(String.format("Cannot set nodes for pool '%s' as it does not exist.", poolName), e);
                }
                throw new RollbackException(rollBackMessage, e);
            }

            try {
                final List<String> mergedIpAddresses = NodeHelper.getMergedIpAddresses(getNodesWithCondition(nodes, false), new String[0], disabledNodesBackup[0], new String[0]);
                setDisabledNodes(config, poolName, mergedIpAddresses);
//                setDrainingNodes(config, poolName, getNodesWithCondition(nodes, NodeCondition.DRAINING));
                setNodeWeights(config, lbId, accountId, nodes);
            } catch (RemoteException e) {
                if (e instanceof InvalidInput) {
                    LOG.error(String.format("Error setting node conditions for pool '%s'. All nodes cannot be disabled.", poolName), e);
                }

                LOG.debug(String.format("Restoring pool '%s' with backup...", poolName));
                serviceStubs.getPoolBinding().setNodes(new String[]{poolName}, enabledNodesBackup);
                serviceStubs.getPoolBinding().setDisabledNodes(new String[]{poolName}, disabledNodesBackup);
                serviceStubs.getPoolBinding().setDrainingNodes(new String[]{poolName}, drainingNodesBackup);
                LOG.debug(String.format("Backup successfully restored for pool '%s'.", poolName));

                throw new RollbackException(rollBackMessage, e);
            }
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public void deleteNodes(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            final String poolName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
            final String rollBackMessage = "Remove node request canceled.";

            try {
                serviceStubs.getPoolBinding().removeNodes(new String[]{poolName}, ListUtil.wrap(ListUtil.convert(NodeHelper.getIpAddressesFromNodes(nodes))));
            } catch (ObjectDoesNotExist odne) {
                LOG.warn(String.format("Node pool '%s' for nodes %s does not exist.", poolName, NodeHelper.getNodeIdsStr(nodes)));
                LOG.warn(StringConverter.getExtendedStackTrace(odne));
            } catch (Exception e) {
                throw new RollbackException(rollBackMessage, e);
            }
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public void updateNode(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Node node) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            final String poolName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
            final String rollBackMessage = "Update node request canceled.";
            final String[][] enabledNodesBackup;
            final String[][] disabledNodesBackup;
            final String[][] drainingNodesBackup;
            final List<String> newEnabledNodes = new ArrayList<String>();
            final List<String> newDisabledNodes = new ArrayList<String>();
            final List<String> newDrainingNodes = new ArrayList<String>();
            final String[] newEnabledNodesArray;
            final String[] newDisabledNodesArray;
            final String[] newDrainingNodesArray;

            Set<Node> nodes = new HashSet<Node>();
            nodes.add(node);
            final String nodeAsString = IpHelper.createZeusIpString(node.getAddress(), node.getPort());

            try {
                LOG.debug(String.format("Backing up nodes for existing pool '%s'", poolName));
                enabledNodesBackup = serviceStubs.getPoolBinding().getNodes(new String[]{poolName});
                disabledNodesBackup = serviceStubs.getPoolBinding().getDisabledNodes(new String[]{poolName});
                drainingNodesBackup = serviceStubs.getPoolBinding().getDrainingNodes(new String[]{poolName});
                LOG.debug(String.format("Backup for existing pool '%s' created.", poolName));

                for (String currentEnabledNode : enabledNodesBackup[0]) {
                    if (currentEnabledNode.equals(nodeAsString) && !node.isEnabled()) {
                        newDisabledNodes.add(nodeAsString); // TODO: Should we drain it or disable it?
                    } else {
                        newEnabledNodes.add(currentEnabledNode);
                    }
                }

                for (String currentDisabledNode : disabledNodesBackup[0]) {
                    if (currentDisabledNode.equals(nodeAsString) && node.isEnabled()) {
                        newEnabledNodes.add(nodeAsString);
                    } else {
                        newDisabledNodes.add(currentDisabledNode);
                    }
                }

                for (String currentDrainingNode : drainingNodesBackup[0]) {
                    newDrainingNodes.add(currentDrainingNode);
                }

                newEnabledNodesArray = newEnabledNodes.toArray(new String[newEnabledNodes.size()]);
                newDisabledNodesArray = newDisabledNodes.toArray(new String[newDisabledNodes.size()]);
                newDrainingNodesArray = newDrainingNodes.toArray(new String[newDrainingNodes.size()]);

                LOG.debug(String.format("Setting nodes for existing pool '%s'", poolName));
                String[] mergedNodes = NodeHelper.getMergedIpAddresses(newEnabledNodesArray, newDisabledNodesArray, newDrainingNodesArray);
                serviceStubs.getPoolBinding().setNodes(new String[]{poolName}, ListUtil.wrap(mergedNodes));
            } catch (RemoteException e) {
                if (e instanceof ObjectDoesNotExist) {
                    LOG.error(String.format("Cannot set nodes for pool '%s' as it does not exist.", poolName), e);
                }
                throw new RollbackException(rollBackMessage, e);
            }

            try {
                setDisabledNodes(config, poolName, newDisabledNodes);
//                setDrainingNodes(config, poolName, getNodesWithCondition(nodes, NodeCondition.DRAINING));
                final PoolWeightingsDefinition[][] nodesWeightings = serviceStubs.getPoolBinding().getNodesWeightings(new String[]{poolName}, ListUtil.wrap(ListUtil.wrap(nodeAsString)));
                if (nodesWeightings[0][0].getWeighting() != node.getWeight()) {
                    setNodeWeights(config, lbId, accountId, nodes);
                }
            } catch (RemoteException e) {
                if (e instanceof InvalidInput) {
                    LOG.error(String.format("Error setting node conditions for pool '%s'. All nodes cannot be disabled.", poolName), e);
                }

                LOG.debug(String.format("Restoring pool '%s' with backup...", poolName));
                serviceStubs.getPoolBinding().setNodes(new String[]{poolName}, ListUtil.wrap(NodeHelper.getMergedIpAddresses(enabledNodesBackup[0], disabledNodesBackup[0], drainingNodesBackup[0])));
                serviceStubs.getPoolBinding().setDisabledNodes(new String[]{poolName}, disabledNodesBackup);
                serviceStubs.getPoolBinding().setDrainingNodes(new String[]{poolName}, drainingNodesBackup);
                LOG.debug(String.format("Backup successfully restored for pool '%s'.", poolName));

                throw new RollbackException(rollBackMessage, e);
            }
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, ConnectionThrottle connectionThrottle) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            final String virtualServerName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
            final String protectionClassName = virtualServerName;
            final String rollBackMessage = "Update connection throttle request canceled.";

            LOG.debug(String.format("Updating connection throttle for virtual server '%s'...", virtualServerName));

            addProtectionClass(config, protectionClassName);

            try {
                if (connectionThrottle.getMaxRequestRate() != null) {
                    // Set the maximum connection rate + rate interval
                    serviceStubs.getProtectionBinding().setMaxConnectionRate(new String[]{protectionClassName}, new UnsignedInt[]{new UnsignedInt(connectionThrottle.getMaxRequestRate())});
                }

                if (connectionThrottle.getRateInterval() != null) {
                    // Set the rate interval for the rates
                    serviceStubs.getProtectionBinding().setRateTimer(new String[]{protectionClassName}, new UnsignedInt[]{new UnsignedInt(connectionThrottle.getRateInterval())});
                }

                // We wont be using this, but it must be set to 0 as our default
                serviceStubs.getProtectionBinding().setMax10Connections(new String[]{protectionClassName}, new UnsignedInt[]{new UnsignedInt(0)});

                // Apply the service protection to the virtual server.
                serviceStubs.getVirtualServerBinding().setProtection(new String[]{virtualServerName}, new String[]{protectionClassName});
            } catch (Exception e) {
                if (e instanceof ObjectDoesNotExist) {
                    LOG.error(String.format("Protection class '%s' does not exist. Cannot update connection throttling.", protectionClassName));
                }
                throw new RollbackException(rollBackMessage, e);
            }

            LOG.info(String.format("Successfully updated connection throttle for virtual server '%s'.", virtualServerName));
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException {
        try {
            final String poolName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
            final String protectionClassName = poolName;
            final String rollBackMessage = "Delete connection throttle request canceled.";

            LOG.debug(String.format("Deleting connection throttle for node pool '%s'...", poolName));

            try {
                zeroOutConnectionThrottleConfig(config, lbId, accountId);
            } catch (RollbackException e) {
                throw new RollbackException(rollBackMessage, e);
            }

            LOG.info(String.format("Successfully deleted connection throttle for node pool '%s'.", poolName));
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, HealthMonitor healthMonitor) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            final String poolName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
            final String monitorName = poolName;

            LOG.debug(String.format("Updating health monitor for node pool '%s'.", poolName));

            addMonitorClass(serviceStubs, monitorName);

            // Set the properties on the monitor class that apply to all configurations.
            serviceStubs.getMonitorBinding().setDelay(new String[]{monitorName}, new UnsignedInt[]{new UnsignedInt(healthMonitor.getDelay())});
            serviceStubs.getMonitorBinding().setTimeout(new String[]{monitorName}, new UnsignedInt[]{new UnsignedInt(healthMonitor.getTimeout())});
            serviceStubs.getMonitorBinding().setFailures(new String[]{monitorName}, new UnsignedInt[]{new UnsignedInt(healthMonitor.getAttemptsBeforeDeactivation())});

            if (healthMonitor.getType().equals(CoreHealthMonitorType.CONNECT)) {
                serviceStubs.getMonitorBinding().setType(new String[]{monitorName}, new CatalogMonitorType[]{CatalogMonitorType.connect});
            } else if (healthMonitor.getType().equals(CoreHealthMonitorType.HTTP) || healthMonitor.getType().equals(CoreHealthMonitorType.HTTPS)) {
                serviceStubs.getMonitorBinding().setType(new String[]{monitorName}, new CatalogMonitorType[]{CatalogMonitorType.http});
                serviceStubs.getMonitorBinding().setPath(new String[]{monitorName}, new String[]{healthMonitor.getPath()});
                if (healthMonitor.getType().equals(CoreHealthMonitorType.HTTPS)) {
                    serviceStubs.getMonitorBinding().setUseSSL(new String[]{monitorName}, new boolean[]{true});
                }
            } else {
                throw new BadRequestException(String.format("Unsupported monitor type: %s", healthMonitor.getType()));
            }

            // Assign monitor to the node pool
            String[][] monitors = new String[1][1];
            monitors[0][0] = monitorName;
            serviceStubs.getPoolBinding().setMonitors(new String[]{poolName}, monitors);

            LOG.info(String.format("Health monitor successfully updated for node pool '%s'.", poolName));
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public void deleteHealthMonitor(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            final String poolName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
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
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public void setSessionPersistence(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, SessionPersistence sessionPersistence) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            final String poolName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
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
                serviceStubs.getPoolBinding().setPersistence(new String[]{poolName}, getPersistenceMode(sessionPersistence));
            } catch (Exception e) {
                if (e instanceof ObjectDoesNotExist) {
                    LOG.error(String.format("Node pool '%s' does not exist. Cannot update session persistence.", poolName));
                }
                throw new RollbackException(rollBackMessage, e);
            }

            LOG.info(String.format("Session persistence successfully set for node pool '%s'.", poolName));
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public void deleteSessionPersistence(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            final String poolName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
            final String rollBackMessage = "Remove session persistence request canceled.";

            try {
                LOG.debug(String.format("Removing session persistence from node pool '%s'...", poolName));
                serviceStubs.getPoolBinding().setPersistence(new String[]{poolName}, new String[]{""});
                LOG.info(String.format("Session persistence successfully removed from node pool '%s'.", poolName));
            } catch (ObjectDoesNotExist odne) {
                LOG.warn(String.format("Node pool '%s' does not exist. No session persistence to remove.", poolName));
            } catch (Exception e) {
                throw new RollbackException(rollBackMessage, e);
            }
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }


    /*
     * *******************
     * * PROTECTED METHODS *
     * *******************
     */

    protected ZxtmServiceStubs getServiceStubs(LoadBalancerEndpointConfiguration config) throws AxisFault {
        return ZxtmServiceStubs.getServiceStubs(config.getEndpointUrl(), config.getUsername(), config.getPassword());
    }

    protected void setLoadBalancingAlgorithm(ZxtmServiceStubs serviceStubs, Integer accountId, Integer lbId, String algorithm) throws AdapterException {
        final String poolName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);

        try {
            LOG.debug(String.format("Setting load balancing algorithm for node pool '%s'...", poolName));
            serviceStubs.getPoolBinding().setLoadBalancingAlgorithm(new String[]{poolName}, new PoolLoadBalancingAlgorithm[]{ZxtmConversionUtils.mapAlgorithm(algorithm)});
            LOG.info(String.format("Load balancing algorithm successfully set for node pool '%s'...", poolName));
        } catch (RemoteException e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot update algorithm for node pool '%s' as it does not exist.", poolName), e);
            }
            throw new RollbackException(String.format("Set load balancing algorithm request canceled for node pool '%s'.", poolName), e);
        }
    }

    protected void addVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws RemoteException, AdapterException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String virtualServerName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lb.getId(), lb.getAccountId());
        String[] failoverTrafficManagers = config.getFailoverHostNames().toArray(new String[config.getFailoverHostNames().size()]);
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
            throw new RollbackException(rollBackMessage, e);
        }

        // Add current traffic ip groups to traffic ip group list
        if (currentTrafficIpGroups != null) {
            updatedTrafficIpGroups.addAll(Arrays.asList(currentTrafficIpGroups[0]));
        }

        // Add new traffic ip groups for IPv4 vips
        for (LoadBalancerJoinVip loadBalancerJoinVipToAdd : lb.getLoadBalancerJoinVipSet()) {
            String newTrafficIpGroup = ZxtmNameHelper.generateTrafficIpGroupName(lb, loadBalancerJoinVipToAdd.getVirtualIp());
            newTrafficIpGroups.add(newTrafficIpGroup);
            updatedTrafficIpGroups.add(newTrafficIpGroup);
            createTrafficIpGroup(config, serviceStubs, loadBalancerJoinVipToAdd.getVirtualIp().getAddress(), newTrafficIpGroup);
        }

        // Add new traffic ip groups for IPv6 vips
        for (LoadBalancerJoinVip6 loadBalancerJoinVip6ToAdd : lb.getLoadBalancerJoinVip6Set()) {
            String newTrafficIpGroup = ZxtmNameHelper.generateTrafficIpGroupName(lb, loadBalancerJoinVip6ToAdd.getVirtualIp());
            newTrafficIpGroups.add(newTrafficIpGroup);
            updatedTrafficIpGroups.add(newTrafficIpGroup);
            try {
                createTrafficIpGroup(config, serviceStubs, loadBalancerJoinVip6ToAdd.getVirtualIp().getDerivedIpString(), newTrafficIpGroup);
            } catch (IPStringConversionException1 e) {
                LOG.error("Rolling back newly created traffic ip groups...", e);
                deleteTrafficIpGroups(serviceStubs, newTrafficIpGroups);
                throw new RollbackException("Cannot derive name from IPv6 virtual ip.", e);
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
            deleteTrafficIpGroups(serviceStubs, newTrafficIpGroups);
            throw new RollbackException(rollBackMessage, e);
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
    protected void createTrafficIpGroup(LoadBalancerEndpointConfiguration config, ZxtmServiceStubs serviceStubs, String ipAddress, String newTrafficIpGroup) throws RemoteException {
        final TrafficIPGroupsDetails details = new TrafficIPGroupsDetails(new String[]{ipAddress}, new String[]{config.getHostName()});

        try {
            LOG.debug(String.format("Adding traffic ip group '%s'...", newTrafficIpGroup));
            serviceStubs.getTrafficIpGroupBinding().addTrafficIPGroup(new String[]{newTrafficIpGroup}, new TrafficIPGroupsDetails[]{details});
            LOG.info(String.format("Traffic ip group '%s' successfully added.", newTrafficIpGroup));
        } catch (ObjectAlreadyExists oae) {
            LOG.debug(String.format("Traffic ip group '%s' already exists. Ignoring...", newTrafficIpGroup));
        }
    }

    protected void deleteTrafficIpGroups(ZxtmServiceStubs serviceStubs, String[] trafficIpGroups) throws RemoteException, BadRequestException {
        for (String trafficIpGroupName : trafficIpGroups) {
            deleteTrafficIpGroup(serviceStubs, trafficIpGroupName);
        }
    }

    protected void deleteTrafficIpGroups(ZxtmServiceStubs serviceStubs, List<String> trafficIpGroups) throws RemoteException {
        for (String trafficIpGroupName : trafficIpGroups) {
            deleteTrafficIpGroup(serviceStubs, trafficIpGroupName);
        }
    }

    protected void deleteTrafficIpGroup(ZxtmServiceStubs serviceStubs, String trafficIpGroupName) throws RemoteException {
        try {
            LOG.debug(String.format("Deleting traffic ip group '%s'...", trafficIpGroupName));
            serviceStubs.getTrafficIpGroupBinding().deleteTrafficIPGroup(new String[]{trafficIpGroupName});
            LOG.debug(String.format("Traffic ip group '%s' successfully deleted.", trafficIpGroupName));
        } catch (ObjectDoesNotExist odne) {
            LOG.debug(String.format("Traffic ip group '%s' already deleted. Ignoring...", trafficIpGroupName));
        } catch (ObjectInUse oiu) {
            LOG.debug(String.format("Traffic ip group '%s' is in use (i.e. shared). Skipping...", trafficIpGroupName));
        }
    }

    protected void createNodePool(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> nodes) throws RemoteException, AdapterException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(loadBalancerId, accountId);

        LOG.debug(String.format("Creating pool '%s' and setting nodes...", poolName));
        String[][] ipAddresses = new String[1][];
        ipAddresses[0] = ListUtil.convert(NodeHelper.getIpAddressesFromNodes(nodes));
        serviceStubs.getPoolBinding().addPool(new String[]{poolName}, ipAddresses);

        final List<Node> nodeToDisable = getNodesWithCondition(nodes, false);
        final List<String> nodesAsZeusString = NodeHelper.getIpAddressesFromNodes(nodeToDisable);
        setDisabledNodes(config, poolName, nodesAsZeusString);
//        setDrainingNodes(config, poolName, getNodesWithCondition(allNodes, NodeCondition.DRAINING));
        setNodeWeights(config, loadBalancerId, accountId, nodes);
    }

    protected List<Node> getNodesWithCondition(Collection<Node> nodes, Boolean enabled) {
        List<Node> nodesWithCondition = new ArrayList<Node>();
        for (Node node : nodes) {
            if (node.isEnabled().equals(enabled)) {
                nodesWithCondition.add(node);
            }
        }
        return nodesWithCondition;
    }

    protected void setDisabledNodes(LoadBalancerEndpointConfiguration config, String poolName, List<String> nodesToDisable) throws RemoteException, BadRequestException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        LOG.debug(String.format("Setting disabled nodes for pool '%s'", poolName));
        serviceStubs.getPoolBinding().setDisabledNodes(new String[]{poolName}, ListUtil.wrap(ListUtil.convert(nodesToDisable)));
    }

    protected void setNodeWeights(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes) throws RemoteException, AdapterException {
        setNodeWeights(config, lbId, accountId, buildPoolWeightingsDefinition(nodes));
    }

    protected void setNodeWeights(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, PoolWeightingsDefinition[] definitions) throws RemoteException, AdapterException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
        final String rollBackMessage = "Update node weights request canceled.";

        try {
            LOG.debug(String.format("Setting node weights for pool '%s'...", poolName));
            serviceStubs.getPoolBinding().setNodesWeightings(new String[]{poolName}, ListUtil.wrap(definitions));
            LOG.info(String.format("Node weights successfully set for pool '%s'.", poolName));
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Node pool '%s' does not exist. Cannot update node weights", poolName), e);
            }
            if (e instanceof InvalidInput) {
                LOG.error(String.format("Node weights are out of range for node pool '%s'. Cannot update node weights", poolName), e);
            }
            throw new RollbackException(rollBackMessage, e);
        }
    }

    protected PoolWeightingsDefinition[] buildPoolWeightingsDefinition(Collection<Node> nodes) throws BadRequestException {
        final PoolWeightingsDefinition[] poolWeightings = new PoolWeightingsDefinition[nodes.size()];
        final Integer DEFAULT_NODE_WEIGHT = 1;

        int i = 0;
        for (Node node : nodes) {
            Integer nodeWeight = node.getWeight() == null ? DEFAULT_NODE_WEIGHT : node.getWeight();
            poolWeightings[i] = new PoolWeightingsDefinition(IpHelper.createZeusIpString(node.getAddress(), node.getPort()), nodeWeight);
            i++;
        }

        return poolWeightings;
    }

    protected void deleteVirtualServer(ZxtmServiceStubs serviceStubs, String virtualServerName) throws RemoteException {
        try {
            LOG.debug(String.format("Deleting virtual server '%s'...", virtualServerName));
            serviceStubs.getVirtualServerBinding().deleteVirtualServer(new String[]{virtualServerName});
            LOG.debug(String.format("Virtual server '%s' successfully deleted.", virtualServerName));
        } catch (ObjectDoesNotExist odne) {
            LOG.debug(String.format("Virtual server '%s' already deleted.", virtualServerName));
        }
    }

    protected void deleteNodePool(ZxtmServiceStubs serviceStubs, String poolName) throws RemoteException {
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

    protected void deleteProtectionCatalog(ZxtmServiceStubs serviceStubs, String poolName) throws RemoteException {
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

    protected void addMonitorClass(ZxtmServiceStubs serviceStubs, String monitorName) throws RemoteException {
        try {
            LOG.debug(String.format("Adding monitor class '%s'...", monitorName));
            serviceStubs.getMonitorBinding().addMonitors(new String[]{monitorName});
            LOG.info(String.format("Monitor class '%s' successfully added.", monitorName));
        } catch (ObjectAlreadyExists oae) {
            LOG.debug(String.format("Monitor class '%s' already exists. Ignoring...", monitorName));
        }
    }

    protected void deleteMonitorClass(ZxtmServiceStubs serviceStubs, String monitorName) throws RemoteException {
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

    /*
     *  Returns true is the protection class is brand new. Returns false if it already exists.
     */
    protected boolean addProtectionClass(LoadBalancerEndpointConfiguration config, String poolName) throws RemoteException {
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

    // Translates from the transfer object "PersistenceMode" to an array of
    // strings, which is used for Zeus.
    protected String[] getPersistenceMode(SessionPersistence sessionPersistence) throws BadRequestException {
        if (sessionPersistence.getPersistenceType().equals(CorePersistenceType.HTTP_COOKIE)) {
            return new String[]{HTTP_COOKIE};
        } else {
            throw new BadRequestException("Unrecognized persistence mode.");
        }
    }

    protected void zeroOutConnectionThrottleConfig(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws RemoteException, AdapterException {
        final String protectionClassName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(loadBalancerId, accountId);

        LOG.debug(String.format("Zeroing out connection throttle settings for protection class '%s'.", protectionClassName));

        ConnectionThrottle throttle = new ConnectionThrottle();
        throttle.setMaxRequestRate(0);
        throttle.setRateInterval(0);

        updateConnectionThrottle(config, loadBalancerId, accountId, throttle);

        LOG.info(String.format("Successfully zeroed out connection throttle settings for protection class '%s'.", protectionClassName));
    }

    protected void attachXFFRuleToVirtualServer(ZxtmServiceStubs serviceStubs, String virtualServerName) throws RemoteException {
        if (serviceStubs.getVirtualServerBinding().getProtocol(new String[]{virtualServerName})[0].equals(VirtualServerProtocol.http)) {
            LOG.debug(String.format("Attaching the XFF rule and enabling it on load balancer '%s'...", virtualServerName));
            serviceStubs.getVirtualServerBinding().addRules(new String[]{virtualServerName}, new VirtualServerRule[][]{{ZxtmAdapterImpl.ruleXForwardedFor}});
            LOG.debug(String.format("XFF rule successfully enabled on load balancer '%s'.", virtualServerName));
        }
    }

    protected void removeXFFRuleFromVirtualServer(ZxtmServiceStubs serviceStubs, String virtualServerName) throws RemoteException {
        LOG.debug(String.format("Removing the XFF rule from load balancer '%s'...", virtualServerName));
        VirtualServerRule[][] virtualServerRules = serviceStubs.getVirtualServerBinding().getRules(new String[]{virtualServerName});
        if (virtualServerRules.length > 0) {
            for (VirtualServerRule virtualServerRule : virtualServerRules[0]) {
                if (virtualServerRule.getName().equals(ZxtmAdapterImpl.ruleXForwardedFor.getName()))
                    serviceStubs.getVirtualServerBinding().removeRules(new String[]{virtualServerName}, new String[][]{{ZxtmAdapterImpl.ruleXForwardedFor.getName()}});
            }
        }
        LOG.debug(String.format("XFF rule successfully removed from load balancer '%s'.", virtualServerName));
    }
}
