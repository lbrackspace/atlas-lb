package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;

public class LoadBalancerId implements Serializable {

    private Integer id;
    private Integer port;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public boolean equals(Object o) {

        if(o == null)
            return false;

        if(!(o instanceof LoadBalancerId))
            return false;

        LoadBalancerId id = (LoadBalancerId) o;
        if(!(getId().equals(id.getId())))
            return false;

        if(!(getPort().equals(id.getPort())))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (port != null ? port.hashCode() : 0);
        return result;
    }
}
