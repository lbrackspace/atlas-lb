package org.openstack.atlas.service.domain.util;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class StringUtilitiesTest {

    public StringUtilitiesTest() {
    }

    @Test
    public void testDelimitString() {
        ArrayList<String> testList = new ArrayList<String>();
        testList.add("1234");
        testList.add("12345");
        testList.add("123456");
        testList.add("1234567");

        String delimitedString = StringUtilities.DelimitString(testList, ", ");
        assertEquals("1234, 12345, 123456, 1234567", delimitedString);
    }

    @Test
    public void testDelimitStringWithEmptyList() {
        ArrayList<String> testList = new ArrayList<String>();

        String delimitedString = StringUtilities.DelimitString(testList, ", ");
        assertEquals("", delimitedString);
    }

    @Test
    public void testDelimitStringAndWrapEntriesWithQuotes() {
        ArrayList<String> testList = new ArrayList<String>();
        testList.add("1234");
        testList.add("12345");
        testList.add("123456");
        testList.add("1234567");

        String delimitedString = StringUtilities.DelimitStringAndWrapEntriesWithQuotes(testList, ", ");
        assertEquals("'1234', '12345', '123456', '1234567'", delimitedString);
    }

    @Test
    public void testDelimitStringAndWrapEntriesWithQuotesWithEmptyList() {
        ArrayList<String> testList = new ArrayList<String>();

        String delimitedString = StringUtilities.DelimitStringAndWrapEntriesWithQuotes(testList, ", ");
        assertEquals("", delimitedString);
    }
}