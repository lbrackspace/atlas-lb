package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.services.AccessListService;
import org.openstack.atlas.service.domain.services.AccountLimitService;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AccessListServiceImpl extends BaseService implements AccessListService {
    private final Log LOG = LogFactory.getLog(AccessListServiceImpl.class);

    @Autowired
    private AccountLimitService accountLimitService;
    @Autowired
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;

    @Override
    public List<AccessList> getAccessListByAccountIdLoadBalancerId(int accountId, int loadbalancerId, Integer... p) throws EntityNotFoundException, DeletedStatusException {
        return loadBalancerRepository.getAccessListByAccountIdLoadBalancerId(accountId, loadbalancerId, p);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public LoadBalancer updateAccessList(LoadBalancer rLb) throws EntityNotFoundException, ImmutableEntityException, BadRequestException, UnprocessableEntityException {
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

         LOG.debug("Updating the lb status to pending_update");
        if(!loadBalancerRepository.testAndSetStatus(dLb.getAccountId(), dLb.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dLb);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        } else {
            //Set status record
            loadBalancerStatusHistoryService.save(dLb.getAccountId(), dLb.getId(), LoadBalancerStatus.PENDING_UPDATE);
        }

        Integer aListTotal = dLb.getAccessLists().size() + rLb.getAccessLists().size();
        Integer accessListLimit = accountLimitService.getLimit(dLb.getAccountId(), AccountLimitType.ACCESS_LIST_LIMIT);

        if (aListTotal > accessListLimit) {
            throw new BadRequestException(String.format("Access list size must not exceed %d items.", accessListLimit));
        }

        if (hasDupeIpInAccessLists(dLb.getAccessLists(), rLb.getAccessLists())) {
            throw new BadRequestException("Must supply a unique access list item to update the current list.");
        }

        try {
            AccessList badAccessList = blackListedItemAccessList(rLb.getAccessLists());
            if (badAccessList != null) {
                throw new BadRequestException(String.format("Invalid network item address. The address '%s' is currently not accepted for this request.", badAccessList.getIpAddress()));
            }
        } catch (IPStringConversionException ipe) {
            LOG.warn("IPStringConversionException thrown. Sending error response to client...");
            throw new BadRequestException("IP address was not converted properly, we are unable to process this request.");
        } catch (IpTypeMissMatchException ipte) {
            LOG.warn("EntityNotFoundException thrown. Sending error response to client...");
            throw new BadRequestException("IP addresses type are mismatched, we are unable to process this request.");
        }

        for (AccessList al : rLb.getAccessLists()) {
            dLb.addAccessList(al);
        }

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
        format = "Pre Zxtm updateing Lb[%d] to PENDING_DELETE while deleting accesslists";
        msg = String.format(format, rLb.getId());
        dLb.setStatus(LoadBalancerStatus.PENDING_UPDATE);
        loadBalancerRepository.update(dLb);

        //Set status record
         loadBalancerStatusHistoryService.save(dLb.getAccountId(), dLb.getId(), LoadBalancerStatus.PENDING_UPDATE);
        return rLb;
    }

    @Override
    @Transactional
    public LoadBalancer markForDeletionNetworkItems(LoadBalancer returnLB, List<Integer> networkItemIds) throws BadRequestException, ImmutableEntityException, EntityNotFoundException {
        LoadBalancer domainLB;
        List<AccessList> accessLists = new ArrayList<AccessList>();
        LOG.debug("Entering " + getClass());

        domainLB = loadBalancerRepository.getByIdAndAccountId(returnLB.getId(), returnLB.getAccountId());

        if (!isActiveLoadBalancer(domainLB, false)) {
            String message = StringHelper.immutableLoadBalancer(domainLB);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }

        List<Integer> badList = new ArrayList<Integer>();
        for (Integer networkItemId : networkItemIds) {
            boolean isFound = false;
            for (AccessList al : domainLB.getAccessLists()) {
                if (networkItemId.equals(al.getId())) {
                    isFound = true;
                    accessLists.add(al);
                }
            }
            if (!isFound) {
                badList.add(networkItemId);
            }
        }

        if (badList.size() != 0) {
            String outList = "";
            for (Integer list : badList) {
                outList += list + ", ";
            }
            String out = outList.substring(0, outList.length() - 2);
            String plural = "";
            if (badList.size() > 1) {
                plural = "s";
            }
            throw new BadRequestException("Network item" + plural + " with id" + plural + " " + out + " not found.");
        }
        domainLB.getAccessLists().removeAll(accessLists);

        LOG.debug("Updating the lb status to pending_update");
        domainLB.setStatus(LoadBalancerStatus.PENDING_UPDATE);
        returnLB.getAccessLists().clear();
        returnLB.getAccessLists().addAll(domainLB.getAccessLists());
        loadBalancerRepository.update(domainLB);

        //Set status record
        loadBalancerStatusHistoryService.save(domainLB.getAccountId(), domainLB.getId(), LoadBalancerStatus.PENDING_UPDATE);
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
