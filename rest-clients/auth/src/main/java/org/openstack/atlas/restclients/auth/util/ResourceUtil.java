package org.openstack.atlas.restclients.auth.util;

import org.w3c.dom.Node;

import javax.xml.bind.*;
import java.io.StringWriter;

public class ResourceUtil {
    public static StringWriter marshallResource(JAXBElement element, JAXBContext context) throws JAXBException {
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
        marshaller.setProperty("jaxb.encoding", "UTF-8");
        StringWriter writer = new StringWriter();
        marshaller.marshal(element, writer);
        return writer;
    }
}
