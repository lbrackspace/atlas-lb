package org.openstack.atlas.util.converters;

import org.openstack.atlas.util.common.exceptions.ConverterException;
import java.util.List;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.openstack.atlas.util.converters.PrimitiveConverters.cdString2IntegerList;
import static org.openstack.atlas.util.converters.PrimitiveConverters.integerList2cdString;

public class PrimitiveConvertersTest {

    private final String is = "1,2,3";
    private final String sis = "1";
    private final String bis = "x,3,4";
    private final String e = "";
    private List<Integer> eil;
    private List<Integer> esil;
    private List<Integer> el;

    @Before
    public void setUp() {
        eil = new ArrayList<Integer>();
        eil.add(1);
        eil.add(2);
        eil.add(3);

        esil = new ArrayList<Integer>();
        esil.add(1);

        el = new ArrayList<Integer>();

    }

    public PrimitiveConvertersTest() {
    }

    @Test
    public void shouldConvert123List() throws ConverterException {
        List<Integer> resultList = cdString2IntegerList(is);
        assertTrue(isListIdentical(eil, resultList));
    }

    @Test
    public void shouldConvert1List() throws ConverterException {
        List<Integer> resultList = cdString2IntegerList(sis);
        assertTrue(isListIdentical(esil, resultList));
    }

    @Test
    public void shouldConvertemptyList() throws ConverterException {
        List<Integer> resultList = cdString2IntegerList(e);
        assertTrue(isListIdentical(el, resultList));
    }

    @Test(expected=ConverterException.class)
    public void shouldThrowExceptionIfIntsIsNull() throws ConverterException{
        List<Integer> resultList = cdString2IntegerList(null);
    }

    @Test(expected=ConverterException.class)
    public void shouldThrowExceptionIfIntsAreBad() throws ConverterException{
        List<Integer> resultList = cdString2IntegerList(bis);
    }

    @Test(expected=ConverterException.class)
    public void shouldThrowExceptionIfListisNull() throws ConverterException{
        String resultStr = integerList2cdString(null);
    }

    @Test
    public void shouldConvert123List2String() throws ConverterException{
        String result = integerList2cdString(eil);
        assertEquals(is,result);
    }

    @Test
    public void shouldConvert1List2String() throws ConverterException{
        String result = integerList2cdString(esil);
        assertEquals(sis,result);
    }

    @Test
    public void shouldConvertEmptyList2EmptyString() throws ConverterException{
        String result = integerList2cdString(el);
        assertEquals(e,result);
    }

    private boolean isListIdentical(List a, List b) {
        int i;
        if (a.size() != b.size()) {
            return false;
        }
        for (i = 0; i < a.size(); i++) {
            if (a.get(i) != b.get(i)) {
                return false;
            }
        }
        return true;
    }
}
