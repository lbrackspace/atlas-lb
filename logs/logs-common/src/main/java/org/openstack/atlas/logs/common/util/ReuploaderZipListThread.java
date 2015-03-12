package org.openstack.atlas.logs.common.util;

import java.util.List;

public class ReuploaderZipListThread extends Thread {

    private List<CacheZipInfo> zipsList;
    private ReuploaderUtils ru;

    public ReuploaderZipListThread(List<CacheZipInfo> zipsList, ReuploaderUtils ru) {
        this.zipsList = zipsList;
        this.ru = ru;
    }

    @Override
    public void run(){
        ru.reuploadFiles(zipsList);
    }
}
