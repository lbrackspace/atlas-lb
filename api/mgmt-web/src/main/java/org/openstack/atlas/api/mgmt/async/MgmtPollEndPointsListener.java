package org.openstack.atlas.api.mgmt.async;

import java.util.ArrayList;
import org.openstack.atlas.service.domain.entities.Host;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;
import java.util.List;
import org.openstack.atlas.api.integration.threads.HostEndpointPollThread;
import org.openstack.atlas.util.debug.Debug;

public class MgmtPollEndPointsListener extends BaseListener {

    final Log LOG = LogFactory.getLog(MgmtPollEndPointsListener.class);

    @Override
    public void doOnMessage(Message message) throws Exception {
        LOG.info("Entering " + getClass());
        LOG.info(message);

        List<Host> endpointHosts = hostService.getAll();
        List<HostEndpointPollThread> epThreads = new ArrayList<HostEndpointPollThread>();
        for (Host epHost : endpointHosts) {
            HostEndpointPollThread epThread = new HostEndpointPollThread();
            epThread.setHost(epHost);
            epThread.setProxyService(reverseProxyLoadBalancerService);
            epThreads.add(epThread);
        }
        Debug.nop();
        double jobStartTime = Debug.getEpochSeconds();
        for (HostEndpointPollThread epThread : epThreads) {
            LOG.info(String.format("Starting endpoint thread for host %s", epThread.toString()));
            epThread.start();
        }
        Debug.nop();
        for (HostEndpointPollThread epThread : epThreads) {
            LOG.info(String.format("Joining endpoint thread for host %s", epThread.toString()));
            epThread.join();
        }
        double jobFinishTime = Debug.getEpochSeconds();
        Debug.nop();

        for (HostEndpointPollThread epThread : epThreads) {
            Exception ex = epThread.getException();
            if (ex != null) {
                LOG.error(String.format("Error retretreiving hostEndpointStatus from thread %s: Exception was %s", epThread.toString(), Debug.getExtendedStackTrace(ex)));
                continue;
            }
            if (epThread.isEndPointWorking()) {
                LOG.info(String.format("Thread %s reports host is up Marking HOST GOOD", epThread.toString()));
                epThread.getHost().setSoapEndpointActive(Boolean.TRUE);

            } else {
                LOG.error(String.format("Thread %s reports host is down MARKING HOST BAD!!!!!", epThread.toString()));
                epThread.getHost().setSoapEndpointActive(Boolean.FALSE);
            }
            hostService.update(epThread.getHost());
        }
        double pollTime = jobFinishTime - jobStartTime;
        LOG.info(String.format("hostendpoint polling took %f seconds", pollTime));
        Debug.nop();
    }
}
