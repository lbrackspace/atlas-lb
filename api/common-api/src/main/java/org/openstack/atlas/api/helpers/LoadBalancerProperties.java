package org.openstack.atlas.api.helpers;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entities.Node;

import java.util.Set;

public class LoadBalancerProperties {

    public static Set<Node> setWeightsforNodes(Set<Node> nodes) {
        LoadBalancer lb;
        LoadBalancerAlgorithm lb_algo;
        if(nodes.isEmpty()) {
            return nodes; // Can't do anything if I can't see the loadbalancer
        }

        lb = nodes.iterator().next().getLoadbalancer();
        if(lb == null) {
            return nodes; //This must be a transient node list Can't do anything with it.
        }
        lb_algo = lb.getAlgorithm();
        if(lb_algo == LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN | lb_algo == LoadBalancerAlgorithm.WEIGHTED_LEAST_CONNECTIONS) {
           for(Node node: nodes) {
               if(node.getWeight() == null) {
                   node.setWeight(1);
               }
           }

        }else {

            //setting weights to null for all other algorithms. JIRA SITESLB-638
            for(Node node : nodes) {
                node.setWeight(null);
            }
        }
        return nodes;

    }
}
