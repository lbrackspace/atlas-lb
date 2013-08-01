package org.openstack.atlas.service.domain.pojos;

import org.openstack.atlas.service.domain.entities.SslTermination;

import java.io.Serializable;

public class ZeusSslTermination
    implements Serializable
{

    private final static long serialVersionUID = 532512316L;

    protected SslTermination sslTermination;

    protected String certIntermediateCert;

    public SslTermination getSslTermination() {
        return sslTermination;
    }

    public void setSslTermination(SslTermination sslTermination) {
        this.sslTermination = sslTermination;
    }

    public String getCertIntermediateCert() {
        return certIntermediateCert;
    }

    public void setCertIntermediateCert(String certIntermediateCert) {
        this.certIntermediateCert = certIntermediateCert;
    }
}
