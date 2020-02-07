package org.rackspace.vtm.client.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.pool.Pool;
import org.rackspace.vtm.client.pool.PoolBasic;
import org.rackspace.vtm.client.pool.PoolProperties;

public class ClientExceptionITest extends VTMTestBase {
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
     * @throws VTMRestClientException,
     *          VTMRestClientObjectNotFoundException
     */
    @Test
    public void testCreatePool() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        pool.getProperties().setHttp(null);
        Pool createdPool = client.createPool(vsName, pool);
        Assert.assertNotNull(createdPool);
        Pool retrievedPool = client.getPool(vsName);
        Assert.assertNotNull(retrievedPool);
        client.deletePool(vsName);
    }


}
