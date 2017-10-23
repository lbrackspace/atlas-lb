

package org.openstack.atlas.util.ca.primitives.bcextenders;

import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import org.bouncycastle.jce.provider.JCERSAPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDKRsaFactoryExtender {
    private static final Class<BCRSAPublicKey> rsaPubKeyClass; 
    private static final Class<KeySpec> keySpecClass; 
    
    static{
        rsaPubKeyClass = BCRSAPublicKey.class;
        keySpecClass = KeySpec.class;
    }
    
    public BCRSAPublicKey getPublicKeyFromSpec(KeySpec ks) throws InvalidKeySpecException{
        Constructor<BCRSAPublicKey> constructor = null;
        try {
            constructor = rsaPubKeyClass.getDeclaredConstructor(keySpecClass);
        } catch (NoSuchMethodException ex) {
            throw new InvalidKeySpecException("Could not get constructor for BCRSAPublicKey", ex);
        } catch (SecurityException ex) {
            throw new InvalidKeySpecException(ex);
        }
        BCRSAPublicKey pubKey = null;
        try {
            pubKey = constructor.newInstance(ks);
        } catch (InstantiationException ex) {
            throw new InvalidKeySpecException(ex);
        } catch (IllegalAccessException ex) {
            throw new InvalidKeySpecException(ex);
        } catch (IllegalArgumentException ex) {
            throw new InvalidKeySpecException(ex);
        } catch (InvocationTargetException ex) {
            throw new InvalidKeySpecException(ex);
        }
        return pubKey;
    }

}
