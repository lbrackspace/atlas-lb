package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;

public class ZeusEvent
    implements Serializable
{

    private final static long serialVersionUID = 532512316L;

    protected String eventType;

    protected String paramLine;

    protected String callbackHost;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String value) {
        this.eventType = value;
    }

    public String getParamLine() {
        return paramLine;
    }

    public void setParamLine(String value) {
        this.paramLine = value;
    }

    public String getCallbackHost() {
        return callbackHost;
    }

    public void setCallbackHost(String callbackHost) {
        this.callbackHost = callbackHost;
    }

}
