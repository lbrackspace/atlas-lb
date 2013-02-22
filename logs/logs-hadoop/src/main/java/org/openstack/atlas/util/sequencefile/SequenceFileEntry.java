package org.openstack.atlas.util.sequencefile;


import org.apache.hadoop.io.Writable;


public class SequenceFileEntry <K extends Writable,V extends Writable>{
    private K key;
    private V value;

    public SequenceFileEntry(){
    }

    public SequenceFileEntry(K key,V value){
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

}