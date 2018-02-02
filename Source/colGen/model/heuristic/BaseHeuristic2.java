package colGen.model.heuristic;

import ilog.concert.IloNumVar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.preprocess.PreProcVer1;
import colGen.model.preprocess.placeNFVI;
import colGen.model.preprocess.preProcFunctions;
import colGen.model.trafficGen.TrafficGenerator;
import colGen.model.ver1.MpVarY;
import colGen.model.ver1.MpVarZ;
import colGen.model.ver1.NewIngEgConfigs;
import colGen.model.ver1.PpVarA;
import colGen.model.ver1.PpVarB;
import colGen.model.ver2.Pp2VarDelta;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class BaseHeuristic2 {	
	
	    //the details of configuration and path used by each traffic pair
		public static class SdDetails{			
			public Set<MpVarY> routeToFirstVNF;
			public Set<MpVarY> routeFromLastVNF;
			public HuerVarZ conFiguration;
			
			public SdDetails(Set<MpVarY> routeToFirstVNF, Set<MpVarY> routeFromLastVNF, HuerVarZ conFiguration){
				this.routeToFirstVNF = new HashSet<MpVarY>(routeToFirstVNF);
				this.routeFromLastVNF = new HashSet<MpVarY>(routeFromLastVNF);
				this.conFiguration = conFiguration;
			}			
		}
	
		//generate all possible configurations
		//list of service chain Id's used		
		public static Map<Integer,ArrayList<HuerVarZ>> generateAllHueVarZConfigurations(List<Integer> scUsed, Map<Integer,ServiceChain> ChainSet, 
				ArrayList<BaseVertex> nfviNodes, HashMap<NodePair,List<Path>> sdpaths, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN, 
				Map<TrafficNodes,ArrayList<HuerVarZ>> configsPerSD){
			//all configurations
			Map<Integer,ArrayList<HuerVarZ>> allConfigs = new HashMap<Integer,ArrayList<HuerVarZ>>();			
			//iterate through the scIDs
			for(int scID : scUsed){
				//find the service chain size
				int chainSize = ChainSet.get(scID).chain_size;		
				//all configurations for this service chain
				ArrayList<HuerVarZ> configsForScId = new ArrayList<HuerVarZ>();
				//generate all possible placements
				ArrayList<ArrayList<BaseVertex>> placements = new ArrayList<ArrayList<BaseVertex>>();
				//temporary placeholder for a placement
				ArrayList<BaseVertex> config = new ArrayList<BaseVertex>();
				//generate the placements
				generateAllPlacements(nfviNodes.size(), chainSize, chainSize, nfviNodes, config, placements);
				//total placements generated
				System.out.println("Total placements generated: " + placements.size());
				System.out.println("Total number of traffic flows: " + serviceChainTN.get(scID).size());
				//add routes for each placement
				for(ArrayList<BaseVertex> plcmnt : placements){
					//find the route for the placement
					Set<PpVarB> CollVarB = generateRouteForPlacement(scID, sdpaths, plcmnt);
					//make heuristic configuration
					HuerVarZ hueVarZ = new HuerVarZ();
					//update service chain ID //add B objects //make A object and add them //update configuration description //add to list of configurations
					hueVarZ.updateSCId(scID);					
					hueVarZ.addBVars(CollVarB);	
					String configDsc = "";
					for(int f_seq=0 ; f_seq < plcmnt.size() ; f_seq++){
						BaseVertex nfviNode = plcmnt.get(f_seq);
						PpVarA varA = new PpVarA(nfviNode,f_seq);
						hueVarZ.addAVar(varA);
						configDsc = configDsc + "_" + nfviNode.get_id();
						//keep track of the first and last VNF NFVI node
						if(f_seq==0){
							hueVarZ.updateFirstVNF(nfviNode);
						}
						if(f_seq==plcmnt.size()-1){
							hueVarZ.updateLastVNF(nfviNode);
						}
					}
					hueVarZ.updateConfigDesc(configDsc);
					//add the configuration to list of configurations
					configsForScId.add(hueVarZ);
				}
				//total number of configurations generated
				System.out.println("Total number of configurations generated: " + configsForScId.size());
				//System.out.println("Executing this!");
				//List of traffic nodes for scId
				ArrayList<TrafficNodes> flowsForScId = serviceChainTN.get(scID);
				//get the list of all possible groups of (s,d) pairs
				HashSet<HashSet<TrafficNodes>> allFlowGroups= new HashSet<HashSet<TrafficNodes>>();
				//generate all possible flow groups
				generateAllFlowGroups(flowsForScId.size(),flowsForScId,allFlowGroups);
				//system total number of groups
				System.out.println("All Flow Groups: " + allFlowGroups.size());
				//System.out.println("Number of flows: " + flowsForScId.size());
				//System.out.println("Executing this!");
				//creating Delta variables
				HashSet<HashSet<Pp2VarDelta>> allVarDGroups = new HashSet<HashSet<Pp2VarDelta>>();
				//iterate through groups of traffic pairs
				for(HashSet<TrafficNodes> flowGroup : allFlowGroups){
					//create new groups of var delta
					HashSet<Pp2VarDelta> varDGroup = new HashSet<Pp2VarDelta>();
					//create the var delta group
					for(TrafficNodes flow : flowGroup){
						varDGroup.add(new Pp2VarDelta(flow));
					}
					//all the list of var D groups
					allVarDGroups.add(varDGroup);
				}
				//total number of var D groups
				System.out.println("All varD Groups: " + allVarDGroups.size());
				//all configurations for this service chain
				ArrayList<HuerVarZ> allConfigsForScId = new ArrayList<HuerVarZ>();
				//initialize configuration number
				int configNum = 0;
				//iterate through the configurations
				for(HuerVarZ element : configsForScId){
					//iterate through groups of traffic pairs
					for(HashSet<Pp2VarDelta> varDGroup : allVarDGroups){
						//create a final config
						HuerVarZ finConfig = new HuerVarZ(element,varDGroup);						
						//update the configuration number //increment it
						finConfig.cgConfig = configNum;
						configNum++;
						//add to the list of configurations
						allConfigsForScId.add(finConfig);
						//add the configuration to the sd pair lists
						for(Pp2VarDelta varD : varDGroup){							
							//if the list of configurations for a (s,d) pair have been added
							if(configsPerSD.get(varD.sd) != null){
								//add the configuration
								configsPerSD.get(varD.sd).add(finConfig);
							}else{
								ArrayList<HuerVarZ> listOfConfigs = new ArrayList<HuerVarZ>();
								//add the configuration to the list
								listOfConfigs.add(finConfig);
								//put the list of configurations in the map
								configsPerSD.put(varD.sd, listOfConfigs);
							}
						}
					}
				}
				//assign the total number of configurations
				MpVarZ.totalNewConfigs = configNum;
				//total number of configurations generated
				System.out.println("Total number of configurations generated: " + allConfigsForScId.size());
				//update the map
				allConfigs.put(scID, allConfigsForScId);
			}
			for(int scID : scUsed){
				System.out.println("Total number of configurations generated: " + allConfigs.get(scID).size());
			}
			return allConfigs;			
		}
		
		//generate all possible flow groups
		public static void generateAllFlowGroups(int flowCount, ArrayList<TrafficNodes> flows,  HashSet<HashSet<TrafficNodes>> flowGroups){
			//determine the group size
			for(int groupSize=1; groupSize<=flowCount; groupSize++){
				//System.out.println("Group Size = " + groupSize);
				//create flowGroup
				HashSet<TrafficNodes> flowGroup = new HashSet<TrafficNodes>();
				//generate all possible flow groups of given size
				generateAllFlowGroupsForGivenGroupSize(flowCount, groupSize, groupSize, flows, flowGroup, flowGroups);
				//System.out.println("All Flow Groups: " + flowGroups.size());
			}
		}
		
		//generate all possible flow groups of given size
		public static void generateAllFlowGroupsForGivenGroupSize(int flowCount, int groupSize, int counter,  ArrayList<TrafficNodes> flows, 
				HashSet<TrafficNodes> flowGroup, HashSet<HashSet<TrafficNodes>> flowGroups){
			 //check if iterations required are over
//			 if( (counter!=0) && (!flows.isEmpty()) ){
			 if(counter!=0){
				//vertex to the configuration
				for(TrafficNodes flow : flows){				 
					//instantiate new object before the first iteration
					if(counter==groupSize){
					    flowGroup = new HashSet<TrafficNodes>();				    
					}
					//instantiate flow Group 
					HashSet<TrafficNodes> flowGroup1 = new HashSet<TrafficNodes>(flowGroup);
					//add the flow and remove it from the global list
					flowGroup1.add(flow);
					//create a new list of flows and remove flow from it
					ArrayList<TrafficNodes> flows1 = new ArrayList<TrafficNodes>(flows);
					flows1.remove(flow);
					generateAllFlowGroupsForGivenGroupSize(flowCount, groupSize,counter-1,flows1,flowGroup1,flowGroups);
				}
			 }else{				
				   //add flow to list of flows
			       flowGroups.add(flowGroup);	
			       //print out the flow group
			       /*System.out.print("\t");
			       for(TrafficNodes flow: flowGroup){
			    	   System.out.print(flow.toString() + " ; ");
			       }
			       System.out.println("");*/
			 }
		}
		
		//generate all possible placements
		public static void generateAllPlacements(int placementCount, int places, int counter, ArrayList<BaseVertex> nfv_nodes, 
				ArrayList<BaseVertex> config, ArrayList<ArrayList<BaseVertex>> configs){
			 //check if iterations required are over
			 if( counter!=0 ){
				//vertex to the configuration
				for(BaseVertex vrt : nfv_nodes){
					//instantiate new object before the first iteration
					if(counter==places){
					    config = new ArrayList<BaseVertex>();				    
					}
					//instantiate config1 
					ArrayList<BaseVertex> config1 = new ArrayList<BaseVertex>(config);
					//add a vertex and move forward
					config1.add(vrt);
					generateAllPlacements(placementCount, places,counter-1,nfv_nodes,config1,configs);

				}
			 }else{				
				   //add the configuration to the list of configurations
			       configs.add(config);
			       //print the placement
			       //printPlacement(config);
			 }
		}
		
		
	
		//generate configuration
		//this configuration generation does not use ingress egress nodes
		public static void generatePlacement(boolean allPossible, int placementCount, int places, int counter, ArrayList<BaseVertex> nfv_nodes, 
				ArrayList<BaseVertex> config, ArrayList<ArrayList<BaseVertex>> configs){
			 //check if iterations required are over
			 if( counter!=0 ){
				//vertex to the configuration
				for(BaseVertex vrt : nfv_nodes){
					//instantiate new object before the first iteration
					if(counter==places){
					    config = new ArrayList<BaseVertex>();				    
					}			
					ArrayList<BaseVertex> config1 = new ArrayList<BaseVertex>(config);			
					if(!config1.isEmpty() && config1.contains(vrt)){
						//check if the last vertex is the same as vert
						if(config1.get(config1.size()-1).get_id() == vrt.get_id()){
							config1.add(vrt);
							generatePlacement(allPossible, placementCount, places,counter-1,nfv_nodes,config1,configs);
						}
						else{
							config1.clear();
							continue;
						}
					}else{
						if(allPossible || (configs.size() < placementCount)){
							config1.add(vrt);
							generatePlacement(allPossible, placementCount,places,counter-1,nfv_nodes,config1,configs);	
						}
					}
				}
			 }else{				
				   //add the configuration to the list of configurations
			       configs.add(config);
			       //print the placement
//			       printPlacement(config);
			 }
		}
		
		//generate repeating nodes as placements
		public static void genPlaceRepeat(int counter, ArrayList<BaseVertex> nfvNodes, ArrayList<ArrayList<BaseVertex>> configs){
			for(BaseVertex vrt : nfvNodes){
				ArrayList<BaseVertex> config = new ArrayList<BaseVertex>();
				for(int pInd=0; pInd<counter; pInd++){
					config.add(vrt);
				}
				configs.add(config);
			}
		}
		
		
		//print out the placement
		public static void printPlacement(ArrayList<BaseVertex> placement){
			for(BaseVertex vrt : placement){
				System.out.print("_"+vrt.get_id());
			}
			System.out.println();
		}	
		
		
		//Find shortest path from 1st VNF placement to Nth VNF placement through the set of VNFs of the service chain
		public static Set<PpVarB> generateRouteForPlacement(int scID, HashMap<NodePair,List<Path>> sdpaths, 
				ArrayList<BaseVertex> placement){	
			Set<PpVarB> varBForPlacement = new HashSet<PpVarB>();		
			for(int pInd=0; pInd<placement.size()-1; pInd++){
				BaseVertex src = placement.get(pInd);
				BaseVertex sink = placement.get(pInd+1);				
				if(src.get_id()!=sink.get_id()){
					Path pFromSrcToSink = sdpaths.get(new NodePair(src,sink)).get(0);				
					for(int vrtInd=0; vrtInd<pFromSrcToSink.get_vertices().size()-1; vrtInd++){
						BaseVertex srcL = pFromSrcToSink.get_vertices().get(vrtInd);
						BaseVertex tarL = pFromSrcToSink.get_vertices().get(vrtInd+1);
						PpVarB tempVarB = new PpVarB(scID,pInd,pInd+1,srcL,tarL);
						//add to set
						varBForPlacement.add(tempVarB);
					}
				}
				
			}	
			return varBForPlacement;
		}
		
		//Find shortest path from source to 1st VNF
		public static Set<MpVarY> routeToFirstVNF(TrafficNodes sd, HashMap<NodePair,List<Path>> sdpaths, 
				BaseVertex source, BaseVertex firstVNF){	
			Set<MpVarY> varYToFirstVNF = new HashSet<MpVarY>();
			if(source.get_id()!=firstVNF.get_id()){
				Path pFromSrcToSink = sdpaths.get(new NodePair(source,firstVNF)).get(0);
				for(int vrtInd=0; vrtInd<pFromSrcToSink.get_vertices().size()-1; vrtInd++){
					BaseVertex srcL = pFromSrcToSink.get_vertices().get(vrtInd);
					BaseVertex tarL = pFromSrcToSink.get_vertices().get(vrtInd+1);
					MpVarY tempY = new MpVarY(sd,0, srcL, tarL);
					varYToFirstVNF.add(tempY);
				}
			}
			return varYToFirstVNF;
		}
		
		//Find shortest path from last VNF to destination
		public static Set<MpVarY> routeFromLastVNF(int lastVNFSeq, TrafficNodes sd, HashMap<NodePair, 
				List<Path>> sdpaths, BaseVertex lastVNF, BaseVertex destination){	
			Set<MpVarY> varYFromLastVNF = new HashSet<MpVarY>();
			if(lastVNF.get_id()!=destination.get_id()){
				Path pFromSrcToSink = sdpaths.get(new NodePair(lastVNF,destination)).get(0);
				for(int vrtInd=0; vrtInd<pFromSrcToSink.get_vertices().size()-1; vrtInd++){
					BaseVertex srcL = pFromSrcToSink.get_vertices().get(vrtInd);
					BaseVertex tarL = pFromSrcToSink.get_vertices().get(vrtInd+1);
					MpVarY tempY = new MpVarY(sd,lastVNFSeq,srcL,tarL);
					varYFromLastVNF.add(tempY);
				}
			}
			return varYFromLastVNF;
		}		
		
		//Find smallest value (s,d) pairs for each configuration
		public static Map<Integer,ArrayList<HuerVarZ>> clusteringBasedOnPath(boolean allPossible, int placementCount,HashMap<NodePair,List<Path>> sdpaths, List<Integer> scUsed, 
				Map<Integer,ServiceChain> ChainSet, ArrayList<BaseVertex> nfv_nodes, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN, Map<TrafficNodes,SdDetails> configPerSD){			
			//Get placements for each service chain
			Map<Integer, ArrayList<ArrayList<BaseVertex>>> serviceChainConfiguration = new HashMap<Integer, ArrayList<ArrayList<BaseVertex>>>();			
			//int total config count
			int totalConfigCount = 0;
			for(int scID : scUsed){
				  //select the service chain from the set of chains to be deployed
				  ServiceChain sc = ChainSet.get(scID);
				  ArrayList<BaseVertex> plcmnt = new ArrayList<BaseVertex>();
				  ArrayList<ArrayList<BaseVertex>> plcmnts = new ArrayList<ArrayList<BaseVertex>>();
				  //generate the configurations
//				  generatePlacement(allPossible, placementCount, sc.getChainSize(), sc.getChainSize(), nfv_nodes, plcmnt, plcmnts);
				  genPlaceRepeat(sc.getChainSize(), nfv_nodes, plcmnts);
				  //add these configuration to the Map
				  serviceChainConfiguration.put(scID, plcmnts);
			}
			//print out the configurations that are generated
			System.out.println("Printing out the configurations!");
			preProcFunctions.printConfigForSC(serviceChainConfiguration, scUsed);
			System.out.println("Computing the routes for each SD and the best configuration for each SD");
			//Which configuration is best for which service chain? //List of configurations		
			Map<Integer,ArrayList<HuerVarZ>> configsPerSC = new HashMap<Integer,ArrayList<HuerVarZ>>();
			for(int scID : scUsed){
				int lastVNFSeq = ChainSet.get(scID).chain_size-1; 
				//make configuration object for each placement
				Map<Integer,HuerVarZ> configList = new HashMap<Integer,HuerVarZ>();			
				for(ArrayList<BaseVertex> plcmnt : serviceChainConfiguration.get(scID)){
					//find the route for the placement
					Set<PpVarB> CollVarB = generateRouteForPlacement(scID, sdpaths, plcmnt);
					//make heuristic configuration
					HuerVarZ hueVarZ = new HuerVarZ();
					//update service chain ID //add B objects //make A object and add them //update configuration description //add to list of configurations
					hueVarZ.updateSCId(scID);					
					hueVarZ.addBVars(CollVarB);	
					String configDsc = "";
					for(int f_seq=0 ; f_seq < plcmnt.size() ; f_seq++){
						BaseVertex nfviNode = plcmnt.get(f_seq);
						PpVarA varA = new PpVarA(nfviNode,f_seq);
						hueVarZ.addAVar(varA);
						configDsc = configDsc + "_" + nfviNode.get_id();
						//keep track of the first and last VNF NFVI node
						if(f_seq==0){
							hueVarZ.updateFirstVNF(nfviNode);
						}
						if(f_seq==plcmnt.size()-1){
							hueVarZ.updateLastVNF(nfviNode);
						}
					}
					hueVarZ.updateConfigDesc(configDsc);
					configList.put(hueVarZ.cgConfig,hueVarZ);
				}				
				for(TrafficNodes sd : serviceChainTN.get(scID)){
					Set<MpVarY> routeToFirstVNF = new HashSet<MpVarY>();
					Set<MpVarY> routeFromLastVNF = new HashSet<MpVarY>();
					int lenOfPath = 0;
					int configNum = 0;
					//find optimal configuration for SD 
					for(HuerVarZ config : configList.values()){
						Set<MpVarY> tempRouteToFirstVNF = routeToFirstVNF(sd,sdpaths,sd.v1,config.firstVNF);
						Set<MpVarY> tempRouteFromLastVNF = routeFromLastVNF(lastVNFSeq,sd,sdpaths,config.lastVNF,sd.v2);
						int tempLenOfPath = tempRouteToFirstVNF.size() + tempRouteFromLastVNF.size() + config.BVarSet.size();
						//Initial routes are empty
						if( routeToFirstVNF.isEmpty() && routeFromLastVNF.isEmpty() ){
							routeToFirstVNF = tempRouteToFirstVNF;
							routeFromLastVNF = tempRouteFromLastVNF;
							lenOfPath = routeToFirstVNF.size() + routeFromLastVNF.size() + config.BVarSet.size();
							configNum = config.cgConfig;									
						}
						//Path length comparison
						if( tempLenOfPath < lenOfPath ){							
							routeToFirstVNF = tempRouteToFirstVNF;
							routeFromLastVNF = tempRouteFromLastVNF;
							lenOfPath = routeToFirstVNF.size() + routeFromLastVNF.size();
							configNum = config.cgConfig;
						}
					}
					//select the configuration //update its delta set
					HuerVarZ selectConfigForSD = configList.get(configNum);
					selectConfigForSD.addDeltaVar(sd);	
					//add the configuration to object
					configPerSD.put(sd, new SdDetails(routeToFirstVNF, routeFromLastVNF, selectConfigForSD));
				}
				//only add the configurations that have a non-empty deltaSet //update configuration number
				ArrayList<HuerVarZ> acceptedConfigs = new ArrayList<HuerVarZ>();				
				for(HuerVarZ config : configList.values()){
					if(!config.DeltaVarSet.isEmpty()){
						totalConfigCount++;
						config.cgConfig = totalConfigCount;
						acceptedConfigs.add(config);
					}
				}
				//update the total configuration count
				MpVarZ.totalNewConfigs = totalConfigCount;
				//update the configs per service chainList
				configsPerSC.put(scID, acceptedConfigs);
			}
			
			return configsPerSC;		
		}		
		
		//update scID after clustering has been done based on clustertingBasedOnPath()
		public static Map<Integer,ArrayList<TrafficNodes>> scIdBasedOnClustering(List<Integer> scUsed, Map<Integer,ArrayList<HuerVarZ>> configsPerSC, 
				ArrayList<Integer> scCopyUsed, Map<Integer,Integer> scCopyToSC,
				Map<Integer,ArrayList<Integer>> scCopies, List<TrafficNodes> pair_list){
			  //create the new data structures
			  for(int scID : scUsed){
				  int numOfConfigs = configsPerSC.get(scID).size();
				  ArrayList<HuerVarZ> configList = configsPerSC.get(scID);
				  ArrayList<Integer> scCopyList = new ArrayList<Integer>();
				  for(int scCpy=0; scCpy<numOfConfigs; scCpy++){
					  int scCopyID = 100*scID + scCpy;
					  //add sc copy to list of copies
					  scCopyList.add(scCopyID);
					  scCopyUsed.add(scCopyID);
					  //add service chain per SC copy  //update service chain ID for configuration and (s,d) pairs
					  HuerVarZ config = configList.get(scCpy);
					  config.updateSCId(scCopyID);
					  for(Pp2VarDelta varD : config.DeltaVarSet){
						varD.sd.updateChainIndex(scCopyID);  
					  }
//					  configForSCcopy.put(scCopyID, config);
					  scCopyToSC.put(scCopyID,scID);
				  }
				  scCopies.put(scID, scCopyList);
			  }
			  //clean out any (s,d) pair repetitions
			  for(int scID : scUsed){
				 for(int scCopyID : scCopies.get(scID)){
//					 HuerVarZ config = configForSCcopy.get(scCopyID);
					 for(HuerVarZ config : configsPerSC.get(scID)){ 
	    	    		if(config.sCiD == scCopyID){
	    	    			 Iterator<Pp2VarDelta> iterator = config.DeltaVarSet.iterator();
							 while (iterator.hasNext()) {
								 Pp2VarDelta varD = iterator.next();
								 if(varD.sd.chain_index != scCopyID){
									 config.DeltaVarSet.remove(varD);
								 }
							 }
	    	    		}
					 }
				 }				 
			  }
			  //create the list of traffic nodes by service chain again
			  return preProcFunctions.sdPairsforServiceChain(scCopyUsed, pair_list);			
		}
		
		//when there is no clustering, but the appropriate settings have to be made
		public static void noScIdUpdate(List<Integer> scUsed, List<Integer> scCopyUsed, Map<Integer,ArrayList<HuerVarZ>> configsPerSC,
				Map<Integer,ArrayList<Integer>> scCopies, Map<Integer,Integer> scCopyToSC){
			  //create the new data structures
			  for(int scID : scUsed){			 
				  ArrayList<Integer> scCopyList = new ArrayList<Integer>();
				  //add sc copy to list of copies
				  scCopyUsed.add(scID);
				  scCopyList.add(scID);
				  scCopies.put(scID,scCopyList);
				  scCopyToSC.put(scID,scID);
			  }		 
		 }
		
		public static  Map<Integer,ArrayList<HuerVarZ>> singleConfigPerSC(HashMap<NodePair,List<Path>> sdpaths, List<Integer> scUsed, 
					Map<Integer, ServiceChain> ChainSet, ArrayList<BaseVertex> nfv_nodes, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN, Map<TrafficNodes,SdDetails> configPerSD){
			 	//Get placements for each service chain
				Map<Integer, ArrayList<ArrayList<BaseVertex>>> serviceChainConfiguration = new HashMap<Integer, ArrayList<ArrayList<BaseVertex>>>();			
				//int total config count
				int totalConfigCount = 0;
				for(int scID : scUsed){
					  //select the service chain from the set of chains to be deployed
					  ServiceChain sc = ChainSet.get(scID);
					  ArrayList<BaseVertex> plcmnt = new ArrayList<BaseVertex>();
					  ArrayList<ArrayList<BaseVertex>> plcmnts = new ArrayList<ArrayList<BaseVertex>>();					  
					  //select a single node
					  Random rand = new Random(System.currentTimeMillis() + scID);
					  ArrayList<BaseVertex> nfvNode = new ArrayList<BaseVertex>();
					  nfvNode.add(nfv_nodes.get(rand.nextInt(nfv_nodes.size())));
					  genPlaceRepeat(sc.getChainSize(), nfvNode, plcmnts);
					  /*for(BaseVertex vrt : nfv_nodes){
						  if(vrt.get_id() == 13){
							  for(int i=0;i<5;i++){
								  plcmnt.add(vrt);
							  }
						  }
					  }
					  //add to placements
					  plcmnts.add(plcmnt);*/
					  //add these configuration to the Map
					  serviceChainConfiguration.put(scID, plcmnts);
				}
				//print out the configurations that are generated
				System.out.println("Printing out the configurations!");
				preProcFunctions.printConfigForSC(serviceChainConfiguration, scUsed);
				System.out.println("Computing the routes for each SD and the best configuration for each SD");
				//Which configuration is best for which service chain? //List of configurations		
				Map<Integer,ArrayList<HuerVarZ>> configsPerSC = new HashMap<Integer,ArrayList<HuerVarZ>>();
				for(int scID : scUsed){
					int lastVNFSeq = ChainSet.get(scID).chain_size-1; 
					//make configuration object for each placement
					Map<Integer,HuerVarZ> configList = new HashMap<Integer,HuerVarZ>();			
					for(ArrayList<BaseVertex> plcmnt : serviceChainConfiguration.get(scID)){
						//find the route for the placement
						Set<PpVarB> CollVarB = generateRouteForPlacement(scID, sdpaths, plcmnt);
						//make heuristic configuration
						HuerVarZ hueVarZ = new HuerVarZ();
						//update service chain ID //add B objects //make A object and add them //update configuration description //add to list of configurations
						hueVarZ.updateSCId(scID);					
						hueVarZ.addBVars(CollVarB);	
						String configDsc = "";
						for(int f_seq=0 ; f_seq < plcmnt.size() ; f_seq++){
							BaseVertex nfviNode = plcmnt.get(f_seq);
							PpVarA varA = new PpVarA(nfviNode,f_seq);
							hueVarZ.addAVar(varA);
							configDsc = configDsc + "_" + nfviNode.get_id();
							//keep track of the first and last VNF NFVI node
							if(f_seq==0){
								hueVarZ.updateFirstVNF(nfviNode);
							}
							if(f_seq==plcmnt.size()-1){
								hueVarZ.updateLastVNF(nfviNode);
							}
						}
						hueVarZ.updateConfigDesc(configDsc);
						configList.put(hueVarZ.cgConfig,hueVarZ);
					}				
					for(TrafficNodes sd : serviceChainTN.get(scID)){
						Set<MpVarY> routeToFirstVNF = new HashSet<MpVarY>();
						Set<MpVarY> routeFromLastVNF = new HashSet<MpVarY>();
						int lenOfPath = 0;
						int configNum = 0;
						//find optimal configuration for SD 
						for(HuerVarZ config : configList.values()){
							Set<MpVarY> tempRouteToFirstVNF = routeToFirstVNF(sd,sdpaths,sd.v1,config.firstVNF);
							Set<MpVarY> tempRouteFromLastVNF = routeFromLastVNF(lastVNFSeq,sd,sdpaths,config.lastVNF,sd.v2);
							int tempLenOfPath = tempRouteToFirstVNF.size() + tempRouteFromLastVNF.size() + config.BVarSet.size();
							//Initial routes are empty
							if( routeToFirstVNF.isEmpty() && routeFromLastVNF.isEmpty() ){
								routeToFirstVNF = tempRouteToFirstVNF;
								routeFromLastVNF = tempRouteFromLastVNF;
								lenOfPath = routeToFirstVNF.size() + routeFromLastVNF.size() + config.BVarSet.size();
								configNum = config.cgConfig;									
							}
							//Path length comparison
							if( tempLenOfPath < lenOfPath ){							
								routeToFirstVNF = tempRouteToFirstVNF;
								routeFromLastVNF = tempRouteFromLastVNF;
								lenOfPath = routeToFirstVNF.size() + routeFromLastVNF.size();
								configNum = config.cgConfig;
							}
						}
						//select the configuration //update its delta set
						HuerVarZ selectConfigForSD = configList.get(configNum);
						selectConfigForSD.addDeltaVar(sd);	
						//add the configuration to object
						configPerSD.put(sd, new SdDetails(routeToFirstVNF, routeFromLastVNF, selectConfigForSD));
					}
					//only add the configurations that have a non-empty deltaSet //update configuration number
					ArrayList<HuerVarZ> acceptedConfigs = new ArrayList<HuerVarZ>();				
					for(HuerVarZ config : configList.values()){
						if(!config.DeltaVarSet.isEmpty()){
							totalConfigCount++;
							config.cgConfig = totalConfigCount;
							acceptedConfigs.add(config);
						}
					}
					//update the total configuration count
					MpVarZ.totalNewConfigs = totalConfigCount;
					//update the configs per service chainList
					configsPerSC.put(scID, acceptedConfigs);
				}
				
				return configsPerSC;
		}
		
		
		/*adjacency of source vertex becomes source cluster, adjacency of destination cluster becomes destination cluster*/
		public static void clusteringBasedOnAdj(Graph g, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN, List<Integer> scUsed, Map<Integer,ArrayList<Integer>> scCopies,
				 Map<Integer,Integer> scCopyToSC, ArrayList<Integer> scCopyUsed){
			 //iterate through the set of (s,d) pairs for a service chain
			 for(int scId : scUsed){				 
				 ArrayList<TrafficNodes> sdPairsPerSC = serviceChainTN.get(scId);
				 int clusterNum = 0;
				 ArrayList<Integer> idCopies = new ArrayList<Integer>();
				 while(!sdPairsPerSC.isEmpty()){
					 int scCopyId = 1000*scId + clusterNum;		
					 //add Id to Id copies
					 idCopies.add(scCopyId);
					 //keep track of service chain
					 scCopyToSC.put(scCopyId,scId);
					 //new list of service chains
					 scCopyUsed.add(scCopyId);
					 //choose a traffic pair (s,d)
					 TrafficNodes sdPick = sdPairsPerSC.get(0);
					 BaseVertex srcVrt = sdPick.v1;
					 BaseVertex destVrt = sdPick.v2;
					 //find adjacency list
					 Set<BaseVertex> srcAdj = new HashSet<BaseVertex>(g.get_adjacent_vertices(srcVrt));
					 //also add the srcVrt
					 srcAdj.add(srcVrt);
					 //find adjacency list
					 Set<BaseVertex> destAdj = new HashSet<BaseVertex>(g.get_adjacent_vertices(destVrt));
					 //also add the destination vertex
					 destAdj.add(destVrt);
					 //keep track of pairs in cluster
					 ArrayList<TrafficNodes> sdPairsInCluster = new ArrayList<TrafficNodes>();
					 //updateID of pick
					 sdPick.updateChainIndex(scCopyId);
					 //add pick to cluster
					 sdPairsInCluster.add(sdPick);
					 for(TrafficNodes sd : sdPairsPerSC){
						 if(srcAdj.contains(sd.v1)&&destAdj.contains(sd.v2)){
							 //update the service chain Id
							 sd.updateChainIndex(scCopyId);
							 //add sd pairs to cluster
							 sdPairsInCluster.add(sd);
						 }
					 }
					 //remove the pairs from the cluster
					 sdPairsPerSC.removeAll(sdPairsInCluster);					 
					 //increase the cluster number
					 clusterNum++;
				 }
				 //add the copyId's to the Id
				 scCopies.put(scId,idCopies);				 
			 }			 
		 }
		 
		public static Map<Integer,ArrayList<HuerVarZ>> singleConfigBasedOnAdj(List<Integer> scUsed, Map<Integer,ServiceChain> ChainSet, ArrayList<BaseVertex> nfviNodes,  
				ArrayList<Integer> scCopyUsed, Map<Integer,Integer> scCopyToSC, HashMap<NodePair,List<Path>> sdpaths, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN,
				Map<Integer,ArrayList<Integer>> scCopies,Map<TrafficNodes,SdDetails> configPerSD){
			//Get placements for each service chain
			Map<Integer, ArrayList<ArrayList<BaseVertex>>> serviceChainConfiguration = new HashMap<Integer, ArrayList<ArrayList<BaseVertex>>>();			
			//int total config count
			int totalConfigCount = 0;
			for(int scID : scUsed){
				  //select the service chain from the set of chains to be deployed
				  ServiceChain sc = ChainSet.get(scID);
				  ArrayList<BaseVertex> plcmnt = new ArrayList<BaseVertex>();
				  ArrayList<ArrayList<BaseVertex>> plcmnts = new ArrayList<ArrayList<BaseVertex>>();
				  //generate the configurations
//				  generatePlacement(allPossible, placementCount, sc.getChainSize(), sc.getChainSize(), nfv_nodes, plcmnt, plcmnts);
				  genPlaceRepeat(sc.getChainSize(), nfviNodes, plcmnts);
				  //add these configuration to the Map
				  serviceChainConfiguration.put(scID, plcmnts);
			}
			//print out the configurations that are generated
			System.out.println("Printing out the configurations!");
			preProcFunctions.printConfigForSC(serviceChainConfiguration, scUsed);
			System.out.println("Find the best configuration for each cluster!");
			//Which configuration is best for which service chain? //List of configurations		
			Map<Integer,ArrayList<HuerVarZ>> configsPerSC = new HashMap<Integer,ArrayList<HuerVarZ>>();					
			//iterate through service chains
			for(int scID : scUsed){
				//accepted configuration list per SC
				ArrayList<HuerVarZ> acceptedConfigs = new ArrayList<HuerVarZ>();
				//get the last VNF Seq
				int lastVNFSeq = ChainSet.get(scID).chain_size-1;
				//make configuration object for each placement
				Map<Integer,HuerVarZ> configList = new HashMap<Integer,HuerVarZ>();
				//iterate through placements
				//generate configurations for scID
				for(ArrayList<BaseVertex> plcmnt : serviceChainConfiguration.get(scID)){
					//find the route for the placement
					Set<PpVarB> CollVarB = generateRouteForPlacement(scID, sdpaths, plcmnt);
					//make heuristic configuration
					HuerVarZ hueVarZ = new HuerVarZ();
					//update service chain ID //add B objects //make A object and add them //update configuration description //add to list of configurations
					hueVarZ.updateSCId(scID);
					hueVarZ.addBVars(CollVarB);	
					String configDsc = "";
					for(int f_seq=0 ; f_seq < plcmnt.size() ; f_seq++){
						BaseVertex nfviNode = plcmnt.get(f_seq);
						PpVarA varA = new PpVarA(nfviNode,f_seq);
						hueVarZ.addAVar(varA);
						configDsc = configDsc + "_" + nfviNode.get_id();
						//keep track of the first and last VNF NFVI node
						if(f_seq==0){
							hueVarZ.updateFirstVNF(nfviNode);
						}
						if(f_seq==plcmnt.size()-1){
							hueVarZ.updateLastVNF(nfviNode);
						}
					}
					hueVarZ.updateConfigDesc(configDsc);
					configList.put(hueVarZ.cgConfig,hueVarZ);
				}
				//select a configuration randomly
				//for each cluster of scID
				for(int clusterID : scCopies.get(scID)){									
					//randomly select a configuration
					//for each cluster ID
					Random rnd = new Random();
					int selectIndex = rnd.nextInt(configList.values().size());
					HuerVarZ config = new HuerVarZ((HuerVarZ)configList.values().toArray()[selectIndex]);
					//update the service chain ID with clusterID
					config.updateSCId(clusterID);
					System.out.println("Service Chain ID: " + scID);
					System.out.println("Cluster ID: " + clusterID);
					//iterate through the list of (s,d) pairs
					for(TrafficNodes sd : serviceChainTN.get(clusterID)){
						Set<MpVarY> routeToFirstVNF = new HashSet<MpVarY>();
						Set<MpVarY> routeFromLastVNF = new HashSet<MpVarY>();
//						int lenOfPath = 0;
						int configNum = 0;						
						//find configuration for SD 						
						Set<MpVarY> tempRouteToFirstVNF = routeToFirstVNF(sd,sdpaths,sd.v1,config.firstVNF);
						Set<MpVarY> tempRouteFromLastVNF = routeFromLastVNF(lastVNFSeq,sd,sdpaths,config.lastVNF,sd.v2);
						//assign routes 
						routeToFirstVNF = tempRouteToFirstVNF;
						routeFromLastVNF = tempRouteFromLastVNF;						
						//assign configuration number
						configNum = config.cgConfig;
						/*int tempLenOfPath = tempRouteToFirstVNF.size() + tempRouteFromLastVNF.size() + config.BVarSet.size();
						//Initial routes are empty
						if( routeToFirstVNF.isEmpty() && routeFromLastVNF.isEmpty() ){
							routeToFirstVNF = tempRouteToFirstVNF;
							routeFromLastVNF = tempRouteFromLastVNF;
							lenOfPath = routeToFirstVNF.size() + routeFromLastVNF.size() + config.BVarSet.size();
							configNum = config.cgConfig;									
						}*/
						//Path length comparison
						/*if( tempLenOfPath < lenOfPath ){							
							routeToFirstVNF = tempRouteToFirstVNF;
							routeFromLastVNF = tempRouteFromLastVNF;
							lenOfPath = routeToFirstVNF.size() + routeFromLastVNF.size();
							configNum = config.cgConfig;
						}*/						
						//update its delta set					
						config.addDeltaVar(sd);	
						//add the configuration to object
						configPerSD.put(sd, new SdDetails(routeToFirstVNF, routeFromLastVNF, config));						
					}
					//update the config ID
					config.cgConfig = totalConfigCount;
					//increment the total configCount
					totalConfigCount++;					
					//update the configs per service chainList							
					acceptedConfigs.add(config);				
				}		
				//update the configurations per SC
				configsPerSC.put(scID, acceptedConfigs);
			}
			//update the total configuration count
			MpVarZ.totalNewConfigs = totalConfigCount;		
			return configsPerSC;
		}
		 
		
		
		
		/*public static void main(String[] args) throws Exception{			
			

		    //generate the graph object
		    Graph g = preProcFunctions.makeGraphObject();	  
		    //print the graph object
		    preProcFunctions.printGraph(g);
		    
		    //generate the routes for the traffic pairs
		    HashMap<NodePair, List<Path>> sdpaths = preProcFunctions.findRoutesForSDpairs(g);
		    
	 		//get the set of service chains
	 		Map<Integer, ServiceChain> ChainSet = preProcFunctions.setOfServiceChains();
	 		//print out the Set of Service Chains
	 		preProcFunctions.printServiceChains(ChainSet);
	 		
	 	    //SD pairs between which we desire traffic to be
			//Store each s-d pair
			//List<TrafficNodes> pair_list = preProcFunctions.setOfSDpairs(sdpaths);
	 		//Using Traffic Generator for generating sd pairs
	 		// List<TrafficNodes> pair_list = TrafficGenerator.getTrafficPairs(InputConstants.trafficLoad, g);
	 		//Generate traffic pairs for a single service chain
	 		List<TrafficNodes> pair_list = TrafficGenerator.getTrafficPairsForSC(InputConstants.trafficLoad, g);
	 		//List of the service chains to be deployed
	 		List<Integer> scUsed = preProcFunctions.serviceChainsUsed(pair_list);
			//print out the pair lists
			preProcFunctions.printSDpairs(pair_list, scUsed);		
			 
		    
			//total list of VNF available
			List<FuncPt> vnf_list = preProcFunctions.totalListOfVNFs();
			//VNFs used across the service chains deployed
			List<FuncPt> func_list = preProcFunctions.listOfVNFsUsed(vnf_list, ChainSet, scUsed);
			//List of service chains in which a VNF occurs
			Map<Integer, ArrayList<Integer>> funcInSC = preProcFunctions.vnfInSCs(scUsed, func_list, ChainSet);
			//print out the function list
			preProcFunctions.printListOfVNFsUsed(func_list);
			
			//traffic pairs for each service chain deployed	
			Map<Integer,ArrayList<TrafficNodes>> serviceChainTN = preProcFunctions.sdPairsforServiceChain(scUsed, pair_list);
		    //print out the traffic nodes available for each service chain
		    preProcFunctions.printTrafficNodesForServiceChains(scUsed, serviceChainTN);	
		    //total function count required
		    Map<Integer,Integer> funcCountMax = preProcFunctions.CountMaxVNF(ChainSet, funcInSC, serviceChainTN);
		    
		    //store the data values in a HashMap
		    //Map<SolKey, SolValue> dataValues = new HashMap<SolKey, SolValue>();
		    
		    //declare k DC node placements
		    List<ArrayList<Integer>> kDCSets;	   	    
	    	//get the k DC placements
	    	kDCSets = placeNFVI.generateLocationsForDC(g);
		    
		    //check if K DC subsets have been created?
		    if(kDCSets.isEmpty()){
		    	System.out.println("//######## No subsets of DC placements are created ########//");
		    	ArrayList<Integer> tokenFiller = new ArrayList<Integer>();
		    	kDCSets.add(tokenFiller);
		    }else{	    
			    //cycle through kDCSets and remove the DC node not required
			    Iterator<ArrayList<Integer>> itr = kDCSets.iterator();
			    while(itr.hasNext()){
			    	if( InputConstants.NOT_DC_SET.contains(itr.next().get(0)) ){
			    		itr.remove();
			    	}
			    }	    
			    //print out the K DC sets
			    System.out.println("Printing out ALTERED set of DC nodes!");
			    for(ArrayList<Integer> dcNode : kDCSets){
			    	System.out.println(dcNode.get(0)+",");
			    }
		    }
	     
//		    String outputFileName = "DC_" + InputConstants.numOfDCnodes + "_" + InputConstants.NFV_Strategy ;
//			//create the output stream		
//			FileOutputStream outStream1 = new FileOutputStream(outputFileName + "_byCoreCount.txt");
//			FileOutputStream outStream2 = new FileOutputStream(outputFileName + "_byTraffic.txt");
//			//create the output stream writer			
//			OutputStreamWriter osw1 = new OutputStreamWriter(outStream1);
//			OutputStreamWriter osw2 = new OutputStreamWriter(outStream2);
//			//file writer	
//			Writer wrt1 = new BufferedWriter(osw1);
//			Writer wrt2 = new BufferedWriter(osw2);
		    
		    
		    //for the various DC node placements
		    for(ArrayList<Integer> dcNodes : kDCSets){
		    		  //DC node placement
//		    		  ArrayList<Integer> dcNodes = kDCSets.get(0);
//		    		  ArrayList<Integer> dcNodes = new ArrayList<Integer>();	    		 	    		  
		    		  
					  //place the DC nodes
					  placeNFVI.placeDC(g, dcNodes);
					  //place the NFV nodes
					  placeNFVI.placeNFVPoP(g, dcNodes);
					    
					  //create the list of DC nodes
					  ArrayList<BaseVertex> dc_nodes = new ArrayList<BaseVertex>();
					  placeNFVI.makeDCList(g, dc_nodes);			
				 	  //create the list of NFV-capable nodes
					  ArrayList<BaseVertex> nfv_nodes = new ArrayList<BaseVertex>();
					  placeNFVI.makeNFVList(g, nfv_nodes);				 					
					  //create the list of NFVI nodes
					  //add the set of DC nodes to the set of nfv nodes
					  ArrayList<BaseVertex> nodesNFVI = new ArrayList<BaseVertex>();
					  //add the set of DC nodes
					  nodesNFVI.addAll(dc_nodes);				  
					  
					  //add the set of NFV nodes
					  nodesNFVI.addAll(nfv_nodes);
					  //print the nodes with NFV capability
					  placeNFVI.printNFVINodes(nodesNFVI);
						
					  //list of vertices without the NFV nodes
					  ArrayList<BaseVertex> vertex_list_without_nfvi_nodes = new ArrayList<BaseVertex>(g._vertex_list);
					  //assuming that the NFV and DC node sets are exclusive				 
					  vertex_list_without_nfvi_nodes.removeAll(nodesNFVI);		
					  
					  //split a single service chain into multiple service chains
					  Map<Integer,ArrayList<Integer>> scCopies = new HashMap<Integer,ArrayList<Integer>>();		
					  //new service to old service
					  Map<Integer,Integer> scCopyToSC = new HashMap<Integer,Integer>();  
					  //create list of service chains
					  ArrayList<Integer> scCopyUsed = new ArrayList<Integer>();
					  //cluster based on adjacency
					  clusteringBasedOnAdj(g, serviceChainTN, scUsed, scCopies, scCopyToSC, scCopyUsed);
					  //calculate traffic nodes per service chain once again
					  //traffic pairs for each service chain deployed	
					  serviceChainTN = preProcFunctions.sdPairsforServiceChain(scCopyUsed, pair_list);
					  //print out the traffic nodes available for each service chain
					  preProcFunctions.printTrafficNodesForServiceChains(scCopyUsed, serviceChainTN);	
					  
					  //valid configurations for each service chain //each (s,d) selects a valid configuration
					  Map<Integer,ArrayList<HuerVarZ>> configsPerSC = new HashMap<Integer,ArrayList<HuerVarZ>>();
					  Map<TrafficNodes,SdDetails> configPerSD = new HashMap<TrafficNodes,SdDetails>();
					  configsPerSC = singleConfigBasedOnAdj(scUsed, ChainSet, nodesNFVI, scCopyUsed, scCopyToSC, sdpaths, serviceChainTN, scCopies, configPerSD);
					  //print the configurations for each SC
					  preProcFunctions.printConfigsPerSCforBH2(scCopyUsed, configsPerSC);					  
					  //print the configuration for each (s,d)
					  preProcFunctions.printConfigForSD(configPerSD);	
		    }
		}*/

}
