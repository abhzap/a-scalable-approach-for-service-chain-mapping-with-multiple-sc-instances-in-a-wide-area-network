package colGen.model.preprocess;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import FileOps.ReadFile;
import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.heuristic.BaseHeuristic;
import colGen.model.heuristic.BaseHeuristic2.SdDetails;
import colGen.model.heuristic.HuerVarZ;
import colGen.model.ver1.MpVarY;
import colGen.model.ver1.MpVarZ;
import colGen.model.ver1.NewIngEgConfigs;
import colGen.model.ver1.PpVarB;
import colGen.model.ver1.VertexRank;
import colGen.model.ver2.Pp2VarDelta;
import edu.asu.emit.qyan.alg.control.YenTopKShortestPathsAlg;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class preProcFunctions {
	
	public static Graph makeGraphObject() throws Exception{		
		//build graph object from given network file
	    Class<?> cls = Class.forName("colGen.model.ver2.CG2");		
	    //returns the ClassLoader
	    ClassLoader cLoader = cls.getClassLoader();
	    //print out the class name
	    System.out.println("Class Name : " + cLoader.getClass());
	    //finds the resource with the given name
	    InputStream networkFileStream = cLoader.getResourceAsStream(InputConstants.FILE_READ_PATH + InputConstants.NETWORK_FILE_NAME);
	    //return the Graph Object
	    return new Graph(networkFileStream);
	}	
	
	public static void makeAllVrtSwitches(Graph g){
		for(BaseVertex vrt: g._vertex_list){
			vrt.set_type("sw");
		}
	}
	
	public static void printGraph(Graph g){
		//print out the nodes
		System.out.println("Vertex List:");
		for(BaseVertex vrt : g.get_vertex_list()){
			System.out.println(vrt.get_id() + "," + vrt.get_type());
		}
		//print out the links
		System.out.println("Edge List:");
		for(BaseVertex source: g.get_vertex_list()){
			for(BaseVertex sink: g.get_adjacent_vertices(source)){
				System.out.println(source.get_id() + "," + sink.get_id() + "," + g.get_edge_weight(source, sink) + "," + g.get_edge_length(source, sink));
			}
		}		
	}
	
	//take input of (s,d) paths and the traffic pairs that are available
	public static Double totalBwUsedOnShortestPaths( HashMap<NodePair,List<Path>> sdpaths, List<TrafficNodes> pair_list){
		Double bwUsed = 0.0;
		//iterate through the pair_list
		for(TrafficNodes tn : pair_list){
			NodePair np = new NodePair(tn.v1,tn.v2);
			//multiply the flow value with the path length
			//add to bandwidth already calculated
			bwUsed += tn.flow_traffic*(sdpaths.get(np).get(0).get_vertices().size()-1);
		}
		return bwUsed;
	}
	
	public static HashMap<NodePair, List<Path>> findRoutesForSDpairs(Graph g) {
    	// k shortest paths
 		int top_k = InputConstants.k_paths;
 		// k shortest path objects
 		YenTopKShortestPathsAlg kpaths = new YenTopKShortestPathsAlg(g);
 		// Store paths for each s-d pair
 		HashMap<NodePair, List<Path>> sdpaths = new HashMap<NodePair, List<Path>>();
 		for(BaseVertex source_vert : g._vertex_list){
 			for(BaseVertex target_vert : g._vertex_list){
 				if (source_vert != target_vert) {
 					List<Path> path_temp = new ArrayList<Path>(kpaths.get_shortest_paths(source_vert, target_vert,top_k));
 					// create the sd-pair for that pair of nodes
 					NodePair sd_temp = new NodePair(source_vert,target_vert);
 					// add to list of paths depending on s-d pair
 					sdpaths.put(sd_temp, path_temp);
 				}
 			}
 		}
 		return sdpaths;
	}
	
	//calculate betweenness centrality of nodes in the graph
	public static Map<BaseVertex,Double> calculateBetweenessCentrality(Graph g){
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
		//calculate between centrality of each vertex
		Map<BaseVertex,Double> bcOfVertex = new HashMap<BaseVertex,Double>();
		for(BaseVertex vrt : g._vertex_list){
			Double betwCent = new Double(0.0);
			for(Map.Entry<NodePair,List<Path>> entry: shortestSdPaths.entrySet()){
				double totalCount = entry.getValue().size();//total number of paths
				double intersectionCount = 0.0;//intersection of paths with vertex
				boolean srcVrt = (entry.getKey().v1.get_id()==vrt.get_id());
			    boolean destVrt = (entry.getKey().v2.get_id()==vrt.get_id());
			    if(!(srcVrt||destVrt)){
					for(Path p : entry.getValue()){			
						if(p.get_vertices().contains(vrt)){
							intersectionCount++;
						}					
					}
					betwCent += (intersectionCount/totalCount);	
			    }
			}	
			bcOfVertex.put(vrt,betwCent);
		}		
		//print out the betweeness centrality that is calculated
		System.out.println("##### Betweenness Centrality #####");
		for(Map.Entry<BaseVertex,Double> entry: bcOfVertex.entrySet()){
			System.out.println("Node "+entry.getKey().get_id()+" = "+entry.getValue());
		}
		return bcOfVertex;
	}

	//calculate product of betweeness centrality and degree centrality
	public static Map<BaseVertex,Double> calProductOfBCandDeg(Graph g, Map<BaseVertex,Double> bcOfVertex){
		 Map<BaseVertex,Double> vertexRank = new HashMap<BaseVertex,Double>();
		 for(Map.Entry<BaseVertex,Double> entry: bcOfVertex.entrySet()){
			 //calculate the product
			 double prod = entry.getValue()*g.get_adjacent_vertices(entry.getKey()).size();
			 //update the ranking
			 vertexRank.put(entry.getKey(),prod);
		 }
		 //print out the ranking of the vertices
		 System.out.println("##### Vertex Ranking #####");
		 for(Map.Entry<BaseVertex,Double> entry: vertexRank.entrySet()){
			System.out.println("Node "+entry.getKey().get_id()+" = "+entry.getValue());
		 }
		 return vertexRank;
	}
	
		
	
	//print out the ranking of the vertices in descending order
	public static void printVertexRanking(List<VertexRank> rankList){
		System.out.println("Printing out rank values in descending order:");
		for(VertexRank vr : rankList){
			System.out.println("\tNode " + vr.vertex.get_id() + " = " + vr.rankValue);
		}
	}
	
	public static Map<Integer, ServiceChain> setOfServiceChains() throws Exception{
		//build graph object from given network file
	    Class<?> cls = Class.forName("colGen.model.ver2.CG2");		
	    //returns the ClassLoader
	    ClassLoader cLoader = cls.getClassLoader();
		// read the Set of Service Chains
 	    InputStream chainSetStream = cLoader.getResourceAsStream(InputConstants.FILE_READ_PATH + InputConstants.CHAIN_SET);
 	    return ReadFile.readChainSet(chainSetStream);		
	}
	
	public static void printServiceChains(Map<Integer, ServiceChain> ChainSet){
		for(Map.Entry<Integer,ServiceChain> entry : ChainSet.entrySet()){
			 System.out.print(entry.getKey() + "\t");
			 for(Integer temp : entry.getValue().getChainSeq()){ 
				 System.out.print(temp + ", "); 
			 }
			 System.out.println("\t" + entry.getValue().getLatReq());
			 System.out.println(); 
		 }
	}	
	
	public static List<TrafficNodes> setOfSDpairs( HashMap<NodePair, List<Path>> sdpaths) throws Exception{
		//build graph object from given network file
	    Class<?> cls = Class.forName("colGen.model.ver2.CG2");		
	    //returns the ClassLoader
	    ClassLoader cLoader = cls.getClassLoader();
		InputStream sdPairStream = cLoader.getResourceAsStream(InputConstants.FILE_READ_PATH + InputConstants.SD_PAIRS);
		return ReadFile.readSDPairs(sdPairStream, sdpaths);
	}
	
	public static void printSDpairs(List<TrafficNodes> pair_list, List<Integer> scUsed){
		System.out.println("***** Traffic Node Pairs *****");
		for(Integer scID : scUsed){	
			System.out.println("Service Chain ; SD pair ; Traffic");
			for(TrafficNodes tn : pair_list){
				//find if service chain ID's are the same
				if(tn.chain_index == scID){
					System.out.println(scID + " ; (" + tn.v1 + "," + tn.v2 + ") ; " + tn.flow_traffic);
				}
			}
		}
		System.out.println("Total number of distinct sd pairs: " + pair_list.size());
	}
	
	public static List<Integer> serviceChainsUsed(List<TrafficNodes> pair_list){
		List<Integer> scUsed = new ArrayList<Integer>();
		//iterate through the traffic nodes
		for(TrafficNodes tn : pair_list){
			//check if the scID has already been added
			if(!scUsed.contains(tn.chain_index)){
				scUsed.add(tn.chain_index);
			}
		}
		return scUsed;
	}
	
	public static List<FuncPt> totalListOfVNFs() throws Exception{
		//build graph object from given network file
	    Class<?> cls = Class.forName("colGen.model.ver2.CG2");		
	    //returns the ClassLoader
	    ClassLoader cLoader = cls.getClassLoader();
		InputStream vnfListStream = cLoader.getResourceAsStream(InputConstants.FILE_READ_PATH	+ InputConstants.FUNCTION_DETAILS);
		return ReadFile.readFnPt(vnfListStream);
	}
	
	public static List<FuncPt> listOfVNFsUsed(List<FuncPt> vnf_list, Map<Integer, ServiceChain> ChainSet, List<Integer> scUsed) throws Exception{
		List<FuncPt> func_list = new ArrayList<FuncPt>();
		//iterate through the VNF list
		for(FuncPt vnf : vnf_list){
			//iterate through the service chains
			for(int sc : scUsed){
				//if a particular service chain has the vnf
				if(ChainSet.get(sc).chain_seq.contains(vnf.getid())){					
					//if VNF does not exists in the function list 
					if(!func_list.contains(vnf)){
						func_list.add(vnf);
					}
				}
			}
		}
		return func_list;
	}
	
	public static void printListOfVNFsUsed(List<FuncPt> func_list){
		for(FuncPt fp: func_list){
			System.out.println("Function ID:\t" + fp.getid() + "\t" + fp.getcore() + "\t" + fp.getProcDelay()); 
		}
	}
	
	public static Map<Integer, ArrayList<TrafficNodes>> sdPairsforServiceChain(List<Integer> scUsed, List<TrafficNodes> pair_list){
		Map<Integer, ArrayList<TrafficNodes>> serviceChainTN = new HashMap<Integer, ArrayList<TrafficNodes>>();
		//iterate through the list of service chains to be deployed
		for(int scID : scUsed){
			  //list of traffic nodes for a service chain
			  ArrayList<TrafficNodes> scTnList = new ArrayList<TrafficNodes>();
			  //iterate through the list of traffic Nodes
			  for(TrafficNodes tn : pair_list){
				  //check the service chain ID
				  if(tn.chain_index == scID){
					  scTnList.add(tn);
				  }
			  }
			  //put the list of traffic nodes in the map
			  serviceChainTN.put(scID, scTnList);
		}
		return serviceChainTN;
	}
	
	public static void printTrafficNodesForServiceChains(List<Integer> scUsed,  Map<Integer, ArrayList<TrafficNodes>> serviceChainTN){
		for(int scID : scUsed){
			  System.out.print("Service Chain : " + scID + " ; " + serviceChainTN.get(scID).size() + " pairs"  + " -> ");
			  //list of traffic node pairs using scID
			  for(TrafficNodes tn : serviceChainTN.get(scID)){
				  System.out.print(" (" + tn.v1.get_id() + "," + tn.v2.get_id() + ");");
			  }
			  System.out.println();
		}
	}
	
	public static void getConfigForSC(Map<Integer, ArrayList<ArrayList<BaseVertex>>> serviceChainConfiguration, List<Integer> scUsed, 
			Map<Integer, ServiceChain> ChainSet, ArrayList<BaseVertex> nfv_nodes){

		//generate the list of configurations for each service chain
		//iterate through the list of service chains to be deployed
		for(int scID : scUsed){
		  //select the service chain from the set of chains to be deployed
		  ServiceChain sc = ChainSet.get(scID);
		  ArrayList<BaseVertex> config = new ArrayList<BaseVertex>();
		  ArrayList<ArrayList<BaseVertex>> configs = new ArrayList<ArrayList<BaseVertex>>();
		  //generate the configurations
		  BaseHeuristic.generateConfiguration(sc.getChainSize(), sc.getChainSize(), nfv_nodes, config, configs);
		  //add these configuration to the Map
		  serviceChainConfiguration.put(scID, configs);
		}
	}
	
	public static void printConfigForSC(Map<Integer, ArrayList<ArrayList<BaseVertex>>> serviceChainConfiguration, List<Integer> scUsed){
		//print the list of configuration for each service chain
		for(int scID : scUsed){
		  	System.out.println("Service Chain : " + scID);
		  	ArrayList<ArrayList<BaseVertex>> permuteList = serviceChainConfiguration.get(scID);
		  	for(ArrayList<BaseVertex> permute : permuteList){
			  	System.out.print("	(");
			  	for(BaseVertex vrt : permute){
				  	System.out.print(vrt.get_id()+",");
			  	}
			  	System.out.println(")");
		  	}
		}
	}
	
	public static void printConfigAndPathForTN(List<TrafficNodes> pair_list, Map<TrafficNodes, ArrayList<NewIngEgConfigs>> OriginalConfigs ){
		 for(TrafficNodes tn : pair_list){	
				System.out.println(tn.toString());	
		 		for(NewIngEgConfigs confg : OriginalConfigs.get(tn)){ 				
		 			System.out.println("Ingress Node : " + confg.ingeg.v1.get_id() + " Egress Node : " + confg.ingeg.v2.get_id());		 		
					System.out.print("Configuration : ");
					for(BaseVertex vrt : confg.cfg.config){
						System.out.print(vrt.get_id() + " ");
					}
					System.out.println();				
					for(Path temp : confg.cfg.config_routes){
						System.out.print("Path : ");
						for(BaseVertex vrt : temp.get_vertices()){
							System.out.print(vrt.get_id() + "->");
						}
						System.out.println();
					}			
		 		} 
			 }
	}
	
	public static Map<Integer, ArrayList<Integer>> vnfInSCs(List<Integer> scUsed, List<FuncPt> func_list, Map<Integer, ServiceChain> ChainSet){
		//create a Hash Map
		Map<Integer, ArrayList<Integer>> tempMap = new HashMap<Integer, ArrayList<Integer>>();
		//iterate through function list
		for(FuncPt vnf : func_list){
			int vnfID = vnf.getid();
			ArrayList<Integer> scWithVNF = new ArrayList<Integer>();
			//iterate through service chains
			for(int scID : scUsed){
				//if VNF is in the service chain
				if(ChainSet.get(scID).chain_seq.contains(vnfID)){
					//add SC to list of SCs
					scWithVNF.add(scID);
				}
			}
			//put it in the Map
			tempMap.put(vnfID, scWithVNF);
		}
		//return Hash Map
		return tempMap;
	}
	
	public static void clusterPerSC( Map<Integer,ArrayList<Integer>> scCopies){
		for(Map.Entry<Integer, ArrayList<Integer>> entry: scCopies.entrySet()){
			System.out.println("SC: " + entry.getKey() + " ; Cluster Count: " + entry.getValue().size());
		}
	}
	
	
	public static Map<Integer,Integer> countMaxVnfBasedOnSdPairs(Map<Integer,ServiceChain> ChainSet, Map<Integer,ArrayList<Integer>> funcInSC, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN){
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
				vnfCountMax += fCountInSC*serviceChainTN.get(scID).size();
//				vnfCountMax += fCountInSC*InputConstants.configCountPerServiceChain;
			}
			//put it in the hashMap
			tempMap.put(vnfID, vnfCountMax);
		}
		//return Hash Map
		return tempMap;
	}
	
	public static Map<Integer,Integer> countMaxVnfBasedOnClustering(Map<Integer,ServiceChain> ChainSet, Map<Integer,ArrayList<Integer>> funcInSC, Map<Integer,ArrayList<Integer>> scCopies){
		//create a Hash Map
		Map<Integer, Integer> tempMap = new HashMap<Integer, Integer>();
		//iterate through the function list
		for( Map.Entry<Integer, ArrayList<Integer>> entryVNF : funcInSC.entrySet()){
			//get function ID
			int vnfID = entryVNF.getKey();
			//count the max number of VNFs
			int vnfCountMax = 0;
			//iterating through service chains
			for(int scID: entryVNF.getValue()){
				int fCountInSC = 0;
				//count the number of VNF occurrences in a SC
				for(int funcID : ChainSet.get(scID).chain_seq){
					if(vnfID == funcID){
						fCountInSC++;
					}
				}
				//multiply by the number of clusters for a SC
				vnfCountMax += fCountInSC*scCopies.get(scID).size();
			}
			//put it in the hashMap
			tempMap.put(vnfID, vnfCountMax);
		}
		//return Hash Map
		return tempMap;
	}
	
	public static void printMaxCountPerVnf( Map<Integer,Integer> CountMaxVNF){
		for(Map.Entry<Integer,Integer> entry: CountMaxVNF.entrySet()){
			System.out.println("\tVNF " + entry.getKey() + " = " + entry.getValue());
		}
	}
	
	public static void printSDpairsSCandTraffic(List<Integer> scUsed,  Map<Integer, ArrayList<TrafficNodes>> serviceChainTN){
		for(int scID : scUsed){
			  System.out.println("Service Chain ; SD pair ; Traffic");
			  //list of traffic node pairs using scID
			  for(TrafficNodes tn : serviceChainTN.get(scID)){
				  System.out.println(scID + " ; (" + tn.v1.get_id() + "," + tn.v2.get_id() + ") ; " + tn.flow_traffic);
			  }
			  System.out.println("No. of SD pairs = " + serviceChainTN.get(scID).size());
			  System.out.println();
		}
	}
	
	public static void printConfigsPerSCforBH2(List<Integer> scUsed, Map<Integer,ArrayList<HuerVarZ>> configsPerSC){
		 System.out.println("##### Print out the configurations for each service chain! #####");
		 for(int scID : scUsed){
			 System.out.println("##### " + scID + " #####");
			  for(MpVarZ entry : configsPerSC.get(scID)){		    		  
	    		  System.out.println("Z No. " + entry.cgConfig);
	    		  System.out.println("\t\t\t\tSC" + entry.sCiD);
	    		  System.out.println("\t\t\t\tConfig " + entry.configDsc);
	    		  //if (s,d) pairs are selected
	    		  if(!entry.DeltaVarSet.isEmpty()){
		    		  System.out.print("\t\t\t\t(S,D): ");
		    		  //print out D variables
		    		  for(Pp2VarDelta varD : entry.DeltaVarSet){
		    			  System.out.print("(" + varD.sd.v1.get_id() + "," + varD.sd.v2.get_id() + "); "); 
		    		  }
		    		  System.out.print("\n");
	    		  }
	    		  //if links are used
	    		  if(!entry.BVarSet.isEmpty()){
		    		  System.out.println("\t\t\t\tLinks: ");
		    		  //print out B variables
		    		  for(PpVarB varB : entry.BVarSet){
		    			  System.out.println("\t\t\t\t\t" + varB.s_vrt.get_id() + "->" + varB.t_vrt.get_id() + " : " + varB.s_f_index);
		    		  }
	    		  }
			  }
		  }
	}

	public static void printConfigForSD(Map<TrafficNodes,SdDetails> configPerSD){
		System.out.println("Print out SD pairs, their routes and configuration selected");
		  for(Map.Entry<TrafficNodes,SdDetails> entrySD : configPerSD.entrySet()){
			  System.out.println("("+entrySD.getKey().v1.get_id()+","+entrySD.getKey().v2.get_id()+") ; SC"+entrySD.getKey().chain_index);
			  //from source to firstVNF
			  for(MpVarY objY : entrySD.getValue().routeToFirstVNF){
				  System.out.println("\t\t\t\tSeq " + objY.f_id + " : " + objY.s_vrt.get_id() + "-->" + objY.t_vrt.get_id());
			  }
			  //configuration used
			  System.out.println("\t\t\t\tConfig No. : " + entrySD.getValue().conFiguration.cgConfig);						  
			  //from lastVNF to destination
			  for(MpVarY objY : entrySD.getValue().routeFromLastVNF){
				  System.out.println("\t\t\t\tSeq " + objY.f_id + " : " + objY.s_vrt.get_id() + "-->" + objY.t_vrt.get_id());
			  }
		  }
	}
}


