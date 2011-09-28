package org.openstack.atlas.datamodel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class AtlasTypeHelper {
    private static AlgorithmType algorithmType;
    private static ProtocolType protocolType;

    @Autowired(required = true)
    public void setAlgorithmType(AlgorithmType algorithmType) {
        AtlasTypeHelper.algorithmType = algorithmType;
    }

    @Autowired(required = true)
    public void setProtocolType(ProtocolType protocolType) {
        AtlasTypeHelper.protocolType = protocolType;
    }

    public static boolean isValidAlgorithm(String algorithm) {
        return isValidAtlasType(algorithm, algorithmType);
    }

    public static boolean isValidProtocol(String protocol) {
        return isValidAtlasType(protocol, protocolType);
    }

    private static boolean isValidAtlasType(String string, AtlasType atlasType) {
        boolean isValidString = false;
        for (int i = 0; i < atlasType.toList().length; i++) {
            if (atlasType.toList()[i].equals(string)) {
                isValidString = true;
                break;
            }
        }

        return isValidString;
    }
}
