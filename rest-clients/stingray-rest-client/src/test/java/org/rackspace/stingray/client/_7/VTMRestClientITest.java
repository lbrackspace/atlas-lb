package org.rackspace.stingray.client._7;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.bandwidth.Bandwidth;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;
import org.rackspace.stingray.client.list.Children;
import org.rackspace.stingray.client.manager.RequestManager;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class VTMRestClientITest {

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenGettingAListOfItems {
        @Mock
        private RequestManager requestManager;
        @Mock
        private Response mockedResponse;
        @InjectMocks
        private StingrayRestClient stingrayRestClient;

        @Before
        public void standUp()  throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            stingrayRestClient = new StingrayRestClient();
            stingrayRestClient.setRequestManager(requestManager);
            Children children = new Children();
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(children);
            when(requestManager.getList(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(mockedResponse);
        }

        @Test
        public void shouldReturnWhenPoolPathValid() throws Exception {
            List<Child> pools = stingrayRestClient.getPools();

            Assert.assertNotNull(pools);
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class whenGettingAnItem {
        @Mock
        private RequestManager requestManager;
        @InjectMocks
        private StingrayRestClient stingrayRestClient;
        @Mock
        private Response mockedResponse;
        private Bandwidth bandwidth;
        private String vsName;


        @Before
        public void standUp()  throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            stingrayRestClient.setRequestManager(requestManager);
            bandwidth = new Bandwidth();

            when(mockedResponse.readEntity(Bandwidth.class)).thenReturn(bandwidth);
            when(requestManager.getItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
        }


        @Test
        public void shouldReturnABandwidth() throws Exception {
            vsName = "12345_1234";
            Bandwidth bandwidth = stingrayRestClient.getBandwidth(vsName);
            Assert.assertNotNull(bandwidth);
        }


    }


    @RunWith(MockitoJUnitRunner.class)
    public static class whenUpdatingAnItem {
        @Mock
        private RequestManager requestManager;
        @InjectMocks
        private StingrayRestClient stingrayRestClient;
        @Mock
        private Response mockedResponse;
        private Bandwidth bandwidth;
        private String vsName;


        @Before
        public void standUp()  throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            stingrayRestClient.setRequestManager(requestManager);
            bandwidth = new Bandwidth();

            when(mockedResponse.readEntity(Bandwidth.class)).thenReturn(bandwidth);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
        }


        @Test
        public void shouldReturnABandwidth() throws Exception {
            vsName = "12345_1234";
            Bandwidth bandwidthTwo = stingrayRestClient.updateBandwidth(vsName, bandwidth);
            Assert.assertNotNull(bandwidthTwo);
        }
    }



    @RunWith(MockitoJUnitRunner.class)
    public static class whenDeletingAnItem {
        @Mock
        private RequestManager requestManager;
        @InjectMocks
        private StingrayRestClient stingrayRestClient;
        @Mock
        private Response mockedResponse;
        private Bandwidth bandwidth;
        private String vsName;


        @Before
        public void standUp()  throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            stingrayRestClient.setRequestManager(requestManager);
            bandwidth = new Bandwidth();
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
        }


        @Test
        public void shouldReturnABandwidth() throws Exception {
            vsName = "12345_1234";
            stingrayRestClient.deletePool(vsName);
            Assert.assertTrue(true);

        }


    }


}