package org.rackspace.stingray.client.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.pool.PoolBasic;
import org.rackspace.stingray.client.pool.PoolProperties;

import javax.ws.rs.core.Response;

public class ClientExceptionITest extends StingrayTestBase {
    //Verify marshaling of all exception messages...
    String vsName;
    Pool pool;
    PoolProperties poolProperties;
    PoolBasic poolBasic;

    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        vsName = TESTNAME;
        pool = new Pool();
        poolProperties = new PoolProperties();
        poolBasic = new PoolBasic();

        poolProperties.setBasic(poolBasic);
        pool.setProperties(poolProperties);

    }


    /**
     * Tests the creation of a Pool
     * Verifies using get and a comparison of content contained
     *
     * @throws StingrayRestClientException,
     *          StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testCreatePool() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        pool.getProperties().setHttp(null);
        Pool createdPool = client.createPool(vsName, pool);
        Assert.assertNotNull(createdPool);
        Pool retrievedPool = client.getPool(vsName);
        Assert.assertNotNull(retrievedPool);
        client.deletePool(vsName);
    }


    /**
     * Tests that StingrayRestClientException is thrown.
     * Checks to make sure StingrayRestClientFoundException is thrown by failing to createPool.
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientException.class)
    public void testThrowStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        pool.setProperties(null);
        Pool createdPool = client.createPool(vsName, pool);
        Assert.assertNull(createdPool);
    }

    /**
     * Tests the deletion of a Pool
     * Checks return of the delete call, and checks to make sure StingrayRestClientObjectNotFoundException is thrown.
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void etestDeletePool() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Response wasDeleted = client.deletePool(vsName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getPool(vsName);
    }

}
