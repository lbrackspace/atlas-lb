package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.common.crypto.HashUtil;
import org.openstack.atlas.service.domain.common.Constants;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.*;
import org.openstack.atlas.service.domain.repository.ClusterRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpv6Repository;
import org.openstack.atlas.service.domain.service.VirtualIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class VirtualIpServiceImpl implements VirtualIpService {
    private final Log LOG = LogFactory.getLog(VirtualIpServiceImpl.class);

    @Autowired
    private VirtualIpRepository virtualIpRepository;

    @Autowired
    private VirtualIpv6Repository virtualIpv6Repository;

    @Autowired
    private ClusterRepository clusterRepository;

    @Override
    public LoadBalancer assignVIpsToLoadBalancer(LoadBalancer loadBalancer) throws PersistenceServiceException {
        if (!loadBalancer.getLoadBalancerJoinVipSet().isEmpty()) {
            Set<LoadBalancerJoinVip> newVipConfig = new HashSet<LoadBalancerJoinVip>();
            List<VirtualIp> vipsOnAccount = virtualIpRepository.getVipsByAccountId(loadBalancer.getAccountId());
            for (LoadBalancerJoinVip loadBalancerJoinVip : loadBalancer.getLoadBalancerJoinVipSet()) {
                if (loadBalancerJoinVip.getVirtualIp().getId() == null) {
                    // Add a new vip to set
                    VirtualIp newVip = allocateIpv4VirtualIp(loadBalancerJoinVip.getVirtualIp(), loadBalancer.getHost().getCluster());
                    LoadBalancerJoinVip newJoinRecord = new LoadBalancerJoinVip();
                    newJoinRecord.setVirtualIp(newVip);
                    newVipConfig.add(newJoinRecord);
                } else {
                    // Add shared vip to set
                    newVipConfig.addAll(getSharedIpv4Vips(loadBalancerJoinVip.getVirtualIp(), vipsOnAccount, loadBalancer.getPort()));
                }
            }
            loadBalancer.setLoadBalancerJoinVipSet(newVipConfig);
        }

        if (!loadBalancer.getLoadBalancerJoinVip6Set().isEmpty()) {
            Set<LoadBalancerJoinVip6> newVip6Config = new HashSet<LoadBalancerJoinVip6>();
            List<VirtualIpv6> vips6OnAccount = virtualIpv6Repository.getVips6ByAccountId(loadBalancer.getAccountId());
            Set<LoadBalancerJoinVip6> loadBalancerJoinVip6SetConfig = loadBalancer.getLoadBalancerJoinVip6Set();
            loadBalancer.setLoadBalancerJoinVip6Set(null);
            for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : loadBalancerJoinVip6SetConfig) {
                if (loadBalancerJoinVip6.getVirtualIp().getId() == null) {
                    VirtualIpv6 ipv6 = allocateIpv6VirtualIp(loadBalancer);
                    LoadBalancerJoinVip6 jbjv6 = new LoadBalancerJoinVip6();
                    jbjv6.setVirtualIp(ipv6);
                    newVip6Config.add(jbjv6);
                } else {
                    //share ipv6 vip here..
                    newVip6Config.addAll(getSharedIpv6Vips(loadBalancerJoinVip6.getVirtualIp(), vips6OnAccount, loadBalancer.getPort()));
                }
                loadBalancer.setLoadBalancerJoinVip6Set(newVip6Config);
            }
        }
        return loadBalancer;
    }

    @Transactional
    public void addAccountRecord(Integer accountId) throws NoSuchAlgorithmException {
        Set<Integer> accountsInAccount = new HashSet<Integer>(virtualIpv6Repository.getAccountIdsAlreadyShaHashed());

        if (accountsInAccount.contains(accountId)) return;

        Account account = new Account();
        String accountIdStr = String.format("%d", accountId);
        account.setId(accountId);
        account.setSha1SumForIpv6(HashUtil.sha1sumHex(accountIdStr.getBytes(), 0, 4));
        try {
            virtualIpRepository.persist(account);
        } catch (Exception e) {
            LOG.warn("High concurrency detected. Ignoring...");
        }
    }

    public boolean isIpv4VipPortCombinationInUse(VirtualIp virtualIp, Integer loadBalancerPort) {
        return virtualIpRepository.getPorts(virtualIp.getId()).containsKey(loadBalancerPort);
    }

    public boolean isIpv6VipPortCombinationInUse(VirtualIpv6 virtualIp, Integer loadBalancerPort) {
        return virtualIpv6Repository.getPorts(virtualIp.getId()).containsKey(loadBalancerPort);
    }

    @Transactional
    public VirtualIpv6 allocateIpv6VirtualIp(LoadBalancer loadBalancer) throws EntityNotFoundException {
        // Acquire lock on account row due to concurrency issue
        virtualIpv6Repository.getLockedAccountRecord(loadBalancer.getAccountId());

        VirtualIpv6 v6;
        Integer clusterId = loadBalancer.getHost().getCluster().getId();
        Integer accountId = loadBalancer.getAccountId();
        Integer vipOctets = virtualIpv6Repository.getNextVipOctet(accountId);
        v6 = newVirtualIpv6(clusterId, accountId, vipOctets);
        virtualIpRepository.persist(v6);
        return v6;
    }

    public VirtualIpv6 newVirtualIpv6(Integer clusterId, Integer accountId, Integer vipOctets) throws EntityNotFoundException {
        VirtualIpv6 v6 = new VirtualIpv6();
        Cluster c = clusterRepository.getById(clusterId);
        v6.setCluster(c);
        v6.setAccountId(accountId);
        v6.setVipOctets(vipOctets);
        return v6;
    }

    private Set<LoadBalancerJoinVip> getSharedIpv4Vips(VirtualIp vipConfig, List<VirtualIp> vipsOnAccount, Integer lbPort) throws AccountMismatchException, UniqueLbPortViolationException {
        Set<LoadBalancerJoinVip> sharedVips = new HashSet<LoadBalancerJoinVip>();
        boolean belongsToProperAccount = false;

        // Verify this is a valid virtual ip to share
        for (VirtualIp vipOnAccount : vipsOnAccount) {
            if (vipOnAccount.getId().equals(vipConfig.getId())) {
                if (this.isIpv4VipPortCombinationInUse(vipOnAccount, lbPort)) {
                    throw new UniqueLbPortViolationException("Another load balancer is currently using the requested port with the shared virtual ip.");
                }
                belongsToProperAccount = true;
                LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
                loadBalancerJoinVip.setVirtualIp(vipOnAccount);
                sharedVips.add(loadBalancerJoinVip);
            }
        }

        if (!belongsToProperAccount) {
            throw new AccountMismatchException("Invalid requesting account for the shared virtual ip.");
        }
        return sharedVips;
    }

    private Set<LoadBalancerJoinVip6> getSharedIpv6Vips(VirtualIpv6 vipConfig, List<VirtualIpv6> vipsOnAccount, Integer lbPort) throws AccountMismatchException, UniqueLbPortViolationException {
        Set<LoadBalancerJoinVip6> sharedVips = new HashSet<LoadBalancerJoinVip6>();
        boolean belongsToProperAccount = false;

        // Verify this is a valid virtual ip to share
        for (VirtualIpv6 vipOnAccount : vipsOnAccount) {
            if (vipOnAccount.getId().equals(vipConfig.getId())) {
                if (this.isIpv6VipPortCombinationInUse(vipOnAccount, lbPort)) {
                    throw new UniqueLbPortViolationException("Another load balancer is currently using the requested port with the shared virtual ip.");
                }
                belongsToProperAccount = true;
                LoadBalancerJoinVip6 loadBalancerJoinVip6 = new LoadBalancerJoinVip6();
                loadBalancerJoinVip6.setVirtualIp(vipOnAccount);
                sharedVips.add(loadBalancerJoinVip6);
            }
        }

        if (!belongsToProperAccount) {
            throw new AccountMismatchException("Invalid requesting account for the shared virtual ip.");
        }
        return sharedVips;
    }

    @Transactional
    public VirtualIp allocateIpv4VirtualIp(VirtualIp vipConfig, Cluster cluster) throws OutOfVipsException {
        Calendar timeConstraintForVipReuse = Calendar.getInstance();
        timeConstraintForVipReuse.add(Calendar.DATE, -Constants.NUM_DAYS_BEFORE_VIP_REUSE);

        if (vipConfig.getVipType() == null) {
            vipConfig.setVipType(VirtualIpType.PUBLIC);
        }

        try {
            return virtualIpRepository.allocateIpv4VipBeforeDate(cluster, timeConstraintForVipReuse, vipConfig.getVipType());
        } catch (OutOfVipsException e) {
            LOG.warn(String.format("Out of IPv4 virtual ips that were de-allocated before '%s'.", timeConstraintForVipReuse.getTime()));
            try {
                return virtualIpRepository.allocateIpv4VipAfterDate(cluster, timeConstraintForVipReuse, vipConfig.getVipType());
            } catch (OutOfVipsException e2) {
                e2.printStackTrace();
                throw e2;
            }
        }
    }
}
