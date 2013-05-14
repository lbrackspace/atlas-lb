package org.openstack.atlas.usage.jobs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.services.JobStateService;
import org.openstack.atlas.util.common.CalendarUtils;

import java.text.ParseException;
import java.util.Calendar;

import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class LoadBalancerUsageRollupJobTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenGettingHourToStop {

        @InjectMocks
        private LoadBalancerUsageRollupJob loadBalancerUsageRollupJob;

        @Test
        public void shouldReturnThisHourOnTheHourMark() {
            Calendar nowTopOfTheHour = Calendar.getInstance();
            nowTopOfTheHour = CalendarUtils.stripOutMinsAndSecs(nowTopOfTheHour);
            final Calendar hourToStop = loadBalancerUsageRollupJob.getHourToStop();
            Assert.assertEquals(nowTopOfTheHour, hourToStop);
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenGettingHourToRollup {

        private JobState mockedJobState;
        private Calendar hourToStop;

        @Mock
        private JobStateService jobStateService;
        @InjectMocks
        private LoadBalancerUsageRollupJob loadBalancerUsageRollupJob;

        @Before
        public void standUp() {
            mockedJobState = new JobState();
            when(jobStateService.getByName(JobName.LB_USAGE_ROLLUP)).thenReturn(mockedJobState);
        }

        @Test
        public void shouldReturnNullWhenLastSuccessfulHourIsLaterThanHourToStop() throws ParseException {
            mockedJobState.setInputPath("2013-01-29 05:00:01");
            hourToStop = CalendarUtils.stringToCalendar("2013-01-29 05:00:00");

            Assert.assertNull(loadBalancerUsageRollupJob.getHourToRollup(hourToStop));
        }

        @Test
        public void shouldReturnNullWhenLastSuccessfulHourEqualsHourToStop() throws ParseException {
            mockedJobState.setInputPath("2013-01-29 05:00:00");
            hourToStop = CalendarUtils.stringToCalendar("2013-01-29 05:00:00");

            Assert.assertNull(loadBalancerUsageRollupJob.getHourToRollup(hourToStop));
        }

        @Test
        public void shouldReturnNullWhenLastSuccessfulHourEqualsLastHour() throws ParseException {
            mockedJobState.setInputPath("2013-01-29 04:00:00");
            hourToStop = CalendarUtils.stringToCalendar("2013-01-29 05:00:00");

            Assert.assertNull(loadBalancerUsageRollupJob.getHourToRollup(hourToStop));
        }

        @Test
        public void shouldReturnLastHourWhenLastSuccessfulHourEqualsTwoHourAgo() throws ParseException {
            mockedJobState.setInputPath("2013-01-29 03:00:00");
            hourToStop = CalendarUtils.stringToCalendar("2013-01-29 05:00:00");

            final Calendar hourToRollup = loadBalancerUsageRollupJob.getHourToRollup(hourToStop);

            Assert.assertEquals("2013-01-29 04:00:00", CalendarUtils.calendarToString(hourToRollup));
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenDeterminingIfWeShouldRollupUsage {

        private JobState mockedJobState;

        @Mock
        private JobStateService jobStateService;
        @InjectMocks
        private LoadBalancerUsageRollupJob loadBalancerUsageRollupJob;

        @Before
        public void standUp() {
            mockedJobState = new JobState();
            when(jobStateService.getByName(JobName.LB_USAGE_POLLER)).thenReturn(mockedJobState);
        }

        @Test
        public void shouldRollupWhenPollerHasEndtimeAfterThisHourAndIsFinished() {
            mockedJobState.setEndTime(Calendar.getInstance());
            mockedJobState.setState(JobStateVal.FINISHED);

            Assert.assertTrue(loadBalancerUsageRollupJob.shouldRollup());
        }

        @Test
        public void shouldRollupWhenPollerHasEndtimeAfterThisHourAndIsRunning() {
            mockedJobState.setEndTime(Calendar.getInstance());
            mockedJobState.setState(JobStateVal.IN_PROGRESS);

            Assert.assertTrue(loadBalancerUsageRollupJob.shouldRollup());
        }

        @Test
        public void shouldNotRollupWhenPollerHasEndtimeAfterThisHourAndHasFailed() {
            mockedJobState.setEndTime(Calendar.getInstance());
            mockedJobState.setState(JobStateVal.FAILED);

            Assert.assertFalse(loadBalancerUsageRollupJob.shouldRollup());
        }

        @Test
        public void shouldNotRollupWhenPollerHasEndtimeBeforeThisHourAndIsFinished() {
            Calendar pollerEndTime = Calendar.getInstance();
            pollerEndTime.add(Calendar.HOUR, -1);
            mockedJobState.setEndTime(pollerEndTime);
            mockedJobState.setState(JobStateVal.FINISHED);

            Assert.assertFalse(loadBalancerUsageRollupJob.shouldRollup());
        }
    }
}
