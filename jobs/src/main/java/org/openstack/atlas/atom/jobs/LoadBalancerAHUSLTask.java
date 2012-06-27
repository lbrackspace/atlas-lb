package org.openstack.atlas.atom.jobs;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.atom.client.AHUSLClient;
import org.openstack.atlas.atom.config.AtomHopperConfiguration;
import org.openstack.atlas.atom.pojo.EntryPojo;
import org.openstack.atlas.atom.util.AHUSLUtil;
import org.openstack.atlas.atom.util.LbaasUsageDataMap;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.w3._2005.atom.Title;
import org.w3._2005.atom.Type;
import org.w3._2005.atom.UsageCategory;
import org.w3._2005.atom.UsageContent;

import javax.ws.rs.core.MediaType;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.List;

public class LoadBalancerAHUSLTask implements Runnable {
    private final Log LOG = LogFactory.getLog(AtomHopperLoadBalancerUsageJob.class);

    //Configuration
    private Configuration configuration = new AtomHopperConfiguration();
    private UsageRepository usageRepository;

    //Defaults
    private String region = "GLOBAL";
    private final String lbaasTitle = "cloudLoadBalancers";
    private final String label = "loadBalancerUsage";
    private String configRegion = null;
    private String uri = null;
    private List<Usage> lbusages;

    private AHUSLClient client;

    public LoadBalancerAHUSLTask(List<Usage> lbusages, AHUSLClient client, UsageRepository usageRepository) {
        this.lbusages = lbusages;
        this.client = client;
        this.usageRepository = usageRepository;
    }

    public LoadBalancerAHUSLTask(List<Usage> lbusages, AHUSLClient client) {
//        this(lbusages, client, usageRepository);  TODO: use when repository deps are updated
    }

    @Override
    public void run() {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Load Balancer AHUSL Task Started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));

        try {
            //Walk each load balancer usage record...
            for (Usage usageRecord : lbusages) {
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
//                    usageRepository.updatePushedRecord(usageRecord);

                    String body = AHUSLUtil.processResponseBody(response);
                    LOG.info(String.format("Status=%s\n", response.getStatus()));
                    LOG.info(String.format("body %s\n", body));
                    response.close();
                }

            }
            usageRepository.batchUpdate(lbusages, false);
        } catch (ConcurrentModificationException cme) {
            System.out.printf("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(cme));
            LOG.warn(String.format("Warning: %s\n", AHUSLUtil.getExtendedStackTrace(cme)));
            LOG.warn(String.format("Job attempted to access usage already being processed, continue processing next data set..."));
        } catch (Throwable t) {
            System.out.printf("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t));
            LOG.error(String.format("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t)));
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        LOG.info(String.format("Load Balancer AHUSL Task Completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }
}
