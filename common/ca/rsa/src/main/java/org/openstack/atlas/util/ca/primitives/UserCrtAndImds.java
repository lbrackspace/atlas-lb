package org.openstack.atlas.util.ca.primitives;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.openstack.atlas.util.ca.PemUtils;

public class UserCrtAndImds {

    private X509CertificateObject userCert;
    private List<X509CertificateObject> imds;

    static {
        RsaConst.init();
    }

    public UserCrtAndImds() {
    }

    public UserCrtAndImds(X509CertificateObject userCert, List<X509CertificateObject> imds) {
        this.userCert = userCert;
        this.imds = imds;
    }

    public X509CertificateObject getUserCert() {
        return userCert;
    }

    public void setUserCert(X509CertificateObject userCert) {
        this.userCert = userCert;
    }

    public List<X509CertificateObject> getImds() {
        if (imds == null) {
            imds = new ArrayList<X509CertificateObject>();
        }
        return imds;
    }

    public void setImds(List<X509CertificateObject> imds) {
        this.imds = imds;
    }
}
