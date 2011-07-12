package org.openstack.atlas.api.filters.wrappers;

import org.openstack.atlas.api.filters.BufferedServletInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class BufferedRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] buffer;

    public BufferedRequestWrapper(HttpServletRequest req) throws IOException {
        super(req);
        // Read InputStream and store its content in a buffer.
        InputStream is = req.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buf[] = new byte[4096];
        int letti;
        while ((letti = is.read(buf)) > 0) {
            baos.write(buf, 0, letti);
        }
        buffer = baos.toByteArray();
    }

    @Override
    public ServletInputStream getInputStream() {
        // Generate a new InputStream by stored buffer
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        // Istantiate a subclass of ServletInputStream
        // (Only ServletInputStream or subclasses of it are accepted by the servlet engine!)
        BufferedServletInputStream bsis = new BufferedServletInputStream(bais);
        return bsis;
    }
}
