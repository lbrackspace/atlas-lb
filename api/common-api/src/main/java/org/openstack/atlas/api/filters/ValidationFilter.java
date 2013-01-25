package org.openstack.atlas.api.filters;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.ValidationErrors;
import org.openstack.atlas.api.filters.helpers.MediaType;
import org.openstack.atlas.api.filters.helpers.XmlValidationExceptionHandler;
import org.openstack.atlas.api.filters.wrappers.BufferedRequestWrapper;
import org.openstack.atlas.api.helpers.JsonObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamReader;

import static org.openstack.atlas.api.filters.helpers.StringUtilities.logId;

public class ValidationFilter implements Filter {

    private final Log LOG = LogFactory.getLog(ValidationFilter.class);
    protected static final String XML = "application/xml";
    protected static final String JSON = "application/json";
    protected static final String VFAIL = "Validation Failure";
    protected static final int PAGESIZE = 4096;
    protected FilterConfig config = null;
    protected String pPkg;
    protected String pXSD;
    protected String fPkg;
    protected String fXSD;
    protected JAXBContext pCtx;
    protected JAXBContext fCtx;
    protected Schema pSchema;
    protected Schema fSchema;
    protected JsonObjectMapper mapper;
    public static final int BUFFSIZE = 16384;
    public static final int BADREQ = 400;
    public static final String dashline = "--------------------------------------\n";
    public static final Pattern jsonUriPattern = Pattern.compile(".*\\.json$", Pattern.CASE_INSENSITIVE);
    public static final Pattern xmlUriPattern = Pattern.compile(".*\\.xml$", Pattern.CASE_INSENSITIVE);

    @Override
    public void init(FilterConfig config) throws ServletException {
        this.setConfig(config);
    }

    @Override
    public void doFilter(ServletRequest sr, ServletResponse sr1, FilterChain fc) throws IOException, ServletException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void destroy() {
    }

    private void deleteme(ServletRequest sreq, ServletResponse sresp) throws IOException {
        HttpServletRequest hreq = (HttpServletRequest) sreq;
        HttpServletResponse hresp = (HttpServletResponse) sresp;
        String accept = hreq.getHeader("Accept");
        BufferedRequestWrapper breq = new BufferedRequestWrapper(hreq);
        String body = readFromInputStream(breq.getInputStream());
        String method = hreq.getMethod();
        MediaType contentMedia = MediaType.newInstance(hreq.getContentType());
        String overideAccept;
    }

    public boolean isHeaderTrue(HttpServletRequest hreq, String name) {
        boolean out = false;
        if (hreq.getHeader(name) == null) {
            return out;
        }
        if (hreq.getHeader(name).equalsIgnoreCase("true")) {
            out = true;
        }
        return out;
    }

    protected void sendXMLErrorResponse(HttpServletRequest req, HttpServletResponse resp, int status, List<String> errList) throws IOException {
        ValidationErrors vFault = new ValidationErrors();
        BadRequest badRequest = new BadRequest();
        badRequest.setValidationErrors(vFault);
        badRequest.setCode(BADREQ);
        badRequest.setMessage(VFAIL);
        PrintWriter writer = resp.getWriter();
        String result;
        try {
            vFault.getMessages().addAll(errList);
            resp.setStatus(status);
            resp.setContentType("application/xml; charset=UTF-8");
            writer = resp.getWriter();
            result = pojo2xml(badRequest, fCtx, fSchema);
            resp.setContentLength(result.length());
            writer.write(result);
            writer.flush();
        } catch (JAXBException ex) {
            String errMsg = jaxbParseExceptionToString(ex, "");
            LOG.error(errMsg);
            resp.sendError(BADREQ, "Bad Request");
        }
    }

    protected void sendJSONErrorResponse(HttpServletRequest req, HttpServletResponse resp, int status, List<String> errList) throws IOException {
        ValidationErrors vFault = new ValidationErrors();
        BadRequest badRequest = new BadRequest();
        badRequest.setValidationErrors(vFault);
        badRequest.setCode(BADREQ);
        badRequest.setMessage(VFAIL);
        PrintWriter writer = resp.getWriter();
        String result;
        vFault.getMessages().addAll(errList);
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        writer = resp.getWriter();
        result = mapper.writeValueAsString(vFault);
        resp.setContentLength(result.length());
        writer.write(result);
        writer.flush();
    }

    protected void sendJSONErrorResponse(HttpServletRequest req, HttpServletResponse resp, int status, String err) throws IOException {
        List<String> errList = new ArrayList<String>();
        errList.add(err);
        sendJSONErrorResponse(req, resp, status, errList);
    }

    protected void sendXMLErrorResponse(HttpServletRequest req, HttpServletResponse resp, int status, String err) throws IOException {
        List<String> errList = new ArrayList<String>();
        errList.add(err);
        sendXMLErrorResponse(req, resp, status, errList);
    }

    protected void startConfig() throws MalformedURLException, SAXException, JAXBException, IOException {
        logId("startConfig() ", this);
        this.pCtx = JAXBContext.newInstance(pPkg);
        this.fCtx = JAXBContext.newInstance(fPkg);
        SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        pSchema = sf.newSchema((new ClassPathResource(pXSD)).getURL());
        fSchema = sf.newSchema((new ClassPathResource(fXSD)).getURL());
        mapper = new JsonObjectMapper();
        mapper.init();
    }

    protected Object xml2pojo(String xml, JAXBContext ctx, Schema schema, XmlValidationExceptionHandler errHandler) throws JAXBException, UnsupportedEncodingException, IOException {
        Object out = null;
        XMLInputFactory xif = XMLInputFactory.newFactory();
        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        XMLStreamReader xsr;
        ByteArrayInputStream bytes = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        try {
            xsr = xif.createXMLStreamReader(bytes);
        } catch (Exception ex) {
            Logger.getLogger(ValidationFilter.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException("Could not create XMLStreamReader", ex);
        }
        Unmarshaller u = ctx.createUnmarshaller();
        u.setSchema(schema);
        if (errHandler != null) {
            u.setEventHandler(errHandler);
        }
        out = u.unmarshal(xsr);
        //out = u.unmarshal(bytes);
        return out;
    }

    protected String pojo2xml(Object pojo, JAXBContext ctx, Schema schema) throws JAXBException {
        String result;
        StringWriter sw = new StringWriter();
        Marshaller m = ctx.createMarshaller();
        m.setSchema(schema);
        m.marshal(pojo, sw);
        result = sw.toString();
        return result;

    }

    protected String readFromInputStream(InputStream is) throws IOException {
        String out;
        StringBuilder sb = new StringBuilder(PAGESIZE);
        int nread;
        byte[] buf;

        while (true) {
            buf = new byte[BUFFSIZE];
            nread = is.read(buf);
            if (nread <= 0) {
                break;
            }
            String sbStr = new String(buf);
            sb.append(sbStr);
        }
        out = sb.toString().trim();
        return out;
    }

    protected static boolean containsMethod(String method, String... methods) {
        if (method == null) {
            return false;
        }
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].toLowerCase().equals(method.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static List<String> lineSplit(String strIn) {
        StringBuilder sb = new StringBuilder();
        int i;
        List<String> lines = new ArrayList<String>();
        char[] chrs = {' '};
        for (i = 0; i < strIn.length(); i++) {
            String singleCharString;
            chrs[0] = strIn.charAt(i);
            singleCharString = new String(chrs);
            if (singleCharString.equals("\n")) {
                lines.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(singleCharString);
            }
        }
        lines.add(sb.toString());
        return lines;
    }

    // Grabs the text where the Error begins
    protected static String nearString(String strIn, int lineNum, int colNum) {
        String out = "";
        StringBuilder sb = new StringBuilder();
        String line;
        String lineOut;
        List<String> lines;
        int i;
        int last_i;

        lines = lineSplit(strIn);
        last_i = lines.size() - 1;
        for (i = lineNum; i <= last_i; i++) {
            lineOut = lines.get(i);
            if (i == lineNum) {
                if (colNum > lineOut.length()) {
                    continue;
                }
                lineOut = lineOut.substring(colNum);
            }
            if (i != last_i) {
                lineOut = String.format("%s\n", lineOut);
            }
            sb.append(lineOut);
        }

        out = sb.toString();
        return out;
    }

    protected static String jaxbParseExceptionToString(Exception ex, String xml) {
        String out;
        StringBuilder sb = new StringBuilder();
        if (ex instanceof JAXBException) {
            JAXBException je = (JAXBException) ex;
            sb.append("JAXBException\n");
            sb.append(dashline);
            sb.append(String.format("%s\n", getExtendedStackTrace(je)));
            sb.append(dashline);
            sb.append("\n");
            if (je.getCause() != null && je.getCause() instanceof SAXParseException) {
                SAXParseException se = (SAXParseException) je.getCause();
                sb.append(String.format("%s", saxParseExceptionToString(se, xml)));
            }
        }
        if (ex instanceof SAXParseException) {
            return saxParseExceptionToString(ex, xml);
        }
        return sb.toString();
    }

    protected static String saxParseExceptionToString(Exception ex, String xml) {
        String out;
        StringBuilder sb = new StringBuilder();
        if (ex instanceof SAXParseException) {
            int lineNum;
            int colNum;
            SAXParseException se = (SAXParseException) ex;
            lineNum = se.getLineNumber();
            colNum = se.getColumnNumber();
            sb.append(String.format("SAXParseException\n"));
            sb.append(dashline);
            sb.append(String.format("Error: %s\n", (se.getMessage() == null) ? "null" : se.getMessage()));
            sb.append(String.format("Line number: %d\n", lineNum - 1));
            sb.append(String.format("Colum number: %d\n", colNum - 1));
            sb.append(String.format("Near %s\n", nearString(xml, lineNum, colNum)));
            sb.append(dashline);
            sb.append("\n");
        }
        out = sb.toString();
        return out;
    }

    protected String overideAcceptType(String uri) {
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

        public static String getExtendedStackTrace(Throwable th) {
        Throwable t;
        StringBuilder sb = new StringBuilder(PAGESIZE);
        Throwable currThrowable;
        String msg;

        t = th;
        while (t != null) {
            if (t instanceof Throwable) {
                currThrowable = (Throwable) t;
                sb.append(String.format("\"%s\":\"%s\"\n", currThrowable.getClass().getName(), currThrowable.getMessage()));
                for (StackTraceElement se : currThrowable.getStackTrace()) {
                    sb.append(String.format("%s\n", se.toString()));
                }
                sb.append("\n");
                t = t.getCause();
            }
        }
        return sb.toString();
    }

    public FilterConfig getConfig() {
        return config;
    }

    public void setConfig(FilterConfig config) {
        this.config = config;
    }

    public String getpPkg() {
        return pPkg;
    }

    public void setpPkg(String pPkg) {
        this.pPkg = pPkg;
    }

    public String getpXSD() {
        return pXSD;
    }

    public void setpXSD(String pXSD) {
        this.pXSD = pXSD;
    }

    public String getfPkg() {
        return fPkg;
    }

    public void setfPkg(String fPkg) {
        this.fPkg = fPkg;
    }

    public String getfXSD() {
        return fXSD;
    }

    public void setfXSD(String fXSD) {
        this.fXSD = fXSD;
    }

    public JAXBContext getpCtx() {
        return pCtx;
    }

    public void setpCtx(JAXBContext pCtx) {
        this.pCtx = pCtx;
    }

    public JAXBContext getfCtx() {
        return fCtx;
    }

    public void setfCtx(JAXBContext fCtx) {
        this.fCtx = fCtx;
    }

    public Schema getpSchema() {
        return pSchema;
    }

    public void setpSchema(Schema pSchema) {
        this.pSchema = pSchema;
    }

    public Schema getfSchema() {
        return fSchema;
    }

    public void setfSchema(Schema fSchema) {
        this.fSchema = fSchema;
    }
}
