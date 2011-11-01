package org.openstack.atlas.adapter.zxtm.helper;

import com.zxtm.service.client.PoolWeightingsDefinition;

import java.util.List;

public class ListUtil {

    public static String[] convert(List<String> list) {
        String[] array = new String[list.size()];

        int i = 0;
        for (String string : list) {
            array[i] = string;
            i++;
        }

        return array;
    }

    public static String[] wrap(String string) {
        String[] wrapperArray = new String[1];
        wrapperArray[0] = string;
        return wrapperArray;
    }

    public static String[][] wrap(String[] array) {
        String[][] wrapperArray = new String[1][];
        wrapperArray[0] = array;
        return wrapperArray;
    }

    public static PoolWeightingsDefinition[][] wrap(PoolWeightingsDefinition[] array) {
        PoolWeightingsDefinition[][] wrapperArray = new PoolWeightingsDefinition[1][];
        wrapperArray[0] = array;
        return wrapperArray;
    }
}
