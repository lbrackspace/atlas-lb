package org.openstack.atlas.api.filters;

import org.openstack.atlas.api.filters.helpers.AcceptTypes;
import org.openstack.atlas.api.filters.helpers.MediaType;
import org.openstack.atlas.api.filters.wrappers.BufferedRequestWrapper;
import org.openstack.atlas.api.helpers.reflection.UriClassDiscover;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JsonValidationFilter extends ValidationFilter {

    private final Log LOG = LogFactory.getLog(JsonValidationFilter.class);

    @Override
    public void init(FilterConfig config) throws ServletException {
        this.setConfig(config);
    }

    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain fc) throws IOException, ServletException {
        Class classForUri;
        Object somePojo = null;
        HttpServletRequest hreq = (HttpServletRequest) sreq;
        HttpServletResponse hresp = (HttpServletResponse) sresp;
        String accept = hreq.getHeader("Accept");
        BufferedRequestWrapper breq = new BufferedRequestWrapper(hreq);
        String body = readFromInputStream(breq.getInputStream());
        String method = hreq.getMethod();
        MediaType contentMedia = MediaType.newInstance(hreq.getContentType());
        String overrideAccept;

        AcceptTypes ats = AcceptTypes.getPrefferedAcceptTypes(accept);
        String acceptType = ats.findSuitableMediaType(JSON, XML);

        // TODO: Remove for production
        if (isHeaderTrue(hreq, "bypass-vjson")) {
            fc.doFilter(breq, sresp);
            return;
        }

        if (acceptType == null) {
            acceptType = JSON; // Default to Json
        }

        // Skip this filter if the request isn't XML
        if (!MediaType.matches(contentMedia, MediaType.newInstance(JSON))) {
            fc.doFilter(breq, sresp);
            return;
        }

        if (method == null
                || containsMethod(method, "GET", "DELETE")
                || body.length() == 0) {
            fc.doFilter(breq, sresp);
            return;
        }

        overrideAccept = overideAcceptType(hreq.getRequestURI());
        if (overrideAccept != null) {
            acceptType = overrideAccept;
        }

        classForUri = UriClassDiscover.getClassForUri(hreq.getRequestURI());
        if (classForUri == null) {
            String errMsg = "JsonValidator could not determine the class to validate against: URI="
                    + hreq.getRequestURI() + " : validation skipped.";
            LOG.warn(errMsg);
            fc.doFilter(breq, sresp);
            return;
        }
        try {
            somePojo = mapper.readValue(body, classForUri);
        } catch (Exception ex) {
            String errMsg = "JSON does not match the expected schema";
            String logMsg = getExtendedStackTrace(ex);

            LOG.error(logMsg);

            if (acceptType.equalsIgnoreCase(XML)) {
                sendXMLErrorResponse(hreq, hresp, BADREQ, errMsg);
                return;
            }
            if (acceptType.equalsIgnoreCase(JSON)) {
                sendJSONErrorResponse(hreq, hresp, BADREQ, errMsg);
                return;
            }
        }
        fc.doFilter(breq, sresp);
    }

    @Override
    public void destroy() {
    }
}
