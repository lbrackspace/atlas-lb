
package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;
import java.util.Calendar;

public class HostUsage implements Serializable {
    private final static long serialVersionUID = 532512316L;
    private Long bandwidthIn;
    private Long bandwidthOut;
    private Calendar day;

    public Long getBandwidthIn() {
        return bandwidthIn;
    }

    public void setBandwidthIn(Long bandwidthIn) {
        this.bandwidthIn = bandwidthIn;
    }

    public Long getBandwidthOut() {
        return bandwidthOut;
    }

    public void setBandwidthOut(Long bandwidthOut) {
        this.bandwidthOut = bandwidthOut;
    }

    public Calendar getDay() {
        return day;
    }

    public void setDay(Calendar day) {
        this.day = day;
    }
}
