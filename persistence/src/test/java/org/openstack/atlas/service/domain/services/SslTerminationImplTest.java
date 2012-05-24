package org.openstack.atlas.service.domain.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.SslTerminationRepository;
import org.openstack.atlas.service.domain.services.impl.LoadBalancerServiceImpl;
import org.openstack.atlas.service.domain.services.impl.SslTerminationServiceImpl;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
public class SslTerminationImplTest {

    public static class sslTerminationOperations {
        Integer accountId = 1234;
        LoadBalancerRepository lbRepository;
        SslTerminationRepository sslTerminationRepository;
        SslTerminationService sslTerminationService;
        LoadBalancerService loadBalancerService;
        LoadBalancer lb;
        LoadBalancer lb2;
        LoadBalancerJoinVip lbjv;
        Set<LoadBalancerJoinVip> lbjvs;
        VirtualIp vip;
        SslTermination ssl1;
        SslTermination ssl2;

        @Before
        public void standUp() {
            lbRepository = mock(LoadBalancerRepository.class);
            sslTerminationRepository = mock(SslTerminationRepository.class);
            sslTerminationService = new SslTerminationServiceImpl();
            loadBalancerService = new LoadBalancerServiceImpl();
        }

        @Before
        public void standUpObjects() {
            lb = new LoadBalancer();
            lb2 = new LoadBalancer();
            lbjv = new LoadBalancerJoinVip();
            lbjvs = new HashSet<LoadBalancerJoinVip>();
            vip = new VirtualIp();
            ssl1 = new SslTermination();
            ssl2 = new SslTermination();

            ssl1.setCertificate("aCert");
            ssl1.setPrivatekey("aKey");
            ssl1.setEnabled(true);
            ssl1.setSecurePort(443);
            ssl1.setSecureTrafficOnly(false);

            ssl2.setCertificate("aCert2");
            ssl2.setPrivatekey("aKey2");
            ssl2.setEnabled(true);
            ssl2.setSecurePort(446);
            ssl2.setSecureTrafficOnly(false);

//            lb.setSslTermination(ssl1);
//            lb2.setSslTermination(ssl2);

            vip.setIpAddress("192.3.3.3");
            lbjv.setVirtualIp(vip);
            lbjvs.add(lbjv);
            lb.setLoadBalancerJoinVipSet(lbjvs);
        }

        //Testing components in helpers test
        @Ignore
        @Test
        public void shouldReturnFalseIfNoTermination() throws EntityNotFoundException {
            Assert.assertFalse(sslTerminationService.getSslTermination(lb.getId(), accountId) == null);
        }

        @Ignore
        @Test
        public void shouldReturnTrueWhenSslTerminationIsValid() throws EntityNotFoundException, BadRequestException, ImmutableEntityException, UnprocessableEntityException {
            try {
                Assert.assertTrue(sslTerminationService.updateSslTermination(lb.getId(), accountId, ssl1) != null);
            } catch (Exception ex) {
                Assert.fail(ex.toString());
            }
        }

        @Ignore
        @Test
        public void shouldFailWhenSslTerminationDidNotPassChecks() throws EntityNotFoundException, BadRequestException, ImmutableEntityException, UnprocessableEntityException {
            try {
                Assert.assertFalse(sslTerminationService.updateSslTermination(lb.getId(), accountId, ssl1) == null);
            } catch (Exception ex) {
                Assert.fail(ex.toString());
            }
        }
    }
}
