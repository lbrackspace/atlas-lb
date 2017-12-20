package org.openstack.atlas.api.mgmt.helpers;

import org.dozer.DozerBeanMapper;
import org.dozer.DozerBeanMapperBuilder;

public class MgmtMapperBuilder {
    public static DozerBeanMapper getConfiguredMapper() {
        String mappingFile = "loadbalancing-dozer-management-mapping.xml";
        DozerBeanMapper mapper = (DozerBeanMapper) DozerBeanMapperBuilder.create()
                .withMappingFiles(mappingFile)
                .withEventListener(new org.openstack.atlas.api.mapper.dozer.converter.EventListener())
                .build();
        return mapper;
    }
}
