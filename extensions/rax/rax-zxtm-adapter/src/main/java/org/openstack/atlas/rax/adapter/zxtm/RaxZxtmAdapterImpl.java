package org.openstack.atlas.rax.adapter.zxtm;

import com.zxtm.service.client.*;
import org.apache.axis.AxisFault;
import org.apache.axis.types.UnsignedInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.adapter.exception.BadRequestException;
import org.openstack.atlas.adapter.exception.RollbackException;
import org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl;
import org.openstack.atlas.adapter.zxtm.helper.ZxtmNameHelper;
import org.openstack.atlas.adapter.zxtm.service.ZxtmServiceStubs;
import org.openstack.atlas.datamodel.CoreHealthMonitorType;
import org.openstack.atlas.datamodel.CoreProtocolType;
import org.openstack.atlas.rax.domain.common.RaxConstants;
import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.rax.domain.entity.RaxAccessListType;
import org.openstack.atlas.rax.domain.entity.RaxHealthMonitor;
import org.openstack.atlas.service.domain.common.Constants;
import org.openstack.atlas.service.domain.entity.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.util.*;

@Primary
@Service
public class RaxZxtmAdapterImpl extends ZxtmAdapterImpl implements RaxZxtmAdapter {

    private static Log LOG = LogFactory.getLog(RaxZxtmAdapterImpl.class.getName());

    @Override
    protected void afterLoadBalancerCreate(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws AdapterException {
        if (lb.getProtocol().equals(CoreProtocolType.HTTP)) {
            setDefaultErrorPage(config, lb.getId(), lb.getAccountId());
        }
    }

    @Override
    protected void afterLoadBalancerDelete(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws AdapterException {
        if (lb.getProtocol().equals(CoreProtocolType.HTTP)) {
            deleteErrorPage(config, lb.getId(), lb.getAccountId());
        }
    }

    @Override
    public void addVirtualIps(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<VirtualIp> ipv4Vips, Set<VirtualIpv6> ipv6Vips) throws AdapterException {
        try {
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setAccountId(accountId);
            loadBalancer.setId(lbId);

            for (VirtualIp ipv4Vip : ipv4Vips) {
                LoadBalancerJoinVip joinVip = new LoadBalancerJoinVip(null, loadBalancer, ipv4Vip);
                loadBalancer.getLoadBalancerJoinVipSet().add(joinVip);
            }

            for (VirtualIpv6 ipv6Vip : ipv6Vips) {
                LoadBalancerJoinVip6 joinVip6 = new LoadBalancerJoinVip6(null, loadBalancer, ipv6Vip);
                loadBalancer.getLoadBalancerJoinVip6Set().add(joinVip6);
            }

            addVirtualIps(config, loadBalancer);
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer lb, List<Integer> vipIdsToDelete) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            final String virtualServerName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lb.getId(), lb.getAccountId());
            String[][] currentTrafficIpGroups;
            List<String> updatedTrafficIpGroupList = new ArrayList<String>();
            final String rollBackMessage = "Delete virtual ip request canceled.";

            try {
                currentTrafficIpGroups = serviceStubs.getVirtualServerBinding().getListenTrafficIPGroups(new String[]{virtualServerName});
            } catch (Exception e) {
                if (e instanceof ObjectDoesNotExist) {
                    LOG.error("Cannot delete virtual ip from virtual server as the virtual server does not exist.", e);
                }
                LOG.error(rollBackMessage + "Rolling back changes...", e);
                throw new RollbackException(rollBackMessage, e);
            }

            // Convert current traffic groups to array
            List<String> trafficIpGroupNames = new ArrayList<String>();
            for (String[] currentTrafficGroup : currentTrafficIpGroups) {
                trafficIpGroupNames.addAll(Arrays.asList(currentTrafficGroup));
            }

            // Get traffic ip group to delete
            List<String> trafficIpGroupNamesToDelete = new ArrayList<String>();
            for (Integer vipIdToDelete : vipIdsToDelete) {
                trafficIpGroupNamesToDelete.add(ZxtmNameHelper.generateTrafficIpGroupName(lb, vipIdToDelete));
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
                throw new RollbackException(rollBackMessage, e);
            }

            if (!trafficIpGroupNamesToDelete.isEmpty()) {
                try {
                    deleteTrafficIpGroups(serviceStubs, trafficIpGroupNamesToDelete);
                } catch (RemoteException re) {
                    LOG.error(rollBackMessage + "Rolling back changes...", re);
                    serviceStubs.getVirtualServerBinding().setListenTrafficIPGroups(new String[]{virtualServerName}, new String[][]{Arrays.copyOf(trafficIpGroupNamesToDelete.toArray(), trafficIpGroupNamesToDelete.size(), String[].class)});
                    serviceStubs.getTrafficIpGroupBinding().setEnabled(trafficIpGroupNames.toArray(new String[trafficIpGroupNames.size()]), generateBooleanArray(trafficIpGroupNames.size(), true));
                    throw new RollbackException(rollBackMessage, re);
                }
            }
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public void updateAccessList(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Collection<RaxAccessList> accessListItems) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            final String protectionClassName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);

            LOG.debug(String.format("Updating access list for protection class '%s'...", protectionClassName));

            if (addProtectionClass(config, protectionClassName)) {
                zeroOutConnectionThrottleConfig(config, lbId, accountId);
            }
            LOG.info("Removing the old access list...");
            //remove the current access list...
            deleteAccessList(config, lbId, accountId);

            LOG.debug("adding the new access list...");
            //add the new access list...
            serviceStubs.getProtectionBinding().setAllowedAddresses(new String[]{protectionClassName}, buildAccessListItems(accessListItems, RaxAccessListType.ALLOW));
            serviceStubs.getProtectionBinding().setBannedAddresses(new String[]{protectionClassName}, buildAccessListItems(accessListItems, RaxAccessListType.DENY));

            LOG.info(String.format("Successfully updated access list for protection class '%s'...", protectionClassName));
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }

    }

    @Override
    public void deleteAccessList(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException {
        String poolName = "";
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            poolName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);

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
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public void updateConnectionLogging(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, boolean isConnectionLogging, String protocol) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            final String virtualServerName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lbId, accountId);
            final String rollBackMessage = "Update connection logging request canceled.";
            final String nonHttpLogFormat = "%v %t %h %A:%p %n %B %b %T";
            final String httpLogFormat = "%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\"";

            if (isConnectionLogging) {
                LOG.debug(String.format("ENABLING logging for virtual server '%s'...", virtualServerName));
            } else {
                LOG.debug(String.format("DISABLING logging for virtual server '%s'...", virtualServerName));
            }

            try {
                if (protocol != CoreProtocolType.HTTP) {
                    serviceStubs.getVirtualServerBinding().setLogFormat(new String[]{virtualServerName}, new String[]{nonHttpLogFormat});
                } else if (protocol == CoreProtocolType.HTTP) {
                    serviceStubs.getVirtualServerBinding().setLogFormat(new String[]{virtualServerName}, new String[]{httpLogFormat});
                }
                serviceStubs.getVirtualServerBinding().setLogFilename(new String[]{virtualServerName}, new String[]{config.getLogFileLocation()});
                serviceStubs.getVirtualServerBinding().setLogEnabled(new String[]{virtualServerName}, new boolean[]{isConnectionLogging});
            } catch (Exception e) {
                if (e instanceof ObjectDoesNotExist) {
                    LOG.error(String.format("Virtual server '%s' does not exist. Cannot update connection logging.", virtualServerName));
                }
                throw new RollbackException(rollBackMessage, e);
            }

            LOG.info(String.format("Successfully updated connection logging for virtual server '%s'...", virtualServerName));
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public void uploadDefaultErrorPage(LoadBalancerEndpointConfiguration config, String content) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            LOG.debug("Attempting to upload the default error file...");
            ConfExtraBindingStub extraService = serviceStubs.getZxtmConfExtraService();
            if (extraService != null) {
                extraService.uploadFile(RaxConstants.DEFAULT_ERROR_PAGE, content.getBytes());
                LOG.info("Successfully uploaded the default error file...");
            }
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public void setDefaultErrorPage(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            final String virtualServerName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(loadBalancerId, accountId);
            LOG.debug(String.format("Attempting to set the default error file for: %s_%s", accountId, loadBalancerId));
            //TODO: uncomment when zeus performance issues are resolved... (VERSION 1) TK-12805
            serviceStubs.getVirtualServerBinding().setErrorFile(new String[]{virtualServerName}, new String[]{RaxConstants.DEFAULT_ERROR_PAGE});
            LOG.info(String.format("Successfully set the default error file for: %s_%s", accountId, loadBalancerId));
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }


    @Override
    public void setErrorPage(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, String content) throws AdapterException {
        try {
            String[] vsNames = new String[1];
            String[] errorFiles = new String[1];

            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            ConfExtraBindingStub extraService = serviceStubs.getZxtmConfExtraService();
            VirtualServerBindingStub virtualServerService = serviceStubs.getVirtualServerBinding();

            try {
                String errorFileName = getErrorFileName(loadBalancerId, accountId);

                LOG.debug("Attempting to upload the error file...");
                extraService.uploadFile(errorFileName, content.getBytes());
                LOG.info(String.format("Successfully uploaded the error file for: %s_%s...", accountId, loadBalancerId));

                vsNames[0] = String.format("%d_%d", accountId, loadBalancerId);
                errorFiles[0] = errorFileName;

                LOG.debug("Attempting to set the error file...");
                virtualServerService.setErrorFile(vsNames, errorFiles);
                LOG.info(String.format("Successfully set the error file for: %s_%s...", accountId, loadBalancerId));
            } catch (AxisFault af) {
                if (af instanceof InvalidInput) {
                    //Couldn't find a custom 'default' error file...
                    errorFiles[1] = "Default";
                    virtualServerService.setErrorFile(vsNames, errorFiles);

                }
            }
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    private String getErrorFileName(Integer loadbalancerId, Integer accountId) {
        String msg = String.format("%d_%d_error.html", accountId, loadbalancerId);
        return msg;
    }

    @Override
    public void deleteErrorPage(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws AdapterException {
        String fileToDelete = "";
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            fileToDelete = getErrorFileName(loadBalancerId, accountId);
            LOG.debug(String.format("Attempting to delete a custom error file for: %s%s", accountId, loadBalancerId));
            serviceStubs.getZxtmConfExtraService().deleteFile(new String[]{fileToDelete});
            LOG.info(String.format("Successfully deleted a custom error file for: %s%s", accountId, loadBalancerId));
        } catch (ObjectDoesNotExist e) {
            LOG.warn(String.format("Cannot delete custom error page as, %s, it does not exist. Ignoring...", fileToDelete));
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

                if (healthMonitor instanceof RaxHealthMonitor) {
                    RaxHealthMonitor raxHealthMonitor = (RaxHealthMonitor) healthMonitor;
                    if (raxHealthMonitor.getBodyRegex() != null || !raxHealthMonitor.getBodyRegex().isEmpty())
                        serviceStubs.getMonitorBinding().setBodyRegex(new String[]{monitorName}, new String[]{raxHealthMonitor.getBodyRegex()});
                    if (raxHealthMonitor.getStatusRegex() != null || !raxHealthMonitor.getStatusRegex().isEmpty())
                        serviceStubs.getMonitorBinding().setStatusRegex(new String[]{monitorName}, new String[]{raxHealthMonitor.getStatusRegex()});
                }

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

    private String[][] buildAccessListItems(Collection<RaxAccessList> accessListItems, RaxAccessListType type) throws BadRequestException {
        String[][] list;

        if (type == RaxAccessListType.ALLOW) {
            List<RaxAccessList> accessList = getFilteredList(accessListItems, RaxAccessListType.ALLOW);
            list = new String[1][accessList.size()];
            for (int i = 0; i < accessList.size(); i++) {
                list[0][i] = accessList.get(i).getIpAddress();
            }
        } else if (type == RaxAccessListType.DENY) {
            List<RaxAccessList> accessList = getFilteredList(accessListItems, RaxAccessListType.DENY);
            list = new String[1][accessList.size()];
            for (int i = 0; i < accessList.size(); i++) {
                list[0][i] = accessList.get(i).getIpAddress();
            }
        } else {
            throw new BadRequestException(String.format("Unsupported rule type '%s' found when building item list", type));
        }

        return list;
    }

    private List<RaxAccessList> getFilteredList(Collection<RaxAccessList> accessListItems, RaxAccessListType type) {
        List<RaxAccessList> filteredItems = new ArrayList<RaxAccessList>();

        for (RaxAccessList item : accessListItems) {
            if (item.getType() == type) {
                filteredItems.add(item);
            }
        }

        return filteredItems;
    }

    private boolean[] generateBooleanArray(int size, boolean value) {
        boolean[] array = new boolean[size];

        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }

        return array;
    }
}
