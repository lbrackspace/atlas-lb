/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.adapter.helpers;

import java.net.SocketTimeoutException;
import java.net.ConnectException;
import java.net.BindException;
import java.net.PortUnreachableException;
import java.net.NoRouteToHostException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.apache.axis.AxisFault;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class IpHelperTest {

    public IpHelperTest() {
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
    public void testCreateZeusIpString() {
        assertEquals("192.168.3.51:80", IpHelper.createZeusIpString("192.168.3.51", 80));
        assertEquals("[ffff::ffff]:80", IpHelper.createZeusIpString("ffff::ffff", 80));
        assertEquals("www.google.com:80", IpHelper.createZeusIpString("www.google.com", 80));
    }

    @Test(expected = RuntimeException.class)
    public void shouldPukeOnNullIp() {
        IpHelper.createZeusIpString(null, 80);
    }

    @Test(expected = RuntimeException.class)
    public void shouldPukeOnNullPort() {
        IpHelper.createZeusIpString("www.google.com", null);
    }

    @Test(expected = RuntimeException.class)
    public void shouldPukeOnNullPortAndNullIp() {
        IpHelper.createZeusIpString(null, null);
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
            assertTrue(expected, IpHelper.isNetworkConnectionException(ex));
            assertTrue(expected, IpHelper.isNetworkConnectionException(axisFault));
            assertTrue(expected, IpHelper.isNetworkConnectionException(zxtmException));
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
            assertFalse(IpHelper.isNetworkConnectionException(ex));
            assertFalse(IpHelper.isNetworkConnectionException(axisFault));
            assertFalse(IpHelper.isNetworkConnectionException(zxtmException));
        }

    }

    @Test(expected = ArithmeticException.class)
    public void shouldPukeWhenDividingByZeroLOL() {
        int a = 1 / 0;
    }

    @Test
    public void yetSeemsPerfectlyFineWhenDoingFloatingPointDivisionByZero() {
        double x;

        x = 1.0 / 0.0;
        assertEquals(Double.POSITIVE_INFINITY, x, 0.0); // AKA +Inf

        x = -1.0 / 0.0;
        assertEquals(Double.NEGATIVE_INFINITY, x, 0.0); // Aka -Inf

        x = 0.0 / 0.0;
        assertEquals(Double.NaN, x, 0.0); // Aka NaN
    }

    @Test
    public void seemsPowBehavesOddlyToo() {
        assertEquals(0.0, Math.pow(0.0, 3.0), 0.0); // Makes sense
        assertEquals(0.0, Math.pow(0.0, 2.0), 0.0); // Makes sense
        assertEquals(0.0, Math.pow(0.0, 1.0), 0.0); // Makes sense
        assertEquals(1.0, Math.pow(0.0, 0.0), 0.0); // Huh?

        assertEquals(Double.POSITIVE_INFINITY, Math.pow(0.0, -1.0), 0.0); // LOL
        assertEquals(Double.POSITIVE_INFINITY, Math.pow(0.0, -2.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, Math.pow(0.0, -2.0), 0.0);

        assertEquals(Double.NEGATIVE_INFINITY, Math.pow(-0.0, -3.0), 0.0);// Were Negative Infinity
        assertEquals(Double.POSITIVE_INFINITY, Math.pow(-0.0, -2.0), 0.0); // Back to Positive
        assertEquals(Double.NEGATIVE_INFINITY, Math.pow(-0.0, -1.0), 0.0); // Negative Again
        assertEquals(1.0, Math.pow(-0.0, -0.0), 0.0); // Back to 1.0
    }
}
