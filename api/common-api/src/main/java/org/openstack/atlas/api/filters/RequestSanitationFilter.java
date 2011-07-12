package org.openstack.atlas.api.filters;

import org.openstack.atlas.api.filters.wrappers.HeadersRequestWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;

public class RequestSanitationFilter implements Filter {

    private final Log LOG = LogFactory.getLog(RequestSanitationFilter.class);
    
    private FilterConfig filterConfig = null;
    private static final String DEFAULT_ACCEPT_HEADER = "application/json";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HeadersRequestWrapper enhancedHttpRequest = new HeadersRequestWrapper(httpServletRequest);

            if (httpServletRequest.getHeader(ACCEPT) == null || httpServletRequest.getHeader(ACCEPT).equals("")) {
                enhancedHttpRequest.overideHeader(ACCEPT);
                enhancedHttpRequest.addHeader(ACCEPT, DEFAULT_ACCEPT_HEADER);
            } else {
                // TODO: Validate accept header? How?
            }

            filterChain.doFilter(enhancedHttpRequest, servletResponse);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }
}
