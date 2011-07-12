package org.openstack.atlas.api.helpers.JsonDeserializer;

import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializerFactory;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;

public class DeserializerProviderBuilder extends StdDeserializerProvider {

    private DeserializationConfig config;

    public DeserializerProviderBuilder() {
        super();
    }

    public DeserializerProviderBuilder(DeserializerFactory jsf) {
        super(jsf);
    }

    public DeserializerProvider createProvider(DeserializationConfig config,
            DeserializerFactory jsf) {
        this.config = config;
        return new DeserializerProviderBuilder(jsf);
    }
}
