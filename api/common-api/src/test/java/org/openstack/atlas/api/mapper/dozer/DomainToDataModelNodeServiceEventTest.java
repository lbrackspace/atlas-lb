package org.openstack.atlas.api.mapper.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.events.entities.NodeServiceEvent;

import java.util.Calendar;

@RunWith(Enclosed.class)
public class DomainToDataModelNodeServiceEventTest {

    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";

    public static class When_mapping_node_service_event_from_domain_to_datamodel {

        private DozerBeanMapper mapper;
        private NodeServiceEvent nodeServiceEvent;
        private org.openstack.atlas.docs.loadbalancers.api.v1.NodeServiceEvent dataNodeServiceEvent;

        @Before
        public void standUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
        }

        @Test
        public void shouldMapNodeServiceEvent() {
            nodeServiceEvent = new NodeServiceEvent();
            nodeServiceEvent.setDetailedMessage("amessage");
            nodeServiceEvent.setNodeId(23);
            nodeServiceEvent.setAccountId(1234);
            nodeServiceEvent.setAuthor("foo");
            nodeServiceEvent.setCategory(CategoryType.UPDATE);
            nodeServiceEvent.setCreated(Calendar.getInstance());
            nodeServiceEvent.setDescription("desc");
            nodeServiceEvent.setId(1);
            nodeServiceEvent.setLoadbalancerId(2);
            nodeServiceEvent.setRelativeUri("/uri/that/is/relative");
            nodeServiceEvent.setSeverity(EventSeverity.CRITICAL);
            nodeServiceEvent.setTitle("NODESTATUS");
            nodeServiceEvent.setType(EventType.UPDATE_NODE);

            try {
                dataNodeServiceEvent = mapper.map(nodeServiceEvent, org.openstack.atlas.docs.loadbalancers.api.v1.NodeServiceEvent.class);
            } catch (Exception e) {
                Assert.fail(e.getLocalizedMessage());
            }

            Assert.assertEquals("amessage", dataNodeServiceEvent.getDetailedMessage());
            Assert.assertEquals((Integer)23, dataNodeServiceEvent.getNodeId());
            Assert.assertEquals((Integer)1234, dataNodeServiceEvent.getAccountId());
            Assert.assertEquals("foo", dataNodeServiceEvent.getAuthor());
        }
    }

    public static class When_mapping_node_service_events_from_domain_to_datamodel {

        private DozerBeanMapper mapper;
        private NodeServiceEvent nodeServiceEvent;
        private org.openstack.atlas.service.domain.events.pojos.NodeServiceEvents nodeServiceEvents;
        private org.openstack.atlas.docs.loadbalancers.api.v1.NodeServiceEvent dataNodeServiceEvent;
        private org.openstack.atlas.docs.loadbalancers.api.v1.NodeServiceEvents dataNodeServiceEvents;

        @Before
        public void standUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
        }

        @Test
        public void shouldMapNodeServiceEvents() {
            nodeServiceEvent = new NodeServiceEvent();
            nodeServiceEvents = new org.openstack.atlas.service.domain.events.pojos.NodeServiceEvents();
            nodeServiceEvent.setDetailedMessage("amessage");
            nodeServiceEvent.setNodeId(23);
            nodeServiceEvent.setAccountId(1234);
            nodeServiceEvent.setAuthor("foo");
            nodeServiceEvent.setCategory(CategoryType.UPDATE);
            nodeServiceEvent.setCreated(Calendar.getInstance());
            nodeServiceEvent.setDescription("desc");
            nodeServiceEvent.setId(1);
            nodeServiceEvent.setLoadbalancerId(2);
            nodeServiceEvent.setRelativeUri("/uri/that/is/relative");
            nodeServiceEvent.setSeverity(EventSeverity.CRITICAL);
            nodeServiceEvent.setTitle("NODESTATUS");
            nodeServiceEvent.setType(EventType.UPDATE_NODE);
            nodeServiceEvents.getNodeServiceEvents().add(nodeServiceEvent);

            try {
                dataNodeServiceEvents = mapper.map(nodeServiceEvents, org.openstack.atlas.docs.loadbalancers.api.v1.NodeServiceEvents.class);
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }
    }
}
