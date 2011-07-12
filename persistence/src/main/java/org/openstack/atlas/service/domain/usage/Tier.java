package org.openstack.atlas.service.domain.usage;

import static org.openstack.atlas.service.domain.usage.Tier.Level.*;

public class Tier {

    public enum Level {
        UNKNOWN, TIER1, TIER2, TIER3, TIER4, TIER5, TIER6;
    }

    public final static Integer TIER1_MAX = 100;
    public final static Integer TIER2_MAX = 200;
    public final static Integer TIER3_MAX = 300;
    public final static Integer TIER4_MAX = 500;
    public final static Integer TIER5_MAX = 1000;

    public static Level calculateTierLevel(Double averageConnections) {
        if(averageConnections == null) return UNKNOWN;
        if(averageConnections <= TIER1_MAX) return TIER1;
        if(averageConnections <= TIER2_MAX) return TIER2;
        if(averageConnections <= TIER3_MAX) return TIER3;
        if(averageConnections <= TIER4_MAX) return TIER4;
        if(averageConnections <= TIER5_MAX) return TIER5;
        return TIER6;
    }
}
