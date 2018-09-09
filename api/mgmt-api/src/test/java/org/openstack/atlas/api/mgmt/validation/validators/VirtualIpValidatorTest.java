package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.api.mgmt.validation.validators.VirtualIpValidator;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.openstack.atlas.api.helpers.ResultMessage.resultMessage;
import static org.openstack.atlas.api.mgmt.validation.contexts.VirtualIpContext.POST;
import static org.openstack.atlas.api.mgmt.validation.contexts.VirtualIpContext.VIPS_POST;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class VirtualIpValidatorTest {

    public static class WhenValidatingPost {

        private VirtualIpValidator cTest;
        private VirtualIp vip;

        @Before
        public void standUp() {
            cTest = new VirtualIpValidator();

            vip = new VirtualIp();
            vip.setAddress("127.0.0.1");
            vip.setType(VipType.SERVICENET);

        }

        @Test
        public void shouldAcceptValidClusterVip() {
            ValidatorResult result = cTest.validate(vip, POST);
            assertTrue(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectClusterId() {
            vip.setClusterId(12);
            ValidatorResult result = cTest.validate(vip, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectNullType() {
            vip.setType(null);
            ValidatorResult result = cTest.validate(vip, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectNullAddress() {
            vip.setAddress(null);
            ValidatorResult result = cTest.validate(vip, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectEmptyAddress() {
            vip.setAddress("");
            ValidatorResult result = cTest.validate(vip, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectLbId() {
            vip.setLoadBalancerId(2);
            ValidatorResult result = cTest.validate(vip, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectVipId() {
            vip.setId(2);
            ValidatorResult result = cTest.validate(vip, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }
    }

    public static class WhenValidatingLoadBalancerVIPPOST {

        private VirtualIpValidator lbvipvalidator;
        private VirtualIp vip;
        private Ticket ticket;

        @Before
        public void standUp() {
            lbvipvalidator = new VirtualIpValidator();

            vip = new VirtualIp();
            vip.setType(VipType.SERVICENET);

            ticket = new Ticket();
            ticket.setTicketId("1234");
            ticket.setComment("My first comment!");
            vip.setTicket(ticket);
        }

        @Test
        public void shouldAcceptValidVip() {
            ValidatorResult result = lbvipvalidator.validate(vip, VIPS_POST);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenIdAndTypeAreBothSet() {
            vip.setId(1234);

            ValidatorResult result = lbvipvalidator.validate(vip, VIPS_POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectIdOnly() {
            VirtualIp vip = new VirtualIp();
            vip.setId(1234);

            ValidatorResult result = lbvipvalidator.validate(vip, VIPS_POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenTicketIsMissing() {
            vip.setTicket(null);

            ValidatorResult result = lbvipvalidator.validate(vip, VIPS_POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectAddress() {
            vip.setAddress("127.0.0.1");
            ValidatorResult result = lbvipvalidator.validate(vip, VIPS_POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenTicketIsInvalid() {
            vip.getTicket().setComment(null);

            ValidatorResult result = lbvipvalidator.validate(vip, VIPS_POST);
            assertFalse(result.passedValidation());
        }
    }
}
