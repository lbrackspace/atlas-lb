package org.openstack.atlas.rax.domain.operation;

import org.openstack.atlas.service.domain.operation.CoreOperation;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Primary
@Component
@Scope("request")
public class RaxOperation extends CoreOperation {
    public static final String RAX_ADD_VIRTUAL_IP = "RAX_ADD_VIRTUAL_IP";
    public static final String RAX_REMOVE_VIRTUAL_IPS = "RAX_REMOVE_VIRTUAL_IPS";
    public static final String UPDATE_CONNECTION_LOGGING = "UPDATE_CONNECTION_LOGGING";
    public static final String UPDATE_ACCESS_LIST = "UPDATE_ACCESS_LIST";

    static {
        add(RAX_ADD_VIRTUAL_IP);
        add(RAX_REMOVE_VIRTUAL_IPS);
        add(UPDATE_CONNECTION_LOGGING);
        add(UPDATE_ACCESS_LIST);
    }
}
