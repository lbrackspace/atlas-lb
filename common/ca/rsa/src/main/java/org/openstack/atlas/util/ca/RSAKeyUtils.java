package org.openstack.atlas.util.ca;

import java.io.File;
import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.asn1.pkcs.RSAPrivateKeyStructure;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.util.encoders.Base64;
import java.math.BigInteger;
import org.rackexp.ca.primitives.RsaPair;
import org.openstack.atlas.util.ca.exceptions.NoSuchAlgorithmException;

public class RSAKeyUtils {

    private static final int SB_INIT_CAPACITY = 4096;

    public static RsaPair genRSAPair(int bits, int certainity) throws NoSuchAlgorithmException {
        return RsaPair.getInstance(bits, certainity);
    }

}
