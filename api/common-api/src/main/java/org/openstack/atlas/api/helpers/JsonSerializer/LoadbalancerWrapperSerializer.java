//package org.openstack.atlas.api.helpers.JsonSerializer;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.core.JsonEncoding;
//import com.fasterxml.jackson.core.JsonFactory;
//import com.fasterxml.jackson.core.JsonGenerationException;
//import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
//import com.fasterxml.jackson.databind.*;
//import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
//import com.fasterxml.jackson.databind.module.SimpleModule;
//import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
//import com.fasterxml.jackson.databind.ser.PropertyWriter;
//import com.fasterxml.jackson.databind.type.TypeFactory;
//import org.openstack.atlas.api.helpers.JsonDeserializer.ObjectWrapperDeserializer;
//import org.openstack.atlas.api.helpers.reflection.ClassReflectionTools;
//import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountRecord;
//import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
//import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostMachineDetails;
//import org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit;
//import org.openstack.atlas.docs.loadbalancers.api.v1.*;
//import org.w3.atom.Link;
//
//import java.beans.Introspector;
//import java.beans.PropertyDescriptor;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.StringWriter;
//import java.lang.reflect.Method;
//import java.util.GregorianCalendar;
//import java.util.Iterator;
//import java.util.List;
//import java.util.function.Predicate;
//
///**
// * This serializer overrides any custom serializers defined for types via a
// * CustomSerializerFactory (or similar) and uses the BeanSerializerFactory for
// * serializing types. It also creates a clean SerializerProvider, which ensures
// * that any previously registered custom serializers are not used. This
// * particular serializer is really valuable when you want collections to be
// * serialized naturally and single objects to be serialized differently (e.g.
// * with a wrapper).
// * <p/>
// * Optionally, a wrapperFieldName can be supplied that will be used to write a
// * wrapping object around the JSON output.
// */
//
//public class LoadbalancerWrapperSerializer extends JsonSerializer<LoadBalancer> {
//    private SerializationConfig config;
//    private String wrapperFieldName;
//
//    public LoadbalancerWrapperSerializer(SerializationConfig config, Class someClass) {
//        this.config = config;
//        this.wrapperFieldName = (someClass == null) ? null : ClassReflectionTools.getXmlRootElementName(someClass);
//    }
//
//    @Override
//    public void serialize(LoadBalancer value, JsonGenerator jgen, SerializerProvider sp) throws IOException {
//
//        if (this.wrapperFieldName != null) {
//            jgen.writeStartObject();
//            jgen.writeFieldName(this.wrapperFieldName);
//            objSerialize(value, jgen, sp);
//        }
//
//        if (wrapperFieldName != null) {
//            jgen.writeEndObject();
//        }
//    }
//
//    private void objSerialize(LoadBalancer obj, JsonGenerator jgen, SerializerProvider sp) throws IOException {
//        SerializerProviderBuilder providerBuilder = new SerializerProviderBuilder();
//        SerializerProvider sp1 = providerBuilder.createProvider(this.config, BeanSerializerFactory.instance);
//
//        JavaType type = TypeFactory.defaultInstance().constructType(obj.getClass());
//        BeanDescription beanDesc = sp.getConfig().introspect(type);
//        JsonSerializer<Object> serializer = BeanSerializerFactory.instance.findBeanSerializer(sp, type, beanDesc);
//
////        List<BeanPropertyDefinition> props = beanDesc.findProperties();
////        for (BeanPropertyDefinition prop : props) {
////            if (prop.getName().equals("nodes")) {
////                sp1.defaultSerializeValue(prop, jgen);
////                obj.getNodes().clear();
////            }
////        }
//
////        sp1.defaultSerializeValue(obj, jgen);
//        serializer.serialize(obj, jgen, sp);
//    }
//
//    private void childSerialize(Object obj, JsonGenerator jgen) throws IOException {
//        SerializerProviderBuilder providerBuilder = new SerializerProviderBuilder();
//        SerializerProvider sp = providerBuilder.createProvider(this.config, BeanSerializerFactory.instance);
//
//        JavaType type = TypeFactory.defaultInstance().constructType(obj.getClass());
//        BeanDescription beanDesc = sp.getConfig().introspect(type);
//        JsonSerializer<Object> serializer = BeanSerializerFactory.instance.findBeanSerializer(sp, type, beanDesc);
//        serializer.serialize(obj, jgen, sp);
//    }
//
//}
