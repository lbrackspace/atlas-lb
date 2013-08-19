package org.openstack.atlas.logs.common.util;

public class ReuploaderThread extends Thread {

    ReuploaderUtils ru;

    public ReuploaderThread(ReuploaderUtils ru) {
        this.ru = ru;
    }

    @Override
    public void run() {
        ru.reuploadFiles();
    }
}
