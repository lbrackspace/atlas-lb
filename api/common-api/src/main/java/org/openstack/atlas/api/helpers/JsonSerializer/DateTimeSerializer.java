package org.openstack.atlas.api.helpers.JsonSerializer;

import org.openstack.atlas.api.helpers.JsonSerializeException;
import org.openstack.atlas.api.helpers.reflection.ClassReflectionTools;

import static org.openstack.atlas.util.converters.DateTimeConverters.calToiso;
import org.openstack.atlas.util.common.exceptions.ConverterException;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;

public class DateTimeSerializer extends JsonSerializer<Object> {

    private final SerializationConfig config;
    private final String wrapperFieldName;

    public DateTimeSerializer(SerializationConfig config, Class someClass) {
        String rootName;
        String nameSpace;
        this.config = config;
        if (someClass == null) {
            this.wrapperFieldName = null;
            return;
        }
        rootName = ClassReflectionTools.getXmlRootElementName(someClass);
        if (rootName == null) {
            this.wrapperFieldName = someClass.getSimpleName();
            return;
        }
        this.wrapperFieldName = rootName;
    }

    @Override
    public void serialize(Object object, JsonGenerator jgen,
            SerializerProvider sp) throws IOException, JsonProcessingException {
        String isoStr;
        Calendar cal;
        cal = (Calendar) object;
        String msg;
        try {
            isoStr = calToiso(cal);
        } catch (ConverterException ex) {
            msg = String.format("Error converting calendar to iso8601 string");
            Logger.getLogger(DateTimeSerializer.class.getName()).log(Level.SEVERE, null, ex);
            throw new JsonSerializeException(msg, ex);
        }
        jgen.writeString(isoStr);
    }
}
