package org.openstack.atlas.data;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class LogDateFormat extends SimpleDateFormat {

    public LogDateFormat() {
        super("yyyyMMdd-HHmmss");
        setTimeZone(TimeZone.getDefault());
    }
}
