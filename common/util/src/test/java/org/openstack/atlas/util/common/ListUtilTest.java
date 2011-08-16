package org.openstack.atlas.util.common;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ListUtilTest {
    @Before
    public void setup() {
    }

    @Test
    public void generateAnIntegerCommaSeparatedList() {
        List<Integer> actual = new ArrayList<Integer>();
        String expected = "0, 1, 2, 3, 4, 5";
        for (int i = 0; i <= 5; i++) {
            actual.add(i);
        }
        assertEquals(expected, ListUtil.generateCommaSeparatedString(actual));
    }

    @Test
    public void generateAnIntegerCommaSeparatedListWithEmptyList() {
        List<Integer> list = new ArrayList<Integer>();
        assertEquals("", ListUtil.generateCommaSeparatedString(list));
    }

    @Test
    public void generateAnIntegerCommaSeparatedListWithNullList() {
        assertEquals("", ListUtil.generateCommaSeparatedString(null));
    }

}