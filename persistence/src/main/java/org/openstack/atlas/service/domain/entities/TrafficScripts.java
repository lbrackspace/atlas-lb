package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name = "traffic_scripts")
public class TrafficScripts extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "name", length = 128, nullable = false)
    private String name;

    @ManyToOne()
    @JoinColumn(name = "host_id", nullable = false)
    private Host host;

    @Column(name = "status", nullable = true)
    private String status;

    @Column(name = "notes", nullable = true)
    private String notes;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar created;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar updated;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

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
}
