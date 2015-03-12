package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@javax.persistence.Entity
@Table(name = "cloud_files_lzo")
public class CloudFilesLzo extends Entity implements Serializable {

    public CloudFilesLzo() {
    }

    public CloudFilesLzo(int hourKey, int frag, String md5, Calendar startTime) {
        this.hourKey = hourKey;
        this.frag = frag;
        this.md5 = md5;
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "CloufFilesLzo{" + "hourKey=" + hourKey + "frag=" + frag + "finished=" + finished + "md5=" + md5 + "startTime=" + startTime + "endTime=" + endTime + '}';
    }

    private final static long serialVersionUID = 532512316L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
    @Column(name = "hour_key", nullable = false)
    private int hourKey;
    @Column(name = "frag", nullable = false)
    private int frag;
    @Column(name = "finished", nullable = false)
    private boolean finished = false;
    @Column(name = "md5", nullable = false, length = 48)
    private String md5;
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

    public int getFrag() {
        return frag;
    }

    public void setFrag(int frag) {
        this.frag = frag;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
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
