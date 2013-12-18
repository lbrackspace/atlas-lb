package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.services.AccountLimitService;
import org.openstack.atlas.service.domain.services.LoadbalancerMetadataService;
import org.openstack.atlas.util.converters.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class LoadbalancerMetadataServiceImpl extends BaseService implements LoadbalancerMetadataService {
    private final Log LOG = LogFactory.getLog(LoadbalancerMetadataServiceImpl.class);

    @Autowired
    private AccountLimitService accountLimitService;

    @Override
    public Set<LoadbalancerMeta> createLoadbalancerMetadata(Integer accountId, Integer loadBalancerId, Collection<LoadbalancerMeta> loadbalancerMetas) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        LoadBalancer oldLb = loadBalancerRepository.getByIdAndAccountId(loadBalancerId, accountId);
        isLbActive(oldLb);

        try {
            Integer potentialTotalNumMetas = oldLb.getLoadbalancerMetadata().size() + loadbalancerMetas.size();
            Integer metaLimit = accountLimitService.getLimit(oldLb.getAccountId(), AccountLimitType.LOADBALANCER_META_LIMIT);

            LOG.debug(String.format("Verifying that metadata limit isn't reached for lb '%d'...", loadBalancerId));
            if (potentialTotalNumMetas > metaLimit) {
                throw new BadRequestException(String.format("Metadata must not exceed %d per load balancer.", metaLimit));
            }
        } catch (EntityNotFoundException e) {
            LOG.warn("No metadata limit found! The user can add as many metadata items as they want!");
        }

        LOG.debug(String.format("Verifying that there are no duplicate metadata keys for lb '%d'...", loadBalancerId));
        if (detectDuplicateMetadata(oldLb.getLoadbalancerMetadata(), loadbalancerMetas)) {
            LOG.warn("Duplicate metadata keys found! Sending failure response back to client...");
            throw new UnprocessableEntityException("Duplicate metadata keys detected. One or more metadata keys already configured on load balancer.");
        }

        LOG.debug(String.format("Current number of metadata items for loadbalancer '%d': %d", loadBalancerId, oldLb.getLoadbalancerMetadata().size()));
        LOG.debug(String.format("Number of new metadata items to be added: %d", loadbalancerMetas.size()));

        final Set<LoadbalancerMeta> loadbalancerMetaSet = loadbalancerMetadataRepository.addLoadbalancerMetas(oldLb, loadbalancerMetas);
        LOG.debug(String.format("Successfully added %d metadata items for loadbalancer '%d'", loadbalancerMetaSet.size(), loadBalancerId));
        return loadbalancerMetaSet;
    }

    @Override
    public Set<LoadbalancerMeta> getLoadbalancerMetadataByAccountIdLoadBalancerId(Integer accountId, Integer loadBalancerId) throws EntityNotFoundException {
        final List<LoadbalancerMeta> metadataByAccountIdLoadBalancerId = loadbalancerMetadataRepository.getLoadbalancerMetadataByAccountIdLoadBalancerId(accountId, loadBalancerId);
        Set<LoadbalancerMeta> loadbalancerMetaSet = new HashSet<LoadbalancerMeta>();

        for (LoadbalancerMeta loadbalancerMeta : metadataByAccountIdLoadBalancerId) {
            loadbalancerMetaSet.add(loadbalancerMeta);
        }
        
        return loadbalancerMetaSet;
    }

    @Override
    public LoadbalancerMeta getLoadbalancerMeta(Integer accountId, Integer loadBalancerId, Integer id) throws EntityNotFoundException {
        return loadbalancerMetadataRepository.getLoadbalancerMeta(accountId, loadBalancerId, id);
    }

    @Override
    public void deleteLoadbalancerMeta(Integer accountId, Integer loadBalancerId, Integer id) throws EntityNotFoundException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(loadBalancerId, accountId);
        loadbalancerMetadataRepository.deleteLoadbalancerMeta(dbLoadBalancer, id);
    }

    @Override
    public void updateLoadbalancerMeta(LoadBalancer msgLb) throws EntityNotFoundException {
        LoadBalancer currentLb = loadBalancerRepository.getByIdAndAccountId(msgLb.getId(), msgLb.getAccountId());

        LoadbalancerMeta loadbalancerMetaToUpdate = msgLb.getLoadbalancerMetadata().iterator().next();
        if (!loadBalancerContainsMeta(currentLb, loadbalancerMetaToUpdate)) {
            LOG.warn("LoadbalancerMeta to update not found. Sending response to client...");
            throw new EntityNotFoundException(String.format("LoadbalancerMeta data item with id #%d not found for loadbalancer #%d", loadbalancerMetaToUpdate.getId(), msgLb.getId()));
        }

        LOG.debug("LoadbalancerMeta on dbLoadbalancer: " + currentLb.getLoadbalancerMetadata().size());
        for (LoadbalancerMeta loadbalancerMeta : currentLb.getLoadbalancerMetadata()) {
            if (loadbalancerMeta.getId().equals(loadbalancerMetaToUpdate.getId())) {
                LOG.info("LoadbalancerMeta to be updated found: " + loadbalancerMeta.getId());
                if (loadbalancerMetaToUpdate.getKey() != null) {
                    loadbalancerMeta.setKey(loadbalancerMetaToUpdate.getKey());
                }
                if (loadbalancerMetaToUpdate.getValue() != null) {
                    loadbalancerMeta.setValue(loadbalancerMetaToUpdate.getValue());
                }
                break;
            }
        }

        loadbalancerMetadataRepository.update(currentLb);
    }

    @Override
    public List<String> prepareForLoadbalancerMetadataDeletion(Integer accountId, Integer loadBalancerId, List<Integer> ids) throws EntityNotFoundException {
        List<String> validationErrors = new ArrayList<String>();
        String format, errMsg;

        LoadBalancer currentLb = loadBalancerRepository.getByIdAndAccountId(loadBalancerId, accountId);
        Set<Integer> currentMetaIds = new HashSet<Integer>();
        Set<Integer> invalidMetaIds = new HashSet<Integer>();

        for (LoadbalancerMeta loadbalancerMeta : currentLb.getLoadbalancerMetadata()) {
            currentMetaIds.add(loadbalancerMeta.getId());
        }

        for (Integer id : ids) {
            if(!currentMetaIds.contains(id)) invalidMetaIds.add(id);
        }

        int batch_delete_limit = accountLimitService.getLimit(accountId, AccountLimitType.BATCH_DELETE_LIMIT);

        if (ids.size() > batch_delete_limit) {
            format = "Request to delete %d metadata items exceeds the account limit"
                    + " BATCH_DELETE_LIMIT of %d please attempt to delete fewer then %d nodes";
            errMsg = String.format(format, ids.size(), batch_delete_limit, batch_delete_limit);
            validationErrors.add(errMsg);
        }
        
        if (!invalidMetaIds.isEmpty()) {
            // Don't even take this request seriously any ID does not belong to this account
            format = "Metadata ids %s are not a part of your loadbalancer";
            errMsg = String.format(format, StringConverter.integersAsString(invalidMetaIds));
            validationErrors.add(errMsg);
        }

        return validationErrors;
    }

    @Transactional
    @Override
    public LoadBalancer deleteMetadata(LoadBalancer lb, Collection<Integer> ids) throws EntityNotFoundException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(lb.getId(), lb.getAccountId());
        return loadbalancerMetadataRepository.deleteLoadbalancerMetadata(dbLoadBalancer, ids);
    }

    private boolean loadBalancerContainsMeta(LoadBalancer lb, LoadbalancerMeta loadbalancerMeta) {
        for (LoadbalancerMeta m : lb.getLoadbalancerMetadata()) {
            if (m.getId().equals(loadbalancerMeta.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean detectDuplicateMetadata(Collection<LoadbalancerMeta> metadata1, Collection<LoadbalancerMeta> metadata2) {
        Set<String> keys = new HashSet<String>();

        for (LoadbalancerMeta loadbalancerMeta : metadata1) {
            if (!keys.add(loadbalancerMeta.getKey())) {
                return true;
            }
        }

        for (LoadbalancerMeta loadbalancerMeta : metadata2) {
            if (!keys.add(loadbalancerMeta.getKey())) {
                return true;
            }
        }

        return false;
    }
}
