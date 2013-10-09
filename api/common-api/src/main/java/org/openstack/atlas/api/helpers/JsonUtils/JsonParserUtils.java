package org.openstack.atlas.api.helpers.JsonUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class JsonParserUtils {

    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
    }

    public static ObjectMapper getObjectMapper() {
        return mapper;
    }

    public static JsonFactory getJsonFactory() {
        return mapper.getJsonFactory();
    }

    public static JsonNode getNode(String jsonStr) throws JsonParseException, JsonProcessingException, IOException {
        return mapper.getJsonFactory().createJsonParser(jsonStr).readValueAsTree();
    }

    public static ObjectNode newObjectNode() {
        return mapper.createObjectNode();
    }

    public static ArrayNode newArrayNode() {
        return mapper.createArrayNode();
    }

    public static JsonNode getNodeFromFile(String fileName) throws FileNotFoundException, UnsupportedEncodingException, JsonProcessingException, IOException {
        return mapper.getJsonFactory().createJsonParser(StaticFileUtils.readFileToString(fileName)).readValueAsTree();
    }

    public static JsonParser getJsonParser(String jsonStr) throws JsonParseException, IOException {
        return mapper.getJsonFactory().createJsonParser(jsonStr);
    }

    public static JsonGenerator getJsonGenerator(Writer wr) throws IOException {
        return mapper.getJsonFactory().createJsonGenerator(wr);
    }

    public static JsonParser getJsonParserFromFile(String fileNameStr) throws JsonParseException, IOException {
        return mapper.getJsonFactory().createJsonParser(StaticFileUtils.readFileToString(fileNameStr));
    }

    public static List<String> getKeys(ObjectNode on) {
        List<String> keys = new ArrayList<String>();
        Iterator<String> keyIter = on.getFieldNames();
        while (keyIter.hasNext()) {
            keys.add(keyIter.next());
        }
        return keys;
    }

    public static JsonGeneratorWriter newJsonGeneratorStringWriter() throws IOException {
        StringWriter wr = new StringWriter();
        JsonGenerator jg = JsonParserUtils.getObjectMapper().getJsonFactory().createJsonGenerator(new StringWriter());
        return new JsonGeneratorWriter(jg, wr);
    }
}
