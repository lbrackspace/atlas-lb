package org.openstack.atlas.adapter.vtm;

import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.helpers.VTMNameBuilder;
import org.openstack.atlas.service.domain.entities.CertificateMapping;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VTMNameBuilderTest {
    private LoadBalancer loadBalancer;
    private VirtualIp virtualIp;
    private CertificateMapping certMapping;

    @Before
    public void setUpIds() {
        loadBalancer = new LoadBalancer();
        virtualIp = new VirtualIp();
        certMapping = new CertificateMapping();

        loadBalancer.setId(1234);
        loadBalancer.setAccountId(777);
        virtualIp.setId(1);
        certMapping.setId(100);
    }

    @Test
    public void generateNameWithAccountIdAndLoadBalancerIdShouldCreateExpectedName() throws InsufficientRequestException {
        String expectedName = loadBalancer.getAccountId() + "_" + loadBalancer.getId();
        String generatedName = VTMNameBuilder.genVSName(loadBalancer);
        Assert.assertEquals(expectedName, generatedName);
    }

    @Test
    public void generateSecureServerNameWithAccountIdAndLoadBalancerIdShouldCreateExpectedName() throws InsufficientRequestException {
        String expectedName = loadBalancer.getAccountId() + "_" + loadBalancer.getId() + "_S";
        String generatedName = VTMNameBuilder.genSslVSName(loadBalancer.getId(), loadBalancer.getAccountId());
        Assert.assertEquals(expectedName, generatedName);
    }

    @Test(expected = InsufficientRequestException.class)
    public void generateSecureServerNameWithAccountIdAndLoadBalancerIdShouldThrowExceptionWhenMissingId() throws InsufficientRequestException {
        loadBalancer.setId(null);
        VTMNameBuilder.genSslVSName(null, loadBalancer.getAccountId());
    }

    @Test(expected = InsufficientRequestException.class)
    public void generateSecureSeverNameWithAccountIdAndLoadBalancerIdShouldThrowExceptionWhenMissingAccountId() throws InsufficientRequestException {
        loadBalancer.setId(null);
        VTMNameBuilder.genSslVSName(loadBalancer.getId(), null);
    }

    @Test(expected = InsufficientRequestException.class)
    public void generateNameWithAccountIdAndLoadBalancerIdShouldThrowExceptionWhenMissingAccountId() throws InsufficientRequestException {
        loadBalancer.setAccountId(null);
        VTMNameBuilder.genVSName(loadBalancer);
    }

    @Test
    public void generateTrafficIpGroupNameShouldCreateExpectedName() throws InsufficientRequestException {
        String expectedName = loadBalancer.getAccountId() + "_" + virtualIp.getId();
        String generatedName = VTMNameBuilder.generateTrafficIpGroupName(loadBalancer, virtualIp);
        Assert.assertEquals(expectedName, generatedName);
    }

    @Test(expected = InsufficientRequestException.class)
    public void generateTrafficIpGroupNameShouldThrowExceptionWhenMissingVipId() throws InsufficientRequestException {
        virtualIp.setId(null);
        VTMNameBuilder.generateTrafficIpGroupName(loadBalancer, virtualIp);
    }

    @Test(expected = InsufficientRequestException.class)
    public void generateNameWithAccountIdAndLoadBalancerIdShouldThrowExceptionWhenMissingLbId() throws InsufficientRequestException {
        VTMNameBuilder.genVSName(null, 1);
    }

    @Test(expected = InsufficientRequestException.class)
    public void generateNameWithAccountIdAndLoadBalancerIdShouldThrowExceptionWhenMissingActId() throws InsufficientRequestException {
        VTMNameBuilder.genVSName(1, null);
    }

    @Test
    public void generateCertificateNameWithAllParameters() throws InsufficientRequestException {
        String expectedName = loadBalancer.getAccountId() + "_" + loadBalancer.getId() + "_" + certMapping.getId();
        String generatedName = VTMNameBuilder.generateCertificateName(loadBalancer.getId(), loadBalancer.getAccountId(), certMapping.getId());
        Assert.assertEquals(expectedName, generatedName);
    }

    @Test(expected = InsufficientRequestException.class)
    public void generateCertificateNameWithSomeParameters() throws InsufficientRequestException {
        VTMNameBuilder.generateCertificateName(loadBalancer.getId(), loadBalancer.getAccountId(), null);
    }
}
