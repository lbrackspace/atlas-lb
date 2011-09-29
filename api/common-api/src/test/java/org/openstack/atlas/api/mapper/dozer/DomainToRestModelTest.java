package org.openstack.atlas.api.mapper.dozer;


import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.api.mapper.DomainToRestModel;
import org.openstack.atlas.docs.loadbalancers.api.v1.LimitTypes;
import org.openstack.atlas.docs.loadbalancers.api.v1.Limits;
import org.openstack.atlas.service.domain.entities.AccountLimitType;
import org.openstack.atlas.service.domain.entities.LimitType;

import java.util.ArrayList;
import java.util.List;

public class DomainToRestModelTest {

    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";
    private static final String managementDozerConfigFile = "loadbalancing-dozer-management-mapping.xml";
    private DozerBeanMapper mapper;
    private Limits rLimits;
    private List<LimitType> dLimitList;
    private LimitTypes rLimitTypes;
    String[] lts;

    @Before
    public void setUp() {
        int i;
        lts = new String[]{"ACCESS_LIST_LIMIT|100|Max number of items for an access list",
                    "BATCH_DELETE_LIMIT|10|Max number of items that can be deleted for batch delete operations|",
                    "IPV6_LIMIT|25|Max number of IPv6 addresses for a load balancer",
                    "LOADBALANCER_LIMIT|25|Max number of load balancers for an account",
                    "NODE_LIMIT|25|Max number of nodes for a load balancer"};
        
        dLimitList = new ArrayList<LimitType>();
        rLimits = new Limits();
        rLimitTypes = new LimitTypes();
        for (i = 0; i < lts.length; i++) {
            LimitType dLimitType = new LimitType();
            org.openstack.atlas.docs.loadbalancers.api.v1.LimitType rLimitType;

            rLimitType = new org.openstack.atlas.docs.loadbalancers.api.v1.LimitType();
            String[] cols = lts[i].split("\\|");
            AccountLimitType name = AccountLimitType.valueOf(cols[0]);
            int defaultValue = Integer.parseInt(cols[1]);
            String description = cols[2];
            dLimitType.setName(name);
            dLimitType.setDefaultValue(defaultValue);
            dLimitType.setDescription(description);

            rLimitType.setDefaultValue(defaultValue);
            rLimitType.setDescription(description);
            rLimitType.setName(name.name());
            dLimitList.add(dLimitType);
            rLimitTypes.getLimitTypes().add(rLimitType);
        }
    }

    @Test
    public void shouldMapListLimitType() {
        org.openstack.atlas.docs.loadbalancers.api.v1.LimitTypes rlts = DomainToRestModel.LimitTypeList2LimitType(dLimitList);
        if(rlts==null || rlts.getLimitTypes().size() != dLimitList.size()) {
            Assert.fail("Error invalid size return during mapping");
        }
        for (int i = 0; i < rlts.getLimitTypes().size(); i++) {
            org.openstack.atlas.docs.loadbalancers.api.v1.LimitType rlt = rlts.getLimitTypes().get(i);
            LimitType dlt = dLimitList.get(i);
            Assert.assertEquals(dlt.getName().name(),rlt.getName());
            Assert.assertEquals(dlt.getDefaultValue(), (Integer)rlt.getDefaultValue());
            Assert.assertEquals(dlt.getDescription(), rlt.getDescription());
        }

    }

    private LimitType newdLimitType(int defaultValue, String description, AccountLimitType accountLimitType) {
        LimitType lt = new LimitType();
        lt.setDefaultValue(defaultValue);
        lt.setDescription(description);
        lt.setName(accountLimitType);
        return lt;
    }
}