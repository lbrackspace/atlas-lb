package org.openstack.atlas.atom.util;

import com.rackspace.docs.core.event.EventType;
import com.rackspace.docs.core.event.Region;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entities.Usage;

import javax.xml.datatype.DatatypeConfigurationException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

@RunWith(Enclosed.class)
public class AHUSLUtilTest {
    public static class WhenConvertingCalToXMLGregorianCal {

        @Before
        public void standUp() {

        }

        @Test
        public void shouldConvertCal() throws DatatypeConfigurationException {
            Calendar cal = Calendar.getInstance();
            AHUSLUtil.processCalendar(cal);
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
            baseUsage.setEventType("CREATE_LOADBALANCER");
        }

        @Test
        public void shouldMapCreateEvent() throws NoSuchAlgorithmException, DatatypeConfigurationException {
            EventType eventType = AHUSLUtil.mapEventType(baseUsage);
            Assert.assertEquals(EventType.CREATE, eventType);
        }

        @Test
        public void shouldMapDeleteEvent() throws NoSuchAlgorithmException, DatatypeConfigurationException {
            baseUsage = new Usage();
            baseUsage.setEventType("DELETE_LOADBALANCER");
            EventType eventType = AHUSLUtil.mapEventType(baseUsage);
            Assert.assertEquals(EventType.DELETE, eventType);
        }

        @Test
        public void shouldMapUpUpdate() throws NoSuchAlgorithmException, DatatypeConfigurationException {
            baseUsage = new Usage();
            baseUsage.setEventType("UPDATE_LOADBALANCER");
            EventType eventType = AHUSLUtil.mapEventType(baseUsage);
            Assert.assertEquals(EventType.UPDATE, eventType);
        }

        @Test
        public void shouldMapSuspend() throws NoSuchAlgorithmException, DatatypeConfigurationException {
            baseUsage = new Usage();
            baseUsage.setEventType("SUSPEND_LOADBALANCER");
            EventType eventType = AHUSLUtil.mapEventType(baseUsage);
            Assert.assertEquals(EventType.SUSPEND, eventType);
        }

        @Test
        public void shouldMapUnSuspend() throws NoSuchAlgorithmException, DatatypeConfigurationException {
            baseUsage = new Usage();
            baseUsage.setEventType("UNSUSPEND_LOADBANCER");
            EventType eventType = AHUSLUtil.mapEventType(baseUsage);
            Assert.assertEquals(EventType.UNSUSPEND, eventType);
        }

        @Test
        public void shouldReturnNullIfEventTypeNull() throws NoSuchAlgorithmException, DatatypeConfigurationException {
            baseUsage = new Usage();
            EventType eventType = AHUSLUtil.mapEventType(baseUsage);
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
            Region region = AHUSLUtil.mapRegion(baseRegion);
            Assert.assertEquals(Region.DFW, region);
        }

        @Test
        public void shouldMapLONRegion() throws NoSuchAlgorithmException {
            Region region = AHUSLUtil.mapRegion("LON");
            Assert.assertEquals(Region.LON, region);
        }

        @Test
        public void shouldMapORDRegion() throws NoSuchAlgorithmException {
            Region region = AHUSLUtil.mapRegion("ORD");
            Assert.assertEquals(Region.ORD, region);
        }

        @Test
        public void shouldMapGlobalIfRegionInvalid() throws NoSuchAlgorithmException {
            Region region = AHUSLUtil.mapRegion("test");
            Assert.assertEquals(Region.GLOBAL, region);
        }
    }
}
