package org.openstack.atlas.jobs.logic;

import org.openstack.atlas.service.domain.entity.UsageRecord;

import java.util.ArrayList;
import java.util.List;

public class UsagesForDay {
    private int dayOfYear;
    private List<UsageRecord> usages;

    public int getDayOfYear() {
        return dayOfYear;
    }

    public void setDayOfYear(int dayOfYear) {
        this.dayOfYear = dayOfYear;
    }

    public List<UsageRecord> getUsages() {
        if (usages == null) {
            usages = new ArrayList<UsageRecord>();
        }
        return usages;
    }

    public void setUsages(List<UsageRecord> usages) {
        this.usages = usages;
    }
}