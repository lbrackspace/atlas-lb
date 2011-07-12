package org.openstack.atlas.api.filters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.servlet.ServletInputStream;

public class BufferedServletInputStream extends ServletInputStream {

    ByteArrayInputStream bais;

    public BufferedServletInputStream(ByteArrayInputStream bais) {
        this.bais = bais;
    }

    @Override
    public int available() {
        return bais.available();
    }

    @Override
    public int read() {
        return bais.read();
    }

    @Override
    public int read(byte[] buf, int off, int len) {
        return bais.read(buf, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return bais.read(b);
    }
}
