package org.openstack.atlas.api.caching;

import net.sf.ehcache.CacheManager;

public class CacheManagerFactory {
    public static CacheManager get() {
        return CacheManager.create();
    }
}
