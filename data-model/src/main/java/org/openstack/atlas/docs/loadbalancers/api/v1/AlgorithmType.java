
package org.openstack.atlas.docs.loadbalancers.api.v1;

import java.util.HashSet;
import java.util.Set;


public final class AlgorithmType {
    private static final Set<String> algorithmTypeSet;
    private static final String[] algorithmTypeStrs;

   static {
       int i;
       algorithmTypeSet = new HashSet<String>();
       algorithmTypeSet.add("LEAST_CONNECTIONS");
       algorithmTypeSet.add("RANDOM");
       algorithmTypeSet.add("ROUND_ROBIN");
       algorithmTypeSet.add("WEIGHTED_LEAST_CONNECTIONS");
       algorithmTypeSet.add("WEIGHTED_ROUND_ROBIN");

       algorithmTypeStrs = new String[algorithmTypeSet.size()];

       i = 0;
       for(String status:algorithmTypeSet) {
           algorithmTypeStrs[i]=status;
           i++;
       }
   }

   public boolean contains(String str) {
       boolean out;
       out = algorithmTypeSet.contains(str);
       return out;
   }

   public static String[] values() {
       return algorithmTypeStrs;
   }
}
