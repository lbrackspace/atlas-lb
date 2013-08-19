/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.logs.lzofaker;

import org.openstack.atlas.logs.itest.LzoFakerMain;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openstack.atlas.util.itest.hibernate.HuApp;
import org.openstack.atlas.util.debug.Debug;

public class LzoFakerMainTest {

    public LzoFakerMainTest() {
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
    public void testbuildRndStringsShouldNotGenerateArrayOutOfBoundsException() {
        String[] rndStrings;
        boolean lastAplhaCharFound = false;
        int nStrings;
        char lastAlphaChar;

        lastAlphaChar = LzoFakerMain.alphaNum.charAt(LzoFakerMain.alphaNum.length() - 1);
        rndStrings = LzoFakerMain.buildRandomStrings(4096, 4096);
        nStrings =  rndStrings.length;
        for (int i = 0; i < nStrings; i++) {
            String rndString = rndStrings[i];
            Debug.nop();
            for (int j = 0; j < rndString.length(); j++) {
                if (rndString.charAt(j) == lastAlphaChar) {
                    lastAplhaCharFound = true;
                }
            }
        }
    }
}
