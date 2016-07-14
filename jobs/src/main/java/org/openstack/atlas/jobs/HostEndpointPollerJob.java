package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerStmAdapter;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.services.helpers.AlertHelper;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.util.List;
import org.openstack.atlas.service.domain.entities.HostStatus;

@Component
public class HostEndpointPollerJob extends AbstractJob {
    private final Log LOG = LogFactory.getLog(HostEndpointPollerJob.class);

    @Autowired
    private ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter;
    @Autowired
    private ReverseProxyLoadBalancerStmAdapter reverseProxyLoadBalancerStmAdapter;
    @Autowired
    private HostRepository hostRepository;

    @Override
    public Log getLogger() {
        return LOG;
    }

    @Override
    public JobName getJobName() {
        return JobName.HOST_ENDPOINT_POLLER;
    }

    @Override
    public void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    }

    //TODO: refactor to use the async service...
    @Override
    public void run() throws Exception {

        try {
            boolean endpointWorks;
            boolean restEndpointWorks;
            List<Host> hosts = hostRepository.getAll();
            for (Host host : hosts) {
                if(host.getHostStatus() == HostStatus.OFFLINE){
                    continue; // Stop pestering hosts marked as offline by engineers
                }
                endpointWorks = reverseProxyLoadBalancerAdapter.isEndPointWorking(getConfigHost(host));
                if (endpointWorks) {
                    host.setSoapEndpointActive(Boolean.TRUE);
                    LOG.info("Host: " + host.getId() + " is active");
                } else {
                    host.setSoapEndpointActive(Boolean.FALSE);
                    LOG.info("Host: " + host.getId() + " is inactive");
                }

                //TODO: Uncomment when using REST interface, important!!!!
//                restEndpointWorks = reverseProxyLoadBalancerStmAdapter.isEndPointWorking(getConfigHost(host));
//                if (restEndpointWorks) {
//                    host.setRestEndpointActive(Boolean.TRUE);
//                    LOG.info("Host: " + host.getId() + " Rest Endpoint is active");
//                } else {
//                    host.setRestEndpointActive(Boolean.FALSE);
//                    LOG.info("Host: " + host.getId() + " Rest Endpoint is inactive");
//                }

                LOG.info("Host: " + host.getId() + " is being updated in the database.");
                hostRepository.update(host);
                LOG.info("Finished updating host: " + host.getId() + " in the database.");
            }
        } catch (Exception e) {
            Alert alert = AlertHelper.createAlert(null, null, e, AlertType.API_FAILURE.name(), "HostEndpointPoller Failure, check Alerts for more information... ");
            alertRepository.save(alert);
            throw e;
        }
    }

    @Override
    public void cleanup() {
    }

    //TODO: refactor to use service/null adapter
    public LoadBalancerEndpointConfiguration getConfigHost(Host host) throws DecryptException, MalformedURLException {
        Cluster cluster = host.getCluster();
        List<String> failoverHostNames = hostRepository.getFailoverHostNames(cluster.getId());
        List<Host> failoverHosts = hostRepository.getFailoverHosts(cluster.getId());
        return new LoadBalancerEndpointConfiguration(host, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHostNames, failoverHosts);
    }

}
