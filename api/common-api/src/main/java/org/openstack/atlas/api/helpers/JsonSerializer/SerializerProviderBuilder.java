package org.openstack.atlas.api.helpers.JsonSerializer;


import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;

public class SerializerProviderBuilder {

	public SerializerProvider createProvider(SerializationConfig config,
											 SerializerFactory jsf) {
		return new DefaultSerializerProvider.Impl().createInstance(config, jsf);
	}
}
