package org.openstack.atlas.api.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;

import java.net.MalformedURLException;

public class AuthTokenValidator {
    private AuthService authService;
    private AccountService accountService;
    private static final Log LOG = LogFactory.getLog(AuthTokenValidator.class);

    public AuthTokenValidator(AuthService authService, AccountService accountService) {

        this.authService = authService;
        this.accountService = accountService;
    }

    public Boolean validate(Integer passedAccountId, String authToken) throws MalformedURLException, XmlRpcException {
        LOG.info("Within validate ... about to call AuthService authenticate and AccountService validate");
        return authService.authenticate(authToken) && accountService.getAccountIdByToken(authToken).equals(passedAccountId);
    }

    public String getUserName(Integer passedAccountId, String authToken) throws MalformedURLException, XmlRpcException {
        LOG.info("Within validate ... about to call AuthService authenticate and AccountService validate");
        return accountService.getUserNameByToken(authToken);
    }
}
