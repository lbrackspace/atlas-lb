package org.openstack.atlas.tools;

import java.io.IOException;

public interface HadoopTool {

    RUN_STATES executeHadoopRun() throws IOException;;

    HadoopConfiguration getConfiguration();

    String getInputDirectory();

    String getOutputDirectory();

    void setupHadoopRun(String inputFolder);

    void setupHadoopRun(String inputFolder, String jobJarPath);

    void setupHadoopRun(QuartzSchedulerConfigs runner);

    static enum RUN_STATES {
        FAILURE, SUCCESS
    }
}
