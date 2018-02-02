package colGen.model.ver1;

//for using the concert API
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.analyze.CpuLinkCapacity;
import colGen.model.heuristic.BaseHeuristic2.SdDetails;
import colGen.model.heuristic.HuerVarZ;
import colGen.model.result.ReportResults;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;


public class CG {
	   
	    public static long cgRunTime; //in milliseconds
		public static long ilpRunTime; //in milliseconds	
		public static long totalTime; //in milliseconds
		public static double eoptimality ; //in terms of percentage
		public static double rmpLpValue; //in Mb
		public static double rmpIlpValue; //in Mb
		public static int totalConfigGenerated;
		public static Set<Integer> vnfPlacementNodes = new HashSet<Integer>();		
		public static Map<Integer, Double> trafficLoadPerNode = new TreeMap<Integer, Double>();
		public static Map<Integer, TreeMap<Integer, Double>> trafficLoadPerNodePerVNF = new TreeMap<Integer, TreeMap<Integer, Double>>();
		
		//get the traffic percentage for each node
		public static void calculateTrafficLoadPerNode(IloCplex masterProblem, Map<MpVarZ, IloNumVar> usedVarZ, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN){
			//System.out.println("calculateTrafficLoadPerNode() function has been run!");
			//iterate through the generated configurations
			try{
				for(Map.Entry<MpVarZ, IloNumVar> entryZ : usedVarZ.entrySet()){
					Double totalTraffic = 0.0;
					if(masterProblem.getValue(entryZ.getValue()) != 0){
						//calculate total traffic for Z ScId
						for(TrafficNodes tnWithSC : serviceChainTN.get(entryZ.getKey().sCiD) ){
							totalTraffic += tnWithSC.flow_traffic;
						}
						//System.out.println("Load Per Node: Total Traffic = " + totalTraffic);
						//get the locations of VNFs for this sevice chain
						//add traffic load to each of these locations
						String [] vnfNodes = entryZ.getKey().configDsc.split("_");
						HashSet<String> uniqueNodes = new HashSet<String>();
						for(int index=1; index < vnfNodes.length; index++ ){
							uniqueNodes.add(vnfNodes[index]);
						}
						for(String uniqueNode : uniqueNodes){
							int vrtId = Integer.valueOf(uniqueNode);
							//System.out.println("Vertex = " + vrtId);
							if( CG.trafficLoadPerNode.get(vrtId) != null){
								Double trafficLoad = totalTraffic + CG.trafficLoadPerNode.get(vrtId);
								CG.trafficLoadPerNode.put(vrtId, trafficLoad);
							}else{
								CG.trafficLoadPerNode.put(vrtId, totalTraffic);
							}
						}
					}
				}
			}catch(IloException exc){
				System.err.println("Catch CPLEX exception");
			}
		}
		
		//get the traffic percentage for each node per VNF
		public static void calculateTrafficLoadPerNodePerVNF(IloCplex masterProblem, Map<MpVarZ, IloNumVar> usedVarZ, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN,
				Map<Integer,Integer> scCopyToSC, Map<Integer,ServiceChain> ChainSet){
			//System.out.println("calculateTrafficLoadPerNodePerVNF() function has been run!");
			//iterate through the generated configurations
			try{
				for(Map.Entry<MpVarZ, IloNumVar> entryZ : usedVarZ.entrySet()){
					Double totalTraffic = 0.0;
					if(masterProblem.getValue(entryZ.getValue()) != 0){
						//calculate total traffic for Z ScId
						for(TrafficNodes tnWithSC : serviceChainTN.get(entryZ.getKey().sCiD) ){
							totalTraffic += tnWithSC.flow_traffic;
						}
						//actual service chain
						int scID = scCopyToSC.get(entryZ.getKey().sCiD);
						ServiceChain sc = ChainSet.get(scID);
						//get the locations of VNFs for this sevice chain
						//add traffic load to each of these locations
						String [] vnfNodes = entryZ.getKey().configDsc.split("_");
						for(int index=1; index < vnfNodes.length; index++ ){
							int vrtId = Integer.valueOf(vnfNodes[index]);
							int vnfId = sc.chain_seq.get(index-1);
							if( CG.trafficLoadPerNodePerVNF.get(vrtId) != null){
								TreeMap<Integer,Double> trafficLoadPerVNF = CG.trafficLoadPerNodePerVNF.get(vrtId);
								if(trafficLoadPerVNF.get(vnfId) != null){
									Double trafficLoad = totalTraffic + trafficLoadPerVNF.get(vnfId);
									trafficLoadPerVNF.put(vnfId, trafficLoad);
									CG.trafficLoadPerNodePerVNF.put(vrtId, trafficLoadPerVNF);
								}else{
									trafficLoadPerVNF.put(vnfId, totalTraffic);
									CG.trafficLoadPerNodePerVNF.put(vrtId, trafficLoadPerVNF);
								}
							}else{
								TreeMap<Integer,Double> trafficLoadPerVNF = new TreeMap<Integer,Double>();
								trafficLoadPerVNF.put(vnfId,totalTraffic);
								CG.trafficLoadPerNodePerVNF.put(vrtId, trafficLoadPerVNF);
							}
						}
					}
				}
			}catch(IloException exc){
				System.err.println("Catch CPLEX exception");
			}
		}
		
		//get the nodes where the VNF has been placed
		public static void getPlacementNodes(IloCplex master_problem, Map<MpVarH,IloNumVar> usedVarH)  throws IloException {
			  //values for variable X
		      for(Map.Entry<MpVarH, IloNumVar> entry : usedVarH.entrySet()){
		    	  if( Math.round(master_problem.getValue(entry.getValue())) != 0 ){
		    		  vnfPlacementNodes.add(entry.getKey().node.get_id());
		    	  }
		      }
		}
		
		//very basic run CG
		public static void runCG(boolean coreCstr, boolean capCstr, Map<BaseVertex,Double> cpuCoreCount, Map<NodePair,Double> linkCapacity,
				Graph g, Map<Integer,ServiceChain> ChainSet, List<TrafficNodes> pair_list, List<Integer> scUsed, List<FuncPt> vnf_list, 
				List<FuncPt> func_list, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN, ArrayList<BaseVertex> nfv_nodes, 
				ArrayList<BaseVertex> nodesNFVI, ArrayList<BaseVertex> vertex_list_without_nfvi_nodes, Map<Integer,ArrayList<Integer>> scCopies,  
				Map<Integer,Integer> scCopyToSC, Map<Integer,ArrayList<HuerVarZ>> configsPerSC, Map<TrafficNodes,SdDetails> configPerSD, 
				Map<Integer,Integer> CountMaxVNF, Map<Integer,Integer> replicaPerVNF, int kNodeCount) throws Exception{   
 	  	 
	    
				try{	
					     //clear vnfPlacementNodes everytime CG it is run
						 CG.vnfPlacementNodes.clear();
						 //clear the traffic load per node
						 CG.trafficLoadPerNode.clear();
						 //clear the traffic load per node per VNF
						 CG.trafficLoadPerNodePerVNF.clear();
					
				         ///################ PRICING PROBLEM ################///
						 //create the list of pricing problem objects
					     ArrayList<PricingProblem> pPList = new ArrayList<PricingProblem>();
//						 ArrayList<LazyPricingProblem2> pPList = new ArrayList<LazyPricingProblem2>();
					     //iterate through the list of service chains
					     for(int scID : scUsed){
					    	 for(int scCopyID : scCopies.get(scID)){
							 //create the pricing problem object		    
						     PricingProblem pPObject = new PricingProblem(coreCstr, capCstr, cpuCoreCount, linkCapacity, serviceChainTN, vnf_list, scCopyID, ChainSet, 
						    		 nodesNFVI, nfv_nodes, vertex_list_without_nfvi_nodes, g, scCopyToSC);
//							 LazyPricingProblem2 pPObject = new LazyPricingProblem2(InputConstants.coreCount, serviceChainTN, vnf_list, scID, ChainSet, nodesNFVI, nfv_nodes, vertex_list_without_nfvi_nodes, g);
						     //set numerical emphasis
						     //pPObject.pricing_problem.setParam(IloCplex.Param.Emphasis.Numerical, true);
						     //add the pricing problem object to the list
							 pPList.add(pPObject);
					    	 }
					     }	
					     System.out.println("####### Pricing Problems Generated! #########");
					    
						 ///################ MASTER PROBLEM ################///			
						 //create the master problem instance	
						 MasterProblem mP = new MasterProblem(coreCstr, capCstr, cpuCoreCount, linkCapacity, func_list, scUsed, serviceChainTN, configsPerSC, 
								 configPerSD, g, nodesNFVI, nfv_nodes, ChainSet, pair_list, vertex_list_without_nfvi_nodes, 
								 scCopies, scCopyToSC, CountMaxVNF, replicaPerVNF, kNodeCount);
						 System.out.println("######### Master Problem Generated! #########");
						
						 //set the display parameter
						 /*System.out.println("Simplex Iterations");
						 mP.master_problem.setParam(IloCplex.IntParam.SimDisplay, 2);*/
						 //set numerical emphasis
						 //mP.master_problem.setParam(IloCplex.Param.Emphasis.Numerical, true);
						 //set the master problem algorithm parameter
						 //mP.master_problem.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Primal);
						 //mP.master_problem.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Dual);
						 //set the master problem simplex optimality constraint
						 //mP.master_problem.setParam(IloCplex.DoubleParam.EpOpt, InputConstants.simplexOptimalityTolerance);	
						 /*System.out.println("#### Number of Rows after adding initial column : " +  mP.master_problem.getNrows() + " #####");
					     System.out.println("#### Number of Columns after adding initial column : " +  mP.master_problem.getNcols() + " #####");
					     System.out.println("#### Number of Non-zero's after adding initial column : " +  mP.master_problem.getNNZs() + " #####");*/
					    									 
							 
						 
					     ///################ COLUMN GENERATION ################///
		 				 //Round Robin Technique 
					     int ppNum = 0;
					     //get the start time
						 long cgStartTime = new Date().getTime();																		
						 /*//HashMap to check reduced costs
						 Map<Integer,Boolean> rcRepeatCheck = new HashMap<Integer,Boolean>();*/																														 
						 for(PricingProblem pPObject : pPList){																			
							 ColumnGeneration.runIterations(coreCstr,capCstr,ppNum, pPObject, mP, serviceChainTN, ChainSet, vnf_list, 
									 func_list, nodesNFVI, nfv_nodes, g, scCopyToSC);
							 //add the check to the HashMap
							 //rcRepeatCheck.put(ppNum, rcRepeat);
							 //increment the pricing problem number
					     	 ppNum++;																	     	 
						 }
						 
						 ///################ COLUMN GENERATION COMPLETE! ################///
					    /* String outputFileName = "Germany.txt";//"Internet2.txt";//"Atlanta.txt";"Germany.txt"
					 	 //create the output stream		
						 FileOutputStream outStream1 = new FileOutputStream(outputFileName);						
						 //create the output stream writer			
						 OutputStreamWriter osw1 = new OutputStreamWriter(outStream1);*/							
						 //file writer	
//						 Writer wrt1 = new BufferedWriter(osw1);
						 //column generation end time
						 long colGenEndTime = new Date().getTime();
						 long timeBeforeILPExecution = colGenEndTime - cgStartTime;					
					     System.out.println("\n\n\n\n\n\n\n");
				         System.out.println("###################Column Generation Complete!##########################");
				         double LPvalue =  mP.master_problem.getObjValue();	
				         rmpLpValue = LPvalue;
				         System.out.println("RMP LP Objective Value : " + LPvalue);
//				         wrt1.write("Final RMP LP Objective Value : " + LPvalue + "\n");
				         //print out CPLEX solving times
				         System.out.println("\n\tTotal RMP Execution Time : " + ColumnGeneration.execTimePP + "ms");
//				         wrt1.write("\tTotal RMP Execution Time : " + ColumnGeneration.execTimePP + "ms\n");
				         System.out.println("\tTotal PP Execution Time : " + ColumnGeneration.execTimePP + "ms");
//				         wrt1.write("\tTotal PP Execution Time : " + ColumnGeneration.execTimePP + "ms\n");
				         System.out.println("\tTime elapsed till ILP Execution Time : " + timeBeforeILPExecution + "ms");
//				         wrt1.write("\tTime elapsed till ILP Execution Time : " + timeBeforeILPExecution + "ms\n");
				         //Also printing out the positive reduced cost of each variable			       
				         /*System.out.println("\nPositive reduced cost for Z variables!");
				         for ( Map.Entry<MpVarZ, IloNumVar> entry : mP.usedVarZ.entrySet()  ) {																        	
				        	 System.out.println("New Z No. " + entry.getKey().cgConfig + "\tSC" + entry.getKey().sCiD + "\tConfig " + entry.getKey().configDsc + " = " + mP.master_problem.getReducedCost(entry.getValue())); 													        	 
				         }*/				         
				         //printing out the number of variables
//				         System.out.println("\nNo. of varaibles before ILP execution");
//				         System.out.println("\tNo. of Xc variables: " + mP.usedVarXc.size());
//				         System.out.println("\tNo. of Y variables: " + mP.usedVarY.size());
//				         System.out.println("\tNo. of Z variables: " + mP.usedVarZ.size());
				         //print put the traffic pairs; their SCs and their traffic
				         //PreProcVer1.printSDpairsSCandTraffic(scUsed, scCopies, serviceChainTN);
				         //get the solution for the LP version of the problem
		//		         ReportResults.report1(mP.master_problem, OriginalConfigs, mP.slackVariables, mP.usedVarZ, mP.usedVarX, mP.usedVarY);
				         //hold the converted values
				         //convert to an integer version of the problem
				         //converting the Xc variables
//				         System.out.println("Converting Xc variables!");
				         for ( Map.Entry<MpVarXc, IloNumVar> var : mP.usedVarXc.entrySet() ) {	        	
				        	 mP.master_problem.add(mP.master_problem.conversion(var.getValue(), IloNumVarType.Int));     	 
				         }
				         //converting the X variables
//				         System.out.println("Converting X variables!");
				         for ( Map.Entry<MpVarX, IloNumVar> var : mP.usedVarX.entrySet() ) {	        	
				        	 mP.master_problem.add(mP.master_problem.conversion(var.getValue(), IloNumVarType.Int));     	 
			         	 }
				         //converting the H variables
//				         System.out.println("Converting H variables!");
				         for ( Map.Entry<MpVarH, IloNumVar> var : mP.usedVarH.entrySet() ) {	        	
				        	 mP.master_problem.add(mP.master_problem.conversion(var.getValue(), IloNumVarType.Int));     	 
			         	 }
				         //converting the Y variables
//				         System.out.println("Converting Y variables!");
				         for ( Map.Entry<MpVarY, IloNumVar> var : mP.usedVarY.entrySet() ) {	        	 
				        	 mP.master_problem.add(mP.master_problem.conversion(var.getValue(), IloNumVarType.Int));
						 }
				         //converting the Z variables
//				         System.out.println("Converting Z variables!");
				         for ( Map.Entry<MpVarZ, IloNumVar> var : mP.usedVarZ.entrySet()  ) {
				        	 //set the upper bound of Z variables for the final ILP
				        	 var.getValue().setUB(1.0);
				        	 mP.master_problem.add(mP.master_problem.conversion(var.getValue(), IloNumVarType.Int));													        	 
				         }
//				         System.out.println("Removing Slack variables!");
				         //remove the slack variables from the master problem			        
				         for(Map.Entry<String, IloNumVar> entry : mP.slackVariables.entrySet()){
				        	 //remove the slack variables by setting upper-bound to 0
				        	 entry.getValue().setUB(0.0);
				        	 //System.out.println("\tRemoving " + entry.getKey());
				        	 //mP.master_problem.end(entry.getValue());
				        	 //mP.master_problem.delete(entry.getValue());	
				        	 //mP.master_problem.remove(entry.getValue());
				         }																	       
//				         System.out.println("Finished Removing Slack vaiables!");
				         //total number of columns
				         System.out.println("######## Total number of columns : " + mP.master_problem.getNcols() + " #########");
				         //total number of rows
				         System.out.println("######## Total number of rows : " + mP.master_problem.getNrows() + " ##########");     
				         //export the master problem
				        /* mP.master_problem.exportModel(InputConstants.NFV_Strategy + "_" + traffic + "_" + coreCount + "_master_problem_final.lp");		
				         mP.master_problem.exportModel(InputConstants.NFV_Strategy + "_" + traffic + "_" + coreCount + "_master_problem_final.sav");*/
//				         mP.master_problem.exportModel(InputConstants.NFV_Strategy + "_NoCoreNoLinkCapacity" + "_master_problem_final.lp");
//				         mP.master_problem.exportModel(InputConstants.NFV_Strategy + "_NoCoreNoLinkCapacity" + "_master_problem_final.sav");				        
					     //set the integrality, optimality and feasibliity constraints
					     //setting the intergrality tolerance
				         /*mP.master_problem.setParam(IloCplex.DoubleParam.EpInt, InputConstants.integerTolerance);
					     //setting the optimality tolerance
				         mP.master_problem.setParam(IloCplex.DoubleParam.EpOpt, InputConstants.simplexOptimalityTolerance);
					     //setting the feasiblity tolerance
				         mP.master_problem.setParam(IloCplex.DoubleParam.EpRHS, InputConstants.feasbilityTolerance);*/																        
				         //use the feasibility repair schemes			        
				         
				         //set parameter for germany for less than 10%
//				         mP.master_problem.setParam(IloCplex.DoubleParam.EpGap,0.1);
				         //ILP start time
				         long ilpStartTime = new Date().getTime();
				         //solve the integer version of the problem
				         mP.master_problem.solve();	         
				         //ILP end time
				         long ilpEndTime = new Date().getTime();
				         //ILP solve time
				         long ilpSolveTime = ilpEndTime - ilpStartTime;				        															         																         
				      
				         //CG Execution time
						 long cgExecTime = ilpEndTime - cgStartTime;
						 
						 //Total CPLEX time
				         double cplexTime = ColumnGeneration.execTimeRMP + ColumnGeneration.execTimePP + ilpSolveTime;
				         
				         //Time Spent in Java Code
				         double javaTime = (cgExecTime - cplexTime);
				         
				         //Total CPLEX Time before CG
				    	 CG.cgRunTime = ColumnGeneration.execTimeRMP + ColumnGeneration.execTimePP + ilpSolveTime;
				    	 //ILP Time
				    	 CG.ilpRunTime = ilpSolveTime;
				    	 //Total time for CG
				    	 CG.totalTime = cgExecTime;
						 
						 
						 //value of result
						 double objValue = mP.master_problem.getObjValue();
						 //get the ILP object value
						 CG.rmpIlpValue = objValue;
						 //e-optimality percentage
						 double eOptimalPercentage = ((objValue - LPvalue)*100)/LPvalue;
						 //store the e-optimality percentage
						 CG.eoptimality = eOptimalPercentage;
				         //report the results of the master problem	
						 //report the slack variables that have been set
						 ReportResults.reportSlackVarsInIlp(mP.master_problem,mP.slackVariables);
				         //get the solution for the ILP version of the problem
//				         ReportResults.reportIntegers(mP.master_problem, mP.usedVarZ, mP.usedVarXc, mP.usedVarY, mP.usedVarX, mP.usedVarH);
						 //do not get results for the Y variables
						 System.out.println("ILP Results");
						 ReportResults.reportIntegers(mP.master_problem, mP.usedVarZ, mP.usedVarXc, mP.usedVarX, mP.usedVarH);
				         //get the VNF placement nodes
				         CG.getPlacementNodes(mP.master_problem, mP.usedVarH);
				         //total traffic for each service chain
				         //iterate through each service chain
				         for(int scCopyID : scCopyToSC.keySet()){
				        	Double totalTraffic = 0.0; 
				        	for(TrafficNodes tn : serviceChainTN.get(scCopyID)){
				        		totalTraffic += tn.flow_traffic;
				        	}
				        	System.out.println("SC = " + scCopyID + " ; Total Traffic = " + totalTraffic);
				         }
				         //CPU cores before getting calculated
				         System.out.println("##### CPU Cores before calculation #####");
				         if(cpuCoreCount.isEmpty()){
				        	 System.out.println("\tNo CPU Cores have been recorded");
				         }
				         for(Map.Entry<BaseVertex, Double> entryCPU : cpuCoreCount.entrySet()){
				        	 System.out.println("Vertex = " + entryCPU.getKey().get_id() + " ; Cores = " + entryCPU.getValue());				        	 
				         }
				         //get the core count and link capacity to be used
				         CpuLinkCapacity.calCpuCapacity(mP.master_problem, mP.usedVarZ, cpuCoreCount);
				         //get traffic load per node
				         CG.calculateTrafficLoadPerNode(mP.master_problem, mP.usedVarZ, serviceChainTN);
				         //get traffic load per node per VNF
				         CG.calculateTrafficLoadPerNodePerVNF(mP.master_problem, mP.usedVarZ, serviceChainTN, scCopyToSC, ChainSet);				         
				         if(cpuCoreCount.isEmpty()){
				        	 System.out.println("########No CPU core counts recorded!#######");
				         }
				         CpuLinkCapacity.calLinkCapacity(mP.master_problem,mP.usedVarZ,mP.usedVarY,linkCapacity);
				         if( linkCapacity.isEmpty()){
				        	 System.out.println("########No link capacity recorded!#######");
				         }
				         System.out.println("\tRMP ILP Execution Time : " + ilpSolveTime + "ms");				       
				         System.out.println("\tCPLEX Total time = " + cplexTime + "ms");
				        
				    	 
				         System.out.println("\tJava Time = " + javaTime);				      
				         System.out.println("\n\t(s,d) pairs = " + pair_list.size() + " ; Initial Configs = " + serviceChainTN.keySet().size());																         
				         //report the MIP Gap
				         System.out.println("\n####### E-Optimal : " + eOptimalPercentage  + " ; CG Execution Time : " + cgExecTime + " ; MIP Relative Gap Tolerance : " + mP.master_problem.getMIPRelativeGap()  + " ########");	
						 /*for(Map.Entry<BaseVertex,Double> entryCPU : cpuCoreCount.entrySet()){
							 System.out.println("CPU Cores for Vertex ID : " + entryCPU.getKey().get_id() + " = " + entryCPU.getValue()/1000);
						 }
						 //iterate through link capacities					
						 for(Map.Entry<NodePair,Double> entryLink : linkCapacity.entrySet()){
							 //get the bandwidth values in Mbps //convert to Gbps
							 System.out.println("Capacity for Link : (" + entryLink.getKey().v1.get_id() + "," + entryLink.getKey().v2.get_id() + ") = " + entryLink.getValue()/1000);
						 }*/
						 
						 System.out.println("\n\n\n");				       
				         //deallocate the master problem CPLEX object		         
				         mP.master_problem.end();		      
				         //deallocate the pricing problem CPLEX object//																       
				         for(PricingProblem pP : pPList){
				        	 pP.pricing_problem.end();
				         }				         
				         //do garbage collection now!		         
				         System.gc();
			}catch(IloException exc){	
				 System.err.println("Concert exception '" + exc + "' caught");				
				 exc.printStackTrace(); 
				 System.out.println("*********************************************");				 
			}		
		}
		
		
		
		//keep track of previously generated columns for service chains
		//run the CG
		public static void runCG(boolean coreCstr, boolean capCstr, Map<BaseVertex,Double> cpuCoreCount, Map<NodePair,Double> linkCapacity,
				Graph g, Map<Integer,ServiceChain> ChainSet, List<TrafficNodes> pair_list, List<Integer> scUsed, List<FuncPt> vnf_list, 
				List<FuncPt> func_list, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN, ArrayList<BaseVertex> nfv_nodes, 
				ArrayList<BaseVertex> nodesNFVI, ArrayList<BaseVertex> vertex_list_without_nfvi_nodes, Map<Integer,ArrayList<Integer>> scCopies,  
				Map<Integer,Integer> scCopyToSC, Map<Integer,ArrayList<HuerVarZ>> configsPerSC, Map<TrafficNodes,SdDetails> configPerSD, 
				Map<Integer,Integer> CountMaxVNF, Map<Integer,Integer> replicaPerVNF, int kNodeCount, Map<Integer, HashSet<HuerVarZ>> generatedColumns) throws Exception{   
 	  	 
	    
				try{	
					     //clear vnfPlacementNodes everytime CG it is run
						 CG.vnfPlacementNodes.clear();
						 //clear the traffic load per node
						 CG.trafficLoadPerNode.clear();
						 //clear the traffic load per node per VNF
						 CG.trafficLoadPerNodePerVNF.clear();
					
				         ///################ PRICING PROBLEM ################///
						 //create the list of pricing problem objects
					     ArrayList<PricingProblem> pPList = new ArrayList<PricingProblem>();
//								 ArrayList<LazyPricingProblem2> pPList = new ArrayList<LazyPricingProblem2>();
					     //iterate through the list of service chains
					     for(int scID : scUsed){
					    	 for(int scCopyID : scCopies.get(scID)){
					    		 System.out.println("Pricing problem for scID " + scCopyID + "created");
							 //create the pricing problem object		    
						     PricingProblem pPObject = new PricingProblem(coreCstr, capCstr, cpuCoreCount, linkCapacity, serviceChainTN, vnf_list, scCopyID, ChainSet, 
						    		 nodesNFVI, nfv_nodes, vertex_list_without_nfvi_nodes, g, scCopyToSC);
//									 LazyPricingProblem2 pPObject = new LazyPricingProblem2(InputConstants.coreCount, serviceChainTN, vnf_list, scID, ChainSet, nodesNFVI, nfv_nodes, vertex_list_without_nfvi_nodes, g);
						     //set numerical emphasis
						     //pPObject.pricing_problem.setParam(IloCplex.Param.Emphasis.Numerical, true);
						     //add the pricing problem object to the list
							 pPList.add(pPObject);
					    	 }
					     }	
					     System.out.println("####### Pricing Problems Generated! #########");
					     
					    
						 ///################ MASTER PROBLEM ################///			
						 //create the master problem instance	
						 MasterProblem mP = new MasterProblem(coreCstr, capCstr, cpuCoreCount, linkCapacity, func_list, scUsed, serviceChainTN, configsPerSC, 
								 configPerSD, g, nodesNFVI, nfv_nodes, ChainSet, pair_list, vertex_list_without_nfvi_nodes, 
								 scCopies, scCopyToSC, CountMaxVNF, replicaPerVNF, kNodeCount, generatedColumns);
						 System.out.println("######### Master Problem Generated! #########");
						
						 //set the display parameter
						 /*System.out.println("Simplex Iterations");
						 mP.master_problem.setParam(IloCplex.IntParam.SimDisplay, 2);*/
						 //set numerical emphasis
						 //mP.master_problem.setParam(IloCplex.Param.Emphasis.Numerical, true);
						 //set the master problem algorithm parameter
						 //mP.master_problem.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Primal);
						 //mP.master_problem.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Dual);
						 //set the master problem simplex optimality constraint
						 //mP.master_problem.setParam(IloCplex.DoubleParam.EpOpt, InputConstants.simplexOptimalityTolerance);	
						 /*System.out.println("#### Number of Rows after adding initial column : " +  mP.master_problem.getNrows() + " #####");
					     System.out.println("#### Number of Columns after adding initial column : " +  mP.master_problem.getNcols() + " #####");
					     System.out.println("#### Number of Non-zero's after adding initial column : " +  mP.master_problem.getNNZs() + " #####");*/
					    									 
							 
						 
					     ///################ COLUMN GENERATION ################///
		 				 //Round Robin Technique 
					     int ppNum = 0;
					     //get the start time
						 long cgStartTime = new Date().getTime();																		
						 /*//HashMap to check reduced costs
						 Map<Integer,Boolean> rcRepeatCheck = new HashMap<Integer,Boolean>();*/																														 
						 for(PricingProblem pPObject : pPList){	
							 System.out.println("Running column generation for ### " + pPObject.scIDpP + " ###\n\n");							 
							 ColumnGeneration.runIterations(coreCstr,capCstr,ppNum, pPObject, mP, serviceChainTN, ChainSet, vnf_list, 
									 func_list, nodesNFVI, nfv_nodes, g, scCopyToSC);
							 //add the check to the HashMap
							 //rcRepeatCheck.put(ppNum, rcRepeat);
							 //increment the pricing problem number
					     	 ppNum++;																	     	 
						 }
						 
						 ///################ COLUMN GENERATION COMPLETE! ################///
					    /* String outputFileName = "Germany.txt";//"Internet2.txt";//"Atlanta.txt";"Germany.txt"
					 	 //create the output stream		
						 FileOutputStream outStream1 = new FileOutputStream(outputFileName);						
						 //create the output stream writer			
						 OutputStreamWriter osw1 = new OutputStreamWriter(outStream1);*/							
						 //file writer	
//								 Writer wrt1 = new BufferedWriter(osw1);
						 //column generation end time
						 long colGenEndTime = new Date().getTime();
						 long timeBeforeILPExecution = colGenEndTime - cgStartTime;					
					     System.out.println("\n\n\n\n\n\n\n");
				         System.out.println("###################Column Generation Complete!##########################");
				         double LPvalue =  mP.master_problem.getObjValue();	
				         CG.rmpLpValue = LPvalue;
				         System.out.println("RMP LP Objective Value : " + LPvalue);
//						         wrt1.write("Final RMP LP Objective Value : " + LPvalue + "\n");
				         //print out CPLEX solving times
				         System.out.println("\n\tTotal RMP Execution Time : " + ColumnGeneration.execTimePP + "ms");
//						         wrt1.write("\tTotal RMP Execution Time : " + ColumnGeneration.execTimePP + "ms\n");
				         System.out.println("\tTotal PP Execution Time : " + ColumnGeneration.execTimePP + "ms");
//						         wrt1.write("\tTotal PP Execution Time : " + ColumnGeneration.execTimePP + "ms\n");
				         System.out.println("\tTime elapsed till ILP Execution Time : " + timeBeforeILPExecution + "ms");
//						         wrt1.write("\tTime elapsed till ILP Execution Time : " + timeBeforeILPExecution + "ms\n");
				         //Also printing out the positive reduced cost of each variable			       
				         System.out.println("\nPositive reduced cost for Z variables!");
				         for ( Map.Entry<MpVarZ, IloNumVar> entry : mP.usedVarZ.entrySet()  ) {																        	
				        	 System.out.println("New Z No. " + entry.getKey().cgConfig + "\tSC" + entry.getKey().sCiD + "\tConfig " + entry.getKey().configDsc + " = " + mP.master_problem.getReducedCost(entry.getValue())); 													        	 
				         }				         
				         //printing out the number of variables
				         System.out.println("\nNo. of varaibles before ILP execution");
				         System.out.println("\tNo. of Xc variables: " + mP.usedVarXc.size());
				         System.out.println("\tNo. of X variables: " + mP.usedVarX.size());
				         System.out.println("\tNo. of H variables: " + mP.usedVarH.size());
				         System.out.println("\tNo. of Y variables: " + mP.usedVarY.size());
				         System.out.println("\tNo. of Z variables: " + mP.usedVarZ.size());
				         //print put the traffic pairs; their SCs and their traffic
				         //PreProcVer1.printSDpairsSCandTraffic(scUsed, scCopies, serviceChainTN);
				         //get the solution for the LP version of the problem
		//		         ReportResults.report1(mP.master_problem, OriginalConfigs, mP.slackVariables, mP.usedVarZ, mP.usedVarX, mP.usedVarY);
				         //hold the converted values
				         //convert to an integer version of the problem
				         //converting the Xc variables
				         System.out.println("Converting Xc variables!");
				         for ( Map.Entry<MpVarXc, IloNumVar> var : mP.usedVarXc.entrySet() ) {	        	
				        	 mP.master_problem.add(mP.master_problem.conversion(var.getValue(), IloNumVarType.Int));     	 
				         }
				         //converting the X variables
				         System.out.println("Converting X variables!");
				         for ( Map.Entry<MpVarX, IloNumVar> var : mP.usedVarX.entrySet() ) {	        	
				        	 mP.master_problem.add(mP.master_problem.conversion(var.getValue(), IloNumVarType.Int));     	 
			         	 }
				         //converting the H variables
				         System.out.println("Converting H variables!");
				         for ( Map.Entry<MpVarH, IloNumVar> var : mP.usedVarH.entrySet() ) {	        	
				        	 mP.master_problem.add(mP.master_problem.conversion(var.getValue(), IloNumVarType.Int));     	 
			         	 }
				         //converting the Y variables
				         System.out.println("Converting Y variables!");
				         for ( Map.Entry<MpVarY, IloNumVar> var : mP.usedVarY.entrySet() ) {	        	 
				        	 mP.master_problem.add(mP.master_problem.conversion(var.getValue(), IloNumVarType.Int));
						 }
				         //converting the Z variables
				         System.out.println("Converting Z variables!");
				         for ( Map.Entry<MpVarZ, IloNumVar> var : mP.usedVarZ.entrySet()  ) {
				        	 //set the upper bound of Z variables for the final ILP
				        	 var.getValue().setUB(1.0);
				        	 mP.master_problem.add(mP.master_problem.conversion(var.getValue(), IloNumVarType.Int));													        	 
				         }
				         System.out.println("Removing Slack variables!");
				         //remove the slack variables from the master problem			        
				         for(Map.Entry<String, IloNumVar> entry : mP.slackVariables.entrySet()){
				        	 //remove the slack variables by setting upper-bound to 0
				        	 entry.getValue().setUB(0.0);
				        	 //System.out.println("\tRemoving " + entry.getKey());
				        	 //mP.master_problem.end(entry.getValue());
				        	 //mP.master_problem.delete(entry.getValue());	
				        	 //mP.master_problem.remove(entry.getValue());
				         }																	       
				         System.out.println("Finished Removing Slack vaiables!");
				         //total number of columns
				         System.out.println("######## Total number of columns : " + mP.master_problem.getNcols() + " #########");
				         //total number of rows
				         System.out.println("######## Total number of rows : " + mP.master_problem.getNrows() + " ##########");     
				         //export the master problem
				        /* mP.master_problem.exportModel(InputConstants.NFV_Strategy + "_" + traffic + "_" + coreCount + "_master_problem_final.lp");		
				         mP.master_problem.exportModel(InputConstants.NFV_Strategy + "_" + traffic + "_" + coreCount + "_master_problem_final.sav");*/
//						         mP.master_problem.exportModel(InputConstants.NFV_Strategy + "_NoCoreNoLinkCapacity" + "_master_problem_final.lp");
				         //mP.master_problem.exportModel(InputConstants.NFV_Strategy + "_NoCoreNoLinkCapacity" + "_master_problem_final.sav");				        
					     //set the integrality, optimality and feasibliity constraints
					     //setting the integrality tolerance
				         /*mP.master_problem.setParam(IloCplex.DoubleParam.EpInt, InputConstants.integerTolerance);
					     //setting the optimality tolerance
				         mP.master_problem.setParam(IloCplex.DoubleParam.EpOpt, InputConstants.simplexOptimalityTolerance);
					     //setting the feasiblity tolerance
				         mP.master_problem.setParam(IloCplex.DoubleParam.EpRHS, InputConstants.feasbilityTolerance);*/																        
				         //use the feasibility repair schemes				        
				         
				        
				         
				         //set parameter for germany for less than 10%
//						         mP.master_problem.setParam(IloCplex.DoubleParam.EpGap,0.1);
				         //ILP start time
				         long ilpStartTime = new Date().getTime();
				         //solve the integer version of the problem
				         mP.master_problem.solve();	         
				         //ILP end time
				         long ilpEndTime = new Date().getTime();
				         //ILP solve time
				         long ilpSolveTime = ilpEndTime - ilpStartTime;
				        															         																         
				         //Time for CG execution				      
						 long cgExecTime = ilpEndTime - cgStartTime;
						 
						 //Total CPLEX time
				         double cplexTime = ColumnGeneration.execTimeRMP + ColumnGeneration.execTimePP + ilpSolveTime;
						 
						 //value of result
						 double objValue = mP.master_problem.getObjValue();
						 //get the ILP object value
						 CG.rmpIlpValue = objValue;
						 //e-optimality percentage
						 double eOptimalPercentage = ((objValue - LPvalue)*100)/LPvalue;
						 //store the e-optimality percentage
						 CG.eoptimality = eOptimalPercentage;
				         //report the results of the master problem	
						 //report the slack variables that have been set
						 ReportResults.reportSlackVarsInIlp(mP.master_problem,mP.slackVariables);
				         //get the solution for the ILP version of the problem
				         ReportResults.reportIntegers(mP.master_problem, mP.usedVarZ, mP.usedVarXc, mP.usedVarY, mP.usedVarX, mP.usedVarH);				        
				         //get the VNF placement nodes
				         CG.getPlacementNodes(mP.master_problem,mP.usedVarH);
				         //get the core count and link capacity to be used
				         CpuLinkCapacity.calCpuCapacity(mP.master_problem, mP.usedVarZ, cpuCoreCount);				        
				         //get traffic load per node per VNF
				         if(cpuCoreCount.isEmpty()){
				        	 System.out.println("########No CPU core counts recorded!#######");
				         }
				         CpuLinkCapacity.calLinkCapacity(mP.master_problem,mP.usedVarZ,mP.usedVarY,linkCapacity);
				         if( linkCapacity.isEmpty()){
				        	 System.out.println("########No link capacity recorded!#######");
				         }
				         System.out.println("\tRMP ILP Execution Time : " + ilpSolveTime + "ms");
				         
				         System.out.println("\tCPLEX Total time = " + cplexTime + "ms");
				         //total cplex time before CG
				    	 CG.cgRunTime = ColumnGeneration.execTimeRMP + ColumnGeneration.execTimePP;
				    	 //total cplex time for ILP
				    	 CG.ilpRunTime = ilpSolveTime;
				    	 //total time for CG
				    	 CG.totalTime = cgExecTime;
				    	 //time spent in java code
				         double javaTime = (cgExecTime - cplexTime);
				         System.out.println("\tJava Time = " + javaTime);				      
				         System.out.println("\n\t(s,d) pairs = " + pair_list.size() + " ; Initial Configs = " + serviceChainTN.keySet().size());																         
				         //report the MIP Gap
				         System.out.println("\n####### E-Optimal : " + eOptimalPercentage  + " ; CG Execution Time : " + cgExecTime + " ; MIP Relative Gap Tolerance : " + mP.master_problem.getMIPRelativeGap()  + " ########");	
						 for(Map.Entry<BaseVertex,Double> entryCPU : cpuCoreCount.entrySet()){
							 System.out.println("CPU Cores for Vertex ID : " + entryCPU.getKey().get_id() + " = " + entryCPU.getValue()/1000);
						 }
						 //iterate through link capacities					
						 for(Map.Entry<NodePair,Double> entryLink : linkCapacity.entrySet()){
							 //get the bandwidth values in Mbps //convert to Gbps
							 System.out.println("Capacity for Link : (" + entryLink.getKey().v1.get_id() + "," + entryLink.getKey().v2.get_id() + ") = " + entryLink.getValue()/1000);
						 }
						 
						 System.out.println("\n\n\n");
						 
						
						 //adding the previous columns for the cluster 
						 for(int scID : scUsed){
							 //Number of clusters for a service chain	
							 int numOfClusters = scCopies.get(scID).size();
							 //keep track of the configuration numbers
							 int configNum = 0;
							 if(generatedColumns.get(numOfClusters) != null){
								 configNum = generatedColumns.get(numOfClusters).size();
							 }							
							 //add the new columns to the list of columns known
							 //store the columns by service chain ID
							 for(MpVarZ objectZ : mP.usedVarZ.keySet()){
								 //check if for same scID
								 if(scCopyToSC.get(objectZ.sCiD) == scID){
									 //change into a HueVarZ object							
									 HuerVarZ tempZ = new HuerVarZ(objectZ);
									 //update the configuration number
									 tempZ.updateConfigNum(configNum);
									 //increment configuration number
									 configNum++;
									 //create the set of Z objects
									 HashSet<HuerVarZ> tempList; 
									 //if there are no Z objects for a service chain Id
									 if(generatedColumns.get(numOfClusters)==null){
										 tempList = new HashSet<HuerVarZ>();							 
									 }else{
										 //get the list of configurations for a service chain
										 tempList = generatedColumns.get(numOfClusters);
									 }
									 //add to the list of objects
									 tempList.add(tempZ);
									 //add the list of columns
									 generatedColumns.put(numOfClusters, tempList);
								 }
							 }
						 }
						 
						 
				         //deallocate the master problem CPLEX object		         
				         mP.master_problem.end();		      
				         //deallocate the pricing problem CPLEX object//																       
				         for(PricingProblem pP : pPList){
				        	 pP.pricing_problem.end();
				         }				         
				         //do garbage collection now!		         
				         System.gc();
			}catch(IloException exc){	
				 System.err.println("Concert exception '" + exc + "' caught");				
				 exc.printStackTrace(); 
				 System.out.println("*********************************************");				 
			}

		
		}
	
	
}
