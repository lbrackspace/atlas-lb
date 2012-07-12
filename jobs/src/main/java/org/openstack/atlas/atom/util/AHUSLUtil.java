package org.openstack.atlas.atom.util;

import com.rackspace.docs.core.event.EventType;
import com.rackspace.docs.core.event.Region;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Usage;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class AHUSLUtil {
    private static final Log LOG = LogFactory.getLog(AHUSLUtil.class);

    public static final int PAGESIZE = 4096;
    public static final int FRAGSIZE = 4;


    /**
     * @param throwable the throwable exception used to parse the extended stack trace.
     * @return the string based off the throwables stack trace
     */
    public static String getExtendedStackTrace(Throwable throwable) {
        Throwable t;
        StringBuilder sb;
        Exception currEx;
        String msg;

        sb = new StringBuilder(PAGESIZE);
        t = throwable;
        while (t != null) {
            if (t instanceof Exception) {
                currEx = (Exception) t;
                msg = String.format("%s\n", getStackTrace(currEx));
                sb.append(msg);
                t = t.getCause();
            }
        }
        return sb.toString();
    }

    /**
     * @param ex the exception used to parse the stack trace
     * @return the string parsed from the exceptions stack trace
     */
    public static String getStackTrace(Exception ex) {
        StringBuilder sb = new StringBuilder(PAGESIZE);
        sb.append(String.format("Exception: %s:%s\n", ex.getMessage(), ex.getClass().getName()));
        for (StackTraceElement se : ex.getStackTrace()) {
            sb.append(String.format("%s\n", se.toString()));
        }
        return sb.toString();
    }

    /**
     * @return the Calendar
     */
    public static Calendar getNow() {
        return Calendar.getInstance();
    }

    /**
     * @return the Calendar minus a month
     */
    public static Calendar getStartCal() {
        Calendar c = getNow();
        c.add(Calendar.MONTH, -2);
        return c;
    }

    /**
     * @param response the clientResponse
     * @return string with response body
     * @throws IOException
     */
    public static String processResponseBody(ClientResponse response) throws IOException {
        InputStream is = response.getEntityInputStream();
        StringBuilder sb = new StringBuilder(AHUSLUtil.PAGESIZE);

        int nbytes;
        do {
            byte[] buff = new byte[AHUSLUtil.FRAGSIZE];
            nbytes = is.read(buff);
            String frag = new String(buff, "UTF-8");
            sb.append(frag);
        } while (nbytes > 0);
        return sb.toString();
    }

    /**
     * This method returns XMLGregorinanCalendar based on milliseconds
     *
     * @param calendar
     * @return
     * @throws DatatypeConfigurationException
     */
    public static XMLGregorianCalendar processCalendar(Calendar calendar) throws DatatypeConfigurationException {
        //TODO: find a better way to transform.............

        Calendar retcal = convertCalendar(calendar, TimeZone.getTimeZone("UTC"));

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(retcal.getTimeInMillis());
        XMLGregorianCalendar xgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
        xgc.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
        xgc.setTimezone(0);
//        System.out.println("XMLGREGORIAN:: " + xgc);
        return xgc;
    }

    public static Calendar convertCalendar(final Calendar calendar, final TimeZone timeZone) {
        Calendar ret = new GregorianCalendar(timeZone);
        ret.setTimeInMillis(calendar.getTimeInMillis() +
                timeZone.getOffset(calendar.getTimeInMillis()) -
                TimeZone.getDefault().getOffset(calendar.getTimeInMillis()));
        ret.getTime();
        return ret;
    }


    /**
     * This method maps string events to EventType
     *
     * @param usageRecord
     * @return
     * @throws DatatypeConfigurationException
     */
    public static EventType mapEventType(Usage usageRecord) throws DatatypeConfigurationException {
        if (usageRecord.getEventType() != null) {
            if (usageRecord.getEventType().equals("CREATE_LOADBALANCER")) {
                return EventType.CREATE;
            } else if (usageRecord.getEventType().equals("DELETE_LOADBALANCER")) {
                return EventType.DELETE;
            } else if (usageRecord.getEventType().equals("SUSPEND_LOADBALANCER")) {
                return EventType.SUSPEND;
            } else if (usageRecord.getEventType().equals("UNSUSPEND_LOADBANCER")) {
                return EventType.UNSUSPEND;
            } else if (usageRecord.getEventType().equals("UPDATE_LOADBALANCER")) {
                return EventType.UPDATE;
            }
        }
        return null;
    }

    public static Region mapRegion(String configRegion) {
        if (configRegion.equals("DFW")) {
            return Region.DFW;
        } else if (configRegion.equals("ORD")) {
            return Region.ORD;
        } else if (configRegion.equals("LON")) {
            return Region.LON;
        } else {
            LOG.error("Region could not be mapped from config, using default");
            return Region.GLOBAL;
        }
    }
}
