package org.openstack.atlas.datamodel.extensions.json;

import java.util.ArrayList;
import java.util.Collection;

public class Extensions {
    private Collection<Extension> extensions;

    public Collection<Extension> getExtensions() {
        if (extensions == null) extensions = new ArrayList<Extension>();
        return extensions;
    }

    public void setExtensions(Collection<Extension> extensions) {
        this.extensions = extensions;
    }
}
