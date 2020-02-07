package org.rackspace.stingray.client.intergration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.pool.PoolBasic;
import org.rackspace.stingray.client.pool.PoolProperties;

import javax.ws.rs.core.Response;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PoolITest extends org.rackspace.stingray.client.intergration.StingrayTestBase {
    String vsName;
    Pool pool;
    PoolProperties poolProperties;
    PoolBasic poolBasic;

    /**
     * Initializes variables prior to test execution
     */
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
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void atestCreatePool() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
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
    public void btestUpdatePool() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        String updateNote = "qwertyuiop";
        pool.getProperties().getBasic().setNote(updateNote);
        Pool updatedPool = client.updatePool(vsName, pool);
        Assert.assertEquals(updateNote, updatedPool.getProperties().getBasic().getNote());
    }

    /**
     * Tests the retrieval of a list of Pools
     * Retrieves a list of action scripts and checks its size
     *
     * @throws StingrayRestClientException
     *
     */
    @Test
    public void ctestGetListOfPools() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
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
    public void dtestGetPool() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
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
    public void etestDeletePool() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Response wasDeleted = client.deletePool(vsName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getPool(vsName);
    }

}
