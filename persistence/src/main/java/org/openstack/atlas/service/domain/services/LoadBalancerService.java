package org.openstack.atlas.service.domain.services;

import javassist.tools.rmi.ObjectNotFoundException;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.AccountBilling;
import org.openstack.atlas.service.domain.pojos.AccountLoadBalancer;
import org.openstack.atlas.service.domain.pojos.ExtendedAccountLoadBalancer;
import org.openstack.atlas.service.domain.pojos.LbQueryStatus;
import org.openstack.atlas.service.domain.usage.BitTags;

import java.util.Calendar;
import java.util.List;

public interface LoadBalancerService {

    LoadBalancer get(Integer id) throws EntityNotFoundException;

    LoadBalancer get(Integer id, Integer accountId) throws EntityNotFoundException;

    List<AccountLoadBalancer> getAccountLoadBalancers(Integer accountId);

    List<ExtendedAccountLoadBalancer> getExtendedAccountLoadBalancer(Integer accountId);

    List<LoadBalancer> getLoadbalancersGeneric(Integer accountId, String status, LbQueryStatus qs, Calendar changedCal, Integer offset, Integer limit, Integer marker) throws BadRequestException;

    AccountBilling getAccountBilling(Integer accountId, Calendar startTime, Calendar endTime) throws EntityNotFoundException;

    LoadBalancer update(LoadBalancer lb) throws Exception;

    LoadBalancer create(LoadBalancer requestLb) throws Exception;

    LoadBalancer prepareForUpdate(LoadBalancer loadBalancer) throws Exception;

    void prepareForDelete(LoadBalancer lb) throws Exception;

    LoadBalancer pseudoDelete(LoadBalancer lb) throws Exception;

    SessionPersistence getSessionPersistenceByAccountIdLoadBalancerId(Integer accountId, Integer loadbalancerId) throws EntityNotFoundException, DeletedStatusException, BadRequestException;

    /* Mutable method */
    void addDefaultValues(LoadBalancer loadBalancer);

    Boolean isLoadBalancerLimitReached(Integer accountId);
    
    Integer getLoadBalancerLimit(Integer accountId) throws EntityNotFoundException;

    void setStatus(LoadBalancer lb, LoadBalancerStatus status);

    Suspension createSuspension(LoadBalancer loadBalancer, Suspension suspension);

    void removeSuspension(int loadbalancerId);

    List<LoadBalancer> reassignLoadBalancerHost(List<LoadBalancer> lbs) throws Exception, BadRequestException;

    void updateLoadBalancers(List<LoadBalancer> lbs) throws Exception;

    void setLoadBalancerAttrs(LoadBalancer lb) throws EntityNotFoundException;

    LoadBalancer prepareMgmtLoadBalancerDeletion(LoadBalancer loadBalancer, LoadBalancerStatus lbstatus) throws EntityNotFoundException, UnprocessableEntityException;

    List<LoadBalancer> getLoadBalancersForAudit(String status, Calendar changedSince) throws Exception;

    void setStatus(Integer accoundId,Integer loadbalancerId,LoadBalancerStatus status) throws EntityNotFoundException;

    List<LoadBalancer> prepareForDelete(Integer accountId, List<Integer> loadBalancerIds) throws EntityNotFoundException, BadRequestException;

    boolean testAndSetStatusPending(Integer accountId,Integer loadbalancerId) throws EntityNotFoundException, UnprocessableEntityException;

    boolean testAndSetStatus(Integer accountId,Integer loadbalancerId, LoadBalancerStatus loadBalancerStatus) throws EntityNotFoundException, UnprocessableEntityException;

    UserPages getUserPages(Integer id,Integer accountId) throws EntityNotFoundException;

    String getErrorPage(Integer lid, Integer aid) throws EntityNotFoundException;

    String getDefaultErrorPage() throws ObjectNotFoundException, EntityNotFoundException;

    boolean setErrorPage(Integer lid,Integer accountId,String content) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException;

    boolean setDefaultErrorPage(String content) throws EntityNotFoundException;

    boolean removeErrorPage(Integer lid,Integer accountId) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException;

    List<LoadBalancer> getLoadBalancersWithNode(String nodeAddress, Integer accountId);

    List<LoadBalancer> getLoadBalancersWithUsage(Integer accountId, Calendar startTime, Calendar endTime, Integer offset, Integer limit);

    boolean isServiceNetLoadBalancer(Integer lbId);

    BitTags getCurrentBitTags(Integer lbId);
}
