package org.openstack.atlas.atom.jobs;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.atom.config.AtomHopperConfiguration;
import org.openstack.atlas.atom.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.atom.pojo.EntryPojo;
import org.openstack.atlas.atom.util.AHUSLUtil;
import org.openstack.atlas.atom.util.LbaasUsageDataMap;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.pojos.AccountLoadBalancer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atomhopper.AHUSLClient;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;
import org.w3._2005.atom.Title;
import org.w3._2005.atom.Type;
import org.w3._2005.atom.UsageCategory;
import org.w3._2005.atom.UsageContent;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AtomHopperLoadBalancerUsageJob extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(AtomHopperLoadBalancerUsageJob.class);

    private Configuration configuration = new AtomHopperConfiguration();
    private LoadBalancerRepository loadBalancerRepository;
    private UsageRepository usageRepository;

    private String region = "GLOBAL"; //default..
    private final String lbaasTitle = "cloudLoadBalancers"; //default..
    private final String label = "loadBalancerUsage";
    private final String author = "LBAAS"; //default..
    private String configRegion = null;
    private String uri = null;

    AHUSLClient client;


    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        startPoller();
    }

    private void startPoller() {
        /**
         * LOG START job-state
         *
         * **/
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Atom hopper load balancer usage poller job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        processJobState(JobName.ATOM_LOADBALANCER_USAGE_POLLER, JobStateVal.IN_PROGRESS);

        if (configuration.getString(AtomHopperConfigurationKeys.allow_ahusl).equals("true")) {
            //URI from config : atomHopper/USL endpoint
            uri = configuration.getString(AtomHopperConfigurationKeys.atom_hopper_endpoint);
            //Create the threaded client to handle requests...
            try {
                client = new AHUSLClient(uri);
            } catch (Exception e) {
                LOG.info("The client failed to initialize: " + Arrays.toString(e.getStackTrace()));
            }

            //Grab all accounts a begin processing usage...
            List<Integer> accounts = loadBalancerRepository.getAllAccountIds();
            for (int id : accounts) {
                //Retrieve all non-deleted lbs for account
                List<AccountLoadBalancer> lbsForAccount = loadBalancerRepository.getAccountLoadBalancers(id);
                //Walk each lb...
                for (AccountLoadBalancer lb : lbsForAccount) {
                    try {
                        //Retrieve usage for account by lbId
                        List<Usage> lbusage = loadBalancerRepository.getUsageByAccountIdandLbId(id, lb.getLoadBalancerId(), AHUSLUtil.getStartCal(), AHUSLUtil.getNow());

                        //Walk each load balancer usage record...
                        for (Usage usageRecord : lbusage) {
                            if (usageRecord.isNeedsPushed()) {

                                EntryPojo entry = new EntryPojo();
                                Title title = new Title();
                                title.setType(Type.TEXT);
                                title.setValue(lbaasTitle);
                                entry.setTitle(title);

                                UsageContent usageContent = new UsageContent();
                                usageContent.setEvent(LbaasUsageDataMap.generateUsageEntry(configuration, configRegion, usageRecord));
                                entry.setContent(usageContent);
                                entry.getContent().setType(MediaType.APPLICATION_XML);

                                UsageCategory usageCategory = new UsageCategory();
                                usageCategory.setLabel(label);
                                usageCategory.setTerm("plain");
                                entry.getCategory().add(usageCategory);


                                LOG.info(String.format("Start Uploading to the atomHopper service now..."));
                                ClientResponse response = client.postEntry(entry);
                                LOG.info(String.format("Finished uploading to the atomHopper service..."));

                                //Notify usage if the record was uploaded or not...
                                if (response.getStatus() == 201) {
                                    usageRecord.setNeedsPushed(false);
                                } else {
                                    LOG.error("There was an error pushing to the atom hopper service" + response.getStatus());
                                    usageRecord.setNeedsPushed(true);
                                }
                                LOG.info("Processing result to the usage table. (Pushed/NotPushed)=" + usageRecord.isNeedsPushed());
                                usageRepository.updatePushedRecord(usageRecord);

                                String body = AHUSLUtil.processResponseBody(response);
                                LOG.info(String.format("Status=%s\n", response.getStatus()));
                                LOG.info(String.format("body %s\n", body));
                                response.close();
                            }
                        }
                    } catch (Throwable t) {
                        System.out.printf("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t));
                        LOG.error(String.format("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t)));
                    }
                }
            }
            client.destroy();
        }

        /**
         * LOG END job-state
         */
        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        processJobState(JobName.ATOM_LOADBALANCER_USAGE_POLLER, JobStateVal.FINISHED);
        LOG.info(String.format("Atom hopper load balancer usage poller job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

    private void processJobState(JobName jobName, JobStateVal jobStateVal) {
        jobStateService.updateJobState(jobName, jobStateVal);
    }

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    @Required
    public void setUsageRepository(UsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }
}
