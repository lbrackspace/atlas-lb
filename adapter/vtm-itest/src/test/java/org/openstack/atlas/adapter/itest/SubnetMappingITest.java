package org.openstack.atlas.adapter.itest;


import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.VTMRollBackException;
import org.openstack.atlas.service.domain.pojos.*;
import org.rackspace.vtm.client.VTMRestClient;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class SubnetMappingITest extends VTMTestBase {

    private String testNetInterface;

    @BeforeClass
    public static void clientInit() {
        vtmClient = new VTMRestClient();
    }

    @Before
    public void standUp() throws InterruptedException, InsufficientRequestException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        testNetInterface = "eth1";
    }

    @Test
    public void retrieveHostSubnetMappings() throws VTMRollBackException {
        // Use defaults...
        Hostsubnet testSubnetMapping = getTestHostSubnet(null, null);
        if (testSubnetMapping == null) {
            testSubnetMapping = createTestSubnet(null, null);
        } else {
            validateTestSubnetMapping(testSubnetMapping, null, null);
        }

        // Clean up
        List<Hostsubnet> lhsm = new ArrayList<>();
        Hostssubnet hostssubnet = new Hostssubnet();
        lhsm.add(testSubnetMapping);
        hostssubnet.setHostsubnets(lhsm);
        deleteSubnetMapping(hostssubnet, null, null);

    }

    @Test
    public void addAndRemoveHostSubnetMapping() throws VTMRollBackException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        String newCidr = "10.2.2.2/14";
        String newInterface = "test13";

        // Create and validate new subnet mapping
        createTestSubnet(newInterface, newCidr);

        // Prep delete new subnet mapping request
        Hostsubnet hostsubnet = new Hostsubnet();
        hostsubnet.setName(config.getTrafficManagerName());
        List<Hostsubnet> lhsm = new ArrayList<>();
        List<NetInterface> netInterfaces = new ArrayList<>();
        NetInterface netInterface = new NetInterface();
        List<Cidr> cidrs = new ArrayList<>();
        Cidr cidr = new Cidr();
        cidr.setBlock(newCidr);
        cidrs.add(cidr);
        netInterface.setName(newInterface);
        netInterface.setCidrs(cidrs);
        netInterfaces.add(netInterface);
        Hostssubnet hostssubnet = new Hostssubnet();
        hostsubnet.setNetInterfaces(netInterfaces);
        lhsm.add(hostsubnet);
        hostssubnet.setHostsubnets(lhsm);

        // Delete the subnet mapping
        deleteSubnetMapping(hostssubnet, newInterface, newCidr);
    }

    @Test
    public void addAndRemoveHostSubnetMappingWithExistingSubnetMappings() throws VTMRollBackException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        String newCidr = "10.2.2.2/14";
        String newInterface = "test13";

        // Create and validate default subnet mapping
        createTestSubnet(null, null);

        // Create and validate new subnet mapping
        createTestSubnet(newInterface, newCidr);

        // Prep delete new subnet mapping request
        Hostsubnet hostsubnet = new Hostsubnet();
        hostsubnet.setName(config.getTrafficManagerName());
        List<Hostsubnet> lhsm = new ArrayList<>();
        List<NetInterface> netInterfaces = new ArrayList<>();
        NetInterface netInterface = new NetInterface();
        List<Cidr> cidrs = new ArrayList<>();
        Cidr cidr = new Cidr();
        cidr.setBlock(newCidr);
        cidrs.add(cidr);
        netInterface.setName(newInterface);
        netInterface.setCidrs(cidrs);
        netInterfaces.add(netInterface);
        Hostssubnet hostssubnet = new Hostssubnet();
        hostsubnet.setNetInterfaces(netInterfaces);
        lhsm.add(hostsubnet);
        hostssubnet.setHostsubnets(lhsm);

        // Delete the new subnet mapping
        deleteSubnetMapping(hostssubnet, newInterface, newCidr);

        // Verify default mapping still exists
        validateTestSubnetMapping(getTestHostSubnet(null, null), null, null);

        // Now remove default
        hostssubnet.getHostsubnets().get(0).getNetInterfaces().get(0).setName(testNetInterface);
        hostssubnet.getHostsubnets().get(0).getNetInterfaces().get(0).getCidrs().get(0).setBlock("10.1.1.1/24");
        deleteSubnetMapping(hostssubnet, null, null);

    }

    private Hostsubnet createTestSubnet(String name, String block) throws VTMRollBackException {
        String cidrBlock = "10.1.1.1/24";
        if (block != null) cidrBlock = block;
        if (name == null) name = testNetInterface;
        List<Hostsubnet> lhsm = new ArrayList<>();
        Hostssubnet hostssubnet = new Hostssubnet();
        Hostsubnet hs = new Hostsubnet();
        List<NetInterface> netInterfaces = new ArrayList<>();
        NetInterface netInterface = new NetInterface();
        List<Cidr> cidrs = new ArrayList<>();
        Cidr cidr = new Cidr();
        cidr.setBlock(cidrBlock);
        cidrs.add(cidr);
        netInterface.setName(name);
        netInterface.setCidrs(cidrs);
        netInterfaces.add(netInterface);
        hs.setName(config.getTrafficManagerName());
        hs.setNetInterfaces(netInterfaces);
        lhsm.add(hs);
        hostssubnet.setHostsubnets(lhsm);
        try {
            vtmAdapter.setSubnetMappings(config, hostssubnet);
        } catch (VTMRollBackException e) {
            Assert.fail("Failed to create test subnet mappings " + e.getCause().toString());
        }

        Hostsubnet testSubnetMapping = getTestHostSubnet(name, cidrBlock);
        validateTestSubnetMapping(testSubnetMapping, name, cidrBlock);
        return testSubnetMapping;
    }

    private Hostsubnet getTestHostSubnet(String name, String block) throws VTMRollBackException {
        String cidrBlock = "10.1.1.1/24";
        if (name == null) name = testNetInterface;
        if (block != null) cidrBlock = block;
        Hostssubnet hsm = vtmAdapter.getSubnetMappings(config, config.getTrafficManagerName());
        Assert.assertNotNull(hsm);
        Assert.assertFalse(hsm.getHostsubnets().isEmpty());
        Hostsubnet testSubnetMapping = null;
        for (Hostsubnet hostsubnet : hsm.getHostsubnets()) {
            for (NetInterface ni : hostsubnet.getNetInterfaces()) {
                if (ni.getName().equals(name)) {
                    for (Cidr cidr : ni.getCidrs()) {
                        if (cidr.getBlock().equals(cidrBlock)) {
                            // Specific mapping found, return this hostsubnet
                            testSubnetMapping = hostsubnet;
                        }
                    }
                }
            }
        }
        return testSubnetMapping;
    }

    private void validateTestSubnetMapping(Hostsubnet hostsubnet, String name, String block) {
        String cidrBlock = "10.1.1.1/24";
        if (name == null) name = testNetInterface;
        if (block != null) cidrBlock = block;
        if (hostsubnet == null) Assert.fail("Test subnet mapping not found/created");
        Assert.assertEquals(config.getTrafficManagerName(), hostsubnet.getName());
        NetInterface netInterface = null;
        for (NetInterface ni : hostsubnet.getNetInterfaces()) {
            if (ni.getName().equals(name)) {
                netInterface = ni;
            }
        }
        Assert.assertNotNull(netInterface);
        Assert.assertEquals(name, netInterface.getName());

        List<String> blocks = new ArrayList<>();
        for (Cidr cidr : netInterface.getCidrs()) {
            blocks.add(cidr.getBlock());
        }
        Assert.assertTrue(blocks.contains(cidrBlock));
    }

    private void deleteSubnetMapping(Hostssubnet hostssubnet, String name, String block) throws VTMRollBackException {
        String cidrBlock = "10.1.1.1/24";
        if (name == null) name = testNetInterface;
        if (block != null) cidrBlock = block;
        vtmAdapter.deleteSubnetMappings(config, hostssubnet);
        Assert.assertNull(getTestHostSubnet(name, cidrBlock));
    }
}
