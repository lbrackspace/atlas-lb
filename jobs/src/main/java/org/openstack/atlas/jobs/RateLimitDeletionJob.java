package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.List;

public class RateLimitDeletionJob extends Job {
    private final Log LOG = LogFactory.getLog(RateLimitDeletionJob.class);
    private LoadBalancerRepository loadBalancerRepository;
    private ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter;
    private HostRepository hostRepository;

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    @Required
    public void setReverseProxyLoadBalancerAdapter(ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter) {
        this.reverseProxyLoadBalancerAdapter = reverseProxyLoadBalancerAdapter;
    }

    @Required
    public void setHostRepository(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Rate limit deletion job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(JobName.RATE_LIMIT_DELETION_JOB, JobStateVal.IN_PROGRESS);

        List<RateLimit> rls;
        rls = loadBalancerRepository.getRateLimitByExpiration();
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
                LOG.info(String.format("Attempting to remove rate limit with id..'%s' from zeus... ", rl.getId()));
                try {
                    reverseProxyLoadBalancerAdapter.deleteRateLimit(config, rl.getLoadbalancer());
                    LOG.info("Removed the rate limit from zeus...");

                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (InsufficientRequestException e) {
                    e.printStackTrace();
                }
            }
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        jobStateService.updateJobState(JobName.RATE_LIMIT_DELETION_JOB, JobStateVal.FINISHED);
        LOG.info(String.format("Rate limit deletion job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

    private LoadBalancerEndpointConfiguration getConfig(Host hostIn) throws DecryptException, MalformedURLException {
        Host hostEnd = hostRepository.getEndPointHost(hostIn.getCluster().getId());
        Cluster cluster = hostEnd.getCluster();
        return new LoadBalancerEndpointConfiguration(hostEnd, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), hostEnd, hostRepository.getFailoverHostNames(cluster.getId()));
    }
}
