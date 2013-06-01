package org.openstack.atlas.util.snmp;

import java.io.IOException;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class SnmpJsonConfigTest {

    public SnmpJsonConfigTest() {
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
    public void testReadJsonConfig() throws Exception {
        SnmpJsonConfig config = SnmpJsonConfig.readJsonConfig(SnmpJsonConfig.exampleJson);
        assertEquals("127.0.0.1/1161", config.getZxtmHosts().get("z1"));
        assertEquals("127.0.0.2/1161", config.getZxtmHosts().get("z2"));
        assertEquals("127.0.0.3/1161", config.getZxtmHosts().get("z3"));
        assertEquals("z1", config.getDefaultHostKey());
    }

    @Test
    public void testReadJsonConfigWithRepeaterOptions() throws IOException {
        SnmpJsonConfig config = SnmpJsonConfig.readJsonConfig(SnmpJsonConfig.exampleJson);
        assertTrue(config.getMaxRepetitions() == null);
        assertTrue(config.getNonRepeaters() == null);

        config = SnmpJsonConfig.readJsonConfig(SnmpJsonConfig.exampleJson2);
        assertTrue(config.getNonRepeaters() != null);
        assertTrue(config.getNonRepeaters().intValue() == 5);

        assertTrue(config.getMaxRepetitions() != null);
        assertTrue(config.getMaxRepetitions().intValue() == 350);
    }
}
