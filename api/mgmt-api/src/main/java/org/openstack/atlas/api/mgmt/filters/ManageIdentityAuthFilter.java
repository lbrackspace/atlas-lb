package org.openstack.atlas.api.mgmt.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.filters.wrappers.HeadersRequestWrapper;
import org.openstack.atlas.api.mgmt.filters.helpers.XmlJsonConfig;
import org.openstack.atlas.api.mgmt.helpers.LDAPTools.IdentityAuth;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManageIdentityAuthFilter implements Filter {
    private final Log LOG = LogFactory.getLog(ManageIdentityAuthFilter.class);

    private static final Pattern jsonUriPattern = Pattern.compile(".*\\.json$", Pattern.CASE_INSENSITIVE);
    private static final Pattern xmlUriPattern = Pattern.compile(".*\\.xml$", Pattern.CASE_INSENSITIVE);

    private static final String XML = "application/xml";
    private static final String JSON = "application/json";

    private IdentityAuth identityAuth;
    private FilterConfig config = null;
    private XmlJsonConfig xmlJsonConfig;

    private final String rolesHeader = "X-Roles";
    private final String X_AUTH_USER_NAME = "X-PP-User";
    private final String X_PP_GROUPS = "X-PP-Groups";

    @Override
    public void init(FilterConfig fc) throws ServletException {
        this.setConfig(getConfig());
    }

    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain fc) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) sreq;

        String username = (httpServletRequest.getHeader(X_AUTH_USER_NAME) != null
                ? httpServletRequest.getHeader(X_AUTH_USER_NAME).split(";")[0]
                : null);

        String roles = httpServletRequest.getHeader(rolesHeader);
        if (roles == null || roles.isEmpty()) {
        }

        HeadersRequestWrapper enhancedHttpRequest = new HeadersRequestWrapper(httpServletRequest);
        enhancedHttpRequest.overideHeader(X_AUTH_USER_NAME);
        enhancedHttpRequest.addHeader(X_AUTH_USER_NAME, username);
        fc.doFilter(enhancedHttpRequest, sresp);
        return;
    }

    public void startConfig() {
    }

    private String pojo2xml(Object pojo) throws JAXBException {
        String result;
        StringWriter sw = new StringWriter();
        Marshaller m = this.xmlJsonConfig.getfCtx().createMarshaller();
        m.setSchema(this.xmlJsonConfig.getfSchema());
        m.marshal(pojo, sw);
        result = sw.toString();
        return result;
    }

    private String pojo2json(Object pojo) throws IOException {
        return this.xmlJsonConfig.getMapper().writeValueAsString(pojo);
    }

    private void sendResponse(HttpServletResponse hresp, String acceptType, Object pojo, int status) throws IOException, ServletException {
        String content = "";
        String contentType;
        PrintWriter pw;
        pw = hresp.getWriter();
        contentType = String.format("%s; charset=UTF-8", acceptType);
        if (acceptType.equals(XML)) {
            try {
                content = pojo2xml(pojo);
            } catch (JAXBException ex) {
                throw new ServletException(ex);
            }
        } else if (acceptType.equals(JSON)) {
            content = pojo2json(pojo);
        }
        hresp.setStatus(status);
        hresp.setContentType(contentType);
        hresp.setContentLength(content.length());
        pw.write(content);
        pw.flush();
        return;
    }

    private String overideAcceptType(String uri) {
        String out = null;
        Matcher m;
        m = xmlUriPattern.matcher(uri);
        if (m.find()) {
            return XML;
        }
        m = jsonUriPattern.matcher(uri);
        if (m.find()) {
            return JSON;
        }
        return out;
    }

    @Override
    public void destroy() {
    }

    private void nop() {
    }

    public void setIdentityAuth(IdentityAuth identityAuth) {
        this.identityAuth = identityAuth;
    }

    public FilterConfig getConfig() {
        return config;
    }

    public void setConfig(FilterConfig config) {
        this.config = config;
    }

    public void setXmlJsonConfig(XmlJsonConfig xmlJsonConfig) {
        this.xmlJsonConfig = xmlJsonConfig;
    }
}
