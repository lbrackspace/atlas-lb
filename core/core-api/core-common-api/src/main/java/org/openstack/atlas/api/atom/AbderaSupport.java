package org.openstack.atlas.api.atom;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static javax.ws.rs.core.MediaType.APPLICATION_ATOM_XML;

@Provider
@Produces(APPLICATION_ATOM_XML)
@Consumes(APPLICATION_ATOM_XML)
public class AbderaSupport implements MessageBodyWriter<Object>, MessageBodyReader<Object> {

    private static final Abdera abderaInstance = new Abdera();

    public static Abdera getAbderaInstance() {
        return abderaInstance;
    }

    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return (Feed.class.isAssignableFrom(aClass) || Entry.class.isAssignableFrom(aClass));
    }

    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return (Feed.class.isAssignableFrom(aClass) || Entry.class.isAssignableFrom(aClass));
    }

    public long getSize(Object o, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    public Object readFrom(Class<Object> receivedClassInfo, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> stringStringMultivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
        Document<Element> doc = getAbderaInstance().getParser().parse(inputStream);
        Element el = doc.getRoot();

        if (!receivedClassInfo.isAssignableFrom(el.getClass())) {
            throw new IOException("Unexpected payload, expected " + receivedClassInfo.getName() + ", received " + el.getClass().getName());
        }

        return el;
    }

    public void writeTo(Object feedOrEntry, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> stringObjectMultivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
        if (!(feedOrEntry instanceof Feed) && !(feedOrEntry instanceof Entry)) {
            //TODO: Failcase goes here
        }

        final Source elementSource = (Source) feedOrEntry;
        final Document document = elementSource.getDocument();

        document.writeTo(outputStream);
    }
}
