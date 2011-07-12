package org.openstack.atlas.api.filters;

import org.openstack.atlas.service.domain.entities.GroupRateLimit;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.GroupRepository;
import org.openstack.atlas.api.exceptions.MalformedUrlException;
import org.openstack.atlas.api.filters.helpers.StringUtilities;
import org.openstack.atlas.api.filters.wrappers.HeadersRequestWrapper;
import org.openstack.atlas.api.helpers.UrlAccountIdExtractor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroupRateLimitFilter implements Filter {

    private final Log LOG = LogFactory.getLog(GroupRateLimitFilter.class);
    
    private FilterConfig filterConfig = null;
    private UrlAccountIdExtractor urlAccountIdExtractor;
    private GroupRepository groupRepository;
    private static final String X_PP_GROUPS = "X-PP-Groups";

    public GroupRateLimitFilter(UrlAccountIdExtractor urlAccountIdExtractor) {
        this.urlAccountIdExtractor = urlAccountIdExtractor;
    }

    public void setGroupRepository(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            Integer accountId;

            try {
                accountId = urlAccountIdExtractor.getAccountId(httpServletRequest.getRequestURL().toString());
            } catch (MalformedUrlException exception) {
                httpServletResponse.sendError(404, exception.getMessage());
                return;
            }

            try {
                // TODO: Get groups via the cache if no cache then get via repository call?
                List<GroupRateLimit> groups = groupRepository.getByAccountId(accountId);
                HeadersRequestWrapper enhancedHttpRequest = new HeadersRequestWrapper(httpServletRequest);
                enhancedHttpRequest.overideHeader(X_PP_GROUPS);
                enhancedHttpRequest.addHeader(X_PP_GROUPS, StringUtilities.DelimitString(groupRateLimitsToGroupNames(groups), ","));
                filterChain.doFilter(enhancedHttpRequest, servletResponse);
            } catch (EntityNotFoundException e) {
                LOG.error("No rate limit groups found. Please add at least the default group via the loadbalancer management API.", e);
                filterChain.doFilter(servletRequest, servletResponse);
            }

        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }

    private List<String> groupRateLimitsToGroupNames(List<GroupRateLimit> groupRateLimits) {
        List<String> groupNames = new ArrayList<String>();

        for (GroupRateLimit groupRateLimit : groupRateLimits) {
            groupNames.add(groupRateLimit.getName());
        }

        return groupNames;
    }
}
