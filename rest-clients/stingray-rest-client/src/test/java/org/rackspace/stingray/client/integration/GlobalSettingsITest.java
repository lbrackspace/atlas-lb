package org.rackspace.stingray.client.integration;


import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.settings.GlobalSettings;
import org.rackspace.stingray.client.settings.GlobalSettingsProperties;
import org.rackspace.stingray.client.settings.GlobalSettingsSsl;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GlobalSettingsITest extends StingrayTestBase {
    GlobalSettings globalSettings;
    GlobalSettingsProperties globalSettingsProperties;
    GlobalSettingsSsl globalSettingsSsl;
    String vsName;
    String ciphers;
    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        globalSettings = new GlobalSettings();
        globalSettingsProperties = new GlobalSettingsProperties();
        globalSettingsSsl = new GlobalSettingsSsl();
        globalSettingsProperties.setSsl(globalSettingsSsl);
        globalSettings.setProperties(globalSettingsProperties);
        vsName = TESTNAME;
        // If ciphers are updated this test will fail
        ciphers = "SSL_ECDHE_RSA_WITH_AES_256_GCM_SHA384, SSL_ECDHE_RSA_WITH_AES_128_GCM_SHA256, " +
                "SSL_ECDHE_RSA_WITH_AES_256_CBC_SHA384, SSL_ECDHE_RSA_WITH_AES_256_CBC_SHA, " +
                "SSL_ECDHE_RSA_WITH_AES_128_CBC_SHA256, SSL_ECDHE_RSA_WITH_AES_128_CBC_SHA, " +
                "SSL_RSA_WITH_AES_256_GCM_SHA384, SSL_RSA_WITH_AES_256_CBC_SHA256, " +
                "SSL_RSA_WITH_AES_256_CBC_SHA, SSL_RSA_WITH_AES_128_GCM_SHA256, " +
                "SSL_RSA_WITH_AES_128_CBC_SHA256, SSL_RSA_WITH_AES_128_CBC_SHA, SSL_RSA_WITH_3DES_EDE_CBC_SHA";
    }

    /**
     * Tests retrieval of global setting sslv3 ciphers
     * Verifies using get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void aTestRetrieveSslv3Ciphers() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        GlobalSettings globalSettings = client.getGlobalSettings();
        Assert.assertNotNull(globalSettings);
        Assert.assertEquals(ciphers, globalSettings.getProperties().getSsl().getSsl3Ciphers());
    }

}
