package org.openstack.atlas.api.helpers.JsonUtils;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.node.ObjectNode;
import org.openstack.atlas.docs.loadbalancers.api.v1.Created;
import org.openstack.atlas.docs.loadbalancers.api.v1.Updated;
import org.openstack.atlas.util.common.exceptions.ConverterException;
import org.openstack.atlas.util.converters.DateTimeConverters;

import java.util.Calendar;

public class DeserializationHelper {

    public static final String NOT_OBJ_NODE = "Error was expecting an ObjectNode({}) but instead found %s";
    public static final String NOT_OBJ_OR_ARR = "Error was expecting an ObjectNode({}) or an ArrayNode([]) but found %s";

    public static Integer getInt(JsonNode jn, String prop) {
        if (jn.get(prop) != null) {
            return new Integer(jn.get(prop).getValueAsInt());
        }
        return null;
    }

    public static Double getDouble(JsonNode jn, String prop) {
        if (jn.get(prop) != null) {
            return new Double(jn.get(prop).getValueAsDouble());
        }
        return null;
    }

    public static Long getLong(JsonNode jn, String prop) {
        if (jn.get(prop) != null) {
            return new Long(jn.get(prop).getValueAsLong());
        }
        return null;
    }

    public static String getString(JsonNode jn, String prop) {
        if (jn.get(prop) != null && jn.get(prop).isTextual()) {
            return jn.get(prop).getValueAsText();
        }
        return null;
    }

    public static Boolean getBoolean(JsonNode jn, String prop) {
        if (jn.get(prop) != null && jn.get(prop).isBoolean()) {
            return jn.get(prop).getBooleanValue();
        }
        return null;
    }

    public static Created getCreated(ObjectNode jsonNodeIn) throws JsonParseException {
        Created created = new Created();
        created.setTime(getDate(jsonNodeIn, "time"));
        return created;
    }

    public static Updated getUpdated(ObjectNode jsonNodeIn) throws JsonParseException {
        Updated updated = new Updated();
        updated.setTime(getDate(jsonNodeIn, "time"));
        return updated;
    }

    public static Calendar getDate(JsonNode jn, String prop) throws JsonParseException {
        Calendar out;
        if (jn.get(prop) != null && jn.get(prop).isTextual()) {
            String dateString = jn.get(prop).getTextValue();
            try {
                out = DateTimeConverters.isoTocal(dateString);
                return out;
            } catch (ConverterException ex) {
                String msg = String.format("Error converting %s to Date. Value must be in anISO 8601", dateString);
                throw new JsonParseException(msg, jn.traverse().getCurrentLocation());
            }
        }
        return null;
    }
}
