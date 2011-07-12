package org.openstack.atlas.api.mgmt.filters.helpers;

import org.openstack.atlas.api.helpers.JsonObjectMapper;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

public class XmlJsonConfig {
    private static final SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
    private JAXBContext fCtx;
    private JsonObjectMapper mapper;

    private Schema fSchema;
    private String fPkg;
    private String fXsd;

    public XmlJsonConfig(){
    }

    public void startConfig() throws JAXBException, SAXException, IOException{
        this.fCtx = JAXBContext.newInstance(fPkg);
        SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        fSchema = sf.newSchema((new ClassPathResource(fXsd)).getURL());
    }

    public JAXBContext getfCtx() {
        return fCtx;
    }

    public void setMapper(JsonObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JsonObjectMapper getMapper() {
        return mapper;
    }

    public Schema getfSchema() {
        return fSchema;
    }

    public void setfPkg(String fPkg) {
        this.fPkg = fPkg;
    }

    public void setfXsd(String fXsd) {
        this.fXsd = fXsd;
    }
}
