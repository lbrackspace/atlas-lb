package org.openstack.atlas.usage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExecutionUtilities {
    private static final Log LOG = LogFactory.getLog(ExecutionUtilities.class);


    public <T> void executeInBatchesInstance(Collection<T> objects, int batchSize, BatchAction<T> batchAction) throws Exception {
        executeInBatches(objects, batchSize, batchAction);
    }

    @Deprecated
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

    public static <T> void ExecuteInBatches(List<T> objects, int batchSize, BatchAction<T> batchAction) throws Exception {
        int batchNumber = 1;
        ArrayList<T> objectBatch = new ArrayList<T>();
        for (int i = 0; i < objects.size(); i++) {
            objectBatch.add(objects.get(i));
            if ((i + 1) % batchSize == 0) {
                logBatchNumber(objects, batchAction, batchNumber);
                batchNumber++;
                batchAction.execute(objectBatch);
                objectBatch = new ArrayList<T>();
            }
        }
        if (objectBatch.size() > 0) {
            logBatchNumber(objects, batchAction, batchNumber);
            batchAction.execute(objectBatch);
        }
    }

    private static <T> void logBatchNumber(List<T> objects, BatchAction<T> batchAction, int batchNumber) {
        LOG.debug(String.format("Executing batch # '%d' %s, (total number of items = %d)", batchNumber, batchAction.getClass().getSimpleName(), objects.size()));
    }
}