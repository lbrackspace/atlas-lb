package org.openstack.atlas.service.domain.util;

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
        StringBuilder result = new StringBuilder();
        if (a.length > 0) {
            result.append(a[0]);
            for (int i = 1; i < a.length; i++) {
                result.append(separator).append(a[i]);
            }
        }
        return result.toString();
    }

    public static String buildDelemtedListFromIntegerArray(Integer[] a, String separator) {
        StringBuilder result = new StringBuilder();
        if (a.length > 0) {
            result.append(String.valueOf(a[0]));
            for (int i = 1; i < a.length; i++) {
                result.append(separator).append(String.valueOf(a[i]));
            }
        }
        return result.toString();
    }
}
