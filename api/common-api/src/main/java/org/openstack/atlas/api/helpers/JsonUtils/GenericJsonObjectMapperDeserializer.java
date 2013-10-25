package org.openstack.atlas.api.helpers.JsonUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.openstack.atlas.util.debug.Debug;

public class GenericJsonObjectMapperDeserializer extends JsonDeserializer {

    private static final Log LOG = LogFactory.getLog(GenericJsonObjectMapperDeserializer.class);
    private Method deserializerMethod;

    public GenericJsonObjectMapperDeserializer(Method serializerMethod) {
        this.deserializerMethod = serializerMethod;
    }

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        Object obj;
        try {
            JsonNode jsonNode = jp.readValueAsTree();
            obj = deserializerMethod.invoke(null, jsonNode);
        } catch (Exception ex) {
            String excMsg = Debug.getExtendedStackTrace(ex);
            LOG.error(String.format("Exception: %s", excMsg), ex);
            throw new JsonMappingException("InvocationtargetException", ex);
        }
        return obj;
    }
}
