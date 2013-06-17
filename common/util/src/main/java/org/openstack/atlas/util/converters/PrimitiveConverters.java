package org.openstack.atlas.util.converters;

import org.openstack.atlas.util.common.exceptions.ConverterException;
import java.util.ArrayList;
import java.util.List;

public class PrimitiveConverters {

    public static List<Integer> cdString2IntegerList(String ints) throws ConverterException {
        List<Integer> out = new ArrayList<Integer>();
        String format;
        String msg;
        Integer currInt;
        int i;

        if (ints == null) {
            throw new ConverterException("ints was null can't convert");
        }

        String[] splitInts = ints.split(",");
        if (splitInts.length == 0) {
            msg = String.format("Error \"%s\" yeilded an empty ArrayList");
            throw new ConverterException(msg);
        }

        if (splitInts[0].trim().equals("")) {
            return out; // If they pass an empty String give them an empty List I guess
        }

        for (i = 0; i < splitInts.length; i++) {
            try {
                currInt = Integer.parseInt(splitInts[i].trim());
            } catch (NumberFormatException ex) {
                format = "Could not convert \"%s\" to Integer\n";
                msg = String.format(format, splitInts[i]);
                throw new ConverterException(msg);
            }
            out.add(currInt);
        }
        return out;
    }

    public static String integerList2cdString(List<Integer> ints) throws ConverterException {
        StringBuilder sb = new StringBuilder();
        String component;
        int intListSize;
        int i;
        if (ints == null) {
            throw new ConverterException("Could not convert a null Integer list to String");
        }
        intListSize = ints.size();

        if (intListSize <= 0) {
            return sb.toString(); // if they pass in a list with no items then return an empty string
        }
        for (i = 0; i < intListSize - 1; i++) {
            component = String.format("%d,", ints.get(i));
            sb.append(component);
        }
        component = String.format("%d",ints.get(intListSize-1));
        sb.append(component);
        return sb.toString();
    }
}
