package colGen.model.ver1;

import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ILP.FuncPt;
import ILP.TrafficNodes;
import colGen.model.ver2.Pp2VarDelta;
import colGen.model.ver2.Pp2VarP;
import colGen.model.ver2.Pp2VarQ;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class CheckFunctions {

	//to calculate the reduced cost in CG model version 1
	public static void checkReducedCost(double initialCostPerChain, Graph g,  List<TrafficNodes> pair_list, List<FuncPt> func_list, ArrayList<BaseVertex> nfv_nodes, 
			Map<PpVarA, Double> newConfig_var_a_v_sigma, Map<PpVarB, Double> newConfig_var_b_l_sigma_i_i1,	Map<PpVarA, Double> dvPlaceConfigConst,  
			Map<PpVarA, Double> dvCoreCapacityConst, Map<PpVarB, Double> dvFlowCapacityConst, Map<PpVarA, Double> dvRelPlaceConst1,  
			Map<PpVarA, Double> dvRelPlaceConst2, IloCplex pricing_problem){
		
			//initialize the reduced cost		
			double reducedCost = initialCostPerChain;
			
			//2nd constraint (negative coefficient)      
	    	for(Map.Entry<PpVarA, Double> entry : newConfig_var_a_v_sigma.entrySet()){    		
	    		//subtract the relevant terms from the reduced cost
	    		reducedCost += entry.getValue()*dvPlaceConfigConst.get(entry.getKey());  
	    		System.out.println( "A_Node" + entry.getKey().node.get_id() + "_vnfSEQ" + entry.getKey().f_seq + " = " + entry.getValue() + " & Coefficient(includes dual) = " + dvPlaceConfigConst.get(entry.getKey()) );
	    	}        
	        //3rd constraint    
			for(Map.Entry<PpVarA, Double> entry : newConfig_var_a_v_sigma.entrySet()){
				//subtract the relevant terms from the reduced cost
	    		reducedCost += entry.getValue()*dvCoreCapacityConst.get(entry.getKey());  
	    		System.out.println( "A_Node" + entry.getKey().node.get_id() + "_vnfSEQ" + entry.getKey().f_seq + " = " + entry.getValue() + " & Coefficient(includes dual) = " + dvCoreCapacityConst.get(entry.getKey()) );
	    	}     
	        //12th constraint       
			for(Map.Entry<PpVarB, Double> entry : newConfig_var_b_l_sigma_i_i1.entrySet()){
				//subtract the relevant terms from the reduced cost
	    		reducedCost += entry.getValue()*dvFlowCapacityConst.get(entry.getKey()); 
	    		System.out.println( "B_Ls" + entry.getKey().s_vrt.get_id() + "_Ld" + entry.getKey().t_vrt.get_id() + "_vP" + entry.getKey().s_f_index + "_vN" + entry.getKey().t_f_index + "_SC" + entry.getKey().sc_index + " = " + entry.getValue() + " & Coefficient(includes dual) = " + dvFlowCapacityConst.get(entry.getKey())  );
			}      
	        //13th constraint(negative coefficient)       
			for(Map.Entry<PpVarA, Double> entry : newConfig_var_a_v_sigma.entrySet()){
				//subtract the relevant terms from the reduced cost
	     		reducedCost += entry.getValue()*dvRelPlaceConst1.get(entry.getKey()); 
	     		System.out.println( "A_Node" + entry.getKey().node.get_id() + "_vnfSEQ" + entry.getKey().f_seq + " = " + entry.getValue() + " & Coefficient(includes dual) = " + dvRelPlaceConst1.get(entry.getKey()) );
	        }	 
	        //14th constraint       
		    for(Map.Entry<PpVarA, Double> entry : newConfig_var_a_v_sigma.entrySet()){
				//subtract the relevant terms from the reduced cost
		     	reducedCost += entry.getValue()*dvRelPlaceConst2.get(entry.getKey()); 
		     	System.out.println( "A_Node" + entry.getKey().node.get_id() + "_vnfSEQ" + entry.getKey().f_seq + " = " + entry.getValue() + " & Coefficient(includes dual) = " + dvRelPlaceConst1.get(entry.getKey()) );
	        }
			 
		    System.out.println("######### Initial cost = " + initialCostPerChain + " #########");
		    System.out.println("######### Reduced cost after inserting the new configuration is : " + reducedCost + " #########");
		
	}
	
	
	
	
	
	//check reduced cost for version 2
	public static double checkReducedCost2(double dualVal45, Graph g,  List<TrafficNodes> pair_list, List<FuncPt> func_list, ArrayList<BaseVertex> nfv_nodes,
			Map<Pp2VarDelta, Double> newConfig_var_d_sd, Map<Pp2VarP, Double> newConfig_var_p_sd_vi, Map<Pp2VarQ, Double> newConfig_var_q_sd_li, 
			Map<PpVarA, Double> newConfig_var_a_v_sigma, Map<PpVarA, Double> dvFuncInstCountConst, Map<Pp2VarP, Double> dvCoreCapacityConst, 
			Map<Pp2VarQ, Double> dvFlowCapacityConst,Map<Pp2VarQ, Double> dvLatencyConst, Map<Pp2VarDelta, Double> dvOnePathPerSdConst,  Map<Pp2VarP, Double> dvSrcOutgoigConst, 
			Map<Pp2VarP, Double> dvSrcIncomingConst, Map<Pp2VarP, Double> dvFlowForFirst2, Map<Pp2VarP, Double> dvDstIncomingConst, 
			Map<Pp2VarP, Double> dvDstOutgoingConst, Map<Pp2VarP, Double> dvFlowForLast2, IloCplex pricingProb){
		
		
		//print out the coefficients in the Pricing Problem
        System.out.println("\n");
        System.out.println("\t\t!!!Printing out coefficients in Reduced Cost Expression!!!"); 
        System.out.println("\t\tReduced Cost CONSTANT = " + -1.0*dualVal45);
        //printing out Delta variables
        for( Map.Entry<Pp2VarDelta, Double> entryD : dvOnePathPerSdConst.entrySet()){
        	if(entryD.getValue() != 0){
        		System.out.println( "\t\tReduced Cost coefficient for Delta_Src" + entryD.getKey().sd.v1.get_id() + "_Dst" + entryD.getKey().sd.v2.get_id() + " = " + entryD.getValue() );
        	}            	
        }
        //printing out A variables
        for( Map.Entry<PpVarA, Double> entryA : dvFuncInstCountConst.entrySet() ){
        	if(entryA.getValue() != 0){
        		System.out.println( "\t\tReduced Cost coefficient for A_Node" + entryA.getKey().node.get_id() + "_vnfSEQ" + entryA.getKey().f_seq + " = " + entryA.getValue());
        	}
        }            
        //printing out Q variables
        Map<Pp2VarQ, Double> coeffPerVarQ = new HashMap<Pp2VarQ, Double>();
        for( Map.Entry<Pp2VarQ, Double> entryQ : newConfig_var_q_sd_li.entrySet() ){
        	double CoeffY = 0.0;
        	if(dvFlowCapacityConst.get(entryQ.getKey()) != null){
        		CoeffY += dvFlowCapacityConst.get(entryQ.getKey());
        	}
        	if(dvLatencyConst.get(entryQ.getKey()) != null){
        		CoeffY += dvLatencyConst.get(entryQ.getKey());
        	}
        	coeffPerVarQ.put(entryQ.getKey(), CoeffY);
        }
        //printing out the Q variables
        for( Map.Entry<Pp2VarQ, Double> entryQ : coeffPerVarQ.entrySet()){
        	if(entryQ.getValue() != 0){
        		System.out.println( "\t\tReduced Cost coefficient for Q_Src" + entryQ.getKey().sd.v1.get_id() + "_Dst" + entryQ.getKey().sd.v2.get_id() + "_VnfSQ" + entryQ.getKey().f_seq 
		    			+ "_sL" + entryQ.getKey().srcL.get_id() + "_sT" +  entryQ.getKey().tarL.get_id() + " = " + entryQ.getValue());
        	}
        }
        Map<Pp2VarP, Double> coeffPerVarP = new HashMap<Pp2VarP, Double>();
        //calculating the coefficients of P variables in the reduced cost
        for( Map.Entry<Pp2VarP, Double> entryP : newConfig_var_p_sd_vi.entrySet()){
        	double Coeff = 0.0;
        	if(dvCoreCapacityConst.get(entryP.getKey()) != null){
        		Coeff += dvCoreCapacityConst.get(entryP.getKey()); 
        	}
        	if(dvDstIncomingConst.get(entryP.getKey()) != null){
        		Coeff += dvDstIncomingConst.get(entryP.getKey()); 
        	}
        	if(dvDstOutgoingConst.get(entryP.getKey()) != null){
        		Coeff += dvDstOutgoingConst.get(entryP.getKey()); 
        	}
        	if(dvFlowForFirst2.get(entryP.getKey()) != null){
        		Coeff += dvFlowForFirst2.get(entryP.getKey()); 
        	}
        	if(dvFlowForLast2.get(entryP.getKey()) != null){
        		Coeff += dvFlowForLast2.get(entryP.getKey()); 
        	}
        	if(dvSrcIncomingConst.get(entryP.getKey()) != null){
        		Coeff += dvSrcIncomingConst.get(entryP.getKey()); 
        	}
        	if(dvSrcOutgoigConst.get(entryP.getKey()) != null){
        		Coeff += dvSrcOutgoigConst.get(entryP.getKey()); 
        	}            	
        	coeffPerVarP.put(entryP.getKey(), Coeff);
        }
        //printing out X variables
        for( Map.Entry<Pp2VarP, Double> entryP : coeffPerVarP.entrySet()){
        	if(entryP.getValue() != 0){
        		System.out.println( "\t\tReduced Cost coefficient for P_Src" + entryP.getKey().sd.v1.get_id() + "_Dst" + entryP.getKey().sd.v2.get_id() + "_Node"
	       				+ entryP.getKey().nfviNode.get_id() + "_VnfSQ" + entryP.getKey().f_seq  + " = " + entryP.getValue() );
        	}
        }		
		
		
		System.out.println();
		System.out.println("\t\t!!!Reduced Cost Calculation!!!");
		//intialize the reduced cost
		double reducedCost = dualVal45;
		System.out.println("\t\tCONSTANT = " + dualVal45);
		//calculate the value of cost gamma
        double costGamma  = 0.0;
		for(Map.Entry<Pp2VarQ, Double> entryQ : newConfig_var_q_sd_li.entrySet()){ 
			double valueY = entryQ.getValue();			
			if(valueY != 0){
				double coeffY = valueY*entryQ.getKey().sd.flow_traffic;
				costGamma += coeffY;
				System.out.println("\t\tCOST GAMMA Contribution of Q_Src" + entryQ.getKey().sd.v1.get_id() + "_Dst" + entryQ.getKey().sd.v2.get_id() + "_VnfSQ" + entryQ.getKey().f_seq 
		    			+ "_sL" + entryQ.getKey().srcL.get_id() + "_sT" +  entryQ.getKey().tarL.get_id() + " : " + valueY*entryQ.getKey().sd.flow_traffic);
			}
		}
		System.out.println("\t\tCOST GAMMA = " + costGamma);
		//update the reduced cost
		reducedCost += costGamma;
		
		//50 - Delta variable
		for(Map.Entry<Pp2VarDelta, Double> entryD : newConfig_var_d_sd.entrySet()){		
			if(entryD.getValue() != 0){
				reducedCost += entryD.getValue()*dvOnePathPerSdConst.get(entryD.getKey());
				System.out.println( "\t\tContribution of (9) Delta_Src" + entryD.getKey().sd.v1.get_id() + "_Dst" + entryD.getKey().sd.v2.get_id() + " : " + dvOnePathPerSdConst.get(entryD.getKey()));
			}			
		}
		//46 - A variables
		for(Map.Entry<PpVarA, Double> entryA : newConfig_var_a_v_sigma.entrySet()){
			if(dvFuncInstCountConst.get(entryA.getKey()) != null){
				reducedCost += entryA.getValue()*dvFuncInstCountConst.get(entryA.getKey());
				if(entryA.getValue() != 0){			
					System.out.println( "\t\tContribution of (4) A_Node" + entryA.getKey().node.get_id() + "_vnfSEQ" + entryA.getKey().f_seq + " : " + dvFuncInstCountConst.get(entryA.getKey()));
				}
			}
		}
		//System.out.println("\tReduced Cost after adding A contributions : " + reducedCost);
		//48 - P variables
		for(Map.Entry<Pp2VarP, Double> entryP : newConfig_var_p_sd_vi.entrySet()){
			/*System.out.println("P_Src" + entryP.getKey().sd.v1.get_id() + "_Dst" + entryP.getKey().sd.v2.get_id() + "_Node"
       				+ entryP.getKey().nfviNode.get_id() + "_VnfSQ" + entryP.getKey().f_seq  + " : " + entryP.getValue());*/
			if(entryP.getValue() != null){
				if(dvCoreCapacityConst.get(entryP.getKey()) != null){					
					if(Math.abs(entryP.getValue()) != 0 ){
						reducedCost += dvCoreCapacityConst.get(entryP.getKey());
						System.out.println( "\t\tContribution of (6) P_Src" + entryP.getKey().sd.v1.get_id() + "_Dst" + entryP.getKey().sd.v2.get_id() + "_Node"
			       				+ entryP.getKey().nfviNode.get_id() + "_VnfSQ" + entryP.getKey().f_seq  + " : " + dvCoreCapacityConst.get(entryP.getKey()));
					}
				}
				//55
				if(dvSrcOutgoigConst.get(entryP.getKey()) != null){													
					if(entryP.getValue() != 0){
						reducedCost += entryP.getValue()*dvSrcOutgoigConst.get(entryP.getKey());
						System.out.println( "\t\tContribution of (10) P_Src" + entryP.getKey().sd.v1.get_id() + "_Dst" + entryP.getKey().sd.v2.get_id() + "_Node"
			       				+ entryP.getKey().nfviNode.get_id() + "_VnfSQ" + entryP.getKey().f_seq  + " : " + dvSrcOutgoigConst.get(entryP.getKey()));
					}					
				}
				//56
				if(dvSrcIncomingConst.get(entryP.getKey()) != null){				
					if(entryP.getValue() != 0){
						reducedCost += entryP.getValue()*dvSrcIncomingConst.get(entryP.getKey());
						System.out.println( "\t\tContribution of (11) P_Src" + entryP.getKey().sd.v1.get_id() + "_Dst" + entryP.getKey().sd.v2.get_id() + "_Node"
			       				+ entryP.getKey().nfviNode.get_id() + "_VnfSQ" + entryP.getKey().f_seq  + " : " + dvSrcIncomingConst.get(entryP.getKey()));
					}
				}
				//57
				if(dvFlowForFirst2.get(entryP.getKey()) != null){										
					if(entryP.getValue() != 0){
						reducedCost += entryP.getValue()*dvFlowForFirst2.get(entryP.getKey());	
						System.out.println( "\t\tContribution of (12) P_Src" + entryP.getKey().sd.v1.get_id() + "_Dst" + entryP.getKey().sd.v2.get_id() + "_Node"
			       				+ entryP.getKey().nfviNode.get_id() + "_VnfSQ" + entryP.getKey().f_seq  + " : " + dvFlowForFirst2.get(entryP.getKey()));
					}					
				}
				//59
				if(dvDstIncomingConst.get(entryP.getKey()) != null){				
					if(entryP.getValue() != 0){
						reducedCost += entryP.getValue()*dvDstIncomingConst.get(entryP.getKey());
						System.out.println( "\t\tContribution of (14) P_Src" + entryP.getKey().sd.v1.get_id() + "_Dst" + entryP.getKey().sd.v2.get_id() + "_Node"
			       				+ entryP.getKey().nfviNode.get_id() + "_VnfSQ" + entryP.getKey().f_seq  + " : " + dvDstIncomingConst.get(entryP.getKey()));
					}
				}
				//60
				if(dvDstOutgoingConst.get(entryP.getKey()) != null){					
					if(entryP.getValue() != 0){
						reducedCost += entryP.getValue()*dvDstOutgoingConst.get(entryP.getKey());
						System.out.println( "\t\tContribution of (15) P_Src" + entryP.getKey().sd.v1.get_id() + "_Dst" + entryP.getKey().sd.v2.get_id() + "_Node"
			       				+ entryP.getKey().nfviNode.get_id() + "_VnfSQ" + entryP.getKey().f_seq  + " : " + dvDstOutgoingConst.get(entryP.getKey()));
					}
				}
				//61
				if(dvFlowForLast2.get(entryP.getKey()) != null){					
					if(entryP.getValue() != 0){
						reducedCost += entryP.getValue()*dvFlowForLast2.get(entryP.getKey());
						System.out.println( "\t\tContribution of (16) P_Src" + entryP.getKey().sd.v1.get_id() + "_Dst" + entryP.getKey().sd.v2.get_id() + "_Node"
			       				+ entryP.getKey().nfviNode.get_id() + "_VnfSQ" + entryP.getKey().f_seq  + " : " + dvFlowForLast2.get(entryP.getKey()));
					}
				}
				/*System.out.println("\tReduced Cost after adding X_Src" + entryP.getKey().sd.v1.get_id() + "_Dst" + entryP.getKey().sd.v2.get_id() + "_Node"
			       				+ entryP.getKey().nfviNode.get_id() + "_VnfSQ" + entryP.getKey().f_seq  + " contributions : " + reducedCost);*/
			}
		}
		//System.out.println("\tReduced Cost after adding X contributions : " + reducedCost);
		//49 - Y variable
		for(Map.Entry<Pp2VarQ, Double> entryQ : newConfig_var_q_sd_li.entrySet()){			
			if(entryQ.getValue() != 0){
				if(dvFlowCapacityConst.get(entryQ.getKey()) != null){
					reducedCost += entryQ.getValue()*dvFlowCapacityConst.get(entryQ.getKey());
					System.out.println( "\t\tContribution of (7) Q_Src" + entryQ.getKey().sd.v1.get_id() + "_Dst" + entryQ.getKey().sd.v2.get_id() + "_VnfSQ" + entryQ.getKey().f_seq 
			    			+ "_sL" + entryQ.getKey().srcL.get_id() + "_sT" +  entryQ.getKey().tarL.get_id() + " : " + dvFlowCapacityConst.get(entryQ.getKey()));
				}
				if(dvLatencyConst.get(entryQ.getKey()) != null){
					reducedCost += entryQ.getValue()*dvLatencyConst.get(entryQ.getKey());
					System.out.println( "\t\tContribution of (8) Q_Src" + entryQ.getKey().sd.v1.get_id() + "_Dst" + entryQ.getKey().sd.v2.get_id() + "_VnfSQ" + entryQ.getKey().f_seq 
			    			+ "_sL" + entryQ.getKey().srcL.get_id() + "_sT" +  entryQ.getKey().tarL.get_id() + " : " + dvLatencyConst.get(entryQ.getKey()));
				}
			}
		}			
		System.out.println("\t\t#### Calculated Reduced Cost : " + reducedCost + " ####");		
		return reducedCost;	
	}
	
	
}
