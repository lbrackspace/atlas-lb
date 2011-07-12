package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.ComplexBean;
import org.openstack.atlas.api.validation.*;
import org.openstack.atlas.api.validation.SimpleBean;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class MustDelegateToTest {

    public static class WhenVerifyingMustDelegateTo {

        private ComplexBean complexBean;
        private SimpleBean simpleBean1;
        private SimpleBean simpleBean2;
        Validator<SimpleBean> simpleBeanValidator;
        Validator<ComplexBean> complexBeanValidator;

        @Before
        public void standUp() {
            simpleBean1 = new SimpleBean("1", "2", "3", 1, 2, 3);
            simpleBean2 = new SimpleBean("1", "2", "3", 1, 2, 3);
            complexBean = new ComplexBean();
            complexBean.addSimpleBean(simpleBean1);
            complexBean.addSimpleBean(simpleBean2);

            simpleBeanValidator = build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                {
                    result(validationTarget().getIntProperty1()).must().exist().withMessage("Must not be null");
                }
            });

            complexBeanValidator = build(new ValidatorBuilder<ComplexBean>(ComplexBean.class) {

                {
                    result(validationTarget().getMySimpleBeans()).if_().exist().then().must().delegateTo(simpleBeanValidator, Context.C1);
                }
            });

        }

        @Test
        public void shouldNotRetainStateWhenUsingDelegation() {
            complexBean.addSimpleBean(new SimpleBean());
            assertFalse(complexBeanValidator.validate(complexBean, Context.C1).passedValidation());

            complexBean.setMySimpleBeans(null);
            complexBean.addSimpleBean(simpleBean1);
            assertTrue(complexBeanValidator.validate(complexBean, Context.C1).passedValidation());
        }

        @Test
        public void shouldValidateWhenSimpleBeansExist() {
            assertTrue(complexBeanValidator.validate(complexBean, Context.C1).passedValidation());
        }

        @Test
        public void shouldValidateWhenSimpleBeansDoNotExist() {
            complexBean.setMySimpleBeans(null);
            assertTrue(complexBeanValidator.validate(complexBean, Context.C1).passedValidation());
        }

        @Test
        public void shouldFailWhenSimpleBeansAreInvalid() {
            SimpleBean simpleBean = new SimpleBean(null, null, null, null, null, null);
            ComplexBean bean = new ComplexBean();
            bean.addSimpleBean(simpleBean);
            ValidatorResult results = complexBeanValidator.validate(bean, Context.C1);
            assertFalse(results.passedValidation());
            assertEquals(2, results.getValidationResults().size());
        }
    }

    private enum Context {
        C1
    }
}