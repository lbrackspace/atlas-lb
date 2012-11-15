package org.openstack.atlas.api.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.openstack.atlas.api.filters.wrappers.HeadersRequestWrapper;
import org.openstack.atlas.api.helpers.UrlAccountIdExtractor;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.LoadBalancerFault;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.openstack.atlas.api.filters.helpers.StringUtilities.getExtendedStackTrace;

public class AuthenticationFilter implements Filter {
    private final Log LOG = LogFactory.getLog(AuthenticationFilter.class);

    private final String X_AUTH_TOKEN = "X-Auth-Token";
    private final String X_AUTH_USER_NAME = "X-PP-User";
    private final String X_AUTH_TENANT_ID = "X-Tenant-Name";

    private final UrlAccountIdExtractor accountIdExtractor;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Not implemented...
    }

    @Override
    public void destroy() {
        // Not implemented...
    }

    public AuthenticationFilter(UrlAccountIdExtractor accountIdExtractor) {
        this.accountIdExtractor = accountIdExtractor;
    }


    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

            String token = httpServletRequest.getHeader(X_AUTH_TOKEN);
            String username = (httpServletRequest.getHeader(X_AUTH_USER_NAME) != null
                    ? httpServletRequest.getHeader(X_AUTH_USER_NAME).split(";")[0]
                    : null); //We are not using the 'quality' portion... 'username;q=1.0'
            String accountId = httpServletRequest.getHeader(X_AUTH_TENANT_ID);

            //...safety net?
            if (accountId == null || username == null || token == null) {
                if (httpServletRequest.getHeader("x-wadl").equals("false")) {
                sendUnauthorizedResponse(httpServletRequest, httpServletResponse, "Unauthorized user access, please attempt request again.");
                }
            }

            try {
                if (username != null) {
                    //Rewrite headers to include only the username, no subs or quality at this time..
                    HeadersRequestWrapper enhancedHttpRequest = new HeadersRequestWrapper(httpServletRequest);
                    enhancedHttpRequest.overideHeader(X_AUTH_USER_NAME);
                    enhancedHttpRequest.addHeader(X_AUTH_USER_NAME, username);
                    LOG.info(String.format("Request successfully authenticated, passing control to the servlet. Account: %s Token: %s Username: %s", accountId, token, username));
                    filterChain.doFilter(enhancedHttpRequest, servletResponse);
                    return;
                }
            } catch (RuntimeException e) {
                String exceptMsg = getExtendedStackTrace(e);
                LOG.error(String.format("Error in filterChain:%s\n", exceptMsg));
                httpServletResponse.sendError(500, "Something unexpected happened. Please contact support.");
                return;
            }
        }

        LOG.info("Request authentication failed, passing control to the servlet.");
        filterChain.doFilter(servletRequest, servletResponse);
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
}
