package org.openstack.atlas.service.domain.entity;

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
    private LoadBalancer loadBalancer;

    @JoinColumn(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private HealthMonitorType type;

    @Column(name = "delay", nullable = false)
    private Integer delay;

    @Column(name = "timeout", nullable = false)
    private Integer timeout;

    @Column(name = "attempts_before_deactivation", nullable = false)
    private Integer attemptsBeforeDeactivation;

    @Column(name = "path", length = 128, nullable = true)
    private String path;

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public HealthMonitorType getType() {
        return type;
    }

    public void setType(HealthMonitorType type) {
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

