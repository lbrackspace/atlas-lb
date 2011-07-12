package org.openstack.atlas.service.domain.pojos;

import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.entities.Alert;

import java.util.Calendar;
import java.util.List;

public class LoadBalancerAudit {
    private int id;
    private LoadBalancerStatus status;
    private List<Alert> alertAudits;
    private Calendar created;
    private Calendar updated;

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    public Calendar getUpdated() {
        return updated;
    }

    public void setUpdated(Calendar updated) {
        this.updated = updated;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LoadBalancerStatus getStatus() {
        return status;
    }

    public void setStatus(LoadBalancerStatus status) {
        this.status = status;
    }

    public List<Alert> getAlertAudits() {
        return alertAudits;
    }

    public void setAlertAudits(List<Alert> alertAudits) {
        this.alertAudits = alertAudits;
    }
}
