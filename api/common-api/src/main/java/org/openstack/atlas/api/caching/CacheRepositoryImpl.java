package org.openstack.atlas.api.caching;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import java.io.Serializable;

public class CacheRepositoryImpl<K, V> implements CacheRepository<K, V> {
    private final String cacheName;
    private CacheManager cacheManager;

    public CacheRepositoryImpl(String cacheName, CacheManagerFactory cacheManagerFactory) {
        this.cacheName = cacheName;
        this.cacheManager = CacheManagerFactory.get();
    }

    @Override
    public V get(final K key) {
        Element element = getCache().get(key);
        if (element != null) {
            return (V) element.getValue();
        }
        return null;
    }

    @Override
    public <T extends Serializable> void put(final K key, final V value, long timeToLiveSeconds) {
        getCache().put(new Element(key, value, timeToLiveSeconds));
    }

    @Override
    public void clear(K key) {
       getCache().remove(key);
    }

    public Ehcache getCache() {
        return cacheManager.getEhcache(cacheName);
    }

}
