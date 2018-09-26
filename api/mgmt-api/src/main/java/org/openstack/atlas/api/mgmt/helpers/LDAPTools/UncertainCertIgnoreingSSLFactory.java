package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

// Since no one wants to pay for signed their certs
// we might as well forget about validating them :(
public class UncertainCertIgnoreingSSLFactory extends SocketFactory {

    private static SocketFactory ignoreCertFactory = null;

    static {

        TrustManager[] bogusTrustMan = new TrustManager[]{new X509TrustManager() {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] c, String a) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] c, String a) {
        }
    }
        };


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
        Socket socket = ignoreCertFactory.createSocket();
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress address, int port) throws IOException {
        Socket socket = ignoreCertFactory.createSocket(address, port);
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
            int localPort) throws IOException {
        Socket socket = ignoreCertFactory.createSocket(address, port, localAddress, localPort);
        return socket;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException,
            UnknownHostException {
        Socket socket = ignoreCertFactory.createSocket(host, port);
        return socket;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        Socket socket = ignoreCertFactory.createSocket(host, port, localHost, localPort);
        return socket;
    }

    public static SocketFactory getDefault() {
        return new UncertainCertIgnoreingSSLFactory();
    }

    public static Socket removeSSLv2v3(Socket socket) {
        SSLSocket sslSocket = (SSLSocket) socket;
        String[] newProts;
        int i;
        int n;
        if (socket instanceof SSLSocket) {
            List<String> allowedProts = new ArrayList<String>();
            for (String currProt : sslSocket.getEnabledProtocols()) {
                if (currProt.equals("SSLv2Hello") || currProt.equals("SSLv3")) {
                    continue;
                }
                allowedProts.add(currProt);
            }
            n = allowedProts.size();
            newProts = new String[n];
            for(i=0;i<n;i++){
                newProts[i] = allowedProts.get(i);
            }
            sslSocket.setEnabledProtocols(newProts);
            return sslSocket;
        } else {
            // Not sure how we got a plain
            // Socket here so lets just return it
            return socket;
        }
    }
}
