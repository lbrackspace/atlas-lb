package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.*;
import org.openstack.atlas.api.validation.context.HttpRequestType;

import java.util.ArrayList;
import java.util.List;
import org.openstack.atlas.api.validation.SimpleBean;
import org.openstack.atlas.api.validation.ComplexBean;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.openstack.atlas.api.validation.ValidatorBuilder.*;
import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class MustBeNullTest {

    public static class WhenValidatingComplexBean {

        private ComplexBean complexBean;
        private Validator<ComplexBean> validator;

        @Before
        public void standUpValidComplexBeanAndExpectation() {
            complexBean = new ComplexBean();
            complexBean.addSimpleBean(new SimpleBean());

            validator = build(new ValidatorBuilder<ComplexBean>(ComplexBean.class) {

                {
                    result(validationTarget().getMySimpleBeans()).must().exist().withMessage("");
                    result(validationTarget().getMySimpleBeans()).must().haveSizeOfAtLeast(1).withMessage("");
                }
            });
        }

        @Test
        public void shouldValidateValidComplexBean() {
            assertTrue(validator.validate(complexBean, HttpRequestType.POST).passedValidation());
        }

        @Test
        public void shouldRejectComplexBeanWithNullSimpleBeans() {
            complexBean.setMySimpleBeans(null);
            assertFalse(validator.validate(complexBean, HttpRequestType.POST).passedValidation());
        }

        @Test
        public void shouldRejectNullComplexBean() {
            assertFalse(validator.validate(null, HttpRequestType.POST).passedValidation());
        }

        @Test
        public void shouldRejectEmptySimpleBeanList() {
            complexBean.getMySimpleBeans().clear();
            assertFalse(validator.validate(complexBean, HttpRequestType.POST).passedValidation());
        }

        public static class WhenValidatingSimpleBean {

            private SimpleBean testBean1;
            private SimpleBean testBean2;
            private List list;

            @Before
            public void standUps() {
                list = new ArrayList();
                list.add(3);
                testBean1 = new SimpleBean("", null, "3", null, 2, 3);
                testBean2 = new SimpleBean(null, "2", "3", 1, null, 3);
            }

            @Test
            public void shouldValidateSimpleBeanIsNull() {
                final Validator<SimpleBean> validator = build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {

                    {
                        result(validationTarget().getStringProperty2()).must().beEmptyOrNull().withMessage(" The ComplexBean must be null ");
                        result(validationTarget().getIntProperty1()).must().beEmptyOrNull().withMessage(" The ComplexBean must be null ");
                    }
                });

                assertTrue(validator.validate(testBean1, HttpRequestType.POST).passedValidation());
                assertFalse(validator.validate(testBean2, HttpRequestType.POST).passedValidation());
            }
        }
    }
}
