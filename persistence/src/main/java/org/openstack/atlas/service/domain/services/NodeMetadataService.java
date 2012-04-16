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
    List<NodeMeta> createNodeMetadata(Integer accountId, Integer nodeId, List<NodeMeta> NodeMetas) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    List<NodeMeta> getNodeMetadataByAccountIdNodeId(Integer accountId, Integer nodeId) throws EntityNotFoundException;

    void deleteNodeMeta(Integer accountId, Integer nodeId, Integer id) throws EntityNotFoundException;

    void updateNodeMeta(Node node) throws EntityNotFoundException;

    List<String> prepareForNodeMetadataDeletion(Integer accountId, Integer nodeId, List<Integer> ids) throws EntityNotFoundException;

    Node deleteNodeMetadata(Node node, Collection<Integer> ids) throws EntityNotFoundException;
}