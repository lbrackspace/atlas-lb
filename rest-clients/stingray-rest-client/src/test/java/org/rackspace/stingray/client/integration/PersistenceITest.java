package org.rackspace.stingray.client.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.pojo.list.Child;
import org.rackspace.stingray.pojo.persistence.Persistence;
import org.rackspace.stingray.pojo.persistence.Basic;
import org.rackspace.stingray.pojo.persistence.Properties;

import java.util.List;

public class PersistenceITest extends StingrayTestBase {
    Persistence persistence;
    Properties persistenceProperties;
    Basic persistenceBasic;
    String vsName;

    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        vsName = TESTNAME;
        persistence = new Persistence();
        persistenceProperties = new Properties();
        persistenceBasic = new Basic();

        persistenceProperties.setBasic(persistenceBasic);
        persistence.setProperties(persistenceProperties);
    }

    /**
     * Tests the creation of a Persistence
     * Verifies using get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testCreatePersistence() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Persistence createdPersistence = client.createPersistence(vsName, persistence);
        Assert.assertNotNull(createdPersistence);
        Assert.assertEquals(createdPersistence, client.getPersistence(vsName));
    }

    /**
     * Tests the updating of a Persistence
     * Verifies using a get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testUpdatePersistence() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        String updateNote = "qwertyuiop";
        persistence.getProperties().getBasic().setNote(updateNote);
        Persistence updatedPersistence = client.updatePersistence(vsName, persistence);
        Assert.assertEquals(updateNote, client.getPersistence(vsName).getProperties().getBasic().getNote());

    }


    /**
     * Tests the retrieval of a list of Persistences
     * Retrieves a list of action scripts and checks its size
     *
     * @throws org.rackspace.stingray.client.exception.StingrayRestClientException
     *
     */
    @Test
    public void testGetListOfPersistences() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getPersistences();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Persistence
     * Retrieves the specific Action Script created earlier
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testGetPersistence() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Persistence retrievedPersistence = client.getPersistence(vsName);
        Assert.assertNotNull(retrievedPersistence);
    }

    /**
     * Tests the deletion of a Persistence
     * Checks return of the delete call, and throws an error
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testDeletePersistence() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Boolean wasDeleted = client.deletePersistence(vsName);
        Assert.assertTrue(wasDeleted);
        client.getPersistence(vsName);
    }


}
