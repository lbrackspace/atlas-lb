package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.Meta;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface NodeMetadataService {
    Set<Meta> createNodeMetadata(Integer accountId, Integer nodeId, Collection<Meta> NodeMetas) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    Set<Meta> getNodeMetadataByAccountIdLoadBalancerId(Integer accountId, Integer nodeId) throws EntityNotFoundException;

    Meta getNodeMeta(Integer accountId, Integer nodeId, Integer id) throws EntityNotFoundException;

    void deleteNodeMeta(Integer accountId, Integer nodeId, Integer id) throws EntityNotFoundException;

    void updateNodeMeta(Node node) throws EntityNotFoundException;

    List<String> prepareForNodeMetadataDeletion(Integer accountId, Integer nodeId, List<Integer> ids) throws EntityNotFoundException;

    Node deleteNodeMetadata(Node node, Collection<Integer> ids) throws EntityNotFoundException;
}