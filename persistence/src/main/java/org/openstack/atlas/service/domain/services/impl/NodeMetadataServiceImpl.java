package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.service.domain.entities.AccountLimitType;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeMeta;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.services.AccountLimitService;
import org.openstack.atlas.service.domain.services.NodeMetadataService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NodeMetadataServiceImpl extends BaseService implements NodeMetadataService {
    private final Log LOG = LogFactory.getLog(NodeMetadataServiceImpl.class);
    private AccountLimitService accountLimitService;

    @Required
    public void setAccountLimitService(AccountLimitService accountLimitService) {
        this.accountLimitService = accountLimitService;
    }

    @Override
    public List<NodeMeta> createNodeMetadata(Integer accountId, Integer nodeId, List<NodeMeta> nodeMetas) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        List<Node> nodes = nodeRepository.getNodesByIds(new ArrayList<Integer>(nodeId));
        Node node;
        if (nodes.size() != 1) {
            throw new EntityNotFoundException(String.format("Node with id %d doesn't exist", nodeId));
        } else {
            node = nodes.get(0);
        }
        isLbActive(node.getLoadbalancer());

        try {
            Integer potentialTotalNumMetas = node.getLoadbalancer().getMetadata().size() + nodeMetas.size();
            Integer metaLimit = accountLimitService.getLimit(node.getLoadbalancer().getAccountId(), AccountLimitType.NODE_META_LIMIT);

            LOG.debug(String.format("Verifying that metadata limit isn't reached for node '%d'...", node.getId()));
            if (potentialTotalNumMetas > metaLimit) {
                throw new BadRequestException(String.format("Metadata must not exceed %d per node.", metaLimit));
            }
        } catch (EntityNotFoundException e) {
            LOG.warn("No metadata limit found! The user can add as many metadata items as they want!");
        }

        LOG.debug(String.format("Verifying that there are no duplicate metadata keys for node '%d'...", nodeId));
        if (detectDuplicateNodeMetadata(node.getNodeMetadata(), nodeMetas)) {
            LOG.warn("Duplicate metadata keys found! Sending failure response back to client...");
            throw new UnprocessableEntityException("Duplicate metadata keys detected. One or more metadata keys already configured on node.");
        }
        LOG.debug(String.format("Current number of metadata items for loadbalancer '%d': %d", nodeId, node.getNodeMetadata().size()));
        LOG.debug(String.format("Number of new metadata items to be added: %d", nodeMetas.size()));

        final List<NodeMeta> metaSet = null;
        try {
            LOG.debug(String.format("Successfully added %d metadata items for node '%d'", metaSet.size(), nodeId));
        } catch (NullPointerException e) {
            LOG.debug(String.format("No metadata items to add for node '%d'", 0, nodeId));
        }
        return metaSet;
    }

    @Override
    public List<NodeMeta> getNodeMetadataByAccountIdNodeId(Integer accountId, Integer nodeId) throws EntityNotFoundException {
        return nodeMetadataRepository.getNodeMetaDataByAccountIdNodeId(nodeId);
    }

    @Override
    public List<String> prepareForNodeMetadataDeletion(Integer accountId, Integer loadBalancerId, Integer nodeId, List<Integer> ids) throws EntityNotFoundException {
        List<String> validationErrors = new ArrayList<String>();
        String format, errorMessage;

        Node currentNode = nodeRepository.getNodeByAccountIdLoadBalancerIdNodeId(accountId, loadBalancerId, nodeId);
        /*
        List<String> validationErrors = new ArrayList<String>();
        String format, errMsg;

        Node currentNode = nodeRepository.getNodeByAccountIdLoadBalancerIdNodeId();
        LoadBalancer currentLb = loadBalancerRepository.getByIdAndAccountId(loadBalancerId, accountId);
        Set<Integer> currentMetaIds = new HashSet<Integer>();
        Set<Integer> invalidMetaIds = new HashSet<Integer>();

        for (Meta meta : currentLb.getMetadata()) {
            currentMetaIds.add(meta.getId());
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
        */
        return null;
    }

    @Override
    public Node deleteNodeMetadata(Node node, Collection<Integer> ids) throws EntityNotFoundException {
        return null;
    }

    private boolean detectDuplicateNodeMetadata(Collection<NodeMeta> nodemeta1, Collection<NodeMeta> nodemeta2) {
        Set<String> keys = new HashSet<String>();
        for (NodeMeta meta : nodemeta1) {
            if (!keys.add(meta.getKey())) {
                return true;
            }
        }
        for (NodeMeta meta : nodemeta2) {
            if (!keys.add(meta.getKey())) {
                return true;
            }
        }

        return false;
    }
}
/*
package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.pojos.NodeMap;
import org.openstack.atlas.service.domain.services.AccountLimitService;
import org.openstack.atlas.service.domain.services.MetadataService;
import org.openstack.atlas.service.domain.services.helpers.NodesPrioritiesContainer;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.util.converters.StringConverter;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    @Override
    public void updateMeta(LoadBalancer msgLb) throws EntityNotFoundException {
        LoadBalancer currentLb = loadBalancerRepository.getByIdAndAccountId(msgLb.getId(), msgLb.getAccountId());

        Meta metaToUpdate = msgLb.getMetadata().iterator().next();
        if (!loadBalancerContainsMeta(currentLb, metaToUpdate)) {
            LOG.warn("Meta to update not found. Sending response to client...");
            throw new EntityNotFoundException(String.format("Meta data item with id #%d not found for loadbalancer #%d", metaToUpdate.getId(), msgLb.getId()));
        }

        LOG.debug("Meta on dbLoadbalancer: " + currentLb.getMetadata().size());
        for (Meta meta : currentLb.getMetadata()) {
            if (meta.getId().equals(metaToUpdate.getId())) {
                LOG.info("Meta to be updated found: " + meta.getId());
                if (metaToUpdate.getKey() != null) {
                    meta.setKey(metaToUpdate.getKey());
                }
                if (metaToUpdate.getValue() != null) {
                    meta.setValue(metaToUpdate.getValue());
                }
                break;
            }
        }

        metadataRepository.update(currentLb);
    }

    @Override
    public List<String> prepareForMetadataDeletion(Integer accountId, Integer loadBalancerId, List<Integer> ids) throws EntityNotFoundException {
        List<String> validationErrors = new ArrayList<String>();
        String format, errMsg;

        LoadBalancer currentLb = loadBalancerRepository.getByIdAndAccountId(loadBalancerId, accountId);
        Set<Integer> currentMetaIds = new HashSet<Integer>();
        Set<Integer> invalidMetaIds = new HashSet<Integer>();

        for (Meta meta : currentLb.getMetadata()) {
            currentMetaIds.add(meta.getId());
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
        return metadataRepository.deleteMetadata(dbLoadBalancer, ids);
    }

    private boolean loadBalancerContainsMeta(LoadBalancer lb, Meta meta) {
        for (Meta m : lb.getMetadata()) {
            if (m.getId().equals(meta.getId())) {
                return true;
            }
        }
        return false;
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

*/