package org.openstack.atlas.api.mgmt.filters;

import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.api.filters.helpers.AcceptTypes;
import org.openstack.atlas.api.filters.wrappers.HeadersRequestWrapper;
import org.openstack.atlas.api.mgmt.filters.helpers.HttpHeadersTools;
import org.openstack.atlas.api.mgmt.filters.helpers.XmlJsonConfig;
import org.openstack.atlas.api.mgmt.helpers.LDAPTools.MossoAuth;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.NamingException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.openstack.atlas.api.filters.helpers.StringUtilities.getExtendedStackTrace;

public class ManageAuthFilter implements Filter {

    private final Log LOG = LogFactory.getLog(ManageAuthFilter.class);
    private static final String XML = "application/xml";
    private static final String JSON = "application/json";
    private MossoAuth mossoAuth;
    private FilterConfig config = null;
    private XmlJsonConfig xmlJsonConfig;
    private static final BadRequest unAuthorized;
    private static final BadRequest requiresAuth;
    private static final BadRequest invalidAuth;
    private static final int SC_UNAUTHORIZED = HttpServletResponse.SC_UNAUTHORIZED;
    private static final String LDAPGROUPS = "LDAPGroups";
    private static final String LDAPUSER = "LDAPUser";
    private static final Pattern jsonUriPattern = Pattern.compile(".*\\.json$", Pattern.CASE_INSENSITIVE);
    private static final Pattern xmlUriPattern = Pattern.compile(".*\\.xml$", Pattern.CASE_INSENSITIVE);

    static {
        invalidAuth = new BadRequest();
        invalidAuth.setCode(SC_UNAUTHORIZED);
        invalidAuth.setMessage("Your Authroization header was improperly formated");

        requiresAuth = new BadRequest();
        requiresAuth.setCode(SC_UNAUTHORIZED);
        requiresAuth.setMessage("You must use BASIC HTTP auth");

        unAuthorized = new BadRequest();
        unAuthorized.setCode(SC_UNAUTHORIZED);
        unAuthorized.setMessage("eDir bind failed");
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {
        this.setConfig(getConfig());
    }

    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain fc) throws IOException, ServletException {
        String user;
        String password;
        Set<String> groups;
        HttpServletRequest hreq = (HttpServletRequest) sreq;
        HttpServletResponse hresp = (HttpServletResponse) sresp;
        Enumeration<String> forcedRolesHeaders;
        String accept = hreq.getHeader("Accept");
        AcceptTypes ats = AcceptTypes.getPrefferedAcceptTypes(accept);
        String acceptType = ats.findSuitableMediaType(JSON, XML);
        HttpHeadersTools httpTools = new HttpHeadersTools(hreq, hresp);

        LOG.info("Requesting URL: " + hreq.getRequestURI());

        String[] splitUrl = hreq.getRequestURL().toString().split(hreq.getContextPath());
        if (hreq.getRequestURL().toString().equals(splitUrl[0] + hreq.getContextPath() + "/application.wadl")) {
            RequestDispatcher dispatcher = hreq.getRequestDispatcher(hreq.getContextPath() + "/?_wadl");
            dispatcher.forward(sreq, sresp);
            return;
        }

        if (httpTools.isHeaderTrue("BYPASS-AUTH")) {
            user = "BYPASS-AUTH";
            groups = new HashSet<String>();
            LOG.info("Bypassed AUTH.... ");
            forcedRolesHeaders = hreq.getHeaders("FORCEROLES");

            if (mossoAuth.getConfig().isAllowforcedRole() && forcedRolesHeaders != null) {
                while (forcedRolesHeaders.hasMoreElements()) {
                    String role = forcedRolesHeaders.nextElement();
                    Map<String, HashSet<String>> roleMap = mossoAuth.getConfig().getRoles();
                    if (roleMap.containsKey(role) && roleMap.get(role).iterator().hasNext()) {
                        String groupToForce = roleMap.get(role).iterator().next();
                        groups.add(groupToForce);
                    }
                }
            }
            HeadersRequestWrapper nreq = new HeadersRequestWrapper(hreq);
            nreq.overideHeader(LDAPGROUPS);
            nreq.overideHeader(LDAPUSER);
            nreq.addHeader(LDAPGROUPS, HttpHeadersTools.set2commastr(groups));
            nreq.addHeader(LDAPUSER, user);
            fc.doFilter(sreq, sresp);
            return;
        }

        if (acceptType == null) {
            acceptType = JSON;
        }

        if (overideAcceptType(acceptType) != null) {
            acceptType = overideAcceptType(acceptType);
        }

        if (!httpTools.isBasicAuth()) {
            hresp.setHeader("WWW-Authenticate", "BASIC realm=\"management\"");
            sendResponse(hresp, acceptType, requiresAuth, SC_UNAUTHORIZED);
            return;
        }

        if (!httpTools.isValidAuth()) {
            hresp.setHeader("WWW-Authenticate", "BASIC realm=\"management\"");
            sendResponse(hresp, acceptType, invalidAuth, SC_UNAUTHORIZED);
            return;
        }

        user = httpTools.getBasicUser();
        password = httpTools.getBasicPassword();

        LOG.info("Basic User: " + user);

        if (!mossoAuth.testAuth(user, password)) {
            sendResponse(hresp, acceptType, unAuthorized, SC_UNAUTHORIZED);
            return;
        }
        try {
            groups = mossoAuth.getGroups(user, password);
        } catch (NamingException ex) {
            LOG.error(ex);
            throw new ServletException("UNABLE to fetch posixgroups from LDAP", ex);
        }
        forcedRolesHeaders = hreq.getHeaders("FORCEROLES");

        if (mossoAuth.getConfig().isAllowforcedRole() && forcedRolesHeaders != null) {
            while (forcedRolesHeaders.hasMoreElements()) {
                String role = forcedRolesHeaders.nextElement();
                Map<String, HashSet<String>> roleMap = mossoAuth.getConfig().getRoles();
                if (roleMap.containsKey(role) && roleMap.get(role).iterator().hasNext()) {
                    String groupToForce = roleMap.get(role).iterator().next();
                    groups.add(groupToForce);
                }
            }
        }
        HeadersRequestWrapper nreq = new HeadersRequestWrapper(hreq);
        nreq.overideHeader(LDAPGROUPS);
        nreq.overideHeader(LDAPUSER);
        nreq.addHeader(LDAPGROUPS, HttpHeadersTools.set2commastr(groups));
        nreq.addHeader(LDAPUSER, user);
        nop();
        try {
            fc.doFilter(nreq, sresp);
        } catch (Exception ex) {
            String msg = getExtendedStackTrace(ex);
            LOG.error(msg, ex);
            nop();
        }

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

    public void setMossoAuth(MossoAuth mossoAuth) {
        this.mossoAuth = mossoAuth;
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

    public static class AuthState {
    }
}
