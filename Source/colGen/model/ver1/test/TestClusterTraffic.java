package colGen.model.ver1.test;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.data.ListOfTrafficFlows;
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

public class TestClusterTraffic {
	
	//run the test cluster experiments
	public static void runTest() throws Exception{
		//traffic distribution
		Boolean UniformTraffic = false;
		//Flow Aware 
		Boolean flowAware = true;
		//traffic load
 		double trafficLoad = 1000000.0;//1 Tbps in Mbps
 		//service chains per service
 		int numberOfScPerService = 1;
 		//traffic matrix percentages
 		ArrayList<Double> trafMatPrcnt = new ArrayList<Double>();
 		trafMatPrcnt.add(0.01);trafMatPrcnt.add(0.02);trafMatPrcnt.add(0.05);
 		trafMatPrcnt.add(0.1);trafMatPrcnt.add(0.15);trafMatPrcnt.add(0.2);	
 		trafMatPrcnt.add(0.25);trafMatPrcnt.add(0.50);trafMatPrcnt.add(0.75);trafMatPrcnt.add(1.0);	
 		
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
	    
	    //get the set of service chains
 		Map<Integer, ServiceChain> ChainSet = preProcFunctions.setOfServiceChains();
 		// print out the Set of Service Chains
 		preProcFunctions.printServiceChains(ChainSet);
 		
 		//total list of VNF available
		List<FuncPt> vnf_list = PreProcVer1.totalListOfVNFs(); 		
 		
		//iterate through the traffic percentages
 		for(Double trafPrcnt : trafMatPrcnt){
			
 			System.out.println("Traffic Percentage: " + trafPrcnt); 			
	 		//list of traffic nodes
	 		ArrayList<TrafficNodes> pairList = new ArrayList<TrafficNodes>();
	 		
	 		
	 		
	 		//persistently stored file name
	 		String trafDist = "";
	 		if(UniformTraffic){
	 			trafDist = "Uniform";
	 		}else{
	 			trafDist = "Skewed";
	 		}
	 		String networkName = InputConstants.NETWORK_FILE_NAME.substring(0, InputConstants.NETWORK_FILE_NAME.lastIndexOf("."));	 		
	 		String trafficFlowFileName = trafDist + "_" + trafPrcnt + "_" + networkName + ".ser";
	 		//check if persistent file exists
	 		//build graph object from given network file
		    Class<?> cls = Class.forName("colGen.model.simulation.Sim");		
		    //returns the ClassLoader
		    ClassLoader cLoader = cls.getClassLoader();
		    //finds the resource with the given name
		    InputStream networkFileStream = cLoader.getResourceAsStream(InputConstants.FILE_READ_PATH + trafficFlowFileName);
		    /*System.out.println(networkFileStream);
		    System.out.println(trafficFlowFileName);*/
		    if(networkFileStream != null){
		    	System.out.println("INPUT FILE STREAM EXISTS!");
		    	ObjectInputStream in = new ObjectInputStream(networkFileStream); 
		    	ListOfTrafficFlows trafficFlows = (ListOfTrafficFlows) in.readObject();
		    	in.close();
		    	networkFileStream.close();
		    	ArrayList<TrafficNodes> tempTrafficList = trafficFlows.trafficFlows;
		    	for(TrafficNodes tn : tempTrafficList){
		    		BaseVertex src = g.get_vertex(tn.v1.get_id());
		    		BaseVertex dst = g.get_vertex(tn.v2.get_id());
		    		pairList.add(new TrafficNodes(src,dst,tn.chain_index,tn.flow_traffic));
		    	}
		    }else{	 
		    	System.out.println("Input file stream does not exist!");
		    	//generate traffic flows for 
		 		//how populated the traffic matrix is
		 		//how many service chains for the 4 services are deployed
		 		//what is the traffic load
		 		if(UniformTraffic){
		 			TrafficGenerator.generateTrafficFlows(pairList,trafPrcnt,totalNumberOfSdPairs,numberOfScPerService,trafficLoad,g);
		 		}else{
		 			TrafficGenerator.generateTrafficFlowsSkewed(pairList,trafPrcnt,totalNumberOfSdPairs,numberOfScPerService,trafficLoad,g);
		 		}
		 		//else write the file
		 		try {
		 	         FileOutputStream fileOut = new FileOutputStream(trafficFlowFileName);
		 	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
		 	         out.writeObject(new ListOfTrafficFlows(pairList));
		 	         /*for(TrafficNodes tn : pairList){
		 	        	 out.writeObject(tn);
		 	         }*/
		 	         out.close();
		 	         fileOut.close();
		 	         System.out.printf("Serialized data is saved as " +  trafficFlowFileName);
		 	    }catch(IOException i) {
		 	         i.printStackTrace();
		 	    }
		    }
	 		
	 		//List of the service chains to be deployed
			List<Integer> scUsed = preProcFunctions.serviceChainsUsed(pairList);		
			//print out the pair lists
			preProcFunctions.printSDpairs(pairList, scUsed);
			
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
		    
			/*for(TrafficNodes tn : pairList){
				System.out.println("(" + tn.v1.get_id()+","+tn.v2.get_id()+") ; "+tn.chain_index + " ; " + tn.flow_traffic);
			}*/
			//traffic pairs for each service chain deployed	
			Map<Integer,ArrayList<TrafficNodes>> serviceChainTN = preProcFunctions.sdPairsforServiceChain(scUsed, pairList);
			//maximum clusters per service chain
			Map<Integer, Integer> maxClusterCountPerSC = new HashMap<Integer,Integer>();
	 		//maximum number of clusters //to avoid any errors in computation
	 		int maxClusterCount = 0;
	 		for(int scID : scUsed){
	 			//get maximum number of clusters
	 			int clusterCount = ClusterHeuristic.formClustersForGivenSdPairs(g, serviceChainTN.get(scID)).size();
	 			if(maxClusterCount < clusterCount){
	 				maxClusterCount = clusterCount;
	 			}
	 			//get max count per service chain
	 			maxClusterCountPerSC.put(scID,clusterCount);
	 		}
	 			 		
	 				
	 		//check for enforcing the number of NFV capable nodes available
//		    for(int kNodeCount=1; kNodeCount<=numOfNfvNodes; kNodeCount++){
	 			int kNodeCount=numOfNfvNodes;
		    	String filename = "PlotData_Traf_" + trafPrcnt + "_Type_" + trafDist +  "_K_" + kNodeCount + ".txt";
	    	    BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
	    	    for(int scID : scUsed){
	    	    	bw.write(scID + "\t" + maxClusterCountPerSC.get(scID) + "\n");
	    	    }
	    	    
	    	    //iterate through cluster counts
	    	    for(int numOfClusters=1;numOfClusters<=maxClusterCount;numOfClusters++){
	    	    	//int numOfClusters=1;
	    	    	//create a copy of pairList //deep copy
	    	    	ArrayList<TrafficNodes> copyOfPairList = new ArrayList<TrafficNodes>();	    	    	
	    	    	for(TrafficNodes tn : pairList){
	    	    		copyOfPairList.add(new TrafficNodes(tn.v1,tn.v2,tn.chain_index,tn.flow_traffic));
	    	    	}
	    	    	
	    	    	//traffic pairs for each service chain deployed	
	    			serviceChainTN = preProcFunctions.sdPairsforServiceChain(scUsed, copyOfPairList);
	    		    //print out the traffic nodes available for each service chain
	    		    preProcFunctions.printTrafficNodesForServiceChains(scUsed, serviceChainTN);
	    	 		
	    		    //max number of VNFs
	    		    Map<Integer, ArrayList<Integer>> funcInSC = preProcFunctions.vnfInSCs(scUsed, func_list, ChainSet);
	    		    Map<Integer,Integer> CountMaxVNF = preProcFunctions.countMaxVnfBasedOnSdPairs(ChainSet, funcInSC, serviceChainTN);
	    		    
	    		    //replica constraint per VNF
	    		    Map<Integer,Integer> replicaPerVNF = new HashMap<Integer,Integer>(CountMaxVNF);
	    		    
	    		    //enforce constraint on maximum number of VNFs
	    		    /*for(Map.Entry<Integer,Integer> entry : replicaPerVNF.entrySet()){
	    		    	replicaPerVNF.put(entry.getKey(),1);
	    		    }*/   	    	    	
	    	    	
	    	    	//form clusters
	    		    // List<ClusterTrafficPairs> clusterGroups = null;
	    		    Map<Integer, ArrayList<ClusterTrafficPairs>> clusterGroups = new HashMap<Integer, ArrayList<ClusterTrafficPairs>>();
	    		    if(flowAware){
	    		    	for(Integer scID : scUsed){
	    		    		if(numOfClusters > maxClusterCountPerSC.get(scID)){
	    		    			clusterGroups.put(scID, ClusterHeuristic.formClustersV2withFlowVolumeAwareness(g, maxClusterCountPerSC.get(scID), serviceChainTN.get(scID)));
	    		    		}else{
	    		    			clusterGroups.put(scID, ClusterHeuristic.formClustersV2withFlowVolumeAwareness(g, numOfClusters, serviceChainTN.get(scID)));
	    		    		}
	    		    	}	    		    	
	    		    }else{
	    		    	for(int scID : scUsed){
	    		    		if(numOfClusters > maxClusterCountPerSC.get(scID)){
	    		    			clusterGroups.put(scID, ClusterHeuristic.formClustersV2(g, maxClusterCountPerSC.get(scID), serviceChainTN.get(scID)));
	    		    		}else{
	    		    			clusterGroups.put(scID, ClusterHeuristic.formClustersV2(g, numOfClusters, serviceChainTN.get(scID)));
	    		    		}
	    		    	}
	    		    }
				    //print clusters
	    		    for(int scID : scUsed){
	    		    	System.out.println("Clusters for service chain ID = " + scID);
	    		    	ClusterHeuristic.printClusters(clusterGroups.get(scID));
	    		    }
				    
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
					  	for(ClusterTrafficPairs ctpr : clusterGroups.get(scID)){
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
					    	for(ClusterTrafficPairs ctpr : clusterGroups.get(scID)){
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
				
				    /*System.out.println("##### Printing out values for scCopyUsed ######");
				    for(int scId : scUsed){
				    	for(int scCopyID : scCopies.get(scId)){
				    		System.out.println("ScId = " + scCopyID);
				    	}
				    }*/
				    
				    
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
			  	  	
			  	  	//maximum loaded link value
			  	  	double maxLoadedLinkValue = 0.0;
			  	  	NodePair link = null;
			  	  	for(Map.Entry<NodePair,Double> entry : linkCapacity.entrySet()){
			  	  		if(maxLoadedLinkValue < entry.getValue()){
			  	  			maxLoadedLinkValue = entry.getValue();
			  	  			link = entry.getKey();
			  	  		}
			  	  	}
			  	  	
			  	  	//core values corresponding to nodes used
			  	  	ArrayList<Double> coreValues = new ArrayList<Double>();
			  	  	for(int vrtID : CG.vnfPlacementNodes){
			  	  		for(Map.Entry<BaseVertex,Double> entry : cpuCoreCount.entrySet()){
			  	  			if(entry.getKey().get_id() == vrtID){
			  	  				coreValues.add(entry.getValue());
			  	  			}
			  	  		}
			  	  	}
				
			  	  	//write to a file
			  	  	bw.write(numOfClusters + "\t" + CG.rmpIlpValue + "\t" + preProcFunctions.totalBwUsedOnShortestPaths(sdpaths, copyOfPairList) + "\t" + 
			  	  			CG.vnfPlacementNodes.size() + "\t" + CG.vnfPlacementNodes.toString() + "\t" + coreValues.toString() + "\t" +
			  	  			"(" + link.v1.get_id() + "," + link.v2.get_id() + ")" + "\t" + maxLoadedLinkValue + "\t" +
			  	  			CG.eoptimality + "\t" + CG.cgRunTime + "\t" + CG.ilpRunTime + "\t" + CG.totalTime + "\n");
			  	  	
			  	  	//total bandwidth across the requests
			  	  	Double connTraffic = 0.0;
			  	  	for(TrafficNodes tn : pairList){
			  	  		connTraffic += tn.flow_traffic;
			  	  	}
			  	  	System.out.println("Total traffic across requests = " + connTraffic);
				  	
	    	    }
	    	    
			    //close the file writer
			    bw.close();
//		    }
 		}
			
	}

}
