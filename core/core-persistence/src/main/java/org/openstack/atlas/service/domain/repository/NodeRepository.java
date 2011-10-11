package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.common.converters.StringConverter;
import org.openstack.atlas.service.domain.common.Constants;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.DeletedStatusException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojo.NodeMap;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

public interface NodeRepository {
    Set<Node> addNodes(LoadBalancer loadBalancer, Collection<Node> nodes);

    LoadBalancer update(LoadBalancer loadBalancer);

    Set<Node> getNodesByAccountIdLoadBalancerId(Integer loadBalancerId, Integer accountId) throws EntityNotFoundException;
}
