package org.openstack.atlas.api.validation.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class MetadataValidatorTest {

    public static class WhenValidatingPost {

        private MetadataValidator validator;
        private Metadata metadata;
        private Meta meta1;

        @Before
        public void setUpValidMetadataObject() {
            validator = new MetadataValidator();
            metadata = new Metadata();

            meta1 = new Meta();
            meta1.setKey("metaKey1");
            meta1.setValue("metaValue1");

            metadata.getMetas().add(meta1);
        }

        @Test
        public void shouldAcceptValidMetadataObject() {
            ValidatorResult result = validator.validate(metadata, HttpRequestType.POST);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectNullMetadataObject() {
            assertFalse(validator.validate(null, HttpRequestType.POST).passedValidation());
        }

        @Test
        public void shouldRejectMetadataObjectWithNoMetas() {
            assertFalse(validator.validate(new Metadata(), HttpRequestType.POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenPassingInDuplicateKeys() {
            Meta metaWithDuplicateKey = meta1;
            metadata.getMetas().add(metaWithDuplicateKey);
            assertFalse(validator.validate(metadata, HttpRequestType.POST).passedValidation());
        }
    }
}
