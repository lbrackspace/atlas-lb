package org.rackspace.stingray.client._7.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.exception.VTMRestClientException;
import org.rackspace.stingray.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.stingray.client_7.pool.Pool;
import org.rackspace.stingray.client_7.pool.PoolBasic;
import org.rackspace.stingray.client_7.pool.PoolProperties;

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
