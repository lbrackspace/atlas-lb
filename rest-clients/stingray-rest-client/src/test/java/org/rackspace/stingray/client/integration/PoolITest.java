package org.rackspace.stingray.client.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.pojo.list.Child;
import org.rackspace.stingray.pojo.pool.Pool;
import org.rackspace.stingray.pojo.pool.Basic;
import org.rackspace.stingray.pojo.pool.Properties;

import java.util.List;

public class PoolITest extends StingrayTestBase {
    String vsName;
    Pool pool;
    Properties poolProperties;
    Basic poolBasic;

    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        vsName = TESTNAME;
        pool = new Pool();
        poolProperties = new Properties();
        poolBasic = new Basic();

        poolProperties.setBasic(poolBasic);
        pool.setProperties(poolProperties);


    }


    /**
     * Tests the creation of a Pool
     * Verifies using get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testCreatePool() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Pool createdPool = client.createPool(vsName, pool);
        Assert.assertNotNull(createdPool);
        Pool retrievedPool = client.getPool(vsName);
        Assert.assertNotNull(retrievedPool);
    }

    /**
     * Tests the updating of a Pool
     * Verifies using a get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testUpdatePool() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        String updateNote = "qwertyuiop";
        pool.getProperties().getBasic().setNote(updateNote);
        Pool updatedPool = client.updatePool(vsName, pool);
        Assert.assertEquals(updateNote, updatedPool.getProperties().getBasic().getNote());
    }

    /**
     * Tests the retrieval of a list of Pools
     * Retrieves a list of action scripts and checks its size
     *
     * @throws org.rackspace.stingray.client.exception.StingrayRestClientException
     *
     */
    @Test
    public void testGetListOfPools() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getPools();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Pool
     * Retrieves the specific Action Script created earlier
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testGetPool() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Pool retrievedPool = client.getPool(vsName);
        Assert.assertNotNull(retrievedPool);
    }

    /**
     * Tests the deletion of a Pool
     * Checks return of the delete call, and throws an error
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testDeletePool() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Boolean wasDeleted = client.deletePool(vsName);
        Assert.assertTrue(wasDeleted);
        client.getPool(vsName);
    }

}
