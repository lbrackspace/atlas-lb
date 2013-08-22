package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class NodesValidatorTest {

    public static class WhenValidatingPost {

        private NodesValidator validator;
        private Nodes nodes;
        private Node node1;

        @Before
        public void setUpValidNodesObject() {
            validator = new NodesValidator();
            nodes = new Nodes();

            node1 = new Node();
            node1.setAddress("10.1.1.1");
            node1.setPort(80);
            node1.setCondition(NodeCondition.ENABLED);

            nodes.getNodes().add(node1);
        }

        @Test
        public void shouldAcceptValidNodesObject() {
            ValidatorResult result = validator.validate(nodes, HttpRequestType.POST);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectNullNodesObject() {
            assertFalse(validator.validate(null, HttpRequestType.POST).passedValidation());
        }

        @Test
        public void shouldAcceptNodesObjectWithNoNodes() {
            assertTrue(validator.validate(new Nodes(), HttpRequestType.POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenPassingInDuplicateNodes() {
            Node duplicateNode = node1;
            nodes.getNodes().add(duplicateNode);
            assertFalse(validator.validate(nodes, HttpRequestType.POST).passedValidation());
        }
    }
}
