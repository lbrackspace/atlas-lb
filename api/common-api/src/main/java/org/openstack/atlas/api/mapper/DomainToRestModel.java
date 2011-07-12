package org.openstack.atlas.api.mapper;

import org.openstack.atlas.docs.loadbalancers.api.v1.Limit;
import org.openstack.atlas.docs.loadbalancers.api.v1.LimitTypes;
import org.openstack.atlas.service.domain.entities.LimitType;

import java.util.List;
import java.util.Map;

public class DomainToRestModel {

    public static org.openstack.atlas.docs.loadbalancers.api.v1.LimitTypes LimitTypeList2LimitType(List<org.openstack.atlas.service.domain.entities.LimitType> dlts) {
        LimitTypes rlts = new LimitTypes();
        for(LimitType dlt  :dlts){
            org.openstack.atlas.docs.loadbalancers.api.v1.LimitType rlt = new org.openstack.atlas.docs.loadbalancers.api.v1.LimitType();
            rlt.setDefaultValue(dlt.getDefaultValue());
            rlt.setDescription(dlt.getDescription());
            rlt.setName(dlt.getName().name());
            rlts.getLimitTypes().add(rlt);
        }
        return rlts;
    }

    public static org.openstack.atlas.docs.loadbalancers.api.v1.Limits AccountLimitMap2Limits(Map<String,Integer> limitsMap){
        org.openstack.atlas.docs.loadbalancers.api.v1.Limits rLimits = new org.openstack.atlas.docs.loadbalancers.api.v1.Limits();

        for(String name : limitsMap.keySet()){
            Integer value = limitsMap.get(name);
            Limit limit = new org.openstack.atlas.docs.loadbalancers.api.v1.Limit();
            limit.setName(name);
            limit.setValue(value);
            rLimits.getAbsolute().add(limit);
        }
        return rLimits;
    }
}
