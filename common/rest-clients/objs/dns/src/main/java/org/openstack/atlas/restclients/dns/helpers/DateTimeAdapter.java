package org.openstack.atlas.restclients.dns.helpers;
import java.util.Calendar;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateTimeAdapter extends XmlAdapter<String, Calendar> {

    @Override
    public String marshal(Calendar value) {
        if (value == null) {
            return null;
        }
        return javax.xml.bind.DatatypeConverter.printDateTime(value);
    }

    @Override
    public Calendar unmarshal(String value) {
        return javax.xml.bind.DatatypeConverter.parseDateTime(value);
    }

}
