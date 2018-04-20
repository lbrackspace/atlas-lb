package org.openstack.atlas.api.helpers.JsonSerializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.openstack.atlas.api.helpers.reflection.ClassReflectionTools;
import org.openstack.atlas.api.helpers.reflection.ClassReflectionToolsException;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;

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

public class PropertyCollectionSerializer extends JsonSerializer<Object> {
    private SerializationConfig config;
    private String wrapperFieldName;
    private String getterName;
    private Boolean hasLinks = false;

    public PropertyCollectionSerializer(SerializationConfig config, Class someClass, String getterName) {
        this.config = config;
        this.wrapperFieldName = (someClass == null) ? null : ClassReflectionTools.getXmlRootElementName(someClass);
        this.getterName = getterName;
    }

    public PropertyCollectionSerializer(SerializationConfig config, Class someClass, String getterName, Boolean links) {
        this.config = config;
        this.wrapperFieldName = (someClass == null) ? null : ClassReflectionTools.getXmlRootElementName(someClass);
        this.getterName = getterName;
        this.hasLinks = links;
    }

    @Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider sp) throws IOException {
        String valClassName = value.getClass().getName();
        List propList;

        try {
            propList = (List) ClassReflectionTools.invokeGetter(value, getterName);
        } catch (ClassReflectionToolsException ex) {
            String format = "Error Failed to dynamically invoke %s.%s() during serialization of %s";
            String errMsg = String.format(format, valClassName, getterName, value.toString());
            throw new JsonGenerationException(errMsg, ex);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addSerializer(GregorianCalendar.class,  new DateTimeSerializer(this.config, null)));

        if (this.wrapperFieldName != null) {
            jgen.writeStartObject();
            jgen.writeFieldName(this.wrapperFieldName);
            writeJsonArray(jgen, propList, true);
        } else {
            writeJsonArray(jgen, propList, false);
        }

        if (hasLinks) {
            writeLinks(value, jgen, valClassName);
        }

        if (wrapperFieldName != null) {
            jgen.writeEndObject();
        }
    }

    private void writeLinks(Object value, JsonGenerator jgen, String valClassName) throws IOException {
        List propList;
        String format;
        String errMsg;
        String linksGetMethod = "getLinks";

        try {
            propList = (List) ClassReflectionTools.invokeGetter(value, linksGetMethod);
        } catch (ClassReflectionToolsException ex) {
            format = "Error Failed to dynamically invoke %s.%s() during serialization of %s";
            errMsg = String.format(format, valClassName, linksGetMethod, value.toString());
            throw new JsonGenerationException(errMsg, ex);
        }

        writeJsonArrayWithFieldName(jgen, propList, false, "links");
    }

    private void writeJsonArray(JsonGenerator jgen, List propList, boolean writeWhenNullOrEmpty) throws IOException {
        if (propList != null && !propList.isEmpty()) {
            jgen.writeStartArray();
            for (Object childObj : propList) {
                childSerialize(childObj, jgen);
            }
            jgen.writeEndArray();
        } else if (writeWhenNullOrEmpty) {
            jgen.writeStartArray();
            jgen.writeEndArray();
        }
    }

    private void writeJsonArrayWithFieldName(JsonGenerator jgen, List propList, boolean writeWhenNullOrEmpty, String fieldName) throws IOException {
        if (writeWhenNullOrEmpty || !propList.isEmpty()) jgen.writeFieldName(fieldName);
        writeJsonArray(jgen, propList, writeWhenNullOrEmpty);
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
