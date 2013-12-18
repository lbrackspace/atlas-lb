package org.openstack.atlas.logs.hadoop.sequencefiles;


import org.apache.hadoop.io.Writable;


public class SequenceFileEntry <K extends Writable,V extends Writable> {
    private String sequenceFileName;
    private int entryNumber;
    private K key;
    private V value;

    public SequenceFileEntry(){
    }

    public SequenceFileEntry(String sequenceFileName,int entryNumber,K key,V value){
        this.sequenceFileName = sequenceFileName;
        this.entryNumber = entryNumber;
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

    public String getSequenceFileName() {
        return sequenceFileName;
    }

    public void setSequenceFileName(String sequenceFileName) {
        this.sequenceFileName = sequenceFileName;
    }

    public int getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(int entryNumber) {
        this.entryNumber = entryNumber;
    }
}