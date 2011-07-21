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
        this.updated = nowInSeconds();
        this.val = val;
    }

    public E getVal() {
        return val;
    }

    public void setVal(E val) {
        this.updated = nowInSeconds();
        this.val = val;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = nowInSeconds();
    }

    public long expiresIn(long ttl){
        long expires = ttl + updated;
        long rightNow = nowInSeconds();
        long secondsLeft = expires - rightNow;
        return secondsLeft;
    }

    public boolean isExpired(long ttl){
        long expiresin = expiresIn(ttl);
        if(expiresin<0){
            return true;
        }
        return false;
    }

    public final long nowInSeconds(){
        long seconds = Calendar.getInstance().getTimeInMillis()/1000;
        return seconds;
    }
}
