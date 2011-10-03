package org.opestack.atlas.api.mapper.dozer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.core.api.v1.HealthMonitor;
import org.openstack.atlas.service.domain.stub.StubFactory;

@RunWith(Enclosed.class)
public class HealthMonitorMappingTest {

    public static class WhenMappingFromApiToDomain extends MappingBase {
        private org.openstack.atlas.core.api.v1.HealthMonitor apiHealthMonitor;
        private org.openstack.atlas.service.domain.entity.HealthMonitor domainHealthMonitor;

        @Before
        public void setUp() throws Exception {
            apiHealthMonitor = StubFactory.createHydratedDataModelHealthMonitor();
            domainHealthMonitor = mapper.map(apiHealthMonitor, org.openstack.atlas.service.domain.entity.HealthMonitor.class);
        }

        @Test
        public void shouldNotFailWhenApiNodeIsEmpty() {
            apiHealthMonitor = new org.openstack.atlas.core.api.v1.HealthMonitor();
            try {
                domainHealthMonitor = mapper.map(apiHealthMonitor, org.openstack.atlas.service.domain.entity.HealthMonitor.class);
            } catch (Exception e) {
                Assert.fail("Empty API node caused this exception");
            }
        }

        @Test
        public void shouldMapAllAttributes() {
            Assert.assertEquals(apiHealthMonitor.getType(), domainHealthMonitor.getType().name());
            Assert.assertEquals(apiHealthMonitor.getDelay(), domainHealthMonitor.getDelay());
            Assert.assertEquals(apiHealthMonitor.getTimeout(), domainHealthMonitor.getTimeout());
            Assert.assertEquals(apiHealthMonitor.getAttemptsBeforeDeactivation(), domainHealthMonitor.getAttemptsBeforeDeactivation());
            Assert.assertEquals(apiHealthMonitor.getPath(), domainHealthMonitor.getPath());
        }

        @Test
        public void shouldMapAttributesToNullWhenNoAttributesSet() {
            apiHealthMonitor = new HealthMonitor();
            domainHealthMonitor = mapper.map(apiHealthMonitor, org.openstack.atlas.service.domain.entity.HealthMonitor.class);

            Assert.assertNull(domainHealthMonitor.getId());
            Assert.assertNull(domainHealthMonitor.getType());
            Assert.assertNull(domainHealthMonitor.getDelay());
            Assert.assertNull(domainHealthMonitor.getTimeout());
            Assert.assertNull(domainHealthMonitor.getAttemptsBeforeDeactivation());
            Assert.assertNull(domainHealthMonitor.getPath());
        }
    }

    public static class WhenMappingFromDomainToApi extends MappingBase {
        private org.openstack.atlas.service.domain.entity.HealthMonitor domainHealthMonitor;
        private org.openstack.atlas.core.api.v1.HealthMonitor apiHealthMonitor;

        @Before
        public void setUp() throws Exception {
            domainHealthMonitor = StubFactory.createHydratedDomainHealthMonitor();
            apiHealthMonitor = mapper.map(domainHealthMonitor, org.openstack.atlas.core.api.v1.HealthMonitor.class);
        }

        @Test
        public void shouldNotFailWhenApiNodeIsNull() {
            domainHealthMonitor = new org.openstack.atlas.service.domain.entity.HealthMonitor();
            try {
                apiHealthMonitor = mapper.map(domainHealthMonitor, org.openstack.atlas.core.api.v1.HealthMonitor.class);
            } catch (Exception e) {
                Assert.fail("Empty API node caused this exception");
            }
        }

        @Test
        public void shouldMapAllAttributes() {
            Assert.assertEquals(domainHealthMonitor.getType().name(), apiHealthMonitor.getType());
            Assert.assertEquals(domainHealthMonitor.getDelay(), apiHealthMonitor.getDelay());
            Assert.assertEquals(domainHealthMonitor.getTimeout(), apiHealthMonitor.getTimeout());
            Assert.assertEquals(domainHealthMonitor.getAttemptsBeforeDeactivation(), apiHealthMonitor.getAttemptsBeforeDeactivation());
            Assert.assertEquals(domainHealthMonitor.getPath(), apiHealthMonitor.getPath());
        }

        @Test
        public void shouldMapAttributesToNullWhenNoAttributesSet() {
            domainHealthMonitor = new org.openstack.atlas.service.domain.entity.HealthMonitor();
            apiHealthMonitor = mapper.map(domainHealthMonitor, org.openstack.atlas.core.api.v1.HealthMonitor.class);

            Assert.assertNull(apiHealthMonitor.getType());
            Assert.assertNull(apiHealthMonitor.getDelay());
            Assert.assertNull(apiHealthMonitor.getTimeout());
            Assert.assertNull(apiHealthMonitor.getAttemptsBeforeDeactivation());
            Assert.assertNull(apiHealthMonitor.getPath());
        }
    }
}
