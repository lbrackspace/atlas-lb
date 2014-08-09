package org.openstack.atlas.service.domain.entities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.persistence.*;
import java.io.Serializable;
import org.openstack.atlas.service.domain.exceptions.FieldNotDerivableException;
import org.openstack.atlas.service.domain.exceptions.PrivateKeyCryptException;
import org.openstack.atlas.service.domain.util.conf.SslTerminationConfig;
import org.openstack.atlas.util.b64aes.Aes;
import org.openstack.atlas.util.b64aes.PaddingException;

@javax.persistence.Entity
@Table(name = "lb_ssl")
public class SslTermination extends Entity implements Serializable {

    private final static long serialVersionUID = 532512316L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
    @OneToOne
    @JoinColumn(name = "loadbalancer_id")
    private LoadBalancer loadbalancer;
    @Column(name = "pem_key", nullable = true, columnDefinition = "mediumtext")
    private String privatekey;
    @Column(name = "pem_cert", nullable = true, columnDefinition = "mediumtext")
    private String certificate;
    @Column(name = "intermediate_certificate", nullable = true, columnDefinition = "mediumtext")
    private String intermediateCertificate;
    @Column(name = "enabled", nullable = true)
    private boolean enabled = true;
    @Column(name = "secure_port", nullable = true)
    private int securePort;
    @Column(name = "secure_traffic_only", nullable = true)
    private boolean secureTrafficOnly;

    public LoadBalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(LoadBalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
    }

    public String getPrivatekey() {
        String key = SslTerminationConfig.getEncryptKey();
        String errMsg = "Unable to decrypt private key";
        if (privatekey == null) {
            return privatekey;
        }
        try {
            String ptext = Aes.b64decrypt_str(privatekey, key);
            return ptext;
        } catch (NoSuchAlgorithmException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        } catch (NoSuchPaddingException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        } catch (InvalidKeySpecException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        } catch (BadPaddingException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        } catch (InvalidKeyException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        } catch (IllegalBlockSizeException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        } catch (UnsupportedEncodingException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        } catch (PaddingException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        } catch (IOException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        }
    }

    public String getPrivatekeyRaw() {
        return privatekey;
    }

    public void setPrivatekey(String privatekey) {
        String key = SslTerminationConfig.getEncryptKey();
        String errMsg = "Unable to encrypt private key";
        if (privatekey == null) {
            this.privatekey = null;
            return;
        }
        try {
            String cText = Aes.b64encrypt_str(privatekey, key);
            this.privatekey = cText;
        } catch (NoSuchAlgorithmException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        } catch (NoSuchPaddingException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        } catch (InvalidKeyException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        } catch (IllegalBlockSizeException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        } catch (BadPaddingException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        } catch (IOException ex) {
            throw new PrivateKeyCryptException(errMsg, ex);
        }
    }

    public void setPrivatekeyRaw(String privatekey) {
        this.privatekey = privatekey;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getIntermediateCertificate() {
        return intermediateCertificate;
    }

    public void setIntermediateCertificate(String intermediateCertificate) {
        this.intermediateCertificate = intermediateCertificate;
    }

    public boolean isSecureTrafficOnly() {
        return secureTrafficOnly;
    }

    public void setSecureTrafficOnly(boolean secureTrafficOnly) {
        this.secureTrafficOnly = secureTrafficOnly;
    }

    public int getSecurePort() {
        return securePort;
    }

    public void setSecurePort(int securePort) {
        this.securePort = securePort;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String key;
        try {
            key = getPrivatekey();
        } catch (Exception ex) {
            key = "<DecryptFailed>";
        }
        sb.append("id: ").append(this.getId()).append("\n").
                append("privatekey:\n").append(key).append("\n").
                append("certificate:\n").append(certificate).append("\n").
                append("intermediateCertificate:\n").append(intermediateCertificate).append("\n").
                append("enabled: ").append(enabled).append("\n").
                append("securePort: ").append(securePort).append("\n").
                append("secureTrafficOnly: ").append(secureTrafficOnly).append("\n").
                append("\n");
        return sb.toString();
    }
}
