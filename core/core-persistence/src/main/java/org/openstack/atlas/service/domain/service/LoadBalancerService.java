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

    public LoadBalancer get(Integer id) throws EntityNotFoundException;

    public LoadBalancer get(Integer id, Integer accountId) throws EntityNotFoundException;

    public List<AccountLoadBalancer> getAccountLoadBalancers(Integer accountId);

    public List<LoadBalancer> getLoadbalancersGeneric(Integer accountId, String status, LbQueryStatus qs, Calendar changedCal, Integer offset, Integer limit, Integer marker) throws BadRequestException;

    //public AccountBilling getAccountBilling(Integer accountId, Calendar startTime, Calendar endTime) throws EntityNotFoundException;

    public LoadBalancer update(LoadBalancer lb) throws Exception;

    public LoadBalancer create(LoadBalancer requestLb) throws Exception;

    public LoadBalancer prepareForUpdate(LoadBalancer loadBalancer) throws Exception;

    public void prepareForDelete(LoadBalancer lb) throws Exception;

    public void pseudoDelete(LoadBalancer lb) throws Exception;

    //public SessionPersistence getSessionPersistenceByAccountIdLoadBalancerId(Integer accountId, Integer loadbalancerId) throws EntityNotFoundException, DeletedStatusException, BadRequestException;

    /* Mutable method */
    public void addDefaultValues(LoadBalancer loadBalancer);

    //public Boolean isLoadBalancerLimitReached(Integer accountId);
    
    //public Integer getLoadBalancerLimit(Integer accountId) throws EntityNotFoundException;

    public void setStatus(LoadBalancer lb, LoadBalancerStatus status);

    //public Suspension createSuspension(LoadBalancer loadBalancer, Suspension suspension);

    public void removeSuspension(int loadbalancerId);

    public List<LoadBalancer> reassignLoadBalancerHost(List<LoadBalancer> lbs) throws Exception, BadRequestException;

    public void updateLoadBalancers(List<LoadBalancer> lbs) throws Exception;

    public void setLoadBalancerAttrs(LoadBalancer lb) throws EntityNotFoundException;

    public LoadBalancer prepareMgmtLoadBalancerDeletion(LoadBalancer loadBalancer, LoadBalancerStatus lbstatus) throws EntityNotFoundException, UnprocessableEntityException;

    public List<LoadBalancer> getLoadBalancersForAudit(String status, Calendar changedSince) throws Exception;

    public void setStatus(Integer accoundId, Integer loadbalancerId, LoadBalancerStatus status) throws EntityNotFoundException;

    public void prepareForDelete(Integer accountId, List<Integer> loadBalancerIds) throws EntityNotFoundException, BadRequestException;

    public boolean testAndSetStatusPending(Integer accountId, Integer loadbalancerId) throws EntityNotFoundException, UnprocessableEntityException;
}
