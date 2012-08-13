package org.openstack.atlas.restclients.dns.exp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JaxbExp {
    public static final int PAGESIZE = 4096;
    public static Object deserialize(String strIn,String pkgName) throws JAXBException, UnsupportedEncodingException {
        Object obj = null;
        JAXBContext jbctx = JAXBContext.newInstance(pkgName);
        Unmarshaller u = jbctx.createUnmarshaller();
        InputStream is = StrToInputStream(strIn);
        obj = u.unmarshal(is);
        return obj;
    }

    public static String serialize(Object obj,String pkgName) throws JAXBException, UnsupportedEncodingException {
        String strOut = null;
        JAXBContext jbctx = JAXBContext.newInstance(pkgName);
        Marshaller m = jbctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_ENCODING,"UTF-8");
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
        m.marshal(obj,baos);
        strOut = baos.toString("UTF-8");
        return strOut;
    }

    public static InputStream StrToInputStream(String strIn) throws UnsupportedEncodingException {
        byte[] bytes = strIn.getBytes("UTF-8");
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        return is;
    }
}
