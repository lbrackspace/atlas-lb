package org.rackspace.vtm.client.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.list.Child;
import org.rackspace.vtm.client.pool.Pool;
import org.rackspace.vtm.client.pool.PoolBasic;
import org.rackspace.vtm.client.pool.PoolProperties;

import javax.ws.rs.core.Response;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PoolITest extends VTMTestBase {
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
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void atestCreatePool() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Pool createdPool = client.createPool(vsName, pool);
        Assert.assertNotNull(createdPool);
        Pool retrievedPool = client.getPool(vsName);
        Assert.assertNotNull(retrievedPool);
    }

    /**
     * Tests the updating of a Pool
     * Verifies using a get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void btestUpdatePool() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        String updateNote = "qwertyuiop";
        pool.getProperties().getBasic().setNote(updateNote);
        Pool updatedPool = client.updatePool(vsName, pool);
        Assert.assertEquals(updateNote, updatedPool.getProperties().getBasic().getNote());
    }

    /**
     * Tests the retrieval of a list of Pools
     * Retrieves a list of action scripts and checks its size
     *
     * @throws VTMRestClientException
     *
     */
    @Test
    public void ctestGetListOfPools() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        List<Child> children = client.getPools();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Pool
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void dtestGetPool() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Pool retrievedPool = client.getPool(vsName);
        Assert.assertNotNull(retrievedPool);
    }

    /**
     * Tests the deletion of a Pool
     * Checks return of the delete call, and throws an error
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void etestDeletePool() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Response wasDeleted = client.deletePool(vsName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getPool(vsName);
    }

}
