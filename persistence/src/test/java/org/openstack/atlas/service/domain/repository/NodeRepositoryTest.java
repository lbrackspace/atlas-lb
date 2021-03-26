package org.openstack.atlas.service.domain.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class NodeRepositoryTest {

    public static class whenRetrievingNode {


        @Mock
        EntityManager entityManager;
        @Mock
        Query qry;
        @InjectMocks
        NodeRepository nodeRepository;

        Node node;
        List<Node> nodes;


        @Before
        public void standUp() {

            MockitoAnnotations.initMocks(this);

            node = new Node();
            node.setId(1);
            nodes = new ArrayList<>();
            nodes.add(node);
            when(entityManager.createQuery(anyString())).thenReturn(qry);
            when(qry.setParameter("id", 1)).thenReturn(qry);
            when(qry.getResultList()).thenReturn(nodes);

        }

        @Test
        public void shouldReturnNode() throws Exception {
            Node dbNode = nodeRepository.getById(1);
            Assert.assertEquals(node, dbNode);

        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowEntityNotFoundWithEmptyNodeList() throws EntityNotFoundException {
            nodes.clear();
            nodeRepository.getById(1);
        }
    }
}
