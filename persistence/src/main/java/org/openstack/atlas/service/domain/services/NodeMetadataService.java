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

    Node deleteNodeMetadata(Node node, Collection<Integer> ids) throws EntityNotFoundException;

    NodeMeta getNodeMeta(Integer nodeId, Integer id) throws EntityNotFoundException;

    List<NodeMeta> deleteNodeMeta(Integer accountId, Integer loadbalancerId, Integer nodeId, Integer id) throws EntityNotFoundException;

    List<NodeMeta> updateNodeMeta(Node node) throws EntityNotFoundException;
}

/*
    Set<Meta> createMetadata(Integer accountId, Integer loadBalancerId, Collection<Meta> metas) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    Set<Meta> getMetadataByAccountIdLoadBalancerId(Integer accountId, Integer loadBalancerId) throws EntityNotFoundException;

    Meta getMeta(Integer accountId, Integer loadBalancerId, Integer id) throws EntityNotFoundException;

    void deleteMeta(Integer accountId, Integer loadBalancerId, Integer id) throws EntityNotFoundException;

    void updateMeta(LoadBalancer domainLb) throws EntityNotFoundException;

    List<String> prepareForMetadataDeletion(Integer accountId, Integer loadBalancerId, List<Integer> ids) throws EntityNotFoundException;

    LoadBalancer deleteMetadata(LoadBalancer lb, Collection<Integer> ids) throws EntityNotFoundException;
*/