package org.rackspace.capman.tools.util;

import org.rackspace.capman.tools.ca.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassSet extends HashSet<Class> {

    public ClassSet(int initialCapacity) {
        super(initialCapacity);
    }

    public ClassSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public ClassSet(Collection c) {
        super(c);
    }

    // Allow the creation of a set with only 1 class at construction time
    public ClassSet(Class... c) {
        super();
        for (int i = 0; i < c.length; i++) {
            this.add(c[i]);
        }
    }

    public ClassSet() {
        super();
    }

    public ClassSet getSupersOf(Class queryClass) {
        ClassSet supers = new ClassSet();
        for (Class classFromSet : this) {
            if (classFromSet == null) {
                continue;
            }
            if (isSuperOf(classFromSet, queryClass)) {
                supers.add(classFromSet);
            }
        }
        return supers;
    }

    public ClassSet getExtendersOf(Class queryClass) {
        ClassSet childClasses = new ClassSet();
        for (Class classFromSet : this) {
            if (classFromSet == null) {
                continue;
            }
            if (isSuperOf(queryClass, classFromSet)) {
                childClasses.add(classFromSet);
            }
        }
        return childClasses;
    }

    public boolean hasSupersOf(Class queryClass) {
        return getSupersOf(queryClass).size() > 0;
    }

    @Override
    public String toString() {
        List<String> classNames = new ArrayList<String>();
        for (Class c : this) {
            if (c == null) {
                continue;
            }
            String className = c.getCanonicalName();
            if (className == null) {
                className = "null";
            }
            classNames.add(String.format("\"%s\"", className));
        }
        Collections.sort(classNames);
        return String.format("{%s}", StringUtils.joinString(classNames, ","));
    }

    public static boolean isSuperOf(Class expectedParent, Class expectedChild) {
        if (expectedParent.equals(expectedChild)) {
            return false;
        }
        return expectedParent.isAssignableFrom(expectedChild);
    }

    public static ClassSet fromInstances(Collection<Object> objs) {
        ClassSet classSetOut = new ClassSet();
        for (Object obj : objs) {
            if (obj == null) {
                continue;
            }
            classSetOut.add(obj.getClass());
        }
        return classSetOut;
    }

    public List<String> classNames() {
        List<String> names = new ArrayList<String>();
        for (Class classInSet : this) {
            if (classInSet == null) {
                continue;
            }
            names.add(classInSet.getName());
        }
        Collections.sort(names);
        return names;
    }
}
