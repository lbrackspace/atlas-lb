package org.openstack.atlas.lb.helpers.ipstring;

import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPOctetOutOfRangeException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringConversionException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringException;

 public class IPv4Range {

    private long lo;
    private long hi;
    private String label;

    public IPv4Range() {
    }

    public IPv4Range(long lo, long hi, String label) {
        this.label = label;
        this.lo = lo;
        this.hi = hi;
    }

    public IPv4Range(String lo, String hi, String label) throws IPStringConversionException, IPOctetOutOfRangeException {
        this.label = label;
        this.lo = IPv4ToolSet.ip2long(lo);
        this.hi = IPv4ToolSet.ip2long(hi);
    }

    public long getLo() {
        return lo;
    }

    public void setLo(long lo) {
        this.lo = lo;
    }

    public long getHi() {
        return hi;
    }

    public void setHi(long hi) {
        this.hi = hi;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        String format = "%s{lo=%s,hi=%s}";

        String block;
        String loStr;
        String hiStr;
        block = (this.label == null) ? "null" : this.label;
        try {
            loStr = IPv4ToolSet.long2ip(this.lo);
            hiStr = IPv4ToolSet.long2ip(this.hi);
        } catch (Exception e) {
            loStr = "Unknown";
            hiStr = "Unknown";
        }
        return String.format(format, block, loStr, hiStr);
    }

    public boolean contains(long ipLong) {
        boolean out;
        out = this.lo <= ipLong && ipLong <= this.hi;
        return out;
    }

    public boolean contains(String ipStr) {
        long ipLong;
        boolean out;
        try {
            ipLong = IPv4ToolSet.ip2long(ipStr);
        } catch (IPStringException ex) {
            out = false;
            return out;
        }
        out = this.contains(ipLong);
        return out;
    }
}
