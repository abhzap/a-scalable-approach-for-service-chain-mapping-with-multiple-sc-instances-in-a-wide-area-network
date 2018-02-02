package colGen.model.ver2;

import ilog.concert.IloColumn;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.ver1.MpCstr7and11;
import colGen.model.ver1.MpCstr81012and14;
import colGen.model.ver1.MpCstr9and13;
import colGen.model.ver1.MpVarX;
import colGen.model.ver1.MpVarY;
import colGen.model.ver1.MpVarZ;
import colGen.model.ver1.NewIngEgConfigs;
import colGen.model.ver1.PpVarB;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class MasterProblem2 {

	    //model for the master problem
		public IloCplex master_problem;
		//Objective for the master problem (44)
		public IloObjective BwUsed;
		//configuration selection constraint (3)			      
	    public Map<Integer,IloRange> config_selection_constraint;	
	    //function instance count (4)
	    public Map<MpVarX,IloRange> function_instance_count;
	    //function instance constraint (5)
	    public ArrayList<IloRange> function_instance_contraint;
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
	    //store the slack variables
	    public Map<String,IloNumVar> slackVariables;
	    //Columns for variable z_gamma			   
	    public Map<MpVarZ,IloColumn> var_z_gamma;
	    //Handles for variable z_gamma
	    public Map<MpVarZ,IloNumVar> usedVarZ;
		//Columns for variable x_vf
	    public Map<MpVarX, IloColumn> var_x_vf;
	    //Handles for variable x_vf
	    public Map<MpVarX, IloNumVar> usedVarX;
		//Columns for var_y_l_sigma_sd
		public Map<MpVarY, IloColumn> var_y_l_sigma_sd;
		//Handles for var_y_l_sigma_sd
		public Map<MpVarY, IloNumVar> usedVarY;
	    //for the moment this seems redundant
	    //configuration constraints 
        //public ArrayList<IloRange> place_config_constraint;
		
		//create the Master Problem Object
//		public MasterProblem2(int coreCount, List<FuncPt> func_list, Map<Integer, Integer> funcCountMax, List<Integer> scUsed, 
//				Map<Integer, ArrayList<TrafficNodes>> serviceChainTN, Map<Integer, ArrayList<NewIngEgConfigs>> configPerServiceChain,
//				Map<TrafficNodes, ArrayList<NewIngEgConfigs>> OriginalConfigs, Graph g, ArrayList<BaseVertex> nodesNFVI, ArrayList<BaseVertex> nfv_nodes, 
//				Map<Integer, ServiceChain> ChainSet, List<TrafficNodes> pair_list, ArrayList<BaseVertex> vertex_list_without_nfvi_nodes,
//				ArrayList<PricingProblem2> pPList) throws Exception{
		public MasterProblem2(int coreCount, List<FuncPt> func_list, Map<Integer, Integer> funcCountMax, List<Integer> scUsed, 
				Map<Integer, ArrayList<TrafficNodes>> serviceChainTN, Map<Integer, ArrayList<NewIngEgConfigs>> configPerServiceChain,
				Map<TrafficNodes, ArrayList<NewIngEgConfigs>> OriginalConfigs, Graph g, ArrayList<BaseVertex> nodesNFVI, ArrayList<BaseVertex> nfv_nodes, 
				Map<Integer, ServiceChain> ChainSet, List<TrafficNodes> pair_list, ArrayList<BaseVertex> vertex_list_without_nfvi_nodes,
				ArrayList<LazyPricingProblem2> pPList) throws Exception{
			
			 	//model for the master problem
		     	this.master_problem = new IloCplex();
		     	//objective for master problem
		     	//trying to minimize the bandwidth used in routing the service requests
		     	//(2) Objective for the master problem
		     	this.BwUsed = this.master_problem.addMinimize(); 	   
		     	//returns the LP Matrix associated with the master problem
		     	IloLPMatrix matrixDummyColumns = this.master_problem.addLPMatrix();
		     	
		     	
            
		     	int cstrNum=0;//reset the constraint no. counter 
		     	//starting constraint number
		     	int cstrIndicator = InputConstants.masterProblemConstraintCount;
			    //Add the RANGED CONSTRAINTS
			 	//configuration selection constraint(3)		
			    this.config_selection_constraint = new HashMap<Integer,IloRange>();
			    for(int chainNumber=0; chainNumber < scUsed.size(); chainNumber++){
			    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
			    	IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, InputConstants.configCountPerServiceChain, constraint);	
			    	cstrNum++;
			    	//service chain ID
			    	int scID = scUsed.get(chainNumber);
			    	//keep track of constraint
			    	//the chain number corresponds to the service chain ID
			    	this.config_selection_constraint.put(scID, rng);
			    	//add the range to the matrix dummy column
				    matrixDummyColumns.addRow(rng);
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
					    matrixDummyColumns.addRow(rng);
				    }	
				}	
			    System.out.println("Total number of (4) constraints: " + cstrNum);
			    cstrNum=0;//reset the constraint no. counter
			    cstrIndicator++;//increase constraint number
				//function instance count (5)
			    this.function_instance_contraint = new ArrayList<IloRange>();
			    for(int fpt=0; fpt< func_list.size(); fpt++){
			    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
			    	/*if(func_list.get(fpt).getid() == 3){
			    		IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, 1.0, constraint);
			    		//keep track of constraint
				    	this.function_instance_contraint.add(rng);
			    	}else{*/
			    		IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, funcCountMax.get(func_list.get(fpt).getid()), constraint);
			    		//keep track of constraint
				    	this.function_instance_contraint.add(rng);
			    	//}
			    	cstrNum++;
			    				    
			    }
			    cstrNum=0;//reset the constraint no. counter		
			    cstrIndicator++;//increase constraint number
//			    if(InputConstants.coreCstr){				   	    
				    //core capacity constraint (6)		  
				    this.core_capacity_constraint = new ArrayList<IloRange>();
				    for(int vnf_node=0; vnf_node < nfv_nodes.size(); vnf_node++){
				    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
				    	IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, coreCount, constraint);
				    	cstrNum++;
				    	//keep track of constraint
				    	this.core_capacity_constraint.add(vnf_node, rng);
				    	//add dummy variable if core capacity constraint is imposed
				    	if(InputConstants.coreCstr){
					    	//add the range to the matrix dummy column
					    	matrixDummyColumns.addRow(rng);
				    	}
				    }
//			    }
			    cstrNum=0;//reset the constraint no. counter
			    cstrIndicator++;//increase constraint number
//			    if(InputConstants.capacityCstr){				   
				    //flow capacity constraint (7)
				    this.flow_capacity_constraint = new ArrayList<IloRange>();
				    int link_num = 0;
				    for(BaseVertex s_vert : g._vertex_list){ 
				    	for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){
							String constraint = "cstr" + cstrIndicator + "_" + link_num + "_link_" + s_vert.get_id() + "_" + t_vert.get_id();
							IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, InputConstants.BANDWIDTH_PER_LAMBDA, constraint);				
							//keep track of constraint
							this.flow_capacity_constraint.add(link_num, rng);
							//add dummy variable if the capacity constraint is enforced
							if(InputConstants.capacityCstr){
								//add the range to the matrix dummy column
						    	matrixDummyColumns.addRow(rng);
							}
							//increment the link number
							link_num++;
				    	} 					
					}
//			    }
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
			    	//add the range to the matrix dummy column
				    matrixDummyColumns.addRow(rng);
			    }
			    
			    cstrNum=0;//reset the constraint no. counter
			    cstrIndicator++;//increase constraint number
			    //one path per c & sd constraint (9)
			    this.path_per_c_sd_contraint = new  HashMap<MpCstr7and11,IloRange>();
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
			    	//add the range to the matrix dummy column
				    matrixDummyColumns.addRow(rng);
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
			    	//add the range to the matrix dummy column
				    matrixDummyColumns.addRow(rng);
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
			    		//add the range to the matrix dummy column
					    matrixDummyColumns.addRow(rng);
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
			    		//add the range to the matrix dummy column
					    matrixDummyColumns.addRow(rng);
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
			    		//add the range to the matrix dummy column
					    matrixDummyColumns.addRow(rng);
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
				    	 //add the range to the matrix dummy column
						 matrixDummyColumns.addRow(rng);
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
			    		//add the range to the matrix dummy column
						matrixDummyColumns.addRow(rng);
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
			     	     
			     System.out.println("#### Number of Rows before adding diagonal matrix : " + this.master_problem.getNrows() + " #####");
			     System.out.println("#### Number of Columns before adding diagonal matrix : " + this.master_problem.getNcols() + " #####");
			     System.out.println("#### Number of Non-zero's before adding diagonal matrix : " + this.master_problem.getNNZs() + " #####");
			     
			     if(InputConstants.slackVars){
			         //initialize the linear expression
				     IloLinearNumExpr test = this.master_problem.linearNumExpr();
				     //store the slack variables
				     this.slackVariables =  new HashMap<String,IloNumVar>();
				     //get the number of rows
				     int numberOfRows = matrixDummyColumns.getNrows();	
				     //penalty to be applied to slack variables
				     double coeffSV = InputConstants.Big_M;
				     //adding the columns and the corresponding non-zero's to the dummy matrix
				     for(int rowIndex=0; rowIndex < numberOfRows; rowIndex++){	
				    	     String slackVar = "SV"+rowIndex;
				    	     //create the temporary variable 
				    	 	 IloNumVar tempVar = this.master_problem.numVar(0.0, 1.0, slackVar);
//				    	     IloNumVar tempVar = this.master_problem.numVar(0.0, Double.MAX_VALUE, slackVar);
				    	 	 //add the variable to the set of master problem variables
				    	     this.slackVariables.put(slackVar, tempVar);
				    	     //add the variable to the linear expression			    	     
				    	     test.addTerm(tempVar, coeffSV);
				    	 	 //add the temp variable to the matrix
				    		 matrixDummyColumns.addColumn(tempVar, new int[]{rowIndex}, new double[]{1.0}); 	    	    
				     }
				     //add the linear expression to the Objective
				     this.BwUsed.setExpr(test);
			     }
			     System.out.println("#### Number of Rows after adding diagonal matrix : " + this.master_problem.getNrows() + " #####");
			     System.out.println("#### Number of Columns after adding diagonal matrix : " + this.master_problem.getNcols() + " #####");
			     System.out.println("#### Number of Non-zero's after adding diagonal matrix : " + this.master_problem.getNNZs() + " #####");
			     
			     
			     
			     
			     
			     
			     //Add the VARIABLES
			     //Columns for variable z_gamma			   
			     this.var_z_gamma = new HashMap<MpVarZ,IloColumn>();
			     //Handles for variable z_gamma
			     this.usedVarZ = new HashMap<MpVarZ,IloNumVar>();
			     //including the columns for the objective - (44)
			     //iterating over the set of service chains
			     for(int scID : scUsed){
				    	 //get Delta variables
		  		    	 Set<Pp2VarDelta> DeltaVarSet = new HashSet<Pp2VarDelta>(); 
		  		    	 //get the B variables
					     Set<PpVarB> BVarSet = new HashSet<PpVarB>();
		  		    	 //get pricing problem and corresponding 
		  		    	 //set of delta variables, B variables
//		  		    	 for(PricingProblem2 pPObject : pPList){
					     for(LazyPricingProblem2 pPObject : pPList){
		  		    		 if(pPObject.scIDpP == scID){
		  		    			 DeltaVarSet.addAll(pPObject.Used_var_d_sd.keySet());
		  		    			 BVarSet.addAll(pPObject.Used_var_b_l_sigma_i_i1.keySet());
		  		    		 }
		  		    	 }
			    		//configurations associated with (c)
			    	    for(NewIngEgConfigs CONFIG_SC : configPerServiceChain.get(scID)){
			    	    	double coeffCONFIG_SC = 0.0;
			    	    	//get the complete configuration
		    				ArrayList<BaseVertex> PLACE_CONFIG = new ArrayList<BaseVertex>();
		    				PLACE_CONFIG.add(CONFIG_SC.ingeg.v1);
		    				PLACE_CONFIG.addAll(CONFIG_SC.cfg.config);
		    				PLACE_CONFIG.add(CONFIG_SC.ingeg.v2);
		    				String zCONFIG="";
		    				for(BaseVertex vrt : PLACE_CONFIG){
		    					zCONFIG = zCONFIG + vrt.get_id();
		    				}    				
		    				//iterating over the set of traffic nodes and their associated service chain (c)
						    for(TrafficNodes tn : serviceChainTN.get(scID)){ 	
						    	for(NewIngEgConfigs config_sc : OriginalConfigs.get(tn)){					    					
							    				//get the complete configuration
							    				ArrayList<BaseVertex> place_config = new ArrayList<BaseVertex>();
							    				place_config.add(config_sc.ingeg.v1);
							    				place_config.addAll(config_sc.cfg.config);
							    				place_config.add(config_sc.ingeg.v2);
							    				String zConfig ="";
							    				for(BaseVertex vrt : place_config){
							    					zConfig = zConfig + vrt.get_id();
							    				}
							    				//System.out.println(zCONFIG + " <=> " + zConfig);
							    				//check if the configurations are the same
							    				if(zConfig.equals(zCONFIG)){
							    					//System.out.println("\tMATCH!");
								    				//get the shortest path for the configuration
								    				Path config_sc_path = config_sc.cfg.config_routes.get(0);
								    				/*System.out.println("##########SD pair: (" + tn.v1.get_id() + ", "+ tn.v2.get_id() + ")############");
								    				System.out.println("Ingress Node : " + config_sc.ingeg.v1.get_id() + " Egress Node : " + config_sc.ingeg.v2.get_id());				
									 				System.out.print("Configuration : ");
									 				for(BaseVertex vrt : config_sc.cfg.config){
									 					System.out.print(vrt.get_id() + " ");
									 				}
									 				System.out.println();		 				
								 					System.out.print("Path : ");
								 					for(BaseVertex vrt : config_sc_path.get_vertices()){
								 						System.out.print(vrt.get_id() + "->");
								 					}
								 					System.out.println();*/
								    				//link inclusion counter  
								    				int link_included = 0; 
								    				//iterating over all the links
								    				for(BaseVertex s_vert : g._vertex_list){ 
								    					for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){
								    					  //checking for successive dependencies
								    					  for(int chain_seq_pos = 0; chain_seq_pos < ChainSet.get(scID).getChainSize() - 1; chain_seq_pos++){		
								    						  BaseVertex i_VNF_node = place_config.get(chain_seq_pos);//NFV node that holds the ith VNF
								    						  BaseVertex i1_VNF_node = place_config.get(chain_seq_pos+1);//NFV node that holds the i+1th VNF
								    						  //get the sequence of links between the i'th and i+1'th VNF in the path
								    						  int index_i_VNF_in_path = config_sc_path.get_vertices().indexOf(i_VNF_node);
								    						  int index_i1_VNF_in_path = config_sc_path.get_vertices().indexOf(i1_VNF_node);
								    						  if(index_i1_VNF_in_path >= index_i_VNF_in_path){
								    							  //no sublist will be created when consecutive VNF's are placed on the same VNF node
									    						  List<BaseVertex> link_sequence = config_sc_path.get_vertices().subList( index_i_VNF_in_path, index_i1_VNF_in_path+1 );
									    						  PpVarB keyVarB = new PpVarB(scID,chain_seq_pos,chain_seq_pos+1,s_vert,t_vert);
									    						  if( link_sequence.contains(s_vert) && link_sequence.contains(t_vert) && ( link_sequence.indexOf(t_vert) > link_sequence.indexOf(s_vert) ) ){
									    							  link_included++;
							//		    							  System.out.println("Links between the " + chain_seq_pos + "th VNF at " + i_VNF_node.get_id() + " node and " + (chain_seq_pos+1) + "th VNF at " + i1_VNF_node.get_id() + " node : ");
							//		    							  System.out.println("*********** Link ( " + s_vert.get_id() + " , " + t_vert.get_id() + " ) has been included ***************");		
									    						  }else{
									    							  //remove the B object from set
									    							  //since the link is not included
									    							  BVarSet.remove(keyVarB);
							//				    					  System.out.println("Link ( " + s_vert.get_id() + " , " + t_vert.get_id() + " ) not included");				    							  
									    						  }
								    						  }
								    					  }
								    				 	} 
								    				}
								    				//add the coefficient to the configuration
								    				coeffCONFIG_SC += tn.flow_traffic*link_included;
								    				//System.out.println("\tcoeffCONFIG_SC = " + coeffCONFIG_SC);
							    				}
						    		}			    		
						    }
				    		//create the variable
				    		MpVarZ temp_elem = new MpVarZ(scID, CONFIG_SC, DeltaVarSet, BVarSet);
				    		//add the column for var_z_gamma
				    		//coefficient for var_z_gamma is the bandwidth multiplied by the number of links used by the configuration 
				    		this.var_z_gamma.put(temp_elem, this.master_problem.column( this.BwUsed, coeffCONFIG_SC ));	
			//	    		System.out.println("Coefficient for VarZGamma in this.BwUsed : " + entry.getKey().flow_traffic*link_included );
			    	    }
			    }			     
			    //add the columns for 
			    //configuration selection constraint (3) 
				for(int chainNumber=0; chainNumber < scUsed.size(); chainNumber++){	
					for(Map.Entry<MpVarZ, IloColumn> entry : this.var_z_gamma.entrySet()){	
						 //service chain to which this chain number corresponds
						 int scID = scUsed.get(chainNumber);				
						 //check if Z has same service chain
						 if(entry.getKey().sCiD == scID){
							 //add the coefficient for the var_z_gamma variable					
							 this.var_z_gamma.put(entry.getKey(),entry.getValue().and(this.master_problem.column( this.config_selection_constraint.get(scID), 1)));
						 }							 
					 }
				}				
				//add the columns for 
				//function instance count (4)
				//iterating over the set of service chains
				for(Map.Entry<MpVarX, IloRange> entryCstr : this.function_instance_count.entrySet()){					
					int fID = entryCstr.getKey().f_id; //function ID				
					BaseVertex vrt = entryCstr.getKey().v; 	//NFVI node
					//iterate through the service chains
					for(int scID : scUsed){
						ServiceChain scTemp = ChainSet.get(scID);
						//configurations associated with (c)
						for(Map.Entry<MpVarZ, IloColumn> entry : this.var_z_gamma.entrySet()){
							//check if the same service chain 
							if(entry.getKey().sCiD == scID){
								double COEFF_CONFIG_SC = 0.0;
								//get the complete configuration
			    				ArrayList<BaseVertex> PLACE_CONFIG = new ArrayList<BaseVertex>();
			    				PLACE_CONFIG.add(entry.getKey().completeConfig.ingeg.v1);
			    				PLACE_CONFIG.addAll(entry.getKey().completeConfig.cfg.config);
			    				PLACE_CONFIG.add(entry.getKey().completeConfig.ingeg.v2);
			    				for(BaseVertex tempVrt : PLACE_CONFIG){
			    					System.out.print(tempVrt.get_id() + ",");
			    				}
			    				System.out.println();
								//iterate through the VNFs of service chain (c)
								for(int i=0; i < scTemp.chain_size; i++ ){
									int fidTemp = scTemp.chain_seq.get(i);							
				    				//vertex that hosts the first VNF
				    				BaseVertex vrtTemp = PLACE_CONFIG.get(i);
				    				//check if same node and same function
				    				if(fID == fidTemp && vrt.get_id() == vrtTemp.get_id()){
				    					//increment the coefficient
				    					COEFF_CONFIG_SC += 1.0;
				    				}
								}
								//add the variable to the constraint
								this.var_z_gamma.put(entry.getKey(), entry.getValue().and(this.master_problem.column( entryCstr.getValue(),COEFF_CONFIG_SC)));
							}
						}
					}
				}
				//check whether core capacity constraint can be imposed or not
				if(InputConstants.coreCstr){
				    //including the columns for the core capacity constraints - (5)
				    //iterating over the set of NFV-capable nodes
				    for(int vnf_node_index=0; vnf_node_index < nfv_nodes.size(); vnf_node_index++){	
					   BaseVertex nfvNode = nfv_nodes.get(vnf_node_index);
					   //iterating over the set of service chains
					   for(int scID : scUsed){
						     //Service Chain corresponding to service chain ID 
						     ServiceChain scTemp = ChainSet.get(scID);
						     int scSize = scTemp.chain_seq.size();
						     //iterating over the set of traffic nodes and their associated service chain (c)
					    	 for(Map.Entry<MpVarZ, IloColumn> entry : this.var_z_gamma.entrySet()){
					    		//check if the configuration is for the same chain 
					    		if(entry.getKey().sCiD == scID){ 
				    				//coefficient to be added
				    				double coeff_core_capacity = 0.0;		    			
				    				//to find the ingress-egress nodes			    			
				    				NodePair tempIngeg = entry.getKey().completeConfig.ingeg;
				    				//get the complete configuration
				    				ArrayList<BaseVertex> place_config = new ArrayList<BaseVertex>();
				    				place_config.add(tempIngeg.v1);
				    				place_config.addAll(entry.getKey().completeConfig.cfg.config);
				    				place_config.add(tempIngeg.v2);
				    				//check if scSize is 1
				    				if(scSize == 1){
				    					//remove the egress node
				    					place_config.remove(1);
				    				}
	//				    			System.out.print("Configuration : ( ");
	//				    			//print out the placeconfig array
	//				    			for(BaseVertex vrt : place_config){
	//				    				System.out.print(vrt.get_id() + ", ");
	//				    			}
	//				    			System.out.println(" )");			    				
				    				//all VNFs required in a service chain have already been placed    				
				   					//check if the particular node exists in the configuration
				   					//check if vnf_node is part of the configuration
				    				//iterating through the traffic nodes
				    				//using a service chain 'c'
				    				for(TrafficNodes tnSC : serviceChainTN.get(scID)){
				    					//iterate through the VNFs of a service chain
				    					for(int i=0; i<scTemp.chain_seq.size(); i++){
				    						int config_fID = scTemp.chain_seq.get(i);
				    						BaseVertex configNode = place_config.get(i);
				    						//check if placement node is same as the NFV node
					   						if(configNode.get_id() == nfvNode.get_id()){
				   							   //Find the core count associated with the VNF ID
				   							   Iterator<FuncPt> itr = func_list.iterator();
				   							   while(itr.hasNext()){
				   								   FuncPt fpt = itr.next();
				   								   if(fpt.getid() == config_fID){
				   									   coeff_core_capacity += fpt.getcore()*tnSC.flow_traffic;
				   								   }
				   							   }
					   						}
				    					}
				    				}  				    					
				    				//add the coefficient for the var_z_gamma variable
				    				this.var_z_gamma.put(entry.getKey(),entry.getValue().and(this.master_problem.column(this.core_capacity_constraint.get(vnf_node_index), coeff_core_capacity )));		    				
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
						 //link inclusion counter
						 int link_included = 0;			 
					 	 for(int scID : scUsed){
						   for(Map.Entry<MpVarZ, IloColumn> entry : this.var_z_gamma.entrySet()){
								 //get the complete configuration
				 				 ArrayList<BaseVertex> PLACE_CONFIG = new ArrayList<BaseVertex>();
				 				 PLACE_CONFIG.add(entry.getKey().completeConfig.ingeg.v1);
				 				 PLACE_CONFIG.addAll(entry.getKey().completeConfig.cfg.config);
				 				 PLACE_CONFIG.add(entry.getKey().completeConfig.ingeg.v2);
				 				 //string for comparison
				 				 String zCONFIG="";
				 				 for(BaseVertex vrt : PLACE_CONFIG){
			    					zCONFIG = zCONFIG + vrt.get_id();
				 				 }
								 //iterate over the service chains
							     //iterating over the set of traffic nodes and their associated service chain (c)
						    	 for(TrafficNodes tn : serviceChainTN.get(scID)){
						    		 for(NewIngEgConfigs config_sc : OriginalConfigs.get(tn)){
					    				//to find the ingress-egress nodes
					    				NodePair tempIngeg = config_sc.ingeg;				    				
					    				//get the complete configuration
					    				ArrayList<BaseVertex> place_config = new ArrayList<BaseVertex>();
					    				place_config.add(tempIngeg.v1);
					    				place_config.addAll(config_sc.cfg.config);
					    				place_config.add(tempIngeg.v2);
					    				String zConfig ="";
					    				for(BaseVertex vrt : place_config){
					    					zConfig = zConfig + vrt.get_id();
					    				}
					    				//check if the configurations are the same
					    				if(zConfig.equals(zCONFIG)){
						    				//get the shortest path for the configuration
						    				Path config_sc_path =  config_sc.cfg.config_routes.get(0);
						    				//checking for successive dependencies
					    			        for(int chain_seq_pos = 0; chain_seq_pos < ChainSet.get(scID).getChainSize() - 1; chain_seq_pos++){
					    			        	 BaseVertex i_VNF_node = place_config.get(chain_seq_pos);//NFV node that holds the ith VNF
					    						 BaseVertex i1_VNF_node = place_config.get(chain_seq_pos+1);//NFV node that holds the i+1th VNF
					    			        	 //get the sequence of links between the i'th and i+1'th VNF in the path
					    			        	 int index_i_VNF_in_path = config_sc_path.get_vertices().indexOf(i_VNF_node);
					    						 int index_i1_VNF_in_path = config_sc_path.get_vertices().indexOf(i1_VNF_node);
					    						 if(index_i1_VNF_in_path >= index_i_VNF_in_path){
						    						  List<BaseVertex> link_sequence = config_sc_path.get_vertices().subList( index_i_VNF_in_path, index_i1_VNF_in_path+1 );
						    						  if( link_sequence.contains(s_vert) && link_sequence.contains(t_vert) && ( link_sequence.indexOf(t_vert) > link_sequence.indexOf(s_vert)) ){
						    							  link_included++;
						    						  }
					    						 }
					    					}
					    			        //add the coefficient to var_z_gamma for the link constraints
					    			        this.var_z_gamma.put(entry.getKey(),entry.getValue().and(this.master_problem.column(this.flow_capacity_constraint.get(link_counter),link_included*tn.flow_traffic)));
					    				}
						    		 }			    		    
						    	}
							}
						 }
				    	//increment the link counter
				    	//this is done to depict the next link 
				    	link_counter++; 
					}				
				 }
			 }
			//latency constraint - (8)
			if(InputConstants.latencyCstr){
				for(Map.Entry<MpCstr7and11,IloRange> entryCstr : this.flow_latency_constraint.entrySet()){
					int scID = entryCstr.getKey().chainIndex;
					TrafficNodes tn = entryCstr.getKey().tn;				
					for(Map.Entry<MpVarZ, IloColumn> entry : this.var_z_gamma.entrySet()){
						 //get the complete configuration
		 				 ArrayList<BaseVertex> PLACE_CONFIG = new ArrayList<BaseVertex>();
		 				 PLACE_CONFIG.add(entry.getKey().completeConfig.ingeg.v1);
		 				 PLACE_CONFIG.addAll(entry.getKey().completeConfig.cfg.config);
		 				 PLACE_CONFIG.add(entry.getKey().completeConfig.ingeg.v2);
		 				 //string for comparison
		 				 String zCONFIG="";
		 				 for(BaseVertex vrt : PLACE_CONFIG){
	    					zCONFIG = zCONFIG + vrt.get_id();
		 				 }							
					     //iterating over the set of traffic nodes and their associated service chain (c)
						for(NewIngEgConfigs config_sc : OriginalConfigs.get(tn)){
		    				//to find the ingress-egress nodes
		    				NodePair tempIngeg = config_sc.ingeg;				    				
		    				//get the complete configuration
		    				ArrayList<BaseVertex> place_config = new ArrayList<BaseVertex>();
		    				place_config.add(tempIngeg.v1);
		    				place_config.addAll(config_sc.cfg.config);
		    				place_config.add(tempIngeg.v2);
		    				String zConfig ="";
		    				for(BaseVertex vrt : place_config){
		    					zConfig = zConfig + vrt.get_id();
		    				}
		    				//check if the configurations are the same
		    				if(zConfig.equals(zCONFIG)){
			    				//get the shortest path for the configuration
			    				Path config_sc_path =  config_sc.cfg.config_routes.get(0);
			    				double coeff = 0.0;
			    				//iterate over the links
			    				for(BaseVertex source : g._vertex_list){ 
			    					for(BaseVertex sink : g.get_adjacent_vertices(source)){
			    						double lProp = InputConstants.SEC_TO_MICROSEC*g.get_edge_length(source, sink)/InputConstants.SPEED_OF_LIGHT;
					    				//checking for successive dependencies
				    			        for(int chain_seq_pos = 0; chain_seq_pos < ChainSet.get(scID).getChainSize() - 1; chain_seq_pos++){
				    			        	 BaseVertex i_VNF_node = place_config.get(chain_seq_pos);//NFV node that holds the ith VNF
				    						 BaseVertex i1_VNF_node = place_config.get(chain_seq_pos+1);//NFV node that holds the i+1th VNF
				    			        	 //get the sequence of links between the i'th and i+1'th VNF in the path
				    			        	 int index_i_VNF_in_path = config_sc_path.get_vertices().indexOf(i_VNF_node);
				    						 int index_i1_VNF_in_path = config_sc_path.get_vertices().indexOf(i1_VNF_node);
				    						 if(index_i1_VNF_in_path >= index_i_VNF_in_path){
					    						  List<BaseVertex> link_sequence = config_sc_path.get_vertices().subList( index_i_VNF_in_path, index_i1_VNF_in_path+1 );
					    						  if( link_sequence.contains(source) && link_sequence.contains(sink) && ( link_sequence.indexOf(sink) > link_sequence.indexOf(source)) ){
					    							  coeff += lProp;
					    						  }
				    						 }
				    					}			    			        
			    					}
			    				}
			    				//add the coefficient to var_z_gamma for the link constraints
		    			        this.var_z_gamma.put(entry.getKey(),entry.getValue().and(this.master_problem.column(entryCstr.getValue(),coeff)));
		    				}
						}
					}				
				}
			}
			//one path per c & sd constraint (9)				
			for(Map.Entry<MpCstr7and11,IloRange> entryCstr : this.path_per_c_sd_contraint.entrySet()){				
			   	int scID = entryCstr.getKey().tn.chain_index;	 	    	
			    for(Map.Entry<MpVarZ, IloColumn> entryZ : this.var_z_gamma.entrySet()){		        	
		        	//check if the same service chain
		        	if(entryZ.getKey().sCiD == scID){			    						   		   
			    		//add the coefficient for var_z_gamma to the constraint
			    		this.var_z_gamma.put(entryZ.getKey(),entryZ.getValue().and(this.master_problem.column(entryCstr.getValue(),1)));			    	
		        	}
			    }			   
			}  
			//source outgoing link constraint (10)				
			for(Map.Entry<MpCstr7and11,IloRange> entryCstr : this.src_outgoing_constraint.entrySet()){
		  	 	//traffic node
			   	BaseVertex srcVrt = entryCstr.getKey().tn.v1;
			   	int scID = entryCstr.getKey().tn.chain_index;	 	    	
		        for(Map.Entry<MpVarZ, IloColumn> entryZ : this.var_z_gamma.entrySet()){		        	
		        	//check if the same service chain
		        	if(entryZ.getKey().sCiD == scID){		        		
				    	//check if source node is a NFV PoP
				    	if(nodesNFVI.contains(srcVrt)){
				    		if(srcVrt.get_id() == entryZ.getKey().completeConfig.ingeg.v1.get_id()){					   		   
					    		//add the coefficient for var_z_gamma to the constraint
					    		this.var_z_gamma.put(entryZ.getKey(),entryZ.getValue().and(this.master_problem.column(entryCstr.getValue(),1)));
				    		}
				     	}
		        	}
		        }			   
		    }
			//source incoming link constraint (11)			
		    for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : this.src_incoming_constraint.entrySet()){
		    	//the NFVI node in the constraint
		    	BaseVertex nfviNode = entryCstr.getKey().nfvi_node;
		    	//iterate through Z variables
		    	for(Map.Entry<MpVarZ, IloColumn> entryZ : this.var_z_gamma.entrySet()){
		    		//the NFVI node in the variable Z
		    		BaseVertex nfviZ = entryZ.getKey().completeConfig.ingeg.v1;
		    		//check if same service chain
			    	if(entryCstr.getKey().chainIndex == entryZ.getKey().sCiD){
			    		//check if same NFVI node
			    		if(nfviNode.get_id() == nfviZ.get_id()){
			    			//add the coefficient for var_z_gamma to the constraint
				    		this.var_z_gamma.put(entryZ.getKey(),entryZ.getValue().and(this.master_problem.column(entryCstr.getValue(),1)));
			    		}			    	
				    }
		    	}
		    }
		    //flow conservation constraint - placement - ingress node (12)		   
	  	    for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : this.flow_place_ing_constraint.entrySet()){
	  	    	//the NFVI node in the constraint
		    	BaseVertex nfviNode = entryCstr.getKey().nfvi_node;
		    	//iterate through Z variables
		    	for(Map.Entry<MpVarZ, IloColumn> entryZ : this.var_z_gamma.entrySet()){
		    		//the NFVI node in the variable Z
		    		BaseVertex nfviZ = entryZ.getKey().completeConfig.ingeg.v1;
		    		//check if same service chain
			    	if(entryCstr.getKey().chainIndex == entryZ.getKey().sCiD){
			    		//check if same NFVI node
			    		if(nfviNode.get_id() == nfviZ.get_id()){
			    			//add the coefficient for var_z_gamma to the constraint
				    		this.var_z_gamma.put(entryZ.getKey(),entryZ.getValue().and(this.master_problem.column(entryCstr.getValue(),1)));
			    		}			    	
				    }
		    	}
	  	    }
	  	    //destination incoming link constraint (14)	  	 
           for(Map.Entry<MpCstr7and11,IloRange> entryCstr : this.dest_incoming_constraint.entrySet()){
		  	 	//traffic node
			   	BaseVertex dstVrt = entryCstr.getKey().tn.v2;
			   	int scID = entryCstr.getKey().tn.chain_index;	 	    	
		        for(Map.Entry<MpVarZ, IloColumn> entryZ : this.var_z_gamma.entrySet()){		        	
		        	//check if the same service chain
		        	if(entryZ.getKey().sCiD == scID){		        		
				    	//check if destination node is a NFV PoP
				    	if(nodesNFVI.contains(dstVrt)){
				    		if(dstVrt.get_id() == entryZ.getKey().completeConfig.ingeg.v2.get_id()){					   		   
					    		//add the coefficient for var_z_gamma to the constraint
					    		this.var_z_gamma.put(entryZ.getKey(),entryZ.getValue().and(this.master_problem.column(entryCstr.getValue(),1)));
				    		}
				     	}
		        	}
		        }	
  		    }
  		    //destination outgoing link constraint (15)  		
  		    for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : this.dest_outgoing_constraint.entrySet()){
  		    	//the NFVI node in the constraint
		    	BaseVertex nfviNode = entryCstr.getKey().nfvi_node;
		    	//iterate through Z variables
		    	for(Map.Entry<MpVarZ, IloColumn> entryZ : this.var_z_gamma.entrySet()){
		    		//the NFVI node in the variable Z
		    		BaseVertex nfviZ = entryZ.getKey().completeConfig.ingeg.v2;
		    		//check if same service chain
			    	if(entryCstr.getKey().chainIndex == entryZ.getKey().sCiD){
			    		//check if same NFVI node
			    		if(nfviNode.get_id() == nfviZ.get_id()){
			    			//add the coefficient for var_z_gamma to the constraint
				    		this.var_z_gamma.put(entryZ.getKey(),entryZ.getValue().and(this.master_problem.column(entryCstr.getValue(),1)));
			    		}			    	
				    }
		    	}				 
  		    }
  		    //flow conservation constraint - placement - egress node (16)
  		    for(Map.Entry<MpCstr81012and14,IloRange> entryCstr : this.flow_place_egr_constraint.entrySet()){
  		    	//the NFVI node in the constraint
		    	BaseVertex nfviNode = entryCstr.getKey().nfvi_node;
		    	//iterate through Z variables
		    	for(Map.Entry<MpVarZ, IloColumn> entryZ : this.var_z_gamma.entrySet()){
		    		//the Egress node in the variable Z
		    		BaseVertex nfviZ = entryZ.getKey().completeConfig.ingeg.v2;		    	
		    		//check if same service chain
			    	if(entryCstr.getKey().chainIndex == entryZ.getKey().sCiD){
			    		//check if same NFVI node
			    		if(nfviNode.get_id() == nfviZ.get_id()){
			    			//add the coefficient for var_z_gamma to the constraint
				    		this.var_z_gamma.put(entryZ.getKey(),entryZ.getValue().and(this.master_problem.column(entryCstr.getValue(),1)));
			    		}			    	
				    }
		    	}	 
  			}
  			 //include the range for var_z_gamma variables
  			 //add the variable to the set of variables
//  		 this.configurationCounter = 0;
  		     for(Map.Entry<MpVarZ, IloColumn> entry : this.var_z_gamma.entrySet()){
  		    	String zName = "Z_SC"+ entry.getKey().sCiD + "_In" + entry.getKey().completeConfig.ingeg.v1.get_id() + "_Out" + entry.getKey().completeConfig.ingeg.v2.get_id(); 
  		    	this.usedVarZ.put(entry.getKey(), this.master_problem.numVar(entry.getValue(), 0.0, Double.MAX_VALUE, zName));
  		     }  
		    
  		   
  		     
  		     
  		   //Columns for variable x_vf
  		   this.var_x_vf = new HashMap<MpVarX, IloColumn>();
  		   //Handles for variable x_vf
  		   this.usedVarX = new HashMap<MpVarX, IloNumVar>();
  		   //function instance count (4)
  		   for(Map.Entry<MpVarX, IloRange> entryCstr : this.function_instance_count.entrySet()){ 
  			   int vnfID = entryCstr.getKey().f_id;
	  		   //create the key //the object MpVarX
	  		   MpVarX temp_elem = new MpVarX(entryCstr.getKey().v, entryCstr.getKey().f_id) ;
	  		   //add the column to the constraint
	  		   IloColumn col = this.master_problem.column(entryCstr.getValue(), -1.0*funcCountMax.get(vnfID));
//	  		   IloColumn col = this.master_problem.column(entryCstr.getValue(), -1.0*InputConstants.Big_M);
	  		   //populate the HashMap
	  		   this.var_x_vf.put(temp_elem, col);				  
  		   } 
  		   //function count limit (5)  	
		   for(int fpt=0; fpt<func_list.size(); fpt++){
			   for(BaseVertex nfvi_node : nodesNFVI){
				   FuncPt tempDetails = func_list.get(fpt);
				   MpVarX tmpVar = new MpVarX(nfvi_node, tempDetails.getid());
				   IloColumn col =  this.var_x_vf.get(tmpVar);
				   col = col.and(this.master_problem.column(this.function_instance_contraint.get(fpt), 1));
				   //populate the HashMap
		  		   this.var_x_vf.put(tmpVar, col);
			   }
		   }
		 //add the variable
		  //include the range for var_x_vf variables
		  for(Map.Entry<MpVarX, IloColumn> entry : this.var_x_vf.entrySet()){
		   	String xName = "X_Nod" + entry.getKey().v.get_id() + "_VNF" + entry.getKey().f_id;
		   	this.usedVarX.put(entry.getKey(),this.master_problem.numVar(entry.getValue(), 0.0, 1.0, xName));		   
		  }
		   
		   
		   
		   
		   
		   
  			//Columns for var_y_l_sigma_sd
  			this.var_y_l_sigma_sd = new HashMap<MpVarY, IloColumn>();
  			//Handles for var_y_l_sigma_sd
  			this.usedVarY = new HashMap<MpVarY, IloNumVar>();
  			//including the columns for the objective - (44)
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
//  		   						if( source_to_ingress.contains(s_vert) && source_to_ingress.contains(t_vert) && ( source_to_ingress.indexOf(t_vert) > source_to_ingress.indexOf(s_vert) ) ){
  									//add the column for var_y_l_sigma1_sd
  			   						MpVarY temp_elem_1 = new MpVarY(tn, firstVNF, s_vert, t_vert);
  			   						//the column
  			   						IloColumn col1 = this.master_problem.column( this.BwUsed, tn.flow_traffic);
  			   		   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
  			   						this.var_y_l_sigma_sd.put(temp_elem_1, col1);
//  			   	   					if(this.var_y_l_sigma_sd.get(temp_elem_1) == null){
//  			   							System.out.println("Ingress node column is empty");
//  			   						}
//  		   						}
//  		   						if( egress_to_destination.contains(s_vert) && egress_to_destination.contains(t_vert) && ( egress_to_destination.indexOf(t_vert) > egress_to_destination.indexOf(s_vert) ) ){
  		  		   		        //add the column for var_y_l_sigmaN_sd
  		  						MpVarY temp_elem_N = new MpVarY(tn, lastVNF, s_vert, t_vert);
  		  						//the column
  		  						IloColumn colN = this.master_problem.column( this.BwUsed, tn.flow_traffic);
  		  		   				//coefficient for var_y_l_sigmaN_sd is the bandwidth 
  		  						this.var_y_l_sigma_sd.put(temp_elem_N, colN);   						
//  			   						if(this.var_y_l_sigma_sd.get(temp_elem_N) == null){
//  			   							System.out.println("Egress node column is empty");
//  			   						}
//  		   						}
  						 	} 
  					}   				
  				}
  			}
  			//check if var_y_l_sigma_sd is empty
//  			if(this.var_y_l_sigma_sd.isEmpty()){
//  				System.out.println("No variables added to Y_l_sigma_sd");
//  			}
//  			//print out the columns in the var_y_l_sigma_sd
//  			for(Map.Entry<MpVarY, IloColumn> entry : this.var_y_l_sigma_sd.entrySet()){	    		
//  				//print out key
//  				System.out.println("SD pair: (" + entry.getKey().tn.v1.get_id() + ", "+ entry.getKey().tn.v2.get_id() + "); Link: (" + entry.getKey().s_vrt.get_id() + ", "+ entry.getKey().t_vrt.get_id() + "); VNF: " + entry.getKey().f_id);
//  				//print out value
//  				if(entry.getValue() != null){
//  					System.out.println("Column has been created");
//  				}
//  			}
//  			System.out.println("*************");
//  			System.out.println("*************");
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
								IloColumn col1 = this.var_y_l_sigma_sd.get(temp_elem_1);
							    //check if key exists
					    	    if(col1 != null){
			  						//modify the column object //add coefficient
			  						col1 = col1.and(this.master_problem.column( this.flow_capacity_constraint.get(lnum), tn.flow_traffic));
			  		   				//update the hashmap
			  						this.var_y_l_sigma_sd.put(temp_elem_1, col1);
					    	    }else{
	// 					    	    	System.out.println("Y_firstVNF variable not added in link constraints");
					    	    } 								
				   		        //add the column for var_y_l_sigmaN_sd
								MpVarY temp_elem_N = new MpVarY(tn, lastVNF, s_vert, t_vert);
							    //get the column object
								IloColumn colN = this.var_y_l_sigma_sd.get(temp_elem_N);
								//check if key exists
								if(colN != null){
			  						//modify the column object //add coefficient
			  						colN = colN.and(this.master_problem.column( this.flow_capacity_constraint.get(lnum), tn.flow_traffic));
			  					    //update the hashmap
			  						this.var_y_l_sigma_sd.put(temp_elem_N, colN);
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
							IloColumn col1 = this.var_y_l_sigma_sd.get(temp_elem_1);
						    //check if key exists
				    	    if(col1 != null){
		  						//modify the column object //add coefficient
		  						col1 = col1.and(this.master_problem.column( entryCstr.getValue(), lProp));
		  		   				//update the hashmap
		  						this.var_y_l_sigma_sd.put(temp_elem_1, col1);
				    	    }else{
	//			    	    	System.out.println("Y_firstVNF variable not added in link constraints");
				    	    } 								
			   		        //add the column for var_y_l_sigmaN_sd
							MpVarY temp_elem_N = new MpVarY(tn, lastVNF, source, sink);
						    //get the column object
							IloColumn colN = this.var_y_l_sigma_sd.get(temp_elem_N);
							//check if key exists
							if(colN != null){
		  						//modify the column object //add coefficient
		  						colN = colN.and(this.master_problem.column( entryCstr.getValue(), lProp));
		  					    //update the hashmap
		  						this.var_y_l_sigma_sd.put(temp_elem_N, colN);
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
  							IloColumn col = this.var_y_l_sigma_sd.get(temp_elem_1);
  						    //check if key exists
  				    	    if(col != null){
  		  		   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
  		  		   				col = col.and(this.master_problem.column( entry.getValue(), 1));		   		   		        	
  		  		   				//update the HashMap
  		  		   			    this.var_y_l_sigma_sd.put(temp_elem_1, col);
  				    	    }
  				    	    else{
  				    	    	//print out key
//  					    		System.out.println("SD pair: (" + tn.v1.get_id() + ", "+ tn.v2.get_id() + "); Link: (" + tn.v1.get_id() + ", "+ t_vert.get_id() + "); VNF: " + f_id_1);
//  				    	    	System.out.println("the column was empty : ");
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
   							IloColumn col = this.var_y_l_sigma_sd.get(temp_elem);
   							//check if key exists
   					    	if(col != null){
   				   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
   				   				col = col.and(this.master_problem.column(entry.getValue(), -1));		   		   		        	
   				   				//update the HashMap
   				   			    this.var_y_l_sigma_sd.put(temp_elem, col);
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
  							IloColumn col = this.var_y_l_sigma_sd.get(temp_elem);
  							//check if key exists
  					    	if(col != null){
  				   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
  				   				col = col.and(this.master_problem.column(entry.getValue(), -1));		   		   		        	
  				   				//update the HashMap
  				   			    this.var_y_l_sigma_sd.put(temp_elem, col);
//  				   			    System.out.println("#####Column created for Const 5##### : Incoming links");
//  				   			    System.out.println("Link : ( " + s_vrt.get_id() + " , " + nfv_node.get_id() + " )");
  					    	}
  				    	 }
  				    	 //outgoing links
  				    	 for(BaseVertex t_vrt : g.get_adjacent_vertices(nfvi_node)){
  				    		//add the column for var_y_l_sigma1_sd
  								MpVarY temp_elem = new MpVarY(tn, firstVNF, nfvi_node, t_vrt);
  								//add the column for var_y_l_sigma1_sd
  								IloColumn col = this.var_y_l_sigma_sd.get(temp_elem);
  								//check if key exists
  						    	if(col != null){
  					   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
  					   				col = col.and(this.master_problem.column(entry.getValue(), 1));		   		   		        	
  					   				//update the HashMap
  					   			    this.var_y_l_sigma_sd.put(temp_elem, col);
//  					   			    System.out.println("#####Column created for Const 5##### : Outgoing links");
//  					   			    System.out.println("Link : ( " + nfv_node.get_id() + " , " + t_vrt.get_id() + " )");
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
  							IloColumn col = this.var_y_l_sigma_sd.get(temp_elem);
  							//check if key exists
  					    	if(col != null){
  				   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
  				   				col = col.and(this.master_problem.column(entry.getValue(), 1));		   		   		        	
  				   				//update the HashMap
  				   			    this.var_y_l_sigma_sd.put(temp_elem, col);
  					    	}
  			    		}	    	
  			    		//incoming links	    		 
  			    		for(BaseVertex s_vrt : g.get_precedent_vertices(non_nfvi_vrt)){
  			    			//add the column for var_y_l_sigma1_sd
  							MpVarY temp_elem = new MpVarY(tn, firstVNF, s_vrt, non_nfvi_vrt);
  							//add the column for var_y_l_sigma1_sd
  							IloColumn col = this.var_y_l_sigma_sd.get(temp_elem);
  							//check if key exists
  					    	if(col != null){
  				   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
  				   				col = col.and(this.master_problem.column(entry.getValue(), -1));		   		   		        	
  				   				//update the HashMap
  				   			    this.var_y_l_sigma_sd.put(temp_elem, col);
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
  								IloColumn col = this.var_y_l_sigma_sd.get(temp_elem);
  								//check if key exists
  						    	if(col != null){
  					   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
  					   				col = col.and(this.master_problem.column(entry.getValue(), 1));		   		   		        	
  					   				//update the HashMap
  					   			    this.var_y_l_sigma_sd.put(temp_elem, col);
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
 							IloColumn col = this.var_y_l_sigma_sd.get(temp_elem_N);
 						    //check if key exists
 				    	    if(col != null){
 		  		   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
 		  		   				col = col.and(this.master_problem.column(entry.getValue(), -1));		   		   		        	
 		  		   				//update the HashMap
 		  		   			    this.var_y_l_sigma_sd.put(temp_elem_N, col);   		   			    
// 		   		   			    	System.out.println("#####Column created for Const 11##### : Incoming links");
// 		   		   			    	System.out.println("Link : ( " + s_vert.get_id() + " , " + tn.v2.get_id() + " )");   		   			    
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
  							IloColumn col = this.var_y_l_sigma_sd.get(temp_elem);
  							//check if key exists
  					    	if(col != null){
  				   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
  				   				col = col.and(this.master_problem.column(entry.getValue(), 1));		   		   		        	
  				   				//update the HashMap
  				   			    this.var_y_l_sigma_sd.put(temp_elem, col);
//  				   			    System.out.println("#####Column created for Const 9##### : Incoming links");
//  				   			    System.out.println("Link : ( " + s_vrt.get_id() + " , " + nfv_node.get_id() + " )");
  					    	}
  				    	 }
  				    	 //outgoing links
  				    	 for(BaseVertex t_vrt : g.get_adjacent_vertices(nfvi_node)){
  				    		//add the column for var_y_l_sigma1_sd
  							MpVarY temp_elem = new MpVarY(tn, lastVNF, nfvi_node, t_vrt);
  							//add the column for var_y_l_sigma1_sd
  							IloColumn col = this.var_y_l_sigma_sd.get(temp_elem);
  							//check if key exists
  					    	if(col != null){
  				   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
  				   				col = col.and(this.master_problem.column(entry.getValue(), -1));		   		   		        	
  				   				//update the HashMap
  				   			    this.var_y_l_sigma_sd.put(temp_elem, col);
//  				   			    System.out.println("#####Column created for Const 9##### : Outgoing links");
//  				   			    System.out.println("Link : ( " + nfv_node.get_id() + " , " + t_vrt.get_id() + " )");
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
  							IloColumn col = this.var_y_l_sigma_sd.get(temp_elem);
  							//check if key exists
  					    	if(col != null){
  				   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
  				   				col = col.and(this.master_problem.column(entry.getValue(), 1));		   		   		        	
  				   				//update the HashMap
  				   			    this.var_y_l_sigma_sd.put(temp_elem, col);
//  				   			    System.out.println("#####Column created for Const 10##### : Outgoing links");
//  				   			    System.out.println("Link : ( " + non_nfv_vrt.get_id() + " , " + t_vrt.get_id() + " )");
  					    	}
  			    		 }	    	  
  			    		 //incoming links	    		
  			    		 for(BaseVertex s_vrt : g.get_precedent_vertices(non_nfvi_vrt)){
  			    			//add the column for var_y_l_sigma1_sd
  							MpVarY temp_elem = new MpVarY(tn, lastVNF, s_vrt, non_nfvi_vrt);
  							//add the column for var_y_l_sigma1_sd
  							IloColumn col = this.var_y_l_sigma_sd.get(temp_elem);
  							//check if key exists
  					    	if(col != null){
  				   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
  				   				col = col.and(this.master_problem.column(entry.getValue(), -1));		   		   		        	
  				   				//update the HashMap
  				   			    this.var_y_l_sigma_sd.put(temp_elem, col);  				   			    
//			   			    	System.out.println("#####Column created for Const 17#### : Incoming links");
//			   			    	System.out.println("Link : ( " + s_vrt.get_id() + " , " + non_nfvi_vrt.get_id() + " )");  				   			    
  					    	}
  			    		 }
  					 }				     
  			    }
  		   }
  		    //outgoing degree 1 for all nodes in the network - (E1)		    
  		   	/*for(Map.Entry<Mp2CstrE, IloRange> entryCstr : this.outgoingE1.entrySet()){
  		   		//Traffic Node
  		   		TrafficNodes tn = entryCstr.getKey().tn;
  		   		int vnfPos = entryCstr.getKey().vnfPos;		    	
	    		//Vertex
	    		BaseVertex s_vrt = entryCstr.getKey().vrt;
		    	//outgoing links
		    	for(BaseVertex t_vrt : g.get_adjacent_vertices(s_vrt)){
		    		//add the column for var_y_l_sigmaLast_sd
					MpVarY temp_elem = new MpVarY(tn, vnfPos, s_vrt, t_vrt);
		    		//get the column object if its exists
		   			IloColumn col = this.var_y_l_sigma_sd.get(temp_elem);		   		
		   		    //check if key exists
		    		if(col != null){
		    			//add the column to the constraint
		    			col = col.and(this.master_problem.column(entryCstr.getValue(), 1.0));
		    			//update the HashMap
		    			this.var_y_l_sigma_sd.put(temp_elem, col);  
		    		}else{		    		
			    		//add the elements to the HashMap
			    		this.var_y_l_sigma_sd.put(temp_elem, this.master_problem.column(entryCstr.getValue(), 1.0));
		    		}		    		
		    	}  			    
  			}
  		    //incoming degree 1 for all nodes in the network - (E2)		    
  			for(Map.Entry<Mp2CstrE, IloRange> entryCstr : this.incomingE2.entrySet()){
  		   		//Traffic Node
  		   		TrafficNodes tn = entryCstr.getKey().tn;  		   		
  		   		int vnfPos = entryCstr.getKey().vnfPos;		    		    
	    		//Vertex
	    		BaseVertex t_vrt = entryCstr.getKey().vrt;
		    	//incoming links
		    	for(BaseVertex s_vrt : g.get_precedent_vertices(t_vrt)){
		    		//add the column for var_y_l_sigmaLast_sd
					MpVarY temp_elem = new MpVarY(tn, vnfPos, s_vrt, t_vrt);
		    		//get the column object if its exists
		   			IloColumn col = this.var_y_l_sigma_sd.get(temp_elem);		   		
		   			//check if key exists
		    		if(col != null){
		    			//add the column to the constraint
		    			col = col.and(this.master_problem.column(entryCstr.getValue(), 1.0));
		    			//update the HashMap
		    			this.var_y_l_sigma_sd.put(temp_elem, col);  
		    		}else{		    		
			    		//add the elements to the HashMap
			    		this.var_y_l_sigma_sd.put(temp_elem, this.master_problem.column(entryCstr.getValue(), 1.0));
		    		}		    	
		    	}
  			    
  			}*/
  		    //add the variable
  			//include the range for var_y_l_sigma_sd variables
  			for(Map.Entry<MpVarY, IloColumn> entry : this.var_y_l_sigma_sd.entrySet()){
  				String yName = "Y_SC"+ entry.getKey().tn.chain_index + "_Ind" + entry.getKey().f_id + "_Src" +  entry.getKey().tn.v1.get_id() + "_Dst" + entry.getKey().tn.v2.get_id() + "_Ls" + entry.getKey().s_vrt.get_id() + "_Ld" + entry.getKey().t_vrt.get_id();
  				this.usedVarY.put(entry.getKey(), this.master_problem.numVar(entry.getValue(),0.0,1.0,yName));  			
  			}
			  	
			   
			   
			     
		}
	
}
