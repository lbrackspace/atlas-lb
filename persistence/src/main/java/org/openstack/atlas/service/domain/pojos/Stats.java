package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;

public class Stats implements Serializable {
    private final static long serialVersionUID = 532512316L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    protected int[] connectTimeOut;
    protected int[] connectError;
    protected int[] connectFailure;
    protected int[] dataTimedOut;
    protected int[] keepAliveTimedOut;
    protected int[] maxConn;

    public int[] getConnectTimeOut() {
        return connectTimeOut;
    }

    public void setConnectTimeOut(int[] connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    public int[] getConnectError() {
        return connectError;
    }

    public void setConnectError(int[] connectError) {
        this.connectError = connectError;
    }

    public int[] getConnectFailure() {
        return connectFailure;
    }

    public void setConnectFailure(int[] connectFailure) {
        this.connectFailure = connectFailure;
    }

    public int[] getDataTimedOut() {
        return dataTimedOut;
    }

    public void setDataTimedOut(int[] dataTimedOut) {
        this.dataTimedOut = dataTimedOut;
    }

    public int[] getKeepAliveTimedOut() {
        return keepAliveTimedOut;
    }

    public void setKeepAliveTimedOut(int[] keepAliveTimedOut) {
        this.keepAliveTimedOut = keepAliveTimedOut;
    }

    public int[] getMaxConn() {
        return maxConn;
    }

    public void setMaxConn(int[] maxConn) {
        this.maxConn = maxConn;
    }
}
