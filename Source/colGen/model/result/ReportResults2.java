package colGen.model.result;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Map;

import Given.InputConstants;
import ILP.NodePair;
import ILP.TrafficNodes;
import colGen.model.ver1.MpVarH;
import colGen.model.ver1.MpVarX;
import colGen.model.ver1.MpVarY;
import colGen.model.ver1.MpVarZ;
import colGen.model.ver1.NewIngEgConfigs;
import colGen.model.ver1.PpVarA;
import colGen.model.ver1.PpVarB;
import colGen.model.ver2.Pp2VarDelta;
import colGen.model.ver2.Pp2VarP;
import colGen.model.ver2.Pp2VarQ;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class ReportResults2 {	
	
	 static void reportSlackVariables(IloCplex master_problem, Map<String,IloNumVar> slackVariables){
		 System.out.println();
		 System.out.println("****Printing out the slack variables*****");
		 try{
			 for(Map.Entry<String, IloNumVar> entry : slackVariables.entrySet()){
				 if( master_problem.getValue(entry.getValue()) != 0 ){
		    		  System.out.println(entry.getKey() + " : " + master_problem.getValue(entry.getValue()) );	    	 
		    	 }
			 }
		 }catch(IloException exp){
			 System.out.println("Exception caught while printing slack variables");
		 }		 
	 }
	
	public static void reportIntegers(IloCplex master_problem, Map<MpVarZ, IloNumVar> usedVarZ, Map<MpVarX, IloNumVar> usedVarX, 
			Map<MpVarY, IloNumVar> usedVarY, Map<MpVarH,IloNumVar> usedVarH) throws IloException {
	      System.out.println();
	      System.out.println("RMP ILP Objective Value : " + master_problem.getObjValue() );    
	      System.out.println();
	      //total number of new configurations added by the pricing problem
	      System.out.println("######### Total number of NEW CONFIGURATIONS : " + MpVarZ.totalNewConfigs + " ##############");
	      //values for variable Z
	      for(Map.Entry<MpVarZ, IloNumVar> entry : usedVarZ.entrySet()){
	    	  if(master_problem.getValue(entry.getValue()) != 0.0){
	    		  System.out.println("Z No. " + entry.getKey().cgConfig + " : " + master_problem.getValue(entry.getValue()));
	    		  System.out.println("\t\t\t\tSC" + entry.getKey().sCiD);
	    		  System.out.println("\t\t\t\tConfig " + entry.getKey().configDsc);
	    		  //if (s,d) pairs are selected
	    		  if(!entry.getKey().DeltaVarSet.isEmpty()){
		    		  System.out.print("\t\t\t\t(S,D): ");
		    		  //print out D variables
		    		  for(Pp2VarDelta varD : entry.getKey().DeltaVarSet){
		    			  System.out.print("(" + varD.sd.v1.get_id() + "," + varD.sd.v2.get_id() + "); "); 
		    		  }
		    		  System.out.print("\n");
	    		  }
	    		  //if links are used
	    		  if(!entry.getKey().BVarSet.isEmpty()){
		    		  System.out.println("\t\t\t\tLinks: ");
		    		  //print out B variables
		    		  for(PpVarB varB : entry.getKey().BVarSet){
		    			  System.out.println("\t\t\t\t\t" + varB.s_vrt.get_id() + "->" + varB.t_vrt.get_id() + " : " + varB.s_f_index);
		    		  }
	    		  }
	    	  }
	      }
	      //values for variable H
	      for(Map.Entry<MpVarH, IloNumVar> entry : usedVarH.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "H_Nod" + entry.getKey().node.get_id() + " : " + master_problem.getValue(entry.getValue()) );
	    	  }
	      }
	      //values for variable X
	      for(Map.Entry<MpVarX, IloNumVar> entry : usedVarX.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "X_Node" + entry.getKey().v.get_id() + "_VNF" + entry.getKey().f_id + " : " + master_problem.getValue(entry.getValue()) );	    	 
	    	  }
	      }
	      //values for variable Y
	      for(Map.Entry<MpVarY, IloNumVar> entry : usedVarY.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "Y_SC"+ entry.getKey().tn.chain_index + "_Ind" + entry.getKey().f_id + "_Src" +  entry.getKey().tn.v1.get_id() + "_Dst" + entry.getKey().tn.v2.get_id()
	    				  + "_Ls" + entry.getKey().s_vrt.get_id() + "_Ld" + entry.getKey().t_vrt.get_id() + " : " + master_problem.getValue(entry.getValue()) );
	    	  }
	      }	      
	      System.out.println();       
	   }
	
	
	
	public static void basisCheckForVariables(IloCplex master_problem, Map<MpVarZ, IloNumVar> usedVarZ, Map<MpVarX, IloNumVar> usedVarX, Map<MpVarY, IloNumVar> usedVarY) throws IloException {
	      //values for variable Z
	      for(Map.Entry<MpVarZ, IloNumVar> entry : usedVarZ.entrySet()){
	    	  if(entry.getKey().cgConfig == 0){
		    	  NodePair ingressEgressNode = entry.getKey().completeConfig.ingeg;
		    	  System.out.print("\t\tBasis status for Z_SC"+ entry.getKey().sCiD);
		    	  System.out.print("_Ing" + ingressEgressNode.v1.get_id());
		    	  for(BaseVertex configNode : entry.getKey().completeConfig.cfg.config){
		    		  System.out.print("_Node" + configNode.get_id());
		    	  }
		    	  System.out.print("_Eg" + ingressEgressNode.v2.get_id());
		    	  System.out.println(" : " + master_problem.getBasisStatus(entry.getValue()));
	    	  }else{
//	    		  System.out.print("Basis status for Z_Src" + entry.getKey().tn.v1.get_id() + "_Dest" + entry.getKey().tn.v2.get_id() + "_SC"+ entry.getKey().tn.chain_index + "_CGConfig_" + entry.getKey().cgConfig);
	    		  System.out.println("\t\tBasis status for New Z No. " + entry.getKey().cgConfig + " : " + master_problem.getValue(entry.getValue()) + " : " + master_problem.getBasisStatus(entry.getValue()) + "\tSC" + entry.getKey().sCiD + "\tConfig " + entry.getKey().configDsc);
	    		  
	    	  }
	      }	      
	      //values for variable X
	      for(Map.Entry<MpVarX, IloNumVar> entry : usedVarX.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "\t\tBasis status for X_Node" + entry.getKey().v.get_id() + "_VNF" + entry.getKey().f_id + " : " + master_problem.getBasisStatus(entry.getValue()) );	    	 
	    	  }
	      }
	      //values for variable Y
	      for(Map.Entry<MpVarY, IloNumVar> entry : usedVarY.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "\t\tBasis status for Y_Index" + entry.getKey().f_id + "_Src" +  entry.getKey().tn.v1.get_id() + "_Dest" + entry.getKey().tn.v2.get_id() +
	    				  "_SC" + entry.getKey().tn.chain_index + "_Ls" +entry.getKey().s_vrt.get_id() + "_Ld" + entry.getKey().t_vrt.get_id() + " : " + master_problem.getBasisStatus(entry.getValue()) );
	    	  }
	      }	      
	      System.out.println();       
	   }
	
	
	public static void reducedCostperVariable (int configNum, double ppReducedCost, IloCplex master_problem, Map<MpVarZ, IloNumVar> usedVarZ, Map<MpVarX, IloNumVar> usedVarX, Map<MpVarY, IloNumVar> usedVarY) throws IloException {
	      //values for variable Z
	      for(Map.Entry<MpVarZ, IloNumVar> entry : usedVarZ.entrySet()){
	    	  //reduced cost value
	    	  double reducedCostValue = master_problem.getReducedCost(entry.getValue());
	    	  if(entry.getKey().cgConfig == 0){
		    	  NodePair ingressEgressNode = entry.getKey().completeConfig.ingeg;
		    	  System.out.print("\t\tMP Reduced cost for Z_SC"+ entry.getKey().sCiD);
		    	  System.out.print("_Ing" + ingressEgressNode.v1.get_id());
		    	  for(BaseVertex configNode : entry.getKey().completeConfig.cfg.config){
		    		  System.out.print("_Node" + configNode.get_id());
		    	  }
		    	  System.out.print("_Eg" + ingressEgressNode.v2.get_id());
		    	  System.out.println(" : " + master_problem.getReducedCost(entry.getValue()));
	    	  }else{
//	    		  System.out.print("Reduced cost for Z_Src" + entry.getKey().tn.v1.get_id() + "_Dest" + entry.getKey().tn.v2.get_id() + "_SC"+ entry.getKey().tn.chain_index + "_CGConfig_" + entry.getKey().cgConfig);
	    		  System.out.println("\t\tMP Reduced cost for New Z No. " + entry.getKey().cgConfig + " : " + master_problem.getValue(entry.getValue())  + "\tSC" + entry.getKey().sCiD + "\tConfig " + entry.getKey().configDsc + " = " + master_problem.getReducedCost(entry.getValue()) );
	    		  //check the configuration number
	    		  /*if(configNum==entry.getKey().cgConfig && master_problem.getReducedCost(entry.getValue())!=ppReducedCost){
	    			  throw new IloException("!!!!!Reduced COST DO NOT MATCH!!!!!");
	    		  }*/
	    	  }
	    	  //check if reduced cost is negative
	    	  if(reducedCostValue <= -InputConstants.RC_EPS){
	    		  //throw new IloException("Reduced Cost is negative!");
	    		  System.out.println("Reduced Cost is negative!");
	    		  System.err.println("Reduced Cost is negative!");
	    	  }
	      }	      
	      //values for variable X
	      for(Map.Entry<MpVarX, IloNumVar> entry : usedVarX.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "\t\tMP Reduced cost for X_Node" + entry.getKey().v.get_id() + "_VNF" + entry.getKey().f_id + " : " + master_problem.getReducedCost(entry.getValue()) );	    	 
	    	  }
	      }
	      //values for variable Y
	      for(Map.Entry<MpVarY, IloNumVar> entry : usedVarY.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "\t\tMP Reduced cost for Y_Index" + entry.getKey().f_id + "_Src" +  entry.getKey().tn.v1.get_id() + "_Dest" + entry.getKey().tn.v2.get_id() +
	    				  "_SC" + entry.getKey().tn.chain_index + "_Ls" +entry.getKey().s_vrt.get_id() + "_Ld" + entry.getKey().t_vrt.get_id() + " : " + master_problem.getReducedCost(entry.getValue()) );
	    	  }
	      }	      
	      System.out.println();       
	   }
	
	
	  public static void report1(IloCplex master_problem, Map<String, IloNumVar> slackVariables, Map<MpVarZ, IloNumVar> usedVarZ, Map<MpVarX, IloNumVar> usedVarX, 
			  Map<MpVarY, IloNumVar> usedVarY, Map<MpVarH,IloNumVar> usedVarH) throws IloException {
	      //total number of new configurations added by the pricing problem
		  System.out.println("\n");
	      System.out.println("######### Total number of NEW CONFIGURATIONS : " + MpVarZ.totalNewConfigs + " ##############");
	      System.out.println();
	      System.out.println("RMP LP Objective Value : " + master_problem.getObjValue() );    
	      System.out.println();	      
	      //report the slack variables
	      if(InputConstants.slackVars){
	    	  reportSlackVariables( master_problem, slackVariables);
	      }
	      //values for variable Z
	      for(Map.Entry<MpVarZ, IloNumVar> entry : usedVarZ.entrySet()){
	    	  if(master_problem.getValue(entry.getValue()) != 0.0){
	    		  System.out.println("Z No. " + entry.getKey().cgConfig + " : " + master_problem.getValue(entry.getValue()));
	    		  System.out.println("\t\t\t\tSC" + entry.getKey().sCiD);
	    		  System.out.println("\t\t\t\tConfig " + entry.getKey().configDsc);
	    		  //if (s,d) pairs are selected
	    		  if(!entry.getKey().DeltaVarSet.isEmpty()){
		    		  System.out.print("\t\t\t\t(S,D): ");
		    		  //print out D variables
		    		  for(Pp2VarDelta varD : entry.getKey().DeltaVarSet){
		    			  System.out.print("(" + varD.sd.v1.get_id() + "," + varD.sd.v2.get_id() + "); "); 
		    		  }
		    		  System.out.print("\n");
	    		  }
	    		  //if links are used
	    		  if(!entry.getKey().BVarSet.isEmpty()){
		    		  System.out.println("\t\t\t\tLinks: ");
		    		  //print out B variables
		    		  for(PpVarB varB : entry.getKey().BVarSet){
		    			  System.out.println("\t\t\t\t\t" + varB.s_vrt.get_id() + "->" + varB.t_vrt.get_id() + " : " + varB.s_f_index);
		    		  }
	    		  }	
	    	  }
	      }
	      //values for variable H
	      for(Map.Entry<MpVarH, IloNumVar> entry : usedVarH.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "H_Nod" + entry.getKey().node.get_id() + " : " + master_problem.getValue(entry.getValue()) );
	    	  }
	      }
	      //values for variable X
	      for(Map.Entry<MpVarX, IloNumVar> entry : usedVarX.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "X_Node" + entry.getKey().v.get_id() + "_VNF" + entry.getKey().f_id + " : " + master_problem.getValue(entry.getValue()) );	    	 
	    	  }
	      }
	      //values for variable Y
	      for(Map.Entry<MpVarY, IloNumVar> entry : usedVarY.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "Y_SC"+ entry.getKey().tn.chain_index + "_Ind" + entry.getKey().f_id + "_Src" +  entry.getKey().tn.v1.get_id() + "_Dst" + entry.getKey().tn.v2.get_id()
	    				  + "_Ls" + entry.getKey().s_vrt.get_id() + "_Ld" + entry.getKey().t_vrt.get_id() + " : " + master_problem.getValue(entry.getValue()) );
	    	  }
	      }	      
	      System.out.println();       
	   }
	  
	  
	  
	   public static void report2(IloCplex pricing_problem, Map<Pp2VarDelta, IloIntVar> Used_var_d_sd, Map<PpVarA, IloIntVar> Used_var_a_v_sigma, 
		Map<PpVarB, IloIntVar> Used_var_b_l_sigma_i_i1, Map<Pp2VarP, IloIntVar> Used_var_x_sd_vi, Map<Pp2VarQ, IloIntVar> Used_var_y_sd_il) throws IloException {
		      System.out.println();
		      System.out.println("\tPP ILP Objective Value ( Reduced Cost ) : " + pricing_problem.getObjValue());      
		      System.out.println();	    
		      //print out the solutions of the pricing problem
		      //print out solutions for var_d_sd
		      for( Map.Entry<Pp2VarDelta, IloIntVar> entryD : Used_var_d_sd.entrySet() ){
		       	if(pricing_problem.getValue(entryD.getValue()) != 0){
		       		System.out.println( "\tDelta_Src" + entryD.getKey().sd.v1.get_id() + "_Dst" + entryD.getKey().sd.v2.get_id() + " : " + pricing_problem.getValue(entryD.getValue()) );
		       	}
		      }
	          //print out solutions for a_v_f
	          for( Map.Entry<PpVarA, IloIntVar> entry : Used_var_a_v_sigma.entrySet() ){
	        	if(pricing_problem.getValue(entry.getValue()) != 0){
	        		System.out.println( "\tA_Node" + entry.getKey().node.get_id() + "_VnfSQ" + entry.getKey().f_seq + " : " + pricing_problem.getValue(entry.getValue()) );
	        	}
	          }
	          //print out solutions for b_s_t_i_i+1_c
	          for( Map.Entry<PpVarB, IloIntVar> entry : Used_var_b_l_sigma_i_i1.entrySet() ){
	        	 if(pricing_problem.getValue(entry.getValue()) != 0){
	        		 System.out.println( "\tB_Ls" + entry.getKey().s_vrt.get_id() + "_Ld" + entry.getKey().t_vrt.get_id() + "_vP" + entry.getKey().s_f_index + "_vN" + 
	        				 entry.getKey().t_f_index + "_SC" + entry.getKey().sc_index + " : " + pricing_problem.getValue(entry.getValue()) );	
	        	 }
	          }
	          //print out solutions for Used_var_p_sd_vi
	          for( Map.Entry<Pp2VarP, IloIntVar> entryP : Used_var_x_sd_vi.entrySet() ){
		       	if(pricing_problem.getValue(entryP.getValue()) != 0){
		       		System.out.println( "\tP_Src" + entryP.getKey().sd.v1.get_id() + "_Dst" + entryP.getKey().sd.v2.get_id() + "_Node" + entryP.getKey().nfviNode.get_id() + "_VnfSQ"
		       				+ entryP.getKey().f_seq  + " : " + pricing_problem.getValue(entryP.getValue()) );
		       	}
		      }
	          //print out solutions for Used_var_q_sd_il
	          for( Map.Entry<Pp2VarQ, IloIntVar> entryQ : Used_var_y_sd_il.entrySet() ){
		       	if(pricing_problem.getValue(entryQ.getValue()) != 0){
		       		System.out.println( "\tQ_Src" + entryQ.getKey().sd.v1.get_id() + "_Dst" + entryQ.getKey().sd.v2.get_id() + "_VnfSQ" + entryQ.getKey().f_seq 
			    			+ "_sL" + entryQ.getKey().srcL.get_id() + "_sT" +  entryQ.getKey().tarL.get_id() + " : " + pricing_problem.getValue(entryQ.getValue()) );
		       	}
			  }
	          System.out.println("\n");
		}
	  
	  
	  
}
