package org.openstack.atlas.api.mgmt.mapper.dozer;

import org.openstack.atlas.service.domain.entities.IpVersion;
import org.openstack.atlas.service.domain.entities.Ticket;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class VirtualIpTest {

    private static final String managementDozerConfigFile = "loadbalancing-dozer-management-mapping.xml";

    public static class WhenMappingAVirtualIpFromDomainToDataModel {
        private DozerBeanMapper mapper;
        private org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp dataModelVip;
        private VirtualIp vip;
        private Ticket ticket;

        @Before
        public void standUp() {
            mapper = MapperBuilder.getConfiguredMapper(managementDozerConfigFile);

            vip = new VirtualIp();
            vip.setId(1234);
            vip.setIpAddress("10.0.0.1");
            vip.setVipType(VirtualIpType.PUBLIC);

            ticket = new Ticket();
            ticket.setTicketId("1234");
            ticket.setComment("My first comment!");
            vip.setTicket(ticket);

            dataModelVip = mapper.map(vip, org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp.class);
        }

        @Test
        public void shouldNotFailWhenDomainVirtualIpIsEmpty() {
            vip = new VirtualIp();
            try {
                dataModelVip = mapper.map(vip, org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp.class);
            } catch (Exception e) {
                Assert.fail("Empty data model vip caused this exception");
            }
        }

        @Test
        public void shouldMapTicket() {
            Assert.assertNotNull(dataModelVip.getTicket());
            Assert.assertEquals(ticket.getTicketId(), dataModelVip.getTicket().getTicketId());
            Assert.assertEquals(ticket.getComment(), dataModelVip.getTicket().getComment());
        }
    }
}
