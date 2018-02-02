package colGen.model.ver1.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
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

public class TestClusterV2VariableTrafficIteration {	
	public static void runTest() throws Exception{
		//traffic matrix percentages
 		/*Map<Double,Integer> trafMatPrcnt = new HashMap<Double,Integer>();
 		trafMatPrcnt.put(0.05,35);trafMatPrcnt.put(0.1,30);
 		trafMatPrcnt.put(0.15,25);trafMatPrcnt.put(0.2,20);	
 		trafMatPrcnt.put(0.25,15);trafMatPrcnt.put(0.50,10);
 		trafMatPrcnt.put(0.75,5);trafMatPrcnt.put(1.0,1);*/
 		
 		Map<Double,Integer> trafMatPrcnt = new HashMap<Double,Integer>();
 		trafMatPrcnt.put(0.05,100);trafMatPrcnt.put(0.1,100);
 		trafMatPrcnt.put(0.15,100);trafMatPrcnt.put(0.2,100);	
 		trafMatPrcnt.put(0.25,100);trafMatPrcnt.put(0.50,100);
 		trafMatPrcnt.put(0.75,100);trafMatPrcnt.put(1.0,1);
 		
 		/*trafMatPrcnt.put(0.05,5);trafMatPrcnt.put(0.1,5);
 		trafMatPrcnt.put(0.15,5);trafMatPrcnt.put(0.2,5);	
 		trafMatPrcnt.put(0.25,5);trafMatPrcnt.put(0.50,5);
 		trafMatPrcnt.put(0.75,5);trafMatPrcnt.put(1.0,1);*/
 		
 		/*trafMatPrcnt.put(0.05,2);trafMatPrcnt.put(0.1,2);		
 		trafMatPrcnt.put(0.15,1);trafMatPrcnt.put(0.2,1);	
 		trafMatPrcnt.put(0.25,1);trafMatPrcnt.put(0.50,1);
 		trafMatPrcnt.put(0.75,1);trafMatPrcnt.put(1.0,1);*/
 		//maximum iteration count
 		int maxIterationCount = Collections.max(trafMatPrcnt.values()); 		
 		
 		//best bandwidth associated with each traffic percentage
 		Map<Double, ArrayList<Double>> bwValuesForTrafPrcnt = new TreeMap<Double, ArrayList<Double>>();
 		//cluster counts associated with each traffic percentage
 		Map<Double,ArrayList<Integer>> clusterCountValuesForTrafPrcnt= new TreeMap<Double,ArrayList<Integer>>();
		
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
 		Map<Integer, ServiceChain> ChainSet = PreProcVer1.populateChainSetBasedOnScenario1();
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
		
		
		
		//iterate through the traffic counts
		for(Map.Entry<Double,Integer> entryTraf: trafMatPrcnt.entrySet()){
			
			//bw values
			ArrayList<Double> bwValues = new ArrayList<Double>();
			//cluster count values
			ArrayList<Integer> clusterCountValues = new ArrayList<Integer>();
			
			//iterate that many number of times
			for(int reRunNo=0; reRunNo < entryTraf.getValue(); reRunNo++){
				//selected node pairs
				ArrayList<NodePair> selPairs = TrafficGenerator.generateNodePairs(entryTraf.getKey(), totalNumberOfSdPairs, g._vertex_list);			
				
				// SD pairs between which we desire traffic to be
				// Store each s-d pair
				List<TrafficNodes> pair_list = new ArrayList<TrafficNodes>();
			    //flow bandwidth per connection
				double flowBw = Math.ceil(trafficLoad/selPairs.size());
				//iterate through the node pairs and create the traffic flows
				for(NodePair np : selPairs){					
					if(np.v1.get_id() != np.v2.get_id()){					
						pair_list.add(new TrafficNodes(np.v1,np.v2,0,flowBw));					
					}					
				}
				
				//all possible shortest path bandwidths
				double aspBw = preProcFunctions.totalBwUsedOnShortestPaths(sdpaths, pair_list);	
				//add value to arraylist
				bwValues.add(aspBw);
				
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
			  	  
			  	  
			  	  	//break if the ilp value matches the all_shortest_path values
			  	  	if(CG.rmpIlpValue==aspBw){
			  	  		clusterCountValues.add(numOfClusters);
			  	  		break;
			  	  	}
	    	    }			
	    	    //close the file writer
			    bw.close();			
			}
			//add the bw values
			bwValuesForTrafPrcnt.put(entryTraf.getKey(), bwValues);
			//add the cluster count values
			clusterCountValuesForTrafPrcnt.put(entryTraf.getKey(), clusterCountValues);
		}
		
		//write the box plot file
	    //name of file for box plot
		String networkName1 = InputConstants.NETWORK_FILE_NAME.substring(0, InputConstants.NETWORK_FILE_NAME.lastIndexOf("."));
		String clusterCountBoxPlotFileName = "ClusterCount_BoxPlotData_" + networkName1 + ".txt";
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
		
		//write the box plot file
		String networkName2 = InputConstants.NETWORK_FILE_NAME.substring(0, InputConstants.NETWORK_FILE_NAME.lastIndexOf("."));
		String bwBoxPlotFileName = "Bandwidth_BoxPlotData_" + networkName2 + ".txt";
		BufferedWriter bw3 = new BufferedWriter(new FileWriter(bwBoxPlotFileName));
		//iterate through the values
		for(int iterationCount=0; iterationCount<maxIterationCount; iterationCount++){
			for(Map.Entry<Double, ArrayList<Double>> entry : bwValuesForTrafPrcnt.entrySet()){
				if(iterationCount < entry.getValue().size() ){
					bw3.write(entry.getValue().get(iterationCount) + "\t");
				}
			}
			bw3.write("\n");
		}
		//close the file
		bw3.close();
	}
}