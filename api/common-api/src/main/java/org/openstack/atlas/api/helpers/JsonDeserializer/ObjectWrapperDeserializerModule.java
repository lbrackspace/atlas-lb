package org.openstack.atlas.api.helpers.JsonDeserializer;

import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.module.SimpleModule;


public class ObjectWrapperDeserializerModule extends SimpleModule {
    private static final String NAME = "ObjectWrapperDeserializerModule";
    private static final VersionUtil VERSION_UTIL = new VersionUtil() {};

    public ObjectWrapperDeserializerModule(Class someClass) {
        super(NAME, VERSION_UTIL.version());
        addDeserializer(Object.class, new ObjectWrapperDeserializer(someClass));
    }
}


