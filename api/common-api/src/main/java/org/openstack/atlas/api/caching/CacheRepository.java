package org.openstack.atlas.api.caching;

import java.io.Serializable;

public interface CacheRepository<K, V> {
    V get(K key);

    <T extends Serializable> void put(K key, V value, long timeToLiveSeconds);
    
    void clear(K key);
}
