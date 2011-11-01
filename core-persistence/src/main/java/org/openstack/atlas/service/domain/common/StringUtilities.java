package org.openstack.atlas.service.domain.common;

import java.util.Collection;

public class StringUtilities {
    public static <T> String DelimitString(Collection<T> objects, String delim) {
        String newString = "";
        for (Object string : objects) {
            newString += string.toString() + delim;
        }

        if (!newString.equals("")) {
            newString = newString.substring(0, newString.length() - delim.length());
        }
        return newString;
    }

    public static <T> String DelimitStringAndWrapEntriesWithQuotes(Collection<T> objects, String delim) {
        String newString = "";
        for (Object string : objects) {
            newString += "'" + string.toString() + "'" + delim;
        }

        if (!newString.equals("")) {
            newString = newString.substring(0, newString.length() - delim.length());
        }
        return newString;
    }

    public static String buildDelemtedListFromStringArray(String[] a, String separator) {
        StringBuffer result = new StringBuffer();
        if (a.length > 0) {
            result.append(a[0]);
            for (int i = 1; i < a.length; i++) {
                result.append(separator);
                result.append(a[i]);
            }
        }
        return result.toString();
    }


}