package org.openstack.atlas.api.validation;

import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ExpectationResult;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.List;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

@RunWith(Enclosed.class)
public class MultipleMessagesReturnTest {
    public static class WhenValidatingComplexBeanWithOneExpectation {
        private ComplexBean complexBean;
        private Validator<ComplexBean> validator;
        final String beanExistMessage = "I need simple beans, Black or Refried or even Borraccho will work!";
        final String grainExistMessage = "Must have 1 or more grains in the bean cup!";
        final String complexStringExistMessage = "I need a complex bean  message.";
        final String simpleStringExistMessage = "I need a simple bean message";

        @Before
        public void Setup() {
            complexBean = new ComplexBean();

            complexBean.addSimpleBean(new SimpleBean());
        }

        @Test
        public void should_be_able_to_retrieve_validation_messages_without_validator_heirarchy_when_validation_fails() {

            validator = build(new ValidatorBuilder<ComplexBean>(ComplexBean.class) {
                {
                    result(validationTarget().getComplexString()).must().exist().withMessage(complexStringExistMessage);

                    result(validationTarget().getMySimpleBeans()).must().adhereTo(new Verifier<List<SimpleBean>>() {

                        @Override
                        public VerifierResult verify(List<SimpleBean> simpleBeans) {
                            for (SimpleBean simpleBean : simpleBeans) {
                                if (simpleBean.getStringProperty1() == null) {
                                    return new VerifierResult(false);
                                }
                            }
                            return new VerifierResult(true);
                        }
                    }).withMessage(simpleStringExistMessage);
                }
            });

            List<ExpectationResult> resultList = validator.validate(complexBean, HttpRequestType.POST).getValidationResults();
            Assert.assertTrue(!resultList.isEmpty());
            Assert.assertEquals(complexStringExistMessage, resultList.get(0).getMessage());
            Assert.assertEquals(simpleStringExistMessage, resultList.get(1).getMessage());

        }
    }
}
