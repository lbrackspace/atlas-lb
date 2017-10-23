package org.openstack.atlas.util.ca;

import org.openstack.atlas.util.ca.primitives.Debug;
import org.openstack.atlas.util.ca.primitives.RsaConst;
import org.openstack.atlas.util.ca.primitives.PemBlock;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import java.util.ArrayList;
import java.util.List;
import java.security.KeyPair;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateCrtKey;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.openstack.atlas.util.ca.exceptions.NotAPemObject;
import org.openstack.atlas.util.ca.primitives.ByteLineReader;
import static org.openstack.atlas.util.ca.primitives.ByteLineReader.cmpBytes;

public class PemUtils {

    public JcaPEMWriter ass;
    public static final byte[] BEG_PRV;
    public static final byte[] END_PRV;
    public static final byte[] BEG_CSR;
    public static final byte[] END_CSR;
    public static final byte[] BEG_CRT;
    public static final byte[] END_CRT;
    public static final byte[] BEG_RSA;
    public static final byte[] END_RSA;
    public static final byte[][] BEG_LINES;
    public static final byte[][] END_LINES;
    private static final int CR = 13;
    private static final int LF = 10;
    private static final int PAGESIZE = 4096;

    static {
        RsaConst.init();

        BEG_PRV = StringUtils.asciiBytes("-----BEGIN RSA PRIVATE KEY-----");
        END_PRV = StringUtils.asciiBytes("-----END RSA PRIVATE KEY-----");
        BEG_CSR = StringUtils.asciiBytes("-----BEGIN CERTIFICATE REQUEST-----");
        END_CSR = StringUtils.asciiBytes("-----END CERTIFICATE REQUEST-----");
        BEG_CRT = StringUtils.asciiBytes("-----BEGIN CERTIFICATE-----");
        END_CRT = StringUtils.asciiBytes("-----END CERTIFICATE-----");
        BEG_RSA = StringUtils.asciiBytes("-----BEGIN PRIVATE KEY-----");
        END_RSA = StringUtils.asciiBytes("-----END PRIVATE KEY-----");

        BEG_LINES = new byte[][]{BEG_PRV, BEG_CSR, BEG_CRT, BEG_RSA};
        END_LINES = new byte[][]{END_PRV, END_CSR, END_CRT, END_RSA};
    }

    public static byte[] toPemBytes(Object obj) throws PemException {
        byte[] out;
        JcaPEMWriter pw;
        OutputStreamWriter osw;
        ByteArrayOutputStream bas;
        bas = new ByteArrayOutputStream(PAGESIZE);
        osw = new OutputStreamWriter(bas);
        pw = new JcaPEMWriter(osw);
        try {
            pw.writeObject(obj);
            pw.flush();
            pw.close();
        } catch (IOException ex) {
            String msg = String.format("Error encoding object %s to PEM",
                    obj.getClass().getCanonicalName());
            throw new PemException(msg);
        }
        out = bas.toByteArray();
        try {
            pw.close();
            osw.close();
            bas.close();
        } catch (IOException ex) {
            // Like really, Can't close with out an exceptuon
        }
        return out;
    }

    public static String toPemString(Object obj) throws PemException {
        byte[] pemBytes;
        String out;
        pemBytes = toPemBytes(obj);
        try {
            out = new String(pemBytes, RsaConst.USASCII);
        } catch (UnsupportedEncodingException ex) {
            throw new PemException("Could not encode pem to us-ascii", ex);
        }
        return out;
    }

    public static Object fromPemBytes(byte[] pemBytes) throws PemException {
        Object obj;
        ByteArrayInputStream bis;
        InputStreamReader isr;
        PEMParser pr;
        if (pemBytes == null) {
            throw new NotAPemObject("pem data was null could not decode");
        }
        bis = new ByteArrayInputStream(pemBytes);
        isr = new InputStreamReader(bis);
        pr = new PEMParser(isr);
        try {
            obj = pr.readObject();
            pr.close();
            isr.close();
            bis.close();
            if (obj == null) {
                String msg = "Returned obj instance was null in call to fromPemBytes(byte[] pemBytes)";
                throw new NotAPemObject(msg);
            }
        } catch (IOException ex) {
            throw new PemException("could not read PEM data", ex);
        }
        if (obj instanceof PEMKeyPair) {
            PEMKeyPair pkp = (PEMKeyPair) obj;
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(RsaConst.BC);
            KeyPair kp;
            try {
                kp = converter.getKeyPair(pkp);
                return kp;
            } catch (PEMException ex) {
                String fmt = "Could not convert %s to KeyPair";
                String msg = String.format(fmt, Debug.findClassPath(obj.getClass()));
                throw new PemException(msg, ex);
            }
        } else if (obj instanceof PrivateKeyInfo) {

            try {
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(RsaConst.BC);
                PrivateKeyInfo privKeyInfo = (PrivateKeyInfo) obj;
                BCRSAPrivateCrtKey privKey = (BCRSAPrivateCrtKey) converter.getPrivateKey(privKeyInfo);
                byte[] privKeyBytes = PemUtils.toPemBytes(privKey);
                KeyPair kp = (KeyPair) PemUtils.fromPemBytes(privKeyBytes);
                return kp;
            } catch (IOException ex) {
                String fmt = "Could not convert %s to %s";
                String msg = String.format(fmt,
                        Debug.findClassPath(obj.getClass()),
                        Debug.findClassPath(KeyPair.class));
            }
        }
        return obj; // Not sure what to convert this object to so just return it
    }

    public static Object fromPemString(String pemStr) throws PemException {
        byte[] pemBytes;
        Object obj;
        if (pemStr == null) {
            throw new NotAPemObject("String parameter in call to PemUtils.fromPemString(String pem) was null");
        }
        try {
            pemBytes = pemStr.getBytes(RsaConst.USASCII);
            obj = fromPemBytes(pemBytes);
            return obj;
        } catch (UnsupportedEncodingException ex) {
            throw new PemException("Error decoding PEM", ex);
        }
    }

    public static String toMultiPemString(List<? extends Object> objList) throws PemException {
        byte[] bytes = toMultiPem(objList);
        return StringUtils.asciiString(bytes);
    }

    public static byte[] toMultiPem(List<? extends Object> objList) throws PemException {
        ByteArrayOutputStream bas = new ByteArrayOutputStream(PAGESIZE);
        for (int i = 0; i < objList.size(); i++) {
            Object obj = objList.get(i);
            byte[] pemBytes;
            if (obj instanceof PemBlock) {
                obj = ((PemBlock) obj).getDecodedObject();
            }
            if (obj == null) {
                continue;
            }
            try {
                pemBytes = toPemBytes(obj);
            } catch (PemException ex) {
                continue;
            }
            try {
                bas.write(pemBytes);
            } catch (IOException ex) {
                throw new PemException("Error writing pemBytes to byte array", ex);
            }
        }
        return bas.toByteArray();
    }

    public static List<PemBlock> parseMultiPem(String multiPemString) {
        byte[] multiPemBytes;
        multiPemBytes = StringUtils.asciiBytes(multiPemString);
        return parseMultiPem(multiPemBytes);
    }

    public static List<PemBlock> parseMultiPem(byte[] multiPemBytes) {
        List<PemBlock> pemBlocks = new ArrayList<PemBlock>();
        ByteLineReader br = new ByteLineReader(multiPemBytes);
        boolean outsideBlock = true;
        int lc = 0;
        int currBytePos;
        ByteArrayOutputStream bos;
        PemBlock pemBlock = null;
        bos = null;
        Object decodedObject = null;
        while (br.bytesAvailable() > 0) {
            currBytePos = br.getBytesRead();
            byte[] line = br.readLine(true);
            line = ByteLineReader.trim(line); // Incase some one whitespaces their crt :(
            lc++;
            if (isEmptyLine(line)) {
                continue;
            }

            if (outsideBlock) {
                if (isBegPemBlock(line)) {
                    bos = new ByteArrayOutputStream(PAGESIZE);
                    pemBlock = new PemBlock();
                    pemBlock.setStartLine(StringUtils.asciiString(line));
                    pemBlock.setLineNum(lc);
                    pemBlock.setDecodedObject(null);
                    pemBlock.setPemData(null);
                    pemBlock.setStartByte(currBytePos);
                    writeLine(bos, line);
                    outsideBlock = !outsideBlock;
                    continue;
                } else {
                    continue; // We are still outside the a block so skip this line
                }
            } else {
                // We are inside a pemBlock
                if (isEndPemBlock(line)) {
                    outsideBlock = !outsideBlock;
                    writeLine(bos, line);
                    byte[] bytes = bos.toByteArray();
                    pemBlock.setPemData(bytes);
                    pemBlock.setEndLine(StringUtils.asciiString(line));
                    try {
                        decodedObject = PemUtils.fromPemBytes(bytes);
                    } catch (PemException ex) {
                        decodedObject = null;
                    }
                    pemBlock.setDecodedObject(decodedObject);
                    currBytePos = br.getBytesRead();
                    pemBlock.setStopByte(currBytePos);
                    pemBlocks.add(pemBlock);
                } else {
                    writeLine(bos, line);
                }
            }
        }
        return pemBlocks;
    }

    public static List<Object> getBlockObjects(List<PemBlock> blocks) {
        List<Object> out = new ArrayList<Object>();
        for (PemBlock block : blocks) {
            Object obj = block.getDecodedObject();
            if (obj == null) {
                continue;
            }
            out.add(obj);
        }
        return out;
    }

    public static void writeLine(ByteArrayOutputStream bos, byte[] line) {
        for (int i = 0; i < line.length; i++) {
            int byteInt = (line[i] >= 0) ? (int) line[i] : (int) line[i] + 256;
            bos.write(byteInt); // Not sure why single byte writes are Exception free. Its annoying.
        }
        // attach LF
        bos.write(LF);

    }

    public static boolean isBegPemBlock(byte[] line) {
        for (int i = 0; i < BEG_LINES.length; i++) {
            if (cmpBytes(line, BEG_LINES[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEndPemBlock(byte[] line) {
        for (int i = 0; i < END_LINES.length; i++) {
            if (cmpBytes(line, END_LINES[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmptyLine(byte[] line) {
        if (line.length <= 0) {
            return true;
        } else {
            return false;
        }
    }
}
