package org.openstack.atlas.service.domain.services.helpers;

public class CloudFilesLzoKey implements Comparable<CloudFilesLzoKey> {

    private int hourKey;
    private int frag;

    @Override
    public int hashCode() {
        return 31 * (31 + this.hourKey) + this.frag;
    }

    public CloudFilesLzoKey(int hourKey, int frag) {
        this.hourKey = hourKey;
        this.frag = frag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CloudFilesLzoKey other = (CloudFilesLzoKey) obj;
        if (this.hourKey != other.hourKey) {
            return false;
        }
        if (this.frag != other.frag) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CloudFilesLzoKey{" + "hourKey=" + hourKey + "frag=" + frag + '}';
    }

    @Override
    public int compareTo(CloudFilesLzoKey o) {
        if (hourKey < o.getHourKey()) {
            return -1;
        }
        if (hourKey > o.getHourKey()) {
            return 1;
        }
        if (frag < o.getFrag()) {
            return -1;
        }
        if (frag > o.getFrag()) {
            return 1;
        }
        return 0;
    }

    public int getHourKey() {
        return hourKey;
    }

    public void setHourKey(int hourKey) {
        this.hourKey = hourKey;
    }

    public int getFrag() {
        return frag;
    }

    public void setFrag(int frag) {
        this.frag = frag;
    }

    public static CloudFilesLzoKey newKey(int hour, int frag) {
        return new CloudFilesLzoKey(hour, frag);
    }
}
