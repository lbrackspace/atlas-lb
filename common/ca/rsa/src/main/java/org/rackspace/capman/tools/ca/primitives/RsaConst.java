package org.rackspace.capman.tools.ca.primitives;

import java.math.BigInteger;
import java.net.URL;
import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.rackspace.capman.tools.ca.StringUtils;

public class RsaConst {

    public static final String DEFAULT_SIGNATURE_ALGO = "SHA1WITHRSA";
    public static final String USASCII = "US-ASCII";
    public static final BigInteger E = new BigInteger("65537");
    public static final int PAGESIZE = 4096;
    public static final int DEFAULT_PRIME_CERTAINTY = 32;
    public static final Map<String, String> oids;
    private static int initCount = 0;

    static {
        Provider bc = new BouncyCastleProvider();
        Security.addProvider(bc);
        // Cause the bouncycastle PKCS10CertificationRequest class was being
        // stingy in letting me access the oid maps.

        oids = new HashMap<String, String>();
        oids.put("1.2.840.10040.4.3", "SHA1WITHDSA");
        oids.put("1.2.840.10045.4.1", "SHA1WITHECDSA");
        oids.put("1.2.840.10045.4.3.1", "SHA224WITHECDSA");
        oids.put("1.2.840.10045.4.3.2", "SHA256WITHECDSA");
        oids.put("1.2.840.10045.4.3.3", "SHA384WITHECDSA");
        oids.put("1.2.840.10045.4.3.4", "SHA512WITHECDSA");
        oids.put("1.2.840.113549.1.1.10", "RSASSA-PSS");
        oids.put("1.2.840.113549.1.1.11", "SHA256WITHRSA");
        oids.put("1.2.840.113549.1.1.12", "SHA384WITHRSA");
        oids.put("1.2.840.113549.1.1.13", "SHA512WITHRSA");
        oids.put("1.2.840.113549.1.1.14", "SHA224WITHRSA");
        oids.put("1.2.840.113549.1.1.2", "RSAWITHMD2");
        oids.put("1.2.840.113549.1.1.4", "MD5WITHRSA");
        oids.put("1.2.840.113549.1.1.5", "SHA1WITHRSA");
        oids.put("1.3.14.3.2.27", "SHA1WITHDSA");
        oids.put("1.3.14.3.2.27", "SHA1WITHDSA");
        oids.put("1.3.14.3.2.29", "SHA1WITHRSA");
        oids.put("1.3.36.3.3.1.2", "RIPEMD160WITHRSA");
        oids.put("1.3.36.3.3.1.3", "RIPEMD128WITHRSA");
        oids.put("1.3.36.3.3.1.4", "RIPEMD256WITHRSA");
        oids.put("2.16.840.1.101.3.4.3.1", "SHA224WITHDSA");
        oids.put("2.16.840.1.101.3.4.3.2", "SHA256WITHDSA");
        oids.put("2.16.840.1.101.3.4.3.3", "SHA384WITHDSA");
        oids.put("2.16.840.1.101.3.4.3.4", "SHA512WITHDSA");
    }

    // Static initializers aren't executed intill a method of the class is
    // accessed for the first time.
    public static void init() {
        initCount++;
    }

    public static int initCount() {
        return initCount;
    }
}
