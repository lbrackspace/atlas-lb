package org.openstack.atlas.api.validation.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;

import java.util.GregorianCalendar;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition.ENABLED;
import static org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType.HTTP_COOKIE;

@RunWith(Enclosed.class)
public class LoadBalancerValidatorTest {

	public static class whenValidatingPost {

		private LoadBalancerValidator validator;
		private Node node1;
		private Node node2;
		private LoadBalancer lb;
        private VirtualIp vip = new VirtualIp();

		@Before
		public void setUpValidator() {
			validator = new LoadBalancerValidator();
		}

		@Before
		public void setupValidLoadBalancerObjects() {
			lb = new LoadBalancer();
			lb.setName("a-new-loadbalancer");
			lb.setProtocol("HTTP");

			vip.setType(VipType.PUBLIC);

			lb.getVirtualIps().add(vip);

			node1 = new Node();
			node1.setAddress("10.1.1.1");
			node1.setPort(80);
			node1.setCondition(ENABLED);

			node2 = new Node();
			node2.setAddress("10.1.1.2");
			node2.setPort(80);
			node2.setCondition(ENABLED);

			lb.getNodes().add(node1);
			lb.getNodes().add(node2);
		}

		@Test
		public void shouldReturnTrueWhenGivenAValidMinimalLoadBalancer() {
			ValidatorResult result = validator.validate(lb, POST);
			assertTrue(result.passedValidation());
		}

		@Test
		public void shouldReturnTrueWhenGivenAValidFullyHydratedLoadBalancer() {
			lb.setAlgorithm("ROUND_ROBIN");
			lb.setProtocol("HTTP");
			lb.setPort(80);
//            ConnectionLogging conLog = new ConnectionLogging();
//            conLog.setEnabled(true);
//			lb.setConnectionLogging(conLog);

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

			ValidatorResult result = validator.validate(lb, POST);
			assertTrue(result.passedValidation());
		}

        @Test
        public void shouldRejectIdForVipIfTypeIsSet() {
           vip.setId(23);
            
           ValidatorResult result = validator.validate(lb, POST);
           assertFalse(result.passedValidation());

        }

        @Test
        public void shouldAcceptIdOnly() {
            lb.getVirtualIps().clear();
            VirtualIp vipper = new VirtualIp();
            vipper.setId(23);
            lb.getVirtualIps().add(vipper);

            ValidatorResult result = validator.validate(lb, POST);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectTypeIfIdIsSet() {
            lb = new LoadBalancer();
            VirtualIp vipper = new VirtualIp();
            vipper.setId(23);
            vipper.setType(VipType.SERVICENET);
            lb.getVirtualIps().add(vipper);
            
            ValidatorResult result = validator.validate(lb, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectMultipleVips() {
            lb.getVirtualIps().clear();

            VirtualIp vip = new VirtualIp();
            vip.setId(23);
            vip.setType(VipType.SERVICENET);
            VirtualIp vip2 = new VirtualIp();
            vip.setId(43);
            vip.setType(VipType.SERVICENET);

            lb.getVirtualIps().add(vip);
            lb.getVirtualIps().add(vip2);

           ValidatorResult result = validator.validate(lb, POST);
           assertFalse(result.passedValidation());

        }

		@Test
		public void shouldFailWhenGivenAnInvalidHTTPHealthMonitor() {
			HealthMonitor monitor = new HealthMonitor();
			monitor.setType(HealthMonitorType.HTTP);
			monitor.setDelay(10);
			monitor.setTimeout(60);
			monitor.setAttemptsBeforeDeactivation(3);
			lb.setHealthMonitor(monitor);

			ValidatorResult result = validator.validate(lb, PUT);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldFailWhenGivenAnInvalidConnectHealthMonitor() {
			HealthMonitor monitor = new HealthMonitor();
			monitor.setType(HealthMonitorType.CONNECT);
			lb.setHealthMonitor(monitor);

			ValidatorResult result = validator.validate(lb, PUT);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldAcceptAlgorithm() {
			lb.setAlgorithm("RANDOM");
			ValidatorResult result = validator.validate(lb, POST);
			assertTrue(result.passedValidation());
		}

		@Test
		public void shouldAcceptPort() {
			lb.setPort(90);
			ValidatorResult result = validator.validate(lb, POST);
			assertTrue(result.passedValidation());
		}

		@Test
		public void shouldAcceptSessionPersistence() {
			SessionPersistence persistence = new SessionPersistence();
			persistence.setPersistenceType(HTTP_COOKIE);
			lb.setSessionPersistence(persistence);

			ValidatorResult result = validator.validate(lb, POST);
			assertTrue(result.passedValidation());
		}

		@Test
		public void shouldAcceptConnectionLimits() {
			ConnectionThrottle throttle = new ConnectionThrottle();
			throttle.setMinConnections(10);
			throttle.setMaxConnections(100);
			throttle.setMaxConnectionRate(60);
			throttle.setRateInterval(45);
			lb.setConnectionThrottle(throttle);

			ValidatorResult result = validator.validate(lb, POST);
			assertTrue(result.passedValidation());
		}

		@Test
		public void shouldRejectNullProtocol() {
			lb.setProtocol(null);
			ValidatorResult result = validator.validate(lb, POST);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldRejectWhenMissingVirtualIps() {
			lb.getVirtualIps().clear();
			ValidatorResult result = validator.validate(lb, POST);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldAcceptWhenMissingNodes() {
			lb.getNodes().clear();
			ValidatorResult result = validator.validate(lb, POST);
			assertTrue(result.passedValidation());
		}

		@Test
		public void shouldRejectId() {
			lb.setId(Integer.SIZE);
			ValidatorResult result = validator.validate(lb, POST);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldRejectStatus() {
			lb.setStatus("BUILD");
			ValidatorResult result = validator.validate(lb, POST);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldRejectCluster() {
			lb.setCluster(new Cluster());
			ValidatorResult result = validator.validate(lb, POST);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldRejectCreated() {
            Created created = new Created();
            created.setTime(new GregorianCalendar());
			lb.setCreated(created);
			ValidatorResult result = validator.validate(lb, POST);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldRejectUpdated() {
            Updated updated = new Updated();
            updated.setTime(new GregorianCalendar());
            lb.setUpdated(updated);
			ValidatorResult result = validator.validate(lb, POST);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldRejectEmptyName() {
			lb.setName(null);
			ValidatorResult result = validator.validate(lb, POST);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldHaveSameErrorsForMultipleValidations() {
			ValidatorResult result = validator.validate(new LoadBalancer(),
					POST);
			int numMessagesFirstPass = result.getValidationErrorMessages()
					.size();
			result = validator.validate(new LoadBalancer(), POST);
			int numMessagesSecondPass = result.getValidationErrorMessages()
					.size();
			assertEquals(numMessagesFirstPass, numMessagesSecondPass);
		}

        @Test
        public void shouldRejectIdWhenVipTypeIsSet() {
            VirtualIp vip = new VirtualIp();
            vip.setId(1234);
            vip.setType(VipType.PUBLIC);
            lb.getVirtualIps().clear();
            lb.getVirtualIps().add(vip);

            ValidatorResult result = validator.validate(lb, POST);
			assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenDuplicateNodesDetected() {
            Node node = new Node();
			node.setAddress("10.1.1.1");
			node.setPort(80);
			node.setCondition(ENABLED);
            lb.getNodes().add(node);

            ValidatorResult result = validator.validate(lb, POST);
			assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectNameLongerThan128Characters() {
            lb.setName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            ValidatorResult result = validator.validate(lb, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectTimeoutOverValue() {
            lb.setTimeout(150);
            ValidatorResult result = validator.validate(lb, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectTimeoutUnderValue() {
            lb.setTimeout(20);
            ValidatorResult result = validator.validate(lb, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldAcceptValidTimeout() {
            lb.setTimeout(70);
            ValidatorResult result = validator.validate(lb, POST);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectSslTerminationElement() {
            lb.setSslTermination(new SslTermination());
            ValidatorResult result = validator.validate(lb, POST);
            assertFalse(result.passedValidation());
        }
	}

	public static class whenValidatingPut {
		private LoadBalancerValidator validator;
		private LoadBalancer lb;

		@Before
		public void setUpValidator() {
			validator = new LoadBalancerValidator();
		}

		@Before
		public void setupValidLoadBalancerObject() {
			lb = new LoadBalancer();
			lb.setName("a-new-loadbalancer");
			lb.setProtocol("HTTP");
			lb.setPort(800);
			lb.setAlgorithm("RANDOM");
//            ConnectionLogging conLog = new ConnectionLogging();
//            conLog.setEnabled(true);
//			lb.setConnectionLogging(conLog);
        }

		@Test
		public void shouldFailWhenNoAttributesToUpdate() {
			ValidatorResult result = validator
					.validate(new LoadBalancer(), PUT);
			assertFalse(result.passedValidation());
		}

        @Test
		public void shouldPassWhenOnlyTimeoutToUpdate() {
            lb.setTimeout(50);
			ValidatorResult result = validator
					.validate(lb, PUT);
			assertTrue(result.passedValidation());
		}

		@Test
		public void shouldPassWhenAtLeastOneAttributeToUpdate() {
			lb.setPort(6);
			lb.setName(null);
			lb.setAlgorithm(null);
			lb.setProtocol(null);
//			lb.setConnectionLogging(null);
			ValidatorResult result = validator.validate(lb, PUT);
			assertTrue(result.passedValidation());
		}

		@Test
		public void shouldRejectWhenNodesIsNotEmpty() {
                        lb.getNodes().add(new Node());
			ValidatorResult result = validator.validate(lb, PUT);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldRejectWhenVirtualIpsIsNotEmpty() {
                        lb.getVirtualIps().add(new org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp());
			ValidatorResult result = validator.validate(lb, PUT);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldRejectId() {
			lb.setId(32);
			ValidatorResult result = validator.validate(lb, PUT);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldRejectStatus() {
			lb.setStatus("BUILD");
			ValidatorResult result = validator.validate(lb, PUT);
			assertFalse(result.passedValidation());
		}

//		@Test
//		public void shouldRejectCurrentUsage() {
//			lb.setCurrentUsage(new CurrentUsage());
//			ValidatorResult result = validator.validate(lb, PUT);
//			assertFalse(result.passedValidation());
//		}

		@Test
		public void shouldRejectCluster() {
			lb.setCluster(new Cluster());
			ValidatorResult result = validator.validate(lb, PUT);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldRejectCreated() {
            Created created = new Created();
            created.setTime(new GregorianCalendar());
			lb.setCreated(created);
			ValidatorResult result = validator.validate(lb, PUT);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldRejectUpdated() {
            Updated updated = new Updated();
            updated.setTime(new GregorianCalendar());
			lb.setUpdated(updated);
			ValidatorResult result = validator.validate(lb, PUT);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldRejectSessionPersistence() {
			SessionPersistence persistence = new SessionPersistence();
			persistence.setPersistenceType(HTTP_COOKIE);
			lb.setSessionPersistence(persistence);

			ValidatorResult result = validator.validate(lb, PUT);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldRejectConnectionLimits() {
			ConnectionThrottle throttle = new ConnectionThrottle();
			throttle.setMinConnections(10);
			throttle.setMaxConnections(100);
			throttle.setMaxConnectionRate(60);
			throttle.setRateInterval(45);
			lb.setConnectionThrottle(throttle);

			ValidatorResult result = validator.validate(lb, PUT);
			assertFalse(result.passedValidation());
		}

		@Test
		public void shouldAcceptPort() {
			lb = new LoadBalancer();
			lb.setPort(80);
			ValidatorResult result = validator.validate(lb, PUT);
			assertTrue(result.passedValidation());
		}

		@Test
		public void shouldAcceptAlgorithm() {
			lb = new LoadBalancer();
			lb.setAlgorithm("RANDOM");
			ValidatorResult result = validator.validate(lb, PUT);
			assertTrue(result.passedValidation());
		}

		@Test
		public void shouldAcceptProtocol() {
			lb = new LoadBalancer();
			lb.setProtocol("HTTP");
			ValidatorResult result = validator.validate(lb, PUT);
			assertTrue(result.passedValidation());
		}

		@Test
		public void shouldAcceptName() {
			lb = new LoadBalancer();
			lb.setName("Biased load BALANCER. Ha!");
			ValidatorResult result = validator.validate(lb, PUT);
			assertTrue(result.passedValidation());
		}

		@Test
		public void shouldRejectConnectionLogging() {
			lb = new LoadBalancer();
            ConnectionLogging conLog = new ConnectionLogging();
            conLog.setEnabled(false);
			lb.setConnectionLogging(conLog);
			ValidatorResult result = validator.validate(lb, PUT);
			assertFalse(result.passedValidation());
		}

        @Test
        public void shouldRejectNameLongerThan128Characters() {
            lb.setName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            ValidatorResult result = validator.validate(lb, PUT);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectTimeoutOverValue() {
            lb.setTimeout(150);
            ValidatorResult result = validator.validate(lb, PUT);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectTimeoutUnderValue() {
            lb.setTimeout(20);
            ValidatorResult result = validator.validate(lb, PUT);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldAcceptValidTimeout() {
            lb.setTimeout(70);
            ValidatorResult result = validator.validate(lb, PUT);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectSslTerminationElement() {
            lb.setSslTermination(new SslTermination());
            ValidatorResult result = validator.validate(lb, PUT);
            assertFalse (result.passedValidation());
        }
	}
}
