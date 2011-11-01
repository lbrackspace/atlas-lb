package org.openstack.atlas.rax.api.mapper.dozer.converter;

import org.junit.Assert;
import org.junit.Test;
import org.openstack.atlas.api.v1.extensions.rax.AccessList;
import org.openstack.atlas.api.v1.extensions.rax.IpVersion;
import org.openstack.atlas.api.v1.extensions.rax.NetworkItemType;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class ExtensionObjectMapperTest {
    private LoadBalancer unMarshalledLb;

    public ExtensionObjectMapperTest() throws JAXBException {
        String lbRequestFile = "/rax-lb.xml";
        JAXBContext jaxbContext = JAXBContext.newInstance(org.openstack.atlas.core.api.v1.ObjectFactory.class.getPackage().getName());
        this.unMarshalledLb = (LoadBalancer) jaxbContext.createUnmarshaller().unmarshal(getClass().getResourceAsStream(lbRequestFile));
    }

    @Test
    public void shouldUnmarshallAccessListFromAniesList() throws JAXBException, ParserConfigurationException, IOException, SAXException {
        AccessList accessList = ExtensionObjectMapper.getAnyElement(unMarshalledLb.getAnies(), AccessList.class);

        Assert.assertNotNull(accessList);
        Assert.assertEquals("1.1.1.1", accessList.getNetworkItems().get(0).getAddress());
        Assert.assertEquals(IpVersion.IPV4, accessList.getNetworkItems().get(0).getIpVersion());
        Assert.assertEquals(NetworkItemType.DENY, accessList.getNetworkItems().get(0).getType());
    }

    @Test
    public void shouldUnmarshallCrazyNameFromOtherAttributes() {
        String crazyName = ExtensionObjectMapper.getOtherAttribute(unMarshalledLb.getOtherAttributes(), "crazyName");

        Assert.assertEquals("foo", crazyName);
    }
}
