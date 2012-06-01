package org.openstack.atlas.api.validation.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

@RunWith(Enclosed.class)
public class MetaValidatorTest {

    private final static int MAX_KEY_LENGTH = 32;
    private final static int MAX_VALUE_LENGTH = 256;

    public static class WhenValidatingPost {

        private Meta Meta;
        private MetaValidator validator;

        @Before
        public void standUp() {
            validator = new MetaValidator();

            Meta = new Meta();
            Meta.setKey("NodeMetaKey1");
            Meta.setValue("NodeMetaValue1");
        }

        @Test
        public void shouldAcceptValidNodeMeta() {
            assertTrue(validator.validate(Meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenIdIsSet() {
            Meta.setId(1234);
            assertFalse(validator.validate(Meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenKeyIsNull() {
            Meta.setKey(null);
            assertFalse(validator.validate(Meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenKeyIsEmpty() {
            Meta.setKey("");
            assertFalse(validator.validate(Meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsNull() {
            Meta.setValue(null);
            assertFalse(validator.validate(Meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsEmpty() {
            Meta.setValue("");
            assertFalse(validator.validate(Meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenKeyIsEqualToMaxLength() {
            Meta.setKey(createStringOfLength(MAX_KEY_LENGTH));
            assertTrue(validator.validate(Meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenKeyIsLongerThanMaxLength() {
            Meta.setKey(createStringOfLength(MAX_KEY_LENGTH + 1));
            assertFalse(validator.validate(Meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsEqualToMaxLength() {
            Meta.setValue(createStringOfLength(MAX_VALUE_LENGTH));
            assertTrue(validator.validate(Meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsLongerThanMaxLength() {
            Meta.setValue(createStringOfLength(MAX_VALUE_LENGTH + 1));
            assertFalse(validator.validate(Meta, POST).passedValidation());
        }
    }

    public static class WhenValidatingPut {

        private Meta Meta;
        private MetaValidator validator;

        @Before
        public void standUp() {
            validator = new MetaValidator();

            Meta = new Meta();
            Meta.setValue("NodeMetaValue1");
        }

        @Test
        public void shouldAcceptValidNodeMeta() {
            assertTrue(validator.validate(Meta, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenIdIsSet() {
            Meta.setId(1234);
            assertFalse(validator.validate(Meta, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsNull() {
            Meta.setValue(null);
            assertFalse(validator.validate(Meta, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsEmpty() {
            Meta.setValue("");
            assertFalse(validator.validate(Meta, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsEqualToMaxLength() {
            Meta.setValue(createStringOfLength(MAX_VALUE_LENGTH));
            assertTrue(validator.validate(Meta, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsLongerThanMaxLength() {
            Meta.setValue(createStringOfLength(MAX_VALUE_LENGTH + 1));
            assertFalse(validator.validate(Meta, PUT).passedValidation());
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
