package org.openstack.atlas.api.validation.verifiers;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.validation.SimpleBean;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

@RunWith(Enclosed.class)
public class RegexValidatorVerifierTest {

    public static class WhenVerifyingRegex {

        private SimpleBean testObjectA;
        private SimpleBean testObjectB;
        private SimpleBean testObjectStar;
        private SimpleBean testObjectLookbehind;
        private SimpleBean testObjectGreedy;

        @Before
        public void standUp() {
            //Uses JREGEX to validate based off of the perl libraries...
            testObjectA = new SimpleBean("^.*$", "2", "3", 1, 2, 3);
            testObjectB = new SimpleBean("^*$", null, null, null, null, null);
            testObjectStar = new SimpleBean("*", null, null, null, null, null);
            testObjectLookbehind = new SimpleBean("(?<!ab{2,4}c{3,5}d)", null, null, null, null, null);
            testObjectGreedy = new SimpleBean("(.*)++", null, null, null, null, null);
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

        @Test
        public void shouldFailIfOnlyStar() {
            final Validator<SimpleBean> validator = build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                {
                    result(validationTarget().getStringProperty1()).if_().exist().then().must().adhereTo(new RegexValidatorVerifier()).withMessage("Must provide a valid body regex");
                }
            });
            assertTrue(validator.validate(testObjectStar, HttpRequestType.POST).getValidationResults().size() == 2);
            assertFalse(validator.validate(testObjectStar, HttpRequestType.POST).passedValidation());
            ValidatorResult r = validator.validate(testObjectStar, HttpRequestType.POST);
            assertTrue(r.getValidationResults().get(1).getMessage().contains("missing term before *"));
        }

        @Test
        public void shouldFailIfLookbehindNotFixedLen() {
            final Validator<SimpleBean> validator = build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                {
                    result(validationTarget().getStringProperty1()).if_().exist().then().must().adhereTo(new RegexValidatorVerifier()).withMessage("Must provide a valid body regex");
                }
            });
            assertTrue(validator.validate(testObjectLookbehind, HttpRequestType.POST).getValidationResults().size() == 2);
            assertFalse(validator.validate(testObjectLookbehind, HttpRequestType.POST).passedValidation());
            ValidatorResult r = validator.validate(testObjectLookbehind, HttpRequestType.POST);
            assertTrue(r.getValidationResults().get(1).getMessage().contains("variable length element within a lookbehind assertion"));
            assertFalse(validator.validate(testObjectLookbehind, HttpRequestType.POST).passedValidation());
        }

        @Test
        public void shouldFailIfGreedy() {
            final Validator<SimpleBean> validator = build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                {
                    result(validationTarget().getStringProperty1()).if_().exist().then().must().adhereTo(new RegexValidatorVerifier()).withMessage("Must provide a valid body regex");
                }
            });
            assertTrue(validator.validate(testObjectGreedy, HttpRequestType.POST).getValidationResults().size() == 2);
            assertFalse(validator.validate(testObjectGreedy, HttpRequestType.POST).passedValidation());
            ValidatorResult r = validator.validate(testObjectGreedy, HttpRequestType.POST);
            assertTrue(r.getValidationResults().get(1).getMessage().contains("can't iterate this type: 32"));
            assertFalse(validator.validate(testObjectGreedy, HttpRequestType.POST).passedValidation());
        }
    }
}
