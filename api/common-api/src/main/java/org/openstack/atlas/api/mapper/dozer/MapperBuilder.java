package org.openstack.atlas.api.mapper.dozer;

import org.dozer.DozerBeanMapper;
import org.dozer.DozerEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapperBuilder {
    public static DozerBeanMapper getConfiguredMapper(String mappingFile) {
        List<String> mappingFiles = new ArrayList<String>();
        mappingFiles.add(mappingFile);
        DozerBeanMapper mapper = new DozerBeanMapper(mappingFiles);
        ArrayList<DozerEventListener> eventListeners = new ArrayList<DozerEventListener>();
        eventListeners.add(new org.openstack.atlas.api.mapper.dozer.converter.EventListener());
        mapper.setEventListeners(eventListeners);
        return mapper;
    }
}
