package org.rackspace.vtm.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.rackspace.vtm.client.bandwidth.Bandwidth;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.list.Child;
import org.rackspace.vtm.client.list.Children;
import org.rackspace.vtm.client.manager.VTMRequestManager;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class VTMRestClientTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenGettingAListOfItems {
        @Mock
        private VTMRequestManager requestManager;
        @Mock
        private Response mockedResponse;
        @InjectMocks
        private VTMRestClient vtmRestClient;

        @Before
        public void standUp()  throws VTMRestClientException, VTMRestClientObjectNotFoundException {
            vtmRestClient = new VTMRestClient();
            vtmRestClient.setRequestManager(requestManager);
            Children children = new Children();
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(children);
            when(requestManager.getList(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(mockedResponse);
        }

        @Test
        public void shouldReturnWhenPoolPathValid() throws Exception {
            List<Child> pools = vtmRestClient.getPools();

            Assert.assertNotNull(pools);
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class whenGettingAnItem {
        @Mock
        private VTMRequestManager requestManager;
        @InjectMocks
        private VTMRestClient vtmRestClient;
        @Mock
        private Response mockedResponse;
        private Bandwidth bandwidth;
        private String vsName;


        @Before
        public void standUp()  throws VTMRestClientException, VTMRestClientObjectNotFoundException {
            vtmRestClient.setRequestManager(requestManager);
            bandwidth = new Bandwidth();

            when(mockedResponse.readEntity(Bandwidth.class)).thenReturn(bandwidth);
            when(requestManager.getItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
        }


        @Test
        public void shouldReturnABandwidth() throws Exception {
            vsName = "12345_1234";
            Bandwidth bandwidth = vtmRestClient.getBandwidth(vsName);
            Assert.assertNotNull(bandwidth);
        }


    }


    @RunWith(MockitoJUnitRunner.class)
    public static class whenUpdatingAnItem {
        @Mock
        private VTMRequestManager requestManager;
        @InjectMocks
        private VTMRestClient vtmRestClient;
        @Mock
        private Response mockedResponse;
        private Bandwidth bandwidth;
        private String vsName;


        @Before
        public void standUp()  throws VTMRestClientException, VTMRestClientObjectNotFoundException {
            vtmRestClient.setRequestManager(requestManager);
            bandwidth = new Bandwidth();

            when(mockedResponse.readEntity(Bandwidth.class)).thenReturn(bandwidth);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
        }


        @Test
        public void shouldReturnABandwidth() throws Exception {
            vsName = "12345_1234";
            Bandwidth bandwidthTwo = vtmRestClient.updateBandwidth(vsName, bandwidth);
            Assert.assertNotNull(bandwidthTwo);
        }
    }



    @RunWith(MockitoJUnitRunner.class)
    public static class whenDeletingAnItem {
        @Mock
        private VTMRequestManager requestManager;
        @InjectMocks
        private VTMRestClient vtmRestClient;
        @Mock
        private Response mockedResponse;
        private Bandwidth bandwidth;
        private String vsName;


        @Before
        public void standUp()  throws VTMRestClientException, VTMRestClientObjectNotFoundException {
            vtmRestClient.setRequestManager(requestManager);
            bandwidth = new Bandwidth();
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
        }


        @Test
        public void shouldReturnABandwidth() throws Exception {
            vsName = "12345_1234";
            vtmRestClient.deletePool(vsName);
            Assert.assertTrue(true);

        }


    }


}