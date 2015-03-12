package org.openstack.atlas.logs.itest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openstack.atlas.logs.common.util.ReuploaderUtils;

public class ReuploadLidsThread extends Thread{
    ReuploaderUtils ru;
    Set<Integer> lids;
    public ReuploadLidsThread(List<Integer> lids,ReuploaderUtils ru){
        this.lids = new HashSet<Integer>(lids);
        this.ru = ru;
    }
    @Override
    public void run() {
        ru.reuploadLids(lids);
    }
}
