package org.openstack.atlas.usage.jobs;

import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.openstack.atlas.usage.BatchAction;
import org.openstack.atlas.usage.ExecutionUtilities;
import org.openstack.atlas.usage.helpers.HostConfigHelper;
import org.openstack.atlas.usage.helpers.ZxtmNameHelper;
import org.openstack.atlas.usage.logic.UsagesForPollingDatabase;
import org.openstack.atlas.util.crypto.exception.DecryptException;

import java.net.ConnectException;
import java.rmi.RemoteException;
import java.util.*;

import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.ACTIVE;

public class LoadBalancerUsagePollerThread extends Thread {
    private final Log LOG = LogFactory.getLog(LoadBalancerUsagePollerThread.class);
    private final int BATCH_SIZE = 100;

    private Host host;
    private LoadBalancerRepository loadBalancerRepository;
    private ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter;
    private HostRepository hostRepository;
    private LoadBalancerUsageRepository usageRepository;

    public LoadBalancerUsagePollerThread(LoadBalancerRepository loadBalancerRepository, String threadName, Host host, ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter, HostRepository hostRepository, LoadBalancerUsageRepository usageRepository) {
        super(threadName);
        this.loadBalancerRepository = loadBalancerRepository;
        this.host = host;
        this.reverseProxyLoadBalancerAdapter = reverseProxyLoadBalancerAdapter;
        this.usageRepository = usageRepository;
        this.hostRepository = hostRepository;
    }

    @Override
    public void run() {
        try {
            final LoadBalancerEndpointConfiguration config = HostConfigHelper.getConfig(host, hostRepository);
            final Set<String> loadBalancerNamesForHost = getValidLoadBalancerNamesForHost(host, config);
            final Set<String> sslLoadBalancerNamesForHost = getValidSslLoadBalancerNamesForHost(host, config);

            BatchAction<String> batchAction = new BatchAction<String>() {
                public void execute(List<String> loadBalancerNames) throws Exception {
                    List<String> loadbalancerNamesWithSsl = getSslLoadBalancerNamesForBatch(loadBalancerNames, sslLoadBalancerNamesForHost);
                    
                    try {
                        LOG.info(String.format("Retrieving bytes in from '%s' (%s)...", host.getName(), host.getEndpoint()));
                        Map<String, Long> bytesInMap = reverseProxyLoadBalancerAdapter.getLoadBalancerBytesIn(config, loadBalancerNames);
                        LOG.info(String.format("Retrieving ssl bytes in from '%s' (%s)...", host.getName(), host.getEndpoint()));
                        Map<String, Long> bytesInMapSsl = reverseProxyLoadBalancerAdapter.getLoadBalancerBytesIn(config, loadbalancerNamesWithSsl);

                        LOG.debug("Listing bandwidth bytes in...");
                        for (String loadBalancerName : bytesInMap.keySet()) {
                            LOG.debug(String.format("LB Name: '%s', Bandwidth Bytes In: %d", loadBalancerName, bytesInMap.get(loadBalancerName)));
                        }
                        LOG.debug("Listing ssl bandwidth bytes in...");
                        for (String loadBalancerName : bytesInMapSsl.keySet()) {
                            LOG.debug(String.format("LB Name: '%s', Bandwidth Bytes In: %d", loadBalancerName, bytesInMapSsl.get(loadBalancerName)));
                        }

                        LOG.info(String.format("Retrieving bytes out from '%s' (%s)...", host.getName(), host.getEndpoint()));
                        Map<String, Long> bytesOutMap = reverseProxyLoadBalancerAdapter.getLoadBalancerBytesOut(config, loadBalancerNames);
                        LOG.info(String.format("Retrieving ssl bytes out from '%s' (%s)...", host.getName(), host.getEndpoint()));
                        Map<String, Long> bytesOutMapSsl = reverseProxyLoadBalancerAdapter.getLoadBalancerBytesOut(config, loadbalancerNamesWithSsl);

                        LOG.debug("Listing bandwidth bytes out...");
                        for (String loadBalancerName : bytesOutMap.keySet()) {
                            LOG.debug(String.format("LB Name: '%s', Bandwidth Bytes Out: %d", loadBalancerName, bytesOutMap.get(loadBalancerName)));
                        }
                        LOG.debug("Listing ssl bandwidth bytes out...");
                        for (String loadBalancerName : bytesOutMapSsl.keySet()) {
                            LOG.debug(String.format("LB Name: '%s', Bandwidth Bytes Out: %d", loadBalancerName, bytesOutMapSsl.get(loadBalancerName)));
                        }

                        LOG.info(String.format("Retrieving current connections from '%s' (%s)...", host.getName(), host.getEndpoint()));
                        Map<String, Integer> currentConnectionsMap = reverseProxyLoadBalancerAdapter.getLoadBalancerCurrentConnections(config, loadBalancerNames);
                        LOG.info(String.format("Retrieving ssl current connections from '%s' (%s)...", host.getName(), host.getEndpoint()));
                        Map<String, Integer> currentConnectionsMapSsl = reverseProxyLoadBalancerAdapter.getLoadBalancerCurrentConnections(config, loadbalancerNamesWithSsl);

                        LOG.debug("Listing concurrent connections...");
                        for (String loadBalancerName : currentConnectionsMap.keySet()) {
                            LOG.debug(String.format("LB Name: '%s', Concurrent Connections: %d", loadBalancerName, currentConnectionsMap.get(loadBalancerName)));
                        }
                        LOG.debug("Listing ssl concurrent connections...");
                        for (String loadBalancerName : currentConnectionsMapSsl.keySet()) {
                            LOG.debug(String.format("LB Name: '%s', Concurrent Connections: %d", loadBalancerName, currentConnectionsMapSsl.get(loadBalancerName)));
                        }

                        updateUsageRecords(loadBalancerNames, bytesInMap, bytesOutMap, currentConnectionsMap, bytesInMapSsl, bytesOutMapSsl, currentConnectionsMapSsl);
                    } catch (RemoteException re) {
                        /* This exception is usually thrown due to the batch containing a
                         * load balancer name that just got suspended or deleted. Zeus,
                         * however, does not tell us which one (jira SITESLB-753) */
                        LOG.error("Remote exception occurred. Load balancer name(s) removed from batch. Skipping batch...", re);
                        for (String ignoredLoadBalancerName : loadBalancerNames) {
                            LOG.error(String.format("LB name in bad batch: '%s'", ignoredLoadBalancerName));
                        }
                    }
                }
            };

            ExecutionUtilities.executeInBatches(loadBalancerNamesForHost, BATCH_SIZE, batchAction);
        } catch (DecryptException de) {
            LOG.error(String.format("Error decrypting configuration for '%s' (%s)", host.getName(), host.getEndpoint()), de);
        } catch (AxisFault af) {
            if (af.getCause() instanceof ConnectException) {
                LOG.error(String.format("Error connecting to '%s' (%s). Skipping...", host.getName(), host.getEndpoint()));
            } else {
                LOG.error("Axis Fault Exception caught", af);
                af.printStackTrace();
            }
        } catch (Exception e) {
            LOG.error("Exception caught", e);
            e.printStackTrace();
        }
    }

    private List<String> getSslLoadBalancerNamesForBatch(List<String> loadBalancerNames, Set<String> sslLoadBalancerNamesForHost) {
        List<String> loadbalancerNamesWithSsl = new ArrayList<String>();

        for (String loadBalancerName : loadBalancerNames) {
            final String sslLoadBalancerName = ZxtmNameBuilder.genSslVSName(loadBalancerName);
            if(sslLoadBalancerNamesForHost.contains(sslLoadBalancerName)) loadbalancerNamesWithSsl.add(sslLoadBalancerName);
        }
        return loadbalancerNamesWithSsl;
    }

    public Set<String> getValidLoadBalancerNamesForHost(Host host, LoadBalancerEndpointConfiguration config) throws RemoteException, InsufficientRequestException {
        Set<String> allLoadBalancerNames = new HashSet<String>(reverseProxyLoadBalancerAdapter.getStatsSystemLoadBalancerNames(config));
        Set<LoadBalancer> loadBalancersForHost = new HashSet<LoadBalancer>(hostRepository.getLoadBalancersWithStatus(host.getId(), ACTIVE));
        Set<String> loadBalancerNamesForHost = ZxtmNameBuilder.generateNamesWithAccountIdAndLoadBalancerId(loadBalancersForHost);
        loadBalancerNamesForHost.retainAll(allLoadBalancerNames); // Get the intersection
        return loadBalancerNamesForHost;
    }

    public Set<String> getValidSslLoadBalancerNamesForHost(Host host, LoadBalancerEndpointConfiguration config) throws RemoteException, InsufficientRequestException {
        Set<String> allLoadBalancerNames = new HashSet<String>(reverseProxyLoadBalancerAdapter.getStatsSystemLoadBalancerNames(config));
        Set<LoadBalancer> sslLoadBalancersForHost = new HashSet<LoadBalancer>(hostRepository.getSslLoadBalancersWithStatus(host.getId(), ACTIVE));
        Set<String> sslLoadBalancerNamesForHost = ZxtmNameBuilder.generateSslNamesWithAccountIdAndLoadBalancerId(sslLoadBalancersForHost);
        sslLoadBalancerNamesForHost.retainAll(allLoadBalancerNames); // Get the intersection
        return sslLoadBalancerNamesForHost;
    }

    private void updateUsageRecords(List<String> loadBalancerNames, Map<String, Long> bytesInMap, Map<String, Long> bytesOutMap, Map<String, Integer> currentConnectionsMap, Map<String, Long> bytesInMapSsl, Map<String, Long> bytesOutMapSsl, Map<String, Integer> currentConnectionsMapSsl) {
        Calendar pollTime = Calendar.getInstance();
        Map<Integer, Integer> lbIdAccountIdMap = ZxtmNameHelper.stripLbIdAndAccountIdFromZxtmName(loadBalancerNames);
        List<LoadBalancerUsage> usages = usageRepository.getMostRecentUsageForLoadBalancers(lbIdAccountIdMap.keySet());
        Map<Integer, LoadBalancerUsage> usagesAsMap = convertUsagesToMap(usages);
        UsagesForPollingDatabase usagesForDatabase = new UsagesForPollingDatabase(loadBalancerRepository, loadBalancerNames, bytesInMap, bytesOutMap, currentConnectionsMap, bytesInMapSsl, bytesOutMapSsl, currentConnectionsMapSsl, pollTime, usagesAsMap).invoke();

        if (!usagesForDatabase.getRecordsToInsert().isEmpty())
            usageRepository.batchCreate(usagesForDatabase.getRecordsToInsert());
        if (!usagesForDatabase.getRecordsToUpdate().isEmpty())
            usageRepository.batchUpdate(usagesForDatabase.getRecordsToUpdate());
    }

    private Map<Integer, LoadBalancerUsage> convertUsagesToMap(List<LoadBalancerUsage> usages) {
        Map<Integer, LoadBalancerUsage> usagesAsMap = new HashMap<Integer, LoadBalancerUsage>();
        for (LoadBalancerUsage usage : usages) {
            usagesAsMap.put(usage.getLoadbalancerId(), usage);
        }
        return usagesAsMap;
    }
}
