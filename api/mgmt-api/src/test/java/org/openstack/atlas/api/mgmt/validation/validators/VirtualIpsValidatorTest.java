package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIps;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.api.mgmt.validation.validators.VirtualIpsValidator;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.openstack.atlas.api.helpers.ResultMessage.resultMessage;
import static org.openstack.atlas.api.mgmt.validation.contexts.VirtualIpContext.POST;
import static org.openstack.atlas.api.mgmt.validation.contexts.VirtualIpContext.VIPS_POST;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class VirtualIpsValidatorTest {

    public static class WhenValidatingPost {

        private VirtualIpsValidator vipsValidator;
        private VirtualIps vips;
        private VirtualIp vip2;

        @Before
        public void setUpValidVipsObject() {
            vipsValidator = new VirtualIpsValidator();

            vips = new VirtualIps();

            vip2 = new VirtualIp();
            vip2.setType(VipType.PUBLIC);

            vips.getVirtualIps().add(vip2);
        }

        @Test
        public void shouldAcceptValidVipsObject() {
            vip2.setAddress("0.0.0.0");
            ValidatorResult result = vipsValidator.validate(vips, POST);
            assertTrue(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectNullVipsObject() {
            ValidatorResult result= vipsValidator.validate(null, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectVipsObjectWithNoVips() {
            ValidatorResult result = vipsValidator.validate(new VirtualIps(), POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shoulRejectLoadBalancerId() {
            vip2.setLoadBalancerId(80);
            ValidatorResult result = vipsValidator.validate(vips, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectClusterId() {
            vip2.setClusterId(80);
            ValidatorResult result = vipsValidator.validate(vips, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectId() {
            vip2.setId(12);
            ValidatorResult result = vipsValidator.validate(vips, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }
    }

    public static class WhenValidatingVipsPost {

        private VirtualIpsValidator vipsValidator;
        private VirtualIps vips;
        private VirtualIp vip2;

        @Before
        public void setUpValidVipsObject() {
            vipsValidator = new VirtualIpsValidator();

            vips = new VirtualIps();

            vip2 = new VirtualIp();
            vip2.setType(VipType.PUBLIC);
            Ticket ticket = new Ticket();
            ticket.setTicketId("1234");
            ticket.setComment("My first comment!");
            vip2.setTicket(ticket);

            vips.getVirtualIps().add(vip2);
        }

        @Test
        public void shouldAcceptValidVipsPOSTObject() {
            ValidatorResult result = vipsValidator.validate(vips, VIPS_POST);
            assertTrue(resultMessage(result, VIPS_POST), result.passedValidation());
        }

        @Test
        public void shouldRejectNullVipsObject() {
            ValidatorResult result = vipsValidator.validate(null, VIPS_POST);
            assertFalse(resultMessage(result, VIPS_POST), result.passedValidation());
        }

        @Test
        public void shouldRejectVipsObjectWithNoVips() {
            ValidatorResult result = vipsValidator.validate(new VirtualIps(), VIPS_POST);
            assertFalse(resultMessage(result, VIPS_POST), result.passedValidation());
        }
        @Ignore
        @Test
        public void shoulRejectLoadBalancerId() {
            vip2.setLoadBalancerId(80);
            ValidatorResult result = vipsValidator.validate(vips, VIPS_POST);
            assertFalse(resultMessage(result, VIPS_POST), result.passedValidation());
        }
        @Ignore
        @Test
        public void shouldRejectClusterId() {
            vip2.setClusterId(80);
            ValidatorResult result = vipsValidator.validate(vips, VIPS_POST);
            assertFalse(resultMessage(result, VIPS_POST), result.passedValidation());
        }

        @Test
        public void shouldRejectIdIfTypeIsSet() {
            vip2.setId(12);
            ValidatorResult result = vipsValidator.validate(vips, VIPS_POST);
            assertFalse(resultMessage(result, VIPS_POST), result.passedValidation());
        }
        @Ignore
        @Test
        public void shouldRejectVipsAddress() {
            ValidatorResult result = vipsValidator.validate(vips, VIPS_POST);
            assertFalse(resultMessage(result, VIPS_POST), result.passedValidation());
        }
    }
}
