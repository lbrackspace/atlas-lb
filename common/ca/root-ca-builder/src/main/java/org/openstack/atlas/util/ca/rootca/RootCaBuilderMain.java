package org.openstack.atlas.util.ca.rootca;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.util.ca.CertUtils;
import org.openstack.atlas.util.ca.CsrUtils;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.RSAKeyUtils;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class RootCaBuilderMain {

    private static final double MILLIS_PER_SEC = 1000.0;
    private static final int PAGESIZE = 4096;
    private static final int BUFFSIZE = 1024 * 64;

    public static void usage(String prog) {
        System.out.printf("Usage is %s <confFile> \n", prog);
        System.out.printf("\n");
        System.out.printf("Generate a root CA key and certificate with the given subject name\n");
        System.out.printf("Within the date range specified above. Usaful for testing\n");
        System.out.printf("An example of the confFile is below\n");
        System.out.printf("%s\n", RootCaConfig.getConfExample());
    }

    public static void main(String[] args) throws RsaException, UnsupportedEncodingException, ParseException, FileNotFoundException, IOException {
        if (args.length < 1) {
            usage(Debug.getProgName(RootCaBuilderMain.class));
            return;
        }
        //BufferedReader stdin = StaticFileUtils.inputStreamToBufferedReader(System.in, PAGESIZE);
        //System.out.printf("press enter to confinute");
        //stdin.readLine();
        String confFile = args[0];
        RootCaConfig conf = RootCaConfig.loadRootCaConfig(confFile);
        System.out.printf("Useing config: %s\n", conf.toString());
        String keyFile = StaticFileUtils.expandUser(conf.getKeyFile());
        String crtFile = StaticFileUtils.expandUser(conf.getCrtFile());
        System.out.printf("Generating %d bit key\n", conf.getKeySize());
        KeyPair rootKey = RSAKeyUtils.genKeyPair(conf.getKeySize());
        String keyPem = PemUtils.toPemString(rootKey);
        System.out.printf("%s\n", keyPem);
        System.out.printf("Saving key to file %s\n", keyFile);
        OutputStream os = StaticFileUtils.openOutputFile(keyFile, BUFFSIZE);
        os.write(keyPem.getBytes("utf-8"));
        os.close();
        System.out.printf("keyfile writtent\n");

        Date now = new Date(System.currentTimeMillis());
        Date notBefore = secsFromDate(now, conf.getNotBefore());
        Date notAfter = secsFromDate(now, conf.getNotAfter());
        System.out.printf("Generating CSR for subj: %s\n", conf.getSubjName());
        System.out.printf("Setting notBefore in CSR to: %s\n", StaticDateTimeUtils.toSqlTime(notBefore));
        System.out.printf("Setting notAfter in CSR to: %s\n", StaticDateTimeUtils.toSqlTime(notAfter));
        PKCS10CertificationRequest csr = CsrUtils.newCsr(conf.getSubjName(), rootKey, true);
        System.out.printf("Csr generated\n");
        String csrPem = PemUtils.toPemString(csr);
        System.out.printf("%s\n", csrPem);
        System.out.printf("Self signing CSR with rootKey\n");
        X509Certificate crt = CertUtils.selfSignCsrCA(csr, rootKey, notBefore, notAfter);
        System.out.printf("cert generated with self signature\n");
        String crtPem = PemUtils.toPemString(crt);
        System.out.printf("%s\n", crtPem);
        System.out.printf("Saving rootCrt to: %s\n", crtFile);
        os = StaticFileUtils.openOutputFile(crtFile, BUFFSIZE);
        os.write(crtPem.getBytes("utf-8"));
        os.close();
        System.out.printf("Crt saved\n");
    }

    public static Date secsFromDate(Date now, double secs) {
        return new Date((long) ((double) (now.getTime()) + secs * MILLIS_PER_SEC));
    }
}
