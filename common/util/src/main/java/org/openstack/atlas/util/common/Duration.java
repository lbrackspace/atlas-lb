package org.openstack.atlas.util.common;

public class Duration {
    private long hours = 0;
    private long mins = 0;
    private long secs = 0;

    public long getHours() {
        return hours;
    }

    public void setHours(long hours) {
        this.hours = hours;
    }

    public long getMins() {
        return mins;
    }

    public void setMins(long mins) {
        this.mins = mins;
    }

    public long getSecs() {
        return secs;
    }

    public void setSecs(long secs) {
        this.secs = secs;
    }

    @Override
    public String toString() {
        if (mins == 0 && hours == 0) {
            return String.format("%d second(s)", secs);
        }

        if (hours == 0) {
            return String.format("%d minute(s), %d second(s)", mins, secs);
        }

        return String.format("%d hours(s), %d minute(s), %d second(s)", hours, mins, secs);
    }
}
