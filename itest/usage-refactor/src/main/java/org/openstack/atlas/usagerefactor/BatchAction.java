package org.openstack.atlas.usagerefactor;

import java.util.Collection;

public interface BatchAction<T> {
    public void execute(Collection<T> objects) throws Exception;
}