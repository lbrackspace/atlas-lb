package org.openstack.atlas.restclients.auth.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.openstack.atlas.restclients.auth.util.IdentityUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest(IdentityUtil.class)
@PowerMockIgnore("javax.management.*")
public class IdentityUtilTest {

    @Mock
    private InputStream streamMock;

    @Before
    public void before() {
        initMocks(this);
    }

    @Test
    public void shouldLoadPropertiesFromClasspathTest() throws IOException {
        PowerMockito.mockStatic(IdentityUtil.class);
        PowerMockito.when(IdentityUtil.class.getResourceAsStream(
                "/identity.properties")).thenReturn(streamMock);

    }

    @Test
    public void shouldGetProperty() throws IOException {
        PowerMockito.mockStatic(IdentityUtil.class);
        PowerMockito.when(IdentityUtil.class.getResourceAsStream(
                "/identity.properties")).thenReturn(streamMock);
        PowerMockito.when(IdentityUtil.getProperty("key")).thenReturn("value");
        Assert.assertEquals("value", IdentityUtil.getProperty("key"));
    }

    @Test
    public void shouldGetPropertyWithDefaultValue() throws IOException {
        PowerMockito.mockStatic(IdentityUtil.class);
        PowerMockito.when(IdentityUtil.class.getResourceAsStream(
                "/identity.properties")).thenReturn(streamMock);
        PowerMockito.when(IdentityUtil.getProperty("key", "default")).thenReturn("default");
        Assert.assertEquals("default", IdentityUtil.getProperty("key", "default"));
    }

    @Test
    public void shouldGetIntProperty() throws Exception {
        PowerMockito.mockStatic(IdentityUtil.class);
        PowerMockito.when(IdentityUtil.class.getResourceAsStream(
                "/identity.properties")).thenReturn(streamMock);
        PowerMockito.when(IdentityUtil.getIntProperty("key")).thenReturn(1);
        Assert.assertEquals(1, IdentityUtil.getIntProperty("key"));
    }
}

