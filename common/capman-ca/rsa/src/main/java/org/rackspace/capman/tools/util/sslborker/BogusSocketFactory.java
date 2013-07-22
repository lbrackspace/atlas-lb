package org.rackspace.capman.tools.util.sslborker;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

// Since no one wants to sign their certs we might as well forget about validating them :(
public class BogusSocketFactory extends SocketFactory {

    private static SocketFactory ignoreCertFactory = null;

    static {
        TrustManager[] bogusTrustMan = new TrustManager[]{new OverTrustingX509TrustManager()};
        try {
            SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(null, bogusTrustMan, new java.security.SecureRandom());
            ignoreCertFactory = ctx.getSocketFactory();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Socket createSocket() throws IOException {
        return ignoreCertFactory.createSocket();
    }

    @Override
    public Socket createSocket(InetAddress address, int port) throws IOException {
        return ignoreCertFactory.createSocket(address, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
            int localPort) throws IOException {
        return ignoreCertFactory.createSocket(address, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException,
            UnknownHostException {
        return ignoreCertFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        return ignoreCertFactory.createSocket(host, port, localHost, localPort);
    }

    public static SocketFactory getDefault() {
        return new BogusSocketFactory();
    }
}
