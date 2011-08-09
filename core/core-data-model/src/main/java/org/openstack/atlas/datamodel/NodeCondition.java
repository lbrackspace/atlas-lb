package org.openstack.atlas.datamodel;

public enum NodeCondition {
    ENABLED,
    DISABLED;

    public static String[] toArray() {
        String[] array = new String[NodeCondition.values().length];
        int i=0;

        for (NodeCondition nodeCondition : NodeCondition.values()) {
            array[i] = nodeCondition.name();
            i++;
        }

        return array;
    }
}
