package org.openstack.atlas.api.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.user.User;

public class AuthTokenValidator {
    private static final Log LOG = LogFactory.getLog(AuthTokenValidator.class);
    private AuthService authService;

    public AuthTokenValidator(AuthService authService) {
        this.authService = authService;
    }

    public User validate(Integer passedAccountId, String authToken) throws Exception {
        LOG.info("Within validate ... about to call AuthService authenticate adn AccountService validate");

        User user = authService.authenticate(passedAccountId, authToken);
        return user.getMossoId().equals(passedAccountId) ? user : null;
    }
}
