package org.openstack.atlas.api.mgmt.mapper.dozer;

import java.util.Calendar;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistItem;
import org.openstack.atlas.service.domain.entities.BlacklistType;
import org.openstack.atlas.service.domain.entities.IpVersion;
import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.events.entities.LoadBalancerServiceEvent;
import org.openstack.atlas.util.debug.Debug;

public class LoadBalancerServiceEventsTest {

    private static final String managementDozerConfigFile = "loadbalancing-dozer-management-mapping.xml";
    private DozerBeanMapper dozerMapper;
    org.openstack.atlas.service.domain.events.pojos.LoadBalancerServiceEvents dEvents;
    org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerServiceEvents rEvents;
    LoadBalancerServiceEvent lbSe;

    @Before
    public void setUp() {
        dEvents = new org.openstack.atlas.service.domain.events.pojos.LoadBalancerServiceEvents();
        rEvents = new org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerServiceEvents();
        dozerMapper = dozerMapper = MapperBuilder.getConfiguredMapper(managementDozerConfigFile);
    }

    @Test
    public void testMapLoadBalancerServiceEvents() {
        int last_i = 3;
        int i;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerServiceEvent rEvent;
        for (i = 0; i < last_i; i++) {
            lbSe = new LoadBalancerServiceEvent();
            lbSe.setAccountId(1000);
            lbSe.setAuthor("Some Author");
            lbSe.setCategory(CategoryType.UPDATE);
            lbSe.setCreated(Calendar.getInstance());
            lbSe.setDescription(String.format("lb %d\n", i));
            lbSe.setId(i);
            lbSe.setLoadbalancerId(i+100);
            lbSe.setRelativeUri(String.format("SomeUri %d",i));
            lbSe.setSeverity(EventSeverity.INFO);
            lbSe.setTitle("No Title");
            lbSe.setType(EventType.BUILD_LOADBALANCER);
            try {
                throw new Exception(String.format("Exception for lb %d", i));
            } catch (Exception ex) {
                if (i % 2 == 1) {
                    lbSe.setDetailedMessage(Debug.getEST(ex));
                }
            }
            dEvents.getLoadBalancerServiceEvents().add(lbSe);
        }
        rEvents = dozerMapper.map(dEvents, rEvents.getClass());
        for (i = 0; i < last_i; i++) {
            rEvent = rEvents.getLoadBalancerServiceEvents().get(i);
            Assert.assertEquals(rEvent.getId().intValue(), i);
            Assert.assertEquals(rEvent.getLoadbalancerId().intValue(), i+100);
            if (i % 2 == 1) {
                Assert.assertTrue(rEvent.getDetailedMessage() != null);
            } else {
                Assert.assertTrue(rEvent.getDetailedMessage() == null);
            }
            Assert.assertEquals(rEvent.getTitle(),"No Title");
            Assert.assertEquals(rEvent.getRelativeUri(),String.format("SomeUri %d",i));
            Assert.assertEquals(rEvent.getSeverity(),"INFO");
        }


    }
}
