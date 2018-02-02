package colGen.model.result;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import colGen.model.ver1.MpVarH;
import colGen.model.ver1.MpVarX;
import colGen.model.ver1.MpVarXc;
import colGen.model.ver1.MpVarY;
import colGen.model.ver1.MpVarZ;
import colGen.model.ver1.PpVarB;
import colGen.model.ver2.Pp2VarDelta;

public class Solution {
	
	public double cgRunTime; //in milliseconds
	public double ilpRunTime; //in milliseconds	
	public double totalTime; //in milliseconds
	public double eoptimality ; //in terms of percentage
	public double rmpLpValue; //in Mb
	public double rmpIlpValue; //in Mb
	
	public Set<MpVarZ> solZ;
	public Set<MpVarX> solX;
	public Set<MpVarY> solY;
	public Set<MpVarH> solH;
	
	//constructor
	public Solution(){
		this.solZ = new HashSet<MpVarZ>();
		this.solH = new HashSet<MpVarH>();
		this.solX = new HashSet<MpVarX>();
		this.solY = new HashSet<MpVarY>();
	}	
	
	
	//clear solution
	public void clearSolution(){
		this.solZ.clear();
		this.solH.clear();
		this.solX.clear();
		this.solY.clear();
	}

    //add the solution here
	public void addSolution (IloCplex master_problem, Map<MpVarZ, IloNumVar> usedVarZ, Map<MpVarH, IloNumVar> usedVarH,
			Map<MpVarX, IloNumVar> usedVarX, Map<MpVarY, IloNumVar> usedVarY) throws IloException {
		 for(Map.Entry<MpVarZ, IloNumVar> entry : usedVarZ.entrySet()){	    	
    		  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		 //add the variable
    			 this.solZ.add(entry.getKey());
    		  }
	      }
	      //values for variable H
	      for(Map.Entry<MpVarH, IloNumVar> entry : usedVarH.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		 //add the variable
	    		 this.solH.add(entry.getKey());
	    	  }
	      }
	      //values for variable X
	      for(Map.Entry<MpVarX, IloNumVar> entry : usedVarX.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		//add the variable
		        this.solX.add(entry.getKey());
	    	  }	    		 
	      }	    
	      //values for variable Y
	      for(Map.Entry<MpVarY, IloNumVar> entry : usedVarY.entrySet()){
	    	  if( master_problem.getValue(entry.getValue()) != 0 ){
	    		//add the variable
			    this.solY.add(entry.getKey());
	    	  }
	      }	
	}
	
	//print the solution
	public void printSolution(){
		  //print LP, ILP values and e-optimality percentage
	      System.out.println("E-optimality : " + this.eoptimality + "% ; ILP value : " + this.rmpIlpValue + " ; LP value : " + this.rmpLpValue);
	      //print out total, ILP and LP run times
	      System.out.println("Total time : " + this.totalTime + " ; ILP time : " + this.ilpRunTime + " ; LP time : " + this.cgRunTime);
		  //values for variable Z
	      for(MpVarZ entry : this.solZ){   	
	    	  System.out.println("Z No. " + entry.cgConfig + " : 1.0" );
    		  System.out.println("\t\t\t\tSC" + entry.sCiD);
    		  System.out.println("\t\t\t\tConfig " + entry.configDsc);
    		  //if (s,d) pairs are selected
    		  if(!entry.DeltaVarSet.isEmpty()){
	    		  System.out.print("\t\t\t\t(S,D): ");
	    		  //print out D variables
	    		  for(Pp2VarDelta varD : entry.DeltaVarSet){
	    			  System.out.print("(" + varD.sd.v1.get_id() + "," + varD.sd.v2.get_id() + "); "); 
	    		  }
	    		  System.out.print("\n");
    		  }
    		  //if links are used
    		  if(!entry.BVarSet.isEmpty()){
	    		  System.out.println("\t\t\t\tLinks: ");
	    		  //print out B variables
	    		  for(PpVarB varB : entry.BVarSet){
	    			  System.out.println("\t\t\t\t\t" + varB.s_vrt.get_id() + "->" + varB.t_vrt.get_id() + " : " + varB.s_f_index);
	    		  }
    		  }   		 
	      }
	      //values for variable H
	      for(MpVarH entry : this.solH){	    	
	    	  System.out.println( "H_Nod" + entry.node.get_id() + " : 1.0");	    	 
	      }
	      //values for variable X
	      for(MpVarX entry : this.solX){	    	
	    	  System.out.println( "X_Nod" + entry.v.get_id() + "_Vnf" + entry.f_id + " : 1.0");	    	  
	      }	   
	      //values for variable Y
	      for(MpVarY entry : this.solY){	    	 
    		  System.out.println( "Y_SC"+ entry.tn.chain_index + "_Ind" + entry.f_id + "_Src" +  entry.tn.v1.get_id() + "_Dst" + entry.tn.v2.get_id()
    				  + "_Ls" + entry.s_vrt.get_id() + "_Ld" + entry.t_vrt.get_id() + " : 1.0" );	    	  
	      }	      
	}
}
