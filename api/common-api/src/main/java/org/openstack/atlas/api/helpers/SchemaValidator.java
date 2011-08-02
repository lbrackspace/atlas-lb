package org.openstack.atlas.api.helpers;

import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Consumes(MediaType.APPLICATION_XML)
public class SchemaValidator implements MessageBodyReader<Object> {
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream inputStream) throws IOException, WebApplicationException {

        if(httpHeaders.get("HTTP_VERB") == null) return null;

        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document document;

        document = new DocumentImpl();
        final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

        try {
            factory = DocumentBuilderFactory.newInstance();

            factory.setValidating(false);
            factory.setNamespaceAware(true);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA);

            factory.setSchema(schemaFactory.newSchema(new Source[] {new StreamSource("org.openstack.cloud/service/loadbalancing/xsd/LoadBalancerApi.xsd")}));

            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new SimpleErrorHandler());
            document = builder.parse(new InputSource("contacts.xml"));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document.getDocumentElement();
    }
}
