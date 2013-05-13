package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.AccountLimitType;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeMeta;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.services.AccountLimitService;
import org.openstack.atlas.service.domain.services.NodeMetadataService;
import org.openstack.atlas.util.converters.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NodeMetadataServiceImpl extends BaseService implements NodeMetadataService {
    private final Log LOG = LogFactory.getLog(NodeMetadataServiceImpl.class);

    @Autowired
    private AccountLimitService accountLimitService;

    @Override
    public List<NodeMeta> createNodeMetadata(Integer accountId, Integer loadbalancerId, Integer nodeId, List<NodeMeta> nodeMetadata) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        Node node;
        try {
            node = nodeRepository.getNodeByAccountIdLoadBalancerIdNodeId(accountId, loadbalancerId, nodeId);
        } catch(Exception e) {
            throw new EntityNotFoundException(String.format("Node with id %d doesn't exist", nodeId));
        }

        try {
            isLbActive(node.getLoadbalancer());
        } catch (Exception e) {
            throw new BadRequestException(String.format("Loadbalancer %d is deleted, therefore it is immutable.", loadbalancerId));
        }

        try {
            Integer potentialTotalNumMetas = node.getNodeMetadata().size() + nodeMetadata.size();
            Integer metaLimit = accountLimitService.getLimit(accountId, AccountLimitType.NODE_META_LIMIT);

            LOG.debug(String.format("Verifying that metadata limit isn't reached for node '%d'...", node.getId()));
            if (potentialTotalNumMetas > metaLimit) {
                throw new BadRequestException(String.format("Metadata must not exceed %d per node.", metaLimit));
            }
        } catch (EntityNotFoundException e) {
            LOG.warn("No metadata limit found! The user can add as many metadata items as they want!");
        }

        LOG.debug(String.format("Verifying that there are no duplicate metadata keys for node '%d'...", nodeId));
        if (detectDuplicateNodeMetadata(node.getNodeMetadata(), nodeMetadata)) {
            LOG.warn("Duplicate metadata keys found! Sending failure response back to client...");
            throw new UnprocessableEntityException("Duplicate metadata keys detected. One or more metadata keys already configured on node.");
        }
        LOG.debug(String.format("Current number of metadata items for node '%d': %d", nodeId, node.getNodeMetadata().size()));
        LOG.debug(String.format("Number of new metadata items to be added: %d", nodeMetadata.size()));

        List<NodeMeta> createdNodeMeta = nodeMetadataRepository.addNodeMetas(node, nodeMetadata);
        try {
            LOG.debug(String.format("Successfully added %d metadata items for node '%d'", createdNodeMeta.size(), nodeId));
        } catch (NullPointerException e) {
            LOG.debug(String.format("No metadata items to add for node '%d'", 0, nodeId));
        }
        return createdNodeMeta;
    }

    @Override
    public List<NodeMeta> getNodeMetadataByAccountIdNodeId(Integer accountId, Integer nodeId) throws EntityNotFoundException {
        return nodeMetadataRepository.getNodeMetaDataByAccountIdNodeId(nodeId);
    }

    @Override
    public List<String> prepareForNodeMetadataDeletion(Integer accountId, Integer loadbalancerId, Integer nodeId, List<Integer> ids) throws EntityNotFoundException {
        List<String> validationErrors = new ArrayList<String>();
        List<Integer> nodeMetaIds = new ArrayList<Integer>();
        List<Integer> invalidIds = new ArrayList<Integer>();
        String format, errorMessage;
        Node currentNode;

        try {
            currentNode = nodeRepository.getNodeByAccountIdLoadBalancerIdNodeId(accountId, loadbalancerId, nodeId);
        } catch (Exception e) {
            validationErrors.add("Could not find node");
            return validationErrors;
        }

        for (NodeMeta meta : currentNode.getNodeMetadata()) {
            nodeMetaIds.add(meta.getId());
        }

        for (Integer id : ids) {
            if (!nodeMetaIds.contains(id)) {
                invalidIds.add(id);
            }
        }

        int batchDeleteLimit = accountLimitService.getLimit(accountId, AccountLimitType.BATCH_DELETE_LIMIT);

        if (ids.size() > batchDeleteLimit) {
            format = "Request to delete %d metadata items exceeds the account batch delete limit.\n" +
                    "BATCH_DELETE_LIMIT is %d";
            errorMessage = String.format(format, ids.size(), batchDeleteLimit);
            validationErrors.add(errorMessage);
        }

        if (!invalidIds.isEmpty()) {
            format = "NodeMetaData ids %s are not part of your node.";
            errorMessage = String.format(format, StringConverter.integersAsString(invalidIds));
            validationErrors.add(errorMessage);
        }

        return validationErrors;
    }

    @Override
    public void deleteNodeMetadata(Node node, Collection<Integer> ids) throws EntityNotFoundException {
        nodeMetadataRepository.deleteMetadata(node, ids);
    }

    @Override
    public NodeMeta getNodeMeta(Integer nodeId, Integer id) throws EntityNotFoundException {
        try {
            return nodeMetadataRepository.getNodeMeta(nodeId, id);
        } catch (Exception e) {
            throw new EntityNotFoundException("Node meta data " + id + " doesn't exist on node " + nodeId + ".");
        }
    }

    @Override
    public NodeMeta updateNodeMeta(Integer accountId, Integer loadbalancerId, Integer nodeId, NodeMeta callNodeMeta) throws EntityNotFoundException {
        Node node = nodeRepository.getNodeByAccountIdLoadBalancerIdNodeId(accountId, loadbalancerId, nodeId);
        List<Integer> ids = new ArrayList<Integer>();

        for (NodeMeta nodeMeta : node.getNodeMetadata()) {
            ids.add(nodeMeta.getId());
            if (nodeMeta.getId().equals(callNodeMeta.getId())) {
                callNodeMeta.setKey(nodeMeta.getKey());
            }
        }

        if (!ids.contains(callNodeMeta.getId())) {
            throw new EntityNotFoundException("Node meta with id " + callNodeMeta.getId() + " doesn't exist.");
        } else {
            callNodeMeta.setNode(node);
        }

        return nodeMetadataRepository.updateNodeMeta(node, callNodeMeta);
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