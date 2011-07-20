package org.openstack.atlas.util.simplecache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleCache<E> {

    Map<String, CacheEntry<E>> cache;
    private long ttl;

    public SimpleCache() {
        cache = new HashMap<String, CacheEntry<E>>();
        this.ttl = -1; // Use negative values to specify No expiration
    }

    public SimpleCache(long ttl) {
        cache = new HashMap<String, CacheEntry<E>>();
        this.ttl = ttl * 10;
    }

    public void put(String key, E val) {
        CacheEntry entry = new CacheEntry<E>();
        entry.setVal(val);
        synchronized (this) {
            cache.put(key, entry);
        }
    }

    public E get(String key) {
        CacheEntry entry;
        E val = null;
        synchronized (this) {
            entry = cache.get(key);
            val = (E) ((entry == null) ? null : entry.getVal());
            if (ttl > 0 && entry != null && entry.isExpired(ttl)) {
                val = null;
                cache.remove(key); // This entry expired so get rid of it.
            }
        }
        return val;
    }

    public Set<String> keySet() {
        Set<String> keys;
        synchronized (this) {
            keys = new HashSet<String>(this.cache.keySet());
        }
        return keys;
    }

    public Set<String> expiredKeys() {
        Set<String> expired = new HashSet<String>();
        if (ttl > 0) {
            synchronized (this) {
                for (String key : new ArrayList<String>(this.cache.keySet())) {
                    CacheEntry entry = cache.get(key);
                    if (entry != null && entry.isExpired(ttl)) {
                        expired.add(key);
                    }
                }
            }
        }
        return expired;
    }

    public void clean() {
        if (ttl <= 0) {
            return;
        }
        synchronized (this) {
            for (String key : new ArrayList<String>(this.cache.keySet())) {
                CacheEntry entry = cache.get(key);
                if (entry != null && entry.isExpired(ttl)) {
                    cache.remove(key);
                }
            }
        }

    }

    public void clear() {
        synchronized (this) {
            cache.clear();
        }
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }
}
