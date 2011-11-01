package org.openstack.atlas.jobs.batch;

import java.util.ArrayList;
import java.util.Collection;

public class BatchExecutor {

    public <T> void executeInBatchesInstance(Collection<T> objects, int batchSize, BatchAction<T> batchAction) throws Exception {
        executeInBatches(objects, batchSize, batchAction);
    }

    public static <T> void executeInBatches(Collection<T> objects, int batchSize, BatchAction<T> batchAction) throws Exception {
        ArrayList<T> objectBatch = new ArrayList<T>();
        int i = 0;
        for (T object : objects) {
            objectBatch.add(object);
            if ((i + 1) % batchSize == 0) {
                batchAction.execute(objectBatch);
                objectBatch = new ArrayList<T>();
            }
            i++;
        }
        if (objectBatch.size() > 0) {
            batchAction.execute(objectBatch);
        }
    }
}