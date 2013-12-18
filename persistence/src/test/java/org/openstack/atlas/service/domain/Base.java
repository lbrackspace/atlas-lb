package org.openstack.atlas.service.domain;

import org.junit.After;
import org.junit.Before;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.AccountLimitRepository;
import org.openstack.atlas.service.domain.repository.ClusterRepository;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.services.AccountLimitService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.VirtualIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;

@Service
@ContextConfiguration(locations = {"classpath:loadbalancing-persistence-test.xml", "classpath:loadbalancing-usage-persistence-test.xml"})
public class Base {

    @Autowired
    protected AccountLimitService accountLimitService;
    @Autowired
    protected LoadBalancerService loadBalancerService;
    @Autowired
    protected VirtualIpService virtualIpService;

    @Autowired
    protected ClusterRepository clusterRepository;
    @Autowired
    protected HostRepository hostRepository;
    @Autowired
    protected AccountLimitRepository accountLimitRepository;
    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;

    protected LoadBalancer loadBalancer;
    protected Integer accountId = 1234;

    @Before
    public void standUp() throws Exception {
        Cluster testCluster = setupTestCluster();
        Host testHost = setupTestHost(testCluster);

        LimitType loadbalancerLimitType = setupLoadBalancerLimit();
        LimitType nodeLimitType = setupNodeLimit();
        setupAccountLimits(loadbalancerLimitType, nodeLimitType);

        loadBalancerService.setDefaultErrorPage("defaultErrorPage");
        virtualIpService.addAccountRecord(accountId);
    }

    @After
    public void tearDown() throws Exception {
    }

    private void setupAccountLimits(LimitType loadbalancerLimitType, LimitType nodeLimitType) throws BadRequestException {
        AccountLimit lbLimitForAccount = new AccountLimit();
        lbLimitForAccount.setAccountId(accountId);
        lbLimitForAccount.setLimitType(loadbalancerLimitType);
        lbLimitForAccount.setLimit(20);

        try {
            accountLimitService.getLimit(accountId, AccountLimitType.LOADBALANCER_LIMIT);
        } catch (EntityNotFoundException e) {
            accountLimitService.save(lbLimitForAccount);
        }

        AccountLimit nodeLimitForAccount = new AccountLimit();
        nodeLimitForAccount.setAccountId(accountId);
        nodeLimitForAccount.setLimitType(nodeLimitType);
        nodeLimitForAccount.setLimit(20);

        try {
            accountLimitService.getLimit(accountId, AccountLimitType.NODE_LIMIT);
        } catch (EntityNotFoundException e) {
            accountLimitService.save(nodeLimitForAccount);
        }
    }

    private LimitType setupNodeLimit() {
        LimitType nodeLimitType = new LimitType();
        nodeLimitType.setName(AccountLimitType.NODE_LIMIT);
        nodeLimitType.setDescription("Node Limit");
        nodeLimitType.setDefaultValue(20);

        if (accountLimitRepository.getAllLimitTypes().size() < 2) {
            accountLimitRepository.save(nodeLimitType);
        }

        return nodeLimitType;
    }

    private LimitType setupLoadBalancerLimit() {
        LimitType loadbalancerLimitType = new LimitType();
        loadbalancerLimitType.setName(AccountLimitType.LOADBALANCER_LIMIT);
        loadbalancerLimitType.setDescription("LB Limit");
        loadbalancerLimitType.setDefaultValue(20);

        if (accountLimitRepository.getAllLimitTypes().size() < 2) {
            accountLimitRepository.save(loadbalancerLimitType);
        }

        return loadbalancerLimitType;
    }

    private Host setupTestHost(Cluster cluster) {
        Host host = new Host();
        host.setCluster(cluster);
        host.setName("test host");
        host.setHostStatus(HostStatus.ACTIVE_TARGET);
        host.setMaxConcurrentConnections(1);
        host.setCoreDeviceId("someId");
        host.setManagementIp("10.0.0.1");
        host.setEndpoint("endpoint");
        host.setTrafficManagerName("trafficManagerName");

        if (hostRepository.getAll().isEmpty()) {
            hostRepository.save(host);
        }

        return host;
    }

    private Cluster setupTestCluster() {
        Cluster cluster = new Cluster();
        cluster.setDataCenter(DataCenter.DFW);
        cluster.setDescription("cluster description");
        cluster.setName("testCluster");
        cluster.setPassword("cluster password");
        cluster.setUsername("cluster username");
        cluster.setStatus(org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterStatus.ACTIVE);
        cluster.setClusterIpv6Cidr("2001:4801:79f1:2::/64");

        if (clusterRepository.getAll().isEmpty()) {
            clusterRepository.save(cluster);
        }

        return cluster;
    }
}
