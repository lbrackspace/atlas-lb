package org.openstack.atlas.api.helpers.JsonUtils;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.node.ObjectNode;
import org.openstack.atlas.util.debug.Debug;

import javax.el.MethodNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericJsonListMapperSerializer extends JsonSerializer {
    private static final Log LOG = LogFactory.getLog(GenericJsonListMapperSerializer.class);
    private Map<Class, Method> classMap;

    public GenericJsonListMapperSerializer() {
        this.classMap = new HashMap<Class, Method>();
    }

    public void addToMap(Class element, String methodName) throws NoSuchMethodException {
        Method method = JsonPublicSerializers.class.getMethod(methodName, ObjectNode.class, List.class);
        classMap.put(element, method);
    }

    @Override
    public void serialize(Object inputObj, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonProcessingException {
        if (inputObj == null || !(inputObj instanceof List) || ((List) inputObj).size() <= 0) {
            return;
        }
        List inputList = (List)inputObj;
        Class elementClass = inputList.get(0).getClass();
        Method method = classMap.get(elementClass);
        if (method == null) {
            throw new JsonMappingException(String.format("Class not found: %s", elementClass.getCanonicalName()));
        }
        ObjectNode jsonObjNode = JsonParserUtils.newObjectNode();
        try {
            method.invoke(null, jsonObjNode, inputObj);
            jg.writeTree(jsonObjNode);
        } catch (Exception ex) {
            String excMsg = Debug.getExtendedStackTrace(ex);
            LOG.error(String.format("Exception: %s", excMsg), ex);
            throw new JsonGenerationException(excMsg, ex);
        }
    }
}
