package org.openstack.atlas.restclients.dns.exp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JaxbExp {

    public static final int PAGESIZE = 4096;
    public static final JAXBContext arryCtx;

    static {
        try {
            arryCtx = JAXBContext.newInstance(getObjClasses());
        } catch (JAXBException ex) {
            throw new RuntimeException("JAXBContext failed to build for JaxbExp");
        }
    }

    public static Object deserialize(Class oClass, String xml) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(oClass);
        Unmarshaller u = ctx.createUnmarshaller();
        return u.unmarshal(new StringReader(xml));
    }

    public static Object deserializeAryClasses(String xml, Class... oClasses) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(oClasses);
        Unmarshaller u = ctx.createUnmarshaller();
        return u.unmarshal(new StringReader(xml));
    }

    public static Object deserialize(String xml) throws JAXBException{
        return arryCtx.createUnmarshaller().unmarshal(new StringReader(xml));
    }

    public static String serialize(Object obj) throws JAXBException {
        Class oClass = obj.getClass();
        StringWriter sw = new StringWriter(PAGESIZE);
        JAXBContext ctx = JAXBContext.newInstance(oClass);
        Marshaller m = ctx.createMarshaller();
        m.marshal(obj, sw);
        return sw.toString();
    }

    public static Class[] getObjClasses() {
        Class[] objClasses = new Class[] {
            org.openstack.atlas.restclients.dns.pub.objects.AsyncJobsStatus.class,
            org.openstack.atlas.restclients.dns.pub.objects.AsyncResponse.class,
            org.openstack.atlas.restclients.dns.pub.objects.BadRequest.class,
            org.openstack.atlas.restclients.dns.pub.objects.Change.class,
            org.openstack.atlas.restclients.dns.pub.objects.ChangeDetail.class,
            org.openstack.atlas.restclients.dns.pub.objects.DeleteFault.class,
            org.openstack.atlas.restclients.dns.pub.objects.DnsContentFileType.class,
            org.openstack.atlas.restclients.dns.pub.objects.DnsFault.class,
            org.openstack.atlas.restclients.dns.pub.objects.DnsStatus.class,
            org.openstack.atlas.restclients.dns.pub.objects.Domain.class,
            org.openstack.atlas.restclients.dns.pub.objects.Domains.class,
            org.openstack.atlas.restclients.dns.pub.objects.InternalServerError.class,
            org.openstack.atlas.restclients.dns.pub.objects.ItemAlreadyExists.class,
            org.openstack.atlas.restclients.dns.pub.objects.ItemNotFound.class,
            org.openstack.atlas.restclients.dns.pub.objects.Nameserver.class,
            org.openstack.atlas.restclients.dns.pub.objects.Nameservers.class,
            org.openstack.atlas.restclients.dns.pub.objects.OverLimit.class,
            org.openstack.atlas.restclients.dns.pub.objects.PublicRecordType.class,
            org.openstack.atlas.restclients.dns.pub.objects.Rdns.class,
            org.openstack.atlas.restclients.dns.pub.objects.Record.class,
            org.openstack.atlas.restclients.dns.pub.objects.RecordType.class,
            org.openstack.atlas.restclients.dns.pub.objects.RecordTypes.class,
            org.openstack.atlas.restclients.dns.pub.objects.RecordsList.class,
            org.openstack.atlas.restclients.dns.pub.objects.Unauthorized.class,
            org.openstack.atlas.restclients.dns.pub.objects.ValidationErrors.class,
            org.openstack.atlas.restclients.dns.pub.objects.ValidationFaults.class,
            org.openstack.atlas.restclients.dns.man.objects.Domain.class,
            org.openstack.atlas.restclients.dns.man.objects.DomainsWrapper.class,
            org.openstack.atlas.restclients.dns.man.objects.GroupLimit.class,
            org.openstack.atlas.restclients.dns.man.objects.GroupLimitType.class,
            org.openstack.atlas.restclients.dns.man.objects.GroupLimitTypes.class,
            org.openstack.atlas.restclients.dns.man.objects.GroupLimits.class,
            org.openstack.atlas.restclients.dns.man.objects.Record.class
        };
        return objClasses;
    }
}
