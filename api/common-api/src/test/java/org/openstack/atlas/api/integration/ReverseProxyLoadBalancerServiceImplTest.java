package org.openstack.atlas.api.integration;

import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import org.apache.axis.AxisFault;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;

public class ReverseProxyLoadBalancerServiceImplTest {

    public ReverseProxyLoadBalancerServiceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIsConnectionException() {
        List<Exception> exceptionsThatShouldMarkEndpointAsBad = new ArrayList<Exception>();
        exceptionsThatShouldMarkEndpointAsBad.add(new NoRouteToHostException());
        exceptionsThatShouldMarkEndpointAsBad.add(new PortUnreachableException());
        exceptionsThatShouldMarkEndpointAsBad.add(new BindException());
        exceptionsThatShouldMarkEndpointAsBad.add(new ConnectException());
        exceptionsThatShouldMarkEndpointAsBad.add(new SocketTimeoutException());

        for (Exception ex : exceptionsThatShouldMarkEndpointAsBad) {
            Exception axisFault = AxisFault.makeFault(ex);
            Exception zxtmException = new ZxtmRollBackException(ex);
            String excClassName = ex.getClass().getSimpleName();
            String expected = "Excpected isNetworkConnectionException(" + excClassName + ") == True but was False";
            assertTrue(expected, ReverseProxyLoadBalancerServiceImpl.isNetworkConnectionException(ex));
            assertTrue(expected, ReverseProxyLoadBalancerServiceImpl.isNetworkConnectionException(axisFault));
            assertTrue(expected, ReverseProxyLoadBalancerServiceImpl.isNetworkConnectionException(zxtmException));
        }

        List<Exception> exceptionsThatShouldBeIgnored = new ArrayList<Exception>();
        exceptionsThatShouldBeIgnored.add(new IOException());
        exceptionsThatShouldBeIgnored.add(new ArithmeticException());
        exceptionsThatShouldBeIgnored.add(new ArrayIndexOutOfBoundsException());
        exceptionsThatShouldBeIgnored.add(new IllegalArgumentException());
        exceptionsThatShouldBeIgnored.add(new RuntimeException());

        // Generic AxisFaults don't count either we only care about network errors
        exceptionsThatShouldBeIgnored.add(AxisFault.makeFault(new Exception()));

        // Don't care about generic zxtmRollBackExceptions either unless they wrap a network error
        exceptionsThatShouldBeIgnored.add(new ZxtmRollBackException(new Exception()));

        for (Exception ex : exceptionsThatShouldBeIgnored) {
            Exception axisFault = AxisFault.makeFault(ex);
            Exception zxtmException = new ZxtmRollBackException(ex);
            String excClassName = ex.getClass().getSimpleName();
            String expected = "Excpected isNetworkConnectionException(" + excClassName + ") == False but was True";
            assertFalse(ReverseProxyLoadBalancerServiceImpl.isNetworkConnectionException(ex));
            assertFalse(ReverseProxyLoadBalancerServiceImpl.isNetworkConnectionException(axisFault));
            assertFalse(ReverseProxyLoadBalancerServiceImpl.isNetworkConnectionException(zxtmException));
        }

    }
}
