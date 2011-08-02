package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.AccessList;
import org.openstack.atlas.service.domain.entities.AccountLimitType;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.services.AccessListService;
import org.openstack.atlas.service.domain.services.AccountLimitService;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AccessListServiceImpl extends BaseService implements AccessListService {
    private final Log LOG = LogFactory.getLog(AccessListServiceImpl.class);
    private AccountLimitService accountLimitService;

    @Required
    public void setAccountLimitService(AccountLimitService accountLimitService) {
        this.accountLimitService = accountLimitService;
    }

    @Override
    public List<AccessList> getAccessListByAccountIdLoadBalancerId(int accountId, int loadbalancerId, Integer... p) throws EntityNotFoundException, DeletedStatusException {
        return loadBalancerRepository.getAccessListByAccountIdLoadBalancerId(accountId, loadbalancerId, p);
    }

    @Override
    @Transactional
    public LoadBalancer updateAccessList(LoadBalancer rLb) throws EntityNotFoundException, ImmutableEntityException, BadRequestException {
        String msg;
        String format;
        LoadBalancer dLb;
        String className = AccessListServiceImpl.class.getName();

        try {
            dLb = loadBalancerRepository.getByIdAndAccountId(rLb.getId(), rLb.getAccountId());
        } catch (EntityNotFoundException ex) {
            Logger.getLogger(className).log(Level.SEVERE, null, ex);
            throw ex;
        }

        if (!isActiveLoadBalancer(dLb, false)) {
            String message = StringHelper.immutableLoadBalancer(dLb);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }

        Integer aListTotal = dLb.getAccessLists().size() + rLb.getAccessLists().size();
        Integer accessListLimit = accountLimitService.getLimit(dLb.getAccountId(), AccountLimitType.ACCESS_LIST_LIMIT);

        if (aListTotal > accessListLimit) {
            throw new BadRequestException(String.format("Access list size must not exceed %d items.", accessListLimit));
        }

        if (hasDupeIpInAccessLists(dLb.getAccessLists(), rLb.getAccessLists())) {
            throw new BadRequestException("Must supply a unique access list item to update the current list.");
        }

        for (AccessList al : rLb.getAccessLists()) {
            dLb.addAccessList(al);
        }
        format = "Pre LBDevice updateing Lb[%d] to PENDING_UPDATE while added accesslists";
        msg = String.format(format, rLb.getId());
        dLb.setStatus(LoadBalancerStatus.PENDING_UPDATE);
        loadBalancerRepository.update(dLb);

        return rLb;
    }

    // Deletes all AccessLists for this Lb
    @Transactional
    @Override
    public LoadBalancer markForDeletionAccessList(LoadBalancer rLb) throws EntityNotFoundException, ImmutableEntityException, DeletedStatusException, UnprocessableEntityException {
        String format;
        String msg;
        List<AccessList> al = new ArrayList<AccessList>();
        LoadBalancer dLb;
        String className = this.getClass().getName();
        al = loadBalancerRepository.getAccessListByAccountIdLoadBalancerId(rLb.getAccountId(), rLb.getId(), 0, 1);
        if (al.isEmpty()) {
            throw new UnprocessableEntityException("No access list found to delete");
        }
        dLb = loadBalancerRepository.getByIdAndAccountId(rLb.getId(), rLb.getAccountId());
        format = "Pre LBDevice updateing Lb[%d] to PENDING_DELETE while deleting accesslists";
        msg = String.format(format, rLb.getId());
        dLb.setStatus(LoadBalancerStatus.PENDING_UPDATE);
        loadBalancerRepository.update(dLb);
        return rLb;
    }

    @Override
    @Transactional
    public LoadBalancer markForDeletionNetworkItems(LoadBalancer returnLB, List<Integer> networkItemIds) throws EntityNotFoundException, ImmutableEntityException {
        LoadBalancer domainLB;
        List<AccessList> accessLists = new ArrayList<AccessList>();
        LOG.debug("Entering " + getClass());

        domainLB = loadBalancerRepository.getByIdAndAccountId(returnLB.getId(), returnLB.getAccountId());

        if (!isActiveLoadBalancer(domainLB, false)) {
            String message = StringHelper.immutableLoadBalancer(domainLB);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }

        for (Integer networkItem : networkItemIds) {
            Boolean isFound = false;
            for (AccessList al : domainLB.getAccessLists()) {
                if (networkItem.equals(al.getId())) {
                    isFound = true;
                    accessLists.add(al);
                }
            }
            if (!isFound) {
                throw new EntityNotFoundException("Network Item with id " + networkItem + " not found.");
            }
            domainLB.getAccessLists().removeAll(accessLists);
        }

        LOG.debug("Updating the lb status to pending_update");
        domainLB.setStatus(LoadBalancerStatus.PENDING_UPDATE);
        returnLB.getAccessLists().clear();
        returnLB.getAccessLists().addAll(domainLB.getAccessLists());
        loadBalancerRepository.update(domainLB);
        return returnLB;
    }

    @Transactional
    @Override
    public LoadBalancer markForDeletionNetworkItem(LoadBalancer rLb) throws EntityNotFoundException, ImmutableEntityException {
        AccessList ai;
        LoadBalancer dLb = null;
        Integer nid = null;
        LOG.debug("Entering " + getClass());

        for (AccessList ni : rLb.getAccessLists()) {
            nid = ni.getId();
            break;
        }

        // If the network item doesn't exist on this account or loadbalancer puke an EntityNotFound
        try {
            ai = loadBalancerRepository.getNetworkItemByAccountIdLoadBalancerIdNetworkItemId(rLb.getAccountId(), rLb.getId(), nid);
            dLb = ai.getLoadbalancer();
        } catch (EntityNotFoundException ex) {
            LOG.warn("EntityNotFoundException thrown. Sending error response to client...");
            throw ex;
        }

        if (!isActiveLoadBalancer(dLb, false)) {
            String message = StringHelper.immutableLoadBalancer(dLb);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }

        LOG.debug("Updating the lb status to pending_update");
        dLb.setStatus(LoadBalancerStatus.PENDING_UPDATE);
        dLb.getAccessLists().remove(ai);
        rLb.getAccessLists().clear();
        rLb.getAccessLists().add(ai);
        loadBalancerRepository.update(dLb);
        return rLb;
    }

    @Override
    public Set<AccessList> diffRequestAccessListWithDomainAccessList(LoadBalancer rLb, LoadBalancer dLb) {
        boolean alInRlb;
        boolean alInDlb;
        Set<AccessList> out = new HashSet<AccessList>();
        Map<String, AccessList> alMap = new HashMap<String, AccessList>();

        for (AccessList al : dLb.getAccessLists()) {
            String ip = al.getIpAddress();
            String type = al.getType().name();
            String key = String.format("%s:%s", ip, type);
            alMap.put(key, al);
        }

        for (AccessList al : rLb.getAccessLists()) {
            String ip = al.getIpAddress();
            String type = al.getType().name();
            String key = String.format("%s:%s", ip, type);
            alInRlb = (al.getId() != null);
            alInDlb = alMap.containsKey(key);
            if(alInRlb == alInDlb) {
                continue; // No Difference so don't add
            }
            if(alInRlb){
                out.add(al);
            }else{// We already know alInDlb at this point
                out.add(alMap.get(key));
            }
        }
        return out;
    }

    private boolean hasDupeIpInAccessLists(Set<AccessList>... als) {
        boolean out;
        int i;
        Set<String> ipSet = new HashSet<String>();
        String ip;
        for (i = 0; i < als.length; i++) {
            for (AccessList al : als[i]) {
                ip = al.getIpAddress();
                if (ipSet.contains(ip)) {
                    out = true;
                    return out;
                }
                ipSet.add(ip);
            }
        }
        out = false;
        return out;
    }
}
