package org.rackspace.capman.tools.util.fileio;

import org.rackspace.capman.tools.util.X509MapValue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.rackspace.capman.tools.ca.PemUtils;
import org.rackspace.capman.tools.ca.primitives.PemBlock;
import org.rackspace.capman.tools.ca.primitives.RsaConst;
import org.rackspace.capman.tools.ca.exceptions.CapManUtilException;
import org.rackspace.capman.tools.ca.exceptions.FileUtilsException;

public class RsaFileUtils {

    private static final int BUFFSIZE = 64 * 1024;

    static {
        RsaConst.init();
    }

    public static byte[] readFileToByteArray(String fileName) throws FileNotFoundException, IOException {
        byte[] data;
        String fmt;
        String msg;
        FileInputStream fis;
        InputStreamReader isr;
        File file;
        file = new File(fileName);
        long flen = file.length();
        if (flen > Integer.MAX_VALUE) {
            fmt = "can not read more then %d bytes\n";
            msg = String.format(fmt, Integer.MAX_VALUE);
            throw new IOException(msg);
        }
        fis = new FileInputStream(file);
        data = new byte[(int) flen];
        fis.read(data, 0, (int) flen); // FAil
        fis.close();
        return data;
    }

    public static void writeFileFromByteArray(String fileName, byte[] data) throws IOException {
        File file;
        FileOutputStream fs;
        DataOutputStream ds;
        file = new File(fileName);
        fs = new FileOutputStream(file);
        ds = new DataOutputStream(fs);
        ds.write(data);
        ds.flush();
        ds.close();
        fs.close();
    }

    public static byte[] readFile(String fileName) throws FileNotFoundException, IOException {
        File file = new File(fileName);
        byte[] bytes = readFile(file);
        return bytes;
    }

    public static byte[] readFileFromClassPath(String fileName) throws IOException {
        InputStream is = RsaFileUtils.class.getResourceAsStream(fileName);
        return readInputStream(is);
    }

    public static byte[] readInputStream(InputStream is) throws IOException {
        byte[] bytesOut;
        byte[] buff;
        int nbytes;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        while (true) {
            buff = new byte[BUFFSIZE];
            nbytes = is.read(buff);
            if (nbytes < 0) {
                break;
            }
            os.write(buff, 0, nbytes);
        }
        bytesOut = os.toByteArray();
        is.close();
        os.close();
        return bytesOut;
    }

    public static byte[] readFile(File file) throws FileNotFoundException, IOException {
        FileInputStream is = new FileInputStream(file);
        return readInputStream(is);
    }

    // for jython
    public static List<File> dirWalk(String dirFileName, String patternString) {
        File dirFile = new File(dirFileName);
        Pattern p = (patternString == null) ? null : Pattern.compile(patternString);
        return dirWalk(dirFile, p);
    }

    // Warning Beware of dirwalk on directories containing directory links
    // A link such as "ln -s .. x" will cause circular walks. 
    public static List<File> dirWalk(File dirFile, Pattern fnPattern) {
        List<File> files = new ArrayList<File>();
        if (!dirFile.canRead() || !dirFile.isDirectory()) {
            return files;// Return empty list if directory is unreadable
        }
        File[] scanFiles = dirFile.listFiles();
        for (int i = 0; i < scanFiles.length; i++) {
            File curFile = scanFiles[i];
            String fullPath = curFile.getAbsolutePath();

            if (!curFile.canRead()) {
                continue; // Don't attempt to list unreadable files
            }
            if (curFile.isDirectory()) {
                files.addAll(dirWalk(curFile, fnPattern));
            }
            if (curFile.isFile()) {
                if (fnPattern != null) {
                    Matcher m = fnPattern.matcher(fullPath);
                    if (!m.matches()) { // If this didn't match the pattern re then skip it;
                        continue;
                    }
                }
                files.add(curFile);
            }
        }
        return files;
    }

    // For Jython
    public static List<X509MapValue> readX509File(String fileName) throws FileNotFoundException, IOException {
        File file = new File(fileName);
        return readX509FileToMapVals(file);
    }

    public static List<X509MapValue> readX509FileToMapVals(File file) throws FileNotFoundException, IOException {
        List<X509MapValue> valMapList = new ArrayList<X509MapValue>();
        byte[] pemBytes = readFile(file);
        List<PemBlock> blocks = PemUtils.parseMultiPem(pemBytes);
        for (PemBlock block : blocks) {
            Object decodedObj = block.getDecodedObject();
            if (decodedObj == null) {
                continue;
            }
            if (!(decodedObj instanceof X509Certificate)) {
                continue;
            }
            X509CertificateObject x509obj = (X509CertificateObject) block.getDecodedObject();
            X509MapValue valMap = new X509MapValue(x509obj, file.getAbsolutePath(), block.getLineNum());
            valMapList.add(valMap);
        }
        return valMapList;
    }

    public static List<X509MapValue> readX509FilesToMapVals(Collection<File> files) {
        List<X509MapValue> mapVals = new ArrayList<X509MapValue>();
        for (File file : files) {
            List<X509MapValue> fileMapVals;
            try {
                fileMapVals = readX509FileToMapVals(file);
            } catch (FileNotFoundException ex) {
                String msg = String.format("File not found %s SKIPPING x509 read\n", file.getAbsolutePath());
                Logger.getLogger(RsaFileUtils.class.getName()).log(Level.WARNING, msg, ex);
                continue;
            } catch (IOException ex) {
                Logger.getLogger(RsaFileUtils.class.getName()).log(Level.WARNING, null, ex);
                continue;
            }
            mapVals.addAll(fileMapVals);
        }
        return mapVals;
    }

    public static String expandUser(String pathIn) {
        if (pathIn == null) {
            return pathIn;
        }
        return pathIn.replace("~", System.getProperty("user.home"));
    }

    public static String[] splitPath(String pathName) {
        List<String> pathList = new ArrayList<String>();
        File file = new File(pathName);
        while (true) {
            if (file == null) {
                break;
            }
            String name = file.getName();
            pathList.add(name);
            file = file.getParentFile();
        }
        Collections.reverse(pathList);
        return pathList.toArray(new String[pathList.size()]);
    }

    public static String joinPath(String firstPart, String secondPart) {
        return splitPathToString(joinPath(splitPath(firstPart), splitPath(secondPart)));
    }

    public static String[] joinPath(String[] firstPart, String[] secondPart) {
        int firstLen = firstPart.length;
        int secondLen = secondPart.length;
        int i;
        String[] newPath = new String[firstLen + secondLen];
        for (i = 0; i < firstLen; i++) {
            newPath[i] = firstPart[i];
        }
        for (i = 0; i < secondLen; i++) {
            newPath[i + firstLen] = secondPart[i];
        }
        return newPath;
    }

    public static String splitPathToString(String[] splitPath) {
        StringBuilder sb = new StringBuilder();
        if (splitPath.length == 0) {
            return null;
        }
        if (splitPath.length == 1) {
            return splitPath[0];
        }

        for (int i = 0; i < splitPath.length - 1; i++) {
            sb.append(splitPath[i]).append(File.separator);
        }
        sb.append(splitPath[splitPath.length - 1]);
        return sb.toString();
    }

    public static String rebaseSplitPath(String srcBase, String srcPath, String dstBase) throws FileUtilsException {
        String[] srcBaseArr = splitPath(srcBase);
        String[] srcPathArr = splitPath(srcPath);
        String[] dstBaseArr = splitPath(dstBase);
        String[] rebasedPath = rebaseSplitPath(srcBaseArr, srcPathArr, dstBaseArr);
        String rebasePathString = splitPathToString(rebasedPath);
        return rebasePathString;
    }

    public static String[] rebaseSplitPath(String[] srcBase, String[] srcPath, String[] dstBase) throws FileUtilsException {
        int srcBaseLen = srcBase.length;
        int srcPathLen = srcPath.length;
        int dstBaseLen = dstBase.length;
        int i;
        int j;
        if (srcPathLen < srcBaseLen) {
            throw new FileUtilsException("srcPath is smaller then srcBase");
        }
        for (i = 0; i < srcBaseLen; i++) {
            if (!srcBase[i].equals(srcPath[i])) {
                throw new FileUtilsException("srcPath does not include srcBase");
            }
        }
        int deltaLen = srcPathLen - srcBaseLen;
        int rebasedLength = dstBaseLen + deltaLen;
        String[] rebasedPath = new String[rebasedLength];
        System.arraycopy(dstBase, 0, rebasedPath, 0, dstBaseLen);
        System.arraycopy(srcPath, srcBaseLen, rebasedPath, dstBaseLen, srcPathLen - srcBaseLen);
        return rebasedPath;
    }

    public static String[] stripBeginingPath(String[] splitPath, int nTimes) {
        if (splitPath.length <= nTimes) {
            return new String[0];
        }

        int newLength = splitPath.length - nTimes;
        String[] newPath = new String[newLength];
        for (int i = 0; i < newLength; i++) {
            newPath[i] = splitPath[nTimes + i];
        }
        return newPath;
    }

    public static String[] stripEndPath(String[] splitPath, int nTimes) {
        if (splitPath.length <= nTimes) {
            return new String[0];
        }
        int newLen = splitPath.length - nTimes;
        String[] newPath = new String[newLen];
        System.arraycopy(splitPath, 0, newPath, 0, newLen);
        return newPath;
    }
}
