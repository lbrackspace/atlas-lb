package org.openstack.atlas.adapter.zxtm;

import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ZxtmNameBuilderTest {
    private LoadBalancer loadBalancer;
    private VirtualIp virtualIp;

    @Before
    public void setUpIds() {
        loadBalancer = new LoadBalancer();
        virtualIp = new VirtualIp();

        loadBalancer.setId(1234);
        loadBalancer.setAccountId(777);
        virtualIp.setId(1);
    }

    @Test
    public void generateNameWithAccountIdAndLoadBalancerIdShouldCreateExpectedName() throws InsufficientRequestException {
        String expectedName = loadBalancer.getAccountId() + "_" + loadBalancer.getId();
        String generatedName = ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(loadBalancer);
        Assert.assertEquals(expectedName, generatedName);
    }

    @Test(expected = InsufficientRequestException.class)
    public void generateNameWithAccountIdAndLoadBalancerIdShouldThrowExceptionWhenMissingId() throws InsufficientRequestException {
        loadBalancer.setId(null);
        ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(loadBalancer);
    }

    @Test(expected = InsufficientRequestException.class)
    public void generateNameWithAccountIdAndLoadBalancerIdShouldThrowExceptionWhenMissingAccountId() throws InsufficientRequestException {
        loadBalancer.setAccountId(null);
        ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(loadBalancer);
    }

    @Test
    public void generateTrafficIpGroupNameShouldCreateExpectedName() throws InsufficientRequestException {
        String expectedName = loadBalancer.getAccountId() + "_" + virtualIp.getId();
        String generatedName = ZxtmNameBuilder.generateTrafficIpGroupName(loadBalancer, virtualIp);
        Assert.assertEquals(expectedName, generatedName);
    }

    @Test(expected = InsufficientRequestException.class)
    public void generateTrafficIpGroupNameShouldThrowExceptionWhenMissingVipId() throws InsufficientRequestException {
        virtualIp.setId(null);
        ZxtmNameBuilder.generateTrafficIpGroupName(loadBalancer, virtualIp);
    }

    @Test(expected = InsufficientRequestException.class)
    public void generateNameWithAccountIdAndLoadBalancerIdShouldThrowExceptionWhenMissingLbId() throws InsufficientRequestException {
        ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(null, 1);
    }

    @Test(expected = InsufficientRequestException.class)
    public void generateNameWithAccountIdAndLoadBalancerIdShouldThrowExceptionWhenMissingActId() throws InsufficientRequestException {
        ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(1, null);
    }
}
