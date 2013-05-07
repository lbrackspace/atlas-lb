package org.openstack.atlas.util.common;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.util.Calendar;

@RunWith(Enclosed.class)
public class CalendarUtilsTest {

    public static class WhenStrippingOutMinutesAndSeconds {

        @Test
        public void shouldZeroOutMinutesAndSeconds() {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);

            cal = CalendarUtils.stripOutMinsAndSecs(cal);

            Assert.assertEquals(0, cal.get(Calendar.MINUTE));
            Assert.assertEquals(0, cal.get(Calendar.SECOND));
            Assert.assertEquals(0, cal.get(Calendar.MILLISECOND));
        }
    }

    public static class WhenConvertingFromStringToCalendar {

        @Test(expected = NullPointerException.class)
        public void shouldThrowNullPointerExceptionWhenNull() throws ParseException {
            CalendarUtils.stringToCalendar(null);
        }

        @Test(expected = ParseException.class)
        public void shouldThrowParseExceptionWhenEmptyString() throws ParseException {
            CalendarUtils.stringToCalendar("");
        }

        @Test
        public void shouldHaveCorrectDateTime() throws ParseException {
            String timestampAsString = "2013-01-29 00:59:59";
            Calendar cal = CalendarUtils.stringToCalendar(timestampAsString);

            Assert.assertEquals(2013, cal.get(Calendar.YEAR));
            Assert.assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
            Assert.assertEquals(29, cal.get(Calendar.DAY_OF_MONTH));
            Assert.assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
            Assert.assertEquals(59, cal.get(Calendar.MINUTE));
            Assert.assertEquals(59, cal.get(Calendar.SECOND));
            Assert.assertEquals(0, cal.get(Calendar.MILLISECOND));
        }
    }

    public static class WhenMakingACopy {

        @Test
        public void shouldCopyEverything() {
            Calendar anHourFromNow = Calendar.getInstance();
            anHourFromNow.add(Calendar.HOUR, 1);

            Calendar copy = CalendarUtils.copy(anHourFromNow);

            Assert.assertEquals(copy, anHourFromNow);
        }
    }
    
    public static class WhenConvertingFromCalendarToString {

        @Test
        public void shouldHaveCorrectTime() {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 2013);
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DAY_OF_MONTH, 29);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);

            String calAsString = CalendarUtils.calendarToString(cal);

            Assert.assertEquals("2013-01-29 23:59:59", calAsString);
        }
    }
}
