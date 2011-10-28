package org.openstack.atlas.service.domain.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.pojo.VirtualIpDozerWrapper;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.stub.StubFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;

@RunWith(Enclosed.class)
public class LoadBalancerServiceITest {

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenCreatingALoadBalancer extends Base {

        @Test(expected = PersistenceException.class)
        public void shouldThrowExceptionWhenLoadBalancerIsNull() throws Exception {
            loadBalancer = new LoadBalancer();
            loadBalancerService.create(loadBalancer);
        }

        @Test
        public void shouldAssignIdLoadBalancerWhenCreateSucceeds() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            Assert.assertNotNull(dbLoadBalancer.getId());
        }

        @Test
        public void shouldPutInQueuedStatusWhenCreateSucceeds() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            Assert.assertEquals(dbLoadBalancer.getStatus(), CoreLoadBalancerStatus.QUEUED);
        }

        @Test
        public void shouldAllocateVirtualIpsWhenCreateSucceeds() throws PersistenceServiceException {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            Assert.assertTrue(!dbLoadBalancer.getLoadBalancerJoinVipSet().isEmpty() || !dbLoadBalancer.getLoadBalancerJoinVip6Set().isEmpty());
        }

        @Test
        public void shouldRetrieveLoadBalancerByIdAndAccountId() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            LoadBalancer loadBalancer = loadBalancerRepository.getByIdAndAccountId(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());
            Assert.assertNotNull(loadBalancer.getId());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenRetrievingLoadBalancerByWrongAccountId() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            loadBalancerRepository.getByIdAndAccountId(dbLoadBalancer.getId(), -99999);
        }

    }


    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenDeletingLoadBalancer extends Base {

        @Test(expected = BadRequestException.class)
        public void shouldThrowExceptionWhenLoadBalancerDoesntExist() throws Exception {
            loadBalancer.setId(-999);
            loadBalancerService.preDelete(loadBalancer.getAccountId(), loadBalancer.getId());
        }

        @Test
        public void shouldPutInPendingDeleteStatusWhenPreDeleteSucceeds() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            dbLoadBalancer = loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ACTIVE);

            loadBalancerService.preDelete(loadBalancer.getAccountId(), dbLoadBalancer.getId());
            dbLoadBalancer = loadBalancerRepository.getById(dbLoadBalancer.getId());
            Assert.assertEquals(dbLoadBalancer.getStatus(), CoreLoadBalancerStatus.PENDING_DELETE);
        }

        @Test
        public void shouldPutInDeletedStatusWhenDeleteSucceeds() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            dbLoadBalancer = loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ACTIVE);

            loadBalancerService.delete(dbLoadBalancer);
            dbLoadBalancer = loadBalancerRepository.getById(dbLoadBalancer.getId());
            Assert.assertEquals(dbLoadBalancer.getStatus(), CoreLoadBalancerStatus.DELETED);
        }

        @Test(expected = BadRequestException.class)
        public void shouldThrowExceptionWhenDeletingImmutableLoadBalancer() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            dbLoadBalancer = loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.PENDING_UPDATE);

            loadBalancerService.preDelete(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId());
        }
    }
}

