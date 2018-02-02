package colGen.model.ver2;

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

import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.analyze.CpuLinkCapacity;
import colGen.model.heuristic.BaseHeuristic2.SdDetails;
import colGen.model.heuristic.HuerVarZ;
import colGen.model.preprocess.preProcFunctions;
import colGen.model.result.ReportResults;
import colGen.model.result.ReportResults2;
import colGen.model.result.Solution;
import colGen.model.ver1.CG;
import colGen.model.ver1.ColumnGeneration;
import colGen.model.ver1.MpVarH;
import colGen.model.ver1.MpVarX;
import colGen.model.ver1.MpVarY;
import colGen.model.ver1.MpVarZ;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class CG2 {
	
	public static double cgRunTime; //in milliseconds
	public static double ilpRunTime; //in milliseconds	
	public static double totalTime; //in milliseconds
	public static Double eoptimality ; //in terms of percentage
	public static double rmpLpValue; //in Mb
	public static double rmpIlpValue; //in Mb
	public static int totalConfigGenerated;
	public static Set<Integer> vnfPlacementNodes = new HashSet<Integer>();		
	
		
	//get the nodes where the VNF has been placed
	public static void getPlacementNodes(IloCplex master_problem, Map<MpVarH,IloNumVar> usedVarH)  throws IloException {
		  //values for variable X
	      for(Map.Entry<MpVarH, IloNumVar> entry : usedVarH.entrySet()){
	    	  if( Math.round(master_problem.getValue(entry.getValue())) != 0 ){
	    		  vnfPlacementNodes.add(entry.getKey().node.get_id());
	    	  }
	      }
	}
	
	//warm start the CG to remember the previous columns that were generated
	public static void runCGwithPrevCols(boolean coreCstr, boolean capCstr, Map<BaseVertex,Double> cpuCoreCount, Map<NodePair,Double> linkCapacity,
			Graph g, Map<Integer,ServiceChain> ChainSet, List<TrafficNodes> pair_list, List<Integer> scUsed, List<FuncPt> vnf_list, 
			List<FuncPt> func_list, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN, ArrayList<BaseVertex> nfv_nodes, ArrayList<BaseVertex> nodesNFVI,
			ArrayList<BaseVertex> vertex_list_without_nfvi_nodes, Map<Integer,ArrayList<Integer>> scCopies, Map<Integer,Integer> scCopyToSC, 
			Map<Integer,ArrayList<HuerVarZ>> configsPerSC, Map<TrafficNodes,SdDetails> configPerSD, Map<Integer,Integer> CountMaxVNF, 
			Map<Integer,Integer> replicaPerVNF, int kNodeCount, Set<HuerVarZ> prevCols, Solution bestSol) throws Exception{    
								  
			/// ######## GENERATE THE CONSTRAINTS ####### ///		
			try{	         	
						 //clear vnfPlacementNodes everytime CG it is run
						 CG.vnfPlacementNodes.clear();
				
				         ///################ PRICING PROBLEM ################///
						 //create the list of pricing problem objects
						 ArrayList<PricingProblem2> pPList = new ArrayList<PricingProblem2>();
//						 ArrayList<LazyPricingProblem2> pPList = new ArrayList<LazyPricingProblem2>();
					     //iterate through the list of service chains
					     for(int scID : scUsed){
							 //create the pricing problem object		    
							 PricingProblem2 pPObject = new PricingProblem2(InputConstants.coreCount, serviceChainTN, vnf_list, scID, ChainSet, nodesNFVI, nfv_nodes, vertex_list_without_nfvi_nodes, g);
//						   	 LazyPricingProblem2 pPObject = new LazyPricingProblem2(InputConstants.coreCount, serviceChainTN, vnf_list, scID, ChainSet, nodesNFVI, nfv_nodes, vertex_list_without_nfvi_nodes, g);
					    	 //set the parallel mode parameter //opportunistic parallel mode
					    	 //pPObject.pricing_problem.setParam(IloCplex.Param.Parallel, -1);
						     //set numerical emphasis
						     //pPObject.pricing_problem.setParam(IloCplex.Param.Emphasis.Numerical, true);
						     //add the pricing problem object to the list
							 pPList.add(pPObject);		    	 
					     }	
				
						 ///################ MASTER PROBLEM ################///			
						 //create the master problem instance	
						 MasterProblem2HueVarZ mP = new MasterProblem2HueVarZ(InputConstants.coreCount, func_list, CountMaxVNF, scUsed, serviceChainTN, configsPerSC, configPerSD, g, nodesNFVI, nfv_nodes, 
								 ChainSet, pair_list, vertex_list_without_nfvi_nodes, pPList, replicaPerVNF, kNodeCount);
						
						 //set the display parameter
						/* System.out.println("Simplex Iterations");
						 mP.master_problem.setParam(IloCplex.IntParam.SimDisplay, 2);*/
						 //set numerical emphasis
						 //mP.master_problem.setParam(IloCplex.Param.Emphasis.Numerical, true);
						 //set the master problem algorithm parameter
						 //mP.master_problem.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Primal);
						 //mP.master_problem.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Dual);
						 //set the master problem simplex optimality constraint
						 //mP.master_problem.setParam(IloCplex.DoubleParam.EpOpt, InputConstants.simplexOptimalityTolerance);	
						 System.out.println("#### Number of Rows after adding initial column : " +  mP.master_problem.getNrows() + " #####");
					     System.out.println("#### Number of Columns after adding initial column : " +  mP.master_problem.getNcols() + " #####");
					     System.out.println("#### Number of Non-zero's after adding initial column : " +  mP.master_problem.getNNZs() + " #####");
					    									 
							 
						 
					     ///################ COLUMN GENERATION ################///
		 				 //Round Robin Technique 
					     int ppNum = 0;					 
					     //get the start time
						 long cgStartTime = new Date().getTime();						
						 //HashMap to check reduced costs
						 Map<Integer,Boolean> rcRepeatCheck = new HashMap<Integer,Boolean>();
						 for(PricingProblem2 pPObject : pPList){
							 ColumnGeneration2.runIterations(ppNum, serviceChainTN, pPObject, mP, pair_list, ChainSet, vnf_list, func_list, nodesNFVI, nfv_nodes, g);
//							 Boolean rcRepeat = PricingCheck.runIterations(ppNum, serviceChainTN, pPObject, mP, OriginalConfigs, pair_list, ChainSet, vnf_list, func_list, nodesNFVI, nfv_nodes, g);
							 //add the check to the HashMap
							 //rcRepeatCheck.put(ppNum, rcRepeat);
							 //increment the pricing problem number
					     	 ppNum++;																	     	 
						 }
						 
						 ///################ COLUMN GENERATION COMPLETE! ################///
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
				         System.out.println("\n\tTotal RMP Execution Time : " + ColumnGeneration2.execTimePP + "ms");
//				         wrt1.write("\tTotal RMP Execution Time : " + ColumnGeneration2.execTimePP + "ms\n");
				         System.out.println("\tTotal PP Execution Time : " + ColumnGeneration2.execTimePP + "ms");
//				         wrt1.write("\tTotal PP Execution Time : " + ColumnGeneration2.execTimePP + "ms\n");
				         System.out.println("\tTime elapsed till ILP Execution Time : " + timeBeforeILPExecution + "ms");
//				         wrt1.write("\tTime elapsed till ILP Execution Time : " + timeBeforeILPExecution + "ms\n");
				         //Also printing out the positive reduced cost of each variable			       
				         System.out.println("\nPositive reduced cost for Z variables!");
				         for ( Map.Entry<MpVarZ, IloNumVar> entry : mP.usedVarZ.entrySet()  ) {																        	
				        	 System.out.println("New Z No. " + entry.getKey().cgConfig + "\tSC" + entry.getKey().sCiD + "\tConfig " + entry.getKey().configDsc + " = " + mP.master_problem.getReducedCost(entry.getValue())); 													        	 
				         }				         
				         //printing out the number of variables
				         System.out.println("\nNo. of varaibles before ILP execution");
				         System.out.println("\tNo. of Y variables: " + mP.usedVarY.size());
				         System.out.println("\tNo. of X variables: " + mP.usedVarX.size());
				         System.out.println("\tNo. of H variables: " + mP.usedVarH.size());
				         System.out.println("\tNo. of Z variables: " + mP.usedVarZ.size());
				         //get the solution for the LP version of the problem
		//		         ReportResults.report1(mP.master_problem, OriginalConfigs, mP.slackVariables, mP.usedVarZ, mP.usedVarX, mP.usedVarY);
				         //hold the converted values
				         //convert to an integer version of the problem
				         //converting the X variables
				         /*for ( Map.Entry<MpVarXc, IloNumVar> var : mP.usedVarX.entrySet() ) {	        	
				        	 mP.master_problem.add(mP.master_problem.conversion(var.getValue(), IloNumVarType.Int));     	 
				         }*/
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
				        	 prevCols.add(new HuerVarZ(var.getKey()));
				        	 var.getValue().setUB(1.0);
				        	 mP.master_problem.add(mP.master_problem.conversion(var.getValue(), IloNumVarType.Int));													        	 
				         } 
				         System.out.println("Columns stored for next iteration!");
				         //remove the slack variables from the master problem
				         if(InputConstants.slackVars){
					         for(Map.Entry<String, IloNumVar> entry : mP.slackVariables.entrySet()){
					        	 //remove the slack variables setting upper-bound to 0
					        	 entry.getValue().setUB(0.0);
					        	 //mP.master_problem.delete(entry.getValue());
					         }	
				         }
				         //total number of columns
				         System.out.println("######## Total number of columns : " + mP.master_problem.getNcols() + " #########");
				         //total number of rows
				         System.out.println("######## Total number of rows : " + mP.master_problem.getNrows() + " ##########");     
				         //export the master problem
//						 mP.master_problem.exportModel(InputConstants.NFV_Strategy + "_" + traffic + "_" + coreCount + "_master_problem_final.lp");		
//						 mP.master_problem.exportModel(InputConstants.NFV_Strategy + "_" + traffic + "_" + coreCount + "_master_problem_final.sav");
						 mP.master_problem.exportModel(InputConstants.NFV_Strategy + "_NoCoreNoLinkCapacity" + "_master_problem_final.lp");
//						 mP.master_problem.exportModel(InputConstants.NFV_Strategy + "_NoCoreNoLinkCapacity" + "_master_problem_final.sav");
					     //set the integrality, optimality and feasibliity constraints
					     //setting the intergrality tolerance
//						 mP.master_problem.setParam(IloCplex.DoubleParam.EpInt, InputConstants.integerTolerance);
						 //setting the optimality tolerance
//						 mP.master_problem.setParam(IloCplex.DoubleParam.EpOpt, InputConstants.simplexOptimalityTolerance);
						 //setting the feasiblity tolerance
//						 mP.master_problem.setParam(IloCplex.DoubleParam.EpRHS, InputConstants.feasbilityTolerance);
				         //use the feasibility repair schemes
						 
						 
						 
						 
						  //ILP start time
				         long ilpStartTime = new Date().getTime();
				         //solve the integer version of the problem
				         mP.master_problem.solve();	         
				         //ILP end time
				         long ilpEndTime = new Date().getTime();
				         //solve time
				         long solveTime = ilpEndTime - ilpStartTime;
				         //ILP solve time
				         long ilpSolveTime = solveTime;																         																         
				         //get the end time for CG execution
				         long cgEndTime = ilpEndTime;
				         //get the execution time
						 long cgExecTime = cgEndTime - cgStartTime;																		 
						 //value of result
						 double objValue = mP.master_problem.getObjValue();
						 //get the ILP object value
						 rmpIlpValue = objValue;
						 //e-optimality percentage
						 double eOptimalPercentage = ((objValue - LPvalue)*100)/LPvalue;
				         //report the results of the master problem	
						 //report the slack variables that have been set
						 ReportResults.reportSlackVarsInIlp(mP.master_problem,mP.slackVariables);
				         //get the solution for the ILP version of the problem
				         ReportResults2.reportIntegers(mP.master_problem, mP.usedVarZ, mP.usedVarX, mP.usedVarY,mP.usedVarH);
				         //get the VNF placement nodes
				         getPlacementNodes(mP.master_problem,mP.usedVarH);
				         //print put the traffic pairs; their SCs and their traffic
				         preProcFunctions.printSDpairsSCandTraffic(scUsed, serviceChainTN);	
				         //get the core count and link capacity to be used
				         CpuLinkCapacity.calCpuCapacity(mP.master_problem, mP.usedVarZ, cpuCoreCount);
				         if(cpuCoreCount.isEmpty()){
				        	 System.out.println("########No CPU core counts recorded!#######");
				         }
				         CpuLinkCapacity.calLinkCapacity(mP.master_problem,mP.usedVarZ,mP.usedVarY,linkCapacity);
				         if( linkCapacity.isEmpty()){
				        	 System.out.println("########No link capacity recorded!#######");
				         }
				         System.out.println("\tRMP ILP Execution Time : " + ilpSolveTime + "ms");				        
				         //find total cplex time
				         double cplexTime = ColumnGeneration2.execTimeRMP + ColumnGeneration2.execTimePP + ilpSolveTime;
				         System.out.println("\tCPLEX Total time = " + cplexTime + "ms");
				         //total cplex time before CG
				    	 cgRunTime = ColumnGeneration2.execTimeRMP + ColumnGeneration2.execTimePP;
				    	 //total cplex time for ILP
				    	 ilpRunTime = ilpSolveTime;
				    	 //total time for CG
				    	 totalTime = cgExecTime;
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
				         //check e-optimality
				         if(eoptimality == null){
				        	 //assign e-optimality value
				        	 eoptimality = eOptimalPercentage; 
				        	 //store the solution
				        	 bestSol.addSolution(mP.master_problem,mP.usedVarZ,mP.usedVarH,mP.usedVarX,mP.usedVarY);
				        	 //store run time values
				        	 bestSol.cgRunTime = cgRunTime;
				        	 bestSol.eoptimality = eoptimality;
				        	 bestSol.ilpRunTime = ilpRunTime;
				        	 bestSol.rmpIlpValue = rmpIlpValue;
				        	 bestSol.rmpLpValue = rmpLpValue;
				        	 bestSol.totalTime = totalTime;
				         }else if(eOptimalPercentage < eoptimality){
				        	 //assign e-optimality value
				        	 eoptimality = eOptimalPercentage;
				        	 //clear the previous solution
				        	 bestSol.clearSolution();
				        	 //store the solution
				        	 bestSol.addSolution(mP.master_problem,mP.usedVarZ,mP.usedVarH,mP.usedVarX,mP.usedVarY); 
				        	 //store run time values
				        	 bestSol.cgRunTime = cgRunTime;
				        	 bestSol.eoptimality = eoptimality;
				        	 bestSol.ilpRunTime = ilpRunTime;
				        	 bestSol.rmpIlpValue = rmpIlpValue;
				        	 bestSol.rmpLpValue = rmpLpValue;
				        	 bestSol.totalTime = totalTime;
				         }
				         
				         //print out if the reduced cost has repeated
				         for(Map.Entry<Integer, Boolean> entryRCCheck : rcRepeatCheck.entrySet()){
				        	 if(entryRCCheck.getValue()){
				        		 System.err.println("!!!! Reduced Cost Repetition in : " +  entryRCCheck.getKey());
				        	 }
				         }											
				         //get the basis status for the variables
				         //ReportResults.basisCheckForVariables(master_problem, OriginalConfigs, usedVarZ, usedVarX, usedVarY);
				         //deallocate the master problem CPLEX object		         
				         mP.master_problem.end();		      
				         //deallocate the pricing problem CPLEX object
				         for(PricingProblem2 pP : pPList){
//						 for(LazyPricingProblem2 pP : pPList){
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
	
	
	//cold start of CG
	//no memory of columns used in previous iterations
	public static void runCG(boolean coreCstr, boolean capCstr, Map<BaseVertex,Double> cpuCoreCount, Map<NodePair,Double> linkCapacity,
			Graph g, Map<Integer,ServiceChain> ChainSet, List<TrafficNodes> pair_list, List<Integer> scUsed, List<FuncPt> vnf_list, 
			List<FuncPt> func_list, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN, ArrayList<BaseVertex> nfv_nodes, ArrayList<BaseVertex> nodesNFVI,
			ArrayList<BaseVertex> vertex_list_without_nfvi_nodes, Map<Integer,ArrayList<Integer>> scCopies, Map<Integer,Integer> scCopyToSC, 
			Map<Integer,ArrayList<HuerVarZ>> configsPerSC, Map<TrafficNodes,SdDetails> configPerSD, Map<Integer,Integer> CountMaxVNF, Map<Integer,Integer> replicaPerVNF, int kNodeCount) throws Exception{    
								  
			/// ######## GENERATE THE CONSTRAINTS ####### ///		
			try{	         	
				         
				 //clear vnfPlacementNodes everytime CG it is run
				 CG.vnfPlacementNodes.clear();
		
		         ///################ PRICING PROBLEM ################///
				 //create the list of pricing problem objects
				 ArrayList<PricingProblem2> pPList = new ArrayList<PricingProblem2>();
//				 ArrayList<LazyPricingProblem2> pPList = new ArrayList<LazyPricingProblem2>();
			     //iterate through the list of service chains
			     for(int scID : scUsed){
					 //create the pricing problem object		    
					 PricingProblem2 pPObject = new PricingProblem2(InputConstants.coreCount, serviceChainTN, vnf_list, scID, ChainSet, nodesNFVI, nfv_nodes, vertex_list_without_nfvi_nodes, g);
//				   	 LazyPricingProblem2 pPObject = new LazyPricingProblem2(InputConstants.coreCount, serviceChainTN, vnf_list, scID, ChainSet, nodesNFVI, nfv_nodes, vertex_list_without_nfvi_nodes, g);
			    	 //set the parallel mode parameter //opportunistic parallel mode
			    	 //pPObject.pricing_problem.setParam(IloCplex.Param.Parallel, -1);
				     //set numerical emphasis
				     //pPObject.pricing_problem.setParam(IloCplex.Param.Emphasis.Numerical, true);
				     //add the pricing problem object to the list
					 pPList.add(pPObject);		    	 
			     }	
		
				 ///################ MASTER PROBLEM ################///			
				 //create the master problem instance	
				 MasterProblem2HueVarZ mP = new MasterProblem2HueVarZ(InputConstants.coreCount, func_list, CountMaxVNF, scUsed, serviceChainTN, configsPerSC, configPerSD, g, nodesNFVI, nfv_nodes, 
						 ChainSet, pair_list, vertex_list_without_nfvi_nodes, pPList, replicaPerVNF, kNodeCount);
				
				 //set the display parameter
				/* System.out.println("Simplex Iterations");
				 mP.master_problem.setParam(IloCplex.IntParam.SimDisplay, 2);*/
				 //set numerical emphasis
				 //mP.master_problem.setParam(IloCplex.Param.Emphasis.Numerical, true);
				 //set the master problem algorithm parameter
				 //mP.master_problem.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Primal);
				 //mP.master_problem.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Dual);
				 //set the master problem simplex optimality constraint
				 //mP.master_problem.setParam(IloCplex.DoubleParam.EpOpt, InputConstants.simplexOptimalityTolerance);	
				 System.out.println("#### Number of Rows after adding initial column : " +  mP.master_problem.getNrows() + " #####");
			     System.out.println("#### Number of Columns after adding initial column : " +  mP.master_problem.getNcols() + " #####");
			     System.out.println("#### Number of Non-zero's after adding initial column : " +  mP.master_problem.getNNZs() + " #####");
			    									 
					 
				 
			     ///################ COLUMN GENERATION ################///
				 //Round Robin Technique 
			     int ppNum = 0;					 
			     //get the start time
				 long cgStartTime = new Date().getTime();						
				 //HashMap to check reduced costs
				 Map<Integer,Boolean> rcRepeatCheck = new HashMap<Integer,Boolean>();
				 for(PricingProblem2 pPObject : pPList){
					 ColumnGeneration2.runIterations(ppNum, serviceChainTN, pPObject, mP, pair_list, ChainSet, vnf_list, func_list, nodesNFVI, nfv_nodes, g);
//					 Boolean rcRepeat = PricingCheck.runIterations(ppNum, serviceChainTN, pPObject, mP, OriginalConfigs, pair_list, ChainSet, vnf_list, func_list, nodesNFVI, nfv_nodes, g);
					 //add the check to the HashMap
					 //rcRepeatCheck.put(ppNum, rcRepeat);
					 //increment the pricing problem number
			     	 ppNum++;																	     	 
				 }
				 
				 ///################ COLUMN GENERATION COMPLETE! ################///
				 //column generation end time
				 long colGenEndTime = new Date().getTime();
				 long timeBeforeILPExecution = colGenEndTime - cgStartTime;	
			     System.out.println("\n\n\n\n\n\n\n");
		         System.out.println("###################Column Generation Complete!##########################");
		         double LPvalue =  mP.master_problem.getObjValue();	
		         rmpLpValue = LPvalue;
		         System.out.println("RMP LP Objective Value : " + LPvalue);
//		         wrt1.write("Final RMP LP Objective Value : " + LPvalue + "\n");
		         //print out CPLEX solving times
		         System.out.println("\n\tTotal RMP Execution Time : " + ColumnGeneration2.execTimePP + "ms");
//		         wrt1.write("\tTotal RMP Execution Time : " + ColumnGeneration2.execTimePP + "ms\n");
		         System.out.println("\tTotal PP Execution Time : " + ColumnGeneration2.execTimePP + "ms");
//		         wrt1.write("\tTotal PP Execution Time : " + ColumnGeneration2.execTimePP + "ms\n");
		         System.out.println("\tTime elapsed till ILP Execution Time : " + timeBeforeILPExecution + "ms");
//		         wrt1.write("\tTime elapsed till ILP Execution Time : " + timeBeforeILPExecution + "ms\n");
		         //Also printing out the positive reduced cost of each variable			       
		         System.out.println("\nPositive reduced cost for Z variables!");
		         for ( Map.Entry<MpVarZ, IloNumVar> entry : mP.usedVarZ.entrySet()  ) {																        	
		        	 System.out.println("New Z No. " + entry.getKey().cgConfig + "\tSC" + entry.getKey().sCiD + "\tConfig " + entry.getKey().configDsc + " = " + mP.master_problem.getReducedCost(entry.getValue())); 													        	 
		         }				         
		         //printing out the number of variables
		         System.out.println("\nNo. of varaibles before ILP execution");
		         System.out.println("\tNo. of Y variables: " + mP.usedVarY.size());
		         System.out.println("\tNo. of X variables: " + mP.usedVarX.size());
		         System.out.println("\tNo. of H variables: " + mP.usedVarH.size());
		         System.out.println("\tNo. of Z variables: " + mP.usedVarZ.size());
		         //get the solution for the LP version of the problem
//		         ReportResults.report1(mP.master_problem, OriginalConfigs, mP.slackVariables, mP.usedVarZ, mP.usedVarX, mP.usedVarY);
		         //hold the converted values
		         //convert to an integer version of the problem
		         //converting the X variables
		         /*for ( Map.Entry<MpVarXc, IloNumVar> var : mP.usedVarX.entrySet() ) {	        	
		        	 mP.master_problem.add(mP.master_problem.conversion(var.getValue(), IloNumVarType.Int));     	 
		         }*/
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
		        	 var.getValue().setUB(1.0);
		        	 mP.master_problem.add(mP.master_problem.conversion(var.getValue(), IloNumVarType.Int));													        	 
		         } 
		         System.out.println("Columns stored for next iteration!");
		         //remove the slack variables from the master problem
		         if(InputConstants.slackVars){
			         for(Map.Entry<String, IloNumVar> entry : mP.slackVariables.entrySet()){
			        	 //remove the slack variables setting upper-bound to 0
			        	 entry.getValue().setUB(0.0);
			        	 //mP.master_problem.delete(entry.getValue());
			         }	
		         }
		         //total number of columns
		         System.out.println("######## Total number of columns : " + mP.master_problem.getNcols() + " #########");
		         //total number of rows
		         System.out.println("######## Total number of rows : " + mP.master_problem.getNrows() + " ##########");     
		         //export the master problem
//				 mP.master_problem.exportModel(InputConstants.NFV_Strategy + "_" + traffic + "_" + coreCount + "_master_problem_final.lp");		
//				 mP.master_problem.exportModel(InputConstants.NFV_Strategy + "_" + traffic + "_" + coreCount + "_master_problem_final.sav");
//				 mP.master_problem.exportModel(InputConstants.NFV_Strategy + "_NoCoreNoLinkCapacity" + "_master_problem_final.lp");
//				 mP.master_problem.exportModel(InputConstants.NFV_Strategy + "_NoCoreNoLinkCapacity" + "_master_problem_final.sav");
			     //set the integrality, optimality and feasibliity constraints
			     //setting the intergrality tolerance
//				 mP.master_problem.setParam(IloCplex.DoubleParam.EpInt, InputConstants.integerTolerance);
				 //setting the optimality tolerance
//				 mP.master_problem.setParam(IloCplex.DoubleParam.EpOpt, InputConstants.simplexOptimalityTolerance);
				 //setting the feasiblity tolerance
//				 mP.master_problem.setParam(IloCplex.DoubleParam.EpRHS, InputConstants.feasbilityTolerance);
		         //use the feasibility repair schemes
				 
				 
				 
				 
				  //ILP start time
		         long ilpStartTime = new Date().getTime();
		         //solve the integer version of the problem
		         mP.master_problem.solve();	         
		         //ILP end time
		         long ilpEndTime = new Date().getTime();
		         //solve time
		         long solveTime = ilpEndTime - ilpStartTime;
		         //ILP solve time
		         long ilpSolveTime = solveTime;																         																         
		         //get the end time for CG execution
		         long cgEndTime = ilpEndTime;
		         //get the execution time
				 long cgExecTime = cgEndTime - cgStartTime;																		 
				 //value of result
				 double objValue = mP.master_problem.getObjValue();
				 //get the ILP object value
				 rmpIlpValue = objValue;
				 //e-optimality percentage
				 double eOptimalPercentage = ((objValue - LPvalue)*100)/LPvalue;
		         //report the results of the master problem	
				 //report the slack variables that have been set
				 ReportResults.reportSlackVarsInIlp(mP.master_problem,mP.slackVariables);
		         //get the solution for the ILP version of the problem
		         ReportResults2.reportIntegers(mP.master_problem, mP.usedVarZ, mP.usedVarX, mP.usedVarY,mP.usedVarH);
		         //get the VNF placement nodes
		         getPlacementNodes(mP.master_problem,mP.usedVarH);
		         //print put the traffic pairs; their SCs and their traffic
		         preProcFunctions.printSDpairsSCandTraffic(scUsed, serviceChainTN);	
		         //get the core count and link capacity to be used
		         CpuLinkCapacity.calCpuCapacity(mP.master_problem, mP.usedVarZ, cpuCoreCount);
		         if(cpuCoreCount.isEmpty()){
		        	 System.out.println("########No CPU core counts recorded!#######");
		         }
		         CpuLinkCapacity.calLinkCapacity(mP.master_problem,mP.usedVarZ,mP.usedVarY,linkCapacity);
		         if( linkCapacity.isEmpty()){
		        	 System.out.println("########No link capacity recorded!#######");
		         }
		         System.out.println("\tRMP ILP Execution Time : " + ilpSolveTime + "ms");				        
		       //find total cplex time
		         double cplexTime = ColumnGeneration2.execTimeRMP + ColumnGeneration2.execTimePP + ilpSolveTime;
		         System.out.println("\tCPLEX Total time = " + cplexTime + "ms");
		         //total cplex time before CG
		    	 cgRunTime = ColumnGeneration2.execTimeRMP + ColumnGeneration2.execTimePP;
		    	 //total cplex time for ILP
		    	 ilpRunTime = ilpSolveTime;
		    	 //total time for CG
		    	 totalTime = cgExecTime;
		    	 //time spent in java code
		         double javaTime = (cgExecTime - cplexTime);
		         System.out.println("\tJava Time = " + javaTime);				      
		         System.out.println("\n\t(s,d) pairs = " + pair_list.size() + " ; Initial Configs = " + serviceChainTN.keySet().size());																         
		         //report the MIP Gap
		         System.out.println("\n####### E-Optimal : " + eOptimalPercentage  + " ; CG Execution Time : " + cgExecTime + 
		        		 " ; MIP Relative Gap Tolerance : " + mP.master_problem.getMIPRelativeGap()  + " ########");		         
		         for(Map.Entry<BaseVertex,Double> entryCPU : cpuCoreCount.entrySet()){
					 System.out.println("CPU Cores for Vertex ID : " + entryCPU.getKey().get_id() + " = " + entryCPU.getValue()/1000);
				 }
				 //iterate through link capacities					
				 for(Map.Entry<NodePair,Double> entryLink : linkCapacity.entrySet()){
					 //get the bandwidth values in Mbps //convert to Gbps
					 System.out.println("Capacity for Link : (" + entryLink.getKey().v1.get_id() + "," + entryLink.getKey().v2.get_id() + ") = " + entryLink.getValue()/1000);
				 }
				 //assign the eOptimalityPercentage
		         eoptimality = eOptimalPercentage;
		         //get the basis status for the variables
		         //ReportResults.basisCheckForVariables(master_problem, OriginalConfigs, usedVarZ, usedVarX, usedVarY);
		         //deallocate the master problem CPLEX object		         
		         mP.master_problem.end();		      
		         //deallocate the pricing problem CPLEX object
		         for(PricingProblem2 pP : pPList){
//						 for(LazyPricingProblem2 pP : pPList){
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
