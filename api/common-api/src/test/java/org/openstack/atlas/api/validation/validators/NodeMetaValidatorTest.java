package org.openstack.atlas.api.validation.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeMeta;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

@RunWith(Enclosed.class)
public class NodeMetaValidatorTest {

    private final static int MAX_KEY_LENGTH = 32;
    private final static int MAX_VALUE_LENGTH = 256;

    public static class WhenValidatingPost {

        private NodeMeta NodeMeta;
        private NodeMetaValidator validator;

        @Before
        public void standUp() {
            validator = new NodeMetaValidator();

            NodeMeta = new NodeMeta();
            NodeMeta.setKey("NodeMetaKey1");
            NodeMeta.setValue("NodeMetaValue1");
        }

        @Test
        public void shouldAcceptValidNodeMeta() {
            assertTrue(validator.validate(NodeMeta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenIdIsSet() {
            NodeMeta.setId(1234);
            assertFalse(validator.validate(NodeMeta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenKeyIsNull() {
            NodeMeta.setKey(null);
            assertFalse(validator.validate(NodeMeta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenKeyIsEmpty() {
            NodeMeta.setKey("");
            assertFalse(validator.validate(NodeMeta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsNull() {
            NodeMeta.setValue(null);
            assertFalse(validator.validate(NodeMeta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsEmpty() {
            NodeMeta.setValue("");
            assertFalse(validator.validate(NodeMeta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenKeyIsEqualToMaxLength() {
            NodeMeta.setKey(createStringOfLength(MAX_KEY_LENGTH));
            assertTrue(validator.validate(NodeMeta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenKeyIsLongerThanMaxLength() {
            NodeMeta.setKey(createStringOfLength(MAX_KEY_LENGTH + 1));
            assertFalse(validator.validate(NodeMeta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsEqualToMaxLength() {
            NodeMeta.setValue(createStringOfLength(MAX_VALUE_LENGTH));
            assertTrue(validator.validate(NodeMeta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsLongerThanMaxLength() {
            NodeMeta.setValue(createStringOfLength(MAX_VALUE_LENGTH + 1));
            assertFalse(validator.validate(NodeMeta, POST).passedValidation());
        }
    }

    public static class WhenValidatingPut {

        private NodeMeta NodeMeta;
        private NodeMetaValidator validator;

        @Before
        public void standUp() {
            validator = new NodeMetaValidator();

            NodeMeta = new NodeMeta();
            NodeMeta.setValue("NodeMetaValue1");
        }

        @Test
        public void shouldAcceptValidNodeMeta() {
            assertTrue(validator.validate(NodeMeta, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenIdIsSet() {
            NodeMeta.setId(1234);
            assertFalse(validator.validate(NodeMeta, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsNull() {
            NodeMeta.setValue(null);
            assertFalse(validator.validate(NodeMeta, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsEmpty() {
            NodeMeta.setValue("");
            assertFalse(validator.validate(NodeMeta, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsEqualToMaxLength() {
            NodeMeta.setValue(createStringOfLength(MAX_VALUE_LENGTH));
            assertTrue(validator.validate(NodeMeta, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsLongerThanMaxLength() {
            NodeMeta.setValue(createStringOfLength(MAX_VALUE_LENGTH + 1));
            assertFalse(validator.validate(NodeMeta, PUT).passedValidation());
        }
    }

    private static String createStringOfLength(int length) {
        String string = new String();

        for(int i=0; i < length; i++) {
            string += "a";
        }

        return string;
    }
}
