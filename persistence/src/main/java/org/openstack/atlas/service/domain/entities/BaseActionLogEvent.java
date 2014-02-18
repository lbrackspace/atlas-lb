package org.openstack.atlas.service.domain.entities;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Calendar;

public class BaseActionLogEvent extends Entity implements Serializable {
    private static final long serialVersionUID = 0L;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private String state;

    @Column(name = "transition", nullable = false)
    @Enumerated(EnumType.STRING)
    private String transition;

    @Column(name = "pushed", nullable = false)
    private Boolean pushed;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar created;

    public String getState() { return state; }

    public void setState(String state) { this.state = state; }

    public String getTransition() { return transition; }

    public void setTransition(String transition) { this.transition = transition; }

    public Boolean isPushed() { return pushed; }

    public void setPushed(Boolean pushed) { this.pushed = pushed; }

    public Calendar getCreated() { return created; }

    public void setCreated(Calendar created) { this.created = created; }
}
