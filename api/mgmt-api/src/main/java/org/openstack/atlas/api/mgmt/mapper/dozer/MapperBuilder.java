package org.openstack.atlas.api.mgmt.mapper.dozer;

import org.dozer.Mapper;
import org.dozer.DozerBeanMapperBuilder;

// The things we have to copy to makeing testing more flexible.
public class MapperBuilder {
    public static Mapper getConfiguredMapper(String mappingFile) {
        Mapper mapper = DozerBeanMapperBuilder.create()
                .withMappingFiles(mappingFile)
                .withEventListener(new org.openstack.atlas.api.mapper.dozer.converter.EventListener())
                .build();
        return mapper;
    }
}
