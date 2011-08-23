package org.openstack.atlas.api.filters;

import org.openstack.atlas.util.simplecache.CacheEntry;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.LoadBalancerFault;
import org.openstack.atlas.api.auth.AuthInfo;
import org.openstack.atlas.api.auth.AuthTokenValidator;
import org.openstack.atlas.api.caching.CacheRepository;
import org.openstack.atlas.api.exceptions.MalformedUrlException;
import org.openstack.atlas.api.filters.wrappers.HeadersRequestWrapper;
import org.openstack.atlas.api.helpers.UrlAccountIdExtractor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import org.openstack.atlas.util.simplecache.SimpleCache;
import static org.openstack.atlas.api.filters.helpers.StringUtilities.getExtendedStackTrace;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

public class AuthenticationFilter implements Filter {

    private final Log LOG = LogFactory.getLog(AuthenticationFilter.class);
    private FilterConfig filterConfig = null;
    private AuthTokenValidator authTokenValidator;
    private UrlAccountIdExtractor urlAccountIdExtractor;
    private SimpleCache<AuthInfo> userCache;
    private final String X_AUTH_USER_NAME = "X-PP-User";
    private String X_AUTH_WADL = "wadl";

    public AuthenticationFilter(AuthTokenValidator authTokenValidator, UrlAccountIdExtractor urlAccountIdExtractor) {
        this.authTokenValidator = authTokenValidator;
        this.urlAccountIdExtractor = urlAccountIdExtractor;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        int purged;
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            Integer accountId;
            String userName;
            String MISSING_TOKEN_MESSAGE = "Missing authentication token.";
            String INVALID_TOKEN_MESSAGE = "Invalid authentication token. Please renew";
            String authToken = httpServletRequest.getHeader("X-AUTH-TOKEN");
            String wadlToken = httpServletRequest.getHeader("X-AUTH-WADL");
            String[] splitUrl = httpServletRequest.getRequestURL().toString().split(httpServletRequest.getContextPath());
            purged = userCache.cleanExpiredByCount(); // Prevent unchecked entries from Living forever
            if (purged > 0) {
                LOG.debug(String.format("cleaning auth userCache: purged %d stale entries", purged));
            }
            //Eww hacks.. need to grab wadl from root
            if (httpServletRequest.getRequestURL().toString().equals(splitUrl[0] + httpServletRequest.getContextPath() + "/application.wadl")) {
                HeadersRequestWrapper enhancedHttpRequest = new HeadersRequestWrapper(httpServletRequest);
                enhancedHttpRequest.overideHeader(X_AUTH_WADL);
                enhancedHttpRequest.addHeader(X_AUTH_WADL, "wadl");
                RequestDispatcher dispatcher = enhancedHttpRequest.getRequestDispatcher("00000/loadbalancers/?_wadl");
                dispatcher.forward(enhancedHttpRequest, httpServletResponse);
                return;
            }

            //Eww Hacks....need to be able to get wadl from root resource...
            String urlQuery = httpServletRequest.getQueryString();
            String url = httpServletRequest.getRequestURL().toString() + "?" + urlQuery;
            if (wadlToken != null) {
                if (url.equals(splitUrl[0] + httpServletRequest.getContextPath() + "/?_wadl")) {
                    RequestDispatcher dispatcher = httpServletRequest.getRequestDispatcher("00000/loadbalancers/?_wadl");
                    dispatcher.forward(httpServletRequest, httpServletResponse);
                    return;
                } else if (url.equals(splitUrl[0] + httpServletRequest.getContextPath() + "00000/loadbalancers/?_wadl")) {
                    return;
                }
            }


            if (StringUtils.isBlank(authToken)) {
                sendUnauthorizedResponse(httpServletRequest, httpServletResponse, MISSING_TOKEN_MESSAGE);
                return;
            }

            try {
                accountId = urlAccountIdExtractor.getAccountId(httpServletRequest.getRequestURL().toString());
            } catch (MalformedUrlException exception) {
                httpServletResponse.sendError(404, exception.getMessage());
                return;
            }


            try {

                LOG.debug("Before calling validate ...");
                String accountStr = String.format("%d", accountId);
                CacheEntry<AuthInfo> ce = userCache.getEntry(accountStr);
                AuthInfo authInfo = null;
                if (ce == null || ce.isExpired()) {
                    userCache.remove(accountStr);
                    LOG.debug(String.format("calling auth XMLRPC server"));
                } else {
                    authInfo = ce.getVal();
                    LOG.debug(String.format("Cache hit %s expires in %d secs", accountStr, ce.expiresIn()));
                }
                if (authInfo == null || !authInfo.getAuthToken().equals(authToken)) {
                    if (!authTokenValidator.validate(accountId, authToken)) {
                        sendUnauthorizedResponse(httpServletRequest, httpServletResponse, INVALID_TOKEN_MESSAGE);
                        return;
                    }
                    userName = authTokenValidator.getUserName(accountId, authToken);
                    authInfo = new AuthInfo(userName, authToken);
                    userCache.put(accountStr, authInfo);
                    LOG.debug(String.format("insert %s into userCache", accountStr));
                } else {
                    userName = authInfo.getUserName();
                }
            } catch (XmlRpcException e) {
                String exceptMsg = getExtendedStackTrace(e);
                String errMsg = String.format("Error while authenticating user:%s\n", exceptMsg);
                LOG.error(errMsg);
                sendUnauthorizedResponse(httpServletRequest, httpServletResponse, INVALID_TOKEN_MESSAGE);
                return;
            }
            HeadersRequestWrapper enhancedHttpRequest = new HeadersRequestWrapper(httpServletRequest);
            enhancedHttpRequest.overideHeader(X_AUTH_USER_NAME);
            enhancedHttpRequest.addHeader(X_AUTH_USER_NAME, userName);
            LOG.info("Request successfully authenticated, passing control to the servlet. Account: " + accountId);
            try {
                filterChain.doFilter(enhancedHttpRequest, servletResponse);

                return;
            } catch (RuntimeException e) {
                String exceptMsg = getExtendedStackTrace(e);
                String errMsg = String.format("Error in filterChain:%s\n", exceptMsg);
                LOG.error(errMsg);
                httpServletResponse.sendError(500, "Something unexpected happened. Please contact support.");
                return;
            }
        }


        LOG.info("Request authentication failed, passing control to the servlet.");
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void sendUnauthorizedResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String message) throws IOException {
        String contentType = urlAccountIdExtractor.getContentType(httpServletRequest.getRequestURL().toString());

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

    private void handleException(HttpServletResponse httpServletResponse, Integer accountId, String authToken, Exception e) throws IOException {
        LOG.info("AcctId: " + accountId + "\nAuth Token: " + authToken + "\nMessage: " + e.getMessage());
        httpServletResponse.sendError(501, e.getMessage()); // For https://jira.mosso.com/browse/SITESLB-410
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }

    public void startConfig() {
    }

    public void setUserCache(SimpleCache<AuthInfo> userCache) {
        this.userCache = userCache;
    }
}
