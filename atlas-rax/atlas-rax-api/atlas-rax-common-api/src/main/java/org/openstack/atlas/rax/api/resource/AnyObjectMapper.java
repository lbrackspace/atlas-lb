package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.v1.extensions.rax.AccessList;
import org.openstack.atlas.api.v1.extensions.rax.NetworkItem;
import org.openstack.atlas.api.v1.extensions.rax.ObjectFactory;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnyObjectMapper {
    private static final Logger LOG = Logger.getLogger("AnyObjectMapper");

    public static AccessList getAccessList(LoadBalancer loadBalancer) {
        Object o = getAnyElement(loadBalancer);
        if (o instanceof AccessList) {
            AccessList list = (AccessList) o;
            List<NetworkItem> ns = list.getNetworkItems();
            return list;
        } else if (o instanceof JAXBElement) {
            JAXBElement<AccessList> jaxbElement = (JAXBElement) o;
            AccessList list = jaxbElement.getValue();
            List<NetworkItem> ns = list.getNetworkItems();
            return list;
        }
        return null;
    }

    public static String getCrazyName(LoadBalancer loadBalancer) {
        String crazyName = "";
        Map<QName, String> otherAttributes = loadBalancer.getOtherAttributes();
        for (QName qname : otherAttributes.keySet()) {
            String value = otherAttributes.get(qname);
            String key = qname.getLocalPart();
            LOG.log(Level.INFO, "Attribute: " + key + " : " + value);
            if (key.equalsIgnoreCase("crazyName")) {
                crazyName = value;
            }
        }
        return crazyName;
    }

    private static Object getAnyElement(LoadBalancer loadBalancer) {
        List<Object> anies = loadBalancer.getAnies();
        for (Object any : anies) {
            LOG.log(Level.INFO, "Class: " + any.getClass());
            if (any instanceof Element) {
                Element element = (Element) any;
                try {
                    JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
                    Unmarshaller unmarshaller = jc.createUnmarshaller();
                    Object o = unmarshaller.unmarshal(element);
                    return o;
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
