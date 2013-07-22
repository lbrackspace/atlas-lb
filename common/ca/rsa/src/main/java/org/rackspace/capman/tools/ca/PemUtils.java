package org.rackspace.capman.tools.ca;

import java.util.Set;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.rackspace.capman.tools.ca.primitives.RsaConst;
import org.rackspace.capman.tools.ca.primitives.PemBlock;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rackspace.capman.tools.ca.exceptions.PemException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.Charset;
import org.rackspace.capman.tools.ca.exceptions.NotAPemObject;
import org.rackspace.capman.tools.ca.primitives.ByteLineReader;
import static org.rackspace.capman.tools.ca.primitives.ByteLineReader.cmpBytes;
import static org.rackspace.capman.tools.ca.primitives.ByteLineReader.appendLF;

public class PemUtils {

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

        BEG_LINES = new byte[][]{BEG_PRV, BEG_CSR,BEG_CRT, BEG_RSA};
        END_LINES = new byte[][]{END_PRV, END_CSR,END_CRT, END_RSA};
    }

    public static Object fromPemString(String pem) throws PemException {
        if (pem == null) {
            throw new NotAPemObject("String parameter in call to PemUtils.fromPemString(String pem) was null");
        }
        try {
            byte[] pemBytes = pem.getBytes(RsaConst.USASCII);
            return fromPem(pemBytes);
        } catch (UnsupportedEncodingException ex) {
            throw new PemException("Error decodeing PEM", ex);
        }
    }

    public static Object fromPem(byte[] pem) throws PemException {
        Object out;
        ByteArrayInputStream bas;
        InputStreamReader isr;
        PEMReader pr;

        if (pem == null) {
            throw new NotAPemObject("byte[] parameter pem in call to PemUtils.fromPem(byte[] pem) was null");
        }

        bas = new ByteArrayInputStream(pem);
        isr = new InputStreamReader(bas);
        pr = new PEMReader(isr);
        try {
            out = pr.readObject();
            pr.close();
            isr.close();
            bas.close();
        } catch (IOException ex) {
            throw new PemException("Could not read PEM data", ex);
        }
        if (out == null) {
            throw new NotAPemObject("Returned obj instance was null in call to Object obj = Object fromPem(bytes[] pem)");
        }
        return out;
    }

    public static String toPemString(Object obj) throws PemException {
        byte[] pemBytes = toPem(obj);
        String out;
        try {
            out = new String(pemBytes, RsaConst.USASCII);
        } catch (UnsupportedEncodingException ex) {
            throw new PemException("Could not encode Object to PEM", ex);
        }
        return out;

    }

    public static byte[] toPem(Object obj) throws PemException {
        byte[] out;
        ByteArrayOutputStream bas;
        OutputStreamWriter osw;
        PEMWriter pw;
        bas = new ByteArrayOutputStream(RsaConst.PAGESIZE);
        osw = new OutputStreamWriter(bas);
        pw = new PEMWriter(osw);
        try {
            pw.writeObject(obj);
            pw.flush();
            pw.close();
        } catch (IOException ex) {
            throw new PemException("Error encoding object to PEM", ex);
        }
        out = bas.toByteArray();
        return out;
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
                pemBytes = toPem(obj);
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
                        decodedObject = PemUtils.fromPem(bytes);
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
        for(int i=0;i<BEG_LINES.length;i++){
            if(cmpBytes(line,BEG_LINES[i])){
                return true;
            }
        }
        return false;
    }

    public static boolean isEndPemBlock(byte[] line) {
        for(int i=0;i<END_LINES.length;i++){
            if(cmpBytes(line,END_LINES[i])){
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
