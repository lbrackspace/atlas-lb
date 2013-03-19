package org.openstack.atlas.atom.tasks;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.openstack.atlas.atom.client.AtomHopperClientImpl;
import org.openstack.atlas.atom.config.AtomHopperConfiguration;
import org.openstack.atlas.atom.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.atom.factory.UsageEntryFactory;
import org.openstack.atlas.atom.factory.UsageEntryFactoryImpl;
import org.openstack.atlas.atom.util.AHUSLUtil;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.w3._2005.atom.UsageEntry;

import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;

public class LoadBalancerAHUSLTask implements Runnable {
    private final Log LOG = LogFactory.getLog(LoadBalancerAHUSLTask.class);
    private Configuration configuration = new AtomHopperConfiguration();
    private UsageEntryFactory usageEntryFactory;
    private UsageRepository usageRepository;
    private AtomHopperClientImpl ahclient;
    private String validationToken;
    private List<Usage> lbusages;

    public LoadBalancerAHUSLTask() {
    }

    public LoadBalancerAHUSLTask(List<Usage> lbusages, AtomHopperClientImpl ahclient, String validationToken, UsageRepository usageRepository) {
        this.lbusages = lbusages;
        this.ahclient = ahclient;
        this.validationToken = validationToken;
        this.usageRepository = usageRepository;
        this.usageEntryFactory = new UsageEntryFactoryImpl();
    }

    @Override
    public void run() {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Load Balancer AHUSL Task Started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));

        try {
            //Walk each load balancer usage record...
            for (Usage usageRecord : lbusages) {
                if (usageRecord.isNeedsPushed()) {

                    Map<Object, Object> entryMap = usageEntryFactory.createEntry(usageRecord, configuration, configuration.getString(AtomHopperConfigurationKeys.ahusl_region));
                    UsageEntry entryobject = (UsageEntry) entryMap.get("entryobject");
                    String entrystring = (String) entryMap.get("entrystring");
                    logEntry(usageRecord, entrystring);

                    //update UUID from generated object to domain object.
                    usageRecord.setUuid(entryobject.getContent().getEvent().getId());

                    ClientResponse response = null;
                    try {
                        response = ahclient.postEntryWithToken(entrystring, validationToken);
                    } catch (ClientHandlerException che) {
                        LOG.warn("Could not post entry because client handler exception for load balancer: "
                                + usageRecord.getLoadbalancer().getId() + "Exception: " + AHUSLUtil.getStackTrace(che));
                    } catch (ConnectionPoolTimeoutException cpe) {
                        LOG.warn("Could not post entry because of limited connections for load balancer: "
                                + usageRecord.getLoadbalancer().getId() + "Exception: " + AHUSLUtil.getStackTrace(cpe));
                    }

                    //Notify usage if the record was uploaded or not...
                    if (response != null && response.getStatus() == HttpStatus.SC_CREATED) {
                        usageRecord.setNeedsPushed(false);
                        response.close();
                    } else if (response != null) {
                        LOG.error("There was an error pushing to the atom hopper service. status code: "
                                + response.getStatus() + " for load balancer: " + usageRecord.getLoadbalancer().getId());

                        String body = AHUSLUtil.processResponseBody(response);
                        LOG.info(String.format("body %s\n", body));

                        response.close();
                        LOG.debug("\n FAILED ENTRY: \n ACCOUNT: " + usageRecord.getAccountId() + " LBID: " + usageRecord.getLoadbalancer().getId()
                                + " \nENTRY: " + entrystring + " :END FAILED ENTRY");

                        if (usageRecord.getEventType() != null && usageRecord.getEventType().equals(UsageEvent.DELETE_LOADBALANCER.name())) {
                            usageRecord.setNeedsPushed(false);
                        } else {
                            usageRecord.setNeedsPushed(true);
                        }
                    } else {
                        LOG.error("The connection timed out, updating record for re-push for load balancer: " + usageRecord.getLoadbalancer().getId());
                        usageRecord.setNeedsPushed(true);
                    }
                }
            }
            LOG.debug("Batch updating: " + lbusages.size() + " usage rows in the database...");
            usageRepository.batchUpdate(lbusages, false);
        } catch (ConcurrentModificationException cme) {
            System.out.printf("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(cme));
            LOG.warn(String.format("Warning: %s\n", AHUSLUtil.getExtendedStackTrace(cme)));
            LOG.warn(String.format("Job attempted to access usage already being processed, continue processing next data set..."));
        } catch (Throwable t) {
            System.out.printf("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t));
            LOG.error(String.format("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t)));
        }

        logElapsedTime(startTime);
    }

    private void logEntry(Usage usageRecord, String entrystring) {
        String d = configuration.getString(AtomHopperConfigurationKeys.ahusl_log_requests);
        if (d != null && d.equals("ENABLED")) {
            LOG.debug(String.format("AHUSL ENTRY: ACCOUNTID: %s LBID: %s ENTRY: \n %s \n", usageRecord.getAccountId(), usageRecord.getLoadbalancer().getId(), entrystring));
        }
    }

    private void logElapsedTime(Calendar startTime) {
        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        LOG.info(String.format("Load Balancer AHUSL Task Completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

//    public LoadBalancerAHUSLTask(List<Usage> lbusages, AHUSLClient client) {
////        this(lbusages, client, usageRepository);  TODO: use when repository deps are updated
//    }
}
