package org.openstack.atlas.atom.tasks;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.openstack.atlas.atom.auth.AHUSLAuthentication;
import org.openstack.atlas.atom.client.AHUSLClient;
import org.openstack.atlas.atom.config.AtomHopperConfiguration;
import org.openstack.atlas.atom.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.atom.jobs.AtomHopperLoadBalancerUsageJob;
import org.openstack.atlas.atom.pojo.EntryPojo;
import org.openstack.atlas.atom.util.AHUSLUtil;
import org.openstack.atlas.atom.util.LbaasUsageDataMap;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.repository.UsageRepository;

import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.List;

public class LoadBalancerAHUSLTask implements Runnable {
    private final Log LOG = LogFactory.getLog(AtomHopperLoadBalancerUsageJob.class);
    private String token;

    public LoadBalancerAHUSLTask() {
    }

    //Configuration
    private Configuration configuration = new AtomHopperConfiguration();
    private AHUSLAuthentication ahuslAuthentication;
    private UsageRepository usageRepository;

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
            ahuslAuthentication = new AHUSLAuthentication();
            token = ahuslAuthentication.getToken(configuration.getString(AtomHopperConfigurationKeys.ahusl_auth_username)).getToken().getId();
            if (token != null) LOG.info("Token successfully retrieved...");
        } catch (Exception e) {
            LOG.error("There was an error retrieving token: " + e);
        }


        if (token != null) {
            try {
                //Walk each load balancer usage record...
                for (Usage usageRecord : lbusages) {
                    if (usageRecord.isNeedsPushed()) {

                        EntryPojo entry = LbaasUsageDataMap.buildUsageEntry(
                                usageRecord,
                                configuration,
                                configuration.getString(AtomHopperConfigurationKeys.ahusl_region));

                        ClientResponse response = null;

                        try {
//                        LOG.debug(String.format("Start Uploading to the atomHopper service now for account: " + usageRecord.getLoadbalancer().getId()));
                            response = client.postEntryWithToken(entry, token);
//                        LOG.info(String.format("Finished uploading to the atomHopper service for account: " + usageRecord.getLoadbalancer().getId()));
                        } catch (ClientHandlerException che) {
                            LOG.warn("Could not post entry because client handler exception for load balancer: " + usageRecord.getLoadbalancer().getId() + "Exception: " + AHUSLUtil.getStackTrace(che));
                        } catch (ConnectionPoolTimeoutException cpe) {
                            LOG.warn("Could not post entry because of limited connections for load balancer: " + usageRecord.getLoadbalancer().getId() + "Exception: " + AHUSLUtil.getStackTrace(cpe));
                        }

                        //Notify usage if the record was uploaded or not...
                        if (response != null && response.getStatus() == 201) {
//                        LOG.debug("Updating needs_pushed: " + false + " for load balancer: " + usageRecord.getLoadbalancer().getId());
                            usageRecord.setNeedsPushed(false);
                            response.close();
                        } else if (response != null) {
                            LOG.error("There was an error pushing to the atom hopper service. status code: " + response.getStatus() + " for load balancer: " + usageRecord.getLoadbalancer().getId());
                            String body = AHUSLUtil.processResponseBody(response);
                            LOG.info(String.format("body %s\n", body));
                            response.close();
                            usageRecord.setNeedsPushed(true);
                        } else {
                            LOG.error("The connection timed out, updating record for re-push for load balancer: " + usageRecord.getLoadbalancer().getId());
                            usageRecord.setNeedsPushed(true);
                        }

//                    String body = AHUSLUtil.processResponseBody(response);
//                    LOG.info(String.format("Status=%s\n", response.getStatus()));
//                    LOG.info(String.format("body %s\n", body));
                    }
                }

                LOG.debug("Batch updating: " + lbusages.size() + " usage rows in the database...");
                usageRepository.batchUpdate(lbusages, false);
//            LOG.info("Successfully batch updated: " + lbusages.size() + " usage rows in the database...");
            } catch (ConcurrentModificationException cme) {
                System.out.printf("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(cme));
                LOG.warn(String.format("Warning: %s\n", AHUSLUtil.getExtendedStackTrace(cme)));
                LOG.warn(String.format("Job attempted to access usage already being processed, continue processing next data set..."));
            } catch (Throwable t) {
                System.out.printf("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t));
                LOG.error(String.format("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t)));
            }
        } else {
            LOG.error("Could not retrieve authentication token, no requests are being processed, please notify operations... ::AUTH FAILED ALERT::");
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        LOG.info(String.format("Load Balancer AHUSL Task Completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }
}
