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
import org.rackspace.vtm.client.location.Location;
import org.rackspace.vtm.client.location.LocationBasic;
import org.rackspace.vtm.client.location.LocationProperties;

import javax.ws.rs.core.Response;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LocationITest extends VTMTestBase {
    Location location;
    LocationProperties locationProperties;
    LocationBasic locationBasic;
    String vsName;
    int locationId;


    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        location = new Location();
        locationProperties = new LocationProperties();
        locationBasic = new LocationBasic();
        locationId = 20;
        vsName = TESTNAME;
        locationBasic.setId(locationId);
        locationProperties.setBasic(locationBasic);
        location.setProperties(locationProperties);

    }

    /**
     * Tests the creation of a Location
     * Verifies using get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void atestCreateLocation() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Location createdLocation = client.createLocation(vsName, location);
        Assert.assertNotNull(createdLocation);
        Assert.assertEquals(createdLocation, client.getLocation(vsName));
    }

    /**
     * Tests the updating of a Location
     * Verifies using a get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void btestUpdateLocation() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        int updateId = 33;
        location.getProperties().getBasic().setId(updateId);
        Location updatedLocation = client.updateLocation(vsName, location);
        Assert.assertEquals(updateId, (int) updatedLocation.getProperties().getBasic().getId());
    }


    /**
     * Tests the retrieval of a list of Locations
     * Retrieves a list of action scripts and checks its size
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     *
     */
    @Test
    public void ctestGetListOfLocations() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        List<Child> children = client.getLocations();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Location
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void dtestGetLocation() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Location retrievedLocation = client.getLocation(vsName);
        Assert.assertNotNull(retrievedLocation);
    }

    /**
     * Tests the deletion of a Location
     * Checks return of the delete call, and throws an error
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void etestDeleteLocation() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Response wasDeleted = client.deleteLocation(vsName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getLocation(vsName);

    }
}
