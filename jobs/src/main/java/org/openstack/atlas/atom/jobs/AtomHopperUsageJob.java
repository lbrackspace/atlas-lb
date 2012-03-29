package org.openstack.atlas.atom.jobs;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.jobs.LBaaSUsage;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.AccountLoadBalancer;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import javax.ws.rs.core.MediaType;
import java.util.Calendar;
import java.util.List;

public class AtomHopperUsageJob extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(AtomHopperUsageJob.class);
    private ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter;
    private HostRepository hostRepository;
    private HostUsageRepository hostUsageRepository;
    private LoadBalancerUsageRepository loadBalancerUsageRepository;
    private LoadBalancerRepository loadBalancerRepository;

    @Required
    public void setReverseProxyLoadBalancerAdapter(ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter) {
        this.reverseProxyLoadBalancerAdapter = reverseProxyLoadBalancerAdapter;
    }

    @Required
    public void setLoadBalancerUsageRepository(LoadBalancerUsageRepository loadBalancerUsageRepository) {
        this.loadBalancerUsageRepository = loadBalancerUsageRepository;
    }

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    @Required
    public void setHostRepository(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    @Required
    public void setHostUsageRepository(HostUsageRepository hostUsageRepository) {
        this.hostUsageRepository = hostUsageRepository;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        startPoller();
    }

    private void startPoller() {
        //Update job status..
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Atom hopper usage poller job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(JobName.ATOM_USAGE_POLLER, JobStateVal.IN_PROGRESS);


        com.sun.jersey.api.client.Client client = com.sun.jersey.api.client.Client.create();
        LBaaSUsage lu = new LBaaSUsage();
        List<Usage> lbusage = null;

        //Grab desired usage and prep for upload to AHUSL
        List<Integer> accounts = loadBalancerRepository.getAllAccountIds();

        for (int id : accounts) {
            List<AccountLoadBalancer> lbsForAccount = loadBalancerRepository.getAccountLoadBalancers(id);
            for (AccountLoadBalancer lb : lbsForAccount) {
                try {
                    lbusage = loadBalancerRepository.getUsageByAccountIdandLbId(id, lb.getLoadBalancerId(), startTime, startTime);
                } catch (EntityNotFoundException e) {
                    LOG.error("The Entity was not found...");
                } catch (DeletedStatusException e) {
                    LOG.error("The item was deleted and cannot be fetched...");
                }

                for (Usage usage : lbusage) {
                    //add these values to LBaaSUsage object the upload...
//                     lu.addTheseThings();

                    ClientResponse cr = client.resource("atom.endpoint.com").path("/").queryParam("bliggityblah", "bloop").accept(MediaType.APPLICATION_XML).post(ClientResponse.class, lu);

                }
            }
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        jobStateService.updateJobState(JobName.ATOM_USAGE_POLLER, JobStateVal.FINISHED);
        LOG.info(String.format("Atom hopper usage poller job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }
}
