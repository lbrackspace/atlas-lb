package org.openstack.atlas.api.auth;

import org.openstack.atlas.cfg.Configuration;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;

import java.net.MalformedURLException;
import java.util.List;

import static org.mockito.Mockito.*;

// This looks more like an integration test
@Ignore
@RunWith(Enclosed.class)
public class AuthServiceTest {
    public static class WhenAuthenticatingAgainstTheXmlRpcService {
        private final Integer SampleTTlInSecs = 100;
        private final Integer SompleExpirationInSecs = 100;
        private String authToken = "Some Auth Token";
        private XmlRpcClientFactory xmlRpcClientFactory;
        private XmlRpcClient xmlRpcClient;
        private Configuration cfg;

        @Before
        public void Setup() throws MalformedURLException {
            cfg = mock(Configuration.class);
            xmlRpcClient = mock(XmlRpcClient.class);
            xmlRpcClientFactory = mock(XmlRpcClientFactory.class);
            when(xmlRpcClientFactory.get(anyString())).thenReturn(xmlRpcClient);
        }

        @Test
        public void should_authenticate_token_successfully() throws XmlRpcException, MalformedURLException {
            when(xmlRpcClient.execute(
                    eq("validateToken"),
                    argThat(new IsListOfSomeNumberOfElements(3)))
            )
                    .thenReturn(new Object[]{SampleTTlInSecs, SompleExpirationInSecs});

            boolean authenticated = new AuthServiceImpl(xmlRpcClientFactory, cfg).authenticate(passedAccountId, authToken);

            verify(xmlRpcClient).execute(eq("validateToken"), argThat(new IsListOfSomeNumberOfElements(3)));
            Assert.assertTrue(authenticated);
        }

        @Test
        public void should_throw_an_authentication_exception_when_authenticating() throws XmlRpcException, MalformedURLException {
            when(xmlRpcClient.execute(
                    eq("validateToken"),
                    argThat(new IsListOfSomeNumberOfElements(3)))
            )
                    .thenReturn(1);

            try {
                new AuthServiceImpl(xmlRpcClientFactory, cfg).authenticate(passedAccountId, authToken);
                Assert.fail("Should have gotten a run time exception, stubbed a bad return");
            }
            catch(RuntimeException exception) {
                //Expected - Intentional
            }
        }

        @Ignore
        @Test
        public void should_throw_a_token_validation_exception_when_authenticating() throws XmlRpcException, MalformedURLException {
            when(xmlRpcClient.execute(
                    eq("validateToken"),
                    argThat(new IsListOfSomeNumberOfElements(3)))
            )
                    .thenReturn(2);

            try {
                new AuthServiceImpl(xmlRpcClientFactory, cfg).authenticate(passedAccountId, authToken);
                Assert.fail("Should have gotten a run time exception, stubbed a bad return");
            }
            catch(RuntimeException exception) {
                //Expected - Intentional
            }
        }

        @Test
        public void should_get_accountid_successfully_when_provided_the_auth_token() throws XmlRpcException, MalformedURLException {
            when(xmlRpcClient.execute(
                    eq("getUserByToken"),
                    argThat(new IsListOfSomeNumberOfElements(1)))
            )
                    .thenReturn(new Object[] {null, new Object[] {"1000"}});

            Integer idByToken = new AccountServiceImpl(xmlRpcClientFactory, cfg).getAccountIdByToken(authToken);

            verify(xmlRpcClient).execute(eq("getUserByToken"), argThat(new IsListOfSomeNumberOfElements(1)));
            Assert.assertNotNull(idByToken);
        }

        class IsListOfSomeNumberOfElements extends ArgumentMatcher<List> {
            private Integer size;

            IsListOfSomeNumberOfElements(Integer size) {
                this.size = size;
            }

            public boolean matches(Object list) {
                return ((List) list).size() == size;
            }
        }
    }
}
