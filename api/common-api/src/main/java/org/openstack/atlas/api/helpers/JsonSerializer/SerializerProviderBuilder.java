package org.openstack.atlas.api.helpers.JsonSerializer;

import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerFactory;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.StdSerializerProvider;

public class SerializerProviderBuilder extends StdSerializerProvider {

	public SerializerProvider createProvider(SerializationConfig config,
			SerializerFactory jsf) {
		return super.createInstance(config, jsf);
	}
}
