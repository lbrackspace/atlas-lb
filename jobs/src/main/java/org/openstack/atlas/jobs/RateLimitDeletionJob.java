package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.RateLimit;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.List;

@Component
public class RateLimitDeletionJob extends AbstractJob {
    private final Log LOG = LogFactory.getLog(RateLimitDeletionJob.class);

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter;
    @Autowired
    private HostRepository hostRepository;

    @Override
    public Log getLogger() {
        return LOG;
    }

    @Override
    public JobName getJobName() {
        return JobName.RATE_LIMIT_DELETION_JOB;
    }

    @Override
    public void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    }

    @Override
    public void run() throws Exception {
        List<RateLimit> rls = loadBalancerRepository.getRateLimitByExpiration();
        LOG.info(String.format("There are '%s' expired rate limits...", rls.size()));

        if (!rls.isEmpty()) {
            for (RateLimit rl : rls) {
                LoadBalancerEndpointConfiguration config = null;

                try {
                    config = getConfig(rl.getLoadbalancer().getHost());
                } catch (DecryptException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                LOG.info(String.format("Attempting to remove rate limit with id..'%s' from the database... ", rl.getId()));
                loadBalancerRepository.removeRateLimitByExpiration(rl.getId());
                LOG.info("Removed the rate limit from the database...");

                try {
                    LOG.info(String.format("Attempting to remove rate limit with id..'%s' from zeus... ", rl.getId()));
                    reverseProxyLoadBalancerAdapter.deleteRateLimit(config, rl.getLoadbalancer());
                    LOG.info("Removed the rate limit from zeus...");
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (InsufficientRequestException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void cleanup() {
    }

    private LoadBalancerEndpointConfiguration getConfig(Host hostIn) throws DecryptException, MalformedURLException {
        Host hostEnd = hostRepository.getEndPointHost(hostIn.getCluster().getId());
        Cluster cluster = hostEnd.getCluster();
        return new LoadBalancerEndpointConfiguration(hostEnd, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), hostEnd, hostRepository.getFailoverHostNames(cluster.getId()));
    }

}
