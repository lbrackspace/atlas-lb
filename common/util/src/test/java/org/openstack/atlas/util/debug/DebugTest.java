package org.openstack.atlas.util.debug;

import org.openstack.atlas.util.debug.exceptions.TestBaseException;
import org.openstack.atlas.util.debug.exceptions.TestSomeOtherException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.security.GeneralSecurityException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.util.debug.exceptions.TestChildException;
import org.openstack.atlas.util.debug.exceptions.TestGrandChildException;
import org.openstack.atlas.util.debug.exceptions.TestGreatGrandChildException;
import static org.junit.Assert.*;

public class DebugTest {

    public DebugTest() {
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
    public void testIsThrowableCausedByOrAssignableFrom() {
        Exception someException = new TestSomeOtherException(new IOException(new TestGrandChildException(new GeneralSecurityException(new IllegalArgumentException(new InstantiationException())))));
        String excMessage = Debug.getEST(someException);
        try {
            throw someException;
        } catch (Exception ex) {
            assertEquals(null, Debug.getThrowableCausedByOrAssignableFrom(someException, NullPointerException.class, IllegalThreadStateException.class));
            assertFalse(Debug.isThrowableCausedByOrAssignableFrom(someException, NullPointerException.class, IllegalThreadStateException.class));

            assertEquals(null, Debug.getThrowableCausedByOrAssignableFrom(someException, ArrayIndexOutOfBoundsException.class, StringIndexOutOfBoundsException.class));
            assertFalse(Debug.isThrowableCausedByOrAssignableFrom(someException, ArrayIndexOutOfBoundsException.class, StringIndexOutOfBoundsException.class));

            assertEquals(TestGrandChildException.class, Debug.getThrowableCausedByOrAssignableFrom(ex, TestGrandChildException.class, NullPointerException.class));
            assertTrue(Debug.isThrowableCausedByOrAssignableFrom(ex, TestGrandChildException.class, NullPointerException.class));

            assertEquals(TestBaseException.class, Debug.getThrowableCausedByOrAssignableFrom(ex, TestBaseException.class, NullPointerException.class));
            assertTrue(Debug.isThrowableCausedByOrAssignableFrom(ex, TestBaseException.class, NullPointerException.class));

            assertEquals(null, Debug.getThrowableCausedByOrAssignableFrom(ex, TestGreatGrandChildException.class));
            assertFalse(Debug.isThrowableCausedByOrAssignableFrom(ex, TestGreatGrandChildException.class));


            assertFalse(Debug.isThrowableCausedByOrAssignableFrom(ex, IllegalThreadStateException.class));
            assertFalse(Debug.isThrowableCausedByOrAssignableFrom(ex, ArithmeticException.class));
            assertFalse(Debug.isThrowableCausedByOrAssignableFrom(ex, NumberFormatException.class));

            assertTrue(Debug.isThrowableCausedByOrAssignableFrom(ex, IOException.class));
            assertTrue(Debug.isThrowableCausedByOrAssignableFrom(ex, Exception.class));
            assertTrue(Debug.isThrowableCausedByOrAssignableFrom(ex,RuntimeException.class));

            assertTrue(Debug.isThrowableCausedByOrAssignableFrom(ex, TestChildException.class));
            assertTrue(Debug.isThrowableCausedByOrAssignableFrom(ex, TestGrandChildException.class));
            assertTrue(Debug.isThrowableCausedByOrAssignableFrom(ex,Throwable.class));
            assertFalse(Debug.isThrowableCausedByOrAssignableFrom(ex, Error.class));
        }
    }
}
