package org.openstack.atlas.restclients.auth.wrapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openstack.atlas.restclients.auth.common.IdentityConstants;
import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.atlas.restclients.auth.util.IdentityUtil;
import org.openstack.identity.client.faults.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.mockito.MockitoAnnotations.initMocks;

public class IdentityResponseWrapperTest {

    @Mock
    Response responseMock;

    IdentityFault ifault;

    @Before
    public void before() {
        initMocks(this);
        ifault = new IdentityFault("error", "ithappened", 415);
    }

    @Test
    public void shouldReturnAuthFault() throws IOException, IdentityFault {
        Assert.assertEquals(IdentityConstants.AUTH_FAULT, IdentityResponseWrapper.buildFaultMessage(responseMock).getCode());
    }

    @Test
    public void shouldReturnIdentityFault400() throws IOException, IdentityFault {
        BadRequestFault brf = new BadRequestFault();
        brf.setCode(400);
        brf.setMessage("fault");
        brf.setDetails("details");
        Mockito.when(responseMock.readEntity((Class<Object>) Matchers.any())).thenReturn(brf);
        Mockito.when(responseMock.getStatus()).thenReturn(IdentityConstants.BAD_REQUEST);
        Assert.assertEquals(IdentityConstants.BAD_REQUEST, IdentityResponseWrapper.buildFaultMessage(responseMock).getCode());
    }

    @Test
    public void shouldReturnIdentityFault401() throws IOException, IdentityFault {
        UnauthorizedFault rf = new UnauthorizedFault();
        rf.setCode(401);
        rf.setMessage("fault");
        rf.setDetails("details");
        Mockito.when(responseMock.readEntity((Class<Object>) Matchers.any())).thenReturn(rf);
        Mockito.when(responseMock.getStatus()).thenReturn(IdentityConstants.UNAUTHORIZED);
        Assert.assertEquals(IdentityConstants.UNAUTHORIZED, IdentityResponseWrapper.buildFaultMessage(responseMock).getCode());
    }

    @Test
    public void shouldReturnIdentityFault403() throws IOException, IdentityFault {
        UserDisabledFault rf = new UserDisabledFault();
        rf.setCode(403);
        rf.setMessage("fault");
        rf.setDetails("details");
        Mockito.when(responseMock.readEntity((Class<Object>) Matchers.any())).thenReturn(rf);
        Mockito.when(responseMock.getStatus()).thenReturn(IdentityConstants.FORBIDDEN);
        Assert.assertEquals(IdentityConstants.FORBIDDEN, IdentityResponseWrapper.buildFaultMessage(responseMock).getCode());
    }

    @Test
    public void shouldReturnIdentityFault404() throws IOException, IdentityFault {
        ItemNotFoundFault rf = new ItemNotFoundFault();
        rf.setCode(404);
        rf.setMessage("fault");
        rf.setDetails("details");
        Mockito.when(responseMock.readEntity((Class<Object>) Matchers.any())).thenReturn(rf);
        Mockito.when(responseMock.getStatus()).thenReturn(IdentityConstants.NOT_FOUND);
        Assert.assertEquals(IdentityConstants.NOT_FOUND, IdentityResponseWrapper.buildFaultMessage(responseMock).getCode());
    }

    @Test
    public void shouldReturnIdentityFault405() throws IOException, IdentityFault {
        Mockito.when(responseMock.getStatus()).thenReturn(IdentityConstants.NOT_PERMITTED);
        Assert.assertEquals(IdentityConstants.NOT_PERMITTED, IdentityResponseWrapper.buildFaultMessage(responseMock).getCode());
    }

    @Test
    public void shouldReturnIdentityFault409() throws IOException, IdentityFault {
        TenantConflictFault rf = new TenantConflictFault();
        rf.setCode(409);
        rf.setMessage("fault");
        rf.setDetails("details");
        Mockito.when(responseMock.readEntity((Class<Object>) Matchers.any())).thenReturn(rf);
        Mockito.when(responseMock.getStatus()).thenReturn(IdentityConstants.NAME_CONFLICT);
        Assert.assertEquals(IdentityConstants.NAME_CONFLICT, IdentityResponseWrapper.buildFaultMessage(responseMock).getCode());
    }

    @Test
    public void shouldReturnIdentityFault503() throws IOException, IdentityFault {
        ServiceUnavailableFault rf = new ServiceUnavailableFault();
        rf.setCode(503);
        rf.setMessage("fault");
        rf.setDetails("details");
        Mockito.when(responseMock.readEntity((Class<Object>) Matchers.any())).thenReturn(rf);
        Mockito.when(responseMock.getStatus()).thenReturn(IdentityConstants.SERVICE_UNAVAILABLE);
        Assert.assertEquals(IdentityConstants.SERVICE_UNAVAILABLE, IdentityResponseWrapper.buildFaultMessage(responseMock).getCode());
    }

    @Test
    public void shouldReturnIdentityFault501() throws IOException, IdentityFault {
        BadRequestFault rf = new BadRequestFault();
        rf.setCode(501);
        rf.setMessage("fault");
        rf.setDetails("details");
        Mockito.when(responseMock.readEntity((Class<Object>) Matchers.any())).thenReturn(rf);
        Mockito.when(responseMock.getStatus()).thenReturn(IdentityConstants.NOT_IMPLMENTED);
        Assert.assertEquals(IdentityConstants.NOT_IMPLMENTED, IdentityResponseWrapper.buildFaultMessage(responseMock).getCode());
    }

    @Test
    public void shouldReturnIdentityFault500() throws IOException, IdentityFault {
        org.openstack.identity.client.faults.IdentityFault rf = new org.openstack.identity.client.faults.IdentityFault();
        rf.setCode(500);
        rf.setMessage("fault");
        rf.setDetails("details");
        Mockito.when(responseMock.readEntity((Class<Object>) Matchers.any())).thenReturn(rf);
        Mockito.when(responseMock.getStatus()).thenReturn(IdentityConstants.AUTH_FAULT);
        Assert.assertEquals(IdentityConstants.AUTH_FAULT, IdentityResponseWrapper.buildFaultMessage(responseMock).getCode());
    }
}

