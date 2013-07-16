package org.openstack.atlas.adapter.stm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entities.SslTermination;

@RunWith(Enclosed.class)
public class StmAdapterImplTest extends STMTestBase{

    public static class LoadBalancerOperationsTest {
        @Test
        public void test() {

        }

    }

    public static class VirtualServerOperationsTest {
        @Test
        public void test() {

        }
    }

    public static class PoolOperationsTest {
        @Before
        public void standUp() {
           Boolean hasSsl = true;
           setupIvars();
           SslTermination ssl = new SslTermination();
           ssl.setEnabled(hasSsl);

           lb.setSslTermination(ssl);
        }

        @After
        public void tearDown() {

        }

        @Test
        public void testSetNodes() {

        }


    }

    public static class VirtualIpOperationsTest {
        @Test
        public void test() {

        }
    }

    public static class HealthMonitorOperationsTest {
        @Test
        public void test() {

        }
    }

    public static class ProtectionOperationsTest {
        @Test
        public void test() {

        }
    }

    public static class SslTerminationOperationsTest {
        @Test
        public void test() {

        }
    }

    public static class RateLimitOperationsTest {
        @Test
        public void test() {

        }
    }

    public static class ErrorFileOperationsTest {
        @Test
        public void test() {

        }
    }

    public static class SubnetMappingOperationsTest {
        @Test
        public void test() {

        }
    }

}
