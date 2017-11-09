package org.openstack.atlas.util.ca.util;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.openstack.atlas.util.ca.RSAKeyUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.primitives.RsaConst;

// Used for the ChainBuilder
public class X509ChainEntry {

    static {
        RsaConst.init();
    }
    private KeyPair key;
    private PKCS10CertificationRequest csr;
    private X509CertificateHolder x509Holder;

    public X509ChainEntry() {
    }

    public X509ChainEntry(KeyPair key, PKCS10CertificationRequest csr, X509CertificateHolder x509holder) {
        this.key = key;
        this.csr = csr;
        this.x509Holder = x509holder;
    }

    public KeyPair getKey() {
        return key;
    }

    public void setKey(KeyPair key) {
        this.key = key;
    }

    public PKCS10CertificationRequest getCsr() {
        return csr;
    }

    public void setCsr(PKCS10CertificationRequest csr) {
        this.csr = csr;
    }

    public X509CertificateHolder getX509Holder() {
        return x509Holder;
    }

    public void getX509Holder(X509CertificateHolder x509obj) {
        this.setX509Holder(x509obj);
    }

    public void setX509Holder(X509CertificateHolder x509Holder) {
        this.x509Holder = x509Holder;
    }
}
