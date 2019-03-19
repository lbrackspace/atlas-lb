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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.bouncycastle.cert.X509CertificateHolder;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.util.X509Inspector;
import org.openstack.atlas.util.ca.util.sslborker.OverTrustingTrustProvider;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class TLSInspectorMain {

    static {
        RsaConst.init();
        Security.addProvider(new OverTrustingTrustProvider());
        Security.setProperty("ssl.TrustManagerFactory.algorithm", "TrustAllCertificates");
    }

    public static void main(String[] args) throws IOException {
        SecureRandom rnd;
        int i;
        int n;
        Map<String, String> kwArgs = StaticStringUtils.argMapper(args);
        String[] posArgs = StaticStringUtils.stripKwArgs(args);
        List<String> tmpStringList = new ArrayList<String>();
        if (posArgs.length < 2) {
            System.out.printf("Usage is <host> <port> [enableProts=...] [disableProts=...] [showPem=true|false]\n]");
            System.out.printf("\n");
            System.out.printf("Connect to the TLS socket and display the\n");
            System.out.printf("cipher and TLS protocol used for the connection.\n");
            System.out.printf("Also you can use enableProts and disableProts\n");
            System.out.printf("to disabler or enable those protocols from the\n");
            System.out.printf("client side\n");
            return;
        }

        BufferedReader stdin = StaticFileUtils.inputStreamToBufferedReader(System.in);
        String host = posArgs[0];
        int port = Integer.valueOf(posArgs[1]);
        boolean showPem = false;
        if (kwArgs.containsKey("showPem") && kwArgs.get("showPem").equalsIgnoreCase("true")) {
            showPem = true;
        }
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
        Set<String> supportedProtocols = toStringSet(sslSocket.getSupportedProtocols());
        Set<String> enabledProtocols = toStringSet(sslSocket.getEnabledProtocols());

        System.out.printf("Suppoertd Protocols:");
        for (String protocol : supportedProtocols) {
            System.out.printf(" %s", protocol);
        }
        System.out.printf("\n");
        System.out.printf("enabledProtocols:");
        for (String protocol : enabledProtocols) {
            System.out.printf(" %s", protocol);
        }
        System.out.printf("\n");
        boolean protsChanged = false;
        if (kwArgs.containsKey("disableProts")) {
            System.out.printf("Attempting to disabled protocols:\n");
            String[] disableSplit = kwArgs.get("disableProts").split(",");
            n = disableSplit.length;
            for (i = 0; i < n; i++) {
                String prot = disableSplit[i];
                System.out.printf("Disableing %s: ", prot);
                if (!supportedProtocols.contains(prot)) {
                    System.out.printf("unknown protocol %s\n", prot);
                } else {
                    if (enabledProtocols.remove(prot)) {
                        System.out.printf("REMOVED\n");
                    } else {
                        System.out.printf("Already removed\n");
                    }
                }
            }
            protsChanged = true;
        }
        if (kwArgs.containsKey("enableProts")) {
            System.out.printf("Attempting to disabled protocols:\n");
            String[] enableSplit = kwArgs.get("disableProts").split(",");
            n = enableSplit.length;
            for (i = 0; i < n; i++) {
                String prot = enableSplit[i];
                System.out.printf("Enableing %s: ", prot);
                if (!supportedProtocols.contains(prot)) {
                    System.out.printf("unknown protocol %s\n", prot);
                } else {
                    if (enabledProtocols.add(prot)) {
                        System.out.printf("ADDED\n");
                    } else {
                        System.out.printf("Already added\n");
                    }
                }
            }
            protsChanged = true;
        }
        if (protsChanged) {
            System.out.printf("allowed protocols changed to:");
            String[] newProts = toStringArray(enabledProtocols);
            n = newProts.length;
            for (i = 0; i < n; i++) {
                System.out.printf(" %s", newProts[i]);
            }
            System.out.printf("\n");
            sslSocket.setEnabledProtocols(newProts);
        }
        System.out.printf("Starting habndshake\n");
        sslSocket.startHandshake();
        SSLSession session = sslSocket.getSession();
        SSLSessionContext sessCtx = session.getSessionContext();
        Certificate[] chain = session.getPeerCertificates();
        n = chain.length;
        X509Inspector xi;
        for (i = 0; i < n; i++) {
            try {
                xi = new X509Inspector((X509Certificate) chain[i]);
            } catch (NotAnX509CertificateException ex) {
                System.out.printf("Unable to decode class %s to X509Certificate\n", chain[i].getClass().getName());
                continue;
            }
            if (showPem) {
                System.out.printf("x509:\n");
                try {
                    X509CertificateHolder x509h = xi.getX509CertificateHolder();
                    String pemStr = PemUtils.toPemString(x509h);
                    System.out.printf("%s\n", pemStr);
                } catch (PemException ex) {
                    System.out.printf("  Unable to encode to pem bytes\n");
                }
            }
            X509CertificateHolder x509obj = xi.getX509CertificateHolder();
            BigInteger authIdSerial = xi.getAuthKeyIdSerial();
            String authIdSerialStr = (authIdSerial == null) ? null : authIdSerial.toString(16);
            System.out.printf("\n");
            System.out.printf("Subject: %s\n", xi.getSubjectName());
            System.out.printf("Issuer: %s\n", xi.getIssuerName());
            System.out.printf("Serial: %s\n", xi.getSerial().toString(16));
            System.out.printf("authDirName = %s\n", xi.getAuthKeyIdDirname());
            try {
                System.out.printf("ocspURI: %s\n", xi.getOCSPUri());
            } catch (NotAnX509CertificateException ex) {
                System.out.printf("Undecodable\n");
            }
            System.out.printf("ocspCaUri: %s\n", xi.getOCSPCaUri());
        }

        System.out.printf("\n");
        System.out.printf("Cipher: %s\n", session.getCipherSuite());
        System.out.printf("protocol: %s\n", session.getProtocol());
        System.out.printf("peer host: %s\n", session.getPeerHost());
        System.out.printf("ip: %s\n", ipStr);
    }

    // Creates a ordered set
    public static Set<String> toStringSet(String[] strArray) {
        Set<String> strSet = new LinkedHashSet<String>();
        Collections.addAll(strSet, strArray);
        return strSet;
    }

    public static String[] toStringArray(Set<String> strSet) {
        String[] strArray = new String[strSet.size()];
        strSet.toArray(strArray);
        return strArray;
    }
}
