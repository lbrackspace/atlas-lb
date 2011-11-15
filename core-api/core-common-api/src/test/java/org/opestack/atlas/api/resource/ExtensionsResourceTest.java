package org.opestack.atlas.api.resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openstack.atlas.api.resource.ExtensionsResource;

import javax.ws.rs.core.Response;

@Ignore
public class ExtensionsResourceTest {
    private ExtensionsResource extensionsResource;

    @Before
    public void standUp() {
        extensionsResource = new ExtensionsResource();
    }

    @Test
    public void shouldReadXmlFromExtensionsXmlFile() {
        final Response response = extensionsResource.retrieveExtensionsAsXml();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldReadJsonFromExtensionsJsonFile() {
        final Response response = extensionsResource.retrieveExtensionsAsJson();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }
}
