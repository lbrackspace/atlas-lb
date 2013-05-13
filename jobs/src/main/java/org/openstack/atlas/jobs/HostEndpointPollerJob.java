package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.services.helpers.AlertHelper;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.List;

public class HostEndpointPollerJob extends AbstractJob implements StatefulJob {
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

    //TODO: refactor to use service/null adapter
    public LoadBalancerEndpointConfiguration getConfigHost(Host host) throws DecryptException, MalformedURLException {
        Cluster cluster = host.getCluster();
        List<String> failoverHosts = hostRepository.getFailoverHostNames(cluster.getId());
        return new LoadBalancerEndpointConfiguration(host, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHosts, null);
    }

    @Override
    public Log getLogger() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JobName getJobName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    //TODO: refactor to use the async service...
    @Override
    public void run() throws Exception {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Host endpoint poller job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(JobName.HOST_ENDPOINT_POLLER, JobStateVal.IN_PROGRESS);

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
            jobStateService.updateJobState(JobName.HOST_ENDPOINT_POLLER, JobStateVal.FAILED);
            LOG.error(String.format("Host endpoint poller job failed while polling host endpoints: %s", e.getMessage()));
            Alert alert = AlertHelper.createAlert(null, null, e, AlertType.API_FAILURE.name(), e.getMessage());
            alertRepository.save(alert);
            return;
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        jobStateService.updateJobState(JobName.HOST_ENDPOINT_POLLER, JobStateVal.FINISHED);
        LOG.info(String.format("Host endpoint poller job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

    @Override
    public void cleanup() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
