package org.openstack.atlas.service.domain.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entities.BlacklistItem;
import org.openstack.atlas.service.domain.services.impl.BlackListServiceImpl;

import java.util.ArrayList;
import java.util.List;

@RunWith(Enclosed.class)
public class BlackListServiceImplTest {
    public static class testingBlacklists {
        BlackListServiceImpl blackListService;
        List<BlacklistItem> blacklist;

        @Before
        public void setUp() {
            blackListService = new BlackListServiceImpl();
            
            blacklist = new ArrayList<BlacklistItem>();
        }

        @Test
        public void shouldReturnTrueWhenEmptyBlacklist() {

        }
    }
}

/*
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.impl.HealthMonitorServiceImpl;

import java.util.HashSet;
import java.util.Set;

@RunWith(Enclosed.class)
public class HealthMonitorServiceImplTest {

    public static class HealthMonitorProtocols {

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
*/