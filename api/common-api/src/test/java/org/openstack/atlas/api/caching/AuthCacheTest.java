package org.openstack.atlas.api.caching;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class AuthCacheTest {
    @Ignore
    public abstract static class AuthCacheTestBase {
        CacheManager cacheManager = new CacheManager();
        Cache cache;

        @Before
        public void setUp() {
            cacheManager.removalAll();
            cache = new Cache("testCache", 20000, true, false, 5, 2);
            cacheManager.addCache((Ehcache) cache);
            continueSetup();
        }

        @After
        public void tearDown() {
            cacheManager.shutdown();
        }

        public abstract void continueSetup();
    }

    public static class WhenVerifyingConfiguration extends AuthCacheTestBase {
        @Override
        public void continueSetup() {
            cache = cacheManager.getCache("testCache");
        }

        @Test
        public void should_be_able_to_retrieves_a_custom_cache() {
            Assert.assertNotNull(cache);
        }

        @Test
        public void should_be_able_to_get_all_cache_names() {
            String[] cacheNames = cacheManager.getCacheNames();
            Assert.assertEquals(1, cacheNames.length);
            Assert.assertEquals("testCache", cacheNames[0]);
        }
    }

    public static class WhenWritingValuesToTheCache extends AuthCacheTestBase {
        Element element;

        @Override
        public void continueSetup() {
            cache = cacheManager.getCache("testCache");
            element = new Element("key1", "value1");
            cache.put(element);
        }

        @Test
        public void should_be_able_to_retrieves_the_custom_value_by_a_key() {
            Assert.assertEquals(element, cache.get("key1"));
        }
    }
}
