package colGen.model.trafficGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import colGen.model.preprocess.PreProcVer1;
import colGen.model.preprocess.placeNFVI;
import colGen.model.preprocess.preProcFunctions;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class TestNodePairGeneration {
	public static void runTest() throws Exception{	
		//traffic matrix percentages 		
 		ArrayList<Double> trafMatPrcnt = new ArrayList<Double>();
 		trafMatPrcnt.add(0.05); trafMatPrcnt.add(0.1); trafMatPrcnt.add(0.15);trafMatPrcnt.add(0.2);	
 		trafMatPrcnt.add(0.25); trafMatPrcnt.add(0.50); trafMatPrcnt.add(0.75);trafMatPrcnt.add(1.0); 		
		
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
		
		//Traffic Node Pairs
		Map<Double,ArrayList<NodePair>> trafPairs = new TreeMap<Double,ArrayList<NodePair>>();
		//existing node pairs
		ArrayList<NodePair> pairListTotal = new ArrayList<NodePair>();
		Double prevPrcnt = 0.0;
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
		
		//print out the traffic nodes generated
		for(Map.Entry<Double, ArrayList<NodePair>> entry : trafPairs.entrySet()){
			System.out.println("Traffic Matrix Fullness Percentage: " + entry.getKey() + " ; Size: " + entry.getValue().size());
			for(NodePair np : entry.getValue()){
				System.out.println("\t" + "(" + np.v1.get_id() + "," + np.v2.get_id() + ")");
			}
		}
		
		
	}
}
