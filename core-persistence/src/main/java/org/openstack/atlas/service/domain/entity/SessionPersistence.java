package org.openstack.atlas.service.domain.entity;

import org.openstack.atlas.datamodel.AtlasTypeHelper;
import org.openstack.atlas.datamodel.CorePersistenceType;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "vendor",
        discriminatorType = DiscriminatorType.STRING
)
@DiscriminatorValue("CORE")
@Table(name = "session_persistence")
public class SessionPersistence extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @OneToOne
    @JoinColumn(name = "load_balancer_id")
    private LoadBalancer loadBalancer;

    @Column(name = "persistence_type", nullable = false)
    private String persistenceType = CorePersistenceType.HTTP_COOKIE;

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public String getPersistenceType() {
        return persistenceType;
    }

    public void setPersistenceType(String persistenceType) {
        if (!AtlasTypeHelper.isValidPersistenceType(persistenceType)) throw new RuntimeException("Persistence type not supported.");
        this.persistenceType = persistenceType;
    }

    @Override
    public String toString() {
        return "SessionPersistence{" +
                "persistenceType='" + persistenceType + '\'' +
                '}';
    }
}
