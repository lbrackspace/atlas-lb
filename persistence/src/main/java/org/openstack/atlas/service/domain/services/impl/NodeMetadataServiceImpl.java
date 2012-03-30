package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.AccountLimitType;
import org.openstack.atlas.service.domain.entities.Meta;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.NodeMetadataRepository;
import org.openstack.atlas.service.domain.services.AccountLimitService;
import org.openstack.atlas.service.domain.services.NodeMetadataService;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;

public class NodeMetadataServiceImpl extends BaseService implements NodeMetadataService {
    private final Log LOG = LogFactory.getLog(NodeMetadataServiceImpl.class);
    private AccountLimitService accountLimitService;
    private NodeMetadataRepository nodeMetadataRepository;

    @Required
    public void setAccountLimitService(AccountLimitService accountLimitService) {
        this.accountLimitService = accountLimitService;
    }

    @Override
    public Set<Meta> createNodeMetadata(Integer accountId, Integer nodeId, Collection<Meta> nodeMetas) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
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
        if (detectDuplicateMetadata(node.getMetadata(), nodeMetas)) {
            LOG.warn("Duplicate metadata keys found! Sending failure response back to client...");
            throw new UnprocessableEntityException("Duplicate metadata keys detected. One or more metadata keys already configured on node.");
        }
        LOG.debug(String.format("Current number of metadata items for loadbalancer '%d': %d", nodeId, node.getMetadata().size()));
        LOG.debug(String.format("Number of new metadata items to be added: %d", nodeMetas.size()));

        final Set<Meta> metaSet = null;
        LOG.debug(String.format("Successfully added %d metadata items for node '%d'", metaSet.size(), nodeId));
        return metaSet;
    }

    @Override
    public Set<Meta> getNodeMetadataByAccountIdLoadBalancerId(Integer accountId, Integer nodeId) throws EntityNotFoundException {
        return null;
    }

    @Override
    public Meta getNodeMeta(Integer accountId, Integer nodeId, Integer id) throws EntityNotFoundException {
        return null;
    }

    @Override
    public void deleteNodeMeta(Integer accountId, Integer nodeId, Integer id) throws EntityNotFoundException {

    }

    @Override
    public void updateNodeMeta(Node node) throws EntityNotFoundException {

    }

    @Override
    public List<String> prepareForNodeMetadataDeletion(Integer accountId, Integer loadBalancerId, List<Integer> ids) throws EntityNotFoundException {
        return null;
    }

    @Override
    public Node deleteNodeMetadata(Node node, Collection<Integer> ids) throws EntityNotFoundException {
        return null;
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