package colGen.model.ver2.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import colGen.model.result.Solution;
import colGen.model.ver1.CG;
import colGen.model.ver2.CG2;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class CgCompareWithILP {

	//run the CG model
	public static void runTest() throws Exception{
		
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
	  	
	  	//generate the routes for the traffic pairs
	    HashMap<NodePair,List<Path>> sdpaths = preProcFunctions.findRoutesForSDpairs(g);
	  	
	    //get the set of service chains
	  	Map<Integer,ServiceChain> ChainSet = PreProcVer1.populateChainSetBasedOnScenario1();
		// print out the Set of Service Chains
		preProcFunctions.printServiceChains(ChainSet);
	    
	    //total list of VNF available
	  	List<FuncPt> vnf_list = PreProcVer1.totalListOfVNFs();
	  	
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
		
		Map<Integer,ArrayList<Integer>> scCopies = new HashMap<Integer,ArrayList<Integer>>();		
	  	//new service to old service
	  	Map<Integer,Integer> scCopyToSC = new HashMap<Integer,Integer>();
	  	//create list of service chains
	  	ArrayList<Integer> scCopyUsed = new ArrayList<Integer>();
	  	  
	  	//valid configurations for each service chain //each (s,d) selects a valid configuration
		Map<Integer,ArrayList<HuerVarZ>> configsPerSC = new HashMap<Integer,ArrayList<HuerVarZ>>();
		Map<TrafficNodes,SdDetails> configPerSD = new HashMap<TrafficNodes,SdDetails>();
		
		//no scID update
		BaseHeuristic2.noScIdUpdate(scUsed, scCopyUsed, configsPerSC, scCopies, scCopyToSC); 
		//get configuration per SC
		configsPerSC = BaseHeuristic2.singleConfigPerSC(sdpaths, scUsed, ChainSet, nfv_nodes, serviceChainTN, configPerSD);
		
		//print the configurations for each SC
	  	preProcFunctions.printConfigsPerSCforBH2(scUsed, configsPerSC);					  
	  	//print the configuration for each (s,d)
	  	preProcFunctions.printConfigForSD(configPerSD);
		
		//calculate the core and link constraints
		boolean coreCstr = false;
		boolean capCstr = false;		
		Map<BaseVertex,Double> cpuCoreCount = new HashMap<BaseVertex,Double>();
		Map<NodePair,Double> linkCapacity = new HashMap<NodePair,Double>();
		//keep track of the best solution
		Solution bestSol = new Solution();
		//cold start CG
		/*CG2.runCG(coreCstr, capCstr, cpuCoreCount, linkCapacity, g, ChainSet, pair_list, scUsed, vnf_list, func_list, serviceChainTN, nfv_nodes, 
				  nodesNFVI, vertex_list_without_nfvi_nodes, scCopies, scCopyToSC, configsPerSC, configPerSD, CountMaxVNF, replicaPerVNF, numOfNfvNodes);*/
		//procedure start time
		long procStartTime = new Date().getTime();
		//warm start CG
		Set<HuerVarZ> prevCols = new HashSet<HuerVarZ>();		
		//iterate once
		for(int repeat=0; repeat<1; repeat++){	
			cpuCoreCount = new HashMap<BaseVertex,Double>();
			linkCapacity = new HashMap<NodePair,Double>();
			CG2.runCGwithPrevCols(coreCstr, capCstr, cpuCoreCount, linkCapacity, g, ChainSet, pair_list, scUsed, vnf_list, func_list, serviceChainTN, nfv_nodes,
					nodesNFVI, vertex_list_without_nfvi_nodes, scCopies, scCopyToSC, configsPerSC, configPerSD, CountMaxVNF, replicaPerVNF, numOfNfvNodes, prevCols, bestSol);
			System.out.println("Repetition number: " + repeat);
			System.out.println("e-optimality value: " + CG2.eoptimality);			
			if (Math.round(CG2.eoptimality)==0.0){
				break;
			}
		}
		//procedure end time
		long procEndTime = new Date().getTime();
		//procedure time
		long procTime = procEndTime - procStartTime;
		System.out.println("##### Total Procedure Time = " + procTime + " #####");
		//print out solution
		bestSol.printSolution();
		
		//create file
	  	String filename = "CG_Motivation_" + InputConstants.NETWORK_FILE_NAME.substring(0, InputConstants.NETWORK_FILE_NAME.lastIndexOf(".")) + ".txt";
	    BufferedWriter bw = new BufferedWriter(new FileWriter(filename));  
		//write to a file
		bw.write(CG2.rmpIlpValue + "\t" + CG2.vnfPlacementNodes.size() + "\t" + CG2.vnfPlacementNodes.toString() + "\t" + CG2.eoptimality + "\t" + 
				CG2.cgRunTime + "\t" + CG2.ilpRunTime + "\t" + CG2.totalTime + "\t" + procTime + "\n");		
		//close the file writer
	    bw.close();
			
	}
}
