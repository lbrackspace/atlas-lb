package org.openstack.atlas.usage.thread;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.atomhopper.exception.AtomHopperMappingException;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.restclients.atomhopper.AtomHopperClient;
import org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfiguration;
import org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil;
import org.openstack.atlas.restclients.auth.IdentityAuthClient;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.events.repository.LoadBalancerEventRepository;
import org.openstack.atlas.usage.thread.helper.AHRecordHelper;

import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;

import static org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil.getExtendedStackTrace;

public abstract class AbstractAtomHopperThread implements Runnable {
    private final Log LOG = LogFactory.getLog(AbstractAtomHopperThread.class);
    private Configuration configuration = new AtomHopperConfiguration();

    protected List<Usage> usages;
    protected AtomHopperClient client;
    protected IdentityAuthClient identityAuthClient;

    protected AlertRepository alertRepository;
    protected LoadBalancerEventRepository loadBalancerEventRepository;

    protected List<Usage> failedRecords;

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

        AHRecordHelper ahelper = null;
        try {
            String authToken = identityAuthClient.getAuthToken();
            ahelper = new AHRecordHelper(configuration.getString(AtomHopperConfigurationKeys
                    .ahusl_log_requests).equals("ENABLED"), client,
                    loadBalancerEventRepository, alertRepository);

            for (Usage usageRecord : usages) {
                Map<Object, Object> entryMap = generateAtomHopperEntry(usageRecord);
                failedRecords = ahelper.handleUsageRecord(usageRecord, authToken, entryMap);
            }

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
            ahelper.generateSevereAlert("Severe Failure processing Atom Hopper requests: ", getExtendedStackTrace(t));
        }

        Double elapsedMins = ((AtomHopperUtil.getNow()
                .getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        LOG.info(String.format("Load Balancer Atom Hopper USL Task: %s Completed at '%s' (Total Time: %f mins)",
                getThreadName(), AtomHopperUtil.getNow().getTime().toString(), elapsedMins));
        LOG.debug(String.format("Load Balancer Atom Hopper USL Task: %s Failed tasks count: %d out of %d",
                getThreadName(), failedRecords.size(), usages.size()));
    }
}
