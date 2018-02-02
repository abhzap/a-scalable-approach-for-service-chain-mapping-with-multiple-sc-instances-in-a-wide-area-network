package colGen.model.ver2;

import ilog.concert.IloColumn;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloRange;

import java.util.ArrayList;
import java.util.Collections;
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
import colGen.model.result.ReportResults2;
import colGen.model.ver1.CheckFunctions;
import colGen.model.ver1.ColumnGeneration;
import colGen.model.ver1.MpCstr7and11;
import colGen.model.ver1.MpCstr81012and14;
import colGen.model.ver1.MpVarX;
import colGen.model.ver1.MpVarZ;
import colGen.model.ver1.NewIngEgConfigs;
import colGen.model.ver1.PpVarA;
import colGen.model.ver1.PpVarB;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class ColumnGeneration2 {
	
	public static long javaTime;
	public static long execTimeRMP;
	public static long execTimePP;
	
	/*public static String runIterations(int ppNum, Map<Integer, ArrayList<TrafficNodes>> serviceChainTN, PricingProblem2 pPObject, MasterProblem2 mP,  
			Map<TrafficNodes, ArrayList<NewIngEgConfigs>> OriginalConfigs, List<TrafficNodes> pair_list, Map<Integer, ServiceChain> ChainSet, 
			List<FuncPt> vnf_list,List<FuncPt> func_list, ArrayList<BaseVertex> nodesNFVI, ArrayList<BaseVertex> nfv_nodes, Graph g) throws Exception{*/
    
	public static String runIterations(int ppNum, Map<Integer, ArrayList<TrafficNodes>> serviceChainTN, PricingProblem2 pPObject, MasterProblem2HueVarZ mP,  
			List<TrafficNodes> pair_list, Map<Integer, ServiceChain> ChainSet, List<FuncPt> vnf_list,List<FuncPt> func_list, ArrayList<BaseVertex> nodesNFVI, 
			ArrayList<BaseVertex> nfv_nodes, Graph g) throws Exception{	
		
	/*public static String runIterations(int ppNum, Map<Integer, ArrayList<TrafficNodes>> serviceChainTN, LazyPricingProblem2 pPObject,  MasterProblem2HueVarZ mP,  
			List<TrafficNodes> pair_list, Map<Integer, ServiceChain> ChainSet, List<FuncPt> vnf_list, List<FuncPt> func_list,
			ArrayList<BaseVertex> nodesNFVI, ArrayList<BaseVertex> nfv_nodes, Graph g) throws Exception{*/
		
		ColumnGeneration2.execTimeRMP = 0L;
		ColumnGeneration2.execTimePP = 0L;
		
		//check if the reduced cost repeats in this configuration
		boolean rcRepeat = false;
		
		//String to be sent out
		String solveTime = "\n";
		
		//service chain being used
		int serviceChainID = pPObject.scIDpP;		
		//set of SD pairs using a service chain 'c'
		ArrayList<TrafficNodes> sdPairs = serviceChainTN.get(serviceChainID);	
		
		//create the list of functions for this service chain
    	ServiceChain sC = ChainSet.get(pPObject.scIDpP);
    	int scSize = sC.chain_size;
    	ArrayList<FuncPt> func_list_sc = new ArrayList<FuncPt>();
    	//iterate through the VNF list for the model instance
    	for(FuncPt vnf : vnf_list){
    		//check if the vnf is part of the service chain
    		if(sC.chain_seq.contains(vnf.getid())){
    			//add the vnf to the function list
    			func_list_sc.add(vnf); 
    		}
    	}
    	
    	//List of VNFs sequentially for this particular service chain
		ArrayList<FuncPt> scVNF = new ArrayList<FuncPt>();
		//iterate through the service chain //find the VNFs
		for(int vnfID : sC.chain_seq){
			for(FuncPt fp : vnf_list){
				if(fp.getid() == vnfID){
					scVNF.add(fp);
				}				
			}
		}
		
		
		//keep track of master problem objective
		double prevMpObj = 0.0;
		
		//keep track of reduced costs
		double prevPpRC = 0.0;
		String prevConfig = null;
		double ppReducedCost = 0.0;
		String ppConfig = null;
			
	 	//solution vectors of the sub-problem are stored here.
	 	//corresponding to the number of variables in the sub-problem			    
	 	/// COLUMN-GENERATION PROCEDURE ///
	 	//loop until conditions are satisfied
		//for(int i=1;i<InputConstants.simeplexInterationLimit + 2;i++){
		for(int i=1;;i++){
			/// OPTIMIZE OVER CURRENT CONFIGURATIONS ///        	
			//name of CG file to be exported
			String cgFileNameLP = "master_problem_cg_enhanced_" + i + "_PP" + ppNum + ".lp";
			/*String cgFileNameSAV = "master_problem_cg_enhanced_" + i + "_PP" + ppNum + ".sav";    
			String cgFileNameMPS = "master_problem_cg_enhanced_" + i + "_PP" + ppNum + ".mps";*/    
		    //export the master problem
			mP.master_problem.exportModel(cgFileNameLP);	
			/*mP.master_problem.exportModel(cgFileNameSAV);
			mP.master_problem.exportModel(cgFileNameMPS);*/
			
						
			//SOLVE THE MASTER PROBLEM	
			//set the master problem algorithm parameter
			//mP.master_problem.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Primal);
			//tune parameters to improve performance			
			//get the start time
			long rmpStartTime = new Date().getTime();
			//solve RMP
            mP.master_problem.solve();
            //get the end time for the execution
            long rmpEndTime = new Date().getTime();
	        //get the solution time
            long rmpExecTime = (-rmpStartTime + rmpEndTime);
			execTimeRMP += rmpExecTime;//in seconds
			//gte currObjVal
            double currObjVal = mP.master_problem.getObjValue();
            //check if RMP objective keeps reducing
            if( i>1 && (currObjVal - prevMpObj > 0.1) ){
            	System.out.println("Previous RMP Objective Value = " + prevMpObj);
            	System.out.println("Current RMP Objective Value = " + currObjVal);
            	System.err.println("!!!!!Master Problem Objective is larger after ADDING COLUMN!!!!!");
            	System.out.println("!!!!!Master Problem Objective is larger after ADDING COLUMN!!!!!");
            	//throw new Exception("!!!!!Master Problem Objective is larger after ADDING COLUMN!!!!!");
            }
            prevMpObj = currObjVal;
            //total number of columns
            /*System.out.println("######## Total number of columns : " + mP.master_problem.getNcols() + " #########");
            //total number of rows
            System.out.println("######## Total number of rows : " + mP.master_problem.getNrows() + " ##########");*/           
            //get the solution for the variables
            ReportResults2.report1(mP.master_problem, mP.slackVariables, mP.usedVarZ, mP.usedVarX, mP.usedVarY, mP.usedVarH);	
            //if( i == InputConstants.simeplexInterationLimit + 1 ){
	            //get the reduced cost for all the variables
//	            ReportResults2.reducedCostperVariable(i, ppReducedCost, mP.master_problem, mP.usedVarZ, mP.usedVarX, mP.usedVarY);	
            //}
            //get the basis status for the variables
//			ReportResults2.basisCheckForVariables(mP.master_problem, mP.usedVarZ, mP.usedVarX, mP.usedVarY);		            
            //find the dual of the configuration selection constraint - (3)
            //set it as the constant for the reduced cost linear expression	
            double dualVal45 = mP.master_problem.getDual(mP.config_selection_constraint.get(pPObject.scIDpP));
            String cstrName = mP.config_selection_constraint.get(pPObject.scIDpP).getName();//constraint Name
            String cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID          
            IloLinearNumExpr exprForReducedCost = pPObject.pricing_problem.linearNumExpr(Math.abs(dualVal45));
            
//            if( dualVal45 != 0.0 ){    			
//				System.out.println("\t\tDual value for " + cstrID + " SC:" + serviceChainID + " configuration selection constraint = " + dualVal45 );
//			}
  
            
            //Fixing the COST GAMMA FUNCTION				           	
			//iterating through traffic nodes for 
        	//SD pair, service chain and flow_traffic
            System.out.println("#### Adding Q Variables in Cost Gamma Function ####");
        	for(TrafficNodes tn : sdPairs ){
        		//iterate through the variables
        		for(Map.Entry<Pp2VarQ, IloIntVar> entryQ : pPObject.Used_var_q_sd_il.entrySet()){        			
        			//check traffic pairs are the same
        			if(tn.equals(entryQ.getKey().sd)){
						//taking absolute value of the possible negative dual values
						exprForReducedCost.addTerm( entryQ.getValue(), tn.flow_traffic);
        			}
        		}
        	}  
        	System.out.println("#### Q Variables added to Cost Gamma Function ####");
            //create hashMaps to store dual values*coefficients
            /*Map<PpVarA, Double> dvFuncInstCountConst = new HashMap<PpVarA, Double>(); //4
            Map<Pp2VarP, Double> dvCoreCapacityConst = new HashMap<Pp2VarP, Double>(); //6
            Map<Pp2VarQ, Double> dvFlowCapacityConst = new HashMap<Pp2VarQ, Double>(); //7
            Map<Pp2VarQ, Double> dvLatencyConst = new HashMap<Pp2VarQ, Double>(); //8
            Map<Pp2VarDelta, Double> dvOnePathPerSdConst = new HashMap<Pp2VarDelta, Double>(); //9
            Map<Pp2VarP, Double> dvSrcOutgoigConst = new HashMap<Pp2VarP, Double>(); //10
            Map<Pp2VarP, Double> dvSrcIncomingConst = new HashMap<Pp2VarP, Double>(); //11
            Map<Pp2VarP, Double> dvFlowForFirst2 = new HashMap<Pp2VarP, Double>(); //12
            Map<Pp2VarP, Double> dvDstIncomingConst = new HashMap<Pp2VarP, Double>(); //14
            Map<Pp2VarP, Double> dvDstOutgoingConst = new HashMap<Pp2VarP, Double>(); //15
            Map<Pp2VarP, Double> dvFlowForLast2 = new HashMap<Pp2VarP, Double>(); //16  
*/            //function instance count constraint - (4) 
            for(Map.Entry<MpVarX,IloRange> entryCstr : mP.function_instance_count.entrySet()){
            	//dual value of the constraint
            	double dualVal = mP.master_problem.getDual(entryCstr.getValue());
            	cstrName = entryCstr.getValue().getName();//constraint Name
            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
            	//details for getting appropriate variable A
            	BaseVertex nfviNode = entryCstr.getKey().v;
            	int fID = entryCstr.getKey().f_id;
            	//iterate through the chain sequence
            	for(int f_seq_index = 0; f_seq_index < scSize; f_seq_index++){
            		//match function ID
            		if(fID == scVNF.get(f_seq_index).getid()){
            			//make key for variable A
            			PpVarA tempKey = new PpVarA(nfviNode,f_seq_index);
            			//get the actual variable A
            			IloIntVar varA = pPObject.Used_var_a_vi.get(tempKey);
            			//if the variable A does not exist
            			if(varA == null){
            				System.out.println(cstrName + " - Dual value of A not found!");
            			}
            			//add in the reduced cost expression
            			exprForReducedCost.addTerm(varA, Math.abs(dualVal));
            			//add the dual value to Hash Map
//            			dvFuncInstCountConst.put(tempKey, Math.abs(dualVal));
            		}
            	}
        		//print only if the dual value is non-zero
//    			if( Math.abs(dualVal) != 0.0 ){    			
//    				System.out.println("\t\tDual value for " + cstrID + " function_instance_count[ Node: " + nfviNode.get_id() + "; VNF: " + fID + "] = " + dualVal );
//    			}
            }
            //function instance count constraint (4)-(2) 
            for(Map.Entry<MpVarX,IloRange> entryCstr : mP.function_instance_count2.entrySet()){
            	//dual value of the constraint
            	double dualVal = mP.master_problem.getDual(entryCstr.getValue());
            	cstrName = entryCstr.getValue().getName();//constraint Name
            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
            	//details for getting appropriate variable A
            	BaseVertex nfviNode = entryCstr.getKey().v;
            	int fID = entryCstr.getKey().f_id;
            	//iterate through the chain sequence
            	for(int f_seq_index = 0; f_seq_index < scSize; f_seq_index++){
            		//match function ID
            		if(fID == scVNF.get(f_seq_index).getid()){
            			//make key for variable A
            			PpVarA tempKey = new PpVarA(nfviNode,f_seq_index);
            			//get the actual variable A
            			IloIntVar varA = pPObject.Used_var_a_vi.get(tempKey);
            			//if the variable A does not exist
            			if(varA == null){
            				System.out.println(cstrName + " - Dual value of A not found!");
            			}
            			//add in the reduced cost expression
            			exprForReducedCost.addTerm(varA, -1.0*Math.abs(dualVal));            			
            		}
            	}
        		//print only if the dual value is non-zero
//    			if( Math.abs(dualVal) != 0.0 ){    			
//    				System.out.println("\t\tDual value for " + cstrID + " function_instance_count2[ Node: " + nfviNode.get_id() + "; VNF: " + fID + "] = " + dualVal );
//    			}
            }
            //Core constraint needs to be imposed
            if(InputConstants.coreCstr){
	            //core capacity constraint - (6)
	            for(int nfv_node_index=0; nfv_node_index < nfv_nodes.size(); nfv_node_index++){
	            	//dual value of constraint
	            	double dualVal = mP.master_problem.getDual(mP.core_capacity_constraint.get(nfv_node_index));
	            	cstrName = mP.core_capacity_constraint.get(nfv_node_index).getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
	            	//details required
	            	BaseVertex nfvNode = nfv_nodes.get(nfv_node_index);	
	            	//iterate through the list of traffic nodes
	            	for(TrafficNodes sd : serviceChainTN.get(serviceChainID)){
	            		//iterate through the list of functions
	            		for(int f_seq_index = 0; f_seq_index < scSize; f_seq_index++){
	            			//get core value
	            			double fCore = scVNF.get(f_seq_index).getcore();
	            			//make the P variable key
	            			Pp2VarP tempKey = new Pp2VarP(sd,f_seq_index,nfvNode);
	            			//get the actual variable P
	            			IloIntVar varP = pPObject.Used_var_p_sd_vi.get(tempKey);
	            			//if the variable X does not exist
	            			if(varP == null){
	            				System.out.println(cstrName + " - Dual value of P not found!");
	            			}
	            			//add in the reduced cost expression
	            			exprForReducedCost.addTerm(varP, Math.abs(dualVal)*sd.flow_traffic*fCore);
	            			//add the dual value to Hash Map
//	            			dvCoreCapacityConst.put(tempKey, Math.abs(dualVal)*sd.flow_traffic*fCore);
	            		}
	            	}        	
	        		//print only if the dual value is non-zero
//	    			if( Math.abs(dualVal) != 0.0 ){    				
//	    				System.out.println("\t\tDual value " + cstrID + " core_capacity_constraint[" + nfv_node_index + "] (Node" + nfvNode.get_id() + " = " + dualVal );
//	    			}
	            } 
            }
            //Link capacity constraints
            if(InputConstants.capacityCstr){
	            //link capacity constraint - (7)
	            int link_num = 0;//reset link number
	            for(BaseVertex s_vert : g._vertex_list){ 
					for(BaseVertex t_vert : g.get_adjacent_vertices(s_vert)){
						//dual value of constraint
						double dualVal = mP.master_problem.getDual(mP.flow_capacity_constraint.get(link_num));
						cstrName = mP.flow_capacity_constraint.get(link_num).getName();//constraint Name
		            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
						//iterate through SD pairs
		            	for( TrafficNodes sd : serviceChainTN.get(serviceChainID) ){
		            		//iterate through VNFs sequentially
		            		for(int f_seq_index = 0; f_seq_index < scSize-1; f_seq_index++){
		            			//make the Q variable key
		            			Pp2VarQ tempKey = new Pp2VarQ(sd,f_seq_index,s_vert,t_vert,serviceChainID);
		            			//get the actual variable Q
		            			IloIntVar varQ = pPObject.Used_var_q_sd_il.get(tempKey);
		            			//if the variable Q does not exist
		            			if(varQ == null){
		            				System.out.println(cstrName + " - Dual value of Q not found!");
		            			}
		            			//add in the reduced cost expression
		            			exprForReducedCost.addTerm(varQ, Math.abs(dualVal)*sd.flow_traffic);
		            			//add the dual value to Hash Map
//		            			dvFlowCapacityConst.put(tempKey, Math.abs(dualVal)*sd.flow_traffic);
		            		}
		            	}            	
		            	//print only if the dual value is non-zero
//						if( Math.abs(dualVal) != 0.0 ){
							//source vertex for the link
							int srcVrt = s_vert.get_id();
							//destination vertex for the link
							int destVrt = t_vert.get_id();
//							System.out.println("\t\tDual value for " + cstrID + " flow_capacity_constraint[" + link_num + "] (Link_Src" + srcVrt + "_Dst" + destVrt + ") = " + dualVal );
//						}
		            	//increment the link counter
						link_num++;
					}
	            }
            }
            //latency constraint - (8)
        	if(InputConstants.latencyCstr){
	            for(Map.Entry<MpCstr7and11, IloRange> entryCstr : mP.flow_latency_constraint.entrySet()){
	            	//check service chain ID
	            	if(entryCstr.getKey().chainIndex == serviceChainID){
		            	//dual value of constraint
						double dualVal = mP.master_problem.getDual(entryCstr.getValue());
						cstrName = entryCstr.getValue().getName();//constraint Name
		            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
		            	TrafficNodes sd = entryCstr.getKey().tn;
		            	//iterate over links
		            	for(BaseVertex source : g._vertex_list){ 
		    				for(BaseVertex sink : g.get_adjacent_vertices(source)){
		    					double lProp = InputConstants.SEC_TO_MICROSEC*g.get_edge_length(source, sink)/InputConstants.SPEED_OF_LIGHT;	    					
		    					//iterate through VNFs sequentially
			            		for(int f_seq_index = 0; f_seq_index < scSize-1; f_seq_index++){
			            			//make the Q variable key
			            			Pp2VarQ tempKey = new Pp2VarQ(sd,f_seq_index,source,sink,serviceChainID);
			            			//get the actual variable Q
			            			IloIntVar varQ = pPObject.Used_var_q_sd_il.get(tempKey);
			            			//if the variable Q does not exist
			            			if(varQ == null){
			            				System.out.println(cstrName + " - Dual value of Q not found!");
			            			}
			            			//add in the reduced cost expression
			            			exprForReducedCost.addTerm(varQ, Math.abs(dualVal)*lProp);
			            			//add the dual value to Hash Map
//			            			dvLatencyConst.put(tempKey, Math.abs(dualVal)*lProp);
			            		}
		    				}
		            	}
		            	//print only if the dual value is non-zero
	//	            	if( dualVal != 0.0 ){
//							System.out.println("\t\tDual value for " + cstrID + " latency_constraint[" + entryCstr.getKey().tn.toString() + "] = " + dualVal );
	//					}
	            	}
	            }
        	}
            //one path per sd and c constraint - (9)
            //iterate through SD pairs
            for(Map.Entry<MpCstr7and11, IloRange> entryCstr : mP.path_per_c_sd_contraint.entrySet()){
            	//check service chain ID
            	if(entryCstr.getKey().chainIndex == serviceChainID){
	            	//dual value of constraint
					double dualVal = mP.master_problem.getDual(entryCstr.getValue());
					cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
	            	//make the D variable key
	    			Pp2VarDelta tempKey = new Pp2VarDelta(entryCstr.getKey().tn);
	    			//get the actual variable D
	    			IloIntVar varD = pPObject.Used_var_d_sd.get(tempKey);
	    			//if the variable D does not exist
	    			if(varD == null){
        				System.out.println(cstrName + " - Dual value of D not found!");
        			}
	    			//add in the reduced cost expression
	    			exprForReducedCost.addTerm(varD, -1.0*dualVal);
	    			//add the dual value to Hash Map
//	    			dvOnePathPerSdConst.put(tempKey, -1.0*dualVal);   
	    			//print only if the dual value is non-zero
//					if( dualVal != 0.0 ){
//						System.out.println("\t\tDual value for " + cstrID + " path_per_c_sd_contraint[" + entryCstr.getKey().tn.toString() + "] = " + dualVal );
//					}
            	}
            }
            //src_outgoing_constraint - (10)
            //iterate through SD pairs
            for(Map.Entry<MpCstr7and11, IloRange> entryCstr : mP.src_outgoing_constraint.entrySet()){
            	//check service chain ID
            	if(entryCstr.getKey().chainIndex == serviceChainID){
	            	//dual value of constraint
					double dualVal = mP.master_problem.getDual(entryCstr.getValue());
					cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
					//details required
					TrafficNodes sd = entryCstr.getKey().tn;
				    BaseVertex srcVrt = sd.v1;
					//source Node is a NFVI node
					if(nodesNFVI.contains(srcVrt)){
		            	//make the P variable key
		    			Pp2VarP tempKey = new Pp2VarP(sd,0,srcVrt);
		    			//get the actual variable P
		    			IloIntVar varP = pPObject.Used_var_p_sd_vi.get(tempKey);
		    			//if the variable P does not exist
		    			if(varP == null){
	        				System.out.println(cstrName + " - Dual value of P not found!");
	        			}
		    			//add in the reduced cost expression
		    			exprForReducedCost.addTerm(varP, -1.0*dualVal);
		    			//add the dual value to Hash Map
//		    			dvSrcOutgoigConst.put(tempKey, -1.0*dualVal);
		    			//print only if the dual value is non-zero
//						if( dualVal != 0.0 ){
//							System.out.println("\t\tDual value for " + cstrID + " src_outgoing_constraint[" + entryCstr.getKey().tn.toString() + "] = " + dualVal );
//						}
					}
            	}
            }
            //src_incoming_constraint (11)
            for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : mP.src_incoming_constraint.entrySet()){
            	//check service chain ID
            	if(entryCstr.getKey().chainIndex == serviceChainID){
	            	//dual value of constraint
					double dualVal = mP.master_problem.getDual(entryCstr.getValue());
					cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
					//details required
					TrafficNodes sd = entryCstr.getKey().tn;
				    BaseVertex nfviNode = entryCstr.getKey().nfvi_node;			
	            	//make the P variable key
	    			Pp2VarP tempKey = new Pp2VarP(sd,0,nfviNode);
	    			//get the actual variable P
	    			IloIntVar varP = pPObject.Used_var_p_sd_vi.get(tempKey);
	    			//if the variable P does not exist
	    			if(varP == null){
        				System.out.println(cstrName + " - Dual value of P not found!");
        			}
	    			//add in the reduced cost expression
	    			exprForReducedCost.addTerm(varP, Math.abs(dualVal));
	    			//add the dual value to Hash Map
//	    			dvSrcIncomingConst.put(tempKey, Math.abs(dualVal));				
	    			//print only if the dual value is non-zero
//					if( Math.abs(dualVal) != 0.0 ){
//						System.out.println("\t\tDual value for " + cstrID + " src_incoming_constraint[" + entryCstr.getKey().tn.toString() + " ; Node:" + nfviNode.get_id() + "] = " + dualVal );
//					}
            	}
            }
            //flow conservation constraint - placement - ingress node (12)
            for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : mP.flow_place_ing_constraint.entrySet()){
            	//check service chain ID
            	if(entryCstr.getKey().chainIndex == serviceChainID){
	            	//dual value of constraint
					double dualVal = mP.master_problem.getDual(entryCstr.getValue());
					cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
					//details required
					TrafficNodes sd = entryCstr.getKey().tn;
				    BaseVertex nfviNode = entryCstr.getKey().nfvi_node;			
	            	//make the P variable key
	    			Pp2VarP tempKey = new Pp2VarP(sd,0,nfviNode);
	    			//get the actual variable P
	    			IloIntVar varP = pPObject.Used_var_p_sd_vi.get(tempKey);
	    			//if the variable P does not exist
	    			if(varP == null){
        				System.out.println(cstrName + " - Dual value of P not found!");
        			}
	    			//add in the reduced cost expression
	    			exprForReducedCost.addTerm(varP, -1.0*dualVal);
	    			//add the dual value to Hash Map
//	    			dvFlowForFirst2.put(tempKey, -1.0*dualVal);				
	    			//print only if the dual value is non-zero
//					if( dualVal != 0.0 ){
//						System.out.println("\t\tDual value for " + cstrID + " flow_place_ing_constraint[" + entryCstr.getKey().tn.toString() + " ; Node:" + nfviNode.get_id() + "] = " + dualVal );
//					}
            	}
            }
            //dest_incoming_constraint - (14)
            //iterate through SD pairs
            for(Map.Entry<MpCstr7and11, IloRange> entryCstr : mP.dest_incoming_constraint.entrySet()){
            	//check service chain ID
            	if(entryCstr.getKey().chainIndex == serviceChainID){
	            	//dual value of constraint
					double dualVal = mP.master_problem.getDual(entryCstr.getValue());
					cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
					//details required
					TrafficNodes sd = entryCstr.getKey().tn;
				    BaseVertex dstVrt = sd.v2;
					//source Node is a NFVI node
					if(nodesNFVI.contains(dstVrt)){
		            	//make the P variable key
		    			Pp2VarP tempKey = new Pp2VarP(sd,scSize-1,dstVrt);
		    			//get the actual variable P
		    			IloIntVar varP = pPObject.Used_var_p_sd_vi.get(tempKey);
		    			//if the variable P does not exist
		    			if(varP == null){
	        				System.out.println(cstrName + " - Dual value of P not found!");
	        			}
		    			//add in the reduced cost expression
		    			exprForReducedCost.addTerm(varP, -1.0*dualVal);
		    			//add the dual value to Hash Map
//		    			dvDstIncomingConst.put(tempKey, -1.0*dualVal);
		    			//print only if the dual value is non-zero
//						if( dualVal != 0.0 ){
//							System.out.println("\t\tDual value for " + cstrID + " dest_incoming_constraint[" + entryCstr.getKey().tn.toString() + "] = " + dualVal );
//						}
					}
            	}
            }
            //dest_outgoing_constraint (15)
            for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : mP.dest_outgoing_constraint.entrySet()){
            	//check service chain ID
            	if(entryCstr.getKey().chainIndex == serviceChainID){
	            	//dual value of constraint
					double dualVal = mP.master_problem.getDual(entryCstr.getValue());
					cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
					//details required
					TrafficNodes sd = entryCstr.getKey().tn;
				    BaseVertex nfviNode = entryCstr.getKey().nfvi_node;			
	            	//make the P variable key
	    			Pp2VarP tempKey = new Pp2VarP(sd,scSize-1,nfviNode);
	    			//get the actual variable P
	    			IloIntVar varP = pPObject.Used_var_p_sd_vi.get(tempKey);
	    			//if the variable P does not exist
	    			if(varP == null){
        				System.out.println(cstrName + " - Dual value of P not found!");
        			}
	    			//add in the reduced cost expression
	    			exprForReducedCost.addTerm(varP, Math.abs(dualVal));
	    			//add the dual value to Hash Map
//	    			dvDstOutgoingConst.put(tempKey, Math.abs(dualVal));				
	    			//print only if the dual value is non-zero
//					if( Math.abs(dualVal) != 0.0 ){
//						System.out.println("\t\tDual value for " + cstrID + " dest_outgoing_constraint[" + entryCstr.getKey().tn.toString() + " ; Node:" + nfviNode.get_id() + "] = " + dualVal );
//					}
            	}
            }
            //flow conservation constraint - placement - egress node (16)
            for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : mP.flow_place_egr_constraint.entrySet()){
            	//check service chain ID
            	if(entryCstr.getKey().chainIndex == serviceChainID){
	            	//dual value of constraint
					double dualVal = mP.master_problem.getDual(entryCstr.getValue());
					cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
					//details required
					TrafficNodes sd = entryCstr.getKey().tn;
				    BaseVertex nfviNode = entryCstr.getKey().nfvi_node;			
	            	//make the P variable key
	    			Pp2VarP tempKey = new Pp2VarP(sd,scSize-1,nfviNode);	    			
	    			//get the actual variable P
	    			IloIntVar varP = pPObject.Used_var_p_sd_vi.get(tempKey);
	    			//if the variable P does not exist
	    			if(varP == null){
        				System.out.println(cstrName + " - Dual value of P not found!");
        			}
	    			//add in the reduced cost expression
	    			exprForReducedCost.addTerm(varP, -1.0*dualVal);
	    			//add the dual value to Hash Map
//	    			dvFlowForLast2.put(tempKey, -1.0*dualVal);				
	    			//print only if the dual value is non-zero
//					if( dualVal != 0.0 ){
//						System.out.println("\t\tDual value for " + cstrID  + " flow_place_egr_constraint[" + entryCstr.getKey().tn.toString() + " ; Node:" + nfviNode.get_id() + "] = " + dualVal );
//					}
            	}
            }   
            System.out.println("\n");
            
            
            
            
            ///SET THE EXPPRESSION FOR THE REDUCED COST///
        	pPObject.reducedCost.setExpr(exprForReducedCost);        
            //name of CG file to be exported
            String ppFileNameLP = "pp_" + ppNum + "_iteration_" + i + ".lp" ;
            String ppFileNameSAV = "pp_" + ppNum + "_iteration_" + i + ".sav" ;
            String ppFileNameMPS = "pp_" + ppNum + "_iteration_" + i + ".mps" ;
            //export the pricing_problem
            /*pPObject.pricing_problem.exportModel(ppFileNameLP);
            pPObject.pricing_problem.exportModel(ppFileNameSAV);
            pPObject.pricing_problem.exportModel(ppFileNameMPS);*/
        	//tune parameters to improve performance
            //pPObject.pricing_problem.tuneParam();
            //get the start time
        	long ppStartTime = new Date().getTime();
			//solve the pricing problem
            //pPObject.pricing_problem.solve();
            pPObject.pricing_problem.solve();
            long ppEndTime = new Date().getTime();
            long ppExecutionTime = (ppEndTime -ppStartTime);
            execTimePP += ppExecutionTime;
            //store the objective value of the pricing problem (reduced cost)
            ppReducedCost = pPObject.pricing_problem.getObjValue();
            //print the results
            ReportResults2.report2(pPObject.pricing_problem, pPObject.Used_var_d_sd, pPObject.Used_var_a_vi, pPObject.Used_var_b_l_sigma_i_i1, 
            		pPObject.Used_var_p_sd_vi, pPObject.Used_var_q_sd_il);
            
            //CHECK OPTIMALITY CONDITION
            if( pPObject.pricing_problem.getObjValue() > -InputConstants.RC_EPS )
              break;				          
            //store the solution of the sub-problem 
            Map<Pp2VarDelta, Double> newConfig_var_d_sd = new HashMap<Pp2VarDelta, Double>(); 
            for( Map.Entry<Pp2VarDelta, IloIntVar> entryD : pPObject.Used_var_d_sd.entrySet()){
            	newConfig_var_d_sd.put(entryD.getKey(), pPObject.pricing_problem.getValue(entryD.getValue()));
            }
            Map<Pp2VarP, Double> newConfig_var_p_sd_vi = new HashMap<Pp2VarP, Double>();
            for( Map.Entry<Pp2VarP, IloIntVar> entryP : pPObject.Used_var_p_sd_vi.entrySet()){
            	newConfig_var_p_sd_vi.put(entryP.getKey(), pPObject.pricing_problem.getValue(entryP.getValue()));
            }
            Map<Pp2VarQ, Double> newConfig_var_q_sd_li = new HashMap<Pp2VarQ, Double>();
            for( Map.Entry<Pp2VarQ, IloIntVar> entryQ : pPObject.Used_var_q_sd_il.entrySet()){
            	newConfig_var_q_sd_li.put(entryQ.getKey(), pPObject.pricing_problem.getValue(entryQ.getValue()));
            }
            Map<PpVarA, Double> newConfig_var_a_v_sigma = new HashMap<PpVarA, Double>(); 
            for( Map.Entry<PpVarA, IloIntVar> entryA : pPObject.Used_var_a_vi.entrySet() ){
            	newConfig_var_a_v_sigma.put(entryA.getKey(), pPObject.pricing_problem.getValue(entryA.getValue()));
            }
            Map<PpVarB, Double> newConfig_var_b_l_sigma_i_i1 = new HashMap<PpVarB, Double>(); 
            for( Map.Entry<PpVarB, IloIntVar> entryB : pPObject.Used_var_b_l_sigma_i_i1.entrySet() ){
            	newConfig_var_b_l_sigma_i_i1.put(entryB.getKey(), pPObject.pricing_problem.getValue(entryB.getValue()));
            }
            
            
            //Declare New Configuration Object
            //Configuration Delta variables
            Set<Pp2VarDelta> DeltaVarSet = new HashSet<Pp2VarDelta>();
            //iterate through the D variables
            for(Map.Entry<Pp2VarDelta, Double> entry : newConfig_var_d_sd.entrySet()){
            	//if the variable has been set
        		if((int) Math.round(entry.getValue()) == 1.0){
        			//add to set
        			DeltaVarSet.add(entry.getKey());
        		}            	
            }
            //Configuration A variables
            String configForNewZ = "";
            //get the configuration that has been selected
            for(int fSeq = 0; fSeq < sC.chain_seq.size() ; fSeq++){
            	//iterate through the list of double values
            	for(Map.Entry<PpVarA, Double> entry : newConfig_var_a_v_sigma.entrySet()){
            		//if the variable has been set
            		if((int) Math.round(entry.getValue()) == 1.0){
            			//check of the vnfID and Object A match
            			if(entry.getKey().f_seq == fSeq){
            				configForNewZ = configForNewZ + "_" + entry.getKey().node.get_id();
            			}
            		}						            		
            	}
            }
            //Configuration B variables
            Set<PpVarB> BVarSet = new HashSet<PpVarB>();
            //iterate through the B variables
            for(Map.Entry<PpVarB, Double> entry : newConfig_var_b_l_sigma_i_i1.entrySet()){
            	//if the variable has been set
        		if((int) Math.round(entry.getValue()) == 1.0){
        			//add to set
        			BVarSet.add(entry.getKey());
        		} 	
            }
            //check if column is to be added or not
            //boolean notAddCol = false;
            MpVarZ newConfigForChainC = new MpVarZ(pPObject.scIDpP, configForNewZ, DeltaVarSet, BVarSet);
            ppConfig = configForNewZ;
            //check if the configuration has already been generated for this service chain
            for(MpVarZ entryZ : mP.usedVarZ.keySet()){
            	if(entryZ.configRepetition(serviceChainID, DeltaVarSet, configForNewZ, BVarSet)){
            		System.out.println("Service Chain: " + serviceChainID + " ; New Configuration = " + configForNewZ);
                	System.err.println("!!!!!This configuration has already been added!!!!!");
                	System.out.println("!!!!!This configuration has already been added!!!!!");
                	//notAddCol = true;
//                	throw new Exception("!!!!!This configuration has already been added!!!!!");
            	}
            }
            String sdPairsOf = ""; 
	    	ArrayList<Integer> sdPairsInConfig = new ArrayList<Integer>();
	    	for(Pp2VarDelta varD : newConfigForChainC.DeltaVarSet){
	    		sdPairsInConfig.add(pair_list.indexOf(varD.sd));
	    	}
	    	//sort the array list of values
	    	Collections.sort(sdPairsInConfig);
	    	//create the string
	    	for(Integer flow : sdPairsInConfig){
	    		sdPairsOf += flow + ",";
	    	}
	    	String zName = "Z_SC" + pPObject.scIDpP + configForNewZ + "_(" + sdPairsOf + ")";
//            String zName = "Z_No." + MpVarZ.totalNewConfigs + "_SC" + pPObject.scIDpP + "_Config" + configForNewZ;           
            
            
//            if(!notAddCol){
	            ///ADD NEW COLUMN TO THE MASTER PROBLEM///				            
	            //add the coefficients to the objective - (44)
	            //multiply with the number of links included				           
	            double objcoeff = 0.0;              
	            //iterating through the traffic pairs
	            for(TrafficNodes sd : serviceChainTN.get(serviceChainID)){
	            	//get the key for var_d_sd
	            	Pp2VarDelta tempKey = new Pp2VarDelta(sd);
	            	//get the solution for var_d_sd
	            	double valDeltaGamma = newConfig_var_d_sd.get(tempKey);            	
	            	for(Map.Entry<PpVarB, Double> entry : newConfig_var_b_l_sigma_i_i1.entrySet()){
	            		objcoeff += valDeltaGamma*entry.getValue()*sd.flow_traffic;
	            	}
	            }
	            //THE NEW COLUMN
	            //add the coefficient to the Objective - (44) 
	            //only one configuration is added in one iteration
	            IloColumn nwColVarZGamma = mP.master_problem.column(mP.BwUsed, objcoeff);            
	            //add the column to the configuration constraint - (3)
	            nwColVarZGamma = nwColVarZGamma.and(mP.master_problem.column(mP.config_selection_constraint.get(pPObject.scIDpP), 1.0));           
	            //including the columns for function instance count constraint - (4)
	            for(Map.Entry<MpVarX, IloRange> entryCstr : mP.function_instance_count.entrySet()){
	            	cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
	            	BaseVertex nfviNode = entryCstr.getKey().v;
	            	int fID = entryCstr.getKey().f_id;
	            	double fCountInSC = 0.0;
	            	//iterate through VNF sequence of service chains
	            	for(int f_seq_index = 0; f_seq_index < scSize; f_seq_index++){
	            		//check the function ID
	            		if(fID == scVNF.get(f_seq_index).getid()){
	            			//create the PpVarA key
	            			PpVarA tempKey = new PpVarA(nfviNode,f_seq_index);
	            			//get the value for variable
	            			double valAGamma = Math.round(newConfig_var_a_v_sigma.get(tempKey));
	            			//increase the counter if valAGamma has been set
	            			if(valAGamma == 1.0){
	            				fCountInSC++;
	            			}
	            		}
	            	}
	            	//add the column to the constraint
	            	nwColVarZGamma = nwColVarZGamma.and(mP.master_problem.column(entryCstr.getValue(), fCountInSC));
	            	if(fCountInSC != 0.0){
	            		System.out.println("\t\tCoefficient for " + cstrID + " function instance count constraint [ Node:" + nfviNode.get_id() + " ;VNF:" + fID + " ]: " + fCountInSC);
	            	}
	            }
	            //including the columns for function instance count constraint - (4)-(2)
	            for(Map.Entry<MpVarX, IloRange> entryCstr : mP.function_instance_count2.entrySet()){
	            	cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
	            	BaseVertex nfviNode = entryCstr.getKey().v;
	            	int fID = entryCstr.getKey().f_id;
	            	double fCountInSC = 0.0;
	            	//iterate through VNF sequence of service chains
	            	for(int f_seq_index = 0; f_seq_index < scSize; f_seq_index++){
	            		//check the function ID
	            		if(fID == scVNF.get(f_seq_index).getid()){
	            			//create the PpVarA key
	            			PpVarA tempKey = new PpVarA(nfviNode,f_seq_index);
	            			//get the value for variable
	            			double valAGamma = Math.round(newConfig_var_a_v_sigma.get(tempKey));
	            			//increase the counter if valAGamma has been set
	            			if(valAGamma == 1.0){
	            				fCountInSC++;
	            			}
	            		}
	            	}
	            	//add the column to the constraint
	            	nwColVarZGamma = nwColVarZGamma.and(mP.master_problem.column(entryCstr.getValue(), fCountInSC));
	            	if(fCountInSC != 0.0){
	            		System.out.println("\t\tCoefficient for " + cstrID + " function instance count constraint [ Node:" + nfviNode.get_id() + " ;VNF:" + fID + " ]: " + fCountInSC);
	            	}
	            }
	            //including the columns for the core capacity constraints - (6)
	            //iterating over the set of NFV-capable nodes				          
	            for(int vnf_node_index=0; vnf_node_index < nfv_nodes.size(); vnf_node_index++){
	            	cstrName = mP.core_capacity_constraint.get(vnf_node_index).getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
	            	BaseVertex nfv_node = nfv_nodes.get(vnf_node_index);
	            	double coeffCore = 0.0;
	            	//iterate through the traffic pairs
	            	for(TrafficNodes sd : serviceChainTN.get(serviceChainID)){
	            		//get the key for var_d_sd
	                	Pp2VarDelta tempKey = new Pp2VarDelta(sd);
	                	//get the solution for var_d_sd
	                	double valDeltaGamma = Math.round(newConfig_var_d_sd.get(tempKey)); 
	                	//iterate through VNF sequence of service chains
	                	for(int f_seq_index = 0; f_seq_index < scSize; f_seq_index++){
	                			//core count
	                			double fCore = scVNF.get(f_seq_index).getcore();
	                			//create the PpVarA key
	                			PpVarA tempKey2 = new PpVarA(nfv_node,f_seq_index);
	                			//get the value for variable
	                			double valAGamma = Math.round(newConfig_var_a_v_sigma.get(tempKey2));
	                			//increase the counter
	                			coeffCore += sd.flow_traffic*fCore*valAGamma*valDeltaGamma;                		
	                	}
	            	}	            	
	            	if(coeffCore != 0.0){
	            		newConfigForChainC.addCoreCount(nfv_node, coeffCore);
	            		System.out.println("\t\tCoefficient for " + cstrID + " core capacity constraint[ Node " + nfv_node.get_id() + " ]= " + coeffCore);
	            	}
	            	//Core constraint is imposed/ not imposed
	                if(InputConstants.coreCstr){
		            	//modify the column //add the column to the constraint
		            	nwColVarZGamma = nwColVarZGamma.and(mP.master_problem.column(mP.core_capacity_constraint.get(vnf_node_index), coeffCore) );	
	                }
	            }         
	            //including the columns for the flow capacity constraints - (7)				            	
	            //reset the link counter
	            int link_num = 0; 
	            for(BaseVertex s_vert : g._vertex_list){ 
	            	for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){ 
	            		cstrName = mP.flow_capacity_constraint.get(link_num).getName();//constraint Name
	                	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
	            		NodePair link = new NodePair(s_vert,t_vert);
	            		double coeffLink = 0.0;
	            		//iterating through the traffic pairs
	                    for(TrafficNodes sd : serviceChainTN.get(serviceChainID)){
	                    	//get the key for var_d_sd
	                    	Pp2VarDelta tempKey = new Pp2VarDelta(sd);
	                    	//get the solution for var_d_sd
	                    	double valDeltaGamma = Math.round(newConfig_var_d_sd.get(tempKey));            	
	                    	//iterate through VNF sequence of service chains
	                    	for(int f_seq_index = 0; f_seq_index < scSize-1; f_seq_index++){
	                    		//get the key for var_b_l_sigma_i_i1
	                        	PpVarB tempKey2 = new PpVarB(serviceChainID,f_seq_index,f_seq_index+1,s_vert,t_vert);
	                        	//get the solution for var_b_l_sigma_i_i1
	                        	double valBGamma = Math.round(newConfig_var_b_l_sigma_i_i1.get(tempKey2));
	                    		coeffLink += valDeltaGamma*valBGamma*sd.flow_traffic;
	                    	}                    	
	                    }                   
	            		if(coeffLink != 0.0){
	            			newConfigForChainC.addLinkCapacity(link, coeffLink);
	            			System.out.println("\t\tCoefficient for " + cstrID + " link capacity constraint [" + link_num + "]: "  + coeffLink);
	            		}
	            		 //Link capacity constraints
	                    if(InputConstants.capacityCstr){
		            		//modify the column //add the column to the constraint 
		            		nwColVarZGamma = nwColVarZGamma.and(mP.master_problem.column(mP.flow_capacity_constraint.get(link_num), coeffLink ) );
	                    }
	            		//increment the link counter
	            		link_num++;
	            	}
	            }            
	            //latency constraint - (8)
	            for(Map.Entry<MpCstr7and11, IloRange> entryCstr : mP.flow_latency_constraint.entrySet()){
	            	cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
	            	TrafficNodes sd = entryCstr.getKey().tn;
	            	double coeffLatency = 0.0;
	            	for(BaseVertex source : g._vertex_list){
	            		for(BaseVertex sink :  g.get_adjacent_vertices(source)){ 
	            			double lProp = InputConstants.SEC_TO_MICROSEC*g.get_edge_length(source, sink)/InputConstants.SPEED_OF_LIGHT;
	            			//get the key for var_d_sd
	                    	Pp2VarDelta tempKey = new Pp2VarDelta(sd);
	                    	//check if this value exists
	                    	if(newConfig_var_d_sd.get(tempKey) != null){
		                    	//get the solution for var_d_sd
		                    	double valDeltaGamma = Math.round(newConfig_var_d_sd.get(tempKey)); 
		                    	//iterate through VNF sequence of service chains
		                    	for(int f_seq_index = 0; f_seq_index < scSize-1; f_seq_index++){
		                    		//get the key for var_b_l_sigma_i_i1
		                        	PpVarB tempKey2 = new PpVarB(serviceChainID,f_seq_index,f_seq_index+1,source,sink);
		                        	//get the solution for var_b_l_sigma_i_i1
		                        	double valBGamma = Math.round(newConfig_var_b_l_sigma_i_i1.get(tempKey2));
		                        	coeffLatency += valDeltaGamma*valBGamma*lProp;
		                    	}
	                    	}
	            		}            		
	            	}
	            	//latency constraints
	            	if(InputConstants.latencyCstr){
		            	//modify the column //add the column to the constraint 
		        		nwColVarZGamma = nwColVarZGamma.and(mP.master_problem.column(entryCstr.getValue(), coeffLatency ) );
	            	}	            	
	            	if(coeffLatency != 0.0){
	        			System.out.println("\t\tCoefficient for " + cstrID + " latency constraint [ SC: " + serviceChainID + " ; SD:" + entryCstr.getKey().tn.toString() + "]: "  + coeffLatency);
	        		}
	            }
	            //including the columns for the one path per sd and c constraint - (9)
	            //iterate through SD pairs
	            for(Map.Entry<MpCstr7and11, IloRange> entryCstr : mP.path_per_c_sd_contraint.entrySet()){
	            	cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
	            	//check if same service chain
	            	if(entryCstr.getKey().chainIndex == serviceChainID){
	            		//get the key for var_d_sd
	                	Pp2VarDelta tempKey = new Pp2VarDelta(entryCstr.getKey().tn);
	                	//get the solution for var_d_sd
	                	double valDeltaGamma = Math.round(newConfig_var_d_sd.get(tempKey)); 
	                	//modify the column //add the column to the constraint 
	            		nwColVarZGamma = nwColVarZGamma.and( mP.master_problem.column( entryCstr.getValue(), valDeltaGamma) );
	            		if(valDeltaGamma != 0.0){
	            			System.out.println("\t\tCoefficient for " + cstrID + " one path per sd and c constraint [ SD:" + entryCstr.getKey().tn.toString() + "]: " + valDeltaGamma);
	            		}
	            		/*if(DeltaVarSet.contains(entryCstr.getKey().tn)){
	            			//modify the column //add the column to the constraint 
		            		nwColVarZGamma = nwColVarZGamma.and( mP.master_problem.column( entryCstr.getValue(), 1.0) );
		            		System.out.println("\t\tCoefficient for " + cstrID + " one path per sd and c constraint [ SD:" + entryCstr.getKey().tn.toString() + "]: " + 1.0);
	            		}*/
	            	}
	            }
	            //src_outgoing_constraint - (10)
	            //iterate through SD pairs
	            for(Map.Entry<MpCstr7and11, IloRange> entryCstr : mP.src_outgoing_constraint.entrySet()){
	            	cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
	            	//check service chain ID
	            	if(entryCstr.getKey().chainIndex == serviceChainID){
	            		TrafficNodes sd = entryCstr.getKey().tn;
	            		//get the key for var_d_sd
	                	Pp2VarDelta tempKey = new Pp2VarDelta(sd);
	                	//get the solution for var_d_sd
	                	double valDeltaGamma = Math.round(newConfig_var_d_sd.get(tempKey)); 
	                	//check if srcVrt is a NFVI node
	                	BaseVertex srcVrt = sd.v1;
	                	if(nodesNFVI.contains(srcVrt)){
	                		//create the PpVarA key
	            			PpVarA tempKey2 = new PpVarA(srcVrt,0);
	            			//get the value for variable
	            			double valAGamma = Math.round(newConfig_var_a_v_sigma.get(tempKey2));
	            			//modify the column //add the column to the constraint 
	                		nwColVarZGamma = nwColVarZGamma.and( mP.master_problem.column( entryCstr.getValue(), valDeltaGamma*valAGamma) );
	                		if(valDeltaGamma*valAGamma != 0.0){
	                			System.out.println("\t\tCoefficient for " + cstrID + " src_outgoing_constraint [ SD:" + entryCstr.getKey().tn.toString() + "]: " + valDeltaGamma*valAGamma);
	                		}
	                	}	            	
	            	}
	            }
	            //src_incoming_constraint (11)
	            for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : mP.src_incoming_constraint.entrySet()){
	            	cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
	            	//check service chain ID
	            	if(entryCstr.getKey().chainIndex == serviceChainID){  
	            		//SD Pair
	            		TrafficNodes sd = entryCstr.getKey().tn;
	            		//NFVI node
	                	BaseVertex nfviNode = entryCstr.getKey().nfvi_node; 
	            		//get the key for var_d_sd
	                	Pp2VarDelta tempKey = new Pp2VarDelta(sd);
	                	//get the solution for var_d_sd
	                	double valDeltaGamma = Math.round(newConfig_var_d_sd.get(tempKey));               	                   
	            		//create the PpVarA key
	        			PpVarA tempKey2 = new PpVarA(nfviNode,0);
	        			//get the value for variable
	        			double valAGamma = Math.round(newConfig_var_a_v_sigma.get(tempKey2));
	        			//modify the column //add the column to the constraint 
	            		nwColVarZGamma = nwColVarZGamma.and( mP.master_problem.column( entryCstr.getValue(), valDeltaGamma*valAGamma) ); 
	            		if(valDeltaGamma*valAGamma != 0.0){
	            			System.out.println("\t\tCoefficient for " + cstrID + " src_incoming_constraint [ SC: " + serviceChainID + " ; Node: " + nfviNode.get_id() +  " ; SD:" + entryCstr.getKey().tn.toString() + "]: " + valDeltaGamma*valAGamma);
	            		}
	            	}
	            }
	            //flow conservation constraint - placement - ingress node (12)
	            for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : mP.flow_place_ing_constraint.entrySet()){
	            	cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
	            	//check service chain ID
	            	if(entryCstr.getKey().chainIndex == serviceChainID){
		            	//dual value of constraint
	            		TrafficNodes sd = entryCstr.getKey().tn;
	            		//NFVI node
	                	BaseVertex nfviNode = entryCstr.getKey().nfvi_node; 
	            		//get the key for var_d_sd
	                	Pp2VarDelta tempKey = new Pp2VarDelta(sd);
	                	//get the solution for var_d_sd
	                	double valDeltaGamma = Math.round(newConfig_var_d_sd.get(tempKey));               	                   
	            		//create the PpVarA key
	        			PpVarA tempKey2 = new PpVarA(nfviNode,0);
	        			//get the value for variable
	        			double valAGamma = Math.round(newConfig_var_a_v_sigma.get(tempKey2));
	        			//modify the column //add the column to the constraint 
	            		nwColVarZGamma = nwColVarZGamma.and( mP.master_problem.column( entryCstr.getValue(), valDeltaGamma*valAGamma) ); 
	            		if(valDeltaGamma*valAGamma != 0.0){
	            			System.out.println("\t\tCoefficient for " + cstrID + " flow conservation constraint - placement - ingress node [ SC: " + serviceChainID + " ; Node: " + nfviNode.get_id() +  " ; SD:" + entryCstr.getKey().tn.toString() + "]: " + valDeltaGamma*valAGamma);
	            		}
	            	}
	            }
	            //dest_incoming_constraint - (14)
	            //iterate through SD pairs
	            for(Map.Entry<MpCstr7and11, IloRange> entryCstr : mP.dest_incoming_constraint.entrySet()){
	            	cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
	            	//check service chain ID
	            	if(entryCstr.getKey().chainIndex == serviceChainID){	            	
						//details required
						TrafficNodes sd = entryCstr.getKey().tn;
					    BaseVertex dstVrt = sd.v2;
					    //get the key for var_d_sd
	                	Pp2VarDelta tempKey = new Pp2VarDelta(sd);
	                	//get the solution for var_d_sd
	                	double valDeltaGamma = Math.round(newConfig_var_d_sd.get(tempKey)); 
						//source Node is a NFVI node
						if(nodesNFVI.contains(dstVrt)){
			            	//make the X variable key
							PpVarA tempKey2 = new PpVarA(dstVrt,scSize-1);
			    			//get the actual variable X
							double valAGamma = Math.round(newConfig_var_a_v_sigma.get(tempKey2));
							//modify the column //add the column to the constraint 
		            		nwColVarZGamma = nwColVarZGamma.and( mP.master_problem.column( entryCstr.getValue(), valDeltaGamma*valAGamma) ); 
		            		if(valDeltaGamma*valAGamma != 0.0){
		            			System.out.println("\t\tCoefficient for " + cstrID + " dest_incoming_constraint [ SD:" + entryCstr.getKey().tn.toString() + "]: " + valDeltaGamma*valAGamma);
		            		}
						}	    		
	            	}
	            }
	            //dest_outgoing_constraint (15)
	            for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : mP.dest_outgoing_constraint.entrySet()){
	            	cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
	            	//check service chain ID
	            	if(entryCstr.getKey().chainIndex == serviceChainID){
	            		//details required
						TrafficNodes sd = entryCstr.getKey().tn;
					    BaseVertex nfviNode = entryCstr.getKey().nfvi_node;
					    //get the key for var_d_sd
	                	Pp2VarDelta tempKey = new Pp2VarDelta(sd);
	                	//get the solution for var_d_sd
	                	double valDeltaGamma = Math.round(newConfig_var_d_sd.get(tempKey)); 				
		            	//make the X variable key
						PpVarA tempKey2 = new PpVarA(nfviNode,scSize-1);
		    			//get the actual variable X
						double valAGamma = Math.round(newConfig_var_a_v_sigma.get(tempKey2));
						//modify the column //add the column to the constraint 
	            		nwColVarZGamma = nwColVarZGamma.and( mP.master_problem.column( entryCstr.getValue(), valDeltaGamma*valAGamma) ); 	
	            		if(valDeltaGamma*valAGamma != 0.0){
	            			System.out.println("\t\tCoefficient for " + cstrID + " dest_outgoing_constraint [ SC: " + serviceChainID + " ; Node: " + nfviNode.get_id() +  " ; SD:" + entryCstr.getKey().tn.toString() + "]: " + valDeltaGamma*valAGamma);	
	            		}
	            	}
	            }
	            //flow conservation constraint - placement - egress node (16)
	            for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : mP.flow_place_egr_constraint.entrySet()){
	            	cstrName = entryCstr.getValue().getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
	            	//check service chain ID
	            	if(entryCstr.getKey().chainIndex == serviceChainID){
	            		//details required
						TrafficNodes sd = entryCstr.getKey().tn;
					    BaseVertex nfviNode = entryCstr.getKey().nfvi_node;
					    //get the key for var_d_sd
	                	Pp2VarDelta tempKey = new Pp2VarDelta(sd);
	                	//get the solution for var_d_sd
	                	double valDeltaGamma = Math.round(newConfig_var_d_sd.get(tempKey)); 				
		            	//make the X variable key
						PpVarA tempKey2 = new PpVarA(nfviNode,scSize-1);
		    			//get the actual variable X
						double valAGamma = Math.round(newConfig_var_a_v_sigma.get(tempKey2));
						//modify the column //add the column to the constraint 
	            		nwColVarZGamma = nwColVarZGamma.and( mP.master_problem.column( entryCstr.getValue(), valDeltaGamma*valAGamma) ); 	
	            		if(valDeltaGamma*valAGamma != 0.0){
	            			System.out.println("\t\tCoefficient for " + cstrID + " flow conservation constraint - placement - egress node [ SC: " + serviceChainID + " ; Node: " + nfviNode.get_id() +  " ; SD:" + entryCstr.getKey().tn.toString() + "]: " + valDeltaGamma*valAGamma);
	            		}
	            	}
	            }	           
	            //add the variable to the set of variables
	            mP.usedVarZ.put(newConfigForChainC, mP.master_problem.numVar(nwColVarZGamma, 0.0, Double.MAX_VALUE, zName) );
//            }
            //check the reduced cost                 
           /* double calculatedRC = CheckFunctions.checkReducedCost2(-1.0*dualVal45, g, pair_list, func_list_sc, nfv_nodes, newConfig_var_d_sd, newConfig_var_p_sd_vi, newConfig_var_q_sd_li,
            		newConfig_var_a_v_sigma, dvFuncInstCountConst, dvCoreCapacityConst, dvFlowCapacityConst, dvLatencyConst, dvOnePathPerSdConst, dvSrcOutgoigConst, dvSrcIncomingConst, dvFlowForFirst2, 
            		dvDstIncomingConst, dvDstOutgoingConst, dvFlowForLast2, pPObject.pricing_problem);*/
            //reduced cost from PP objective
            System.out.println("\t\t#### Retrieved Reduced Cost from PP : " + ppReducedCost + " ####");
            //print out the times
           /* System.out.println("RMP Solve Time for " + i + "th iteration: " + mPexecTime);
            System.out.println("PP Solve Time for " + i + "th iteration: " + pPexecTime);
            //concatenate the strings
            solveTime = solveTime + "RMP Solve Time for " + i + "th iteration: " + mPexecTime + "\n";
            solveTime = solveTime + "PP Solve Time for " + i + "th iteration: " + pPexecTime + "\n";*/
            //check  calculated and retrieved reduced costs
            /*if((int) Math.round(calculatedRC) != (int) Math.round(ppReducedCost)){
            	throw new Exception("!!!!!PP Objective and Calculated Reduced Cost DO NOT MATCH!!!!!");
            }*/
            //check if the CG is non-terminating
            //check if reduced cost remains
           /* if(i>1 && (ppReducedCost == prevPpRC) && prevConfig.equals(ppConfig)){
            	System.out.println("Current Reduced Cost = " + ppReducedCost);
            	System.out.println("Previous Reduced Cost = " + prevPpRC);
            	rcRepeat = true;
            	System.err.println("!!!!!Reduced Cost does not change after ADDING COLUMN!!!!!");
            	System.out.println("!!!!!Reduced Cost does not change after ADDING COLUMN!!!!!");
            	//throw new Exception("!!!!!Reduced Cost does not change after ADDING COLUMN and Repeating Column!!!!!");            	
            }        
            //assign the current RC to previous RC
            prevPpRC = ppReducedCost;
            prevConfig = ppConfig;*/
            //set the number of simplex iterations to zero
            /*if( i == InputConstants.simeplexInterationLimit )            
            	mP.master_problem.setParam(IloCplex.LongParam.ItLim, 0);*/
            
			
		}		
		
		//return the execution times
		return solveTime;
	}

}
