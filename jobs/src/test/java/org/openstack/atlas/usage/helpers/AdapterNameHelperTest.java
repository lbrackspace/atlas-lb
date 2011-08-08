package org.openstack.atlas.usage.helpers;

import org.junit.Assert;
import org.junit.Test;

public class AdapterNameHelperTest {

    @Test
    public void shouldReturnAccountIdFromAdapterName() {
        String name = "1234_56";
        Integer accountId = AdapterNameHelper.stripAccountIdFromName(name);

        Assert.assertEquals(new Integer(1234), accountId);
    }

    @Test
    public void shouldReturnLoadBalancerIdFromName() {
        String name = "1234_56";
        Integer lbId = AdapterNameHelper.stripLbIdFromName(name);

        Assert.assertEquals(new Integer(56), lbId);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldThrowNumberFormatExceptionWhenStrippingAccountIdFromBadName() {
        String badName = "Evil_load_balancer_name";
        AdapterNameHelper.stripAccountIdFromName(badName);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldThrowNumberFormatExceptionWhenStrippingLbIdFromBadName() {
        String badName = "Evil_load_balancer_name";
        AdapterNameHelper.stripLbIdFromName(badName);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void shouldThrowArrayIndexOutOfBoundsExceptionWhenStrippingLbIdFromBadName() {
        String badName = "Evil load balancer name";
        AdapterNameHelper.stripLbIdFromName(badName);
    }
}
