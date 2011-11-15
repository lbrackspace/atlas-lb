package org.openstack.atlas.datamodel.extensions.json;

import java.util.Collection;

public class Links {
    private Collection<Link> values;

    public Collection<Link> getValues() {
        return values;
    }

    public void setValues(Collection<Link> values) {
        this.values = values;
    }
}
