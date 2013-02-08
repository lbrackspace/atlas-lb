package org.openstack.atlas.service.domain.events.entities;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "health_monitor_event")
public class HealthMonitorEvent extends Event implements Serializable {

    private final static long serialVersionUID = 532512316L;
    @Column(name = "health_monitor_id", nullable = false)
    private Integer healthMonitorId;

    public Integer getHealthMonitorId() {
        return healthMonitorId;
    }

    public void setHealthMonitorId(Integer healthMonitorId) {
        this.healthMonitorId = healthMonitorId;
    }

    @Override
    public String getAttributesAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getAttributesAsString());
        sb.append(String.format("healthMonitorId=%s", vorn(getHealthMonitorId())));
        return sb.toString();
    }
}
