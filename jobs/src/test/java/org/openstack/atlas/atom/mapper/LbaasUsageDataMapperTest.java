package org.openstack.atlas.atom.mapper;

import com.rackspace.docs.usage.lbaas.StatusEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.atom.config.AtomHopperConfiguration;
import org.openstack.atlas.atom.pojo.EntryPojo;
import org.openstack.atlas.atom.pojo.LBaaSUsagePojo;
import org.openstack.atlas.atom.util.AHUSLUtil;
import org.openstack.atlas.atom.util.UUIDUtil;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.w3._2005.atom.Type;

import javax.xml.datatype.DatatypeConfigurationException;
import java.security.NoSuchAlgorithmException;

@RunWith(Enclosed.class)
public class LbaasUsageDataMapperTest {
    public static class WhenMappingUsageRecordToEntry {
        Usage usageRecord1;
        private Configuration configuration = new AtomHopperConfiguration();

        @Before
        public void standUp() {
            usageRecord1 = new Usage();
            usageRecord1.setEventType("CREATE_LOADBALANCER");
            usageRecord1.setStartTime(AHUSLUtil.getStartCal());
            usageRecord1.setEndTime(AHUSLUtil.getNow());
            usageRecord1.setAccountId(12345);
            usageRecord1.setAverageConcurrentConnections((double) 2345556);
            usageRecord1.setAverageConcurrentConnectionsSsl((double) 234892357);
            usageRecord1.setEntryVersion(1);
            usageRecord1.setIncomingTransfer((long) 234235);
            usageRecord1.setIncomingTransferSsl((long) 2343566);
            usageRecord1.setNeedsPushed(true);
            usageRecord1.setNumberOfPolls(23);
            usageRecord1.setNumVips(2);
            usageRecord1.setOutgoingTransfer((long) 26435);
            usageRecord1.setOutgoingTransferSsl((long) 26435);
            usageRecord1.setTags(1);
            usageRecord1.setId(234);

            LoadBalancer lb = new LoadBalancer();
            lb.setId(234);
            lb.setAccountId(12345);

            usageRecord1.setLoadbalancer(lb);
        }

        @Test
        public void shouldMapBasicRecord() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "ORD");
        }

        @Test
        public void shouldMapAvgCC() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            EntryPojo entry = LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "ORD");
            LBaaSUsagePojo lbaasEntry = (LBaaSUsagePojo) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((Object) usageRecord1.getAverageConcurrentConnections(), lbaasEntry.getAvgConcurrentConnections());
        }

        @Test
        public void shouldMapAvgCCSSL() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            EntryPojo entry = LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "ORD");
            LBaaSUsagePojo lbaasEntry = (LBaaSUsagePojo) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((Object) usageRecord1.getAverageConcurrentConnectionsSsl(), lbaasEntry.getAvgConcurrentConnectionsSsl());
        }

        @Test
        public void shouldMapProductSchemaVersion() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            EntryPojo entry = LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "ORD");
            LBaaSUsagePojo lbaasEntry = (LBaaSUsagePojo) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((Object) usageRecord1.getEntryVersion(), Integer.valueOf(lbaasEntry.getVersion()));
        }

        @Test
        public void shouldMapBandwidthIn() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            EntryPojo entry = LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "ORD");
            LBaaSUsagePojo lbaasEntry = (LBaaSUsagePojo) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((long)usageRecord1.getIncomingTransfer(), lbaasEntry.getBandWidthIn());
        }

        @Test
        public void shouldMapBandwidthInSSL() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            EntryPojo entry = LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "ORD");
            LBaaSUsagePojo lbaasEntry = (LBaaSUsagePojo) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((long) usageRecord1.getIncomingTransferSsl(), lbaasEntry.getBandWidthInSsl());
        }

        @Test
        public void shouldMapBandwidthOut() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            EntryPojo entry = LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "ORD");
            LBaaSUsagePojo lbaasEntry = (LBaaSUsagePojo) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((long) usageRecord1.getOutgoingTransfer(), lbaasEntry.getBandWidthOut());
        }

        @Test
        public void shouldMapBandwidthOutSSL() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            EntryPojo entry = LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "ORD");
            LBaaSUsagePojo lbaasEntry = (LBaaSUsagePojo) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((long) usageRecord1.getOutgoingTransferSsl(), lbaasEntry.getBandWidthOutSsl());
        }

        @Test
        public void shouldMapNumPolls() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            EntryPojo entry = LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "ORD");
            LBaaSUsagePojo lbaasEntry = (LBaaSUsagePojo) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((Object) usageRecord1.getNumberOfPolls(), lbaasEntry.getNumPolls());
        }

        @Test
        public void shouldMapNumVips() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            EntryPojo entry = LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "ORD");
            LBaaSUsagePojo lbaasEntry = (LBaaSUsagePojo) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((Object) usageRecord1.getNumVips(), lbaasEntry.getNumVips());
        }

        @Test
        public void shouldMapCoreEntryTitle() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            EntryPojo entry = LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "ORD");

            Assert.assertEquals("cloudLoadBalancers", entry.getTitle().getValue());
            Assert.assertEquals(Type.TEXT, entry.getTitle().getType());
        }

        @Test
        public void shouldMapCategory() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            EntryPojo entry = LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "ORD");

            Assert.assertEquals("loadBalancerUsage", entry.getCategory().get(0).getLabel());
            Assert.assertEquals("plain", entry.getCategory().get(0).getTerm());
        }

        @Test
        public void shouldMapUUID() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            EntryPojo entry = LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "DFW");
            String usageID = entry.getContent().getEvent().getId();

            String uuid = usageRecord1.getId() + "_" + usageRecord1.getLoadbalancer().getId() + "_" + "DFW";
            Assert.assertNotNull(UUIDUtil.genUUIDMD5Hash(uuid).toString());
        }

        @Test
        public void shouldMapStatusSuspendedIfLBSuspend() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            usageRecord1.setEventType("SUSPEND_LOADBALANCER");
            EntryPojo entry = LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "DFW");
            LBaaSUsagePojo lbaasEntry = (LBaaSUsagePojo) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals(StatusEnum.SUSPENDED, lbaasEntry.getStatus());
        }

        @Test
        public void shouldMapStatusActive() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            EntryPojo entry = LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "DFW");
            LBaaSUsagePojo lbaasEntry = (LBaaSUsagePojo) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals(StatusEnum.ACTIVE, lbaasEntry.getStatus());
        }

        @Test
        public void shouldSetEventTimeForDelete() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            usageRecord1.setEventType("DELETE_LOADBALANCER");
            EntryPojo entry = LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "DFW");
            Assert.assertNotNull(entry.getContent().getEvent().getEventTime());
            Assert.assertNull(entry.getContent().getEvent().getStartTime());
            Assert.assertNull(entry.getContent().getEvent().getEndTime());
        }

        @Test
        public void shouldNotSetEventTimeforNonDelete() throws DatatypeConfigurationException, NoSuchAlgorithmException {
            EntryPojo entry = LbaasUsageDataMapper.buildUsageEntry(usageRecord1, configuration, "DFW");
            Assert.assertNull(entry.getContent().getEvent().getEventTime());
            Assert.assertNotNull(entry.getContent().getEvent().getStartTime());
            Assert.assertNotNull(entry.getContent().getEvent().getEndTime());
        }
    }
}
