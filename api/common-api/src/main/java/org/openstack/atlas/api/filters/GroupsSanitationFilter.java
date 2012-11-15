package org.openstack.atlas.api.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.filters.helpers.StringUtilities;
import org.openstack.atlas.api.filters.wrappers.HeadersRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.openstack.atlas.api.filters.helpers.StringUtilities.getExtendedStackTrace;

public class GroupsSanitationFilter implements Filter {
    private final Log LOG = LogFactory.getLog(GroupsSanitationFilter.class);

    private FilterConfig filterConfig = null;

    private static final String X_PP_GROUPS = "X-PP-Groups";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

            List<String> sanitizedGroups;
            try {
                sanitizedGroups = sanitizeGroupsHeader(httpServletRequest.getHeaders(X_PP_GROUPS));
                if (!sanitizedGroups.isEmpty()) {
                    HeadersRequestWrapper enhancedHttpRequest = new HeadersRequestWrapper(httpServletRequest);
                    enhancedHttpRequest.overideHeader(X_PP_GROUPS);
                    enhancedHttpRequest.addHeader(X_PP_GROUPS, StringUtilities.DelimitString(sanitizedGroups, ","));
                    filterChain.doFilter(enhancedHttpRequest, servletResponse);
                    return;
                }
            } catch (Exception e) {
                String exceptMsg = getExtendedStackTrace(e);
                LOG.error(String.format("Error in filterChain:%s\n", exceptMsg));
                httpServletResponse.sendError(500, "Something unexpected happened. Please contact support.");
                return;
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }

    //Groups now come from auth, and  handled in Repose But need to clean them up a bit for us...
    private List<String> sanitizeGroupsHeader(Enumeration groups) {
        List<String> groupNames = new ArrayList<String>();

        if (groups != null) {
            while (groups.hasMoreElements()) {
                String group = (String) groups.nextElement();
                String[] groupIndividual = group.split(",");
                try {
                    for (int i = 0; i < groupIndividual.length; i++) {
                        groupNames.add(groupIndividual[i].split(";")[0]);
                    }
                } catch (Exception e) {
                    LOG.debug("No groups remain to parse, continue..."); //Ignore
                }
            }
        }
        return groupNames;
    }
}
