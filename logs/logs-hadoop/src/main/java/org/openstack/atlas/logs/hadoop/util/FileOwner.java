package org.openstack.atlas.logs.hadoop.util;

public class FileOwner {

    private String user;
    private String group;

    public FileOwner(String user, String group) {
        this.user = user;
        this.group = group;
    }

    @Override
    public String toString() {
        return "FileOwner{user=" + user + ", group=" + group + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileOwner other = (FileOwner) obj;
        if ((this.user == null) ? (other.user != null) : !this.user.equals(other.user)) {
            return false;
        }
        if ((this.group == null) ? (other.group != null) : !this.group.equals(other.group)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.user != null ? this.user.hashCode() : 0);
        hash = 89 * hash + (this.group != null ? this.group.hashCode() : 0);
        return hash;
    }

    public FileOwner() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
