package org.openstack.atlas.api.mgmt.filters;

import org.openstack.atlas.api.filters.wrappers.HeadersRequestWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class HeaderEnhancerFilter implements Filter {
    private FilterConfig filterConfig = null;
    private static final Log LOG =  LogFactory.getLog(HeaderEnhancerFilter.class);
    private static final String X_AUTH_USER_NAME = "X-PP-User";
    private static final String defaultUserName = "OpenStack Cloud";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

            // TODO: When management auth is ready make sure to properly set user name header.
            HeadersRequestWrapper enhancedHttpRequest = new HeadersRequestWrapper(httpServletRequest);
            enhancedHttpRequest.overideHeader(X_AUTH_USER_NAME);
            enhancedHttpRequest.addHeader(X_AUTH_USER_NAME, defaultUserName);
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
