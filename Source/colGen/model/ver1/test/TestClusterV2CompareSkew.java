package colGen.model.ver1.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.heuristic.BaseHeuristic2;
import colGen.model.heuristic.BaseHeuristic2.SdDetails;
import colGen.model.heuristic.ClusterHeuristic;
import colGen.model.heuristic.ClusterHeuristic.ClusterTrafficPairs;
import colGen.model.heuristic.HuerVarZ;
import colGen.model.preprocess.PreProcVer1;
import colGen.model.preprocess.placeNFVI;
import colGen.model.preprocess.preProcFunctions;
import colGen.model.trafficGen.TrafficGenerator;
import colGen.model.ver1.CG;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class TestClusterV2CompareSkew {
	public static void runTest() throws Exception{
		//maximum iteration count
 		int maxIterationCount = 100;		
		
		//traffic matrix percentages 		
 		ArrayList<Double> trafMatPrcnt = new ArrayList<Double>();
 		trafMatPrcnt.add(0.05); trafMatPrcnt.add(0.1); trafMatPrcnt.add(0.15);trafMatPrcnt.add(0.2);	
 		trafMatPrcnt.add(0.25); trafMatPrcnt.add(0.50); trafMatPrcnt.add(0.75); 		
 				 		
 		
 		//best bandwidth associated with each traffic percentage
 		Map<Double, ArrayList<Double>> aspValuesForTrafPrcnt = new TreeMap<Double, ArrayList<Double>>();
 		//cluster counts associated with each traffic percentage
 		Map<Double,ArrayList<Integer>> clusterCountValuesForTrafPrcnt= new TreeMap<Double,ArrayList<Integer>>(); 		
 		//cluster counts associated with each traffic percentage
 		Map<Double,ArrayList<Integer>> clusterCountValuesForTrafPrcntTA = new TreeMap<Double,ArrayList<Integer>>();
		
 		//bandwidth associated with each cluster count for each traffic percentage
 		Map<Double, ArrayList<ArrayList<Double>>> bwValuesForClustersAndTrafPrcnt = new TreeMap<Double, ArrayList<ArrayList<Double>>>(); 
 		//bandwidth associated with each cluster count for each traffic percentage
 		Map<Double, ArrayList<ArrayList<Double>>> bwValuesForClustersAndTrafPrcntTA = new TreeMap<Double, ArrayList<ArrayList<Double>>>();
 		
		//generate the graph object
	    Graph g = PreProcVer1.makeGraphObject();
	    //population of US cities
	    Map<BaseVertex, Double> popUsCities = new HashMap<BaseVertex, Double>();
 		popUsCities.put(g.get_vertex(1),660000.0);
 		popUsCities.put(g.get_vertex(2),850000.0);
 		popUsCities.put(g.get_vertex(3),1400000.0);
 		popUsCities.put(g.get_vertex(4),191000.0);
 		popUsCities.put(g.get_vertex(5),660000.0);
 		popUsCities.put(g.get_vertex(6),2200000.0);
 		popUsCities.put(g.get_vertex(7),450000.0); 	
 		popUsCities.put(g.get_vertex(8),2700000.0);//chicago
 		popUsCities.put(g.get_vertex(9),300000.0);
 		popUsCities.put(g.get_vertex(10),460000.0);
 		popUsCities.put(g.get_vertex(11),680000.0); 		
 		popUsCities.put(g.get_vertex(12),8500000.0);//new york
 		popUsCities.put(g.get_vertex(13),1600000.0);
 		popUsCities.put(g.get_vertex(14),670000.0);
	    //set all nodes to type switches
	    preProcFunctions.makeAllVrtSwitches(g);
	    //populate ChainSet details
	    InputConstants.populateServices();
	    //generate the routes for the traffic pairs
	    HashMap<NodePair,List<Path>> sdpaths = preProcFunctions.findRoutesForSDpairs(g);
	    //total number of (s,d) pairs
 		int totalNumberOfSdPairs = sdpaths.keySet().size(); 	
 		//NFV nodes 
		//All nodes are NFV capable
		int numOfNfvNodes = g._vertex_list.size();
		//the number of nodes allowed to host VNFs
		int kNodeCount = numOfNfvNodes;
		//Traffic Load //Scenario 1 Load where there is a full-mesh traffic
		int trafficLoad = 1000*totalNumberOfSdPairs;
		System.out.println("Total traffic Load = " + trafficLoad);
		
		 //get the set of service chains
 		Map<Integer, ServiceChain> ChainSet = preProcFunctions.setOfServiceChains();
 		// print out the Set of Service Chains
 		preProcFunctions.printServiceChains(ChainSet);
 		
 		//total list of VNF available
		List<FuncPt> vnf_list = PreProcVer1.totalListOfVNFs();
		
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
		
		//max cluster count for each traffic value
		Map<Double, Integer> maxClusterCountForTraffic = new TreeMap<Double, Integer>();
		for(Double trafPrcnt : trafMatPrcnt){
			maxClusterCountForTraffic.put(trafPrcnt, 0);
		}
		
		//iterate for TA oblivious
		//iterate that many number of times
		for(int reRunNo=0; reRunNo < maxIterationCount; reRunNo++){
			Map<Double,ArrayList<NodePair>> trafPairs = new TreeMap<Double,ArrayList<NodePair>>();
			//existing node pairs
			ArrayList<NodePair> pairListTotal = new ArrayList<NodePair>();		
			//iterate through traffic percentages
			for(Double trafPrcnt : trafMatPrcnt){
				//the number of node pairs to be added
				int reqNumOfNodePairs = (int) Math.ceil(trafPrcnt*totalNumberOfSdPairs) - pairListTotal.size();		
				//selected node pairs
				ArrayList<NodePair> selPairs = TrafficGenerator.generateNodePairs(pairListTotal, reqNumOfNodePairs, g._vertex_list);	
				//add the node pairs to existing node pairs
				pairListTotal.addAll(selPairs);			
				//add the selected node pairs
				ArrayList<NodePair> pairsForTrafPrcnt = new ArrayList<NodePair>(pairListTotal);
				//add the list of node pairs to the map
				trafPairs.put(trafPrcnt, pairsForTrafPrcnt);
			}
				
			//iterate through the traf percentages
			for(Map.Entry<Double, ArrayList<NodePair>> entryTraf : trafPairs.entrySet()){
				// SD pairs between which we desire traffic to be
				// Store each s-d pair
				List<TrafficNodes> pair_list = new ArrayList<TrafficNodes>();			
				//iterate through the node pairs and generate the vertex list
				ArrayList<BaseVertex> usedVerts = new ArrayList<BaseVertex>();
				//total population besides current vertex
				Map<BaseVertex, Double> remPopulation = new HashMap<BaseVertex, Double>();
				double totalPopulation = 0;
				for(NodePair np : entryTraf.getValue()){					
					if(!usedVerts.contains(np.v1)){
						usedVerts.add(np.v1);
						//System.out.println("Population of City " + np.v1.get_id() + " = " + popUsCities.get(np.v1));
						totalPopulation += popUsCities.get(np.v1);						
					}
					if(remPopulation.get(np.v1) != null){
						double nwPop = remPopulation.get(np.v1) + popUsCities.get(np.v2);
						remPopulation.put(np.v1,nwPop);
					}else{
						remPopulation.put(np.v1,popUsCities.get(np.v2));
					}
				}
				//System.out.println("Total Population = " + totalPopulation);
				//weight of each vertex based on population
				Map<BaseVertex, Double> relWtOfEachVert = new HashMap<BaseVertex, Double>();
				
				for(BaseVertex usedVert : usedVerts){
					relWtOfEachVert.put(usedVert, (popUsCities.get(usedVert)/totalPopulation));					
				}
				//System.out.println("Relative Weight for each vertex");
				double totalRelWt = 0.0;
				for(Map.Entry<BaseVertex, Double> entryVrt : relWtOfEachVert.entrySet()){
					//System.out.println("\t" + entryVrt.getKey().get_id() + " = " + entryVrt.getValue());
					totalRelWt += entryVrt.getValue();
				}
				/*System.out.println("Total relative weight = " + totalRelWt);
				System.out.println("Remaining Population for each vertex");
				for(Map.Entry<BaseVertex, Double> entryPop : remPopulation.entrySet()){
					System.out.println("\t" + entryPop.getKey().get_id() + " = " + entryPop.getValue());
				}*/
				
				//assign the connection Bw to traffic flows
				for(NodePair np : entryTraf.getValue()){
					//System.out.println("(" + np.v1.get_id() + "," + np.v2.get_id() + ") = " + popUsCities.get(np.v2)/remPopulation.get(np.v1));
					double connBw = relWtOfEachVert.get(np.v1)*(popUsCities.get(np.v2)/remPopulation.get(np.v1))*trafficLoad;
					pair_list.add(new TrafficNodes(np.v1,np.v2,2,connBw));
				}
				
				//all possible shortest path bandwidths
				double aspBw = preProcFunctions.totalBwUsedOnShortestPaths(sdpaths, pair_list);	
				System.out.println("\n\nIteration No.: " + reRunNo + " ; Traffic Percentage = " + entryTraf.getKey() + " ; ASP = " + aspBw);
				System.out.println("\tTraffic Flows: " + pair_list.size());
				double totalFlow = 0.0;
				for(TrafficNodes tn : pair_list){
					if(tn.flow_traffic < 0){
						totalFlow += tn.flow_traffic;
						System.out.println("\t\t\t(" + tn.v1.get_id() + "," + tn.v2.get_id() + "," + tn.chain_index + "," + tn.flow_traffic + ")");
					}else{
						totalFlow += tn.flow_traffic;
						System.out.println("\t\t(" + tn.v1.get_id() + "," + tn.v2.get_id() + "," + tn.chain_index + "," + tn.flow_traffic + ")");
					}
				}
				System.out.println("Total Flow = " + totalFlow);
				if(aspValuesForTrafPrcnt.get(entryTraf.getKey()) != null){
					aspValuesForTrafPrcnt.get(entryTraf.getKey()).add(aspBw);
				}else{
					ArrayList<Double> aspValues = new ArrayList<Double>();
					//add value to arraylist
					aspValues.add(aspBw);
					//add the bw values
					aspValuesForTrafPrcnt.put(entryTraf.getKey(), aspValues);
				}					
				
				//List of the service chains to be deployed
				List<Integer> scUsed = preProcFunctions.serviceChainsUsed(pair_list);		
				//print out the pair lists
				preProcFunctions.printSDpairs(pair_list, scUsed);	    
				
				//VNFs used across the service chains deployed
				List<FuncPt> func_list = preProcFunctions.listOfVNFsUsed(vnf_list, ChainSet, scUsed);		
				//print out the function list
				preProcFunctions.printListOfVNFsUsed(func_list);				
				
				//get maximum number of clusters
	 			int maxClusterCount = ClusterHeuristic.formClustersForGivenSdPairs(g, pair_list).size();	 			
		    	String filename = "PlotData_Traf_" + entryTraf.getKey() + "_Iteration_" + reRunNo +  "_K_" + kNodeCount + ".txt";
	    	    BufferedWriter bw = new BufferedWriter(new FileWriter(filename));    	   
	    	    
	    	    //iterate through cluster counts
	    	    for(int numOfClusters=1;numOfClusters<=maxClusterCount;numOfClusters++){
	    	    	//int numOfClusters = maxClusterCount;
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
	    		    
	    		    //replica constraint per VNF
	    		    Map<Integer,Integer> replicaPerVNF = new HashMap<Integer,Integer>(CountMaxVNF);
	    		   
	    		    //form the clusterGroups
	    		    ArrayList<ClusterTrafficPairs> clusterGroups = ClusterHeuristic.formClustersV2(g, numOfClusters, copyOfPairList);	    		    
	    		    
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
				  	
					//calculate the core and link constraints
			  	  	boolean coreCstr = false;
			  	  	boolean capCstr = false;
			  	  	Map<BaseVertex,Double> cpuCoreCount = new HashMap<BaseVertex,Double>();
			  	  	Map<NodePair,Double> linkCapacity = new HashMap<NodePair,Double>();
			  	  	CG.runCG(coreCstr,capCstr,cpuCoreCount,linkCapacity,g, ChainSet, copyOfPairList, scUsed, vnf_list, func_list, serviceChainTN, nfv_nodes, 
						  nodesNFVI, vertex_list_without_nfvi_nodes, scCopies, scCopyToSC, configsPerSC, configPerSD, CountMaxVNF, replicaPerVNF,kNodeCount);
			  	  	
			  	  	//write to a file
			  	  	bw.write(numOfClusters + "\t" + CG.rmpIlpValue  + "\t" + CG.vnfPlacementNodes.size() + "\t" + CG.vnfPlacementNodes.toString()
			  	  			+ "\t" + preProcFunctions.totalBwUsedOnShortestPaths(sdpaths, copyOfPairList) + "\t" +
			  	  			CG.eoptimality + "\t" + CG.cgRunTime + "\t" + CG.ilpRunTime + "\t" + CG.totalTime + "\n");
			  	  
			  		//add bw values for clusters for various traffic percentage
			  	  	if(bwValuesForClustersAndTrafPrcnt.get(entryTraf.getKey()) != null){
			  	  		ArrayList<ArrayList<Double>> bwValues = bwValuesForClustersAndTrafPrcnt.get(entryTraf.getKey());
			  	  		//check if array list for cluster count exists in array list
			  	  		if(bwValues.size() >= numOfClusters){			  	  			
			  	  			//add list		  	  			
			  	  			bwValues.get(numOfClusters-1).add(CG.rmpIlpValue);			  	  		
			  	  		}else{
			  	  			//ArrayList of storing values bw values for a particular cluster count
				  	  		ArrayList<Double> bwValuesForClusterCount = new ArrayList<Double>();
				  	  		//add value
				  	  		bwValuesForClusterCount.add(CG.rmpIlpValue);			  	  		
				  	  		//add list
				  	  		bwValues.add(bwValuesForClusterCount);					  	  				  	  			
			  	  		}
			  	  	}else{
			  	  		//create the array list for storing bandwidth values
			  	  		ArrayList<ArrayList<Double>> bwValues = new ArrayList<ArrayList<Double>>();
			  	  		//ArrayList of storing values bw values for a particular cluster count
			  	  		ArrayList<Double> bwValuesForClusterCount = new ArrayList<Double>();
			  	  		//add value			  	  		
			  	  		bwValuesForClusterCount.add(CG.rmpIlpValue);		  	  		
			  	  		//add list
			  	  		bwValues.add(bwValuesForClusterCount);	
			  	  		//put in map
			  	  		bwValuesForClustersAndTrafPrcnt.put(entryTraf.getKey(),bwValues);
			  	  	}
			  	  	
			  	  
			  	  	//break if the ILP value matches the all_shortest_path values
			  	  	if( Math.ceil(CG.rmpIlpValue) == Math.ceil(aspBw) ){	 		
			  	  		//add to the set of cluster counts
			  	  		if(clusterCountValuesForTrafPrcnt.get(entryTraf.getKey()) != null){
			  	  			clusterCountValuesForTrafPrcnt.get(entryTraf.getKey()).add(numOfClusters);
			  	  		}else{
			  	  			//create the array list
			  	  			ArrayList<Integer> clusterCountValues = new ArrayList<Integer>();
			  	  			clusterCountValues.add(numOfClusters);
			  	  			//add the cluster count values
							clusterCountValuesForTrafPrcnt.put(entryTraf.getKey(), clusterCountValues);				  	  			
			  	  		}	
			  	  		//add to the max cluster count
			  	  		if(numOfClusters > maxClusterCountForTraffic.get(entryTraf.getKey())){
			  	  			maxClusterCountForTraffic.put(entryTraf.getKey(), numOfClusters);
			  	  		}
			  	  		//add the aspBw bandwidth
			  	  		ArrayList<ArrayList<Double>> bwValues = bwValuesForClustersAndTrafPrcnt.get(entryTraf.getKey());
			  	  		for(int clusterCount = numOfClusters+1; clusterCount <= copyOfPairList.size(); clusterCount++){
			  	  			if(bwValues.size() > clusterCount){
			  	  				//System.out.println("Number of Clusters = " + numOfClusters  + " ; Size of List = " + bwValues.size());
			  	  				bwValues.get(clusterCount-1).add(aspBw);
			  	  			}else{
			  	  				//ArrayList of storing values bw values for a particular cluster count
					  	  		ArrayList<Double> bwValuesForClusterCount = new ArrayList<Double>();
					  	  		bwValuesForClusterCount.add(aspBw);
					  	  		//add to array list
					  	  		bwValues.add(bwValuesForClusterCount);
			  	  			}
			  	  		}
			  	  		break;
			  	  	}				  	  	
			  	  		
	    	    }			
	    	    //close the file writer
			    bw.close();	
				
			    
			    String filenameTA = "TA_PlotData_Traf_" + entryTraf.getKey() + "_Iteration_" + reRunNo +  "_K_" + kNodeCount + ".txt";
	    	    BufferedWriter bwTA = new BufferedWriter(new FileWriter(filenameTA));     	 
	    	    
	    	    //iterate through cluster counts
	    	    for(int numOfClusters=1;numOfClusters<=maxClusterCount;numOfClusters++){
	    	    	//int numOfClusters = maxClusterCount;
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
	    		    
	    		    //replica constraint per VNF
	    		    Map<Integer,Integer> replicaPerVNF = new HashMap<Integer,Integer>(CountMaxVNF);
	    		   
	    		    //form the clusterGroups
	    		    ArrayList<ClusterTrafficPairs> clusterGroups = ClusterHeuristic.formClustersV2withFlowVolumeAwareness(g, numOfClusters, copyOfPairList);	    		    
	    		    
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
				  	
					//calculate the core and link constraints
			  	  	boolean coreCstr = false;
			  	  	boolean capCstr = false;
			  	  	Map<BaseVertex,Double> cpuCoreCount = new HashMap<BaseVertex,Double>();
			  	  	Map<NodePair,Double> linkCapacity = new HashMap<NodePair,Double>();
			  	  	CG.runCG(coreCstr,capCstr,cpuCoreCount,linkCapacity,g, ChainSet, copyOfPairList, scUsed, vnf_list, func_list, serviceChainTN, nfv_nodes, 
						  nodesNFVI, vertex_list_without_nfvi_nodes, scCopies, scCopyToSC, configsPerSC, configPerSD, CountMaxVNF, replicaPerVNF,kNodeCount);
			  	  	
			  	  	//write to a file
			  	  	bwTA.write(numOfClusters + "\t" + CG.rmpIlpValue  + "\t" + CG.vnfPlacementNodes.size() + "\t" + CG.vnfPlacementNodes.toString()
			  	  			+ "\t" + preProcFunctions.totalBwUsedOnShortestPaths(sdpaths, copyOfPairList) + "\t" +
			  	  			CG.eoptimality + "\t" + CG.cgRunTime + "\t" + CG.ilpRunTime + "\t" + CG.totalTime + "\n");
			  	  
			  	  	//add bw values for clusters for various traffic percentage
			  	  	if(bwValuesForClustersAndTrafPrcntTA.get(entryTraf.getKey()) != null){
			  	  		ArrayList<ArrayList<Double>> bwValues = bwValuesForClustersAndTrafPrcntTA.get(entryTraf.getKey());
			  	  		//check if array list for cluster count exists in array list
			  	  		if(bwValues.size() >= numOfClusters){
			  	  			//add list			  	  		
			  	  			bwValues.get(numOfClusters-1).add(CG.rmpIlpValue);			  	  			
			  	  		}else{
			  	  			//ArrayList of storing values bw values for a particular cluster count
				  	  		ArrayList<Double> bwValuesForClusterCount = new ArrayList<Double>();
				  	  		//add value				  	  		
				  	  		bwValuesForClusterCount.add(CG.rmpIlpValue);				  	  		
				  	  		//add list
				  	  		bwValues.add(bwValuesForClusterCount);					  	  				  	  			
			  	  		}
			  	  	}else{
			  	  		//create the array list for storing bandwidth values
			  	  		ArrayList<ArrayList<Double>> bwValues = new ArrayList<ArrayList<Double>>();
			  	  		//ArrayList of storing values bw values for a particular cluster count
			  	  		ArrayList<Double> bwValuesForClusterCount = new ArrayList<Double>();
			  	  		//add value			  	  		
			  	  		bwValuesForClusterCount.add(CG.rmpIlpValue);			  	  		
			  	  		//add list
			  	  		bwValues.add(bwValuesForClusterCount);	
			  	  		//put in map
			  	  		bwValuesForClustersAndTrafPrcntTA.put(entryTraf.getKey(),bwValues);
			  	  	}
			  	  
			  	  	//break if the ilp value matches the all_shortest_path values
			  	  	if( Math.ceil(CG.rmpIlpValue) == Math.ceil(aspBw) ){
			  	  		//add to the cluster count values
				  	  	if(clusterCountValuesForTrafPrcntTA.get(entryTraf.getKey()) != null){
			  	  			clusterCountValuesForTrafPrcntTA.get(entryTraf.getKey()).add(numOfClusters);
			  	  		}else{
			  	  			//create the arraylist
			  	  			ArrayList<Integer> clusterCountValuesTA = new ArrayList<Integer>();
			  	  			clusterCountValuesTA.add(numOfClusters);
			  	  			//add the cluster count values
							clusterCountValuesForTrafPrcntTA.put(entryTraf.getKey(), clusterCountValuesTA);				  	  			
			  	  		}	
				  	  	//add to the max cluster count
			  	  		if(numOfClusters > maxClusterCountForTraffic.get(entryTraf.getKey())){
			  	  			maxClusterCountForTraffic.put(entryTraf.getKey(), numOfClusters);
			  	  		}
			  	  		//add the aspBw bandwidth
			  	  		ArrayList<ArrayList<Double>> bwValues = bwValuesForClustersAndTrafPrcntTA.get(entryTraf.getKey());
			  	  		for(int clusterCount = (numOfClusters+1); clusterCount <= copyOfPairList.size(); clusterCount++){
			  	  			if(bwValues.size() > clusterCount){
			  	  				//System.out.println("Number of Clusters = " + numOfClusters  + " ; Size of List = " + bwValues.size());
			  	  				bwValues.get(clusterCount-1).add(aspBw);
			  	  			}else{
			  	  				//ArrayList of storing values bw values for a particular cluster count
					  	  		ArrayList<Double> bwValuesForClusterCount = new ArrayList<Double>();
					  	  		bwValuesForClusterCount.add(aspBw);
					  	  		//add to array list
					  	  		bwValues.add(bwValuesForClusterCount);
			  	  			}
			  	  		}
				  	  	break;
			  	  	}
	    	    }
	    	    //close the file writer
			    bwTA.close();	
			}
		}		
		
		//write the box plot file
	    //name of file for box plot
		String networkName1 = InputConstants.NETWORK_FILE_NAME.substring(0, InputConstants.NETWORK_FILE_NAME.lastIndexOf("."));
		String clusterCountBoxPlotFileName = "Compare_Skewed_ClusterCount_BoxPlotData_" + networkName1 + ".txt";
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(clusterCountBoxPlotFileName));
		System.out.println("\nCluster Count Comparisons!");
		//iterate through the values
		for(int iterationCount=0; iterationCount<maxIterationCount; iterationCount++){
			System.out.println("\tIteration Count = " + iterationCount);
			System.out.print("\t\t");
			for(Map.Entry<Double, ArrayList<Integer>> entry : clusterCountValuesForTrafPrcnt.entrySet()){				
				ArrayList<Integer> countTA = clusterCountValuesForTrafPrcntTA.get(entry.getKey());
				if(iterationCount < entry.getValue().size() ){
					bw2.write(entry.getValue().get(iterationCount) + "\t" + countTA.get(iterationCount) + "\t");
					System.out.print(entry.getValue().get(iterationCount) + "\t" + countTA.get(iterationCount) + "\t");
				}
			}
			bw2.write("\n");
			System.out.print("\n");
		}
		//close the file
		bw2.close();	
		
		//iterate through the traffic percentages
		for(Double trafPrcnt : trafMatPrcnt){
			String bwBoxPlotFileName = "Compare_Bandwidth_Skewed_BoxPlotData_Traf_" + trafPrcnt + "_"  + networkName1 + ".txt";
			System.out.println("\nBandwidth Comparison : " + trafPrcnt);
			BufferedWriter bw3 = new BufferedWriter(new FileWriter(bwBoxPlotFileName));
			ArrayList<ArrayList<Double>> bwValues = bwValuesForClustersAndTrafPrcnt.get(trafPrcnt);			
			ArrayList<ArrayList<Double>> bwValuesTA = bwValuesForClustersAndTrafPrcntTA.get(trafPrcnt);
			int minIter = maxClusterCountForTraffic.get(trafPrcnt);
			for(int rowNum=0; rowNum<maxIterationCount; rowNum++){
				System.out.print("\n\t\t");
				for(int instanceCount=0; instanceCount<minIter; instanceCount++){
					ArrayList<Double> bwValuesForClusterCount = bwValues.get(instanceCount);
					if(bwValuesForClusterCount.size()>rowNum){
						bw3.write(String.valueOf(bwValuesForClusterCount.get(rowNum)));
						System.out.print(String.valueOf(bwValuesForClusterCount.get(rowNum)));
					}
					bw3.write("\t");
					System.out.print("\t");
					ArrayList<Double> bwValuesForClusterCountTA = bwValuesTA.get(instanceCount);
					if(bwValuesForClusterCountTA.size()>rowNum){
						bw3.write(String.valueOf(bwValuesForClusterCountTA.get(rowNum)));
						System.out.print(String.valueOf(bwValuesForClusterCountTA.get(rowNum)));
					}
					bw3.write("\t");
					System.out.print("\t");
				}
				//start a new row
				bw3.write("\n");
				System.out.print("\n");
			}
			//close the file
			bw3.close();
		}
		
		//write the box plot file		
		String aspBoxPlotFileName = "ASP_Bandwidth_Skewed_BoxPlotData_" + networkName1 + ".txt";
		BufferedWriter bw4 = new BufferedWriter(new FileWriter(aspBoxPlotFileName));
		//iterate through the values
		for(int iterationCount=0; iterationCount<maxIterationCount; iterationCount++){
			for(Map.Entry<Double, ArrayList<Double>> entry : aspValuesForTrafPrcnt.entrySet()){
				if(iterationCount < entry.getValue().size() ){
					bw4.write(entry.getValue().get(iterationCount) + "\t");
				}
			}
			bw4.write("\n");
		}
		//close the file
		bw4.close();
	}
}
