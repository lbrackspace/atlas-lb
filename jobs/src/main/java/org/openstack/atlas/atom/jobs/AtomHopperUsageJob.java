package org.openstack.atlas.atom.jobs;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.atom.pojo.EntryPojo;
import org.openstack.atlas.atom.pojo.LBaaSUsagePojo;
import org.openstack.atlas.atom.pojo.UsageV1Pojo;
import org.openstack.atlas.atom.util.*;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.pojos.AccountLoadBalancer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import javax.ws.rs.core.MediaType;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class AtomHopperUsageJob extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(AtomHopperUsageJob.class);
    private LoadBalancerRepository loadBalancerRepository;
    private UsageRepository usageRepository;

    private Configuration configuration = new AtomHopperConfiguration();

    private String region = "GLOBAL"; //default..
    private final String title = "LBAAS-USAGE"; //default..
    private final String author = "LBAAS"; //default..

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        startPoller();
    }

    private void startPoller() {
        region = configuration.getString(AtomHopperConfigurationKeys.atom_hopper_endpoint);

        //URI from config : atomHopper/USL endpoint
        String URI = configuration.getString(AtomHopperConfigurationKeys.atom_hopper_endpoint);

        /**
         * LOG START job-state
         *
         * **/
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Atom hopper usage poller job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        processJobState(JobName.ATOM_USAGE_POLLER, JobStateVal.IN_PROGRESS);

        //Create the threaded client to handle requests...
        Client client = ClientUtil.makeHttpClient();

        //Grab all accounts a begin processing usage...
        List<Integer> accounts = loadBalancerRepository.getAllAccountIds();
        for (int id : accounts) {
            //Retrieve all non-deleted lbs for account
            List<AccountLoadBalancer> lbsForAccount = loadBalancerRepository.getAccountNonDeleteLoadBalancers(id);
            //Walk each lb...
            for (AccountLoadBalancer lb : lbsForAccount) {
                try {
                    //Retrieve usage for account by lbId
                    List<Usage> lbusage = loadBalancerRepository.getUsageByAccountIdandLbId(id, lb.getLoadBalancerId(), ResponseUtil.getStartTime(), ResponseUtil.getNow());

                    //Walk each record...
                    for (Usage ur : lbusage) {
                        if (!ur.isPushed()) {
                            EntryPojo entry = new EntryPojo();
                            entry.setTitle(title);
                            entry.setAuthor(author);

                            String usage = UsageMarshaller.marshallUsage(generateUsageEntry(ur));
                            entry.setContent(usage);

                            LOG.info(String.format("Contacting and uploading to the atomHopper service now..."));
                            ClientResponse response = client.resource(URI)
                                    .accept(MediaType.APPLICATION_XML)
                                    .type(MediaType.APPLICATION_ATOM_XML)
                                    .post(ClientResponse.class, entry);

                            //Notify usage if the record was pushed or not...
                            if (response.getStatus() == 201) {
                                ur.setPushed(true);
                            } else {
                                ur.setPushed(false);
                            }
                            usageRepository.updateEntryVersion(ur);

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

        /**
         * LOG END job-state
         */
        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        processJobState(JobName.ATOM_USAGE_POLLER, JobStateVal.FINISHED);
        LOG.info(String.format("Atom hopper usage poller job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

    private UsageV1Pojo generateUsageEntry(Usage usageRecord) throws DatatypeConfigurationException {
        UsageV1Pojo usageV1 = new UsageV1Pojo();
        usageV1.setRegion(region);
        usageV1.setServiceCode(title);

        usageV1.setVersion(usageRecord.getEntryVersion().toString());
        usageV1.setStartTime(processCalendar(usageRecord.getStartTime().getTimeInMillis()));
        usageV1.setEndTime(processCalendar(usageRecord.getEndTime().getTimeInMillis()));

        //TODO: map values of event type lb to generic event types...
//        if (usageRecord.getEventType() == null) {
//            usageV1.setEventType(null);
//        } else {
//            usageV1.setEventType(fromValue(usageRecord.getEventType()));
//        }
        usageV1.setEventType(org.openstack.atlas.jobs.EventType.CREATE);
        usageV1.setTenantId(usageRecord.getAccountId().toString());
        usageV1.setResourceId("magicID" + usageRecord.getId());
        usageV1.setUsageId("specialIdForUsageRecord");


        LBaaSUsagePojo lu = new LBaaSUsagePojo();
        lu.setAvgConcurrentConnections(usageRecord.getAverageConcurrentConnections());
        lu.setAvgConcurrentConnectionsSsl(usageRecord.getAverageConcurrentConnectionsSsl());
        lu.setBandWidthIn(usageRecord.getIncomingTransfer());
        lu.setBandWidthInSsl(usageRecord.getIncomingTransferSsl());
        lu.setBandWidthOut(usageRecord.getOutgoingTransfer());
        lu.setBandWidthOutSsl(usageRecord.getOutgoingTransferSsl());
        lu.setNumPolls(usageRecord.getNumberOfPolls());
        lu.setNumVips(usageRecord.getNumVips());
        lu.setTagsBitmask(usageRecord.getTags());

        usageV1.getAny().add(lu);
        return usageV1;
    }

    private XMLGregorianCalendar processCalendar(long timeInMillis) throws DatatypeConfigurationException {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(timeInMillis);
        DatatypeFactory dtf = DatatypeFactory.newInstance();
        return dtf.newXMLGregorianCalendar(gc);
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
