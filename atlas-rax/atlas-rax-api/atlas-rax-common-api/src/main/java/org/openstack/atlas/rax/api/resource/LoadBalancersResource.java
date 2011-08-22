package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.v1.extensions.rax.AccessList;
import org.openstack.atlas.api.v1.extensions.rax.NetworkItem;
import org.openstack.atlas.api.v1.extensions.rax.ObjectFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.rax.domain.entity.AccessListType;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.service.domain.operation.Operation;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.w3c.dom.Element;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller("RAX-LoadBalancersResource")
@Scope("request")
public class LoadBalancersResource extends org.openstack.atlas.api.resource.LoadBalancersResource {
    Logger logger = Logger.getLogger("LoadBalancersResource");

//    @Override
    public Response createLoadBalancer(LoadBalancer loadBalancer) {
        logger.log(Level.INFO, "loadbalancer: " + loadBalancer);

        AccessList accessList1 = (AccessList) getAnyElement(loadBalancer);
        Set<org.openstack.atlas.rax.domain.entity.AccessList> accessLists = new HashSet<org.openstack.atlas.rax.domain.entity.AccessList>();
        if (accessList1 == null) {
            logger.log(Level.INFO, "No accesslist found");
        } else {
            for (NetworkItem networkItem1 : accessList1.getNetworkItem()) {
                logger.log(Level.INFO, "Element Network Item1: " + networkItem1.getAddress() + " : " + networkItem1.getType1());
                org.openstack.atlas.rax.domain.entity.AccessList accessList = new org.openstack.atlas.rax.domain.entity.AccessList();
                accessList.setIpAddress(networkItem1.getAddress());
                accessList.setType(AccessListType.valueOf(networkItem1.getType1().value()));
                accessLists.add(accessList);
            }
        }
        //loadBalancer.getOtherAttributes().put(new QName("http://docs.openstack.org/test", "gender"), "male");

        String crazyName = "";
        Map<QName, String> otherAttributes = loadBalancer.getOtherAttributes();
        for (QName qname : otherAttributes.keySet()) {
            String value = otherAttributes.get(qname);
            String key = qname.getLocalPart();
            logger.log(Level.INFO, "Attribute: " + key + " : " + value);
            if(key.equalsIgnoreCase("crazyName")) {
                crazyName = value;
            }
        }

        ValidatorResult result = validator.validate(loadBalancer, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }

        try {
            org.openstack.atlas.service.domain.entity.LoadBalancer mappedLb = dozerMapper.map(loadBalancer, org.openstack.atlas.service.domain.entity.LoadBalancer.class);
            mappedLb.setAccountId(accountId);

            RaxLoadBalancer raxLoadBalancer = dozerMapper.map(mappedLb, RaxLoadBalancer.class);
            raxLoadBalancer.setAccessLists(accessLists);
            raxLoadBalancer.setCrazyName(crazyName);

            virtualIpService.addAccountRecord(accountId);

            org.openstack.atlas.service.domain.entity.LoadBalancer newlyCreatedLb = loadbalancerService.create(raxLoadBalancer);
            MessageDataContainer msg = new MessageDataContainer();
            msg.setLoadBalancer(newlyCreatedLb);
            asyncService.callAsyncLoadBalancingOperation(Operation.CREATE_LOADBALANCER, msg);
            return Response.status(Response.Status.ACCEPTED).entity(dozerMapper.map(newlyCreatedLb, LoadBalancer.class)).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
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
                    if (o instanceof AccessList) {
                        AccessList list1 = (AccessList) o;
                        List<NetworkItem> ns = list1.getNetworkItem();
                        return list1;
                    } else if (o instanceof JAXBElement) {
                        JAXBElement<AccessList> jaxbElement = (JAXBElement) o;
                        AccessList list1 = jaxbElement.getValue();
                        List<NetworkItem> ns = list1.getNetworkItem();
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
