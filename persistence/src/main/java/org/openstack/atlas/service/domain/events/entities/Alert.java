package org.openstack.atlas.service.domain.events.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name = "alert")
public class Alert implements Serializable {
    private final static long serialVersionUID = 532512317L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;


    @Column(name = "account_id", nullable = true)
    private Integer accountId;
    @Column(name = "loadbalancer_id", nullable = true)
    private Integer loadbalancerId;
    @Column(name = "alert_type", nullable = false)
    private String alertType;
    @Column(name = "message_name", nullable = true)
    private String messageName;
    @Column(name = "message", columnDefinition="text",  nullable = false)
    private String message;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private AlertStatus status;
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar created;

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getLoadbalancerId() {
        return loadbalancerId;
    }

    public void setLoadbalancerId(Integer loadbalancerId) {
        this.loadbalancerId = loadbalancerId;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}
