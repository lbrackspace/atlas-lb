package org.rackspace.vtm.client.manager.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;


@RunWith(Enclosed.class)
public class VTMRequestManagerUtilTest {

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore("javax.management.*")
    public static class WhenHandlingRequest {

        final Response mockResponse = Mockito.mock(Response.class);


        private VTMRequestManagerUtil requestManagerUtil;

        @Before
        public void standUp() throws URISyntaxException, IOException {
            requestManagerUtil = new VTMRequestManagerUtil();
        }

        @Test
        public void shouldReturnATrueWhenResponseIsValid() {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(200);

            Assert.assertTrue(requestManagerUtil.isResponseValid(mockResponse));

        }

        @Test
        public void shouldReturnATrueWhenResponseIsOK() {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(202);

            Assert.assertTrue(requestManagerUtil.isResponseValid(mockResponse));

        }

        @Test
        public void shouldReturnAFalseWhenResponseIsInvalid() {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(500);

            Assert.assertFalse(requestManagerUtil.isResponseValid(mockResponse));

        }

        @Test(expected = VTMRestClientException.class)
        public void shouldThrowClientExceptionForGeneralException() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
            Mockito.when(this.mockResponse.readEntity(String.class)).thenThrow(Exception.class);
            requestManagerUtil.buildFaultMessage(this.mockResponse);
        }

        @Test(expected = VTMRestClientObjectNotFoundException.class)
        public void shouldThrowExceptionWhenObjectNotFound() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(404);
            Mockito.when(this.mockResponse.readEntity(String.class)).thenReturn("not found");
            requestManagerUtil.buildFaultMessage(this.mockResponse);
        }

        @Test(expected = VTMRestClientObjectNotFoundException.class)
        public void shouldThrowExceptionWhenInvalidURI() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(400);
            Mockito.when(this.mockResponse.readEntity(String.class)).thenReturn("Invalid resource URI");
            requestManagerUtil.buildFaultMessage(this.mockResponse);
        }

        @Test(expected = VTMRestClientObjectNotFoundException.class)
        public void shouldThrowExceptionWhenDoesNotExist() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(404);
            Mockito.when(this.mockResponse.readEntity(String.class)).thenReturn("does not exist");
            requestManagerUtil.buildFaultMessage(this.mockResponse);
        }

        @Test(expected = VTMRestClientException.class)
        public void shouldThrowExceptionWhenUncheckedException() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(500);
            Mockito.when(this.mockResponse.readEntity(String.class)).thenReturn("FAIL");
            requestManagerUtil.buildFaultMessage(this.mockResponse);
        }


    }
}
