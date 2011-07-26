package org.openstack.atlas.util.simplecache;

import java.util.Calendar;

public class CacheEntry <E>{
    private long updated;
    private long ttl;
    private E val;

    public CacheEntry(){
        updated = nowInSeconds();
        ttl = 300;
        val = null;
    }

    public CacheEntry(long ttl){
        updated = nowInSeconds();
        this.ttl = ttl;
        val = null;
    }

    public CacheEntry(long ttl,E val){
        updated = nowInSeconds();
        this.ttl = ttl;
        this.val = val;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        setUpdated();
    }

    public void setUpdated(){
        this.updated = nowInSeconds();
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public E getVal() {
        return val;
    }

    public void setVal(E val) {
        this.val = val;
    }


    private long nowInSeconds(){
        long seconds = Calendar.getInstance().getTimeInMillis()/1000;
        return seconds;
    }


    public long expiresIn(){
        return ttl + updated - nowInSeconds();
    }

    public boolean isExpired(){
        return (expiresIn()<0);
    }
}
