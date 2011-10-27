package org.openstack.atlas.service.domain.service;

import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.datamodel.AtlasTypeHelper;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.datamodel.CoreProtocolType;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.NodeRepository;
import org.openstack.atlas.service.domain.stub.StubFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.util.HashSet;
import java.util.Set;

@Ignore
@RunWith(Enclosed.class)
public class NodeServiceITest {

//    @RunWith(SpringJUnit4ClassRunner.class)
//    public static class WhenCreatingNodes extends Base {
//
//        @Test
//        public void shouldAssignIdNodeWhenCreateSucceeds() throws Exception {
//            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
//            dbLoadBalancer = loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ACTIVE);
//
//            Set<Node> nodes = new HashSet<Node>();
//            Node node = new Node();
//            node.setAddress("10.2.2.2");
//            node.setPort(90);
//            nodes.add(node);
//
//            LoadBalancer lb = new LoadBalancer();
//            lb.setAccountId(dbLoadBalancer.getAccountId());
//            lb.setId(dbLoadBalancer.getId());
//            lb.setNodes(nodes);
//
//            nodeService.createNodes(lb);
//
//            LoadBalancer updatedLb = loadBalancerRepository.getByIdAndAccountId(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());
//
//            Assert.assertEquals(2, updatedLb.getNodes().size());
//
//        }
//TODO: more tests...
//    }
}

