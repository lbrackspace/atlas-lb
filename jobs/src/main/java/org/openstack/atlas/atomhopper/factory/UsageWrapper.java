package org.openstack.atlas.atomhopper.factory;

import org.openstack.atlas.service.domain.entities.Usage;

import java.util.ArrayList;
import java.util.List;

public class UsageWrapper {
    List<Usage> currentRecords;
    List<Usage> correctedRecords;
    List<Usage> failedRecords;

    public List<Usage> getCurrentRecords() {
        if (currentRecords == null) {
            currentRecords = new ArrayList<Usage>();
        }
        return currentRecords;
    }

    public void setCurrentRecords(List<Usage> currentRecords) {
        this.currentRecords = currentRecords;
    }

    public List<Usage> getCorrectedRecords() {
        if (correctedRecords == null) {
            correctedRecords = new ArrayList<Usage>();
        }
        return correctedRecords;
    }

    public void setCorrectedRecords(List<Usage> correctedRecords) {
        this.correctedRecords = correctedRecords;
    }

    public List<Usage> getFailedRecords() {
        if (failedRecords == null) {
            failedRecords = new ArrayList<Usage>();
        }
        return failedRecords;
    }

    public void setFailedRecords(List<Usage> failedRecords) {
        this.failedRecords = failedRecords;
    }
}
