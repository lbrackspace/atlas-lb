package org.openstack.atlas.logs.itest;

public class IdCounter {
    private long id = 0;
    private long count = 0;

    public IdCounter(){
    }

    public IdCounter(long id,long count){
        this.id = id;
        this.count = count;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
