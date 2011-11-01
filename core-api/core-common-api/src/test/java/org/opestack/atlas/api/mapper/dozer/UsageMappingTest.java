package org.opestack.atlas.api.mapper.dozer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.stub.StubFactory;

@RunWith(Enclosed.class)
public class UsageMappingTest {

    public static class WhenMappingFromDomainToApi extends MappingBase {
        private org.openstack.atlas.service.domain.entity.UsageRecord domainUsageRecord;
        private org.openstack.atlas.core.api.v1.LoadBalancerUsageRecord apiUsageRecord;

        @Before
        public void setUp() throws Exception {
            domainUsageRecord = StubFactory.createHydratedDomainUsageRecord();
            apiUsageRecord = mapper.map(domainUsageRecord, org.openstack.atlas.core.api.v1.LoadBalancerUsageRecord.class);
        }

        @Test
        public void shouldNotFailWhenDomainUsageRecordIsEmpty() {
            domainUsageRecord = new org.openstack.atlas.service.domain.entity.UsageRecord();
            try {
                apiUsageRecord = mapper.map(domainUsageRecord, org.openstack.atlas.core.api.v1.LoadBalancerUsageRecord.class);
            } catch (Exception e) {
                Assert.fail("Empty domain usage record caused this exception");
            }
        }

        @Test
        public void shouldMapAllAttributes() {
            Assert.assertEquals(domainUsageRecord.getId(), apiUsageRecord.getId());
            Assert.assertEquals(domainUsageRecord.getTransferBytesIn(), apiUsageRecord.getTransferBytesIn());
            Assert.assertEquals(domainUsageRecord.getTransferBytesOut(), apiUsageRecord.getTransferBytesOut());
            Assert.assertEquals(domainUsageRecord.getStartTime(), apiUsageRecord.getStartTime());
            Assert.assertEquals(domainUsageRecord.getEndTime(), apiUsageRecord.getEndTime());
        }

        @Test
        public void shouldNotMapExtensionAttributes() {
            Assert.assertTrue(apiUsageRecord.getOtherAttributes().isEmpty());
        }
    }
}
