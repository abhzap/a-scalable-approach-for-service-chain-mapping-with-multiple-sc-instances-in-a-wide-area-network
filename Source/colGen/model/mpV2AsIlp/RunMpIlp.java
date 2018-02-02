package colGen.model.mpV2AsIlp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import colGen.model.heuristic.ClusterHeuristic.ClusterTrafficPairs;
import colGen.model.preprocess.PreProcVer1;
import colGen.model.preprocess.placeNFVI;
import colGen.model.preprocess.preProcFunctions;
import colGen.model.ver1.VertexRank;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class RunMpIlp {
	
	//run the Integer Linear Program
	public static void runIlp() throws Exception{
		
		//generate the graph object
	    Graph g = PreProcVer1.makeGraphObject();
	    //set all nodes to type switches
	    preProcFunctions.makeAllVrtSwitches(g);
	    //populate ChainSet details
	    InputConstants.populateServices();
	    //print the graph object
	    preProcFunctions.printGraph(g);	    
	    
	    //NFV nodes 
		//All nodes are NFV capable
		int numOfNfvNodes = g._vertex_list.size();
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
		
		//generate the routes for the traffic pairs
	    HashMap<NodePair,List<Path>> sdpaths = preProcFunctions.findRoutesForSDpairs(g);  	  
	    
	    //get the set of service chains
	  	Map<Integer,ServiceChain> ChainSet = PreProcVer1.populateChainSetBasedOnScenario1();
		// print out the Set of Service Chains
		preProcFunctions.printServiceChains(ChainSet);
		
		//total list of VNF available
		List<FuncPt> vnf_list = PreProcVer1.totalListOfVNFs();		
				
		// SD pairs between which we desire traffic to be
		// Store each s-d pair
	  /*  List<TrafficNodes> pair_list = PreProcVer1.setOfSDpairs(sdpaths);
		for(TrafficNodes tn : pair_list){ tn.updateTraffic(1000); }*/
		
		// SD pairs between which we desire traffic to be
		// Store each s-d pair
		List<TrafficNodes> pair_list = new ArrayList<TrafficNodes>();
		//generate all (s,d) pairs
		for(BaseVertex srcVrt: g.get_vertex_list()){
			for(BaseVertex destVrt: g.get_vertex_list()){
				if(srcVrt.get_id() != destVrt.get_id()){					
					pair_list.add(new TrafficNodes(srcVrt,destVrt,0,1000));					
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
		placeNFVI.placeNFVPoP(g, rankList, numOfNfvNodes);
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
		
		//valid configurations for each service chain //each (s,d) selects a valid configuration
		Map<TrafficNodes,ArrayList<HuerVarZ>> configsPerSD = new HashMap<TrafficNodes,ArrayList<HuerVarZ>>();
		//get the start time
		long configGenStartTime = new Date().getTime();	
		//configuration to be used
		Map<Integer,ArrayList<HuerVarZ>> configsPerSC = BaseHeuristic2.generateAllHueVarZConfigurations(scUsed, ChainSet, nodesNFVI, sdpaths, serviceChainTN, configsPerSD);
		//the total number of configurations generated
		for(int scID : scUsed){
			System.out.println("Total number of configurations generated: " + configsPerSC.get(scID).size());
			//print out the configuration no's
			/*for(HuerVarZ config : configsPerSC.get(scID)){
				System.out.println(config.cgConfig);
			}*/
		}
		//number of configurations for each (s,d) pair
		for(Map.Entry<TrafficNodes,ArrayList<HuerVarZ>> entry : configsPerSD.entrySet()){
			System.out.println("("+entry.getKey().v1.get_id()+","+entry.getKey().v2.get_id()+") : " + entry.getValue().size());
		}
		//ILP start time
		long ilpStartTime = new Date().getTime();			
		MasterProblemV2AsILP cplexObject = new MasterProblemV2AsILP(InputConstants.coreCount, func_list, CountMaxVNF, scUsed, serviceChainTN,
				configsPerSC, configsPerSD, g, nodesNFVI, nfv_nodes, ChainSet, pair_list, vertex_list_without_nfvi_nodes, replicaPerVNF, numOfNfvNodes);
		//export ILP model
		cplexObject.master_problem.exportModel("mpAsILP.lp");
		//solve ILP model
		cplexObject.master_problem.solve();	
		//ILP end time
		long ilpEndTime = new Date().getTime();
		//get the end time
		long configGenEndTime = new Date().getTime();
		//total time
		long totalTime = configGenEndTime - configGenStartTime;
		//ilp solve time
		long ilpSolveTime = ilpEndTime - ilpStartTime;
		//heuristic time
		long configGenTime = totalTime - ilpSolveTime; 
		//give solution to ILP model
		ReportILP.reportAllVariables(cplexObject.master_problem, cplexObject.usedVarZ, cplexObject.usedVarX, cplexObject.usedVarY, cplexObject.usedVarH);
		//print out the various solve times
	    System.out.println("Total time: " + totalTime + " ; ILP solve time: " + ilpSolveTime + " ; Config generation time: " + configGenTime);
		
	}

}
