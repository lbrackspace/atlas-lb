package org.openstack.atlas.api.validation.validators;

import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.api.validation.context.NetworkItemContext;
import org.openstack.atlas.api.validation.results.ExpectationResult;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItem;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItemType;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openstack.atlas.api.validation.context.NetworkItemContext.FULL;
import static org.openstack.atlas.api.validation.context.NetworkItemContext.PARTIAL;

public class NetworkItemValidatorTest {

    private NetworkItemValidator nwValidator;
    private NetworkItem nwTest;

    @Before
    public void setupValidNetworkItemObject() {
        nwValidator = new NetworkItemValidator();

        nwTest = new NetworkItem();
        nwTest.setAddress("69.172.35.111");
        nwTest.setType(NetworkItemType.ALLOW);

    }

    public static NetworkItem initNetworkItem(String ip, String networkitemtype, String id) {
        NetworkItem networkitem = new NetworkItem();
        networkitem.setAddress(ip);
        networkitem.setType((networkitemtype == null) ? null : NetworkItemType.valueOf(networkitemtype));
        networkitem.setId((id == null) ? null : Integer.parseInt(id));
        return networkitem;
    }

    @Test
    public void shouldRejectInvalidIpForLBDevice() {
        NetworkItem nwTest1 = new NetworkItem();
        nwTest1.setAddress("0.0.0.0");
        ValidatorResult result = nwValidator.validate(nwTest1, FULL);
        assertFalse(result.passedValidation());
    }

    @Test
    public void shouldAcceptValidNetworkItem() {
        ValidatorResult result = nwValidator.validate(nwTest, FULL);
        assertTrue(resultMessage(result, FULL), result.passedValidation());

        result = setPartialContextAndFetchPutResults(nwTest);
        assertTrue(resultMessage(result, PARTIAL), result.passedValidation());
    }

    @Test
    public void shouldRejectNullIp() {
        nwTest = initNetworkItem(null, "DENY", null);
        ValidatorResult result = nwValidator.validate(nwTest, FULL);
        assertFalse(resultMessage(result, FULL), result.passedValidation());
    }

    @Test
    public void shouldRejectNullType() {
        nwTest = initNetworkItem("69.172.35.111", null, null);
        ValidatorResult result = nwValidator.validate(nwTest, FULL);
        assertFalse(resultMessage(result, FULL), result.passedValidation());
    }

    @Test
    public void shouldRejectId() {
        nwTest.setId(1);
        ValidatorResult result = nwValidator.validate(nwTest, FULL);
        assertFalse(resultMessage(result, NetworkItemContext.FULL), result.passedValidation());

        result = setPartialContextAndFetchPutResults(nwTest);
        assertFalse(resultMessage(result, PARTIAL), result.passedValidation()); // Should Reject ID during Partial as well

    }

    @Test
    public void shouldAcceptHexdigitslessthan4() {
        nwTest = initNetworkItem("1:2:3:4:5:6:7:8", "ALLOW", null);
        ValidatorResult result = nwValidator.validate(nwTest, FULL);
        assertTrue(resultMessage(result, FULL), result.passedValidation());

        result = setPartialContextAndFetchPutResults(nwTest);
        assertTrue(resultMessage(result, PARTIAL), result.passedValidation());
    }

    @Test
    public void shouldAcceptSingleAttributesInPut() { // Except ip addres and version should be in pairs
        nwTest = initNetworkItem("192.168.3.51", null, null);
        ValidatorResult result = nwValidator.validate(nwTest, PARTIAL);
        assertTrue(resultMessage(result, PARTIAL), result.passedValidation());


        nwTest = initNetworkItem(null, "ALLOW", null);
        result = nwValidator.validate(nwTest, NetworkItemContext.PARTIAL);
        assertTrue(resultMessage(result, PARTIAL), result.passedValidation());
    }

    //@Test
    public void ShouldAcceptIPv4Subnets() {
        int i;
        String ip;
        for (i = -65536; i <= 6536; i++) {
            ip = String.format("192.168.3.51/%d", i);
            nwTest = initNetworkItem(ip, "ALLOW", null);
            ValidatorResult result = nwValidator.validate(nwTest, FULL);
            if (i >= 0 && i <= 32) {
                assertTrue(String.format("Expected acceptance of %s", ip), result.passedValidation());
            } else {
                assertFalse(String.format("Expected rejection of %s", ip), result.passedValidation());
            }
        }
    }

    @Test
    public void shouldRejectNegIntIdOnPut() {
        nwTest = initNetworkItem("192.168.3.1", "ALLOW", "-1");
        ValidatorResult result = nwValidator.validate(nwTest, PARTIAL);
        assertFalse(resultMessage(result, PARTIAL), result.passedValidation());
    }

    @Test
    public void shouldReject1billionorMoreForIdOnPut() {
        nwTest = initNetworkItem("192.168.3.1", "ALLOW", "1000000000");
        ValidatorResult result = nwValidator.validate(nwTest, PARTIAL);
        assertFalse(resultMessage(result, PARTIAL), result.passedValidation());
    }

    @Test
    public void shouldAcceptIfNoIdPresentOnPut() {
        nwTest = initNetworkItem("192.168.3.51", "ALLOW", null);
        ValidatorResult result = nwValidator.validate(nwTest, PARTIAL);
        assertTrue(resultMessage(result, PARTIAL), result.passedValidation());
    }

    @Test
    public void shouldRejectNetworkItemifAllAttributesAreMissingOnPut() {
        nwTest = initNetworkItem(null, null, null);
        ValidatorResult result = nwValidator.validate(nwTest, FULL);
        assertFalse(resultMessage(result, PARTIAL), result.passedValidation());
    }

    public ValidatorResult setPartialContextAndFetchPutResults(NetworkItem nwTest) {
        NetworkItemValidator nwv = new NetworkItemValidator();
        ValidatorResult result;
        //nwTest.setId(100);
        result = nwv.validate(nwTest, PARTIAL);
        return result;

    }

    public String resultMessage(ValidatorResult result, Enum ctx) {
        StringBuffer sb;
        sb = new StringBuffer();
        if (!result.passedValidation()) {
            List<ExpectationResult> ers = result.getValidationResults();
            sb.append(String.format("ON %s result.withMessage([", ctx.toString()));
            for (ExpectationResult er : ers) {
                sb.append(String.format("%s", er.getMessage()));
                sb.append("])");
            }
        } else {
            sb.append(String.format("On %s All Expectations PASSED\n", ctx.toString()));
        }
        return sb.toString();
    }

    @Test
    public void shouldRejectTooFewOctetsIPv4() {
        nwTest = initNetworkItem("200.200.200", "ALLOW", null);
        ValidatorResult result = nwValidator.validate(nwTest, FULL);
        assertFalse(resultMessage(result, FULL), result.passedValidation());

        result = setPartialContextAndFetchPutResults(nwTest);
        assertFalse(resultMessage(result, PARTIAL), result.passedValidation());
    }

    @Test
    public void shouldRejectTooManyOctetsIPv4() {
        nwTest = initNetworkItem("200.200.200.200.200", "ALLOW", null);
        ValidatorResult result = nwValidator.validate(nwTest, FULL);
        assertFalse(resultMessage(result, FULL), result.passedValidation());

        result = setPartialContextAndFetchPutResults(nwTest);
        assertFalse(resultMessage(result, PARTIAL), result.passedValidation());
    }

    @Test
    public void shouldRejectOctetOutofRangeIPv4() {
        nwTest = initNetworkItem("266.0.0.0", "ALLOW", null);
        ValidatorResult result = nwValidator.validate(nwTest, FULL);
        assertFalse(resultMessage(result, NetworkItemContext.FULL), result.passedValidation());

        result = setPartialContextAndFetchPutResults(nwTest);
        assertFalse(resultMessage(result, PARTIAL), result.passedValidation());
    }

    @Test
    public void shouldRejectHostNamesIpv4() {
        nwTest = initNetworkItem("www.google.com", "ALLOW", null);
        ValidatorResult result = nwValidator.validate(nwTest, FULL);
        assertFalse(resultMessage(result, FULL), result.passedValidation());

        result = setPartialContextAndFetchPutResults(nwTest);
        assertFalse(resultMessage(result, PARTIAL), result.passedValidation());
    }

    @Test
    public void shouldRejectEmptyIpStrings() {
        nwTest = initNetworkItem("", "ALLOW", null);
        ValidatorResult result = nwValidator.validate(nwTest, FULL);
        assertFalse(resultMessage(result, FULL), result.passedValidation());

        result = setPartialContextAndFetchPutResults(nwTest);
        assertFalse(resultMessage(result, PARTIAL), result.passedValidation());
    }
}
