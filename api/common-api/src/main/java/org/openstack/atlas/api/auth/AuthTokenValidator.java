package org.openstack.atlas.api.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AuthTokenValidator {
    private AuthService authService;
    private AccountService accountService;
    private static final Log LOG = LogFactory.getLog(AuthTokenValidator.class);

    public AuthTokenValidator(AuthService authService, AccountService accountService) {
        this.authService = authService;
        this.accountService = accountService;
    }

    public Boolean validate(String passedAccountId, String authToken) throws Exception {
        LOG.info("Within validate ... about to call AuthService authenticate");
        return authService.authenticate(passedAccountId,authToken);
    }

    public String getUserName(Integer passedAccountId, String authToken) throws Exception{
        LOG.info("Within validate ... about to call AccountService retrieve username");
        return accountService.getUsernameByToken(authToken);
    }
}
