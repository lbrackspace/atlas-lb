package org.openstack.atlas.util.debug;

public class StringBuilderWriter {

    private StringBuilder sb;

    public StringBuilderWriter() {
        sb = new StringBuilder();
    }

    public StringBuilderWriter(int n) {
        sb = new StringBuilder(n);
    }

    public void printf(String fmt, Object... objs) {
        sb.append(String.format(fmt, objs));
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
