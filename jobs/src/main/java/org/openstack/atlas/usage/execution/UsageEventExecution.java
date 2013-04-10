package org.openstack.atlas.usage.execution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UsageEventExecution extends AbstractUsageExecution {
    private final Log LOG = LogFactory.getLog(UsageEventExecution.class);


    @Override
    public String getJobName() {
        return "USAGE_EVENT_PROCESSING";
    }

    @Override public void processUsages() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}