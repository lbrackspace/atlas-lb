package org.openstack.atlas.restclients.dns.helpers;

import java.util.List;
import org.openstack.atlas.restclients.dns.pub.objects.Rdns;
import org.openstack.atlas.restclients.dns.pub.objects.Record;
import org.openstack.atlas.restclients.dns.pub.objects.RecordType;
import org.openstack.atlas.restclients.dns.pub.objects.RecordsList;
import org.w3._2005.atom.Link;


public class DnsMockObjectGenerator {

    public static Rdns newRdns() {
        Rdns rdns = new Rdns();
        Link link = new Link();
        rdns.setLink(link);
        link.setRel("cloudLoadBalancers");
        link.setHref("http://somedomain/somelb/blah/blah");
        RecordsList rList = new RecordsList();
        rdns.setRecordsList(rList);
        List<Record> recs = rList.getRecords();
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
