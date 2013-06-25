package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;

public class StmConstants {

    public static final LoadBalancerAlgorithm DEFAULT_ALGORITHM = LoadBalancerAlgorithm.RANDOM;
    public static final String XFF = "add_x_forwarded_for_header";
    public static final String XFP = "add_x_forwarded_proto";
    public static final String SOURCE_IP = "ip";
    public static final String HTTP_COOKIE = "cookie";
}