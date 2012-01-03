package org.openstack.atlas.util.ca;

import org.rackexp.ca.primitives.PemBlock;
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
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.openstack.atlas.util.ca.exceptions.PemException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.nio.charset.Charset;
import org.rackexp.ca.primitives.ByteLineList;
import org.rackexp.ca.primitives.ByteLineListEntry;
import org.rackexp.ca.primitives.ByteLineReader;
import static org.rackexp.ca.primitives.ByteLineReader.cmpBytes;
import static org.rackexp.ca.primitives.ByteLineReader.appendLF;

public class PemUtils {

    private static final int PAGESIZE = 4096;
    private static final byte[] BEG_PRV;
    private static final byte[] END_PRV;
    private static final byte[] BEG_CSR;
    private static final byte[] END_CSR;
    private static final byte[] BEG_CRT;
    private static final byte[] END_CRT;

    static {
        BEG_PRV = StringUtils.asciiBytes("-----BEGIN RSA PRIVATE KEY-----");
        END_PRV = StringUtils.asciiBytes("-----END RSA PRIVATE KEY-----");
        BEG_CSR = StringUtils.asciiBytes("-----BEGIN CERTIFICATE REQUEST-----");
        END_CSR = StringUtils.asciiBytes("-----END CERTIFICATE REQUEST-----");
        BEG_CRT = StringUtils.asciiBytes("-----BEGIN CERTIFICATE-----");
        END_CRT = StringUtils.asciiBytes("-----END CERTIFICATE-----");
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
        fis.read(data, 0, (int) flen);
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
    }

    public static Object fromPem(byte[] pem) throws PemException {
        Object out = null;
        ByteArrayInputStream bas;
        InputStreamReader isr;
        PEMReader pr;

        bas = new ByteArrayInputStream(pem);
        isr = new InputStreamReader(bas);
        pr = new PEMReader(isr);
        try {
            out = pr.readObject();
            pr.close();
        } catch (IOException ex) {
            throw new PemException("Could not read data to PEM object", ex);
        }
        return out;
    }

    public static byte[] toPem(Object obj) throws PemException {
        byte[] out;
        ByteArrayOutputStream bas;
        OutputStreamWriter osw;
        PEMWriter pw;
        bas = new ByteArrayOutputStream(PAGESIZE);
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

    public static List<PemBlock> parseMultiPem(byte[] multiPemBytes) {
        List<PemBlock> pemBlocks = new ArrayList<PemBlock>();
        PemBlock pemBlock;
        ByteLineList lines;
        ByteLineReader br;
        int lc;
        byte[] line;
        byte[] pemBlockBytes;
        boolean outsidePemBlock;
        Object decodedObject;
        outsidePemBlock = true;
        lc = 1;
        br = new ByteLineReader(multiPemBytes);
        lines = new ByteLineList();
        pemBlock = new PemBlock();
        pemBlock.setLineNum(lc);
        while (br.bytesAvailable() > 0) {
            line = br.readLine(true);
            lc++;
            if(outsidePemBlock && isBegPemBlock(line)){
                if(!lines.empty()) {
                    pemBlockBytes = lines.toBytes(); // This must be garbage data
                    pemBlock.setPemData(pemBlockBytes);
                    pemBlock.setDecodedObject(null);
                    lines = new ByteLineList();
                    pemBlocks.add(pemBlock);
                    pemBlock = new PemBlock();
                    pemBlock.setLineNum(lc);
                }
                outsidePemBlock = false; // toggle state to inside
                lines.addLine(appendLF(line));
            }else if(!outsidePemBlock && isEndPemBlock(line)){
                lines.addLine(appendLF(line));
                pemBlockBytes = lines.toBytes();
                lines = new ByteLineList();
                pemBlock.setPemData(pemBlockBytes);
                try {
                    decodedObject = PemUtils.fromPem(pemBlockBytes);
                } catch (PemException ex) {
                    decodedObject = null;
                }
                pemBlock.setDecodedObject(decodedObject);
                pemBlocks.add(pemBlock);
                pemBlock = new PemBlock();
                pemBlock.setLineNum(lc);
                outsidePemBlock = true;
            }else{
                lines.addLine(appendLF(line));
            }
        }
        return pemBlocks;
    }

    public static boolean isBegPemBlock(byte[] line) {
        if (cmpBytes(line, BEG_PRV)) {
            return true;
        }
        if (cmpBytes(line, BEG_CSR)) {
            return true;
        }
        if (cmpBytes(line, BEG_CRT)) {
            return true;
        }
        return false;
    }

    public static boolean isEndPemBlock(byte[] line) {
        if (cmpBytes(line, END_PRV)) {
            return true;
        }
        if (cmpBytes(line, END_CSR)) {
            return true;
        }
        if (cmpBytes(line, END_CRT)) {
            return true;
        }
        return false;
    }
}
