package org.openstack.atlas.rax.datamodel;

import org.openstack.atlas.datamodel.CoreUsageEventType;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Primary
@Component
@Scope("singleton")
public class RaxUsageEventType extends CoreUsageEventType {
    public static final String ADD_VIRTUAL_IP = "ADD_VIRTUAL_IP";
    public static final String REMOVE_VIRTUAL_IP = "REMOVE_VIRTUAL_IP";

    static {
        add(ADD_VIRTUAL_IP);
        add(REMOVE_VIRTUAL_IP);
    }
}
