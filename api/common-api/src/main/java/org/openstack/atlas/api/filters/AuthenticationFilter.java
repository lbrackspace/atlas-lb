package org.openstack.atlas.api.filters;

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

import static org.openstack.atlas.api.filters.helpers.StringUtilities.getExtendedStackTrace;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

public class AuthenticationFilter implements Filter {

    private final Log LOG = LogFactory.getLog(AuthenticationFilter.class);       

    private FilterConfig filterConfig = null;
    private AuthTokenValidator authTokenValidator;
    private UrlAccountIdExtractor urlAccountIdExtractor;
    private CacheRepository<Integer, AuthInfo> authenticatedUsers;
    private final String X_AUTH_USER_NAME = "X-PP-User";
    private String X_AUTH_WADL = "wadl";
    private final Integer CACHE_TTL_SECONDS = 1800;

    public AuthenticationFilter(AuthTokenValidator authTokenValidator, UrlAccountIdExtractor urlAccountIdExtractor) {
        this.authTokenValidator = authTokenValidator;
        this.urlAccountIdExtractor = urlAccountIdExtractor;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            Integer accountId;
            String userName;
            String MISSING_TOKEN_MESSAGE = "Missing authentication token.";
            String INVALID_TOKEN_MESSAGE = "Invalid authentication token. Please renew";
            String authToken = httpServletRequest.getHeader("X-AUTH-TOKEN");
            String wadlToken = httpServletRequest.getHeader("X-AUTH-WADL");
            String[] splitUrl =  httpServletRequest.getRequestURL().toString().split(httpServletRequest.getContextPath());

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

                LOG.info("Before calling validate ...");
                AuthInfo authInfo = authenticatedUsers.get(accountId);
                if (authInfo == null || !authInfo.getAuthToken().equals(authToken)) {
                    if (!authTokenValidator.validate(accountId, authToken)) {
                        sendUnauthorizedResponse(httpServletRequest, httpServletResponse, INVALID_TOKEN_MESSAGE);
                        return;
                    }
                    userName = authTokenValidator.getUserName(accountId, authToken);
                    authInfo = new AuthInfo(userName, authToken);
                    authenticatedUsers.put(accountId, authInfo, CACHE_TTL_SECONDS);
                } else {
                    userName = authInfo.getUserName();
                }
                
                HeadersRequestWrapper enhancedHttpRequest = new HeadersRequestWrapper(httpServletRequest);
                enhancedHttpRequest.overideHeader(X_AUTH_USER_NAME);
                enhancedHttpRequest.addHeader(X_AUTH_USER_NAME, userName);
                LOG.info("Request successfully authenticated, passing control to the servlet. Account: " + accountId);
                filterChain.doFilter(enhancedHttpRequest, servletResponse);
                return;
            } catch (XmlRpcException e) {
                String exceptMsg = getExtendedStackTrace(e);
                String errMsg = String.format("Error while authenticating user:%s\n", exceptMsg);
                LOG.error(errMsg);
                sendUnauthorizedResponse(httpServletRequest, httpServletResponse, INVALID_TOKEN_MESSAGE);
                return;
            } catch (RuntimeException e) {
                String exceptMsg = getExtendedStackTrace(e);
                String errMsg = String.format("Error while authenticating user:%s\n", exceptMsg);
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

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void destroy() {
        this.filterConfig = null;
    }

    public void setAuthenticatedUsers(CacheRepository<Integer, AuthInfo> authenticatedUsers) {
        this.authenticatedUsers = authenticatedUsers;
    }
}
