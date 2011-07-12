package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.SimpleBean;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class MustBeEmptyTest {

    public static class WhenVerifyingMustBeEmpty {

        private SimpleBean testObjectA;
        private SimpleBean testObjectB;

        @Before
        public void standUp() {
            testObjectA = new SimpleBean("1", "2", "3", 1, 2, 3);
            testObjectB = new SimpleBean(null, null, null, null, null, null);
        }

        @Test
        public void shouldValidateNotNull() {
            final Validator<SimpleBean> validator = build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                {
                    result(validationTarget().getIntProperty1()).must().not().beEmptyOrNull().withMessage("Must not be empty or null");
                }
            });

            assertTrue(validator.validate(testObjectA, HttpRequestType.POST).passedValidation());
            assertFalse(validator.validate(testObjectB, HttpRequestType.POST).passedValidation());
        }
    }
}
