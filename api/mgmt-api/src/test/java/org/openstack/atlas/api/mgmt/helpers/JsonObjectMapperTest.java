package org.openstack.atlas.api.mgmt.helpers;

import org.junit.*;
import org.openstack.atlas.api.helpers.JsonObjectMapper;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ByIdOrName;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Suspension;

import java.io.IOException;

import static org.openstack.atlas.api.filters.helpers.StringUtilities.getExtendedStackTrace;

public class JsonObjectMapperTest {

    private JsonObjectMapper mapper;

    public JsonObjectMapperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        mapper = new JsonObjectMapper();
        mapper.init();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void shouldMapSuspension() throws IOException {
        String suspensionJsonString = "{" +
                "        \"reason\":\"Non-payment\"," +
                "        \"user\":\"bob\"," +
                "        \"ticket\": {" +
                "             \"ticketId\":\"1234\"," +
                "             \"comment\":\"Late payment\"" +
                "        }" +
                "    }";

        Suspension suspension = mapper.readValue(suspensionJsonString, Suspension.class);
        Assert.assertEquals("Non-payment", suspension.getReason());
        Assert.assertEquals("bob", suspension.getUser());
        Assert.assertEquals("1234", suspension.getTicket().getTicketId());
        Assert.assertEquals("Late payment", suspension.getTicket().getComment());
    }

    @Test
    public void shouldMapByIdOrNameForManagement() throws IOException {
        String bnJson = "{\"id\":1}";
        try {
            ByIdOrName byIdOrName = mapper.readValue(bnJson, ByIdOrName.class);
        } catch (Exception ex) {
            String msg = getExtendedStackTrace(ex);
            nop();
        }

    }

    public void nop() {
    }
}
