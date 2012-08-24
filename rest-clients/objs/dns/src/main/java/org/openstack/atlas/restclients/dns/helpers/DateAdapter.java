package org.openstack.atlas.restclients.dns.helpers;

import java.util.Calendar;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateAdapter extends XmlAdapter<String, Calendar> {

    @Override
    public String marshal(Calendar value) {
        if (value == null) {
            return null;
        }
        return javax.xml.bind.DatatypeConverter.printDate(value);
    }

    @Override
    public Calendar unmarshal(String value) {
        return javax.xml.bind.DatatypeConverter.parseDate(value);
    }

}
