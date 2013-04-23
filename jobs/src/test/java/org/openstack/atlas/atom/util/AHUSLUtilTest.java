package org.openstack.atlas.atom.util;

import com.rackspace.docs.core.event.EventType;
import com.rackspace.docs.core.event.Region;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.atomhopper.factory.UsageEntryFactoryImpl;
import org.openstack.atlas.atomhopper.util.AHUSLServiceUtil;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;

import javax.xml.datatype.DatatypeConfigurationException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RunWith(Enclosed.class)
public class AHUSLUtilTest {
    public static class WhenConvertingCalToXMLGregorianCal {

        @Before
        public void standUp() {

        }

        @Test
        public void shouldConvertCal() throws DatatypeConfigurationException {
            Calendar cal = Calendar.getInstance();
            AHUSLServiceUtil.processCalendar(cal);
        }
    }

    public static class WhenMappingEventType {
        String baseEvent;
        Usage baseUsage;

        @Before
        public void standUp() {
            //UUID=(recordID, resourceID, region)
            baseEvent = "DFW";
            baseUsage = new Usage();
            baseUsage.setEventType(UsageEvent.CREATE_LOADBALANCER.name());
        }

        @Test
        public void shouldMapCreateEvent() throws NoSuchAlgorithmException, DatatypeConfigurationException {
            EventType eventType = UsageEntryFactoryImpl.mapEventType(baseUsage);
            Assert.assertEquals(EventType.CREATE, eventType);
        }

        @Test
        public void shouldMapDeleteEvent() throws NoSuchAlgorithmException, DatatypeConfigurationException {
            baseUsage = new Usage();
            baseUsage.setEventType(UsageEvent.DELETE_LOADBALANCER.name());
            EventType eventType = UsageEntryFactoryImpl.mapEventType(baseUsage);
            Assert.assertEquals(EventType.DELETE, eventType);
        }

        @Test
        public void shouldMapSuspend() throws NoSuchAlgorithmException, DatatypeConfigurationException {
            baseUsage = new Usage();
            baseUsage.setEventType(UsageEvent.SUSPEND_LOADBALANCER.name());
            EventType eventType = UsageEntryFactoryImpl.mapEventType(baseUsage);
            Assert.assertEquals(EventType.SUSPEND, eventType);
        }

        @Test
        public void shouldMapUnSuspend() throws NoSuchAlgorithmException, DatatypeConfigurationException {
            baseUsage = new Usage();
            baseUsage.setEventType(UsageEvent.UNSUSPEND_LOADBALANCER.name());
            EventType eventType = UsageEntryFactoryImpl.mapEventType(baseUsage);
            Assert.assertEquals(EventType.UNSUSPEND, eventType);
        }

        @Test
        public void shouldReturnNullIfEventTypeNull() throws NoSuchAlgorithmException, DatatypeConfigurationException {
            baseUsage = new Usage();
            EventType eventType = UsageEntryFactoryImpl.mapEventType(baseUsage);
            Assert.assertEquals(null, eventType);
        }
    }

    public static class WhenMappingRegion {
        String baseRegion;

        @Before
        public void standUp() {
            baseRegion = "DFW";
        }

        @Test
        public void shouldMapDFWRegion() throws NoSuchAlgorithmException {
            Region region = UsageEntryFactoryImpl.mapRegion(baseRegion);
            Assert.assertEquals(Region.DFW, region);
        }

        @Test
        public void shouldMapLONRegion() throws NoSuchAlgorithmException {
            Region region = UsageEntryFactoryImpl.mapRegion("LON");
            Assert.assertEquals(Region.LON, region);
        }

        @Test
        public void shouldMapORDRegion() throws NoSuchAlgorithmException {
            Region region = UsageEntryFactoryImpl.mapRegion("ORD");
            Assert.assertEquals(Region.ORD, region);
        }

        @Test
        public void shouldMapGlobalIfRegionInvalid() throws NoSuchAlgorithmException {
            Region region = UsageEntryFactoryImpl.mapRegion("test");
            Assert.assertEquals(Region.GLOBAL, region);
        }

        @Test
        public void shouldSortUsageList() throws NoSuchAlgorithmException {
            List<Usage> usages = new ArrayList<Usage>();
            Usage u1 = new Usage();
            u1.setId(5);
            usages.add(u1);
            Usage u2 = new Usage();
            u2.setId(4);
            usages.add(u2);
            Usage u3 = new Usage();
            u3.setId(3);
            usages.add(u3);
            Usage u4 = new Usage();
            u4.setId(2);
            usages.add(u4);
            Usage u5 = new Usage();
            u5.setId(1);
            usages.add(u5);
            Assert.assertEquals(5, (int)usages.get(0).getId());

             Collections.sort(usages, new Comparator<Usage>() {
                 public int compare(Usage s1, Usage s2) {
                     return s1.getId().compareTo(s2.getId());
                 }
             });
            Assert.assertEquals(1, (int)usages.get(0).getId());
        }

    }
}
