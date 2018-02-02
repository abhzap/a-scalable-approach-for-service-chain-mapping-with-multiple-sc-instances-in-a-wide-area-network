package colGen.model.ver2;

import ilog.concert.IloColumn;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.result.ReportResults2;
import colGen.model.ver1.MpCstr7and11;
import colGen.model.ver1.MpCstr81012and14;
import colGen.model.ver1.MpVarX;
import colGen.model.ver1.MpVarZ;
import colGen.model.ver1.NewIngEgConfigs;
import colGen.model.ver1.PpVarA;
import colGen.model.ver1.PpVarB;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class PricingCheck {
	
	public static boolean runIterations(int ppNum, Map<Integer, ArrayList<TrafficNodes>> serviceChainTN, PricingProblem2 pPObject, MasterProblem2 mP,  
			Map<TrafficNodes, ArrayList<NewIngEgConfigs>> OriginalConfigs, List<TrafficNodes> pair_list, Map<Integer, ServiceChain> ChainSet, 
			List<FuncPt> vnf_list,List<FuncPt> func_list, ArrayList<BaseVertex> nodesNFVI, ArrayList<BaseVertex> nfv_nodes, Graph g) throws Exception{
		
		//check if the reduced cost repeats in this configuration
		boolean rcRepeat = false;
		
		//service chain being used
		int serviceChainID = pPObject.scIDpP;		
		//set of SD pairs using a service chain 'c'
		ArrayList<TrafficNodes> sdPairs = serviceChainTN.get(serviceChainID);
		
		//create the list of functions for this service chain
    	ServiceChain sC = ChainSet.get(pPObject.scIDpP);
    	int scSize = sC.chain_size;
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
		
		//corresponding to the number of variables in the sub-problem			    
	 	/// COLUMN-GENERATION PROCEDURE ///
	 	//loop until conditions are satisfied
		//for(int i=1;i<InputConstants.simeplexInterationLimit + 2;i++){
		for(int i=1;;i++){
			/// OPTIMIZE OVER CURRENT CONFIGURATIONS ///        	
			//name of CG file to be exported
			String cgFileNameLP = "master_problem_cg_enhanced_" + i + "_PP" + ppNum + ".lp";
			String cgFileNameSAV = "master_problem_cg_enhanced_" + i + "_PP" + ppNum + ".sav";    
			String cgFileNameMPS = "master_problem_cg_enhanced_" + i + "_PP" + ppNum + ".mps";    
		    //export the master problem
			mP.master_problem.exportModel(cgFileNameLP);	
			mP.master_problem.exportModel(cgFileNameSAV);
			mP.master_problem.exportModel(cgFileNameMPS);
			
						
			//SOLVE THE MASTER PROBLEM	
			//set the master problem algorithm parameter
			//mP.master_problem.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Primal);
			//tune parameters to improve performance
			//mP.master_problem.tuneParam();
            mP.master_problem.solve();            
            double currObjVal = mP.master_problem.getObjValue();
            if( i>1 && (currObjVal - prevMpObj > 0.1) ){
            	System.out.println("Previous RMP Objective Value = " + prevMpObj);
            	System.out.println("Current RMP Objective Value = " + currObjVal);
            	System.err.println("!!!!!Master Problem Objective is larger after ADDING COLUMN!!!!!");
            	//throw new Exception("!!!!!Master Problem Objective is larger after ADDING COLUMN!!!!!");
            }
            prevMpObj = currObjVal;
            //total number of columns
            System.out.println("######## Total number of columns : " + mP.master_problem.getNcols() + " #########");
            //total number of rows
            System.out.println("######## Total number of rows : " + mP.master_problem.getNrows() + " ##########");           
            //get the solution for the variables
//            ReportResults2.report1(mP.master_problem, mP.slackVariables, mP.usedVarZ, mP.usedVarX, mP.usedVarY);	
            //if( i == InputConstants.simeplexInterationLimit + 1 ){
	            //get the reduced cost for all the variables
	            ReportResults2.reducedCostperVariable(i, ppReducedCost, mP.master_problem, mP.usedVarZ, mP.usedVarX, mP.usedVarY);	
            //}
            //get the basis status for the variables
			ReportResults2.basisCheckForVariables(mP.master_problem, mP.usedVarZ, mP.usedVarX, mP.usedVarY);
		
			
			
		    
			//QUERY DUAL VALUES
			Map<Pp2VarQ,Double> coeffVarQ = new HashMap<Pp2VarQ,Double>();
			Map<Pp2VarP,Double> coeffVarP = new HashMap<Pp2VarP,Double>();
			//configuration selection constraint (3)
			//get dual value
	    	double dualVal3 =  mP.master_problem.getDual(mP.config_selection_constraint.get(serviceChainID));
	    	String cstrName = mP.config_selection_constraint.get(pPObject.scIDpP).getName();//constraint Name
	        String cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID          
	    	System.out.println("\t\tDual value for " + cstrID + " SC:" + serviceChainID + " configuration selection constraint = " + dualVal3 );
		    	//calculate coefficient //add to linear expression	    	
				IloLinearNumExpr exprForReducedCost = pPObject.pricing_problem.linearNumExpr(Math.abs(dualVal3));
			//add the COST_GAMMA factor into the reduced cost expression
			//iterate through sd pairs
			for(TrafficNodes sd : sdPairs){				
				//iterate through the links
		    	int link_num = 0;
			    for(BaseVertex source : g._vertex_list){ 
			    	for(BaseVertex sink :  g.get_adjacent_vertices(source)){
			    		//iterate through function sequence in service chain
			    		for(int f_seq_index = 0; f_seq_index < scSize-1; f_seq_index++){
			    			double coeff = sd.flow_traffic;
			    			//get Q variable
		    				Pp2VarQ keyQ = new Pp2VarQ(sd,f_seq_index,source,sink,serviceChainID);
		    				IloIntVar varQ = pPObject.Used_var_q_sd_il.get(keyQ);
		    				//add to linear expression
//		    				exprForReducedCost.addTerm(coeff, varQ);
		    				//add the coefficient for variable P
	                		if( coeffVarQ.get(keyQ) != null ){
	                			coeff = coeff + coeffVarQ.get(keyQ);		                			
	                		}
	                		coeffVarQ.put(keyQ, coeff);
			    		}
			    	}
			    	//increment the link counter
			    	link_num++;
			    }				
			}
			//print out the Q values
			/*System.out.println("\t\t\t Q variables after COST GAMMA");
			for(Map.Entry<Pp2VarQ, Double> entryQ : coeffVarQ.entrySet()){
				String strQ = "Q_("+entryQ.getKey().sd.v1.get_id()+","+entryQ.getKey().sd.v2.get_id()+
						")_Seq"+entryQ.getKey().f_seq + "_Link(" + entryQ.getKey().srcL.get_id() + "," + entryQ.getKey().tarL.get_id() + ")";
				System.out.println("\t\t\t\tCoefficient of " + strQ + " = " + entryQ.getValue());
			}*/
	    	//function instance count (4)
	    	//iterate through the constraints
	    	for(Map.Entry<MpVarX,IloRange> entryCstr : mP.function_instance_count.entrySet()){
	    		//get dual value
	    		double dualVal = mP.master_problem.getDual(entryCstr.getValue());
	    		cstrName = entryCstr.getValue().getName();//constraint Name
            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
            	System.out.println("\t\tDual value for " + cstrID + 
            			" function_instance_count[ Node: " + entryCstr.getKey().v.get_id() + 
            			"; VNF: " + entryCstr.getKey().f_id + "] = " + dualVal );
	    			//calculate coefficient    			
					BaseVertex nfvNode = entryCstr.getKey().v;
					int vnfID = entryCstr.getKey().f_id;				
					//iterate through the chain sequence
	            	for(int f_seq_index = 0; f_seq_index < scSize; f_seq_index++){
	            		int recvFiD = scVNF.get(f_seq_index).getid();
	            		if(vnfID == recvFiD){
	            			//get A variable
	            			PpVarA keyA = new PpVarA(nfvNode, f_seq_index);
	            			IloIntVar varA = pPObject.Used_var_a_vi.get(keyA);
	            			//add to linear expression
	            			exprForReducedCost.addTerm(Math.abs(dualVal), varA);
	            		}
	            	}  		
	    	}    
	    	//Core constraint needs to be imposed
            if(InputConstants.coreCstr){
		    	//core capacity constraint	(6)	
		    	for(int vnf_node=0; vnf_node < nfv_nodes.size(); vnf_node++){
		    		//get dual value
		    		double dualVal = mP.master_problem.getDual(mP.core_capacity_constraint.get(vnf_node));
		    		cstrName = mP.core_capacity_constraint.get(vnf_node).getName();//constraint Name
	            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID	            	
		    		//get NFV Node
		    		BaseVertex nfvNode = nfv_nodes.get(vnf_node);
		    		System.out.println("\t\tDual value " + cstrID + " core_capacity_constraint[" + vnf_node + "] (Node" + nfvNode.get_id() + " = " + dualVal );
		    			//iterate over sd pairs
		    			for(TrafficNodes sd : sdPairs){
		    				double trafSD = sd.flow_traffic;
		    				//iterate through the chain sequence
		                	for(int f_seq_index = 0; f_seq_index < scSize; f_seq_index++){
		                		//calculate coefficient
		                		double coreCount = scVNF.get(f_seq_index).getcore();
		                		double coeff = trafSD*coreCount*Math.abs(dualVal);
		                		//get P variable
		                		Pp2VarP keyP = new Pp2VarP(sd,f_seq_index,nfvNode);
		                		IloIntVar varP = pPObject.Used_var_p_sd_vi.get(keyP);
		                		//add to linear expression
//		                		exprForReducedCost.addTerm(coeff, varP);
		                		//add the coefficient for variable P
		                		if( coeffVarP.get(keyP) != null ){
		                			coeff = coeff + coeffVarP.get(keyP);		                			
		                		}
		                		coeffVarP.put(keyP, coeff);		                		
		                	}
		    			}   		
		    	}  
            }
            int link_num = 0;
            //Link capacity constraints
            if(InputConstants.capacityCstr){
		    	//flow capacity constraint (7)		    	
			    for(BaseVertex s_vert : g._vertex_list){ 
			    	for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){
			    		//get dual value
			    		double dualVal = mP.master_problem.getDual(mP.flow_capacity_constraint.get(link_num));
			    		cstrName = mP.flow_capacity_constraint.get(link_num).getName();//constraint Name
		            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
		            	System.out.println("\t\tDual value for " + cstrID + " flow_capacity_constraint[" + link_num + "] (Link_Src" + s_vert.get_id() + "_Dst" + t_vert.get_id() + ") = " + dualVal );
				    		//iterate over sd pairs
			    			for(TrafficNodes sd : sdPairs){			    				
			    				double trafSD = sd.flow_traffic;			    				
			    				//iterate through the chain sequence
			                	for(int f_seq_index = 0; f_seq_index < scSize-1; f_seq_index++){
			                		//calculate coefficient
			                		double coeff = trafSD*Math.abs(dualVal);
				    				//get Q variable
				    				Pp2VarQ keyQ = new Pp2VarQ(sd,f_seq_index,s_vert,t_vert,serviceChainID);
				    				IloIntVar varQ = pPObject.Used_var_q_sd_il.get(keyQ);
				    				//add to linear expression
//				    				exprForReducedCost.addTerm(coeff, varQ);
				    				//add the coefficient for variable Q
				    				if( coeffVarQ.get(keyQ) != null ){
			                			coeff = coeff + coeffVarQ.get(keyQ);		                			
			                		}
			                		coeffVarQ.put(keyQ, coeff);
			                	}
			    			}	    		
				    		//increment the link number
				    		link_num++;
			    	}
			    } 
            }
            //print out the Q values
			/*System.out.println("\t\t\t Q variables after (7)");
			for(Map.Entry<Pp2VarQ, Double> entryQ : coeffVarQ.entrySet()){
				String strQ = "Q_("+entryQ.getKey().sd.v1.get_id()+","+entryQ.getKey().sd.v2.get_id()+
						")_Seq"+entryQ.getKey().f_seq + "_Link(" + entryQ.getKey().srcL.get_id() + "," + entryQ.getKey().tarL.get_id() + ")";
				System.out.println("\t\t\t\tCoefficient of " + strQ + " = " + entryQ.getValue());
			}*/
	    	//latency constraint (8)
		    for(Map.Entry<MpCstr7and11,IloRange> entryCstr : mP.flow_latency_constraint.entrySet()){
		    	//get dual value
		    	double dualVal = mP.master_problem.getDual(entryCstr.getValue());
		    	cstrName = entryCstr.getValue().getName();//constraint Name
            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
		    	System.out.println("\t\tDual value for " + cstrID + " latency_constraint[" + entryCstr.getKey().tn.toString() + "] = " + dualVal );
		    	TrafficNodes sd = entryCstr.getKey().tn;
		    	    //iterate through the links
			    	link_num = 0;
				    for(BaseVertex s_vert : g._vertex_list){ 
				    	for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){
				    		double lProp = InputConstants.SEC_TO_MICROSEC*g.get_edge_length(s_vert, t_vert)/InputConstants.SPEED_OF_LIGHT;
				    		//iterate through the chain sequence
		                	for(int f_seq_index = 0; f_seq_index < scSize-1; f_seq_index++){
		                		//calculate coefficient
		                		double coeff = lProp*Math.abs(dualVal);
		                		//get Q variable
			    				Pp2VarQ keyQ = new Pp2VarQ(sd,f_seq_index,s_vert,t_vert,serviceChainID);
			    				IloIntVar varQ = pPObject.Used_var_q_sd_il.get(keyQ);
			    				//add to linear expression
//			    				exprForReducedCost.addTerm(coeff, varQ);
			    				//add the coefficient for variable Q
			    				if( coeffVarQ.get(keyQ) != null ){
			    					//System.out.println("\t\t\t\t" + coeff);
		                			coeff = coeff + coeffVarQ.get(keyQ);		                			
		                		}
		                		coeffVarQ.put(keyQ, coeff);		                	
		                	}
				    	}
				    	//increment the link number
			    		link_num++;
				    }			    
		    }
		    //print out the Q values
			/*System.out.println("\t\t\t Q variables after (8)");
			for(Map.Entry<Pp2VarQ, Double> entryQ : coeffVarQ.entrySet()){
				String strQ = "Q_("+entryQ.getKey().sd.v1.get_id()+","+entryQ.getKey().sd.v2.get_id()+
						")_Seq"+entryQ.getKey().f_seq + "_Link(" + entryQ.getKey().srcL.get_id() + "," + entryQ.getKey().tarL.get_id() + ")";
				System.out.println("\t\t\t\tCoefficient of " + strQ + " = " + entryQ.getValue());
			}*/
		    //one path per c & sd constraint (9)
		    for(Map.Entry<MpCstr7and11,IloRange> entryCstr : mP.path_per_c_sd_contraint.entrySet()){
		    	//get dual value
		    	double dualVal = mP.master_problem.getDual(entryCstr.getValue());
		    	cstrName = entryCstr.getValue().getName();//constraint Name
            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
		    	System.out.println("\t\tDual value for " + cstrID + " path_per_c_sd_contraint[" + entryCstr.getKey().tn.toString() + "] = " + dualVal );
			    	//get the variable D
			    	Pp2VarDelta keyD = new Pp2VarDelta(entryCstr.getKey().tn);
			    	IloIntVar varD = pPObject.Used_var_d_sd.get(keyD);
			    	//calculate coefficient //add to linear expression
			    	exprForReducedCost.addTerm(-1.0*dualVal, varD);
		    }	    
		    //source outgoing link constraint (10)
		    for(Map.Entry<MpCstr7and11,IloRange> entryCstr : mP.src_outgoing_constraint.entrySet()){
		    	//get dual value
		    	double dualVal = mP.master_problem.getDual(entryCstr.getValue());
		    	cstrName = entryCstr.getValue().getName();//constraint Name
            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
		    	System.out.println("\t\tDual value for " + cstrID + " src_outgoing_constraint[" + entryCstr.getKey().tn.toString() + "] = " + dualVal );
		    	//calculate coefficient
		    	double coeff = -1.0*dualVal;
		    	TrafficNodes sd = entryCstr.getKey().tn;
		    	BaseVertex srcVrt = sd.v1;		    		
			    	if(nodesNFVI.contains(srcVrt)){				    	
			    		//get P variable
		        		Pp2VarP keyP = new Pp2VarP(sd,0,srcVrt);
		        		IloIntVar varP = pPObject.Used_var_p_sd_vi.get(keyP);
		        		//add to linear expression
//		        		exprForReducedCost.addTerm(coeff, varP);
		        		//add the coefficient for variable P
		        		if( coeffVarP.get(keyP) != null ){
                			coeff = coeff + coeffVarP.get(keyP);		                			
                		}
                		coeffVarP.put(keyP, coeff);
			    	}	    	
		    }	    
		    //source incoming link constraint (11)
		    for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : mP.src_incoming_constraint.entrySet()){
		    	//get dual value
		    	double dualVal = mP.master_problem.getDual(entryCstr.getValue());
		    	cstrName = entryCstr.getValue().getName();//constraint Name
            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
		    	System.out.println("\t\tDual value for " + cstrID + " src_incoming_constraint[" + entryCstr.getKey().tn.toString() 
		    			+ " ; Node:" + entryCstr.getKey().nfvi_node.get_id() + "] = " + dualVal );
		    	//calculate coefficient 
		    	double coeff = Math.abs(dualVal);
		    	TrafficNodes sd = entryCstr.getKey().tn;
		    	BaseVertex nfviNode = entryCstr.getKey().nfvi_node;		    
			    	//get P variable
		    		Pp2VarP keyP = new Pp2VarP(sd,0,nfviNode);
		    		IloIntVar varP = pPObject.Used_var_p_sd_vi.get(keyP);
			    	//add to linear expression
//		    		exprForReducedCost.addTerm(coeff,varP);
		    		//add the coefficient for variable P
		    		if( coeffVarP.get(keyP) != null ){
            			coeff = coeff + coeffVarP.get(keyP);		                			
            		}
            		coeffVarP.put(keyP, coeff);
		    }	   
		    //flow conservation constraint - placement - ingress node (12)
		    for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : mP.flow_place_ing_constraint.entrySet()){
		    	//get dual value
		    	double dualVal = mP.master_problem.getDual(entryCstr.getValue());
		    	cstrName = entryCstr.getValue().getName();//constraint Name
            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
		    	System.out.println("\t\tDual value for " + cstrID + " flow_place_ing_constraint[" + entryCstr.getKey().tn.toString() 
		    			+ " ; Node:" + entryCstr.getKey().nfvi_node.get_id() + "] = " + dualVal );
		    	//calculate coefficient 
		    	double coeff = -1.0*dualVal;
		    	TrafficNodes sd = entryCstr.getKey().tn;
		    	BaseVertex nfviNode = entryCstr.getKey().nfvi_node;
			    	///get P variable
		    		Pp2VarP keyP = new Pp2VarP(sd,0,nfviNode);
		    		IloIntVar varP = pPObject.Used_var_p_sd_vi.get(keyP);
			    	//add to linear expression
//		    		exprForReducedCost.addTerm(coeff,varP);
		    		//add the coefficient for variable P
		    		if( coeffVarP.get(keyP) != null ){
            			coeff = coeff + coeffVarP.get(keyP);		                			
            		}
            		coeffVarP.put(keyP, coeff);
		    }	       
		    //destination incoming link constraint (14)
		    for(Map.Entry<MpCstr7and11,IloRange> entryCstr : mP.dest_incoming_constraint.entrySet()){
		    	//get dual value
		    	double dualVal = mP.master_problem.getDual(entryCstr.getValue());
		    	cstrName = entryCstr.getValue().getName();//constraint Name
            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
		    	System.out.println("\t\tDual value for " + cstrID + " dest_incoming_constraint[" + entryCstr.getKey().tn.toString() + "] = " + dualVal );
		    	//calculate coefficient 
		    	double coeff = -1.0*dualVal;
		    	TrafficNodes sd = entryCstr.getKey().tn;
		    	BaseVertex dstVrt = sd.v2;
			    	if(nodesNFVI.contains(dstVrt)){
			    		//get P variable
		        		Pp2VarP keyP = new Pp2VarP(sd,scSize-1,dstVrt);
		        		IloIntVar varP = pPObject.Used_var_p_sd_vi.get(keyP);
		        		//add to linear expression
//		        		exprForReducedCost.addTerm(coeff,varP);
		        		//add the coefficient for variable P
		        		if( coeffVarP.get(keyP) != null ){
                			coeff = coeff + coeffVarP.get(keyP);		                			
                		}
                		coeffVarP.put(keyP, coeff);
			    	}    	
		    }	  
		    //destination outgoing link constraint (15)
		    for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : mP.dest_outgoing_constraint.entrySet()){
		    	//get dual value
		    	double dualVal = mP.master_problem.getDual(entryCstr.getValue());
		    	cstrName = entryCstr.getValue().getName();//constraint Name
            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
		    	System.out.println("\t\tDual value for " + cstrID + " dest_outgoing_constraint[" + entryCstr.getKey().tn.toString() 
		    			+ " ; Node:" + entryCstr.getKey().nfvi_node.get_id() + "] = " + dualVal );
		    	//calculate coefficient 
		    	double coeff = Math.abs(dualVal);
		    	TrafficNodes sd = entryCstr.getKey().tn;
		    	BaseVertex nfviNode = entryCstr.getKey().nfvi_node;
			    	//get P variable
		    		Pp2VarP keyP = new Pp2VarP(sd,scSize-1,nfviNode);
		    		IloIntVar varP = pPObject.Used_var_p_sd_vi.get(keyP);
			    	//calculate coefficient //add to linear expression
//		    		exprForReducedCost.addTerm(coeff,varP);
		    		//add the coefficient for variable P
		    		if( coeffVarP.get(keyP) != null ){
            			coeff = coeff + coeffVarP.get(keyP);		                			
            		}
            		coeffVarP.put(keyP, coeff);
		    }	   
		    //flow conservation constraint - placement - egress node (16)
		    for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : mP.flow_place_egr_constraint.entrySet()){
		    	//get dual value
		    	double dualVal = mP.master_problem.getDual(entryCstr.getValue());
		    	cstrName = entryCstr.getValue().getName();//constraint Name
            	cstrID = "(" + cstrName.substring(4, cstrName.length()) + ")";//constraint ID
		    	System.out.println("\t\tDual value for " + cstrID  + " flow_place_egr_constraint[" + entryCstr.getKey().tn.toString()
		    			+ " ; Node:" + entryCstr.getKey().nfvi_node.get_id() + "] = " + dualVal );
		    	//calculate coefficient 
		    	double coeff = -1.0*dualVal;
		    	TrafficNodes sd = entryCstr.getKey().tn;
		    	BaseVertex nfviNode = entryCstr.getKey().nfvi_node;
			    	///get P variable
		    		Pp2VarP keyP = new Pp2VarP(sd,scSize-1,nfviNode);
		    		IloIntVar varP = pPObject.Used_var_p_sd_vi.get(keyP);
			    	//calculate coefficient //add to linear expression
//		    		exprForReducedCost.addTerm(coeff,varP);
		    		//add the coefficient for variable P
		    		if( coeffVarP.get(keyP) != null ){
            			coeff = coeff + coeffVarP.get(keyP);		                			
            		}
            		coeffVarP.put(keyP, coeff);
		    }
		    //P variables to the reduced cost expression
		    for(Map.Entry<Pp2VarP, Double> entryP : coeffVarP.entrySet()){
		    	///get P variable
		    	IloIntVar varP = pPObject.Used_var_p_sd_vi.get(entryP.getKey());
		    	//add to linear expression
		    	exprForReducedCost.addTerm(entryP.getValue(),varP);
		    }	
		    //Q variables to the reduced cost expression
		    for(Map.Entry<Pp2VarQ, Double> entryQ : coeffVarQ.entrySet()){
		    	//get Q variable			
				IloIntVar varQ = pPObject.Used_var_q_sd_il.get(entryQ.getKey());				
				//Q string
				//String strQ = "Q_("+entryQ.getKey().sd.v1.get_id()+","+entryQ.getKey().sd.v2.get_id()+")_Seq"+entryQ.getKey().f_seq + "_Link(" + entryQ.getKey().srcL.get_id() + "," + entryQ.getKey().tarL.get_id() + ")";
				//print out the Keys & Values
				//System.out.println(strQ + " = " + entryQ.getValue());
				//add to linear expression
		    	exprForReducedCost.addTerm(entryQ.getValue(),varQ);
		    }
		    
		    
		    ///SET THE EXPPRESSION FOR THE REDUCED COST///
	    	pPObject.reducedCost.setExpr(exprForReducedCost);        
	        //name of CG file to be exported
	        String ppFileNameLP = "pp_" + ppNum + "_iteration_" + i + ".lp" ;
	        String ppFileNameSAV = "pp_" + ppNum + "_iteration_" + i + ".sav" ;
	        String ppFileNameMPS = "pp_" + ppNum + "_iteration_" + i + ".mps" ;
	        //export the pricing_problem
	        pPObject.pricing_problem.exportModel(ppFileNameLP);
	        pPObject.pricing_problem.exportModel(ppFileNameSAV);
	        pPObject.pricing_problem.exportModel(ppFileNameMPS);
	    	//tune parameters to improve performance
	        //pPObject.pricing_problem.tuneParam();
			//solve the pricing problem
	        pPObject.pricing_problem.solve();
	        //store the objective value of the pricing problem (reduced cost)
	        ppReducedCost = pPObject.pricing_problem.getObjValue();
	        //print the results
	        ReportResults2.report2(pPObject.pricing_problem, pPObject.Used_var_d_sd, pPObject.Used_var_a_vi, pPObject.Used_var_b_l_sigma_i_i1, pPObject.Used_var_p_sd_vi, pPObject.Used_var_q_sd_il);
	        
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
	        
	       
	        //Declare new configuration object
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
	        //check if column is to be added or not
	        boolean notAddCol = false;
	        MpVarZ newConfigForChainC = new MpVarZ(pPObject.scIDpP, configForNewZ);
	        ppConfig = configForNewZ;
	        //check if the configuration has already been generated for this service chain
	        for(MpVarZ entryZ : mP.usedVarZ.keySet()){
	        	if(entryZ.sCiD==serviceChainID && entryZ.configDsc.equals(configForNewZ)){
	        		System.out.println("Service Chain: " + serviceChainID + " ; New Configuration = " + configForNewZ);
	            	System.err.println("!!!!!This configuration has already been added!!!!!");
	            	//notAddCol = true;
	//            	throw new Exception("!!!!!This configuration has already been added!!!!!");
	        	}
	        }
	        String zName = "Z_No." + MpVarZ.totalNewConfigs + "_SC" + pPObject.scIDpP + "_Config" + configForNewZ;           
	        
	        
	        if(!notAddCol){
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
	            link_num = 0; 
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
//	            mP.usedVarZ.put(newConfigForChainC, mP.master_problem.numVar(nwColVarZGamma, 0.0, 1.0, zName) );
	            mP.usedVarZ.put(newConfigForChainC, mP.master_problem.numVar(nwColVarZGamma, 0.0, Double.MAX_VALUE, zName) ); 
	        }
	        //check if the CG is non-terminating
            //check if reduced cost remains
            if(i>1 && (ppReducedCost == prevPpRC) && prevConfig.equals(ppConfig)){
            	System.out.println("Current Reduced Cost = " + ppReducedCost);
            	System.out.println("Previous Reduced Cost = " + prevPpRC);
            	rcRepeat = true;
            	//System.err.println("!!!!!Reduced Cost does not change after ADDING COLUMN!!!!!");
            	//throw new Exception("!!!!!Reduced Cost does not change after ADDING COLUMN and Repeating Column!!!!!");            	
            }        
            //assign the current RC to previous RC
            prevPpRC = ppReducedCost;
            prevConfig = ppConfig;
		}
	   
		//return the reduced cost check
		return rcRepeat;
	}
	

}
