package org.rackspace.capman.tools.util;

import java.security.cert.X509Certificate;
import org.bouncycastle.jce.provider.X509CertificateObject;

import org.rackspace.capman.tools.ca.primitives.RsaConst;

public class X509MapValue {

    static {
        RsaConst.init();
    }
    private X509CertificateObject x509CertificateObject;
    private String fileName;
    private int lineNum;

    public X509MapValue(X509CertificateObject x509CertificateObject, String fileName, int lineNum) {
        this.x509CertificateObject = x509CertificateObject;
        this.fileName = fileName;
        this.lineNum = lineNum;
    }

    public X509CertificateObject getX509CertificateObject() {
        return x509CertificateObject;
    }

    public X509Certificate getX509Certificate() {
        return (X509Certificate) x509CertificateObject;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNum() {
        return lineNum;
    }

    @Override
    public int hashCode() {
        int xh = (this.x509CertificateObject == null) ? 0 : this.x509CertificateObject.hashCode();
        int fh = (this.fileName == null) ? 0 : this.fileName.hashCode();
        int lh = this.lineNum;
        return (((37 + xh) * 23 + fh) * 47 + lh);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof X509MapValue) {
            X509MapValue mapVal = (X509MapValue) obj;
            if (mapVal.getX509CertificateObject().equals(this.x509CertificateObject)
                    && mapVal.getFileName().equals(this.fileName)
                    && mapVal.getLineNum() == this.lineNum) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%s[%d]", fileName, lineNum);
    }
}
