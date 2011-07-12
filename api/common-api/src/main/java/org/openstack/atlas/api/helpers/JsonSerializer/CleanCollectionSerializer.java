package org.openstack.atlas.api.helpers.JsonSerializer;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.ser.BeanSerializerFactory;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

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

		JavaType type = TypeFactory.type(value.getClass());
		BasicBeanDescription beanDesc = config.introspect(type);
		JsonSerializer<Object> serializer = bsf.findBeanSerializer(type,
				config, beanDesc);

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
