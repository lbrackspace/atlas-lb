package org.openstack.atlas.api.helpers.JsonSerializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.openstack.atlas.api.helpers.reflection.ClassReflectionTools;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;

import java.io.IOException;

public class LoadBalancerWrapperSerializer extends JsonSerializer<LoadBalancer> {

    private final SerializationConfig config;
    private final String wrapperFieldName;

    public LoadBalancerWrapperSerializer(SerializationConfig config, Class<LoadBalancer> loadBalancer) {
        String rootName;
        this.config = config;
        if (loadBalancer == null) {
            this.wrapperFieldName = null;
            return;
        }
        rootName = ClassReflectionTools.getXmlRootElementName(loadBalancer);
        if (rootName == null) {
            this.wrapperFieldName = loadBalancer.getName();
            return;
        }
        this.wrapperFieldName = rootName;
    }


    @Override
    public void serialize(LoadBalancer loadBalancer, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        JavaType type = TypeFactory.defaultInstance().constructType(loadBalancer.getClass());
        BeanDescription beanDesc = serializerProvider.getConfig().introspect(type);
        JsonSerializer<Object> serializer = BeanSerializerFactory.instance.findBeanSerializer(serializerProvider, type, beanDesc);

        if (wrapperFieldName != null) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName(wrapperFieldName);
        }
        ObjectMapper mapper = new ObjectMapper();
        String carJson = mapper.writeValueAsString(loadBalancer);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(carJson));

        serializer.serialize(loadBalancer, jsonGenerator, serializerProvider);
        if (wrapperFieldName != null) {
            jsonGenerator.writeEndObject();
        }

    }


}
