package org.openstack.atlas.common.collections;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {
    /**
     * Verifies that list2 contains the items of list1 and returns the list of the items from list1 that don't belong to list2
     */
    public static List<Integer> compare(List<Integer> list1, List<Integer> list2) {
        List<Integer> badList = new ArrayList<Integer>();

       for (Integer i : list1) {
         if (!list2.contains(i)) badList.add(i);
       }
       return badList;
    }

    /**
     * Verifies that list2 contains the items of list1 and returns the list of the items from list1 that belong to list2
     */
    public static List<Integer> compareAndReturnMatches(List<Integer> list1, List<Integer> list2) {
        List<Integer> returnList = new ArrayList<Integer>();

       for (Integer i : list1) {
         if (list2.contains(i)) returnList.add(i);
       }
       return returnList;
    }
}