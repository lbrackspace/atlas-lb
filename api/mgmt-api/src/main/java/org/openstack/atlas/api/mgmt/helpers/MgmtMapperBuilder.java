package org.openstack.atlas.api.mgmt.helpers;

import org.dozer.DozerBeanMapper;
import org.dozer.DozerEventListener;

import java.util.ArrayList;
import java.util.List;

public class MgmtMapperBuilder {
    public static DozerBeanMapper getConfiguredMapper() {
        List<String> mappingFiles = new ArrayList<String>();
        mappingFiles.add("loadbalancing-dozer-management-mapping.xml");
        DozerBeanMapper mapper = new DozerBeanMapper(mappingFiles);
        ArrayList<DozerEventListener> eventListeners = new ArrayList<DozerEventListener>();
        eventListeners.add(new org.openstack.atlas.api.mapper.dozer.converter.EventListener());
        mapper.setEventListeners(eventListeners);
        return mapper;
    }
}
