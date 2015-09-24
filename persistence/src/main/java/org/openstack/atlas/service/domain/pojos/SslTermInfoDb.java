package org.openstack.atlas.service.domain.pojos;

public class SslTermInfoDb {
    private int loadbalancerId = -1;
    private int accountId = -1;
    private int sslId = -1;
    private String privatekey = null;
    private String certificate = null;
    private String intermediateCertificate = null;

    public int getLoadbalancerId() {
        return loadbalancerId;
    }

    public void setLoadbalancerId(int loadbalancerId) {
        this.loadbalancerId = loadbalancerId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getSslId() {
        return sslId;
    }

    public void setSslId(int sslId) {
        this.sslId = sslId;
    }

    public String getPrivatekey() {
        return privatekey;
    }

    public void setPrivatekey(String privatekey) {
        this.privatekey = privatekey;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getIntermediateCertificate() {
        return intermediateCertificate;
    }

    public void setIntermediateCertificate(String intermediateCertificate) {
        this.intermediateCertificate = intermediateCertificate;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{ loadbalancerId=").append(loadbalancerId).
                append(", sslId=").append(sslId).
                append(", privatekey=").append(privatekey).
                append(", certificate=").append(certificate).
                append(", intermediateCertificate=").append(intermediateCertificate).
                append("}");
        return sb.toString();
    }

}
