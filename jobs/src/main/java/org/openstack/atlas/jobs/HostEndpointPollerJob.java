package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerServiceImpl;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.net.MalformedURLException;
import java.util.List;

public class HostEndpointPollerJob extends QuartzJobBean {
    private final Log LOG = LogFactory.getLog(HostEndpointPollerJob.class);
    private ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;
    private ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter;
    private HostRepository hostRepository;


    @Required
    public void setReverseProxyLoadBalancerAdapter(ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter) {
        this.reverseProxyLoadBalancerAdapter = reverseProxyLoadBalancerAdapter;
    }

    @Required
    public void setReverseProxyLoadBalancerService(ReverseProxyLoadBalancerService reverseProxyLoadBalancerService) {
        this.reverseProxyLoadBalancerService = reverseProxyLoadBalancerService;
    }

    @Required
    public void setHostRepository(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    //TODO: refactor to use the async service...
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info("HostEndpointPollerJob job has started...");
        try {
            boolean endpointWorks;
            List<Host> hosts = hostRepository.getAllHosts();
            for (Host host : hosts) {
                endpointWorks = reverseProxyLoadBalancerAdapter.isEndPointWorking(getConfigHost(host));
                if (endpointWorks) {
                    host.setSoapEndpointActive(Boolean.TRUE);
                    LOG.info("Host: " + host.getId() + " is active");
                } else {
                    host.setSoapEndpointActive(Boolean.FALSE);
                    LOG.info("Host: " + host.getId() + " is inactive");
                }
                LOG.info("Host: " + host.getId() + " is being updated in the database.");
                hostRepository.update(host);
                LOG.info("Finished updating host: " + host.getId() + " in the database.");
            }
        } catch (Exception e) {
            LOG.error("There was a problem polling host endpoints. 'HostEndpointPollerJob'");
        }
        LOG.info("HostEndpointPollerJob job has finished...");
    }

    //TODO: refactor to use service/null adapter
    public LoadBalancerEndpointConfiguration getConfigHost(Host host) throws DecryptException, MalformedURLException {
        Cluster cluster = host.getCluster();
        List<String> failoverHosts = hostRepository.getFailoverHostNames(cluster.getId());
        return new LoadBalancerEndpointConfiguration(host, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHosts, null);
    }
}
