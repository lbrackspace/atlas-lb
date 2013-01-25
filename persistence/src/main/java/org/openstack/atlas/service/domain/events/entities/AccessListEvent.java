package org.openstack.atlas.service.domain.events.entities;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "access_list_event")
public class AccessListEvent extends Event implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "access_list_id", nullable = false)
    private Integer access_list_id;

    public Integer getAccess_list_id() {
        return access_list_id;
    }

    public void setAccess_list_id(Integer access_list_id) {
        this.access_list_id = access_list_id;
    }

    @Override
    public String getAttributesAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getAttributesAsString());
        sb.append(String.format("access_list_id=%s",getAccess_list_id()));
        return sb.toString();
    }

}
