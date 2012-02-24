package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.AccountLimitType;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Meta;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.services.AccountLimitService;
import org.openstack.atlas.service.domain.services.MetadataService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MetadataServiceImpl extends BaseService implements MetadataService {
    private final Log LOG = LogFactory.getLog(MetadataServiceImpl.class);
    private AccountLimitService accountLimitService;

    @Required
    public void setAccountLimitService(AccountLimitService accountLimitService) {
        this.accountLimitService = accountLimitService;
    }

    @Override
    public Set<Meta> createMetadata(Integer accountId, Integer loadBalancerId, Collection<Meta> metas) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        LoadBalancer oldLb = loadBalancerRepository.getByIdAndAccountId(loadBalancerId, accountId);
        isLbActive(oldLb);

        try {
            Integer potentialTotalNumMetas = oldLb.getMetadata().size() + metas.size();
            Integer metaLimit = accountLimitService.getLimit(oldLb.getAccountId(), AccountLimitType.LOADBALANCER_META_LIMIT);

            LOG.debug(String.format("Verifying that metadata limit isn't reached for lb '%d'...", loadBalancerId));
            if (potentialTotalNumMetas > metaLimit) {
                throw new BadRequestException(String.format("Metadata must not exceed %d per load balancer.", metaLimit));
            }
        } catch (EntityNotFoundException e) {
            LOG.warn("No metadata limit found! The user can add as many metadata items as they want!");
        }

        LOG.debug(String.format("Verifying that there are no duplicate metadata keys for lb '%d'...", loadBalancerId));
        if (detectDuplicateMetadata(oldLb.getMetadata(), metas)) {
            LOG.warn("Duplicate metadata keys found! Sending failure response back to client...");
            throw new UnprocessableEntityException("Duplicate metadata keys detected. One or more metadata keys already configured on load balancer.");
        }

        LOG.debug(String.format("Current number of metadata items for loadbalancer '%d': %d", loadBalancerId, oldLb.getMetadata().size()));
        LOG.debug(String.format("Number of new metadata items to be added: %d", metas.size()));

        final Set<Meta> metaSet = metadataRepository.addMetas(oldLb, metas);
        LOG.debug(String.format("Successfully added %d metadata items for loadbalancer '%d'", metaSet.size(), loadBalancerId));
        return metaSet;
    }

    @Override
    public Set<Meta> getMetadataByAccountIdLoadBalancerId(Integer accountId, Integer loadBalancerId) throws EntityNotFoundException {
        final List<Meta> metadataByAccountIdLoadBalancerId = metadataRepository.getMetadataByAccountIdLoadBalancerId(accountId, loadBalancerId);
        Set<Meta> metaSet = new HashSet<Meta>();

        for (Meta meta : metadataByAccountIdLoadBalancerId) {
            metaSet.add(meta);
        }
        
        return metaSet;
    }

    @Override
    public Meta getMeta(Integer accountId, Integer loadBalancerId, Integer id) throws EntityNotFoundException {
        return metadataRepository.getMeta(accountId, loadBalancerId, id);
    }

    @Override
    public void deleteMeta(Integer accountId, Integer loadBalancerId, Integer id) throws EntityNotFoundException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(loadBalancerId, accountId);
        metadataRepository.deleteMeta(dbLoadBalancer, id);
    }

    private boolean detectDuplicateMetadata(Collection<Meta> metadata1, Collection<Meta> metadata2) {
        Set<String> keys = new HashSet<String>();

        for (Meta meta : metadata1) {
            if (!keys.add(meta.getKey())) {
                return true;
            }
        }

        for (Meta meta : metadata2) {
            if (!keys.add(meta.getKey())) {
                return true;
            }
        }

        return false;
    }
}
