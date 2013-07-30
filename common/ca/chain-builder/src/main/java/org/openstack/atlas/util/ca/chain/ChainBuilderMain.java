package org.openstack.atlas.util.ca.chain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.openstack.atlas.util.ca.CertUtils;
import org.openstack.atlas.util.ca.CsrUtils;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.RSAKeyUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.primitives.PemBlock;
import org.openstack.atlas.util.ca.util.X509ChainEntry;
import org.openstack.atlas.util.ca.util.X509Inspector;
import org.openstack.atlas.util.ca.util.X509PathBuilder;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class ChainBuilderMain {

    private static final double MILLIS_PER_SEC = 1000.0;
    public static final int PAGESIZE = 4096;
    public static final int BUFFSIZE = 1024 * 64;

    public static void main(String[] args) throws IOException, NotAnX509CertificateException, RsaException {
        ChainConfig conf;
        String confFile;
        if (args.length <= 0) {
            System.out.printf("usage is %s <config_file> [notAfterSecs] [notBeforeSecs]\n", Debug.getProgName(ChainBuilderMain.class));
            System.out.printf("\n");
            System.out.printf("Builds a certificate based on the criteria in the config_file\n");
            System.out.printf("An example configuration file is:\n");
            System.out.printf("%s", ChainConfig.getConfExample());
            return;
        }
        //BufferedReader stdin = StaticFileUtils.inputStreamToBufferedReader(System.in, PAGESIZE);
        //System.out.printf("press enter to confinute");
        //stdin.readLine();
        confFile = args[0];
        try {
            conf = ChainConfig.loadChainerConfig(confFile);
        } catch (Exception ex) {
            System.out.printf("%s\n", Debug.getExtendedStackTrace(ex));
            return;
        }

        if (args.length >= 2) {
            conf.setNotAfter(Double.parseDouble(args[1]));
        }

        if (args.length >= 3) {
            conf.setNotBefore(Double.parseDouble(args[2]));
        }

        System.out.printf("using config: %s\n", conf.toString());
        KeyPair rootKey = null;
        X509CertificateObject rootCrt = null;

        List<PemBlock> pemBlocks;
        pemBlocks = PemUtils.parseMultiPem(StaticFileUtils.readFile(conf.getRootKeyFile()));
        for (PemBlock pemBlock : pemBlocks) {
            System.out.printf("block class in %s = %s\n", conf.getRootKeyFile(), pemBlock.getDecodedObject().getClass().getName());
            if (pemBlock.getDecodedObject() instanceof KeyPair) {
                rootKey = (KeyPair) pemBlock.getDecodedObject();
                break;
            }
        }

        pemBlocks = PemUtils.parseMultiPem(StaticFileUtils.readFile(conf.getRootCrtFile()));
        for (PemBlock pemBlock : pemBlocks) {
            System.out.printf("block class in %s = %s\n", conf.getCrtFile(), pemBlock.getDecodedObject().getClass().getName());
            if (pemBlock.getDecodedObject() instanceof X509CertificateObject) {
                rootCrt = (X509CertificateObject) pemBlock.getDecodedObject();
                break;
            }
        }

        if (rootKey == null || rootCrt == null) {
            System.out.printf("could not decode root key or crt\n");
            return;
        }
        List<String> issuerNames = conf.getIssuers();
        Date now = new Date(System.currentTimeMillis());
        Date notBefore = secsFromDate(now, conf.getNotBefore());
        Date notAfter = secsFromDate(now, conf.getNotAfter());
        List<X509ChainEntry> chainEntries = X509PathBuilder.newChain(rootKey, rootCrt, issuerNames, conf.getKeySize(), notBefore, notAfter);
        KeyPair finalSigningKey;
        X509CertificateObject finalIssueingCrt;
        System.out.printf("RootCrt:\n%s\n", inspectCrt(rootCrt));
        System.out.printf("Imd crts:\n");
        if (!chainEntries.isEmpty()) {
            for (X509ChainEntry entry : chainEntries) {
                System.out.printf("%s\n\n", inspectCrt(entry.getX509obj()));
            }
            finalSigningKey = chainEntries.get(chainEntries.size() - 1).getKey();
            finalIssueingCrt = chainEntries.get(chainEntries.size() - 1).getX509obj();
        } else {
            finalSigningKey = rootKey;
            finalIssueingCrt = rootCrt;
        }
        String keyFile = StaticFileUtils.expandUser(conf.getKeyFile());
        String crtFile = StaticFileUtils.expandUser(conf.getCrtFile());
        String imdFile = StaticFileUtils.expandUser(conf.getImdFile());

        System.out.printf("Saving imd chain to file %s\n", imdFile);
        Collections.reverse(chainEntries);
        OutputStream os;
        os = StaticFileUtils.openOutputFile(imdFile, BUFFSIZE);
        for (X509ChainEntry entry : chainEntries) {
            String x509Pem = PemUtils.toPemString(entry.getX509obj());
            os.write(x509Pem.getBytes("utf-8"));
        }
        os.close();
        System.out.printf("imd file saved\n");

        System.out.printf("Generating %d bit key for userKey\n", conf.getKeySize());
        KeyPair key = RSAKeyUtils.genKeyPair(conf.getKeySize());
        System.out.printf("user key generated\n");
        String keyPem = PemUtils.toPemString(key);
        System.out.printf("%s\n", keyPem);
        System.out.printf("Saving key top %s\n", keyFile);
        os = StaticFileUtils.openOutputFile(keyFile, BUFFSIZE);
        os.write(keyPem.getBytes("utf-8"));
        os.close();
        System.out.printf("key saved\n");

        System.out.printf("Generating csr for key with subjName: %s\n", conf.getSubjName());
        PKCS10CertificationRequest csr = CsrUtils.newCsr(conf.getSubjName(), key, false);
        System.out.printf("CSR generated\n");
        String csrPem = PemUtils.toPemString(csr);
        System.out.printf("%s\n", csrPem);
        System.out.printf("Signing CSR with crt %s\n", inspectCrt(finalIssueingCrt));
        X509CertificateObject crt = (X509CertificateObject) CertUtils.signCSR(csr, finalSigningKey, finalIssueingCrt, notBefore, notAfter, BigInteger.ZERO);
        System.out.printf("Certificate signed\n%s\n", inspectCrt((X509CertificateObject) crt));
        String crtPem = PemUtils.toPemString(crt);
        System.out.printf("%s\n", crtPem);
        System.out.printf("Saving crt to file %s\n", crtFile);
        os = StaticFileUtils.openOutputFile(crtFile, BUFFSIZE);
        os.write(crtPem.getBytes("utf-8"));
        os.close();
        System.out.printf("crt saved\n");
    }

    public static Date secsFromDate(Date now, double secs) {
        return new Date((long) ((double) (now.getTime()) + secs * MILLIS_PER_SEC));
    }

    public static String inspectCrt(X509CertificateObject crt) throws NotAnX509CertificateException {
        StringBuilder sb = new StringBuilder();
        X509Inspector xi = new X509Inspector(crt);
        String notBefore = StaticDateTimeUtils.toSqlTime(StaticDateTimeUtils.toDate(xi.getNotBefore()));
        String notAfter = StaticDateTimeUtils.toSqlTime(StaticDateTimeUtils.toDate(xi.getNotAfter()));
        String serial = xi.getSerial().toString(16);
        sb.append("issuer=").append(xi.getIssuerName()).append("\n").
                append("subj=").append(xi.getSubjectName()).append("\n").
                append("serial=").append(serial).append("\n").
                append("notBefore=").append(notBefore).append("\n").
                append("notAfter=").append(notAfter).append("\n").
                append("authKey=").append(inspectAuthKey(xi)).append("\n").
                append("subjKey=").append(xi.getSubjKeyId()).append("\n");

        return sb.toString();
    }

    public static String inspectAuthKey(X509Inspector xi) {
        StringBuilder sb = new StringBuilder();

        sb.append("{").
                append("id=").append((xi.getAuthKeyId() == null) ? "null" : xi.getAuthKeyId()).
                append(", serial=").append((xi.getAuthKeyIdSerial() == null) ? "null" : xi.getAuthKeyIdSerial().toString(16)).
                append(", dirName=").append((xi.getAuthKeyIdDirname() == null) ? "null" : xi.getAuthKeyIdDirname()).
                append("}");

        return sb.toString();
    }
}
