package org.openstack.atlas.api.helpers.JsonSerializer;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.openstack.atlas.api.helpers.reflection.ClassReflectionTools;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.function.Predicate;

/**
 * This serializer overrides any custom serializers defined for types via a
 * CustomSerializerFactory (or similar) and uses the BeanSerializerFactory for
 * serializing types. It also creates a clean SerializerProvider, which ensures
 * that any previously registered custom serializers are not used. This
 * particular serializer is really valuable when you want collections to be
 * serialized naturally and single objects to be serialized differently (e.g.
 * with a wrapper).
 * <p/>
 * Optionally, a wrapperFieldName can be supplied that will be used to write a
 * wrapping object around the JSON output.
 */

public class LoadbalancerWrapperSerializer extends JsonSerializer<Object> {
    private SerializationConfig config;
    private String wrapperFieldName;
    private Boolean hasLinks = false;

    public LoadbalancerWrapperSerializer(SerializationConfig config, Class someClass) {
        this.config = config;
        this.wrapperFieldName = (someClass == null) ? null : ClassReflectionTools.getXmlRootElementName(someClass);
    }

    @Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider sp) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addSerializer(GregorianCalendar.class,  new DateTimeSerializer(this.config, null)));

        if (this.wrapperFieldName != null) {
            jgen.writeStartObject();
            jgen.writeFieldName(this.wrapperFieldName);
            childSerialize(value, jgen);
        }

        if (wrapperFieldName != null) {
            jgen.writeEndObject();
        }
    }

    private void childSerialize(Object obj, JsonGenerator jgen) throws IOException {
        SerializerProviderBuilder providerBuilder = new SerializerProviderBuilder();
        SerializerProvider sp = providerBuilder.createProvider(this.config, BeanSerializerFactory.instance);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addSerializer(GregorianCalendar.class,  new DateTimeSerializer(this.config, null)));

        JavaType type = TypeFactory.defaultInstance().constructType(obj.getClass());
        BeanDescription beanDesc = sp.getConfig().introspect(type);
        JsonSerializer<Object> serializer = BeanSerializerFactory.instance.findBeanSerializer(sp, type, beanDesc);
        serializer.serialize(obj, jgen, sp);
    }

}
