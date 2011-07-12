package org.openstack.atlas.osgi.cfg;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class ContextConfigurationTest {
    public static class WhenGettingConfigurationValues {
        private static final String EXPECTED_CONFIG_VALUE = "expected";

        private Configuration config;

        @Before
        public void standUp() {
            config = mock(Configuration.class);

            when(config.getString(any(ConfigurationKey.class))).thenReturn(EXPECTED_CONFIG_VALUE);
            when(config.hasKeys(any(ConfigurationKey.class))).thenReturn(Boolean.FALSE);
        }

        @Test
        public void shouldReturnValues() {
            assertEquals(EXPECTED_CONFIG_VALUE, config.getString(ConfigurationKeys.KEY_NUMBER_ONE));
        }

        @Test
        public void shouldReturnFalseIfKeyHasNoValue() {
            assertEquals(Boolean.FALSE, config.hasKeys(ConfigurationKeys.KEY_NUMBER_ONE));
        }
    }
}
