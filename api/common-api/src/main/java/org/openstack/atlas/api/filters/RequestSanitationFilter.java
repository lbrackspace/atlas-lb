package org.openstack.atlas.api.filters;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.filters.wrappers.HeadersRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;

public class RequestSanitationFilter implements Filter {

    private final Log LOG = LogFactory.getLog(RequestSanitationFilter.class);

    private FilterConfig filterConfig = null;
    private static final String DEFAULT_ACCEPT_HEADER = "application/json";
    private static final String X_PP_GROUPS = "x-pp-groups";
    private static final String X_WADL = "x-wadl";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            HeadersRequestWrapper enhancedHttpRequest = new HeadersRequestWrapper(httpServletRequest);

            String uri = httpServletRequest.getRequestURL().toString();
            if (verifyWADLRequest(uri)) {
                LOG.debug("WADL request detected. ");
                enhancedHttpRequest.addHeader(X_WADL, "true");
            } else {
                enhancedHttpRequest.addHeader(X_WADL, "false");
            }

            if (httpServletRequest.getHeader(ACCEPT) == null
                    || httpServletRequest.getHeader(ACCEPT).equals("*/*")
                    || httpServletRequest.getHeader(ACCEPT).equals("")) {
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

    private boolean verifyWADLRequest(String uri) {
        //Repose will let this request through
        //based on regex, we want to tag and
        //track it through filters..
        final String[] cases = {
                ".wadl",
                "application.wadl",
                "wadl",
                "?wadl",
                "?_wadl"
        };
        return StringUtils.indexOfAny(uri, cases) != -1;
    }
}
