package org.openstack.atlas.api.mgmt.resources.providers;

import org.dozer.CustomConverter;
import org.dozer.DozerBeanMapperBuilder;
import org.dozer.DozerEventListener;
import org.dozer.Mapper;
import org.dozer.classmap.MappingFileData;

import java.util.List;

public class MgmtDozerMapperBuilderBean {

    protected Mapper dozerMapperObject;

    private List<String> mappingFiles;

    private List<DozerEventListener> eventListeners;

    public List<String> getMappingFiles() {
        return mappingFiles;
    }

    public void setMappingFiles(List<String> mappingFiles) {
        this.mappingFiles = mappingFiles;
    }

    public List<DozerEventListener> getEventListeners() {
        return eventListeners;
    }

    public void setEventListeners(List<DozerEventListener> eventListeners) {
        this.eventListeners = eventListeners;
    }

    public Mapper getDozerMapperObject() {
        return dozerMapperObject;
    }

    public void setDozerMapperObject(Mapper dozerMapperObject) {
        this.dozerMapperObject = dozerMapperObject;
    }

    public void init() throws Exception{
        DozerBeanMapperBuilder dozerBeanMapperBuilder = DozerBeanMapperBuilder.create()
                .withMappingFiles(mappingFiles.toArray(new String[mappingFiles.size()]));
        for(DozerEventListener listener : eventListeners) {
            //Multiple calls of this method will register multiple listeners in the order of calling
            dozerBeanMapperBuilder = dozerBeanMapperBuilder.withEventListener(listener);
        }
        dozerMapperObject = dozerBeanMapperBuilder.build();
    }
}
