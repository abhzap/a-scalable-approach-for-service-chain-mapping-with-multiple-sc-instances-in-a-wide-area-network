package colGen.model.heuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ILP.NodePair;
import ILP.TrafficNodes;
import colGen.model.preprocess.PreProcVer1;
import colGen.model.preprocess.preProcFunctions;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class ClusterHeuristic {
	
	//class that holds the multiple groups of (s,d) pairs and their values
	public static class ClusterTrafficPairs{
		public NodePair foundationPair;	
		public int shortestPathValue;//value of the shortest path between source-destination
		public double bcValue;//betweenness-centrality value
		public int sdGroupNum;//group number
		public int sdCount;
		public ArrayList<NodePair> clusterPairs; //maintains a list of node pairs
		public ArrayList<NodePair> extraClusterPairs; //the list of node pairs that were added
		
		//create constructor
		public ClusterTrafficPairs(NodePair foundationPair, int shortestPathLength){
			this.foundationPair = foundationPair;
			this.bcValue = 0.0;
			this.sdGroupNum = 0;
			this.sdCount = 0;
			this.shortestPathValue = shortestPathLength;
			this.clusterPairs = new ArrayList<NodePair>();
			this.extraClusterPairs = new ArrayList<NodePair>();
		}
		
		//update traffic pair
		public void addNodePair(NodePair np){
			this.clusterPairs.add(np);
		}
		
		//add traffic pair to the extra cluster pairs
		public void addExtraNodePair(NodePair np){
			this.extraClusterPairs.add(np);
		}
		
		//update betweenness-centrality value
		public void updateBcValue(double bcValue){
			this.bcValue = bcValue;
		}
		
		//update (s,d) group number
		public void updateSdGroupNum(int sdGroupNum){
			this.sdGroupNum = sdGroupNum;
		}
		
		//update the number of (s,d) pairs 
		public void updateSdCount(int sdCount){
			this.sdCount += sdCount;
		}
	}	
	
	//sort the traffic pairs based on traffic flow values
	//sort in descending order
	public static void sortOnFlowValue(List<TrafficNodes> sdPairs){
		Collections.sort(sdPairs, new Comparator<TrafficNodes>(){
			public int compare(TrafficNodes tn1, TrafficNodes tn2){
				if(tn1.flow_traffic < tn2.flow_traffic) return 1;
				if(tn1.flow_traffic > tn2.flow_traffic) return -1;				
				return 0;
			}
		});
	}

	//sort cluster trafficPairs based on betweenness-centrality values
	//sorting is done in a descending order
	public static void sortOnBcValue(List<ClusterTrafficPairs> bcSdGroups){
		Collections.sort(bcSdGroups, new Comparator<ClusterTrafficPairs>(){
			public int compare(ClusterTrafficPairs cp1, ClusterTrafficPairs cp2){
				if(cp1.bcValue < cp2.bcValue) return 1;
				if(cp1.bcValue > cp2.bcValue) return -1;				
				return 0;
			}
			
		});
	}	
	
	//sort cluster trafficPairs based on (s,d) count values
	//sort based on number of (s,d) pairs
	//in descending order
	public static void sortOnSdCountValue(List<ClusterTrafficPairs> bcSdGroups){
		Collections.sort(bcSdGroups, new Comparator<ClusterTrafficPairs>(){
			public int compare(ClusterTrafficPairs cp1, ClusterTrafficPairs cp2){
				if(cp1.sdCount < cp2.sdCount) return 1;
				if(cp1.sdCount > cp2.sdCount) return -1;				
				return 0;
			}			
		});
	}
	
	//sort on the foundation pair 
	//in ascending order
	public static void sortOnFoundationPair(List<ClusterTrafficPairs> sameBcSdGroups){
		Collections.sort(sameBcSdGroups, new Comparator<ClusterTrafficPairs>(){
			public int compare(ClusterTrafficPairs cp1, ClusterTrafficPairs cp2){
				//foundation pair numbers for both cluster pairs
				int cp1Fp = cp1.foundationPair.v1.get_id()*100 + cp1.foundationPair.v2.get_id();
				int cp2Fp = cp2.foundationPair.v1.get_id()*100 + cp2.foundationPair.v2.get_id();
				if(cp1Fp < cp2Fp) return -1;
				if(cp1Fp > cp2Fp) return 1;				
				return 0;
			}			
		});		
	}
	
	//sort on node pair
	//in ascending order
	public static void sortOnTrafficNodePair(ArrayList<TrafficNodes> sameFlowTrafficPairs){
		Collections.sort(sameFlowTrafficPairs, new Comparator<TrafficNodes>(){
			public int compare(TrafficNodes tn1, TrafficNodes tn2){
				//foundation pair numbers for both cluster pairs
				int tn1Fp = tn1.v1.get_id()*100 + tn1.v2.get_id();
				int tn2Fp = tn2.v1.get_id()*100 + tn2.v2.get_id();
				if(tn1Fp < tn2Fp) return -1;
				if(tn1Fp > tn2Fp) return 1;				
				return 0;
			}			
		});		
	}
	
	//to remove randomness
	//from list of cluster traffic pairs
	public static List<ClusterTrafficPairs> removeRandomnessFromClusterList(List<ClusterTrafficPairs> bcSdGroups){
		List<ClusterTrafficPairs> passBackBcSdGroups =  new ArrayList<ClusterTrafficPairs>();
		//Map holding the same size cluster traffic pair groups
		Map<Integer, ArrayList<ClusterTrafficPairs>> sameBcSdGroups = new TreeMap<Integer, ArrayList<ClusterTrafficPairs>>(); 
		for( ClusterTrafficPairs cp : bcSdGroups){
			if( sameBcSdGroups.get(cp.sdCount) != null ){
				sameBcSdGroups.get(cp.sdCount).add(cp);				
			}else{
				ArrayList<ClusterTrafficPairs> listOfBcSdGroups = new ArrayList<ClusterTrafficPairs>();
				listOfBcSdGroups.add(cp);
				sameBcSdGroups.put(cp.sdCount, listOfBcSdGroups);
			}
		}
		//sort the cluster traffic pairs based on foundation pair values
		Map<Integer, ArrayList<ClusterTrafficPairs>> descSameBcSdGroups = new TreeMap<Integer, ArrayList<ClusterTrafficPairs>>(Collections.reverseOrder());
		descSameBcSdGroups.putAll(sameBcSdGroups);
		for(Map.Entry<Integer, ArrayList<ClusterTrafficPairs>> entry : descSameBcSdGroups.entrySet()){
			sortOnFoundationPair(entry.getValue());
			passBackBcSdGroups.addAll(entry.getValue());
		}
		return passBackBcSdGroups;
	}
	
	//to remove randomness 
	//from list of traffic nodes
	public static ArrayList<TrafficNodes> removeRandomnessFromTrafficList(ArrayList<TrafficNodes> pairList){
		ArrayList<TrafficNodes> randomnessRemovedTrafficList = new ArrayList<TrafficNodes>();
		//Map holding the same size cluster traffic pair groups
		Map<Double, ArrayList<TrafficNodes>> sameFlowTrafficPairs = new TreeMap<Double, ArrayList<TrafficNodes>>(); 
		for( TrafficNodes tn : pairList){
			if( sameFlowTrafficPairs.get(tn.flow_traffic) != null ){
				sameFlowTrafficPairs.get(tn.flow_traffic).add(tn);				
			}else{
				ArrayList<TrafficNodes> listOfTrafficPairs = new ArrayList<TrafficNodes>();
				listOfTrafficPairs.add(tn);
				sameFlowTrafficPairs.put(tn.flow_traffic, listOfTrafficPairs);
			}
		}
		//sort the cluster traffic pairs based on foundation pair values
		Map<Double, ArrayList<TrafficNodes>> descSameBcSdGroups = new TreeMap<Double, ArrayList<TrafficNodes>>(Collections.reverseOrder());
		descSameBcSdGroups.putAll(sameFlowTrafficPairs);
		for(Map.Entry<Double, ArrayList<TrafficNodes>> entry : descSameBcSdGroups.entrySet()){
			sortOnTrafficNodePair(entry.getValue());
			randomnessRemovedTrafficList.addAll(entry.getValue());
		}
		return randomnessRemovedTrafficList;
	}
	
	//print the groups of (s,d) pairs
	public static void printSdGroups(Map<Integer,ClusterTrafficPairs> sdGroups, 
			List<NodePair> zeroBcNodePairs, List<NodePair> bcNodePairs, Map<NodePair,ArrayList<Integer>> pairGroupNums){
		System.out.println("##### (s,d) pair groups #####");
		//print out the sorted map of BC values
		for(Map.Entry<Integer,ClusterTrafficPairs> entry: sdGroups.entrySet()){
			System.out.println(entry.getKey() + " : (" + entry.getValue().foundationPair.v1.get_id() + "," + 
					entry.getValue().foundationPair.v2.get_id() + ")" );
			System.out.println("\tBetweenness Centrality = " + entry.getValue().bcValue);
			System.out.println("\tClustered Pairs = " + entry.getValue().sdCount);
			System.out.println("\tPairs:");			
			//iterate through the node pairs that are part of this cluster
			for(NodePair pr : entry.getValue().clusterPairs){
				System.out.println("\t\t("+pr.v1.get_id()+","+pr.v2.get_id()+")");
			}
		}
		//print out list of zero BC centrality pairs //group Nums they belong to
		System.out.println("\n\n#### Node Pairs not forming Groups #####");
		for(NodePair zeroPr : zeroBcNodePairs){
			System.out.print("(" + zeroPr.v1.get_id() + "," + zeroPr.v2.get_id() + ") = [");
			for(int groupNum : pairGroupNums.get(zeroPr)){
				System.out.print(groupNum+",");
			}
			System.out.println("]");
		}
		//print out list of non-zero BC centrality pairs //group Nums they belong to
		System.out.println("\n\n#### Node Pairs with Groups #####");
		for(NodePair bcPr : bcNodePairs){
			System.out.print("(" + bcPr.v1.get_id() + "," + bcPr.v2.get_id() + ") = [");
			for(int groupNum : pairGroupNums.get(bcPr)){
				System.out.print(groupNum+",");
			}
			System.out.println("]");
		}
	}
	
	//groups that are valid have a non-zero betweenness centrality on the foundation pair of the group
	public static List<ClusterTrafficPairs> getValidSdGroups(List<NodePair> zeroBcNodePairs, List<NodePair> bcNodePairs, 
			Map<NodePair,ClusterTrafficPairs> bcOfNodePair){
		//(s,d) groups with non-zero BC		
		List<ClusterTrafficPairs> bcSdGroups = new ArrayList<ClusterTrafficPairs>();
		//keep track of the pairs with betweenness centrality zero or non-zero
		for(Map.Entry<NodePair,ClusterTrafficPairs> entry: bcOfNodePair.entrySet()){			
			//check if there are no (s,d) pairs in a group
//			if(entry.getValue().clusterPairs.isEmpty()){
			if(entry.getValue().bcValue == 0){
				//add nodes with zero betweenness-centrality
				zeroBcNodePairs.add(entry.getKey());
			}else{			
				//add nodes with non-zero betweenness-centrality
				bcNodePairs.add(entry.getKey());
				//add the (s,d) group to non-zero (s,d) pairs
				bcSdGroups.add(entry.getValue());		
			}			
		}
		return bcSdGroups;
	}
	
	//list cluster numbers to which (s,d) pairs belong
	//iterate through node pairs //find node pair belongs to which clusters
	public static Map<NodePair,ArrayList<Integer>> getGroupNumForPairs(Map<NodePair,List<Path>> shortestSdPaths, 
			LinkedHashMap<Integer,ClusterTrafficPairs> sdGroups){		
		Map<NodePair,ArrayList<Integer>> pairGroupNums= new HashMap<NodePair,ArrayList<Integer>>();
		for(NodePair pr : shortestSdPaths.keySet()){
			//list of group numbers
			ArrayList<Integer> groupNums = new ArrayList<Integer>();
			for(Map.Entry<Integer, ClusterTrafficPairs> entry : sdGroups.entrySet()){
				//either the (s,d) group contains pair or the pair is the foundation pair
//				if(entry.getValue().clusterPairs.contains(pr)||pr.equals(entry.getValue().foundationPair)){
				if(entry.getValue().clusterPairs.contains(pr)){
					//add to the list of group numbers
					groupNums.add(entry.getKey());
				}
			}
			//update the pairGroupNumber Map
			pairGroupNums.put(pr, groupNums);
		}		
		return pairGroupNums;
	}
	
	//function prints out all the cluster traffic Pairs
	public static void printBcGroups(List<ClusterTrafficPairs> pairClusters){	
		//clusters formed
		//get the list of clusters
		System.out.println("\n\nPrinting out the clusters to be used!");
		int ClusterNumber = 0;	
		for(ClusterTrafficPairs entry: pairClusters){
			System.out.println("\nCluster Number: " + ClusterNumber);
			System.out.println("Foundation Pair: (" + entry.foundationPair.v1.get_id() + "," + entry.foundationPair.v2.get_id()+ ")");
			System.out.println("\t(s,d) pairs:" + entry.sdCount);
			for(NodePair pr : entry.clusterPairs){
				System.out.println("\t\t("+pr.v1.get_id()+","+pr.v2.get_id()+")");
			}		
			//increment the cluster number
			ClusterNumber++;
		}
	}
	
    //function prints out all the cluster traffic Pairs
	public static void printClusters(List<ClusterTrafficPairs> pairClusters){	
		//clusters formed
		//get the list of clusters
		//System.out.println("Printing out the clusters to be used!");
		int ClusterNumber = 0;
		int totalSdPairs = 0;
		for(ClusterTrafficPairs entry: pairClusters){
			System.out.println("\tCluster Number: " + ClusterNumber);
			System.out.println("\t\tFoundation Pair: (" + entry.foundationPair.v1.get_id() + "," + entry.foundationPair.v2.get_id()+ ")");
			System.out.println("\t\t\t(s,d) pairs:" + entry.sdCount);
			for(NodePair pr : entry.clusterPairs){
				System.out.println("\t\t\t\t("+pr.v1.get_id()+","+pr.v2.get_id()+")");
			}
			//increase the total of (s,d) pairs
			totalSdPairs += entry.clusterPairs.size();
			System.out.println("\t\tTotal (s,d) pairs: " + totalSdPairs);
			//increment the cluster number
			ClusterNumber++;
		}
	}
	
	/*adjacency of source vertex becomes source cluster, adjacency of destination cluster becomes destination cluster*/
	public static void clusteringBasedOnAdj(Graph g, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN, List<Integer> scUsed, Map<Integer,ArrayList<Integer>> scCopies,
			 Map<Integer,Integer> scCopyToSC, ArrayList<Integer> scCopyUsed){
		 //iterate through the set of (s,d) pairs for a service chain
		 for(int scId : scUsed){				 
			 ArrayList<TrafficNodes> sdPairsPerSC = serviceChainTN.get(scId);
			 int clusterNum = 0;
			 ArrayList<Integer> idCopies = new ArrayList<Integer>();
			 while(!sdPairsPerSC.isEmpty()){
				 int scCopyId = 1000*scId + clusterNum;		
				 //add Id to Id copies
				 idCopies.add(scCopyId);
				 //keep track of service chain
				 scCopyToSC.put(scCopyId,scId);
				 //new list of service chains
				 scCopyUsed.add(scCopyId);
				 //choose a traffic pair (s,d)
				 TrafficNodes sdPick = sdPairsPerSC.get(0);
				 BaseVertex srcVrt = sdPick.v1;
				 BaseVertex destVrt = sdPick.v2;
				 //find adjacency list
				 Set<BaseVertex> srcAdj = new HashSet<BaseVertex>(g.get_adjacent_vertices(srcVrt));
				 //also add the srcVrt
				 srcAdj.add(srcVrt);
				 //find adjacency list
				 Set<BaseVertex> destAdj = new HashSet<BaseVertex>(g.get_adjacent_vertices(destVrt));
				 //also add the destination vertex
				 destAdj.add(destVrt);
				 //keep track of pairs in cluster
				 ArrayList<TrafficNodes> sdPairsInCluster = new ArrayList<TrafficNodes>();
				 //updateID of pick
				 sdPick.updateChainIndex(scCopyId);
				 //add pick to cluster
				 sdPairsInCluster.add(sdPick);
				 for(TrafficNodes sd : sdPairsPerSC){
					 if(srcAdj.contains(sd.v1)&&destAdj.contains(sd.v2)){
						 //update the service chain Id
						 sd.updateChainIndex(scCopyId);
						 //add sd pairs to cluster
						 sdPairsInCluster.add(sd);
					 }
				 }
				 //remove the pairs from the cluster
				 sdPairsPerSC.removeAll(sdPairsInCluster);					 
				 //increase the cluster number
				 clusterNum++;
			 }
			 //add the copyId's to the Id
			 scCopies.put(scId,idCopies);				 
		 }			 
	 }
	
	//form clusters when there is a all mesh traffic
	//store shortest paths for each (s,d) pair
	public static List<ClusterTrafficPairs> formClustersWhenAllSdPairs(Graph g){	
			Map<NodePair, List<Path>> shortestSdPaths = PreProcVer1.allShortestPathsForAllPairs(g);
			//get the list of (s,d) pairs
			ArrayList<NodePair> pairList = new ArrayList<NodePair>(shortestSdPaths.keySet());
				
			//calculate betweenness centrality for ordered node pair
			Map<NodePair,ClusterTrafficPairs> bcOfNodePair = PreProcVer1.calculateBetweennessCentralityForOrderedNodePairs(g, shortestSdPaths, pairList);	
					
			//List the Node Pairs which have zero BC
			List<NodePair> zeroBcNodePairs = new ArrayList<NodePair>();
			//List the Node Pairs with non-zero BC
			List<NodePair> bcNodePairs = new ArrayList<NodePair>();
			//(s,d) groups with non-zero BC		
			List<ClusterTrafficPairs> bcSdGroups = ClusterHeuristic.getValidSdGroups(zeroBcNodePairs, bcNodePairs, bcOfNodePair);		
			
			//sort the list of cluster traffic pairs based on betweenness-centrality values		
			//sorting in descending order	
			//ClusterHeuristic.sortOnBcValue(bcSdGroups);
			//sort the list of cluster traffic pairs based on number of (s,d) pairs	
			//sorting in descending order
			ClusterHeuristic.sortOnSdCountValue(bcSdGroups);		
			
			//create a linked HashMap
			//with the clusterPairs as values and the group no's as keys
			LinkedHashMap<Integer,ClusterTrafficPairs> sdGroups = new LinkedHashMap<Integer,ClusterTrafficPairs>();
			int sdGroupNum = 0;
			for(ClusterTrafficPairs cp : bcSdGroups){
				cp.updateSdGroupNum(sdGroupNum);
				sdGroups.put(sdGroupNum, cp);
				sdGroupNum++;
			}
					
			//list group numbers to which (s,d) pairs belong
			//iterate through node pairs //find node pair belongs to which clusters
			Map<NodePair,ArrayList<Integer>> pairGroupNums = ClusterHeuristic.getGroupNumForPairs(shortestSdPaths, sdGroups);		
			
			//print out the relevant data structures
//				ClusterHeuristic.printSdGroups(sdGroups, zeroBcNodePairs, bcNodePairs, pairGroupNums);	
			
			
			//reference to new generated NodePairGroup
			Map<NodePair,ClusterTrafficPairs> refNodePairGroup;
			//get the list of cluster traffic pairs
			List<ClusterTrafficPairs> refPairList;
			//create new group numbers
			List<ClusterTrafficPairs> pairClusters = new ArrayList<ClusterTrafficPairs>();
			//the set of all (s,d) pairs
			ArrayList<NodePair> allSdPairsRemaining = new ArrayList<NodePair>(shortestSdPaths.keySet());		
				//add to the sdPairGroup
				pairClusters.add(bcSdGroups.get(0));
				//remove the list of (s,d) pairs
				allSdPairsRemaining.removeAll(bcSdGroups.get(0).clusterPairs);			
			
			//when zero traffic count is encountered	
			boolean clearAllTrafficPairs = false;	
			//iterate through the sorted list of groups
			while(!allSdPairsRemaining.isEmpty()){
				//the new (s,d) clusters based on the remaining (s,d) pairs
				refNodePairGroup = PreProcVer1.calculateBetweennessCentralityForOrderedNodePairs(g, shortestSdPaths, allSdPairsRemaining);
				//get the groups of (s,d) pairs
				refPairList = new ArrayList<ClusterTrafficPairs>(refNodePairGroup.values());
				//sort the list //sorting in descending order
				ClusterHeuristic.sortOnSdCountValue(refPairList);
				if(refPairList.get(0).sdCount == 0){
					//set boolean flag
					clearAllTrafficPairs = true;
					//add all the (s,d) pair clusters
					for(ClusterTrafficPairs ctpr : refPairList){
						//add to the sdPairGroup
						pairClusters.add(ctpr);
					}
				}else{
					//add to the sdPairGroup
					pairClusters.add(refPairList.get(0));
				}			
				if(!clearAllTrafficPairs){
					//remove the list of (s,d) pairs
					allSdPairsRemaining.removeAll(refPairList.get(0).clusterPairs);
				}else{
					//clear the list of (s,d) pairs
					allSdPairsRemaining.clear();
				}
			}
			
			//return the object
			return pairClusters;
		}
	
	//form the clusters given the number of clusters to be formed
	public static List<ClusterTrafficPairs> formClusters(Graph g, int clusterCount){		
		
		//store shortest paths for each (s,d) pair
		Map<NodePair, List<Path>> shortestSdPaths = PreProcVer1.allShortestPathsForAllPairs(g);
		//get the list of (s,d) pairs
		ArrayList<NodePair> pairList = new ArrayList<NodePair>(shortestSdPaths.keySet());
			
		//calculate betweenness centrality for ordered node pair
		Map<NodePair,ClusterTrafficPairs> bcOfNodePair = PreProcVer1.calculateBetweennessCentralityForOrderedNodePairs(g, shortestSdPaths, pairList);	
				
		//List the Node Pairs which have zero BC
		List<NodePair> zeroBcNodePairs = new ArrayList<NodePair>();
		//List the Node Pairs with non-zero BC
		List<NodePair> bcNodePairs = new ArrayList<NodePair>();
		//(s,d) groups with non-zero BC		
		List<ClusterTrafficPairs> bcSdGroups = ClusterHeuristic.getValidSdGroups(zeroBcNodePairs, bcNodePairs, bcOfNodePair);		
		
		//sort the list of cluster traffic pairs based on betweenness-centrality values		
		//sorting in descending order	
		//ClusterHeuristic.sortOnBcValue(bcSdGroups);
		//sort the list of cluster traffic pairs based on number of (s,d) pairs	
		//sorting in descending order
		ClusterHeuristic.sortOnSdCountValue(bcSdGroups);		
		
		//create a linked HashMap
		//with the clusterPairs as values and the group no's as keys
		LinkedHashMap<Integer,ClusterTrafficPairs> sdGroups = new LinkedHashMap<Integer,ClusterTrafficPairs>();
		int sdGroupNum = 0;
		for(ClusterTrafficPairs cp : bcSdGroups){
			cp.updateSdGroupNum(sdGroupNum);
			sdGroups.put(sdGroupNum, cp);
			sdGroupNum++;
		}
				
		//list group numbers to which (s,d) pairs belong
		//iterate through node pairs //find node pair belongs to which clusters
		Map<NodePair,ArrayList<Integer>> pairGroupNums = ClusterHeuristic.getGroupNumForPairs(shortestSdPaths, sdGroups);		
		
		//print out the relevant data structures
//		ClusterHeuristic.printSdGroups(sdGroups, zeroBcNodePairs, bcNodePairs, pairGroupNums);	
		
		
		//reference to new generated NodePairGroup
		Map<NodePair,ClusterTrafficPairs> refNodePairGroup;
		//get the list of cluster traffic pairs
		List<ClusterTrafficPairs> refPairList;
		//create new group numbers
		ArrayList<ClusterTrafficPairs> pairClusters = new ArrayList<ClusterTrafficPairs>();
		//the set of all (s,d) pairs
		ArrayList<NodePair> allSdPairsRemaining = new ArrayList<NodePair>(shortestSdPaths.keySet());		
			//add to the sdPairGroup
			pairClusters.add(bcSdGroups.get(0));
			//remove the list of (s,d) pairs
			allSdPairsRemaining.removeAll(bcSdGroups.get(0).clusterPairs);			
		
		//when zero traffic count is encountered	
		boolean clearAllTrafficPairs = false;	
		//iterate through the sorted list of groups
		while(!allSdPairsRemaining.isEmpty()){
			//the new (s,d) clusters based on the remaining (s,d) pairs
			refNodePairGroup = PreProcVer1.calculateBetweennessCentralityForOrderedNodePairs(g, shortestSdPaths, allSdPairsRemaining);
			//get the groups of (s,d) pairs
			refPairList = new ArrayList<ClusterTrafficPairs>(refNodePairGroup.values());
			//sort the list //sorting in descending order
			ClusterHeuristic.sortOnSdCountValue(refPairList);
			if(refPairList.get(0).sdCount == 0){
				//set boolean flag
				clearAllTrafficPairs = true;
				//add all the (s,d) pair clusters
				for(ClusterTrafficPairs ctpr : refPairList){
					//add to the sdPairGroup
					pairClusters.add(ctpr);
				}
			}else{
				//add to the sdPairGroup
				pairClusters.add(refPairList.get(0));
			}			
			if(!clearAllTrafficPairs){
				//remove the list of (s,d) pairs
				allSdPairsRemaining.removeAll(refPairList.get(0).clusterPairs);
			}else{
				//clear the list of (s,d) pairs
				allSdPairsRemaining.clear();
			}
		}	
		
		//clusters formed
		//get the list of clusters
		/*System.out.println("\n\nPrinting out the clusters to be used!");
		int ClusterNumber = 0;
		for(ClusterTrafficPairs entry: pairClusters){
			System.out.println("Cluster Number: " + ClusterNumber);
			System.out.println("Foundation Pair: (" + entry.foundationPair.v1.get_id() + "," + entry.foundationPair.v2.get_id()+ ")");
			System.out.println("\t(s,d) pairs:" + entry.sdCount);
			for(NodePair pr : entry.clusterPairs){
				System.out.println("\t\t("+pr.v1.get_id()+","+pr.v2.get_id()+")");
			}
			//increment the cluster number
			ClusterNumber++;
		}*/
				
		//number of clusters to be used
		List<ClusterTrafficPairs> clusterGroups = new ArrayList<ClusterTrafficPairs>();
		//the set of all (s,d) pairs
		allSdPairsRemaining = new ArrayList<NodePair>(shortestSdPaths.keySet());		
		//iterate through cluster no.'s
		for(int count=0; count<clusterCount; count++){
			//update the group num
			pairClusters.get(count).updateSdGroupNum(count);
			//add the cluster groups
			clusterGroups.add(pairClusters.get(count));
			//remove the list of (s,d) pairs
			allSdPairsRemaining.removeAll(pairClusters.get(count).clusterPairs);
		}
		
		//add the remaining (s,d) pairs to the clusters
		//based on the shortest path to any of the clusters
		for(NodePair pr : allSdPairsRemaining){	
			//it is the index of the cluster in the array list
			int chosenCluster = 0;
			ClusterTrafficPairs clusterPair = clusterGroups.get(chosenCluster);
			//total path length //path length to source foundation vertex //path length to destination foundation vertex
			int totalPathLength = clusterPair.shortestPathValue;
			if(pr.v1.get_id() != clusterPair.foundationPair.v1.get_id()){
				totalPathLength += shortestSdPaths.get(new NodePair(pr.v1,clusterPair.foundationPair.v1)).get(0).get_vertices().size()-1;
			}
			if(pr.v2.get_id() != clusterPair.foundationPair.v2.get_id()){
				totalPathLength += shortestSdPaths.get(new NodePair(clusterPair.foundationPair.v2,pr.v2)).get(0).get_vertices().size()-1;
			}
					 
			//iterate through the remaining clusters
			for(int count=1; count<clusterCount; count++){
				clusterPair = clusterGroups.get(count);
				int tempPathLength = clusterPair.shortestPathValue;
				if(pr.v1.get_id() != clusterPair.foundationPair.v1.get_id()){
					tempPathLength += shortestSdPaths.get(new NodePair(pr.v1,clusterPair.foundationPair.v1)).get(0).get_vertices().size()-1;
				}
				if(pr.v2.get_id() != clusterPair.foundationPair.v2.get_id()){
					tempPathLength += shortestSdPaths.get(new NodePair(clusterPair.foundationPair.v2,pr.v2)).get(0).get_vertices().size()-1;
				}
				//check if length of the path is smaller than previous one
				if(tempPathLength < totalPathLength){
					//assign new path length
					totalPathLength = tempPathLength;
					//assign the chosen cluster
					chosenCluster = count;
				}
			}
			
			//add the (s,d) pair to the chosen cluster
			//this cluster gives the shortest path
			clusterGroups.get(chosenCluster).addNodePair(pr);
			clusterGroups.get(chosenCluster).addExtraNodePair(pr);
			//update total (s,d) count
			clusterGroups.get(chosenCluster).sdCount = clusterGroups.get(chosenCluster).clusterPairs.size();
		}
		
		//return the cluster groups
		return clusterGroups;
		
	}
	
	//form the clusters given the number of clusters to be formed //all traffic pairs in the network
	//modified version of formClusters where the addition of trafficNodes to clusters is more refined
	public static List<ClusterTrafficPairs> formClustersV2(Graph g, int clusterCount){		
				
			//store shortest paths for each (s,d) pair
			Map<NodePair, List<Path>> shortestSdPaths = PreProcVer1.allShortestPathsForAllPairs(g);
			//store all the (s,d) paths
			HashMap<NodePair,List<Path>> allSdPaths = preProcFunctions.findRoutesForSDpairs(g);
			    
			//get the list of (s,d) pairs
			ArrayList<NodePair> pairList = new ArrayList<NodePair>(shortestSdPaths.keySet());
				
			//calculate betweenness centrality for ordered node pair
			Map<NodePair,ClusterTrafficPairs> bcOfNodePair = PreProcVer1.calculateBetweennessCentralityForOrderedNodePairs(g, shortestSdPaths, pairList);	
					
			//List the Node Pairs which have zero BC
			List<NodePair> zeroBcNodePairs = new ArrayList<NodePair>();
			//List the Node Pairs with non-zero BC
			List<NodePair> bcNodePairs = new ArrayList<NodePair>();
			//(s,d) groups with non-zero BC		
			List<ClusterTrafficPairs> bcSdGroups = ClusterHeuristic.getValidSdGroups(zeroBcNodePairs, bcNodePairs, bcOfNodePair);		
			
			//sort the list of cluster traffic pairs based on betweenness-centrality values		
			//sorting in descending order	
			//ClusterHeuristic.sortOnBcValue(bcSdGroups);
			//sort the list of cluster traffic pairs based on number of (s,d) pairs	
			//sorting in descending order
			ClusterHeuristic.sortOnSdCountValue(bcSdGroups);		
			
			//create a linked HashMap
			//with the clusterPairs as values and the group no's as keys
			LinkedHashMap<Integer,ClusterTrafficPairs> sdGroups = new LinkedHashMap<Integer,ClusterTrafficPairs>();
			int sdGroupNum = 0;
			for(ClusterTrafficPairs cp : bcSdGroups){
				cp.updateSdGroupNum(sdGroupNum);
				sdGroups.put(sdGroupNum, cp);
				sdGroupNum++;
			}
					
			//list group numbers to which (s,d) pairs belong
			//iterate through node pairs //find node pair belongs to which clusters
			Map<NodePair,ArrayList<Integer>> pairGroupNums = ClusterHeuristic.getGroupNumForPairs(shortestSdPaths, sdGroups);		
			
			//print out the relevant data structures
//					ClusterHeuristic.printSdGroups(sdGroups, zeroBcNodePairs, bcNodePairs, pairGroupNums);	
			
			
			//reference to new generated NodePairGroup
			Map<NodePair,ClusterTrafficPairs> refNodePairGroup;
			//get the list of cluster traffic pairs
			List<ClusterTrafficPairs> refPairList;
			//create new group numbers
			ArrayList<ClusterTrafficPairs> pairClusters = new ArrayList<ClusterTrafficPairs>();
			//the set of all (s,d) pairs
			ArrayList<NodePair> allSdPairsRemaining = new ArrayList<NodePair>(shortestSdPaths.keySet());		
				//add to the sdPairGroup
				pairClusters.add(bcSdGroups.get(0));
				//remove the list of (s,d) pairs
				allSdPairsRemaining.removeAll(bcSdGroups.get(0).clusterPairs);	
				
			
			//when zero traffic count is encountered	
			boolean clearAllTrafficPairs = false;	
			//iterate through the sorted list of groups
			while(!allSdPairsRemaining.isEmpty()){
				//the new (s,d) clusters based on the remaining (s,d) pairs
				refNodePairGroup = PreProcVer1.calculateBetweennessCentralityForOrderedNodePairs(g, shortestSdPaths, allSdPairsRemaining);
				//get the groups of (s,d) pairs
				refPairList = new ArrayList<ClusterTrafficPairs>(refNodePairGroup.values());
				//sort the list //sorting in descending order
				ClusterHeuristic.sortOnSdCountValue(refPairList);
				if(refPairList.get(0).sdCount == 0){
					//set boolean flag
					clearAllTrafficPairs = true;
					//add all the (s,d) pair clusters
					for(ClusterTrafficPairs ctpr : refPairList){
						//add to the sdPairGroup
						pairClusters.add(ctpr);
					}
				}else{
					//add to the sdPairGroup
					pairClusters.add(refPairList.get(0));
				}			
				if(!clearAllTrafficPairs){
					//remove the list of (s,d) pairs
					allSdPairsRemaining.removeAll(refPairList.get(0).clusterPairs);
				}else{
					//clear the list of (s,d) pairs
					allSdPairsRemaining.clear();
				}
			}	
			
			//clusters formed
			//get the list of clusters
			/*System.out.println("\n\nPrinting out the clusters to be used!");
			int ClusterNumber = 0;
			for(ClusterTrafficPairs entry: pairClusters){
				System.out.println("Cluster Number: " + ClusterNumber);
				System.out.println("Foundation Pair: (" + entry.foundationPair.v1.get_id() + "," + entry.foundationPair.v2.get_id()+ ")");
				System.out.println("\t(s,d) pairs:" + entry.sdCount);
				for(NodePair pr : entry.clusterPairs){
					System.out.println("\t\t("+pr.v1.get_id()+","+pr.v2.get_id()+")");
				}
				//increment the cluster number
				ClusterNumber++;
			}*/
					
			//number of clusters to be used
			List<ClusterTrafficPairs> clusterGroups = new ArrayList<ClusterTrafficPairs>();
			//the set of all (s,d) pairs
			allSdPairsRemaining = new ArrayList<NodePair>(shortestSdPaths.keySet());		
			//iterate through cluster no.'s
			for(int count=0; count<clusterCount; count++){
				//update the group num
				pairClusters.get(count).updateSdGroupNum(count);
				//add the cluster groups
				clusterGroups.add(pairClusters.get(count));
				//remove the list of (s,d) pairs
				allSdPairsRemaining.removeAll(pairClusters.get(count).clusterPairs);
			}
			
			//add the remaining (s,d) pairs to the clusters
			//based on the shortest path to any of the clusters
			for(NodePair pr : allSdPairsRemaining){	
				//it is the index of the cluster in the array list
				int chosenCluster = 0;
				ClusterTrafficPairs clusterPair = clusterGroups.get(chosenCluster);
				//total path length //path length to source foundation vertex //path length to destination foundation vertex
				int totalPathLength = clusterPair.shortestPathValue;
				if(pr.v1.get_id() != clusterPair.foundationPair.v1.get_id()){
					//find the path which does not include the destination
					NodePair np = new NodePair(pr.v1,clusterPair.foundationPair.v1);
					//iterate through the paths
					for(Path p : allSdPaths.get(np)){
						//path from source to source of node pair
						//path should not contain the destination
						if(!p.get_vertices().contains(clusterPair.foundationPair.v2)){
							totalPathLength += p.get_vertices().size()-1;
						}
					}					
				}
				if(pr.v2.get_id() != clusterPair.foundationPair.v2.get_id()){
					//find the path which does not include the destination
					NodePair np = new NodePair(clusterPair.foundationPair.v2,pr.v2);
					//iterate through the paths
					for(Path p : allSdPaths.get(np)){
						//path from target of node pair to destination
						//path should not contain the source
						if(!p.get_vertices().contains(clusterPair.foundationPair.v1)){
							totalPathLength += p.get_vertices().size()-1;
						}
					}					
				}
						 
				//iterate through the remaining clusters
				for(int count=1; count<clusterCount; count++){
					clusterPair = clusterGroups.get(count);
					int tempPathLength = clusterPair.shortestPathValue;
					if(pr.v1.get_id() != clusterPair.foundationPair.v1.get_id()){
						//find the path which does not include the destination
						NodePair np = new NodePair(pr.v1,clusterPair.foundationPair.v1);
						//iterate through the paths
						for(Path p : allSdPaths.get(np)){
							//path from source to source of node pair
							//path should not contain the destination
							if(!p.get_vertices().contains(clusterPair.foundationPair.v2)){
								tempPathLength += p.get_vertices().size()-1;
							}
						}					
					}
					if(pr.v2.get_id() != clusterPair.foundationPair.v2.get_id()){
						//find the path which does not include the destination
						NodePair np = new NodePair(clusterPair.foundationPair.v2,pr.v2);
						//iterate through the paths
						for(Path p : allSdPaths.get(np)){
							//path from target of node pair to destination
							//path should not contain the source
							if(!p.get_vertices().contains(clusterPair.foundationPair.v1)){
								tempPathLength += p.get_vertices().size()-1;
							}
						}					
					}
					//check if length of the path is smaller than previous one
					if(tempPathLength < totalPathLength){
						//assign new path length
						totalPathLength = tempPathLength;
						//assign the chosen cluster
						chosenCluster = count;
					}
				}
				
				//add the (s,d) pair to the chosen cluster
				//this cluster gives the shortest path
				clusterGroups.get(chosenCluster).addNodePair(pr);
				clusterGroups.get(chosenCluster).addExtraNodePair(pr);
				//update total (s,d) count
				clusterGroups.get(chosenCluster).sdCount = clusterGroups.get(chosenCluster).clusterPairs.size();
			}
			
			//return the cluster groups
			return clusterGroups;				
		}
	
	//form clusters when given list of traffic flows
	//store shortest paths for each (s,d) pair
	public static ArrayList<ClusterTrafficPairs> formClustersForGivenSdPairs(Graph g, List<TrafficNodes> allSdFlows){	
		//store shortest paths for each (s,d) pair
		Map<NodePair, List<Path>> shortestSdPaths = PreProcVer1.allShortestPathsForAllPairs(g);
		//store all the (s,d) paths
		HashMap<NodePair,List<Path>> allSdPaths = preProcFunctions.findRoutesForSDpairs(g);
		    
		//get the list of (s,d) pairs
		List<NodePair> pairList = new ArrayList<NodePair>();		
		for(TrafficNodes sdFlow : allSdFlows){
			pairList.add(new NodePair(sdFlow.v1, sdFlow.v2));
		}
			
		//calculate betweenness centrality for ordered node pair
		Map<NodePair,ClusterTrafficPairs> bcOfNodePair = PreProcVer1.calculateBetweennessCentralityForOrderedNodePairs(g, shortestSdPaths, pairList);	
				
		//List the Node Pairs which have zero BC
		List<NodePair> zeroBcNodePairs = new ArrayList<NodePair>();
		//List the Node Pairs with non-zero BC
		List<NodePair> bcNodePairs = new ArrayList<NodePair>();
		//(s,d) groups with non-zero BC		
		List<ClusterTrafficPairs> bcSdGroups = ClusterHeuristic.getValidSdGroups(zeroBcNodePairs, bcNodePairs, bcOfNodePair);		
		
		//sort the list of cluster traffic pairs based on betweenness-centrality values		
		//sorting in descending order	
		//ClusterHeuristic.sortOnBcValue(bcSdGroups);
		//sort the list of cluster traffic pairs based on number of (s,d) pairs	
		//sorting in descending order
		ClusterHeuristic.sortOnSdCountValue(bcSdGroups);	
		//remove randomness
		bcSdGroups = ClusterHeuristic.removeRandomnessFromClusterList(bcSdGroups);
		
		//create a linked HashMap
		//with the clusterPairs as values and the group no's as keys
		LinkedHashMap<Integer,ClusterTrafficPairs> sdGroups = new LinkedHashMap<Integer,ClusterTrafficPairs>();
		int sdGroupNum = 0;
		for(ClusterTrafficPairs cp : bcSdGroups){
			cp.updateSdGroupNum(sdGroupNum);
			sdGroups.put(sdGroupNum, cp);
			sdGroupNum++;
		}
				
		//list group numbers to which (s,d) pairs belong
		//iterate through node pairs //find node pair belongs to which clusters
		Map<NodePair,ArrayList<Integer>> pairGroupNums = ClusterHeuristic.getGroupNumForPairs(shortestSdPaths, sdGroups);		
		
		//print out the relevant data structures
//		ClusterHeuristic.printSdGroups(sdGroups, zeroBcNodePairs, bcNodePairs, pairGroupNums);	
		
		
		//reference to new generated NodePairGroup
		Map<NodePair,ClusterTrafficPairs> refNodePairGroup;
		//get the list of cluster traffic pairs
		List<ClusterTrafficPairs> refPairList;
		//create new group numbers
		ArrayList<ClusterTrafficPairs> pairClusters = new ArrayList<ClusterTrafficPairs>();			
		//the set of all traffic nodes
		ArrayList<TrafficNodes> allSdPairsRemaining = new ArrayList<TrafficNodes>(allSdFlows);	
		//remove randomness
		allSdPairsRemaining = ClusterHeuristic.removeRandomnessFromTrafficList(allSdPairsRemaining);
			//add to the sdPairGroup
			pairClusters.add(bcSdGroups.get(0));
			//remove the list of (s,d) pairs
			//allSdPairsRemaining.removeAll(bcSdGroups.get(0).clusterPairs);
			//remove the list of (s,d) pairs
			for(NodePair np : bcSdGroups.get(0).clusterPairs){
				Iterator<TrafficNodes> itr = allSdPairsRemaining.iterator();
				while(itr.hasNext()){
					TrafficNodes tn = itr.next();
					if(tn.equals(np)){						
						itr.remove();
						break;
					}
				}
			}
			
		
		//when zero traffic count is encountered	
		boolean clearAllTrafficPairs = false;	
		//iterate through the sorted list of groups
		while(!allSdPairsRemaining.isEmpty()){
			//the new (s,d) clusters based on the remaining (s,d) pairs
			refNodePairGroup = PreProcVer1.calculateBetweennessCentralityForOrderedNodePairs(g, shortestSdPaths, allSdPairsRemaining);
			//get the groups of (s,d) pairs
			refPairList = new ArrayList<ClusterTrafficPairs>(refNodePairGroup.values());
			//sort the list //sorting in descending order
			ClusterHeuristic.sortOnSdCountValue(refPairList);
			if(refPairList.get(0).sdCount == 0){
				//set boolean flag
				clearAllTrafficPairs = true;
				//add all the (s,d) pair clusters
				for(ClusterTrafficPairs ctpr : refPairList){
					//add to the sdPairGroup
					pairClusters.add(ctpr);
				}
			}else{
				//add to the sdPairGroup
				pairClusters.add(refPairList.get(0));
			}			
			if(!clearAllTrafficPairs){
				//remove the list of (s,d) pairs						
				for(NodePair np : refPairList.get(0).clusterPairs){
					Iterator<TrafficNodes> itr = allSdPairsRemaining.iterator();
					while(itr.hasNext()){
						TrafficNodes tn = itr.next();
						if(tn.equals(np)){							
							itr.remove();
							break;
						}
					}
				}
			}else{
				//clear the list of (s,d) pairs
				allSdPairsRemaining.clear();
			}
		}
			
		//return the object
		return pairClusters;
	}
		
	//form the clusters given the number of clusters to be formed //given traffic pairs in the network	
	public static ArrayList<ClusterTrafficPairs> formClustersV2(Graph g, int clusterCount, List<TrafficNodes> allSdFlows){		
					
			//store shortest paths for each (s,d) pair
			Map<NodePair, List<Path>> shortestSdPaths = PreProcVer1.allShortestPathsForAllPairs(g);
			//store all the k-shortest (s,d) paths
			HashMap<NodePair,List<Path>> allSdPaths = preProcFunctions.findRoutesForSDpairs(g);
			    
			//get the list of (s,d) pairs
			List<NodePair> pairList = new ArrayList<NodePair>();		
			for(TrafficNodes sdFlow : allSdFlows){
				pairList.add(new NodePair(sdFlow.v1, sdFlow.v2));
			}
				
			//calculate betweenness centrality for ordered node pair
			Map<NodePair,ClusterTrafficPairs> bcOfNodePair = PreProcVer1.calculateBetweennessCentralityForOrderedNodePairs(g, shortestSdPaths, pairList);	
					
			//List the Node Pairs which have zero BC
			List<NodePair> zeroBcNodePairs = new ArrayList<NodePair>();
			//List the Node Pairs with non-zero BC
			List<NodePair> bcNodePairs = new ArrayList<NodePair>();
			//(s,d) groups with non-zero BC		
			List<ClusterTrafficPairs> bcSdGroups = ClusterHeuristic.getValidSdGroups(zeroBcNodePairs, bcNodePairs, bcOfNodePair);		
			
			//sort the list of cluster traffic pairs based on betweenness-centrality values		
			//sorting in descending order	
			//ClusterHeuristic.sortOnBcValue(bcSdGroups);
			//sort the list of cluster traffic pairs based on number of (s,d) pairs	
			//sorting in descending order
			ClusterHeuristic.sortOnSdCountValue(bcSdGroups);
			//remove randomness
			bcSdGroups = ClusterHeuristic.removeRandomnessFromClusterList(bcSdGroups);
			//print the bcGroups
			//ClusterHeuristic.printBcGroups(bcSdGroups);
			
			//create a linked HashMap
			//with the clusterPairs as values and the group no's as keys
			LinkedHashMap<Integer,ClusterTrafficPairs> sdGroups = new LinkedHashMap<Integer,ClusterTrafficPairs>();
			int sdGroupNum = 0;
			for(ClusterTrafficPairs cp : bcSdGroups){
				cp.updateSdGroupNum(sdGroupNum);
				sdGroups.put(sdGroupNum, cp);
				sdGroupNum++;
			}
					
			//list group numbers to which (s,d) pairs belong
			//iterate through node pairs //find node pair belongs to which clusters
			Map<NodePair,ArrayList<Integer>> pairGroupNums = ClusterHeuristic.getGroupNumForPairs(shortestSdPaths, sdGroups);		
			
			//print out the relevant data structures
//			ClusterHeuristic.printSdGroups(sdGroups, zeroBcNodePairs, bcNodePairs, pairGroupNums);	
			
			
			//reference to new generated NodePairGroup
			Map<NodePair,ClusterTrafficPairs> refNodePairGroup;
			//get the list of cluster traffic pairs
			List<ClusterTrafficPairs> refPairList;
			//create new group numbers
			ArrayList<ClusterTrafficPairs> pairClusters = new ArrayList<ClusterTrafficPairs>();			
			//the set of all traffic nodes
			ArrayList<TrafficNodes> allSdPairsRemaining = new ArrayList<TrafficNodes>(allSdFlows);
			//remove randomness
			allSdPairsRemaining = ClusterHeuristic.removeRandomnessFromTrafficList(allSdPairsRemaining);
				//add to the sdPairGroup
				pairClusters.add(bcSdGroups.get(0));
				//remove the list of (s,d) pairs
				//allSdPairsRemaining.removeAll(bcSdGroups.get(0).clusterPairs);
				//remove the list of (s,d) pairs
				for(NodePair np : bcSdGroups.get(0).clusterPairs){
					Iterator<TrafficNodes> itr = allSdPairsRemaining.iterator();
					while(itr.hasNext()){
						TrafficNodes tn = itr.next();
						if(tn.equals(np)){
							//System.out.println("\t\tRemoving traffic node (" + tn.v1.get_id() + "," + tn.v2.get_id() + ")");
							itr.remove();
							break;
						}
					}
				}
				
			
			//when zero traffic count is encountered	
			boolean clearAllTrafficPairs = false;	
			//iterate through the sorted list of groups
			while(!allSdPairsRemaining.isEmpty()){
				//the new (s,d) clusters based on the remaining (s,d) pairs
				refNodePairGroup = PreProcVer1.calculateBetweennessCentralityForOrderedNodePairs(g, shortestSdPaths, allSdPairsRemaining);
				//get the groups of (s,d) pairs
				refPairList = new ArrayList<ClusterTrafficPairs>(refNodePairGroup.values());
				//sort the list //sorting in descending order
				ClusterHeuristic.sortOnSdCountValue(refPairList);
				//remove randomness
				refPairList = ClusterHeuristic.removeRandomnessFromClusterList(refPairList);
				if(refPairList.get(0).sdCount == 0){
					//set boolean flag
					clearAllTrafficPairs = true;
					//add all the (s,d) pair clusters
					for(ClusterTrafficPairs ctpr : refPairList){
						//add to the sdPairGroup
						pairClusters.add(ctpr);
					}
				}else{
					//add to the sdPairGroup
					pairClusters.add(refPairList.get(0));
				}			
				if(!clearAllTrafficPairs){
					//remove the list of (s,d) pairs						
					for(NodePair np : refPairList.get(0).clusterPairs){
						Iterator<TrafficNodes> itr = allSdPairsRemaining.iterator();
						while(itr.hasNext()){
							TrafficNodes tn = itr.next();
							if(tn.equals(np)){
								//System.out.println("\t\tRemoving traffic node (" + tn.v1.get_id() + "," + tn.v2.get_id() + ")");
								itr.remove();
								break;
							}
						}
					}
				}else{
					//clear the list of (s,d) pairs
					allSdPairsRemaining.clear();
				}
			}
			//printClusters(pairClusters);
			
			//clusters formed
			//get the list of clusters
			/*System.out.println("\n\nPrinting out the clusters to be used!");
			int ClusterNumber = 0;
			for(ClusterTrafficPairs entry: pairClusters){
				System.out.println("Cluster Number: " + ClusterNumber);
				System.out.println("Foundation Pair: (" + entry.foundationPair.v1.get_id() + "," + entry.foundationPair.v2.get_id()+ ")");
				System.out.println("\t(s,d) pairs:" + entry.sdCount);
				for(NodePair pr : entry.clusterPairs){
					System.out.println("\t\t("+pr.v1.get_id()+","+pr.v2.get_id()+")");
				}
				//increment the cluster number
				ClusterNumber++;
			}*/
					
			//number of clusters to be used
			ArrayList<ClusterTrafficPairs> clusterGroups = new ArrayList<ClusterTrafficPairs>();
			//the set of all (s,d) pairs
			allSdPairsRemaining = new ArrayList<TrafficNodes>(allSdFlows);		
			//iterate through cluster no.'s
			for(int count=0; count<clusterCount; count++){
				//update the group num
				pairClusters.get(count).updateSdGroupNum(count);
				//add the cluster groups
				clusterGroups.add(pairClusters.get(count));
				//remove the list of (s,d) pairs				
				for(NodePair np : pairClusters.get(count).clusterPairs){
					Iterator<TrafficNodes> itr = allSdPairsRemaining.iterator();
					while(itr.hasNext()){
						TrafficNodes tn = itr.next();
						if(tn.equals(np)){
							//System.out.println("\t\tRemoving traffic node (" + tn.v1.get_id() + "," + tn.v2.get_id() + ")");
							itr.remove();
							break;
						}
					}
				}
			}
			
			//add the remaining (s,d) pairs to the clusters
			//based on the shortest path to any of the clusters
			for(TrafficNodes tn : allSdPairsRemaining){	
				NodePair pr = new NodePair(tn.v1,tn.v2);
				//it is the index of the cluster in the array list
				int chosenCluster = 0;
				ClusterTrafficPairs clusterPair = clusterGroups.get(chosenCluster);
				//total path length //path length to source foundation vertex //path length to destination foundation vertex
				int totalPathLength = clusterPair.shortestPathValue;
				if(pr.v1.get_id() != clusterPair.foundationPair.v1.get_id()){
					//find the path which does not include the destination
					NodePair np = new NodePair(pr.v1,clusterPair.foundationPair.v1);
					//iterate through the paths
					for(Path p : allSdPaths.get(np)){
						//path from source to source of node pair
						//path should not contain the destination
						if(!p.get_vertices().contains(clusterPair.foundationPair.v2)){
							totalPathLength += p.get_vertices().size()-1;
						}
					}					
				}
				if(pr.v2.get_id() != clusterPair.foundationPair.v2.get_id()){
					//find the path which does not include the destination
					NodePair np = new NodePair(clusterPair.foundationPair.v2,pr.v2);
					//iterate through the paths
					for(Path p : allSdPaths.get(np)){
						//path from target of node pair to destination
						//path should not contain the source
						if(!p.get_vertices().contains(clusterPair.foundationPair.v1)){
							totalPathLength += p.get_vertices().size()-1;
						}
					}					
				}
						 
				//iterate through the remaining clusters
				for(int count=1; count<clusterCount; count++){
					clusterPair = clusterGroups.get(count);
					int tempPathLength = clusterPair.shortestPathValue;
					if(pr.v1.get_id() != clusterPair.foundationPair.v1.get_id()){
						//find the path which does not include the destination
						NodePair np = new NodePair(pr.v1,clusterPair.foundationPair.v1);
						//iterate through the paths
						for(Path p : allSdPaths.get(np)){
							//path from source to source of node pair
							//path should not contain the destination
							if(!p.get_vertices().contains(clusterPair.foundationPair.v2)){
								tempPathLength += p.get_vertices().size()-1;
							}
						}					
					}
					if(pr.v2.get_id() != clusterPair.foundationPair.v2.get_id()){
						//find the path which does not include the destination
						NodePair np = new NodePair(clusterPair.foundationPair.v2,pr.v2);
						//iterate through the paths
						for(Path p : allSdPaths.get(np)){
							//path from target of node pair to destination
							//path should not contain the source
							if(!p.get_vertices().contains(clusterPair.foundationPair.v1)){
								tempPathLength += p.get_vertices().size()-1;
							}
						}					
					}
					//check if length of the path is smaller than previous one
					if(tempPathLength < totalPathLength){
						//assign new path length
						totalPathLength = tempPathLength;
						//assign the chosen cluster
						chosenCluster = count;
					}
				}
				
				//add the (s,d) pair to the chosen cluster
				//this cluster gives the shortest path
				clusterGroups.get(chosenCluster).addNodePair(pr);
				clusterGroups.get(chosenCluster).addExtraNodePair(pr);
				//update total (s,d) count
				clusterGroups.get(chosenCluster).sdCount = clusterGroups.get(chosenCluster).clusterPairs.size();
			}
			
			//return the cluster groups
			return clusterGroups;
				
		}
	
	//remove (s,d) pairs //add cluster
	//give as input clusters sorted on size
	public static void addClusterAndRemoveTrafficNodes(List<ClusterTrafficPairs> bcSdGroups, List<TrafficNodes> allSdPairsRemaining, List<ClusterTrafficPairs> pairClusters, NodePair largestFlowNodePair){
		//find largest sdPairGroup containing largestFlowNodePair
		for(ClusterTrafficPairs bcSdGroup : bcSdGroups){
			//check if sdPairGroup contains nodePair
			if(bcSdGroup.clusterPairs.contains(largestFlowNodePair)){
				//add to the sdPairGroup
				pairClusters.add(bcSdGroup);
				System.out.println("Adding cluster group!");
				//remove the list of (s,d) pairs
				for(NodePair np : bcSdGroup.clusterPairs){
					Iterator<TrafficNodes> itr = allSdPairsRemaining.iterator();
					while(itr.hasNext()){
						TrafficNodes tn = itr.next();
						if(tn.equals(np)){
							System.out.println("\t\tRemoving traffic node (" + tn.v1.get_id() + "," + tn.v2.get_id() + ")");
							itr.remove();
							break;
						}
					}
				}
				//total number of traffic nodes remaining
				System.out.println("\tNumber of traffic pairs remaining: " + allSdPairsRemaining.size());
				//break out of loop if cluster is found
				break;
			}
		}
	}
	
	//form the clusters given the number of clusters to be formed with traffic awareness
	public static ArrayList<ClusterTrafficPairs> formClustersV2withFlowVolumeAwareness(Graph g, int clusterCount, List<TrafficNodes> allSdFlows){		
			
			//store shortest paths for each (s,d) pair
			Map<NodePair, List<Path>> shortestSdPaths = PreProcVer1.allShortestPathsForAllPairs(g);
			//store all the k-shortest (s,d) paths
			HashMap<NodePair,List<Path>> allSdPaths = preProcFunctions.findRoutesForSDpairs(g);
			    
			//get the list of (s,d) pairs
			List<NodePair> pairList = new ArrayList<NodePair>();		
			for(TrafficNodes sdFlow : allSdFlows){
				pairList.add(new NodePair(sdFlow.v1, sdFlow.v2));
			}
				
			System.out.println("\n\n###### Traffic Flow Size: " + pairList.size() + " ######");
			
			//calculate betweenness centrality for ordered node pair
			Map<NodePair,ClusterTrafficPairs> bcOfNodePair = PreProcVer1.calculateBetweennessCentralityForOrderedNodePairs(g, shortestSdPaths, pairList);
			//print out the betweenness centrality for ordered node pair
			/*for(Map.Entry<NodePair, ClusterTrafficPairs> entry : bcOfNodePair.entrySet()){
				System.out.println("Node pair : (" + entry.getKey().v1.get_id() + "," + entry.getKey().v2.get_id() + ")");
				System.out.println("\t BC: " + entry.getValue().bcValue + " ; (s,d) pairs:" + entry.getValue().sdCount);
				System.out.println("\t (s,d) pairs:");
				for(NodePair np : entry.getValue().clusterPairs){
					System.out.println("\t\t(" + np.v1.get_id() + "," + np.v2.get_id() + ")");
				}
			}*/			
					
			//List the Node Pairs which have zero BC
			List<NodePair> zeroBcNodePairs = new ArrayList<NodePair>();
			//List the Node Pairs with non-zero BC
			List<NodePair> bcNodePairs = new ArrayList<NodePair>();
			//(s,d) groups with non-zero BC		
			List<ClusterTrafficPairs> bcSdGroups = ClusterHeuristic.getValidSdGroups(zeroBcNodePairs, bcNodePairs, bcOfNodePair);
			System.out.println("Printing (s,d) groups with non-zero BC");
			//printBcGroups(bcSdGroups);
			
			//sort the list of cluster traffic pairs based on betweenness-centrality values		
			//sorting in descending order	
			//ClusterHeuristic.sortOnBcValue(bcSdGroups);
			//sort the list of cluster traffic pairs based on number of (s,d) pairs	
			//sorting in descending order
			ClusterHeuristic.sortOnSdCountValue(bcSdGroups);		
			//remove randomness
			bcSdGroups = ClusterHeuristic.removeRandomnessFromClusterList(bcSdGroups);
			//print the bcGroups
			//ClusterHeuristic.printBcGroups(bcSdGroups);
			
			//create a linked HashMap
			//with the clusterPairs as values and the group no's as keys
			LinkedHashMap<Integer,ClusterTrafficPairs> sdGroups = new LinkedHashMap<Integer,ClusterTrafficPairs>();
			//linked hashmap
			//with cluster size as value and the group no's as keys
			LinkedHashMap<Integer,Integer> sdGroupSize = new LinkedHashMap<Integer,Integer>();
			int sdGroupNum = 0;
			for(ClusterTrafficPairs cp : bcSdGroups){
				cp.updateSdGroupNum(sdGroupNum);
				sdGroups.put(sdGroupNum, cp);
				sdGroupSize.put(sdGroupNum,cp.sdCount);
				sdGroupNum++;
			}
					
			//list group numbers to which (s,d) pairs belong
			//iterate through node pairs //find node pair belongs to which clusters
			Map<NodePair,ArrayList<Integer>> pairGroupNums = ClusterHeuristic.getGroupNumForPairs(shortestSdPaths, sdGroups);		
			
			//print out the relevant data structures
//			ClusterHeuristic.printSdGroups(sdGroups, zeroBcNodePairs, bcNodePairs, pairGroupNums);	
			
			
			//reference to new generated NodePairGroup
			Map<NodePair,ClusterTrafficPairs> refNodePairGroup;
			//get the list of cluster traffic pairs
			List<ClusterTrafficPairs> refPairList = new ArrayList<ClusterTrafficPairs>();
			//create new group numbers
			ArrayList<ClusterTrafficPairs> pairClusters = new ArrayList<ClusterTrafficPairs>();
			//the set of all traffic nodes
			ArrayList<TrafficNodes> allSdPairsRemaining = new ArrayList<TrafficNodes>(allSdFlows);
			//sort the traffic nodes based on the traffic flow value
			sortOnFlowValue(allSdPairsRemaining);
			//remove randomness
			allSdPairsRemaining = ClusterHeuristic.removeRandomnessFromTrafficList(allSdPairsRemaining);
			//print the traffic nodes
			
			System.out.println("### Begin calculating all clusters ###");
			int iteration = 1;
			//get the list of traffic flows with highest traffic
			List<TrafficNodes> largestTrafficFlows = new ArrayList<TrafficNodes>();
			double flowPrev = 0.0;
			//iterate through traffic nodes
			for(TrafficNodes tn : allSdPairsRemaining){
				if(flowPrev == 0.0){
					largestTrafficFlows.add(tn);
					flowPrev = tn.flow_traffic;
				}else if( flowPrev > tn.flow_traffic){
					break;
				}else{
					largestTrafficFlows.add(tn);
					flowPrev = tn.flow_traffic;
				}
			}	
			//size of the first largest traffic flows
			System.out.println("Size of largest flows: " + largestTrafficFlows.size());
			
			//find the flow with the largest cluster
			TrafficNodes largestFlow = allSdPairsRemaining.get(0);
			int clusterPairSize = 1;
			for(ClusterTrafficPairs bcSdGroup : bcSdGroups){
				for(TrafficNodes tn : largestTrafficFlows){
					NodePair np =  new NodePair(tn.v1,tn.v2);
					if(bcSdGroup.clusterPairs.contains(np) && (bcSdGroup.clusterPairs.size()>clusterPairSize)){
						largestFlow = tn;
						clusterPairSize = bcSdGroup.clusterPairs.size();						
					}
				}
			}			
			NodePair largestFlowNodePair = new NodePair(largestFlow.v1,largestFlow.v2);
			System.out.println("Largest Flow: (" +  largestFlow.v1.get_id() + "," + largestFlow.v2.get_id() + ") for iteration " + iteration + "; Bw=" + largestFlow.flow_traffic);
			//add the cluster and remove the traffic nodes
			addClusterAndRemoveTrafficNodes(bcSdGroups, allSdPairsRemaining, pairClusters, largestFlowNodePair);
			iteration++;
			
			//when zero traffic count is encountered	
			//boolean clearAllTrafficPairs = false;	
			//iterate through the sorted list of groups
			while(!allSdPairsRemaining.isEmpty()){	
				//sort the traffic flows based on flow value
				sortOnFlowValue(allSdPairsRemaining);
				//remove randomness
				allSdPairsRemaining = ClusterHeuristic.removeRandomnessFromTrafficList(allSdPairsRemaining);
				//the largest flow value
				largestFlow = allSdPairsRemaining.get(0);
				flowPrev = largestFlow.flow_traffic;
				//get the list of traffic flows with highest traffic
				largestTrafficFlows = new ArrayList<TrafficNodes>();				
				//iterate through traffic nodes to 
				//find the flows with traffic equal to largest flow value
				for(TrafficNodes tn : allSdPairsRemaining){
					if( flowPrev > tn.flow_traffic){
						break;
					}else{
						largestTrafficFlows.add(tn);
						flowPrev = tn.flow_traffic;
					}
				}
				//the new (s,d) clusters based on the remaining (s,d) pairs
				refNodePairGroup = PreProcVer1.calculateBetweennessCentralityForOrderedNodePairs(g, shortestSdPaths, allSdPairsRemaining);
				//get the groups of (s,d) pairs
				refPairList = new ArrayList<ClusterTrafficPairs>(refNodePairGroup.values());
				//printClusters(refPairList);
				//sort the list //sorting in descending order //based on number of 
				ClusterHeuristic.sortOnSdCountValue(refPairList);
				//remove randomness
				refPairList = ClusterHeuristic.removeRandomnessFromClusterList(refPairList);
				//printClusters(refPairList);
				//find the flow with the largest cluster
				clusterPairSize = 1;
				//find the largest cluster among the largest flows		
				for(ClusterTrafficPairs bcSdGroup : refPairList){					
					for(TrafficNodes tn : largestTrafficFlows){
						NodePair np = new NodePair(tn.v1,tn.v2);
						if( bcSdGroup.clusterPairs.contains(np) && (bcSdGroup.clusterPairs.size()>clusterPairSize)  ){							
							largestFlow = tn;
							clusterPairSize = bcSdGroup.clusterPairs.size();							
						}
					}
				}
				System.out.println("Largest Flow: (" +  largestFlow.v1.get_id() + "," + largestFlow.v2.get_id() + ") for iteration " + iteration);				
				largestFlowNodePair = new NodePair(largestFlow.v1,largestFlow.v2);				
				//add the cluster and remove the traffic nodes
				addClusterAndRemoveTrafficNodes(refPairList, allSdPairsRemaining, pairClusters, largestFlowNodePair);							
				//increment the iteration count
				iteration++;				
			}
			System.out.println("### Finished calculating all clusters ###");	
		    //printClusters(pairClusters);
			
					
			//number of clusters to be used
			ArrayList<ClusterTrafficPairs> clusterGroups = new ArrayList<ClusterTrafficPairs>();
			//the set of all (s,d) pairs
			allSdPairsRemaining = new ArrayList<TrafficNodes>(allSdFlows);		
			//iterate through cluster no.'s
			for(int count=0; count<clusterCount; count++){
				//update the group num
				pairClusters.get(count).updateSdGroupNum(count);
				//add the cluster groups
				clusterGroups.add(pairClusters.get(count));
				//remove the list of (s,d) pairs
				for(NodePair pr : pairClusters.get(count).clusterPairs){
					Iterator<TrafficNodes> itr = allSdPairsRemaining.iterator();
						while(itr.hasNext()){
							TrafficNodes tn = itr.next();
							if(tn.equals(pr)){
								allSdPairsRemaining.remove(tn);
								break;
							}
						}
					
				}
			}
			
			
			
			
			//add the remaining (s,d) pairs to the clusters
			//based on the shortest path to any of the clusters		
			for(TrafficNodes tn : allSdPairsRemaining){	
				//it is the index of the cluster in the array list
				int chosenCluster = 0;
				ClusterTrafficPairs clusterPair = clusterGroups.get(chosenCluster);
				//total path length //path length to source foundation vertex //path length to destination foundation vertex
				int totalPathLength = clusterPair.shortestPathValue;
				if(tn.v1.get_id() != clusterPair.foundationPair.v1.get_id()){
					//find the path which does not include the destination
					NodePair np = new NodePair(tn.v1,clusterPair.foundationPair.v1);
					//iterate through the paths
					for(Path p : allSdPaths.get(np)){
						//path from source to source of node pair
						//path should not contain the destination
						if(!p.get_vertices().contains(clusterPair.foundationPair.v2)){
							totalPathLength += p.get_vertices().size()-1;
						}
					}					
				}
				if(tn.v2.get_id() != clusterPair.foundationPair.v2.get_id()){
					//find the path which does not include the destination
					NodePair np = new NodePair(clusterPair.foundationPair.v2,tn.v2);
					//iterate through the paths
					for(Path p : allSdPaths.get(np)){
						//path from target of node pair to destination
						//path should not contain the source
						if(!p.get_vertices().contains(clusterPair.foundationPair.v1)){
							totalPathLength += p.get_vertices().size()-1;
						}
					}					
				}
						 
				//iterate through the remaining clusters
				for(int count=1; count<clusterCount; count++){
					clusterPair = clusterGroups.get(count);
					int tempPathLength = clusterPair.shortestPathValue;
					if(tn.v1.get_id() != clusterPair.foundationPair.v1.get_id()){
						//find the path which does not include the destination
						NodePair np = new NodePair(tn.v1,clusterPair.foundationPair.v1);
						//iterate through the paths
						for(Path p : allSdPaths.get(np)){
							//path from source to source of node pair
							//path should not contain the destination
							if(!p.get_vertices().contains(clusterPair.foundationPair.v2)){
								tempPathLength += p.get_vertices().size()-1;
							}
						}					
					}
					if(tn.v2.get_id() != clusterPair.foundationPair.v2.get_id()){
						//find the path which does not include the destination
						NodePair np = new NodePair(clusterPair.foundationPair.v2,tn.v2);
						//iterate through the paths
						for(Path p : allSdPaths.get(np)){
							//path from target of node pair to destination
							//path should not contain the source
							if(!p.get_vertices().contains(clusterPair.foundationPair.v1)){
								tempPathLength += p.get_vertices().size()-1;
							}
						}					
					}
					//check if length of the path is smaller than previous one
					if(tempPathLength < totalPathLength){
						//assign new path length
						totalPathLength = tempPathLength;
						//assign the chosen cluster
						chosenCluster = count;
					}
				}
				
				//add the (s,d) pair to the chosen cluster
				//this cluster gives the shortest path
				clusterGroups.get(chosenCluster).addNodePair(new NodePair(tn.v1,tn.v2));
				clusterGroups.get(chosenCluster).addExtraNodePair(new NodePair(tn.v1,tn.v2));
				//update total (s,d) count
				clusterGroups.get(chosenCluster).sdCount = clusterGroups.get(chosenCluster).clusterPairs.size();
			}
			
			
			//return the cluster groups
			return clusterGroups;
			
	}
	
	//print the clusters
	/*public static void printClusters(List<ClusterTrafficPairs> clusterGroups){
		int ClusterNumber = 0;		
		for(ClusterTrafficPairs entry: clusterGroups){
			System.out.println("Cluster Number: " + ClusterNumber);
			System.out.println("Foundation Pair: (" + entry.foundationPair.v1.get_id() + "," + entry.foundationPair.v2.get_id()+ ")");
			System.out.println("\t(s,d) pairs:" + entry.sdCount);
			for(NodePair pr : entry.clusterPairs){
				System.out.println("\t\t("+pr.v1.get_id()+","+pr.v2.get_id()+")");
			}
			//increment the cluster number
			ClusterNumber++;
		}
	}*/
	

	
	
	/*public static void main(String args[]) throws Exception{
		//print the console output to the "consoleOutput.txt" file		
	    PrintStream out = new PrintStream(new FileOutputStream("consoleOutput.txt"));
		System.setOut(out);
				
		//generate the graph object
	    Graph g = PreProcVer1.makeGraphObject();
	    //set all nodes to type switches
	    preProcFunctions.makeAllVrtSwitches(g);
	    //populate ChainSet details
	    InputConstants.populateServices();
	    //print the graph object
	    preProcFunctions.printGraph(g);
	    
	    // SD pairs between which we desire traffic to be
		// Store each s-d pair
		List<TrafficNodes> pair_list = new ArrayList<TrafficNodes>();
		pair_list.add(new TrafficNodes(g.get_vertex(1), g.get_vertex(2), 0, 1000));
		pair_list.add(new TrafficNodes(g.get_vertex(1), g.get_vertex(3), 0, 1000));
		pair_list.add(new TrafficNodes(g.get_vertex(1), g.get_vertex(4), 0, 1000));
//		pair_list.add(new TrafficNodes(g.get_vertex(1), g.get_vertex(5), 0, 2000));
		pair_list.add(new TrafficNodes(g.get_vertex(2), g.get_vertex(1), 0, 1000));
		pair_list.add(new TrafficNodes(g.get_vertex(2), g.get_vertex(3), 0, 1000));
		pair_list.add(new TrafficNodes(g.get_vertex(2), g.get_vertex(4), 0, 1000));
		pair_list.add(new TrafficNodes(g.get_vertex(2), g.get_vertex(5), 0, 1000));
		pair_list.add(new TrafficNodes(g.get_vertex(3), g.get_vertex(1), 0, 1000));
		pair_list.add(new TrafficNodes(g.get_vertex(3), g.get_vertex(2), 0, 1000));
		pair_list.add(new TrafficNodes(g.get_vertex(3), g.get_vertex(4), 0, 1000));
		pair_list.add(new TrafficNodes(g.get_vertex(3), g.get_vertex(5), 0, 1000));
		pair_list.add(new TrafficNodes(g.get_vertex(4), g.get_vertex(1), 0, 1000));
		pair_list.add(new TrafficNodes(g.get_vertex(4), g.get_vertex(2), 0, 1000));
		pair_list.add(new TrafficNodes(g.get_vertex(4), g.get_vertex(3), 0, 1000));
		pair_list.add(new TrafficNodes(g.get_vertex(4), g.get_vertex(5), 0, 1000));
		pair_list.add(new TrafficNodes(g.get_vertex(5), g.get_vertex(1), 0, 1000));
		pair_list.add(new TrafficNodes(g.get_vertex(5), g.get_vertex(2), 0, 1000));
		pair_list.add(new TrafficNodes(g.get_vertex(5), g.get_vertex(3), 0, 1000));
//		pair_list.add(new TrafficNodes(g.get_vertex(5), g.get_vertex(4), 0, 4000));
	    
	    //form clusters when all (s,d) pairs
		//List<ClusterTrafficPairs> fullTrafficClustersWithoutTA =  formClustersWhenAllSdPairs(g);
		//printClusters(fullTrafficClustersWithoutTA);
	    
	    //40 when NSFNET
	    //87 when US24
	    //176 when Germany
	    //form clusters
//	    List<ClusterTrafficPairs> clusterGroups = formClustersV2withFlowVolumeAwareness(g, 2, pair_list);
		System.out.println("##### Total number of clusters = " + formClustersForGivenSdPairs(g, pair_list).size() + " #####");
		List<ClusterTrafficPairs> clusterGroups = formClustersV2(g, 2, pair_list);
	    //print clusters
	    printClusters(clusterGroups);*/
	    
	    
	   
	    
	    //calculate betweenness centrality
	    //Map<BaseVertex,Double> bcOfVertex = preProcFunctions.calculateBetweenessCentrality(g);
	    
	    //calculate ranking of vertices based on 
	    //product of betweeness-centrality and degree centrality
//	    Map<BaseVertex,Double> vertexRank = preProcFunctions.calProductOfBCandDeg(g,bcOfVertex);
//	    //list of vertex ranks
//	    List<VertexRank> rankList = new ArrayList<VertexRank>();
//	    //make list
//	    for(Map.Entry<BaseVertex, Double> entry : vertexRank.entrySet()){
//	    	VertexRank obj = new VertexRank(entry.getKey(),entry.getValue());
//	    	rankList.add(obj);
//	    }	  
//	    //sort list in descending order
//	    Collections.sort(rankList);
//	    //print out the vertex Ranking
//	    preProcFunctions.printVertexRanking(rankList);
//	       
//	    
//	    //generate the routes for the traffic pairs
//	    HashMap<NodePair,List<Path>> sdpaths = preProcFunctions.findRoutesForSDpairs(g);
//	    
//	    //get the set of service chains
//	  	Map<Integer,ServiceChain> ChainSet = PreProcVer1.populateChainSetBasedOnScenario1();
//		// print out the Set of Service Chains
//		preProcFunctions.printServiceChains(ChainSet);
//		
//		//total list of VNF available
//		List<FuncPt> vnf_list = PreProcVer1.totalListOfVNFs();
//		
//		//NFV nodes 
//		//All nodes are NFV capable
//		int numOfNfvNodes = 24;
//				
//		// SD pairs between which we desire traffic to be
//		// Store each s-d pair
//		List<TrafficNodes> pair_list = new ArrayList<TrafficNodes>();
//		//generate all (s,d) pairs
//		for(BaseVertex srcVrt: g.get_vertex_list()){
//			for(BaseVertex destVrt: g.get_vertex_list()){
//				if(srcVrt.get_id() != destVrt.get_id()){					
//					pair_list.add(new TrafficNodes(srcVrt,destVrt,0,1000));					
//				}
//			}			
//		}	
//		
//		//List of the service chains to be deployed
//		List<Integer> scUsed = preProcFunctions.serviceChainsUsed(pair_list);		
//		//print out the pair lists
//		preProcFunctions.printSDpairs(pair_list, scUsed);	    
//		
//		//VNFs used across the service chains deployed
//		List<FuncPt> func_list = preProcFunctions.listOfVNFsUsed(vnf_list, ChainSet, scUsed);		
//		//print out the function list
//		preProcFunctions.printListOfVNFsUsed(func_list);
//		
//		//traffic pairs for each service chain deployed	
//		Map<Integer,ArrayList<TrafficNodes>> serviceChainTN = preProcFunctions.sdPairsforServiceChain(scUsed, pair_list);
//	    //print out the traffic nodes available for each service chain
//	    preProcFunctions.printTrafficNodesForServiceChains(scUsed, serviceChainTN);	
//	    
//	    //split a single service chain into multiple service chains
//	  	Map<Integer,ArrayList<Integer>> scCopies = new HashMap<Integer,ArrayList<Integer>>();		
//	  	//new service to old service
//	  	Map<Integer,Integer> scCopyToSC = new HashMap<Integer,Integer>();
//	  	//create list of service chains
//	  	ArrayList<Integer> scCopyUsed = new ArrayList<Integer>();
//	  	
//	    //max number of VNFs
//	    Map<Integer, ArrayList<Integer>> funcInSC = preProcFunctions.vnfInSCs(scUsed, func_list, ChainSet);
//	    Map<Integer,Integer> CountMaxVNF = preProcFunctions.countMaxVnfBasedOnSdPairs(ChainSet, funcInSC, serviceChainTN);
//	    
//	    //replica constraint per VNF
//	    Map<Integer,Integer> replicaPerVNF = new HashMap<Integer,Integer>(CountMaxVNF);
//	  	
//	  	 //DC node placement		  
//  		ArrayList<Integer> dcNodes = new ArrayList<Integer>();  
//  		  
//  		//place the DC nodes
//  		placeNFVI.placeDC(g, dcNodes);
//  		//place the NFV nodes
//  		placeNFVI.placeNFVPoP(g, rankList, numOfNfvNodes);
//  		//create the list of NFV-capable nodes
//  		ArrayList<BaseVertex> nfv_nodes = new ArrayList<BaseVertex>();
//  		placeNFVI.makeNFVList(g, nfv_nodes);
//  		
//  		//create the list of NFVI nodes
//  		//add the set of DC nodes to the set of nfv nodes
//  		ArrayList<BaseVertex> nodesNFVI = new ArrayList<BaseVertex>();
//  		//add the set of NFV nodes
//  		nodesNFVI.addAll(nfv_nodes);
//  		//print the nodes with NFV capability
//  		placeNFVI.printNFVINodes(nodesNFVI);
//  		
//  		//list of vertices without the NFV nodes
//  		ArrayList<BaseVertex> vertex_list_without_nfvi_nodes = new ArrayList<BaseVertex>(g._vertex_list);
//  		//assuming that the NFV and DC node sets are exclusive				 
//  		vertex_list_without_nfvi_nodes.removeAll(nodesNFVI);		    
//  		
//  	  	  
//		//valid configurations for each service chain //each (s,d) selects a valid configuration
//		Map<Integer,ArrayList<HuerVarZ>> configsPerSC = new HashMap<Integer,ArrayList<HuerVarZ>>();
//		Map<TrafficNodes,SdDetails> configPerSD = new HashMap<TrafficNodes,SdDetails>();
//		
//	
//		//cluster traffic pairs according to service chains
//		serviceChainTN = preProcFunctions.sdPairsforServiceChain(scCopyUsed, pair_list);
//		 //print out the traffic nodes available for each service chain
//	    preProcFunctions.printTrafficNodesForServiceChains(scCopyUsed, serviceChainTN);
//		//get configuration per SC
//		configsPerSC = BaseHeuristic2.singleConfigBasedOnAdj(scUsed, ChainSet, nodesNFVI, scCopyUsed, scCopyToSC, sdpaths, serviceChainTN, scCopies, configPerSD);
//		
//		//print the configurations for each SC
//	  	preProcFunctions.printConfigsPerSCforBH2(scUsed, configsPerSC);					  
//	  	//print the configuration for each (s,d)
//	  	preProcFunctions.printConfigForSD(configPerSD);
//	  	
//	    //calculate the core and link constraints
//  		boolean coreCstr = false;
//  		boolean capCstr = false;
//  		Map<BaseVertex,Double> cpuCoreCount = new HashMap<BaseVertex,Double>();
//  		Map<NodePair,Double> linkCapacity = new HashMap<NodePair,Double>();
//  		CG.runCG(coreCstr,capCstr,cpuCoreCount,linkCapacity,g, ChainSet, pair_list, scUsed, vnf_list, func_list, serviceChainTN, nfv_nodes, 
//  				  nodesNFVI, vertex_list_without_nfvi_nodes, scCopies, scCopyToSC, configsPerSC, configPerSD, CountMaxVNF, replicaPerVNF);
//	}
	
}
