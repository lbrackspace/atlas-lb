package org.openstack.atlas.atom.util;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
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

        marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
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

//    public static String marshallUsage(UsageV1Pojo usageV1) throws JAXBException {
//        return marshallObject(usageV1);
//    }
//
//    public static String marshallEntry(EntryPojo entry) throws JAXBException {
//        return marshallObject(entry);
//    }
}
