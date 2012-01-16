package org.openstack.atlas.rax.datamodel;

import org.openstack.atlas.datamodel.CoreAlgorithmType;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Primary
@Component
@Scope("singleton")
public class RaxAlgorithmType extends CoreAlgorithmType {
    public static final String RANDOM = "RANDOM";
    public static final String WEIGHTED_LEAST_CONNECTIONS = "WEIGHTED_LEAST_CONNECTIONS";
    public static final String WEIGHTED_ROUND_ROBIN = "WEIGHTED_ROUND_ROBIN";

    static {
        add(RANDOM);
        add(WEIGHTED_LEAST_CONNECTIONS);
        add(WEIGHTED_ROUND_ROBIN);
    }
}
