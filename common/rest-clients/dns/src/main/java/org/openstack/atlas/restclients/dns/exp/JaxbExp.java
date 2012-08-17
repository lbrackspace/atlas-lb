package org.openstack.atlas.restclients.dns.exp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JaxbExp {
    public static final int PAGESIZE = 4096;
    public static Object deserialize(Class oClass,String xml) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(oClass);
        Unmarshaller m = ctx.createUnmarshaller();
        return m.unmarshal(new StringReader(xml));
    }

    public static String serialize(Object obj) throws JAXBException {
        Class oClass = obj.getClass();
        StringWriter sw = new StringWriter(PAGESIZE);
        JAXBContext ctx = JAXBContext.newInstance(oClass);
        Marshaller m = ctx.createMarshaller();
        m.marshal(obj,sw);
        return sw.toString();
    }

    public static InputStream StrToInputStream(String strIn) throws UnsupportedEncodingException {
        byte[] bytes = strIn.getBytes("UTF-8");
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        return is;
    }
}
