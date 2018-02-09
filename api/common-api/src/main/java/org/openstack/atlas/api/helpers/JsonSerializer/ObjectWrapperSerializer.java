package org.openstack.atlas.api.helpers.JsonSerializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
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

        JavaType type = TypeFactory.defaultInstance().constructType(object.getClass());
        BeanDescription beanDesc = sp.getConfig().introspect(type);
        JsonSerializer<Object> serializer = BeanSerializerFactory.instance.findBeanSerializer(sp, type, beanDesc);

        if (wrapperFieldName != null) {
            jgen.writeStartObject();
            jgen.writeFieldName(wrapperFieldName);
        }

        serializer.serialize(object, jgen, sp);
        if (wrapperFieldName != null) {
            jgen.writeEndObject();
        }
    }
}
