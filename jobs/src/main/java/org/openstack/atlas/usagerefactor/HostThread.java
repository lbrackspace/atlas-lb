package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.entities.Host;

public class HostThread implements Runnable {
    public final Host host;

    public HostThread(Host host) {
        this.host = host;
    }

    @Override
    public void run() {
    }
}