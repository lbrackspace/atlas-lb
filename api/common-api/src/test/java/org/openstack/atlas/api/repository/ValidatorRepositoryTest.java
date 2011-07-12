package org.openstack.atlas.api.repository;

import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.api.validation.validators.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class ValidatorRepositoryTest {
    public static class WhenUsingTheRepositoryProvider {
        ValidatorRepository repository;

        @Before
        public void standUp() {
            repository = new ValidatorRepository();
        }

        @Test
        public void should_provide_appropriate_validator() {
            assertTrue(repository.getValidatorFor(LoadBalancer.class) instanceof LoadBalancerValidator);
            assertTrue(repository.getValidatorFor(AccessList.class) instanceof AccessListValidator);
            assertTrue(repository.getValidatorFor(HealthMonitor.class) instanceof HealthMonitorValidator);
            assertTrue(repository.getValidatorFor(NetworkItem.class) instanceof NetworkItemValidator);
            assertTrue(repository.getValidatorFor(Nodes.class) instanceof NodesValidator);
            assertTrue(repository.getValidatorFor(Node.class) instanceof NodeValidator);
            assertTrue(repository.getValidatorFor(SessionPersistence.class) instanceof SessionPersistenceValidator);
            assertTrue(repository.getValidatorFor(VirtualIps.class) instanceof VirtualIpsValidator);
            assertTrue(repository.getValidatorFor(VirtualIp.class) instanceof VirtualIpValidator);
        }
    }
}


