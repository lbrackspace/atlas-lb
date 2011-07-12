package org.openstack.atlas.service.domain.usage.entities;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name = "host_usage")
public class HostUsage extends Entity implements Serializable {
    private final static long serialVersionUID = 532512317L;

    @Column(name = "host_id", nullable = false)
    Integer hostId;
    @Column(name = "bandwidth_bytes_in", nullable = false)
    Long bandwidthBytesIn = 0L;
    @Column(name = "bandwidth_bytes_out", nullable = false)
    Long bandwidthBytesOut = 0L;
    @Column(name = "snapshot_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Calendar snapshotTime;

    public Integer getHostId() {
        return hostId;
    }

    public void setHostId(Integer hostId) {
        this.hostId = hostId;
    }

    public Long getBandwidthBytesIn() {
        return bandwidthBytesIn;
    }

    public void setBandwidthBytesIn(Long bandwidthBytesIn) {
        this.bandwidthBytesIn = bandwidthBytesIn;
    }

    public Long getBandwidthBytesOut() {
        return bandwidthBytesOut;
    }

    public void setBandwidthBytesOut(Long bandwidthBytesOut) {
        this.bandwidthBytesOut = bandwidthBytesOut;
    }

    public Calendar getSnapshotTime() {
        return snapshotTime;
    }

    public void setSnapshotTime(Calendar snapshotTime) {
        this.snapshotTime = snapshotTime;
    }
}
