package org.openstack.atlas.datamodel.extensions.json;

import java.util.Collection;

public class Extension {
    private String name;
    private String nameSpace;
    private String alias;
    private String updated;
    private String description;
    private Collection<Link> links;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<Link> getLinks() {
        return links;
    }

    public void setLinks(Collection<Link> links) {
        this.links = links;
    }
}
