package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeStatus;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class NodeValidatorTest {

    public static class WhenValidatingPost {

        private Node node;
        private NodeValidator validator;

        @Before
        public void standUp() {
            validator = new NodeValidator();

            node = new Node();
            node.setAddress("1.0.5.5");
            node.setPort(80);
            node.setCondition(NodeCondition.ENABLED);
            node.setWeight(1);
        }

        @Test
        public void shouldAcceptValidNode() {
            assertTrue(validator.validate(node, POST).passedValidation());
        }

        @Test
        public void shouldRejectCidrIpAddress() {
            node.setAddress("10.1.1.1/32");
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectNullIp() {
            node.setAddress(null);
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectInvalidIPv4Address() {
            Node node1 = new Node();
            node1.setAddress("0.0.0.0");
            ValidatorResult result = validator.validate(node1, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectInvalidIPv6Address() {
            //Will now be validated against domain node verification, characters are expected now...
            node.setAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334:::");
            ValidatorResult result = validator.validate(node, POST);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldAcceptValidIPv6Address() {
            node.setAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
            ValidatorResult result = validator.validate(node, POST);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectIpv4CidrAddress() {
            //Will now be validated against domain node verification, characters are expected now...
            ValidatorResult result = validator.validate(node, POST);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectIpv6CidrAddress() {
            //Will now be validated against domain node verification, characters are expected now...
            node.setAddress("ffff:ffff:ffff:ffff::/64");
            ValidatorResult result = validator.validate(node, POST);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectInvalidIp() {
            node.setAddress("0.0.0.0.0");
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldReject127LoopBackIps() {
            node.setAddress("127.64.0.0");
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectAll255s() {
            node.setAddress("255.255.255.255");
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectSubnetIp() {
            node.setAddress("0.0.0.0");
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectNullPort() {
            node.setPort(null);
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectInvalidPort() {
            node.setPort(0);
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectNullCondition() {
            node.setCondition(null);
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectZeroWeight() {
            node.setWeight(0);
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWeightJustAboveCeiling() {
            node.setWeight(101);
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectNegativeWeight() {
            node.setWeight(-1);
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenIdIsSet() {
            node.setId(1);
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenStatusIsSet() {
            node.setStatus(NodeStatus.OFFLINE);
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

    }

    public static class whenValidatingPut {

        private Node node;
        private NodeValidator validator;

        @Before
        public void setUp() {
            validator = new NodeValidator();

            node = new Node();
            node.setCondition(NodeCondition.ENABLED);
            node.setWeight(1);
        }

        @Test
        public void shouldAcceptValidNode() {
            ValidatorResult result = validator.validate(node, PUT);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldAcceptWhenOnlyConditionIsSet() {
            node.setWeight(null);
            ValidatorResult result = validator.validate(node, PUT);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldAcceptWhenOnlyWeightIsSet() {
            node.setCondition(null);
            ValidatorResult result = validator.validate(node, PUT);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenMissingAttributes() {
            ValidatorResult result = validator.validate(new Node(), PUT);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectIpAddress() {
            node.setAddress("10.10.10.100");
            ValidatorResult result = validator.validate(node, PUT);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectPort() {
            node.setPort(80);
            ValidatorResult result = validator.validate(node, PUT);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectZeroForWeight() {
            node.setWeight(0);
            ValidatorResult result = validator.validate(node, PUT);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWeightJustAboveCeiling() {
            node.setWeight(101);
            ValidatorResult result = validator.validate(node, PUT);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectNegativeWeight() {
            node.setWeight(-1);
            ValidatorResult result = validator.validate(node, PUT);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenIdIsSet() {
            node.setId(1);
            ValidatorResult result = validator.validate(node, PUT);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenStatusIsSet() {
            node.setStatus(NodeStatus.OFFLINE);
            ValidatorResult result = validator.validate(node, PUT);
            assertFalse(result.passedValidation());
        }
    }
}
