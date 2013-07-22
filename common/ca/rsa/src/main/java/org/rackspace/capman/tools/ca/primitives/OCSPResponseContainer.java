
package org.rackspace.capman.tools.ca.primitives;

import org.bouncycastle.ocsp.OCSPResp;

@Deprecated
public class OCSPResponseContainer {
    private int ocspHttpResponseCode=-1;
    private OCSPResponseEvent ocspResponseEvent;
    private OCSPResp rawOCSPResponse = null;
    private int respCode = -1;
    private String msg = null;
    private Throwable exception = null;

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable th) {
        this.exception = th;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getOcspHttpResponseCode() {
        return ocspHttpResponseCode;
    }

    public void setOcspHttpResponseCode(int ocspHttpResponseCode) {
        this.ocspHttpResponseCode = ocspHttpResponseCode;
    }

    public OCSPResponseEvent getOcspResponseEvent() {
        return ocspResponseEvent;
    }

    public void setOcspResponseEvent(OCSPResponseEvent ocspResponseEvent) {
        this.ocspResponseEvent = ocspResponseEvent;
    }

    public int getRespCode() {
        return respCode;
    }

    public void setRespCode(int respCode) {
        this.respCode = respCode;
    }

    public OCSPResp getRawOCSPResponse() {
        return rawOCSPResponse;
    }

    public void setRawOCSPResponse(OCSPResp rawOCSPResponse) {
        this.rawOCSPResponse = rawOCSPResponse;
    }

}
