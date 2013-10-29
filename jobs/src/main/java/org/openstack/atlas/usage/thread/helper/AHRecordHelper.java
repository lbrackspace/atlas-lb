package org.openstack.atlas.usage.thread.helper;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.openstack.atlas.restclients.atomhopper.AtomHopperClient;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.entities.*;
import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.events.repository.LoadBalancerEventRepository;
import org.openstack.atlas.service.domain.util.Constants;
import org.w3._2005.atom.UsageEntry;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;

import static org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil.getExtendedStackTrace;
import static org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil.getStackTrace;

public class AHRecordHelper {
    private final Log LOG = LogFactory.getLog(AHRecordHelper.class);

    protected AtomHopperClient client;

    protected AlertRepository alertRepository;
    protected LoadBalancerEventRepository loadBalancerEventRepository;

    protected List<Usage> failedRecords;
    protected boolean isVerboseLog;

    public AHRecordHelper(boolean isVerboseLog, AtomHopperClient client,
                          LoadBalancerEventRepository loadBalancerEventRepository,
                          AlertRepository alertRepository) {
        this.isVerboseLog = isVerboseLog;
        this.client = client;
        this.loadBalancerEventRepository = loadBalancerEventRepository;
        this.alertRepository = alertRepository;

        failedRecords = new ArrayList<Usage>();
    }

    public List<Usage> handleUsageRecord(Usage usageRecord, String authToken, Map<Object, Object> entryMap) {
        String responseBody = null;
        String entryString = null;
        ClientResponse response = null;

        try {
            if (usageRecord.isNeedsPushed()) {
                entryString = (String) entryMap.get("entrystring");
                UsageEntry entryObject = (UsageEntry) entryMap.get("entryobject");

                response = client.postEntryWithToken(entryString, authToken);
                if (response != null) {
                    processResponse(response, authToken, usageRecord, entryObject, entryString);
                } else {
                    LOG.error(String.format("Alert! :: Client Response is Null for LBID: %d, UUID: %s",
                            usageRecord.getLoadbalancer().getId(), usageRecord.getUuid()));
                    logAndAlert(null, usageRecord, entryString);
                    failedRecords.add(usageRecord);
                    return failedRecords;
                }
                response.close();
            }

        } catch (ClientHandlerException che) {
            LOG.error(String.format("Could not post entry because " +
                    "client handler exception for load balancer: %d :\n: Exception: %s ",
                    usageRecord.getLoadbalancer().getId(), getStackTrace(che)));
            logAndAlert(getStackTrace(che), usageRecord, entryString);
            failedRecords.add(usageRecord);
        } catch (ConnectionPoolTimeoutException cpe) {
            LOG.error(String.format("Could not post entry because " +
                    "of limited connections for load balancer: %d :\n: Exception: %s ",
                    usageRecord.getLoadbalancer().getId(), getStackTrace(cpe)));
            logAndAlert(getStackTrace(cpe), usageRecord, entryString);
            failedRecords.add(usageRecord);
        } catch (ConcurrentModificationException cme) {
            LOG.warn(String.format("Warning: %s\n", getExtendedStackTrace(cme)));
            LOG.warn(String.format("Job attempted to access usage already being processed, " +
                    "continue processing next data set..."));
        } catch (Exception t) {
            LOG.error(String.format("Exception during Atom-Hopper processing: %s\n", getExtendedStackTrace(t)));
            generateSevereAlert("Severe Failure processing Atom Hopper requests: ", getExtendedStackTrace(t));
            generateServiceEventRecord(usageRecord, entryString, buildMessage(responseBody));
            failedRecords.add(usageRecord);
        }
        return failedRecords;
    }

    protected void processResponse(ClientResponse response, String authToken, Usage usageRecord,
                                   UsageEntry entryobject, String entrystring) throws Exception {

        //Set numAttempts and the UUID before processing so its saved if failure occurs.
        usageRecord.setNumAttempts(usageRecord.getNumAttempts() + 1);
        usageRecord.setUuid(entryobject.getContent().getEvent().getId());

        int status = response.getStatus();
        String body = response.getEntity(String.class);

        if (status == 201) {
            usageRecord.setNeedsPushed(false);
            usageRecord.setNumAttempts(0);
            usageRecord.setCorrected(false);
            logSuccess(body, usageRecord, entrystring);

        } else if (status == 409) {
            boolean isDupe = isDuplicateEntry(authToken, usageRecord);
            if (isDupe) {
                LOG.warn(String.format("Entry UUID: %s is a duplicate record and will be removed from the list. " +
                        "Alert created to notify of potential issues... ", usageRecord.getUuid()));
                usageRecord.setNeedsPushed(false);
            } else {
                LOG.warn(String.format("Entry UUID: %s seems to NOT be a duplicate " +
                        "record and will NOT be removed from the list. " +
                        "Alert created to notify of potential issues... ", usageRecord.getUuid()));
                usageRecord.setNeedsPushed(true);
            }
            failedRecords.add(usageRecord);
            generateServiceEventRecord(usageRecord, entrystring, buildMessage(body));
            logAndAlert(body, usageRecord, entrystring);

        } else if (status == 400) {
            usageRecord.setNeedsPushed(true);
            failedRecords.add(usageRecord);
            generateServiceEventRecord(usageRecord, entrystring, buildMessage(body));
            logAndAlert(body, usageRecord, entrystring);

        } else {
            LOG.error(String.format("Error processing entry in Atom Hopper service occurred, " +
                    "updating record for re-push for: Account: %d LBID: %d UUID: %s",
                    usageRecord.getAccountId(), usageRecord.getLoadbalancer().getId(), usageRecord.getUuid()));
            usageRecord.setNeedsPushed(true);
            failedRecords.add(usageRecord);
            logAndAlert(body, usageRecord, entrystring);
        }
    }

    protected boolean isDuplicateEntry(String token, Usage usageRecord) {
        try {
            ClientResponse response = client.getEntry(token, usageRecord.getUuid());
            UsageEntry entry = response.getEntity(UsageEntry.class);
            if (response.getStatus() == 200) {
                if (!(entry.getContent().getEvent().getId().equals(usageRecord.getUuid()))) {
                    return false;
                }
            }
        } catch (Exception e) {
            LOG.warn(String.format("Could not verify duplicate entry for UUID: %s, attempt to re-push the record...",
                    usageRecord.getUuid()));
            return false;
        }
        return true;
    }

    protected void logAndAlert(String body, Usage usageRecord, String entrystring) {
        LOG.info(String.format("Creating alert for Atom hopper entry: Account: %d: LBID: %d Entry UUID: %s",
                usageRecord.getAccountId(), usageRecord.getLoadbalancer().getId(), usageRecord.getUuid()));
        generateAtomHopperAlertRecord(usageRecord, "AH-FAILED-ENTRY-"
                + usageRecord.getUuid(), buildMessage(body));

        //LOG all failures regardless of log mode...
        LOG.info(buildEntryLog(body, usageRecord, entrystring));
    }

    protected void logSuccess(String body, Usage usageRecord, String entrystring) {
        if (isVerboseLog) {
            LOG.info(String.format("Atom Hopper entry successfully pushed! : %s",
                    buildEntryLog(body, usageRecord, entrystring)));
        }
    }

    protected String buildEntryLog(String body, Usage usageRecord, String entrystring) {
        return String.format("Atom Hopper Request Body for LB %s:  \n%s\n" +
                "\nACCOUNT: %d \n<ENTRY>: %s \n:</END ENTRY>",
                usageRecord.getLoadbalancer().getId(), body,
                usageRecord.getAccountId(), entrystring);
    }

    protected String buildMessage(String body) {
        String message;
        if (body != null) {
            if (body.contains("<message>")) {
                message = body.split("<message>")[1].split("</message>")[0];
            } else {
                message = body;
            }
        } else {
            message = "Unidentified Error processing Atom Hopper entry, " +
                    "please view logs and notify developer immediately!";
        }
        return message;
    }

    public void generateServiceEventRecord(Usage usageRecord, String entrystring, String responseMessage) {
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

    public void generateAtomHopperAlertRecord(Usage usageRecord, String alertName, String alertMessage) {
        Alert alert = new Alert();
        alert.setAccountId(usageRecord.getAccountId());
        alert.setAlertType(Constants.AH_USAGE_EVENT_FAILURE);
        alert.setStatus(AlertStatus.UNACKNOWLEDGED);
        alert.setLoadbalancerId(usageRecord.getLoadbalancer().getId());
        alert.setMessageName(alertName);
        alert.setMessage(alertMessage);
        alertRepository.save(alert);
    }

    public void generateSevereAlert(String alertName, String alertMessage) {
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

    public boolean isVerboseLog() {
        return isVerboseLog;
    }

    public void setVerboseLog(boolean verboseLog) {
        isVerboseLog = verboseLog;
    }

    public List<Usage> getFailedRecords() {
        return failedRecords;
    }
}