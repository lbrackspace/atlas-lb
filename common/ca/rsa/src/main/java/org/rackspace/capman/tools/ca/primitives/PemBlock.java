package org.rackspace.capman.tools.ca.primitives;

import java.util.Arrays;

public class PemBlock {

    private int lineNum;
    private int startByte;
    private int stopByte;
    private String startLine;
    private String endLine;
    private byte[] pemData;
    private Object decodedObject;

    public PemBlock() {
        pemData = null;
        decodedObject = null;
        lineNum = -1;
        startByte = -1;
        stopByte = -1;
    }

    public PemBlock(int lineNum, byte[] pemData, Object decodedObject) {
        this.lineNum = lineNum;
        this.decodedObject = decodedObject;
        if (pemData == null) {
            this.pemData = null;
            return;
        }
        this.pemData = Arrays.copyOf(pemData, pemData.length);

    }

    public byte[] getPemData() {
        return pemData;
    }

    public void setPemData(byte[] pemData) {
        int i;
        if (pemData == null) {
            this.pemData = null;
        } else {
            this.pemData = Arrays.copyOf(pemData, pemData.length);
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

    public int getStartByte() {
        return startByte;
    }

    public void setStartByte(int startByte) {
        this.startByte = startByte;
    }

    public int getStopByte() {
        return stopByte;
    }

    public void setStopByte(int stopByte) {
        this.stopByte = stopByte;
    }

    public String getStartLine() {
        return startLine;
    }

    public void setStartLine(String startLine) {
        this.startLine = startLine;
    }

    public String getEndLine() {
        return endLine;
    }

    public void setEndLine(String endLine) {
        this.endLine = endLine;
    }
}
