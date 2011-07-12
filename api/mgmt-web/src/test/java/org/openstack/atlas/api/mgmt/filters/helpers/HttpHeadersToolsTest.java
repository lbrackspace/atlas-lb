package org.openstack.atlas.api.mgmt.filters.helpers;

import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

public class HttpHeadersToolsTest {

    private Set<String> emptySet;
    private Set<String> oneItem;
    private Set<String> threeItems;
    private HttpServletRequest noAuthReq;
    private HttpServletRequest AuthReq;
    private HttpServletRequest badBase64;
    private HttpServletRequest badUserPassPair;
    private HttpServletRequest otherAuthReq;
    private HttpServletResponse resp;
    private HttpHeadersTools httpHeadersTools;
    private static final String digest = "Digest username=\"x\",realm=\"x\","
            + "nonce=\"blah\",uri=\"/index.html\",qop=auth,nc=00000001,cnonce"
            + "=\"X\",response=\"X\",opaque=\"x\"";

    public HttpHeadersToolsTest() {
    }

    @Before
    public void setUp() {
        resp = mock(HttpServletResponse.class);

        noAuthReq = mock(HttpServletRequest.class);
        when(noAuthReq.getHeader(anyString())).thenReturn(null);

        AuthReq = mock(HttpServletRequest.class);
        when(AuthReq.getHeader("Authorization")).thenReturn("BASIC c29tZW9uZTpzb21lcGFzc3dvcmQ=");

        badBase64 = mock(HttpServletRequest.class);
        when(badBase64.getHeader("Authorization")).thenReturn("BASIC c29tZW9uZTpzb21lcGFzc3dvcmQ");

        badUserPassPair = mock(HttpServletRequest.class);
        when(badUserPassPair.getHeader("Authorization")).thenReturn("BASIC c29tZW9uZUBzb21lcGFzc3dvcmQ=");

        otherAuthReq = mock(HttpServletRequest.class);
        when(otherAuthReq.getHeader("Authorization")).thenReturn(digest);


        emptySet = new HashSet<String>();
        oneItem = new HashSet<String>();
        oneItem.add("AAA");

        threeItems = new HashSet<String>();
        threeItems.add("AAA");
        threeItems.add("BBB");
        threeItems.add("CCC");
    }

    @Test
    public void testIsBasicAuthShouldReturnFalseWhenNoAuthHeaderSet() {
        httpHeadersTools = new HttpHeadersTools(noAuthReq, resp);
        assertFalse(httpHeadersTools.isBasicAuth());
    }

    @Test
    public void testIsBasicAuthShouldReturnFalseWhenAuthHeaderIsNotBasic() {
        httpHeadersTools = new HttpHeadersTools(otherAuthReq, resp);
        assertFalse(httpHeadersTools.isBasicAuth());
    }

    @Test
    public void testIsBasicAuthShouldReturnTrueWhenAuthIsBasic() {
        assertTrue(new HttpHeadersTools(AuthReq, resp).isBasicAuth());
        assertTrue(new HttpHeadersTools(badBase64, resp).isBasicAuth());
        assertTrue(new HttpHeadersTools(badUserPassPair, resp).isBasicAuth());

    }

    @Test
    public void testIsValidAuthShouldReturnFalseWhenAuthIsInvalid() {
        assertFalse(new HttpHeadersTools(noAuthReq, resp).isValidAuth());
        assertFalse(new HttpHeadersTools(otherAuthReq, resp).isValidAuth());
        assertFalse(new HttpHeadersTools(badBase64, resp).isValidAuth());
        assertFalse(new HttpHeadersTools(badUserPassPair, resp).isValidAuth());
    }

    @Test
    public void testIsValudAuthShouldReturnTrueWhenAuthIsValid() {
        assertTrue(new HttpHeadersTools(AuthReq, resp).isValidAuth());
    }

    @Test
    public void testShouldMapAuthUserAndPasswordCorrectly() {
        httpHeadersTools = new HttpHeadersTools(AuthReq, resp);
        assertEquals("someone", httpHeadersTools.getBasicUser());
        assertEquals("somepassword", httpHeadersTools.getBasicPassword());
    }

    @Test
    public void testShouldMapCommaListEmpty() {
        String strList = HttpHeadersTools.set2commastr(emptySet);
        assertEquals("", strList);
    }

    @Test
    public void testShouldMapCommaListWithOneItem() {
        String strList = HttpHeadersTools.set2commastr(oneItem);
        assertEquals("AAA", strList);
    }

    @Test
    public void testShouldMapCommaListWithThreeItems() {
        String strList = HttpHeadersTools.set2commastr(threeItems);
        assertEquals("AAA,BBB,CCC", strList);
    }
}
