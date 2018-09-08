package org.openstack.atlas.api.mgmt.mapper.dozer;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Backup;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.Zone;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.LoadBalancerServiceEvent;
import org.openstack.atlas.service.domain.events.pojos.AccountLoadBalancerServiceEvents;
import org.openstack.atlas.service.domain.events.pojos.LoadBalancerServiceEvents;
import org.openstack.atlas.service.domain.pojos.HostUsage;
import org.openstack.atlas.service.domain.pojos.HostUsageList;
import org.openstack.atlas.service.domain.pojos.HostUsageRecord;
import org.openstack.atlas.api.helpers.CalendarHelper;
import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(Enclosed.class)
public class DomainToDataModelBackupTest {

    private static final String managementDozerConfigFile = "loadbalancing-dozer-management-mapping.xml";

    public static class WhenMappingABackupFromDomainToDataModel {
    
        private DozerBeanMapper mapper;
        private Backup backup;
        private org.openstack.atlas.service.domain.entities.Backup domainBackup;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(managementDozerConfigFile);

            domainBackup = new org.openstack.atlas.service.domain.entities.Backup();

            domainBackup.setId(1);
            domainBackup.setName("MyFirstBackup");
            domainBackup.setBackupTime(new GregorianCalendar(2010, Calendar.OCTOBER, 7));

            Host host = new Host();
            host.setId(1234);
            host.setZone(Zone.A);
            domainBackup.setHost(host);



            backup = mapper.map(domainBackup, Backup.class);
        }

        @Test
        public void shouldNotFailWhenDomainBackupIsNull() {
            domainBackup = new org.openstack.atlas.service.domain.entities.Backup();
            try {
                backup = mapper.map(domainBackup, Backup.class);
            } catch (Exception e) {
                Assert.fail("Empty domain load balancer caused this exception");
            }
        }

        @Test
        public void shouldMapAllFieldsExceptHostIdWhenUsingDefaultMap() {
            Assert.assertEquals(new Integer(1), backup.getId());
            Assert.assertEquals("MyFirstBackup", backup.getName());
            Assert.assertEquals(new GregorianCalendar(2010, Calendar.OCTOBER, 7), backup.getBackupTime());
            Assert.assertNull(backup.getHostId());
        }

        @Test
        public void shouldMapAllFieldsWhenUsingFullMapper() {
            backup = mapper.map(domainBackup, Backup.class, "FULL_BACKUP");
            Assert.assertEquals(new Integer(1), backup.getId());
            Assert.assertEquals("MyFirstBackup", backup.getName());
            Assert.assertEquals(new GregorianCalendar(2010, Calendar.OCTOBER, 7), backup.getBackupTime());
            Assert.assertEquals(new Integer(1234), backup.getHostId());
        }
    }


    public static class When_mapping_Host_UsageList {

        private DozerBeanMapper mapper;
        private HostUsageList dae;
        private org.openstack.atlas.docs.loadbalancers.api.management.v1.HostUsageList rae;

        @Before
        public void setUp() {
            rae = new org.openstack.atlas.docs.loadbalancers.api.management.v1.HostUsageList();
            mapper = MapperBuilder.getConfiguredMapper(managementDozerConfigFile);
            dae = new HostUsageList();

            List<HostUsageRecord> x = new ArrayList();
            HostUsageRecord sel = new HostUsageRecord();
            sel.setHostId(new Integer(3));

            List<HostUsage> xs = new ArrayList();
            Calendar now = Calendar.getInstance();
            now = CalendarHelper.zeroOutTime(now);
            xs.add(newHostUsage(23L, 44L, now));
            xs.add(newHostUsage(42L, 33L, Calendar.getInstance()));
            sel.setHostUsages(xs);
            x.add(sel);
            sel = new HostUsageRecord();
            sel.setHostId(31337);
            xs = new ArrayList();

            xs.add(newHostUsage(26L, 45L, Calendar.getInstance()));
            xs.add(newHostUsage(26L, 43L, Calendar.getInstance()));
            sel.setHostUsages(xs);
            x.add(sel);

            dae.setHostUsageRecords(x);
        }


        @Test
        public void ShouldHaveIdenticalHostIds() {
            rae = mapper.map(dae, rae.getClass());
            Assert.assertEquals(new Integer(3), rae.getHostUsageRecords().get(0).getHostId());
            Assert.assertEquals(new Integer(31337), rae.getHostUsageRecords().get(1).getHostId());
        }


        @Test
        public void ShouldHaveIdenticalBandwidths() {
            rae = mapper.map(dae, rae.getClass());
            Assert.assertEquals(new Long(23), rae.getHostUsageRecords().get(0).getHostUsages().get(0).getBandwidthIn());
            Assert.assertEquals(new Long(44), rae.getHostUsageRecords().get(0).getHostUsages().get(0).getBandwidthOut());
            Assert.assertEquals(dae.getHostUsageRecords().get(0).getHostUsages().get(0).getDay(), rae.getHostUsageRecords().get(0).getHostUsages().get(0).getDay());
        }



        private static HostUsage newHostUsage(Long bin, Long bout, Calendar r) {
            HostUsage out = new HostUsage();
            out.setBandwidthIn(bin);
            out.setBandwidthOut(bout);
            out.setDay(r);
            return out;
        }
    }

    public static class When_mapping_Account_LoadBalancerEvents {

        private DozerBeanMapper mapper;
        private AccountLoadBalancerServiceEvents dae;
        private org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountLoadBalancerServiceEvents rae;

        @Before
        public void setUp() {
            rae = new org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountLoadBalancerServiceEvents();
            mapper = MapperBuilder.getConfiguredMapper(managementDozerConfigFile);
            dae = new AccountLoadBalancerServiceEvents();
            dae.setAccountId(3000);
            LoadBalancerServiceEvents sel = new LoadBalancerServiceEvents();
            sel.setLoadbalancerId(69);
            sel.getLoadBalancerServiceEvents().add(newLoadBalancerServiceEvent(1, "user69"));
            sel.getLoadBalancerServiceEvents().add(newLoadBalancerServiceEvent(2, "user69"));
            dae.getLoadBalancerServiceEvents().add(sel);
            sel = new LoadBalancerServiceEvents();
            sel.setLoadbalancerId(31337);
            sel.getLoadBalancerServiceEvents().add(newLoadBalancerServiceEvent(3, "user31337"));
            sel.getLoadBalancerServiceEvents().add(newLoadBalancerServiceEvent(4, "user31337"));
            dae.getLoadBalancerServiceEvents().add(sel);
        }

        @Test
        public void shouldMapAccountIdCorrectly() {
            rae = mapper.map(dae, rae.getClass());
            Assert.assertEquals(new Integer(3000), dae.getAccountId());
        }

        @Test
        public void accountServiceEventShouldHave2Itmems() {
            rae = mapper.map(dae, rae.getClass());
            Assert.assertEquals(2, rae.getLoadBalancerServiceEvents().size());
        }

        @Test
        public void accountServiceEventShouldHaveIdenticalLoadBalncerIds() {
            rae = mapper.map(dae, rae.getClass());
            Assert.assertEquals(new Integer(69), rae.getLoadBalancerServiceEvents().get(0).getLoadbalancerId());
            Assert.assertEquals(new Integer(31337), rae.getLoadBalancerServiceEvents().get(1).getLoadbalancerId());
        }

        @Test
        public void loadBalancerEventIdsShouldBeMappedCorrectly() {
            rae = mapper.map(dae, rae.getClass());
            Assert.assertEquals(new Integer(1), rae.getLoadBalancerServiceEvents().get(0).getLoadBalancerServiceEvents().get(0).getId());
            Assert.assertEquals(new Integer(2), rae.getLoadBalancerServiceEvents().get(0).getLoadBalancerServiceEvents().get(1).getId());
            Assert.assertEquals(new Integer(3), rae.getLoadBalancerServiceEvents().get(1).getLoadBalancerServiceEvents().get(0).getId());
            Assert.assertEquals(new Integer(4), rae.getLoadBalancerServiceEvents().get(1).getLoadBalancerServiceEvents().get(1).getId());
        }

        @Test
        public void loadBalancerEventSeverityShouldBeMappedCorrectly() {
            rae = mapper.map(dae, rae.getClass());

            Assert.assertEquals("user69", rae.getLoadBalancerServiceEvents().get(0).getLoadBalancerServiceEvents().get(0).getAuthor());
            Assert.assertEquals("user69", rae.getLoadBalancerServiceEvents().get(0).getLoadBalancerServiceEvents().get(1).getAuthor());
            Assert.assertEquals("user31337", rae.getLoadBalancerServiceEvents().get(1).getLoadBalancerServiceEvents().get(0).getAuthor());
            Assert.assertEquals("user31337", rae.getLoadBalancerServiceEvents().get(1).getLoadBalancerServiceEvents().get(1).getAuthor());
        }

        @Test
        public void loadBalancerEventCategorysShouldBeMappedCorrectly() {
            rae = mapper.map(dae, rae.getClass());
            String cat = "CREATE";
            Assert.assertEquals(cat, rae.getLoadBalancerServiceEvents().get(0).getLoadBalancerServiceEvents().get(0).getCategory());
            Assert.assertEquals(cat, rae.getLoadBalancerServiceEvents().get(0).getLoadBalancerServiceEvents().get(1).getCategory());
            Assert.assertEquals(cat, rae.getLoadBalancerServiceEvents().get(1).getLoadBalancerServiceEvents().get(0).getCategory());
            Assert.assertEquals(cat, rae.getLoadBalancerServiceEvents().get(1).getLoadBalancerServiceEvents().get(1).getCategory());
        }

        @Test
        public void loadBalancerEventDescriptionShouldBeMappedCorrectly() {
            rae = mapper.map(dae, rae.getClass());

            Assert.assertEquals("Test1", rae.getLoadBalancerServiceEvents().get(0).getLoadBalancerServiceEvents().get(0).getDescription());
            Assert.assertEquals("Test2", rae.getLoadBalancerServiceEvents().get(0).getLoadBalancerServiceEvents().get(1).getDescription());
            Assert.assertEquals("Test3", rae.getLoadBalancerServiceEvents().get(1).getLoadBalancerServiceEvents().get(0).getDescription());
            Assert.assertEquals("Test4", rae.getLoadBalancerServiceEvents().get(1).getLoadBalancerServiceEvents().get(1).getDescription());
        }

        private static LoadBalancerServiceEvent newLoadBalancerServiceEvent(Integer id, String author) {
            LoadBalancerServiceEvent out = new LoadBalancerServiceEvent();
            out.setAuthor(author);
            out.setId(id);
            out.setCategory(CategoryType.CREATE);
            out.setDescription(String.format("Test%d", id));
            out.setSeverity(EventSeverity.CRITICAL);
            return out;
        }
    }
}
