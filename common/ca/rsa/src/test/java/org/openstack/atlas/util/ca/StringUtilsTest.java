/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstack.atlas.util.ca;

import org.openstack.atlas.util.ca.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class StringUtilsTest {

    public StringUtilsTest() {
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
    public void testEscape_html() {
        System.out.println("escape_html");
        String html = "<html>\"Tom & Jerry\"\n</html>";
        String expResult = "&lt;html&gt;&quot;Tom&nbsp;&amp;&nbsp;Jerry&quot;<br/>&lt;/html&gt;";
        String result = StringUtils.escape_html(html);
        assertEquals(expResult, result);
    }

}