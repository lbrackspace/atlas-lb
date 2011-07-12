package org.openstack.atlas.util;

import org.openstack.atlas.data.LogDateFormat;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;

public class LogDateFormatTest extends TestCase {

    @Test
    public void testActualFormat() throws Exception {
        LogDateFormat format = new LogDateFormat();
        // yyyyMMdd-HHmmss
        Calendar formatCal = Calendar.getInstance();
        formatCal.set(Calendar.YEAR, 1999);
        formatCal.set(Calendar.MONTH, Calendar.APRIL);
        formatCal.set(Calendar.DAY_OF_MONTH, 1);
        formatCal.set(Calendar.MINUTE, 1);
        formatCal.set(Calendar.SECOND, 1);
        formatCal.set(Calendar.MILLISECOND, 1);
        String stringFormat = format.format(formatCal.getTime());
        String restOfExpected = getStringFormat(formatCal.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals("19990401-" + restOfExpected + "0101", stringFormat);
    }

    @Test
    public void testGetStringFormat() throws Exception {
        Assert.assertEquals(2, getStringFormat(1).length());
        Assert.assertEquals(2, getStringFormat(11).length());
    }

    private String getStringFormat(int day) {
        if ((day + "").length() == 1) {
            return "0" + day;
        } else {
            return day + "";
        }
    }

}
