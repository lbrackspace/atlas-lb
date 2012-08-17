package org.openstack.atlas.restclients.dns.objects.CustomObjects;

import java.util.List;
import org.openstack.atlas.restclients.dns.objects.LinkType;
import org.openstack.atlas.restclients.dns.objects.Rdns;
import org.openstack.atlas.restclients.dns.objects.Record;
import org.openstack.atlas.restclients.dns.objects.RecordType;
import org.openstack.atlas.restclients.dns.objects.RecordsList;

public class DnsMockObjectGenerator {

    public static RootElementRdns newRootElementRdns() {
        RootElementRdns rdns = new RootElementRdns();
        LinkType lt = new LinkType();
        rdns.setLink(lt);
        lt.setRel("cloudLoadBalancers");
        lt.setHref("http://somedomain/somelb/blah/blah");
        RecordsList rList = new RecordsList();
        rdns.setRecordsList(rList);
        List<Record> recs = rList.getRecord();
        recs.add(newARecord("www.home.org","127.0.0.1"));
        recs.add(newARecord("www.somedomain.org","10.0.0.1"));
        return rdns;
    }

    public static Record newARecord(String host,String ip){
        Record rec = new Record();
        rec.setType(RecordType.A);
        rec.setData(ip);
        rec.setName(host);
        return rec;
    }
}
