package colGen.model.preprocess;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import FileOps.ReadFile;
import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.heuristic.HuerVarZ;
import colGen.model.heuristic.ClusterHeuristic.ClusterTrafficPairs;
import colGen.model.ver1.PpVarA;
import colGen.model.ver1.PpVarB;
import colGen.model.ver2.Pp2VarDelta;
import edu.asu.emit.qyan.alg.control.YenTopKShortestPathsAlg;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class PreProcVer1 {
	
	public static Graph makeGraphObject() throws Exception{		
		//build graph object from given network file
	    Class<?> cls = Class.forName("colGen.model.simulation.Sim");		
	    //returns the ClassLoader
	    ClassLoader cLoader = cls.getClassLoader();
	    //print out the class name
	    System.out.println("Class Name : " + cLoader.getClass());
	    //finds the resource with the given name
	    InputStream networkFileStream = cLoader.getResourceAsStream(InputConstants.FILE_READ_PATH + InputConstants.NETWORK_FILE_NAME);
	    //return the Graph Object
	    return new Graph(networkFileStream);
	}
	
	public static Graph makeGraphObject(String fileName) throws Exception{		
		//build graph object from given network file
	    Class<?> cls = Class.forName("colGen.model.simulation.Sim");		
	    //returns the ClassLoader
	    ClassLoader cLoader = cls.getClassLoader();
	    //print out the class name
	    System.out.println("Class Name : " + cLoader.getClass());
	    //finds the resource with the given name
	    InputStream networkFileStream = cLoader.getResourceAsStream(InputConstants.FILE_READ_PATH + fileName);
	    //return the Graph Object
	    return new Graph(networkFileStream);
	}
	
	public static Map<Integer, ServiceChain> setOfServiceChains() throws Exception{
		//build graph object from given network file
	    Class<?> cls = Class.forName("colGen.model.simulation.Sim");		
	    //returns the ClassLoader
	    ClassLoader cLoader = cls.getClassLoader();
		// read the Set of Service Chains
 	    InputStream chainSetStream = cLoader.getResourceAsStream(InputConstants.FILE_READ_PATH + InputConstants.CHAIN_SET);
 	    return ReadFile.readChainSet(chainSetStream);		
	}
	
	public static List<TrafficNodes> setOfSDpairs(HashMap<NodePair,List<Path>> sdpaths) throws Exception{
		//build graph object from given network file
	    Class<?> cls = Class.forName("colGen.model.simulation.Sim");		
	    //returns the ClassLoader
	    ClassLoader cLoader = cls.getClassLoader();
		InputStream sdPairStream = cLoader.getResourceAsStream(InputConstants.FILE_READ_PATH + InputConstants.SD_PAIRS);
		return ReadFile.readSDPairs(sdPairStream, sdpaths);
	}
	
	public static List<FuncPt> totalListOfVNFs() throws Exception{
		//build graph object from given network file
	    Class<?> cls = Class.forName("colGen.model.simulation.Sim");		
	    //returns the ClassLoader
	    ClassLoader cLoader = cls.getClassLoader();
		InputStream vnfListStream = cLoader.getResourceAsStream(InputConstants.FILE_READ_PATH	+ InputConstants.FUNCTION_DETAILS);
		return ReadFile.readFnPt(vnfListStream);
	}	
	
	public static void printSDpairsSCandTraffic(List<Integer> scUsed,  Map<Integer,ArrayList<Integer>> scCopies, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN){
		for(int scID : scUsed){
			for(int scCopyID : scCopies.get(scID)){
			  System.out.println("Service Chain ; SD pair ; Traffic");
			  //list of traffic node pairs using scID
			  for(TrafficNodes tn : serviceChainTN.get(scCopyID)){
				  System.out.println(scCopyID + " ; (" + tn.v1.get_id() + "," + tn.v2.get_id() + ") ; " + tn.flow_traffic);
			  }
			  System.out.println("No. of SD pairs = " + serviceChainTN.get(scCopyID).size());
			  System.out.println();
			}
		}
	}
	
	public static void populateCoresLinks(Map<Integer,ArrayList<HuerVarZ>> configsPerSC, Map<Integer,ServiceChain> ChainSet, List<FuncPt> func_list){
		for(Map.Entry<Integer, ArrayList<HuerVarZ>> entry : configsPerSC.entrySet()){
			int scID = entry.getKey();
			ServiceChain sc = ChainSet.get(scID);
			List<HuerVarZ> clustersForSC = entry.getValue();
			for(HuerVarZ cluster : clustersForSC){
				double totalTraffic = 0.0;
				for(Pp2VarDelta varD : cluster.DeltaVarSet){
					totalTraffic += varD.sd.flow_traffic;
				}
				for(PpVarA varA : cluster.AVarSet){
					BaseVertex nfvNode = varA.node;
					int fSeq = varA.f_seq;
					int fID = sc.chain_seq.get(fSeq);
					double coreCount = 0.0;
					for(FuncPt fpt : func_list){
						if(fpt.getid()==fID){
							coreCount = totalTraffic*fpt.getcore();
							break;
						}
					}
					cluster.addCoreCount(nfvNode, coreCount);
				}
				for(PpVarB varB : cluster.BVarSet){
					NodePair link = new NodePair(varB.s_vrt,varB.t_vrt);
					cluster.addLinkCapacity(link, totalTraffic);
				}
			}
		}
		
	}
	
	public static Map<Integer,ServiceChain> populateChainSetBasedOnScenario1(){
		//get the set of service chains
		Map<Integer,ServiceChain> ChainSet = new HashMap<Integer,ServiceChain>();
		//service chain 1
		ArrayList<Integer> sc0 = new ArrayList<Integer>();
		//add VNFs in service chain
		sc0.add(1);sc0.add(2);sc0.add(3);
		//add to map of service chains
		ChainSet.put(0, new ServiceChain(0,sc0));
		//service chain 2
		ArrayList<Integer> sc1 = new ArrayList<Integer>();
		//add VNFs in service chain
		sc1.add(3);sc1.add(4);sc1.add(5);
		//add to map of service chains
		ChainSet.put(1, new ServiceChain(1,sc1));
		//service chain 3
		ArrayList<Integer> sc2 = new ArrayList<Integer>();
		//add VNFs in service chain
		sc2.add(5);sc2.add(6);sc2.add(1);
		//add to map of service chains
		ChainSet.put(2, new ServiceChain(2,sc2));
		//return chain set
		return ChainSet;
	}
	
	public static Map<Integer,Integer> countMaxVnfBasedOnScNum(Map<Integer,ServiceChain> ChainSet, Map<Integer,ArrayList<Integer>> funcInSC){
		//create a Hash Map
		Map<Integer, Integer> tempMap = new HashMap<Integer, Integer>();
		//iterate through the function list
		for( Map.Entry<Integer, ArrayList<Integer>> entryVNF : funcInSC.entrySet()){
			int vnfID = entryVNF.getKey();
			int vnfCountMax = 0;
			//iterating through service chains
			for(int scID : entryVNF.getValue()){
				int fCountInSC = 0;
				//count the number of VNF occurrences in a SC
				for(int funcID : ChainSet.get(scID).chain_seq){
					if(vnfID == funcID){
						fCountInSC++;
					}
				}
				vnfCountMax += fCountInSC;
			}
			//put it in the hashMap
			tempMap.put(vnfID, vnfCountMax);
		}
		//return Hash Map
		return tempMap;
	}
	
	//store all shortest paths for all (s,d) pairs
	public static Map<NodePair,List<Path>> allShortestPathsForAllPairs(Graph g){
		//store shortest paths for each (s,d) pair
		Map<NodePair, List<Path>> shortestSdPaths = new HashMap<NodePair, List<Path>>();
		//store the shortest paths for each (s,d) pair
		for(BaseVertex source_vert : g._vertex_list){
 			for(BaseVertex target_vert : g._vertex_list){
 				if (source_vert.get_id() != target_vert.get_id()) { 
 					//shortest paths
 					YenTopKShortestPathsAlg sPaths = new YenTopKShortestPathsAlg(g,source_vert,target_vert);
 					//get the shortest path
 					Path shortestPath = sPaths.get_shortest_path(source_vert,target_vert);
 					int shortestPathLength = shortestPath.get_vertices().size()-1;
 					//add to list of paths
 					List<Path> path_temp = new ArrayList<Path>(); 					
 					path_temp.add(shortestPath);
 					// create the sd-pair for that pair of nodes
 					NodePair sd_temp = new NodePair(source_vert,target_vert);
 					//boolean flag for shortest path
 					boolean shortestFlag = true;
 					//add any shortest paths
 					while(sPaths.has_next() && shortestFlag){ 					
 						Path p = sPaths.next();
 						if((p.get_vertices().size()-1)>shortestPathLength){
 							shortestFlag = false;
 						}else if(!path_temp.contains(p)){ 							
 							path_temp.add(p);
 						}
 					}
 					//update the map
 					shortestSdPaths.put(sd_temp, path_temp);
 					//destroy object
 					sPaths.clear();
 				}
 			}
		}
		//return object
		return shortestSdPaths;
	}
	
	//calculate betweenness-centrality for ordered node pairs	
	public static Map<NodePair,ClusterTrafficPairs> calculateBetweennessCentralityForOrderedNodePairs(Graph g, 
			Map<NodePair,List<Path>> shortestSdPaths, List<NodePair> sdPairs){
		Map<NodePair,ClusterTrafficPairs> bcOfNodePair = new HashMap<NodePair,ClusterTrafficPairs>();
		int sdPairCount = 0;	
		//iterate over traffic pairs
		for(NodePair tempPair: sdPairs){
			//source and target vertices
			BaseVertex source_vert=tempPair.v1; BaseVertex target_vert=tempPair.v2;				
			//record their shortest path length			
			int shortestPathLength = shortestSdPaths.get(tempPair).get(0).get_vertices().size()-1;
			//create ClusterTrafficPairs
			ClusterTrafficPairs tempCluster = new ClusterTrafficPairs(tempPair,shortestPathLength);
			//record the betweenness-centrality value
			Double betwCent = new Double(0.0);
			//boolean flag to add NodePair
			boolean addNodePair = false;
			//iterate through the node pairs
			for(NodePair tempPair2: sdPairs){
//			for(Map.Entry<NodePair,List<Path>> entry: shortestSdPaths.entrySet()){
				List<Path> shortestPathsForPair = shortestSdPaths.get(tempPair2);
				double totalCount = shortestPathsForPair.size();//total number of paths
				//System.out.println("Total count= " + totalCount);
				double intersectionCount = 0.0;//intersection of paths with vertex
				//remove all the traffic pairs whose source vertex is the same as source vertex
				/*addNodePair = true;
				boolean srcVrt = (tempPair2.v1.get_id()==source_vert.get_id());*/			
				//remove all the traffic pairs whose sink vertex is the same as destination vertex
			    /*boolean destVrt = (tempPair2.v2.get_id()==target_vert.get_id());
			    if(!(srcVrt||destVrt)){*/
//				if(!srcVrt){
			    	//iterate through the paths
					for(Path p : shortestPathsForPair){
						//get the list of vertices in the path
						List<BaseVertex> pathVrts = p.get_vertices();
						//if path p passes through nodepair
						if(pathVrts.contains(source_vert)&&pathVrts.contains(target_vert)&&(pathVrts.indexOf(target_vert)>pathVrts.indexOf(source_vert))){
							//System.out.println("\t\t\t\t\tIntersection Count Incremented!");
							intersectionCount++;									
						}					
					}
					betwCent += (intersectionCount/totalCount);	
//			    }
			    //if a path for Node Pair passes through Temp Pair
			    if(intersectionCount != 0){
			    	tempCluster.addNodePair(tempPair2);
			    }
			}
			//add the foundation pair
			/*if(addNodePair){
				tempCluster.addNodePair(tempPair);
			}*/
			//update the betweennessCentrality value for the cluster of traffic pairs
			tempCluster.updateBcValue(betwCent);
			//update the number of (s,d) pairs in the cluster
			tempCluster.updateSdCount(tempCluster.clusterPairs.size());
			//update bc map
			bcOfNodePair.put(tempPair,tempCluster);
			//increase the sd count
			sdPairCount++;
			/*System.out.println("\nNumber of (s,d) pairs: " + sdPairCount);
			//print out the betweenness centrality for ordered node pair
			for(Map.Entry<NodePair, ClusterTrafficPairs> entry : bcOfNodePair.entrySet()){
				System.out.println("\tNode pair : (" + entry.getKey().v1.get_id() + "," + entry.getKey().v2.get_id() + ")");
				System.out.println("\t\t BC: " + entry.getValue().bcValue + " ; (s,d) pairs:" + entry.getValue().sdCount);
				System.out.println("\t\t (s,d) pairs:");
				for(NodePair np : entry.getValue().clusterPairs){
					System.out.println("\t\t\t(" + np.v1.get_id() + "," + np.v2.get_id() + ")");
				}
			}*/
		}	
		//System.out.println("Total number of (s,d) pairs: " + sdPairCount );
		//return the object
		return bcOfNodePair;
	}
	
	//calculate betweenness-centrality for ordered traffic node pairs	
	public static Map<NodePair,ClusterTrafficPairs> calculateBetweennessCentralityForOrderedNodePairs(Graph g, 
			Map<NodePair,List<Path>> shortestSdPaths, ArrayList<TrafficNodes> sdFlows){
			Map<NodePair,ClusterTrafficPairs> bcOfNodePair = new HashMap<NodePair,ClusterTrafficPairs>();
			//make sd pairs
			ArrayList<NodePair> sdPairs = new ArrayList<NodePair>();
			for(TrafficNodes sdFlow : sdFlows){
				sdPairs.add(new NodePair(sdFlow.v1, sdFlow.v2));
			}		
			int sdPairCount = 0;
			//iterate over traffic pairs
			for(NodePair tempPair: sdPairs){
				//source and target vertices
				BaseVertex source_vert=tempPair.v1; BaseVertex target_vert=tempPair.v2;			
				//record their shortest path length			
				int shortestPathLength = shortestSdPaths.get(tempPair).get(0).get_vertices().size()-1;
				//create ClusterTrafficPairs
				ClusterTrafficPairs tempCluster = new ClusterTrafficPairs(tempPair,shortestPathLength);
				//record the betweenness-centrality value
				Double betwCent = new Double(0.0);
				//boolean flag to add NodePair
				boolean addNodePair = false;
				//iterate through the node pairs
				for(NodePair tempPair2: sdPairs){
	//				for(Map.Entry<NodePair,List<Path>> entry: shortestSdPaths.entrySet()){
					List<Path> shortestPathsForPair = shortestSdPaths.get(tempPair2);
					double totalCount = shortestPathsForPair.size();//total number of paths				
					double intersectionCount = 0.0;//intersection of paths with vertex
					//remove all the traffic pairs whose source vertex is the same as source vertex
					/*addNodePair = true;
					boolean srcVrt = (tempPair2.v1.get_id()==source_vert.get_id());			
					//remove all the traffic pairs whose sink vertex is the same as destination vertex
				    boolean destVrt = (tempPair2.v2.get_id()==target_vert.get_id());
				    //if(!(srcVrt||destVrt)){
	//					if(!srcVrt){
*/				    	//iterate through the paths
						for(Path p : shortestPathsForPair){
							//get the list of vertices in the path
							List<BaseVertex> pathVrts = p.get_vertices();
							//if path p passes through nodepair
							if(pathVrts.contains(source_vert)&&pathVrts.contains(target_vert)&&(pathVrts.indexOf(target_vert)>pathVrts.indexOf(source_vert))){
								intersectionCount++;									
							}					
						}
						betwCent += (intersectionCount/totalCount);	
	//				    }
					    //if a path for Node Pair passes through Temp Pair
					    if(intersectionCount != 0){
					    	tempCluster.addNodePair(tempPair2);
					    }
				    //}
				}
				//add the foundation pair
				/*if(addNodePair){
					tempCluster.addNodePair(tempPair);
				}*/
				//update the betweennessCentrality value for the cluster of traffic pairs
				tempCluster.updateBcValue(betwCent);
				//update the number of (s,d) pairs in the cluster
				tempCluster.updateSdCount(tempCluster.clusterPairs.size());
				//update bc map
				bcOfNodePair.put(tempPair,tempCluster);
				//increase the sd count
				sdPairCount++;
			}	
			//return the object
			return bcOfNodePair;
	}
}
