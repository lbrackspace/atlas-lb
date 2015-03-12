package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@javax.persistence.Entity
@Table(name = "hdfs_lzo")
public class HdfsLzo extends Entity implements Serializable {

    public HdfsLzo(int hourKey, boolean finished, Calendar startTime) {
        this.hourKey = hourKey;
        this.finished = finished;
        this.startTime = startTime;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public HdfsLzo() {
    }
    private final static long serialVersionUID = 532512316L;
    @Column(name = "hour_key", nullable = false)
    private int hourKey;
    @Column(name = "finished", nullable = false)
    private boolean finished = false;
    @Column(name = "start_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar startTime;
    @Column(name = "end_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar endTime;

    public int getHourKey() {
        return hourKey;
    }

    public void setHourKey(int hourKey) {
        this.hourKey = hourKey;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }
}
