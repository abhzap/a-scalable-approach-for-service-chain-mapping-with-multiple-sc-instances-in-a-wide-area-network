package colGen.model.ver1.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.heuristic.BaseHeuristic2;
import colGen.model.heuristic.HuerVarZ;
import colGen.model.heuristic.BaseHeuristic2.SdDetails;
import colGen.model.preprocess.PreProcVer1;
import colGen.model.preprocess.placeNFVI;
import colGen.model.preprocess.preProcFunctions;
import colGen.model.trafficGen.TrafficGenerator;
import colGen.model.ver1.CG;
import colGen.model.ver1.VertexRank;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class Test2 {
	
	public static void runTest(boolean cluster) throws Exception{
		//generate the graph object
	    Graph g = PreProcVer1.makeGraphObject();	   
	    
	    
	    //populate ChainSet details
	    InputConstants.populateServices();
	    //print the graph object
	    preProcFunctions.printGraph(g);
	    
	    //calculate betweenness centrality
	    Map<BaseVertex,Double> bcOfVertex = preProcFunctions.calculateBetweenessCentrality(g);
	    
	    //calculate ranking of vertices based on 
	    //product of betweeness-centrality and degree centrality
	    Map<BaseVertex,Double> vertexRank = preProcFunctions.calProductOfBCandDeg(g,bcOfVertex);
	    //list of vertex ranks
	    List<VertexRank> rankList = new ArrayList<VertexRank>();
	    //make list
	    for(Map.Entry<BaseVertex, Double> entry : vertexRank.entrySet()){
	    	VertexRank obj = new VertexRank(entry.getKey(),entry.getValue());
	    	rankList.add(obj);
	    }	  
	    //sort list in descending order
	    Collections.sort(rankList);
	    //print out the vertex Ranking
	    preProcFunctions.printVertexRanking(rankList);
	    
	    //generate the routes for the traffic pairs
	    HashMap<NodePair,List<Path>> sdpaths = preProcFunctions.findRoutesForSDpairs(g);
	    
	    //get the set of service chains
		Map<Integer,ServiceChain> ChainSet = PreProcVer1.setOfServiceChains();
		// print out the Set of Service Chains
		preProcFunctions.printServiceChains(ChainSet);
			
			
		// SD pairs between which we desire traffic to be
		// Store each s-d pair
	    List<TrafficNodes> pair_list = PreProcVer1.setOfSDpairs(sdpaths);
		for(TrafficNodes tn : pair_list){ tn.updateTraffic(1000); }
		//Using Traffic Generator for generating sd pairs
		//List<TrafficNodes> pair_list = TrafficGenerator.getTrafficPairs(InputConstants.trafficLoad, 1, g);
		// Generate traffic pairs for a single service chain
		//"web","voip","videostream","cloudgame"	
		//List<TrafficNodes> pair_list = TrafficGenerator.generateDistinctPairsAndAllocateTraffic(InputConstants.trafficLoad,10,"web",1,g._vertex_list);
	
		//List of the service chains to be deployed
		List<Integer> scUsed = preProcFunctions.serviceChainsUsed(pair_list);		
		//print out the pair lists
		preProcFunctions.printSDpairs(pair_list, scUsed);		
		 
	    
		//total list of VNF available
		List<FuncPt> vnf_list = PreProcVer1.totalListOfVNFs();
		//VNFs used across the service chains deployed
		List<FuncPt> func_list = preProcFunctions.listOfVNFsUsed(vnf_list, ChainSet, scUsed);		
		//print out the function list
		preProcFunctions.printListOfVNFsUsed(func_list);
		
		//traffic pairs for each service chain deployed	
		Map<Integer,ArrayList<TrafficNodes>> serviceChainTN = preProcFunctions.sdPairsforServiceChain(scUsed, pair_list);
	    //print out the traffic nodes available for each service chain
	    preProcFunctions.printTrafficNodesForServiceChains(scUsed, serviceChainTN);	
	    
	    //max number of VNFs
	    Map<Integer, ArrayList<Integer>> funcInSC = preProcFunctions.vnfInSCs(scUsed, func_list, ChainSet);
	    Map<Integer,Integer> CountMaxVNF = preProcFunctions.countMaxVnfBasedOnSdPairs(ChainSet, funcInSC, serviceChainTN);
	    
	    //replica constraint per VNF
	    Map<Integer,Integer> replicaPerVNF = new HashMap<Integer,Integer>(CountMaxVNF);
		
	    //DC node placement		  
		ArrayList<Integer> dcNodes = new ArrayList<Integer>();  
		  
		//place the DC nodes
		placeNFVI.placeDC(g, dcNodes);
		//place the NFV nodes
		placeNFVI.placeNFVPoP(g, dcNodes);
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
		
		//print out the adjacency list of the graph
		for(BaseVertex vrt : g.get_vertex_list()){
		   	System.out.println("Vertex: " + vrt.get_id() + " ; Adj List: " + g.get_adjacent_vertices(vrt));
		}	
	    
	  //split a single service chain into multiple service chains
  	  Map<Integer,ArrayList<Integer>> scCopies = new HashMap<Integer,ArrayList<Integer>>();		
  	  //new service to old service
  	  Map<Integer,Integer> scCopyToSC = new HashMap<Integer,Integer>();
  	  //create list of service chains
  	  ArrayList<Integer> scCopyUsed = new ArrayList<Integer>();
  	  
  	  //valid configurations for each service chain //each (s,d) selects a valid configuration
	  Map<Integer,ArrayList<HuerVarZ>> configsPerSC = new HashMap<Integer,ArrayList<HuerVarZ>>();
	  Map<TrafficNodes,SdDetails> configPerSD = new HashMap<TrafficNodes,SdDetails>();
  	
  	  //if clustering happens
  	  if(cluster){
		  //cluster based on adjacency
		  BaseHeuristic2.clusteringBasedOnAdj(g, serviceChainTN, scUsed, scCopies, scCopyToSC, scCopyUsed);	    
		  //calculate traffic nodes per service chain once again
		  //traffic pairs for each service chain deployed	
		  serviceChainTN = preProcFunctions.sdPairsforServiceChain(scCopyUsed, pair_list);
		  //print out the traffic nodes available for each service chain
		  preProcFunctions.printTrafficNodesForServiceChains(scCopyUsed, serviceChainTN);
		  //get configuration per SC
		  configsPerSC = BaseHeuristic2.singleConfigBasedOnAdj(scUsed, ChainSet, nodesNFVI, scCopyUsed, scCopyToSC, sdpaths, serviceChainTN, scCopies, configPerSD);
  		 
  	  }else{  		  
  		  //no scID update
		  BaseHeuristic2.noScIdUpdate(scUsed, scCopyUsed, configsPerSC, scCopies, scCopyToSC); 
		  //get configuration per SC
		  configsPerSC = BaseHeuristic2.singleConfigPerSC(sdpaths, scUsed, ChainSet, nfv_nodes, serviceChainTN, configPerSD);
  	  }
  	  
  	  //print the configurations for each SC
  	  preProcFunctions.printConfigsPerSCforBH2(scCopyUsed, configsPerSC);					  
  	  //print the configuration for each (s,d)
  	  preProcFunctions.printConfigForSD(configPerSD);  	  
  	
  	 
  		
	  //calculate the core and link constraints
	  boolean coreCstr = false;
	  boolean capCstr = false;
	  Map<BaseVertex,Double> cpuCoreCount = new HashMap<BaseVertex,Double>();
	  Map<NodePair,Double> linkCapacity = new HashMap<NodePair,Double>();
	  CG.runCG(coreCstr,capCstr,cpuCoreCount,linkCapacity,g, ChainSet, pair_list, scUsed, vnf_list, func_list, serviceChainTN, nfv_nodes, 
			  nodesNFVI, vertex_list_without_nfvi_nodes, scCopies, scCopyToSC, configsPerSC, configPerSD, CountMaxVNF, replicaPerVNF, nfv_nodes.size());	 
	    
	}

}
