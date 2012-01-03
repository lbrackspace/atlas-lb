package org.bouncycastle.jce.provider;

/* The JCERSA constructors are Package private so I had to add this class as
 * a way to access those very useful constructors.
 */
import java.math.BigInteger;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;

// Alot of really usefull methods are locked in the provider package
// So I'm forced to place an Accessor class right here in the provider package since
// a lot of usefull methods are locked provider only. Mechanism over policy forces
// ugly Hacks.
public class HackedProviderAccessor {
    public static JCERSAPrivateCrtKey newJCERSAPrivateCrtKey(RSAPrivateCrtKeyParameters priv){
        return new JCERSAPrivateCrtKey(priv);
    }

    public static JCERSAPublicKey newJCERSAPublicKey(RSAKeyParameters pub){
        return new JCERSAPublicKey(pub);
    }
    
    public static RSAPrivateCrtKeyParameters newRSAPrivateCrtKeyParameters(JCERSAPrivateCrtKey jPriv){
        RSAPrivateCrtKeyParameters priv=null;
        BigInteger n = jPriv.getModulus();
        BigInteger e = jPriv.getPublicExponent();
        BigInteger d = jPriv.getPrivateExponent();
        BigInteger p = jPriv.getPrimeP();
        BigInteger q = jPriv.getPrimeQ();
        BigInteger dP = jPriv.getPrimeExponentP();
        BigInteger dQ = jPriv.getPrimeExponentQ();
        BigInteger qInv = jPriv.getCrtCoefficient();
        priv = new RSAPrivateCrtKeyParameters(n,e,d,p,q,dP,dQ,qInv);
        return priv;
    }

    public static RSAKeyParameters newRSAKeyParameters(JCERSAPublicKey jPub){
        RSAKeyParameters pub;

        BigInteger n = jPub.getModulus();
        BigInteger e = jPub.getPublicExponent();
        boolean isPrivate = false;

        pub = new RSAKeyParameters(isPrivate,n,e);
        return pub;
    }

}
