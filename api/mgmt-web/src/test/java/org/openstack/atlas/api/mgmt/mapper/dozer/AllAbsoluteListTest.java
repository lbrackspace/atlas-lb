package org.openstack.atlas.api.mgmt.mapper.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entities.AccountLimit;
import org.openstack.atlas.service.domain.entities.AccountLimitType;
import org.openstack.atlas.service.domain.entities.LimitType;
import org.openstack.atlas.service.domain.pojos.AllAbsoluteLimits;

import java.util.ArrayList;
import java.util.List;

@RunWith(Enclosed.class)
public class AllAbsoluteListTest {

    private static final String managementDozerConfigFile = "loadbalancing-dozer-management-mapping.xml";

    public static class When_mapping_a_load_balancer_from_datamodel_to_domain {

        private AllAbsoluteLimits limits; 
        private DozerBeanMapper mapper;
        private org.openstack.atlas.service.domain.entities.LoadBalancer domainLoadBalancer;

        @Before
        public void setUp() {
           mapper = MapperBuilder.getConfiguredMapper(managementDozerConfigFile);
            List<AccountLimit> customLimits = new ArrayList<AccountLimit>();

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

            customLimits.add(customLimit);

            List<LimitType> defaultLimits = new ArrayList<LimitType>();

            LimitType lType = new LimitType();
            lType.setDefaultValue(15);
            lType.setDescription("I am another test description");
            lType.setName(AccountLimitType.ACCESS_LIST_LIMIT);

            defaultLimits.add(lType);

            limits = new AllAbsoluteLimits();
            limits.setCustomLimits(customLimits);
            limits.setDefaultLimits(defaultLimits);
        }

        @Test
        public void shouldMapAllAbsoluteLimitsClasses() {

            try {
                org.openstack.atlas.docs.loadbalancers.api.management.v1.AllAbsoluteLimits mapLimits = mapper.map(limits,
                    org.openstack.atlas.docs.loadbalancers.api.management.v1.AllAbsoluteLimits.class);
            } catch (Exception e) {
                Assert.fail("List didn't map properly to the other list according to the mapping file.");
            }
        }
    }
}