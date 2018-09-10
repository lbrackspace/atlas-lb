package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.SSLSession;
import static org.openstack.atlas.api.mgmt.helpers.LDAPTools.MossoAuth.escapeDn;

public class LDAPCtxContainer {

    private MossoAuthConfig mossoAuthConfig;
    private LdapContext ctx;
    private StartTlsResponse tls;
    private SSLSession sslsess;
    private ClassConfig userConfig;

    public LDAPCtxContainer() {
    }

    public LDAPCtxContainer(MossoAuthConfig mossoAuthConfig, ClassConfig userConfig) {
        this.mossoAuthConfig = mossoAuthConfig;
        ctx = null;
        tls = null;
        sslsess = null;
        this.userConfig = userConfig;
        nop();
    }

    public void connect(String user, String passwd) throws NamingException, IOException {
        switch (this.mossoAuthConfig.getConnectMethod()) {
            case SSL:
                connectSSL(user, passwd);
                nop();
                break;
            case TLS:
                connectTLS(user, passwd);
                nop();
                break;
            default:
                break;
        }
    }

    public void disconnect() throws NamingException, IOException {
        switch (this.mossoAuthConfig.getConnectMethod()) {
            case SSL:
                disconnectSSL();
                nop();
                break;
            case TLS:
                disconnectTLS();
                nop();
                break;
            default:
                break;
        }
    }

    private void connectSSL(String user, String passwd) throws NamingException {
        Hashtable env = new Hashtable();
        String bindDN;
        String url = String.format("ldaps://%s:%d", this.mossoAuthConfig.getHost(), this.mossoAuthConfig.getPort());
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, url);
        //env.put(Context.SECURITY_PROTOCOL, "ssl");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userDn(user));
        env.put(Context.SECURITY_CREDENTIALS, passwd);
        env.put("java.naming.ldap.factory.socket", UncertainCertIgnoreingSSLFactory.class.getName());
        ctx = new InitialLdapContext(env, null);
        nop();
    }

    private void disconnectSSL() throws NamingException {
        this.ctx.close();
    }

    private void connectTLS(String user, String passwd) throws NamingException, IOException {
        Hashtable env = new Hashtable();
        String bindDN;
        String url = String.format("ldap://%s:%d", mossoAuthConfig.getHost(), mossoAuthConfig.getPort());
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, url);
        Control[] controls = new Control[]{};
        ctx = new InitialLdapContext(env, null);
        ctx.setRequestControls(controls);
        // Oracle is breaking connectivity with ActiveDirectory
        // So clear the controls especiall 2.16.840.1.113730.3.4.2
        tls = (StartTlsResponse) ctx.extendedOperation(new StartTlsRequest());
        tls.setHostnameVerifier(new EmptyHostnameVerifier());
        ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, userDn(user));
        ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, passwd);
        ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
        sslsess = tls.negotiate();
    }

    private void disconnectTLS() throws IOException, NamingException {
        tls.close();
        ctx.close();
    }

    private String userDn(String user) {
        if (mossoAuthConfig.isIsActiveDirectory()) {
            StringBuilder sb = new StringBuilder(32);
            sb.append(user);
            sb.append(mossoAuthConfig.getAppendName());
            String activeDirectoryUserName = sb.toString();
            return activeDirectoryUserName;
        }
        String dn = this.userConfig.getDn();
        String sdn = this.userConfig.getSdn();
        String out = String.format("%s=%s,%s", sdn, escapeDn(user), dn);
        return out;
    }

    private static void nop() {
    }

    public LdapContext getCtx() {
        return ctx;
    }

    public void setCtx(LdapContext ctx) {
        this.ctx = ctx;
    }

    public StartTlsResponse getTls() {
        return tls;
    }

    public void setTls(StartTlsResponse tls) {
        this.tls = tls;
    }

    public SSLSession getSslsess() {
        return sslsess;
    }

    public void setSslsess(SSLSession sslsess) {
        this.sslsess = sslsess;
    }

    public MossoAuthConfig getMossoAuthConfig() {
        return mossoAuthConfig;
    }

    public void setMossoAuthConfig(MossoAuthConfig mossoAuthConfig) {
        this.mossoAuthConfig = mossoAuthConfig;
    }

    public ClassConfig getUserConfig() {
        return userConfig;
    }

    public void setUserConfig(ClassConfig userConfig) {
        this.userConfig = userConfig;
    }
}
