package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Cluster;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterStatus;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.DataCenter;
import org.openstack.atlas.api.mgmt.validation.validators.ClusterValidator;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.openstack.atlas.api.helpers.ResultMessage.resultMessage;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class ClusterValidatorTest {

    public static class WhenValidatingPost {

        private ClusterValidator cTest;
        private Cluster cluster;

        @Before
        public void standUp() {
            cTest = new ClusterValidator();

            cluster = new Cluster();
            cluster.setDescription("a-new-cluster");
            cluster.setName("a-cluster-name");
            cluster.setDataCenter(DataCenter.DFW);
            cluster.setUsername("username");
            cluster.setPassword("password");
            cluster.setStatus(ClusterStatus.ACTIVE);
        }

        @Test
        public void shouldAcceptValidCluster() {
            ValidatorResult result = cTest.validate(cluster, POST);
            assertTrue(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectNullCluster() {
            ValidatorResult result = cTest.validate(null, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectNullName() {
            cluster.setName(null);
            ValidatorResult result = cTest.validate(cluster, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectNullDescription() {
            cluster.setDescription(null);
            ValidatorResult result = cTest.validate(cluster, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectNullDataCenter() {
            cluster.setDataCenter(null);
            ValidatorResult result = cTest.validate(cluster, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectId() {
            cluster.setId(2);
            ValidatorResult result = cTest.validate(cluster, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectHostMachines() {
            cluster.setNumberOfHostMachines(2);
            ValidatorResult result = cTest.validate(cluster, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectLbConfigs() {
            cluster.setNumberOfLoadBalancingConfigurations(2);
            ValidatorResult result = cTest.validate(cluster, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectUniqueCustomers() {
            cluster.setNumberOfUniqueCustomers(2);
            ValidatorResult result = cTest.validate(cluster, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectUtiliztation() {
            cluster.setUtilization("1%");
            ValidatorResult result = cTest.validate(cluster, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }
    }

    public static class WhenValidatingPut {

        private ClusterValidator cTest;
        private Cluster cluster;

        @Before
        public void setUpValidObject() {
            cTest = new ClusterValidator();

            cluster = new Cluster();
            cluster.setDescription("a-desc");
            cluster.setName("aName");
        }

        @Test
        public void shouldAcceptValidCluster() {
            ValidatorResult result = cTest.validate(cluster, PUT);
            assertTrue(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectNullCluster() {
            ValidatorResult result = cTest.validate(null, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectNullName() {
            cluster.setName(null);
            ValidatorResult result = cTest.validate(cluster, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectNullDescription() {
            cluster.setDescription(null);
            ValidatorResult result = cTest.validate(cluster, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectDataCenter() {
            cluster.setDataCenter(DataCenter.LON);
            ValidatorResult result = cTest.validate(cluster, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectHostMachines() {
            cluster.setNumberOfHostMachines(2);
            ValidatorResult result = cTest.validate(cluster, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectLbConfigs() {
            cluster.setNumberOfLoadBalancingConfigurations(2);
            ValidatorResult result = cTest.validate(cluster, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectUniqueCustomers() {
            cluster.setNumberOfUniqueCustomers(2);
            ValidatorResult result = cTest.validate(cluster, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectUtiliztation() {
            cluster.setUtilization("1%");
            ValidatorResult result = cTest.validate(cluster, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectId() {
            cluster.setId(2);
            ValidatorResult result = cTest.validate(cluster, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }
    }
}
