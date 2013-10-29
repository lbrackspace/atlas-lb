package org.openstack.atlas.atom.util;

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

import javax.xml.bind.JAXBException;
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

        @Before
        public void standUp() {
            cal = Calendar.getInstance();

            baseUsage = new Usage();
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
    }
}