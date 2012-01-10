package org.openstack.atlas.datamodel;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Scope("singleton")
public class CoreAlgorithmType implements AlgorithmType {
    public static final String LEAST_CONNECTIONS = "LEAST_CONNECTIONS";
    public static final String ROUND_ROBIN = "ROUND_ROBIN";
    private static final Set<String> algorithmTypes;

    static {
        algorithmTypes = new HashSet<String>();
        algorithmTypes.add(LEAST_CONNECTIONS);
        algorithmTypes.add(ROUND_ROBIN);
    }

    public boolean contains(String str) {
        boolean out;
        out = algorithmTypes.contains(str);
        return out;
    }

    public static String[] values() {
        return algorithmTypes.toArray(new String[algorithmTypes.size()]);
    }

    @Override
    public String[] toList() {
        return algorithmTypes.toArray(new String[algorithmTypes.size()]);
    }

    protected static void add(String algorithm) {
        algorithmTypes.add(algorithm);
    }
}
