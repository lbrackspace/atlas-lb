package org.openstack.atlas.rax.api.mapper.dozer.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dozer.CustomConverter;
import org.openstack.atlas.api.v1.extensions.rax.NetworkItem;
import org.openstack.atlas.api.v1.extensions.rax.NetworkItemType;
import org.openstack.atlas.api.v1.extensions.rax.ObjectFactory;
import org.openstack.atlas.rax.domain.entity.AccessList;
import org.openstack.atlas.rax.domain.entity.AccessListType;
import org.openstack.atlas.service.domain.entity.IpVersion;
import org.openstack.atlas.service.domain.exception.NoMappableConstantException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccessListConverter implements CustomConverter {
    private static Log LOG = LogFactory.getLog(AccessListConverter.class.getName());

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class<?> destinationClass, Class<?> sourceClass) {

        if (sourceFieldValue == null) {
            return null;
        }

        if (destinationClass == List.class && sourceFieldValue instanceof java.util.Set) {
            final Set<AccessList> accessListSet = (Set<AccessList>) sourceFieldValue;
            List<Object> anies = (List<Object>) existingDestinationFieldValue;
            if (anies == null) anies = new ArrayList<Object>();

            try {
                Node node = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

                org.openstack.atlas.api.v1.extensions.rax.AccessList dataModelAccessList = new org.openstack.atlas.api.v1.extensions.rax.AccessList();
                for (org.openstack.atlas.rax.domain.entity.AccessList accessList : accessListSet) {
                    NetworkItem networkItem = new NetworkItem();
                    networkItem.setId(accessList.getId());
                    networkItem.setAddress(accessList.getIpAddress());
                    networkItem.setIpVersion(org.openstack.atlas.api.v1.extensions.rax.IpVersion.fromValue(accessList.getIpVersion().name()));
                    networkItem.setType(NetworkItemType.fromValue(accessList.getType().name()));
                    dataModelAccessList.getNetworkItems().add(networkItem);
                }

                jaxbContext.createMarshaller().marshal(dataModelAccessList, node);
                Node accessListNode = node.getFirstChild();
                setPrefixRecursively(accessListNode, "rax");
                clearAttributes(accessListNode);
                anies.add(accessListNode);
            } catch (Exception e) {
                LOG.error("Error converting accessList from domain to data model", e);
            }

            return anies;
        }

        if (destinationClass == Set.class) {
            Set<org.openstack.atlas.rax.domain.entity.AccessList> accessLists = new HashSet<AccessList>();
            org.openstack.atlas.api.v1.extensions.rax.AccessList _accessList = AnyObjectMapper.getAnyElement((List<Object>) sourceFieldValue, org.openstack.atlas.api.v1.extensions.rax.AccessList.class);

            if (_accessList == null) return null;

            for (NetworkItem networkItem : _accessList.getNetworkItems()) {
                AccessList accessList = new AccessList();
                accessList.setId(networkItem.getId());
                accessList.setIpAddress(networkItem.getAddress());
                accessList.setIpVersion(IpVersion.valueOf(networkItem.getIpVersion().name()));
                accessList.setType(AccessListType.valueOf(networkItem.getType().name()));
                accessLists.add(accessList);
            }

            return accessLists;
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }

    private void setPrefixRecursively(Node node, String prefix) {
        node.setPrefix(prefix);
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            setPrefixRecursively(node.getChildNodes().item(i), prefix);
        }
    }

    private void clearAttributes(Node node) {
        final NamedNodeMap attributes = node.getAttributes();
        while (attributes.getLength() > 0) {
            attributes.removeNamedItem(attributes.item(0).getNodeName());
        }
    }
}
