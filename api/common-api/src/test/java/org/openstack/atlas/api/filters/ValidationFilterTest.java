/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstack.atlas.api.filters;

import java.util.List;
import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author crc
 */
public class ValidationFilterTest {

    public ValidationFilterTest() {
    }

    @Test
    public void testlineSplit() {
        List<String> lines = ValidationFilter.lineSplit("\n212345\n2\n3\n4\n");
        assertEquals(6, lines.size());
        assertEquals("", lines.get(0));
        assertEquals("212345", lines.get(1));
        assertEquals("2", lines.get(2));
        assertEquals("3", lines.get(3));
        assertEquals("4", lines.get(4));
        assertEquals("", lines.get(5));

        lines = ValidationFilter.lineSplit("\n212345\n2\n3\n4");
        assertEquals(5, lines.size());
        assertEquals("", lines.get(0));
        assertEquals("212345", lines.get(1));
        assertEquals("2", lines.get(2));
        assertEquals("3", lines.get(3));
        assertEquals("4", lines.get(4));
    }

    @Test
    public void testnearString() {
        String testStr = "\nA1234567890\nB1234567890\nC1234567890\n\nE1234567890";
        Assert.assertEquals(testStr, ValidationFilter.nearString(testStr, 0, 0));
        Assert.assertEquals("890\nB1234567890\nC1234567890\n\nE1234567890", ValidationFilter.nearString(testStr, 1, 8));
        Assert.assertEquals("90", ValidationFilter.nearString(testStr, 5, 9));
        Assert.assertEquals("", ValidationFilter.nearString("abc", 5, 1)); // Should return empty string
        Assert.assertEquals("\n12345\n12345\n", ValidationFilter.nearString("12345\n12345\n12345\n12345\n", 1, 5));

    }

}