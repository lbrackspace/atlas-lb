package org.openstack.atlas.api.integration.threads;

import java.util.concurrent.*;

public class ThreadExecutorService {
    private static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();

    public static <T> T call(Callable<T> c, long timeout, TimeUnit timeUnit)
        throws InterruptedException, ExecutionException, TimeoutException
    {
        FutureTask<T> t = new FutureTask<T>(c);
        THREADPOOL.execute(t);
        return t.get(timeout, timeUnit);
    }
}
