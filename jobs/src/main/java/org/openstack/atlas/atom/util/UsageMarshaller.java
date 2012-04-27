package org.openstack.atlas.atom.util;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import org.openstack.atlas.atom.pojo.UsageV1Pojo;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class UsageMarshaller {
    public static String marshallUsage(UsageV1Pojo usageV1) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(UsageV1Pojo.class);

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
        marshaller.setProperty(CharacterEscapeHandler.class.getName(), new CharacterEscapeHandler() {
            @Override
            public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
                out.write(ch, start, length);
            }
        });

        StringWriter st = new StringWriter();
        marshaller.marshal(usageV1, st);
        String xml = st.toString();
        return xml;
    }
}
