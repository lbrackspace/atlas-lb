package org.openstack.atlas.api.filters.helpers;

import org.junit.Ignore;
import junit.framework.Assert;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class AcceptTypesTest {

    private String[] initString = new String[]{
        "audio/*;q=0.2,audio/basic",
        "text/*,text/html,text/html;level=1,*/*",
        "text/*;q=0.3,text/html;q=0.7,text/html;level=1,text/html;level=2;q=0.4,*/*;q=0.5,text/html;level=3;q=0.7"
    };
    private String[] expectedMTypes = new String[]{
        "audio/basic,audio/*",
        "text/html;level=1,text/html,text/*,*/*",
        "text/html;level=1,text/html;level=3,text/html,*/*,text/html;level=2,text/*"
    };
    private String XML = "application/xml";
    private String JSON = "application/json";
    private String HTML = "text/html";
    private String XHTML = "application/xhtml+xml";
    private String PLAIN = "text/plain";
    List<AcceptTypes> acceptTypes;

    @Before
    public void setUp() {
        acceptTypes = new ArrayList<AcceptTypes>();
        int i;
        for (i = 0; i < initString.length; i++) {
            acceptTypes.add(AcceptTypes.getPrefferedAcceptTypes(initString[i]));
        }

    }

    @Test
    public void testPrecedence() {
        int j;
        int i;
        for (j = 0; j < acceptTypes.size(); j++) {
            i = 0;
            for (String mMap : expectedMTypes[j].split(",")) {
                AcceptType o = AcceptType.newInstance(mMap);
                AcceptType t = acceptTypes.get(j).getAcceptTypeList().get(i);
                Assert.assertTrue(AcceptType.mediaMatch(t, o));
                i++;
            }
        }

    }

    @Test
    public void shouldFindSuitableMediaType() {
        String accept = "text/html,application/xhtml+xml,audio/*,audio/mp3,application/xml;q=0.9,*/*;q=0.8";
        AcceptTypes ats = AcceptTypes.getPrefferedAcceptTypes(accept);

        Assert.assertEquals("application/xhtml+xml",
                ats.findSuitableMediaType("application/xml", "text/plain", "audio/*", "application/xhtml+xml"));

        Assert.assertEquals("audio/mp3", ats.findSuitableMediaType("audio/mp3","text/plain"));
        Assert.assertEquals("audio/iTunes", ats.findSuitableMediaType("application/mp3","audio/iTunes", "text/plain"));
        Assert.assertEquals("application/json", ats.findSuitableMediaType("application/json","text/plain"));


        ats = AcceptTypes.getPrefferedAcceptTypes("application/xml,application/json;q=0.9,*/*");
        Assert.assertEquals("application/xml", ats.findSuitableMediaType("application/xml","application/json"));
        Assert.assertEquals("application/xml", ats.findSuitableMediaType("application/json","application/xml"));
        

        ats = AcceptTypes.getPrefferedAcceptTypes("application/json;q=0.9,application/xml,*/*");
        Assert.assertEquals("application/xml", ats.findSuitableMediaType("application/xml","application/json"));
        Assert.assertEquals("application/xml", ats.findSuitableMediaType("application/json","application/xml"));
        Assert.assertEquals("application/xml", ats.findSuitableMediaType("application/octet-stream","application/json","application/xml"));
    }
}
