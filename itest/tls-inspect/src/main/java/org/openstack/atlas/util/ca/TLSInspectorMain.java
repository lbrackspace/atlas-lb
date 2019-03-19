package org.openstack.atlas.util.ca;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import org.openstack.atlas.util.ca.primitives.RsaConst;
import java.security.SecureRandom;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.bouncycastle.cert.X509CertificateHolder;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.util.X509Inspector;
import org.openstack.atlas.util.ca.util.sslborker.OverTrustingTrustProvider;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class TLSInspectorMain {

    static {
        RsaConst.init();
        Security.addProvider(new OverTrustingTrustProvider());
        Security.setProperty("ssl.TrustManagerFactory.algorithm", "TrustAllCertificates");
    }

    public static void main(String[] args) throws IOException {
        SecureRandom rnd;
        int i;
        int nCerts;
        if (args.length < 2) {
            System.out.printf("Usage is <host> <port>\n");
            System.out.printf("\n");
            System.out.printf("Connect to the TLS socket and display the\n");
            System.out.printf("cipher and TLS protocol used for the connection.\n");
            return;
        }

        BufferedReader stdin = StaticFileUtils.inputStreamToBufferedReader(System.in);
        String host = args[0];
        int port = Integer.valueOf(args[1]);

        System.out.printf("Connecting to host %s port %d\n", host, port);
        rnd = new SecureRandom();
        InetAddress ip;
        try {
            ip = InetAddress.getByName(host);
        } catch (UnknownHostException ex) {
            System.out.printf("unknown host %s\n", host);
            return;
        }
        String ipStr = ip.getHostAddress();
        System.out.printf("Connecting to ip %s\n", ipStr);
        SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket) sf.createSocket(ip, port);
        SSLSession session = sslSocket.getSession();
        Certificate[] chain = session.getPeerCertificates();
        nCerts = chain.length;
        X509Inspector xi;
        for (i = 0; i < nCerts; i++) {
            try {
                xi = new X509Inspector((X509Certificate) chain[i]);
            } catch (NotAnX509CertificateException ex) {
                System.out.printf("Unable to decode class %s to X509Certificate\n", chain[i].getClass().getName());
                continue;
            }
            X509CertificateHolder x509obj = xi.getX509CertificateHolder();
            String pem;
            try {
                pem = PemUtils.toPemString(x509obj);
            } catch (PemException ex) {
                pem = "Unable to decode PemString\n";
            }
            BigInteger authIdSerial = xi.getAuthKeyIdSerial();
            String authIdSerialStr = (authIdSerial == null) ? null : authIdSerial.toString(16);
            System.out.printf("Pem: %s\n", pem);
            System.out.printf("\n");
            System.out.printf("Subject: %s\n", xi.getSubjectName());
            System.out.printf("Issuer: %s\n", xi.getIssuerName());
            System.out.printf("Serial: %s\n", xi.getSerial().toString(16));
            System.out.printf("authKeyId = %s\n", xi.getAuthKeyId());
            System.out.printf("authDirName = %s\n", xi.getAuthKeyIdDirname());
            System.out.printf("authIdSerial: %s\n", authIdSerialStr);
            System.out.printf("subjKeyId: %s\n", xi.getSubjKeyId());
            try {
                System.out.printf("ocspURI: %s\n", xi.getOCSPUri());
            } catch (NotAnX509CertificateException ex) {
                System.out.printf("Undecodable\n");
            }
            System.out.printf("ocspCaUri: %s\n", xi.getOCSPCaUri());
            System.out.printf("%s\n", pem);
        }

        System.out.printf("\n");
        System.out.printf("Cipher: %s\n", session.getCipherSuite());
        System.out.printf("protocol: %s\n", session.getProtocol());
        System.out.printf("peer host: %s\n", session.getPeerHost());
        System.out.printf("ip: %s\n", ipStr);
    }
}
