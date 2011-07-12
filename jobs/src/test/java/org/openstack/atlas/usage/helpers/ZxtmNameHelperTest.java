package org.openstack.atlas.usage.helpers;

import org.junit.Assert;
import org.junit.Test;

public class ZxtmNameHelperTest {

    @Test
    public void shouldReturnAccountIdFromZxtmName() {
        String zxtmName = "1234_56";
        Integer accountId = ZxtmNameHelper.stripAccountIdFromZxtmName(zxtmName);

        Assert.assertEquals(new Integer(1234), accountId);
    }

    @Test
    public void shouldReturnLoadBalancerIdFromZxtmName() {
        String zxtmName = "1234_56";
        Integer lbId = ZxtmNameHelper.stripLbIdFromZxtmName(zxtmName);

        Assert.assertEquals(new Integer(56), lbId);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldThrowNumberFormatExceptionWhenStrippingAccountIdFromBadName() {
        String badZxtmName = "Evil_load_balancer_name";
        ZxtmNameHelper.stripAccountIdFromZxtmName(badZxtmName);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldThrowNumberFormatExceptionWhenStrippingLbIdFromBadName() {
        String badZxtmName = "Evil_load_balancer_name";
        ZxtmNameHelper.stripLbIdFromZxtmName(badZxtmName);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void shouldThrowArrayIndexOutOfBoundsExceptionWhenStrippingLbIdFromBadName() {
        String badZxtmName = "Evil load balancer name";
        ZxtmNameHelper.stripLbIdFromZxtmName(badZxtmName);
    }
}
