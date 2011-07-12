package org.openstack.atlas.api.filters.helpers;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

public class MediaTypeTest {

    public MediaTypeTest() {
    }
    private String Q = "q=0.6";
    private String UTF8 = "UTF-8";
    private String APPLICATION = "application";
    private String XML = "xml";
    private String JSON = "json";
    private String m1;
    private String m2;
    private String m3;
    private String m4;
    private MediaType ins1;
    private MediaType ins2;
    private MediaType ins3;
    private MediaType ins4;

    @Before
    public void setUp() {
        m1 = String.format(" %s/%s ;%s", APPLICATION, XML, UTF8);
        m2 = String.format("%s/%s; %s", APPLICATION, JSON, UTF8);
        m3 = String.format(" %s/%s", APPLICATION, XML);
        m4 = String.format("  %s/%s ; %s ; %s", APPLICATION, XML, UTF8, Q);
        ins1 = new MediaType();
        ins1.setType(APPLICATION);
        ins1.setSubtype(XML);
        ins1.getParameters().add(UTF8);
        ins2 = new MediaType();
        ins2.setType(APPLICATION);
        ins2.setSubtype(JSON);
        ins2.getParameters().add(UTF8);
        ins3 = new MediaType();
        ins3.setType(APPLICATION);
        ins3.setSubtype(XML);
        ins4 = new MediaType();
        ins4.setType(APPLICATION);
        ins4.setSubtype(XML);
        ins4.getParameters().add(UTF8);
        ins4.getParameters().add(Q);
    }

    @Test
    public void shouldReturnNullWhenStringIsInvalid() {
        Assert.assertEquals("Expected to map to a null value when passing in null", null, MediaType.newInstance(null));
        Assert.assertEquals("Expected to map to a null value when passing \"blah\"", null, MediaType.newInstance("blah"));
    }

    @Test
    public void shouldMapIns1Correctly() {
        MediaType mt = MediaType.newInstance(m1);
        Assert.assertEquals(APPLICATION, ins1.getType());
        Assert.assertEquals(XML, mt.getSubtype());
        Assert.assertEquals(1, mt.getParameters().size());
        Assert.assertEquals(UTF8, mt.getParameters().get(0));
    }

    @Test
    public void shouldMapIns2Correctly() {
        MediaType mt = MediaType.newInstance(m2);
        Assert.assertEquals(APPLICATION, mt.getType());
        Assert.assertEquals(JSON, mt.getSubtype());
        Assert.assertEquals(1, mt.getParameters().size());
        Assert.assertEquals(UTF8, mt.getParameters().get(0));
    }

    @Test
    public void shouldMapIns3Correctly() {
        MediaType mt = MediaType.newInstance(m3);
        Assert.assertEquals(APPLICATION, mt.getType());
        Assert.assertEquals(XML, mt.getSubtype());
        Assert.assertEquals(0, mt.getParameters().size());
    }

    @Test
    public void shouldMapIns4Correctly() {
        MediaType mt = MediaType.newInstance(m4);
        Assert.assertEquals(APPLICATION, mt.getType());
        Assert.assertEquals(XML, mt.getSubtype());
        Assert.assertEquals(2, mt.getParameters().size());
        Assert.assertEquals(UTF8, mt.getParameters().get(0));
        Assert.assertEquals(Q, mt.getParameters().get(1));
    }

    @Test
    public void shouldMediaMatchCorrectly() {
        MediaType xml = MediaType.newInstance("application/xml");
        MediaType json = MediaType.newInstance("application/json");

        Assert.assertFalse(MediaType.matches(null,null));
        Assert.assertFalse(MediaType.matches(null,xml));
        Assert.assertFalse(MediaType.matches(xml,null));
        Assert.assertFalse(MediaType.matches(xml,json));
        Assert.assertFalse(MediaType.matches(json,xml));
        Assert.assertFalse(MediaType.matches(xml, json));
        Assert.assertTrue(MediaType.matches(xml,xml));
        Assert.assertTrue(MediaType.matches(json, json));
    }

    @Test
    public void shouldNotThrowNullPointerExceptionDuringMediaMatching() {
        MediaType xml = MediaType.newInstance("application/xml");
        MediaType json = MediaType.newInstance("application/json");
        xml.setType(null);
        json.setSubtype(null);
        Assert.assertFalse(MediaType.matches(null,null));
        Assert.assertFalse(MediaType.matches(xml, xml));
        Assert.assertFalse(MediaType.matches(json,json));
    }
}
