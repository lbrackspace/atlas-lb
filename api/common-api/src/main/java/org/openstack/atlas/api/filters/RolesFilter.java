package org.openstack.atlas.api.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.openstack.atlas.api.filters.helpers.StringUtilities.getExtendedStackTrace;

public class RolesFilter implements Filter {
    private final Log LOG = LogFactory.getLog(RolesFilter.class);

    private FilterConfig filterConfig = null;

    private static final String X_ROLES = "X-Roles";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

            try {
                //TODO: Extract roles... V1-B-34715 #2 RBAC ..mgmt also at some point

                filterChain.doFilter(servletRequest, servletResponse);
            } catch (Exception e) {
                String exceptMsg = getExtendedStackTrace(e);
                LOG.error(String.format("Error in filterChain:%s\n", exceptMsg));
                httpServletResponse.sendError(500, "Something unexpected happened. Please contact support.");
            }

        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }
}
