package colGen.model.nfinstances;

import java.util.ArrayList;
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
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class Instance {
	
	public static void runInstances() throws Exception{
		
		//run Internet2 instances //run Atlanta instances //run 
		String [] networkTopology = new String[3];		
		String [] trafficDemands = new String[3];		
		//populate the arrays
		networkTopology[0]=InstanceConstants.internet2Topology;
		networkTopology[1]=InstanceConstants.atlantaTopology;
		networkTopology[2]=InstanceConstants.germanyTopology;		
		trafficDemands[0]=InstanceConstants.internet2Demands;
		trafficDemands[1]=InstanceConstants.atlantaDemands;
		trafficDemands[2]=InstanceConstants.germanyDemands;
		
		//get the set of service chains
 		Map<Integer,ServiceChain> ChainSet = new HashMap<Integer,ServiceChain>();
 		ArrayList<Integer> chainSeq0 = new ArrayList<Integer>();chainSeq0.add(0);chainSeq0.add(1);chainSeq0.add(5);chainSeq0.add(3);chainSeq0.add(4);
 		ChainSet.put(0, new ServiceChain(0,chainSeq0));
 		String sc0="01534";
 		ArrayList<Integer> chainSeq1 = new ArrayList<Integer>();chainSeq1.add(0);chainSeq1.add(1);chainSeq1.add(2);chainSeq1.add(3);chainSeq1.add(4);
 		ChainSet.put(1, new ServiceChain(1,chainSeq1));
 		String sc1="01234";
 		ArrayList<Integer> chainSeq2 = new ArrayList<Integer>();chainSeq2.add(0);chainSeq2.add(1);chainSeq2.add(2);chainSeq2.add(1);chainSeq2.add(0);
 		ChainSet.put(2, new ServiceChain(2,chainSeq2));
 		String sc2="01210";
 		ArrayList<Integer> chainSeq3 = new ArrayList<Integer>();chainSeq3.add(0);chainSeq3.add(1);chainSeq3.add(2);chainSeq3.add(5);chainSeq3.add(4);
 		ChainSet.put(3, new ServiceChain(3,chainSeq3));
 		String sc3="01254";
 		//print out the Set of Service Chains
 	 	preProcFunctions.printServiceChains(ChainSet);
		
 	 	//internet2 //atlanta15 //germany50
 	 	int instNum = 2;//0,1,2
 	 	
//		for(int instNum=0; instNum < networkTopology.length; instNum++){		
			//generate the graph object
		    Graph g = InstancePreProc.makeGraphObject(networkTopology[instNum]);	  
		    //print the graph object
		    preProcFunctions.printGraph(g);
		    
		    //generate the routes for the traffic pairs
		    HashMap<NodePair, List<Path>> sdpaths = preProcFunctions.findRoutesForSDpairs(g);
		    System.out.println("K-shortest paths have been generated!");
		    //read the traffic pairs
		    List<TrafficNodes> pair_list = InstancePreProc.setOfSDpairs(sc0,sc1,sc2,sc3,trafficDemands[instNum], sdpaths);
		    //List of the service chains to be deployed
	 		List<Integer> scUsed = preProcFunctions.serviceChainsUsed(pair_list);
			//print out the pair lists
			preProcFunctions.printSDpairs(pair_list, scUsed);
			
			//total list of VNF available
			List<FuncPt> vnf_list = new ArrayList<FuncPt>();
			vnf_list.add(new FuncPt(0,1));
			vnf_list.add(new FuncPt(1,1));
			vnf_list.add(new FuncPt(2,1));
			vnf_list.add(new FuncPt(3,0.1));
			vnf_list.add(new FuncPt(4,0.01));
			vnf_list.add(new FuncPt(5,0.01));
			//VNFs used across the service chains deployed
			List<FuncPt> func_list = preProcFunctions.listOfVNFsUsed(vnf_list, ChainSet, scUsed);		
			//print out the function list
			preProcFunctions.printListOfVNFsUsed(func_list);
			
			//traffic pairs for each service chain deployed	
			Map<Integer, ArrayList<TrafficNodes>> serviceChainTN = preProcFunctions.sdPairsforServiceChain(scUsed, pair_list);
		    //print out the traffic nodes available for each service chain
		    preProcFunctions.printTrafficNodesForServiceChains(scUsed, serviceChainTN);	
			
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
			
			//print out sd paths
			for(Map.Entry<NodePair, List<Path>> entrySD : sdpaths.entrySet()){
				System.out.println("("+entrySD.getKey().v1.get_id()+","+entrySD.getKey().v2.get_id()+")");
				Path p = entrySD.getValue().get(0);
				System.out.print("\tPath: ");
				for(BaseVertex vrtPath : p.get_vertices()){
					System.out.print(vrtPath.get_id()+"->");
				}
				System.out.println();
			}
			  
		    //valid configurations for each service chain //each (s,d) selects a valid configuration
		    Map<Integer,ArrayList<HuerVarZ>> configsPerSC = new HashMap<Integer,ArrayList<HuerVarZ>>();
		    Map<TrafficNodes,SdDetails> configPerSD = new HashMap<TrafficNodes,SdDetails>();		  
		    //get the configurations for each Traffic Node
//		    configsPerSC = BaseHeuristic2.clusteringBasedOnPath(InputConstants.allPossiblePlcmnts, InputConstants.placementCount, sdpaths, scUsed, ChainSet, nfv_nodes, serviceChainTN, configPerSD);
		    configsPerSC = BaseHeuristic2.singleConfigPerSC(sdpaths, scUsed, ChainSet, nfv_nodes, serviceChainTN, configPerSD);
		    //print the configurations for each SC
		    preProcFunctions.printConfigsPerSCforBH2(scUsed, configsPerSC);
		    //print the configuration for each (s,d)
		    preProcFunctions.printConfigForSD(configPerSD);						
			  
			  //split a single service chain into multiple service chains
			  Map<Integer,ArrayList<Integer>> scCopies = new HashMap<Integer,ArrayList<Integer>>();		
			  //new service to old service
			  Map<Integer,Integer> scCopyToSC = new HashMap<Integer,Integer>();
			  //create list of service chains
			  ArrayList<Integer> scCopyUsed = new ArrayList<Integer>();
			  
			  //service chain ID based on clustering
			  //serviceChainTN = BaseHeuristic2.scIdBasedOnClustering(scUsed, configsPerSC, scCopyUsed, scCopyToSC, scCopies, pair_list);
			 
			  //ICC paper model //no scID update
			  BaseHeuristic2.noScIdUpdate(scUsed, scCopyUsed, configsPerSC, scCopies, scCopyToSC);
			  
			  //for(TrafficNodes tn : pair_list){ tn.updateTraffic(1000); }
			  PreProcVer1.populateCoresLinks(configsPerSC, ChainSet, func_list);
			  
			  //calculate the core and link constraints
			  boolean coreCstr = false;
			  boolean capCstr = false;
			  Map<BaseVertex,Double> cpuCoreCount = new HashMap<BaseVertex,Double>();
			  Map<NodePair,Double> linkCapacity = new HashMap<NodePair,Double>();
			 /* CG.runCG(coreCstr,capCstr,cpuCoreCount,linkCapacity,g, ChainSet, pair_list, scUsed, vnf_list, func_list, serviceChainTN, nfv_nodes, 
					  nodesNFVI, vertex_list_without_nfvi_nodes, scCopies, scCopyToSC, configsPerSC, configPerSD);*/
			  //calculate the new core counts and link capacity constraints
//			  for(Map.Entry<BaseVertex,Double> entry : cpuCoreCount.entrySet()){
//				  cpuCoreCount.put(entry.getKey(), 2.0*entry.getValue());
//			  }
			  //run with core and capacity constraints enforced
			  coreCstr = true;
			  capCstr = true;
			  /*CG.runCG(coreCstr,capCstr,cpuCoreCount,linkCapacity,g, ChainSet, pair_list, scUsed, vnf_list, func_list, serviceChainTN, nfv_nodes, 
					  nodesNFVI, vertex_list_without_nfvi_nodes, scCopies, scCopyToSC, configsPerSC, configPerSD);*/
//		}
	}

}
