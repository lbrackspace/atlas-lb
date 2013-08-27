package org.openstack.atlas.atom.helper;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.openstack.atlas.atomhopper.factory.UsageEntryFactoryImpl;
import org.openstack.atlas.atomhopper.util.AHUSLServiceUtil;
import org.openstack.atlas.restclients.atomhopper.AtomHopperClient;
import org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.events.entities.LoadBalancerServiceEvent;
import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.events.repository.LoadBalancerEventRepository;
import org.openstack.atlas.usage.thread.helper.AHRecordHelper;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(Enclosed.class)
public class AHRecordHelperTest {

    @RunWith(PowerMockRunner.class)
    @PrepareForTest(AtomHopperUtil.class)
    public static class WhenVerifyingAHrecordHelper {

        @Mock
        public AtomHopperClient client;

        @Mock
        public ClientResponse response;

        @Mock
        public AlertRepository alertRepository;

        @Mock
        public LoadBalancerEventRepository loadBalancerEventRepository;

        @Mock
        public AHUSLServiceUtil ahsutil;

        public AtomHopperUtil ahutil;

        public AHRecordHelper ahelper;

        private Usage baseUsage;
        private Calendar cal;

        private UsageEntryFactoryImpl entryFactory;
        private String token;
        private Map<Object, Object> emap;

        @Before
        public void standUp() throws Exception {
            initMocks(this);
            mockStatic(AtomHopperUtil.class);
            ahelper = new AHRecordHelper(true, client, loadBalancerEventRepository, alertRepository);

            baseUsage = new Usage();
            baseUsage.setCorrected(false);
            baseUsage.setAccountId(54321);
            baseUsage.setAverageConcurrentConnections(0.0);
            baseUsage.setAverageConcurrentConnectionsSsl(0.0);
            cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY, -1);
            cal.getTime();
            baseUsage.setEndTime(cal);
            baseUsage.setStartTime(cal);
            baseUsage.setEntryVersion(1);
            baseUsage.setEventType(org.openstack.atlas.service.domain.events.entities.EventType.CREATE_LOADBALANCER.name());
            baseUsage.setIncomingTransfer((long) 0);
            baseUsage.setNeedsPushed(true);
            baseUsage.setUuid(null);
            baseUsage.setTags(0);
            baseUsage.setNumAttempts(0);
            baseUsage.setNumVips(1);
            baseUsage.setOutgoingTransfer((long) 0);
            baseUsage.setOutgoingTransferSsl((long) 0);
            LoadBalancer lb = new LoadBalancer();
            lb.setId(1223);
            lb.setName("base");
            baseUsage.setLoadbalancer(lb);

            token = "atokenHere";
            entryFactory = new UsageEntryFactoryImpl();
            emap = entryFactory.createEntry(baseUsage);

            doNothing().when(response).close();
            when(AtomHopperUtil.processResponseBody(Matchers.<ClientResponse>any())).thenReturn("test");
        }

        @Test
        public void shouldInit() {
            Assert.assertTrue(ahelper.isVerboseLog());
        }

        @Test
        public void shouldFailAndAlertForClientFailure() throws Exception {
            when(client.postEntryWithToken(Matchers.any(), Matchers.<String>any())).thenReturn(null);
            ahelper.handleUsageRecord(baseUsage, token, emap);
            Assert.assertEquals(1, ahelper.getFailedRecords().size());
            verify(alertRepository, times(1)).save(Matchers.<Alert>any());
        }

        @Test
        public void shouldFailAndAlertForClientHandlerException() throws Exception {
            doThrow(ClientHandlerException.class)
                    .when(client).postEntryWithToken(Matchers.any(), Matchers.<String>any());
            ahelper.handleUsageRecord(baseUsage, token, emap);
            Assert.assertEquals(1, ahelper.getFailedRecords().size());
            verify(alertRepository, times(1)).save(Matchers.<Alert>any());
        }

        @Test
        public void shouldFailAndAlertForPoolTimeoutException() throws Exception {
            doThrow(ConnectionPoolTimeoutException.class)
                    .when(client).postEntryWithToken(Matchers.any(), Matchers.<String>any());
            ahelper.handleUsageRecord(baseUsage, token, emap);
            Assert.assertEquals(1, ahelper.getFailedRecords().size());
            verify(alertRepository, times(1)).save(Matchers.<Alert>any());
        }

        @Test
        public void shouldFailAndWarmForPoolTimeoutException() throws Exception {
            doThrow(ConcurrentModificationException.class)
                    .when(client).postEntryWithToken(Matchers.any(), Matchers.<String>any());
            ahelper.handleUsageRecord(baseUsage, token, emap);
            Assert.assertEquals(0, ahelper.getFailedRecords().size());
            verify(alertRepository, times(0)).save(Matchers.<Alert>any());
        }

        @Test
        public void shouldFailAlertAndEventForException() throws Exception {
            doThrow(Exception.class)
                    .when(client).postEntryWithToken(Matchers.any(), Matchers.<String>any());
            ahelper.handleUsageRecord(baseUsage, token, emap);
            Assert.assertEquals(1, ahelper.getFailedRecords().size());
            verify(alertRepository, times(1)).save(Matchers.<Alert>any());
            verify(loadBalancerEventRepository, times(1)).save(Matchers.<LoadBalancerServiceEvent>any());
        }

        @Test
        public void shouldFailAndAlertForRandomStatus() throws Exception {
            when(client.postEntryWithToken(Matchers.any(), Matchers.<String>any())).thenReturn(response);
            when(response.getStatus()).thenReturn(9735);
            ahelper.handleUsageRecord(baseUsage, token, emap);
            Assert.assertEquals(1, ahelper.getFailedRecords().size());
            verify(alertRepository, times(1)).save(Matchers.<Alert>any());
            verify(loadBalancerEventRepository, times(0)).save(Matchers.<LoadBalancerServiceEvent>any());
        }

        @Test
        public void shouldFailAlertAndEventFor400Status() throws Exception {
            when(client.postEntryWithToken(Matchers.any(), Matchers.<String>any())).thenReturn(response);
            when(response.getStatus()).thenReturn(400);
            ahelper.handleUsageRecord(baseUsage, token, emap);
            Assert.assertEquals(1, ahelper.getFailedRecords().size());
            verify(alertRepository, times(1)).save(Matchers.<Alert>any());
            verify(loadBalancerEventRepository, times(1)).save(Matchers.<LoadBalancerServiceEvent>any());
        }

        @Test
        public void shouldFailAndAlertFor409Status() throws Exception {
            when(client.postEntryWithToken(Matchers.any(), Matchers.<String>any())).thenReturn(response);
            when(response.getStatus()).thenReturn(409);
            ahelper.handleUsageRecord(baseUsage, token, emap);
            Assert.assertEquals(1, ahelper.getFailedRecords().size());
            verify(alertRepository, times(1)).save(Matchers.<Alert>any());
            verify(loadBalancerEventRepository, times(1)).save(Matchers.<LoadBalancerServiceEvent>any());
        }

        @Test
        public void shouldNotFailOrAlertFor201Status() throws Exception {
            when(client.postEntryWithToken(Matchers.any(), Matchers.<String>any())).thenReturn(response);
            when(response.getStatus()).thenReturn(201);
            ahelper.handleUsageRecord(baseUsage, token, emap);
            Assert.assertEquals(0, ahelper.getFailedRecords().size());
            verify(alertRepository, times(0)).save(Matchers.<Alert>any());
            verify(loadBalancerEventRepository, times(0)).save(Matchers.<LoadBalancerServiceEvent>any());
        }

        @Test
        public void shouldGenUUID() throws Exception {
            when(client.postEntryWithToken(Matchers.any(), Matchers.<String>any())).thenReturn(response);
            when(response.getStatus()).thenReturn(201);
            Assert.assertNull(baseUsage.getUuid());
            ahelper.handleUsageRecord(baseUsage, token, emap);
            Assert.assertEquals(0, ahelper.getFailedRecords().size());
            verify(alertRepository, times(0)).save(Matchers.<Alert>any());
            verify(loadBalancerEventRepository, times(0)).save(Matchers.<LoadBalancerServiceEvent>any());
            Assert.assertNotNull(baseUsage.getUuid());
            Assert.assertTrue(baseUsage.getNumAttempts() == 0);
        }

        @Test
        public void shouldGenUUIDAndBumpNumAttemptsfor400() throws Exception {
            when(client.postEntryWithToken(Matchers.any(), Matchers.<String>any())).thenReturn(response);
            when(response.getStatus()).thenReturn(400);
            Assert.assertNull(baseUsage.getUuid());
            ahelper.handleUsageRecord(baseUsage, token, emap);
            Assert.assertEquals(1, ahelper.getFailedRecords().size());
            verify(alertRepository, times(1)).save(Matchers.<Alert>any());
            verify(loadBalancerEventRepository, times(1)).save(Matchers.<LoadBalancerServiceEvent>any());
            Assert.assertNotNull(baseUsage.getUuid());
            Assert.assertTrue(baseUsage.getNumAttempts() == 1);
        }

        @Test
        public void shouldGenUUIDAndBumpNumAttemptsFor409() throws Exception {
            when(client.postEntryWithToken(Matchers.any(), Matchers.<String>any())).thenReturn(response);
            when(response.getStatus()).thenReturn(409);
            Assert.assertNull(baseUsage.getUuid());
            ahelper.handleUsageRecord(baseUsage, token, emap);
            Assert.assertEquals(1, ahelper.getFailedRecords().size());
            verify(alertRepository, times(1)).save(Matchers.<Alert>any());
            verify(loadBalancerEventRepository, times(1)).save(Matchers.<LoadBalancerServiceEvent>any());
            Assert.assertNotNull(baseUsage.getUuid());
            Assert.assertTrue(baseUsage.getNumAttempts() == 1);
        }

        @Test
        public void shouldGenUUIDAndBumpNumAttemptsForRandomStatus() throws Exception {
            when(client.postEntryWithToken(Matchers.any(), Matchers.<String>any())).thenReturn(response);
            when(response.getStatus()).thenReturn(999);
            Assert.assertNull(baseUsage.getUuid());
            ahelper.handleUsageRecord(baseUsage, token, emap);
            Assert.assertEquals(1, ahelper.getFailedRecords().size());
            verify(alertRepository, times(1)).save(Matchers.<Alert>any());
            verify(loadBalancerEventRepository, times(0)).save(Matchers.<LoadBalancerServiceEvent>any());
            Assert.assertNotNull(baseUsage.getUuid());
            Assert.assertTrue(baseUsage.getNumAttempts() == 1);
        }
    }
}
