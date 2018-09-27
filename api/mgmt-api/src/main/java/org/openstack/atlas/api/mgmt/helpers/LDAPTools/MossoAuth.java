package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import java.io.IOException;
import java.util.*;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import javax.naming.directory.Attribute;
import org.openstack.atlas.util.debug.Debug;

public class MossoAuth {

    private static final int PAGESIZE = 4096;
    private MossoAuthConfig config;
    private GroupConfig groupMap;
    private ClassConfig classMap;

    static {
    }

    public MossoAuth() {
    }

    public MossoAuth(MossoAuthConfig config) {
        this.config = config;
        this.classMap = config.getClassConfig();
        this.groupMap = config.getGroupConfig();
    }

    public boolean testAuth(String user, String passwd) {
        LdapContext ctx;
        String udn;
        nop();
        NamingEnumeration<SearchResult> results;

        if (user == null || passwd == null || user.equals("") || passwd.equals("")) {
            return false; // Prevents annonymouse binds.
        }
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(config.getScope());
        String filter = String.format("cn=%s", user);
        LDAPCtxContainer ct = new LDAPCtxContainer(config, classMap);
        try {
            ct.connect(user, passwd);
            ctx = ct.getCtx();
            results = ctx.search(classMap.getDn(), filter, ctls);
            ct.disconnect();
        } catch (NamingException ex) {
            String msg = Debug.getEST(ex);
            return false;
        } catch (IOException ex) {
            String msg = Debug.getEST(ex);
            return false;
        }
        return true;
    }

    public Set<String> getGroups(String user, String passwd) throws NamingException, IOException {
        int i;
        int nGroups;
        nop();
        LdapContext ctx;
        NamingEnumeration<SearchResult> answer;
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(config.getScope());
        Set<String> groupSet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        Map<String, String> attrMap;
        if (user == null || passwd == null || user.equals("") || passwd.equals("")) {
            return groupSet; // Returns empty groupset on annonymouse binds.
        }
        GroupConfig gc = this.groupMap;
        LDAPCtxContainer ct = new LDAPCtxContainer(config, classMap);
        String query = gc.getUserQuery();
        String escapeUser = escapeFilter(user);
        String filter = String.format(query, escapeUser);
        String dn = gc.getDn();
        String sdn = gc.getSdn();
        String memberField = gc.getMemberField();
        ct.connect(user, passwd);
        ctx = ct.getCtx();
        ArrayList<String> dnList = new ArrayList<String>();
        if (dn.contains(":")) {
            // We have to query multiple org units
            dnList = buildDn(dn);
        } else {
            // We're working against a single org unit
            dnList.add(dn);
        }
        for (String d : dnList) {
            answer = ctx.search(d, filter, ctls);
            String groupName;
            while (answer.hasMore()) {
                SearchResult sr = (SearchResult) answer.next();
                Attribute groupList = sr.getAttributes().get(memberField);
                nGroups = groupList.size();
                for (i = 0; i < nGroups; i++) {
                    String groupListStr = (String) groupList.get(i);
                    attrMap = attrSplit(groupListStr);
                    groupName = attrMap.get(sdn);
                    groupSet.add(groupName);
                    nop();
                }
            }
        }
        ct.disconnect();
        nop();
        return groupSet;
    }

    private ArrayList<String> buildDn(String baseDn) {
        // Returns sanitized DN list with a single OU per DN entry
        ArrayList<String> dnList = new ArrayList<String>();
        String[] dsplit = baseDn.split(",");
        String ous = dsplit[0];
        for (String ou : ous.split(":")) {
            dnList.add(String.format("ou=%s,%s,%s",
                    ou.replaceAll("ou=", ""),
                    dsplit[1], dsplit[2]));
        }
        return dnList;
    }

    public MossoAuthConfig getConfig() {
        return config;
    }

    public void setConfig(MossoAuthConfig config) {
        this.config = config;
    }

    public GroupConfig getGroupMap() {
        return groupMap;
    }

    public void setGroupMap(GroupConfig classMap) {
        this.groupMap = classMap;
    }

    public ClassConfig getClassMap() {
        return classMap;
    }

    public void setClassMap(ClassConfig classMap) {
        this.classMap = classMap;
    }

    public static final String escapeFilter(String filter) {
        StringBuilder sb;
        nop();
        sb = new StringBuilder(PAGESIZE);
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
        StringBuilder sb;
        nop();
        sb = new StringBuilder();
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
        Map<String, String> mapOut;
        mapOut = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
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

    private static void nop() {
    }
}
