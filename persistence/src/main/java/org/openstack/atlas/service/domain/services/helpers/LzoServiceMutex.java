package org.openstack.atlas.service.domain.services.helpers;

public class LzoServiceMutex {

    private static int init_count=0;

    static {
        init_count++;
    }

    public static synchronized int get() {
        return init_count;
    }

    public static int inc() {
        int val;
        synchronized (LzoServiceMutex.class) {
            init_count++;
            val = init_count;
        }
        return val;
    }

    public static int dec() {
        int val;
        synchronized (LzoServiceMutex.class) {
            init_count--;
            val = init_count;
        }
        return val;
    }
}
