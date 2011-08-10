package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import java.io.IOException;
import java.security.Security;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import javax.naming.directory.Attribute;

public class MossoAuth {

    private MossoAuthConfig config;
    private Map<String, GroupConfig> groupMap;
    private Map<String, ClassConfig> classMap;

    static {
        Security.addProvider(new OverTrustingTrustProvider());
        Security.setProperty("ssl.TrustManagerFactory.algorithm", "TrustAllCertificates");
    }

    public MossoAuth() {
    }

    public MossoAuth(MossoAuthConfig config, Map<String, GroupConfig> groupMap, Map<String, ClassConfig> classMap) {
        this.config = config;
        this.groupMap = groupMap;
        this.classMap = classMap;
    }


    public boolean testAuth(String user, String passwd) {
        ClassConfig uc;
        LdapContext ctx;
        String udn;
        NamingEnumeration<SearchResult> results;
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        uc = classMap.get("user");
        String filter = String.format("cn=%s", user);
        udn = uc.getDn();
        LDAPCtxContainer ct = new LDAPCtxContainer(config, classMap.get("user"));
        try {
            ct.connect(user, passwd);
            ctx = ct.getCtx();
            results = ctx.search(udn, filter, ctls);
            ct.disconnect();
        } catch (NamingException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public Set<String> getGroups(String user, String passwd) throws NamingException, IOException {
        int i;
        int j;
        LdapContext ctx;
        NamingEnumeration<SearchResult> answer;
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        Set<String> groupSet = new HashSet<String>();
        Map<String,String> attrMap;
        GroupConfig gc = this.groupMap.get("groups");
        LDAPCtxContainer ct = new LDAPCtxContainer(config, classMap.get("user"));
        String filter = String.format(gc.getUserQuery(), escapeFilter(user));
        ct.connect(user, passwd);
        ctx = ct.getCtx();
        answer = ctx.search(gc.getDn(), filter, ctls);
        String groupName;
        while (answer.hasMore()) {
            SearchResult sr = (SearchResult) answer.next();
            Attribute groupList = sr.getAttributes().get(gc.getMemberField());
            for (i = 0; i < groupList.size(); i++) {
                attrMap = attrSplit((String)groupList.get(i));
                groupName = attrMap.get(gc.getSdn());
                groupSet.add(groupName);
                nop();
            }
        }
        ct.disconnect();
        nop();
        return groupSet;
    }

    public MossoAuthConfig getConfig() {
        return config;
    }

    public void setConfig(MossoAuthConfig config) {
        this.config = config;
    }

    public Map<String, GroupConfig> getGroupMap() {
        return groupMap;
    }

    public void setGroupMap(Map<String, GroupConfig> classMap) {
        this.groupMap = classMap;
    }

    public Map<String, ClassConfig> getClassMap() {
        return classMap;
    }

    public void setClassMap(Map<String, ClassConfig> classMap) {
        this.classMap = classMap;
    }

    public static final String escapeFilter(String filter) {
        StringBuffer sb = new StringBuffer(); // If using JDK >= 1.5 consider using StringBuilder
        for (int i = 0; i < filter.length(); i++) {
            char curChar = filter.charAt(i);
            switch (curChar) {
                case '\\':
                    sb.append("\\5c");
                    break;
                case '*':
                    sb.append("\\2a");
                    break;
                case '(':
                    sb.append("\\28");
                    break;
                case ')':
                    sb.append("\\29");
                    break;
                case '\u0000':
                    sb.append("\\00");
                    break;
                default:
                    sb.append(curChar);
            }
        }
        return sb.toString();
    }

    public static String escapeDn(String name) {
        StringBuffer sb = new StringBuffer(); // If using JDK >= 1.5 consider using StringBuilder
        if ((name.length() > 0) && ((name.charAt(0) == ' ') || (name.charAt(0) == '#'))) {
            sb.append('\\'); // add the leading backslash if needed
        }
        for (int i = 0; i < name.length(); i++) {
            char curChar = name.charAt(i);
            switch (curChar) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case ',':
                    sb.append("\\,");
                    break;
                case '+':
                    sb.append("\\+");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '<':
                    sb.append("\\<");
                    break;
                case '>':
                    sb.append("\\>");
                    break;
                case ';':
                    sb.append("\\;");
                    break;
                default:
                    sb.append(curChar);
            }
        }
        if ((name.length() > 1) && (name.charAt(name.length() - 1) == ' ')) {
            sb.insert(sb.length() - 1, '\\'); // add the trailing backslash if needed
        }
        return sb.toString();
    }

    private Map<String, String> attrSplit(String attrs) {
        Map<String, String> mapOut = new HashMap<String, String>();
        String[] kv;
        for (String term : attrs.split(",")) {
            kv = term.split("=");
            if (kv.length != 2) {
                continue;
            }
            String key = kv[0].trim();
            String val = kv[1].trim();
            mapOut.put(key, val);
        }
        return mapOut;
    }

    private void nop() {
    }
}
