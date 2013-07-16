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

    }

    public static class VirtualServerOperationsTest {

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

    }

    public static class HealthMonitorOperationsTest {

    }

    public static class ProtectionOperationsTest {

    }

    public static class SslTerminationOperationsTest {

    }

    public static class RateLimitOperationsTest {

    }

    public  static class ErrorFileOperationsTest {

    }

    public static class SubnetMappingOperationsTest {

    }

}
