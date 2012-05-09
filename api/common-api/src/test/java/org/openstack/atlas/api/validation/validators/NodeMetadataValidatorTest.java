package org.openstack.atlas.api.validation.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeMetadata;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeMeta;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class NodeMetadataValidatorTest {

    public static class WhenValidatingPost {

        private NodeMetadataValidator validator;
        private NodeMetadata metadata;
        private NodeMeta meta1;

        @Before
        public void setUpValidMetadataObject() {
            validator = new NodeMetadataValidator();
            metadata = new NodeMetadata();

            meta1 = new NodeMeta();
            meta1.setKey("metaKey1");
            meta1.setValue("metaValue1");

            metadata.getNodeMetas().add(meta1);
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
            assertFalse(validator.validate(new NodeMetadata(), HttpRequestType.POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenPassingInDuplicateKeys() {
            NodeMeta metaWithDuplicateKey = meta1;
            metadata.getNodeMetas().add(metaWithDuplicateKey);
            assertFalse(validator.validate(metadata, HttpRequestType.POST).passedValidation());
        }
    }
}
