package org.openstack.atlas.service.domain.pojos;

import org.openstack.atlas.service.domain.entities.AccountLimit;
import org.openstack.atlas.service.domain.entities.LimitType;

import java.util.ArrayList;
import java.util.List;

public class AllAbsoluteLimits {

    protected List<LimitType> defaultLimits;
    protected List<AccountLimit> customLimits;

    public List<LimitType> getDefaultLimits() {
        return defaultLimits;
    }

    public void setDefaultLimits(List<LimitType> defaultLimits) {
        this.defaultLimits = new ArrayList<LimitType>();
        this.defaultLimits.addAll(defaultLimits);
    }

    public List<AccountLimit> getCustomLimits() {
        return customLimits;
    }

    public void setCustomLimits(List<AccountLimit> customLimits) {
        this.customLimits = new ArrayList<AccountLimit>();
        this.customLimits.addAll(customLimits);
    }
}