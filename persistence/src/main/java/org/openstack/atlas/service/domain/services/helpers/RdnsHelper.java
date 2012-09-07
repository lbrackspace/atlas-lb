package org.openstack.atlas.service.domain.services.helpers;

import java.io.UnsupportedEncodingException;
import org.openstack.atlas.util.b64aes.Aes;
import org.openstack.atlas.util.config.LbConfiguration;
import org.openstack.atlas.util.config.MossoConfigValues;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.util.debug.Debug;
import com.sun.jersey.api.client.ClientResponse;
import org.openstack.atlas.restclients.dns.DnsClient1_0;

public class RdnsHelper {

    private static final Log LOG = LogFactory.getLog(RdnsHelper.class);
    private static final String LB_SERVICE_NAME = "cloudLoadBalancers";
    private String lbaasBaseUrl;
    private String rdnsUrl;
    private String rdnsUser;
    private String rdnsPasswd;

    public RdnsHelper() {
        LbConfiguration conf = new LbConfiguration();
        lbaasBaseUrl = conf.getString(MossoConfigValues.lbaas_url);
        rdnsUrl = conf.getString(MossoConfigValues.rdns_admin_url);
        rdnsUser = conf.getString(MossoConfigValues.rdns_admin_user);

        String key = conf.getString(MossoConfigValues.rdns_crypto_key);
        String ctext = conf.getString(MossoConfigValues.rdns_admin_passwd);
        try {
            rdnsPasswd = Aes.b64decrypt_str(ctext, key);
        } catch (Exception ex) {
            String stackTrace = Debug.getEST(ex);
            String fmt = "Error decrypting rDNS admin passwd. Call to delete "
                    + "PTR record will fail: %s";
            LOG.error(String.format(fmt, stackTrace), ex);
            rdnsPasswd = "????";
        }
    }

    // Cause typing "newRdnsHelper().delPtrRecord(aid,lid,ip) doesn't looks as "
    // silly as  of "(new RdnsHelper()).delPtrRecord(aid,lid,ip)"
    public static RdnsHelper newRdnsHelper(){
        return new RdnsHelper();
    }

    @Override
    public String toString() {
        String fmt = "{lbaasBaseUrl=%s,rdnsUrl=%s,rdnsUser=%s,rdnsPasswd=%s}";
        return String.format(fmt,lbaasBaseUrl,rdnsUrl,rdnsUser,"Censored");
    }

    public ClientResponse delPtrRecord(int aid,int lid,String ip) throws UnsupportedEncodingException{
        DnsClient1_0 dns = new DnsClient1_0("",rdnsUrl,rdnsUser,rdnsPasswd,"",aid);
        return dns.delPtrRecordMan(buildDeviceUri(aid,lid),LB_SERVICE_NAME,ip);
        
    }

    public String buildDeviceUri(int aid,int lid){
        return String.format("%s/%d/loadbalancers/%d",lbaasBaseUrl,aid,lid);
    }

    public String getLbaasBaseUrl() {
        return lbaasBaseUrl;
    }

    public String getRdnsUrl() {
        return rdnsUrl;
    }

    public String getRdnsUser() {
        return rdnsUser;
    }

    public String getRdnsPasswd() {
        return rdnsPasswd;
    }
}
