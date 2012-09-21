package org.openstack.atlas;

import org.junit.Test;
import org.openstack.atlas.usage.BatchAction;
import org.openstack.atlas.usage.ExecutionUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExecutionUtilitiesTest {
    
    @Test
    public void testExecuteInBatchesWithNoRemainder() throws Exception {
        final List<Object> objects = createDummyArrayList(26);
        final int batchSize = 2;

        final List batchCountList = runExecuteInBatches(objects, batchSize);
        assertEquals(13, batchCountList.size());
    }

    @Test
    public void testExecuteInBatchesWithRemainder() throws Exception {
        final List<Object> objects = createDummyArrayList(251);
        final int batchSize = 20;

        List batchCountList = runExecuteInBatches(objects, batchSize);
        assertEquals(13, batchCountList.size());
    }

    @Test
    public void testExecuteInBatchesWhereListSizeIsSmallerThanBatchSize() throws Exception {
        final List<Object> objects = createDummyArrayList(1);
        final int batchSize = 2;
        List batchCountList = runExecuteInBatches(objects, batchSize);
        assertEquals(1, batchCountList.size());
    }

    @Test
    public void testExecuteInBatchesWithNoEntriesInBatch() throws Exception {
        final List<Object> objects = new ArrayList<Object>();
        final int batchSize = 2;
        List batchCountList = runExecuteInBatches(objects, batchSize);
        assertEquals(0, batchCountList.size());
    }

    private <T> List<String> runExecuteInBatches(final List<T> objects, final int batchSize) throws Exception {
        final List<String> batchCountList = new ArrayList<String>();
        BatchAction<T> batchAction = new BatchAction<T>() {
            public void execute(Collection<T> objects) throws Exception {
                batchCountList.add("");
            }
        };
        ExecutionUtilities.executeInBatches(objects, batchSize, batchAction);
        return batchCountList;
    }

    private List<Object> createDummyArrayList(int listCount) {
        ArrayList<Object> list = new ArrayList<Object>();

        for (int i = 0; i < listCount; i++) {
            list.add(new Object());
        }

        return list;
    }
}
