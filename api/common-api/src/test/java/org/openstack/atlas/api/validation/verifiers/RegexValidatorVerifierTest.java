package org.openstack.atlas.api.validation.verifiers;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.validation.SimpleBean;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.context.HttpRequestType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

@RunWith(Enclosed.class)
public class RegexValidatorVerifierTest {

    public static class WhenVerifyingRegex {

        private SimpleBean testObjectA;
        private SimpleBean testObjectB;

        @Before
        public void standUp() {
            testObjectA = new SimpleBean("^.*$", "2", "3", 1, 2, 3);
            testObjectB = new SimpleBean("^*$", null, null, null, null, null);
        }

        @Test
        public void shouldValidateNotNull() {
            final Validator<SimpleBean> validator = build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                {
                    result(validationTarget().getStringProperty1()).if_().exist().then().must().adhereTo(new RegexValidatorVerifier()).withMessage("Must provide a valid body regex");
                }
            });

            assertTrue(validator.validate(testObjectA, HttpRequestType.POST).passedValidation());
            assertFalse(validator.validate(testObjectB, HttpRequestType.POST).passedValidation());
        }
    }
}
