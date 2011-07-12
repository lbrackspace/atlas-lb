package org.openstack.atlas.util;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.openstack.atlas.util.DateHelper;

import java.util.Calendar;
import java.util.Date;

public class DateHelperTest extends TestCase {

    @Test
    public void testParseDate() throws Exception {
        Date parseDate = DateHelper.parseDate("02/May/2009:02:35:30");
        Calendar c = Calendar.getInstance();
        c.setTime(parseDate);
        Assert.assertEquals(2, c.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(Calendar.MAY, c.get(Calendar.MONTH));
        Assert.assertEquals(2009, c.get(Calendar.YEAR));
        Assert.assertEquals(2, c.get(Calendar.HOUR));
        Assert.assertEquals(35, c.get(Calendar.MINUTE));
        Assert.assertEquals(30, c.get(Calendar.SECOND));
    }
}
