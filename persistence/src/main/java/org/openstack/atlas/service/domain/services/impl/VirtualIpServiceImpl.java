package org.openstack.atlas.service.domain.services.impl;

import com.sun.jersey.api.client.ClientResponse;
import org.openstack.atlas.service.domain.deadlock.DeadLockRetry;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.services.AccountLimitService;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.VirtualIpService;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.service.domain.util.StringUtilities;
import org.openstack.atlas.util.common.ListUtil;
import org.openstack.atlas.util.crypto.HashUtil;
import org.openstack.atlas.util.ip.IPv6;
import org.openstack.atlas.util.ip.IPv6Cidr;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import org.openstack.atlas.restclients.dns.StaticDNSClientUtils;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.events.entities.LoadBalancerServiceEvent;
import org.openstack.atlas.service.domain.services.helpers.RdnsHelper;
import org.openstack.atlas.util.debug.Debug;

@Service
public class VirtualIpServiceImpl extends BaseService implements VirtualIpService {
    private final Log LOG = LogFactory.getLog(VirtualIpServiceImpl.class);
    private static final int SB_INIT_SIZE = 1024 * 8;
    public static final String DEL_PTR_FAILED = "Delete PTR on Virtual IP Fail";
    public static final String DEL_PTR_PASSED = "Delete PTR on Virtual IP Passed";

    @Autowired
    private AccountLimitService accountLimitService;

    @Autowired
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;

    @Override
    public VirtualIp get(Integer id) throws EntityNotFoundException {
        return virtualIpRepository.getById(id);
    }

    @Override
    public Set<VirtualIp> get(Integer accountId, Integer loadBalancerId, Integer offset, Integer limit, Integer marker) throws EntityNotFoundException, DeletedStatusException {
        return loadBalancerRepository.getVipsByLbId(loadBalancerId, offset, limit, marker);
    }

    @Override
    public Set<VirtualIpv6> getVirtualIpv6ByAccountIdLoadBalancerId(Integer aid, Integer lid) throws EntityNotFoundException {
        LoadBalancer lb = loadBalancerRepository.getByIdAndAccountId(lid, aid);
        return getVirtualIpv6ByLoadBalancerId(lid);
    }

    @Override
    public Set<VirtualIpv6> getVirtualIpv6ByLoadBalancerId(Integer lid) throws EntityNotFoundException {
        return virtualIpv6Repository.getVirtualIpv6sByLoadBalancerId(lid);
    }

    @Override
    public List<org.openstack.atlas.service.domain.entities.LoadBalancer> getLoadBalancerByVipId(Integer vipId) {
        return virtualIpRepository.getLoadBalancersByVipId(vipId);
    }

    @Override
    public List<org.openstack.atlas.service.domain.entities.LoadBalancer> getLoadBalancerByVipAddress(String address) {
        return virtualIpRepository.getLoadBalancerByVipAddress(address);
    }

    @Override
    public List<LoadBalancer> getLoadBalancerByVip6Address(String address) throws IPStringConversionException, EntityNotFoundException {
        List<Integer> accountIds;
        List<LoadBalancer> out = new ArrayList<LoadBalancer>();
        Integer clusterId = null;
        Integer accountId = null;
        Integer vipOctets = null;
        VirtualIpv6 vip6;

        IPv6 ipv6;

        boolean matchfound;
        List<LoadBalancer> loadbalancers;
        ipv6 = new IPv6();


        loadbalancers = new ArrayList<LoadBalancer>();
        ipv6 = new IPv6(address);
        ipv6.expand();
        try { // Find the vip octet
            vipOctets = ipv6.getVipOctets();
        } catch (IPStringConversionException ex) {
            vipOctets = null;
        }

        String[] strSplit = address.split(":");
        String searchSha = String.format("%s%s", strSplit[4], strSplit[5]);
        IPv6Cidr searchCidr = new IPv6Cidr(String.format("%s:%s:%s:%s::/64",
                strSplit[0], strSplit[1], strSplit[2], strSplit[3]));
        for (Cluster cl : clusterRepository.getAll()) {
            try {
                IPv6Cidr foundCidr = new IPv6Cidr(cl.getClusterIpv6Cidr());
                try {
                    matchfound = foundCidr.matches(searchCidr);
                    if (matchfound) {  // This must be our cluster since the cidrs matched.
                        clusterId = cl.getId();
                        break;
                    }
                } catch (IpTypeMissMatchException ex) {
                    continue; // This one can't be it. Coulden't even match the IP type :(
                }

            } catch (IPStringConversionException ex) {
                continue; // This one is an even bigger fail pfft.
            }
        } // Hopefully we recovered the cluster from the Vip otherwise its null;

        accountIds = virtualIpRepository.getAccountBySha1Sum(searchSha);
        if (accountIds.size() == 1) {
            accountId = accountIds.get(0);
        }


        vip6 = virtualIpRepository.getVirtualIpv6BytClusterAccountOctet(clusterId, accountId, vipOctets);
        return getLoadBalancerByVip6Id(vip6.getId());
    }

    @Override
    public List<LoadBalancer> getLoadBalancerByVip6Id(Integer vip6Id) {
        return virtualIpRepository.getLoadBalancerByVip6Address(vip6Id);
    }

    @Override
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

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, UnprocessableEntityException.class, ImmutableEntityException.class, BadRequestException.class, OutOfVipsException.class, UniqueLbPortViolationException.class, AccountMismatchException.class})
    public VirtualIp addVirtualIpToLoadBalancer(VirtualIp vipConfig, LoadBalancer lb, Ticket ticket) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException, BadRequestException, OutOfVipsException, UniqueLbPortViolationException, AccountMismatchException {
        VirtualIp vipToAdd;
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(lb.getId());

        if (vipConfig.getId() != null) {
            vipToAdd = virtualIpRepository.getById(vipConfig.getId());

            if (!vipTypeMatchesTypeForLoadBalancer(vipToAdd.getVipType(), dbLoadBalancer)) {
                String message = StringHelper.mismatchingVipType(vipConfig.getVipType());
                LOG.debug(message);
                throw new BadRequestException(message);
            }

            if (!doesVipBelongToAccount(vipToAdd, dbLoadBalancer.getAccountId())) {
                String message = "The requested virtual ip does not belong to this account";
                LOG.debug(message);
                throw new AccountMismatchException(message);
            }

            if (!isVipConfiguredOnHost(vipToAdd, dbLoadBalancer.getHost())) {
                String message = "The virtual ip being added has a different host than the host for this loadbalancer";
                LOG.debug(message);
                throw new BadRequestException(message);
            }

            if (isVipPortCombinationInUse(vipToAdd, dbLoadBalancer.getPort())) {
                String message = "The virtual ip and load balancer port combination is currently in use";
                LOG.debug(message);
                throw new UniqueLbPortViolationException(message);
            }
        } else {
            if (!vipTypeMatchesTypeForLoadBalancer(vipConfig.getVipType(), dbLoadBalancer)) {
                String message = StringHelper.mismatchingVipType(vipConfig.getVipType());
                LOG.debug(message);
                throw new BadRequestException(message);
            }

            vipToAdd = allocateIpv4VirtualIp(vipConfig, dbLoadBalancer.getHost().getCluster());
        }

        if (!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }

        loadBalancerRepository.createTicket(dbLoadBalancer, ticket);

        dbLoadBalancer.getLoadBalancerJoinVipSet().add(new LoadBalancerJoinVip(dbLoadBalancer.getPort(), dbLoadBalancer, vipToAdd));
        loadBalancerRepository.update(dbLoadBalancer);
        return vipToAdd;
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, UnprocessableEntityException.class, ImmutableEntityException.class, BadRequestException.class, OutOfVipsException.class, UniqueLbPortViolationException.class, AccountMismatchException.class, LimitReachedException.class})
    public VirtualIpv6 addIpv6VirtualIpToLoadBalancer(VirtualIpv6 vipConfig, LoadBalancer lb) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException, BadRequestException, OutOfVipsException, UniqueLbPortViolationException, AccountMismatchException, LimitReachedException {

        VirtualIpv6 vipToAdd;
        LoadBalancer dlb = loadBalancerRepository.getByIdAndAccountId(lb.getId(), lb.getAccountId());

        Integer ipv6Limit = accountLimitService.getLimit(dlb.getAccountId(), AccountLimitType.IPV6_LIMIT);
        if (dlb.getLoadBalancerJoinVip6Set().size() >= ipv6Limit) {
            throw new LimitReachedException(String.format("Your load balancer cannot have more than %d IPv6 virtual ips.", ipv6Limit));
        }

        if (vipConfig.getId() != null) {
            try {
                vipToAdd = virtualIpv6Repository.getById(vipConfig.getId());
            } catch (EntityNotFoundException enfe) {
                LOG.debug(String.format("The id specified: %s could not be found. This is a request to a IPV4 virtual ip id. Or it is indeed a non-existent virutal ip. Account: %s. LoadBalancer: %s. %s", vipConfig.getId(), lb.getAccountId(), lb.getId(), enfe.getMessage()));
                throw new EntityNotFoundException("Please provide a valid IPV6 virtual ip.");
            }

            if (!vipTypeMatchesTypeForLoadBalancer(VirtualIpType.PUBLIC, dlb)) {
                String message = StringHelper.mismatchingVipType(VirtualIpType.PUBLIC);
                LOG.debug(message);
                throw new BadRequestException(message);
            }

            if (!isIpv6VipConfiguredOnHost(vipToAdd, dlb.getHost())) {
                String message = "The virtual ip being added has a different host than the host for this loadbalancer";
                LOG.debug(message);
                throw new BadRequestException(message);
            }

            if (isIpv6VipPortCombinationInUse(vipToAdd, dlb.getPort())) {
                String message = "The virtual ip and load balancer port combination is currently in use";
                LOG.debug(message);
                throw new UniqueLbPortViolationException(message);
            }
        } else {
            if (!vipTypeMatchesTypeForLoadBalancer(VirtualIpType.PUBLIC, dlb)) {
                String message = StringHelper.mismatchingVipType(VirtualIpType.PUBLIC);
                LOG.debug(message);
                throw new BadRequestException(message);
            }

            vipToAdd = allocateIpv6VirtualIp(dlb);
        }

        if (!loadBalancerRepository.testAndSetStatus(dlb.getAccountId(), dlb.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dlb);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        } else {
            //Set status record
            loadBalancerStatusHistoryService.save(dlb.getAccountId(), dlb.getId(), LoadBalancerStatus.PENDING_UPDATE);
        }

        LoadBalancerJoinVip6 jv = new LoadBalancerJoinVip6(dlb.getPort(), dlb, vipToAdd);
        virtualIpRepository.persist(jv);
        return vipToAdd;
    }

    @Override
    @Transactional
    public boolean isVipAllocatedToMultipleLoadBalancers(VirtualIp virtualIp) {
        final int MIN_NUM_LBS_FOR_VIP = 1;
        Long numLoadBalancersAttachedToVip = virtualIpRepository.getNumLoadBalancersAttachedToVip(virtualIp);
        LOG.debug(String.format("%d load balancer(s) currently using vip '%d'", numLoadBalancersAttachedToVip, virtualIp.getId()));
        return numLoadBalancersAttachedToVip > MIN_NUM_LBS_FOR_VIP;
    }

    @Override
    @Transactional
    public boolean isVipAllocatedToAnyLoadBalancer(VirtualIp virtualIp) {
        Long numLoadBalancersAttachedToVip = virtualIpRepository.getNumLoadBalancersAttachedToVip(virtualIp);
        LOG.debug(String.format("%d load balancer(s) currently using vip '%d'", numLoadBalancersAttachedToVip, virtualIp.getId()));
        return numLoadBalancersAttachedToVip > 0;
    }

    @Override
    @Transactional
    public boolean isVipAllocatedToAnotherLoadBalancer(LoadBalancer lb, VirtualIp virtualIp) {
        List<LoadBalancerJoinVip> joinRecords = virtualIpRepository.getJoinRecordsForVip(virtualIp);

        for (LoadBalancerJoinVip joinRecord : joinRecords) {
            if (!joinRecord.getLoadBalancer().getId().equals(lb.getId())) {
                LOG.debug(String.format("Virtual ip '%d' is used by a load balancer other than load balancer '%d'.", virtualIp.getId(), lb.getId()));
                return true;
            }
        }

        return false;
    }

    @Override
    @Transactional
    public boolean isIpv6VipAllocatedToAnotherLoadBalancer(LoadBalancer lb, VirtualIpv6 virtualIp) {
        List<LoadBalancerJoinVip6> joinRecords = virtualIpv6Repository.getJoinRecordsForVip(virtualIp);

        for (LoadBalancerJoinVip6 joinRecord : joinRecords) {
            if (!joinRecord.getLoadBalancer().getId().equals(lb.getId())) {
                LOG.debug(String.format("IPv6 virtual ip '%d' is used by a load balancer other than load balancer '%d'.", virtualIp.getId(), lb.getId()));
                return true;
            }
        }

        return false;
    }

    @Override
    @Transactional
    public void removeAllVipsFromLoadBalancer(LoadBalancer lb) {
        for (LoadBalancerJoinVip loadBalancerJoinVip : lb.getLoadBalancerJoinVipSet()) {
            virtualIpRepository.removeJoinRecord(loadBalancerJoinVip);
            reclaimVirtualIp(lb, loadBalancerJoinVip.getVirtualIp());
        }

        lb.getLoadBalancerJoinVipSet().clear();

        for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : lb.getLoadBalancerJoinVip6Set()) {
            virtualIpv6Repository.removeJoinRecord(loadBalancerJoinVip6);
            reclaimIpv6VirtualIp(lb, loadBalancerJoinVip6.getVirtualIp());
        }

        lb.getLoadBalancerJoinVip6Set().clear();
    }

    @Override
    @Transactional
    public void removeVipFromLoadBalancer(LoadBalancer lb, Integer vipId) {
        for (LoadBalancerJoinVip loadBalancerJoinVip : lb.getLoadBalancerJoinVipSet()) {
            if (loadBalancerJoinVip.getVirtualIp().getId().equals(vipId)) {
                virtualIpRepository.removeJoinRecord(loadBalancerJoinVip);
                reclaimVirtualIp(lb, loadBalancerJoinVip.getVirtualIp());
                lb.getLoadBalancerJoinVipSet().remove(loadBalancerJoinVip);
                break;
            }
        }

        for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : lb.getLoadBalancerJoinVip6Set()) {
            if (loadBalancerJoinVip6.getVirtualIp().getId().equals(vipId)) {
                virtualIpv6Repository.removeJoinRecord(loadBalancerJoinVip6);
                reclaimIpv6VirtualIp(lb, loadBalancerJoinVip6.getVirtualIp());
                lb.getLoadBalancerJoinVip6Set().remove(loadBalancerJoinVip6);
                break;
            }
        }
    }

    @Override
    @Transactional
    public void removeVipsFromLoadBalancer(LoadBalancer lb, List<Integer> vipIds) {
        for (Integer vipIdToDelete : vipIds) {
            removeVipFromLoadBalancer(lb, vipIdToDelete);
        }
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class, BadRequestException.class})
    public void prepareForVirtualIpDeletion(LoadBalancer lb, Integer vipId) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        List<Integer> vipIdsToDelete = new ArrayList<Integer>();
        vipIdsToDelete.add(vipId);
        prepareForVirtualIpsDeletion(lb.getAccountId(), lb.getId(), vipIdsToDelete);
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class, BadRequestException.class})
    public void prepareForVirtualIpsDeletion(Integer accountId, Integer loadbalancerId, List<Integer> virtualIpIds) throws EntityNotFoundException, BadRequestException, UnprocessableEntityException, ImmutableEntityException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(loadbalancerId, accountId);

        if (!hasAtLeastMinRequiredVips(dbLoadBalancer, virtualIpIds)) {
            LOG.debug("Updating the lb status to active");
            throw new BadRequestException(String.format("Cannot delete virtual ips. Minimum number of virtual ips required is %d.", Constants.MIN_REQUIRED_VIPS));
        }

        List<Integer> badVipIds = doesVipsBelongToLoadBalancer(dbLoadBalancer, virtualIpIds);
        if (!badVipIds.isEmpty()) {
            LOG.debug("Updating the lb status to active");
            throw new BadRequestException(String.format("Must provide valid virtual ips, %s could not be found.", StringUtilities.DelimitString(badVipIds, ",")));
        }

        if (!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        } else {
            //Set status record
            loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE);
        }
    }

    @Override
    @Transactional
    public boolean hasAtLeastMinRequiredVips(LoadBalancer lb, List<Integer> virtualIpIds) {
        Long numVipsAssignedToLoadBalancer = virtualIpRepository.getNumIpv4VipsForLoadBalancer(lb);
        numVipsAssignedToLoadBalancer += virtualIpv6Repository.getNumIpv6VipsForLoadBalancer(lb);
        LOG.debug(String.format("%d vip(s) currently assigned to load balancer '%d'", numVipsAssignedToLoadBalancer, lb.getId()));
        return (numVipsAssignedToLoadBalancer - virtualIpIds.size()) >= Constants.MIN_REQUIRED_VIPS;
    }

    @Override
    @Transactional
    public boolean hasExactlyMinRequiredVips(LoadBalancer lb) {
        Long numVipsAssignedToLoadBalancer = virtualIpRepository.getNumIpv4VipsForLoadBalancer(lb);
        numVipsAssignedToLoadBalancer += virtualIpv6Repository.getNumIpv6VipsForLoadBalancer(lb);
        LOG.debug(String.format("%d vip(s) currently assigned to load balancer '%d'", numVipsAssignedToLoadBalancer, lb.getId()));
        return numVipsAssignedToLoadBalancer == Constants.MIN_REQUIRED_VIPS;
    }

    @Override
    @Transactional
    public boolean doesVipBelongToLoadBalancer(LoadBalancer lb, Integer vipId) {
        List<VirtualIp> vipsForLb = virtualIpRepository.getVipsByLoadBalancerId(lb.getId());
        for (VirtualIp vipForLb : vipsForLb) {
            if (vipId.equals(vipForLb.getId())) {
                return true;
            }
        }

        List<VirtualIpv6> ipv6VipsForLb = virtualIpv6Repository.getVipsByLoadBalancerId(lb.getId());
        for (VirtualIpv6 vipForLb : ipv6VipsForLb) {
            if (vipId.equals(vipForLb.getId())) {
                return true;
            }
        }

        return false;
    }

    @Override
    @Transactional
    public boolean doesVipBelongToAccount(VirtualIp virtualIp, Integer accountId) {
        List<Integer> accountsUsingVip = virtualIpRepository.getAccountIds(virtualIp);
        if (accountsUsingVip.size() > Constants.MIN_ACCOUNTS_PER_VIP) {
            LOG.warn(String.format("Multiple accounts using virtual ip '%d'", virtualIp.getId()));
        }

        for (Integer accountUsingVip : accountsUsingVip) {
            if (accountId.equals(accountUsingVip)) {
                return true;
            }
        }

        return false;
    }

    @Override
    @Transactional
    public void removeVipFromCluster(VirtualIp virtualIp) throws ImmutableEntityException, EntityNotFoundException {
        virtualIp = virtualIpRepository.getById(virtualIp.getId());
        LOG.info(String.format("Removing virtual ip '%d' from cluster...", virtualIp.getId()));
        LOG.debug(String.format("Verifying that virtual ip '%d' is not in use by any load balancer.", virtualIp.getId()));
        if (isVipAllocatedToAnyLoadBalancer(virtualIp)) {
            LOG.warn(String.format("Virtual ip '%d' is in use by another load balancer and cannot be removed.", virtualIp.getId()));
            throw new ImmutableEntityException("The virtual ip belongs to one or more load balancers.");
        }

        virtualIpRepository.deleteVirtualIp(virtualIp);
        LOG.info(String.format("Successfully removed virtual ip '%d' from cluster.", virtualIp.getId()));
    }

    @Override
    public List<VirtualIp> getVipsByClusterId(Integer clusterId) {
        return virtualIpRepository.getVipsByClusterId(clusterId);
    }

    @Override
    @Transactional
    public void persist(Object obj) {
        virtualIpRepository.persist(obj);
    }

    private void reclaimVirtualIp(LoadBalancer lb, VirtualIp virtualIp) {
        if (!isVipAllocatedToAnotherLoadBalancer(lb, virtualIp)) {
            delPtrRecord(lb.getAccountId(), lb.getId(), virtualIp.getIpAddress());
            virtualIpRepository.deallocateVirtualIp(virtualIp);
            Debug.nop();
        }
    }

    private void reclaimIpv6VirtualIp(LoadBalancer lb, VirtualIpv6 virtualIpv6) {
        String ip;
        int aid = lb.getAccountId();
        int lid = lb.getId();
        RdnsHelper rdns = RdnsHelper.newRdnsHelper(aid);
        LoadBalancerServiceEvent lbe;
        if (!isIpv6VipAllocatedToAnotherLoadBalancer(lb, virtualIpv6)) {
            try {
                ip = virtualIpv6.getDerivedIpString();
                delPtrRecord(aid, lid, ip);
            } catch (IPStringConversionException ex) {
                String stackTrace = Debug.getEST(ex);
                String fmt = "Error while attempting to delete PTR record for"
                        + " Virtual IPv6 address for lb %s\n";
                String msg = String.format(fmt, rdns.buildDeviceUri(aid, lid));
                LOG.error(msg + "Exception: " + stackTrace, ex);
                lbe = newDelPtrEvent(aid, lid, "null", msg, stackTrace, true);
                loadBalancerRepository.save(lbe);
            }
            virtualIpv6Repository.deleteVirtualIp(virtualIpv6);
            Debug.nop();
        }
    }

    private LoadBalancerServiceEvent newDelPtrEvent(int aid, int lid, String ip, String msg, String detailedMsg, boolean failed) {
        LoadBalancerServiceEvent lbe = new LoadBalancerServiceEvent();
        lbe.setAccountId(aid);
        lbe.setLoadbalancerId(lid);
        if (failed) {
            lbe.setTitle(DEL_PTR_FAILED);
            lbe.setSeverity(EventSeverity.WARNING);
        } else {
            lbe.setTitle(DEL_PTR_PASSED);
            lbe.setSeverity(EventSeverity.INFO);
        }
        lbe.setDescription(String.format("ip{%s}|%s", ip, msg));
        lbe.setType(EventType.DELETE_VIRTUAL_IP);
        lbe.setCategory(CategoryType.DELETE);
        lbe.setRelativeUri(RdnsHelper.newRdnsHelper(aid).relativeUri(aid, lid));
        lbe.setCreated(Calendar.getInstance());
        if (detailedMsg != null) {
            lbe.setDetailedMessage(detailedMsg);
        }
        return lbe;
    }

    @Transactional
    private void delPtrRecord(int aid, int lid, String ip) {
        RdnsHelper rdns = RdnsHelper.newRdnsHelper(aid);
        ClientResponse resp;
        String msg;
        String fmt;
        String respStr;
        String lbUrl = rdns.buildDeviceUri(aid, lid);
        try {
            resp = rdns.delPtrPubRecord(lid, ip);
        } catch (Exception ex) {
            String stackTrace = Debug.getEST(ex);
            msg = "Exception rDNS caught while attempting to delete ptr " + ""
                    + "for ip " + ip
                    + " on loadbalancer "
                    + lbUrl + "\n";
            LOG.error(msg + stackTrace, ex);
            LoadBalancerServiceEvent lbe = newDelPtrEvent(aid, lid, ip, msg,
                    "Exception: " + stackTrace, true);
            loadBalancerEventRepository.save(lbe);
            return;
        }

        int statusCode = resp.getStatus();


        if (statusCode / 100 != 2) {
            fmt = "ERROR rDNS returned http status code %d when trying "
                    + "attempting to delete PTR record for ip %s for on loadbalancer %s\n";
            msg = String.format(fmt, statusCode, ip, lbUrl);
            respStr = StaticDNSClientUtils.clientResponseToString(resp);
            LOG.error(msg + respStr);
            System.out.printf("%s%s\n", msg, respStr);
            LoadBalancerServiceEvent lbe = newDelPtrEvent(aid, lid, ip, msg, respStr, true);
            loadBalancerEventRepository.save(lbe);
            return;
        } else {
            respStr = StaticDNSClientUtils.clientResponseToString(resp);
            fmt = "SUCCESS rDNS DELETING PTR for account=%d loadbalancerId=%d\n%s";
            msg = String.format(fmt, aid, lid, respStr);
            LoadBalancerServiceEvent lbe = newDelPtrEvent(aid, lid, ip, msg, respStr, false);
            LOG.info(msg);
            loadBalancerEventRepository.save(lbe);
            return;
        }
    }

    private boolean vipTypeMatchesTypeForLoadBalancer(VirtualIpType vipType, LoadBalancer dbLoadBalancer) {
        for (LoadBalancerJoinVip loadBalancerJoinVip : dbLoadBalancer.getLoadBalancerJoinVipSet()) {
            if (!loadBalancerJoinVip.getVirtualIp().getVipType().equals(vipType)) {
                return false;
            }
        }
        return true;
    }

    private boolean isVipConfiguredOnHost(VirtualIp virtualIp, Host host) {
        List<LoadBalancer> loadBalancersUsingVip = virtualIpRepository.getLoadBalancersByVipId(virtualIp.getId());

        for (LoadBalancer loadBalancer : loadBalancersUsingVip) {
            if (!(loadBalancer.getHost().getName().equals(host.getName()))) {
                return false;
            }
        }
        return true;
    }

    private boolean isIpv6VipConfiguredOnHost(VirtualIpv6 virtualIp, Host host) {
        List<LoadBalancer> loadBalancersUsingVip = virtualIpv6Repository.getLoadBalancersByVipId(virtualIp.getId());

        for (LoadBalancer loadBalancer : loadBalancersUsingVip) {
            if (!(loadBalancer.getHost().getName().equals(host.getName()))) {
                return false;
            }
        }
        return true;
    }

    private boolean isVipPortCombinationInUse(VirtualIp virtualIp, Integer loadBalancerPort) {
        return virtualIpRepository.getPorts(virtualIp.getId()).containsKey(loadBalancerPort);
    }

    @Override
    public boolean isIpv4VipPortCombinationInUse(VirtualIp virtualIp, Integer loadBalancerPort) {
        return virtualIpRepository.getPorts(virtualIp.getId()).containsKey(loadBalancerPort);
    }

    @Override
    public boolean isIpv6VipPortCombinationInUse(VirtualIpv6 virtualIp, Integer loadBalancerPort) {
        return virtualIpv6Repository.getPorts(virtualIp.getId()).containsKey(loadBalancerPort);
    }

    private boolean testForDuplicatesByCluster(VirtualIp vip, Integer clusterId) {
        List<VirtualIp> dbVips = virtualIpRepository.getVipsByClusterId(clusterId);
        for (VirtualIp nvip : dbVips) {
            if (vip.getIpAddress().equals(nvip.getIpAddress())) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public Integer getNextVipOctet(Integer accountId) {
        return virtualIpv6Repository.getNextVipOctet(accountId);
    }

    @Override
    public VirtualIpv6 newVirtualIpv6(Integer clusterId, Integer accountId, Integer vipOctets) throws EntityNotFoundException {
        VirtualIpv6 v6 = new VirtualIpv6();
        Cluster c = clusterRepository.getById(clusterId);
        v6.setCluster(c);
        v6.setAccountId(accountId);
        v6.setVipOctets(vipOctets);
        return v6;
    }

    @Override
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

    @Override
    @Transactional
    public List<Integer> genSha1SumsForAccountTable() throws NoSuchAlgorithmException {
        List<Integer> out = new ArrayList<Integer>();
        Set<Integer> accountsInAccountTable;
        Set<Integer> accountsToHash;
        Set<Integer> accountsInLbTable;
        accountsInAccountTable = new HashSet<Integer>(virtualIpv6Repository.getAccountIdsAlreadyShaHashed());
        accountsInLbTable = new HashSet<Integer>(virtualIpv6Repository.getAccountIds());
        accountsToHash = new HashSet<Integer>(accountsInLbTable);
        accountsToHash.removeAll(accountsInAccountTable);
        for (Integer accountId : accountsToHash) {
            Account account = new Account();
            String accountIdStr = String.format("%d", accountId);
            account.setId(accountId);
            account.setSha1SumForIpv6(HashUtil.sha1sumHex(accountIdStr.getBytes(), 0, 4));
            persist(account);
            out.add(accountId);
        }
        return out;
    }

    @Override
    @Transactional
    public void addAccountRecord(Integer accountId) throws NoSuchAlgorithmException {
        Set<Integer> accountsInAccountTable = new HashSet<Integer>(virtualIpv6Repository.getAccountIdsAlreadyShaHashed());

        if (accountsInAccountTable.contains(accountId)) {
            return;
        }

        Account account = new Account();
        String accountIdStr = String.format("%d", accountId);
        account.setId(accountId);
        account.setSha1SumForIpv6(HashUtil.sha1sumHex(accountIdStr.getBytes(), 0, 4));
        try {
            persist(account);
        } catch (Exception e) {
            LOG.warn("High concurrency detected. Ignoring...");
        }
    }

    @Override
    public String getVirtualIpv6String(Integer vip6Id) throws EntityNotFoundException, IPStringConversionException {
        VirtualIpv6 v6 = virtualIpv6Repository.getById(vip6Id);
        return getVirtualIpv6String(v6);
    }

    @Override
    public String getVirtualIpv6String(VirtualIpv6 vip6) throws IPStringConversionException {
        String out;
        String clusterCidrString = vip6.getCluster().getClusterIpv6Cidr();
        if (clusterCidrString == null) {
            String msg = String.format("Cluster[%d] has null value for ClusterIpv6Cider", vip6.getCluster().getId());
            throw new IPStringConversionException(msg);
        }
        IPv6Cidr v6Cidr = new IPv6Cidr(clusterCidrString);
        IPv6 v6 = new IPv6("::");
        v6.setClusterPartition(v6Cidr);
        v6.setAccountPartition(vip6.getAccountId());
        v6.setVipOctets(vip6.getVipOctets());
        out = v6.expand();
        return out;
    }

    private List<Integer> doesVipsBelongToLoadBalancer(LoadBalancer dbLoadBalancer, List<Integer> virtualIpIdsToDelete) {
        return ListUtil.compare(virtualIpIdsToDelete, getVipIdsInDb(dbLoadBalancer));
    }

    private List<Integer> getVipIdsInDb(LoadBalancer loadBalancer) {
        List<Integer> vipIdsInDb = new ArrayList<Integer>();
        for (LoadBalancerJoinVip loadBalancerJoinVip : loadBalancer.getLoadBalancerJoinVipSet()) {
            vipIdsInDb.add(loadBalancerJoinVip.getVirtualIp().getId());
        }

        for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : loadBalancer.getLoadBalancerJoinVip6Set()) {
            vipIdsInDb.add(loadBalancerJoinVip6.getVirtualIp().getId());
        }
        return vipIdsInDb;
    }

    @Override
    public Map<Integer, List<VirtualIp>> getAllocatedVipsMappedByLbId() {
        Map<Integer, List<VirtualIp>> vipMap = new HashMap<Integer, List<VirtualIp>>();
        List<VirtualIp> vips = virtualIpRepository.getAll();
        for (VirtualIp vip : vips) {
            if(vip.getLoadBalancerJoinVipSet().size() == 0) {
                continue;
            }
            Integer lbId = vip.getLoadBalancerJoinVipSet().iterator().next().getLoadBalancer().getId();
            if (vip.isAllocated() && vip.getLoadBalancerJoinVipSet() != null &&
                    !vip.getLoadBalancerJoinVipSet().isEmpty()) {
                if (!vipMap.containsKey(lbId)) {
                    vipMap.put(lbId, new ArrayList<VirtualIp>());
                }
                vipMap.get(lbId).add(vip);
            }
        }
        return vipMap;
    }

}
