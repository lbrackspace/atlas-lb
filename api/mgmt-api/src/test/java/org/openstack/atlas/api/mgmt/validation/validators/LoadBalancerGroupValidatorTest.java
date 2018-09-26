package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerLimitGroup;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerLimitGroups;
import org.openstack.atlas.api.mgmt.validation.validators.LoadBalancerLimitGroupValidator;
import org.openstack.atlas.api.mgmt.validation.validators.LoadBalancerLimitGroupsValidator;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.openstack.atlas.api.helpers.ResultMessage.resultMessage;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class LoadBalancerGroupValidatorTest {
    public static class WhenValidatingPost {

        private LoadBalancerLimitGroupsValidator lbLimitGroupsValidator;
        private LoadBalancerLimitGroupValidator lbLimitGroupValidator;
        private LoadBalancerLimitGroups lbLimitGroups;
        private LoadBalancerLimitGroup lbLimitGroup;

        @Before
        public void setUpValidVipsObject() {
            lbLimitGroupsValidator = new LoadBalancerLimitGroupsValidator();
            lbLimitGroupValidator = new LoadBalancerLimitGroupValidator();
            lbLimitGroups = new LoadBalancerLimitGroups();
            lbLimitGroup = new LoadBalancerLimitGroup();

            lbLimitGroup.setLimit(100);
            lbLimitGroup.setIsDefault(true);
            lbLimitGroup.setName("aName");
            lbLimitGroups.getLoadBalancerLimitGroups().add(lbLimitGroup);
        }

        @Test
        public void shouldAcceptValidLbLimitObject() {
            ValidatorResult result = lbLimitGroupsValidator.validate(lbLimitGroups, HttpRequestType.POST);
            assertTrue(resultMessage(result, HttpRequestType.POST), result.passedValidation());
        }

        @Test
        public void shouldRejectNullLbLimitObject() {
            ValidatorResult result = lbLimitGroupsValidator.validate(null, HttpRequestType.POST);
            assertFalse(resultMessage(result, HttpRequestType.POST), result.passedValidation());
        }

        @Test
        public void shouldRejectNullLbLimitName() {
            lbLimitGroup.setName(null);
            ValidatorResult result = lbLimitGroupValidator.validate(lbLimitGroup, HttpRequestType.POST);
            assertFalse(resultMessage(result, HttpRequestType.POST), result.passedValidation());
        }

        @Test
        public void shouldRejectNullValidLbLimitDefault() {
            lbLimitGroup.setIsDefault(null);
            ValidatorResult result = lbLimitGroupValidator.validate(lbLimitGroup, HttpRequestType.POST);
            assertFalse(resultMessage(result, HttpRequestType.POST), result.passedValidation());
        }

        @Test
        public void shouldRejectNullValidLbLimitlimit() {
            lbLimitGroup.setLimit(null);
            ValidatorResult result = lbLimitGroupValidator.validate(lbLimitGroup, HttpRequestType.POST);
            assertFalse(resultMessage(result, HttpRequestType.POST), result.passedValidation());
        }

        @Test
        public void shouldRejectLbLimitlimitID() {
            lbLimitGroup.setId(1);
            ValidatorResult result = lbLimitGroupValidator.validate(lbLimitGroup, HttpRequestType.PUT);
            assertFalse(resultMessage(result, HttpRequestType.PUT), result.passedValidation());
        }

        public static class WhenValidatingPut {

            private LoadBalancerLimitGroupsValidator lbLimitGroupsValidator;
            private LoadBalancerLimitGroupValidator lbLimitGroupValidator;
            private LoadBalancerLimitGroups lbLimitGroups;
            private LoadBalancerLimitGroup lbLimitGroup;

            @Before
            public void setUpValidVipsObject() {
                lbLimitGroupsValidator = new LoadBalancerLimitGroupsValidator();
                lbLimitGroupValidator = new LoadBalancerLimitGroupValidator();
                lbLimitGroups = new LoadBalancerLimitGroups();
                lbLimitGroup = new LoadBalancerLimitGroup();

                lbLimitGroup.setLimit(100);
                lbLimitGroup.setIsDefault(true);
                lbLimitGroup.setName("aName");
                lbLimitGroups.getLoadBalancerLimitGroups().add(lbLimitGroup);
            }

            @Test
            public void shouldAcceptValidLbLimitObjectWhenFieldsAreMissing() {
                LoadBalancerLimitGroup lblg = new LoadBalancerLimitGroup();
                lblg.setName("aName");
                ValidatorResult result = lbLimitGroupsValidator.validate(lbLimitGroups, HttpRequestType.PUT);
                assertTrue(resultMessage(result, HttpRequestType.PUT), result.passedValidation());
            }

            @Test
            public void shouldRejectNullLbLimitName() {
                lbLimitGroup.setName(null);
                ValidatorResult result = lbLimitGroupValidator.validate(lbLimitGroup, HttpRequestType.PUT);
                assertFalse(resultMessage(result, HttpRequestType.PUT), result.passedValidation());
            }

            @Test
            public void shouldRejectNullValidLbLimitDefault() {
                lbLimitGroup.setIsDefault(null);
                ValidatorResult result = lbLimitGroupValidator.validate(lbLimitGroup, HttpRequestType.PUT);
                assertFalse(resultMessage(result, HttpRequestType.PUT), result.passedValidation());
            }

            @Test
            public void shouldRejectLbLimitlimitID() {
                lbLimitGroup.setId(1);
                ValidatorResult result = lbLimitGroupValidator.validate(lbLimitGroup, HttpRequestType.PUT);
                assertFalse(resultMessage(result, HttpRequestType.PUT), result.passedValidation());
            }


        }
    }
}