package org.openstack.atlas.api.helpers.JsonSerializer;

import java.io.IOException;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers;
import org.openstack.atlas.api.helpers.reflection.ClassReflectionTools;

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
public class CleanCollectionSerializer extends JsonSerializer<Object> {

	private final SerializationConfig config;
	private final String wrapperFieldName;

	public CleanCollectionSerializer(SerializationConfig config,
			Class someClass) {
		this.config = config;
		this.wrapperFieldName = ClassReflectionTools.getXmlRootElementName(someClass);
	}

	public CleanCollectionSerializer(SerializationConfig config) {
		this.config = config;
		this.wrapperFieldName = null;
	}

	@Override
	public void serialize(Object value, JsonGenerator jgen,
			SerializerProvider sp) throws IOException, JsonProcessingException {

		BeanSerializerFactory bsf = BeanSerializerFactory.instance;

		JavaType type = TypeFactory.defaultInstance().uncheckedSimpleType(value.getClass());
		BeanDescription beanDesc = config.introspect(type);
		JsonSerializer<Object> serializer = bsf.findBeanSerializer(sp, type, beanDesc);

		SerializerProviderBuilder provider = new SerializerProviderBuilder();

		if (wrapperFieldName != null) {
			jgen.writeStartObject();
			jgen.writeFieldName(wrapperFieldName);
		}

		serializer.serialize(value, jgen, provider.createProvider(config, bsf));

		if (wrapperFieldName != null) {
			jgen.writeEndObject();
		}
	}

}
