package org.openstack.atlas.api.filters;

import org.openstack.atlas.api.filters.helpers.AcceptTypes;
import org.openstack.atlas.api.filters.helpers.MediaType;
import org.openstack.atlas.api.filters.helpers.XmlValidationExceptionHandler;
import org.openstack.atlas.api.filters.wrappers.BufferedRequestWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.util.debug.Debug;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class XmlValidationFilter extends ValidationFilter {

    private final Log LOG = LogFactory.getLog(XmlValidationFilter.class);

    @Override
    public void init(FilterConfig config) throws ServletException {
        this.setConfig(config);
    }

    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain fc) throws IOException, ServletException {
        XmlValidationExceptionHandler errHandler;
        Object somePojo;
        HttpServletRequest hreq = (HttpServletRequest) sreq;
        HttpServletResponse hresp = (HttpServletResponse) sresp;
        String accept = hreq.getHeader("Accept");
        BufferedRequestWrapper breq = new BufferedRequestWrapper(hreq);
        String body = readFromInputStream(breq.getInputStream());
        String method = hreq.getMethod();
        MediaType contentMedia = MediaType.newInstance(hreq.getContentType());
        String overideAccept;

        AcceptTypes ats = AcceptTypes.getPrefferedAcceptTypes(accept);
        String acceptType = ats.findSuitableMediaType(JSON, XML);
        errHandler = new XmlValidationExceptionHandler();

        if (isHeaderTrue(hreq, "bypass-vxml")) {
            fc.doFilter(breq, sresp);
            return;
        }

        if (acceptType == null) {
            acceptType = JSON; // Default to Json
        }

        // Skip this filter if the request isn't XML
        if (!MediaType.matches(contentMedia, MediaType.newInstance(XML))) {
            fc.doFilter(breq, sresp);
            return;
        }

        if (method == null
                || containsMethod(method, "GET", "DELETE")
                || body.length() == 0) {
            fc.doFilter(breq, sresp);
            return;
        }

        overideAccept = overideAcceptType(hreq.getRequestURI());
        if (overideAccept != null) {
            acceptType = overideAccept;
        }

        try {
            somePojo = xml2pojo(body, pCtx, pSchema, errHandler);

            if (!errHandler.getErrList().isEmpty()) {
                if (acceptType.equalsIgnoreCase(XML)) {
                    sendXMLErrorResponse(hreq, hresp, BADREQ, errHandler.getErrList());
                    return;
                }
                if (acceptType.equalsIgnoreCase(JSON)) {
                    sendJSONErrorResponse(hreq, hresp, BADREQ, errHandler.getErrList());
                    return;
                }
            }
        } catch (Exception ex) {
            String exceptionString = Debug.getExtendedStackTrace(ex);
            LOG.error(String.format("Unknown Exception: %s\n", exceptionString));
            if (!errHandler.getErrList().isEmpty()) {
                if (acceptType.equalsIgnoreCase(XML)) {
                    sendXMLErrorResponse(hreq, hresp, BADREQ, errHandler.getErrList());
                    return;
                }
                if (acceptType.equalsIgnoreCase(JSON)) {
                    sendJSONErrorResponse(hreq, hresp, BADREQ, errHandler.getErrList());
                    return;
                }

            } else {
                if (acceptType.equalsIgnoreCase(XML)) {
                    sendXMLErrorResponse(hreq, hresp, BADREQ, String.format("Unknown Exception ex: %s\n", getExtendedStackTrace(ex)));
                    return;
                }
                if (acceptType.equalsIgnoreCase(JSON)) {
                    sendJSONErrorResponse(hreq, hresp, BADREQ, String.format("Unknown Exception ex: %s\n", getExtendedStackTrace(ex)));
                    return;
                }
            }
        }

        fc.doFilter(breq, sresp);
    }
}
