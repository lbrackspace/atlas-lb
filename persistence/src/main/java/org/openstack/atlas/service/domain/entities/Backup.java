package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name = "host_backup")
public class Backup extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "backup_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar backupTime;

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private Host host;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getBackupTime() {
        return backupTime;
    }

    public void setBackupTime(Calendar backupTime) {
        this.backupTime = backupTime;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }
}
