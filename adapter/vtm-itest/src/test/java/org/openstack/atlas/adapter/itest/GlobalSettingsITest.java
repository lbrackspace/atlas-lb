package org.openstack.atlas.adapter.itest;


import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.rackspace.vtm.client.VTMRestClient;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;

import java.rmi.RemoteException;

public class GlobalSettingsITest extends VTMTestBase {


    @BeforeClass
    public static void clientInit() {
        vtmClient = new VTMRestClient();
    }

    @Before
    public void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
    }

    @After
    public void destroy() {
        removeLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        teardownEverything();
    }

    @Test
    public void getGlobalCiphers() throws InsufficientRequestException, VTMRestClientObjectNotFoundException, VTMRestClientException, RollBackException, RemoteException {
        // Matches engineer defined cipher suites. Will need updating when cipher added
        String ciphers = "SSL_ECDHE_RSA_WITH_AES_256_GCM_SHA384, SSL_ECDHE_RSA_WITH_AES_128_GCM_SHA256, " +
                "SSL_ECDHE_RSA_WITH_AES_256_CBC_SHA384, SSL_ECDHE_RSA_WITH_AES_256_CBC_SHA, " +
                "SSL_ECDHE_RSA_WITH_AES_128_CBC_SHA256, SSL_ECDHE_RSA_WITH_AES_128_CBC_SHA, " +
                "SSL_RSA_WITH_AES_256_GCM_SHA384, SSL_RSA_WITH_AES_256_CBC_SHA256, SSL_RSA_WITH_AES_256_CBC_SHA, " +
                "SSL_RSA_WITH_AES_128_GCM_SHA256, SSL_RSA_WITH_AES_128_CBC_SHA256, SSL_RSA_WITH_AES_128_CBC_SHA, " +
                "SSL_RSA_WITH_3DES_EDE_CBC_SHA";

        String s = vtmAdapter.getSsl3Ciphers(config);
        Assert.assertNotNull(s);
        Assert.assertEquals(ciphers, s);
    }
}
