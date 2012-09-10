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
import org.openstack.atlas.atom.pojo.EntryPojo;
import org.openstack.atlas.atom.util.AHUSLUtil;
import org.openstack.atlas.atom.util.LbaasUsageDataMap;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.repository.AccountUsageRepository;

import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.List;

public class AccountLoadBalancerAHUSLTask implements Runnable {
    private final Log LOG = LogFactory.getLog(AccountLoadBalancerAHUSLTask.class);
    private String token;

    public AccountLoadBalancerAHUSLTask() {
    }

    //Configuration
    private Configuration configuration = new AtomHopperConfiguration();
    private AHUSLAuthentication ahuslAuthentication;
    private AccountUsageRepository accountUsageRepository;

    private List<AccountUsage> accountUsages;

    private AHUSLClient client;

    public AccountLoadBalancerAHUSLTask(List<AccountUsage> accountUsages, AHUSLClient client, AccountUsageRepository accountUsageRepository) {
        this.accountUsages = accountUsages;
        this.client = client;
        this.accountUsageRepository = accountUsageRepository;
    }


    @Override
    public void run() {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Account AHUSL Task Started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        try {
            ahuslAuthentication = new AHUSLAuthentication();
            token = ahuslAuthentication.getToken(configuration.getString(AtomHopperConfigurationKeys.ahusl_auth_username)).getToken().getId();
            if (token != null) LOG.info("Token successfully retrieved...");

        } catch (Exception e) {
            LOG.error("There was an error retrieving token: " + e);
        }

        if (token != null) {
            try {

                for (AccountUsage asausage : accountUsages) {
                    if (asausage.isNeedsPushed()) {


                        EntryPojo entry = LbaasUsageDataMap.buildAccountUsageEntry(
                                asausage,
                                configuration,
                                configuration.getString(AtomHopperConfigurationKeys.ahusl_region));

                        ClientResponse response = null;

                        try {
//                        LOG.info(String.format("Start Uploading to the atomHopper service now..."));
                            response = client.postEntryWithToken(entry, token);
//                        LOG.info(String.format("Finished uploading to the atomHopper service..."));
                        } catch (ClientHandlerException che) {
                            LOG.warn("Could not post entry because client handler exception for account: " + asausage.getAccountId() + "Exception: " + AHUSLUtil.getStackTrace(che));
                        } catch (ConnectionPoolTimeoutException cpe) {
                            LOG.warn("Could not post entry because of limited connections for account: " + asausage.getAccountId() + "Exception: " + AHUSLUtil.getStackTrace(cpe));
                        }

                        //Notify usage if the record was uploaded or not...
                        if (response != null && response.getStatus() == 201) {
//                        LOG.debug("Updating needs_pushed: " + false + " for load balancer: " + usageRecord.getLoadbalancer().getId());
                            asausage.setNeedsPushed(false);
                            response.close();
                        } else if (response != null) {
                            LOG.error("There was an error pushing to the atom hopper service. status code: " + response.getStatus() + " for account: " + asausage.getAccountId());
                            String body = AHUSLUtil.processResponseBody(response);
                            LOG.info(String.format("body %s\n", body));
                            response.close();
                            asausage.setNeedsPushed(true);
                        } else {
                            LOG.error("The connection timed out, updating record for re-push for account: " + asausage.getAccountId());
                            asausage.setNeedsPushed(true);
                        }
                    }
                }

                LOG.debug("Batch updating: " + accountUsages.size() + " account usage rows in the database...");
                accountUsageRepository.batchUpdate(accountUsages);
//            LOG.info("Successfully batch updated: " + lbusages.size() + " usage rows in the database...");
            } catch (ConcurrentModificationException
                    cme) {
                System.out.printf("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(cme));
                LOG.warn(String.format("Warning: %s\n", AHUSLUtil.getExtendedStackTrace(cme)));
                LOG.warn(String.format("Job attempted to access account usage already being processed, continue processing next data set..."));
            } catch (Throwable
                    t) {
                System.out.printf("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t));
                LOG.error(String.format("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t)));
            }
        } else {
            LOG.error("Could not retrieve authentication token, no requests are being processed, please notify operations... ::AUTH FAILED ALERT::");
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        LOG.info(String.format("Account AHUSL Task Completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }
}
