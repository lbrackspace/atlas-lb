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

    public static class WhenDeterminingIfBetweenTwoTimesInclusive {

        @Test
        public void shouldReturnTrueWhenEqualToStartTime() throws ParseException {
            Calendar cal = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar startTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar endTime = CalendarUtils.stringToCalendar("2013-01-29 01:00:00");

            boolean isBetween = CalendarUtils.isBetween(cal, startTime, endTime, true);

            Assert.assertTrue(isBetween);
        }

        @Test
        public void shouldReturnFalseWhenBeforeStartTime() throws ParseException {
            Calendar cal = CalendarUtils.stringToCalendar("2013-01-28 23:59:59");
            Calendar startTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar endTime = CalendarUtils.stringToCalendar("2013-01-29 01:00:00");

            boolean isBetween = CalendarUtils.isBetween(cal, startTime, endTime, true);

            Assert.assertFalse(isBetween);
        }

        @Test
        public void shouldReturnTrueWhenEqualToEndTime() throws ParseException {
            Calendar cal = CalendarUtils.stringToCalendar("2013-01-29 01:00:00");
            Calendar startTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar endTime = CalendarUtils.stringToCalendar("2013-01-29 01:00:00");

            boolean isBetween = CalendarUtils.isBetween(cal, startTime, endTime, true);

            Assert.assertTrue(isBetween);
        }

        @Test
        public void shouldReturnFalseWhenAfterEndTime() throws ParseException {
            Calendar cal = CalendarUtils.stringToCalendar("2013-01-29 01:00:01");
            Calendar startTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar endTime = CalendarUtils.stringToCalendar("2013-01-29 01:00:00");

            boolean isBetween = CalendarUtils.isBetween(cal, startTime, endTime, true);

            Assert.assertFalse(isBetween);
        }
    }

    public static class WhenDeterminingIfBetweenTwoTimesExclusive {

        @Test
        public void shouldReturnTrueWhenEqualToStartTime() throws ParseException {
            Calendar cal = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar startTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar endTime = CalendarUtils.stringToCalendar("2013-01-29 01:00:00");

            boolean isBetween = CalendarUtils.isBetween(cal, startTime, endTime, false);

            Assert.assertTrue(isBetween);
        }

        @Test
        public void shouldReturnFalseWhenBeforeStartTime() throws ParseException {
            Calendar cal = CalendarUtils.stringToCalendar("2013-01-28 23:59:59");
            Calendar startTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar endTime = CalendarUtils.stringToCalendar("2013-01-29 01:00:00");

            boolean isBetween = CalendarUtils.isBetween(cal, startTime, endTime, false);

            Assert.assertFalse(isBetween);
        }

        @Test
        public void shouldReturnTrueWhenRightBeforeEndTime() throws ParseException {
            Calendar cal = CalendarUtils.stringToCalendar("2013-01-29 00:59:59");
            Calendar startTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar endTime = CalendarUtils.stringToCalendar("2013-01-29 01:00:00");

            boolean isBetween = CalendarUtils.isBetween(cal, startTime, endTime, false);

            Assert.assertTrue(isBetween);
        }

        @Test
        public void shouldReturnFalseWhenEqualToEndTime() throws ParseException {
            Calendar cal = CalendarUtils.stringToCalendar("2013-01-29 01:00:00");
            Calendar startTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar endTime = CalendarUtils.stringToCalendar("2013-01-29 01:00:00");

            boolean isBetween = CalendarUtils.isBetween(cal, startTime, endTime, false);

            Assert.assertFalse(isBetween);
        }
    }

    public static class WhenCalculatingDuration {

        @Test
        public void shouldCalculateCorrectlyWhenNoDuration() throws ParseException {
            Calendar startTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar endTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");

            Duration duration = CalendarUtils.calcDuration(startTime, endTime);

            Assert.assertEquals(0, duration.getHours());
            Assert.assertEquals(0, duration.getMins());
            Assert.assertEquals(0, duration.getSecs());
        }

        @Test
        public void shouldCalculateCorrectlyWhenOneSecond() throws ParseException {
            Calendar startTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar endTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:01");

            Duration duration = CalendarUtils.calcDuration(startTime, endTime);

            Assert.assertEquals(0, duration.getHours());
            Assert.assertEquals(0, duration.getMins());
            Assert.assertEquals(1, duration.getSecs());
        }

        @Test
        public void shouldCalculateCorrectlyWhenLessThanAMinute() throws ParseException {
            Calendar startTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar endTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:59");

            Duration duration = CalendarUtils.calcDuration(startTime, endTime);

            Assert.assertEquals(0, duration.getHours());
            Assert.assertEquals(0, duration.getMins());
            Assert.assertEquals(59, duration.getSecs());
        }

        @Test
        public void shouldCalculateCorrectlyWhenMoreThanAMinute() throws ParseException {
            Calendar startTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar endTime = CalendarUtils.stringToCalendar("2013-01-29 00:01:01");

            Duration duration = CalendarUtils.calcDuration(startTime, endTime);

            Assert.assertEquals(0, duration.getHours());
            Assert.assertEquals(1, duration.getMins());
            Assert.assertEquals(1, duration.getSecs());
        }

        @Test
        public void shouldCalculateCorrectlyWhenLessThanAnHour() throws ParseException {
            Calendar startTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar endTime = CalendarUtils.stringToCalendar("2013-01-29 00:59:59");

            Duration duration = CalendarUtils.calcDuration(startTime, endTime);

            Assert.assertEquals(0, duration.getHours());
            Assert.assertEquals(59, duration.getMins());
            Assert.assertEquals(59, duration.getSecs());
        }

        @Test
        public void shouldCalculateCorrectlyWhenEqualToHour() throws ParseException {
            Calendar startTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar endTime = CalendarUtils.stringToCalendar("2013-01-29 01:00:00");

            Duration duration = CalendarUtils.calcDuration(startTime, endTime);

            Assert.assertEquals(1, duration.getHours());
            Assert.assertEquals(0, duration.getMins());
            Assert.assertEquals(0, duration.getSecs());
        }

        @Test
        public void shouldCalculateCorrectlyWhenMoreThanAnHour() throws ParseException {
            Calendar startTime = CalendarUtils.stringToCalendar("2013-01-29 00:00:00");
            Calendar endTime = CalendarUtils.stringToCalendar("2013-01-29 01:00:01");

            Duration duration = CalendarUtils.calcDuration(startTime, endTime);

            Assert.assertEquals(1, duration.getHours());
            Assert.assertEquals(0, duration.getMins());
            Assert.assertEquals(1, duration.getSecs());
        }
    }
}
