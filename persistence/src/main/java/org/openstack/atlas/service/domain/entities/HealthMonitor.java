package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "health_monitor")
public class HealthMonitor extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @OneToOne
    @JoinColumn(name = "loadbalancer_id")
    private LoadBalancer loadbalancer;

    @JoinColumn(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private HealthMonitorType type;

    @Column(name = "delay", nullable = false)
    private Integer delay;

    @Column(name = "timeout", nullable = false)
    private Integer timeout;

    @Column(name = "attempts_before_deactivation", nullable = false)
    private Integer attemptsBeforeDeactivation;

    @Column(length = 128, nullable = true)
    private String path;

    @Column(name = "status_regex", length = 128, nullable = true)
    private String statusRegex;

    @Column(name = "body_regex", length = 128, nullable = true)
    private String bodyRegex;

    @Column(name = "host_header", length = 256, nullable = true)
    private String hostHeader;

    public LoadBalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(LoadBalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
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

    public String getHostHeader() {
        return hostHeader;
    }

    public void setHostHeader(String hostHeader) {
        this.hostHeader = hostHeader;
    }

    public HealthMonitorType getType() {
        return type;
    }

    public void setType(HealthMonitorType type) {
        this.type = type;
    }
}
