package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AlertAudits implements Serializable {
    private final static long serialVersionUID = 532512316L;
    protected List<AlertAudit> alertAudits;

    public List<AlertAudit> getAlertAudits() {
        if (alertAudits == null) return new ArrayList<AlertAudit>();
        return alertAudits;
    }

    public void setAlertAudits(List<AlertAudit> alertAudits) {
        this.alertAudits = alertAudits;
    }
}

