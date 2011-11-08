package org.openstack.atlas.datamodel.extensions.json;

public class ExtensionsWrapper {
    private Extensions extensions;

    public Extensions getExtensions() {
        if (extensions == null) extensions = new Extensions();
        return extensions;
    }

    public void setExtensions(Extensions extensions) {
        this.extensions = extensions;
    }
}
