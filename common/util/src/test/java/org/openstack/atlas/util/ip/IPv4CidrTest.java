package org.openstack.atlas.util.ip;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class IPv4CidrTest {

    public IPv4CidrTest() {
    }



    @Before
    public void setUp() {
    }

    @Test
    public void shouldContainIps48through63() throws Exception {
        int i;
        String ipStr;
        IPv4Cidr cidr = new IPv4Cidr("192.168.3.48/28");
        assertFalse(cidr.contains("192.168.3.47"));
        for(i=48;i<=63;i++){
            ipStr = String.format("192.168.3.%d",i);
        }
        assertFalse(cidr.contains("192.168.3.64"));
    }

}