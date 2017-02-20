package org.openstack.atlas.api.mapper.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle;
import org.openstack.atlas.service.domain.entities.ConnectionLimit;
import org.openstack.atlas.util.constants.ConnectionThrottleDefaultContants;


@RunWith(Enclosed.class)
public class DataModelToThrottleDomainTest {

    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";

    public static class When_mapping_connection_throtal{

        private DozerBeanMapper mapper;
        private ConnectionThrottle apiConnectionThrottle;
        private ConnectionLimit dbConnectionLimit;

        @Before
        public void standUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
        }

        @Test
        public void shouldAlwaysMapAllNullValuesToDefaultValues() {
            apiConnectionThrottle = new ConnectionThrottle();
            dbConnectionLimit = mapper.map(apiConnectionThrottle, ConnectionLimit.class);
            assertExpectationsToDatabase(apiConnectionThrottle, dbConnectionLimit);
        }

        @Test
        public void shouldAlwaysMapNullMaxConnectionRate() {
            apiConnectionThrottle = new ConnectionThrottle();
            apiConnectionThrottle.setMaxConnections(2);
            apiConnectionThrottle.setMinConnections(3);
            apiConnectionThrottle.setRateInterval(4);

            dbConnectionLimit = mapper.map(apiConnectionThrottle, ConnectionLimit.class);
            assertExpectationsToDatabase(apiConnectionThrottle, dbConnectionLimit);
        }
        
        @Test
        public void shouldAlwaysMapNullMinConnections() {
            apiConnectionThrottle = new ConnectionThrottle();
            apiConnectionThrottle.setMaxConnectionRate(1);
            apiConnectionThrottle.setMaxConnections(2);
            apiConnectionThrottle.setRateInterval(4);
            dbConnectionLimit = mapper.map(apiConnectionThrottle, ConnectionLimit.class);
            assertExpectationsToDatabase(apiConnectionThrottle, dbConnectionLimit);
        }

        @Test
        public void shouldAlwaysMapNullRateInterval(){
            apiConnectionThrottle = new ConnectionThrottle();
            apiConnectionThrottle.setMaxConnectionRate(1);
            apiConnectionThrottle.setMaxConnections(2);
            apiConnectionThrottle.setMinConnections(3);
            dbConnectionLimit = mapper.map(apiConnectionThrottle, ConnectionLimit.class);
            assertExpectationsToDatabase(apiConnectionThrottle, dbConnectionLimit);
        }

        @Test
        public void testReverseMapStillWorks(){
            dbConnectionLimit = new ConnectionLimit();
            dbConnectionLimit.setMaxConnectionRate(1);
            dbConnectionLimit.setMaxConnections(2);
            dbConnectionLimit.setMinConnections(3);
            dbConnectionLimit.setRateInterval(4);
            apiConnectionThrottle = mapper.map(dbConnectionLimit, ConnectionThrottle.class);
            Assert.assertEquals(apiConnectionThrottle.getMaxConnectionRate(), dbConnectionLimit.getMaxConnectionRate());
            Assert.assertEquals(apiConnectionThrottle.getMaxConnections(), dbConnectionLimit.getMaxConnections());
            Assert.assertEquals(apiConnectionThrottle.getMinConnections(), dbConnectionLimit.getMinConnections());
            Assert.assertEquals(apiConnectionThrottle.getRateInterval(), dbConnectionLimit.getRateInterval());
        }

        private void assertExpectationsToDatabase(
                ConnectionThrottle apiCt, ConnectionLimit dbCl) {
            // These properties were deprecated so these default values should
            // be expected on null
            if (apiCt.getMaxConnectionRate() == null) {
                // Default value should be 0
                Assert.assertEquals(dbCl.getMaxConnectionRate(), ConnectionThrottleDefaultContants.getMaxConnectionRate());
            } else {
                Assert.assertEquals(dbCl.getMaxConnectionRate(), apiCt.getMaxConnectionRate());
            }

            if (apiCt.getMinConnections() == null) {
                // Should default to 0
                Assert.assertEquals(dbCl.getMinConnections(), ConnectionThrottleDefaultContants.getMinConnections());
            } else {
                Assert.assertEquals(dbCl.getMinConnections(), apiCt.getMinConnections());
            }

            if (apiCt.getRateInterval() == null) {
                // Should Default to 1
                Assert.assertEquals(dbCl.getRateInterval(), ConnectionThrottleDefaultContants.getRateInterval());
            } else {
                Assert.assertEquals(dbCl.getRateInterval(), apiCt.getRateInterval());
            }

            // Lastly assert that the maxConnections are equal.
            // Another validator is already testing that this value is not null.
            Assert.assertEquals(apiCt.getMaxConnections(), dbCl.getMaxConnections());
        }
    }
}
