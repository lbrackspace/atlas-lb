package org.openstack.atlas.restclients.atomhopper.actionlogs;

import org.openstack.atlas.restclients.atomhopper.AtomHopperClient;
import org.openstack.atlas.restclients.atomhopper.AtomHopperClientImpl;
import org.openstack.atlas.restclients.atomhopper.exception.ClientInstantiationException;

public class ActionLogsClient {
    //Take in or get default ActionLogs(AL) url, map the object(s) then send them to the AH service to be processed by AL
    private String endpoint;
    private AtomHopperClient atomHopperClient;

    public ActionLogsClient(String endpoint) throws ClientInstantiationException {
        this.endpoint = endpoint;
        try {
            atomHopperClient = new AtomHopperClientImpl(endpoint);
        } catch (Exception e) {
            throw new ClientInstantiationException("Could not instantiate ActionLogsClient. ", e);
        }
    }

    public void createNodeState() {
        //Take in data object and send to mapper
        //After its been mapped push via AHClient
    }

    public void createVipState() {
        //Take in data object and send to mapper
        //After its been mapped push via AHClient
    }

    public void createLBState() {
        //Take in data object and send to mapper
        //After its been mapped push via AHClient
    }
}
