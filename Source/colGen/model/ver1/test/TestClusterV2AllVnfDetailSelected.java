package colGen.model.ver1.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.heuristic.BaseHeuristic2;
import colGen.model.heuristic.ClusterHeuristic;
import colGen.model.heuristic.HuerVarZ;
import colGen.model.heuristic.BaseHeuristic2.SdDetails;
import colGen.model.heuristic.ClusterHeuristic.ClusterTrafficPairs;
import colGen.model.preprocess.PreProcVer1;
import colGen.model.preprocess.placeNFVI;
import colGen.model.preprocess.preProcFunctions;
import colGen.model.ver1.CG;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class TestClusterV2AllVnfDetailSelected {

	public static void runTest() throws Exception{
		//generate the graph object
	    Graph g = PreProcVer1.makeGraphObject();
	    //set all nodes to type switches
	    preProcFunctions.makeAllVrtSwitches(g);
	    //populate ChainSet details
	    InputConstants.populateServices();
	    //generate the routes for the traffic pairs
	    HashMap<NodePair,List<Path>> sdpaths = preProcFunctions.findRoutesForSDpairs(g);
	    //NFV nodes 
	  	//All nodes are NFV capable
	  	int numOfNfvNodes = g._vertex_list.size();
	  	//all cluster counts
	  	int maxClusterCount = ClusterHeuristic.formClustersWhenAllSdPairs(g).size();  
	  	//total traffic load
	  	double trafficLoad = 1000*sdpaths.keySet().size();
	    
	    //get the set of service chains
 		Map<Integer, ServiceChain> ChainSet = preProcFunctions.setOfServiceChains();
 		// print out the Set of Service Chains
 		preProcFunctions.printServiceChains(ChainSet);
		
		//total list of VNF available
		List<FuncPt> vnf_list = PreProcVer1.totalListOfVNFs();		
				
		// SD pairs between which we desire traffic to be
		// Store each s-d pair
		List<TrafficNodes> pair_list = new ArrayList<TrafficNodes>();
		//generate all (s,d) pairs
		for(BaseVertex srcVrt: g.get_vertex_list()){
			for(BaseVertex destVrt: g.get_vertex_list()){
				if(srcVrt.get_id() != destVrt.get_id()){					
					pair_list.add(new TrafficNodes(srcVrt,destVrt,2,1000));					
				}
			}			
		}
		
		//List of the service chains to be deployed
		List<Integer> scUsed = preProcFunctions.serviceChainsUsed(pair_list);		
		//print out the pair lists
		preProcFunctions.printSDpairs(pair_list, scUsed);	    
		
		//VNFs used across the service chains deployed
		List<FuncPt> func_list = preProcFunctions.listOfVNFsUsed(vnf_list, ChainSet, scUsed);		
		//print out the function list
		preProcFunctions.printListOfVNFsUsed(func_list);
		
		//DC node placement		  
		ArrayList<Integer> dcNodes = new ArrayList<Integer>();		  
		//place the DC nodes
		placeNFVI.placeDC(g, dcNodes);
		//place the NFV nodes
		placeNFVI.allNFV(g);
		//create the list of NFV-capable nodes
		ArrayList<BaseVertex> nfv_nodes = new ArrayList<BaseVertex>();
		placeNFVI.makeNFVList(g, nfv_nodes);			
		//create the list of NFVI nodes
		//add the set of DC nodes to the set of nfv nodes
		ArrayList<BaseVertex> nodesNFVI = new ArrayList<BaseVertex>();
		//add the set of NFV nodes
		nodesNFVI.addAll(nfv_nodes);
		//print the nodes with NFV capability
		placeNFVI.printNFVINodes(nodesNFVI);			
		//list of vertices without the NFV nodes
		ArrayList<BaseVertex> vertex_list_without_nfvi_nodes = new ArrayList<BaseVertex>(g._vertex_list);
		//assuming that the NFV and DC node sets are exclusive				 
		vertex_list_without_nfvi_nodes.removeAll(nodesNFVI);
		
		ArrayList<Integer> selectedKnodeCount = new ArrayList<Integer>();
		selectedKnodeCount.add(1);selectedKnodeCount.add(2);selectedKnodeCount.add(3);selectedKnodeCount.add(4);selectedKnodeCount.add(5);
		selectedKnodeCount.add(14);
		
		//iterate through K values
		for(int kNodeCount : selectedKnodeCount){
			//int kNodeCount=numOfNfvNodes;
	    	String filename = "PlotData_Traf_100%_Uniform_K_" + kNodeCount + ".txt";
    	    BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
    	    //iterate through cluster counts
//    	    for(int numOfClusters=1;numOfClusters<=maxClusterCount;numOfClusters++){
    	    	int numOfClusters=maxClusterCount;
    	    	//form clusters
			    List<ClusterTrafficPairs> clusterGroups = ClusterHeuristic.formClustersV2(g, numOfClusters);
			    System.out.println("Clusters Generated!");
			    //print clusters
			    ClusterHeuristic.printClusters(clusterGroups);
    	    	
    	    	
    	    	//create a copy of pairList //deep copy
    	    	ArrayList<TrafficNodes> copyOfPairList = new ArrayList<TrafficNodes>();	    	    	
    	    	for(TrafficNodes tn : pair_list){
    	    		copyOfPairList.add(new TrafficNodes(tn.v1,tn.v2,tn.chain_index,tn.flow_traffic));
    	    	}    	    	
    	    	
    	    	//traffic pairs for each service chain deployed	
    			Map<Integer,ArrayList<TrafficNodes>> serviceChainTN = preProcFunctions.sdPairsforServiceChain(scUsed, copyOfPairList);
    			
    			//print out the traffic nodes available for each service chain
    		    preProcFunctions.printTrafficNodesForServiceChains(scUsed, serviceChainTN);
    	 		
    		    //max number of VNFs
    		    Map<Integer, ArrayList<Integer>> funcInSC = preProcFunctions.vnfInSCs(scUsed, func_list, ChainSet);
    		    Map<Integer,Integer> CountMaxVNF = preProcFunctions.countMaxVnfBasedOnSdPairs(ChainSet, funcInSC, serviceChainTN);
    		    
			    //split a single service chain into multiple service chains
			  	Map<Integer,ArrayList<Integer>> scCopies = new HashMap<Integer,ArrayList<Integer>>();		
			  	//new service to old service
			  	Map<Integer,Integer> scCopyToSC = new HashMap<Integer,Integer>();
			  	//create list of service chains
			  	ArrayList<Integer> scCopyUsed = new ArrayList<Integer>();
			  	//iterate through the service chains
			  	//fill the various IDs
			  	for(int scID: scUsed){
			  		ArrayList<Integer> scCopyList = new ArrayList<Integer>();
				  	//add to map and list's
				  	for(ClusterTrafficPairs ctpr : clusterGroups){
				  		//get a sc copy Id
				    	int scCopyId = 1000*scID + ctpr.sdGroupNum;
				    	scCopyToSC.put(scCopyId,scID);
				    	scCopyUsed.add(scCopyId);
				    	scCopyList.add(scCopyId);
				  	}
				  	 //add all scCopyId's in map
				    scCopies.put(scID, scCopyList);
			  	}
			  	
			  	//iterate through scIDs
			  	for(int scID : scUsed){
			  		//iterate through (s,d) pairs
				    for(TrafficNodes tn : serviceChainTN.get(scID)){
				    	NodePair tnPr = new NodePair(tn.v1,tn.v2);
				    	//iterate through the traffic groups
				    	for(ClusterTrafficPairs ctpr : clusterGroups){
				    		//chech if the (s,d) cluster contains the node pair
				    		if(ctpr.clusterPairs.contains(tnPr)){
					    		//get a sc copy Id
						    	int scCopyId = 1000*scID + ctpr.sdGroupNum;
						    	//update chain index for (s,d)
						    	tn.updateChainIndex(scCopyId); 
				    		}
				    	}
				    }		   
			    }
			  	
				//valid configurations for each service chain //each (s,d) selects a valid configuration
				Map<Integer,ArrayList<HuerVarZ>> configsPerSC = new HashMap<Integer,ArrayList<HuerVarZ>>();
				Map<TrafficNodes,SdDetails> configPerSD = new HashMap<TrafficNodes,SdDetails>();
				
				//cluster traffic pairs according to service chains
				serviceChainTN = preProcFunctions.sdPairsforServiceChain(scCopyUsed, copyOfPairList);
				 //print out the traffic nodes available for each service chain
			    preProcFunctions.printTrafficNodesForServiceChains(scCopyUsed, serviceChainTN);
				//get configuration per SC
				configsPerSC = BaseHeuristic2.singleConfigBasedOnAdj(scUsed, ChainSet, nodesNFVI, scCopyUsed, scCopyToSC, sdpaths, serviceChainTN, scCopies, configPerSD);
				
				//print the configurations for each SC
			  	preProcFunctions.printConfigsPerSCforBH2(scUsed, configsPerSC);					  
			  	//print the configuration for each (s,d)
			  	preProcFunctions.printConfigForSD(configPerSD);
			  	
			  	//initialize to number of nodes in graph //only one instance of VNF per node 
			  	for(int replicaCount=1; replicaCount<=kNodeCount; replicaCount++){	
			  		//int replicaCount=kNodeCount;
			  		Map<Integer, Map<BaseVertex,Double>> cpuCoresAcrossVnfReplicas = new TreeMap<Integer, Map<BaseVertex,Double>>();
			  		//Iterate through VNF Ids
			  		for(FuncPt fpt : func_list){
			  			int vnfID = fpt.getid();
		    		    //replica constraint per VNF
		    		    Map<Integer,Integer> replicaPerVNF = new HashMap<Integer,Integer>();	    		   
		    		    
		    		    replicaPerVNF.put(vnfID,replicaCount);
		    		    //populate the others with maximum number of VNF replica counts
		    		    for(int vnfsWithNoReplicaConstraint : CountMaxVNF.keySet()){
		    		    	if(vnfsWithNoReplicaConstraint != vnfID){
		    		    		replicaPerVNF.put(vnfsWithNoReplicaConstraint, kNodeCount);
		    		    	}
		    		    }
					  	
						//calculate the core and link constraints
				  	  	boolean coreCstr = false;
				  	  	boolean capCstr = false;
				  	  	Map<BaseVertex,Double> cpuCoreCount = new HashMap<BaseVertex,Double>();
				  	  	Map<NodePair,Double> linkCapacity = new HashMap<NodePair,Double>();
				  	  	CG.runCG(coreCstr,capCstr,cpuCoreCount,linkCapacity,g, ChainSet, copyOfPairList, scUsed, vnf_list, func_list, serviceChainTN, nfv_nodes, 
							  nodesNFVI, vertex_list_without_nfvi_nodes, scCopies, scCopyToSC, configsPerSC, configPerSD, CountMaxVNF, replicaPerVNF,kNodeCount);
				  	  	
				  	  	//add CPU cores for each VNF replica
				  	  	cpuCoresAcrossVnfReplicas.put(vnfID, cpuCoreCount);
				  	  	
				  	  	//VNF placement nodes
				  	  	ArrayList<Integer> placementNodes = new ArrayList<Integer>();				  	
				  	  	for(int vrtID : CG.vnfPlacementNodes){				  	  		
				  	  		placementNodes.add(vrtID);
				  	  	}
					  	
				  	  	//core values corresponding to nodes used
				  	  	//convert and round the core values
				  	  	ArrayList<Double> coreValues = new ArrayList<Double>();
				  	  	for(int vrtID : placementNodes){
				  	  		for(Map.Entry<BaseVertex,Double> entry : cpuCoreCount.entrySet()){
				  	  			if(entry.getKey().get_id() == vrtID){
				  	  				System.out.println("Vertex = " + vrtID + " ; CPU cores = " + entry.getValue());
				  	  				Double cpuCores = Math.ceil(entry.getValue()/1000);
				  	  				coreValues.add(cpuCores);
				  	  			}
				  	  		}
				  	  	}				  	  	
				  	  	
				  	  	//traffic percentages per Node
				  	  	ArrayList<Double> trafLoadPerNode = new ArrayList<Double>();
				  	  	for(int vrtID : placementNodes){
				  	  		System.out.println("Total Traffic Load = " + trafficLoad );
				  	  		System.out.println("Vertex = " + vrtID + " ; Traffic Load = " + CG.trafficLoadPerNode.get(vrtID));
				  	  		Double tl = CG.trafficLoadPerNode.get(vrtID)/trafficLoad;				  	  		
				  	  		trafLoadPerNode.add(tl);				  	  		
				  	  	}
				  	  	
				  	  	//traffic percentages per Node per VNF
				  	  	String trafPerNodePerVNF = "[";
				  	  	for(int vrtID : placementNodes){
				  	  		String trafPerVNF = " [ ";
				  	  		for(Map.Entry<Integer,Double> entry : CG.trafficLoadPerNodePerVNF.get(vrtID).entrySet()){
				  	  			System.out.println("Vertex = " + vrtID + " ; VNF = " + entry.getKey() + " ; Traffic Load = " + entry.getValue());
				  	  			Double tl = entry.getValue()/trafficLoad;
				  	  			trafPerVNF += entry.getKey() + ":" + tl + "; ";
				  	  		}
				  	  		trafPerVNF += "] ;";
				  	  		trafPerNodePerVNF += trafPerVNF;
				  	  	}
				  	  	trafPerNodePerVNF += "]";
					
				  	  	//write to a file
				  	  	bw.write(numOfClusters + "\t" + CG.rmpIlpValue + "\t" + preProcFunctions.totalBwUsedOnShortestPaths(sdpaths, copyOfPairList) + "\t" + 
				  	  			"[ VNF=" + vnfID + ", Replicas=" + replicaCount + " ]" + "\t" +CG.vnfPlacementNodes.size() + "\t" + placementNodes.toString() + "\t" + coreValues.toString() + "\t" +	
				  	  			trafLoadPerNode.toString() + "\t" + trafPerNodePerVNF + "\t" +				  	  					  	  			
				  	  			CG.eoptimality + "\t" + CG.cgRunTime + "\t" + CG.ilpRunTime + "\t" + CG.totalTime + "\n");
				  	  	
				  	  	//clear the map
				  	  	replicaPerVNF.clear();
			  		}
			  		System.out.println();
			  		//print out CPU cores for each VNF license
			  		for(Map.Entry<Integer, Map<BaseVertex, Double>> entry : cpuCoresAcrossVnfReplicas.entrySet()){
			  			System.out.println("VNF = " + entry.getKey());
			  			for(Map.Entry<BaseVertex, Double> entryInner : entry.getValue().entrySet()){
			  				System.out.println("\tVertex = " + entryInner.getKey().get_id() + " ; Cores = " + entryInner.getValue());
			  			}
			  		}
			  	}
//    	    }
    	    //close the file writer
		    bw.close();
		}
	}
}
