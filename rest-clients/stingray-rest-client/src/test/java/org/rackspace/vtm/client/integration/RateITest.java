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
import org.rackspace.vtm.client.rate.Rate;
import org.rackspace.vtm.client.rate.RateBasic;
import org.rackspace.vtm.client.rate.RateProperties;
import javax.ws.rs.core.Response;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RateITest extends VTMTestBase {
    String vsName;
    Rate rate;
    RateProperties rateProperties;
    RateBasic rateBasic;

    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        vsName = TESTNAME;
        rate = new Rate();
        rateProperties = new RateProperties();
        rateBasic = new RateBasic();

        rateProperties.setBasic(rateBasic);
        rate.setProperties(rateProperties);

    }

    /**
     * Tests the creation of a Rate
     * Verifies using get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void atestCreateRate() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Rate createdRate = client.createRate(vsName, rate);
        Assert.assertNotNull(createdRate);
        Assert.assertEquals(createdRate, client.getRate(vsName));
    }

    /**
     * Tests the updating of a Rate
     * Verifies using a get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void btestUpdateRate() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        int updatePerMin = 17;
        rate.getProperties().getBasic().setMaxRatePerMinute(updatePerMin);
        Rate updatedRate = client.updateRate(vsName, rate);
        Assert.assertNotNull(updatedRate);
        int retrievedPerMin = updatedRate.getProperties().getBasic().getMaxRatePerMinute();
        Assert.assertEquals(updatePerMin, retrievedPerMin);
    }

    /**
     * Tests the retrieval of a list of Rates
     * Retrieves a list of action scripts and checks its size
     *
     * @throws VTMRestClientException
     *
     */
    @Test
    public void ctestGetListOfRates() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        List<Child> children = client.getRates();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Rate
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void dtestGetRate() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Rate retrievedRate = client.getRate(vsName);
        Assert.assertNotNull(retrievedRate);
    }

    /**
     * Tests the deletion of a Rate
     * Checks return of the delete call, and throws an error
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void etestDeleteRate() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Response wasDeleted = client.deleteRate(vsName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getRate(vsName);
    }
}
