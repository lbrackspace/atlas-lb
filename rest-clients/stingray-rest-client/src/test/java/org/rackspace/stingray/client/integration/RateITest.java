package org.rackspace.stingray.client.integration;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;
import org.rackspace.stingray.client.rate.Rate;
import org.rackspace.stingray.client.rate.RateBasic;
import org.rackspace.stingray.client.rate.RateProperties;

import java.util.List;

public class RateITest extends StingrayTestBase {
    StingrayRestClient client;
    String vsName;
    Rate rate;
    RateProperties rateProperties;
    RateBasic rateBasic;

    /**
     * Initializes variables prior to test execution
     */
    @Before
    public void standUp() {
        client = new StingrayRestClient();
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
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testCreateRate() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Rate createdRate = client.createRate(vsName, rate);
        Assert.assertNotNull(createdRate);
        Assert.assertEquals(createdRate, client.getRate(vsName));
    }

    /**
     * Tests the updating of a Rate
     * Verifies using a get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testUpdateRate() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        int updatePerMin = 17;
        rate.getProperties().getBasic().setMax_rate_per_minute(updatePerMin);
        Rate updatedRate = client.updateRate(vsName, rate);
        Assert.assertNotNull(updatedRate);
        int retrievedPerMin = updatedRate.getProperties().getBasic().getMax_rate_per_minute();
        Assert.assertEquals(updatePerMin, retrievedPerMin);
    }

    /**
     * Tests the retrieval of a list of Rates
     * Retrieves a list of action scripts and checks its size
     *
     * @throws org.rackspace.stingray.client.exception.StingrayRestClientException
     *
     */
    @Test
    public void testGetListOfRates() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getRates();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Rate
     * Retrieves the specific Action Script created earlier
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testGetRate() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Rate retrievedRate = client.getRate(vsName);
        Assert.assertNotNull(retrievedRate);
    }

    /**
     * Tests the deletion of a Rate
     * Checks return of the delete call, and throws an error
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testDeleteRate() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Boolean wasDeleted = client.deleteRate(vsName);
        Assert.assertTrue(wasDeleted);
        client.getRate(vsName);
    }
}
