package org.openstack.atlas.rax.datamodel;

import org.openstack.atlas.datamodel.CoreNodeCondition;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Primary
@Component
@Scope("request")
public class RaxNodeCondition extends CoreNodeCondition {
    public static final String DRAINING = "DRAINING";

    static {
        CoreNodeCondition.add(DRAINING);
    }
}
