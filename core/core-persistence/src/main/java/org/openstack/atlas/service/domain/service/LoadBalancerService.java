package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.DeletedStatusException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.openstack.atlas.service.domain.pojo.AccountLoadBalancer;
import org.openstack.atlas.service.domain.pojo.LbQueryStatus;

import java.util.Calendar;
import java.util.List;

public interface LoadBalancerService {

    LoadBalancer get(Integer id) throws EntityNotFoundException;

    LoadBalancer get(Integer id, Integer accountId) throws EntityNotFoundException;

    List<AccountLoadBalancer> getAccountLoadBalancers(Integer accountId);

    List<LoadBalancer> getLoadbalancersGeneric(Integer accountId, String status, LbQueryStatus qs, Calendar changedCal, Integer offset, Integer limit, Integer marker) throws BadRequestException;

    //public AccountBilling getAccountBilling(Integer accountId, Calendar startTime, Calendar endTime) throws EntityNotFoundException;

    LoadBalancer update(LoadBalancer lb) throws Exception;

    LoadBalancer create(LoadBalancer requestLb) throws Exception;

    LoadBalancer prepareForUpdate(LoadBalancer loadBalancer) throws Exception;

    void prepareForDelete(LoadBalancer lb) throws Exception;

    void pseudoDelete(LoadBalancer lb) throws Exception;

    //public SessionPersistence getSessionPersistenceByAccountIdLoadBalancerId(Integer accountId, Integer loadbalancerId) throws EntityNotFoundException, DeletedStatusException, BadRequestException;

    /* Mutable method */
    void addDefaultValues(LoadBalancer loadBalancer);

    //public Boolean isLoadBalancerLimitReached(Integer accountId);
    
    //public Integer getLoadBalancerLimit(Integer accountId) throws EntityNotFoundException;

    void setStatus(LoadBalancer lb, LoadBalancerStatus status);

    //public Suspension createSuspension(LoadBalancer loadBalancer, Suspension suspension);

    void removeSuspension(int loadbalancerId);

    List<LoadBalancer> reassignLoadBalancerHost(List<LoadBalancer> lbs) throws Exception, BadRequestException;

    void updateLoadBalancers(List<LoadBalancer> lbs) throws Exception;

    void setLoadBalancerAttrs(LoadBalancer lb) throws EntityNotFoundException;

    LoadBalancer prepareMgmtLoadBalancerDeletion(LoadBalancer loadBalancer, LoadBalancerStatus lbstatus) throws EntityNotFoundException, UnprocessableEntityException;

    List<LoadBalancer> getLoadBalancersForAudit(String status, Calendar changedSince) throws Exception;

    void setStatus(Integer accoundId, Integer loadbalancerId, LoadBalancerStatus status) throws EntityNotFoundException;

    void prepareForDelete(Integer accountId, List<Integer> loadBalancerIds) throws EntityNotFoundException, BadRequestException;

    boolean testAndSetStatusPending(Integer accountId, Integer loadbalancerId) throws EntityNotFoundException, UnprocessableEntityException;
}
