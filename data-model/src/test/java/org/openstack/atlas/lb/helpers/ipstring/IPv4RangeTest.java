package org.openstack.atlas.lb.helpers.ipstring;


import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class IPv4RangeTest {
    private IPv4Range onenintytwo;

    public IPv4RangeTest() {
    }

    @Before
    public void setUp() throws IPStringException {
        onenintytwo = new IPv4Range("192.168.3.50", "192.168.3.54", "test");
    }

    @Test
    public void testConStructor() {
       IPv4Range range = new IPv4Range(100,200,"Test");
       assertEquals(100,range.getLo());
       assertEquals(200,range.getHi());
       assertEquals("Test",range.getLabel());
    }

    @Test
    public void shouldExceptIpInRange(){
        assertTrue(onenintytwo.contains("192.168.3.52"));
    }

    @Test
    public void shouldRejectIpOutOfRange(){
        assertFalse(onenintytwo.contains("192.168.3.49"));
    }

}