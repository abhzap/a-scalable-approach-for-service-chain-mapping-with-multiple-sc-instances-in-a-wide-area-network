package colGen.model.heuristic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import colGen.model.ver1.Configuration;
import colGen.model.ver1.NewIngEgConfigs;
import Given.InputConstants;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class BaseHeuristic {
	//generate configuration
	//this configuration generation does not use ingress egress nodes
	public static void generateConfiguration(int places, int counter, ArrayList<BaseVertex> nfv_nodes, ArrayList<BaseVertex> config, ArrayList<ArrayList<BaseVertex>> configs){
		 //check if iterations required are over
		 if(counter!=0){
			//vertex to the configuration
			for(BaseVertex vrt : nfv_nodes){
				//instantiate new object before the first iteration
				if(counter == places){
				    config = new ArrayList<BaseVertex>();				    
				}			
				ArrayList<BaseVertex> config1 = new ArrayList<BaseVertex>(config);			
				if(!config1.isEmpty() && config1.contains(vrt)){
					//check if the last vertex is the same as vert
					if(config1.get(config1.size()-1).get_id() == vrt.get_id()){
						config1.add(vrt);
						generateConfiguration(places,counter-1,nfv_nodes,config1,configs);
					}
					else{
						config1.clear();
						continue;
					}
				}else{
					config1.add(vrt);
					generateConfiguration(places,counter-1,nfv_nodes,config1,configs);	
				}
			}
		 }else{
			   //add the configuration to the list of configurations
		       configs.add(config);			      
		 }
	}
	
	public static void runHeuristic(List<Integer> scUsed, Map<Integer, ArrayList<TrafficNodes>> serviceChainTN, 
			Map<Integer, ArrayList<ArrayList<BaseVertex>>> serviceChainConfiguration,  Map<Integer, ArrayList<NewIngEgConfigs>> configPerServiceChain,
			Map<Integer, ServiceChain> ChainSet, HashMap<NodePair, List<Path>> sdpaths,  Map<TrafficNodes, ArrayList<NewIngEgConfigs>> OriginalConfigs ){
		  	  //check if all traffic nodes with service chain have path for this configuration
		  	  //if not repeat then generate another configuration
			  for(int scID : scUsed){
				  //get the traffic nodes using the service chain ID
				  ArrayList<TrafficNodes> scTN = serviceChainTN.get(scID);
				  //get the configurations associated with a service chain
				  ArrayList<ArrayList<BaseVertex>> scConfigs = serviceChainConfiguration.get(scID);
				  //get the service chain associated with a ID
				  ServiceChain sc = ChainSet.get(scID);	  
				  //check if configuration is used by all service chains or not	
				  int configUsedByAllTN = 0;
				  //iterate through the service chain configurations
				  //check if path exists for the current configuration in all the traffic nodes		
				  for(ArrayList<BaseVertex> scConfig : scConfigs){
									  Map<TrafficNodes, NewIngEgConfigs> tempConfigs = new HashMap<TrafficNodes, NewIngEgConfigs>();
									  //print out the number of usable configurations
									  if(configUsedByAllTN != 0){
										  System.out.println("####### Service Chain : " + scID + "; No. of Usable Configurations : " + configUsedByAllTN + " ########");
									  }
									  //Check if configuration is being used by all Traffic Nodes 
									  if(configUsedByAllTN < InputConstants.configCount){						  
												  //print out the configuration being checked
												  System.out.print("Configuration being checked : (");
												  for(BaseVertex vrt : scConfig){
													  System.out.print(vrt.get_id()+",");
												  }
												  System.out.println(")");
												  //iterate through the list of traffic nodes
												  for(TrafficNodes tn : scTN){
																	System.out.println("\tRoute for traffic pair " + tn.v1.get_id() + " -> " + tn.v2.get_id() + " ? ");
																	//get k-shortest paths for the traffic nodes
																	NodePair sdPair = new NodePair(tn.v1,tn.v2);
															 		List<Path> routes = sdpaths.get(sdPair);
															 		//check if the path list is empty
															 		if(routes.isEmpty()){
															 			System.out.println("ROUTES ARE EMPTY!");
															 		}
															 		//flag variable for when a route is found
															 		boolean routeFound = false;
													 		  		for(Path route : routes){ 
															 		  			if(routeFound){
															 	    				System.out.println("\t\tRoute found, so break ");
															 	    				//route has been of the traffic node and the configuration
															 	    				break;	 	    				
															 	    			}
															 		  			//route contains all the nodes
															 	    			if(route.get_vertices().containsAll( scConfig ) ){ 		 				
															 	    				int node_order = 0;
															 	    				boolean order_check = true;
															 	    				//check the order of the variables
															 	    				for( BaseVertex vrt : scConfig ){	
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
													//		 	    					System.out.println("###########  Path passes the order check  ###########");
															 	    					//route has been found!
															 	    					routeFound = true;
															 	    					if(routeFound){
															 	    						System.out.println("\t\tFOUND !!!");
															 	    					} 	    					
															 	    					//make the ingress-egress configuration object
															 	    					BaseVertex ingressNode = scConfig.get(0);
															 	    					//if service chain size is greater than 2
															 	    					if( sc.chain_size > 2){
																 	    					BaseVertex egressNode = scConfig.get(scConfig.size()-1);		 	    				 	    				
																 	    					//add details to the configuration object
																 	    					ArrayList<BaseVertex> internalConfig = new ArrayList<BaseVertex>(scConfig);
																 	    					//remove the ingress node
																 	    					internalConfig.remove(0);
																 	    					//remove the egress node
																 	    					internalConfig.remove(internalConfig.size()-1);
																 	    					//list of paths using the configuration
																 	    					ArrayList<Path> configRoutes = new ArrayList<Path>();
																 	    					//add the path to the list of paths
																 	    					configRoutes.add(route);
																 	    					//add internal configuration to ingress egress configuration
																 	    					NewIngEgConfigs ingegconfig = new NewIngEgConfigs(new NodePair(ingressNode,egressNode),new Configuration(internalConfig, configRoutes));
																 	    					//add configuration to the map
																 	    					tempConfigs.put(tn, ingegconfig);
															 	    					}else if(sc.chain_size == 2){//if the service chain size equals 2
															 	    						BaseVertex egressNode = scConfig.get(scConfig.size()-1);	 	    						
																 	    					//add details to the configuration object
																 	    					ArrayList<BaseVertex> internalConfig = new ArrayList<BaseVertex>();
																 	    					//list of paths using the configuration
																 	    					ArrayList<Path> configRoutes = new ArrayList<Path>();
																 	    					//add the path to the list of paths
																 	    					configRoutes.add(route);
																 	    					//add internal configuration to ingress egress configuration
																 	    					NewIngEgConfigs ingegconfig = new NewIngEgConfigs(new NodePair(ingressNode,egressNode),new Configuration(internalConfig, configRoutes));
																 	    					//add configuration to the map
																 	    					tempConfigs.put(tn, ingegconfig);
															 	    					}else{//if service chain size is 1	 	    						
																 	    					//add details to the configuration object
																 	    					ArrayList<BaseVertex> internalConfig = new ArrayList<BaseVertex>();
																 	    					//list of paths using the configuration
																 	    					ArrayList<Path> configRoutes = new ArrayList<Path>();
																 	    					//add the path to the list of paths
																 	    					configRoutes.add(route);
																 	    					//add internal configuration to ingress egress configuration
																 	    					NewIngEgConfigs ingegconfig = new NewIngEgConfigs(new NodePair(ingressNode,ingressNode),new Configuration(internalConfig, configRoutes));
																 	    					//add configuration to the map
																 	    					tempConfigs.put(tn, ingegconfig);
															 	    					}
													 	    				}else{
										//			 	    					System.out.println(" #### order check failed ##### ");
													 	    				}
													 	    			}
											 		  		}
													 		if(!routeFound){
													 			System.out.println("\t\tNo route found :( :( :(");
													 		}
										  }
									  
										  //flag to keep track of the number of empty routes that exist
										  int countEmptyRoutes = 0;
										  //check if all the traffic nodes for the particular service chain
										  //have a route added for the configuration
										  for(TrafficNodes tn : scTN){
											   NewIngEgConfigs configTN = tempConfigs.get(tn);
											   if( configTN == null ){// if configuration is not added
												  System.out.println("^^^^^ Configuration not accepted ^^^^");
												  countEmptyRoutes++;//increment the counter  
												  break;
											   }else if(configTN.cfg.config_routes.isEmpty()){//if path does not exist
												   countEmptyRoutes++;//increment the counter  
											   }
											  
										  }										 
										  System.out.println("Empty route counter : " + countEmptyRoutes);
										  
										  //if configuration not used by all the traffic nodes
					//					  if(countEmptyRoutes != 0){
					//						  OriginalConfigs.clear();
					//					  }
										  //if configuration used by all traffic nodes
										  //and the tempConfigs for that configuration is not empty 
										  if(countEmptyRoutes == 0 && !tempConfigs.isEmpty()){	
											  //add the configuration to the list of accepted configurations
											  ArrayList<NewIngEgConfigs> configForSC = configPerServiceChain.get(scID);
											  //if empty create a new arraylist
											  if(configForSC == null){
												  configForSC = new ArrayList<NewIngEgConfigs>();
											  }
											  boolean configInserted = false;
											  //add the ingress egress configuration to OriginalConfigs
											  for(Map.Entry<TrafficNodes, NewIngEgConfigs> entry : tempConfigs.entrySet()){
												  //retrieve the list of configurations for a particular traffic node
												  ArrayList<NewIngEgConfigs> listConfigs = OriginalConfigs.get(entry.getKey());
												  //create the new list 
												  if(listConfigs == null){
													  listConfigs = new ArrayList<NewIngEgConfigs>();
												  }
												  //add the configuration to the list
												  listConfigs.add(entry.getValue());
												  //put this list back in the Map
												  OriginalConfigs.put(entry.getKey(),listConfigs);
												  //put the config in the list of configurations
												  if(!configInserted){
													  //change the flag to true
													  configInserted = true;
													  //create a new configuration
													  Configuration tempConfig = new Configuration(entry.getValue().cfg.config);
													  //create a new object
													  NewIngEgConfigs tmp = new NewIngEgConfigs(entry.getValue().ingeg, tempConfig);									
													  //add the tmp configuration
													  configForSC.add(tmp);									  
													  System.out.println("##### Configuration added for SC : " + scID + " ########");
													  //add the list of configurations to the HashMap
													  configPerServiceChain.put(scID, configForSC);
												  }
											  }
											  configUsedByAllTN++;
											
											 
										  }
									  }else{
										  break;
									  }
				  }
			  } 		 
	}

}
