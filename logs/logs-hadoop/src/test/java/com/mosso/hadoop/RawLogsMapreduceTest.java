package com.mosso.hadoop;

import org.apache.hadoop.mapred.HadoopTestCase;
import org.junit.Test;

import java.io.IOException;

public class RawLogsMapreduceTest extends HadoopTestCase {

    private static final int DATANODES = 1;

    private static final int FSMODE = HadoopTestCase.LOCAL_FS;

    private static final int MRMODE = HadoopTestCase.LOCAL_MR;

    private static final int TASKTRACKERS = 1;

    public RawLogsMapreduceTest() throws IOException {
        super(MRMODE, FSMODE, TASKTRACKERS, DATANODES);
    }

    @Test
    public void testPlaceholder() {

    }
}
