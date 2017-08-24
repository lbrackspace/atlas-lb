package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.SslCipherProfile;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.SslCipherProfileRepository;
import org.openstack.atlas.service.domain.repository.SslTerminationRepository;
import org.openstack.atlas.service.domain.services.helpers.AlertHelper;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CipherSyncJob extends AbstractJob {
    private final Log LOG = LogFactory.getLog(CipherSyncJob.class);

    //TODO pull from constants
    String defaultProfile = "default";

    List<LoadBalancer> loadbalancersToUpdate = new ArrayList<LoadBalancer>();

    @Autowired
    private SslCipherProfileRepository sslCipherProfileRepository;
    @Autowired
    private SslTerminationRepository sslTerminationRepository;
    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;

    @Override
    public Log getLogger() {
        return LOG;
    }

    @Override
    public JobName getJobName() {
        return JobName.DAILY_DELETION_JOB;
    }

    @Override
    public void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    }

    @Override
    public void run() throws Exception {
        try {
            List<SslCipherProfile> cps = retrienveCipherProfiles();
            List<SslTermination> sts = retrieveSslTerminations();
            for (SslTermination st : sts) {
                if (st.getCipherProfile().getName() != defaultProfile) {
                    compareCiphers(cps, st);
                }
            }
            // We've determined which profiles need updating. Sync the related load baalncers.
            for (LoadBalancer lb : loadbalancersToUpdate) {
                // TODO, we may want to maintain similar logic from sync listener.
                reverseProxyLoadBalancerService.syncLoadBalancer(lb);
            }
        } catch (Exception e) {
            Alert alert = AlertHelper.createAlert(null, null, e, AlertType.API_FAILURE.name(), e.getMessage());
            alertRepository.save(alert);
            throw e;
        }
    }

    @Override
    public void cleanup() {
    }

    private void  compareCiphers(List<SslCipherProfile> domainCipherProfiles, SslTermination sslTermination) {
        for(SslCipherProfile cp : domainCipherProfiles) {
            if(cp.getName() != defaultProfile && cp.getName() == sslTermination.getCipherProfile().getName()) {
                if(cp.getCiphers() != sslTermination.getCipherList()) {
                    loadbalancersToUpdate.add(sslTermination.getLoadbalancer());
                }
            }
        }
    }

    private List<SslCipherProfile> retrienveCipherProfiles() throws EntityNotFoundException {
        LOG.info("Gathering Cipher Profiles.");
        return sslCipherProfileRepository.fetchAllProfiles();
    }

    private List<SslTermination> retrieveSslTerminations() {
        LOG.info("Gathering SSL Termination for Cipher Profile comparison.");
        return sslTerminationRepository.getAll();
    }
}
