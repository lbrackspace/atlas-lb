package org.openstack.atlas.service.domain.services;


import org.junit.Assert;
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

        @Test()
        public void shouldReturnFaultIfUpdatieMissingFieldsConnect() {
            healthMonitor.setType(HealthMonitorType.CONNECT);
            lb.setProtocol(LoadBalancerProtocol.HTTP);
            try {
                healthMonitorService.verifyMonitorUpdateRestrictions(healthMonitor, null);
            } catch (BadRequestException ex) {
                Assert.assertEquals("Please provide all the required fields when creating a CONNECT health monitor:" +
                        "'attemptsBeforeDeactivation', 'delay', 'timeout' and 'type'", ex.getMessage());
            }
        }

        @Test()
        public void shouldReturnFaultIfUpdatieMissingFieldsHttp() {
            healthMonitor.setType(HealthMonitorType.HTTP);
            lb.setProtocol(LoadBalancerProtocol.HTTP);
            try {
                healthMonitorService.verifyMonitorUpdateRestrictions(healthMonitor, null);
            } catch (BadRequestException ex) {
                Assert.assertEquals("Please provide all the required fields when creating an HTTP(S) health monitor:" +
                        "'attemptsBeforeDeactivation', 'delay', 'timeout', 'type', 'path', 'statusRegex' and 'bodyRegex'", ex.getMessage());
            }
        }

        @Test
        public void shouldReturnFaultIfUpdatieMissingFieldsForConnectToHttp() {
            healthMonitor.setType(HealthMonitorType.HTTP);
            healthMonitor2.setType(HealthMonitorType.CONNECT);
            lb.setProtocol(LoadBalancerProtocol.HTTP);
            try {
                healthMonitorService.verifyMonitorUpdateRestrictions(healthMonitor, healthMonitor2);
            } catch (BadRequestException ex) {
                Assert.assertEquals("Updating from CONNECT monitor. Please provide the additional required fields for HTTP(S) health monitor: " +
                        "'path', 'statusRegex' and 'bodyRegex'", ex.getMessage());
            }
        }

        @Test
        public void shouldSuccedForHttpCreate() throws BadRequestException {
            healthMonitor.setType(HealthMonitorType.HTTP);
            lb.setProtocol(LoadBalancerProtocol.HTTP);
            healthMonitor.setAttemptsBeforeDeactivation(1);
            healthMonitor.setBodyRegex(".*");
            healthMonitor.setStatusRegex(".*");
            healthMonitor.setDelay(2);
            healthMonitor.setPath("/");
            healthMonitor.setTimeout(10);
            // Should not throw junit5 has an assert for this
            healthMonitorService.verifyMonitorUpdateRestrictions(healthMonitor, null);
        }

        @Test
        public void shouldSuccedForConnectCreate() throws BadRequestException {
            healthMonitor.setType(HealthMonitorType.CONNECT);
            lb.setProtocol(LoadBalancerProtocol.HTTP);
            healthMonitor.setAttemptsBeforeDeactivation(1);
            healthMonitor.setDelay(2);
            healthMonitor.setTimeout(10);
            // Should not throw junit5 has an assert for this
            healthMonitorService.verifyMonitorUpdateRestrictions(healthMonitor, null);
        }

        @Test
        public void shouldSuccedForConnectToHttpUpdate() throws BadRequestException {
            healthMonitor.setType(HealthMonitorType.HTTP);
            healthMonitor2.setType(HealthMonitorType.CONNECT);
            lb.setProtocol(LoadBalancerProtocol.HTTP);
            healthMonitor.setAttemptsBeforeDeactivation(1);
            healthMonitor.setBodyRegex(".*");
            healthMonitor.setStatusRegex(".*");
            healthMonitor.setDelay(2);
            healthMonitor.setPath("/");
            healthMonitor.setTimeout(10);
            // Should not throw junit5 has an assert for this
            healthMonitorService.verifyMonitorUpdateRestrictions(healthMonitor, healthMonitor2);
        }

        @Test
        public void shouldSuccedForHttpToConnectUpdate() throws BadRequestException {
            healthMonitor.setType(HealthMonitorType.CONNECT);
            healthMonitor2.setType(HealthMonitorType.HTTP);
            lb.setProtocol(LoadBalancerProtocol.HTTP);
            healthMonitor.setAttemptsBeforeDeactivation(1);
            healthMonitor.setDelay(2);
            healthMonitor.setTimeout(10);
            // Should not throw junit5 has an assert for this
            healthMonitorService.verifyMonitorUpdateRestrictions(healthMonitor, healthMonitor2);
        }
    }
}
