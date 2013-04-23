package org.openstack.atlas.usage.thread;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.openstack.atlas.atomhopper.exception.AtomHopperMappingException;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.restclients.atomhopper.AtomHopperClient;
import org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfiguration;
import org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil;
import org.openstack.atlas.restclients.auth.IdentityAuthClient;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.entities.*;
import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.events.repository.LoadBalancerEventRepository;
import org.openstack.atlas.service.domain.util.Constants;
import org.w3._2005.atom.UsageEntry;

import java.util.*;

import static org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil.getExtendedStackTrace;
import static org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil.getStackTrace;

public abstract class AbstractAtomHopperThread implements Runnable {
    private final Log LOG = LogFactory.getLog(AbstractAtomHopperThread.class);
    private Configuration configuration = new AtomHopperConfiguration();
    private AtomHopperClient client;
    private IdentityAuthClient identityAuthClient;
    private AlertRepository alertRepository;
    private LoadBalancerEventRepository loadBalancerEventRepository;
    private List<Usage> usages;

    public abstract Map<Object, Object> generateAtomHopperEntry(Usage usage) throws AtomHopperMappingException;

    public abstract void updatePushedRecords(List<Usage> successfullyPushedRecordIds);

    public abstract String getThreadName();

    public AbstractAtomHopperThread(List<Usage> usages, AtomHopperClient client, IdentityAuthClient identityAuthClient,
                                    LoadBalancerEventRepository loadBalancerEventRepository,
                                    AlertRepository alertRepository) {
        this.usages = usages;
        this.client = client;
        this.identityAuthClient = identityAuthClient;
        this.loadBalancerEventRepository = loadBalancerEventRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    public void run() {
        Calendar startTime = AtomHopperUtil.getNow();
        LOG.info(String.format("Load Balancer AHUSL Task Started at %s (Timezone: %s)",
                startTime.getTime().toString(), startTime.getTimeZone().getDisplayName()));

        List<Usage> successfullyPushedRecords = new ArrayList<Usage>();
        List<Usage> failedToPushRecords = new ArrayList<Usage>();
        try {
            String authToken = identityAuthClient.getAuthToken();
            for (Usage usageRecord : usages) {
                if (usageRecord.isNeedsPushed()) {
                    ClientResponse response = null;
                    Map<Object, Object> entryMap = generateAtomHopperEntry(usageRecord);
                    String entrystring = (String) entryMap.get("entrystring");
                    UsageEntry entryobject = (UsageEntry) entryMap.get("entryobject");

                    try {
                        response = client.postEntryWithToken(entrystring, authToken);
                    } catch (ClientHandlerException che) {
                        LOG.warn("Could not post entry because client handler exception for load balancer: "
                                + usageRecord.getLoadbalancer().getId() + "Exception: " + getStackTrace(che));
                    } catch (ConnectionPoolTimeoutException cpe) {
                        LOG.warn("Could not post entry because of limited connections for load balancer: "
                                + usageRecord.getLoadbalancer().getId() + "Exception: " + getStackTrace(cpe));
                    }


                    String message;
                    String body = AtomHopperUtil.processResponseBody(response);
                    int numAttempts = usageRecord.getNumAttempts();
                    if (response != null) {
                        if (response.getStatus() == 201) {
                            usageRecord.setNeedsPushed(false);
                            usageRecord.setNumAttempts(0);
                            usageRecord.setCorrected(false);
                            usageRecord.setUuid(entryobject.getContent().getEvent().getId());
                            successfullyPushedRecords.add(usageRecord);
                            response.close();
                        } else if (response.getStatus() == 400) {
                            usageRecord.setNeedsPushed(true);
                            usageRecord.setNumAttempts(numAttempts + 1);
                            failedToPushRecords.add(usageRecord);

                            message = body.split("<message>")[1].split("</message>")[0];
                            generateServiceEventRecord(usageRecord, entrystring, message);
                            generateAtomHopperAlertRecord(usageRecord, "AH-FAILED-ENTRY-"
                                    + usageRecord.getLoadbalancer().getId(), message);
                            if (configuration.getString(AtomHopperConfigurationKeys
                                    .ahusl_log_requests).equals("ENABLED")) {
                                LOG.info(String.format("body %s\n", body));
                                LOG.debug("\nFAILED ENTRY: \nACCOUNT: " + usageRecord.getAccountId()
                                        + " \nLBID: " + usageRecord.getLoadbalancer().getId()
                                        + " \nENTRY: " + entrystring + " \n:END FAILED ENTRY");
                            }
                            response.close();
                        } else if (response.getStatus() == 500) {
                            usageRecord.setNeedsPushed(true);
                            usageRecord.setNumAttempts(numAttempts + 1);
                            failedToPushRecords.add(usageRecord);

                            message = body.split("<message>")[1].split("</message>")[0];
                            generateAtomHopperAlertRecord(usageRecord, "AH-SERVICE-"
                                    + usageRecord.getLoadbalancer().getId(),
                                    "Atom Hopper service failure: " + message);
                            LOG.error("Failure to process Atom Hopper usage due to Atom Hopper service error: ");
                            LOG.debug(String.format("body %s\n", body));

                            response.close();
                        }
                    } else {
                        LOG.error("Communication error with the Atom Hopper service occurred, " +
                                "updating record for re-push for data base: " + usageRecord.getAccountId());
                        usageRecord.setNeedsPushed(true);
                        failedToPushRecords.add(usageRecord);
                    }
                }
            }

//            try {
                LOG.info("Batch updating: " + usages.size() + " usage rows in the database...");
                updatePushedRecords(usages);
                LOG.info("Successfully batch updated: " + usages.size() + " usage rows in the database...");
//            } catch (Exception lex) {
//                LOG.error("There was batch updating usages, number of usages affected: " + usages.size() +
//                        " Retrying because of deadlock: Exception: " + lex);
//                batchUpdateRecords();
//            }
        } catch (ConcurrentModificationException cme) {
            System.out.printf("Exception: %s\n", getExtendedStackTrace(cme));
            LOG.warn(String.format("Warning: %s\n", getExtendedStackTrace(cme)));
            LOG.warn(String.format("Job attempted to access usage already being processed, " +
                    "continue processing next data set..."));
        } catch (Throwable t) {
            System.out.printf("Exception: %s\n", getExtendedStackTrace(t));
            LOG.error(String.format("Exception: %s\n", getExtendedStackTrace(t)));
        }

        Double elapsedMins = ((AtomHopperUtil.getNow()
                .getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        LOG.info(String.format("Load Balancer AHUSL Task: " + getThreadName()
                + " Completed at '%s' (Total Time: %f mins)",
                AtomHopperUtil.getNow().getTime().toString(), elapsedMins));
        LOG.debug(String.format("Load Balancer AHUSL Task: " + getThreadName()
                + " Failed tasks count: %d out of %d", failedToPushRecords.size(), usages.size()));
    }

    private void generateServiceEventRecord(Usage usageRecord, String entrystring, String responseMessage) {
        LoadBalancerServiceEvent loadBalancerEvent = new LoadBalancerServiceEvent();
        loadBalancerEvent.setAccountId(usageRecord.getAccountId());
        loadBalancerEvent.setDescription(entrystring);
        loadBalancerEvent.setAuthor(Constants.AH_USAGE_EVENT_AUTHOR);
        loadBalancerEvent.setCategory(CategoryType.UPDATE);
        loadBalancerEvent.setLoadbalancerId(usageRecord.getLoadbalancer().getId());
        loadBalancerEvent.setTitle(responseMessage);
        loadBalancerEvent.setType(EventType.AH_USAGE_EXECUTION);
        loadBalancerEvent.setSeverity(EventSeverity.CRITICAL);
        loadBalancerEventRepository.save(loadBalancerEvent);
    }

    private void generateAtomHopperAlertRecord(Usage usageRecord, String alertName, String alertMessage) {
        Alert alert = new Alert();
        alert.setAccountId(usageRecord.getAccountId());
        alert.setAlertType(Constants.AH_USAGE_EVENT_FAILURE);
        alert.setStatus(AlertStatus.UNACKNOWLEDGED);
        alert.setLoadbalancerId(usageRecord.getLoadbalancer().getId());
        alert.setMessageName(alertName);
        alert.setMessage(alertMessage);
        alertRepository.save(alert);
    }

    private void batchUpdateRecords() {
        LOG.info("Batch updating: " + usages.size() + " usage rows in the database...");
        updatePushedRecords(usages);
        LOG.info("Successfully batch updated: " + usages.size() + " usage rows in the database...");
    }
}
