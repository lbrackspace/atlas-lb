package org.openstack.atlas.util.converters;

import java.util.List;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class StringConverterTest {

    private List<Integer> emptyList;
    private List<Integer> oneInList;
    private List<Integer> threeInList;
    private String emptyString = "";
    private String oneInString = "1";
    private String threeInString = "1,2,3";

    @Before
    public void setUp() {
        emptyList = new ArrayList<Integer>();
        oneInList = new ArrayList<Integer>();
        oneInList.add(1);
        threeInList = new ArrayList<Integer>();
        threeInList.add(1);
        threeInList.add(2);
        threeInList.add(3);
    }

    @Test
    public void testStrList3(){
        List<String> strList = new ArrayList<String>();
        strList.add("1");
        strList.add("2");
        strList.add("3");
        assertEquals("1, 2, 3",StringConverter.commaSeperatedStringList(strList));
    }

    @Test
    public void testEmptyString() {
        assertEquals(emptyString, StringConverter.integersAsString(emptyList));
    }

    @Test
    public void testOneString() {
        assertEquals(oneInString, StringConverter.integersAsString(oneInList));
    }

    @Test
    public void testThreeString() {
        assertEquals(threeInString, StringConverter.integersAsString(threeInList));
    }
}