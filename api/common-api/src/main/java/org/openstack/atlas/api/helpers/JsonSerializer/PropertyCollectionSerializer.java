package org.openstack.atlas.api.helpers.JsonSerializer;

import org.openstack.atlas.api.helpers.reflection.ClassReflectionTools;
import org.openstack.atlas.api.helpers.reflection.ClassReflectionToolsException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;

/***
 * This serializer overrides any custom serializers defined for types via a
 * CustomSerializerFactory (or similar) and uses the BeanSerializerFactory for
 * serializing types. It also creates a clean SerializerProvider, which ensures
 * that any previously registered custom serializers are not used. This
 * particular serializer is really valuable when you want collections to be
 * serialized naturally and single objects to be serialized differently (e.g.
 * with a wrapper).
 *
 * Optionally, a wrapperFieldName can be supplied that will be used to write a
 * wrapping object around the JSON output.
 *
 * @author jodom
 *
 */


public class PropertyCollectionSerializer extends JsonSerializer<Object> {

    private SerializationConfig config;
    private String wrapperFieldName;
    private String getterName;

    public PropertyCollectionSerializer(SerializationConfig config,
            Class someClass, String getterName) {
        this.config = config;

        this.wrapperFieldName = (someClass == null) ? null : ClassReflectionTools.getXmlRootElementName(someClass);
        this.getterName = getterName;
    }

    @Override
    public void serialize(Object value, JsonGenerator jgen,
            SerializerProvider sp) throws JsonProcessingException, IOException {

        String format;
        String valClassName;
        String errMsg;
        Class childClass;
        List propList;

        valClassName = value.getClass().getName();
        try {
            propList = (List) ClassReflectionTools.invokeGetter(value, getterName);
        } catch (ClassReflectionToolsException ex) {
            format = "Error Failed to dynamicly invoke %s.%s() during serialization of %s";
            errMsg = String.format(format, valClassName, getterName, value.toString());
            throw new org.codehaus.jackson.JsonGenerationException(errMsg, ex);
        }

        CustomSerializerFactory csf = new CustomSerializerFactory();
        csf.addSpecificMapping(GregorianCalendar.class, new DateTimeSerializer(config,null));


        if (this.wrapperFieldName != null) {
            jgen.writeStartObject();
            jgen.writeFieldName(this.wrapperFieldName);
        }

        if (propList != null && !propList.isEmpty()) {
            jgen.writeStartArray();
            for (Object childObj : propList) {
                childSerialize(childObj, jgen);
            }
            jgen.writeEndArray();
        }
        else {
            //list is Empty JIRA SITESLB-747
            jgen.writeStartArray();
            jgen.writeEndArray();
        }

        if (wrapperFieldName != null) {
            jgen.writeEndObject();
        }
    }

    // Cause I kept getting confused when this was don in the serializer method directly
    private void childSerialize(Object obj, JsonGenerator jgen) throws JsonProcessingException, IOException {
        SerializerProviderBuilder providerBuilder = new SerializerProviderBuilder();
        //BeanSerializerFactory csf = BeanSerializerFactory.instance;
        CustomSerializerFactory csf = new CustomSerializerFactory();
        csf.addSpecificMapping(GregorianCalendar.class, new DateTimeSerializer(config,null));
        SerializerProvider childProvider;
        JavaType childType = TypeFactory.type(obj.getClass());
        BasicBeanDescription childBeanDesc = this.config.introspect(childType);
        JsonSerializer<Object> childSerializer = csf.findBeanSerializer(childType, config, childBeanDesc);
        childProvider = providerBuilder.createProvider(config, csf);
        childSerializer.serialize(obj, jgen, childProvider);
    }
}
