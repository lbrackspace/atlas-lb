package org.openstack.atlas.rax.api.resources;

import org.openstack.atlas.docs.loadbalancers.api.extensions.v1.AccessList1;
import org.openstack.atlas.docs.loadbalancers.api.extensions.v1.NetworkItem1;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.extensions.v1.ObjectFactory;
import org.w3c.dom.Element;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoadBalancersResource extends org.openstack.atlas.api.resources.LoadBalancersResource {
    Logger logger = Logger.getLogger("LoadBalancersResource");

    @Override
    public Response createLoadBalancer(LoadBalancer loadBalancer) {
        logger.log(Level.INFO, "loadbalancer: " + loadBalancer);

        AccessList1 accessList1 = (AccessList1) getAnyElement(loadBalancer);
        if (accessList1 == null) {
            logger.log(Level.INFO, "No accesslist found");
        } else {
            for (NetworkItem1 networkItem1 : accessList1.getNetworkItem1()) {
                logger.log(Level.INFO, "Element Network Item1: " + networkItem1.getAddress() + " : " + networkItem1.getType1());
            }
        }
        //loadBalancer.getOtherAttributes().put(new QName("http://docs.openstack.org/test", "gender"), "male");

        Map<QName, String> otherAttributes = loadBalancer.getOtherAttributes();
        for (QName qname : otherAttributes.keySet()) {
            String value = otherAttributes.get(qname);
            String key = qname.getLocalPart();
            logger.log(Level.INFO, "Attribute: " + key + " : " + value);
        }
        return Response.status(Response.Status.OK).entity(loadBalancer).build();
    }

    private Object getAnyElement(LoadBalancer loadBalancer) {
        List<Object> anies = loadBalancer.getAnies();
        for (Object any : anies) {
            logger.log(Level.INFO, "Class: " + any.getClass());
            if (any instanceof Element) {
                Element element = (Element) any;
                try {
                    JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
                    Unmarshaller unmarshaller = jc.createUnmarshaller();
                    Object o = unmarshaller.unmarshal(element);
                    if (o instanceof AccessList1) {
                        AccessList1 list1 = (AccessList1) o;
                        List<NetworkItem1> ns = list1.getNetworkItem1();
                        return list1;
                    } else if (o instanceof JAXBElement) {
                        JAXBElement<AccessList1> jaxbElement = (JAXBElement) o;
                        AccessList1 list1 = jaxbElement.getValue();
                        List<NetworkItem1> ns = list1.getNetworkItem1();
                        return list1;
                    }
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
