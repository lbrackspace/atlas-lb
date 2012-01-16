package org.openstack.atlas.rax.datamodel;

import org.openstack.atlas.api.v1.extensions.rax.ObjectFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public final class XmlHelper {

    public static Node marshall(Object objectToMarshall) throws ParserConfigurationException, JAXBException {
        Node rootNode = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        jaxbContext.createMarshaller().marshal(objectToMarshall, rootNode);
        Node objectNode = rootNode.getFirstChild();
        XmlHelper.setPrefixRecursively(objectNode, "rax");
        //XmlHelper.clearAttributes(objectNode);
        return objectNode;
    }

    private static void setPrefixRecursively(Node node, String prefix) {
        node.setPrefix(prefix);
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            setPrefixRecursively(node.getChildNodes().item(i), prefix);
        }
    }

    private static void clearAttributes(Node node) {
        final NamedNodeMap attributes = node.getAttributes();
        while (attributes.getLength() > 0) {
            attributes.removeNamedItem(attributes.item(0).getNodeName());
        }
    }
}
