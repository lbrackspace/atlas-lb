package org.openstack.atlas.adapter.itest;


import org.junit.Test;
import org.openstack.atlas.service.domain.pojos.Cidr;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Hostsubnet;
import org.openstack.atlas.service.domain.pojos.NetInterface;

import java.util.ArrayList;
import java.util.List;

public class SubnetMappingITest {

    @Test
    public void whatAmIDoing() {

    }

    private void createSubnetMapping() {
        String subnetName = "TestSubnet";
        String netInterfaceName = "TestNetInterface";
        String cidrBlock = "IDK";
        Hostssubnet hostssubnet = new Hostssubnet();
        List<Hostsubnet> subnetList = new ArrayList<Hostsubnet>();
        Hostsubnet hostsubnet = new Hostsubnet();
        hostsubnet.setName(subnetName);
        List<NetInterface> netInterfaceList = new ArrayList<NetInterface>();
        NetInterface netInterface = new NetInterface();
        netInterface.setName(netInterfaceName);
        List<Cidr> cidrList = new ArrayList<Cidr>();
        Cidr cidr = new Cidr();
        cidr.setBlock(cidrBlock);
        cidrList.add(cidr);
        netInterface.setCidrs(cidrList);
        netInterfaceList.add(netInterface);
        hostsubnet.setNetInterfaces(netInterfaceList);
        subnetList.add(hostsubnet);
        hostssubnet.setHostsubnets(subnetList);

    }
}
