package org.openstack.atlas.adapter.itest;


import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.entities.SessionPersistence;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

import javax.mail.Session;

public class UpdateSessionPersistenceITest extends STMTestBase {

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        removeLoadBalancer();
        stmClient.destroy();
    }

    @Test
    public void updateSessionPersistenceHTTP() throws InsufficientRequestException,
            StingrayRestClientObjectNotFoundException, StingrayRestClientException, StmRollBackException {
        StingrayRestClient client = new StingrayRestClient();
        lb.setSessionPersistence(SessionPersistence.HTTP_COOKIE);
        stmAdapter.updateLoadBalancer(config, lb, lb, null);

        Assert.assertEquals(SessionPersistence.HTTP_COOKIE.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistence_class());
    }

    @Test
    public void updateSessionPersistenceSourceIP() throws InsufficientRequestException,
            StingrayRestClientObjectNotFoundException, StingrayRestClientException, StmRollBackException {
        StingrayRestClient client = new StingrayRestClient();
        lb.setSessionPersistence(SessionPersistence.SOURCE_IP);
        stmAdapter.updateLoadBalancer(config, lb, lb, null);

        Assert.assertEquals(SessionPersistence.SOURCE_IP.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistence_class());
    }

    @Test
    public void updateSessionPersistenceSSLID() throws InsufficientRequestException,
            StingrayRestClientObjectNotFoundException, StingrayRestClientException, StmRollBackException {
        StingrayRestClient client = new StingrayRestClient();
        lb.setSessionPersistence(SessionPersistence.SSL_ID);
        stmAdapter.updateLoadBalancer(config, lb, lb, null);

        Assert.assertEquals(SessionPersistence.SSL_ID.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistence_class());
    }

    @Test
    public void removeSessionPersistenceHTTP() throws InsufficientRequestException,
            StingrayRestClientObjectNotFoundException, StingrayRestClientException, StmRollBackException {
        StingrayRestClient client = new StingrayRestClient();

        //Set as NONE
        lb.setSessionPersistence(SessionPersistence.HTTP_COOKIE);
        stmAdapter.updateLoadBalancer(config, lb, lb, null);
        Assert.assertEquals(SessionPersistence.HTTP_COOKIE.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistence_class());

        lb.setSessionPersistence(SessionPersistence.NONE);
        stmAdapter.updateLoadBalancer(config, lb, lb, null);
        Assert.assertEquals("", client.getPool(poolName()).getProperties().getBasic().getPersistence_class());

        //Set as Null
        lb.setSessionPersistence(SessionPersistence.HTTP_COOKIE);
        stmAdapter.updateLoadBalancer(config, lb, lb, null);
        Assert.assertEquals(SessionPersistence.HTTP_COOKIE.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistence_class());

        lb.setSessionPersistence(null);
        stmAdapter.updateLoadBalancer(config, lb, lb, null);
        Assert.assertEquals("", client.getPool(poolName()).getProperties().getBasic().getPersistence_class());
    }

    @Test
    public void removeSessionPersistenceSourceIP() throws InsufficientRequestException,
            StingrayRestClientObjectNotFoundException, StingrayRestClientException, StmRollBackException {
        StingrayRestClient client = new StingrayRestClient();

        //Set as NONE
        lb.setSessionPersistence(SessionPersistence.SOURCE_IP);
        stmAdapter.updateLoadBalancer(config, lb, lb, null);
        Assert.assertEquals(SessionPersistence.SOURCE_IP.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistence_class());

        lb.setSessionPersistence(SessionPersistence.NONE);
        stmAdapter.updateLoadBalancer(config, lb, lb, null);
        Assert.assertEquals("", client.getPool(poolName()).getProperties().getBasic().getPersistence_class());

        //Set as Null
        lb.setSessionPersistence(SessionPersistence.SOURCE_IP);
        stmAdapter.updateLoadBalancer(config, lb, lb, null);
        Assert.assertEquals(SessionPersistence.SOURCE_IP.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistence_class());

        lb.setSessionPersistence(null);
        stmAdapter.updateLoadBalancer(config, lb, lb, null);
        Assert.assertEquals("", client.getPool(poolName()).getProperties().getBasic().getPersistence_class());
    }

    @Test
    public void removeSessionPersistenceSSLID() throws InsufficientRequestException,
            StingrayRestClientObjectNotFoundException, StingrayRestClientException, StmRollBackException {
        StingrayRestClient client = new StingrayRestClient();

        //Set as NONE
        lb.setSessionPersistence(SessionPersistence.SSL_ID);
        stmAdapter.updateLoadBalancer(config, lb, lb, null);
        Assert.assertEquals(SessionPersistence.SSL_ID.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistence_class());

        lb.setSessionPersistence(SessionPersistence.NONE);
        stmAdapter.updateLoadBalancer(config, lb, lb, null);
        Assert.assertEquals("", client.getPool(poolName()).getProperties().getBasic().getPersistence_class());

        //Set as Null
        lb.setSessionPersistence(SessionPersistence.SSL_ID);
        stmAdapter.updateLoadBalancer(config, lb, lb, null);
        Assert.assertEquals(SessionPersistence.SSL_ID.name(),
                client.getPool(poolName()).getProperties().getBasic().getPersistence_class());

        lb.setSessionPersistence(null);
        stmAdapter.updateLoadBalancer(config, lb, lb, null);
        Assert.assertEquals("", client.getPool(poolName()).getProperties().getBasic().getPersistence_class());
    }
}
