package org.openstack.atlas.adapter.itest;


import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.service.domain.entities.SessionPersistence;
import org.rackspace.vtm.client.VTMRestClient;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;

import javax.mail.Session;

public class UpdateSessionPersistenceITest extends VTMTestBase {

    @BeforeClass
    public static void clientInit() {
        vtmClient = new VTMRestClient();
    }

    @Before
    public void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
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
    public void updateSessionPersistenceHTTP() throws InsufficientRequestException,
          VTMRestClientObjectNotFoundException, VTMRestClientException, StmRollBackException {
        VTMRestClient client = new VTMRestClient();
        lb.setSessionPersistence(SessionPersistence.HTTP_COOKIE);
        vtmAdapter.updateLoadBalancer(config, lb, lb);

        Assert.assertEquals(SessionPersistence.HTTP_COOKIE.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistenceClass());
    }

    @Test
    public void updateSessionPersistenceSourceIP() throws InsufficientRequestException,
            VTMRestClientObjectNotFoundException, VTMRestClientException, StmRollBackException {
        VTMRestClient client = new VTMRestClient();
        lb.setSessionPersistence(SessionPersistence.SOURCE_IP);
        vtmAdapter.updateLoadBalancer(config, lb, lb);

        Assert.assertEquals(SessionPersistence.SOURCE_IP.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistenceClass());
    }

    @Test
    public void updateSessionPersistenceSSLID() throws InsufficientRequestException,
            VTMRestClientObjectNotFoundException, VTMRestClientException, StmRollBackException {
        VTMRestClient client = new VTMRestClient();
        lb.setSessionPersistence(SessionPersistence.SSL_ID);
        vtmAdapter.updateLoadBalancer(config, lb, lb);

        Assert.assertEquals(SessionPersistence.SSL_ID.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistenceClass());
    }

    @Test
    public void removeSessionPersistenceHTTP() throws InsufficientRequestException,
            VTMRestClientObjectNotFoundException, VTMRestClientException, StmRollBackException {
        VTMRestClient client = new VTMRestClient();

        //Set as NONE
        lb.setSessionPersistence(SessionPersistence.HTTP_COOKIE);
        vtmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertEquals(SessionPersistence.HTTP_COOKIE.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistenceClass());

        lb.setSessionPersistence(SessionPersistence.NONE);
        vtmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertEquals("", client.getPool(poolName()).getProperties().getBasic().getPersistenceClass());

        //Set as Null
        lb.setSessionPersistence(SessionPersistence.HTTP_COOKIE);
        vtmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertEquals(SessionPersistence.HTTP_COOKIE.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistenceClass());

        lb.setSessionPersistence(null);
        vtmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertEquals("", client.getPool(poolName()).getProperties().getBasic().getPersistenceClass());
    }

    @Test
    public void removeSessionPersistenceSourceIP() throws InsufficientRequestException,
            VTMRestClientObjectNotFoundException, VTMRestClientException, StmRollBackException {
        VTMRestClient client = new VTMRestClient();

        //Set as NONE
        lb.setSessionPersistence(SessionPersistence.SOURCE_IP);
        vtmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertEquals(SessionPersistence.SOURCE_IP.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistenceClass());

        lb.setSessionPersistence(SessionPersistence.NONE);
        vtmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertEquals("", client.getPool(poolName()).getProperties().getBasic().getPersistenceClass());

        //Set as Null
        lb.setSessionPersistence(SessionPersistence.SOURCE_IP);
        vtmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertEquals(SessionPersistence.SOURCE_IP.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistenceClass());

        lb.setSessionPersistence(null);
        vtmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertEquals("", client.getPool(poolName()).getProperties().getBasic().getPersistenceClass());
    }

    @Test
    public void removeSessionPersistenceSSLID() throws InsufficientRequestException,
            VTMRestClientObjectNotFoundException, VTMRestClientException, StmRollBackException {
        VTMRestClient client = new VTMRestClient();

        //Set as NONE
        lb.setSessionPersistence(SessionPersistence.SSL_ID);
        vtmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertEquals(SessionPersistence.SSL_ID.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistenceClass());

        lb.setSessionPersistence(SessionPersistence.NONE);
        vtmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertEquals("", client.getPool(poolName()).getProperties().getBasic().getPersistenceClass());

        //Set as Null
        lb.setSessionPersistence(SessionPersistence.SSL_ID);
        vtmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertEquals(SessionPersistence.SSL_ID.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistenceClass());

        lb.setSessionPersistence(null);
        vtmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertEquals("", client.getPool(poolName()).getProperties().getBasic().getPersistenceClass());
    }
}
