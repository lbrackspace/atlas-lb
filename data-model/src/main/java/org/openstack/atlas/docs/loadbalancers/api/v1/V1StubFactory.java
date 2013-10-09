package org.openstack.atlas.docs.loadbalancers.api.v1;

import java.math.BigInteger;
import java.util.Random;
import javax.xml.namespace.QName;
import org.w3.atom.Link;

public class V1StubFactory {

    private static Random rnd;

    static {
        rnd = new Random();
    }

    public static VirtualIp newVirtualIp() {
        VirtualIp vip = new VirtualIp();
        vip.setAddress(newIPv4Addr());
        vip.setId(rnd.nextInt(4096));
        vip.setType(rndChoice(VipType.values()));
        vip.setIpVersion(rndChoice(IpVersion.values()));
        return vip;
    }

    public static VirtualIps newVirtualIps(int nVips,int nLinks){
        VirtualIps vips= new VirtualIps();
        for(int i=0;i<nVips;i++){
            vips.getVirtualIps().add(newVirtualIp());
        }
        for(int i=0;i<nLinks;i++){
            vips.getLinks().add(newAtomLink());
        }
        return vips;
    }


    public static Link newAtomLink() {
        String ri = Integer.toString(rnd.nextInt(4096));
        Link atomLink = new Link();
        atomLink.setBase("SomeBase_" + ri);
        atomLink.setContent("SomeContent_" + ri);
        atomLink.setHref("SomeHref_" + ri);
        atomLink.setHreflang("SomeLangRef_" + ri);
        atomLink.setLang("SomeLang_" + ri);
        atomLink.setLength(new BigInteger("10000000000000000000"));
        atomLink.setRel("somRel_" + ri);
        atomLink.setTitle("someTitle_" + ri);
        atomLink.setType("someType");
        atomLink.getOtherAttributes().put(new QName("NSURI", "SomelocalPart", "somePrefix"), "SomeAttr_" + ri);
        atomLink.getOtherAttributes().put(new QName("anotherNSURI", "anotherLocalPart", "AnoterPrefix"), "AnotherAttr_" + ri);
        return atomLink;
    }

    public static String newIPv4Addr() {
        return Integer.toString(rnd.nextInt(256)) + "."
                + Integer.toString(rnd.nextInt(256)) + "."
                + Integer.toString(rnd.nextInt(256)) + "."
                + Integer.toString(rnd.nextInt(256)) + ".";

    }

    public static <E> E rndChoice(E[] list) {
        E chosen = list[rnd.nextInt(list.length)];
        return chosen;
    }
}
