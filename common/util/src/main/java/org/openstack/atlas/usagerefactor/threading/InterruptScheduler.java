package org.openstack.atlas.usagerefactor.threading;

import java.util.TimerTask;

public class InterruptScheduler extends TimerTask {
    private Thread target;

    public InterruptScheduler(Thread target) {
        this.target = target;
    }

    @Override
    public void run() {
        target.interrupt();
    }
}