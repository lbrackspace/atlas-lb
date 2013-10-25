package org.openstack.atlas.api.helpers.JsonUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.openstack.atlas.util.debug.Debug;

public class GenericJsonObjectMapperSerializer extends JsonSerializer {

    private static final Log LOG = LogFactory.getLog(GenericJsonObjectMapperSerializer.class);
    private Boolean hasLinks;
    private Method serializerMethod;

    public GenericJsonObjectMapperSerializer(Method serializerMethod, Boolean hasLinks) {
        this.serializerMethod = serializerMethod;
        this.hasLinks = hasLinks;
    }

    @Override
    public void serialize(Object inputObj, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonProcessingException {
        ObjectNode jsonObjNode = JsonParserUtils.newObjectNode();
        try {
            if (hasLinks != null && hasLinks) {
                serializerMethod.invoke(null, jsonObjNode, inputObj, (hasLinks) ? true : false);
            } else {
                serializerMethod.invoke(null, jsonObjNode, inputObj);
            }
            jg.writeTree(jsonObjNode);
        } catch (Exception ex) {
            String excMsg = Debug.getExtendedStackTrace(ex);
            LOG.error(String.format("Exception: %s", excMsg), ex);
            throw new JsonGenerationException(excMsg, ex);
        }
    }
}
