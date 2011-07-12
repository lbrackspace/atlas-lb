package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.api.mgmt.validation.contexts.ReassignHostContext;
import org.openstack.atlas.api.mgmt.validation.validators.LoadBalancerValidator;
import org.openstack.atlas.api.mgmt.validation.validators.LoadBalancersValidator;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition.ENABLED;
import static org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType.HTTP_COOKIE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class LoadBalancersValidatorTest {

    public static class whenValidatinReassignHost {
        private LoadBalancerValidator lbvalidator;
        private LoadBalancersValidator lbsvalidator;
        private LoadBalancer loadBalancer;
        private LoadBalancers loadBalancers;
        private LoadBalancer lb;
        private VirtualIp vip;
        private Node node;
        private Host host;

        @Before
        public void standUp() {
            lbvalidator = new LoadBalancerValidator();
            lbsvalidator = new LoadBalancersValidator();
            loadBalancer = new LoadBalancer();
            loadBalancers = new LoadBalancers();

            loadBalancer.setId(23);
            loadBalancers.getLoadBalancers().add(loadBalancer);
        }

        @Test
        public void shouldAcceptValidLBForReassignHost() {
            LoadBalancer loadbalancer = new LoadBalancer();
            host = new Host();
            host.setId(23);
            loadbalancer.setId(23);
            loadbalancer.setHost(host);

            loadBalancers.getLoadBalancers().add(loadbalancer);

            ValidatorResult result = lbsvalidator.validate(loadBalancers, ReassignHostContext.REASSIGN_HOST);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectLBAttributesForReassignHost() {
            LoadBalancer lb = new LoadBalancer();
            lb.setPort(23);
            lb.setName("name");
            lb.setProtocol("HTTP");

            loadBalancers.getLoadBalancers().add(lb);

            ValidatorResult result = lbsvalidator.validate(loadBalancers, ReassignHostContext.REASSIGN_HOST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectEmptyLbs() {
            LoadBalancers lbs = new LoadBalancers();

            ValidatorResult result = lbsvalidator.validate(lbs, ReassignHostContext.REASSIGN_HOST);
            assertFalse(result.passedValidation());

        }

        @Test
        public void shourRejectInvalidHydratedLbForReassignHost() {
            lb = new org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer();
            lb.setName("a-new-loadbalancer");
            lb.setProtocol("HTTP");

            lb.getVirtualIps().add(vip);

            node = new Node();
            node.setAddress("10.1.1.1");
            node.setPort(80);
            node.setCondition(ENABLED);

            lb.getNodes().add(node);
            lb.setAlgorithm("ROUND_ROBIN");
            lb.setProtocol("HTTP");
            lb.setPort(80);
            ConnectionLogging conLog = new ConnectionLogging();
            conLog.setEnabled(true);
            lb.setConnectionLogging(conLog);

            SessionPersistence persistence = new SessionPersistence();
            persistence.setPersistenceType(HTTP_COOKIE);
            lb.setSessionPersistence(persistence);

            ConnectionThrottle throtttle = new ConnectionThrottle();
            throtttle.setMinConnections(10);
            throtttle.setMaxConnections(100);
            throtttle.setMaxConnectionRate(60);
            throtttle.setRateInterval(45);
            lb.setConnectionThrottle(throtttle);

            HealthMonitor monitor = new HealthMonitor();
            monitor.setType(HealthMonitorType.CONNECT);
            monitor.setDelay(10);
            monitor.setTimeout(60);
            monitor.setAttemptsBeforeDeactivation(3);
            lb.setHealthMonitor(monitor);

            AccessList aList = new AccessList();
            NetworkItem nItem = new NetworkItem();
            nItem.setAddress("10.10.10.10");
            nItem.setType(NetworkItemType.ALLOW);

            aList.getNetworkItems().add(nItem);
            loadBalancers.getLoadBalancers().add(lb);

            ValidatorResult result = lbsvalidator.validate(loadBalancers, ReassignHostContext.REASSIGN_HOST);
            assertFalse(result.passedValidation());
        }

       // TODO: write more test
    }
}
