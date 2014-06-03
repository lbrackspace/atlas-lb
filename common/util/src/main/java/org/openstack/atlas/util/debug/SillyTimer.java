package org.openstack.atlas.util.debug;

public class SillyTimer {

    private static final double milliCoef = 1.0 / 1000.0;
    private long begin;
    private long end;
    private long stored;
    boolean stopped;

    public SillyTimer() {
        begin = nowMillis();
        end = nowMillis();
        stored = 0L;
        stopped = true;
    }

    public void reset() {
        begin = nowMillis();
        end = nowMillis();
        stored = 0L;
    }

    public void restart() {
        reset();
        start();
    }

    public void start() {
        if (!stopped) {
            return;
        }
        begin = nowMillis();
        stopped = false;
    }

    public void stop() {
        if (stopped) {
            return;
        }
        end = nowMillis();
        stored += end - begin;
        stopped = true;
    }

    public long read() {
        if (stopped) {
            return stored;
        }
        return nowMillis() - begin + stored;
    }

    public double readSeconds() {
        return ((double) read()) * milliCoef;
    }

    public static long nowMillis() {
        return System.currentTimeMillis();
    }
}
