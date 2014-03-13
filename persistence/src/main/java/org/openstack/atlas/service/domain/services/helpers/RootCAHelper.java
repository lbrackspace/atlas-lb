package org.openstack.atlas.service.domain.services.helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.util.ca.exceptions.X509PathBuildException;
import org.openstack.atlas.util.ca.primitives.PemBlock;
import org.openstack.atlas.util.ca.primitives.RsaConst;
import org.openstack.atlas.util.ca.util.X509BuiltPath;
import org.openstack.atlas.util.ca.util.X509PathBuilder;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.util.ClassSet;
import org.openstack.atlas.util.debug.Debug;

public class RootCAHelper {

    private static final String rootCaFileName;
    private static Set<X509Certificate> rootCAs;
    private static final Log LOG = LogFactory.getLog(RootCAHelper.class);

    static {
        RsaConst.init();
        RestApiConfiguration conf = new RestApiConfiguration();
        rootCaFileName = conf.getString(PublicApiServiceConfigurationKeys.root_ca_file);
        rootCAs = new HashSet<X509Certificate>();
        try {
            reloadCAs();
        } catch (FileNotFoundException ex) {
            rootCaFileReadError(ex);
        } catch (IOException ex) {
            rootCaFileReadError(ex);
        }
    }

    public static int size() {
        int nRootCas;
        synchronized (RootCAHelper.class) {
            nRootCas = rootCAs.size();
        }
        return nRootCas;
    }

    private static void rootCaFileReadError(Throwable ex) {
        String excMsg = Debug.getExtendedStackTrace(ex);
        LOG.error(String.format("error reading %s for RootCAs:%s", rootCaFileName, excMsg));
    }

    public static int reloadCAs() throws FileNotFoundException, IOException {
        int nCas = 0;
        Set<X509Certificate> rootCasFromFile = new HashSet<X509Certificate>();
        File caFile = new File(StaticFileUtils.expandUser(rootCaFileName));
        byte[] rootCaBytes = StaticFileUtils.readFile(caFile);
        List<PemBlock> blocks = PemUtils.parseMultiPem(rootCaBytes);
        for (PemBlock block : blocks) {
            Object obj = block.getDecodedObject();
            if (ClassSet.isSuperOf(X509Certificate.class, obj.getClass())) {
                rootCasFromFile.add((X509Certificate) obj);
            }
        }

        synchronized (RootCAHelper.class) {
            rootCAs = new HashSet<X509Certificate>();
            rootCAs.addAll(rootCasFromFile);
            nCas = rootCAs.size();
        }
        return nCas;
    }

    public static String getRootCaFileName() {
        return rootCaFileName;
    }

    public static void clear() {
        synchronized (RootCAHelper.class) {
            rootCAs = new HashSet<X509Certificate>();
        }
    }

    public static boolean add(X509Certificate rootCa) {
        boolean result;
        synchronized (RootCAHelper.class) {
            result = rootCAs.add(rootCa);
        }
        return result;
    }

    public static void add(Collection<X509Certificate> rootCasIn) {
        synchronized (RootCAHelper.class) {
            rootCAs.addAll(rootCAs);
        }
    }

    public static X509BuiltPath<X509Certificate> suggestPath(X509Certificate crt, List<X509Certificate> imds, Date date) throws X509PathBuildException {
        Set<X509Certificate> imdSet = new HashSet<X509Certificate>();
        imdSet.addAll(imds);
        return suggestPath(crt, imdSet, date);
    }

    public static X509BuiltPath<X509Certificate> suggestPath(X509Certificate crt, Set<X509Certificate> imdSet, Date date) throws X509PathBuildException {
        Set<X509Certificate> rootCaCopy = getRootCASet();
        X509PathBuilder<X509Certificate> pathBuilder = new X509PathBuilder<X509Certificate>(rootCaCopy, imdSet);
        X509BuiltPath<X509Certificate> path = pathBuilder.buildPath(crt, date);
        return path;
    }

    public static List<X509Certificate> getRootCAList() {
        List<X509Certificate> rootCaCopy = new ArrayList<X509Certificate>();
        synchronized (RootCAHelper.class) {
            rootCaCopy.addAll(rootCAs);
        }
        return rootCaCopy;
    }

    public static Set<X509Certificate> getRootCASet() {
        Set<X509Certificate> rootCaCopy = new HashSet<X509Certificate>();
        synchronized (RootCAHelper.class) {
            rootCaCopy.addAll(rootCAs);
        }
        return rootCaCopy;
    }
}
