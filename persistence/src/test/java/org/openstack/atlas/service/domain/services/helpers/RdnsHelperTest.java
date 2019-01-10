package org.openstack.atlas.service.domain.services.helpers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openstack.atlas.restclients.auth.IdentityAuthClientImpl;
import org.openstack.identity.client.access.Access;
import org.openstack.identity.client.access.Token;
import org.openstack.identity.client.user.User;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.doReturn;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RdnsHelper.class)
@PowerMockIgnore("javax.management.*")
public class RdnsHelperTest {
    int userAccountId = 12345;

    @Mock
    IdentityAuthClientImpl identityAuthClient;

    RdnsHelper rdnsHelper = new RdnsHelper(userAccountId);

    @Before
    public void standUp() {
        initMocks(this);
        identityAuthClient = PowerMockito.mock(IdentityAuthClientImpl.class);
    }

    @Test
    public void shouldGetImpersonationToken() throws Exception {
        Access impAccess = new Access();
        Token token = new Token();
        token.setId("77777");
        impAccess.setToken(token);
        User user = new User();
        user.setUsername("bob");

        PowerMockito.whenNew(IdentityAuthClientImpl.class).withAnyArguments().thenReturn(identityAuthClient);
        doReturn("43215").when(identityAuthClient).getAuthToken();
        doReturn(user).when(identityAuthClient).getPrimaryUserForTenantId("43215", Integer.valueOf(userAccountId).toString());
        doReturn(impAccess).when(identityAuthClient).impersonateUser("43215", "bob");

        Assert.assertEquals("77777", rdnsHelper.getImpersanatedUserToken());

    }
    // TODO: test older methods
}
