package org.openstack.atlas.api.mgmt.mapper.dozer;

import org.dozer.Mapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.Created;
import org.openstack.atlas.docs.loadbalancers.api.v1.Updated;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.entities.Ticket;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

@RunWith(Enclosed.class)
public class DataModelToDomainHostsSubnetsTest {

    private static final String managementDozerConfigFile = "loadbalancing-dozer-management-mapping.xml";

    public static class When_mapping_hostsubnets_from_datamodel_to_domain {

        private Mapper mapper;
        private Hostssubnet hostssubnet;
        private Hostsubnet hostsubnet;
        private NetInterface netInterface;
        private org.openstack.atlas.service.domain.pojos.Hostsubnet domainHostSubnet;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(managementDozerConfigFile);

            hostssubnet = new Hostssubnet();
            hostsubnet = new Hostsubnet();
            netInterface = new NetInterface();

            netInterface.setName("eth0");
            Cidr cidr = new Cidr();
            cidr.setBlock("127.0.0.1/16");
            netInterface.getCidrs().add(cidr);
            hostsubnet.setName("host1");
            hostsubnet.getNetInterfaces().add(netInterface);

            domainHostSubnet = mapper.map(hostsubnet,
                    org.openstack.atlas.service.domain.pojos.Hostsubnet.class);
        }

        @Test
        public void should_map_host_subnet_single_cidr() {
            Assert.assertEquals(1, domainHostSubnet.getNetInterfaces().size());
            Assert.assertEquals("host1", domainHostSubnet.getName());
            Assert.assertEquals("eth0", domainHostSubnet.getNetInterfaces().get(0).getName());
            Assert.assertEquals("127.0.0.1/16", domainHostSubnet.getNetInterfaces().get(0).getCidrs().get(0).getBlock());
        }

        @Test
        public void should_map_host_subnet_multiple_cidr() {
            Cidr cidr = new Cidr();
            cidr.setBlock("192.0.0.1/16");
            netInterface.getCidrs().add(cidr);
            domainHostSubnet = mapper.map(hostsubnet,
                    org.openstack.atlas.service.domain.pojos.Hostsubnet.class);

            Assert.assertEquals(1, domainHostSubnet.getNetInterfaces().size());
            Assert.assertEquals(2, domainHostSubnet.getNetInterfaces().get(0).getCidrs().size());
            List<org.openstack.atlas.service.domain.pojos.Cidr> cidrs = domainHostSubnet.getNetInterfaces().get(0).getCidrs();
            for (org.openstack.atlas.service.domain.pojos.Cidr cider : cidrs) {
                Assert.assertTrue(cider.getBlock().matches("(192.0.0.1\\/16|127.0.0.1\\/16)"));
            }
        }
    }
}
