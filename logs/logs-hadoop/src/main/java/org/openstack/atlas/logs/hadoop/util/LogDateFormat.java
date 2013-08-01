package org.openstack.atlas.logs.hadoop.util;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Deprecated
public class LogDateFormat extends SimpleDateFormat {

    public LogDateFormat() {
        super("yyyyMMdd-HHmmss");
        setTimeZone(TimeZone.getDefault());
    }
}
