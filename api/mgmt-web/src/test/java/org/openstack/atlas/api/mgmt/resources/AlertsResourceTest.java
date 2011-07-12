package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.events.entities.AlertStatus;
import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.services.AlertService;
import junit.framework.Assert;
import org.dozer.DozerBeanMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AlertsResourceTest {
    private AlertService alertService;
    private AlertsResource alertsResource;
    private OperationResponse operationResponse;
    List<Alert> alerts;
    List<Integer> accounts;

    @Before
    public void setUp() {
        alertsResource = new AlertsResource();
        alertsResource.setMockitoAuth(true);
        AlertRepository arepo = mock(AlertRepository.class);
        alertService = mock(AlertService.class);
        alertsResource.setAlertRepository(arepo);
        operationResponse = new OperationResponse();
        operationResponse.setExecutedOkay(true);
        List<String> mappingFiles = new ArrayList<String>();
        mappingFiles.add("loadbalancing-dozer-management-mapping.xml");
        alertsResource.setDozerMapper(new DozerBeanMapper(mappingFiles));

        Alert alert1 = new Alert();
        alert1.setAccountId(548939);
        alert1.setAlertType("Test_Alert");
        alert1.setCreated(Calendar.getInstance());
        alert1.setId(1);
        alert1.setLoadbalancerId(702);
        alert1.setMessage("I am the first test alert message.");
        alert1.setMessageName("FirstName");
        alert1.setStatus(AlertStatus.UNACKNOWLEDGED);

        Alert alert2 = new Alert();
        alert2.setAccountId(549838);
        alert2.setAlertType("Test_Alert");
        alert2.setId(2);
        alert2.setLoadbalancerId(813);
        alert2.setMessage("I am the second test alert message.");
        alert2.setStatus(AlertStatus.UNACKNOWLEDGED);

        Alert alert3 = new Alert();
        alert3.setAccountId(549838);
        alert3.setAlertType("Test_Alert");
        alert3.setId(3);
        alert3.setLoadbalancerId(35);
        alert3.setMessage("I am the second test alert message.");
        alert3.setStatus(AlertStatus.UNACKNOWLEDGED);

        Alert alert4 = new Alert();
        alert4.setAccountId(549999);
        alert4.setAlertType("Test_Alert");
        alert4.setId(4);
        alert3.setLoadbalancerId(83);
        alert4.setMessage("I am the fourth test alert message.");
        alert4.setStatus(AlertStatus.UNACKNOWLEDGED);

        alerts = new ArrayList<Alert>();
        alerts.add(alert1);
        alerts.add(alert2);
        alerts.add(alert3);
        alerts.add(alert4);
    }

    @Ignore
    @Test
    public void shouldReturn200WhenMultipleAccountsPassedAsQueryParameters() throws BadRequestException {
        operationResponse.setExecutedOkay(true);
        accounts = new ArrayList<Integer>();
        for (int i = 900; i < 1000; i = i + 10) {
            accounts.add(i);
        }
        when(alertService.getByAccountId(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString(),
                Matchers.anyString())).thenReturn(alerts);
        Response resp = alertsResource.retrieveByAccountIds(null, null, accounts, null, null, false);
        Assert.assertEquals(200, resp.getStatus());
    }

    @Ignore
    @Test
    public void shouldReturn200WhenStartDateAndEndDateSupplied() throws BadRequestException {
        operationResponse.setExecutedOkay(true);
        accounts = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            accounts.add(i);
        }
        when(alertService.getByAccountId(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString(),
                Matchers.anyString())).thenReturn(null);
        Response resp = alertsResource.retrieveByAccountIds(null, null, accounts, Calendar.getInstance().toString(), Calendar.getInstance().toString(), false);
        Assert.assertEquals(200, resp.getStatus());
    }
}