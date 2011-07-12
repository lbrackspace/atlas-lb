package org.openstack.atlas.api.mgmt.mapper.dozer;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostMachineDetails;
import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(Enclosed.class)
public class HostMachineDetailsTest {
    private static final String managementDozerConfigFile = "loadbalancing-dozer-management-mapping.xml";

    public static class WhenMappingHostDataModeToRestMachineDetails {
        private DozerBeanMapper mapper;
        private org.openstack.atlas.service.domain.pojos.HostMachineDetails hostMachine;
        private org.openstack.atlas.service.domain.entities.Host host;
        private List<org.openstack.atlas.service.domain.entities.Host> hosts;
        private HostMachineDetails dHostMD;

        @Before
        public void setUp() {
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add(managementDozerConfigFile);
            mapper = new DozerBeanMapper(mappingFiles);
            host = new org.openstack.atlas.service.domain.entities.Host();
            host.setId(1);
            host.setManagementIp("10.0.0.0");
            host.setCoreDeviceId("1");
            host.setName("imAHost");
            host.setManagementIp("10.0.0.0");
            host.setMaxConcurrentConnections(1);
            host.setTrafficManagerName("imaTrafficeMngr");
            host.setSoapEndpointActive(true);
            hosts = new ArrayList<org.openstack.atlas.service.domain.entities.Host>();
            hosts.add(host);

            hostMachine = new org.openstack.atlas.service.domain.pojos.HostMachineDetails();
            hostMachine.setActiveLBConfigurations((long) 10);
            hostMachine.setAvailableConcurrentConnections(4);
            hostMachine.setCurrentUtilization("1.0%");
            hostMachine.setHost(host);
            hostMachine.setTotalConcurrentConnections(8);
            hostMachine.setUniqueCustomers(10000);

            dHostMD = mapper.map(hostMachine, HostMachineDetails.class);
        }

        @Test
        public void shouldMapAllFields() {
            Assert.assertEquals(10, (long)dHostMD.getActiveLBConfigurations());
            Assert.assertEquals(new Integer(4), dHostMD.getAvailableConcurrentConnections());
            Assert.assertEquals("1.0%", dHostMD.getCurrentUtilization());
            Assert.assertEquals(host.getId(), dHostMD.getHost().getId());
            Assert.assertEquals(new Integer(8), dHostMD.getTotalConcurrentConnections());
            Assert.assertEquals(new Integer(10000), dHostMD.getUniqueCustomers());
        }

        @Test
        public void shouldMapHostProperties() {
            Assert.assertEquals(host.getId(), dHostMD.getHost().getId());
            Assert.assertEquals(host.getMaxConcurrentConnections(), dHostMD.getHost().getMaxConcurrentConnections());
            Assert.assertEquals(host.getCoreDeviceId(), dHostMD.getHost().getCoreDeviceId());
            Assert.assertEquals(host.getManagementIp(), dHostMD.getHost().getManagementIp());
            Assert.assertEquals(host.getName(), dHostMD.getHost().getName());
            Assert.assertEquals(host.getTrafficManagerName(), dHostMD.getHost().getTrafficManagerName());
            Assert.assertEquals(host.isSoapEndpointActive(), dHostMD.getHost().isSoapEndpointActive());
        }
        //TODO:more test
    }
}
