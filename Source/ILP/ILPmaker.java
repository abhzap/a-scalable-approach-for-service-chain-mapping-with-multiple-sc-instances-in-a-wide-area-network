package ILP;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import FileOps.ReadFile;
import Given.InputConstants;
import edu.asu.emit.qyan.alg.control.DijkstraShortestPathAlg;
import edu.asu.emit.qyan.alg.control.YenTopKShortestPathsAlg;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class ILPmaker {

	private BufferedWriter writer;

	// create the desired file
	protected void createFileWriter(String fileName) throws Exception {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(fileName));
		} catch (IOException exp) {
			throw exp;
		} catch (Exception exp) {
			throw exp;
		}
		this.writer = out;
	}

	// write to the file
	protected void writeToFile(String line) throws Exception {
		try {
			// System.out.println("Writing to file:"+line);
			this.writer.write("\n" + line);
		} catch (IOException exp) {
			throw exp;
		} catch (Exception exp) {
			throw exp;
		}
	}

	// close the file
	protected void closeFile() throws Exception {
		try {
			this.writer.close();
		} catch (Exception exp) {
			throw exp;
		}
	}

	public void generateLPFile(Graph g, List<TrafficNodes> pair_list, int dc_node, ArrayList<BaseVertex> nfv_nodes, List<FuncPt> func_list, Map<Integer, ServiceChain> ChainSet, List<ChainConfig> ConfigList,
			HashMap<TrafficNodes,List<IngressEgressConfiguration>> Configuration, int traf_int, int cr_count) {

		// store the list of source vertices
		List<BaseVertex> source_vertice = new ArrayList<BaseVertex>();
		// store the list of destination vertices
		List<BaseVertex> destination_vertice = new ArrayList<BaseVertex>();

		// filling up the source_vertice and destination_vertice lists
		for (TrafficNodes tn_entr : pair_list) {
			if (!source_vertice.contains(tn_entr.v1) || source_vertice == null) {
				source_vertice.add(tn_entr.v1);
			}
			if (!destination_vertice.contains(tn_entr.v2)
					|| destination_vertice == null) {
				destination_vertice.add(tn_entr.v2);
			}
		}

		try {
			// Constructs a StringBuilder with an initial value of "" (an empty
			// string) and a capacity of 16.
			StringBuffer buf = new StringBuffer();
			// NumOfDCs, Hrs, TotalVMCount, DCcapacity, VMsPerRack ,
			// VMsPerServer
			this.createFileWriter(InputConstants.ILP_WRITE_PATH + InputConstants.ILP_FILE_NAME + "_" + InputConstants.FUNC_REQ.size() + "_" + traf_int + "_" + cr_count + ".lp");

			// **********OBJECTIVE FUNCTION*********************
			this.writeToFile("minimize");
			System.out.println("\n\n**********************Generating Objective**************\n\n");
			// clear the buffer
			buf.delete(0, buf.length());
			// iterate through service chains (only keys)
			for (Integer chain_index : ChainSet.keySet()) {
				int total_flow = 0;
				int chain_length = ChainSet.get(chain_index).getChainSize();
				//not be executed for 2 or less VNF service chains
				if (chain_length > 2){
					// get configurations for the particular service chain
					for (ChainConfig config_tuple : ConfigList) {
						// check the chain length, configuration depends on it
						// configurations
						if (config_tuple.getChainLength() == chain_length) {						
							for(TrafficNodes tn : Configuration.keySet()){
								//check - traffic nodes have same service chain traffic
								if(tn.getChainIndex() == chain_index){
									for(IngressEgressConfiguration ingeg : Configuration.get(tn)){
										//check the configuration 
//										if(config_path.node_seq.equals(config_tuple.getChainConfig())){
											//iterate through the paths for the configuration
											for(Path p : ingeg.config_routes){
												//at present taking only shortest path through the configuration
												if(ingeg.config_routes.indexOf(p) < InputConstants.allowed_config_paths){
													/*//copy the node sequence
													ArrayList<BaseVertex> updated_node_seq = new ArrayList<BaseVertex>(config_path.node_seq);
													//remove the first  
													updated_node_seq.remove(0);
													//get the node sequence length
													int remaining_seq_length = updated_node_seq.size();
													//remove the last
													updated_node_seq.remove(remaining_seq_length);*/
												}											
											}
//										}
									}
								}
							}			
						}
					}
				}
			}

			// *******CONSTRAINTS******
			this.writeToFile("\n\n");
			this.writeToFile("subject to");

			// VNF placement at a node
			for (ChainConfig cc : ConfigList) {

			}

			// Binary variables
			this.writeToFile("\n\n" + "binary");
			System.out.println("#####The ILP file has been generated!!#####");
			// flush the file
			// close the file
			this.closeFile();

		} catch (Exception exp) {
			System.err.println("Error in Creating/Writing the ILP file");
		} finally {
			try {
				// close the file
				this.closeFile();
			} catch (Exception exp) {
				System.out.println("Error in Closing the ILP file");
			}
		}
	}
	
	
	
	//generating permutations
	//done for generating all possible positions for VNF placements
	public static void generatePermutation(int places, int counter, ArrayList<BaseVertex> nfv_nodes, ArrayList<BaseVertex> config, ArrayList<ArrayList<BaseVertex>> configs){
		 //check if iterations required are over
		 if(counter!=0){
			//vertex to the configuration
			for(BaseVertex vrt : nfv_nodes){
				//instantiate new object before the first iteration
				if(counter == places){
				   config = new ArrayList<BaseVertex>();
				}				
				ArrayList<BaseVertex> config1 = new ArrayList<BaseVertex>(config);
				config1.add(vrt);
				generatePermutation(places,counter-1,nfv_nodes,config1,configs);				
			}
		 }else{
			   //add the configuration to the list of configurations
		       configs.add(config);			      
		 }
	}

	public static void main(String args[]) {

		// build graph object from given network file
		Graph g = new Graph(InputConstants.FILE_READ_PATH + InputConstants.NETWORK_FILE_NAME);		
		// print out the edges
		/*
		 * for(BaseVertex s_vert : g._vertex_list){ 
		 * 	for(BaseVertex t_vert : * g.get_adjacent_vertices(s_vert)){
		 *  	System.out.println( s_vert.get_id() * + "->" + t_vert.get_id()); 
		 * 	} 
		 * }
		 */
		//create the list of NFV-capable nodes
		ArrayList<BaseVertex> nfv_nodes = new ArrayList<BaseVertex>();
		for (BaseVertex tmp_vrt : g._vertex_list) {		
			if (tmp_vrt.get_type().equalsIgnoreCase("nfv")) {
				nfv_nodes.add(tmp_vrt);
			}
		}

		// k shortest paths
		int top_k = InputConstants.k_paths;
		// k shortest path objects
		YenTopKShortestPathsAlg kpaths = new YenTopKShortestPathsAlg(g);
		// Store paths for each s-d pair
		HashMap<TrafficNodes, List<Path>> sdpaths = new HashMap<TrafficNodes, List<Path>>();
		for (BaseVertex source_vert : g._vertex_list) {
			for (BaseVertex target_vert : g._vertex_list) {
				if (source_vert != target_vert) {
					List<Path> path_temp = new ArrayList<Path>(kpaths.get_shortest_paths(source_vert, target_vert,top_k));
					// create the sd-pair for that pair of nodes
					TrafficNodes sd_temp = new TrafficNodes(source_vert,target_vert);
					// add to list of paths depending on s-d pair
					sdpaths.put(sd_temp, path_temp);
				}
			}
		}

		// read the Set of Service Chains
		Map<Integer, ServiceChain> ChainSet = ReadFile.readChainSet(InputConstants.FILE_READ_PATH + InputConstants.CHAIN_SET);
		// print out the Set of Service Chains
		/*
		 * for(Map.Entry<Integer,ServiceChain> entry : ChainSet.entrySet()){
		 * System.out.print(entry.getKey() + "\t"); for(Integer temp :
		 * entry.getValue().getChainSeq()){ System.out.print(temp + ", "); }
		 * System.out.println(); }
		 */

		// Set of Configurations for a Service Chain 
		// assuming that the first and the last 
		// to make this string immutable
    	ArrayList<ChainConfig> ConfigList = new ArrayList<ChainConfig>();
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
	    }

		// allocate traffic to the given SD pairs
		// SD pairs between which we desire traffic to be
		// Store each s-d pair
		List<TrafficNodes> pair_list = new ArrayList<TrafficNodes>();
		pair_list = ReadFile.readSDPairs(InputConstants.FILE_READ_PATH + InputConstants.SD_PAIRS, sdpaths);
		// Store configurations for each set of traffic Nodes
		HashMap<TrafficNodes,List<IngressEgressConfiguration>> Configuration = new HashMap<TrafficNodes,List<IngressEgressConfiguration>>();
		DijkstraShortestPathAlg dijk = new DijkstraShortestPathAlg(g); 
		for (TrafficNodes tn : pair_list){
			BaseVertex source_vrt = tn.v1;
			BaseVertex destination_vrt = tn.v2;
			int chain_size = ChainSet.get(tn.chain_index).getChainSize();
			//create a list of ingress-egress configurations
			List<IngressEgressConfiguration> temp_list = new ArrayList<IngressEgressConfiguration>();			
			//create a TreeMap
			TreeMap<Integer,List<BaseVertex>> source_nfv_node_distances= new TreeMap<Integer,List<BaseVertex>>();
			TreeMap<Integer,List<BaseVertex>> destination_nfv_node_distances= new TreeMap<Integer,List<BaseVertex>>();
			for(BaseVertex nfv_node : nfv_nodes){
				//get shortest path
				int source_key = dijk.get_shortest_path(source_vrt, nfv_node).get_vertices().size()-1;	
				//check if key exists else create arraylist
				if(source_nfv_node_distances.containsKey(source_key)){
					source_nfv_node_distances.get(source_key).add(nfv_node);
				}else{
					List<BaseVertex> temp = new ArrayList<BaseVertex>();
					temp.add(nfv_node);
					source_nfv_node_distances.put(source_key, temp);
				}
				//get shortest path
				int destination_key = dijk.get_shortest_path(nfv_node, destination_vrt).get_vertices().size()-1;	
				//check if key exists else create arraylist
				if(destination_nfv_node_distances.containsKey(destination_key)){
					destination_nfv_node_distances.get(destination_key).add(nfv_node);
				}else{
					List<BaseVertex> temp = new ArrayList<BaseVertex>();
					temp.add(nfv_node);
					destination_nfv_node_distances.put(destination_key, temp);
				}
			}
			//get value of first key
			//find the closest vertex to the source node //can be more than one
			List<BaseVertex> ingress_nodes = new ArrayList<BaseVertex>(source_nfv_node_distances.firstEntry().getValue());	
			//find the closest vertex to the destination node // can be more than one
			List<BaseVertex> egress_nodes = new ArrayList<BaseVertex>(destination_nfv_node_distances.firstEntry().getValue());
			//list of ingress-egress nodes for the s-d pair
			if(ChainSet.get(tn.chain_index).getChainSize()>1){
				//create the list of ingress and egress nodes for the traffic nodes
				for(BaseVertex ingress_node : ingress_nodes){
					for(BaseVertex egress_node : egress_nodes){
						//generate combination if chain_size greater than 2
						if(chain_size > 2){
							ArrayList<ArrayList<BaseVertex>> configs = new ArrayList<ArrayList<BaseVertex>>();
							ArrayList<BaseVertex> config = new ArrayList<BaseVertex>();
							generatePermutation(chain_size-2,chain_size-2,nfv_nodes,config,configs);
							//generate new IngressEgressConfiguration for each configuration
							for(ArrayList<BaseVertex> item_config : configs){
								temp_list.add(new IngressEgressConfiguration(ingress_node,egress_node,item_config));
							}
						}else{
							IngressEgressConfiguration temp = new IngressEgressConfiguration(ingress_node,egress_node);
							temp_list.add(temp);
						}
					}
				}
			}else{
				for(BaseVertex ingress_node : ingress_nodes){
					IngressEgressConfiguration temp = new IngressEgressConfiguration(ingress_node,ingress_node);
					temp_list.add(temp);
				}
			}						
		    //add the ingress-egress node combinations to the Configuration
			Configuration.put(tn, temp_list);			
		}
		//Display the computed set of ingress-egress nodes and the configurations corresponding to the trafficnodes
		for(Map.Entry<TrafficNodes, List<IngressEgressConfiguration>> entry : Configuration.entrySet()){
			System.out.println(entry.getKey().v1.get_id() + " -> " + entry.getKey().v2.get_id());
			for(IngressEgressConfiguration egr : entry.getValue()){
				System.out.println("Ingress Node : " + egr.ingeg.v1.get_id() + ", Egress Node: " + egr.ingeg.v2.get_id());
				for(BaseVertex node_seq : egr.config){
					System.out.print(node_seq.get_id() + " ");
				}
				System.out.println();
			}
		}
		// Store the paths for each configuration		
		for(TrafficNodes tn : pair_list){
			ArrayList<BaseVertex> node_set = new ArrayList<BaseVertex>();
			List<IngressEgressConfiguration> config_list = Configuration.get(tn); 
			//get k-shortest paths for the traffic nodes
			List<Path> routes = sdpaths.get(tn);
			//select the shortest path between the ingress and egress node
			for(IngressEgressConfiguration pair : config_list){
		  		for(Path route : routes){
		  			//add the ingress node
		  			node_set.add(pair.ingeg.v1);
		  			//add the configuration
		  			node_set.addAll(pair.config);
		  			//add the egress node
		  			node_set.add(pair.ingeg.v2);		  			
		  			//remove the duplicates in the configuration
		  			Set<BaseVertex> no_duplicates = new HashSet();
		  			no_duplicates.addAll(node_set);
		  			//create the arraylist
		  			node_set.clear();
		  			//add the non-duplicates to the arraylist again
		  			node_set.addAll(no_duplicates);	
		  			//reverse the arraylist
 		  			Collections.reverse(node_set);
		  		    //check whether route as all the nodes in the configuration
	    			if(route.get_vertices().containsAll(node_set)){
	    				int node_order = 0;
	    				boolean order_check = true;
	    				//check the order of the variables
	    				for(BaseVertex vrt : node_set ){	
		    					if(node_order == 0)
		    						node_order = route.get_vertices().indexOf(vrt);
		    					if(node_order <= route.get_vertices().indexOf(vrt)){
		    						node_order = route.get_vertices().indexOf(vrt);
		    					}else{
		    						order_check = false;
		    						break;
		    					}		    						
	    				}
	    				//check if boolean value is same
	    				if(order_check){		    					
	    					//add the path to the configuration
	    					pair.config_routes.add(route);
	    				}		    
	    			}
		  		}
		  	}		    
		}	
		
		//print out details of ValidRoutes
		for(Map.Entry<TrafficNodes,List<IngressEgressConfiguration>> entry : Configuration.entrySet()){
			System.out.println("\n" + "\n");
			System.out.println(entry.getKey().toString());
			for(IngressEgressConfiguration temp_conf : entry.getValue() ){
				System.out.println("Ingress Node : " + temp_conf.ingeg.v1.get_id() + " Egress Node : " + temp_conf.ingeg.v2.get_id());				
				System.out.print("Configuration : ");
				for(BaseVertex vrt : temp_conf.config){
					System.out.print(vrt.get_id() + " ");
				}
				System.out.println();				
				for(Path temp : temp_conf.config_routes){
					System.out.print("Path : ");
					for(BaseVertex vrt : temp.get_vertices()){
						System.out.print(vrt.get_id() + "->");
					}
					System.out.println();
				}
			}
		}		

		// read the function point details
		List<FuncPt> func_list = new ArrayList<FuncPt>();
		func_list = ReadFile.readFnPt(InputConstants.FILE_READ_PATH	+ InputConstants.FUNCTION_DETAILS);	
		
		//DC node
		int dc_node = 0;
		
		
        // Generating the ILP
		int total_flow = 0;
		// ILP Generator object
		ILPmaker ilpgen = new ILPmaker();
		// assign num of cores to NFV capable nodes
		for (int core_count : InputConstants.CORE_NUM_LIST) {
			// assign traffic to sdpairs and generate ILP
			for (int traf_prcnt : InputConstants.TRAF_INT) {
				total_flow = 0;
				int br_flow = traf_prcnt * InputConstants.BANDWIDTH_PER_LAMBDA/ 100;
				int hq_flow = traf_prcnt * InputConstants.BANDWIDTH_PER_LAMBDA* 3 / 200;
				for (TrafficNodes tmp : pair_list) {
					// check for hq id //which is 2 now
					if (tmp.v1.get_id() == 2 || tmp.v2.get_id() == 2) {
						tmp.flow_traffic = hq_flow;
						total_flow += hq_flow;
					} else {
						tmp.flow_traffic = br_flow;
						total_flow += br_flow;
					}
					System.out.println("Percentage: " + traf_prcnt	+ " Total Flow: " + total_flow);
					// generate LP file
					ilpgen.generateLPFile(g, pair_list, dc_node, nfv_nodes, func_list, ChainSet, ConfigList, Configuration, traf_prcnt, core_count);					
				}

			}
		}
		
		

	}

}
