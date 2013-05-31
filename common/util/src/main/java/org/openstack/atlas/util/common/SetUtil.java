package org.openstack.atlas.util.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetUtil {
    // Cause I keep forget what a Set operations really look like

    public static <U> Set<U> andSet(Set<U> a, Set<U> b) {
        Set<U> aCopy = new HashSet<U>(a);
        Set<U> bCopy = new HashSet<U>(b);
        aCopy.retainAll(bCopy);
        return aCopy;
    }

    public static <U> Set<U> orSet(Set<U> a, Set<U> b) {
        Set<U> aCopy = new HashSet<U>(a);
        Set<U> bCopy = new HashSet<U>(b);
        aCopy.addAll(bCopy);
        return aCopy;
    }

    // Also known as the asymetric difference of 2 sets
    public static <U> Set<U> subtractSet(Set<U> a, Set<U> b) {
        Set<U> aCopy = new HashSet<U>(a);
        Set<U> bCopy = new HashSet<U>(b);
        aCopy.removeAll(b);
        return aCopy;
    }

    public static <U> List<U> toSortedList(Set<U> a, Comparator<? super U> comparator) {
        List<U> sortList = new ArrayList<U>(a);
        Collections.sort(sortList, comparator);
        return sortList;
    }

    public static <U> List<U> toSortedList(Set<U> a) {
        return toSortedList(a, null);
    }
}
