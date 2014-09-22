package org.openstack.atlas.usagerefactor.collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;
import org.openstack.atlas.usagerefactor.SnmpStats;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Component
public class StatsCollection {
    private final Log LOG = LogFactory.getLog(StatsCollection.class);
    private ThreadPoolExecutor poolExecutor;

    public StatsCollection() {
    }

    public List<SnmpStats> getStatsForHosts(LoadBalancer lb, List<Host> hosts) throws UsageEventCollectionException {

        if (hosts != null && !hosts.isEmpty()) {
            LOG.debug("Collecting SNMP stats for load balancer: " + lb.getId());

            List<Future<SnmpStats>> futures;
            List<Callable<SnmpStats>> callables = new ArrayList<Callable<SnmpStats>>();

            for (Host h : hosts) {
                callables.add(new SnmpStatsCollector(h, lb));
            }

            ExecutorService threadPool = Executors.newFixedThreadPool(hosts.size());
            try {
                LOG.debug("Executing SNMP stats collection tasks for load balancer: " + lb.getId());
                futures = threadPool.invokeAll(callables);
            } catch (InterruptedException e) {
                LOG.error("Error executing SNMP stats collection: " + e);
                throw new UsageEventCollectionException("Error executing SNMP stats collection: ", e);
            } finally {
                shutdownAndAwaitTermination(threadPool);
            }

            List<SnmpStats> snmpStats = new ArrayList<SnmpStats>();

            for (Future<SnmpStats> f : futures) {
                try {
                    SnmpStats stats = f.get();
                    if (stats != null) {
                        snmpStats.add(stats);
                    } else {
                        LOG.warn("A null SnmpStats was encountered by will not be used for processing.");
                    }
                } catch (InterruptedException e) {
                    LOG.error("Error retrieving SNMP futures: " + e);
                    throw new UsageEventCollectionException("Error retrieving SNMP futures: ", e);
                } catch (ExecutionException e) {
                    LOG.error("Error retrieving SNMP futures: " + e);
                    throw new UsageEventCollectionException("Error retrieving SNMP futures: ", e);
                }
            }

            return snmpStats;
        } else {
            LOG.error("Hosts data invalid, this shouldn't happen... Verify DB for data and notify developer immediately. ");
            throw new UsageEventCollectionException("Hosts data invalid, please contact support.");
        }
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        final int THREAD_POOL_TIMEOUT = 30;

        pool.shutdown(); // Disable new tasks from being submitted

        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(THREAD_POOL_TIMEOUT, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(THREAD_POOL_TIMEOUT, TimeUnit.SECONDS))
                    LOG.error(String.format("Pool '%s' did not terminate!", pool.toString()));
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
