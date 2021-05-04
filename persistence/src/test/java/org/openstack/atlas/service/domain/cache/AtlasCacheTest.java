package org.openstack.atlas.service.domain.cache;

import net.spy.memcached.MemcachedClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.cfg.ServicesConfigurationKeys;
import org.openstack.atlas.service.domain.exceptions.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@RunWith(Enclosed.class)
public class AtlasCacheTest {

    @RunWith(PowerMockRunner.class)
    @PrepareForTest(AtlasCache.class)
    @PowerMockIgnore("javax.management.*")
    public static class WhenVerifyingCachingBehaviors {

        @Mock
        RestApiConfiguration restApiConfiguration;
        @Mock
        MemcachedClient memcachedClient;

        AtlasCache atlasCache;

        @Before
        public void standUp() throws Exception {
            restApiConfiguration = mock(RestApiConfiguration.class);
            memcachedClient = mock(MemcachedClient.class);

            when(restApiConfiguration.hasKeys(ServicesConfigurationKeys.memcached_servers)).thenReturn(Boolean.TRUE);
            when(restApiConfiguration.getString(ServicesConfigurationKeys.memcached_servers)).thenReturn("localhost:11211");
            when(restApiConfiguration.hasKeys(ServicesConfigurationKeys.ttl)).thenReturn(Boolean.TRUE);
            when(restApiConfiguration.getString(ServicesConfigurationKeys.ttl)).thenReturn("300");

            when(memcachedClient.get(anyString())).thenReturn("memecachedstring");
            PowerMockito.whenNew(MemcachedClient.class).withAnyArguments().thenReturn(memcachedClient);

            atlasCache = new AtlasCache(restApiConfiguration);


        }

        @Test(expected = MissingFieldException.class)
        public void ShouldFailToReturnCachedItemForUnmodifiedConfig() throws IOException {
            when(restApiConfiguration.hasKeys(ServicesConfigurationKeys.memcached_servers)).thenReturn(Boolean.FALSE);
            atlasCache = new AtlasCache(restApiConfiguration);
            Assert.assertEquals("memecachedstring", atlasCache.get("testmemecache"));
            verify(memcachedClient, times(1)).get("testmemecache");

        }

        @Test
        public void ShouldReturnCachedItemForUnmodifiedConfig() {
            Assert.assertEquals("memecachedstring", atlasCache.get("testmemecache"));
            verify(memcachedClient, times(1)).get("testmemecache");
        }

        @Test
        public void ShouldReturnCachedItemForModifiedConfig() {
            when(restApiConfiguration.getString(ServicesConfigurationKeys.ttl)).thenReturn("200");

            Assert.assertEquals("memecachedstring", atlasCache.get("testmemecache"));
            verify(memcachedClient, times(1)).get("testmemecache");
            verify(memcachedClient, times(1)).shutdown();

        }

        @Test
        public void ShouldContainKeyForModifiedConfig() {
            when(restApiConfiguration.getString(ServicesConfigurationKeys.memcached_servers)).thenReturn("localhost:11212");

            Assert.assertTrue(atlasCache.containsKey("testmemecache"));
            verify(memcachedClient, times(1)).get("testmemecache");
            verify(memcachedClient, times(0)).shutdown();

        }

        @Test
        public void ShouldSetKeyForModifiedConfig() {
            when(restApiConfiguration.getString(ServicesConfigurationKeys.memcached_servers)).thenReturn("localhost:11212");

            atlasCache.set("testmemecache", "memecachedstring");
            verify(memcachedClient, times(1)).set("testmemecache", 300, "memecachedstring");
            verify(memcachedClient, times(0)).shutdown();

        }
    }
}