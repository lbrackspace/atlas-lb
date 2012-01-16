package org.openstack.atlas.service.domain.entity;

import org.openstack.atlas.datamodel.AtlasTypeHelper;
import org.openstack.atlas.datamodel.CoreHealthMonitorType;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "vendor",
        discriminatorType = DiscriminatorType.STRING
)
@DiscriminatorValue("CORE")
@Table(name = "health_monitor")
public class HealthMonitor extends org.openstack.atlas.service.domain.entity.Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @OneToOne
    @JoinColumn(name = "load_balancer_id")
    protected LoadBalancer loadBalancer;

    @JoinColumn(name = "type", nullable = false)
    protected String type = CoreHealthMonitorType.CONNECT;

    @Column(name = "delay", nullable = false)
    protected Integer delay = 3600;

    @Column(name = "timeout", nullable = false)
    protected Integer timeout = 300;

    @Column(name = "attempts_before_deactivation", nullable = false)
    protected Integer attemptsBeforeDeactivation = 10;

    @Column(name = "path", length = 128, nullable = true)
    protected String path = "/";

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (!AtlasTypeHelper.isValidHealthMonitorType(type)) throw new RuntimeException("Health monitor type not supported.");
        this.type = type;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getAttemptsBeforeDeactivation() {
        return attemptsBeforeDeactivation;
    }

    public void setAttemptsBeforeDeactivation(Integer attemptsBeforeDeactivation) {
        this.attemptsBeforeDeactivation = attemptsBeforeDeactivation;
    }

    public String getPath() {
        if (CoreHealthMonitorType.CONNECT.equals(type)) return null;
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "HealthMonitor{" +
                "path='" + path + '\'' +
                ", attemptsBeforeDeactivation=" + attemptsBeforeDeactivation +
                ", timeout=" + timeout +
                ", delay=" + delay +
                ", type=" + type +
                '}';
    }
}

