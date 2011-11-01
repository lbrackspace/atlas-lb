package org.opestack.atlas.api.mapper.dozer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.core.api.v1.HealthMonitor;
import org.openstack.atlas.datamodel.CoreHealthMonitorType;
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
        public void shouldNotFailWhenApiMonitorIsEmpty() {
            apiHealthMonitor = new org.openstack.atlas.core.api.v1.HealthMonitor();
            try {
                domainHealthMonitor = mapper.map(apiHealthMonitor, org.openstack.atlas.service.domain.entity.HealthMonitor.class);
            } catch (Exception e) {
                Assert.fail("Empty API health monitor caused this exception");
            }
        }

        @Test
        public void shouldMapAllAttributes() {
            Assert.assertEquals(apiHealthMonitor.getType(), domainHealthMonitor.getType());
            Assert.assertEquals(apiHealthMonitor.getDelay(), domainHealthMonitor.getDelay());
            Assert.assertEquals(apiHealthMonitor.getTimeout(), domainHealthMonitor.getTimeout());
            Assert.assertEquals(apiHealthMonitor.getAttemptsBeforeDeactivation(), domainHealthMonitor.getAttemptsBeforeDeactivation());
            Assert.assertEquals(apiHealthMonitor.getPath(), domainHealthMonitor.getPath());
        }

        @Test
        public void shouldMapAttributesToNullOrDefaultWhenNoAttributesSet() {
            apiHealthMonitor = new HealthMonitor();
            domainHealthMonitor = mapper.map(apiHealthMonitor, org.openstack.atlas.service.domain.entity.HealthMonitor.class);

            Assert.assertEquals(CoreHealthMonitorType.CONNECT, domainHealthMonitor.getType());;
            Assert.assertEquals(3600, domainHealthMonitor.getDelay().intValue());
            Assert.assertEquals(300, domainHealthMonitor.getTimeout().intValue());
            Assert.assertEquals(10, domainHealthMonitor.getAttemptsBeforeDeactivation().intValue());
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
        public void shouldNotFailWhenDomainMonitorIsEmpty() {
            domainHealthMonitor = new org.openstack.atlas.service.domain.entity.HealthMonitor();
            try {
                apiHealthMonitor = mapper.map(domainHealthMonitor, org.openstack.atlas.core.api.v1.HealthMonitor.class);
            } catch (Exception e) {
                Assert.fail("Empty domain health monitor caused this exception");
            }
        }

        @Test
        public void shouldMapAllAttributes() {
            Assert.assertEquals(domainHealthMonitor.getType(), apiHealthMonitor.getType());
            Assert.assertEquals(domainHealthMonitor.getDelay(), apiHealthMonitor.getDelay());
            Assert.assertEquals(domainHealthMonitor.getTimeout(), apiHealthMonitor.getTimeout());
            Assert.assertEquals(domainHealthMonitor.getAttemptsBeforeDeactivation(), apiHealthMonitor.getAttemptsBeforeDeactivation());
            Assert.assertEquals(domainHealthMonitor.getPath(), apiHealthMonitor.getPath());
        }

        @Test
        public void shouldMapAttributesToNullOrDefaultWhenNoAttributesSet() {
            domainHealthMonitor = new org.openstack.atlas.service.domain.entity.HealthMonitor();
            apiHealthMonitor = mapper.map(domainHealthMonitor, org.openstack.atlas.core.api.v1.HealthMonitor.class);

            Assert.assertEquals(CoreHealthMonitorType.CONNECT, apiHealthMonitor.getType());
            Assert.assertEquals(3600, apiHealthMonitor.getDelay().intValue());
            Assert.assertEquals(300, apiHealthMonitor.getTimeout().intValue());
            Assert.assertEquals(10, apiHealthMonitor.getAttemptsBeforeDeactivation().intValue());
            Assert.assertNull(apiHealthMonitor.getPath());
        }

        @Test
        public void shouldNotMapExtensionAttributes() {
            Assert.assertTrue(apiHealthMonitor.getOtherAttributes().isEmpty());
        }
    }
}
