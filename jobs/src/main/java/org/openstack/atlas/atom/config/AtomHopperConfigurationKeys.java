package org.openstack.atlas.atom.config;

import org.openstack.atlas.cfg.ConfigurationKey;

public enum AtomHopperConfigurationKeys implements ConfigurationKey {
    region,
    allow_ahusl,
    ahusl_data_center,
    atom_hopper_endpoint,
    ahusl_max_total_connections,
    ahusl_time_out,
    ahusl_max_redirects,
    http_protocol,
}
