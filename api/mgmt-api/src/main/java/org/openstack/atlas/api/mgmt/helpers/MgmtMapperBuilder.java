package org.openstack.atlas.api.mgmt.helpers;

import org.dozer.Mapper;
import org.dozer.DozerBeanMapperBuilder;

public class MgmtMapperBuilder {
    public static Mapper getConfiguredMapper() {
        String mappingFile = "loadbalancing-dozer-management-mapping.xml";
        Mapper mapper =  DozerBeanMapperBuilder.create()
                .withMappingFiles(mappingFile)
                .withEventListener(new org.openstack.atlas.api.mapper.dozer.converter.EventListener())
                .build();
        return mapper;
    }
}
