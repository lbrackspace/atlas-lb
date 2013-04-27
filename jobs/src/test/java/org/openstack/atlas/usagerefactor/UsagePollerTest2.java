package org.openstack.atlas.usagerefactor;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRefactorRepository;
import org.openstack.atlas.usagerefactor.generator.UsagePollerGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
    To see what each case is testing please refer to their respective xml
    file for more information.
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations = {"classpath:context.xml"})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
public class UsagePollerTest2 {

    @Autowired
    private HostUsageRefactorRepository hostUsageRefactorRepository;

    @MockitoAnnotations.Mock
    private HostService hostService;

    @MockitoAnnotations.Mock
    private SnmpUsageCollector snmpUsageCollector;

    @InjectMocks
    private UsagePoller usagePoller = new UsagePollerImpl();

    @MockitoAnnotations.Mock
    private UsageRefactorService usageRefactorService;

    @MockitoAnnotations.Mock
    private HostRepository hostRepository;

    private static final int NUM_HOSTS = 2;
    private static final int NUM_LBS = 3;
    private static final int FIRST_LB_ID = 123;
    private Calendar firstPollTime = new GregorianCalendar(2013, 4, 13, 11, 1, 0);;

    private List<Host> hostList;
    private Map<Integer, Map<Integer, SnmpUsage>> snmpMap;
    private Map<Integer, List<LoadBalancerHostUsage>> lbHostMap;

    @Before
    public void standUp() throws Exception {
//        hostList = UsagePollerGenerator.generateHosts(NUM_HOSTS);
//        snmpMap = UsagePollerGenerator.generateSnmpMap(NUM_HOSTS, NUM_LBS);
//        lbHostMap = UsagePollerGenerator.generateLoadBalancerHostUsageMap(NUM_HOSTS,
//                        NUM_LBS, 1, firstPollTime, FIRST_LB_ID);
//        when(hostService.getAllHosts()).thenReturn(hostList);
//        when(hostRepository.getAllHosts()).thenReturn(hostList);
//        when(snmpUsageCollector.getCurrentData()).thenReturn(snmpMap);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case3.xml")
    public void test() {
        int i = 0;
    }
}