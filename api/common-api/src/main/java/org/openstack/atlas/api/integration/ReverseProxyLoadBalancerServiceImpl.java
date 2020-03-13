package org.openstack.atlas.api.integration;

import com.zxtm.service.client.ObjectDoesNotExist;
import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.api.exceptions.StingrayTimeoutException;
import org.openstack.atlas.api.integration.threads.ThreadExecutorService;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.api.helpers.CacheKeyGen;
import org.openstack.atlas.api.helpers.DateHelpers;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.cache.AtlasCache;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.services.HealthMonitorService;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.usagerefactor.SnmpStats;
import org.openstack.atlas.usagerefactor.collection.StatsCollection;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.openstack.atlas.util.debug.Debug;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Calendar.getInstance;

public class ReverseProxyLoadBalancerServiceImpl implements ReverseProxyLoadBalancerService {

    final Log LOG = LogFactory.getLog(ReverseProxyLoadBalancerServiceImpl.class);
    private ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter;
    private LoadBalancerService loadBalancerService;
    private StatsCollection statsCollection;
    private HostService hostService;
    private NotificationService notificationService;
    private HealthMonitorService healthMonitorService;
    private Configuration configuration;
    private AtlasCache atlasCache;

    public void setLoadBalancerService(LoadBalancerService loadBalancerService) {
        this.loadBalancerService = loadBalancerService;
    }

    public void setStatsCollection(StatsCollection statsCollection) {
        this.statsCollection = statsCollection;
    }

    public void setHostService(HostService hostService) {
        this.hostService = hostService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setHealthMonitorService(HealthMonitorService healthMonitorService) {
        this.healthMonitorService = healthMonitorService;
    }

    @Override
    public void createLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.createLoadBalancer(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.deleteLoadBalancer(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void syncLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateLoadBalancer(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void setRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.setRateLimit(config, loadBalancer, rateLimit);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteRateLimit(LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.deleteRateLimit(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateRateLimit(config, loadBalancer, rateLimit);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateAlgorithm(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.setLoadBalancingAlgorithm(config, lb.getId(), lb.getAccountId(), lb.getAlgorithm());
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void changeHostForLoadBalancer(LoadBalancer lb, Host newHost) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.changeHostForLoadBalancer(config, lb, newHost);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updatePort(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updatePort(config, lb.getId(), lb.getAccountId(),
                    lb.getPort());
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateTimeout(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateTimeout(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateProtocol(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateProtocol(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateHalfClosed(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateHalfClosed(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    public void updateHttpsRedirect(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateHttpsRedirect(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateConnectionLogging(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateConnectionLogging(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateContentCaching(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateContentCaching(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void addVirtualIps(Integer lbId, Integer accountId, LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.addVirtualIps(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void setNodes(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.setNodes(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeNode(Integer lbId, Integer accountId, Node node) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.removeNode(config, lbId, accountId, node.getIpAddress(), node.getPort());
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeNodes(Integer lbId, Integer accountId, Collection<Node> nodes) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.removeNodes(config, lbId, accountId, nodes);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void setNodeWeights(Integer lbId, Integer accountId, Set<Node> nodes) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.setNodeWeights(config, lbId, accountId, nodes);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }

    }

    @Override
    public void updateAccessList(LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateAccessList(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;

        }
    }

    @Override
    public void deleteAccessList(Integer lbId, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.deleteAccessList(config, lbId, accountId);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateConnectionThrottle(LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateConnectionThrottle(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.deleteConnectionThrottle(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateSessionPersistence(Integer lbId, Integer accountId, SessionPersistence persistenceMode) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.setSessionPersistence(config, lbId, accountId, persistenceMode);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeSessionPersistence(Integer lbId, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.removeSessionPersistence(config, lbId, accountId);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateHealthMonitor(LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateHealthMonitor(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeHealthMonitor(LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.removeHealthMonitor(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void createHostBackup(Host host,
            String backupName) throws RemoteException, MalformedURLException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        try {
            reverseProxyLoadBalancerAdapter.createHostBackup(config, backupName);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void restoreHostBackup(Host host, String backupName) throws RemoteException, ObjectDoesNotExist, MalformedURLException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        try {
            reverseProxyLoadBalancerAdapter.restoreHostBackup(config, backupName);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteHostBackup(Host host, String backupName) throws RemoteException, ObjectDoesNotExist, MalformedURLException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        try {
            reverseProxyLoadBalancerAdapter.deleteHostBackup(config, backupName);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void suspendLoadBalancer(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.suspendLoadBalancer(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeSuspension(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.removeSuspension(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public int getTotalCurrentConnectionsForHost(Host host) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        int conn;
        try {
            conn = reverseProxyLoadBalancerAdapter.getTotalCurrentConnectionsForHost(config);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
        return conn;
    }

    // TODO: unused
    @Override
    public Integer getLoadBalancerCurrentConnections(LoadBalancer lb, boolean isSsl) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigHost(lb.getHost());
        int conn;
        try {
            conn = reverseProxyLoadBalancerAdapter.getLoadBalancerCurrentConnections(config, lb.getAccountId(), lb.getId(), isSsl);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
        return conn;
    }

    // TODO: unused
    @Override
    public Long getLoadBalancerBytesIn(LoadBalancer lb, boolean isSsl) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigHost(lb.getHost());
        long bytesIn;
        try {
            bytesIn = reverseProxyLoadBalancerAdapter.getLoadBalancerBytesIn(config, lb.getAccountId(), lb.getId(), isSsl);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
        return bytesIn;
    }

    // TODO: unused
    @Override
    public Long getLoadBalancerBytesOut(LoadBalancer lb, boolean isSsl) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigHost(lb.getHost());
        long bytesOut;
        try {
            bytesOut = reverseProxyLoadBalancerAdapter.getLoadBalancerBytesOut(config, lb.getAccountId(), lb.getId(), isSsl);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
        return bytesOut;
    }

    @Override
    public Stats getLoadBalancerStats(final LoadBalancer loadBalancer) throws StingrayTimeoutException, EntityNotFoundException, MalformedURLException, DecryptException, InsufficientRequestException, RemoteException {
        Integer loadbalancerId = loadBalancer.getId();
        Integer accountId = loadBalancer.getAccountId();
        String key = CacheKeyGen.generateKeyName(accountId, loadbalancerId);
        Stats lbStats;

        long timer = getInstance().getTimeInMillis();
        lbStats = (Stats) atlasCache.get(key);

        if (lbStats == null) {
            Host defaultHost = loadBalancerService.get(loadbalancerId).getHost();
            List<Host> failoverHosts = hostService.getFailoverHosts(defaultHost.getCluster().getId());
            final long TIMEOUT_IN_MILLIS = Long.parseLong(configuration.getString(PublicApiServiceConfigurationKeys.stats_timeout_in_millis));
            final List<Host> hostToGatherStatsFrom = new ArrayList<Host>();
            hostToGatherStatsFrom.add(defaultHost);
            hostToGatherStatsFrom.addAll(failoverHosts);
            List<SnmpStats> snmpStatsList;

            try {
                snmpStatsList = ThreadExecutorService.call(new Callable<List<SnmpStats>>() {

                    public List<SnmpStats> call() throws Exception {
                        return statsCollection.getStatsForHosts(loadBalancer, hostToGatherStatsFrom);
                    }
                }, TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS);
                String message = String.format("Date: %s, AccountId: %d, GetLoadBalancerStats, Missed from cache, retrieved from api... Time taken: %s ms. Timeout set to: %d ms.", DateHelpers.getDate(Calendar.getInstance().getTime()), accountId, DateHelpers.getTotalTimeTaken(timer), TIMEOUT_IN_MILLIS);
                LOG.info(message);
            } catch (TimeoutException e) {
                String message = "Stats request is taking too long to complete. Timing out...";
                LOG.error(message);
                throw new StingrayTimeoutException(message);
            } catch (InterruptedException e) {
                // TODO: How to handle this?
                String message = "Stats request is taking too long to complete. Timing out...";
                LOG.error(message);
                throw new StingrayTimeoutException(message);
            } catch (ExecutionException e) {
                // TODO: How to handle this?
                String message = "Stats request is taking too long to complete. Timing out...";
                LOG.error(message);
                throw new StingrayTimeoutException(message);
            }

            SnmpStats aggregate = new SnmpStats();
            for (SnmpStats stats : snmpStatsList) {
                aggregate = SnmpStats.add(aggregate, stats);
            }

            lbStats = convertSnmpStatsToStats(aggregate);
            atlasCache.set(key, lbStats);
            return lbStats;
        } else {
            LOG.info("Date:" + DateHelpers.getDate(Calendar.getInstance().getTime()) + " AccountID: " + accountId + " GetLoadBalancerStats, retrieved from cache... Time taken: " + DateHelpers.getTotalTimeTaken(timer) + " ms");
            return lbStats;
        }
    }

    private Stats convertSnmpStatsToStats(SnmpStats snmpStats) {
        int[] connectionTimedOut = new int[]{snmpStats.getConnectTimedOut()};
        int[] connectionError = new int[]{snmpStats.getConnectionErrors()};
        int[] connectionFailure = new int[]{snmpStats.getConnectionFailures()};
        int[] dataTimedOut = new int[]{snmpStats.getDataTimedOut()};
        int[] keepaliveTimedOut = new int[]{snmpStats.getKeepaliveTimedOut()};
        int[] maxConnections = new int[]{snmpStats.getMaxConnections()};
        int[] currentConnections = new int[]{snmpStats.getConcurrentConnections()};

        int[] connectionTimedOutSsl = new int[]{snmpStats.getConnectTimedOutSsl()};
        int[] connectionErrorSsl = new int[]{snmpStats.getConnectionErrorsSsl()};
        int[] connectionFailureSsl = new int[]{snmpStats.getConnectionFailuresSsl()};
        int[] dataTimedOutSsl = new int[]{snmpStats.getDataTimedOutSsl()};
        int[] keepaliveTimedOutSsl = new int[]{snmpStats.getKeepaliveTimedOutSsl()};
        int[] maxConnectionsSsl = new int[]{snmpStats.getMaxConnectionsSsl()};
        int[] currentConnectionsSsl = new int[]{snmpStats.getConcurrentConnectionsSsl()};

        Stats stats = new Stats();
        stats.setConnectTimeOut((long) connectionTimedOut[0]);
        stats.setConnectError((long) connectionError[0]);
        stats.setConnectFailure((long) connectionFailure[0]);
        stats.setDataTimedOut((long) dataTimedOut[0]);
        stats.setKeepAliveTimedOut((long) keepaliveTimedOut[0]);
        stats.setMaxConn((long) maxConnections[0]);
        stats.setCurrentConn((long) currentConnections[0]);

        stats.setConnectTimeOutSsl((long) connectionTimedOutSsl[0]);
        stats.setConnectErrorSsl((long) connectionErrorSsl[0]);
        stats.setConnectFailureSsl((long) connectionFailureSsl[0]);
        stats.setDataTimedOutSsl((long) dataTimedOutSsl[0]);
        stats.setKeepAliveTimedOutSsl((long) keepaliveTimedOutSsl[0]);
        stats.setMaxConnSsl((long) maxConnectionsSsl[0]);
        stats.setCurrentConnSsl((long) currentConnectionsSsl[0]);

        return stats;
    }

    @Override
    public void deleteVirtualIp(LoadBalancer lb, Integer id) {
        try {
            LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
            try {
                reverseProxyLoadBalancerAdapter.deleteVirtualIp(config, lb, id);
            } catch (AxisFault af) {
                checkAndSetIfSoapEndPointBad(config, af);
                throw af;
            }
        } catch (Exception e) {
            LOG.error("Error during removal of the virtualIp:", e);
        }
    }

    @Override
    public void setErrorFile(LoadBalancer loadBalancer, String content) throws DecryptException, MalformedURLException, RemoteException, EntityNotFoundException, InsufficientRequestException {
        LoadBalancer lb = loadBalancerService.get(loadBalancer.getId(), loadBalancer.getAccountId());
        Host host = lb.getHost();
        LoadBalancerEndpointConfiguration config = getConfig(host);
        try {
            reverseProxyLoadBalancerAdapter.setErrorFile(config, loadBalancer, content);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        } catch (RollBackException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void deleteVirtualIps(LoadBalancer lb, List<Integer> ids) {
        try {
            LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
            try {
                reverseProxyLoadBalancerAdapter.deleteVirtualIps(config, lb, ids);
            } catch (AxisFault af) {
                checkAndSetIfSoapEndPointBad(config, af);
                throw af;
            }
        } catch (Exception e) {
            LOG.error("Error during removal of the virtualIp:", e);
        }
    }

    @Override
    public boolean isEndPointWorking(Host host) throws Exception {
        boolean out;
        LoadBalancerEndpointConfiguration hostConfig = getConfigHost(host);
        out = reverseProxyLoadBalancerAdapter.isEndPointWorking(hostConfig);
        return out;
    }

    @Override
    public Hostssubnet getSubnetMappings(Host host) throws Exception {
        LoadBalancerEndpointConfiguration hostConfig = getConfig(host);
        String hostName = host.getTrafficManagerName();
        Hostssubnet hostssubnet;
        try {
            hostssubnet = reverseProxyLoadBalancerAdapter.getSubnetMappings(getConfig(host), hostName);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(hostConfig, af);
            throw af;
        }
        return hostssubnet;
    }

    @Override
    public void setSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception {
        LoadBalancerEndpointConfiguration hostConfig = getConfig(host);
        String hostName = host.getTrafficManagerName();
        try {
            reverseProxyLoadBalancerAdapter.setSubnetMappings(hostConfig, hostssubnet);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(hostConfig, af);
            throw af;
        }
    }

    @Override
    public void deleteSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception {
        LoadBalancerEndpointConfiguration hostConfig = getConfig(host);
        String hostName = host.getTrafficManagerName();
        try {
            reverseProxyLoadBalancerAdapter.deleteSubnetMappings(hostConfig, hostssubnet);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(hostConfig, af);
            throw af;
        }
    }

    @Override
    public void removeAndSetDefaultErrorFile(LoadBalancer loadBalancer) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.removeAndSetDefaultErrorFile(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        } catch (RollBackException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void setDefaultErrorFile(LoadBalancer loadBalancer) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException {
        LoadBalancer lb = loadBalancerService.get(loadBalancer.getId(), loadBalancer.getAccountId());
        LoadBalancerEndpointConfiguration config = getConfig(lb.getHost());
        try {
            reverseProxyLoadBalancerAdapter.setDefaultErrorFile(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        } catch (RollBackException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void uploadDefaultErrorFile(Integer clusterId, String content) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException {
        LoadBalancerEndpointConfiguration config = getConfigbyClusterId(clusterId);
        try {
            reverseProxyLoadBalancerAdapter.uploadDefaultErrorFile(config, content);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        } catch (RollBackException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void deleteErrorFile(LoadBalancer loadBalancer) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException {
        LoadBalancer lb = loadBalancerService.get(loadBalancer.getId(), loadBalancer.getAccountId());
        LoadBalancerEndpointConfiguration config = getConfig(lb.getHost());
        try {
            reverseProxyLoadBalancerAdapter.deleteErrorFile(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        } catch (RollBackException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public LoadBalancerEndpointConfiguration getConfigFirstAvaliableSoap() throws EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = null;
        Host soapHost = hostService.getFirstAvailableSoapEndPointHost();
        Cluster cluster = soapHost.getCluster();
        Integer clusterId = cluster.getId();
        List<String> failoverHostNames = hostService.getFailoverHostNames(clusterId);
        List<Host> failoverHosts = hostService.getFailoverHosts(clusterId);
        String userName = cluster.getUsername();
        String cipherText = cluster.getPassword();
        String passwd = CryptoUtil.decrypt(cipherText);
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        config = new LoadBalancerEndpointConfiguration(soapHost, userName, passwd, soapHost, failoverHostNames, logFileLocation, failoverHosts);
        return config;
    }

    LoadBalancerEndpointConfiguration getConfigbyClusterId(Integer clusterId) throws EntityNotFoundException, DecryptException {
        Cluster cluster = hostService.getClusterById(clusterId);
        Host soapEndpointHost = hostService.getEndPointHost(cluster.getId());
        List<String> failoverHostNames = hostService.getFailoverHostNames(cluster.getId());
        List<Host> failoverHosts = hostService.getFailoverHosts(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);

        return new LoadBalancerEndpointConfiguration(soapEndpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), soapEndpointHost, failoverHostNames, logFileLocation, failoverHosts);
    }

    // Send request to proper SOAPEndpoint(Calculated by the database) for host's traffic manager
    @Override
    public LoadBalancerEndpointConfiguration getConfig(Host host) throws DecryptException, MalformedURLException {
        Cluster cluster = host.getCluster();
        Host soapEndpointHost = hostService.getEndPointHost(cluster.getId());
        List<String> failoverHostNames = hostService.getFailoverHostNames(cluster.getId());
        List<Host> failoverHosts = hostService.getFailoverHosts(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);

        return new LoadBalancerEndpointConfiguration(soapEndpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHostNames, logFileLocation, failoverHosts);
    }

    // Send SOAP request directly to the hosts traffic manager.
    @Override
    public LoadBalancerEndpointConfiguration getConfigHost(Host host) throws DecryptException, MalformedURLException {
        Cluster cluster = host.getCluster();
        List<String> failoverHostNames = hostService.getFailoverHostNames(cluster.getId());
        List<Host> failoverHosts = hostService.getFailoverHosts(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);

        return new LoadBalancerEndpointConfiguration(host, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHostNames, logFileLocation, failoverHosts);
    }

    private LoadBalancerEndpointConfiguration getConfigbyLoadBalancerId(Integer lbId) throws EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancer loadBalancer = loadBalancerService.get(lbId);
        Host host = loadBalancer.getHost();
        Cluster cluster = host.getCluster();
        Host soapEndpointHost = hostService.getEndPointHost(cluster.getId());
        List<String> failoverHostNames = hostService.getFailoverHostNames(cluster.getId());
        List<Host> failoverHosts = hostService.getFailoverHosts(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        return new LoadBalancerEndpointConfiguration(soapEndpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHostNames, logFileLocation, failoverHosts);
    }

    public void setReverseProxyLoadBalancerAdapter(ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter) {
        this.reverseProxyLoadBalancerAdapter = reverseProxyLoadBalancerAdapter;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private void checkAndSetIfSoapEndPointBad(LoadBalancerEndpointConfiguration config, AxisFault af) throws AxisFault {
        Host configuredHost = config.getEndpointUrlHost();
        //TODO: TMP for ONE/ONF exception handing, need to update debug to grab these messages from throwable..
        if ((!af.getFaultString().contentEquals("Object not found")) || (!af.getFaultString().contentEquals("Object does not exist"))) {
            if (IpHelper.isNetworkConnectionException(af)) {
                LOG.error(String.format("SOAP endpoint %s went bad marking host[%d] as bad. Exception was %s", configuredHost.getEndpoint(), configuredHost.getId(), Debug.getExtendedStackTrace(af)));
                configuredHost.setSoapEndpointActive(Boolean.FALSE);
                hostService.update(configuredHost);
            } else {
                LOG.warn(String.format("SOAP endpoint %s on host[%d] throw an AxisFault but not marking as bad as it was not a network connection error: Exception was %s", configuredHost.getEndpoint(), configuredHost.getId(), Debug.getExtendedStackTrace(af)));
            }
        }
        LOG.warn(String.format("SOAP endpoint Failure: %s for host[%d] Exception messages %s", configuredHost.getEndpoint(), configuredHost.getId(), af.getFaultString()));
    }

    @Override
    public void updateSslTermination(LoadBalancer loadBalancer, ZeusSslTermination sslTermination) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateSslTermination(config, loadBalancer, sslTermination);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeSslTermination(LoadBalancer lb) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.removeSslTermination(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void enableDisableSslTermination(LoadBalancer loadBalancer, boolean isSslTermination) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.enableDisableSslTermination(config, loadBalancer, isSslTermination);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }

    }

    @Override
    public void setNodesPriorities(String poolName, LoadBalancer lb) throws DecryptException, EntityNotFoundException, MalformedURLException, RemoteException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.setNodesPriorities(config, poolName, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }

    }

    @Override
    public void updateCertificateMapping(Integer lbId, Integer accountId, CertificateMapping certMappingToUpdate) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, RollBackException, InsufficientRequestException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.updateCertificateMapping(config, lbId, accountId, certMappingToUpdate);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public String getSslCiphers(Integer accountId, Integer loadbalancerId) throws EntityNotFoundException, RemoteException, MalformedURLException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadbalancerId);
        String ciphers = null;
        try {
            ciphers = reverseProxyLoadBalancerAdapter.getSslCiphersByVhost(config, accountId, loadbalancerId);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
        return ciphers;
    }

    @Override
    public void setSslCiphers(Integer accountId, Integer loadbalancerId, String ciphers) throws EntityNotFoundException, RemoteException, MalformedURLException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadbalancerId);
        try {
            reverseProxyLoadBalancerAdapter.setSslCiphersByVhost(config, accountId, loadbalancerId, ciphers);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public String getSsl3Ciphers() throws RemoteException, EntityNotFoundException, MalformedURLException, DecryptException {
        String globalCiphers = null;
        LoadBalancerEndpointConfiguration config = getConfigFirstAvaliableSoap();
        try {
            globalCiphers = reverseProxyLoadBalancerAdapter.getSsl3Ciphers(config);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
        return globalCiphers;
    }

    @Override
    public void removeCertificateMapping(Integer lbId, Integer accountId, CertificateMapping certMappingToDelete) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, RollBackException, InsufficientRequestException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.deleteCertificateMapping(config, lbId, accountId, certMappingToDelete);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    public void setAtlasCache(AtlasCache atlasCache) {
        this.atlasCache = atlasCache;
    }

    public AtlasCache getAtlasCache() {
        return atlasCache;
    }
}
