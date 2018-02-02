package colGen.model.heuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ILP.TrafficNodes;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class OldHeuristic {
	
	//sort the map according to its values
	private static Map<TrafficNodes, Double> sortByComparator(Map<TrafficNodes, Double> unsortMap) {
		// Convert Map to List
		ArrayList<Map.Entry<TrafficNodes, Double>> list = new ArrayList<Map.Entry<TrafficNodes, Double>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<TrafficNodes, Double>>() {
			public int compare(Map.Entry<TrafficNodes, Double> o1, Map.Entry<TrafficNodes, Double> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<TrafficNodes, Double> sortedMap = new HashMap<TrafficNodes, Double>();
		for (Iterator<Map.Entry<TrafficNodes, Double>> it = list.iterator(); it.hasNext();) {
			Map.Entry<TrafficNodes, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	//generating permutations
	//done for generating all possible positions for VNF placements
	//generatePermutation(chain_size-2,chain_size-2,nfv_nodes,config,configs);
	public static void generatePermutation(int places, int counter, BaseVertex ingress_node, BaseVertex egress_node, ArrayList<BaseVertex> nfv_nodes, ArrayList<BaseVertex> config, ArrayList<ArrayList<BaseVertex>> configs){
		 //check if iterations required are over
		 if(counter!=0){
			//vertex to the configuration
			for(BaseVertex vrt : nfv_nodes){
				//instantiate new object before the first iteration
				if(counter == places){
				    config = new ArrayList<BaseVertex>();				    
				}			
				ArrayList<BaseVertex> config1 = new ArrayList<BaseVertex>(config);			
				
					if(!config1.isEmpty() && vrt.get_id() == ingress_node.get_id()){//check if nfv_node is ingress node
						//last vertex added in the configuration is also ingress node
						if(config1.get(config1.size()-1).get_id() == ingress_node.get_id()){
							config1.add(vrt);
							generatePermutation(places,counter-1,ingress_node,egress_node,nfv_nodes,config1,configs);
						}else{
							config1.clear();
							continue;
						}
					}else if(!config1.isEmpty() && config1.get(config1.size()-1).get_id() == egress_node.get_id() ){//check if nfv_node is egress node
						//last vertex added in the configuration is also egress node
						if(vrt.get_id() == egress_node.get_id()){
							config1.add(vrt);
							generatePermutation(places,counter-1,ingress_node,egress_node,nfv_nodes,config1,configs);
						}else{
							config1.clear();
							continue;
						}
					}else if(!config1.isEmpty() && config1.contains(vrt)){
						//check if the last vertex is the same as vert
						if(config1.get(config1.size()-1).get_id() == vrt.get_id()){
							config1.add(vrt);
							generatePermutation(places,counter-1,ingress_node,egress_node,nfv_nodes,config1,configs);
						}
						else{
							config1.clear();
							continue;
						}
					}
				else{
					config1.add(vrt);
					generatePermutation(places,counter-1,ingress_node,egress_node,nfv_nodes,config1,configs);	
				}
			}
		 }else{
				   //add the configuration to the list of configurations
			       configs.add(config);			      
		 }
	}
	
	
	// Set of Configurations for a Service Chain 
			// assuming that the first and the last VNFs are placed at the ingress and egress nodes
			// to make this string immutable
	    	/*ArrayList<ChainConfig> ConfigList = new ArrayList<ChainConfig>();
			for(ServiceChain sc : ChainSet.values()){
				if(sc.chain_size > 2){
					ArrayList<ArrayList<BaseVertex>> configs = new ArrayList<ArrayList<BaseVertex>>();
					ArrayList<BaseVertex> config = new ArrayList<BaseVertex>();
					generatePermutation(sc.chain_size - 2, sc.chain_size - 2, nfv_nodes, config, configs);
					//generate new IngressEgressConfiguration for each configuration
					for(ArrayList<BaseVertex> item_config : configs){
						ConfigList.add(new ChainConfig(sc.chain_size,item_config));
					}
					configs.clear();
				}
			}
			// print out the set of configurations
		    for(ChainConfig cc : ConfigList){
		    	System.out.print(cc.chain_length + " : " );
		    	for(BaseVertex vrt : cc.node_seq){
		    	    System.out.print(vrt.get_id() + ", ");
		    	}
		    	System.out.println();
		    }*/	
	 //Store the ingress-egress configurations for each set of traffic Nodes
//	 HashMap<TrafficNodes,List<IngressEgressConfiguration>> Configuration = new HashMap<TrafficNodes,List<IngressEgressConfiguration>>();
//	 DijkstraShortestPathAlg dijk = new DijkstraShortestPathAlg(g); 
//	 for (TrafficNodes tn : pair_list){
//		 	BaseVertex source_vrt = tn.v1;
//	 		BaseVertex destination_vrt = tn.v2;
//			int chain_size = ChainSet.get(tn.chain_index).getChainSize();
//	 		//create a list of ingress-egress configurations
//	 		List<IngressEgressConfiguration> temp_list = new ArrayList<IngressEgressConfiguration>();			
//	 		//create a TreeMap since it sorts according to the natural ordering of its keys
//	 		TreeMap<Integer,List<BaseVertex>> source_nfv_node_distances = new TreeMap<Integer,List<BaseVertex>>();
//	 		TreeMap<Integer,List<BaseVertex>> destination_nfv_node_distances = new TreeMap<Integer,List<BaseVertex>>();
//	 		//to find the nearest NFV node to the source and the destination
//	 		//NFV node nearest to the source will be ingress node
//	 		//NFV ndoe nearest to the destination will be egress node
//	 		for(BaseVertex nfv_node : nfv_nodes){
//	 			//get shortest path
//	 			int source_key = dijk.get_shortest_path(source_vrt, nfv_node).get_vertices().size()-1;	
//	 			//check if key exists else create arraylist
//	 			if(source_nfv_node_distances.containsKey(source_key)){
//	 				source_nfv_node_distances.get(source_key).add(nfv_node);
//	 			}else{
//	 				List<BaseVertex> temp = new ArrayList<BaseVertex>();
//	 				temp.add(nfv_node);
//	 				source_nfv_node_distances.put(source_key, temp);
//	 			}
//	 			//get shortest path
//	 			int destination_key = dijk.get_shortest_path(nfv_node, destination_vrt).get_vertices().size()-1;	
//	 			//check if key exists else create arraylist
//	 			if(destination_nfv_node_distances.containsKey(destination_key)){
//	 					destination_nfv_node_distances.get(destination_key).add(nfv_node);
//	 			}else{
//	 					List<BaseVertex> temp = new ArrayList<BaseVertex>();
//	 					temp.add(nfv_node);
//	 					destination_nfv_node_distances.put(destination_key, temp);
//	 				}
//	 			}
//	 			//get value of first key
//	 			//find the closest vertex to the source node //can be more than one
//	 			List<BaseVertex> ingress_nodes = new ArrayList<BaseVertex>(source_nfv_node_distances.firstEntry().getValue());	
//	 			//find the closest vertex to the destination node // can be more than one
//	 			List<BaseVertex> egress_nodes = new ArrayList<BaseVertex>(destination_nfv_node_distances.firstEntry().getValue());
//	 			//list of ingress-egress nodes for the s-d pair
//	 			if(ChainSet.get(tn.chain_index).getChainSize()>1){
//	 				//create the list of ingress and egress nodes for the traffic nodes
//	 				for(BaseVertex ingress_node : ingress_nodes){
//	 					for(BaseVertex egress_node : egress_nodes){
//	 						//generate combination if chain_size greater than 2
//	 						if(chain_size > 2){
//	 							ArrayList<ArrayList<BaseVertex>> configs = new ArrayList<ArrayList<BaseVertex>>();
//	 							ArrayList<BaseVertex> config = new ArrayList<BaseVertex>();
//	 							generatePermutation(chain_size-2,chain_size-2,ingress_node,egress_node,nfv_nodes,config,configs);
//	 							//generate new IngressEgressConfiguration for each configuration
//	 							for(ArrayList<BaseVertex> item_config : configs){
//	 								temp_list.add(new IngressEgressConfiguration(ingress_node,egress_node,item_config));
//	 							}
//	 						}else{
//	 							IngressEgressConfiguration temp = new IngressEgressConfiguration(ingress_node,egress_node);
//	 							temp_list.add(temp);
//	 						}
//	 					}
//	 				}
//	 			}else{
//	 				for(BaseVertex ingress_node : ingress_nodes){
//	 					IngressEgressConfiguration temp = new IngressEgressConfiguration(ingress_node,ingress_node);
//	 					temp_list.add(temp);
//	 				}
//	 			}						
//	 		    //add the ingress-egress node combinations to the Configuration
//	 			Configuration.put(tn, temp_list);			
//	 		}
//	 		//Display the computed set of ingress-egress nodes and the configurations corresponding to the trafficnodes
//	 	  /*for(Map.Entry<TrafficNodes, List<IngressEgressConfiguration>> entry : Configuration.entrySet()){
//	 			System.out.println(entry.getKey().v1.get_id() + " -> " + entry.getKey().v2.get_id() + " : " + entry.getKey().chain_index);
//	 			for(IngressEgressConfiguration egr : entry.getValue()){
//	 				System.out.println("Ingress Node : " + egr.ingeg.v1.get_id() + ", Egress Node: " + egr.ingeg.v2.get_id());
//	 				System.out.print("Configuration : ");
//	 				for(BaseVertex node_seq : egr.config){
//	 					System.out.print(node_seq.get_id() + " ");
//	 				}
//	 				System.out.println();
//	 			}
//	 		}	 		*/
//	 		
//	 		
//	 		
//	 		
//	 		// Store the paths for each configuration		
//	 		for(TrafficNodes tn : pair_list){
////	 			System.out.println();
////	 			System.out.println("############# SD PAIR ( " + tn.v1.get_id() + " , " + tn.v2.get_id() + " ) ################");	 			
//	 			List<IngressEgressConfiguration> config_list = Configuration.get(tn); 
//	 			/*for(IngressEgressConfiguration temp : config_list){
//	 				System.out.println("Ingress Node : " + temp.ingeg.v1.get_id() + ", Egress Node: " + temp.ingeg.v2.get_id());
//	 				System.out.print("Configuration : ");
//	 				for(BaseVertex node_seq : temp.config){
//	 					System.out.print(node_seq.get_id() + " ");
//	 				}
//	 				System.out.println();
//	 			}*/
//	 			//to store the join of the ingress, configuration and egress
//	 			ArrayList<BaseVertex> node_set = new ArrayList<BaseVertex>();
//	 			//get k-shortest paths for the traffic nodes
//	 			List<Path> routes = sdpaths.get(tn);
//	 			//select the shortest path between the ingress and egress node
//	 			for(IngressEgressConfiguration temp1 : config_list){
//	 				//add the ingress node and egress node 		  			
////		  			System.out.println("Ingress Node : " + temp1.ingeg.v1.get_id() + ", Egress Node : " + temp1.ingeg.v2.get_id());
//		  			node_set.add(temp1.ingeg.v1);
//		  			node_set.addAll(temp1.config); 		
//		  			node_set.add(temp1.ingeg.v2);		  			
//		  			//add the configuration 		  			
////		  			for(BaseVertex node_seq : temp1.config){
////	 					System.out.print("Configuration : " + node_seq.get_id() + " ");
////	 				}
////	 				System.out.println();	 				  			
//		  			//to store the first occurrence of the vertex
//		  		/*	ArrayList<BaseVertex> already_occurred = new ArrayList<BaseVertex>();
//		  			//to avoid concurrent modification exception			
//		  			Iterator<BaseVertex> itr = node_set.iterator();
//		  			//remove the duplicates from the configuration
//		  			while(itr.hasNext()){
//		  				BaseVertex vrt = itr.next();
//		  				//the vertex has already been encountered
//		  				if(already_occurred.contains(vrt)){
//		  					itr.remove();//remove the duplicate
//		  				}else{
//		  					already_occurred.add(vrt);
//		  				}
//		  			}*/  			
//		  			//remove the duplicates in the configuration
//		  			/*Set<BaseVertex> no_duplicates = new HashSet<BaseVertex>();
//		  			no_duplicates.addAll(node_set);
//		  			//create the arraylist
//		  			node_set.clear();
//		  			//add the non-duplicates to the arraylist again
//		  			node_set.addAll(no_duplicates);	*/
//		  			//reverse the arraylist
////		  			Collections.reverse(node_set);
//		  			//Print out the configuration for which routes are being searched
////		  			for(BaseVertex vrt : node_set){
////		  				System.out.print(vrt.get_id() + ", ");
////		  			}
////		  			System.out.println();
//		  		    //check whether route has all the nodes in the configuration
//	 		  		for(Path route : routes){	 		  		    
//	 	    			if(route.get_vertices().containsAll(node_set)){
////	 	    				System.out.println("Path contains the node_set");
////	 	    				System.out.print("Path : ");
////	 		 				for(BaseVertex vrt : route.get_vertices()){
////	 		 					System.out.print(vrt.get_id() + "->");
////	 		 				}
////	 		 				System.out.println();
//	 		 				
//	 	    				int node_order = 0;
//	 	    				boolean order_check = true;
//	 	    				//check the order of the variables
//	 	    				for(BaseVertex vrt : node_set ){	
//	 		    					if(node_order == 0)
//	 		    						node_order = route.get_vertices().indexOf(vrt);
//	 		    					if(node_order <= route.get_vertices().indexOf(vrt)){
//	 		    						node_order = route.get_vertices().indexOf(vrt);
//	 		    					}else{
//	 		    						order_check = false;
//	 		    						break;
//	 		    					}		    						
//	 	    				}
//	 	    				//check if boolean value is same
//	 	    				if(order_check){
////	 	    					System.out.println("###########  Path passes the order check  ###########");
//	 	    					//add the path to the configuration
//	 	    					temp1.config_routes.add(route);
//	 	    				}		    
//	 	    			}
//	 		  		}
//	 		  		//clear node_set
//	 		  		node_set.clear();
//	 		  	}		    
//	 		}	
//	 		
//	 		
//	 		//remove the configurations for which no valid routes are found
//	 		for(Map.Entry<TrafficNodes,List<IngressEgressConfiguration>> entry : Configuration.entrySet()){
//	 			Iterator<IngressEgressConfiguration> itr = entry.getValue().iterator();
//	 			while(itr.hasNext()){
//		 			if(itr.next().config_routes.isEmpty()){
//		 				//remove the object from the iterator
//		 				itr.remove();
//		 			}
//	 			}
//	 		}
	 		
	 		
	 		
	 		//print out details of ValidRoutes
//	 		for(Map.Entry<TrafficNodes,List<IngressEgressConfiguration>> entry : Configuration.entrySet()){
////	 			System.out.println("\n" + "\n");
//	 			System.out.println(entry.getKey().toString());
//	 			for(IngressEgressConfiguration temp_conf : entry.getValue() ){
//	 				System.out.println("Ingress Node : " + temp_conf.ingeg.v1.get_id() + " Egress Node : " + temp_conf.ingeg.v2.get_id());				
//	 				System.out.print("Configuration : ");
//	 				for(BaseVertex vrt : temp_conf.config){
//	 					System.out.print(vrt.get_id() + " ");
//	 				}
//	 				System.out.println();				
//	 				for(Path temp : temp_conf.config_routes){
//	 					System.out.print("Path : ");
//	 					for(BaseVertex vrt : temp.get_vertices()){
//	 						System.out.print(vrt.get_id() + "->");
//	 					}
//	 					System.out.println();
//	 				}
//	 			}
//	 		}

	 		 //Store configurations for each set of traffic nodes as originally conceptualized in the formulation
//	 		 HashMap<TrafficNodes,IngressEgressConfigs> OriginalConfigs = new HashMap<TrafficNodes,IngressEgressConfigs>();
//	 		 for(TrafficNodes tn : pair_list){
//	 			 List<IngressEgressConfiguration> temp_configs = Configuration.get(tn); 
//	 			 //set the shortest path length parameter in each of the configurations
//	 			 for(IngressEgressConfiguration itr : temp_configs){
//	 				 itr.setShortestPathLength();
//	 			 }
//	 			 //sort the list based on the shortest path length 
//	 			 IngressEgressConfigurationComparator comparison = new IngressEgressConfigurationComparator();
//			     Collections.sort(temp_configs, comparison);	
//			     //choose the ingress node - egress node configuration that gives the shortest path
////			     IngressEgressConfiguration shortestPathIngEg = temp_configs.get(0);
//			     //take the non optimal path
//			     IngressEgressConfiguration nonOptimalPathIngEg = temp_configs.get(1);
//			     //add the ingress egress node
////			     IngressEgressConfigs temp = new IngressEgressConfigs(shortestPathIngEg.ingeg); 	
//			     IngressEgressConfigs temp = new IngressEgressConfigs(nonOptimalPathIngEg.ingeg); 			    
//			     //iterate through the list and find all the viable configurations of the Shortest Path route
//			     for(IngressEgressConfiguration ingegconfig : temp_configs){
////			    	if(shortestPathIngEg.ingeg.equals(ingegconfig.ingeg)){
//			    	if(nonOptimalPathIngEg.ingeg.equals(ingegconfig.ingeg)){
//			    		 temp.configs.add(new Configuration(ingegconfig.config,ingegconfig.config_routes));
//			    	}
//			     }
//			     //add the IngressIgressConfigs to OriginalConfigs
//			     OriginalConfigs.put(tn,temp);
//	 		 }
//	 		
//	 		if(OriginalConfigs.values().isEmpty()){
//	 			System.out.println("OriginalConfigs Values are EMPTY!!!!!!");
//	 		}
	
}
