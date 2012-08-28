package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterStatus;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.DataCenter;
import org.openstack.atlas.service.domain.repository.ClusterRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import org.junit.Ignore;

@Ignore
@RunWith(Enclosed.class)
public class ClusterServiceImplIntegrationTest {

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations={"classpath:db-services-test.xml"})
    @Transactional
    public static class WhenAddingCluster {

        @Autowired
        private ClusterRepository clusterRepository;

        @Autowired
        private ClusterService clusterService;

        @PersistenceContext(unitName = "loadbalancing")
        private EntityManager entityManager;


        private Cluster cluster;

        @Before
        public void setUp() {

        }

        @After
        public void tearDown() {

        }

        @Test
        public void shouldAddClusterWhenOperationSucceeds() throws Exception {
            List<Cluster> clusters = clusterService.getAll();

            cluster = new Cluster();
            cluster.setDataCenter(DataCenter.DFW);
            cluster.setDescription("cluster description");
            cluster.setName("my cluster name");
            cluster.setPassword("cluster password");
            cluster.setUsername("cluster username");
            cluster.setStatus(ClusterStatus.ACTIVE);
            clusterRepository.save(cluster);

            Assert.assertEquals((clusters.size() + 1), clusterService.getAll().size());
        }
    }

}

