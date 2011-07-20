package org.openstack.atlas.util.simplecache;

import java.util.Calendar;

public class CacheEntry <E>{
    private long updated;
    private E val;
    public CacheEntry(){
        updated = 0;
        val = null;
    }

    public CacheEntry(E val){
        this.updated = Calendar.getInstance().getTimeInMillis();
        this.val = val;
    }

    public E getVal() {
        return val;
    }

    public void setVal(E val) {
        this.updated = Calendar.getInstance().getTimeInMillis();
        this.val = val;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public boolean isExpired(long ttl){
        long expires = ttl*1000 + updated;
        long rightNow = Calendar.getInstance().getTimeInMillis();
        if(rightNow > expires){
            return true;
        }
        return false;
    }
}
