package org.opestack.atlas.api.validation.validator;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.NodeValidator;
import org.openstack.atlas.api.validation.validator.builder.NodeValidatorBuilder;
import org.openstack.atlas.core.api.v1.Node;
import org.openstack.atlas.datamodel.CoreNodeStatus;
import org.openstack.atlas.service.domain.stub.StubFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

@RunWith(Enclosed.class)
public class NodeValidatorTest {

    public static class WhenValidatingPostContext {
        private Node node;
        private NodeValidator validator;

        @Before
        public void standUp() {
            validator = new NodeValidator(new NodeValidatorBuilder());
            node = StubFactory.createMinimalDataModelNodeForPost();
        }

        @Test
        public void shouldAcceptValidMinimalNode() {
            ValidatorResult result = validator.validate(node, POST);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectNullIp() {
            node.setAddress(null);
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectInvalidIPv4Address1() {
            node.setAddress("0.0.0.0");
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectInvalidIPv4Address2() {
            node.setAddress("255.255.255.255");
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
        public void shouldRejectIpv4CidrAddress() {
            node.setAddress("123.123.123.123/24");
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldAcceptValidIPv6Address() {
            node.setAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
            ValidatorResult result = validator.validate(node, POST);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectInvalidIPv6Address() {
            node.setAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334:::");
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectIpv6CidrAddress() {
            node.setAddress("ffff:ffff:ffff:ffff::/64");
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
        public void shouldRejectInvalidPort1() {
            node.setPort(0);
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectInvalidPort2() {
            node.setPort(65536);
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldAcceptEnabledAndDisabled() {
            node.setEnabled(true);
            ValidatorResult result = validator.validate(node, POST);
            assertTrue(result.passedValidation());

            node.setEnabled(false);
            result = validator.validate(node, POST);
            assertTrue(result.passedValidation());
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
            node.setId(1234);
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenStatusIsSetToCoreNodeStatus() {
            for (String nodeStatus : CoreNodeStatus.values()) {
                node.setStatus(nodeStatus);
                ValidatorResult result = validator.validate(node, POST);
                assertFalse(result.passedValidation());
            }
        }

        @Test
        public void shouldRejectWhenStatusIsSetToErroneousNodeStatus() {
            node.setStatus("SOME_BOGUS_STATUS");
            ValidatorResult result = validator.validate(node, POST);
            assertFalse(result.passedValidation());
        }
    }


    public static class WhenValidatingPutContext {
        private Node node;
        private NodeValidator validator;

        @Before
        public void setUp() {
            validator = new NodeValidator(new NodeValidatorBuilder());

            node = new Node();
            node.setEnabled(true);
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
            node.setEnabled(null);
            ValidatorResult result = validator.validate(node, PUT);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenMissingAllAttributes() {
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
            node.setId(1234);
            ValidatorResult result = validator.validate(node, PUT);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenStatusIsSetToCoreNodeStatus() {
            for (String nodeStatus : CoreNodeStatus.values()) {
                node.setStatus(nodeStatus);
                ValidatorResult result = validator.validate(node, PUT);
                assertFalse(result.passedValidation());
            }
        }

        @Test
        public void shouldRejectWhenStatusIsSetToErroneousNodeStatus() {
            node.setStatus("SOME_BOGUS_STATUS");
            ValidatorResult result = validator.validate(node, PUT);
            assertFalse(result.passedValidation());
        }
    }
}
