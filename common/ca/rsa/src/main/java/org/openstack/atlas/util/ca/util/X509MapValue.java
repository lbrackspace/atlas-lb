package org.openstack.atlas.util.ca.util;

import java.security.cert.X509Certificate;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jce.provider.X509CertificateObject;

import org.openstack.atlas.util.ca.primitives.RsaConst;

public class X509MapValue {

    static {
        RsaConst.init();
    }
    private X509CertificateHolder x509CertificateHolder;
    private String fileName;
    private int lineNum;

    public X509MapValue(X509CertificateHolder x509CertificateHolder, String fileName, int lineNum) {
        this.x509CertificateHolder = x509CertificateHolder;
        this.fileName = fileName;
        this.lineNum = lineNum;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNum() {
        return lineNum;
    }

    @Override
    public int hashCode() {
        int xh = (this.getX509CertificateHolder() == null) ? 0 : this.getX509CertificateHolder().hashCode();
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
            if (mapVal.getX509CertificateHolder().equals(this.getX509CertificateHolder())
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

    public X509CertificateHolder getX509CertificateHolder() {
        return x509CertificateHolder;
    }

    public void setX509CertificateHolder(X509CertificateHolder x509CertificateHolder) {
        this.x509CertificateHolder = x509CertificateHolder;
    }
}
