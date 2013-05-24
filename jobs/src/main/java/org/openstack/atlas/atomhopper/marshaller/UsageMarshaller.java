package org.openstack.atlas.atomhopper.marshaller;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import org.w3c.dom.Node;

import javax.xml.bind.*;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class UsageMarshaller {
    /**
     *
     * @param object: the object to marshall
     * @return String of marshalled xml
     * @throws JAXBException
     */
    public static String marshallObject(Object object) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(object.getClass());

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.TRUE);
        marshaller.setProperty(CharacterEscapeHandler.class.getName(), new CharacterEscapeHandler() {
            @Override
            public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
                out.write(ch, start, length);
            }
        });

        StringWriter st = new StringWriter();

        marshaller.marshal(object, st);
        String xml = st.toString();
        return xml;
    }

    public static StringWriter marshallResource(JAXBElement element, JAXBContext context) throws JAXBException {
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
        marshaller.setProperty("jaxb.encoding", "UTF-8");
        StringWriter writer = new StringWriter();
        marshaller.marshal(element, writer);
        return writer;
    }

    public static JAXBElement unmarshallResource(Object content, JAXBContext context) throws JAXBException {
        Unmarshaller um = context.createUnmarshaller();
        return (JAXBElement) um.unmarshal((Node) content);
    }
}
