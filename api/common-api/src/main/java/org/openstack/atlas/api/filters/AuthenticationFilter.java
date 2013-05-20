package org.openstack.atlas.api.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.openstack.atlas.api.auth.AuthInfo;
import org.openstack.atlas.api.auth.AuthTokenValidator;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.api.exceptions.MalformedUrlException;
import org.openstack.atlas.api.filters.wrappers.HeadersRequestWrapper;
import org.openstack.atlas.api.helpers.UrlAccountIdExtractor;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.LoadBalancerFault;
import org.openstack.atlas.util.b64aes.Base64;
import org.openstack.atlas.util.b64aes.PaddingException;
import org.openstack.atlas.util.simplecache.CacheEntry;
import org.openstack.atlas.util.simplecache.SimpleCache;
import org.openstack.identity.client.fault.IdentityFault;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.openstack.atlas.api.filters.helpers.StringUtilities.getExtendedStackTrace;

public class AuthenticationFilter implements Filter {
    private final Log LOG = LogFactory.getLog(AuthenticationFilter.class);

    private final String X_AUTH_TENANT_ID = "X-Tenant-Name";
    private final String X_AUTH_USER_NAME = "X-PP-User";
    private final String X_AUTH_TOKEN = "X-Auth-Token";
    private final String AUTHORIZATION_HEADER = "Authorization";

    private UrlAccountIdExtractor accountIdExtractor;
    private AuthTokenValidator authTokenValidator;
    private RestApiConfiguration configuration;
    private FilterConfig filterConfig = null;
    private SimpleCache<AuthInfo> userCache;

    public AuthenticationFilter(UrlAccountIdExtractor urlAccountIdExtractor) {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {// Not implemented...
    }

    @Override
    public void destroy() {// Not implemented...
    }

    public AuthenticationFilter(AuthTokenValidator authTokenValidator, UrlAccountIdExtractor urlAccountIdExtractor) {
        this.authTokenValidator = authTokenValidator;
        this.accountIdExtractor = urlAccountIdExtractor;
    }


    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            handleAuthenticationRequest(httpServletRequest, httpServletResponse, filterChain);
        }
    }

    private void handleAuthenticationRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws IOException {
        String token = null;

        if (httpServletRequest.getHeader(X_AUTH_TOKEN) != null) {
            token = httpServletRequest.getHeader(X_AUTH_TOKEN);
        }

        //Rewrite headers to include only the username, no subs or quality at this time..
        String username = (httpServletRequest.getHeader(X_AUTH_USER_NAME) != null
                ? httpServletRequest.getHeader(X_AUTH_USER_NAME).split(";")[0]
                : null);

        String accountId = null;
        if (httpServletRequest.getHeader(X_AUTH_TENANT_ID) != null) {
            accountId = httpServletRequest.getHeader(X_AUTH_TENANT_ID);
        }

        String authorization = null;
        if (httpServletRequest.getHeader(AUTHORIZATION_HEADER) != null) {
            authorization = httpServletRequest.getHeader(AUTHORIZATION_HEADER);
        }

        try {
            if (username != null && accountId != null && token != null && authorization != null) {
                String decoded = Base64.decode(authorization.split(" ")[1]);
                if (decoded.equals(configuration.getString(PublicApiServiceConfigurationKeys.basic_auth_user)
                        + ":" + configuration.getString(PublicApiServiceConfigurationKeys.basic_auth_key))) {
                    HeadersRequestWrapper enhancedHttpRequest = new HeadersRequestWrapper(httpServletRequest);
                    enhancedHttpRequest.overideHeader(X_AUTH_USER_NAME);
                    enhancedHttpRequest.addHeader(X_AUTH_USER_NAME, username);
                    LOG.info(String.format("Request successfully authenticated, passing control to the servlet. Account: %s Token: %s Username: %s", accountId, token, username));
                    filterChain.doFilter(enhancedHttpRequest, httpServletResponse);
                    return;
                }
            } else if (httpServletRequest.getRequestURL().toString().contains("application.wadl")) {
                //TODO:Handle un-authorized access here when we use query param for wadl
                handleWadlRequest(httpServletRequest, httpServletResponse);
            } else {
                LOG.debug("Not a WADL nor Repose request.. attempt to validate the user with provided credentials");
                handleInternalAuthenticationRequest(httpServletRequest, httpServletResponse, filterChain);
            }
        } catch (RuntimeException e) {
            handleErrorReposnse(httpServletRequest, httpServletResponse, 500, e);
        } catch (ServletException e) {
            handleErrorReposnse(httpServletRequest, httpServletResponse, 500, e);
        } catch (PaddingException e) {
            handleErrorReposnse(httpServletRequest, httpServletResponse, 401, e);
        } catch (UnsupportedEncodingException e) {
            handleErrorReposnse(httpServletRequest, httpServletResponse, 401, e);
        } catch (IOException e) {
            handleErrorReposnse(httpServletRequest, httpServletResponse, 500, e);
        }
    }

    private void handleInternalAuthenticationRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws IOException, ServletException {
        String INVALID_TOKEN_MESSAGE = "Invalid authentication credentials. Please review request and try again with valid credentials";
        String AUTH_FAULT_MESSAGE = "There was an error while authenticating, please contact support.";
        String authToken = httpServletRequest.getHeader("X-AUTH-TOKEN");
        String MISSING_TOKEN_MESSAGE = "Missing authentication token.";
        String username = null;
        Integer accountId;
        int purged;

        purged = userCache.cleanExpiredByCount(); // Prevent unchecked entries from Living forever
        if (purged > 0) LOG.debug(String.format("cleaning auth userCache: purged %d stale entries", purged));

        if (authToken == null || authToken.isEmpty()) {
            sendUnauthorizedResponse(httpServletRequest, httpServletResponse, MISSING_TOKEN_MESSAGE);
            return;
        }

        try {
            accountId = accountIdExtractor.getAccountId(httpServletRequest.getRequestURL().toString());
        } catch (MalformedUrlException exception) {
            handleErrorReposnse(httpServletRequest, httpServletResponse, 404, exception);
            return;
        }

        try {
            LOG.debug(String.format("Before calling validate on account: %s with token: %s", accountId, authToken));
            String accountStr = String.format("%d", accountId);
            CacheEntry<AuthInfo> ce = userCache.getEntry(accountStr);
            AuthInfo authInfo = null;

            if (ce == null || ce.isExpired()) {
                userCache.remove(accountStr);
            } else {
                authInfo = ce.getVal();
                LOG.debug(String.format("Cache hit %s expires in %d secs", accountStr, ce.expiresIn()));
            }

            if (authInfo == null || !authInfo.getAuthToken().equals(authToken)) {
                LOG.info(String.format("Attempting to contact the auth service for account %s with token: %s", accountId, authToken));
                username = authTokenValidator.validate(authToken, String.valueOf(accountId)).getUser().getName();
                if (username == null) {
                    sendUnauthorizedResponse(httpServletRequest, httpServletResponse, INVALID_TOKEN_MESSAGE);
                    return;
                }

                LOG.info(String.format("Successfully retrieved users info from the auth service for account: %s with token: %s returned username: %s", accountId, authToken, username));
                authInfo = new AuthInfo(username, authToken);

                LOG.debug(String.format("insert %s-%s-%s into userCache", accountStr, authToken, username));
                userCache.put(accountStr, authInfo);
            } else {
                username = authInfo.getUserName();
            }
        } catch (IdentityFault kex) {
            String exceptMsg = getExtendedStackTrace(kex);
            if (kex.code == 401 || kex.code == 404) {
                LOG.error(String.format("Error while authenticating user %s-%s-%s: ERROR CODE: %d Message: %s Full-Stack: %s\n", accountId, authToken, username, kex.code, kex.message, exceptMsg));
                sendUnauthorizedResponse(httpServletRequest, httpServletResponse, INVALID_TOKEN_MESSAGE);
                return;
            } else {
                LOG.error(String.format("Error while authenticating user %s-%s-%s: ERROR CODE: %d Message: %s Details: %s Full-Stack: %s\n", accountId, authToken, username, kex.code, kex.message, kex.details, exceptMsg));
                sendUnauthorizedResponse(httpServletRequest, httpServletResponse, AUTH_FAULT_MESSAGE);
                return;
            }
        } catch (Exception e) {
            String exceptMsg = getExtendedStackTrace(e);
            LOG.error(String.format("Error while authenticating user %s-%s-%s:%s\n", accountId, authToken, username, exceptMsg));
            httpServletResponse.sendError(500, e.getMessage());
            return;
        }

        HeadersRequestWrapper enhancedHttpRequest = new HeadersRequestWrapper(httpServletRequest);
        enhancedHttpRequest.overideHeader(X_AUTH_USER_NAME);
        enhancedHttpRequest.addHeader(X_AUTH_USER_NAME, username);

        try {
            LOG.info(String.format("Request successfully authenticated, passing control to the servlet. Account: %s Token: %s Username: %s", accountId, authToken, username));
            filterChain.doFilter(enhancedHttpRequest, httpServletResponse);
            return;
        } catch (RuntimeException e) {
            handleErrorReposnse(httpServletRequest, httpServletResponse, 500, e);
        }
    }

    private void handleWadlRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        //Temp for fix in Repose to handle query params horrible things happen here
        LOG.info("WADL request, forwarding to CXF to produce the WADL.");
        if (httpServletRequest.getRequestURL().toString().contains("application.wadl")
                || httpServletRequest.getQueryString().contains("wadl")) {
            HeadersRequestWrapper enhancedHttpRequest = new HeadersRequestWrapper(httpServletRequest);
            String root = httpServletRequest.getRequestURI().split("/application.wadl")[0];
            RequestDispatcher dispatcher = enhancedHttpRequest.getRequestDispatcher(
                    "/00000/loadbalancers?_wadl"); //have to forward to resource other then root...
            dispatcher.forward(enhancedHttpRequest, httpServletResponse);
        }
    }

    private void handleErrorReposnse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, int errorCode, Exception e) throws IOException {
        final String UNEXPECTED = "Something unexpected happened. Please contact support.";
        final String UNAUTHENTICATED = "User not authenticated, please retry the request with valid auth credentials. ";

        if (errorCode == 500) {
            String exceptMsg = getExtendedStackTrace(e);
            LOG.error(String.format("Error in filterChain:%s\n", exceptMsg));
            httpServletResponse.sendError(errorCode, UNEXPECTED);
        } else if (errorCode == 401) {
            LOG.error(String.format("Error in filterChain:%s\n", e.getLocalizedMessage()));
            sendUnauthorizedResponse(httpServletRequest, httpServletResponse, UNAUTHENTICATED);
        } else {
            LOG.error(String.format("Error in filterChain:%s\n", e.getLocalizedMessage()));
            httpServletResponse.sendError(errorCode, e.getMessage());
        }
    }

    private void sendUnauthorizedResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String message) throws IOException {
        String contentType = accountIdExtractor.getContentType(httpServletRequest.getRequestURL().toString());

        LoadBalancerFault unauthorized = new LoadBalancerFault();
        unauthorized.setCode(401);
        unauthorized.setMessage(message);
        httpServletResponse.setStatus(SC_UNAUTHORIZED);

        if (contentType.equals("xml") || !contentType.equals("json") && httpServletRequest.getContentType() != null && httpServletRequest.getContentType().equals("application/xml")) {
            try {
                httpServletResponse.setContentType("application/xml; charset=UTF-8");
                Marshaller marshaller = JAXBContext.newInstance(unauthorized.getClass()).createMarshaller();
                marshaller.marshal(unauthorized, httpServletResponse.getWriter());
            } catch (JAXBException e) {
                String ErrorMsg = getExtendedStackTrace(e);
                LOG.error("Marshalling failed", e);
                httpServletResponse.sendError(SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            httpServletResponse.setContentType("application/json; charset=UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            AnnotationIntrospector introspector = new JacksonAnnotationIntrospector();
            mapper.getDeserializationConfig().setAnnotationIntrospector(introspector);
            mapper.getSerializationConfig().setAnnotationIntrospector(introspector);
            mapper.writeValue(httpServletResponse.getWriter(), unauthorized);
        }
    }

    public void startConfig() {
        //Init
    }

    public void setUserCache(SimpleCache userCache) {
        this.userCache = userCache;
    }

    public SimpleCache getUserCache() {
        return userCache;
    }

    public void setConfiguration(RestApiConfiguration configuration) {
        this.configuration = configuration;
    }

    public RestApiConfiguration getConfiguration() {
        return configuration;
    }
}