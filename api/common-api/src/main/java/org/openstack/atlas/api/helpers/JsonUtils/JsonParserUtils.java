package org.openstack.atlas.api.helpers.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonFactory;
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

    public static JsonFactory getJsonFactory() {
        return mapper.getJsonFactory();
    }

    public static JsonParser getJsonParser(String jsonStr) throws JsonParseException, IOException {
        return mapper.getJsonFactory().createJsonParser(jsonStr);
    }

    public static JsonParser getJsonParserFromFile(String fileNameStr) throws JsonParseException, IOException {
        String data = StaticFileUtils.readFileToString(fileNameStr);
        JsonParser jp = mapper.getJsonFactory().createJsonParser(data);
        return jp;
    }

    public static List<JsonNode> getChildrenNodes(ArrayNode on) {
        List<JsonNode> children = new ArrayList<JsonNode>();
        for (JsonNode jn : on) {
            children.add(jn);
        }
        return children;
    }

    public static List<String> getChildrenNodeKeys(ObjectNode on) {
        List<String> keys = new ArrayList<String>();
        Iterator<String> keyIterator = on.getFieldNames();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            keys.add(key);
        }
        return keys;
    }

    public static Map<String, JsonNode> getChildrenNodes(ObjectNode on) {
        Map<String, JsonNode> map = new HashMap<String, JsonNode>();
        Iterator<Entry<String, JsonNode>> fields = on.getFields();
        while (fields.hasNext()) {
            Entry<String, JsonNode> field = fields.next();
            map.put(field.getKey(), field.getValue());
        }
        return map;
    }
}
