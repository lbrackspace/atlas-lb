package org.openstack.atlas.atom.jobs;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.atom.pojo.Note;
import org.openstack.atlas.atom.pojo.NotesList;
import org.openstack.atlas.atom.util.AtomHopperConfiguration;
import org.openstack.atlas.atom.util.AtomHopperConfigurationKeys;
import org.openstack.atlas.atom.util.ClientUtil;
import org.openstack.atlas.atom.util.ResponseUtil;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.pojos.AccountLoadBalancer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

public class AtomHopperUsageJob extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(AtomHopperUsageJob.class);
    private LoadBalancerRepository loadBalancerRepository;
    private LoadBalancerUsageRepository loadBalancerUsageRepository;

    private Configuration configuration = new AtomHopperConfiguration();

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        startPoller();
    }

    private void startPoller() {

        //URI from config : atomHopper/USL endpoint
        String URI = configuration.getString(AtomHopperConfigurationKeys.atom_hopper_endpoint);

        /**
         * Log START job state
         *
         * **/
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Atom hopper usage poller job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        processJobState(JobName.ATOM_USAGE_POLLER, JobStateVal.IN_PROGRESS);

        /**
         * Create the client
         *
         * **/
        Client client = ClientUtil.makeHttpClient();

        List<Integer> accounts = loadBalancerRepository.getAllAccountIds();
        for (int id : accounts) {
            List<AccountLoadBalancer> lbsForAccount = loadBalancerRepository.getAccountLoadBalancers(id);

            List<Usage> lbusage;
            for (AccountLoadBalancer lb : lbsForAccount) {
                try {
                    lbusage = loadBalancerRepository.getUsageByAccountIdandLbId(id, lb.getLoadBalancerId(), ResponseUtil.getStartTime(), ResponseUtil.getNow());

                    //Tmp
                    int i = 0;
                    //Tmp

                    for (Usage ur : lbusage) {
//                        LBaasUsagePojo lu = new LBaasUsagePojo();
//                        lu.setMemory(ur.getNumberOfPolls());


                        //Tmp till contract for AH is ready...
                        NotesList notesList = new NotesList();
                        Note note = new Note();
                        note.setText("This is my note:" + i + "LBID: " + ur.getLoadbalancer().getId());
                        notesList.getNotes().add(note);
                        i++;


                        LOG.info(String.format("Contacting and uploading to the atomHopper service now..."));
                        ClientResponse response = client.resource(URI)
                                .path("/notes/new")
                                .accept(MediaType.APPLICATION_XML)
                                .type(MediaType.APPLICATION_XML)
                                .post(ClientResponse.class, notesList);


                        //Reading the response to parse the body to LOG (String)
                        InputStream is = response.getEntityInputStream();
                        StringBuilder sb = new StringBuilder(ResponseUtil.PAGESIZE);
                        if (response.getStatus() == 200) {
                          ur.setPushed(true);
                        } else {
                            ur.setPushed(false);
                        }


                        int nbytes;
                        do {
                            byte[] buff = new byte[ResponseUtil.FRAGSIZE];
                            nbytes = is.read(buff);
                            String frag = new String(buff, "UTF-8");
                            sb.append(frag);
                        } while (nbytes > 0);

                        String body = sb.toString();
                        LOG.info(String.format("Status=%s\n", response.getStatus()));
                        LOG.info(String.format("body %s\n", body));
                        response.close();
                    }
                } catch (Throwable t) {
                    System.out.printf("Exception: %s\n", ResponseUtil.getExtendedStackTrace(t));
                    LOG.error(String.format("Exception: %s\n", ResponseUtil.getExtendedStackTrace(t)));
                }
            }
        }
        client.destroy();

        /**
         * Log END job state
         */
        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        processJobState(JobName.ATOM_USAGE_POLLER, JobStateVal.FINISHED);
        LOG.info(String.format("Atom hopper usage poller job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

    private void processJobState(JobName jobName, JobStateVal jobStateVal) {
        jobStateService.updateJobState(jobName, jobStateVal);
    }

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }
}
