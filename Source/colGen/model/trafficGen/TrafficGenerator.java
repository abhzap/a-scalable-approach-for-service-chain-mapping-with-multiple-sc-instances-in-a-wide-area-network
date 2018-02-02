package colGen.model.trafficGen;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import Given.InputConstants;
import Given.InputConstants.ServiceDetails;
import ILP.NodePair;
import ILP.TrafficNodes;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class TrafficGenerator {
	
	//generate the number of node pairs corresponding to traffic matrix fullness
	public static ArrayList<NodePair> generateNodePairs(ArrayList<NodePair> pairList, int reqNumOfNodePairs, List<BaseVertex> vertexList){
		HashSet<NodePair> selectedNodePairs = new HashSet<NodePair>();
		//create total number of pairs
		ArrayList<NodePair> totalPairs = new ArrayList<NodePair>();
		for(BaseVertex srcVrt : vertexList){
			for(BaseVertex destVrt : vertexList){
				if(srcVrt.get_id() != destVrt.get_id()){
					totalPairs.add(new NodePair(srcVrt,destVrt));
				}
			}
		}
		//remove the already existing node pairs
		for(NodePair np : pairList){
			totalPairs.remove(np);
		}
		//selected the required number of NodePairs
		while(selectedNodePairs.size() < reqNumOfNodePairs){
			Random rand = new Random(System.currentTimeMillis());
			NodePair np = totalPairs.get(rand.nextInt(totalPairs.size()));
			//add node pair to hashset
			selectedNodePairs.add(np);
		}		
		//create a list and return it 
		ArrayList<NodePair> listOfPairs = new ArrayList<NodePair>();
		for(NodePair pr : selectedNodePairs){
			listOfPairs.add(pr);
		}
		return listOfPairs;
	}
	
	//generate the number of nodePairs corresponding to traffic matrix fullness
	public static ArrayList<NodePair> generateNodePairs(double trafficMatrixPercentage, int totalTrafficPairs, List<BaseVertex> vertexList){
		//System.out.println("generateNodePairs() function is executed!");
		HashSet<NodePair> selectedNodePairs = new HashSet<NodePair>();
		int reqNumOfNodePairs = (int) Math.ceil(trafficMatrixPercentage*totalTrafficPairs);	
		int nodePairCount = 0;
		while(nodePairCount < reqNumOfNodePairs){
			Random rand = new Random(System.currentTimeMillis() + nodePairCount);
			BaseVertex srcV = vertexList.get(rand.nextInt(vertexList.size()));
			BaseVertex dstV = vertexList.get(rand.nextInt(vertexList.size()));
			//make sure source and destination are distinct
			while(srcV.get_id() == dstV.get_id()){
				srcV = vertexList.get(rand.nextInt(vertexList.size()));
				dstV = vertexList.get(rand.nextInt(vertexList.size()));
			}
			//add to the hash set
			selectedNodePairs.add(new NodePair(srcV,dstV));
			//increment pair count
			nodePairCount = selectedNodePairs.size();
		}
		//list of node pairs
		/*System.out.println("### Print Set ###");
 		for(NodePair pr : selectedNodePairs){
 			System.out.println("(" + pr.v1.get_id() + "," + pr.v2.get_id() + ")");
 		}*/
		//create a list and return it 
		ArrayList<NodePair> listOfPairs = new ArrayList<NodePair>();
		for(NodePair pr : selectedNodePairs){
			listOfPairs.add(pr);
		}
		return listOfPairs;
	}
	
	//allocate requests to node pairs //uniformly distributed
	public static void allocateRequestsToNodePairs(ArrayList<TrafficNodes> pairList, ArrayList<NodePair> selectedNodePairs, double trafficLoad, 
			double trafficPercentage, double trafficPerConnection, int[] scIDList){
		double trafficWS = trafficPercentage*trafficLoad;
		System.out.println("\tTraffic Load = " + trafficWS);
		double reqWSperSC = trafficWS/(trafficPerConnection*scIDList.length);
		System.out.println("\tNumber of requests per SC = " + reqWSperSC);
		//HashMap for storing the traffic Nodes
		HashMap<TrafficNodes, Integer> tempMap = new HashMap<TrafficNodes, Integer>();
		Random rand = new Random();
		//iterate through the service chains
		for(int scNum=0; scNum < scIDList.length; scNum++){
			int scID = scIDList[scNum];				
			for(int reqNum=0; reqNum < reqWSperSC; reqNum++){
				//System.currentTimeMillis();System.nanoTime()				
				NodePair np = selectedNodePairs.get(rand.nextInt(selectedNodePairs.size()));			
				//make the traffic node
				TrafficNodes tempNode = new TrafficNodes(np.v1,np.v2,scID);
				if(tempMap.get(tempNode) != null){
					int val = tempMap.get(tempNode) + 1;
					tempMap.put(tempNode, val);					
				}else{
					tempMap.put(tempNode, 1);	
				}
			}
		}
		//iterate through the map
		for(Map.Entry<TrafficNodes, Integer> entryTN : tempMap.entrySet()){
			double traffic = entryTN.getValue()*trafficPerConnection;
			entryTN.getKey().flow_traffic = traffic;				
			//add the key to pairList
			pairList.add(entryTN.getKey());				
		}
		/*for(TrafficNodes tn : pairList){
			System.out.println("(" + tn.v1.get_id() + "," + tn.v2.get_id() + ") ; " + tn.chain_index + " ; " + tn.flow_traffic);
		}*/
	}
	
	//allocate requests to node pairs //distributed in a skewed fashion
	public static void allocateRequestsToNodePairsSkewed(ArrayList<TrafficNodes> pairList, ArrayList<NodePair> selectedNodePairs, double trafficLoad, 
			double trafficPercentage, double trafficPerConnection, int[] scIDList){
		double trafficWS = trafficPercentage*trafficLoad;
		System.out.println("\tTraffic Load = " + trafficWS);
		int reqWSperSC = (int) Math.ceil(trafficWS/(trafficPerConnection*scIDList.length)) ;
		System.out.println("\tNumber of requests per SC = " + reqWSperSC);
		//HashMap for storing the traffic Nodes
		HashMap<TrafficNodes, Integer> tempMap = new HashMap<TrafficNodes, Integer>();
		Random rand = new Random();
		//iterate through the service chains
		for(int scNum=0; scNum < scIDList.length; scNum++){
			int scID = scIDList[scNum];	
			//allocate requests in a skewed fashion
			while(reqWSperSC > 0){
				//System.out.println("\t\t reqWSperSC = " + reqWSperSC);
				//System.currentTimeMillis();System.nanoTime()
				//select number of requests
				int reqsAllocated = rand.nextInt(reqWSperSC);
				if(reqsAllocated == 0){
					break;
				}
				//System.out.println("\t\t reqsAllocated = " + reqsAllocated);
				//select node pair
				NodePair np = selectedNodePairs.get(rand.nextInt(selectedNodePairs.size()));			
				//make the traffic node
				TrafficNodes tempNode = new TrafficNodes(np.v1,np.v2,scID);
				if(tempMap.get(tempNode) != null){
					int val = tempMap.get(tempNode) + reqsAllocated;
					tempMap.put(tempNode, val);					
				}else{
					tempMap.put(tempNode, reqsAllocated);	
				}
				//reduce the number of requests allocated
				reqWSperSC = reqWSperSC - reqsAllocated ;
			}
		}
		//iterate through the map
		for(Map.Entry<TrafficNodes, Integer> entryTN : tempMap.entrySet()){
			double traffic = entryTN.getValue()*trafficPerConnection;
			entryTN.getKey().flow_traffic = traffic;				
			//add the key to pairList
			pairList.add(entryTN.getKey());				
		}
		/*for(TrafficNodes tn : pairList){
			System.out.println("(" + tn.v1.get_id() + "," + tn.v2.get_id() + ") ; " + tn.chain_index + " ; " + tn.flow_traffic);
		}*/
	}
	
	
	//create traffic flows //uniformly distributed
	public static void generateTrafficFlows(ArrayList<TrafficNodes> pairList,double trafficMatrixPercentage, int totalNumberOfPairs, int scPerService, double trafficLoad, Graph g){
		//generate selected node pairs
		ArrayList<NodePair> selectedNodePairs = generateNodePairs(trafficMatrixPercentage, totalNumberOfPairs, g._vertex_list);	
		//list of node pairs
		/*System.out.println("### Print List ###");
 		for(NodePair pr : selectedNodePairs){
 			System.out.println("(" + pr.v1.get_id() + "," + pr.v2.get_id() + ")");
 		}*/
		//generate the traffic flows for each type of service
		for(Map.Entry<String,ServiceDetails> entry: InputConstants.services.entrySet()){
			//find the traffic pairs for the service desired
			System.out.println("Generating sd pairs for " + entry.getKey() + " !");
			ServiceDetails detServ = entry.getValue();
			int scIDs[] = new int[scPerService];
			for(int i=0; i<scPerService; i++){
				scIDs[i] = entry.getValue().setOfIDs.get(i);
			}
			allocateRequestsToNodePairs(pairList, selectedNodePairs, trafficLoad, detServ.totalTrafficPercentage, detServ.connectionBandwidth, scIDs);
		}		
	}
	
	//create traffic flows //Distributed in a skewed fashion
	public static void generateTrafficFlowsSkewed(ArrayList<TrafficNodes> pairList,double trafficMatrixPercentage, int totalNumberOfPairs, int scPerService, double trafficLoad, Graph g){
		//System.out.println("Generate traffic flow function is executed!");
		//generate selected node pairs
		ArrayList<NodePair> selectedNodePairs = generateNodePairs(trafficMatrixPercentage, totalNumberOfPairs, g._vertex_list);	
		//list of node pairs
		System.out.println("### Print List ###");
 		for(NodePair pr : selectedNodePairs){
 			System.out.println("(" + pr.v1.get_id() + "," + pr.v2.get_id() + ")");
 		}
		//generate the traffic flows for each type of service
		for(Map.Entry<String,ServiceDetails> entry: InputConstants.services.entrySet()){
			//find the traffic pairs for the service desired
			System.out.println("Generating sd pairs for " + entry.getKey() + " !");
			ServiceDetails detServ = entry.getValue();
			int scIDs[] = new int[scPerService];
			for(int i=0; i<scPerService; i++){
				scIDs[i] = entry.getValue().setOfIDs.get(i);
			}
			allocateRequestsToNodePairsSkewed(pairList, selectedNodePairs, trafficLoad, detServ.totalTrafficPercentage, detServ.connectionBandwidth, scIDs);
		}		
	}
	
	
	
	//#####OLD#######
	//generate requests and add to traffic pairs
	public static void generatePairsAndAllocateRequests(List<TrafficNodes> pairList, List<BaseVertex> vertexList, double trafficLoad, 
			double trafficPercentage, double trafficPerConnection, int[] scIDList){		
		double trafficWS = trafficPercentage*trafficLoad;
		System.out.println("\tTraffic Load = " + trafficWS);
		double reqWSperSC = trafficWS/(trafficPerConnection*scIDList.length);
		System.out.println("\tNumber of requests per SC = " + reqWSperSC);
		//HashMap for storing the traffic Nodes
		HashMap<TrafficNodes, Integer> tempMap = new HashMap<TrafficNodes, Integer>();
		//iterate through the service chains
		for(int scNum=0; scNum < scIDList.length; scNum++){
			int scID = scIDList[scNum];
			//System.out.println("\t\tSD pairs for Service Chain : " + scID);
			for(int reqNum=0; reqNum < reqWSperSC; reqNum++){
				Random rand = new Random(System.currentTimeMillis() + scID + reqNum);
				BaseVertex src = vertexList.get(rand.nextInt(vertexList.size()));
				BaseVertex dst = vertexList.get(rand.nextInt(vertexList.size()));
				//make sure source and destination are distinct
				while(src.get_id() == dst.get_id()){
					src = vertexList.get(rand.nextInt(vertexList.size()));
					dst = vertexList.get(rand.nextInt(vertexList.size()));
				}
				//make the traffic node
				TrafficNodes tempNode = new TrafficNodes(src,dst,scID);
				//print out the traffic node
				//System.out.println("\t\t(" + tempNode.v1.get_id() + "," + tempNode.v2.get_id() + ")");
				//add the traffic node to the HashMap
				if(tempMap.get(tempNode) != null){
					int val = tempMap.get(tempNode) + 1;
					tempMap.put(tempNode, val);					
				}else{
					tempMap.put(tempNode, 1);	
				}
			}
			System.out.println();
			//print out the contents of tempMap
			System.out.println("Size of tempMap : " + tempMap.size());
			System.out.println(" Service Chain ; (s,d) pair ; no. of occurrences");
			//count total number of requests generated
			int countTotalReqs = 0;
			for(Map.Entry<TrafficNodes, Integer> entryM : tempMap.entrySet()){
				System.out.println("\t" + entryM.getKey().chain_index + " ; (" + entryM.getKey().v1.get_id() + "," + entryM.getKey().v2.get_id() + ") ; " + entryM.getValue());
				countTotalReqs += entryM.getValue();
			}
			System.out.println("Total no. of requests generated = " +  countTotalReqs);
			System.out.println("\tTotal no. of requests required = " + reqWSperSC);
			System.out.println();						
		}
		//iterate through the map
		for(Map.Entry<TrafficNodes, Integer> entryTN : tempMap.entrySet()){
			double traffic = entryTN.getValue()*trafficPerConnection;
			entryTN.getKey().flow_traffic = traffic;				
			//add the key to pairList
			pairList.add(entryTN.getKey());				
		}
	}
	
	//#####OLD#######
	//generate the sd pairs based on traffic load
	public static List<TrafficNodes> getTrafficPairs(int scPerService, double trafficLoad, Graph g){		
		List<TrafficNodes> pairList = new ArrayList<TrafficNodes>();	
		List<BaseVertex> vertexList = g.get_vertex_list();		
		for(Map.Entry<String,ServiceDetails> entry: InputConstants.services.entrySet()){
			//find the traffic pairs for the service desired
			System.out.println("Generating sd pairs for " + entry.getKey() + " !");
			ServiceDetails detServ = entry.getValue();
			int scIDs[] = new int[scPerService];
			for(int i=0; i<scPerService; i++){
				scIDs[i] = entry.getValue().setOfIDs.get(i);
			}
			generatePairsAndAllocateRequests(pairList, vertexList, trafficLoad, detServ.totalTrafficPercentage, detServ.connectionBandwidth, scIDs);
		}	
		return pairList;
	}
	
	//#####OLD#######
	//generate traffic for only one service chain
	public static List<TrafficNodes> getTrafficPairsForSC(double trafficLoad, String service, Graph g){		
		List<TrafficNodes> pairList = new ArrayList<TrafficNodes>();	
		List<BaseVertex> vertexList = g.get_vertex_list();		
		//find the traffic pairs for Web Service (WS)
		System.out.println("Generating sd pairs for " + service  + " !");
		int singleSC[] = new int[]{InputConstants.services.get(service).setOfIDs.get(0)};		
		generatePairsAndAllocateRequests(pairList, vertexList, trafficLoad, InputConstants.services.get(service).totalTrafficPercentage, InputConstants.services.get(service).totalTrafficPercentage, singleSC);		
		return pairList;
	}

	//#####OLD#######
	//generate traffic pairs (s,d)
	public static List<TrafficNodes> generateDistinctPairsAndAllocateTraffic(double trafficLoad, int totalSdPairs, String service, int scPerService, List<BaseVertex> vertexList){
		List<TrafficNodes> pairList = new ArrayList<TrafficNodes>();
		//Generate traffic pairs for a service
		System.out.println("Generating sd pairs for " + service + " !");
		//get details of service
		ServiceDetails detServ = InputConstants.services.get(service);	
		//traffic value and number of requests
		System.out.println("Traffic Percentage = " + detServ.totalTrafficPercentage*100 + "%");
		double trafficForService = detServ.totalTrafficPercentage*trafficLoad;
		System.out.println("\tTraffic Load for " + service +  " = " + trafficForService);
		double reqPerSCForService = trafficForService/(detServ.connectionBandwidth*scPerService);
		System.out.println("\tNumber of requests per SC = " + reqPerSCForService);
		//iterate through the service chains
		for(int scNum=0; scNum < scPerService; scNum++){
			int scID = detServ.setOfIDs.get(scNum);
			List<TrafficNodes> tempList = new ArrayList<TrafficNodes>();
			//System.out.println("\t\tSD pairs for Service Chain : " + scID);
			for(int reqNum=0; reqNum < totalSdPairs; reqNum++){
				Random rand = new Random(System.currentTimeMillis() + scID + reqNum);
				BaseVertex src = vertexList.get(rand.nextInt(vertexList.size()));
				BaseVertex dst = vertexList.get(rand.nextInt(vertexList.size()));
				//make the traffic node
				TrafficNodes tempNode = new TrafficNodes(src,dst,scID);
				//make sure source and destination are distinct
				while((src.get_id() == dst.get_id()) || tempList.contains(tempNode)){
					src = vertexList.get(rand.nextInt(vertexList.size()));
					dst = vertexList.get(rand.nextInt(vertexList.size()));
					tempNode = new TrafficNodes(src,dst,scID);
				}
				//add the traffic node to the list of nodes
				tempList.add(tempNode);
			}
			for(int reqNum=0; reqNum < Math.floor(reqPerSCForService); reqNum++){
				Random rand = new Random(System.currentTimeMillis() + reqNum);
				TrafficNodes sd = tempList.get(rand.nextInt(tempList.size()));
				//update traffic in (s,d) pair
				sd.addTraffic(detServ.connectionBandwidth);
			}
			//add all traffic nodes to parent list
			pairList.addAll(tempList);
		}	
		return pairList;
	}
	
	//#####OLD#######
	//generate (s,d) pairs for all the given services
	public static List<TrafficNodes> generateDistinctPairsForAllSC(double trafficLoad, int totalSdPairs, int scPerService, List<BaseVertex> vertexList){
		List<TrafficNodes> pairList = new ArrayList<TrafficNodes>();
		//iterate through all the given services
		for(Map.Entry<String,ServiceDetails> entry: InputConstants.services.entrySet()){
			pairList.addAll(generateDistinctPairsAndAllocateTraffic(trafficLoad, totalSdPairs,entry.getKey(),scPerService,vertexList));	
		}
		return pairList;
	}
	
	
	
	//generate distinct (s,d) pairs for all service chains	
	/*public static void main(String args[]) throws Exception{
		 //generate the graph object
	    Graph g = preProcFunctions.makeGraphObject();	
	    //populate ChainSet details
	    InputConstants.populateServices();
	    //print the graph object
	    //preProcFunctions.printGraph(g);
	    
	    //generate the routes for the traffic pairs
	    HashMap<NodePair, List<Path>> sdpaths = preProcFunctions.findRoutesForSDpairs(g);
	    //total number of (s,d) pairs
	    int totalNumberOfSdPairs = sdpaths.keySet().size();
	    
 		//get the set of service chains
 		Map<Integer, ServiceChain> ChainSet = preProcFunctions.setOfServiceChains();
 		// print out the Set of Service Chains
 		preProcFunctions.printServiceChains(ChainSet);
 		
 		//list of traffic nodes
 		ArrayList<TrafficNodes> pairList = new ArrayList<TrafficNodes>();
 		generateTrafficFlows(pairList,0.25,totalNumberOfSdPairs,1,InputConstants.trafficLoad,g);
 		
 		//print out the traffic nodes
 		for(TrafficNodes tn : pairList){
 			System.out.println("(" + tn.v1.get_id() + "," +  tn.v2.get_id() + ") ; " + tn.chain_index + " ; " + tn.flow_traffic);
 		}*/
 		
 		/*//list of (s,d) pairs
 		HashSet<NodePair> nodePairList = generateNodePairs(0.75,totalNumberOfSdPairs,g._vertex_list);
 		System.out.println("Total no. of pairs: " + nodePairList.size());
 		
 		//list of node pairs
 		for(NodePair pr : nodePairList){
 			System.out.println("(" + pr.v1.get_id() + "," + pr.v2.get_id() + ")");
 		}*/
 		
 	    //Generate traffic pairs for all service chains	
 		//List<TrafficNodes> pair_list = TrafficGenerator.getTrafficPairs(InputConstants.trafficLoad, g);
 		//Generate traffic pairs for a single service chain
 		//List<TrafficNodes> pair_list =  TrafficGenerator.generateDistinctPairsAndAllocateTraffic(InputConstants.trafficLoad,40,"web",1,g._vertex_list);
 		//List of the service chains to be deployed
		//List<Integer> scUsed = preProcFunctions.serviceChainsUsed(pair_list);
		//print out the pair lists
		//preProcFunctions.printSDpairs(pair_list,scUsed);	   
		
		
		//traffic pairs for each service chain deployed	
		//Map<Integer, ArrayList<TrafficNodes>> serviceChainTN = preProcFunctions.sdPairsforServiceChain(scUsed, pair_list);
	    //print out the traffic nodes available for each service chain
	    //preProcFunctions.printTrafficNodesForServiceChains(scUsed, serviceChainTN);		
		//preProcFunctions.printSDpairsSCandTraffic(scUsed, serviceChainTN);
//	}

}
