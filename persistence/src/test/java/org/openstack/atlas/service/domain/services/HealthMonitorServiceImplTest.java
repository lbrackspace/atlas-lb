package org.openstack.atlas.service.domain.services;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerStatusHistoryRepository;
import org.openstack.atlas.service.domain.repository.NodeRepository;
import org.openstack.atlas.service.domain.services.impl.HealthMonitorServiceImpl;
import org.openstack.atlas.service.domain.services.impl.LoadBalancerStatusHistoryServiceImpl;
import org.openstack.atlas.service.domain.services.impl.NodeServiceImpl;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class HealthMonitorServiceImplTest {

    public static class HealthMonitorProtocols {
        @InjectMocks
        HealthMonitorServiceImpl healthMonitorService = new HealthMonitorServiceImpl();
        @Mock
        LoadBalancerRepository lbRepository;
//        LoadBalancerStatusHistoryRepository loadBalancerStatusHistoryRepository;
        @Mock
        private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;
        @Captor
        private ArgumentCaptor<LoadBalancer> loadBalancerArgumentCaptor;

        LoadBalancer lb;
        LoadBalancer lb2;
        LoadBalancerJoinVip lbjv;
        Set<LoadBalancerJoinVip> lbjvs;
        VirtualIp vip;
        HealthMonitor healthMonitor;
        HealthMonitor healthMonitor2;

        @Before
        public void standUp() {
            MockitoAnnotations.initMocks(this);
        }

        @Before
        public void standUpObjects() throws EntityNotFoundException {
            lb = new LoadBalancer();
            lb2 = new LoadBalancer();
            lbjv = new LoadBalancerJoinVip();
            lbjvs = new HashSet<LoadBalancerJoinVip>();
            vip = new VirtualIp();
            healthMonitor = new HealthMonitor();
            healthMonitor2 = new HealthMonitor();

            vip.setIpAddress("192.3.3.3");
            lbjv.setVirtualIp(vip);
            lbjvs.add(lbjv);
            lb.setLoadBalancerJoinVipSet(lbjvs);
            lb.setId(1);
            lb.setAccountId(2333);

            when(loadBalancerStatusHistoryService.save(any())).thenReturn(new LoadBalancerStatusHistory());
        }

        @Test(expected = BadRequestException.class)
        public void shouldReturnFaultIfLbAndMonitorNotHTTP() throws EntityNotFoundException, BadRequestException {
            healthMonitor.setType(HealthMonitorType.HTTP);
            lb.setProtocol(LoadBalancerProtocol.HTTPS);
            healthMonitorService.verifyMonitorProtocol(healthMonitor, lb, healthMonitor2);
        }

        @Test(expected = BadRequestException.class)
        public void shouldReturnFaultIfLbAndMonitorNotHTTPs() throws EntityNotFoundException, BadRequestException {
            healthMonitor.setType(HealthMonitorType.HTTPS);
            lb.setProtocol(LoadBalancerProtocol.HTTP);
            healthMonitorService.verifyMonitorProtocol(healthMonitor, lb, healthMonitor2);
        }

        @Test
        public void hostHeaderShouldReturnNullForNonHTTP() throws UnprocessableEntityException, EntityNotFoundException, BadRequestException, ImmutableEntityException {
            healthMonitor.setType(HealthMonitorType.CONNECT);
            healthMonitor.setAttemptsBeforeDeactivation(1);
            healthMonitor.setDelay(2);
            healthMonitor.setTimeout(10);
            healthMonitor.setHostHeader("Test.com");

            lb.setProtocol(LoadBalancerProtocol.HTTP);
            lb.setAccountId(21323);
            lb.setId(1);
            lb.setHealthMonitor(new HealthMonitor());

            when(lbRepository.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(lb);

            LoadBalancer reqLb = new LoadBalancer();
            reqLb.setAccountId(21323);
            reqLb.setId(1);
            reqLb.setHealthMonitor(healthMonitor);

            when(lbRepository.testAndSetStatus(lb.getAccountId(), lb.getId(), LoadBalancerStatus.PENDING_UPDATE, false)).thenReturn(true);
            when(lbRepository.update(lb)).thenReturn(lb);

            healthMonitorService.update(reqLb);
            verify(lbRepository).update((loadBalancerArgumentCaptor.capture()));
            LoadBalancer lbCheck = loadBalancerArgumentCaptor.getValue();
            Assert.assertNull(lbCheck.getHealthMonitor().getHostHeader());
        }

        @Test
        public void hostHeaderShouldReturnValueForHTTP() throws UnprocessableEntityException, EntityNotFoundException, BadRequestException, ImmutableEntityException {
            healthMonitor.setType(HealthMonitorType.HTTP);
            healthMonitor.setAttemptsBeforeDeactivation(1);
            healthMonitor.setDelay(2);
            healthMonitor.setTimeout(10);
            healthMonitor.setStatusRegex(".*");
            healthMonitor.setPath("/");
            healthMonitor.setHostHeader("Test.com");

            lb.setProtocol(LoadBalancerProtocol.HTTP);
            lb.setAccountId(21323);
            lb.setId(1);
            lb.setHealthMonitor(new HealthMonitor());

            when(lbRepository.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(lb);

            LoadBalancer reqLb = new LoadBalancer();
            reqLb.setAccountId(21323);
            reqLb.setId(1);
            reqLb.setHealthMonitor(healthMonitor);

            when(lbRepository.testAndSetStatus(lb.getAccountId(), lb.getId(), LoadBalancerStatus.PENDING_UPDATE, false)).thenReturn(true);
            when(lbRepository.update(lb)).thenReturn(lb);

            healthMonitorService.update(reqLb);
            verify(lbRepository).update((loadBalancerArgumentCaptor.capture()));
            LoadBalancer lbCheck = loadBalancerArgumentCaptor.getValue();
            Assert.assertEquals("Test.com",lbCheck.getHealthMonitor().getHostHeader());
        }

        @Test
        public void shouldSucceedForHttpCreateNoBodyRegex() throws BadRequestException, EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException {
            healthMonitor.setType(HealthMonitorType.HTTP);
            healthMonitor.setAttemptsBeforeDeactivation(1);
            healthMonitor.setStatusRegex(".*");
            healthMonitor.setDelay(2);
            healthMonitor.setPath("/");
            healthMonitor.setTimeout(10);

            lb.setProtocol(LoadBalancerProtocol.HTTP);
            lb.setAccountId(21323);
            lb.setId(1);
            lb.setHealthMonitor(new HealthMonitor());

            when(lbRepository.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(lb);

            LoadBalancer reqLb = new LoadBalancer();
            reqLb.setAccountId(21323);
            reqLb.setId(1);
            reqLb.setHealthMonitor(healthMonitor);

            when(lbRepository.testAndSetStatus(lb.getAccountId(), lb.getId(), LoadBalancerStatus.PENDING_UPDATE, false)).thenReturn(true);
            when(lbRepository.update(lb)).thenReturn(lb);

            healthMonitorService.update(reqLb);
            verify(lbRepository, Mockito.times(1)).update(lb);
        }

        @Test
        public void shouldSucceedForHttpCreateFull() throws BadRequestException, EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException {
            healthMonitor.setType(HealthMonitorType.HTTP);
            healthMonitor.setAttemptsBeforeDeactivation(1);
            healthMonitor.setBodyRegex(".*");
            healthMonitor.setStatusRegex(".*");
            healthMonitor.setDelay(2);
            healthMonitor.setPath("/");
            healthMonitor.setTimeout(10);

            lb.setProtocol(LoadBalancerProtocol.HTTP);
            lb.setAccountId(21323);
            lb.setId(1);
            lb.setHealthMonitor(new HealthMonitor());

            when(lbRepository.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(lb);

            LoadBalancer reqLb = new LoadBalancer();
            reqLb.setAccountId(21323);
            reqLb.setId(1);
            reqLb.setHealthMonitor(healthMonitor);

            when(lbRepository.testAndSetStatus(lb.getAccountId(), lb.getId(), LoadBalancerStatus.PENDING_UPDATE, false)).thenReturn(true);
            when(lbRepository.update(lb)).thenReturn(lb);

            healthMonitorService.update(reqLb);
            verify(lbRepository, Mockito.times(1)).update(lb);
        }
    }
}
