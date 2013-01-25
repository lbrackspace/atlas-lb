package org.openstack.atlas.lb.helpers.ipstring;

import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPBlocksOverLapException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPCidrBlockOutOfRangeException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPOctetOutOfRangeException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringConversionException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPRangeTooBigException;

import java.util.HashSet;
import java.util.Set;

public class IPv4Ranges {

    private Set<IPv4Range> ranges;

    public IPv4Ranges() {
        ranges = new HashSet<IPv4Range>();
    }

    public boolean add(String ipBlock) throws IPBlocksOverLapException, IPStringConversionException, IPOctetOutOfRangeException, IPCidrBlockOutOfRangeException {
        IPv4Range range = IPv4ToolSet.ipv4BlockToRange(ipBlock);
        return add(range);
    }

    public boolean add(String lo, String hi) throws IPStringConversionException, IPOctetOutOfRangeException, IPBlocksOverLapException, IPRangeTooBigException {
        String blockStr = String.format("%s:%s", lo, hi);
        IPv4Range range = new IPv4Range(lo, hi, blockStr);
        if (range.getHi() < range.getLo() || (range.getHi() - range.getLo() > (1 << 20))) {
            throw new IPRangeTooBigException(String.format("Range %s is too big to add Sorry.", range));
        }
        return add(range);
    }

    public boolean add(IPv4Range range) throws IPBlocksOverLapException {
        for (IPv4Range stored : ranges) {
            if (range.getLo() >= stored.getHi() || range.getHi() <= stored.getLo()) {
                continue;
            } else {
                throw new IPBlocksOverLapException(String.format("Blocks %s and %s overlap", range, stored));
            }
        }
        return ranges.add(range);
    }

    public Set<IPv4Range> getRanges() {
        return ranges;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (IPv4Range stored : ranges) {
            sb.append(String.format("%s, ", stored));
        }
        sb.append("]");
        return sb.toString();
    }

    public boolean contains(long ipLong) {
        boolean out;
        out = false;
        for (IPv4Range ipv4Range : this.ranges) {
            if (ipv4Range.contains(ipLong)) {
                out = true;
                return out;
            }
        }
        return out;
    }

    public boolean contains(String ipStr) {
        boolean out;
        long ipLong;
        try {
            ipLong = IPv4ToolSet.ip2long(ipStr);
            out = this.contains(ipLong);
        } catch (IPStringException ex) {
            out = false;
            return out;
        }
        return out;
    }
}
