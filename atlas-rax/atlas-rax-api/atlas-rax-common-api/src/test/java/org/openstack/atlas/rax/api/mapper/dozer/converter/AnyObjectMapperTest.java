package org.openstack.atlas.rax.api.mapper.dozer.converter;

import org.junit.Test;
import org.junit.Assert;
import org.openstack.atlas.api.v1.extensions.rax.AccessList;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class AnyObjectMapperTest {

    private JAXBContext jaxbContext;
    private LoadBalancer unMarshaledLb;
    private Node node = null;

    public AnyObjectMapperTest() throws JAXBException {
        String lbRequestFile = "/rax-lb.xml";
        this.jaxbContext = JAXBContext.newInstance(org.openstack.atlas.core.api.v1.ObjectFactory.class.getPackage().getName());
        this.unMarshaledLb = (LoadBalancer) jaxbContext.createUnmarshaller().unmarshal(getClass().getResourceAsStream(lbRequestFile));
    }

    @Test
    public void shouldUnmarshalAccessList() throws JAXBException, ParserConfigurationException, IOException, SAXException {
        AccessList accessList = AnyObjectMapper.getAnyElement(unMarshaledLb.getAnies(), AccessList.class);

        Assert.assertNotNull(accessList);
/*        node = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .newDocument();


        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        jaxbContext.createMarshaller().marshal(unMarshaledLb, node);

        System.out.println(node.getFirstChild());

        unMarshaledLb = (LoadBalancer) jaxbContext.createUnmarshaller().unmarshal(node.getFirstChild());
        System.out.println(unMarshaledLb);*/
    }
}
