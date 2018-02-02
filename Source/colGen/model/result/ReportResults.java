package colGen.model.result;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.Map;

import ILP.NodePair;
import ILP.TrafficNodes;
import colGen.model.ver1.IngressEgressConfigs;
import colGen.model.ver1.MpVarH;
import colGen.model.ver1.MpVarX;
import colGen.model.ver1.MpVarXc;
import colGen.model.ver1.MpVarY;
import colGen.model.ver1.MpVarZ;
import colGen.model.ver1.PpVarA;
import colGen.model.ver1.PpVarB;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;


public class ReportResults {
	
	 public static void reportSlackVariables(IloCplex master_problem, Map<String,IloNumVar> slackVariables){
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
	 
	 public static void reportSlackVarsInIlp(IloCplex master_problem, Map<String,IloNumVar> slackVariables) throws Exception {
		 System.out.println();
		 System.out.println("****Printing out the slack variables*****");
		 try{
			 for(Map.Entry<String, IloNumVar> entry : slackVariables.entrySet()){
				 if( master_problem.getValue(entry.getValue()) != 0 ){
		    		  System.out.println(entry.getKey() + " : " + master_problem.getValue(entry.getValue()) );			    		
//		    		  throw new Exception("Slack variable is Set!");
		    	 }
			 }
		 }catch(IloException exp){
			 System.out.println("Exception caught while printing slack variables");
		 }
	 }
	
	 public static void reportIntegers(IloCplex master_problem, Map<MpVarZ,IloNumVar> usedVarZ, Map<MpVarXc,IloNumVar> usedVarXc,
			 Map<MpVarX,IloNumVar> usedVarX, Map<MpVarH,IloNumVar> usedVarH) throws IloException {
	      System.out.println();
	      System.out.println("RMP ILP Objective Value : " + master_problem.getObjValue() );    
	      System.out.println();
	      //total number of new configurations added by the pricing problem
	      System.out.println("######### Total number of NEW CONFIGURATIONS : " + MpVarZ.totalNewConfigs + " ##############");
	      //values for variable Z
	      for(Map.Entry<MpVarZ, IloNumVar> entry : usedVarZ.entrySet()){
	    	  /*if(entry.getKey().cgConfig == 0){
		    	  NodePair ingressEgressNode = entry.getKey().completeConfig.ingeg;
		    	  System.out.print("Z_SC"+ entry.getKey().sCiD);
		    	  System.out.print("_Ing" + ingressEgressNode.v1.get_id());
		    	  for(BaseVertex configNode : entry.getKey().completeConfig.cfg.config){
		    		  System.out.print("_Node" + configNode.get_id());
		    	  }
		    	  System.out.print("_Eg" + ingressEgressNode.v2.get_id());
		    	  System.out.println(" : " + master_problem.getValue(entry.getValue()));
	    	  }else{*/
	    		  if( master_problem.getValue(entry.getValue()) != 0 ){
//	    		  	  System.out.print("Z_Src" + entry.getKey().tn.v1.get_id() + "_Dest" + entry.getKey().tn.v2.get_id() + "_SC"+ entry.getKey().tn.chain_index + "_CGConfig_" + entry.getKey().cgConfig);
	    			  System.out.println("New Z No. " + entry.getKey().cgConfig + " : " + master_problem.getValue(entry.getValue()) + "\tSC" + entry.getKey().sCiD + "\tConfig " + entry.getKey().configDsc);	    		 
	    		  }
//	    	  }
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
	    		  System.out.println( "X_Nod" + entry.getKey().v.get_id() + "_Vnf" + entry.getKey().f_id + " : " + master_problem.getValue(entry.getValue()) );	    	 
	    	  }
	      }
	      //values for variable Xc
	      for(Map.Entry<MpVarXc, IloNumVar> entry : usedVarXc.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "X_SC" + entry.getKey().scID + "_Nod" + entry.getKey().v.get_id() + "_VnfSQ" + entry.getKey().f_seq + " : " + master_problem.getValue(entry.getValue()) );	    	 
	    	  }
	      }	     	      
	      System.out.println();       
	   } 
	 
	 
	 public static void reportIntegers(IloCplex master_problem, Map<MpVarZ,IloNumVar> usedVarZ, Map<MpVarXc,IloNumVar> usedVarXc,
			 Map<MpVarY,IloNumVar> usedVarY, Map<MpVarX,IloNumVar> usedVarX, Map<MpVarH,IloNumVar> usedVarH) throws IloException {
	      System.out.println();
	      System.out.println("RMP ILP Objective Value : " + master_problem.getObjValue() );    
	      System.out.println();
	      //total number of new configurations added by the pricing problem
	      System.out.println("######### Total number of NEW CONFIGURATIONS : " + MpVarZ.totalNewConfigs + " ##############");
	      //values for variable Z
	      for(Map.Entry<MpVarZ, IloNumVar> entry : usedVarZ.entrySet()){
	    	  /*if(entry.getKey().cgConfig == 0){
		    	  NodePair ingressEgressNode = entry.getKey().completeConfig.ingeg;
		    	  System.out.print("Z_SC"+ entry.getKey().sCiD);
		    	  System.out.print("_Ing" + ingressEgressNode.v1.get_id());
		    	  for(BaseVertex configNode : entry.getKey().completeConfig.cfg.config){
		    		  System.out.print("_Node" + configNode.get_id());
		    	  }
		    	  System.out.print("_Eg" + ingressEgressNode.v2.get_id());
		    	  System.out.println(" : " + master_problem.getValue(entry.getValue()));
	    	  }else{*/
	    		  if( master_problem.getValue(entry.getValue()) != 0 ){
//	    		  	  System.out.print("Z_Src" + entry.getKey().tn.v1.get_id() + "_Dest" + entry.getKey().tn.v2.get_id() + "_SC"+ entry.getKey().tn.chain_index + "_CGConfig_" + entry.getKey().cgConfig);
	    			  System.out.println("New Z No. " + entry.getKey().cgConfig + " : " + master_problem.getValue(entry.getValue()) + "\tSC" + entry.getKey().sCiD + "\tConfig " + entry.getKey().configDsc);	    		 
	    		  }
//	    	  }
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
	    		  System.out.println( "X_Nod" + entry.getKey().v.get_id() + "_Vnf" + entry.getKey().f_id + " : " + master_problem.getValue(entry.getValue()) );	    	 
	    	  }
	      }
	      //values for variable Xc
	      for(Map.Entry<MpVarXc, IloNumVar> entry : usedVarXc.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "X_SC" + entry.getKey().scID + "_Nod" + entry.getKey().v.get_id() + "_VnfSQ" + entry.getKey().f_seq + " : " + master_problem.getValue(entry.getValue()) );	    	 
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
	
	public static void basisCheckForVariables(IloCplex master_problem, Map<TrafficNodes, IngressEgressConfigs> OriginalConfigs, Map<MpVarZ, IloNumVar> usedVarZ, Map<MpVarX, IloNumVar> usedVarX, Map<MpVarY, IloNumVar> usedVarY) throws IloException {
	      
	      //values for variable Z
	      for(Map.Entry<MpVarZ, IloNumVar> entry : usedVarZ.entrySet()){
	    	  if(entry.getKey().cgConfig == 0){
		    	  NodePair ingressEgressNode = entry.getKey().completeConfig.ingeg;
		    	  System.out.print("Basis status for Z_SC"+ entry.getKey().sCiD);
		    	  System.out.print("_Ing" + ingressEgressNode.v1.get_id());
		    	  for(BaseVertex configNode : entry.getKey().completeConfig.cfg.config){
		    		  System.out.print("_Node" + configNode.get_id());
		    	  }
		    	  System.out.print("_Eg" + ingressEgressNode.v2.get_id());
		    	  System.out.println(" : " + master_problem.getBasisStatus(entry.getValue()));
	    	  }else{
//	    		  System.out.print("Basis status for Z_Src" + entry.getKey().tn.v1.get_id() + "_Dest" + entry.getKey().tn.v2.get_id() + "_SC"+ entry.getKey().tn.chain_index + "_CGConfig_" + entry.getKey().cgConfig);
	    		  System.out.println("New Z No. " + entry.getKey().cgConfig + " : " + master_problem.getValue(entry.getValue()) + "\tSC" + entry.getKey().sCiD + "\tConfig " + entry.getKey().configDsc);
	    		  
	    	  }
	      }	      
	      //values for variable X
	      for(Map.Entry<MpVarX, IloNumVar> entry : usedVarX.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "Basis status for X_Node" + entry.getKey().v.get_id() + "_VNF" + entry.getKey().f_id + " : " + master_problem.getBasisStatus(entry.getValue()) );	    	 
	    	  }
	      }
	      //values for variable Y
	      for(Map.Entry<MpVarY, IloNumVar> entry : usedVarY.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "Basis status for Y_SC"+ entry.getKey().tn.chain_index + "_Ind" + entry.getKey().f_id + "_Src" +  entry.getKey().tn.v1.get_id() + "_Dst" + entry.getKey().tn.v2.get_id()
	    				  + "_Ls" + entry.getKey().s_vrt.get_id() + "_Ld" + entry.getKey().t_vrt.get_id() + " : " + master_problem.getBasisStatus(entry.getValue()) );
	    	  }
	      }	      
	      System.out.println();       
	   }
	
	
	public static void reducedCostperVariable(IloCplex master_problem, Map<TrafficNodes, IngressEgressConfigs> OriginalConfigs, Map<MpVarZ, IloNumVar> usedVarZ, Map<MpVarX, IloNumVar> usedVarX, Map<MpVarY, IloNumVar> usedVarY) throws IloException {
	     	      
	      //values for variable Z
	      for(Map.Entry<MpVarZ, IloNumVar> entry : usedVarZ.entrySet()){
	    	  if(entry.getKey().cgConfig == 0){
		    	  NodePair ingressEgressNode = entry.getKey().completeConfig.ingeg;
		    	  System.out.print("Reduced cost for Z_SC"+ entry.getKey().sCiD);
		    	  System.out.print("_Ing" + ingressEgressNode.v1.get_id());
		    	  for(BaseVertex configNode : entry.getKey().completeConfig.cfg.config){
		    		  System.out.print("_Node" + configNode.get_id());
		    	  }
		    	  System.out.print("_Eg" + ingressEgressNode.v2.get_id());
		    	  System.out.println(" : " + master_problem.getReducedCost(entry.getValue()));
	    	  }else{
//	    		  System.out.print("Reduced cost for Z_Src" + entry.getKey().tn.v1.get_id() + "_Dest" + entry.getKey().tn.v2.get_id() + "_SC"+ entry.getKey().tn.chain_index + "_CGConfig_" + entry.getKey().cgConfig);
	    		  System.out.println("New Z No. " + entry.getKey().cgConfig + " : " + master_problem.getValue(entry.getValue())  + "\tSC" + entry.getKey().sCiD + "\tConfig " + entry.getKey().configDsc);
	    		
	    	  }
	      }	      
	      //values for variable X
	      for(Map.Entry<MpVarX, IloNumVar> entry : usedVarX.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "Reduced cost for X_Node" + entry.getKey().v.get_id() + "_VNF" + entry.getKey().f_id + " : " + master_problem.getReducedCost(entry.getValue()) );	    	 
	    	  }
	      }
	      //values for variable Y
	      for(Map.Entry<MpVarY, IloNumVar> entry : usedVarY.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "Reduced cost for Y_SC"+ entry.getKey().tn.chain_index + "_Ind" + entry.getKey().f_id + "_Src" +  entry.getKey().tn.v1.get_id() + "_Dst" + entry.getKey().tn.v2.get_id()
	    				  + "_Ls" + entry.getKey().s_vrt.get_id() + "_Ld" + entry.getKey().t_vrt.get_id() + " : " + master_problem.getReducedCost(entry.getValue()) );
	    	  }
	      }	      
	      System.out.println();       
	   }  
	
	  public static void report1(IloCplex master_problem, Map<String,IloNumVar> slackVariables, Map<MpVarZ,IloNumVar> usedVarZ, Map<MpVarXc,IloNumVar> usedVarXc,
			  Map<MpVarY,IloNumVar> usedVarY, Map<MpVarX,IloNumVar> usedVarX, Map<MpVarH,IloNumVar> usedVarH) throws IloException {
	      System.out.println();
	      System.out.println("RMP LP Objective Value : " + master_problem.getObjValue() );    
	      System.out.println();	      
	      //report the slack variables
	      reportSlackVariables( master_problem, slackVariables);
	      //total number of new configurations added by the pricing problem
	      System.out.println("######### Total number of NEW CONFIGURATIONS : " + MpVarZ.totalNewConfigs + " ##############");
	      //values for variable Z
	      for(Map.Entry<MpVarZ, IloNumVar> entry : usedVarZ.entrySet()){
	    	 /* if(entry.getKey().cgConfig == 0){
		    	  NodePair ingressEgressNode = entry.getKey().completeConfig.ingeg;
		    	  System.out.print("Z_SC"+ entry.getKey().sCiD);
		    	  System.out.print("_Ing" + ingressEgressNode.v1.get_id());
		    	  for(BaseVertex configNode : entry.getKey().completeConfig.cfg.config){
		    		  System.out.print("_Node" + configNode.get_id());
		    	  }
		    	  System.out.print("_Eg" + ingressEgressNode.v2.get_id());
		    	  System.out.println(" : " + master_problem.getValue(entry.getValue()));
	    	  }else{*/
//	    		  System.out.print("Z_Src" + entry.getKey().tn.v1.get_id() + "_Dest" + entry.getKey().tn.v2.get_id() + "_SC"+ entry.getKey().tn.chain_index + "_CGConfig_" + entry.getKey().cgConfig);
	    		  System.out.println("New Z No. " + entry.getKey().cgConfig + " : " + master_problem.getValue(entry.getValue()) + "\tSC" + entry.getKey().sCiD + "\tConfig " + entry.getKey().configDsc);	    		  
//	    	  }
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
	    		  System.out.println( "X_Nod" + entry.getKey().v.get_id() + "_Vnf" + entry.getKey().f_id + " : " + master_problem.getValue(entry.getValue()) );	    	 
	    	  }
	      }
	      //values for variable Xc
	      for(Map.Entry<MpVarXc, IloNumVar> entry : usedVarXc.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		  System.out.println( "X_SC" + entry.getKey().scID + "_Nod" + entry.getKey().v.get_id() + "_VnfSQ" + entry.getKey().f_seq + " : " + master_problem.getValue(entry.getValue()) );	    	 
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
	   
	   
	   public static void report2(IloCplex pricing_problem, Map<PpVarA, IloIntVar> Used_var_a_v_sigma, Map<PpVarB, IloIntVar> Used_var_b_l_sigma_i_i1) throws IloException {
	      System.out.println();
	      System.out.println(" PP LP Objective Value ( Reduced Cost ) : " + pricing_problem.getObjValue());      
	      System.out.println();	    
	      //print out the solutions of the pricing problem
          //print out solutions for a_v_f
          for( Map.Entry<PpVarA, IloIntVar> entry : Used_var_a_v_sigma.entrySet() ){
        	if(pricing_problem.getValue(entry.getValue()) != 0){
        		System.out.println( "A_Node" + entry.getKey().node.get_id() + "_vnfSEQ" + entry.getKey().f_seq + " : " + pricing_problem.getValue(entry.getValue()) );
        	}
          }
          //print out solutions for b_s_t_i_i+1_c
          for( Map.Entry<PpVarB, IloIntVar> entry : Used_var_b_l_sigma_i_i1.entrySet() ){
        	 if(pricing_problem.getValue(entry.getValue()) != 0){
        		 System.out.println( "B_Ls" + entry.getKey().s_vrt.get_id() + "_Ld" + entry.getKey().t_vrt.get_id() + "_vP" + entry.getKey().s_f_index + "_vN" + entry.getKey().t_f_index + "_SC" + entry.getKey().sc_index + " : " + pricing_problem.getValue(entry.getValue()) );	
        	 }
          }
	   }    
	   

}
