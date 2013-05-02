package org.openstack.atlas.usagerefactor.threading;

import java.util.Arrays;
import java.util.Collection;

public class ThreadingUtil {

    public static void waitFor(Collection<? extends Thread> c) throws InterruptedException {
        for (Thread t : c) t.join();
    }

    public static void waitFor(Thread[] ts) throws InterruptedException {
        waitFor(Arrays.asList(ts));
    }

}
