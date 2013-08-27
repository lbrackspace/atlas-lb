package org.openstack.atlas.atom.util;

import com.rackspace.docs.core.event.EventType;
import com.rackspace.docs.event.lbaas.delete.ResourceTypes;
import com.rackspace.docs.usage.lbaas.CloudLoadBalancersType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.atomhopper.exception.AtomHopperMappingException;
import org.openstack.atlas.atomhopper.factory.UsageEntryFactoryImpl;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.w3._2005.atom.ObjectFactory;
import org.w3._2005.atom.UsageEntry;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;


@RunWith(Enclosed.class)
public class UsageMarshallerTest {
    public static class WhenMarshallingGeneratedXML {
        private UUID uuid;
        private Calendar cal;
        private Usage baseUsage;
        private ObjectFactory entryFactory;
        private final String USAGE_LABEL = "loadBalancerUsage";
        private final String LBAAS_TITLE = "cloudLoadBalancers";
        private final String SERVICE_CODE = "CloudLoadBalancers";


        @Before
        public void standUp() {
            cal = Calendar.getInstance();

            baseUsage = new Usage();
            baseUsage.setId(1);
            baseUsage.setCorrected(false);
            baseUsage.setAccountId(54321);
            baseUsage.setAverageConcurrentConnections(0.0);
            baseUsage.setAverageConcurrentConnectionsSsl(0.0);
            baseUsage.setEndTime(cal);
            cal.add(Calendar.HOUR_OF_DAY, -1);
            cal.getTime();
            baseUsage.setStartTime(cal);
            baseUsage.setEntryVersion(1);
            baseUsage.setEventType(org.openstack.atlas.service.domain.events.entities.EventType.CREATE_LOADBALANCER.name());
            baseUsage.setIncomingTransfer((long) 0);
            baseUsage.setNeedsPushed(true);
            baseUsage.setUuid(null);
            baseUsage.setTags(0);
            baseUsage.setNumAttempts(0);
            baseUsage.setNumVips(1);
            baseUsage.setOutgoingTransfer((long) 0);
            baseUsage.setOutgoingTransferSsl((long) 0);
            LoadBalancer lb = new LoadBalancer();
            lb.setId(1223);
            lb.setName("base");
            baseUsage.setLoadbalancer(lb);

            entryFactory = new ObjectFactory();
        }

        @Test
        public void shouldSuccessfullyMarshallEntry() throws AtomHopperMappingException, JAXBException {
            UsageEntryFactoryImpl usageEntryFactory = new UsageEntryFactoryImpl();
            Map<Object, Object> mappedobj = usageEntryFactory.createEntry(baseUsage);
            String entry = (String) mappedobj.get("entrystring");
            Assert.assertTrue(entry.contains("54321"));
        }

        @Test
        public void shouldSuccessfullyMarshallDeleteEntry() throws AtomHopperMappingException, JAXBException {
            UsageEntryFactoryImpl usageEntryFactory = new UsageEntryFactoryImpl();
            baseUsage.setEventType(org.openstack.atlas.service.domain.events.entities.EventType.DELETE_LOADBALANCER.name());
            Map<Object, Object> mappedobj = usageEntryFactory.createEntry(baseUsage);
            String entry = (String) mappedobj.get("entrystring");
            Assert.assertTrue(entry.contains("54321"));
        }

        @Test
        public void shouldGenerateCorrectUUID() throws AtomHopperMappingException, JAXBException, NoSuchAlgorithmException {
            UsageEntryFactoryImpl usageEntryFactory = new UsageEntryFactoryImpl();
            Map<Object, Object> mappedobj = usageEntryFactory.createEntry(baseUsage);
            String entry = (String) mappedobj.get("entrystring");
            UsageEntry entryobj = (UsageEntry) mappedobj.get("entryobject");
            Assert.assertTrue(entry.contains("54321"));
            Assert.assertTrue(entryobj.getContent().getEvent().getId().equals(usageEntryFactory.genUUIDObject(baseUsage).toString()));
        }

        @Test
        public void shouldMapAllDataForUsageRecord() throws AtomHopperMappingException, JAXBException, NoSuchAlgorithmException {
            UsageEntryFactoryImpl usageEntryFactory = new UsageEntryFactoryImpl();
            Map<Object, Object> mappedobj = usageEntryFactory.createEntry(baseUsage);
            String entry = (String) mappedobj.get("entrystring");
            UsageEntry entryobj = (UsageEntry) mappedobj.get("entryobject");
            Assert.assertTrue(entry.contains("54321"));
            Assert.assertTrue(entryobj.getContent().getEvent().getId().equals(usageEntryFactory.genUUIDObject(baseUsage).toString()));
            Assert.assertEquals(String.valueOf(baseUsage.getAccountId()), entryobj.getContent().getEvent().getTenantId());
            Assert.assertEquals(String.valueOf(baseUsage.getLoadbalancer().getId()), entryobj.getContent().getEvent().getResourceId());
            Assert.assertEquals(String.valueOf(baseUsage.getEntryVersion()), entryobj.getContent().getEvent().getVersion());
            Assert.assertEquals(EventType.USAGE, entryobj.getContent().getEvent().getType());

            CloudLoadBalancersType ctype = (CloudLoadBalancersType) ((JAXBElement) entryobj.getContent().getEvent().getAny().get(0)).getValue();
            Assert.assertEquals((Object) baseUsage.getNumVips(), ctype.getNumVips());
            Assert.assertEquals(SERVICE_CODE, ctype.getServiceCode());
            Assert.assertEquals(com.rackspace.docs.usage.lbaas.ResourceTypes.LOADBALANCER, ctype.getResourceType());


            Assert.assertEquals((Object) baseUsage.getAverageConcurrentConnections(), ctype.getAvgConcurrentConnections());
            Assert.assertEquals((Object) baseUsage.getAverageConcurrentConnectionsSsl(), ctype.getAvgConcurrentConnectionsSsl());
            Assert.assertEquals((Object) baseUsage.getAverageConcurrentConnectionsSsl(), ctype.getAvgConcurrentConnectionsSsl());
            Assert.assertEquals((Object) baseUsage.getIncomingTransfer(), ctype.getBandWidthIn());
            Assert.assertEquals((Object) baseUsage.getIncomingTransferSsl(), ctype.getBandWidthInSsl());
            Assert.assertEquals((Object) baseUsage.getOutgoingTransfer(), ctype.getBandWidthOut());
            Assert.assertEquals((Object) baseUsage.getOutgoingTransferSsl(), ctype.getBandWidthOutSsl());
        }

        @Test
        public void shouldMapAllDataForUsageRecordDelete() throws AtomHopperMappingException, JAXBException, NoSuchAlgorithmException {
            UsageEntryFactoryImpl usageEntryFactory = new UsageEntryFactoryImpl();
            baseUsage.setEventType(org.openstack.atlas.service.domain.events.entities.EventType.DELETE_LOADBALANCER.name());
            Map<Object, Object> mappedobj = usageEntryFactory.createEntry(baseUsage);
            String entry = (String) mappedobj.get("entrystring");
            UsageEntry entryobj = (UsageEntry) mappedobj.get("entryobject");
            Assert.assertTrue(entry.contains("54321"));
            Assert.assertTrue(entryobj.getContent().getEvent().getId().equals(usageEntryFactory.genUUIDObject(baseUsage).toString()));
            Assert.assertEquals(String.valueOf(baseUsage.getAccountId()), entryobj.getContent().getEvent().getTenantId());
            Assert.assertEquals(String.valueOf(baseUsage.getLoadbalancer().getId()), entryobj.getContent().getEvent().getResourceId());
            Assert.assertEquals(String.valueOf(baseUsage.getEntryVersion()), entryobj.getContent().getEvent().getVersion());
            com.rackspace.docs.event.lbaas.delete.CloudLoadBalancersType ctype = (com.rackspace.docs.event.lbaas.delete.CloudLoadBalancersType) ((JAXBElement) entryobj.getContent().getEvent().getAny().get(0)).getValue();
            Assert.assertEquals(SERVICE_CODE, ctype.getServiceCode());
            Assert.assertEquals(ResourceTypes.LOADBALANCER, ctype.getResourceType());
            Assert.assertEquals(EventType.DELETE, entryobj.getContent().getEvent().getType());
        }
    }
}