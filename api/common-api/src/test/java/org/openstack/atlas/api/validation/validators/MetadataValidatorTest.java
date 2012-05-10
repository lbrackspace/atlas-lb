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
        private LoadbalancerMetadata metadata;
        private LoadbalancerMeta meta1;

        @Before
        public void setUpValidMetadataObject() {
            validator = new MetadataValidator();
            metadata = new LoadbalancerMetadata();

            meta1 = new LoadbalancerMeta();
            meta1.setKey("metaKey1");
            meta1.setValue("metaValue1");

            metadata.getLoadbalancerMetas().add(meta1);
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
            assertFalse(validator.validate(new LoadbalancerMetadata(), HttpRequestType.POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenPassingInDuplicateKeys() {
            LoadbalancerMeta metaWithDuplicateKey = meta1;
            metadata.getLoadbalancerMetas().add(metaWithDuplicateKey);
            assertFalse(validator.validate(metadata, HttpRequestType.POST).passedValidation());
        }
    }
}
