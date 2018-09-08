package org.openstack.atlas.api.mgmt.mapper.dozer;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistItem;
import org.openstack.atlas.service.domain.entities.BlacklistType;
import org.openstack.atlas.service.domain.entities.IpVersion;
import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class BlacklistItemTest {
    private static final String managementDozerConfigFile = "loadbalancing-dozer-management-mapping.xml";

    public static class WhenMappingABlacklistItemFromDomainToDataModel {

        private DozerBeanMapper mapper;
        private BlacklistItem dataModelBlacklistItem;
        private org.openstack.atlas.service.domain.entities.BlacklistItem domainBlacklistItem;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(managementDozerConfigFile);

            domainBlacklistItem = new org.openstack.atlas.service.domain.entities.BlacklistItem();

            domainBlacklistItem.setId(1);
            domainBlacklistItem.setCidrBlock("10.1.1.1/24");
            domainBlacklistItem.setIpVersion(IpVersion.IPV4);
            domainBlacklistItem.setBlacklistType(BlacklistType.NODE);
        }

        @Test
        public void shouldNotFailWhenDomainBlackListIsEmpty() {
            domainBlacklistItem = new org.openstack.atlas.service.domain.entities.BlacklistItem();

            try {
                dataModelBlacklistItem = mapper.map(domainBlacklistItem, BlacklistItem.class);
            } catch (Exception e) {
                Assert.fail("Empty domain black list item caused this exception");
            }
        }

        @Test
        public void shouldMapNullFieldsWhenBlackListIsEmpty() {
            domainBlacklistItem = new org.openstack.atlas.service.domain.entities.BlacklistItem();
            dataModelBlacklistItem = mapper.map(domainBlacklistItem, BlacklistItem.class);

            Assert.assertNull(dataModelBlacklistItem.getId());
            Assert.assertNull(dataModelBlacklistItem.getCidrBlock());
            Assert.assertNull(dataModelBlacklistItem.getIpVersion());
            Assert.assertNull(dataModelBlacklistItem.getType());
        }

        @Test
        public void shouldMapAllFields() {
            dataModelBlacklistItem = mapper.map(domainBlacklistItem, BlacklistItem.class);

            Assert.assertEquals(new Integer(1), dataModelBlacklistItem.getId());
            Assert.assertEquals("10.1.1.1/24", dataModelBlacklistItem.getCidrBlock());
            Assert.assertEquals(org.openstack.atlas.docs.loadbalancers.api.management.v1.IpVersion.IPV4, dataModelBlacklistItem.getIpVersion());
            Assert.assertEquals(org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistType.NODE, dataModelBlacklistItem.getType());
        }
    }

    public static class WhenMappingABlacklistItemFromDataModelToDomain {

        private DozerBeanMapper mapper;
        private BlacklistItem dataModelBlacklistItem;
        private org.openstack.atlas.service.domain.entities.BlacklistItem domainBlacklistItem;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(managementDozerConfigFile);

            dataModelBlacklistItem = new BlacklistItem();

            dataModelBlacklistItem.setId(1);
            dataModelBlacklistItem.setCidrBlock("10.1.1.1/24");
            dataModelBlacklistItem.setIpVersion(org.openstack.atlas.docs.loadbalancers.api.management.v1.IpVersion.IPV4);
            dataModelBlacklistItem.setType(org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistType.NODE);
        }

        @Test
        public void shouldNotFailWhenDomainBlackListIsEmpty() {
            dataModelBlacklistItem = new BlacklistItem();

            try {
                domainBlacklistItem = mapper.map(dataModelBlacklistItem, org.openstack.atlas.service.domain.entities.BlacklistItem.class);
            } catch (Exception e) {
                Assert.fail("Empty domain black list item caused this exception");
            }
        }

        @Test
        public void shouldMapNullFieldsWhenBlackListIsEmpty() {
            dataModelBlacklistItem = new BlacklistItem();
            domainBlacklistItem = mapper.map(dataModelBlacklistItem, org.openstack.atlas.service.domain.entities.BlacklistItem.class);

            Assert.assertNull(domainBlacklistItem.getId());
            Assert.assertNull(domainBlacklistItem.getCidrBlock());
            Assert.assertNull(domainBlacklistItem.getIpVersion());
            Assert.assertNull(domainBlacklistItem.getBlacklistType());
        }

        @Test
        public void shouldMapAllFields() {
            domainBlacklistItem = mapper.map(dataModelBlacklistItem, org.openstack.atlas.service.domain.entities.BlacklistItem.class);

            Assert.assertEquals(new Integer(1), domainBlacklistItem.getId());
            Assert.assertEquals("10.1.1.1/24", domainBlacklistItem.getCidrBlock());
            Assert.assertEquals(IpVersion.IPV4, domainBlacklistItem.getIpVersion());
            Assert.assertEquals(BlacklistType.NODE, domainBlacklistItem.getBlacklistType());
        }
    }
}
