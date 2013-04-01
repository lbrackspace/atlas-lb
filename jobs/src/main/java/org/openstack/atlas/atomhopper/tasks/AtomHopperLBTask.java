package org.openstack.atlas.atomhopper.tasks;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.openstack.atlas.atomhopper.factory.UsageEntryFactory;
import org.openstack.atlas.atomhopper.factory.UsageEntryFactoryImpl;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.restclients.atomhopper.AtomHopperClient;
import org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfiguration;
import org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.w3._2005.atom.UsageEntry;

import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;

import static org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil.getExtendedStackTrace;
import static org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil.getStackTrace;

@Deprecated
public class AtomHopperLBTask implements Runnable {
    private final Log LOG = LogFactory.getLog(AtomHopperLBTask.class);
    private Configuration configuration = new AtomHopperConfiguration();
    private UsageEntryFactory usageEntryFactory;
    private UsageRepository usageRepository;
    private AtomHopperClient ahclient;
    private String validationToken;
    private List<Usage> lbusages;

    public AtomHopperLBTask() {
    }

    public AtomHopperLBTask(List<Usage> lbusages, AtomHopperClient ahclient, String validationToken, UsageRepository usageRepository) {
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
            for (Usage usageRecord : lbusages) {
                if (usageRecord.isNeedsPushed()) {

                    Map<Object, Object> entryMap = usageEntryFactory.createEntry(usageRecord);

                    UsageEntry entryobject = (UsageEntry) entryMap.get("entryobject");
                    String entrystring = (String) entryMap.get("entrystring");
                    logEntry(usageRecord, entrystring);


                    ClientResponse response = null;
                    try {
                        response = ahclient.postEntryWithToken(entrystring, validationToken);
                    } catch (ClientHandlerException che) {
                        LOG.warn("Could not post entry because client handler exception for load balancer: "
                                + usageRecord.getLoadbalancer().getId() + "Exception: " + getStackTrace(che));
                    } catch (ConnectionPoolTimeoutException cpe) {
                        LOG.warn("Could not post entry because of limited connections for load balancer: "
                                + usageRecord.getLoadbalancer().getId() + "Exception: " + getStackTrace(cpe));
                    }

                    //Notify usage if the record was uploaded or not...
                    if (response != null && response.getStatus() == HttpStatus.SC_CREATED) {
                        //update UUID from generated object to domain object.
                        usageRecord.setUuid(entryobject.getContent().getEvent().getId());

                        usageRecord.setNeedsPushed(false);
                        response.close();
                    } else if (response != null) {
                        LOG.error("There was an error pushing to the Atom Hopper service. status code: "
                                + response.getStatus() + " for load balancer: " + usageRecord.getLoadbalancer().getId());

//                        if (configuration.getString(AtomHopperConfigurationKeys.ahusl_log_fail_requests).equals("ENABLED")) {
//                            String body = AtomHopperUtil.processResponseBody(response);
//                            LOG.info(String.format("body %s\n", body));
//
//                            response.close();
//                            LOG.debug("\n FAILED ENTRY: \n ACCOUNT: " + usageRecord.getAccountId() + " LBID: " + usageRecord.getLoadbalancer().getId()
//                                    + " \nENTRY: " + entrystring + " :END FAILED ENTRY");
//                        }

                        //TODO: remove if statement and push all records ....sooon...
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
            System.out.printf("Exception: %s\n", getExtendedStackTrace(cme));
            LOG.warn(String.format("Warning: %s\n", getExtendedStackTrace(cme)));
            LOG.warn(String.format("Job attempted to access usage already being processed, continue processing next data set..."));
        } catch (Throwable t) {
            System.out.printf("Exception: %s\n", getExtendedStackTrace(t));
            LOG.error(String.format("Exception: %s\n", getExtendedStackTrace(t)));
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
