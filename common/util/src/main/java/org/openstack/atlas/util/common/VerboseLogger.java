
package org.openstack.atlas.util.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VerboseLogger {
    private Log LOG;
    private LogLevel level;
    private static final LogLevel defaultLogLevel = LogLevel.INFO; // Change this to what ever log is allowed to be noisy.
    public static enum LogLevel {

        INFO, WARN, DEBUG, ERROR, FATAL, NULL
    };

    public VerboseLogger(Class aClass){
        this.LOG = LogFactory.getLog(aClass);
        this.level = defaultLogLevel;
    }

    public VerboseLogger(Class aClass,LogLevel level){
        this.LOG = LogFactory.getLog(aClass);
        this.level = level;
    }


    public void log(Object obj) {
        switch (level) {
            case DEBUG:
                LOG.debug(obj);
                break;
            case ERROR:
                LOG.error(obj);
                break;
            case FATAL:
                LOG.fatal(obj);
                break;
            case INFO:
                LOG.info(obj);
                break;
            case WARN:
                LOG.warn(obj);
                break;
            case NULL:
            default:
                break;
        }
    }

    public void log(Object obj, Throwable th) {
        switch (level) {
            case DEBUG:
                LOG.debug(obj, th);
                break;
            case ERROR:
                LOG.error(obj, th);
                break;
            case FATAL:
                LOG.fatal(obj, th);
                break;
            case INFO:
                LOG.info(obj, th);
                break;
            case WARN:
                LOG.warn(obj, th);
                break;
            case NULL:
            default:
                break;
        }
    }

    public Log getLOG() {
        return LOG;
    }

    public void setLOG(Log LOG) {
        this.LOG = LOG;
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public void printf(String  fmt,Object... args){
        String logLine = String.format(fmt,args);
        log(logLine);
    }
}
