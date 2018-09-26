package org.openstack.atlas.api.mgmt.helpers;

import java.util.Comparator;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.SslTermInfo;

public class SslTermInfoComparator implements Comparator<SslTermInfo> {

    @Override
    public int compare(SslTermInfo o1, SslTermInfo o2) {
        if ((o1 == null || o1.getExpiresInDays() == null) && (o2 == null || o2.getExpiresInDays() == null)) {
            return 0;
        }
        if (o1 == null || o1.getExpiresInDays() == null) {
            return -1;
        }
        if (o2 == null || o2.getExpiresInDays() == null) {
            return 1;
        }
        double o1Expires = o1.getExpiresInDays();
        double o2Expires = o2.getExpiresInDays();
        if (o1Expires < o2Expires) {
            return -1;
        }
        if (o1Expires > o2Expires) {
            return 1;
        }
        return 0;

    }
}
