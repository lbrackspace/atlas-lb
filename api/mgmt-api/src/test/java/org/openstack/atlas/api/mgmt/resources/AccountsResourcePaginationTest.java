package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.openstack.atlas.api.helpers.PaginationHelper;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.GeneralFault;
import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.repository.AccountUsageRepository;
import org.openstack.atlas.util.common.exceptions.ConverterException;
import org.openstack.atlas.util.converters.DateTimeConverters;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.w3.atom.Link;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest({AccountsResource.class, DateTimeConverters.class, PaginationHelper.class})
public class AccountsResourcePaginationTest {

    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

    @Mock
    private AccountUsageRepository accountUsageRepository;
    @InjectMocks
    private AccountsResource accountsResource;
    private Link link;
    private Calendar startTime;
    private Calendar endTime;
    private AccountUsage accountUsage1;
    private AccountUsage accountUsage2;
    private List<AccountUsage> accountUsagesList;
    private String startTimeParam;
    private String endTimeParam;
    private Integer offset;
    private Integer limit;
    private String relativeUri;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        accountsResource = new AccountsResource();
        accountsResource.setMockitoAuth(true);
        accountsResource.setDozerMapper(DozerBeanMapperBuilder.create()
                .withMappingFiles(mappingFile)
                .build());
        accountsResource.setAccountUsageRepository(accountUsageRepository);

        accountUsage1 = new AccountUsage();
        accountUsage2 = new AccountUsage();
        accountUsage1.setAccountId(8654);
        accountUsage1.setId(1);
        accountUsage1.setStartTime(startTime);
        accountUsage2.setAccountId(7654);
        accountUsage2.setId(2);
        accountUsage2.setStartTime(startTime);
        accountUsagesList = new ArrayList<AccountUsage>();
        accountUsagesList.add(accountUsage1);
        accountUsagesList.add(accountUsage2);
        link = new Link();
        link.setTitle("https://localhost/management/accounts/usage?startTime=2021-05-04T13:00:00-05:00&endTime=2021-05-05T14:00:00-05:00&offset=500&limit=1000");
        startTime = new GregorianCalendar(2021,05,04);
        endTime = new GregorianCalendar(2021,05,05);
        startTimeParam = "2021-05-04T13:00:00-05:00";
        endTimeParam = "2021-05-05T14:00:00-05:00";
        relativeUri = "https://localhost/management/accounts/usage?startTime=2021-05-04T13:00:00-05:00&endTime=2021-05-05T14:00:00-05:00&offset=500&limit=1000";
    }

    @Test
    public void shouldThrowBadRequestException400(){
        startTimeParam = null;
        endTimeParam = null;
        String expected = "\'startTime\' and \'endTime\' query parameters are required";
        Response response = accountsResource.retrieveAllAccountUsage(startTimeParam, endTimeParam, offset, limit);
        Assert.assertEquals(400, response.getStatus());
        String actual = ((GeneralFault)response.getEntity()).getMessage();
        Assert.assertEquals(expected, actual);
    }

    @Test(expected = ConverterException.class)
    public void shouldThrowConverterException() throws ConverterException {
        String actual = "Date parameters must follow ISO-8601 (yyyy-MM-dd'T'HH:mm:ss) format";
        PowerMockito.spy(DateTimeConverters.class);
        BDDMockito.given(DateTimeConverters.isoTocal(ArgumentMatchers.anyString())).willThrow(ConverterException.class);
        Response response = accountsResource.retrieveAllAccountUsage(startTimeParam, endTimeParam, offset, limit);
        String expected = ((BadRequest)response.getEntity()).getMessage();
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void shouldThrowException() {
        doThrow(Exception.class).when(accountUsageRepository).getAccountUsageRecords(ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Response response = accountsResource.retrieveAllAccountUsage(startTimeParam, endTimeParam, offset, limit);
        Assert.assertEquals(500,response.getStatus());
    }

    @Test
    public void shouldRetrieveAccountUsageWithOffsetZeroAndRecordsLessThanLimit() throws ConverterException {
        PowerMockito.mockStatic(PaginationHelper.class);
        PowerMockito.mockStatic(DateTimeConverters.class);
        when(DateTimeConverters.isoTocal(any(String.class))).thenReturn(Calendar.getInstance());
        when(DateTimeConverters.isoTocal(any(String.class))).thenReturn(Calendar.getInstance());
        when(PaginationHelper.createLink(any(String.class), any(String.class), anyBoolean())).thenReturn(link);
        when(PaginationHelper.determinePageLimit(ArgumentMatchers.eq(limit))).thenReturn(100);
        when(PaginationHelper.determinePageOffset(ArgumentMatchers.eq(offset))).thenReturn(0);
        doReturn(accountUsagesList).when(accountUsageRepository).getAccountUsageRecords(ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Response response = accountsResource.retrieveAllAccountUsage(startTimeParam, endTimeParam, offset, limit);
        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldRetrieveAccountUsageWithOffsetZeroAndRecordsGreaterThanLimit() throws ConverterException {
        PowerMockito.mockStatic(PaginationHelper.class);
        PowerMockito.mockStatic(DateTimeConverters.class);
        when(DateTimeConverters.isoTocal(any(String.class))).thenReturn(Calendar.getInstance());
        when(DateTimeConverters.isoTocal(any(String.class))).thenReturn(Calendar.getInstance());
        when(PaginationHelper.createLink(any(String.class), any(String.class), anyBoolean())).thenReturn(link);
        when(PaginationHelper.determinePageLimit(ArgumentMatchers.eq(limit))).thenReturn(1);
        when(PaginationHelper.determinePageOffset(ArgumentMatchers.eq(offset))).thenReturn(0);
        doReturn(accountUsagesList).when(accountUsageRepository).getAccountUsageRecords(ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Response response = accountsResource.retrieveAllAccountUsage(startTimeParam, endTimeParam, offset, limit);
        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldRetrieveAccountUsageWithOffsetGreaterThanZeroAndRecordsGreaterThanLimit() throws ConverterException {
        PowerMockito.mockStatic(PaginationHelper.class);
        PowerMockito.mockStatic(DateTimeConverters.class);
        when(DateTimeConverters.isoTocal(any(String.class))).thenReturn(Calendar.getInstance());
        when(DateTimeConverters.isoTocal(any(String.class))).thenReturn(Calendar.getInstance());
        when(PaginationHelper.createLink(any(String.class), any(String.class), anyBoolean())).thenReturn(link);
        when(PaginationHelper.determinePageLimit(ArgumentMatchers.eq(limit))).thenReturn(1);
        when(PaginationHelper.determinePageOffset(ArgumentMatchers.eq(offset))).thenReturn(1);
        doReturn(accountUsagesList).when(accountUsageRepository).getAccountUsageRecords(ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Response response = accountsResource.retrieveAllAccountUsage(startTimeParam, endTimeParam, offset, limit);
        Assert.assertEquals(200, response.getStatus());
    }
}
