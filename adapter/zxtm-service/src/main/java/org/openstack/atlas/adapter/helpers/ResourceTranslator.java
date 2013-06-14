package org.openstack.atlas.adapter.helpers;

import com.zxtm.service.client.PoolLoadBalancingAlgorithm;
import com.zxtm.service.client.PoolPriorityValueDefinition;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Node;
import org.rackspace.stingray.client.pool.*;

import java.util.ArrayList;
import java.util.List;

public class ResourceTranslator {
    //Translate resouce objects here..


    public Pool translatePoolResource(LoadBalancer loadBalancer) {
        Pool pool = new Pool();
        PoolProperties properties = new PoolProperties();
        PoolBasic basic = new PoolBasic();
        List<PoolNodeWeight> weights = new ArrayList<PoolNodeWeight>();
        PoolLoadbalancing poollb = new PoolLoadbalancing();
        PoolHttp phttp = new PoolHttp();
        PoolConnection connection = new PoolConnection();

        poollb.setAlgorithm(loadBalancer.getAlgorithm().name());

        if (loadBalancer.getAlgorithm().equals(PoolLoadBalancingAlgorithm.wroundrobin)
                || loadBalancer.getAlgorithm().equals(PoolLoadBalancingAlgorithm.wconnections)) {
            PoolNodeWeight nw;
            for (Node n : loadBalancer.getNodes()) {
                nw = new PoolNodeWeight();
                nw.setNode(n.getIpAddress());
                nw.setWeight(n.getWeight());
                weights.add(nw);
            }
            poollb.setNode_weighting(weights);
        }

        ZeusNodePriorityContainer znpc = new ZeusNodePriorityContainer(loadBalancer.getNodes());
        poollb.setPriority_enabled(znpc.hasSecondary());
        PoolPriorityValueDefinition[][] pv = znpc.getPriorityValues();
        pv[0][0].getPriority();
        poollb.setPriority_nodes(znpc.getPriorityValues()[1].length);


        return null;
    }
}
