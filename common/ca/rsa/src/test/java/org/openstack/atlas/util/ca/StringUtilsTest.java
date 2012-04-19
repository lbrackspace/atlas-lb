/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstack.atlas.util.ca;

import java.util.List;
import java.util.ArrayList;
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

    @Test
    public void testJoinString(){
        List<String> strList = new ArrayList<String>();
        strList.add("1");
        strList.add("2");
        strList.add("3");       
        assertEquals("1,2,3",StringUtils.joinString(strList, ","));
        nop();

        strList = new ArrayList<String>();
        assertEquals("",StringUtils.joinString(strList,","));

        strList = new ArrayList<String>();
        strList.add("1");
        assertEquals("1",StringUtils.joinString(strList,","));
    }

    private static void nop(){
    }
}