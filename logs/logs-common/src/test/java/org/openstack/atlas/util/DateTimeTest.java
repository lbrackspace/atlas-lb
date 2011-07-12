package org.openstack.atlas.util;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Calendar;

@Ignore
public class DateTimeTest extends TestCase {
    private static final Log LOG = LogFactory.getLog(DateTimeTest.class);

    @Test
    public void testCompareTo() {
        Calendar c = Calendar.getInstance();
        Assert.assertEquals(0, new DateTime(c).compareTo(new DateTime(c)));
    }

    @Test
    public void testDateTimeApacheFunkinessMillis() throws Exception {
        DateTime t = new DateTime("17/May/2010:09:11:30", DateTime.APACHE);
        System.err.println(t.getCalendar().getTimeInMillis() / 1000);
    }


    @Test
    public void testConstructors() {
        DateTime t = new DateTime();
        t = new DateTime(Calendar.getInstance());
        t = new DateTime("2009-01-01 01:01:01", DateTime.ISO_DATE);
        t = new DateTime("09/Jul/2009:08:33:14", DateTime.APACHE);
        t = new DateTime("2009-01-01 01:01:01", DateTime.ISO);
        t.roll(Calendar.DATE, 1);
        Assert.assertEquals(t.getIso(), t.toString());
    }

    @Test
    public void testDateTimeApache() {
        LOG.info(new DateTime("09/Jul/2009:08:33:14", DateTime.APACHE));
    }

    @Test
    public void testDateTimeRuntime() {
        LOG.info(new DateTime("20100528-144250", DateTime.RUNTIME));
    }

    @Test
    public void testDoubleDateTimeApache() {
        Calendar c = new DateTime("09/Jul/2009:08:33:14", DateTime.APACHE).getCalendar();

        LOG.info(c.get(Calendar.MONTH));
        c = new DateTime(c).getCalendar();
        LOG.info(c.get(Calendar.MONTH));
        String iso = new DateTime(c).getIso();
        LOG.info(new DateTime(iso));

        c = new DateTime(iso).getCalendar();
        LOG.info(new DateTime(c).getIso());

    }

    @Test
    public void testGetNumericalMonth() {
        Assert.assertEquals(1, DateTime.getNumericalMonth("JAN"));
        Assert.assertEquals(2, DateTime.getNumericalMonth("FEB"));
        Assert.assertEquals(3, DateTime.getNumericalMonth("MAR"));
        Assert.assertEquals(4, DateTime.getNumericalMonth("APR"));
        Assert.assertEquals(5, DateTime.getNumericalMonth("MAY"));
        Assert.assertEquals(6, DateTime.getNumericalMonth("JUN"));
        Assert.assertEquals(7, DateTime.getNumericalMonth("JUL"));
        Assert.assertEquals(8, DateTime.getNumericalMonth("AUG"));
        Assert.assertEquals(9, DateTime.getNumericalMonth("SEP"));
        Assert.assertEquals(10, DateTime.getNumericalMonth("OCT"));
        Assert.assertEquals(11, DateTime.getNumericalMonth("NOV"));
        Assert.assertEquals(12, DateTime.getNumericalMonth("DEC"));
        Assert.assertEquals(0, DateTime.getNumericalMonth("FOO"));
    }

    @Test
    public void testPrependZero() {
        Assert.assertEquals("01", DateTime.prependZero(1));
        Assert.assertEquals("12", DateTime.prependZero(12));
    }
}
