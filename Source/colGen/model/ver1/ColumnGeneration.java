package colGen.model.ver1;

import ilog.concert.IloColumn;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloRange;

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
import colGen.model.result.ReportResults;
import colGen.model.ver2.Pp2VarDelta;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class ColumnGeneration {
	
	public static long execTimeRMP;
	public static long execTimePP;
	
	public static void runIterations(boolean coreCstr, boolean capCstr, int ppNum, PricingProblem pPObject, MasterProblem mP,   
			Map<Integer,ArrayList<TrafficNodes>> serviceChainTN, Map<Integer, ServiceChain> ChainSet, 
			List<FuncPt> vnf_list, List<FuncPt> func_list, ArrayList<BaseVertex> nodesNFVI, 
			ArrayList<BaseVertex> nfv_nodes, Graph g, Map<Integer,Integer> scCopyToSC) throws Exception{
		
		ColumnGeneration.execTimeRMP = 0L;
		ColumnGeneration.execTimePP = 0L;
		//solution vectors of the sub-problem are stored here.
	 	//corresponding to the number of variables in the sub-problem			    
	 	/// COLUMN-GENERATION PROCEDURE ///
	 	//loop until conditions are satisfied
 		for(int i=0;;i++){ 			
 			
    	         	/// OPTIMIZE OVER CURRENT CONFIGURATIONS ///        	
    				//name of CG file to be exported
        			/*String cgFileNameLP = "master_problem_cg_enhanced_" + i + "_PP" + ppNum + ".lp";        			     				            			
        		    //export the master problem
					mP.master_problem.exportModel(cgFileNameLP);*/
					/*String cgFileNameSAV = "master_problem_cg_enhanced_" + i + "_PP" + ppNum + ".sav"; 
					mP.master_problem.exportModel(cgFileNameSAV);*/										
					
												
        			//SOLVE THE MASTER PROBLEM
 					long rmpStartTime = new Date().getTime();					
		            mP.master_problem.solve();
		            long rmpEndTime = new Date().getTime();
		            long rmpExecTime = (-rmpStartTime + rmpEndTime);
		            execTimeRMP += rmpExecTime;//in milli seconds
		            //total number of columns
		            System.out.println("######## Total number of columns : " + mP.master_problem.getNcols() + " #########");
		            //get the solution for the variables
//		            ReportResults.report1(mP.master_problem, mP.slackVariables, mP.usedVarZ, mP.usedVarXc, mP.usedVarY, mP.usedVarX, mP.usedVarH);				            
		            //get the reduced cost for all the variables
//					ReportResults.reducedCostperVariable(master_problem, OriginalConfigs, usedVarZ, usedVarX, usedVarY);				            
		            //get the basis status for the variables
//					ReportResults.basisCheckForVariables(master_problem, OriginalConfigs, usedVarZ, usedVarX, usedVarY);		            
		            //find the dual of the configuration selection constraint - (3)
		            //set it as the constant for the reduced cost linear expression					           
		            IloLinearNumExpr exprForReducedCost = pPObject.pricing_problem.linearNumExpr(-1.0*mP.master_problem.getDual(mP.config_selection_constraint.get(pPObject.scIDpP)));			            
		            //All the traffic (for a particular chain) will pass through this new configuration
		            double totalTraffic = 0.0;
		            for(TrafficNodes tn : serviceChainTN.get(pPObject.scIDpP)){		            
	            		totalTraffic += tn.flow_traffic;		            	
		            }
		            //Fixing the COST GAMMA FUNCTION				           	
					//iterating through traffic nodes for 
	            	//SD pair, service chain and flow_traffic
              		//iterate through the variables
            		for(Map.Entry<PpVarB, IloIntVar> entry : pPObject.Used_var_b_l_sigma_i_i1.entrySet()){           					            		
    					//taking absolute value of the possible negative dual values
    					exprForReducedCost.addTerm( entry.getValue(), totalTraffic);            			
            		}
	            	    	
				
	            	//create the list of functions for this service chain
	            	ServiceChain sC = ChainSet.get(scCopyToSC.get(pPObject.scIDpP));
	            	ArrayList<FuncPt> func_list_sc = new ArrayList<FuncPt>();
	            	//iterate through the VNF list for the model instance
	            	for(FuncPt vnf : vnf_list){
	            		//check if the vnf is part of the service chain
	            		if(sC.chain_seq.contains(vnf.getid())){
	            			//add the vnf to the function list
	            			func_list_sc.add(vnf); 
	            		}
	            	}
		            //create hashMaps to store dual values*coefficients
		            Map<PpVarA, Double> dvPlaceConfigConst = new HashMap<PpVarA, Double>();
		            Map<PpVarA, Double> dvCoreCapacityConst = new HashMap<PpVarA, Double>();
		            Map<PpVarB, Double> dvFlowCapacityConst = new HashMap<PpVarB, Double>();
		            Map<PpVarA, Double> dvRelPlaceConst1 = new HashMap<PpVarA, Double>();
		            Map<PpVarA, Double> dvRelPlaceConst2 = new HashMap<PpVarA, Double>();
		            //get dual values from the master problem  	
		            //dual value for the configuration selection constraint - (3)
//		            System.out.println("Dual value for config_selection_constraint[" + pPObject.scIDpP  + "] = " +  mP.master_problem.getDual(mP.config_selection_constraint.get(pPObject.scIDpP)) );
		            //5th constraint (negative coefficient)
		            //place configuration constraint - (5)
		            /*for(int f=0; f<func_list_sc.size(); f++){
		            	FuncPt vnf = func_list_sc.get(f);
		            	int fID = vnf.getid();
		            	int fSeqIndex = sC.chain_seq.indexOf(fID);
		            	int f_index_in_all_F = func_list.indexOf(vnf);
		            	for(Map.Entry<PpVarA, IloIntVar> entry : pPObject.Used_var_a_vi.entrySet()){
		            		//considering the same function
		            		//since a_v_sigma will have only those functions 'f' that are part of the service chain 'c'
		            		if(entry.getKey().f_seq == fSeqIndex){				            			
		            			exprForReducedCost.addTerm( entry.getValue(), -1.0*Math.abs(mP.master_problem.getDual(mP.place_config_constraint.get(f_index_in_all_F))) );
		            			//add the dual value to the hash map
		            			dvPlaceConfigConst.put(entry.getKey(), -1.0*Math.abs(mP.master_problem.getDual(mP.place_config_constraint.get(f_index_in_all_F))) );				            			
		            		}
		            	}
		            	//print only if the dual value is non-zero
            			if( Math.abs(mP.master_problem.getDual(mP.place_config_constraint.get(f_index_in_all_F))) >= InputConstants.integerTolerance ){            				
//            				System.out.println("Dual value for place_config_constraint[" + f_index_in_all_F + "] (Coeff of A_NodeX_VnfSQ" + fSeqIndex + ") = " + mP.master_problem.getDual(mP.place_config_constraint.get(f_index_in_all_F)) );
            			}
		            }*/
		            if(coreCstr){
		            //6th constraint
		            //core capacity constraint - (6)
		            for(int nfv_node_index=0; nfv_node_index < nfv_nodes.size(); nfv_node_index++){
		            	BaseVertex nfv_node = nfv_nodes.get(nfv_node_index);		            	
	            		//iterate through the function points
	            		for(FuncPt fp : func_list_sc){
	            			//check sequence number
	            			int fID = fp.getid();
			            	int fSeqIndex = sC.chain_seq.indexOf(fID);
	            			for(Map.Entry<PpVarA, IloIntVar> entry : pPObject.Used_var_a_vi.entrySet()){
	    	            		//considering the same function and the same NFV-capable node
	    	            		if( entry.getKey().checkEquality(nfv_node, fSeqIndex) ){				    	            			
	    	            			//taking absolute value of the negative dual values
	    	            			exprForReducedCost.addTerm( entry.getValue(), Math.abs(mP.master_problem.getDual(mP.core_capacity_constraint.get(nfv_node_index))*totalTraffic*fp.getcore()) );
	    	            			//add the dual value to the hash map
			            			dvCoreCapacityConst.put( entry.getKey(), Math.abs(mP.master_problem.getDual(mP.core_capacity_constraint.get(nfv_node_index))*totalTraffic*fp.getcore()));
			            		}
	    	            	}	            			
	            		}
	            		//print only if the dual value is non-zero
            			if( Math.abs(mP.master_problem.getDual(mP.core_capacity_constraint.get(nfv_node_index))) >= InputConstants.integerTolerance ){
            				//node ID for dual value
            				int nodeId = nfv_nodes.get(nfv_node_index).get_id();
//            				System.out.println("Dual value for core_capacity_constraint[" + nfv_node_index + "] (Coeff of A_Node" + nodeId + "_vnfSEQx) = " + mP.master_problem.getDual(mP.core_capacity_constraint.get(nfv_node_index)) );
            			}
		            }
		            }
		            if(capCstr){
		            //15th constraint
		            //link capacity constraint - (15)
		            int link_num = 0;//reset link number
		            for(BaseVertex s_vert : g._vertex_list){ 
						for(BaseVertex t_vert : g.get_adjacent_vertices(s_vert)){	
							//iterating through traffic nodes for 
			            	//SD pair, service chain and flow_traffic
			            	for(TrafficNodes tn : serviceChainTN.get(pPObject.scIDpP)){
			            		//iterate through the variables
			            		for(Map.Entry<PpVarB, IloIntVar> entry : pPObject.Used_var_b_l_sigma_i_i1.entrySet()){		            			
		            				//for the same link as in the iteration
		            				if( (s_vert.get_id()==entry.getKey().s_vrt.get_id()) && (t_vert.get_id()==entry.getKey().t_vrt.get_id()) ){
		            					//taking absolute value of the possible negative dual values
		            					exprForReducedCost.addTerm( entry.getValue(), Math.abs(mP.master_problem.getDual(mP.flow_capacity_constraint.get(link_num)))*tn.flow_traffic );
				        				//add the dual value to the hash map
				            			dvFlowCapacityConst.put(entry.getKey(), Math.abs(mP.master_problem.getDual(mP.flow_capacity_constraint.get(link_num)))*tn.flow_traffic );
		            				}	            								            			
			            		}
			            	}
			            	//print only if the dual value is non-zero
        					if( Math.abs(mP.master_problem.getDual(mP.flow_capacity_constraint.get(link_num))) >= InputConstants.integerTolerance ){
        						//source vertex for the link
        						int srcVrt = s_vert.get_id();
        						//destination vertex for the link
        						int destVrt = t_vert.get_id();
//        						System.out.println("Dual value for flow_capacity_constraint[" + link_num + "] (Coeff of B_" + srcVrt + "_" + destVrt + ") = " +  mP.master_problem.getDual(mP.flow_capacity_constraint.get(link_num)) );
        					}
			            	//increment the link counter
							link_num++;
						}
		            }
		            }
		            //16th constraint(negative coefficient)
		            for(Map.Entry<NwMpCstr7and8, IloRange> entryCstr : mP.rel_place_conf_constraint_1.entrySet()){
		            	 //check if same service chain
		            	 if(entryCstr.getKey().scID == pPObject.scIDpP){		            	
		            	 	 int fSeqIndex = entryCstr.getKey().fSeq;
				    		 BaseVertex nfvi_node = entryCstr.getKey().nfviNode;
				    		 for( Map.Entry<PpVarA, IloIntVar> entry : pPObject.Used_var_a_vi.entrySet() ){
			            		//considering the same function
			    			    //considering the same node
			            		//since a_v_sigma will have only those functions 'f' that are part of the service chain 'c'
			            		//if(entry.getKey().f_id == f_id){
			    			 	if( entry.getKey().checkEquality(nfvi_node, fSeqIndex) ){
//			            			exprForReducedCost.addTerm( entry.getValue(), -1.0*Math.abs(mP.master_problem.getDual( entryCstr.getValue() )) );
			            			exprForReducedCost.addTerm( entry.getValue(), -1.0*mP.master_problem.getDual( entryCstr.getValue() ) );
			            			//add the dual value to the hash map
//			            			dvRelPlaceConst1.put( entry.getKey(), -1.0*Math.abs(mP.master_problem.getDual( entryCstr.getValue() )) );
			            			dvRelPlaceConst1.put( entry.getKey(), -1.0*mP.master_problem.getDual( entryCstr.getValue() ) );
			    			 	}
				              }
				    		  //print only if the dual value is non-zero
		            		  if( Math.abs(mP.master_problem.getDual( entryCstr.getValue() )) >= InputConstants.integerTolerance ){					            			
		            			//Print out the dual values
		            			//System.out.println("Dual value for rel_place_conf_constraint_1 (Coeff of A_Node" + nfv_node.get_id() + "_vnfSEQ" + f_id + ") = " + mP.master_problem.getDual(entryCstr.getValue()) );
		            		  }	
		            	 }
					}
		            //17th constraint
		            /*for(Map.Entry<NwMpCstr7and8, IloRange> entryCstr : mP.rel_place_conf_constraint_2.entrySet()){
		            	 //check if same service chain
		            	 if(entryCstr.getKey().scID == pPObject.scIDpP){		            	
		            		 int fSeqIndex = entryCstr.getKey().fSeq;
		            		 BaseVertex nfvi_node = entryCstr.getKey().nfviNode;
				    		 for( Map.Entry<PpVarA, IloIntVar> entry : pPObject.Used_var_a_vi.entrySet() ){
			            		//considering the same function
			            		//since a_v_sigma will have only those functions 'f' that are part of the service chain 'c'
			            		if(entry.getKey().checkEquality(nfvi_node, fSeqIndex)){
				            		//taking absolute value of the negative dual values
			            			exprForReducedCost.addTerm( entry.getValue(), Math.abs(mP.master_problem.getDual( entryCstr.getValue() )) );
			            			//add the dual value to the hash map
			            			dvRelPlaceConst2.put(entry.getKey(), Math.abs(mP.master_problem.getDual( entryCstr.getValue() )) );
			            		}
				              }
				    		  //print only if the dual value is non-zero
		            		  if( Math.abs(mP.master_problem.getDual( entryCstr.getValue() )) >= InputConstants.integerTolerance ){					            			
			            		  //Print out the dual values
		            			  //System.out.println("Dual value for rel_place_conf_constraint_2 (Coeff of A_Node" + nfv_node.get_id() + "_VNF" + f_id + ") = " +  mP.master_problem.getDual(entryCstr.getValue()) );
		            		  }
		            	 }
					}*/


		            
		            ///SET THE EXPPRESSION FOR THE REDUCED COST///
	            	pPObject.reducedCost.setExpr(exprForReducedCost);        
		            //name of CG file to be exported
		            /*String ppFileNameLP = "pp_" + ppNum + "_iteration_" + i + ".lp" ;
		            String ppFileNameSAV = "pp_" + ppNum + "_iteration_" + i + ".sav" ;
		            //export the pricing_problem
		            pPObject.pricing_problem.exportModel(ppFileNameLP);
		            pPObject.pricing_problem.exportModel(ppFileNameSAV);*/
					//solve the pricing problem
	            	long ppStartTime = new Date().getTime();
		            pPObject.pricing_problem.solve();
		            long ppEndTime = new Date().getTime();
		            long ppExecutionTime = (ppEndTime -ppStartTime);
		            execTimePP += ppExecutionTime;
		            //store the objective value of the pricing problem (reduced cost)
		            double ppReducedCost = pPObject.pricing_problem.getObjValue();
		            //print the results
//		            ReportResults.report2(pPObject.pricing_problem, pPObject.Used_var_a_vi, pPObject.Used_var_b_l_sigma_i_i1);
		          
		            
		            
		            
		          
		            //CHECK OPTIMALITY CONDITION
		            if( pPObject.pricing_problem.getObjValue() > -InputConstants.RC_EPS )
		               break;				          
		            //store the solution of the sub-problem 
		            Map<PpVarA, Double> newConfig_var_a_v_sigma = new HashMap<PpVarA, Double>(); 
		            for( Map.Entry<PpVarA, IloIntVar> entry : pPObject.Used_var_a_vi.entrySet() ){
		            	newConfig_var_a_v_sigma.put(entry.getKey(), pPObject.pricing_problem.getValue(entry.getValue()));
		            }
		            Map<PpVarB, Double> newConfig_var_b_l_sigma_i_i1 = new HashMap<PpVarB, Double>(); 
		            for( Map.Entry<PpVarB, IloIntVar> entry : pPObject.Used_var_b_l_sigma_i_i1.entrySet() ){
		            	newConfig_var_b_l_sigma_i_i1.put(entry.getKey(), pPObject.pricing_problem.getValue(entry.getValue()));
		            }		
		            
		            
		            //Declare New Configuration Object
		            //Configuration Delta variables
		            Set<Pp2VarDelta> DeltaVarSet = new HashSet<Pp2VarDelta>();
		            for(TrafficNodes sd : serviceChainTN.get(pPObject.scIDpP)){
		            	DeltaVarSet.add(new Pp2VarDelta(sd));
		            }		          
		            //Configuration A variables
		            String configForNewZ = "";
		            //get the configuration that has been selected
		            for(int fSeq = 0; fSeq < sC.chain_seq.size() ; fSeq++){
		            	//iterate through the list of double values
		            	for(Map.Entry<PpVarA, Double> entry : newConfig_var_a_v_sigma.entrySet()){
		            		//if the variable has been set
		            		if(entry.getValue() == 1.0){
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
		            //add the variable to the set of variables
		            MpVarZ newConfigForChainC = new MpVarZ(pPObject.scIDpP, configForNewZ, DeltaVarSet, BVarSet);
		            String zName = "Z_No." + MpVarZ.totalNewConfigs + "_SC" + pPObject.scIDpP + "_Config" + configForNewZ;

		            
		            ///ADD NEW COLUMN TO THE MASTER PROBLEM///				            
                    //calculate coefficient of the objective - (2)		          
		            double link_included = 0.0;
		            //iterating over the links
		            for(BaseVertex s_vert : g._vertex_list){ 
						for(BaseVertex t_vert : g.get_adjacent_vertices(s_vert)){
						  //checking for successive dependencies
						  for(int chain_seq_pos = 0; chain_seq_pos < sC.chain_size - 1; chain_seq_pos++){
							  for(Map.Entry<PpVarB, Double> entry : newConfig_var_b_l_sigma_i_i1.entrySet()){
								  //check if variable matches the parameters
								  if(entry.getKey().checkEquality(pPObject.scIDpP, chain_seq_pos, chain_seq_pos+1, s_vert, t_vert)){
									 link_included += entry.getValue().doubleValue(); 
								  }
							  }							  
						  }	 
						} 
					}				          
		            //multiply with the number of links included				           
		            double objcoeff = totalTraffic*link_included;
		            //THE NEW COLUMN
		            //add the coefficient to the Objective - (2) 
		            //only one configuration is added in one iteration
		            IloColumn nwColVarZGamma = mP.master_problem.column(mP.BwUsed, objcoeff);   
		            //add the column to the configuration constraint - (3)
		            nwColVarZGamma = nwColVarZGamma.and(mP.master_problem.column(mP.config_selection_constraint.get(pPObject.scIDpP), 1.0));
		            //including the columns for configuration constraints - (5)       
		           /* for(int f = 0; f < func_list_sc.size(); f++){	
            			 int f_id = func_list_sc.get(f).getid();
            			 int fSeqIndex = sC.chain_seq.indexOf(f_id);
            			 FuncPt vnf = func_list_sc.get(f);
			             int f_index_in_all_F = func_list.indexOf(vnf);
            			 double coeff = 0.0;
            			 for(BaseVertex nfvi_node : nodesNFVI){
            				 for(Map.Entry<PpVarA, Double> entry : newConfig_var_a_v_sigma.entrySet()){
            					 //check if same a_v_sigma
            					 if( entry.getKey().checkEquality(nfvi_node, fSeqIndex) ){
            						 coeff+=entry.getValue();
            					 }
            				 }
            			 }
            			 //modify the column //add the coefficient to the configuration constraint 
            			 nwColVarZGamma = nwColVarZGamma.and(mP.master_problem.column(mP.place_config_constraint.get(f_index_in_all_F),coeff) );
//						 System.out.println("place_config_constraint: " + coeff);
            		 }*/
		            System.out.println(zName);
		            System.out.println("Total traffic = " + totalTraffic);
		            //including the columns for the core capacity constraints - (6)
		            //iterating over the set of NFV-capable nodes				          
		            for(int vnf_node_index=0; vnf_node_index < nfv_nodes.size(); vnf_node_index++){
		            	BaseVertex nfvNode = nfv_nodes.get(vnf_node_index);
		            	double coeffCore = 0.0;
		            	for(FuncPt f : func_list_sc){
		            		int f_id = f.getid();
	            			int fSeqIndex = sC.chain_seq.indexOf(f_id);
		            		for(Map.Entry<PpVarA, Double> entry : newConfig_var_a_v_sigma.entrySet()){
		            			//check if same a_v_sigma
		            			if( entry.getKey().checkEquality(nfvNode, fSeqIndex) && (entry.getValue()!=0.0)){
//					            				coeff+=entry.getValue().doubleValue()*f.getcore()*SDPairTraffic;
		            				System.out.println("\tVertex = " + nfvNode.get_id() + " ; CPU cores = ");
		            				System.out.println("\t\tVertex = " + nfvNode.get_id() + " ; Total CPU cores = " + coeffCore);
		            				coeffCore+=f.getcore()*totalTraffic;
		            			}
		            		}	            				 
		            	}
		            	if(coeffCore != 0.0){
		            		System.out.println("\t\t\tVertex = " + nfvNode.get_id() + " ; Final CPU cores = " + coeffCore);
		            		newConfigForChainC.addCoreCount(nfvNode, coeffCore);
//		            		System.out.println("\t\tCoefficient for core capacity constraint[ Node " + nfvNode.get_id() + " ]= " + coeffCore);
		            	}
		            	if(coreCstr){
			            	//modify the column //add the coefficient to the configuration constraint 
			            	nwColVarZGamma = nwColVarZGamma.and(mP.master_problem.column(mP.core_capacity_constraint.get(vnf_node_index), coeffCore) );	
		            	}
//						 System.out.println("core capacity constraint: " + coeff);
		            }
		            //including the columns for the flow capacity constraints - (15)				            	
		            //reset the link counter
		            int link_num = 0; 
		            for(BaseVertex s_vert : g._vertex_list){ 
		            	for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){
		            		link_included = 0.0;
		            		NodePair link = new NodePair(s_vert,t_vert);
		            		//checking for successive dependencies
		            		for(int chain_seq_pos = 0; chain_seq_pos < sC.chain_size - 1; chain_seq_pos++){
		            			for(Map.Entry<PpVarB, Double> entry : newConfig_var_b_l_sigma_i_i1.entrySet()){
		            				//check if variable matches the parameters
		            				if(entry.getKey().checkEquality(pPObject.scIDpP, chain_seq_pos, chain_seq_pos+1, s_vert, t_vert)){
		            					link_included+=entry.getValue(); 
		            				}
		            			}							  
		            		}
		            		double coeffLink = link_included*totalTraffic;
		            		if(coeffLink != 0.0){
		            			newConfigForChainC.addLinkCapacity(link, coeffLink);
//		            			System.out.println("\t\tCoefficient for link capacity constraint [" + link_num + "]: "  + coeffLink);
		            		}
		            		 //Link capacity constraints
		                    if(capCstr){
			            		//modify the column //add the coefficient to the configuration constraint 
			            		nwColVarZGamma = nwColVarZGamma.and(mP.master_problem.column(mP.flow_capacity_constraint.get(link_num),  coeffLink) );
		                    }
//							System.out.println("full flow for link " + link_num + " : " + link_included);
		            		//increment the link counter
		            		link_num++;
		            	}
		            }
		            //including the columns for function placement relating to configuration selection constraint - 1 - (16) 				     
		            for(Map.Entry<NwMpCstr7and8, IloRange> entryCstr : mP.rel_place_conf_constraint_1.entrySet()){
		            	//check if same service chain
		            	if(entryCstr.getKey().scID == pPObject.scIDpP){	            	 	 
	            	 	 	int fSeqIndex = entryCstr.getKey().fSeq;
	            	 	 	BaseVertex nfvi_node = entryCstr.getKey().nfviNode;					            		
		            		for(Map.Entry<PpVarA, Double> entry : newConfig_var_a_v_sigma.entrySet()){
		            			//check if same a_v_sigma
		            			if( entry.getKey().checkEquality(nfvi_node, fSeqIndex) ){
		            				//modify the column //add the coefficient to the configuration constraint 
		            				nwColVarZGamma = nwColVarZGamma.and(mP.master_problem.column( entryCstr.getValue(), entry.getValue() ) ); 		     		           			   
//						    					System.out.println("VNF Location Constraint 16 coefficient :" + -1.0*entry.getValue().doubleValue());
		            			}
		            		}
		            	}
		            }			            
		            //including the columns for function placement relating to configuration selection constraint - 2 - (17)		            	
		            /*for(Map.Entry<NwMpCstr7and8, IloRange> entryCstr : mP.rel_place_conf_constraint_2.entrySet()){
		            	//check if same service chain
		            	if(entryCstr.getKey().scID == pPObject.scIDpP){	            	 	 	
	            	 	 	int fSeqIndex = entryCstr.getKey().fSeq;
	            	 	 	BaseVertex nfvi_node = entryCstr.getKey().nfviNode;
		            		for(Map.Entry<PpVarA, Double> entry : newConfig_var_a_v_sigma.entrySet()){
		            			//check if same a_v_sigma
		            			if( entry.getKey().checkEquality(nfvi_node, fSeqIndex) ){
		            				//modify the column //add the coefficient to the configuration constraint 
		            				nwColVarZGamma = nwColVarZGamma.and( mP.master_problem.column( entryCstr.getValue(), entry.getValue() ) ); 					     		           			   
//						    					System.out.println("VNF Location Constraint 17 coefficient :" + entry.getValue().doubleValue());
		            			}
		            		}
		            	}
		            }*/
		            
		            
//		            mP.usedVarZ.put(newConfigForChainC, mP.master_problem.numVar(nwColVarZGamma, 0.0, 1.0, zName) );
		            mP.usedVarZ.put(newConfigForChainC, mP.master_problem.numVar(nwColVarZGamma, 0.0, Double.MAX_VALUE, zName) );
		            //print out the core counts
//					 System.out.println("Config:" + newConfigForChainC.configDsc + "\t" + newConfigForChainC.sCiD);
					/*for(Map.Entry<BaseVertex, Double> corePerNode : newConfigForChainC.nfvNodeCpuCores.entrySet()){
						 System.out.println("\t\tNode " + corePerNode.getKey().get_id() + " = " + corePerNode.getValue());
					 }*/
//		            MasterProblem.configurationCounter++;
//					            usedVarZ.put(new MpVarZ(smallestInitialCostSDpair.getKey()), master_problem.numVar(nwColVarZGamma, 0.0, 1.0) );
//					            System.out.println("############# Configuration No. : " + MpVarZ.totalNewConfigs + " added to the master problem #############"); 
		            //check the reduced cost
//					            CheckFunctions.checkReducedCost(minInitialCost, g, pair_list, func_list_sc, vertex_list_without_nfv_nodes, newConfig_var_a_v_sigma, newConfig_var_b_l_sigma_i_i1, dvPlaceConfigConst, dvCoreCapacityConst, dvFlowCapacityConst, dvRelPlaceConst1, dvRelPlaceConst2, pricing_problem);;
		            //reduced cost from PP objective
//					            System.out.println("Reduced cost from the Pricing Problem Objective : " + ppReducedCost); 
		            //set the number of simplex iterations to zero
//					            if( i == (InputConstants.cgIterations-2) )
//					            	master_problem.setParam(IloCplex.IntParam.ItLim, 0);				            
		           			          
        
 		} 		
	}

}
