package org.openstack.atlas.usage.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.jobs.AbstractJob;
import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.repository.AccountUsageRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;

@Component
public class AccountUsagePoller extends AbstractJob {
    private final Log LOG = LogFactory.getLog(AccountUsagePoller.class);

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private AccountUsageRepository accountUsageRepository;
    @Autowired
    private VirtualIpRepository virtualIpRepository;

    @Override
    public Log getLogger() {
        return LOG;
    }

    @Override
    public JobName getJobName() {
        return JobName.ACCOUNT_USAGE_POLLER;
    }

    @Override
    public void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    }

    @Override
    public void run() throws Exception {
        List<Integer> accountIds = loadBalancerRepository.getAllAccountIds();

        for (Integer accountId : accountIds) {
            LOG.debug(String.format("Creating account usage entry for account '%d'...", accountId));
            createAccountUsageEntry(accountId);
            LOG.debug(String.format("Account usage entry successfully created for account '%d'.", accountId));
        }
    }

    @Override
    public void cleanup() {
    }

    private void createAccountUsageEntry(Integer accountId) {
        AccountUsage usage = new AccountUsage();
        usage.setAccountId(accountId);
        usage.setNumLoadBalancers(loadBalancerRepository.getNumNonDeletedLoadBalancersForAccount(accountId));
        usage.setNumPublicVips(virtualIpRepository.getNumUniqueVipsForAccount(accountId, VirtualIpType.PUBLIC));
        usage.setNumServicenetVips(virtualIpRepository.getNumUniqueVipsForAccount(accountId, VirtualIpType.SERVICENET));
        usage.setStartTime(Calendar.getInstance());
        accountUsageRepository.save(usage);
    }

}
