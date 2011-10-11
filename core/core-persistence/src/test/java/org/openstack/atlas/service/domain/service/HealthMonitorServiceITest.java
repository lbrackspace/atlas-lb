package org.openstack.atlas.service.domain.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.entity.HealthMonitor;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.repository.HealthMonitorRepository;
import org.openstack.atlas.service.domain.stub.StubFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(Enclosed.class)
public class HealthMonitorServiceITest {

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenCreatingAHealthMonitor extends Base {
        @Autowired
        private HealthMonitorService healthMonitorService;

        @Autowired
        private HealthMonitorRepository healthMonitorRepository;

        private HealthMonitor healthMonitor;

        @Before
        public void setUp() throws PersistenceServiceException {
            loadBalancer = loadBalancerService.create(loadBalancer);
            loadBalancerRepository.changeStatus(loadBalancer, CoreLoadBalancerStatus.ACTIVE);
            healthMonitor = StubFactory.createHydratedDomainHealthMonitor();
        }

        @Test
        public void shouldAssignIdAndValuesWhenUpdateSucceeds() throws PersistenceServiceException {
            HealthMonitor dbHealthMonitor = healthMonitorService.update(loadBalancer.getId(), healthMonitor);
            Assert.assertNotNull(dbHealthMonitor.getId());
            Assert.assertEquals(loadBalancer.getId(), dbHealthMonitor.getLoadBalancer().getId());
            Assert.assertEquals(healthMonitor.getType(), dbHealthMonitor.getType());
            Assert.assertEquals(healthMonitor.getTimeout(), dbHealthMonitor.getTimeout());
            Assert.assertEquals(healthMonitor.getDelay(), dbHealthMonitor.getDelay());
            Assert.assertEquals(healthMonitor.getAttemptsBeforeDeactivation(), dbHealthMonitor.getAttemptsBeforeDeactivation());
            Assert.assertEquals(healthMonitor.getPath(), dbHealthMonitor.getPath());

        }

        @Test
        public void shouldUseDefaultValuesWhenHealthMonitorIsEmpty() throws PersistenceServiceException {
            healthMonitor = new HealthMonitor();
            healthMonitor = healthMonitorService.update(loadBalancer.getId(), healthMonitor);

            HealthMonitor defaultMonitor = new HealthMonitor();
            Assert.assertEquals(defaultMonitor.getType(), healthMonitor.getType());
            Assert.assertEquals(defaultMonitor.getTimeout(), healthMonitor.getTimeout());
            Assert.assertEquals(defaultMonitor.getDelay(), healthMonitor.getDelay());
            Assert.assertEquals(defaultMonitor.getAttemptsBeforeDeactivation(), healthMonitor.getAttemptsBeforeDeactivation());
            Assert.assertEquals(defaultMonitor.getPath(), healthMonitor.getPath());
        }

        @Test
        public void shouldPutLbInPendingUpdateStatusWhenCreateSucceeds() throws Exception {
            healthMonitorService.update(loadBalancer.getId(), healthMonitor);
            LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(loadBalancer.getId());
            Assert.assertEquals(dbLoadBalancer.getStatus(), CoreLoadBalancerStatus.PENDING_UPDATE);
        }

        @Test
        public void shouldRetrieveHealthMonitorById() throws PersistenceServiceException {
            healthMonitorService.update(loadBalancer.getId(), healthMonitor);
            Assert.assertNotNull(healthMonitorRepository.getByLoadBalancerId(loadBalancer.getId()));
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenRetrievingByWrongLoadBalancerId() throws PersistenceServiceException {
            healthMonitorService.update(loadBalancer.getId(), healthMonitor);
            healthMonitorRepository.getByLoadBalancerId(-99999);
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenUpdatingWithWrongLoadBalancerId() throws PersistenceServiceException {
            healthMonitorService.update(-99999, healthMonitor);
        }

        @Test(expected = NullPointerException.class)
        public void shouldThrowExceptionWhenUpdatingWithNullHealthMonitor() throws PersistenceServiceException {
            healthMonitorService.update(loadBalancer.getId(), null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowExceptionWhenUpdatingWithNullLoadBalancerId() throws PersistenceServiceException {
            healthMonitorService.update(null, healthMonitor);
        }
    }


    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenPreDeletingAHealthMonitor extends Base {
        @Autowired
        private HealthMonitorService healthMonitorService;

        private HealthMonitor healthMonitor;

        @Before
        public void setUp() throws PersistenceServiceException {
            loadBalancer = loadBalancerService.create(loadBalancer);
            loadBalancerRepository.changeStatus(loadBalancer, CoreLoadBalancerStatus.ACTIVE);
            healthMonitor = StubFactory.createHydratedDomainHealthMonitor();
        }

        @Test
        public void shouldSetStatusToPendingUpdateWhenOperationSucceeds() throws PersistenceServiceException {
            healthMonitorService.update(loadBalancer.getId(), healthMonitor);
            healthMonitorService.preDelete(loadBalancer.getId());
            LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(loadBalancer.getId());
            Assert.assertEquals(dbLoadBalancer.getStatus(), CoreLoadBalancerStatus.PENDING_UPDATE);
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenHealthMonitorDoesNotExist() throws PersistenceServiceException {
            healthMonitorService.preDelete(loadBalancer.getId());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenLoadBalancerDoesNotExist() throws PersistenceServiceException {
            healthMonitorService.preDelete(-99999);
        }
    }


    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenDeletingAHealthMonitor extends Base {
        @Autowired
        private HealthMonitorService healthMonitorService;

        @Autowired
        private HealthMonitorRepository healthMonitorRepository;

        private HealthMonitor healthMonitor;

        @Before
        public void setUp() throws PersistenceServiceException {
            loadBalancer = loadBalancerService.create(loadBalancer);
            loadBalancerRepository.changeStatus(loadBalancer, CoreLoadBalancerStatus.ACTIVE);
            healthMonitor = StubFactory.createHydratedDomainHealthMonitor();
            healthMonitor = healthMonitorService.update(loadBalancer.getId(), healthMonitor);
            healthMonitorService.preDelete(loadBalancer.getId());
            healthMonitorService.delete(loadBalancer.getId());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldNotExistWhenDeleteSucceeds() throws PersistenceServiceException {
            healthMonitor = healthMonitorRepository.getByLoadBalancerId(loadBalancer.getId());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenHealthMonitorDoesntExist() throws PersistenceServiceException {
            healthMonitorService.delete(loadBalancer.getId());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenDeletingFromNonExistentLoadBalancer() throws PersistenceServiceException {
            healthMonitorService.delete(-99999);
        }
    }
}
