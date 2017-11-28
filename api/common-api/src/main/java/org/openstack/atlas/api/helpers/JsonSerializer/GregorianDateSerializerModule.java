package org.openstack.atlas.api.helpers.JsonSerializer;

import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.GregorianCalendar;


public class GregorianDateSerializerModule extends SimpleModule {
    private static final String NAME = "ObjectWrapperSerializerModule";
    private static final VersionUtil VERSION_UTIL = new VersionUtil() {};

    public GregorianDateSerializerModule(SerializationConfig config, Class someClass) {
        super(NAME, VERSION_UTIL.version());
        addSerializer(GregorianCalendar.class, new DateTimeSerializer(config, someClass));
    }
}


