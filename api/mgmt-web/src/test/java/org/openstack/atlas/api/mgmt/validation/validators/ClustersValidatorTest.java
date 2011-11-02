package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Cluster;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterStatus;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Clusters;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.DataCenter;
import org.openstack.atlas.api.mgmt.validation.validators.ClusterValidator;
import org.openstack.atlas.api.mgmt.validation.validators.ClustersValidator;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.openstack.atlas.api.helpers.ResultMessage.resultMessage;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class ClustersValidatorTest {

    public static class WhenValidatingClusters {

        private ClustersValidator clustersValidator;
        private ClusterValidator clusterValidator;
        private Clusters clusters;
        private Cluster cluster;

        @Before
        public void setUpValidVipsObject() {
            clustersValidator = new ClustersValidator();
            clusters = new Clusters();

            cluster = new Cluster();
            cluster.setDataCenter(DataCenter.DFW);
            cluster.setDescription("aDesc");
            cluster.setName("aName");
            cluster.setUsername("username");
            cluster.setPassword("password");
            cluster.setStatus(ClusterStatus.ACTIVE);

            clusters.getClusters().add(cluster);
        }

        @Test
        public void shouldAcceptValidClustersObject() {
            ValidatorResult result = clustersValidator.validate(clusters, HttpRequestType.POST);
            assertTrue(resultMessage(result, HttpRequestType.POST), result.passedValidation());
        }

        @Test
        public void shouldRejectNullClustersObject() {
            ValidatorResult result = clustersValidator.validate(null, HttpRequestType.POST);
            assertFalse(resultMessage(result, HttpRequestType.POST), result.passedValidation());
        }

        @Test
        public void shouldRejectClustersObjectWithNoCluster() {
            ValidatorResult result = clustersValidator.validate(new Clusters(), HttpRequestType.POST);
            assertFalse(resultMessage(result, HttpRequestType.POST), result.passedValidation());
        }
    }
}
