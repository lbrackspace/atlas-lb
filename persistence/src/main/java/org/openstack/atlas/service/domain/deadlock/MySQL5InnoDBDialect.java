package org.openstack.atlas.service.domain.deadlock;

import org.hibernate.dialect.Dialect;

import java.util.HashSet;
import java.util.Set;

public class MySQL5InnoDBDialect extends Dialect implements ErrorCodeAware {

    private final int ER_LOCK_DEADLOCK = 1213;

    @Override
    public Set<Integer> getDeadlockErrorCodes() {
        Set<Integer> deadlockErrorCodes = new HashSet<Integer>();

        deadlockErrorCodes.add(ER_LOCK_DEADLOCK);

        return deadlockErrorCodes;
    }
}
