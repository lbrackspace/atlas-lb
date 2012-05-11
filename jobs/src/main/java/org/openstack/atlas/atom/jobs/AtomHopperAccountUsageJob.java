package org.openstack.atlas.atom.jobs;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.atom.pojo.AccountLBaaSUsagePojo;
import org.openstack.atlas.atom.pojo.EntryPojo;
import org.openstack.atlas.atom.pojo.UsageV1Pojo;
import org.openstack.atlas.atom.util.*;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.pojos.AccountBilling;
import org.openstack.atlas.service.domain.pojos.AccountLoadBalancer;
import org.openstack.atlas.service.domain.repository.AccountUsageRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;
import org.w3._2005.atom.UsageCategory;
import org.w3._2005.atom.UsageContent;

import javax.ws.rs.core.MediaType;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class AtomHopperAccountUsageJob extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(AtomHopperAccountUsageJob.class);

    private Configuration configuration = new AtomHopperConfiguration();
    private LoadBalancerRepository loadBalancerRepository;
    private AccountUsageRepository accountUsageRepository;

    private String region = "GLOBAL"; //default..
    private final String title = "cloudLoadBalancers"; //default..
    private final String author = "LBAAS"; //default..
    private String configRegion = null;
    private String uri = null;

    Client client;


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
            //URI from config : atomHopper/USL endpoint
            uri = configuration.getString(AtomHopperConfigurationKeys.atom_hopper_endpoint);
            //Create the threaded client to handle requests...
            client = ClientUtil.makeHttpClient();

            //Grab all accounts a begin processing usage...
            List<Integer> accounts = loadBalancerRepository.getAllAccountIds();
            for (int id : accounts) {
                //Retrieve all non-deleted lbs for account
                List<AccountLoadBalancer> lbsForAccount = loadBalancerRepository.getAccountNonDeleteLoadBalancers(id);
                //Walk each lb...
                for (AccountLoadBalancer lb : lbsForAccount) {
                    try {
                        //Retrieve usage for account by lbId
                        List<Usage> lbusage = loadBalancerRepository.getUsageByAccountIdandLbId(id, lb.getLoadBalancerId(), ResponseUtil.getStartCal(), ResponseUtil.getNow());
                        //Latest AccountUsage record
                        AccountBilling accountUsage = loadBalancerRepository.getAccountBilling(id, ResponseUtil.getStartCal(), ResponseUtil.getNow());

                        //Walk each load balancer usage record...
                        for (AccountUsage asausage : accountUsage.getAccountUsageRecords()) {
                            if (asausage.isNeedsPushed()) {

                                EntryPojo entry = new EntryPojo();
                                entry.setTitle(title);
                                entry.setAuthor(author);

                                UsageContent usageContent = new UsageContent();
                                usageContent.setUsage(generateUsageEntry(asausage));
                                entry.setContent(usageContent);
                                entry.getContent().setType(MediaType.APPLICATION_XML);
                                UsageCategory usageCategory = new UsageCategory();
                                usageCategory.setLabel("accountLoadBalancerUsage");
                                usageCategory.setTerm("plain");
                                entry.getCategory().add(usageCategory);


                                LOG.info(String.format("Uploading to the atomHopper service now..."));
                                ClientResponse response = client.resource(uri)
                                        .accept(MediaType.APPLICATION_XML)
                                        .type(MediaType.APPLICATION_ATOM_XML)
                                        .post(ClientResponse.class, entry);

                                //Notify usage if the record was uploaded or not...
                                if (response.getStatus() == 201) {
                                    asausage.setNeedsPushed(false);
                                } else {
                                    LOG.error("There was an error pushing to the atom hopper service" + response.getStatus());
                                    asausage.setNeedsPushed(true);
                                }
                                accountUsageRepository.updatePushedRecord(asausage);

                                String body = ResponseUtil.processResponseBody(response);
                                LOG.info(String.format("Status=%s\n", response.getStatus()));
                                LOG.info(String.format("body %s\n", body));
                                response.close();
                            }
                        }
                    } catch (Throwable t) {
                        System.out.printf("Exception: %s\n", ResponseUtil.getExtendedStackTrace(t));
                        LOG.error(String.format("Exception: %s\n", ResponseUtil.getExtendedStackTrace(t)));
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
        processJobState(JobName.ATOM_ACCOUNT_USAGE_POLLER, JobStateVal.FINISHED);
        LOG.info(String.format("Atom hopper account usage poller job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

    private UsageV1Pojo generateUsageEntry(AccountUsage accountUsage) throws DatatypeConfigurationException, NoSuchAlgorithmException {
        configRegion = configuration.getString(AtomHopperConfigurationKeys.region);
        if (configRegion != null) {
            region = configRegion;
        }

        UsageV1Pojo usageV1 = new UsageV1Pojo();
        usageV1.setRegion(region);
        usageV1.setServiceCode(title);

        usageV1.setVersion("1");//Rows are not updated...
        usageV1.setStartTime(processCalendar(accountUsage.getStartTime().getTimeInMillis()));
        usageV1.setEndTime(processCalendar(accountUsage.getStartTime().getTimeInMillis()));

        usageV1.setEventType(null); //No events
        usageV1.setTenantId(accountUsage.getAccountId().toString());
        usageV1.setResourceId(accountUsage.getId().toString());

        //Generate UUID
        UUID uuid = UUIDUtil.genUUID(genUUIDString(accountUsage));
        usageV1.setUsageId(uuid.toString());

        //LBaaS account usage
        AccountLBaaSUsagePojo ausage = new AccountLBaaSUsagePojo();
        ausage.setAccountId(accountUsage.getAccountId());
        ausage.setId(accountUsage.getId());
        ausage.setNumLoadbalancers(accountUsage.getNumLoadBalancers());
        ausage.setNumPublicVips(accountUsage.getNumPublicVips());
        ausage.setNumServicenetVips(accountUsage.getNumServicenetVips());
        ausage.setStartTime(processCalendar(accountUsage.getStartTime().getTimeInMillis()));
        usageV1.getAny().add(ausage);

        return usageV1;
    }

    private XMLGregorianCalendar processCalendar(long timeInMillis) throws DatatypeConfigurationException {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(timeInMillis);
        DatatypeFactory dtf = DatatypeFactory.newInstance();
        return dtf.newXMLGregorianCalendar(gc);
    }

    private String genUUIDString(AccountUsage usageRecord) {
        return usageRecord.getId() + "_" + usageRecord.getAccountId() + "_" + region;
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
