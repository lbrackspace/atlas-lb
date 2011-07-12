package org.openstack.atlas.api.helpers.JsonDeserializer;

import org.openstack.atlas.api.helpers.reflection.ClassReflectionTools;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.List;

import static org.openstack.atlas.api.filters.helpers.StringUtilities.getExtendedStackTrace;

public class PropertyListDeserializer extends JsonDeserializer {

    private final Log LOG = LogFactory.getLog(PropertyListDeserializer.class);

    private ObjectMapper cleanObjectMapper = new ObjectMapper();
    private Class forClass;
    private Class itemClass;
    private String getterName;

    public PropertyListDeserializer() {
    }

    public PropertyListDeserializer(Class forClass,Class itemClass,String getterName) {
        this.forClass = forClass;
        this.itemClass = itemClass;
        this.getterName = getterName;
    }

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Object out = null;
        List childList;
        String nodeStr = "";
        String rootName = ClassReflectionTools.getXmlRootElementName(this.forClass);
        String errMsg = "";
        String excMsg = "";
        JsonNode node = jp.readValueAsTree();
        JsonNode childNode = null;
        Object nodeObj = null;
        if (node.has(rootName)) { // If a root name is found on input strip it off
                                  //and continue decoding
            childNode = node.get(rootName);
            JsonParser childParser = childNode.traverse();
        } else {
            childNode = node; // If its not wrapped don't worry about it cause 
                              // its probably a nested child. For example Node fro Nodes
        }
        nodeStr = childNode.toString();
        try {
            out = ClassReflectionTools.newInstance(forClass);
            childList = (List) ClassReflectionTools.invokeGetter(out, getterName);
            for(JsonNode itemNode:childNode) {
                String itemJson = itemNode.toString();
                Object item = cleanObjectMapper.readValue(itemJson,itemClass);
                childList.add(item);
            }
        } catch (Exception ex) {
            excMsg = getExtendedStackTrace(ex);
            errMsg = String.format("Error converting \"%s\" into class %s\n",nodeStr,forClass.toString());
            LOG.error(errMsg);
            throw JsonMappingException.from(jp,errMsg);
        }
        return out;
    }
}
