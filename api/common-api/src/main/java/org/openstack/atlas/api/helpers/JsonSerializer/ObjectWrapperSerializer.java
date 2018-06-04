package org.openstack.atlas.api.helpers.JsonSerializer;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;
import org.openstack.atlas.api.helpers.reflection.ClassReflectionTools;

import java.io.IOException;
import java.util.GregorianCalendar;

public class ObjectWrapperSerializer extends JsonSerializer<Object> {

    private final SerializationConfig config;
    private final String wrapperFieldName;

    public ObjectWrapperSerializer(SerializationConfig config, Class someClass) {
        String rootName;
        this.config = config;
        if (someClass == null) {
            this.wrapperFieldName = null;
            return;
        }
        rootName = ClassReflectionTools.getXmlRootElementName(someClass);
        if (rootName == null) {
            this.wrapperFieldName = someClass.getSimpleName();
            return;
        }
        this.wrapperFieldName = rootName;
    }

    @Override
    public void serialize(Object object, JsonGenerator jgen, SerializerProvider sp) throws IOException {
        //BeanSerializerFactory bsf = BeanSerializerFactory.instance;
        CustomSerializerFactory csf = new CustomSerializerFactory();
        csf.addSpecificMapping(GregorianCalendar.class, new DateTimeSerializer(config, null));

        JavaType type = TypeFactory.type(object.getClass());
        BasicBeanDescription beanDesc = config.introspect(type);
        JsonSerializer<Object> serializer = csf.findBeanSerializer(type, config, beanDesc);

        if (wrapperFieldName != null) {
            jgen.writeStartObject();
            jgen.writeFieldName(wrapperFieldName);
        }

        SerializerProviderBuilder provider = new SerializerProviderBuilder();
        serializer.serialize(object, jgen, provider.createProvider(config, csf));
        if (wrapperFieldName != null) {
            jgen.writeEndObject();
        }
    }
}
