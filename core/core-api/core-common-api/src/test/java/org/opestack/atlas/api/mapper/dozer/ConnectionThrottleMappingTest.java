package org.opestack.atlas.api.mapper.dozer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.stub.StubFactory;

@RunWith(Enclosed.class)
public class ConnectionThrottleMappingTest {

    public static class WhenMappingFromApiToDomain extends MappingBase {
        private org.openstack.atlas.core.api.v1.ConnectionThrottle apiConnectionThrottle;
        private org.openstack.atlas.service.domain.entity.ConnectionThrottle domainConnectionThrottle;

        @Before
        public void setUp() throws Exception {
            apiConnectionThrottle = StubFactory.createHydratedDataModelConnectionThrottle();
            domainConnectionThrottle = mapper.map(apiConnectionThrottle, org.openstack.atlas.service.domain.entity.ConnectionThrottle.class);
        }

        @Test
        public void shouldNotFailWhenApiThrottleIsEmpty() {
            apiConnectionThrottle = new org.openstack.atlas.core.api.v1.ConnectionThrottle();
            try {
                domainConnectionThrottle = mapper.map(apiConnectionThrottle, org.openstack.atlas.service.domain.entity.ConnectionThrottle.class);
            } catch (Exception e) {
                Assert.fail("Empty API connection throttle caused this exception");
            }
        }

        @Test
        public void shouldMapAllAttributes() {
            Assert.assertEquals(apiConnectionThrottle.getMaxRequestRate(), domainConnectionThrottle.getMaxRequestRate());
            Assert.assertEquals(apiConnectionThrottle.getRateInterval(), domainConnectionThrottle.getRateInterval());
        }

        @Test
        public void shouldMapAttributesToNullOrDefaultWhenNoAttributesSet() {
            apiConnectionThrottle = new org.openstack.atlas.core.api.v1.ConnectionThrottle();
            domainConnectionThrottle = mapper.map(apiConnectionThrottle, org.openstack.atlas.service.domain.entity.ConnectionThrottle.class);

            Assert.assertNull(domainConnectionThrottle.getId());
            Assert.assertEquals(100000, domainConnectionThrottle.getMaxRequestRate().intValue());
            Assert.assertEquals(3600, domainConnectionThrottle.getRateInterval().intValue());
        }
    }

    public static class WhenMappingFromDomainToApi extends MappingBase {
        private org.openstack.atlas.service.domain.entity.ConnectionThrottle domainConnectionThrottle;
        private org.openstack.atlas.core.api.v1.ConnectionThrottle apiConnectionThrottle;

        @Before
        public void setUp() throws Exception {
            domainConnectionThrottle = StubFactory.createHydratedDomainConnectionThrottle();
            apiConnectionThrottle = mapper.map(domainConnectionThrottle, org.openstack.atlas.core.api.v1.ConnectionThrottle.class);
        }

        @Test
        public void shouldNotFailWhenDomainThrottleIsEmpty() {
            domainConnectionThrottle = new org.openstack.atlas.service.domain.entity.ConnectionThrottle();
            try {
                apiConnectionThrottle = mapper.map(domainConnectionThrottle, org.openstack.atlas.core.api.v1.ConnectionThrottle.class);
            } catch (Exception e) {
                Assert.fail("Empty domain connection throttle caused this exception");
            }
        }

        @Test
        public void shouldMapAllAttributes() {
            Assert.assertEquals(domainConnectionThrottle.getMaxRequestRate(), apiConnectionThrottle.getMaxRequestRate());
            Assert.assertEquals(domainConnectionThrottle.getRateInterval(), apiConnectionThrottle.getRateInterval());
        }

        @Test
        public void shouldMapAttributesToNullOrDefaultWhenNoAttributesSet() {
            domainConnectionThrottle = new org.openstack.atlas.service.domain.entity.ConnectionThrottle();
            apiConnectionThrottle = mapper.map(domainConnectionThrottle, org.openstack.atlas.core.api.v1.ConnectionThrottle.class);

            Assert.assertEquals(100000, apiConnectionThrottle.getMaxRequestRate().intValue());
            Assert.assertEquals(3600, apiConnectionThrottle.getRateInterval().intValue());
        }

        @Test
        public void shouldNotMapExtensionAttributes() {
            Assert.assertTrue(apiConnectionThrottle.getOtherAttributes().isEmpty());
        }
    }
}
