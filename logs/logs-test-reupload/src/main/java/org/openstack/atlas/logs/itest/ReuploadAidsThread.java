package org.openstack.atlas.logs.itest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openstack.atlas.logs.common.util.ReuploaderUtils;

public class ReuploadAidsThread extends Thread {

    ReuploaderUtils ru;
    Set<Integer> aids;

    public ReuploadAidsThread(List<Integer> aids, ReuploaderUtils ru) {
        this.aids = new HashSet<Integer>(aids);
        this.ru = ru;
    }

    @Override
    public void run() {
        ru.reuploadAids(aids);
    }
}
