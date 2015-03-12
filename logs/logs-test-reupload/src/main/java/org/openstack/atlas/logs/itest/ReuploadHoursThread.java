package org.openstack.atlas.logs.itest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openstack.atlas.logs.common.util.ReuploaderUtils;

public class ReuploadHoursThread extends Thread {

    public ReuploadHoursThread(List<Long> hours, ReuploaderUtils ru) {
        this.ru = ru;
        this.hours = new HashSet<Long>(hours);
    }
    ReuploaderUtils ru;
    Set<Long> hours;

    @Override
    public void run() {
        ru.reuploadHours(hours);
    }
}
