package org.openstack.atlas.service.domain.event.entity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "connection_limit_event")
public class ConnectionLimitEvent extends Event implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "connection_limit_id", nullable = false)
    private Integer connectionLimitId;

    public Integer getConnectionLimitId() {
        return connectionLimitId;
    }

    public void setConnectionLimitId(Integer connectionLimitId) {
        this.connectionLimitId = connectionLimitId;
    }

    @Override
    public String getAttributesAsString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.getAttributesAsString());
        sb.append(String.format("connectionLimitId=%s",vorn(getConnectionLimitId())));
        return sb.toString();
    }
}
