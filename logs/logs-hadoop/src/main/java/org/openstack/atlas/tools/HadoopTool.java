package org.openstack.atlas.tools;

import java.io.IOException;

public interface HadoopTool {

    RUN_STATES executeHadoopRun() throws IOException;;

    HadoopConfiguration getConfiguration();

    String getInputDirectory();

    String getOutputDirectory();

    @Deprecated
    void setupHadoopRun(String inputFolder);

    @Deprecated
    void setupHadoopRun(String inputFolder, String jobJarPath);

    void setupHadoopRun(HadoopRunner runner);

    static enum RUN_STATES {
        FAILURE, SUCCESS
    }
}
