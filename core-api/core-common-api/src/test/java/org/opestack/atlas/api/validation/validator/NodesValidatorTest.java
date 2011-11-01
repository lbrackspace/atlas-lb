package org.opestack.atlas.api.validation.validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.NodesValidator;
import org.openstack.atlas.api.validation.validator.builder.NodeValidatorBuilder;
import org.openstack.atlas.api.validation.validator.builder.NodesValidatorBuilder;
import org.openstack.atlas.core.api.v1.Node;
import org.openstack.atlas.core.api.v1.Nodes;
import org.openstack.atlas.service.domain.stub.StubFactory;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

@RunWith(Enclosed.class)
public class NodesValidatorTest {

    public static class WhenValidatingPostContext {
        private NodesValidator validator;
        private Nodes nodes;

        @Before
        public void setUp() {
            nodes = StubFactory.createMinimalDataModelNodesForPost();
            validator = new NodesValidator(
                    new NodesValidatorBuilder(
                            new NodeValidatorBuilder()));
        }

        @Test
        public void shouldPassValidationWhenGivenAValidMinimalNodesObject() {
            ValidatorResult result = validator.validate(nodes, POST);
            Assert.assertTrue(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenNodesObjectIsNull() {
            nodes.getNodes().clear();
            ValidatorResult result = validator.validate(null, POST);
            Assert.assertFalse(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenNoNodesAreGiven() {
            nodes.getNodes().clear();
            ValidatorResult result = validator.validate(nodes, POST);
            Assert.assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenPassingInDuplicateNodes() {
            Nodes duplicateNodes = StubFactory.createMinimalDataModelNodesForPost();
            nodes.getNodes().addAll(duplicateNodes.getNodes());
            ValidatorResult result = validator.validate(nodes, POST);
            Assert.assertFalse(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenPassingInMoreThanMaxNodes() {
            Nodes nodes = new Nodes();
            int maxNodes = 25;

            for (int i = 0; i < maxNodes + 1; i++) {
                Node node = new Node();
                node.setAddress("1.1.1." + String.valueOf(i));
                node.setPort(i+1);
                nodes.getNodes().add(node);
            }

            ValidatorResult result = validator.validate(nodes, POST);
            Assert.assertFalse(result.passedValidation());
        }

        @Test
        public void shouldPassValidationWhenGivenAValidHydratedNodesObject() {
            nodes = StubFactory.createHydratedDataModelNodesForPost();
            ValidatorResult result = validator.validate(nodes, POST);
            Assert.assertTrue(result.passedValidation());
        }
    }
}
