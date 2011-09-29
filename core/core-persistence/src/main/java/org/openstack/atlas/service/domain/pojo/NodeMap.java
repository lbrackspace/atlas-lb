package org.openstack.atlas.service.domain.pojo;

import org.openstack.atlas.service.domain.entity.Node;

import java.util.*;

public class NodeMap {

    private Map<Integer, Node> nodeHashMap;

    public NodeMap() {
        nodeHashMap = new HashMap<Integer, Node>();
    }

    public NodeMap(Collection<Node> nodes){
        nodeHashMap = new HashMap<Integer,Node>();
        for(Node node: nodes){
            nodeHashMap.put(node.getId(), node);
        }
    }

    public boolean containsKey(Integer id){
        return nodeHashMap.containsKey(id);
    }

    public void addNode(Node n) {
        nodeHashMap.put(n.getId(), n);
    }

    public Set<Integer> getIds() {
        return new HashSet<Integer>(nodeHashMap.keySet());
    }

    public static List<Integer> setToSortedList(Set<Integer> ids){
        ArrayList arrayList = new ArrayList();
        for(Integer id : ids){
            arrayList.add(id);
        }
        Collections.sort(arrayList);
        return arrayList;
    }

    public static Set<Integer> listToSet(List<Integer> ids){
        return new HashSet<Integer>(ids);
    }

    public List<Node> getNodesList(Set<Integer>ids){
        List<Node> out=new ArrayList<Node>();
        for(Integer id : setToSortedList(ids)) {
            if(nodeHashMap.containsKey(id)){
                out.add(nodeHashMap.get(id));
            }
        }
        return out;

    }

    public Set<Integer> nodesInConditionAfterDelete(boolean isEnabled,Set<Integer> doomedIds){
        Set<Integer> out = new HashSet<Integer>();
        Set<Integer> nodesThatWillSurvive = getIds();
        nodesThatWillSurvive.removeAll(doomedIds);
        for(Integer id: nodesThatWillSurvive){
            if(nodeHashMap.get(id).isEnabled() == isEnabled) {
                out.add(id);
            }
        }
        return out;
    }

    public Set<Integer> idsThatAreNotInThisMap(Set<Integer> ids){
        Set<Integer> idSet = new HashSet<Integer>(ids);
        idSet.removeAll(getIds());
        return idSet;

    }

    public void clearMap(){
        nodeHashMap = new HashMap<Integer,Node>();
    }
}
