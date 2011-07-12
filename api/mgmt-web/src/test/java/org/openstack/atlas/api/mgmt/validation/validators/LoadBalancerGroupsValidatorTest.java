package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.*;
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
public class LoadBalancerGroupsValidatorTest {
    public static class WhenValidatingPost {

    private LoadBalancerLimitGroupsValidator lbLimitGroupsValidator;
    private LoadBalancerLimitGroupValidator lbLimitGroupValidator;
    private LoadBalancerLimitGroups lbLimitGroups;

        @Before
        public void setUpValidVipsObject() {
            lbLimitGroupsValidator= new LoadBalancerLimitGroupsValidator();
            lbLimitGroups = new LoadBalancerLimitGroups();

            LoadBalancerLimitGroup lbLimitGroup = new LoadBalancerLimitGroup();
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
        public void shouldRejectLbLimitObjectWithNoLbLimit() {
            ValidatorResult result = lbLimitGroupsValidator.validate(new LoadBalancerLimitGroups(), HttpRequestType.POST);
            assertFalse(resultMessage(result, HttpRequestType.POST), result.passedValidation());
        }
    }
}
