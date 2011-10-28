package org.openstack.atlas.jobs.batch;

import java.util.List;

public interface BatchAction<T> {
    public void execute(List<T> objects) throws Exception;
}