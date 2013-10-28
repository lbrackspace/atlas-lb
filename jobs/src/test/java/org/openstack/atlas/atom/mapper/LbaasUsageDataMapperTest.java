package org.openstack.atlas.atom.mapper;

import com.rackspace.docs.usage.lbaas.CloudLoadBalancersType;
import com.rackspace.docs.usage.lbaas.StatusEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.atomhopper.exception.AtomHopperMappingException;
import org.openstack.atlas.atomhopper.factory.UsageEntryFactory;
import org.openstack.atlas.atomhopper.factory.UsageEntryFactoryImpl;
import org.openstack.atlas.atomhopper.factory.UsageEntryWrapper;
import org.openstack.atlas.atomhopper.util.UUIDUtil;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfiguration;
import org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.w3._2005.atom.Type;
import org.w3._2005.atom.UsageEntry;

import javax.xml.bind.JAXBElement;
import java.security.NoSuchAlgorithmException;

@RunWith(Enclosed.class)
public class LbaasUsageDataMapperTest {
    public static class WhenMappingUsageRecordToEntry {
        Usage usageRecord1;
        private Configuration configuration = new AtomHopperConfiguration();
        UsageEntryFactory usageEntryFactory = new UsageEntryFactoryImpl();

        @Before
        public void standUp() {
            usageRecord1 = new Usage();
            usageRecord1.setEventType("CREATE_LOADBALANCER");
            usageRecord1.setStartTime(AtomHopperUtil.getStartCal());
            usageRecord1.setEndTime(AtomHopperUtil.getNow());
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
        public void shouldMapBasicRecord() throws AtomHopperMappingException {
            usageEntryFactory.createEntry(usageRecord1);
        }

        @Test
        public void shouldMapAvgCC() throws AtomHopperMappingException {
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();

            JAXBElement<CloudLoadBalancersType> lbaasEntry = (JAXBElement<CloudLoadBalancersType>) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((Object) usageRecord1.getAverageConcurrentConnections(), lbaasEntry.getValue().getAvgConcurrentConnections());
        }

        @Test
        public void shouldMapAvgCCSSL() throws AtomHopperMappingException {
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();
            JAXBElement<CloudLoadBalancersType> lbaasEntry = (JAXBElement<CloudLoadBalancersType>) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((Object) usageRecord1.getAverageConcurrentConnectionsSsl(), lbaasEntry.getValue().getAvgConcurrentConnectionsSsl());
        }

        @Test
        public void shouldMapProductSchemaVersion() throws AtomHopperMappingException {
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();

            JAXBElement<CloudLoadBalancersType> lbaasEntry = (JAXBElement<CloudLoadBalancersType>) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((Object) usageRecord1.getEntryVersion(), Integer.valueOf(lbaasEntry.getValue().getVersion()));
        }

        @Test
        public void shouldMapBandwidthIn() throws AtomHopperMappingException {
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();

            JAXBElement<CloudLoadBalancersType> lbaasEntry = (JAXBElement<CloudLoadBalancersType>) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((long) usageRecord1.getIncomingTransfer(), lbaasEntry.getValue().getBandWidthIn());
        }

        @Test
        public void shouldMapBandwidthInSSL() throws AtomHopperMappingException {
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();

            JAXBElement<CloudLoadBalancersType> lbaasEntry = (JAXBElement<CloudLoadBalancersType>) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((long) usageRecord1.getIncomingTransferSsl(), lbaasEntry.getValue().getBandWidthInSsl());
        }

        @Test
        public void shouldMapBandwidthOut() throws AtomHopperMappingException {
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();

            JAXBElement<CloudLoadBalancersType> lbaasEntry = (JAXBElement<CloudLoadBalancersType>) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((long) usageRecord1.getOutgoingTransfer(), lbaasEntry.getValue().getBandWidthOut());
        }

        @Test
        public void shouldMapBandwidthOutSSL() throws AtomHopperMappingException {
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();

            JAXBElement<CloudLoadBalancersType> lbaasEntry = (JAXBElement<CloudLoadBalancersType>) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((long) usageRecord1.getOutgoingTransferSsl(), lbaasEntry.getValue().getBandWidthOutSsl());
        }

        @Test
        public void shouldMapNumPolls() throws AtomHopperMappingException {
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();

            JAXBElement<CloudLoadBalancersType> lbaasEntry = (JAXBElement<CloudLoadBalancersType>) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((Object) usageRecord1.getNumberOfPolls(), lbaasEntry.getValue().getNumPolls());
        }

        @Test
        public void shouldMapNumVips() throws AtomHopperMappingException {
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();

            JAXBElement<CloudLoadBalancersType> lbaasEntry = (JAXBElement<CloudLoadBalancersType>) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals((Object) usageRecord1.getNumVips(), lbaasEntry.getValue().getNumVips());
        }

        @Test
        public void shouldMapCoreEntryTitle() throws AtomHopperMappingException {
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();

            Assert.assertEquals("cloudLoadBalancers", entry.getTitle().getValue());
            Assert.assertEquals(Type.TEXT, entry.getTitle().getType());
        }

        @Test
        public void shouldMapCategory() throws AtomHopperMappingException {
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();

            Assert.assertEquals("loadBalancerUsage", entry.getCategory().get(0).getLabel());
            Assert.assertEquals("plain", entry.getCategory().get(0).getTerm());
        }

        @Test
        public void shouldGenerateUUIDMD5Hash() throws AtomHopperMappingException, NoSuchAlgorithmException {
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();

            String usageID = entry.getContent().getEvent().getId();

            String uuid = usageRecord1.getId() + "_" + usageRecord1.getLoadbalancer().getId() + "_" + "DFW";
            Assert.assertNotNull(UUIDUtil.genUUIDMD5Hash(uuid).toString());
        }

        @Test
        public void shouldMapStatusSuspendedIfLBSuspended() throws AtomHopperMappingException {
            usageRecord1.setEventType("SUSPEND_LOADBALANCER");
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();

            JAXBElement<CloudLoadBalancersType> lbaasEntry = (JAXBElement<CloudLoadBalancersType>) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals(StatusEnum.SUSPENDED, lbaasEntry.getValue().getStatus());
        }

        @Test
        public void shouldMapStatusActive() throws AtomHopperMappingException {
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();

            JAXBElement<CloudLoadBalancersType> lbaasEntry = (JAXBElement<CloudLoadBalancersType>) entry.getContent().getEvent().getAny().get(0);

            Assert.assertEquals(StatusEnum.ACTIVE, lbaasEntry.getValue().getStatus());
        }

        @Test
        public void shouldSetEventTimeForDelete() throws AtomHopperMappingException {
            usageRecord1.setEventType("DELETE_LOADBALANCER");
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();

            Assert.assertNotNull(entry.getContent().getEvent().getEventTime());
            Assert.assertNull(entry.getContent().getEvent().getStartTime());
            Assert.assertNull(entry.getContent().getEvent().getEndTime());
        }

        @Test
        public void shouldNotSetEventTimeforNonDelete() throws AtomHopperMappingException {
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();

            Assert.assertNull(entry.getContent().getEvent().getEventTime());
            Assert.assertNotNull(entry.getContent().getEvent().getStartTime());
            Assert.assertNotNull(entry.getContent().getEvent().getEndTime());
        }

        @Test
        public void shouldNotUpdateRefIdIfUUIDIsNull() throws AtomHopperMappingException {
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();

            Assert.assertNull(entry.getContent().getEvent().getReferenceId());
        }

        @Test
        public void shouldUpdateRefIdIfCorrectedUsage() throws AtomHopperMappingException, NoSuchAlgorithmException {
            usageRecord1.setUuid(usageEntryFactory.genUUIDObject(usageRecord1).toString());
            usageRecord1.setReferenceId("52ab8665-1a1c-3765-96cd-29d54d0f7627");
            usageRecord1.setCorrected(true);
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();
            Assert.assertNotNull(entry.getContent().getEvent().getReferenceId());
            Assert.assertEquals(entry.getContent().getEvent().getReferenceId(), usageRecord1.getReferenceId());
            Assert.assertNotSame(entry.getContent().getEvent().getId(), entry.getContent().getEvent().getReferenceId());
            Assert.assertEquals(entry.getContent().getEvent().getId(), usageRecord1.getUuid());
        }

        @Test
        public void shouldNotUpdateRefIdIfUUIDNotNullAndCorrectedUsageFalse() throws AtomHopperMappingException {
            usageRecord1.setUuid("52ab8665-1a1c-3765-96cd-29d54d0f7624");
            usageRecord1.setCorrected(false);
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            UsageEntry entry = entryMap.getEntryObject();
            Assert.assertNull(entry.getContent().getEvent().getReferenceId());
        }

        @Test
        public void shouldNotMapRefIdForNewEntry() throws AtomHopperMappingException {
            UsageEntryWrapper entryMap = usageEntryFactory.createEntry(usageRecord1);
            usageRecord1.setCorrected(true);
            UsageEntry entry = entryMap.getEntryObject();
            String entrystring = entryMap.getEntryString();
//            System.out.print(entrystring);
            Assert.assertFalse(entrystring.contains("referenceId"));
            Assert.assertNull(entry.getContent().getEvent().getReferenceId());
        }
    }
}
