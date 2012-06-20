package org.openstack.atlas.atom.jobs;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.atom.client.AHUSLClient;
import org.openstack.atlas.atom.config.AtomHopperConfiguration;
import org.openstack.atlas.atom.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.atom.pojo.EntryPojo;
import org.openstack.atlas.atom.util.AHUSLUtil;
import org.openstack.atlas.atom.util.LbaasUsageDataMap;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.pojos.AccountBilling;
import org.openstack.atlas.service.domain.repository.AccountUsageRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
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

public class AtomHopperAccountUsageJob extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(AtomHopperAccountUsageJob.class);

    private Configuration configuration = new AtomHopperConfiguration();
    private LoadBalancerRepository loadBalancerRepository;
    private AccountUsageRepository accountUsageRepository;

    private String region = "GLOBAL"; //default..
    private final String lbaasTitle = "cloudLoadBalancers"; //default..
    private final String author = "LBAAS"; //default..
    private final String label = "accountLoadBalancerUsage";
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
        LOG.info(String.format("Atom hopper account usage poller job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        processJobState(JobName.ATOM_ACCOUNT_USAGE_POLLER, JobStateVal.IN_PROGRESS);

        if (configuration.getString(AtomHopperConfigurationKeys.allow_ahusl).equals("true")) {

            //Create the threaded client to handle requests...
            try {
                client = new AHUSLClient();
            } catch (Exception e) {
                LOG.error("There was an error instantiating the client" + Arrays.toString(e.getStackTrace()));
            }

            //Grab all accounts a begin processing usage...
            List<Integer> accounts = loadBalancerRepository.getAllAccountIds();
            for (int accountId : accounts) {
                try {
                    //Latest AccountUsage record
                    AccountBilling accountUsage = loadBalancerRepository.getAccountBilling(accountId, AHUSLUtil.getStartCal(), AHUSLUtil.getNow());

                    //Walk each load balancer usage record...
                    for (AccountUsage asausage : accountUsage.getAccountUsageRecords()) {
                        if (asausage.isNeedsPushed()) {

                            EntryPojo entry = new EntryPojo();

                            Title title = new Title();
                            title.setType(Type.TEXT);
                            title.setValue(lbaasTitle);
                            entry.setTitle(title);

                            UsageContent usageContent = new UsageContent();
                            usageContent.setEvent(LbaasUsageDataMap.generateAccountUsageEntry(configuration, configRegion, asausage));
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
                                asausage.setNeedsPushed(false);
                            } else {
                                LOG.error("There was an error pushing to the atom hopper service" + response.getStatus());
                                asausage.setNeedsPushed(true);
                            }

                            LOG.info("Processing result to the usage table. (Pushed/NotPushed)=" + asausage.isNeedsPushed());
                            accountUsageRepository.updatePushedRecord(asausage);

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
            client.destroy();
        }

        /**
         * LOG END job-state
         */
        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        processJobState(JobName.ATOM_ACCOUNT_USAGE_POLLER, JobStateVal.FINISHED);
        LOG.info(String.format("Atom hopper account usage poller job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

    private void processJobState(JobName jobName, JobStateVal jobStateVal) {
        jobStateService.updateJobState(jobName, jobStateVal);
    }

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    @Required
    public void setAccountUsageRepository(AccountUsageRepository accountUsageRepository) {
        this.accountUsageRepository = accountUsageRepository;
    }
}
