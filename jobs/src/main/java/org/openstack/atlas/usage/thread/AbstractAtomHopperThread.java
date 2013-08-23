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

import java.io.IOException;
import java.util.*;

import static org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil.getExtendedStackTrace;
import static org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil.getStackTrace;

public abstract class AbstractAtomHopperThread implements Runnable {
    private final Log LOG = LogFactory.getLog(AbstractAtomHopperThread.class);
    private Configuration configuration = new AtomHopperConfiguration();

    private List<Usage> usages;
    private AtomHopperClient client;
    private IdentityAuthClient identityAuthClient;

    private AlertRepository alertRepository;
    private LoadBalancerEventRepository loadBalancerEventRepository;

    private List<Usage> successfullyPushedRecords;
    private List<Usage> failedToPushRecords;

    private boolean isVerboseLog = false;

    public abstract String getThreadName();

    public abstract Map<Object, Object> generateAtomHopperEntry(Usage usage) throws AtomHopperMappingException;

    public abstract void updatePushedRecords(List<Usage> successfullyPushedRecordIds);

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
        LOG.info(String.format("Load Balancer Atom Hopper USL Task Started at %s (Timezone: %s)",
                startTime.getTime().toString(), startTime.getTimeZone().getDisplayName()));

        isVerboseLog = configuration.getString(AtomHopperConfigurationKeys
                .ahusl_log_requests).equals("ENABLED");

        successfullyPushedRecords = new ArrayList<Usage>();
        failedToPushRecords = new ArrayList<Usage>();

        try {
            //Retrieve identity-admin user
            String authToken = identityAuthClient.getAuthToken();
            for (Usage usageRecord : usages) {
                if (usageRecord.isNeedsPushed()) {
                    ClientResponse response = null;
                    Map<Object, Object> entryMap = generateAtomHopperEntry(usageRecord);
                    String entrystring = (String) entryMap.get("entrystring");
                    UsageEntry entryobject = (UsageEntry) entryMap.get("entryobject");

                    String body = AtomHopperUtil.processResponseBody(response);

                    try {
                        response = client.postEntryWithToken(entrystring, authToken);
                    } catch (ClientHandlerException che) {
                        LOG.warn("Could not post entry because client handler exception for load balancer: "
                                + usageRecord.getLoadbalancer().getId() + "Exception: " + getStackTrace(che));
                        logAndAlert(body, usageRecord, entrystring);
                        failedToPushRecords.add(usageRecord);
                    } catch (ConnectionPoolTimeoutException cpe) {
                        LOG.warn("Could not post entry because of limited connections for load balancer: "
                                + usageRecord.getLoadbalancer().getId() + "Exception: " + getStackTrace(cpe));
                        logAndAlert(body, usageRecord, entrystring);
                        failedToPushRecords.add(usageRecord);

                    }

                    processResponse(response, body, usageRecord, entryobject, entrystring);
                    response.close();
                }
            }

            //Task complete update records...
            LOG.info(String.format("Batch updating: %d " +
                    "Atom Hopper usage entries in the database...", usages.size()));
            updatePushedRecords(usages);
            LOG.info(String.format("Successfully batch updated: %d " +
                    "Atom Hopper entries in the database...", usages.size()));

        } catch (ConcurrentModificationException cme) {
            System.out.printf("Exception: %s\n", getExtendedStackTrace(cme));
            LOG.warn(String.format("Warning: %s\n", getExtendedStackTrace(cme)));
            LOG.warn(String.format("Job attempted to access usage already being processed, " +
                    "continue processing next data set..."));
        } catch (Throwable t) {
            LOG.error(String.format("Exception during Atom-Hopper processing: %s\n", getExtendedStackTrace(t)));
            generateSevereAlert("Severe Failure processing Atom Hopper requests: ", getExtendedStackTrace(t));
        }

        Double elapsedMins = ((AtomHopperUtil.getNow()
                .getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        LOG.info(String.format("Load Balancer Atom Hopper USL Task: %s Completed at '%s' (Total Time: %f mins)",
                getThreadName(), AtomHopperUtil.getNow().getTime().toString(), elapsedMins));
        LOG.debug(String.format("Load Balancer Atom Hopper USL Task: %s Failed tasks count: %d out of %d",
                getThreadName(), failedToPushRecords.size(), usages.size()));
        LOG.debug(String.format("Load Balancer Atom Hopper Task: %s Sucessfully pushed tasks count: %d out of %d",
                getThreadName(), successfullyPushedRecords.size(), usages.size()));
    }

    private void processResponse(ClientResponse response, String body, Usage usageRecord,
                                 UsageEntry entryobject, String entrystring) throws IOException {

        int numAttempts = usageRecord.getNumAttempts();

        usageRecord.setUuid(entryobject.getContent().getEvent().getId());

        if (response != null) {
            if (response.getStatus() == 201) {
                usageRecord.setNeedsPushed(false);
                usageRecord.setNumAttempts(0);
                usageRecord.setCorrected(false);
                successfullyPushedRecords.add(usageRecord);
                logSuccess(body, usageRecord, entrystring);

            } else if (response.getStatus() == 409) {
                //Duplicate record, we will assume its been pushed, but should alert and verify...
                usageRecord.setNeedsPushed(false);
                usageRecord.setNumAttempts(numAttempts + 1);
                failedToPushRecords.add(usageRecord);
                generateServiceEventRecord(usageRecord, entrystring, buildMessage(body));
                logAndAlert(body, usageRecord, entrystring);

            } else if (response.getStatus() == 400) {
                usageRecord.setNeedsPushed(true);
                usageRecord.setNumAttempts(numAttempts + 1);
                failedToPushRecords.add(usageRecord);
                generateServiceEventRecord(usageRecord, entrystring, buildMessage(body));
                logAndAlert(body, usageRecord, entrystring);

            } else {
                LOG.error(String.format("Error processing entry in Atom Hopper service occurred, " +
                        "updating record for re-push for: Account: %d LBID: %d UUID: %s",
                        usageRecord.getAccountId(), usageRecord.getLoadbalancer().getId(), usageRecord.getUuid()));
                usageRecord.setNeedsPushed(true);
                usageRecord.setNumAttempts(numAttempts + 1);
                failedToPushRecords.add(usageRecord);
                logAndAlert(body, usageRecord, entrystring);
            }
        }
    }

    private void logAndAlert(String body, Usage usageRecord, String entrystring) {
        LOG.info(String.format("Creating alert for Atom hopper entry: Account: %d: LBID: %d Entry UUID: %s",
                usageRecord.getAccountId(), usageRecord.getLoadbalancer().getId(), usageRecord.getUuid()));
        generateAtomHopperAlertRecord(usageRecord, "AH-FAILED-ENTRY-"
                + usageRecord.getUuid(), buildMessage(body));

        if (isVerboseLog) {
            LOG.info(buildEntryLog(body, usageRecord, entrystring));
        }
    }

    private void logSuccess(String body, Usage usageRecord, String entrystring) {
        if (isVerboseLog) {
            LOG.info(String.format("Atom Hopper entry successfully pushed! : %s",
                    buildEntryLog(body, usageRecord, entrystring)));
        }
    }

    private String buildEntryLog(String body, Usage usageRecord, String entrystring) {
        return String.format("Atom Hopper Request Body for LB %s:  \n%s\n" +
                "\nACCOUNT: %d \n<ENTRY>: %s \n:</END ENTRY>",
                usageRecord.getLoadbalancer().getId(), body,
                usageRecord.getAccountId(), entrystring);
    }

    private String buildMessage(String body) {
        String message;
        if (body != null) {
            message = body.split("<message>")[1].split("</message>")[0];
        } else {
            message = "Unidentified Error processing Atom Hopper entry, " +
                    "please view logs and notify developer immediately!";
        }
        return message;
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

    private void generateSevereAlert(String alertName, String alertMessage) {
        //Severe alerts can be searched by aid/lbid = 1
        Alert alert = new Alert();
        alert.setAccountId(1);
        alert.setAlertType(Constants.AH_USAGE_EVENT_FAILURE);
        alert.setStatus(AlertStatus.UNACKNOWLEDGED);
        alert.setLoadbalancerId(1);
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
