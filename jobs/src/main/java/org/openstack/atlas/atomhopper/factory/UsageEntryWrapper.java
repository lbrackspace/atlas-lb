package org.openstack.atlas.atomhopper.factory;

import org.openstack.atlas.service.domain.entities.Usage;
import org.w3._2005.atom.UsageEntry;

public class UsageEntryWrapper {
    Usage usage;
    UsageEntry entryObject;
    String entryString;

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    public UsageEntry getEntryObject() {
        return entryObject;
    }

    public void setEntryObject(UsageEntry entryObject) {
        this.entryObject = entryObject;
    }

    public String getEntryString() {
        return entryString;
    }

    public void setEntryString(String entryString) {
        this.entryString = entryString;
    }
}
