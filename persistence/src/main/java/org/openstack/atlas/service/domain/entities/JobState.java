package org.openstack.atlas.service.domain.entities;

import org.openstack.atlas.service.domain.logs.entities.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name = "state")
public class JobState extends org.openstack.atlas.service.domain.entities.Entity implements Serializable {

    @Column(name = "jobname")
    @Enumerated(EnumType.STRING)
    private JobName jobName;

    @Enumerated(EnumType.STRING)
    private JobStateVal state;

    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar startTime;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar endTime;

    @Column(name = "inputpath")
    private String inputPath;

    public JobName getJobName() {
        return jobName;
    }

    public void setJobName(JobName jobName) {
        this.jobName = jobName;
    }

    public JobStateVal getState() {
        return state;
    }

    public void setState(JobStateVal state) {
        this.state = state;
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

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    @Override
    public String toString() {
        return this.getId() + ":" + state + ":" + jobName + ":" + inputPath + ":" + new DateTime(startTime).getIso();
    }

}
