package colGen.model.mpV2AsIlp;

import ilog.concert.IloColumn;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Given.InputConstants;
import ILP.FuncPt;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.heuristic.HuerVarZ;
import colGen.model.heuristic.BaseHeuristic2.SdDetails;
import colGen.model.ver1.MpCstr7and11;
import colGen.model.ver1.MpCstr81012and14;
import colGen.model.ver1.MpCstr9and13;
import colGen.model.ver1.MpVarH;
import colGen.model.ver1.MpVarX;
import colGen.model.ver1.MpVarY;
import colGen.model.ver1.MpVarZ;
import colGen.model.ver1.PpVarA;
import colGen.model.ver1.PpVarB;
import colGen.model.ver2.Pp2VarDelta;
import colGen.model.ver2.PricingProblem2;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class MasterProblemV2AsILP {

	//model for the master problem
	public IloCplex master_problem;
	//Objective for the master problem (44)
	public IloObjective BwUsed;
	//configuration selection constraint (3)			      
    public Map<Integer,IloRange> config_selection_constraint;	
    //function instance count (4)
    public Map<MpVarX,IloRange> function_instance_count;
    //function instance count (4)-(2)
    public Map<MpVarX,IloRange> function_instance_count2;
    //function instance constraint (5)
    public ArrayList<IloRange> function_instance_contraint;
    //VNF Location 1
    public Map<BaseVertex,IloRange> vnf_location_constraint_1;
    //VNF Location 2
    public Map<BaseVertex,IloRange> vnf_location_constraint_2;
    //VNF Location 3
    public IloRange vnf_location_constraint_3;
    //core capacity constraint	(6)		  
    public ArrayList<IloRange> core_capacity_constraint;
    //flow capacity constraint (7)
    public ArrayList<IloRange> flow_capacity_constraint;
    //latency constraint (8)
    public Map<MpCstr7and11,IloRange> flow_latency_constraint;
    //one path per c & sd constraint (9)
    public Map<MpCstr7and11,IloRange> path_per_c_sd_contraint;
    //source outgoing link constraint (10)
    public Map<MpCstr7and11,IloRange> src_outgoing_constraint;
    //source incoming link constraint (11)
    public Map<MpCstr81012and14,IloRange> src_incoming_constraint;
    //flow conservation constraint - placement - ingress node (12)
    public Map<MpCstr81012and14,IloRange> flow_place_ing_constraint;
    //flow conservation constraint - no placement - ingress node (13)
    public Map<MpCstr9and13,IloRange> flow_noplace_ing_constraint;	   
    //destination incoming link constraint (14)
    public Map<MpCstr7and11,IloRange> dest_incoming_constraint;
    //destination outgoing link constraint (15)
    public Map<MpCstr81012and14,IloRange>  dest_outgoing_constraint;
    //flow conservation constraint - placement - egress node (16)
    public Map<MpCstr81012and14,IloRange> flow_place_egr_constraint;
    //flow conservation constraint - no placement - egress node (17)
    public Map<MpCstr9and13,IloRange> flow_nplace_egr_constraint;   
    //Handles for variable z_gamma
    public Map<MpVarZ,IloNumVar> usedVarZ;	
    //Handles for variable x_vf
    public Map<MpVarX, IloNumVar> usedVarX;
    //Handles for variables h_v
    public Map<MpVarH, IloNumVar> usedVarH;
	//Handles for var_y_l_sigma_sd
	public Map<MpVarY, IloNumVar> usedVarY;
	
	//Steps to convert to ILP
	//changed all variable bounds to integer
	//removed slack variables from ILP model
	
	public MasterProblemV2AsILP(int coreCount, List<FuncPt> func_list, Map<Integer, Integer> CountMaxVNF, List<Integer> scUsed, 
			Map<Integer, ArrayList<TrafficNodes>> serviceChainTN, Map<Integer,ArrayList<HuerVarZ>> configsPerSC,  
			Map<TrafficNodes,ArrayList<HuerVarZ>> configsPerSD, Graph g, ArrayList<BaseVertex> nodesNFVI, ArrayList<BaseVertex> nfv_nodes, 
			Map<Integer, ServiceChain> ChainSet, List<TrafficNodes> pair_list, ArrayList<BaseVertex> vertex_list_without_nfvi_nodes,
			Map<Integer,Integer> replicaPerVNF, int kNodeCount) throws Exception{	
		
		//Columns for variable z_gamma			   
	    Map<MpVarZ,IloColumn> var_z_gamma;
	    //Columns for variable x_vf
	    Map<MpVarX, IloColumn> var_x_vf;
	    //Columns for variable h_v
	    Map<MpVarH, IloColumn> var_h_v;
		//Columns for var_y_l_sigma_sd
		Map<MpVarY, IloColumn> var_y_l_sigma_sd;

	 	//model for the master problem
     	this.master_problem = new IloCplex();
     	//objective for master problem
     	//trying to minimize the bandwidth used in routing the service requests
     	//(2) Objective for the master problem
     	this.BwUsed = this.master_problem.addMinimize(); 	   
     	//returns the LP Matrix associated with the master problem
//     	IloLPMatrix matrixDummyColumns = this.master_problem.addLPMatrix();
     	
     	
       	int cstrNum=0;//reset the constraint no. counter 
     	//starting constraint number
     	int cstrIndicator = InputConstants.masterProblemConstraintCount;
	    //Add the RANGED CONSTRAINTS
	 	//configuration selection constraint(3)		
	    this.config_selection_constraint = new HashMap<Integer,IloRange>();
	    for(int chainNumber=0; chainNumber < scUsed.size(); chainNumber++){
	    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
	    	IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, InputConstants.configCountPerServiceChain, constraint);	
//	    	IloRange rng = this.master_problem.addRange(2, Double.MAX_VALUE, constraint);	
	    	cstrNum++;
	    	//service chain ID
	    	int scID = scUsed.get(chainNumber);
	    	//keep track of constraint
	    	//the chain number corresponds to the service chain ID
	    	this.config_selection_constraint.put(scID, rng);
	    	//add the range to the matrix dummy column
//		    matrixDummyColumns.addRow(rng);
	    }			     
	    cstrNum=0;//reset the constraint no. counter
	    cstrIndicator++;//increase constraint number
		//function instance count (4)
		this.function_instance_count = new HashMap<MpVarX,IloRange>();
	    for(FuncPt fpt : func_list){
	    	//get the function ID
	    	int f = fpt.getid();
	    	for(BaseVertex nfviNode : nodesNFVI){
	    		//create the constraint index object
	    		MpVarX cstr = new MpVarX(cstrNum, nfviNode, f);
	    		String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
	    		IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, 0.0, constraint);
	    		cstrNum++;			    		
	    		//keep track of constraint
	    		this.function_instance_count.put(cstr, rng);
	    		//add the range to the matrix dummy column
//			    matrixDummyColumns.addRow(rng);
		    }	
		}	
	    System.out.println("Total number of (4) constraints: " + cstrNum);
	    cstrNum=0;//reset the constraint no. counter
	    cstrIndicator++;//increase constraint number
	    //function instance count (4)-(2)
		this.function_instance_count2 = new HashMap<MpVarX,IloRange>();
	    for(FuncPt fpt : func_list){
	    	//get the function ID
	    	int f = fpt.getid();
	    	for(BaseVertex nfviNode : nodesNFVI){
	    		//create the constraint index object
	    		MpVarX cstr = new MpVarX(cstrNum, nfviNode, f);
	    		String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
	    		IloRange rng = this.master_problem.addRange(0.0, Double.MAX_VALUE, constraint);
	    		cstrNum++;			    		
	    		//keep track of constraint
	    		this.function_instance_count2.put(cstr, rng);
	    		//add the range to the matrix dummy column
//			    matrixDummyColumns.addRow(rng);
		    }	
		}	
	    System.out.println("Total number of (4)-(2) constraints: " + cstrNum);
	    cstrNum=0;//reset the constraint no. counter
	    cstrIndicator++;//increase constraint number
		//function instance count (5)
	    this.function_instance_contraint = new ArrayList<IloRange>();
	    for(int fpt=0; fpt< func_list.size(); fpt++){
	    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;	    
    		IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, replicaPerVNF.get(func_list.get(fpt).getid()), constraint);
    		//keep track of constraint
	    	this.function_instance_contraint.add(rng);	    	
	    	cstrNum++;			    				    
	    }
	    //VNF location constraints
	    //location constraint 1
	    cstrNum=0;//reset the constraint no. counter
	    this.vnf_location_constraint_1 = new HashMap<BaseVertex,IloRange>();
	    for(BaseVertex nfviNode : nodesNFVI){    		 
			String constraint = "cstrVnfLocation1_" + cstrNum;
			IloRange rng = this.master_problem.addRange(0.0, Double.MAX_VALUE, constraint);
			cstrNum++;
			//keep track of constraint
			this.vnf_location_constraint_1.put(nfviNode,rng);	    	
	    }
	    System.out.println("##### Constraints for vnf location constraint 1 generated! #####");
	    //location constraint 2
	    cstrNum=0;//reset the constraint no. counter
	    this.vnf_location_constraint_2 = new HashMap<BaseVertex,IloRange>();
	    for(BaseVertex nfviNode : nodesNFVI){    		 
			String constraint = "cstrVnfLocation2_" + cstrNum;
			IloRange rng = this.master_problem.addRange(0.0, Double.MAX_VALUE, constraint);
			cstrNum++;
			//keep track of constraint    		
			this.vnf_location_constraint_2.put(nfviNode,rng);	    	
	    }	    
	    System.out.println("##### Constraints for vnf location constraint 2 generated! #####");
	    //location constraint 3
	    this.vnf_location_constraint_3 = this.master_problem.addRange(-Double.MAX_VALUE,kNodeCount,"cstrVnfLocation3");
	    System.out.println("##### Constraints for vnf location constraint 3 generated! #####");
	     
	     
	    cstrNum=0;//reset the constraint no. counter		
	    cstrIndicator++;//increase constraint number
//	    if(InputConstants.coreCstr){				   	    
		    //core capacity constraint (6)		  
		    this.core_capacity_constraint = new ArrayList<IloRange>();
		    for(int vnf_node=0; vnf_node < nfv_nodes.size(); vnf_node++){
		    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
		    	IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, coreCount, constraint);
		    	cstrNum++;
		    	//keep track of constraint
		    	this.core_capacity_constraint.add(vnf_node, rng);
		    }
//	    }
	    cstrNum=0;//reset the constraint no. counter
	    cstrIndicator++;//increase constraint number
//	    if(InputConstants.capacityCstr){				   
		    //flow capacity constraint (7)
		    this.flow_capacity_constraint = new ArrayList<IloRange>();
		    int link_num = 0;
		    for(BaseVertex s_vert : g._vertex_list){ 
		    	for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){
					String constraint = "cstr" + cstrIndicator + "_" + link_num + "_link_" + s_vert.get_id() + "_" + t_vert.get_id();
					IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, InputConstants.BANDWIDTH_PER_LAMBDA, constraint);				
					//keep track of constraint
					this.flow_capacity_constraint.add(link_num, rng);	
					//increment the link number
					link_num++;
		    	} 					
			}
//	    }
	    cstrNum = 0;//reset the constraint no. counter
	    cstrIndicator++;//increase constraint number
	    //flow latency constraint - (8)
	    this.flow_latency_constraint = new HashMap<MpCstr7and11,IloRange>();
	    //iterate through the list of service chain indices			 			    	
	    for(int sd_count=0; sd_count < pair_list.size(); sd_count++){			    	
	    	//create the traffic node
	    	TrafficNodes tn = pair_list.get(sd_count);
	    	int scID = tn.chain_index;	
	    	double latencyReq = ChainSet.get(scID).getLatReq();
	    	double totalProcTime = 0.0;
	    	double sdTraffic = tn.flow_traffic;			    	
			//iterate through the service chain //find the VNFs
			for(int vnfID : ChainSet.get(scID).chain_seq){
				for(FuncPt fp : func_list){
					if(fp.getid() == vnfID){
						totalProcTime += sdTraffic*fp.getProcDelay();						
					}
				}
			}
	    	double upperBound = latencyReq - totalProcTime;
	    	//create the constraint object
	    	MpCstr7and11 cstrLat = new MpCstr7and11(cstrNum,tn.chain_index,tn);
	    	//create the range to be considered
	    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
	    	IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE,upperBound,constraint);
	    	cstrNum++;
	    	//keep track of constraint
	    	this.flow_latency_constraint.put(cstrLat, rng);	
	    }
	    
	    
	    
	    
	    cstrNum=0;//reset the constraint no. counter
	    cstrIndicator++;//increase constraint number
	    //one path per c & (s,d) constraint (9)
	    this.path_per_c_sd_contraint = new HashMap<MpCstr7and11,IloRange>();
	    //iterate through the list of service chain indices			 			    	
	    for(int sd_count=0; sd_count < pair_list.size(); sd_count++){
	    	//create the traffic node
	    	TrafficNodes tn = pair_list.get(sd_count);
	    	//create the constraint object
	    	MpCstr7and11 cstr = new MpCstr7and11(cstrNum,tn.chain_index,tn);
	    	//create the range to be considered
	    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
	    	IloRange rng = this.master_problem.addRange(1.0, 1.0, constraint);
	    	cstrNum++;
	    	//keep track of constraint
	    	this.path_per_c_sd_contraint.put(cstr, rng);	  
	    }    
	     //ROUTE FROM SOURCE TO 1st VNF LOCATION
	     cstrNum=0;//reset the constraint no. counter
	     cstrIndicator++;//increase constraint number
	     //source outgoing link constraint (10)
	     this.src_outgoing_constraint = new HashMap<MpCstr7and11,IloRange>();
	     //iterate through the list of service chain indices			 			    	
	     for(int sd_count=0; sd_count < pair_list.size(); sd_count++){
	    	//create the traffic node
	    	TrafficNodes tn = pair_list.get(sd_count);
	    	//create the constraint object
	    	MpCstr7and11 cstr = new MpCstr7and11(cstrNum,tn.chain_index,tn);
	    	//create the range to be considered
	    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
	    	IloRange rng = this.master_problem.addRange(1.0, 1.0, constraint);
	    	cstrNum++;
	    	//keep track of constraint
	    	this.src_outgoing_constraint.put(cstr, rng);	  
	     }			     
	     cstrNum=0;//reset the constraint no. counter
	     cstrIndicator++;//increase constraint number
	     //source incoming link constraint (11)
	     this.src_incoming_constraint = new HashMap<MpCstr81012and14,IloRange>();
	     //only sd-pairs where the source node is not a NFV-capable node
	     for(TrafficNodes tn : pair_list){
	    	//add the set of NFV nodes
	    	ArrayList<BaseVertex> temp_nfvi_nodes = new ArrayList<BaseVertex>(nodesNFVI);	    	
	    	//remove the source vertex if it is a NFV node
	    	if(temp_nfvi_nodes.contains(tn.v1)){
	    		temp_nfvi_nodes.remove(tn.v1);
	    	}
	    	for(BaseVertex nfvi_node : temp_nfvi_nodes){
	    		//create the constraint
	    		MpCstr81012and14 cstr = new MpCstr81012and14(cstrNum,tn.chain_index, tn, nfvi_node); 
	    		//create the range to be considered
		    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
	    		IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, 0.0,  constraint);
	    		cstrNum++;			    		
	    		//add range to the constraint 
	    		this.src_incoming_constraint.put(cstr, rng);
		     }			    	
	     }			     
	     cstrNum=0;//reset the constraint no. counter
	     cstrIndicator++;//increase constraint number
	     //flow conservation constraint - placement - ingress node (12)
	     this.flow_place_ing_constraint = new HashMap<MpCstr81012and14,IloRange>();
	     //only sd-pairs where the source node is not a NFV-capable node
	     for(TrafficNodes tn : pair_list){
	    	//add the NFVI nodes
	    	ArrayList<BaseVertex> temp_nfvi_nodes = new ArrayList<BaseVertex>(nodesNFVI);	    	
	    	//remove the source vertex if it is a NFV node or a DC node
	    	if(temp_nfvi_nodes.contains(tn.v1)){
	    		temp_nfvi_nodes.remove(tn.v1);
	    	}
	    	for(BaseVertex nfvi_node : temp_nfvi_nodes){				    	
	    		//create the constraint
	    		MpCstr81012and14 cstr = new MpCstr81012and14(cstrNum, tn.chain_index, tn, nfvi_node); 
	    		//create the range to be considered
		    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
	    		IloRange rng = this.master_problem.addRange(0.0, 0.0, constraint);
	    		cstrNum++;
	    		//add range to the constraint 
	    		this.flow_place_ing_constraint.put(cstr, rng); 
		     }			    	
	     }			     
	     cstrNum=0;//reset the constraint no. counter
	     cstrIndicator++;//increase constraint number
	     //flow conservation constraint - no placement - ingress node (13)
	     this.flow_noplace_ing_constraint = new HashMap<MpCstr9and13,IloRange>();
	     //only sd-pairs where the source node is not a NFV-capable node
	     for(TrafficNodes tn : pair_list){
	    	ArrayList<BaseVertex> temp_non_nfvi_nodes = new ArrayList<BaseVertex>(vertex_list_without_nfvi_nodes);
	    	//remove the source vertex if in the non-NFV node list
	    	if(temp_non_nfvi_nodes.contains(tn.v1)){
	    		temp_non_nfvi_nodes.remove(tn.v1);
	    	}
	    	for(BaseVertex non_nfv_node : temp_non_nfvi_nodes){
	    		//create the constraint
	    		MpCstr9and13 cstr = new MpCstr9and13(cstrNum, tn.chain_index, tn, non_nfv_node); 			    		
	    		//create the range to be considered
		    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
	    		IloRange rng = this.master_problem.addRange(0.0, 0.0, constraint);
	    		cstrNum++;			
	    		//add range to the constraint 
	    		this.flow_noplace_ing_constraint.put(cstr, rng);			    		    		
		     }			    	
	     }
	     
	     
	     
	     //ROUTE FROM LAST VNF TO DESTINATION
	     cstrNum=0;//reset the constraint no. counter
	     cstrIndicator++;//increase constraint number
	     //destination incoming link constraint (14)
	     this.dest_incoming_constraint = new HashMap<MpCstr7and11,IloRange>();			   
	     for(int sd_count=0; sd_count < pair_list.size(); sd_count++){
		    	//create the traffic node
		    	TrafficNodes tn = pair_list.get(sd_count);
		    	//create the constraint object
		    	MpCstr7and11 cstr = new MpCstr7and11(cstrNum,tn.chain_index,tn);
		    	//create the range to be considered
		    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
	    		IloRange rng = this.master_problem.addRange(1.0, 1.0, constraint);
	    		cstrNum++;
	    		//keep track of constraint
	    		this.dest_incoming_constraint.put(cstr, rng);
		 }
	     cstrNum=0;//reset the constraint no. counter	
	     cstrIndicator++;//increase constraint number
	     //destination outgoing link constraint (15)
	     this.dest_outgoing_constraint = new HashMap<MpCstr81012and14,IloRange>();
	     for(TrafficNodes tn : pair_list){
	    	 //add the nfv nodes
	    	 ArrayList<BaseVertex> temp_nfvi_nodes = new ArrayList<BaseVertex>(nodesNFVI);	    	
	    	 //remove the destinatio node vertex if in the NFV node list
	    	 if(temp_nfvi_nodes.contains(tn.v2)){
	    		 temp_nfvi_nodes.remove(tn.v2);
	    	 }
		     for(BaseVertex nfvi_node : temp_nfvi_nodes){
		    	 //create the constraint
		    	 MpCstr81012and14 cstr = new MpCstr81012and14(cstrNum, tn.chain_index, tn, nfvi_node); 
		    	 //create the range to be considered
			     String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
		    	 IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, 0.0,  constraint);
		    	 cstrNum++;				    	
		    	 //keep track of constraint
		    	 this.dest_outgoing_constraint.put(cstr, rng);
		     }
	     }
	     cstrNum=0;//reset the constraint no. counter
	     cstrIndicator++;//increase constraint number
	     //flow conservation constraint - placement - egress node (16)
	     this.flow_place_egr_constraint = new HashMap<MpCstr81012and14,IloRange>();
	     for(TrafficNodes tn : pair_list){
	    	//add the set of NFV nodes
	    	ArrayList<BaseVertex> temp_nfvi_nodes = new ArrayList<BaseVertex>(nodesNFVI);	    	
	    	//remove the destination vertex if it is a NFV node
	    	if(temp_nfvi_nodes.contains(tn.v2)){
	    		temp_nfvi_nodes.remove(tn.v2);
	    	}
	    	for(BaseVertex nfvi_node : temp_nfvi_nodes){
	    		//create the constraint
	    		MpCstr81012and14 cstr = new MpCstr81012and14(cstrNum, tn.chain_index, tn, nfvi_node);
	    		//create the range to be considered
		    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
	    		IloRange rng = this.master_problem.addRange(0.0, 0.0, constraint);
	    		cstrNum++;			    		 
	    		//add range to the constraint 
	    		this.flow_place_egr_constraint.put(cstr, rng); 	    		
		     }			    	
		 }
	     cstrNum=0;//reset the constraint no. counter
	     cstrIndicator++;//increase constraint number
	     //flow conservation constraint - no placement - egress node (17)
	     this.flow_nplace_egr_constraint = new HashMap<MpCstr9and13,IloRange>();
	     //only sd-pairs where the source node is not a NFV-capable node
	     for(TrafficNodes tn : pair_list){
	    	ArrayList<BaseVertex> temp_non_nfvi_nodes = new ArrayList<BaseVertex>(vertex_list_without_nfvi_nodes);
	    	//remove the destination vertex if in the non-NFV node list
	    	if(temp_non_nfvi_nodes.contains(tn.v2)){
	    		temp_non_nfvi_nodes.remove(tn.v2);
	    	}
	    	for(BaseVertex non_nfv_node : temp_non_nfvi_nodes){	
	    		//create the constraint
	    		MpCstr9and13 cstr = new MpCstr9and13(cstrNum, tn.chain_index, tn, non_nfv_node); 
	    		//create the range to be considered
		    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
	    		IloRange rng = this.master_problem.addRange(0.0, 0.0, constraint);
	    		cstrNum++;			    		
	    		//add range to the constraint 
	    		this.flow_nplace_egr_constraint.put(cstr, rng); 
		     }			    	
	     }			   
	     
  
 
	     
	     
	     
	     //Add the VARIABLES
	     //Columns for variable z_gamma			   
	     var_z_gamma = new HashMap<MpVarZ,IloColumn>();
	     //Handles for variable z_gamma
	     this.usedVarZ = new HashMap<MpVarZ,IloNumVar>();
	     //including the columns for the objective - (2)
	     //iterating over the set of service chains
	     	//configurations associated with (c)
	     		//iterating over (s,d) pairs using previous configuration
	     for(int scID : scUsed){		    		
	    	    for(HuerVarZ config : configsPerSC.get(scID)){
	    	    	double coeffZObj = 0.0;
	    	    	for(Pp2VarDelta varD : config.DeltaVarSet){
	    	    		coeffZObj += varD.sd.flow_traffic*config.BVarSet.size();
	    	    	}				    		
		    		//add the column for var_z_gamma 	//coefficient for var_z_gamma is the bandwidth multiplied by the number of links used by the configuration			    	
		    		var_z_gamma.put(config, this.master_problem.column( this.BwUsed, coeffZObj));	
	//	    		System.out.println("Coefficient for VarZGamma in this.BwUsed : " + entry.getKey().flow_traffic*link_included );
	    	    }
	    }			     
	    //add the columns for 
	    //configuration selection constraint (3) 
		for(int chainNumber=0; chainNumber < scUsed.size(); chainNumber++){	
			for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){	
				 //service chain to which this chain number corresponds
				 int scID = scUsed.get(chainNumber);				
				 //check if Z has same service chain
				 if(entry.getKey().sCiD == scID){
					 //add the coefficient for the var_z_gamma variable					
					 var_z_gamma.put(entry.getKey(),entry.getValue().and(this.master_problem.column( this.config_selection_constraint.get(scID), 1)));
				 }							 
			 }
		}				
		//add the columns for 
		//function instance count (4)
		//iterating over the set of service chains
		for(Map.Entry<MpVarX, IloRange> entryCstr : this.function_instance_count.entrySet()){					
			int fID = entryCstr.getKey().f_id;//function ID				
			BaseVertex nfviNode = entryCstr.getKey().v;//NFVI node
			//iterate through the service chains //configurations associated with(c) //check if the same service chain
			for(int scID : scUsed){
				ServiceChain scTemp = ChainSet.get(scID);						
				for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){	
					HuerVarZ config = (HuerVarZ) entry.getKey();
					if(entry.getKey().sCiD == scID){
						double coeff = 0.0;							
						//iterate through the VNFs of service chain (c)
						for(int f_seq=0; f_seq < scTemp.chain_size; f_seq++ ){			    				
		    				//check if same node and same function
		    				if( (fID==scTemp.chain_seq.get(f_seq)) && config.AVarSet.contains(new PpVarA(nfviNode,f_seq))){
		    					//increment the coefficient
		    					 coeff+= 1.0;
		    				}
						}
						//add the variable to the constraint
						var_z_gamma.put(entry.getKey(), entry.getValue().and(this.master_problem.column(entryCstr.getValue(),coeff)));
					}
				}
			}
		}
		//add the columns for 
		//function instance count (4)-(2)
		//iterating over the set of service chains
		for(Map.Entry<MpVarX, IloRange> entryCstr : this.function_instance_count2.entrySet()){					
			int fID = entryCstr.getKey().f_id;//function ID				
			BaseVertex nfviNode = entryCstr.getKey().v;//NFVI node
			//iterate through the service chains //configurations associated with(c) //check if the same service chain
			for(int scID : scUsed){
				ServiceChain scTemp = ChainSet.get(scID);						
				for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){	
					HuerVarZ config = (HuerVarZ) entry.getKey();
					if(entry.getKey().sCiD == scID){
						double coeff = 0.0;							
						//iterate through the VNFs of service chain (c)
						for(int f_seq=0; f_seq < scTemp.chain_size; f_seq++ ){			    				
		    				//check if same node and same function
		    				if( (fID==scTemp.chain_seq.get(f_seq)) && config.AVarSet.contains(new PpVarA(nfviNode,f_seq))){
		    					//increment the coefficient
		    					 coeff+= 1.0;
		    				}
						}
						//add the variable to the constraint
						var_z_gamma.put(entry.getKey(), entry.getValue().and(this.master_problem.column(entryCstr.getValue(),coeff)));
					}
				}
			}
		}
		//check whether core capacity constraint can be imposed or not
		if(InputConstants.coreCstr){
		    //including the columns for the core capacity constraints - (6)
		    //iterating over the set of NFV-capable nodes
		    for(int vnf_node_index=0; vnf_node_index < nfv_nodes.size(); vnf_node_index++){	
			   BaseVertex nfvNode = nfv_nodes.get(vnf_node_index);
			   //iterating over the set of service chains
			   for(int scID : scUsed){
				     //Service Chain corresponding to service chain ID 
				     ServiceChain scTemp = ChainSet.get(scID);
				     int scSize = scTemp.chain_seq.size();
				     //iterating over the set of traffic nodes and their associated service chain (c) //check if the configuration is for the same chain
			    	 for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){
			    		HuerVarZ config = (HuerVarZ) entry.getKey();					    		 
			    		if(config.sCiD == scID){				    			
		    				double coeffCore = 0.0;		    				
		    				for(Pp2VarDelta varD : config.DeltaVarSet){				    					
		    					//iterate through the VNFs of a service chain
		    					for(int f_seq=0; f_seq<scSize; f_seq++){
		    						PpVarA varA = new PpVarA(nfvNode,f_seq);
		    						//check if varA exists in set
			   						if(config.AVarSet.contains(varA)){
		   							   //Find the core count associated with the VNF ID
		   							   Iterator<FuncPt> itr = func_list.iterator();
		   							   while(itr.hasNext()){
		   								   FuncPt fpt = itr.next();
		   								   if(fpt.getid() == scTemp.chain_seq.get(f_seq)){
		   									   coeffCore += fpt.getcore()*varD.sd.flow_traffic;
		   								   }
		   							   }
			   						}
		    					}
		    				}  				    					
		    				//add the coefficient for the var_z_gamma variable
		    				var_z_gamma.put(entry.getKey(),entry.getValue().and(this.master_problem.column(this.core_capacity_constraint.get(vnf_node_index), coeffCore)));		    				
			    		}
			    	 }	
			   }
		   }
		}
	  if(InputConstants.capacityCstr){
	      //including the columns for the flow capacity constraints - (7)
		  //iterating over all the links
		  int link_counter = 0; 
		  for(BaseVertex s_vert : g._vertex_list){ 
			for(BaseVertex t_vert : g.get_adjacent_vertices(s_vert)){						 	 
			 	 for(int scID : scUsed){
			 	   ServiceChain scTemp = ChainSet.get(scID);	 
				   for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){
					   	HuerVarZ config = (HuerVarZ) entry.getKey();
					   	if(config.sCiD == scID){
						   	 //link inclusion counter
							 int link_included = 0;	
							 for(int f_seq=0; f_seq<scTemp.chain_size-1; f_seq++){
				    			 PpVarB tempVarB = new PpVarB(scID,f_seq,f_seq+1,s_vert,t_vert);
				    			 if(config.BVarSet.contains(tempVarB)){
				    				//increment the link counter
						    		link_included++; 
				    			 }
				    		 }		    					
							 //total traffic over configuration
							 double totalTrafficOverConfig = 0.0;
						     //iterating over the set of traffic nodes using configuration
					    	 for(Pp2VarDelta varD : config.DeltaVarSet){
					    		 totalTrafficOverConfig += varD.sd.flow_traffic;		    			       				    										    					    		    
					    	 }
					    	 //add the coefficient to var_z_gamma for the link constraints
		    			     var_z_gamma.put(entry.getKey(),entry.getValue().and(this.master_problem.column(this.flow_capacity_constraint.get(link_counter),link_included*totalTrafficOverConfig)));
					   	}
					}						   	
				 }
		    	//increment the link counter				   
		    	link_counter++; 
			}				
		 }
	 }
	//latency constraint - (8)
	if(InputConstants.latencyCstr){
		for(Map.Entry<MpCstr7and11,IloRange> entryCstr : this.flow_latency_constraint.entrySet()){
			int scID = entryCstr.getKey().chainIndex;
			TrafficNodes tn = entryCstr.getKey().tn;
			//iterate through the congfigurations for (s,d)		
			for(HuerVarZ config : configsPerSD.get(tn)){
				double coeff = 0.0;
				//iterate over the links
				for(BaseVertex source : g._vertex_list){ 
					for(BaseVertex sink : g.get_adjacent_vertices(source)){
						double lProp = InputConstants.SEC_TO_MICROSEC*g.get_edge_length(source, sink)/InputConstants.SPEED_OF_LIGHT;
	    				//checking for successive dependencies
				        for(int f_seq=0; f_seq<ChainSet.get(scID).getChainSize()-1; f_seq++){
				        	PpVarB varB = new PpVarB(scID,f_seq,f_seq+1,source,sink);
				        	if(config.BVarSet.contains(varB)){
				        		coeff += lProp;	    			        		
				        	}
						}			    			        
					}
				}
				//add the coefficient to var_z_gamma for the link constraints
				IloColumn col = var_z_gamma.get(config);
		        var_z_gamma.put(config,col.and(this.master_problem.column(entryCstr.getValue(),coeff)));	
			}
		}
	}
	//one path per c & sd constraint (9)				
	for(Map.Entry<MpCstr7and11,IloRange> entryCstr : this.path_per_c_sd_contraint.entrySet()){				
	   	int scID = entryCstr.getKey().chainIndex;
	   	TrafficNodes sd = entryCstr.getKey().tn;
	   	//iterate through the configuration and find the configuration with the (s,d) pair
	   	for(HuerVarZ config : configsPerSD.get(sd)){		
		   	IloColumn col = var_z_gamma.get(config);
		   	//add the coefficient for var_z_gamma to the constraint
			var_z_gamma.put(config,col.and(this.master_problem.column(entryCstr.getValue(),1)));	
	   	}
	}  
	//source outgoing link constraint (10)				
	for(Map.Entry<MpCstr7and11,IloRange> entryCstr : this.src_outgoing_constraint.entrySet()){
  	 	//traffic node
		TrafficNodes tn = entryCstr.getKey().tn;
	   	BaseVertex srcVrt = entryCstr.getKey().tn.v1;
	   	int scID = entryCstr.getKey().tn.chain_index;	 	    	
        for(Map.Entry<MpVarZ, IloColumn> entryZ : var_z_gamma.entrySet()){	
        	HuerVarZ config = (HuerVarZ) entryZ.getKey();
        	//if variable Z has the traffic Node of the constraint
    		if(config.DeltaVarSet.contains(new Pp2VarDelta(tn))){
	        	//check if the same service chain
	        	if(entryZ.getKey().sCiD == scID){		        		
			    	//check if source node is a NFV PoP
			    	if(nodesNFVI.contains(srcVrt)){
			    		if(srcVrt.get_id() == config.firstVNF.get_id()){					   		   
				    		//add the coefficient for var_z_gamma to the constraint
				    		var_z_gamma.put(entryZ.getKey(),entryZ.getValue().and(this.master_problem.column(entryCstr.getValue(),1)));
			    		}
			     	}
	        	}
    		}
        }			   
    }
	//source incoming link constraint (11)			
    for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : this.src_incoming_constraint.entrySet()){
    	//the NFVI node in the constraint
    	BaseVertex nfviNode = entryCstr.getKey().nfvi_node;
    	//traffic node in constraint
    	TrafficNodes tn = entryCstr.getKey().tn;
    	//iterate through Z variables
    	for(Map.Entry<MpVarZ, IloColumn> entryZ : var_z_gamma.entrySet()){
    		HuerVarZ config = (HuerVarZ) entryZ.getKey();	
    		//the NFVI node in the variable Z
    		BaseVertex nfviZ = config.firstVNF;
    		//if variable Z has the traffic Node of the constraint
    		if(config.DeltaVarSet.contains(new Pp2VarDelta(tn))){
	    		//check if same service chain
		    	if(entryCstr.getKey().chainIndex == entryZ.getKey().sCiD){
		    		//check if same NFVI node
		    		if(nfviNode.get_id() == nfviZ.get_id()){
		    			//add the coefficient for var_z_gamma to the constraint
			    		var_z_gamma.put(entryZ.getKey(),entryZ.getValue().and(this.master_problem.column(entryCstr.getValue(),1)));
		    		}			    	
			    }
    		}
    	}
    }
    //flow conservation constraint - placement - ingress node (12)		   
    for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : this.flow_place_ing_constraint.entrySet()){
    	//the NFVI node in the constraint
    	BaseVertex nfviNode = entryCstr.getKey().nfvi_node;
    	//traffic node in constraint
    	TrafficNodes tn = entryCstr.getKey().tn;
    	//iterate through Z variables
    	for(Map.Entry<MpVarZ, IloColumn> entryZ : var_z_gamma.entrySet()){
    		HuerVarZ config = (HuerVarZ) entryZ.getKey();
    		//the NFVI node in the variable Z
    		BaseVertex nfviZ = config.firstVNF;
    		//if variable Z has the traffic Node of the constraint
    		if(config.DeltaVarSet.contains(new Pp2VarDelta(tn))){
	    		//check if same service chain
		    	if(entryCstr.getKey().chainIndex == entryZ.getKey().sCiD){
		    		//check if same NFVI node
		    		if(nfviNode.get_id() == nfviZ.get_id()){
		    			//add the coefficient for var_z_gamma to the constraint
			    		var_z_gamma.put(entryZ.getKey(),entryZ.getValue().and(this.master_problem.column(entryCstr.getValue(),1)));
		    		}			    	
			    }
    		}
    	}
    }
    //destination incoming link constraint (14)	  	 
    for(Map.Entry<MpCstr7and11,IloRange> entryCstr : this.dest_incoming_constraint.entrySet()){
  	 	//traffic node
	   	BaseVertex dstVrt = entryCstr.getKey().tn.v2;
	   	int scID = entryCstr.getKey().tn.chain_index;
	   	//traffic node in constraint
    	TrafficNodes tn = entryCstr.getKey().tn;
        for(Map.Entry<MpVarZ, IloColumn> entryZ : var_z_gamma.entrySet()){	
        	HuerVarZ config = (HuerVarZ) entryZ.getKey();
        	//if variable Z has the traffic Node of the constraint
    		if(config.DeltaVarSet.contains(new Pp2VarDelta(tn))){
	        	//check if the same service chain
	        	if(entryZ.getKey().sCiD == scID){		        		
			    	//check if destination node is a NFV PoP
			    	if(nodesNFVI.contains(dstVrt)){
			    		if(dstVrt.get_id() == config.lastVNF.get_id()){					   		   
				    		//add the coefficient for var_z_gamma to the constraint
				    		var_z_gamma.put(entryZ.getKey(),entryZ.getValue().and(this.master_problem.column(entryCstr.getValue(),1)));
			    		}
			     	}
	        	}
    		}
        }	
    }
    //destination outgoing link constraint (15)  		
    for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : this.dest_outgoing_constraint.entrySet()){
    	//the NFVI node in the constraint
    	BaseVertex nfviNode = entryCstr.getKey().nfvi_node;
    	//traffic node in constraint
    	TrafficNodes tn = entryCstr.getKey().tn;
    	//iterate through Z variables
    	for(Map.Entry<MpVarZ, IloColumn> entryZ : var_z_gamma.entrySet()){
    		HuerVarZ config = (HuerVarZ) entryZ.getKey();
    		//the NFVI node in the variable Z
    		BaseVertex nfviZ =  config.lastVNF;
    		//if variable Z has the traffic Node of the constraint
    		if(config.DeltaVarSet.contains(new Pp2VarDelta(tn))){
	    		//check if same service chain
		    	if(entryCstr.getKey().chainIndex == entryZ.getKey().sCiD){
		    		//check if same NFVI node
		    		if(nfviNode.get_id() == nfviZ.get_id()){
		    			//add the coefficient for var_z_gamma to the constraint
			    		var_z_gamma.put(entryZ.getKey(),entryZ.getValue().and(this.master_problem.column(entryCstr.getValue(),1)));
		    		}			    	
			    }
    		}
    	}				 
    }
    //flow conservation constraint - placement - egress node (16)
    for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : this.flow_place_egr_constraint.entrySet()){
    	//the NFVI node in the constraint
    	BaseVertex nfviNode = entryCstr.getKey().nfvi_node;
    	//traffic node in constraint
    	TrafficNodes tn = entryCstr.getKey().tn;
    	//iterate through Z variables
    	for(Map.Entry<MpVarZ, IloColumn> entryZ : var_z_gamma.entrySet()){
    		HuerVarZ config = (HuerVarZ) entryZ.getKey();
    		//the Egress node in the variable Z
    		BaseVertex nfviZ = config.lastVNF;
    		//if variable Z has the traffic Node of the constraint
    		if(config.DeltaVarSet.contains(new Pp2VarDelta(tn))){
	    		//check if same service chain
		    	if(entryCstr.getKey().chainIndex == entryZ.getKey().sCiD){
		    		//check if same NFVI node
		    		if(nfviNode.get_id() == nfviZ.get_id()){
		    			//add the coefficient for var_z_gamma to the constraint
			    		var_z_gamma.put(entryZ.getKey(),entryZ.getValue().and(this.master_problem.column(entryCstr.getValue(),1)));
		    		}			    	
			    }
    		}
    	}	 
	}
	//include the range for var_z_gamma variables
	//add the variable to the set of variables
    //this.configurationCounter = 0;
    for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){
    	String zName = "Z_SC"+ entry.getKey().sCiD + entry.getKey().configDsc + "_" + entry.getKey().cgConfig; 
//    	System.out.println(entry.getKey().configDsc);
//	    this.usedVarZ.put(entry.getKey(), this.master_problem.numVar(entry.getValue(), 0.0, Double.MAX_VALUE, zName));
    	//for certain z variables set the upper bound to 1    	
    /*	if( entry.getKey().configDsc.equals("_1_3_3") && (entry.getKey().DeltaVarSet.size()==1) ){
    		System.out.println("check passed!");
    		for(Pp2VarDelta varD : entry.getKey().DeltaVarSet){
    			if(varD.sd.v1.get_id()==1){ 
    				//set lower bound equal to upper bound    				
    				this.usedVarZ.put(entry.getKey(), this.master_problem.intVar(entry.getValue(), 0, 1, zName));
    				System.out.println(zName + " is set to 1");
    			}
    		}
    	}else if( entry.getKey().configDsc.equals("_5_4_4") && (entry.getKey().DeltaVarSet.size()==1) ){
    		for(Pp2VarDelta varD : entry.getKey().DeltaVarSet){
    			if(varD.sd.v1.get_id()==5){
    				//set lower bound equal to upper bound    				
    				this.usedVarZ.put(entry.getKey(), this.master_problem.intVar(entry.getValue(), 0, 1, zName));
    				System.out.println(zName + " is set to 1");
    			}
    		}
    	}else{*/
	    	//since model is to be run as an ILP
	    	this.usedVarZ.put(entry.getKey(), this.master_problem.intVar(entry.getValue(), 0, 1, zName));
//    	}
     }  
     System.out.println("##### Z variables added! #####");
	   
	     
     
     
	     
	   //Columns for variable x_vf
	   var_x_vf = new HashMap<MpVarX, IloColumn>();
	   //Handles for variable x_vf
	   this.usedVarX = new HashMap<MpVarX, IloNumVar>();
	   //function instance count (4)
	   for(Map.Entry<MpVarX, IloRange> entryCstr : this.function_instance_count.entrySet()){ 
		   int vnfID = entryCstr.getKey().f_id;
		   //create the key //the object MpVarX
		   MpVarX temp_elem = new MpVarX(entryCstr.getKey().v, entryCstr.getKey().f_id) ;
		   //add the column to the constraint
		   IloColumn col = this.master_problem.column(entryCstr.getValue(), -1.0*CountMaxVNF.get(vnfID));
		   //populate the HashMap
		   var_x_vf.put(temp_elem, col);				  
	   }
	   //function instance count (4)-(2)
	   for(Map.Entry<MpVarX, IloRange> entryCstr : this.function_instance_count2.entrySet()){  			
		   //create the key //the object MpVarX
		   MpVarX temp_elem = new MpVarX(entryCstr.getKey().v, entryCstr.getKey().f_id) ;
		   //add the column to the constraint
		   IloColumn col =  var_x_vf.get(temp_elem);
		   col = col.and(this.master_problem.column(entryCstr.getValue(), -1.0));
		   //populate the HashMap
		   var_x_vf.put(temp_elem, col);				  
	   }
	   //function count limit (5)  	
	   for(int fpt=0; fpt<func_list.size(); fpt++){
		   for(BaseVertex nfvi_node : nodesNFVI){
			   FuncPt tempDetails = func_list.get(fpt);
			   MpVarX tmpVar = new MpVarX(nfvi_node, tempDetails.getid());
			   IloColumn col =  var_x_vf.get(tmpVar);
			   col = col.and(this.master_problem.column(this.function_instance_contraint.get(fpt), 1.0));
			   //populate the HashMap
	  		   var_x_vf.put(tmpVar, col);
		   }
	   }
	   //VNF location constraint - (1)
	   for(BaseVertex node : nodesNFVI){
		   for(FuncPt fpt : func_list){
			   //create the key //the object MpVarX
			   MpVarX varX = new MpVarX(node,fpt.getid());
			   //get the column
			   IloColumn col = var_x_vf.get(varX);
			   //add the column to the constraint
			   col = col.and(this.master_problem.column(this.vnf_location_constraint_1.get(node),-1.0));
			   //populate the HashMap
			   var_x_vf.put(varX,col);
		   }	   
	   }
	   //VNF location constraint - (2)
	   for(BaseVertex node : nodesNFVI){
		   for(FuncPt fpt : func_list){
			   //create the key //the object MpVarX
			   MpVarX varX = new MpVarX(node,fpt.getid());
			   //get the column
			   IloColumn col = var_x_vf.get(varX);
			   //add the column to the constraint
			   col = col.and(this.master_problem.column(this.vnf_location_constraint_2.get(node),1.0));
			   //populate the HashMap
			   var_x_vf.put(varX,col);
		   }
	   }
	  //add the variable
	  //include the range for var_x_vf variables
	  for(Map.Entry<MpVarX, IloColumn> entry : var_x_vf.entrySet()){
	   	String xName = "X_Nod" + entry.getKey().v.get_id() + "_VNF" + entry.getKey().f_id;
	   	//this.usedVarX.put(entry.getKey(),this.master_problem.numVar(entry.getValue(), 0.0, 1.0, xName));
	   	//since model is to be run as an ILP
	   	this.usedVarX.put(entry.getKey(),this.master_problem.intVar(entry.getValue(), 0, 1, xName));
	  }
	  System.out.println("##### X variables added! #####");
	  
	  
	  
	  
	  
	  
   
	   //add the H variables
	   var_h_v = new HashMap<MpVarH, IloColumn>();
	   //Handles for variable x_vf  
	   this.usedVarH = new HashMap<MpVarH,IloNumVar>();
	   //VNF location constraint - (1)
	   for(BaseVertex node : nodesNFVI){	
		   //create the key //the object MpVarH
		   MpVarH varH = new MpVarH(node);	
		   //add column to constraint
		   //Big M here is the number of functions
		   IloColumn col = this.master_problem.column(this.vnf_location_constraint_1.get(node), func_list.size());
		   //populate the HashMap
		   var_h_v.put(varH,col);   	   
	   }
	   //VNF location constraint - (2)
	   for(BaseVertex node : nodesNFVI){
//		   System.out.println("Node: " + node.get_id() + " = " + this.vnf_location_constraint_2.get(node).toString());
		   //create the key //the object MpVarH
		   MpVarH var = new MpVarH(node);
//		   System.out.println("Node: " + node.get_id() + " = " + this.vnf_location_constraint_2.get(node).toString());
		   //get the column
		   IloColumn col = var_h_v.get(var);
		   /*System.out.println("Column: " + col.toString());
		   System.out.println("Node: "+ node.get_id());
		   System.out.println("Node: " + node.get_id() + " = " + this.vnf_location_constraint_2.get(node).toString());*/
		   //add the column to the constraint
		   col = col.and(this.master_problem.column(this.vnf_location_constraint_2.get(node),-1.0));
		   //populate the HashMap
		   var_h_v.put(var,col);	   
	   }
	   //VNF location constraint - (3)
	   for(BaseVertex node : nodesNFVI){	 
		   //create the key //the object MpVarH
		   MpVarH var = new MpVarH(node);	
		   //get the column
		   IloColumn col = var_h_v.get(var);
		   //add the column to the constraint
		   col = col.and(this.master_problem.column(this.vnf_location_constraint_3,1.0));
		   //populate the HashMap
		   var_h_v.put(var,col);	   
	   }   
	   //add the variable
	   //include the range for var_x_vf variables
	   for(Map.Entry<MpVarH, IloColumn> entry : var_h_v.entrySet()){
	  	 String hName = "H_Nod" + entry.getKey().node.get_id();
	  	 this.usedVarH.put(entry.getKey(),this.master_problem.intVar(entry.getValue(), 0, 1, hName));
	   }
	   System.out.println("##### H variables added! #####");
   
	   
	   
	   
	   
	   
	   
   
		//Columns for var_y_l_sigma_sd
		var_y_l_sigma_sd = new HashMap<MpVarY, IloColumn>();
		//Handles for var_y_l_sigma_sd
		this.usedVarY = new HashMap<MpVarY, IloNumVar>();
		//including the columns for the objective - (2)
		//iterating over the set of service chains
		for(int scID : scUsed){
	   //iterating over the set of traffic nodes and their associated service chain (c)
			for(TrafficNodes tn : serviceChainTN.get(scID)){	    	
				//function ID for first function in the service chain
				int firstVNF = 0;
				//function ID for last function in the service chain
				int lastVNF = ChainSet.get(tn.chain_index).chain_size-1;
				for(BaseVertex s_vert : g._vertex_list){ 
						for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){	
//	   						if( source_to_ingress.contains(s_vert) && source_to_ingress.contains(t_vert) && ( source_to_ingress.indexOf(t_vert) > source_to_ingress.indexOf(s_vert) ) ){
								//add the column for var_y_l_sigma1_sd
		   						MpVarY temp_elem_1 = new MpVarY(tn, firstVNF, s_vert, t_vert);
		   						//the column
		   						IloColumn col1 = this.master_problem.column( this.BwUsed, tn.flow_traffic);
		   		   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
		   						var_y_l_sigma_sd.put(temp_elem_1, col1);
//		   	   					if(this.var_y_l_sigma_sd.get(temp_elem_1) == null){
//		   							System.out.println("Ingress node column is empty");
//		   						}
//	   						}
//	   						if( egress_to_destination.contains(s_vert) && egress_to_destination.contains(t_vert) && ( egress_to_destination.indexOf(t_vert) > egress_to_destination.indexOf(s_vert) ) ){
	  		   		        //add the column for var_y_l_sigmaN_sd
	  						MpVarY temp_elem_N = new MpVarY(tn, lastVNF, s_vert, t_vert);
	  						//the column
	  						IloColumn colN = this.master_problem.column( this.BwUsed, tn.flow_traffic);
	  		   				//coefficient for var_y_l_sigmaN_sd is the bandwidth 
	  						var_y_l_sigma_sd.put(temp_elem_N, colN);   						
//		   						if(this.var_y_l_sigma_sd.get(temp_elem_N) == null){
//		   							System.out.println("Egress node column is empty");
//		   						}
//	   						}
					 	} 
				}   				
			}
		}
		//check if var_y_l_sigma_sd is empty
//		if(this.var_y_l_sigma_sd.isEmpty()){
//			System.out.println("No variables added to Y_l_sigma_sd");
//		}
//		//print out the columns in the var_y_l_sigma_sd
//		for(Map.Entry<MpVarY, IloColumn> entry : this.var_y_l_sigma_sd.entrySet()){	    		
//			//print out key
//			System.out.println("SD pair: (" + entry.getKey().tn.v1.get_id() + ", "+ entry.getKey().tn.v2.get_id() + "); Link: (" + entry.getKey().s_vrt.get_id() + ", "+ entry.getKey().t_vrt.get_id() + "); VNF: " + entry.getKey().f_id);
//			//print out value
//			if(entry.getValue() != null){
//				System.out.println("Column has been created");
//			}
//		}
//		System.out.println("*************");
//		System.out.println("*************");
		if(InputConstants.capacityCstr){
			//flow capacity constraint (7)	    	
			//declare the counter
			int lnum = 0;	
		    for(BaseVertex s_vert : g._vertex_list){ 
				for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){									
				    //iterating over the set of traffic nodes and their associated service chain (c)
					for(int scID : scUsed){	
						//iterate over the traffic nodes that use that traffic chain
						for(TrafficNodes tn : serviceChainTN.get(scID)){			   						   		
				    		//function ID for first function in the service chain
				    		int firstVNF = 0;
				    		//function ID for last function in the service chain
				    		int lastVNF = ChainSet.get(tn.chain_index).chain_size-1; 
			    			//add the column for var_y_l_sigma1_sd
							MpVarY temp_elem_1 = new MpVarY(tn, firstVNF, s_vert, t_vert);
							//get the column object
							IloColumn col1 = var_y_l_sigma_sd.get(temp_elem_1);
						    //check if key exists
				    	    if(col1 != null){
		  						//modify the column object //add coefficient
		  						col1 = col1.and(this.master_problem.column( this.flow_capacity_constraint.get(lnum), tn.flow_traffic));
		  		   				//update the hashmap
		  						var_y_l_sigma_sd.put(temp_elem_1, col1);
				    	    }else{
	// 					    	    	System.out.println("Y_firstVNF variable not added in link constraints");
				    	    } 								
			   		        //add the column for var_y_l_sigmaN_sd
							MpVarY temp_elem_N = new MpVarY(tn, lastVNF, s_vert, t_vert);
						    //get the column object
							IloColumn colN = var_y_l_sigma_sd.get(temp_elem_N);
							//check if key exists
							if(colN != null){
		  						//modify the column object //add coefficient
		  						colN = colN.and(this.master_problem.column( this.flow_capacity_constraint.get(lnum), tn.flow_traffic));
		  					    //update the hashmap
		  						var_y_l_sigma_sd.put(temp_elem_N, colN);
							}else{
	// 									System.out.println("Y_lastVNF variable not added in link constraints");
							} 						    
				    	}
					}
					//increment the link_num
					lnum++;
				} 
			 }
		}
   //latency constraint - (8)
	   if(InputConstants.latencyCstr){
		   for(Map.Entry<MpCstr7and11,IloRange> entryCstr : this.flow_latency_constraint.entrySet()){
			   int scID = entryCstr.getKey().chainIndex;
			   TrafficNodes tn = entryCstr.getKey().tn;
			   for(BaseVertex source : g._vertex_list){ 
				   for(BaseVertex sink :  g.get_adjacent_vertices(source)){
					   double lProp = InputConstants.SEC_TO_MICROSEC*g.get_edge_length(source, sink)/InputConstants.SPEED_OF_LIGHT;
					   //function ID for first function in the service chain
			    	   int firstVNF = 0;
			    	   //function ID for last function in the service chain
			    	   int lastVNF = ChainSet.get(scID).chain_size-1;
			    	   //add the column for var_y_l_sigma1_sd
						MpVarY temp_elem_1 = new MpVarY(tn, firstVNF, source, sink);
						//get the column object
						IloColumn col1 = var_y_l_sigma_sd.get(temp_elem_1);
					    //check if key exists
			    	    if(col1 != null){
	  						//modify the column object //add coefficient
	  						col1 = col1.and(this.master_problem.column( entryCstr.getValue(), lProp));
	  		   				//update the hashmap
	  						var_y_l_sigma_sd.put(temp_elem_1, col1);
			    	    }else{
	//			    	    	System.out.println("Y_firstVNF variable not added in link constraints");
			    	    } 								
		   		        //add the column for var_y_l_sigmaN_sd
						MpVarY temp_elem_N = new MpVarY(tn, lastVNF, source, sink);
					    //get the column object
						IloColumn colN = var_y_l_sigma_sd.get(temp_elem_N);
						//check if key exists
						if(colN != null){
	  						//modify the column object //add coefficient
	  						colN = colN.and(this.master_problem.column( entryCstr.getValue(), lProp));
	  					    //update the hashmap
	  						var_y_l_sigma_sd.put(temp_elem_N, colN);
						}else{
	//							System.out.println("Y_lastVNF variable not added in link constraints");
						}
				   }
			   }
		   } 	
	   }
		//source outgoing link constraint - (10)	
		for(int scID : scUsed){
		    for(Map.Entry<MpCstr7and11,IloRange> entry : this.src_outgoing_constraint.entrySet()){	   	
		        //traffic nodes
		    	TrafficNodes tn = entry.getKey().tn;
		        //for all the traffic nodes using that service chain
		    	if(tn.chain_index == scID){
			    	//function ID for first function in the service chain
			    	int firstVNF = 0;			    
	  				//iterating over all the links   				
					for(BaseVertex t_vert :  g.get_adjacent_vertices(tn.v1)){
					    //add the column for var_y_l_sigma1_sd
						MpVarY temp_elem_1 = new MpVarY(tn, firstVNF, tn.v1, t_vert);					
						//add the column for var_y_l_sigma1_sd
						IloColumn col = var_y_l_sigma_sd.get(temp_elem_1);
					    //check if key exists
			    	    if(col != null){
	  		   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
	  		   				col = col.and(this.master_problem.column( entry.getValue(), 1));		   		   		        	
	  		   				//update the HashMap
	  		   			    var_y_l_sigma_sd.put(temp_elem_1, col);
			    	    }
			    	    else{
			    	    	//print out key
//				    		System.out.println("SD pair: (" + tn.v1.get_id() + ", "+ tn.v2.get_id() + "); Link: (" + tn.v1.get_id() + ", "+ t_vert.get_id() + "); VNF: " + f_id_1);
//			    	    	System.out.println("the column was empty : ");
			    	    }
					}
		    	}
		    }
		}
	   //source incoming link constraint - (11)
	   for(int scID : scUsed){	    	
		    for(Map.Entry<MpCstr81012and14,IloRange> entry : this.src_incoming_constraint.entrySet()){
		        if(entry.getKey().chainIndex == scID){			    	
			    	 TrafficNodes tn = entry.getKey().tn;
		    		 int firstVNF = 0;
		    		 BaseVertex nfvi_node = entry.getKey().nfvi_node;		    		
		    		 //incoming links
			    	 for(BaseVertex s_vrt : g.get_precedent_vertices(nfvi_node)){
		    		    //add the column for var_y_l_sigma1_sd
						MpVarY temp_elem = new MpVarY(tn, firstVNF, s_vrt, nfvi_node);	
						//add the column for var_y_l_sigma1_sd
						IloColumn col = var_y_l_sigma_sd.get(temp_elem);
						//check if key exists
				    	if(col != null){
			   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
			   				col = col.and(this.master_problem.column(entry.getValue(), -1));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);
				    	}
			    	 }
			    }			     
		    }
		}
	   //flow conservation constraint - placement - ingress node (12)	
	   for(int scID : scUsed){    	
		    for(Map.Entry<MpCstr81012and14,IloRange> entry : this.flow_place_ing_constraint.entrySet()){	
		         if(entry.getKey().chainIndex == scID){ 		    
			    	 //SD pair
	  			     TrafficNodes tn = entry.getKey().tn; // the traffic nodes
			    	 //function identifier for the first function in the service chain
			    	 int firstVNF = 0;
			    	 BaseVertex nfvi_node = entry.getKey().nfvi_node;			    
		    		 //incoming links
			    	 for(BaseVertex s_vrt : g.get_precedent_vertices(nfvi_node)){
		    		    //add the column for var_y_l_sigma1_sd
						MpVarY temp_elem = new MpVarY(tn, firstVNF, s_vrt, nfvi_node);	
						//add the column for var_y_l_sigma1_sd
						IloColumn col = var_y_l_sigma_sd.get(temp_elem);
						//check if key exists
				    	if(col != null){
			   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
			   				col = col.and(this.master_problem.column(entry.getValue(), -1));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);
//			   			    System.out.println("#####Column created for Const 5##### : Incoming links");
//			   			    System.out.println("Link : ( " + s_vrt.get_id() + " , " + nfv_node.get_id() + " )");
				    	}
			    	 }
			    	 //outgoing links
			    	 for(BaseVertex t_vrt : g.get_adjacent_vertices(nfvi_node)){
			    		//add the column for var_y_l_sigma1_sd
							MpVarY temp_elem = new MpVarY(tn, firstVNF, nfvi_node, t_vrt);
							//add the column for var_y_l_sigma1_sd
							IloColumn col = var_y_l_sigma_sd.get(temp_elem);
							//check if key exists
					    	if(col != null){
				   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
				   				col = col.and(this.master_problem.column(entry.getValue(), 1));		   		   		        	
				   				//update the HashMap
				   			    var_y_l_sigma_sd.put(temp_elem, col);
//				   			    System.out.println("#####Column created for Const 5##### : Outgoing links");
//				   			    System.out.println("Link : ( " + nfv_node.get_id() + " , " + t_vrt.get_id() + " )");
					    	}				    	
			    	 }
			    }
			     
		    }
		}	    	
	   //flow conservation constraint - no placement - ingress node (13)
	   for(int scID : scUsed){	    	
			for(Map.Entry<MpCstr9and13,IloRange> entry : this.flow_noplace_ing_constraint.entrySet()){	
				if(entry.getKey().chainIndex == scID){		    	
			    	TrafficNodes tn = entry.getKey().tn; //traffic nodes
			    	//function ID of the first function in the service chain
			    	int firstVNF = 0;
			    	BaseVertex non_nfvi_vrt = entry.getKey().node;			    	 
		    	 	//iterate through the list of non-NFV nodes
	   		 		//outgoing links	    		
		    		for(BaseVertex t_vrt : g.get_adjacent_vertices(non_nfvi_vrt)){
		    			//add the column for var_y_l_sigma1_sd
						MpVarY temp_elem = new MpVarY(tn, firstVNF, non_nfvi_vrt, t_vrt);
					    //add the column for var_y_l_sigma1_sd
						IloColumn col = var_y_l_sigma_sd.get(temp_elem);
						//check if key exists
				    	if(col != null){
			   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
			   				col = col.and(this.master_problem.column(entry.getValue(), 1));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);
				    	}
		    		}	    	
		    		//incoming links	    		 
		    		for(BaseVertex s_vrt : g.get_precedent_vertices(non_nfvi_vrt)){
		    			//add the column for var_y_l_sigma1_sd
						MpVarY temp_elem = new MpVarY(tn, firstVNF, s_vrt, non_nfvi_vrt);
						//add the column for var_y_l_sigma1_sd
						IloColumn col = var_y_l_sigma_sd.get(temp_elem);
						//check if key exists
				    	if(col != null){
			   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
			   				col = col.and(this.master_problem.column(entry.getValue(), -1));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);
				    	}
		    		}
				} 				     
			}
		} 		  	    	
	   //destination incoming link constraint (14)	
	   for(int scID : scUsed){    	
		    for(Map.Entry<MpCstr7and11,IloRange> entry : this.dest_incoming_constraint.entrySet()){
		    	if(entry.getKey().chainIndex == scID){			    	
			    	 TrafficNodes tn = entry.getKey().tn;
			    	 int lastVNF = ChainSet.get(tn.chain_index).chain_size-1;				    	    		 
		    		 //outgoing links
			    	 for(BaseVertex s_vrt : g.get_precedent_vertices(tn.v2)){
			    		//add the column for var_y_l_sigma1_sd
							MpVarY temp_elem = new MpVarY(tn, lastVNF, s_vrt, tn.v2);
							//add the column for var_y_l_sigma1_sd
							IloColumn col = var_y_l_sigma_sd.get(temp_elem);
							//check if key exists
					    	if(col != null){
				   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
				   				col = col.and(this.master_problem.column(entry.getValue(), 1));		   		   		        	
				   				//update the HashMap
				   			    var_y_l_sigma_sd.put(temp_elem, col);
					    	}
			    	 }
			    }				     			    	 
		    }
	   }
	  //destination outgoing link constraint (15)
	  for(int scID : scUsed){ 
		     for(Map.Entry<MpCstr81012and14,IloRange> entry : this.dest_outgoing_constraint.entrySet()){
		     	if(entry.getKey().chainIndex == scID){
		    	 	TrafficNodes tn = entry.getKey().tn;
			    	//function ID of the first function in the service chain
			    	int lastVNF = ChainSet.get(tn.chain_index).chain_size-1; 
			    	//node at which the VNF has been placed
			    	BaseVertex nfviNode = entry.getKey().nfvi_node;
				   	//iterating over the outgoing links from the last vnf placment
			    	//these links will take you to the destination
					for(BaseVertex t_vert :  g.get_adjacent_vertices(entry.getKey().nfvi_node)){
					    //add the column for var_y_l_sigma1_sd
						MpVarY temp_elem_N = new MpVarY(tn, lastVNF, nfviNode, t_vert);	   					
						//add the column for var_y_l_sigma1_sd
						IloColumn col = var_y_l_sigma_sd.get(temp_elem_N);
					    //check if key exists
			    	    if(col != null){
	  		   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
	  		   				col = col.and(this.master_problem.column(entry.getValue(), -1));		   		   		        	
	  		   				//update the HashMap
	  		   			    var_y_l_sigma_sd.put(temp_elem_N, col);   		   			    
//	   		   			    	System.out.println("#####Column created for Const 11##### : Incoming links");
//	   		   			    	System.out.println("Link : ( " + s_vert.get_id() + " , " + tn.v2.get_id() + " )");   		   			    
			    	    }		   					
					}

			    }
		    }
		}
	   //flow conservation constraint - placement - egress node - outgoing links (16)	 
	   for(int scID : scUsed){   
		    for(Map.Entry<MpCstr81012and14,IloRange> entry : this.flow_place_egr_constraint.entrySet()){
		    	if(entry.getKey().chainIndex == scID){		    	 
			    	 //SD pair
	  			     TrafficNodes tn = entry.getKey().tn; // the traffic nodes
	  			     //function ID for first function in the service chain
	  			     int lastVNF = ChainSet.get(tn.chain_index).chain_size-1;	
			    	 BaseVertex nfvi_node = entry.getKey().nfvi_node;		    	 
		    		 //incoming links
			    	 for(BaseVertex s_vrt : g.get_precedent_vertices(nfvi_node)){
		    		    //add the column for var_y_l_sigma1_sd
						MpVarY temp_elem = new MpVarY(tn, lastVNF, s_vrt, nfvi_node);	
						//add the column for var_y_l_sigma1_sd
						IloColumn col = var_y_l_sigma_sd.get(temp_elem);
						//check if key exists
				    	if(col != null){
			   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
			   				col = col.and(this.master_problem.column(entry.getValue(), 1));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);
//			   			    System.out.println("#####Column created for Const 9##### : Incoming links");
//			   			    System.out.println("Link : ( " + s_vrt.get_id() + " , " + nfv_node.get_id() + " )");
				    	}
			    	 }
			    	 //outgoing links
			    	 for(BaseVertex t_vrt : g.get_adjacent_vertices(nfvi_node)){
			    		//add the column for var_y_l_sigma1_sd
						MpVarY temp_elem = new MpVarY(tn, lastVNF, nfvi_node, t_vrt);
						//add the column for var_y_l_sigma1_sd
						IloColumn col = var_y_l_sigma_sd.get(temp_elem);
						//check if key exists
				    	if(col != null){
			   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
			   				col = col.and(this.master_problem.column(entry.getValue(), -1));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);
//			   			    System.out.println("#####Column created for Const 9##### : Outgoing links");
//			   			    System.out.println("Link : ( " + nfv_node.get_id() + " , " + t_vrt.get_id() + " )");
				    	}
			    	 }		
			    }	     
		    }
	   }	    	
	   //flow conservation constraint - no placement - egress node (17)
	   for(int scID : scUsed){ 	    	
		    for(Map.Entry<MpCstr9and13,IloRange> entry : this.flow_nplace_egr_constraint.entrySet()){
		    	if(entry.getKey().chainIndex == scID){			    	
			    	 TrafficNodes tn = entry.getKey().tn; //traffic nodes
			    	 //function ID of the first function in the service chain
			    	 int lastVNF = ChainSet.get(tn.chain_index).chain_size-1;	
			    	 BaseVertex non_nfvi_vrt = entry.getKey().node;
			    	 //iterate through the list of non-NFV nodes			    	
		    		 //outgoing links	    		
		    		 for(BaseVertex t_vrt : g.get_adjacent_vertices(non_nfvi_vrt)){
		    			//add the column for var_y_l_sigma1_sd
						MpVarY temp_elem = new MpVarY(tn, lastVNF, non_nfvi_vrt, t_vrt);
					    //add the column for var_y_l_sigma1_sd
						IloColumn col = var_y_l_sigma_sd.get(temp_elem);
						//check if key exists
				    	if(col != null){
			   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
			   				col = col.and(this.master_problem.column(entry.getValue(), 1));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);
//			   			    System.out.println("#####Column created for Const 10##### : Outgoing links");
//			   			    System.out.println("Link : ( " + non_nfv_vrt.get_id() + " , " + t_vrt.get_id() + " )");
				    	}
		    		 }	    	  
		    		 //incoming links	    		
		    		 for(BaseVertex s_vrt : g.get_precedent_vertices(non_nfvi_vrt)){
		    			//add the column for var_y_l_sigma1_sd
						MpVarY temp_elem = new MpVarY(tn, lastVNF, s_vrt, non_nfvi_vrt);
						//add the column for var_y_l_sigma1_sd
						IloColumn col = var_y_l_sigma_sd.get(temp_elem);
						//check if key exists
				    	if(col != null){
			   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
			   				col = col.and(this.master_problem.column(entry.getValue(), -1));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);  				   			    
//	   			    	System.out.println("#####Column created for Const 17#### : Incoming links");
//	   			    	System.out.println("Link : ( " + s_vrt.get_id() + " , " + non_nfvi_vrt.get_id() + " )");  				   			    
				    	}
		    		 }
				 }				     
		    }
	   }  		    
	    //add the variable
		//include the range for var_y_l_sigma_sd variables
		for(Map.Entry<MpVarY, IloColumn> entry : var_y_l_sigma_sd.entrySet()){
			String yName = "Y_SC"+ entry.getKey().tn.chain_index + "_Ind" + entry.getKey().f_id + "_Src" +  entry.getKey().tn.v1.get_id() + "_Dst" + entry.getKey().tn.v2.get_id() + "_Ls" + entry.getKey().s_vrt.get_id() + "_Ld" + entry.getKey().t_vrt.get_id();
//			this.usedVarY.put(entry.getKey(), this.master_problem.numVar(entry.getValue(),0.0,1.0,yName));
		   	//since model is to be run as an ILP
			this.usedVarY.put(entry.getKey(), this.master_problem.intVar(entry.getValue(),0,1,yName));
			
		}
		 System.out.println("##### Y variables added! #####");
	}
	
}
