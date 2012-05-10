package org.openstack.atlas.api.validation.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadbalancerMeta;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

@RunWith(Enclosed.class)
public class LoadbalancerMetaValidatorTest {

    private final static int MAX_KEY_LENGTH = 32;
    private final static int MAX_VALUE_LENGTH = 256;

    public static class WhenValidatingPost {

        private LoadbalancerMeta meta;
        private LoadbalancerMetaValidator validatorLoadbalancer;

        @Before
        public void standUp() {
            validatorLoadbalancer = new LoadbalancerMetaValidator();

            meta = new LoadbalancerMeta();
            meta.setKey("metaKey1");
            meta.setValue("metaValue1");
        }

        @Test
        public void shouldAcceptValidLoadbalancerMeta() {
            assertTrue(validatorLoadbalancer.validate(meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenIdIsSet() {
            meta.setId(1234);
            assertFalse(validatorLoadbalancer.validate(meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenKeyIsNull() {
            meta.setKey(null);
            assertFalse(validatorLoadbalancer.validate(meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenKeyIsEmpty() {
            meta.setKey("");
            assertFalse(validatorLoadbalancer.validate(meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsNull() {
            meta.setValue(null);
            assertFalse(validatorLoadbalancer.validate(meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsEmpty() {
            meta.setValue("");
            assertFalse(validatorLoadbalancer.validate(meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenKeyIsEqualToMaxLength() {
            meta.setKey(createStringOfLength(MAX_KEY_LENGTH));
            assertTrue(validatorLoadbalancer.validate(meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenKeyIsLongerThanMaxLength() {
            meta.setKey(createStringOfLength(MAX_KEY_LENGTH + 1));
            assertFalse(validatorLoadbalancer.validate(meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsEqualToMaxLength() {
            meta.setValue(createStringOfLength(MAX_VALUE_LENGTH));
            assertTrue(validatorLoadbalancer.validate(meta, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsLongerThanMaxLength() {
            meta.setValue(createStringOfLength(MAX_VALUE_LENGTH + 1));
            assertFalse(validatorLoadbalancer.validate(meta, POST).passedValidation());
        }
    }

    public static class WhenValidatingPut {

        private LoadbalancerMeta meta;
        private LoadbalancerMetaValidator validatorLoadbalancer;

        @Before
        public void standUp() {
            validatorLoadbalancer = new LoadbalancerMetaValidator();

            meta = new LoadbalancerMeta();
            meta.setValue("metaValue1");
        }

        @Test
        public void shouldAcceptValidLoadbalancerMeta() {
            assertTrue(validatorLoadbalancer.validate(meta, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenIdIsSet() {
            meta.setId(1234);
            assertFalse(validatorLoadbalancer.validate(meta, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsNull() {
            meta.setValue(null);
            assertFalse(validatorLoadbalancer.validate(meta, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsEmpty() {
            meta.setValue("");
            assertFalse(validatorLoadbalancer.validate(meta, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsEqualToMaxLength() {
            meta.setValue(createStringOfLength(MAX_VALUE_LENGTH));
            assertTrue(validatorLoadbalancer.validate(meta, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenValueIsLongerThanMaxLength() {
            meta.setValue(createStringOfLength(MAX_VALUE_LENGTH + 1));
            assertFalse(validatorLoadbalancer.validate(meta, PUT).passedValidation());
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
