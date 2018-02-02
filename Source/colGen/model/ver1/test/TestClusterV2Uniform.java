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
import colGen.model.trafficGen.TrafficGenerator;
import colGen.model.ver1.CG;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class TestClusterV2Uniform {
	public static void runTest() throws Exception{
		//maximum iteration count
 		int maxIterationCount = 10;
		
		//traffic matrix percentages 		
 		ArrayList<Double> trafMatPrcnt = new ArrayList<Double>();
 		trafMatPrcnt.add(0.05); trafMatPrcnt.add(0.1); trafMatPrcnt.add(0.15);trafMatPrcnt.add(0.2);	
 		trafMatPrcnt.add(0.25); trafMatPrcnt.add(0.50); trafMatPrcnt.add(0.75);trafMatPrcnt.add(1.0); 		
 				 		
 		
 		//best bandwidth associated with each traffic percentage
 		Map<Double, ArrayList<Double>> aspValuesForTrafPrcnt = new TreeMap<Double, ArrayList<Double>>();
 		//cluster counts associated with each traffic percentage
 		Map<Double,ArrayList<Integer>> clusterCountValuesForTrafPrcnt= new TreeMap<Double,ArrayList<Integer>>(); 	
 		
 		//bandwidth associated with each cluster count for each traffic percentage
 		Map<Double, ArrayList<ArrayList<Double>>> bwValuesForClustersAndTrafPrcnt = new TreeMap<Double, ArrayList<ArrayList<Double>>>();  		
 		
 		
		//generate the graph object
	    Graph g = PreProcVer1.makeGraphObject();
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
		double trafficLoad = 1000*totalNumberOfSdPairs;
		
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
			    //flow bandwidth per connection
				double flowBw = Math.ceil(trafficLoad/entryTraf.getValue().size());
				//iterate through the node pairs and create the traffic flows
				for(NodePair np : entryTraf.getValue()){					
					if(np.v1.get_id() != np.v2.get_id()){					
						pair_list.add(new TrafficNodes(np.v1,np.v2,2,flowBw));					
					}					
				}
				
				//all possible shortest path bandwidths
				double aspBw = preProcFunctions.totalBwUsedOnShortestPaths(sdpaths, pair_list);	
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
	 			//int kNodeCount=1;
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
			    
			}	
		}
		
		
		//write the box plot file
	    //name of file for box plot
		String networkName1 = InputConstants.NETWORK_FILE_NAME.substring(0, InputConstants.NETWORK_FILE_NAME.lastIndexOf("."));
		String clusterCountBoxPlotFileName = "Uniform_ClusterCount_BoxPlotData_" + networkName1 + ".txt";
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(clusterCountBoxPlotFileName));
		//iterate through the values
		for(int iterationCount=0; iterationCount<maxIterationCount; iterationCount++){
			for(Map.Entry<Double, ArrayList<Integer>> entry : clusterCountValuesForTrafPrcnt.entrySet()){			
				if(iterationCount < entry.getValue().size() ){
					bw2.write(entry.getValue().get(iterationCount) + "\t");
				}
			}
			bw2.write("\n");
		}
		//close the file
		bw2.close();
		
		//iterate through the traffic percentages
		for(Double trafPrcnt : trafMatPrcnt){
			String bwBoxPlotFileName = "Bandwidth_Uniform_BoxPlotData_Traf_" + trafPrcnt + "_" + networkName1 + ".txt";
			BufferedWriter bw3 = new BufferedWriter(new FileWriter(bwBoxPlotFileName));
			ArrayList<ArrayList<Double>> bwValues = bwValuesForClustersAndTrafPrcnt.get(trafPrcnt);			
			int minIter = maxClusterCountForTraffic.get(trafPrcnt);
			for(int rowNum=0; rowNum<maxIterationCount; rowNum++){
				for(int instanceCount=0; instanceCount<minIter; instanceCount++){
					ArrayList<Double> bwValuesForClusterCount = bwValues.get(instanceCount);
					if(bwValuesForClusterCount.size()>rowNum){
						bw3.write(String.valueOf(bwValuesForClusterCount.get(rowNum)));
					}
					bw3.write("\t");				
				}
				//start a new row
				bw3.write("\n");
			}
			//close the file
			bw3.close();
		}
		
		//write the box plot file		
		String aspBoxPlotFileName = "ASP_Bandwidth_Uniform_BoxPlotData_" + networkName1 + ".txt";
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
