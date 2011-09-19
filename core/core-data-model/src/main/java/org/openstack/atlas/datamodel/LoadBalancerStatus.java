package org.openstack.atlas.datamodel;

import java.util.HashSet;
import java.util.Set;


public final class LoadBalancerStatus {
    private static final Set<String> loadBalancerStatusSet;
    private static final String[] loadBalancerStatusStrs;

   static {
       int i;
       loadBalancerStatusSet = new HashSet<String>();
       loadBalancerStatusSet.add("QUEUED");
       loadBalancerStatusSet.add("BUILD");
       loadBalancerStatusSet.add("ACTIVE");
       loadBalancerStatusSet.add("ERROR");
       loadBalancerStatusSet.add("PENDING_UPDATE");
       loadBalancerStatusSet.add("SUSPENDED");
       loadBalancerStatusSet.add("PENDING_DELETE");
       loadBalancerStatusSet.add("DELETED");

       loadBalancerStatusStrs = new String[loadBalancerStatusSet.size()];

       i = 0;
       for(String status:loadBalancerStatusSet) {
           loadBalancerStatusStrs[i]=status;
           i++;
       }
   }

   public boolean contains(String str) {
       boolean out;
       out = loadBalancerStatusSet.contains(str);
       return out;
   }

   public static String[] values() {
       return loadBalancerStatusStrs;
   }
}
