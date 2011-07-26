package org.openstack.atlas.service.domain.services;


import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.impl.HealthMonitorServiceImpl;

import java.util.HashSet;
import java.util.Set;

@RunWith(Enclosed.class)
public class HealthMonitorServiceImplTest {

    public static class HealthMonitorProtocols {
        HealthMonitorServiceImpl healthMonitorService;
        LoadBalancer lb;
        LoadBalancer lb2;
        LoadBalancerJoinVip lbjv;
        Set<LoadBalancerJoinVip> lbjvs;
        VirtualIp vip;
        HealthMonitor healthMonitor;
        HealthMonitor healthMonitor2;

        @Before
        public void standUp() {
            healthMonitorService = new HealthMonitorServiceImpl();
        }

        @Before
        public void standUpObjects() {
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
    }
}
