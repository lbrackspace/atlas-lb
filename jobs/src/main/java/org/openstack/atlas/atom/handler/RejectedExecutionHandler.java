package org.openstack.atlas.atom.handler;

import org.apache.log4j.Logger;

import java.util.concurrent.ThreadPoolExecutor;

public class RejectedExecutionHandler implements java.util.concurrent.RejectedExecutionHandler {
 
    private static Logger log = Logger.getLogger(RejectedExecutionHandler.class);

    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
        log.debug(runnable.toString() + " : has been rejected");
    }
}