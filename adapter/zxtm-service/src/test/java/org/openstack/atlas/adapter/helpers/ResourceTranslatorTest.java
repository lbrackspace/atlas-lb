package org.openstack.atlas.adapter.helpers;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.rackspace.stingray.client.persistence.Persistence;
import org.rackspace.stingray.client.persistence.PersistenceBasic;
import org.rackspace.stingray.client.persistence.PersistenceProperties;

import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
public class ResourceTranslatorTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenTranslatingAPersistence {
        private Persistence persistence;
        private PersistenceProperties properties;
        private PersistenceBasic basic;
        private LoadBalancer mockedLoadBalancer;

        @Before
        public void standUp() {
            persistence = new Persistence();
            properties = new PersistenceProperties();
            basic = new PersistenceBasic();
            mockedLoadBalancer = mock(LoadBalancer.class);
        }

        @Test
        public void testStub() {
        }
    }
}