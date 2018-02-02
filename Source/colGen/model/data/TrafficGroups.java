package colGen.model.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import colGen.model.heuristic.ClusterHeuristic.ClusterTrafficPairs;

public class TrafficGroups{	
	
	//make a map object
	public Map<Integer,List<ClusterTrafficPairs>> clusterGroups;
	
	//create a object
	public TrafficGroups(){
		this.clusterGroups = new HashMap<Integer,List<ClusterTrafficPairs>>();
	}

	//add cluster
	public void addGroup(int clusterCount, List<ClusterTrafficPairs> clusterList){
		this.clusterGroups.put(clusterCount, clusterList);
	}
	
	
}
