package org.openstack.atlas.datamodel;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Scope("singleton")
public class CorePersistenceType implements PersistenceType {
    public static final String HTTP_COOKIE = "HTTP_COOKIE";
    private static final Set<String> persistenceTypes;

    static {
        persistenceTypes = new HashSet<String>();
        persistenceTypes.add(HTTP_COOKIE);
    }

    public CorePersistenceType() {
    }

    public boolean contains(String str) {
        boolean out;
        out = persistenceTypes.contains(str);
        return out;
    }

    public static String[] values() {
        return persistenceTypes.toArray(new String[persistenceTypes.size()]);
    }

    @Override
    public String[] toList() {
        return persistenceTypes.toArray(new String[persistenceTypes.size()]);
    }


    protected static void add(String persistenceType) {
        persistenceTypes.add(persistenceType);
    }
}
