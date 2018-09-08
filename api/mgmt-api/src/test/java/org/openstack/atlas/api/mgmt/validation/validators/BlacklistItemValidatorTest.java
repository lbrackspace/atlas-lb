package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistItem;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.IpVersion;
import static org.openstack.atlas.api.helpers.ResultMessage.resultMessage;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

import org.openstack.atlas.api.mgmt.validation.validators.BlacklistItemValidator;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class BlacklistItemValidatorTest {

    private BlacklistItemValidator bv;
    private BlacklistItem bli;

    public BlacklistItemValidatorTest() {
    }

    @Before
    public void setUp() {
        bv = new BlacklistItemValidator();
        bli = new BlacklistItem();

    }

    @Test
    public void shouldRejectVersionMisMatchIPv4() {
        bli.setIpVersion(IpVersion.IPV6);
        bli.setCidrBlock("192.168.3.51/24");
        ValidatorResult result = bv.validate(bli, POST);
        assertFalse(resultMessage(result, POST), result.passedValidation());
    }

    @Test
    public void shouldRejectVersionMisMatchIPv6() {
        bli.setIpVersion(IpVersion.IPV6);
        bli.setCidrBlock("192.168.3.51/24");
        ValidatorResult result = bv.validate(bli, POST);
        assertFalse(resultMessage(result, POST), result.passedValidation());
    }

    @Test
    public void shouldRejectIPv6String() {
        bli.setIpVersion(IpVersion.IPV6);
        bli.setCidrBlock("::");
        ValidatorResult result = bv.validate(bli, POST);
        assertFalse(resultMessage(result, POST), result.passedValidation());
    }

    @Test
    public void shouldRejectIPv4String() {
        bli.setIpVersion(IpVersion.IPV6);
        bli.setCidrBlock("192.168.3.54");
        ValidatorResult result = bv.validate(bli, POST);
        assertFalse(resultMessage(result, POST), result.passedValidation());
    }

    @Test
    public void shouldaCeptIPv4Cidr() {
        bli.setIpVersion(IpVersion.IPV4);
        bli.setCidrBlock("192.168.3.54/24");
        ValidatorResult result = bv.validate(bli, POST);
        assertTrue(resultMessage(result, POST), result.passedValidation());
    }

    @Test
    public void shouldaCeptIPv6Cidr() {
        bli.setIpVersion(IpVersion.IPV6);
        bli.setCidrBlock("::/64");
        ValidatorResult result = bv.validate(bli, POST);
        assertTrue(resultMessage(result, POST), result.passedValidation());
    }
}
