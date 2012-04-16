package org.openstack.atlas.util.ip;

import org.apache.log4j.Logger;
import org.openstack.atlas.util.common.StringUtils;

import javax.naming.InvalidNameException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

public class DnsUtil {

    private static int timeout = 2000;
    private static int retries = 2;
    private static final Logger LOG = Logger.getLogger(DnsUtil.class);

    private static Hashtable initEnv() {
        String to = String.format("%d", timeout);
        String rt = String.format("%d", retries);
        String ContextInitial = javax.naming.Context.INITIAL_CONTEXT_FACTORY;
        Hashtable env = new Hashtable();
        env.put(ContextInitial, "com.sun.jndi.dns.DnsContextFactory");
        env.put("com.sun.jndi.dns.timeout.initial", to);
        env.put("com.sun.jndi.dns.timeout.retries", rt);
        return env;
    }

    // record types should be "A" and "AAAA" for our project
    public static List<String> lookup(String fqdn, String... record_types) throws NamingException {
        List<String> records = new ArrayList<String>();
        Hashtable env = initEnv();
        String msg;
        String fmt;
        int i;
        Object obj;
        try {
            DirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(fqdn, record_types);
            for (i = 0; i < record_types.length; i++) {
                Attribute attr = attrs.get(record_types[i]);
                if (attr == null) {
                    continue; // No records of this type found
                }
                NamingEnumeration attrEnum = attr.getAll();
                while (attrEnum.hasMoreElements()) {
                    String ip = (String) attrEnum.next();
                    records.add(ip);
                }
            }
        } catch (NameNotFoundException ex) {
            msg = String.format("No records found for domain %s returning empty List", fqdn);
            LOG.warn(msg);
            return records;
        } catch (InvalidNameException inv) {
            fmt = "Error looking up dns records for %s exception was %s";
            msg = String.format(fmt, fqdn, StringUtils.getEST(inv));
            LOG.error(msg);
            throw inv;
        } catch (NamingException ne) {
            fmt = "Error looking up dns records for %s exception was %s";
            msg = String.format(fmt, fqdn, StringUtils.getEST(ne));
            LOG.error(msg);
            System.out.print(msg);
            throw ne;
        }
        Collections.sort(records);
        return records;
    }
}
