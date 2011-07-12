package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItem;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItemType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;

import static org.openstack.atlas.api.helpers.ResultMessage.resultMessage;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AccessListValidatorTest {

    private AccessListValidator alValidator;
    private AccessList alTest;
    private AccessList alTest2;
    private NetworkItem nwTest;
    private NetworkItem nwTest2;

    @Before
    public void setUpAccessListValidatorObject() {
        alValidator = new AccessListValidator();

        nwTest = new NetworkItem();
        nwTest.setAddress("69.172.35.111");
        nwTest.setType(NetworkItemType.ALLOW);

        nwTest2 = new NetworkItem();
        nwTest2.setAddress("69999.188.256.111");
        nwTest2.setType(null);


        alTest = new AccessList();
        alTest.getNetworkItems().add(nwTest);

        alTest2 = new AccessList();
        alTest2.getNetworkItems().add(nwTest2);
    }

    @Test
    public void shouldHaveValidNetworkItem() {
        ValidatorResult result = alValidator.validate(alTest, POST);
        assertTrue(resultMessage(result, POST), result.passedValidation());

        result = alValidator.validate(alTest, PUT);
        assertTrue(resultMessage(result, PUT), result.passedValidation());

    }

    @Test
    public void shouldRejectEmptyNetworkList() {
        AccessList al = new AccessList();

        ValidatorResult result = alValidator.validate(al, POST);
        assertFalse(resultMessage(result, POST), result.passedValidation());

        result = alValidator.validate(al, PUT);
        assertFalse(resultMessage(result, PUT), result.passedValidation());

    }

    @Test
    public void shouldAcceptMultipleNetworkItemsWhenTheyAreValid() {
        AccessList al = new AccessList();
        al.getNetworkItems().add(initNetworkItem("192.168.3.51/32", "DENY", null));
        //al.getNetworkItems().add(initNetworkItem("ffff::/120", "ALLOW", null));
        al.getNetworkItems().add(initNetworkItem("127.0.0.1/8", "ALLOW", null));
        ValidatorResult result = alValidator.validate(al, POST);
        assertTrue(resultMessage(result, POST), result.passedValidation());

        result = alValidator.validate(al, PUT);
        assertTrue(resultMessage(result, PUT), result.passedValidation());


    }

    @Test
    public void shouldRejectNullAddress() {
        alTest.getNetworkItems().remove(nwTest);
        nwTest.setAddress(null);
        alTest.getNetworkItems().add(nwTest);

        ValidatorResult result = alValidator.validate(alTest, POST);
        assertFalse(resultMessage(result, POST), result.passedValidation());

        result = alValidator.validate(alTest, PUT);
        assertFalse(resultMessage(result, PUT), result.passedValidation());
    }

    @Test
    public void shouldRejectNetworkItemIdOnAccessListPutOrPost() {
        AccessList al = new AccessList();
        al.getNetworkItems().add(initNetworkItem("192.168.3.51/32", "DENY", null)); // Good
        al.getNetworkItems().add(initNetworkItem("192.168.3.24/32", "ALLOW", "31")); //Bad
        al.getNetworkItems().add(initNetworkItem("192.168.3.54/32", "DENY", null)); // Good
        ValidatorResult result = alValidator.validate(al, POST);
        assertFalse(resultMessage(result, POST), result.passedValidation());

        result = alValidator.validate(al, PUT);
        assertFalse(resultMessage(result, PUT), result.passedValidation());
    }

    @Test
    public void shouldRejectPartialNetworkItemsDuringPutOrPost() {
        AccessList al = new AccessList();
        al.getNetworkItems().add(initNetworkItem("192.168.3.51", null, null));
        al.getNetworkItems().add(initNetworkItem(null, "ALLOW", null));
        ValidatorResult result = alValidator.validate(al, PUT);
        assertFalse(resultMessage(result, PUT), result.passedValidation());

        result = alValidator.validate(al, POST);
        assertFalse(resultMessage(result, POST), result.passedValidation());
    }

    @Test
    public void shouldRejectWholeListIfASingleNetworkItemIsInvalid() {
        AccessList al = new AccessList();
        al.getNetworkItems().add(initNetworkItem("192.168.3.54/32", "DENY", null)); // Good
        al.getNetworkItems().add(initNetworkItem("192.168.3.54/32", null, null)); // Bad
        al.getNetworkItems().add(initNetworkItem("::/32", "DENY", null)); // Good
        ValidatorResult result = alValidator.validate(al, POST);
        assertFalse(resultMessage(result, POST), result.passedValidation());

        result = alValidator.validate(al, PUT);
        assertFalse(resultMessage(result, PUT), result.passedValidation());
    }

    private static NetworkItem initNetworkItem(String ip,String networkitemtype, String id) {
        NetworkItem networkitem = new NetworkItem();
        networkitem.setAddress(ip);
        networkitem.setType((networkitemtype == null) ? null : NetworkItemType.valueOf(networkitemtype));
        networkitem.setId((id == null) ? null : Integer.parseInt(id));
        return networkitem;
    }
     @Test
    public void shouldRejectInvalidValidNetworkItemList() {
        AccessList al = new AccessList();
        al.getNetworkItems().add(initNetworkItem("192.168.3.1/32", "DENY", null)); // Good
        al.getNetworkItems().add(initNetworkItem("192.168.3.2/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.3/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.4/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.5/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.6/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.7/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.8/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.9/32", "DENY", null)); // Good
             al.getNetworkItems().add(initNetworkItem("192.168.3.10/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.11/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.12/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.13/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.14/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.15/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.16/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.17/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.18/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.19/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.20/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.21/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.22/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.23/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.24/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.25/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.26/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.27/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.28/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.29/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.30/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.31/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.32/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.33/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.34/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.35/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.36/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.37/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.38/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.39/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.40/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.41/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.42/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.4.43/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.4.54/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.5.54/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.6.54/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.7.54/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.8.54/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.9.54/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.50.54/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.51.54/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.52.54/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.53.54/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.54/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.55/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.56/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.57/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.58/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.59/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.60/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.61/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.62/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.54/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.63/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.64/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.65/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.66/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.67/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.68/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.69/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.70/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.71/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.72/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.73/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.74/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.75/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.76/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.77/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.78/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.79/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.80/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.81/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.82/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.83/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.84/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.85/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.86/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.87/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.88/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.89/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.90/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.91/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.92/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.93/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.94/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.95/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.96/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.97/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.98/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.99/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.167.3.01/32", "DENY", null)); // Good
         al.getNetworkItems().add(initNetworkItem("192.168.3.54/32", "DENY", null)); // Good

        al.getNetworkItems().add(initNetworkItem("::/32", "DENY", null)); // Good
        ValidatorResult result = alValidator.validate(al, POST);
        assertFalse(resultMessage(result, POST), result.passedValidation());

        result = alValidator.validate(al, PUT);
        assertFalse(resultMessage(result, PUT), result.passedValidation());

    }
}
