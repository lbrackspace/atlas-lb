package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.api.mgmt.validation.contexts.ReassignHostContext;
import org.openstack.atlas.api.mgmt.validation.validators.LoadBalancerValidator;
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
public class LoadBalancerValidatorTest {

    public static class whenValidatingReassignHost {
        private LoadBalancerValidator lbvalidator;
        private LoadBalancer loadBalancer;
        private LoadBalancer lb;
        private VirtualIp vip;
        private Node node;
        private Host host;

        @Before
        public void standUp() {
            lbvalidator = new LoadBalancerValidator();
            loadBalancer = new LoadBalancer();
            loadBalancer.getVirtualIps().clear();
            host = new Host();
            host.setId(23);

            loadBalancer.setId(23);
            loadBalancer.setHost(host);

        }

        @Test
        public void shouldAcceptValidLBForReassignHost() {
            ValidatorResult result = lbvalidator.validate(loadBalancer, ReassignHostContext.REASSIGN_HOST);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectInvalidLBAttributesForReassignHost() {
            LoadBalancer lb = new LoadBalancer();
            lb.setPort(23);
            lb.setName("name");
            lb.setProtocol("HTTP");

            ValidatorResult result = lbvalidator.validate(lb, ReassignHostContext.REASSIGN_HOST);
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
            ValidatorResult result = lbvalidator.validate(lb, ReassignHostContext.REASSIGN_HOST);
            assertFalse(result.passedValidation());
        }
        // TODO: add more test when i get a chance......
    }
}
