package org.openstack.atlas.usage.logic;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;

import java.util.ArrayList;
import java.util.List;

public class UsagesForHour {
    private int dayOfYear;
    private int hourOfDay;
    private List<LoadBalancerUsage> usages;

    public int getDayOfYear() {
        return dayOfYear;
    }

    public void setDayOfYear(int dayOfYear) {
        this.dayOfYear = dayOfYear;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public List<LoadBalancerUsage> getUsages() {
        if (usages == null) {
            usages = new ArrayList<LoadBalancerUsage>();
        }
        return usages;
    }

    public void setUsages(List<LoadBalancerUsage> usages) {
        this.usages = usages;
    }
}