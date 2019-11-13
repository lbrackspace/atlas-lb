package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;

public class Stats implements Serializable {
    private final static long serialVersionUID = 532512316L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    protected long connectTimeOut;
    protected long connectError;
    protected long connectFailure;
    protected long currentConn;
    protected long dataTimedOut;
    protected long keepAliveTimedOut;
    protected long maxConn;
    protected long connectTimeOutSsl;
    protected long connectErrorSsl;
    protected long connectFailureSsl;
    protected long currentConnSsl;
    protected long dataTimedOutSsl;
    protected long keepAliveTimedOutSsl;
    protected long maxConnSsl;

    public long getConnectTimeOut() {
        return connectTimeOut;
    }

    public void setConnectTimeOut(long connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    public long getConnectError() {
        return connectError;
    }

    public void setConnectError(long connectError) {
        this.connectError = connectError;
    }

    public long getConnectFailure() {
        return connectFailure;
    }

    public void setConnectFailure(long connectFailure) {
        this.connectFailure = connectFailure;
    }

    public long getCurrentConn() {
        return currentConn;
    }

    public void setCurrentConn(long currentConn) {
        this.currentConn = currentConn;
    }

    public long getDataTimedOut() {
        return dataTimedOut;
    }

    public void setDataTimedOut(long dataTimedOut) {
        this.dataTimedOut = dataTimedOut;
    }

    public long getKeepAliveTimedOut() {
        return keepAliveTimedOut;
    }

    public void setKeepAliveTimedOut(long keepAliveTimedOut) {
        this.keepAliveTimedOut = keepAliveTimedOut;
    }

    public long getMaxConn() {
        return maxConn;
    }

    public void setMaxConn(long maxConn) {
        this.maxConn = maxConn;
    }

    public long getConnectTimeOutSsl() {
        return connectTimeOutSsl;
    }

    public void setConnectTimeOutSsl(long connectTimeOutSsl) {
        this.connectTimeOutSsl = connectTimeOutSsl;
    }

    public long getConnectErrorSsl() {
        return connectErrorSsl;
    }

    public void setConnectErrorSsl(long connectErrorSsl) {
        this.connectErrorSsl = connectErrorSsl;
    }

    public long getConnectFailureSsl() {
        return connectFailureSsl;
    }

    public void setConnectFailureSsl(long connectFailureSsl) {
        this.connectFailureSsl = connectFailureSsl;
    }

    public long getCurrentConnSsl() {
        return currentConnSsl;
    }

    public void setCurrentConnSsl(long currentConnSsl) {
        this.currentConnSsl = currentConnSsl;
    }

    public long getDataTimedOutSsl() {
        return dataTimedOutSsl;
    }

    public void setDataTimedOutSsl(long dataTimedOutSsl) {
        this.dataTimedOutSsl = dataTimedOutSsl;
    }

    public long getKeepAliveTimedOutSsl() {
        return keepAliveTimedOutSsl;
    }

    public void setKeepAliveTimedOutSsl(long keepAliveTimedOutSsl) {
        this.keepAliveTimedOutSsl = keepAliveTimedOutSsl;
    }

    public long getMaxConnSsl() {
        return maxConnSsl;
    }

    public void setMaxConnSsl(long maxConnSsl) {
        this.maxConnSsl = maxConnSsl;
    }
}
