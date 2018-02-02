package colGen.model.mpV2AsIlp;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.Map;

import colGen.model.ver1.MpVarH;
import colGen.model.ver1.MpVarX;
import colGen.model.ver1.MpVarXc;
import colGen.model.ver1.MpVarY;
import colGen.model.ver1.MpVarZ;
import colGen.model.ver1.PpVarB;
import colGen.model.ver2.Pp2VarDelta;

public class ReportILP {
	
	public static void reportAllVariables(IloCplex master_problem, Map<MpVarZ,IloNumVar> usedVarZ, 
			 Map<MpVarX,IloNumVar> usedVarX, Map<MpVarY,IloNumVar> usedVarY, Map<MpVarH,IloNumVar> usedVarH)  throws IloException {
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
	    		  System.out.println( "X_Nod" + entry.getKey().v.get_id() + "_Vnf" + entry.getKey().f_id + " : " + master_problem.getValue(entry.getValue()) );	    	 
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
	

}
