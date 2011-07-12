package org.openstack.atlas.constants;

import java.text.SimpleDateFormat;

public interface Constants {

    String JOBJAR_PATH = "job.jar.path";

    public interface Urchin {
        String FILE_NAME = "file.name";
        String FQDN = "fqdn";
    }

    public interface BaseMapreduceJob {

        String COPY_ALL_FILES = "allfiles";

        String COPY_SINGLE_FILE = "singlefile";

        String FORMATTED_RUNTIME = "formattedruntime";

        String INPUT_TYPE = "inputype";

        String RUNTIME = "runtime";

        String LOG_JOIN_FILE = "logjoin";

        String LOG_ORDER_FILE = "logorder";

        String BASELINE_DUMP_JOIN_FILE = "baselinedump";

        String OLDEST_LOGDATE = "oldestlogdate";

    }

    public interface CloudFiles {
        String CONTAINER_NAME = "cloudfiles.container.name";

    }

    public interface Rawlogs {

        static final SimpleDateFormat RAWLOGS_FORMAT = new SimpleDateFormat("MM-dd-yyyy hh:mm aaa");

        String INPUT_DIR = "inputdir";
        String FILEDATE = "file.date";
    }

}
