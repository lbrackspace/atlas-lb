/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.api.filters.helpers;

import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AcceptTypeTest {

    public AcceptTypeTest() {
    }

    @Test
    public void qParamShouldReturnNullWhenNoQparamisInvalid() {
        Assert.assertNull(AcceptType.getQfromString("Nothing here"));
        Assert.assertNull(AcceptType.getQfromString("q = this is not a q param"));
        Assert.assertNull(AcceptType.getQfromString("q = 3.22  this is also not a q param"));
    }

    @Test
    public void shouldFetchStringIntoAcceptType() {
        AcceptType at = AcceptType.newInstance("text/html ; level = 1; q = 0.5 ; tp1 ; tp=2 ");
        Assert.assertEquals("type Mismatch","text",at.getMediaType().getType());
        Assert.assertEquals("subtype Mismatch","html",at.getMediaType().getSubtype());
        Assert.assertEquals("Q mismatch",0.5,at.getQ());
        Assert.assertEquals("mediaParametersize mismatch",1,at.getMediaType().getParameters().size());
        Assert.assertEquals("etra parameters mismatch","level = 1",at.getMediaType().getParameters().get(0));
        Assert.assertEquals("expected 2 extensions",2,at.getAcceptExtensions().size());
        Assert.assertEquals("extension1 mismatch","tp1",at.getAcceptExtensions().get(0));
        Assert.assertEquals("extension2 mismatch","tp=2",at.getAcceptExtensions().get(1));
    }

    @Test
    public void qParameterShouldDefaultToOneWhenNotSpecified(){
        AcceptType at = AcceptType.newInstance("text/html;UTF-8");
        Assert.assertEquals("Q should be 1.0",1.0, at.getQ());

    }

    @Test
    public void qParamShouldReturnValidDoublsWhenInputStringIsValid() {
        Assert.assertEquals(3.141, AcceptType.getQfromString("q = 3.141"));
        Assert.assertEquals(3.141, AcceptType.getQfromString(" q =   3.141   "));
    }
}
