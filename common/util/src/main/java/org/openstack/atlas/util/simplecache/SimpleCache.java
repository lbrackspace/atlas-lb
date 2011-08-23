package org.openstack.atlas.util.simplecache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SimpleCache<E> {

    private int count = 0;
    private int cleanExpiredOn = -1;
    private Map<String, CacheEntry<E>> cache;
    private long ttl;

    public SimpleCache() {
        cache = new HashMap<String, CacheEntry<E>>();
        this.ttl = 300; // Use negative values to specify No expiration
    }

    public int cleanExpiredByCount() {
        boolean shouldClean=false;
        synchronized(this){
            if (cleanExpiredOn > 0 && count >= cleanExpiredOn) {
                count = 0;
                shouldClean = true;
            }
        }
        if (shouldClean) {
            return removeExpired();
        } else {
            return -1;
        }
    }

    public SimpleCache(long ttl) {
        cache = new HashMap<String, CacheEntry<E>>();
        this.ttl = ttl;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public void put(String key, E val) {
        CacheEntry entry = new CacheEntry<E>();
        entry.setVal(val);
        entry.setTtl(ttl);
        entry.setUpdated();
        synchronized (this) {
            cache.put(key, entry);
        }
    }

    public CacheEntry<E> getEntry(String key) {
        CacheEntry<E> entry;
        synchronized (this) {
            entry = cache.get(key);
        }
        return entry;
    }

    public boolean remove(String key) {
        synchronized (this) {
            if (cache.containsKey(key)) {
                cache.remove(key);
                return true;
            }
        }
        return false;
    }

    public boolean containsKey(String key) {
        boolean out;
        synchronized (this) {
            out = cache.containsKey(key);
        }
        return out;
    }

    public Set<String> getKeySet() {
        Set<String> keys;
        synchronized (this) {
            keys = new HashSet<String>(cache.keySet());
        }
        return keys;
    }

    public Set<String> getExpiredKeySet() {
        Set<String> keys = new HashSet<String>();
        Set<Entry<String, CacheEntry<E>>> entrySet;
        synchronized (this) {
            entrySet = new HashSet<Entry<String, CacheEntry<E>>>(cache.entrySet());
        }
        for (Entry<String, CacheEntry<E>> e : entrySet) {
            if (e.getValue().isExpired()) {
                keys.add(e.getKey());
            }
        }
        return keys;
    }

    public int numExpiredKeys() {
        int expiredCount = 0;
        Set<Entry<String, CacheEntry<E>>> entrySet;
        synchronized (this) {
            entrySet = new HashSet<Entry<String, CacheEntry<E>>>(cache.entrySet());
            for (Entry<String, CacheEntry<E>> e : entrySet) {
                if (e.getValue().isExpired()) {
                    expiredCount++;
                }
            }
        }
        return expiredCount;
    }

    public void clear() {
        cache.clear();
    }

    public Map<String, Long> expiresIn() {
        Map<String, Long> out = new HashMap<String, Long>();

        Set<Entry<String, CacheEntry<E>>> entrySet;
        synchronized (this) {
            entrySet = new HashSet<Entry<String, CacheEntry<E>>>(cache.entrySet());
        }
        for (Entry<String, CacheEntry<E>> e : entrySet) {
            String key = e.getKey();
            CacheEntry<E> cacheEntry = e.getValue();
            out.put(key, cacheEntry.expiresIn());
        }
        return out;
    }

    public Map<String, CacheEntry<E>> getExpiredEntriesAndRemoveFromCache() {
        Map<String, CacheEntry<E>> out = new HashMap<String, CacheEntry<E>>();
        synchronized (this) {
            Set<Entry<String, CacheEntry<E>>> entrySet;
            entrySet = new HashSet<Entry<String, CacheEntry<E>>>(cache.entrySet());
            for (Entry<String, CacheEntry<E>> e : entrySet) {
                String key = e.getKey();
                CacheEntry<E> cacheEntry = e.getValue();
                if (cacheEntry.isExpired()) {
                    out.put(key, cacheEntry);
                    cache.remove(key);
                }
            }
        }
        return out;
    }

    public int removeExpired() {
        int n = 0;
        synchronized (this) {
            Set<Entry<String, CacheEntry<E>>> entrySet;
            entrySet = new HashSet<Entry<String, CacheEntry<E>>>(cache.entrySet());
            for (Entry<String, CacheEntry<E>> e : entrySet) {
                String key = e.getKey();
                CacheEntry<E> cacheEntry = e.getValue();
                if (cacheEntry.isExpired()) {
                    cache.remove(key);
                    n++;
                }
            }
        }
        return n;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCleanExpiredOn() {
        return cleanExpiredOn;
    }

    public void setCleanExpiredOn(int cleanExpiredOn) {
        this.cleanExpiredOn = cleanExpiredOn;
    }
}
