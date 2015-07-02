package org.openstack.atlas.util.ca.util;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.openstack.atlas.util.ca.primitives.Debug;
import org.openstack.atlas.util.ca.util.sslborker.OverTrustingSSLContext;

public class ConnectUtils {

    public static List<String> getSupportedCiphers() throws NoSuchAlgorithmException, KeyManagementException {
        List<String> cipherList = new ArrayList<String>();
        SSLContext sc = OverTrustingSSLContext.newOverTrustingSSLContext("SSL");
        SSLSocketFactory sf = sc.getSocketFactory();
        Collections.addAll(cipherList, sf.getSupportedCipherSuites());
        return cipherList;
    }

    public static List<String> getDefaultCiphers() throws NoSuchAlgorithmException, KeyManagementException {
        List<String> cipherList = new ArrayList<String>();
        SSLContext sc = OverTrustingSSLContext.newOverTrustingSSLContext("SSL");
        SSLSocketFactory sf = sc.getSocketFactory();
        Collections.addAll(cipherList, sf.getDefaultCipherSuites());
        return cipherList;
    }

    public static List<String> getServerCiphers(String host, int port) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        List<String> ordered_ciphers = new ArrayList<String>();
        Set<String> availableCiphers = new HashSet<String>(getSupportedCiphers());
        SSLContext sc = OverTrustingSSLContext.newOverTrustingSSLContext("SSL");
        SSLSocketFactory sf = sc.getSocketFactory();
        while (true) {
            int availableCipherCount = availableCiphers.size();
            if (availableCipherCount <= 0) {
                break;
            }
            String[] enableSiphers = availableCiphers.toArray(new String[availableCiphers.size()]);
            SSLSocket ss = (SSLSocket) sf.createSocket(host, port);
            try {
                ss.setEnabledCipherSuites(enableSiphers);
                SSLSession session = ss.getSession();
                String foundCipher = session.getCipherSuite();
                ordered_ciphers.add(foundCipher);
                if (!availableCiphers.contains(foundCipher)) {
                    break; // This is not a cipher that we asked for
                }
                availableCiphers.remove(foundCipher);
                try {
                    ss.close();
                } catch (Exception ex) {
                }
            } catch (Exception ex) {
                String exception = Debug.getExtendedStackTrace(ex);
                return ordered_ciphers;
            }
        }
        return ordered_ciphers;
    }
}
