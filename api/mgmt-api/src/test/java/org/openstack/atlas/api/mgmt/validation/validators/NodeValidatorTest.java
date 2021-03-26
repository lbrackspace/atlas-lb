package org.openstack.atlas.api.mgmt.validation.validators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Node;

import static org.openstack.atlas.api.helpers.ResultMessage.resultMessage;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

@RunWith(Enclosed.class)
public class NodeValidatorTest {

    public static class whenUpdatingANode{

        NodeValidator nTest;
        Node node;
        org.openstack.atlas.service.domain.entities.Node dbNode;

        @Before
        public void setUp() {


            nTest = new NodeValidator();
            node = new Node();
            dbNode = new org.openstack.atlas.service.domain.entities.Node();
            node.setWeight(80);


        }

        @Test
        public void shouldAcceptValidNodeType() {
            node.setType("PRIMARY");
            node.setWeight(60);
            ValidatorResult result = nTest.validate(node, PUT);
            Assert.assertTrue(resultMessage(result, PUT), result.passedValidation());

        }

        @Test
        public void shouldDenyNodeWithNoInfo() {
            node.setWeight(null);
            ValidatorResult result = nTest.validate(node, PUT);
            Assert.assertFalse(resultMessage(result, PUT), result.passedValidation());

        }

        @Test
        public void shouldDenyNodeWithIncorrectType() {
            node.setType("INCORRECT");
            ValidatorResult result = nTest.validate(node, PUT);
            Assert.assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldDenyInvalidNodeStatus() {

            node.setStatus("INCORRECT");
            ValidatorResult result = nTest.validate(node, PUT);
            Assert.assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldAllowValidNodeStatus() {
            node.setStatus("ONLINE");
            node.setWeight(80);
            ValidatorResult result = nTest.validate(node, PUT);
            Assert.assertTrue(resultMessage(result, PUT), result.passedValidation());

        }

        @Test
        public void shouldAllowValidNodeCondition() {
            node.setCondition("ENABLED");
            node.setWeight(80);
            ValidatorResult result = nTest.validate(node, PUT);
            Assert.assertTrue(resultMessage(result, PUT), result.passedValidation());

        }


        @Test
        public void shouldDenyInvalidNodeCondition() {

            node.setCondition("INCORRECT");
            ValidatorResult result = nTest.validate(node, PUT);
            Assert.assertFalse(resultMessage(result, PUT), result.passedValidation());
        }






    }




}
