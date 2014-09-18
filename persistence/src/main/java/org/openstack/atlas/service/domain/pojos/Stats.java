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
    protected int[] currentConn;
    protected int[] dataTimedOut;
    protected int[] keepAliveTimedOut;
    protected int[] maxConn;
    protected int[] connectTimeOutSsl;
    protected int[] connectErrorSsl;
    protected int[] connectFailureSsl;
    protected int[] currentConnSsl;
    protected int[] dataTimedOutSsl;
    protected int[] keepAliveTimedOutSsl;
    protected int[] maxConnSsl;

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

    public int[] getCurrentConn() {
        return currentConn;
    }

    public void setCurrentConn(int[] currentConn) {
        this.currentConn = currentConn;
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

    public int[] getConnectTimeOutSsl() {
        return connectTimeOutSsl;
    }

    public void setConnectTimeOutSsl(int[] connectTimeOutSsl) {
        this.connectTimeOutSsl = connectTimeOutSsl;
    }

    public int[] getConnectErrorSsl() {
        return connectErrorSsl;
    }

    public void setConnectErrorSsl(int[] connectErrorSsl) {
        this.connectErrorSsl = connectErrorSsl;
    }

    public int[] getConnectFailureSsl() {
        return connectFailureSsl;
    }

    public void setConnectFailureSsl(int[] connectFailureSsl) {
        this.connectFailureSsl = connectFailureSsl;
    }

    public int[] getCurrentConnSsl() {
        return currentConnSsl;
    }

    public void setCurrentConnSsl(int[] currentConnSsl) {
        this.currentConnSsl = currentConnSsl;
    }

    public int[] getDataTimedOutSsl() {
        return dataTimedOutSsl;
    }

    public void setDataTimedOutSsl(int[] dataTimedOutSsl) {
        this.dataTimedOutSsl = dataTimedOutSsl;
    }

    public int[] getKeepAliveTimedOutSsl() {
        return keepAliveTimedOutSsl;
    }

    public void setKeepAliveTimedOutSsl(int[] keepAliveTimedOutSsl) {
        this.keepAliveTimedOutSsl = keepAliveTimedOutSsl;
    }

    public int[] getMaxConnSsl() {
        return maxConnSsl;
    }

    public void setMaxConnSsl(int[] maxConnSsl) {
        this.maxConnSsl = maxConnSsl;
    }
}
