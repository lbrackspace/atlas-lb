package org.openstack.atlas.usage.execution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.openstack.atlas.usage.thread.util.CastUtil.cast;

public abstract class AbstractUsageExecution {
    private final Log LOG = LogFactory.getLog(UsageAtomHopperExecution.class);
    private HostRepository hostRepository;

    protected ExecutorService executorService;
    protected ExecutorCompletionService<Object> ecs;
    ArrayList<Callable<Object>> callables;
//    ArrayList<Callable<EventThread>> eventCallables;

    @Required
    public void setHostRepository(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    public abstract String getJobName();

    public abstract void processUsages() throws Exception;


    private Map<Integer, Map<Integer, SnmpUsage>> execute() {
        callables = new ArrayList<Callable<Object>>();

        return null;
    }

    private Map<Integer, Map<Integer, SnmpUsage>> execute(LoadBalancer loadBalancer) {

        return null;
    }

    public void collect() throws IOException {
        List<Host> hostList = hostRepository.getAllHosts();
         Map<Integer, Map<Integer, SnmpUsage>> mergedHostsUsage = new HashMap<Integer, Map<Integer, SnmpUsage>>();

        ecs = new ExecutorCompletionService<Object>(executorService);
        executorService = Executors.newFixedThreadPool(hostList.size());

        for (Host host : hostList) {
            callables.add(new Worker(host));
        }
        //Store the potential results of each worker thread.
        Map<Integer, Future<Object>> futures =
                new HashMap<Integer, Future<Object>>();


        Object exception = null;
        try {
            //Add all workers to the ECS and Future collection.
            for (Host host : hostList) {
                futures.put(host.getId(), ecs.submit(new Worker(host)));
            }
            for (int i = 0; i < futures.size(); i++) {
                try {
                    //Get each result as it's available, sometimes blocking.
                    Map<Integer, SnmpUsage> e = cast(ecs.take().get());


                } catch (InterruptedException e) {
                    break;
                } catch (ExecutionException e) {
                    break;
                }
            }
        } finally {
            //Stop any pending tasks if we broke early.
            for (Future<Object> f : futures.values())
                f.cancel(true);
            //And kill all of the threads.
            executorService.shutdownNow();
        }
    }
}