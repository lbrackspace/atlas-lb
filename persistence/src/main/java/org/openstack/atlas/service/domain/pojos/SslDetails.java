package org.openstack.atlas.service.domain.pojos;

public class SslDetails {

    private String privateKey;
    private String certificate;
    private String intermediateCertificate;

    public SslDetails() {
    }

    public SslDetails(String privateKey, String certificate, String intermediateCertificate) {
        this.privateKey = privateKey;
        this.certificate = certificate;
        this.intermediateCertificate = intermediateCertificate;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
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

    // TODO: Write unit tests
    public static SslDetails sanitize(SslDetails dirtyDetails) {
        final String cleanRegex = "(?m)^[ \t]*\r?\n";
        SslDetails cleanDetails = new SslDetails();

        if (dirtyDetails.getPrivateKey() != null) {
            cleanDetails.setPrivateKey(dirtyDetails.getPrivateKey().replaceAll(cleanRegex, "").trim());
        }

        if (dirtyDetails.getCertificate() != null) {
            cleanDetails.setCertificate(dirtyDetails.getCertificate().replaceAll(cleanRegex, "").trim());
        }

        if (dirtyDetails.getIntermediateCertificate() != null) {
            cleanDetails.setIntermediateCertificate(dirtyDetails.getIntermediateCertificate().replaceAll(cleanRegex, "").trim());
        }

        return cleanDetails;
    }
}
