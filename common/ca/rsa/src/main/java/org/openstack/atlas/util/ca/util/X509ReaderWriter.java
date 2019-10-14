package org.openstack.atlas.util.ca.util;

import java.net.ProtocolException;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.openstack.atlas.util.ca.util.fileio.RsaFileUtils;
import java.io.InputStream;
import org.openstack.atlas.util.ca.exceptions.X509ReaderException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.util.logging.Level;
import javax.net.ssl.SSLContext;
import org.openstack.atlas.util.ca.util.sslborker.TrustAllHostsVerifier;
import java.io.IOException;
import java.security.cert.Certificate;
import javax.net.ssl.HttpsURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.logging.Logger;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bouncycastle.cert.X509CertificateHolder;

import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.StringUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.primitives.Debug;
import org.openstack.atlas.util.ca.primitives.PemBlock;
import org.openstack.atlas.util.ca.primitives.RsaConst;
import org.openstack.atlas.util.ca.util.sslborker.OverTrustingSSLContext;
import static org.openstack.atlas.util.ca.StringUtils.asciiString;

public class X509ReaderWriter {

    private static final Logger LOG = Logger.getLogger(X509ReaderWriter.class.getName());
    private static int nopCount = 0;
    private static final int PAGESIZE = 4096;
    private static final String BEG_PRV;
    private static final String END_PRV;
    private static final String BEG_CSR;
    private static final String END_CSR;
    private static final String BEG_CRT;
    private static final String END_CRT;

    static {
        RsaConst.init();
        BEG_PRV = asciiString(PemUtils.BEG_PRV);
        BEG_CSR = asciiString(PemUtils.BEG_CSR);
        BEG_CRT = asciiString(PemUtils.BEG_CRT);
        END_PRV = asciiString(PemUtils.END_PRV);
        END_CSR = asciiString(PemUtils.END_CSR);
        END_CRT = asciiString(PemUtils.END_CRT);
    }

    public static ResponseWithExcpetions<String> writeSet(Collection<X509CertificateHolder> x509objs) {
        StringBuilder sb = new StringBuilder(PAGESIZE * 4);
        List<Exception> exceptions = new ArrayList<Exception>();
        for (X509CertificateHolder x509obj : x509objs) {
            try {
                String pem = PemUtils.toPemString(x509obj);
                sb.append(pem);
            } catch (PemException ex) {
                String fmt = "Object with hashcode %d "
                        + "could not be serialized to X509Certificate Pem";
                String msg = String.format(fmt, x509obj.hashCode());
                exceptions.add(buildNotX509ObjectException(msg, null, null, ex));
            }
        }
        return new ResponseWithExcpetions<String>(exceptions, sb.toString());
    }

    public static ResponseWithExcpetions<Set<X509CertificateHolder>> readSet(String pemString) {
        ResponseWithExcpetions<Set<X509CertificateHolder>> resp;
        List<Exception> exceptions = new ArrayList<Exception>();
        byte[] pemBytes = StringUtils.asciiBytes(pemString);
        Set<X509CertificateHolder> x509objs = new HashSet<X509CertificateHolder>();
        List<PemBlock> blocks = null;
        try {
            blocks = PemUtils.parseMultiPem(pemBytes);
        } catch (PemException ex) {
            exceptions.add(buildNotX509ObjectException(ex.getMessage(), null, null, ex));
        }
        for (PemBlock block : blocks) {
            if (!StringUtils.strEquals(block.getStartLine(), BEG_CRT)) {
                continue;
            }
            if (block.getDecodedObject() == null) {
                exceptions.add(buildNotX509ObjectException(null, block.getLineNum(), asciiString(block.getPemData()), null));
                continue;
            }
            if (!(block.getDecodedObject() instanceof X509CertificateHolder)) {
                exceptions.add(buildNotX509ObjectException(null, block.getLineNum(), asciiString(block.getPemData()), null));
                continue;
            }
            X509CertificateHolder x509obj = (X509CertificateHolder) block.getDecodedObject();
            x509objs.add(x509obj);
        }
        return new ResponseWithExcpetions<Set<X509CertificateHolder>>(exceptions, x509objs);
    }

    private static NotAnX509CertificateException buildNotX509ObjectException(String msg, Integer lineNum, String pemString, Throwable th) {
        NotAnX509CertificateException ex;
        StringBuilder sb = new StringBuilder();
        if (msg != null) {
            sb.append(msg);
        } else {
            if (lineNum == null) {
                sb.append("Object was not an X509Certificate");
            } else {
                sb.append(String.format("Object at line %d was not an X509Certificate", lineNum.intValue()));
            }
            if (pemString != null) {
                sb.append(String.format("\n%s", pemString));
            }
        }
        if (th != null) {
            ex = new NotAnX509CertificateException(sb.toString(), th);
        } else {
            ex = new NotAnX509CertificateException(sb.toString());
        }
        return ex;
    }

    public static List<X509CertificateHolder> getX509CertificateHolderFromSSLServer(String uriStr) throws X509ReaderException {
        URI uri;
        String fmt;
        String msg;

        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException ex) {
            fmt = "Caught exception trying to convert %s to uri";
            msg = String.format(fmt, uriStr);
            throw new X509ReaderException(msg, ex);
        }
        return getX509CertificateHolderFromSSLServer(uri);
    }

    public static List<X509CertificateHolder> getX509CertificateHolderFromSSLServer(URI uri) throws X509ReaderException {
        int i;
        String fmt;
        String msg;
        List<X509CertificateHolder> x509certObjs = new ArrayList<X509CertificateHolder>();
        URL url;
        try {
            url = uri.toURL();
        } catch (MalformedURLException ex) {
            fmt = "Caught Exception trying to convert %s to url:%s";
            msg = String.format(fmt, uri.toString(), StringUtils.getEST(ex));
            throw new X509ReaderException(msg, ex);
        }
        HttpsURLConnection con;
        try {
            con = (HttpsURLConnection) url.openConnection();
        } catch (IOException ex) {
            fmt = "Got IOException opening url to %s:%s";
            msg = String.format(fmt, url.toString(), StringUtils.getEST(ex));
            logEx(msg, ex);
            throw new X509ReaderException(msg, ex);
        }
        SSLContext sc;
        Certificate[] crts;
        try {
            sc = OverTrustingSSLContext.newOverTrustingSSLContext("SSL");
            con.setSSLSocketFactory(sc.getSocketFactory());
            con.setHostnameVerifier(new TrustAllHostsVerifier());
        } catch (NoSuchAlgorithmException ex) {
            logAndThrowSSLSetupExeption(uri, ex);
        } catch (KeyManagementException ex) {
            logAndThrowSSLSetupExeption(uri, ex);
        }
        try {
            con.connect();
        } catch (IOException ex) {
            fmt = "Exception caught when trying to connect to ssl Server: %s";
            msg = String.format(fmt, uri.toString(), StringUtils.getEST(ex));
            logEx(msg, ex);
            throw new X509ReaderException(msg, ex);
        }
        try {
            crts = con.getServerCertificates();
        } catch (SSLPeerUnverifiedException ex) {
            fmt = "Exception caught when trying to retrieve Peer certs from ssl Server: %s";
            msg = String.format(fmt, uri.toString(), StringUtils.getEST(ex));
            logEx(msg, ex);
            throw new X509ReaderException(msg, ex);
        }

        for (Certificate crt : crts) {
            String className = crt.getClass().getName();
            if (crt instanceof X509Certificate) {
                X509Certificate x509 = (X509Certificate) crt;
                String exMsg;
                try {
                    X509Inspector xi = X509Inspector.newX509Inspector(x509);
                    X509CertificateHolder x509obj = xi.getX509CertificateHolder();
                    x509certObjs.add(x509obj);
                } catch (CertificateEncodingException ex) {
                    logEx(ex);
                    continue;
                } catch (CertificateParsingException ex) {
                    logEx(ex);
                    continue;
                } catch (NotAnX509CertificateException ex) {
                    logEx(ex);
                    continue;
                }
            }
        }
        con.disconnect();
        return x509certObjs;
    }

    private static X509ReaderException logAndThrowSSLSetupExeption(URI uri, Exception ex) {


        String fmt = "Exception caught setting up over trusting SSL socket factory for %s:%s";
        String msg = String.format(uri.toString(), StringUtils.getEST(ex));
        logEx(msg, ex);
        X509ReaderException newEx = new X509ReaderException(msg, ex);
        return newEx;
    }

    public static List<X509CertificateHolder> getCaX509CertificateObjectFromUriString(String uriStr) throws X509ReaderException {
        String fmt;
        String msg;
        URI uri;

        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException ex) {
            fmt = "Caught exception trying to convert %s to uri";
            msg = String.format(fmt, uriStr);
            throw new X509ReaderException(msg, ex);
        }
        return getCaX509CertificateObjectFromUri(uri);
    }

    public static List<X509CertificateHolder> getCaX509CertificateObjectFromUri(URI uri) throws X509ReaderException {
        String fmt;
        String msg;
        URL url;
        try {
            url = uri.toURL();
        } catch (MalformedURLException ex) {
            fmt = "Caught Exception trying to convert %s to url:%s";
            msg = String.format(fmt, uri.toString(), StringUtils.getEST(ex));
            throw new X509ReaderException(msg, ex);
        }
        String uriScheme = uri.getScheme();


        HttpURLConnection con;
        if (uriScheme.equals("http")) {
            try {
                con = (HttpURLConnection) url.openConnection();
            } catch (IOException ex) {
                fmt = "Got IOException opening url to %s:%s";
                msg = String.format(fmt, url.toString(), StringUtils.getEST(ex));
                logEx(msg, ex);
                throw new X509ReaderException(msg, ex);
            }
        } else if (uriScheme.equals("https")) {
            try {
                con = (HttpsURLConnection) url.openConnection();
            } catch (IOException ex) {
                fmt = "Got IOException opening url to %s:%s";
                msg = String.format(fmt, url.toString(), StringUtils.getEST(ex));
                logEx(msg, ex);
                throw new X509ReaderException(msg, ex);
            }
        } else {
            fmt = "Unknown schema %s from url %s";
            msg = String.format(fmt, url.toString());
            LOG.severe(msg);
            throw new X509ReaderException(msg);
        }
        con.setRequestProperty("accept", "application/x-x509-ca-cert");
        try {
            con.setRequestMethod("GET");
        } catch (ProtocolException ex) {
            fmt = "Error GET method protocal not recognized when setting up connection to %s: %s ";
            msg = String.format(fmt, url.toString(), StringUtils.getEST(ex));
            logEx(msg, ex);
            throw new X509ReaderException(msg);
        }
        con.setDoOutput(false);
        con.setDoInput(true);
        InputStream is;
        byte[] pemBytes;
        String pemStr;
        try {
            is = (InputStream) con.getContent();
            pemBytes = RsaFileUtils.readInputStream(is);
            pemStr = new String(pemBytes);
        } catch (IOException ex) {
            fmt = "Got exception while reading http connection from %s:%s";
            msg = String.format(fmt, url.toString(), StringUtils.getEST(ex));
            throw new X509ReaderException(msg);
        }
        List<X509CertificateHolder> x509objs;
        try {
            x509objs = decodeAsPem(pemBytes);
        } catch (PemException ex) {
            throw new X509ReaderException(ex.getMessage());
        }
        if (x509objs.size() > 0) {
            return x509objs;
        }
        try {
            x509objs = decodeAsDer(pemBytes);
            if(x509objs.size() < 1){
                throw new X509ReaderException("Error decoded 0 objects from DER encoded response");
            }
            return x509objs;
        } catch (X509ReaderException ex) {
            throw new X509ReaderException("Could not decode X509 certificate", ex);
        }
    }

    private static List<X509CertificateHolder> decodeAsDer(byte[] pemBytes) throws X509ReaderException {
        InputStream is;
        is = new ByteArrayInputStream(pemBytes);
        CertificateFactory cf;
        List<X509CertificateHolder> x509objs = new ArrayList<X509CertificateHolder>();
        try {
            cf = CertificateFactory.getInstance("X.509", "BC");
        } catch (CertificateException ex) {
            throw new X509ReaderException("Could not initialize CertificateFactory CertificateException", ex);
        } catch (NoSuchProviderException ex) {
            throw new X509ReaderException("Could not initialize CertificateFactory Could not find Bouncycastle provider", ex);
        }

        Object obj;
        while (true) {
            try {
                obj = cf.generateCertificate(is);
            } catch (CertificateException ex) {
                throw new X509ReaderException("Certificate Error trying decode X509CertificateObject as DER");
            }
            if (obj == null) {
                break;
            }
            if (!(obj instanceof X509CertificateHolder)) {
                continue;
            }
            x509objs.add((X509CertificateHolder)obj);
        }
        return x509objs;
    }

    private static List<X509CertificateHolder> decodeAsPem(byte[] pemBytes) throws PemException {
        String msg;
        List<X509CertificateHolder> x509ObjList = new ArrayList<X509CertificateHolder>();
        List<PemBlock> blocks = PemUtils.parseMultiPem(pemBytes);
        for (PemBlock block : blocks) {
            if (block.getDecodedObject() == null) {
                continue;
            }
            if (!(block.getDecodedObject() instanceof X509CertificateHolder)) {
                continue;
            }
            x509ObjList.add((X509CertificateHolder) block.getDecodedObject());
        }

        return x509ObjList;
    }

    private static void logEx(Throwable th) {
        String exMsg = StringUtils.getEST(th);
        String msg = "Exception Causght: " + StringUtils.getEST(th);
        LOG.severe(msg);
    }

    private static void logEx(String msg, Throwable th) {
        String exMsg = String.format("%s:%s", msg, StringUtils.getEST(th));
        LOG.severe(msg);
    }
}
