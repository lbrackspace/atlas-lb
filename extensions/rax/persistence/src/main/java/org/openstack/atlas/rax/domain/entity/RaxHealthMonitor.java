package org.openstack.atlas.rax.domain.entity;

import org.openstack.atlas.service.domain.entity.HealthMonitor;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import java.io.Serializable;

@javax.persistence.Entity
@DiscriminatorValue(Discriminator.RAX)
public class RaxHealthMonitor extends HealthMonitor implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "status_regex", length = 128, nullable = true)
    private String statusRegex;

    @Column(name = "body_regex", length = 128, nullable = true)
    private String bodyRegex;

    public String getStatusRegex() {
        return statusRegex;
    }

    public void setStatusRegex(String statusRegex) {
        this.statusRegex = statusRegex;
    }

    public String getBodyRegex() {
        return bodyRegex;
    }

    public void setBodyRegex(String bodyRegex) {
        this.bodyRegex = bodyRegex;
    }

    @Override
    public String toString() {
        return "RaxHealthMonitor{" +
                "statusRegex='" + statusRegex + '\'' +
                ", bodyRegex='" + bodyRegex + '\'' +
                "} " + super.toString();
    }
}
