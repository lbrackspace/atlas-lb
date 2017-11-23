package org.openstack.atlas.api.helpers.JsonDeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import org.openstack.atlas.api.helpers.reflection.ClassReflectionTools;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.openstack.atlas.util.converters.DateTimeConverters.isoTocal;

import java.io.IOException;

import static org.openstack.atlas.api.filters.helpers.StringUtilities.getExtendedStackTrace;

public class DateTimeDeserializer extends JsonDeserializer {

    private final Log LOG = LogFactory.getLog(DateTimeDeserializer.class);

    private Class forClass;
    private ObjectMapper cleanObjectMapper = new ObjectMapper();

    public DateTimeDeserializer() {
    }

    public DateTimeDeserializer(Class forClass) {
        this.forClass = forClass;
    }

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
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
        nodeStr = childNode.toString().replace("\"","");
        try {
            nodeObj = isoTocal(nodeStr);
        } catch (Exception ex) {
            excMsg = getExtendedStackTrace(ex);
            errMsg = String.format("Error converting \"%s\" into class %s\n",nodeStr,forClass.toString());
            LOG.error(errMsg);
            throw JsonMappingException.from(jp,errMsg);
        }
        return nodeObj;
    }
}
