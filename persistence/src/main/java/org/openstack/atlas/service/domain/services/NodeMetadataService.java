package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeMeta;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;

import java.util.Collection;
import java.util.List;

public interface NodeMetadataService {
    List<NodeMeta> createNodeMetadata(Integer accountId, Integer loadbalancerId, Integer nodeId, List<NodeMeta> NodeMetas) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    List<NodeMeta> getNodeMetadataByAccountIdNodeId(Integer accountId, Integer nodeId) throws EntityNotFoundException;

    List<String> prepareForNodeMetadataDeletion(Integer accountId, Integer loadbalancerId, Integer nodeId, List<Integer> ids) throws EntityNotFoundException;

    void deleteNodeMetadata(Node node, Collection<Integer> ids) throws EntityNotFoundException;

    NodeMeta getNodeMeta(Integer nodeId, Integer id) throws EntityNotFoundException;

    NodeMeta updateNodeMeta(Integer accountId, Integer loadbalancerId, Integer nodeId, NodeMeta callNodeMeta) throws EntityNotFoundException;
}