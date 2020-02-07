package org.rackspace.stingray.client._7.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.exception.VTMRestClientException;
import org.rackspace.stingray.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;
import org.rackspace.stingray.client.persistence.Persistence;
import org.rackspace.stingray.client.persistence.PersistenceBasic;
import org.rackspace.stingray.client.persistence.PersistenceProperties;

import javax.ws.rs.core.Response;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PersistenceITest extends VTMTestBase {
    Persistence persistence;
    PersistenceProperties persistenceProperties;
    PersistenceBasic persistenceBasic;
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
        persistenceProperties = new PersistenceProperties();
        persistenceBasic = new PersistenceBasic();

        persistenceProperties.setBasic(persistenceBasic);
        persistence.setProperties(persistenceProperties);
    }

    /**
     * Tests the creation of a Persistence
     * Verifies using get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void atestCreatePersistence() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Persistence createdPersistence = client.createPersistence(vsName, persistence);
        Assert.assertNotNull(createdPersistence);
        Assert.assertEquals(createdPersistence, client.getPersistence(vsName));
    }

    /**
     * Tests the updating of a Persistence
     * Verifies using a get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void btestUpdatePersistence() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        String updateNote = "qwertyuiop";
        persistence.getProperties().getBasic().setNote(updateNote);
        Persistence updatedPersistence = client.updatePersistence(vsName, persistence);
        Assert.assertEquals(updateNote, client.getPersistence(vsName).getProperties().getBasic().getNote());

    }


    /**
     * Tests the retrieval of a list of Persistences
     * Retrieves a list of action scripts and checks its size
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     *
     */
    @Test
    public void ctestGetListOfPersistences() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        List<Child> children = client.getPersistences();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Persistence
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void dtestGetPersistence() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Persistence retrievedPersistence = client.getPersistence(vsName);
        Assert.assertNotNull(retrievedPersistence);
    }

    /**
     * Tests the deletion of a Persistence
     * Checks return of the delete call, and throws an error
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void etestDeletePersistence() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Response wasDeleted = client.deletePersistence(vsName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getPersistence(vsName);
    }


}
