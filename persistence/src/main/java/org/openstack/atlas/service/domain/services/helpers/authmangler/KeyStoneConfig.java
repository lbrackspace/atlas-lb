package org.openstack.atlas.service.domain.services.helpers.authmangler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.openstack.atlas.service.domain.services.helpers.RdnsHelper;

public class KeyStoneConfig {

    private static final int BUFFSIZE = 1024 * 16;
    private final String adminUrl;
    private final String adminKey;
    private final String adminUser;
    private final String publicUrl;

    public KeyStoneConfig(String adminUrl, String adminUser, String adminPasswd, String publicUrl) {
        this.adminUrl = adminUrl;
        this.adminUser = adminUser;
        this.adminKey = adminPasswd;
        this.publicUrl = publicUrl;
    }

    public KeyStoneConfig(RdnsHelper rdnsHelper) {
        this.adminUrl = rdnsHelper.getAuthAdminUrl();
        this.publicUrl = rdnsHelper.getAuthPublicUrl();
        this.adminUser = rdnsHelper.getAuthAdminUser();
        this.adminKey = rdnsHelper.getAuthAdminKey();
    }


    public String getAdminUrl() {
        return adminUrl;
    }

    public String getAdminKey() {
        return adminKey;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    @Override
    public String toString() {
        String fmt = ""
                + "adminUrl: %s\n"
                + "adminUser: %s\n"
                + "adminKey: %s\n"
                + "publicUrl: %s\n"
                + "\n";
        return String.format(fmt, adminUrl, adminUser, "CENSORED", publicUrl);
    }
}
