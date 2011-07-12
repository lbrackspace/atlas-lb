package org.openstack.atlas.api.validation;

import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.exceptions.UnfinishedExpectationChainException;
import org.openstack.atlas.api.validation.exceptions.ValidationChainExecutionException;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class ValidatorTest {

    public static class WhenBuildingValidators {

        @Test(expected = IllegalArgumentException.class)
        public void shouldNotAcceptObjectsThatArenNotUnderInterceptorControl() {
            build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                {
                    result("".toCharArray()).must().beEmptyOrNull();
                }
            });
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldNotAcceptMethodsWithParameters() {
            build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                {
                    result(validationTarget().doSomething("", Integer.SIZE)).must().beEmptyOrNull();
                }
            });
        }

        @Test(expected = UnfinishedExpectationChainException.class)
        public void shouldNotValidateUsingUnfinishedMethodExpectations() {
            build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                {
                    result(validationTarget().getStringProperty1());
                }
            }).validate(null, HttpRequestType.POST);
        }

        @Test(expected = UnfinishedExpectationChainException.class)
        public void shouldNotValidateUsingUnfinishedRootExpectations() {
            build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                {
                    must();
                }
            }).validate(null, HttpRequestType.POST);
        }
    }

    public static class WhenValidating {

        private SimpleBean testObjectA;
        private SimpleBean testObjectB;

        @Before
        public void standUp() {
            testObjectA = new SimpleBean("1", "2", "3", 1, null, 3);
            testObjectB = new SimpleBean("1", "2", "3", null, 2, 3);
        }

        @Test
        public void shouldValidateAgainstRootTarget() {
            final Validator<SimpleBean> validator = build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                {
                    must().adhereTo(new Verifier<SimpleBean>() {

                        @Override
                        public VerifierResult verify(SimpleBean obj) {
                            return new VerifierResult(obj.getStringProperty1().equals(obj.getStringProperty1()));
                        }
                    });
                }
            });

            assertTrue(validator.validate(testObjectA, HttpRequestType.POST).passedValidation());
            assertTrue(validator.validate(testObjectA, HttpRequestType.POST).passedValidation());
        }

        @Test
        public void shouldValidateNotNull() {
            final Validator<SimpleBean> validator = build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                {
                    result(validationTarget().getIntProperty1()).must().exist().withMessage("Must not be null");
                }
            });

            assertTrue(validator.validate(testObjectA, HttpRequestType.POST).passedValidation());
            assertFalse(validator.validate(testObjectB, HttpRequestType.POST).passedValidation());
        }

        @Test
        public void shouldValidateNotNotNotNull() {
            final Validator<SimpleBean> validator = build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                {
                    result(validationTarget().getIntProperty1()).must().not().not().exist().withMessage("Must not be null");
                }
            });

            assertTrue(validator.validate(testObjectA, HttpRequestType.POST).passedValidation());
            assertFalse(validator.validate(testObjectB, HttpRequestType.POST).passedValidation());
        }

        @Test
        public void shouldValidateNotEmpty() {
            final Validator<SimpleBean> validator = build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                {
                    result(validationTarget().getIntProperty1()).must().exist().withMessage("Must not be null");
                    result(validationTarget().getStringProperty1()).must().not().beEmptyOrNull().withMessage("Must not be null");
                }
            });

            assertTrue(validator.validate(testObjectA, HttpRequestType.POST).passedValidation());
            assertFalse(validator.validate(testObjectB, HttpRequestType.POST).passedValidation());
        }

        @Test
        public void shouldValidateNull() {
            final Validator<SimpleBean> validator = build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                {
                    result(validationTarget().getIntProperty2()).must().not().exist().withMessage("Must not be null");
                }
            });

            assertTrue(validator.validate(testObjectA, HttpRequestType.POST).passedValidation());
            assertFalse(validator.validate(testObjectB, HttpRequestType.POST).passedValidation());
        }

        @Test
        public void shouldReturnedClonedExpectationResultLists() {
            final Validator<SimpleBean> validator = build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                {
                    result(validationTarget().getIntProperty2()).must().not().exist().withMessage("Must not be null");
                    result(validationTarget().getIntProperty1()).must().not().exist().withMessage("Must not be null");
                }
            });

            final ValidatorResult result = validator.validate(testObjectB, HttpRequestType.POST);

            assertTrue(result.getValidationResults().size() == 1);
            result.getValidationResults().clear();
            assertTrue(result.getValidationResults().size() == 1);
        }
    }

    public static class WhenHandlingExceptionsDuringValidation {

        @Test(expected = ValidationChainExecutionException.class)
        public void shouldNotValidateRaisedExpcetions() throws Exception {
            build(
                    new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                        {
                            result(validationTarget().getIntProperty3()).must().exist();
                        }
                    }).validate(new SimpleBean(), HttpRequestType.POST);
        }
    }
}
