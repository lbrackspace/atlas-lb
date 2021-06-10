package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.ItemNotFound;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.LoadBalancerFault;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.events.entities.AlertStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.impl.AlertServiceImpl;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class AlertResourceTest {

    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

    public static class whenRetrievingAlerts {

        AlertResource alertResource;
        Alert alert;

        @Mock
        AlertServiceImpl alertService;
        @Mock
        SecurityContext securityContext;

        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            alertResource = new AlertResource();
            alertResource.setAlertService(alertService);

            alert = new Alert();
            alert.setStatus(AlertStatus.ACKNOWLEDGED);
            alert.setAccountId(1234);
            alert.setAlertType("test");
            alert.setLoadbalancerId(1);
            alert.setId(1);
            alert.setMessage("test");
            alert.setMessageName("test");
            alertResource.setMockitoAuth(true);
            alertResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
            when(alertService.getById(anyInt())).thenReturn(alert);
        }


        @Test
        public void shouldReturn200WhenRetrievingAlerts() {
            Response response = alertResource.retrieveAlerts(securityContext);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturn404WhenRetrievingAlerts() throws EntityNotFoundException {
            when(alertService.getById(anyInt())).thenThrow(EntityNotFoundException.class);
            Response response = alertResource.retrieveAlerts(securityContext);
            Assert.assertEquals(404, response.getStatus());
            Assert.assertEquals("Object not Found", ((ItemNotFound) response.getEntity()).getMessage());
        }
    }

        public static class whenUpdatingAlertStatus {

            AlertResource alertResource;
            @Mock
            NotificationService notificationService;
            @Mock
            Alert alert;

            @Before
            public void setUp() throws Exception {
                MockitoAnnotations.initMocks(this);
                alertResource = new AlertResource();
                alertResource.setMockitoAuth(true);
                alertResource.setNotificationService(notificationService);
                when(notificationService.getAlert(anyInt())).thenReturn(alert);
            }

            @Test
            public void shouldReturn202WhenUpdatingAlert() {
                Response response = alertResource.updateStatus();
                Assert.assertEquals(202, response.getStatus());
            }

            @Test
            public void shouldReturn404WhenUpdatingAlert() throws Exception {
                when(notificationService.getAlert(anyInt())).thenThrow(EntityNotFoundException.class);
                Response response = alertResource.updateStatus();
                Assert.assertEquals(404, response.getStatus());
                Assert.assertEquals("Object not Found", ((ItemNotFound) response.getEntity()).getMessage());
            }

            @Test
            public void shouldReturn400WhenUpdatingAlert() throws Exception {
                doThrow(Exception.class).when(notificationService).updateAlert(alert);
                Response response = alertResource.updateStatus();
                Assert.assertEquals(500, response.getStatus());
                Assert.assertEquals("An unknown exception has occurred. Please contact support.", ((LoadBalancerFault) response.getEntity()).getMessage());
            }

        }





}
