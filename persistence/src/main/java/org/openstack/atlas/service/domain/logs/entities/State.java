package org.openstack.atlas.service.domain.logs.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name = "state")
public class State extends Entity implements Serializable {

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar endTime;

    @Column(name = "inputpath")
    private String inputPath;

    @Column(name = "jobname")
    @Enumerated(EnumType.STRING)
    private NameVal jobName;

    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar startTime;

    @Enumerated(EnumType.STRING)
    private StateVal state;

    public Calendar getEndTime() {
        return endTime;
    }
    
    public String getInputPath() {
        return inputPath;
    }

    public NameVal getJobName() {
        return jobName;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public StateVal getState() {
        return state;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public void setJobName(NameVal jobName) {
        this.jobName = jobName;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public void setState(StateVal state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return this.getId() + ":" + state + ":" + jobName + ":" + inputPath + ":" + new DateTime(startTime).getIso();
    }

}
