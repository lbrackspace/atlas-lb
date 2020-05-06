package org.openstack.atlas.adapter.itest;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.service.domain.pojos.Cidr;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Hostsubnet;
import org.openstack.atlas.service.domain.pojos.NetInterface;

import java.util.ArrayList;
import java.util.List;

public class SubnetMappingITest extends STMTestBase {

    private String testNetInterface;

    @Before
    public void standUp() throws InterruptedException, InsufficientRequestException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        testNetInterface = "eth1";
    }

    @Test
    public void retrieveHostSubnetMappings() throws StmRollBackException, InsufficientRequestException {
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
    public void addAndRemoveHostSubnetMapping() throws StmRollBackException, InsufficientRequestException {
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
    public void addAndRemoveHostSubnetMappingWithExistingSubnetMappings() throws StmRollBackException, InsufficientRequestException {
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

    private Hostsubnet createTestSubnet(String name, String block) throws StmRollBackException, InsufficientRequestException {
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
            stmAdapter.setSubnetMappings(config, hostssubnet);
        } catch (StmRollBackException | InsufficientRequestException e) {
            Assert.fail("Failed to create test subnet mappings " + e.getCause().toString());
        }

        Hostsubnet testSubnetMapping = getTestHostSubnet(name, cidrBlock);
        validateTestSubnetMapping(testSubnetMapping, name, cidrBlock);
        return testSubnetMapping;
    }

    private Hostsubnet getTestHostSubnet(String name, String block) throws StmRollBackException, InsufficientRequestException {
        String cidrBlock = "10.1.1.1/24";
        if (name == null) name = testNetInterface;
        if (block != null) cidrBlock = block;
        Hostssubnet hsm = stmAdapter.getSubnetMappings(config, config.getTrafficManagerName());
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

    private void deleteSubnetMapping(Hostssubnet hostssubnet, String name, String block) throws StmRollBackException, InsufficientRequestException {
        String cidrBlock = "10.1.1.1/24";
        if (name == null) name = testNetInterface;
        if (block != null) cidrBlock = block;
        stmAdapter.deleteSubnetMappings(config, hostssubnet);
        Assert.assertNull(getTestHostSubnet(name, cidrBlock));
    }
//    @BeforeClass
//    public static void setUpClass() throws StmRollBackException {
//        EXISTING_MAPPINGS = stmAdapter.getSubnetMappings(config, TARGET_HOST);
//    }
//
//    @AfterClass
//    public static void tearDownClass() throws StmRollBackException {
//        try {
//        stmAdapter.deleteLoadBalancer(config, lb);
//        } catch(Exception e) {
//
//        }
//    }
//
//    @Test
//    public void getSubnetMappingsTest() throws StmRollBackException {
//        Hostssubnet existingMapping = stmAdapter.getSubnetMappings(config, TARGET_HOST);
//        List<Hostsubnet> existingSubnets = existingMapping.getHostsubnets();
//        Hostsubnet targetHost = existingSubnets.get(0);
//        List<NetInterface> interfaceList = targetHost.getNetInterfaces();
//        NetInterface netInterface = interfaceList.get(0);
//        String ifaceName = netInterface.getName();
//        List<Cidr> cidrList = netInterface.getCidrs();
//        Cidr cidr = cidrList.get(0);
//        String subnetBlock = cidr.getBlock();
//
//        Assert.assertNotNull(existingMapping);
//        Assert.assertFalse(existingSubnets.isEmpty());
//        Assert.assertNotNull(targetHost);
//        Assert.assertFalse(interfaceList.isEmpty());
//        Assert.assertNotNull(netInterface);
//        Assert.assertFalse(cidrList.isEmpty());
//        Assert.assertFalse(ifaceName.isEmpty());
//        Assert.assertNotNull(cidr);
//        Assert.assertFalse(subnetBlock.isEmpty());
//    }
//
//    @Test
//    public void setSubnetMappingsTest() throws StmRollBackException {
//        Hostssubnet newHss = makeHostssubnet();
//        Hostssubnet beforeHss = stmAdapter.getSubnetMappings(config, TARGET_HOST);
//        List<String> cidrBlocksExisting = getCidrBlocks(EXISTING_MAPPINGS);
//        List<String> cidrBlocksBefore = getCidrBlocks(beforeHss);
//        List<String> cidrBlocksNew = getCidrBlocks(newHss);
//
//        Assert.assertEquals(cidrBlocksBefore, cidrBlocksExisting);
//        Assert.assertFalse(cidrBlocksBefore.equals(cidrBlocksNew));
//
//        stmAdapter.setSubnetMappings(config, newHss);
//        Hostssubnet setHss = stmAdapter.getSubnetMappings(config, TARGET_HOST);
//        List<String> cidrBlocksSet = getCidrBlocks(setHss);
//        Assert.assertEquals(cidrBlocksSet, cidrBlocksNew);
//
//        stmAdapter.setSubnetMappings(config, beforeHss);
//        setHss = stmAdapter.getSubnetMappings(config, TARGET_HOST);
//        cidrBlocksSet = getCidrBlocks(setHss);
//        Assert.assertEquals(cidrBlocksSet, cidrBlocksBefore);
//    }
//
//    private void createSubnetMapping() {
//        String subnetName = "TestSubnet";
//        String netInterfaceName = "TestNetInterface";
//        String cidrBlock = "IDK";
//        Hostssubnet hostssubnet = new Hostssubnet();
//        List<Hostsubnet> subnetList = new ArrayList<Hostsubnet>();
//        Hostsubnet hostsubnet = new Hostsubnet();
//        hostsubnet.setName(subnetName);
//    }
//
//
//    @Test
//    public void deleteSubnetMappingsTest() throws StmRollBackException {
//        Hostssubnet backupHss = null;
//        Hostssubnet beforeHss = stmAdapter.getSubnetMappings(config, TARGET_HOST);
//        List<String> cidrBlocksBefore = null;
//        try {
//            cidrBlocksBefore = getCidrBlocks(beforeHss);
//        } catch (Exception e) {
//        }
//
//        if (cidrBlocksBefore == null || cidrBlocksBefore.isEmpty()) {
//            stmAdapter.setSubnetMappings(config, makeHostssubnet());
//            backupHss = beforeHss;
//            beforeHss = stmAdapter.getSubnetMappings(config, TARGET_HOST);
//            cidrBlocksBefore = getCidrBlocks(beforeHss);
//        }
//
//        Assert.assertFalse(cidrBlocksBefore.isEmpty());
//
//        String interfaceName = beforeHss.getHostsubnets().get(0).getNetInterfaces().get(0).getName();
//        String firstBlock = cidrBlocksBefore.get(0);
//
//        Assert.assertFalse(interfaceName.isEmpty());
//        Assert.assertFalse(firstBlock.isEmpty());
//
//        Hostssubnet newHss = new Hostssubnet();
//        Hostsubnet newHs = new Hostsubnet();
//        NetInterface netInterface = new NetInterface();
//        Cidr cidr = new Cidr();
//
//        List<Hostsubnet> hostsubnetList = new ArrayList<Hostsubnet>();
//        List<NetInterface> netInterfaceList = new ArrayList<NetInterface>();
//        List<Cidr> cidrList = new ArrayList<Cidr>();
//
//        cidr.setBlock(firstBlock);
//        cidrList.add(cidr);
//
//        netInterface.setName(interfaceName);
//        netInterface.setCidrs(cidrList);
//        netInterfaceList.add(netInterface);
//
//        newHs.setName(TARGET_HOST);
//        newHs.setNetInterfaces(netInterfaceList);
//
//        hostsubnetList.add(newHs);
//        newHss.setHostsubnets(hostsubnetList);
//
//        stmAdapter.deleteSubnetMappings(config, newHss);
//
//        Hostssubnet afterHss = stmAdapter.getSubnetMappings(config, TARGET_HOST);
//        List<String> cidrBlocksAfter = getCidrBlocks(afterHss);
//        Assert.assertFalse(cidrBlocksAfter.contains(firstBlock));
//
//        if (backupHss != null)
//            stmAdapter.setSubnetMappings(config, backupHss);
//        else
//            stmAdapter.setSubnetMappings(config, beforeHss);
//    }
//
//    private List<String> getCidrBlocks(Hostssubnet hss) {
//        List<Cidr> cidrList = hss.getHostsubnets().get(0).getNetInterfaces().get(0).getCidrs();
//        List<String> cidrBlocks = new ArrayList<String>();
//        for (Cidr cidr : cidrList) {
//            cidrBlocks.add(cidr.getBlock());
//        }
//        return cidrBlocks;
//    }
//
//    private Hostssubnet makeHostssubnet() {
//        Hostssubnet newHss = new Hostssubnet();
//        Hostsubnet newHs = new Hostsubnet();
//        NetInterface netInterface = new NetInterface();
//        Cidr cidr = new Cidr();
//
//        List<Hostsubnet> hostsubnetList = new ArrayList<Hostsubnet>();
//        List<NetInterface> netInterfaceList = new ArrayList<NetInterface>();
//        List<Cidr> cidrList = new ArrayList<Cidr>();
//
//        cidr.setBlock("192.168.0.0/21");
//        cidrList.add(cidr);
//
//        netInterface.setName("eth0");
//        netInterface.setCidrs(cidrList);
//        netInterfaceList.add(netInterface);
//
//        newHs.setName(TARGET_HOST);
//        newHs.setNetInterfaces(netInterfaceList);
//
//        hostsubnetList.add(newHs);
//        newHss.setHostsubnets(hostsubnetList);
//
//        return newHss;
//    }
//
//    @Ignore
//    @Test
//    public void visualConfirmTest() throws StmRollBackException, InsufficientRequestException, StingrayRestClientObjectNotFoundException, StingrayRestClientException {
//        List<Child> trafficManagerList = stmClient.getTrafficManagers();
//
//        for (Child c : trafficManagerList) {
//            String trafficManagerName = c.getName();
//            TrafficManager trafficManager = stmClient.getTrafficManager(trafficManagerName);
//            TrafficManagerProperties trafficManagerProperties = trafficManager.getProperties();
//            TrafficManagerBasic trafficManagerBasic = trafficManagerProperties.getBasic();
//            List<TrafficManagerTrafficIp> trafficManagerTrafficIpList = trafficManagerBasic.getTrafficip();
//            System.out.println("Traffic Manager: " + trafficManagerName);
//
//            for (TrafficManagerTrafficIp trafficManagerTrafficIp : trafficManagerTrafficIpList) {
//                String interfaceName = trafficManagerTrafficIp.getName();
//                Set<String> masks = trafficManagerTrafficIp.getNetworks();
//                System.out.println("\tNetwork Interface: " + interfaceName);
//                System.out.println("\tSubnets:");
//                for (String mask : masks) {
//                    System.out.println("\t\t" + mask);
//                }
//            }
//        }
//    }
}
