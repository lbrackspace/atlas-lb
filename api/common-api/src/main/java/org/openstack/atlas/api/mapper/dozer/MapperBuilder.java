package org.openstack.atlas.api.mapper.dozer;

import org.dozer.DozerBeanMapper;
import org.dozer.DozerBeanMapperBuilder;

public class MapperBuilder {
    public static DozerBeanMapper getConfiguredMapper(String mappingFile) {
        DozerBeanMapper mapper = (DozerBeanMapper) DozerBeanMapperBuilder.create()
                .withMappingFiles(mappingFile)
                .withEventListener(new org.openstack.atlas.api.mapper.dozer.converter.EventListener())
                .build();
        return mapper;
    }
}
