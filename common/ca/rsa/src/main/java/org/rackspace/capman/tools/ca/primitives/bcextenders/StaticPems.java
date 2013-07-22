package org.rackspace.capman.tools.ca.primitives.bcextenders;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.rackspace.capman.tools.ca.PemUtils;
import org.rackspace.capman.tools.ca.primitives.PemBlock;
import org.rackspace.capman.tools.ca.primitives.RsaConst;
import org.rackspace.capman.tools.util.StaticHelpers;
import org.rackspace.capman.tools.util.fileio.RsaFileUtils;

public class StaticPems {

    public static final Set<X509CertificateObject> roots;
    public static final Set<X509CertificateObject> imds;

    static {
        RsaConst.init();
        Set<X509CertificateObject> x509Objs;
        try {
            x509Objs = getX509CertificateObjectSet("/pems/roots.crt");
        } catch (IOException ex) {
            x509Objs = new HashSet<X509CertificateObject>();
        }
        roots = x509Objs;
        try {
            x509Objs = getX509CertificateObjectSet("/pems/imds.crt");
        } catch (IOException ex) {
            x509Objs = new HashSet<X509CertificateObject>();
        }
        imds = x509Objs;
    }

    public static Set<X509CertificateObject> getRootsSet() {
        return new HashSet<X509CertificateObject>(roots);
    }

    public static Set<X509CertificateObject> getImdSet() {
        return new HashSet<X509CertificateObject>(imds);
    }

    public static Set<X509CertificateObject> getX509CertificateObjectSet(String fileName) throws IOException {
        Set<X509CertificateObject> objSet = new HashSet<X509CertificateObject>();
        List objList = readPemObjectsFromClass(fileName);
        for (Object obj : objList) {
            objSet.add((X509CertificateObject) obj);
        }
        return objSet;
    }

    private static List readPemObjectsFromClass(String fileName) throws IOException {
        byte[] pemBytes = RsaFileUtils.readFileFromClassPath(fileName);
        List<PemBlock> blocks = PemUtils.parseMultiPem(pemBytes);
        List objList = StaticHelpers.filterObjectList(PemUtils.getBlockObjects(blocks), null);
        return objList;
    }
}
