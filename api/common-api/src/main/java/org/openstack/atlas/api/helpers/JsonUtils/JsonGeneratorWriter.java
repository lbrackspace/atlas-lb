package org.openstack.atlas.api.helpers.JsonUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.codehaus.jackson.JsonGenerator;

public class JsonGeneratorWriter {

    private JsonGenerator jsonGenerator;
    private Writer writer;

    public JsonGeneratorWriter(JsonGenerator jg, Writer wr) {
        jsonGenerator = jg;
        writer = wr;
    }


    public JsonGenerator getJsonGenerator() {
        return jsonGenerator;
    }

    public void setJsonGenerator(JsonGenerator jsonGenerator) {
        this.jsonGenerator = jsonGenerator;
    }

    public Writer getWriter() {
        return writer;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }
}
