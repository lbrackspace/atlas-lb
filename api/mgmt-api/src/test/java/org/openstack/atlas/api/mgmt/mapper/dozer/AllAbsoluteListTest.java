package org.openstack.atlas.api.mgmt.mapper.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AllAbsoluteLimits;
import org.openstack.atlas.service.domain.entities.AccountLimit;
import org.openstack.atlas.service.domain.entities.AccountLimitType;
import org.openstack.atlas.service.domain.entities.LimitType;

import java.util.ArrayList;
import java.util.List;

@RunWith(Enclosed.class)
public class AllAbsoluteListTest {

    private static final String managementDozerConfigFile = "loadbalancing-dozer-management-mapping.xml";

    public static class When_mapping_a_load_balancer_from_datamodel_to_domain {

        private org.openstack.atlas.service.domain.pojos.AllAbsoluteLimits limits;
        private DozerBeanMapper mapper;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(managementDozerConfigFile);

            AccountLimit customLimit = new AccountLimit();
            customLimit.setAccountId(1234);
            customLimit.setId(1);
            customLimit.setLimit(30);
            customLimit.setUserName("testUser");

            LimitType type = new LimitType();
            type.setDefaultValue(25);
            type.setDescription("I am a test description");
            type.setName(AccountLimitType.LOADBALANCER_LIMIT);
            customLimit.setLimitType(type);


            List<AccountLimit> customLimits = new ArrayList<AccountLimit>();
            customLimits.add(customLimit);

            LimitType lType = new LimitType();
            lType.setDefaultValue(15);
            lType.setDescription("I am another test description");
            lType.setName(AccountLimitType.ACCESS_LIST_LIMIT);

            List<LimitType> defaultLimits = new ArrayList<LimitType>();
            defaultLimits.add(lType);

            limits = new org.openstack.atlas.service.domain.pojos.AllAbsoluteLimits();
            limits.setCustomLimits(customLimits);
            limits.setDefaultLimits(defaultLimits);
        }

        @Test
        public void shouldMapAllAbsoluteLimitsClasses() {
            try {
                AllAbsoluteLimits mapLimits = mapper.map(limits, AllAbsoluteLimits.class);
                Assert.assertTrue("AllAbsoluteLimits classes mapped successfully.", true);
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void shouldMap5CustomAbsoluteLimits() {
            try {
                limits.setDefaultLimits(new ArrayList<LimitType>());
                add4CustomLimits(limits);

                AllAbsoluteLimits mapLimits = mapper.map(limits, AllAbsoluteLimits.class);
                Assert.assertTrue("AllAbsoluteLimits with all custom limits mapped successfully.", true);
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void shouldMap5DefaultAbsoluteLimits() {
            try {
                limits.setCustomLimits(new ArrayList<AccountLimit>());
                add4DefaultLimits(limits);

                AllAbsoluteLimits mapLimits = mapper.map(limits, AllAbsoluteLimits.class);
                Assert.assertTrue("AllAbsoluteLimits with all default limits mapped successfully.", true);
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        private void add4CustomLimits (org.openstack.atlas.service.domain.pojos.AllAbsoluteLimits limits) {
            List<AccountLimit> customLimits = new ArrayList<AccountLimit>();

            AccountLimit customLimit2 = new AccountLimit();
            customLimit2.setAccountId(1234);
            customLimit2.setId(2);
            customLimit2.setLimit(30);
            customLimit2.setUserName("testUser");

            LimitType type2 = new LimitType();
            type2.setDefaultValue(25);
            type2.setDescription("I am a test description");
            type2.setName(AccountLimitType.IPV6_LIMIT);
            customLimit2.setLimitType(type2);
            customLimits.add(customLimit2);

            AccountLimit customLimit3 = new AccountLimit();
            customLimit3.setAccountId(1234);
            customLimit3.setId(3);
            customLimit3.setLimit(30);
            customLimit3.setUserName("testUser");

            LimitType type3 = new LimitType();
            type3.setDefaultValue(25);
            type3.setDescription("I am a test description");
            type3.setName(AccountLimitType.NODE_LIMIT);
            customLimit3.setLimitType(type3);
            customLimits.add(customLimit3);

            AccountLimit customLimit4 = new AccountLimit();
            customLimit4.setAccountId(1234);
            customLimit4.setId(4);
            customLimit4.setLimit(30);
            customLimit4.setUserName("testUser");

            LimitType type4 = new LimitType();
            type4.setDefaultValue(25);
            type4.setDescription("I am a test description");
            type4.setName(AccountLimitType.ACCESS_LIST_LIMIT);
            customLimit4.setLimitType(type4);
            customLimits.add(customLimit4);

            AccountLimit customLimit5 = new AccountLimit();
            customLimit5.setAccountId(1234);
            customLimit5.setId(5);
            customLimit5.setLimit(30);
            customLimit5.setUserName("testUser");

            LimitType type5 = new LimitType();
            type5.setDefaultValue(25);
            type5.setDescription("I am a test description");
            type5.setName(AccountLimitType.BATCH_DELETE_LIMIT);
            customLimit5.setLimitType(type5);
            customLimits.add(customLimit5);

            limits.getCustomLimits().addAll(customLimits);
        }


        private void add4DefaultLimits (org.openstack.atlas.service.domain.pojos.AllAbsoluteLimits limits) {
            
            List<LimitType> defaultLimits = new ArrayList<LimitType>();

            LimitType lType2 = new LimitType();
            lType2.setDefaultValue(15);
            lType2.setDescription("I am another test description");
            lType2.setName(AccountLimitType.IPV6_LIMIT);
            defaultLimits.add(lType2);

            LimitType lType3 = new LimitType();
            lType3.setDefaultValue(15);
            lType3.setDescription("I am another test description");
            lType3.setName(AccountLimitType.NODE_LIMIT);
            defaultLimits.add(lType3);

            LimitType lType4 = new LimitType();
            lType4.setDefaultValue(15);
            lType4.setDescription("I am another test description");
            lType4.setName(AccountLimitType.BATCH_DELETE_LIMIT);
            defaultLimits.add(lType4);

            LimitType lType5 = new LimitType();
            lType5.setDefaultValue(15);
            lType5.setDescription("I am another test description");
            lType5.setName(AccountLimitType.LOADBALANCER_LIMIT);
            defaultLimits.add(lType5);
            
            limits.getDefaultLimits().addAll(defaultLimits);
        }
    }
}