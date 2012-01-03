package org.rackexp.ca.primitives;

public class PemBlock {
    private int lineNum;
    private byte[]  pemData;
    private Object decodedObject;

    public PemBlock(){
        pemData = null;
        decodedObject = null;
    }

    public PemBlock(int lineNum,byte[] pemData,Object decodedObject){
        int i;
        this.lineNum = lineNum;
        this.decodedObject = decodedObject;
        for(i=0;i<pemData.length;i++){
            this.pemData[i] = pemData[i];
        }
    }

    public byte[] getPemData() {
        return pemData;
    }

    public void setPemData(byte[] pemData) {
        int i;
        if(pemData==null){
            this.pemData = null;
        }
        this.pemData = new byte[pemData.length];
        for(i=0;i<pemData.length;i++){
            this.pemData[i] = pemData[i];
        }
    }

    public Object getDecodedObject() {
        return decodedObject;
    }

    public void setDecodedObject(Object decodedObject) {
        this.decodedObject = decodedObject;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

}
