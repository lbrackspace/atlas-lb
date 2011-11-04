package org.openstack.atlas.rax.domain.entity;

import org.openstack.atlas.service.domain.entity.ConnectionThrottle;
import org.openstack.atlas.service.domain.entity.LoadBalancer;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("RAX")
public class RaxConnectionThrottle extends ConnectionThrottle implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "min_connections", nullable = false)
	private Integer minConnections;

	@Column(name = "max_connections", nullable = false)
	private Integer maxConnections;

    public Integer getMinConnections() {
        return minConnections;
    }

    public void setMinConnections(Integer minConnections) {
        this.minConnections = minConnections;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }
}
