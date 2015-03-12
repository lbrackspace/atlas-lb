package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;

@javax.persistence.Entity
@Table(name = "hdfs_lzo")
public class HdfsLzo extends Entity implements Serializable {

    public static final int NEEDS_REUPLOAD = 8;
    public static final int NEEDS_MD5 = 4;
    public static final int NEEDS_HDFS = 2;
    public static final int NEEDS_CF = 1;
    public static final int NEEDS_MASK = 15;
    private final static long serialVersionUID = 532512316L;
    @Column(name = "hour_key", nullable = false)
    private int hourKey;
    @Column(name = "finished", nullable = false)
    private boolean finished = false;
    @Column(name = "reupload_needed", nullable = false)
    private boolean reuploadNeeded = false;
    @Column(name = "md5_needed", nullable = false)
    private boolean md5Needed = true;
    @Column(name = "file_size", nullable = false)
    private long fileSize = -1L;
    @Column(name = "cf_needed", nullable = false)
    private boolean cfNeeded = true;
    @Column(name = "hdfs_needed", nullable = true)
    private boolean hdfsNeeded = true;
    @Column(name = "start_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar startTime;
    @Column(name = "end_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar endTime;

    public HdfsLzo(int hourKey, long file_size) {
        this.hourKey = hourKey;
        this.finished = false;
        this.startTime = StaticDateTimeUtils.toCal(StaticDateTimeUtils.nowDateTime(true));
        this.reuploadNeeded = false;
        this.md5Needed = true;
        this.cfNeeded = true;
        this.hdfsNeeded = true;
        this.endTime = null;
        this.fileSize = file_size;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "HdfsLzo{" + "hourKey=" + hourKey + ", finished=" + finished + ", reuploadNeeded=" + isReuploadNeeded() + ", md5Needed=" + isMd5Needed() + ", cfNeeded=" + isCfNeeded() + ", hdfsNeeded=" + isHdfsNeeded() + ", startTime=" + startTime + ", endTime=" + endTime + ", fileSize" + fileSize + '}';
    }

    public HdfsLzo() {
    }

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
        if (finished) {
            this.endTime = StaticDateTimeUtils.toCal(StaticDateTimeUtils.nowDateTime(true));
        } else {
            this.endTime = null;
        }
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

    public boolean isReuploadNeeded() {
        return reuploadNeeded;
    }

    public void setReuploadNeeded(boolean reuploadNeeded) {
        this.reuploadNeeded = reuploadNeeded;
    }

    public boolean isMd5Needed() {
        return md5Needed;
    }

    public void setMd5Needed(boolean md5Needed) {
        this.md5Needed = md5Needed;
    }

    public boolean isCfNeeded() {
        return cfNeeded;
    }

    public void setCfNeeded(boolean cfNeeded) {
        this.cfNeeded = cfNeeded;
    }

    public boolean isHdfsNeeded() {
        return hdfsNeeded;
    }

    public void setHdfsNeeded(boolean hdfsNeeded) {
        this.hdfsNeeded = hdfsNeeded;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
