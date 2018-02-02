package colGen.model.ver1;

import ilog.concert.IloColumn;
import ilog.concert.IloCopyable;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.heuristic.BaseHeuristic2.SdDetails;
import colGen.model.heuristic.HuerVarZ;
import colGen.model.output.NfviNodeDetails;
import colGen.model.ver2.Pp2VarDelta;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class MasterProblem {
	//model for the master problem
	public IloCplex master_problem;
	//Objective for the master problem (2)
	public IloObjective BwUsed;
	//configuration selection constraint(3)			      
    public Map<Integer,IloRange> config_selection_constraint;
    //configuration constraints (4)
//  public ArrayList<IloRange> place_config_constraint;
    //core capacity constraint	(5)		  
    public ArrayList<IloRange> core_capacity_constraint;
    //flow capacity constraint (6)
    public ArrayList<IloRange> flow_capacity_constraint;
    //function placement relating to configuration selection constraint - 1 (7)
    public Map<NwMpCstr7and8,IloRange> rel_place_conf_constraint_1;
    //function placement relating to configuration selection constraint - 2 (8)
//    public Map<NwMpCstr7and8,IloRange> rel_place_conf_constraint_2;
    //atleast one function instance is to be enforced
//  public ArrayList<IloRange> vnf_feasibility;
    //vnf replica constraint 1
    public Map<MpVarX,IloRange> vnf_replica_constraint_1;
    //vnf replica constraint 2
    public ArrayList<IloRange> vnf_replica_constraint_2;
    //vnf replica constraint 3
    public Map<MpVarX,IloRange> vnf_replica_constraint_3;
    //VNF Location 1
    public Map<BaseVertex,IloRange> vnf_location_constraint_1;
    //VNF Location 2
    public Map<BaseVertex,IloRange> vnf_location_constraint_2;
    //VNF Location 3
    public IloRange vnf_location_constraint_3;    
    //source outgoing link constraint (9)
    public Map<MpCstr7and11,IloRange> src_outgoing_constraint;
    //source incoming link constraint (10)
    public Map<MpCstr81012and14,IloRange> src_incoming_constraint;
    //flow conservation constraint - placement - ingress node (11)
    public Map<MpCstr81012and14,IloRange> flow_place_ing_constraint;
    //flow conservation constraint - no placement - ingress node (12)
    public Map<MpCstr9and13,IloRange> flow_noplace_ing_constraint;    
    //destination incoming link constraint (13)
    public Map<MpCstr7and11,IloRange> dest_incoming_constraint;
    //destination outgoing link constraint (14)
    public Map<MpCstr81012and14,IloRange>  dest_outgoing_constraint;
    //flow conservation constraint - placement - egress node (15)
    public Map<MpCstr81012and14,IloRange> flow_place_egr_constraint;
    //flow conservation constraint - no placement - egress node (16)
    public Map<MpCstr9and13,IloRange> flow_nplace_egr_constraint;
    
    //store the slack variables
    public Map<String,IloNumVar> slackVariables;
    //Handles for variable z_gamma
    public Map<MpVarZ,IloNumVar> usedVarZ;	
    //Handles for variable x_vf
    public Map<MpVarX, IloNumVar> usedVarX;
    //Handles for variable x_v^ci
    public Map<MpVarXc, IloNumVar> usedVarXc;	
    //Handles for variables h_v
    public Map<MpVarH, IloNumVar> usedVarH;
	//Handles for var_y_l_sigma_sd
	public Map<MpVarY, IloNumVar> usedVarY;


	
	//create the Master Problem Object
	//without remembering previous columns
	public MasterProblem(boolean coreCstr, boolean capCstr, Map<BaseVertex,Double> cpuCoreCount, Map<NodePair,Double> linkCapacity,
			List<FuncPt> func_list, List<Integer> scUsed,  Map<Integer,ArrayList<TrafficNodes>> serviceChainTN, Map<Integer,ArrayList<HuerVarZ>> configsPerSC, 
			Map<TrafficNodes,SdDetails> configPerSD, Graph g, ArrayList<BaseVertex> nodesNFVI, ArrayList<BaseVertex> nfv_nodes, 
			Map<Integer, ServiceChain> ChainSet, List<TrafficNodes> pair_list, ArrayList<BaseVertex> vertex_list_without_nfvi_nodes,  
			Map<Integer,ArrayList<Integer>> scCopies,  Map<Integer,Integer> SCcopyToSC, Map<Integer,Integer> CountMaxVNF, Map<Integer,Integer> replicaPerVNF,Integer kNodeCount) throws Exception{
		
		
		
		 //Columns for variable z_gamma			   
	     Map<MpVarZ,IloColumn> var_z_gamma;
	     //Columns for variable x_vf
	     Map<MpVarX, IloColumn> var_x_vf;
	     //Columns for variable h_v
	     Map<MpVarH, IloColumn> var_h_v;
	     //Columns for variable x_v^ci
	     Map<MpVarXc, IloColumn> var_x_civ;
	     //Columns for var_y_l_sigma_sd
		 Map<MpVarY, IloColumn> var_y_l_sigma_sd;
		
		
		 //model for the master problem
	     this.master_problem = new IloCplex();
	     //objective for master problem
	     //trying to minimize the bandwidth used in routing the service requests
	     //(2) Objective for the master problem
	     this.BwUsed = this.master_problem.addMinimize(); 	   
	     //returns the LP Matrix associated with the master problem
		 IloLPMatrix matrixDummyColumns = this.master_problem.addLPMatrix();	
	     
		 		 
	     int cstrNum=0;//reset the constraint no. counter 
	     //Add the RANGED CONSTRAINTS
	 	 //configuration selection constraint(3)		
	     this.config_selection_constraint = new HashMap<Integer,IloRange>();
	     for(int scID : scUsed){
	    	 for(int scCopyID : scCopies.get(scID)){
		    	 String constraint = "cstr3" + "_" + cstrNum;
		    	 IloRange rng = this.master_problem.addRange(1.0, 1.0, constraint);	
		    	 cstrNum++;	    	
		    	 //keep track of constraint
		    	 //the chain number corresponds to the service chain ID
		    	 this.config_selection_constraint.put( scCopyID, rng);
		    	 //add the range to the matrix dummy column
			     matrixDummyColumns.addRow(rng);
	    	 }
	     }
	     System.out.println("Constraint (3) generated!");
	     /*cstrNum=0;//reset the constraint no. counter
	     //configuration constraints (4)
	     this.place_config_constraint = new ArrayList<IloRange>();
	     for(int f=0; f<func_list.size(); f++){
	    	String constraint = "cstr4" + "_" + cstrNum;
	    	IloRange rng = this.master_problem.addRange(1.0, Double.MAX_VALUE, constraint);
	    	cstrNum++;
	    	//keep track of constraint
	    	this.place_config_constraint.add(rng);
	    	//add the range to the matrix dummy column
	    	matrixDummyColumns.addRow(rng);
	     }	
	     System.out.println("Constraint (4) generated!");*/
	     cstrNum=0;//reset the constraint no. counter
	     //core capacity constraint	(5)		  
	     this.core_capacity_constraint = new ArrayList<IloRange>();
	     for(int vnf_node=0; vnf_node < nfv_nodes.size(); vnf_node++){
	    	BaseVertex nfvNode = nfv_nodes.get(vnf_node);
	    	double coreCount = 0.0;
	    	if(cpuCoreCount.get(nfvNode) != null){
	    		coreCount = cpuCoreCount.get(nfvNode);
	    	}
	    	String constraint = "cstr5" + "_" + cstrNum;
	    	IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, coreCount, constraint);
	    	cstrNum++;
	    	//keep track of constraint
	    	this.core_capacity_constraint.add(rng);
	    	//add dummy variable if core capacity constraint is imposed
	    	if(coreCstr){
		    	//add the range to the matrix dummy column
		    	matrixDummyColumns.addRow(rng);
	    	}
	     }
	     System.out.println("Constraint (5) generated!");
	     //flow capacity constraint (6)
	     this.flow_capacity_constraint = new ArrayList<IloRange>();
	     int link_num = 0;
	     for(BaseVertex s_vert : g._vertex_list){ 
			for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){
				NodePair link = new NodePair(s_vert,t_vert);
				double linkBw = 0.0;
				if(linkCapacity.get(link)!=null){
					linkBw = linkCapacity.get(link);
				}
				String constraint = "cstr6" + "_" + s_vert.get_id() + "_" + t_vert.get_id();
				IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, linkBw, constraint);				
				//keep track of constraint
				this.flow_capacity_constraint.add(link_num,  rng);
				//add dummy variable if the capacity constraint is enforced
				if(capCstr){
					//add the range to the matrix dummy column
			    	matrixDummyColumns.addRow(rng);
				}
				//increment the link number
				link_num++;
			} 					
		 }
	     System.out.println("Constraint (6) generated!");
	     cstrNum=0;//reset the constraint no. counter
	     //function placement relating to configuration selection constraint - 1 (7)
	     this.rel_place_conf_constraint_1 = new HashMap<NwMpCstr7and8,IloRange>();
	     for(int scID : scUsed){
	    	 for(int scCopyID : scCopies.get(scID)){
		    	 for(int f_in_C=0; f_in_C < ChainSet.get(scID).chain_seq.size(); f_in_C++ ){	    		
		    		 for(BaseVertex nfviNode : nodesNFVI){	    		
			    		 String constraint = "cstr7" + "_" + cstrNum;
//			    		 IloRange rng = this.master_problem.addRange(0.0, Double.MAX_VALUE, constraint);
			    		 IloRange rng = this.master_problem.addRange(0.0, 0.0, constraint);
			    		 cstrNum++;
			    		 //create the constraint index object
			    		 NwMpCstr7and8 cstr7 = new NwMpCstr7and8(scCopyID, f_in_C, nfviNode);
			    		 //keep track of constraint
			    		 this.rel_place_conf_constraint_1.put(cstr7, rng );
			    		 //add the range to the matrix dummy column
					     matrixDummyColumns.addRow(rng);
		    		 }
			     }	
	    	 }
		 }
	     System.out.println("Constraint (7) generated!");	 
	     /*cstrNum=0;//reset the constraint no. counter
	     //function placement relating to configuration selection constraint - 2 (8)
	     this.rel_place_conf_constraint_2 = new HashMap<NwMpCstr7and8,IloRange>();
	     for(int scID : scUsed){
	    	 for(int scCopyID : scCopies.get(scID)){
		    	 for(int f_in_C=0; f_in_C < ChainSet.get(scID).chain_seq.size(); f_in_C++ ){	    	
		    		 for(BaseVertex nfviNode : nodesNFVI){	    		 
			    		 String constraint = "cstr8" + "_" + cstrNum;
			    		 IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, 0.0, constraint);
			    		 cstrNum++;
			    		 //create the constraint index object
			    		 NwMpCstr7and8 cstr8 = new NwMpCstr7and8(scCopyID, f_in_C, nfviNode);
			    		 //create the constraint
			    		 this.rel_place_conf_constraint_2.put(cstr8, rng);
			    		 //add the range to the matrix dummy column
					     matrixDummyColumns.addRow(rng);
		    		 }
			     }
	    	 }
		 }
	     System.out.println("Constraint (8) generated!");*/	 
	     //VNF replica constraints
	     cstrNum=0;//reset the constraint no. counter
	     this.vnf_replica_constraint_1 = new HashMap<MpVarX,IloRange>();
	     for(BaseVertex nfviNode : nodesNFVI){
	    	 for(FuncPt fpt : func_list){
	    		 MpVarX tempCstr = new MpVarX(nfviNode,fpt.getid());
	    		 String constraint = "cstrVnfReplica1_" + cstrNum;
	    		 IloRange rng = this.master_problem.addRange(0.0, Double.MAX_VALUE, constraint);
	    		 cstrNum++;
	    		 //keep track of constraint
	    		 this.vnf_replica_constraint_1.put(tempCstr,rng);
	    	 }
	     }
	     System.out.println("##### Constraints for vnf replica constraint 1 generated! #####");
	     cstrNum=0;//reset the constraint no. counter
	     this.vnf_replica_constraint_2 = new ArrayList<IloRange>();
	     for(int f=0; f<func_list.size(); f++){
	    	int fID = func_list.get(f).getid();
	    	String constraint = "vnfID" + fID + "_cstrVnfReplica2_" + "_" + cstrNum;
	    	IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, replicaPerVNF.get(fID), constraint);
	    	cstrNum++;
	    	//keep track of constraint
	    	this.vnf_replica_constraint_2.add(rng);	    	
	     }
	     System.out.println("##### Constraints for vnf replica constraint 2 generated! #####");
	     cstrNum=0;//reset the constraint no. counter
	     this.vnf_replica_constraint_3 = new HashMap<MpVarX,IloRange>();
	     for(BaseVertex nfviNode : nodesNFVI){
	    	 for(FuncPt fpt : func_list){
	    		 MpVarX tempCstr = new MpVarX(nfviNode,fpt.getid());
	    		 String constraint = "cstrVnfReplica3_" + cstrNum;
	    		 IloRange rng = this.master_problem.addRange(0.0, Double.MAX_VALUE, constraint);
	    		 cstrNum++;
	    		 //keep track of constraint
	    		 this.vnf_replica_constraint_3.put(tempCstr,rng);
	    	 }
	     }
	     System.out.println("##### Constraints for vnf replica constraint 3 generated! #####");
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
	     for(Map.Entry<BaseVertex,IloRange> entry : this.vnf_location_constraint_2.entrySet()){
	    	 System.out.println("Node: " + entry.getKey().get_id() + ", Range:" + entry.getValue().toString());
	     }
	     System.out.println("##### Constraints for vnf location constraint 2 generated! #####");
	     //location constraint 3
	     this.vnf_location_constraint_3 = this.master_problem.addRange(-Double.MAX_VALUE,kNodeCount,"cstrVnfLocation3");
	     System.out.println("##### Constraints for vnf location constraint 3 generated! #####");
	     
	     
	     
	     /*cstrNum=0;//reset the constraint no. counter
	     //function replica constraint (4)
	     //changed the replica constraint!!!!!
	     ArrayList<IloRange> replica_constraint = new ArrayList<IloRange>();
	     for(int f=0; f<func_list.size(); f++){
	    	 String constraint = "cstr4" + "_" + cstrNum;
	    	 IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, InputConstants.replicaLimit, constraint);
//	    	 IloRange rng = this.master_problem.addRange(InputConstants.replicaLimit, Double.MAX_VALUE,  constraint);
	    	 cstrNum++;
	    	 //keep track of constraint
	    	 replica_constraint.add(rng);				    	
		 }*/
	     cstrNum=0;//reset the constraint no. counter
	     //source outgoing link constraint (9)
	     this.src_outgoing_constraint = new HashMap<MpCstr7and11,IloRange>();
	     //iterate through the list of service chain indices			 			    	
	     for(int sd_count=0; sd_count<pair_list.size(); sd_count++){
	    	//create the traffic node
	    	TrafficNodes tn = pair_list.get(sd_count);
	    	//create the constraint object
	    	MpCstr7and11 cstr9 = new MpCstr7and11(tn.chain_index,tn);
	    	//create the range to be considered
	    	String constraint = "cstr9" + "_" + cstrNum;
	    	IloRange rng = this.master_problem.addRange(1.0, 1.0, constraint);
	    	cstrNum++;
	    	//keep track of constraint
	    	this.src_outgoing_constraint.put(cstr9, rng);			    	
	     }
	     System.out.println("Constraint (9) generated!");
	     cstrNum=0;//reset the constraint no. counter
	     //source incoming link constraint (10)
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
	    		//create the range to be considered
		    	String constraint = "cstr10" + "_" + cstrNum;
	    		IloRange rng = this.master_problem.addRange(0.0, Double.MAX_VALUE, constraint);
	    		cstrNum++;
	    		//create the constraint
	    		MpCstr81012and14 cstr10 = new MpCstr81012and14(tn.chain_index, tn, nfvi_node); 
	    		//add range to the constraint 
	    		this.src_incoming_constraint.put(cstr10, rng); 
		     }			    	
	     }
	     System.out.println("Constraint (10) generated!");
	     cstrNum=0;//reset the constraint no. counter
	     //flow conservation constraint - placement - ingress node (11)
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
	    		MpCstr81012and14 cstr11 = new MpCstr81012and14(tn.chain_index, tn, nfvi_node); 
	    		//create the range to be considered
		    	String constraint = "cstr11" + "_" + cstrNum;
	    		IloRange rng = this.master_problem.addRange(0.0, 0.0, constraint);
	    		cstrNum++;
	    		//add range to the constraint 
	    		this.flow_place_ing_constraint.put(cstr11, rng); 
		     }			    	
	     }
	     System.out.println("Constraint (11) generated!");
	     cstrNum=0;//reset the constraint no. counter
	     //flow conservation constraint - no placement - ingress node (12)
	     this.flow_noplace_ing_constraint = new HashMap<MpCstr9and13,IloRange>();
	     //only sd-pairs where the source node is not a NFV-capable node
	     for(TrafficNodes tn : pair_list){
	    	ArrayList<BaseVertex> temp_non_nfvi_nodes = new ArrayList<BaseVertex>(vertex_list_without_nfvi_nodes);
	    	//remove the source vertex if in the non-NFV node list
	    	if(temp_non_nfvi_nodes.contains(tn.v1)){
	    		temp_non_nfvi_nodes.remove(tn.v1);
	    	}
	    	for(BaseVertex non_nfv_node : temp_non_nfvi_nodes){
	    		//create the range to be considered
		    	String constraint = "cstr12" + "_" + cstrNum;
	    		IloRange rng = this.master_problem.addRange(0.0, 0.0, constraint);
	    		cstrNum++;
	    		//create the constraint
	    		MpCstr9and13 cstr12 = new MpCstr9and13(tn.chain_index, tn, non_nfv_node); 
	    		//add range to the constraint 
	    		this.flow_noplace_ing_constraint.put(cstr12, rng);
		     }			    	
	     }
	     System.out.println("Constraint (12) generated!");	
	     cstrNum=0;//reset the constraint no. counter
	     //destination incoming link constraint (13)
	     this.dest_incoming_constraint = new HashMap<MpCstr7and11,IloRange>();			   
	     for(int sd_count=0; sd_count < pair_list.size(); sd_count++){
		    	//create the traffic node
		    	TrafficNodes tn = pair_list.get(sd_count);
		    	//create the constraint object
		    	MpCstr7and11 cstr13 = new MpCstr7and11(tn.chain_index,tn);
		    	//create the range to be considered
		    	String constraint = "cstr13" + "_" + cstrNum;
	    		IloRange rng = this.master_problem.addRange(1.0, 1.0, constraint);
	    		cstrNum++;
	    		//keep track of constraint
	    		this.dest_incoming_constraint.put(cstr13, rng);			    	
		 }
	     System.out.println("Constraint (13) generated!");	
	     cstrNum=0;//reset the constraint no. counter			    
	     //destination outgoing link constraint (14)
	     this.dest_outgoing_constraint = new HashMap<MpCstr81012and14,IloRange>();
	     for(TrafficNodes tn : pair_list){
	    	 //add the nfv nodes
	    	 ArrayList<BaseVertex> temp_nfvi_nodes = new ArrayList<BaseVertex>(nodesNFVI);	    	
	    	 //remove the destinatio node vertex if in the NFV node list
	    	 if(temp_nfvi_nodes.contains(tn.v2)){
	    		 temp_nfvi_nodes.remove(tn.v2);
	    	 }
		     for(BaseVertex nfvi_node : temp_nfvi_nodes){
		    	 //create the range to be considered
			     String constraint = "cstr14" + "_" + cstrNum;
		    	 IloRange rng = this.master_problem.addRange(0.0, Double.MAX_VALUE, constraint);
		    	 cstrNum++;
		    	 //create the constraint
		    	 MpCstr81012and14 cstr14 = new MpCstr81012and14(tn.chain_index, tn, nfvi_node); 
		    	 //keep track of constraint
		    	 this.dest_outgoing_constraint.put( cstr14 , rng);
		     }
	     }
	     System.out.println("Constraint (14) generated!");
	     cstrNum=0;//reset the constraint no. counter
	     //flow conservation constraint - placement - egress node (15)
	     this.flow_place_egr_constraint = new HashMap<MpCstr81012and14,IloRange>();
	     for(TrafficNodes tn : pair_list){
	    	//add the set of NFV nodes
	    	ArrayList<BaseVertex> temp_nfvi_nodes = new ArrayList<BaseVertex>(nodesNFVI);	    	
	    	//remove the destination vertex if it is a NFV node
	    	if(temp_nfvi_nodes.contains(tn.v2)){
	    		temp_nfvi_nodes.remove(tn.v2);
	    	}
	    	for(BaseVertex nfvi_node : temp_nfvi_nodes){
	    		//create the range to be considered
		    	String constraint = "cstr15" + "_" + cstrNum;
	    		IloRange rng = this.master_problem.addRange(0.0, 0.0, constraint);
	    		cstrNum++;
	    		//create the constraint
	    		MpCstr81012and14 cstr15 = new MpCstr81012and14(tn.chain_index, tn, nfvi_node); 
	    		//add range to the constraint 
	    		this.flow_place_egr_constraint.put(cstr15, rng); 
		     }			    	
		 }
	     System.out.println("Constraint (15) generated!");
	     cstrNum=0;//reset the constraint no. counter
	     //flow conservation constraint - no placement - egress node (16)
	     this.flow_nplace_egr_constraint = new HashMap<MpCstr9and13,IloRange>();
	     //only sd-pairs where the source node is not a NFV-capable node
	     for(TrafficNodes tn : pair_list){
	    	ArrayList<BaseVertex> temp_non_nfvi_nodes = new ArrayList<BaseVertex>(vertex_list_without_nfvi_nodes);
	    	//remove the destination vertex if in the non-NFV node list
	    	if(temp_non_nfvi_nodes.contains(tn.v2)){
	    		temp_non_nfvi_nodes.remove(tn.v2);
	    	}
	    	for(BaseVertex non_nfv_node : temp_non_nfvi_nodes){	
	    		//create the range to be considered
		    	String constraint = "cstr16" + "_" + cstrNum;
	    		IloRange rng = this.master_problem.addRange(0.0, 0.0, constraint);
	    		cstrNum++;
	    		//create the constraint
	    		MpCstr9and13 cstr16 = new MpCstr9and13(tn.chain_index, tn, non_nfv_node); 
	    		//add range to the constraint 
	    		this.flow_nplace_egr_constraint.put(cstr16, rng); 
		     }			    	
	     }	 
	     System.out.println("Constraint (16) generated!");
	     /*System.out.println("#### Number of Rows before adding diagonal matrix : " + this.master_problem.getNrows() + " #####");
	     System.out.println("#### Number of Columns before adding diagonal matrix : " + this.master_problem.getNcols() + " #####");
	     System.out.println("#### Number of Non-zero's before adding diagonal matrix : " + this.master_problem.getNNZs() + " #####");*/
	     
	     
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
	    	 	 //add the variable to the set of master problem variables
	    	     this.slackVariables.put(slackVar, tempVar);	    	    
	    	     //add the variable to the linear expression			    	     
	    	     test.addTerm(tempVar, coeffSV);
	    	 	 //add the temp variable to the matrix
	    		 matrixDummyColumns.addColumn(tempVar, new int[]{rowIndex}, new double[]{1.0}); 	    	    
	     }
	     //add the linear expression to the Objective
	     this.BwUsed.setExpr(test);
	     System.out.println("Slack variables added to objective!");
	     /*System.out.println("#### Number of Rows after adding diagonal matrix : " + this.master_problem.getNrows() + " #####");
	     System.out.println("#### Number of Columns after adding diagonal matrix : " + this.master_problem.getNcols() + " #####");
	     System.out.println("#### Number of Non-zero's after adding diagonal matrix : " + this.master_problem.getNNZs() + " #####");*/
	   
	 
	    
	     
	     
	     
	    
	     
	     
	     //Add the VARIABLES
	     //Columns for variable z_gamma			   
	     var_z_gamma = new HashMap<MpVarZ,IloColumn>();
	     //Handles for variable z_gamma
	     this.usedVarZ = new HashMap<MpVarZ,IloNumVar>();
	     //including the columns for the objective - (2)
	     //iertating over the set of service chains
	     for(int scID : scUsed){
    	    for(int scCopyID : scCopies.get(scID)){	    	    	
    	    	for(HuerVarZ config : configsPerSC.get(scID)){ 
    	    		if(config.sCiD == scCopyID){
		    	    	double coeffZObj = 0.0;
		    	    	for(Pp2VarDelta varD : config.DeltaVarSet){
		    	    		coeffZObj += varD.sd.flow_traffic*config.BVarSet.size();
		    	    	}				    		
			    		//add the column for var_z_gamma 	//coefficient for var_z_gamma is the bandwidth multiplied by the number of links used by the configuration			    	
			    		var_z_gamma.put(config, this.master_problem.column( this.BwUsed, coeffZObj));	
		//	    		System.out.println("Coefficient for VarZGamma in this.BwUsed : " + entry.getKey().flow_traffic*link_included );
    	    		}
    	    	}
    	    }
	    }
		//configuration selection constraint (3) 
	     for(int scID : scUsed){
	    	 for(int scCopyID : scCopies.get(scID)){
		    	 for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){				 			
					 //check if Z has same service chain
					 if(entry.getKey().sCiD == scCopyID){
						 //add the coefficient for the var_z_gamma variable					
						 var_z_gamma.put(entry.getKey(),entry.getValue().and(this.master_problem.column( this.config_selection_constraint.get(scCopyID), 1.0)));
					 }					 
				 }
	    	 }
	    }	 
	   //including the columns for configuration constraints - (4)
	   //iterating over the set of traffic nodes and their associated service chain (c)
	   /*for(int f=0; f<func_list.size(); f++){
		   int VNF_required = func_list.get(f).getid();
	       for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){
	    	   int scCopyID = entry.getKey().sCiD;
	    	   int scID = SCcopyToSC.get(scCopyID);
	    	   //number of times the VNF occurs in Service Chain
	    	   int number_of_occurences_of_VNF_required = Collections.frequency(ChainSet.get(scID).chain_seq,VNF_required);	    	   
	    	   IloColumn col = entry.getValue();
	    	   col = col.and(this.master_problem.column( this.place_config_constraint.get(f), number_of_occurences_of_VNF_required));
	    	   var_z_gamma.put(entry.getKey(),col);
	    	   //add the coefficient for the var_z_gamma variable
//	    	   var_z_gamma.put(entry.getKey(),entry.getValue().and(this.master_problem.column( this.place_config_constraint.get(f), number_of_occurences_of_VNF_required)));				    	   
	       } 	 
	   }*/  
	   if(coreCstr){
		   //including the columns for the core capacity constraints - (5)
		   //iterating over the set of NFV-capable nodes
		   for(int vnf_node_index=0; vnf_node_index < nfv_nodes.size(); vnf_node_index++){	
			   BaseVertex nfvNode = nfv_nodes.get(vnf_node_index);
			   //iterating over the set of service chains
			   for(int scID : scUsed){//Service Chain corresponding to service chain ID 
				     ServiceChain scTemp = ChainSet.get(scID);
				     int scSize = scTemp.chain_seq.size();
				     for(int scCopyID : scCopies.get(scID)){
					     //iterating over the set of traffic nodes and their associated service chain (c) //check if the configuration is for the same chain
				    	 for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){
				    		HuerVarZ config = (HuerVarZ) entry.getKey();					    		 
				    		if(config.sCiD == scCopyID){				    			
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
	   }
	  if(capCstr){
		  //including the columns for the flow capacity constraints - (6)
		  //iterating over all the links
		  int link_counter = 0; 
		  for(BaseVertex s_vert : g._vertex_list){ 
			for(BaseVertex t_vert : g.get_adjacent_vertices(s_vert)){					 
				for(int scID : scUsed){
			 	   ServiceChain scTemp = ChainSet.get(scID);
			 	   for(int scCopyID : scCopies.get(scID)){
					   for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){
						   	 HuerVarZ config = (HuerVarZ) entry.getKey();	
						   	 if(config.sCiD == scCopyID){
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
				 }
		    	 //increment the link counter
		    	 //this is done to depict the next link 
		    	 link_counter++; 
			}				
		 }
	 }
	 //including the columns for function placement relating to configuration selection constraint - 1 - (7)
	 for(Map.Entry<NwMpCstr7and8, IloRange> entryCstr : this.rel_place_conf_constraint_1.entrySet()){
		 	 //the required function
			 int VNF_SEQ = entryCstr.getKey().fSeq;
			 //the NFV_node under consideration
			 BaseVertex NFVI_node = entryCstr.getKey().nfviNode;
			 //variable A
		 	  PpVarA tempVarA = new PpVarA(NFVI_node,VNF_SEQ);
		     //iterating over the set of traffic nodes and their associated service chain (c)
	    	 for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){
	    		 	HuerVarZ config = (HuerVarZ) entry.getKey();				    
				    //match the constraint scID to MpVarZ scID
				    if( (entryCstr.getKey().scID==config.sCiD) && (config.AVarSet.contains(tempVarA)) ){	   					
						//add the coefficient for the this.var_z_gamma variable
	    				var_z_gamma.put(entry.getKey(),entry.getValue().and(this.master_problem.column(entryCstr.getValue(), 1.0)));	
				    }
	    	}				  
	 }
	 //including the columns for function placement relating to configuration selection constraint - 2 - (8)
	 //iterating over the set of traffic nodes and their associated service chain (c)		
	 /*for(Map.Entry<NwMpCstr7and8, IloRange> entryCstr : this.rel_place_conf_constraint_2.entrySet()){
	 	  //the required function
	 	  int VNF_SEQ = entryCstr.getKey().fSeq;
	 	  //the NFV_node under consideration
	 	  BaseVertex NFVI_node = entryCstr.getKey().nfviNode;
	 	  //variable A
	 	  PpVarA tempVarA = new PpVarA(NFVI_node,VNF_SEQ);
	 	  //iterate through the configurations
		  for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){	
			    HuerVarZ config = (HuerVarZ) entry.getKey();				    
			    //match the constraint scID to MpVarZ scID
			    if( (entryCstr.getKey().scID==config.sCiD) && (config.AVarSet.contains(tempVarA)) ){	   					
					//add the coefficient for the this.var_z_gamma variable
    				var_z_gamma.put(entry.getKey(),entry.getValue().and(this.master_problem.column(entryCstr.getValue(), 1.0)));		   								   				
			    }
		  }		 				    	
	 }*/
	 //update the CPU cores per node and the capacity for each link
	 //iterate through the set of Z variables
	 for(MpVarZ objZ : var_z_gamma.keySet()){
		 //cleat object z core values
		 objZ.nfvNodeCpuCores.clear();
		 //print out the Z name
		 System.out.println("Z_No." + objZ.cgConfig + "_SC" + objZ.sCiD + "_Config" + objZ.configDsc);
		 //find total traffic for this configuration
		 Double totalTraffic = 0.0;
		 for(TrafficNodes tn :  serviceChainTN.get(objZ.sCiD)){
			 totalTraffic += tn.flow_traffic;
		 }
		 System.out.println("\tTotal traffic = " + totalTraffic);
		 int scID = SCcopyToSC.get(objZ.sCiD);
		 ArrayList<Integer> scVNFs = ChainSet.get(scID).chain_seq;
		 String [] nodes = objZ.configDsc.split("_");
		 /*System.out.println("Printing out ouput of split function!!!");
		 for(int index=0; index<nodes.length; index++){
			 System.out.println("Index " + index + " = " + nodes[index]);
		 }*/
		 //iterae through nodes
		 for(int index=1; index<nodes.length; index++){
			 int vrtID = Integer.valueOf(nodes[index]);
			 System.out.println("\tVertex = " + vrtID);
			 BaseVertex vrt = g.get_vertex(vrtID);
			 int vnfID = scVNFs.get(index-1);
			 //iterate through VNFs
			 for(FuncPt fpt : func_list){
				 if(fpt.getid() == vnfID){
					 System.out.println("\t\tVNF = " + vnfID + " ; CPU Cores = " + totalTraffic*fpt.getcore());
					 if(objZ.nfvNodeCpuCores.get(vrt) != null){
						 Double cpuCores = totalTraffic*fpt.getcore() + objZ.nfvNodeCpuCores.get(vrt);
						 objZ.nfvNodeCpuCores.put(vrt, cpuCores); 
					 }else{
						 objZ.nfvNodeCpuCores.put(vrt, totalTraffic*fpt.getcore()); 
					 }
					 System.out.println("\t\t\tTotal CPU Cores = " +  objZ.nfvNodeCpuCores.get(vrt));
				 }
			 }
			 System.out.println("\t\tFinal CPU Cores = " + objZ.nfvNodeCpuCores.get(vrt));
		 }
	 }
	 //print out CPU cores per node
	 System.out.println("Printing out CPU cores for each node");
	 for(MpVarZ objZ : var_z_gamma.keySet()){
		 System.out.println("Configuration for SC: " + objZ.sCiD);
		 for(Map.Entry<BaseVertex,Double> entry : objZ.nfvNodeCpuCores.entrySet()){
			 System.out.println("\tVertex " + entry.getKey().get_id() + " = " + entry.getValue());
		 }
	 }
	 //include the range for var_z_gamma variables
	 //add the variable to the set of variables
//	 this.configurationCounter = 0;
     for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){
    	String zName = "Z_No." + entry.getKey().cgConfig + "_SC" + entry.getKey().sCiD + "_Config" + entry.getKey().configDsc;
    	this.usedVarZ.put(entry.getKey(), this.master_problem.numVar(entry.getValue(), 0.0, Double.MAX_VALUE, zName));		    	
     }  
     //deallocate var_z_gamma
     var_z_gamma.clear();
     System.out.println("######## Variable Z has been added to RMP model! ##########");
     
     
     
     
   //Columns for variable x_vf
   var_x_vf = new HashMap<MpVarX, IloColumn>();
   //Handles for variable x_vf  
   this.usedVarX = new HashMap<MpVarX,IloNumVar>();
   //vnf replica constraint - (1)
   for(Map.Entry<MpVarX,IloRange> entry : this.vnf_replica_constraint_1.entrySet()){
	   //get VNF Id
	   int vnfId = entry.getKey().f_id;
	   //add column to constraint
	   IloColumn col = this.master_problem.column(entry.getValue(), CountMaxVNF.get(vnfId));
	   //populate the HashMap
	   var_x_vf.put(entry.getKey(),col);
   }
   //vnf replica constraint - (3)
   for(Map.Entry<MpVarX,IloRange> entry : this.vnf_replica_constraint_3.entrySet()){
	   //get the column
	   IloColumn col = var_x_vf.get(entry.getKey());
	   //add the column to the constraint
	   col = col.and(this.master_problem.column(entry.getValue(),-1.0));
	   //populate the HashMap
	   var_x_vf.put(entry.getKey(),col);
   }
   //vnf replica constraint - (2)
   for(int f=0; f<func_list.size(); f++){
	   int fID = func_list.get(f).getid();
	   for(BaseVertex nfviNode : nodesNFVI){
		   //create the key //the object MpVarX
		   MpVarX varX = new MpVarX(nfviNode,fID);
		   //get the column
		   IloColumn col = var_x_vf.get(varX);
		   //add the column to the constraint
		   col = col.and(this.master_problem.column(this.vnf_replica_constraint_2.get(f), 1.0));
		   //populate the HashMap
		   var_x_vf.put(varX,col);
	   }
   }
   //vnf feasibility
  /* for(int f=0; f<func_list.size(); f++){
	   int fID = func_list.get(f).getid();
	   for(BaseVertex nfviNode : nodesNFVI){
		   //create the key //the object MpVarX
		   MpVarX varX = new MpVarX(nfviNode,fID);
		   //get the column
		   IloColumn col = var_x_vf.get(varX);
		   //add the column to the constraint
		   col = col.and(this.master_problem.column(this.vnf_feasibility.get(f), 1.0));
		   //populate the HashMap
		   var_x_vf.put(varX,col);
	   }
   }*/
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
  	 String xName = "X_Nod" + entry.getKey().v.get_id() + "_Vnf" + entry.getKey().f_id;
  	 this.usedVarX.put(entry.getKey(),this.master_problem.numVar(entry.getValue(), 0.0, 1.0, xName));
   }
   //deallocate X variables
   var_x_vf.clear();
   System.out.println("######## Variable X has been added to RMP model! ##########");  
   
   
   
   
   
   
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
   /*for(BaseVertex node : nodesNFVI){
	   if(this.vnf_location_constraint_2.get(node) == null){
		   System.out.println("####### Node does not exist! #######");
	   }else{
		   System.out.println("Node: " + node.get_id() + " = " + this.vnf_location_constraint_2.get(node).toString());
	   }
   }*/
   //VNF location constraint - (2)
   for(BaseVertex node : nodesNFVI){
	   //System.out.println("Node: " + node.get_id() + " = " + this.vnf_location_constraint_2.get(node).toString());
	   //create the key //the object MpVarH
	   MpVarH var = new MpVarH(node);
	   //System.out.println("Node: " + node.get_id() + " = " + this.vnf_location_constraint_2.get(node).toString());
	   //get the column
	   IloColumn col = var_h_v.get(var);
	  /* System.out.println("Column: " + col.toString());
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
  	 this.usedVarH.put(entry.getKey(),this.master_problem.numVar(entry.getValue(), 0.0, 1.0, hName));
   }
   //deallocate H variables
   var_h_v.clear();
   System.out.println("######## Variable H has been added to RMP model! ##########");  
     
     
     
     
   //Columns for variable x_v^ci
   var_x_civ = new HashMap<MpVarXc, IloColumn>();
   //column for replica y
// IloColumn replicaY = this.master_problem.column(this.BwUsed, InputConstants.Big_M);
   //Handles for variable x_v^ci
   this.usedVarXc = new HashMap<MpVarXc,IloNumVar>();
   //configuration selection constraint - 1 (7)		      
   for(Map.Entry<NwMpCstr7and8, IloRange> entryCstr : this.rel_place_conf_constraint_1.entrySet()){      
	   //create the key //the object MpVarX
	   MpVarXc temp_elem = new MpVarXc(entryCstr.getKey().scID, entryCstr.getKey().nfviNode, entryCstr.getKey().fSeq) ;
	   //add the column to the constraint
	   IloColumn col = this.master_problem.column(entryCstr.getValue(), -1.0);				 
	   //populate the HashMap
	   var_x_civ.put(temp_elem, col);				  
   }
   //configuration selection constraint - 2 (8)	
   /*for(Map.Entry<NwMpCstr7and8, IloRange> entryCstr : this.rel_place_conf_constraint_2.entrySet()){	   
	  //create the key // the object MpVarX
	  MpVarXc temp_elem = new MpVarXc(entryCstr.getKey().scID, entryCstr.getKey().nfviNode, entryCstr.getKey().fSeq);
	  //get the column
	  IloColumn col = var_x_civ.get(temp_elem);	
	  //add the column to the constraint				
	  col = col.and(this.master_problem.column(entryCstr.getValue(), -InputConstants.Big_M));
	  //populate the HashMap
	  var_x_civ.put(temp_elem, col);	 
   }*/
   //vnf replica constraint - (1)
   for(Map.Entry<MpVarX,IloRange> entry : this.vnf_replica_constraint_1.entrySet()){
	   int fID = entry.getKey().f_id;
	   BaseVertex nfviNode = entry.getKey().v;
	   for(int scID : scUsed){
		 ServiceChain scTemp = ChainSet.get(scID);
		 for(int scCopyID : scCopies.get(scID)){
			 for(int f_seq=0; f_seq<scTemp.chain_size; f_seq++){
				 int tempID=scTemp.chain_seq.get(f_seq);
				 //the temp ID matches with VNF ID
				 if(fID==tempID){			
					  //create the key // the object MpVarX
					  MpVarXc temp_elem = new MpVarXc(scCopyID, nfviNode, f_seq);
					  //get the column
					  IloColumn col = var_x_civ.get(temp_elem);	
					  //add the column to the constraint				
					  col = col.and(this.master_problem.column(entry.getValue(), -1.0));
					  //populate the HashMap
					  var_x_civ.put(temp_elem, col);					 
				 }
			 }
		 }
	   }
   }
   //vnf replica constraint - (3)
   for(Map.Entry<MpVarX,IloRange> entry : this.vnf_replica_constraint_3.entrySet()){
	   int fID = entry.getKey().f_id;
	   BaseVertex nfviNode = entry.getKey().v;
	   for(int scID : scUsed){
		 ServiceChain scTemp = ChainSet.get(scID);
		 for(int scCopyID : scCopies.get(scID)){
			 for(int f_seq=0; f_seq<scTemp.chain_size; f_seq++){
				 int tempID=scTemp.chain_seq.get(f_seq);
				 //the temp ID matches with VNF ID
				 if(fID==tempID){			
					  //create the key // the object MpVarX
					  MpVarXc temp_elem = new MpVarXc(scCopyID, nfviNode, f_seq);
					  //get the column
					  IloColumn col = var_x_civ.get(temp_elem);	
					  //add the column to the constraint				
					  col = col.and(this.master_problem.column(entry.getValue(), 1.0));
					  //populate the HashMap
					  var_x_civ.put(temp_elem, col);					 
				 }
			 }
		 }
	   }
   }
   //vnf replica constraint - (2)
//   for(int f=0; f<func_list.size(); f++){
//	   int fID = func_list.get(f).getid();
//	   for(int scID : scUsed){
//		 ServiceChain scTemp = ChainSet.get(scID);
//		 for(int scCopyID : scCopies.get(scID)){
//			 for(int f_seq=0; f_seq<scTemp.chain_size; f_seq++){
//				 int tempID=scTemp.chain_seq.get(f_seq);
//				 //the temp ID matches with VNF ID
//				 if(fID==tempID){
//					 for(BaseVertex nfviNode : nodesNFVI){
//						 //create the key // the object MpVarX
//						  MpVarXc temp_elem = new MpVarXc(scCopyID, nfviNode, f_seq);
//						  //get the column
//						  IloColumn col = var_x_civ.get(temp_elem);	
//						  //add the column to the constraint				
//						  col = col.and(this.master_problem.column(this.vnf_replica_constraint_2.get(f), 1.0));
//						  //populate the HashMap
//						  var_x_civ.put(temp_elem, col);
//					 }
//				 }
//			 }
//		 }
//	   }
//   }
   //function replica constraint (4)
   //adding columns for x_vf to this constraint	
//   for(int f=0; f<func_list.size(); f++){
//  	  //iterate through the list of vnf_nodes
//		  for(int vnf_node_index=0; vnf_node_index < nodesNFVI.size(); vnf_node_index++){
//			  //create the key // the object MpVarX
//			  MpVarX temp_elem = new MpVarX( nfv_nodes.get(vnf_node_index), func_list.get(f).getid() );	
//			  //add the column to the constraint
//			  IloColumn col = this.var_x_vf.get(temp_elem);
//			  //add the column to the constraint //modify the object
//			  col = col.and(this.master_problem.column(replica_constraint.get(f),1.0));
//			  //add the replica y to the same constraint
////			  replicaY = replicaY.and(this.master_problem.column(replica_constraint.get(f),-1.0));
//			  //update the HashMap
//			  this.var_x_vf.put(temp_elem, col);
//		  }		 	    	
//	 }	
   //add replicaY to the list of variables of the master problem
//   this.master_problem.numVar(replicaY, 0.0, Double.MAX_VALUE, "replicaY");
   //source outgoing link constraint (9)
   //adding columns for x_vf to this constraint
   for(int scID : scUsed){
	 for(int scCopyID : scCopies.get(scID)){
		 for(Map.Entry<MpCstr7and11,IloRange> entry : this.src_outgoing_constraint.entrySet()){
  		 	//traffic node
	    	TrafficNodes tn = entry.getKey().tn;
	    	//all the traffic nodes that use that service chain
	    	if(tn.chain_index == scCopyID){
		    	BaseVertex v = tn.v1;//Vertex where the function is placed
		    	int f_seq_index_in_C = 0;//sequence index of the first function in the service chain		    	
		    	MpVarXc temp_elem = new MpVarXc(scCopyID, v, f_seq_index_in_C);		    	
		    	//check if source node is a VNF capable node
		    	if(nodesNFVI.contains(tn.v1)){
	    		    //get the column object 
		    	 	IloColumn col = var_x_civ.get(temp_elem);
		    	 	//add the column to the constraint //modify the object
		    		col = col.and(this.master_problem.column(entry.getValue(),1.0));
		    		//update the HashMap
		    		var_x_civ.put(temp_elem, col);
		     	}
	    	}
	     }
	   }
   }
   //source incoming link constraint (10)
   for(int scID : scUsed){
 	  for(int scCopyID : scCopies.get(scID)){
 	    for(Map.Entry<MpCstr81012and14,IloRange> entry : this.src_incoming_constraint.entrySet()){
 	    	if(entry.getKey().chainIndex == scCopyID){
 	    		//sequence index of the first function in the service chain
 	    		int f_seq_index_in_C = 0; 
 		    	//create an object
 		    	MpVarXc temp_elem = new MpVarXc(scCopyID, entry.getKey().nfvi_node,f_seq_index_in_C);	
 		    	//get the column object 
 		    	IloColumn col = var_x_civ.get(temp_elem);
 		    	//check if key exists
 		    	if(col != null){
 			    	 //add the column to the constraint //modify the object
 			    	 col = col.and(this.master_problem.column(entry.getValue(), -1.0));
 			    	 //update the HashMap
 			    	 var_x_civ.put(temp_elem, col);
 		    	}
 		    }
 	    }
 	  }
   }	
 //flow conservation constraint - placement - ingress node (11)
   for(int scID : scUsed){ 
 	  for(int scCopyID : scCopies.get(scID)){
 	    for(Map.Entry<MpCstr81012and14,IloRange> entry : this.flow_place_ing_constraint.entrySet()){
 	    	if(entry.getKey().chainIndex == scCopyID){
 		    	 //sequence index of the first function in the service chain
 	    		 int f_seq_index_in_C = 0; 
 		    	 //create an object
 		    	 MpVarXc temp_elem = new MpVarXc(scCopyID, entry.getKey().nfvi_node,f_seq_index_in_C);		    	
 		    	 //get the column object 
 		    	 IloColumn col = var_x_civ.get(temp_elem);
 		    	 //check if key exists
 		    	 if(col != null){
 			    	 //add the column to the constraint //modify the object
 			    	 col = col.and(this.master_problem.column(entry.getValue(),1.0));
 			    	 //update the HashMap
 			    	 var_x_civ.put(temp_elem, col);
 		    	 }
 	    	}
 	    }
 	  }
   }
   //destination incoming link constraint (13)
   for(int scID : scUsed){
 	  for(int scCopyID : scCopies.get(scID)){
 	    for(Map.Entry<MpCstr7and11,IloRange> entry : this.dest_incoming_constraint.entrySet()){
 	    	if(entry.getKey().chainIndex == scCopyID){
 	    		 //Traffic Node
 	    		 TrafficNodes tn = entry.getKey().tn;
 		    	 //Service chain
 		    	 ServiceChain sc = ChainSet.get(scID);
 		    	 //sequence index of the last function in the service chain	
 			     int f_seq_index_in_C = sc.chain_size-1;
 		    	 //check if destination node is an NFV node
 		    	 if(nodesNFVI.contains(tn.v2)){
 			    	 //create an object
 			    	 MpVarXc temp_elem = new MpVarXc(scCopyID, tn.v2,f_seq_index_in_C);
 			    	 //get the column object 
 			    	 IloColumn col = var_x_civ.get(temp_elem);
 			    	 //check if key exists
 			    	 if(col != null){
 				    	 //add the column to the constraint //modify the object
 				    	 col = col.and(this.master_problem.column(entry.getValue(), 1.0));
 				    	 //update the HashMap
 				    	 var_x_civ.put(temp_elem, col);
 			    	 }
 		    	 }
 		    }
 	    }
 	  }
   }	
   //destination outgoing link constraint (14)
   for(int scID : scUsed){
	   for(int scCopyID : scCopies.get(scID)){
	     for(Map.Entry<MpCstr81012and14,IloRange> entry : this.dest_outgoing_constraint.entrySet()){
	     	if(entry.getKey().chainIndex == scCopyID){		    		
		    	ServiceChain sc = ChainSet.get(scID);
		        //sequence index of the last function in the service chain	
		    	int f_seq_index_in_C = sc.chain_size-1;
		    	//create an object
		    	MpVarXc temp_elem = new MpVarXc(scCopyID, entry.getKey().nfvi_node, f_seq_index_in_C);
		    	//get the column object 
		    	IloColumn col = var_x_civ.get(temp_elem);
		    	//check if destination node is a VNF capable node
		    	if(col != null){
			    	 //add the column to the constraint //modify the object
			    	 col = col.and(this.master_problem.column(entry.getValue(), -1.0));
			    	 //update the HashMap
			    	 var_x_civ.put(temp_elem, col);
		     	}
		    }				 
	     }
	   }
	}	    
	  //flow conservation constraint - placement - egress node (15)
	  for(int scID : scUsed){
		  for(int scCopyID : scCopies.get(scID)){
		    for(Map.Entry<MpCstr81012and14,IloRange> entry : this.flow_place_egr_constraint.entrySet()){
		    	if(entry.getKey().chainIndex == scCopyID){
			    	 //service chain
			    	 ServiceChain sc = ChainSet.get(scID);
			    	 //function identifier for the last function in the service chain
	//		    	 int f_id = sc.chain_seq.get(sc.chain_seq.size() - 1);
			    	 //sequence index of the last function in the service chain	
				     int f_seq_index_in_C = sc.chain_size-1;
			    	 //create an object
			    	 MpVarXc temp_elem = new MpVarXc(scCopyID, entry.getKey().nfvi_node,f_seq_index_in_C);	
			    	 //get the column object 
			    	 IloColumn col = var_x_civ.get(temp_elem);
			    	 //check if key exists
			    	 if(col != null){
				    	 //add the column to the constraint //modify the object
				    	 col = col.and(this.master_problem.column(entry.getValue(), -1.0));
				    	 //update the HashMap
				    	 var_x_civ.put(temp_elem, col);
			    	 }
			    }	 
		    }
		  }
	  }		   
	  //add the variable
	  //include the range for var_x_vf variables
	  for(Map.Entry<MpVarXc, IloColumn> entry : var_x_civ.entrySet()){
	  	String xName = "X_SC" + entry.getKey().scID + "_Nod" + entry.getKey().v.get_id() + "_VnfSQ" + entry.getKey().f_seq;
	  	this.usedVarXc.put(entry.getKey(),this.master_problem.numVar(entry.getValue(), 0.0, 1.0, xName));
	  }
	  //deallocate X variables
	  var_x_civ.clear();
	  System.out.println("######## Variable Xc has been added to RMP model! ##########");
	
	
	
	//Columns for var_y_l_sigma_sd
	var_y_l_sigma_sd = new HashMap<MpVarY, IloColumn>();
	//Handles for var_y_l_sigma_sd
	this.usedVarY = new HashMap<MpVarY, IloNumVar>();
	//including the columns for the objective - (2)
	//iterating over the set of service chains
	for(int scID : scUsed){
		for(int scCopyID : scCopies.get(scID)){
			//iterating over the set of traffic nodes and their associated service chain (c)
			for(TrafficNodes tn : serviceChainTN.get(scCopyID)){	    	
		   		//function ID for first function in the service chain
		   		int firstVNF = 0;
		   		//function ID for last function in the service chain
		   		int lastVNF = ChainSet.get(scID).chain_size-1;
				for(BaseVertex s_vert : g._vertex_list){ 
					for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){ 						
						//add the column for var_y_l_sigma1_sd
						MpVarY temp_elem_1 = new MpVarY(tn, firstVNF, s_vert, t_vert);
						//the column
						IloColumn col1 = this.master_problem.column( this.BwUsed, tn.flow_traffic);
		   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
						var_y_l_sigma_sd.put(temp_elem_1, col1);
		   		        //add the column for var_y_l_sigmaN_sd
						MpVarY temp_elem_N = new MpVarY(tn, lastVNF, s_vert, t_vert);
						//the column
						IloColumn colN = this.master_problem.column( this.BwUsed, tn.flow_traffic);
		   				//coefficient for var_y_l_sigmaN_sd is the bandwidth 
						var_y_l_sigma_sd.put(temp_elem_N, colN);						
				 	} 
				}   				
			}
		}		
	}
//	System.out.println("\tY variables added to objective (2)");
	//source outgoing link constraint - (9)	
	for(int scID : scUsed){
		for(int scCopyID : scCopies.get(scID)){
		    for(Map.Entry<MpCstr7and11,IloRange> entry : this.src_outgoing_constraint.entrySet()){	   	
		        //traffic nodes
		    	TrafficNodes tn = entry.getKey().tn;
		        //for all the traffic nodes using that service chain
		    	if(tn.chain_index == scCopyID){
			    	//function ID for first function in the service chain
			    	int firstVNF = 0;			    
	  				//iterating over all the links   				
					for(BaseVertex t_vert : g.get_adjacent_vertices(tn.v1)){
					    //add the column for var_y_l_sigma1_sd
						MpVarY temp_elem_1 = new MpVarY(tn, firstVNF, tn.v1, t_vert);					
						//add the column for var_y_l_sigma1_sd
						IloColumn col = var_y_l_sigma_sd.get(temp_elem_1);
					    //check if key exists
			    	    if(col != null){
	  		   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
	  		   				col = col.and(this.master_problem.column( entry.getValue(), 1.0));		   		   		        	
	  		   				//update the HashMap
	  		   			    var_y_l_sigma_sd.put(temp_elem_1, col);
			    	    }
			    	    else{
			    	    	//print out key
				    		/*System.out.println("SD pair: (" + tn.v1.get_id() + ", "+ tn.v2.get_id() + "); Link: (" + tn.v1.get_id() + ", "+ t_vert.get_id() + "); VNF: " + firstVNF);
			    	    	System.out.println("the column was empty : ");*/
			    	    }
					}
		    	}
		    }	    
		}
	}
//	System.out.println("\tY variables added to constraint (9)");
   //flow conservation constraint - placement - ingress node (11)	
   for(int scID : scUsed){ 
	   for(int scCopyID : scCopies.get(scID)){
	    for(Map.Entry<MpCstr81012and14,IloRange> entry : this.flow_place_ing_constraint.entrySet()){	
	         if(entry.getKey().chainIndex == scCopyID){ 		    
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
		   				col = col.and(this.master_problem.column(entry.getValue(), -1.0));		   		   		        	
		   				//update the HashMap
		   			    var_y_l_sigma_sd.put(temp_elem, col);
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
		   				col = col.and(this.master_problem.column(entry.getValue(), 1.0));		   		   		        	
		   				//update the HashMap
		   			    var_y_l_sigma_sd.put(temp_elem, col);
			    	}				    	
		    	 }
		    }
		     
	    }
	   }
	}
//   System.out.println("\tY variables added to constraint (11)");
   //flow conservation constraint - no placement - ingress node (12)
   for(int scID : scUsed){	   
	  for(int scCopyID : scCopies.get(scID)){
		for(Map.Entry<MpCstr9and13,IloRange> entry : this.flow_noplace_ing_constraint.entrySet()){	
			if(entry.getKey().chainIndex == scCopyID){		    	
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
		   				col = col.and(this.master_problem.column(entry.getValue(), 1.0));		   		   		        	
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
		   				col = col.and(this.master_problem.column(entry.getValue(), -1.0));		   		   		        	
		   				//update the HashMap
		   			    var_y_l_sigma_sd.put(temp_elem, col);
			    	}
	    		}
			} 				     
		}
	  }
	}
//   System.out.println("\tY variables added to constraint (12)");
   //source incoming link constraint (10)
   for(int scID : scUsed){
	 for(int scCopyID : scCopies.get(scID)){
	    for(Map.Entry<MpCstr81012and14,IloRange> entry : this.src_incoming_constraint.entrySet()){
	        if(entry.getKey().chainIndex == scCopyID){			    	
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
		   				col = col.and(this.master_problem.column(entry.getValue(), 1.0));		   		   		        	
		   				//update the HashMap
		   			    var_y_l_sigma_sd.put(temp_elem, col);
			    	}
		    	 }
		    }			     
	    }
	 }
   }
//   System.out.println("\tY variables added to constraint (10)");
   //destination incoming link constraint (13)	
   for(int scID : scUsed){
	  for(int scCopyID : scCopies.get(scID)){ 
	    for(Map.Entry<MpCstr7and11,IloRange> entry : this.dest_incoming_constraint.entrySet()){
	    	if(entry.getKey().chainIndex == scCopyID){			    	
		    	 TrafficNodes tn = entry.getKey().tn;		    	
		    	 int lastVNF = ChainSet.get(scID).chain_size-1;				    	    		 
	    		 //outgoing links
		    	 for(BaseVertex s_vrt : g.get_precedent_vertices(tn.v2)){
		    		//add the column for var_y_l_sigma1_sd
						MpVarY temp_elem = new MpVarY(tn, lastVNF, s_vrt, tn.v2);
						//add the column for var_y_l_sigma1_sd
						IloColumn col = var_y_l_sigma_sd.get(temp_elem);
						//check if key exists
				    	if(col != null){
			   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
			   				col = col.and(this.master_problem.column(entry.getValue(), 1.0));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);
				    	}
		    	 }
		    }				     			    	 
	    }
	  }
   }
//   System.out.println("\tY variables added to constraint (13)");
   int count15 = 0;
   //flow conservation constraint - placement - egress node - outgoing links (15)	 
   for(int scID : scUsed){
	 for(int scCopyID : scCopies.get(scID)){ 
	    for(Map.Entry<MpCstr81012and14,IloRange> entry : this.flow_place_egr_constraint.entrySet()){
	    	if(entry.getKey().chainIndex == scCopyID){	    		
		    	 //SD pair
  			     TrafficNodes tn = entry.getKey().tn; // the traffic nodes
  			     //function ID for first function in the service chain
  			     int lastVNF = ChainSet.get(scID).chain_size-1;	
		    	 BaseVertex nfvi_node = entry.getKey().nfvi_node;
		    	/* System.out.println("\t\t"+scCopyID+", ("+tn.v1.get_id()+","+tn.v2.get_id()+"), "+nfvi_node.get_id());
		    	 System.out.print("\t\t\tPrecedent vertices: ");*/
	    		 //incoming links
		    	 for(BaseVertex s_vrt : g.get_precedent_vertices(nfvi_node)){
		    		//System.out.print(s_vrt.get_id()+","); 
	    		    //add the column for var_y_l_sigma1_sd
					MpVarY temp_elem = new MpVarY(tn, lastVNF, s_vrt, nfvi_node);	
					//add the column for var_y_l_sigma1_sd
					IloColumn col = var_y_l_sigma_sd.get(temp_elem);
					//check if key exists
			    	if(col != null){
		   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
		   				col = col.and(this.master_problem.column(entry.getValue(), -1.0));		   		   		        	
		   				//update the HashMap
		   			    var_y_l_sigma_sd.put(temp_elem, col);
			    	}
		    	 }
		    	 /*System.out.print("\n"); 
		    	 System.out.print("\t\t\tAdjacent vertices: ");*/
		    	 //outgoing links
		    	 for(BaseVertex t_vrt : g.get_adjacent_vertices(nfvi_node)){
		    		 //System.out.print(t_vrt.get_id()+","); 
		    		//add the column for var_y_l_sigma1_sd
					MpVarY temp_elem = new MpVarY(tn, lastVNF, nfvi_node, t_vrt);
					//add the column for var_y_l_sigma1_sd
					IloColumn col = var_y_l_sigma_sd.get(temp_elem);
					//check if key exists
			    	if(col != null){
		   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
		   				col = col.and(this.master_problem.column(entry.getValue(), 1.0));		   		   		        	
		   				//update the HashMap
		   			    var_y_l_sigma_sd.put(temp_elem, col);
			    	}
		    	 }
		    	 //System.out.print("\n"); 
		    }
	    	//increment the constraint number
	    	count15++;
	    }
	 }
   }
//   System.out.println("\tY variables added to constraint (15)");
   //flow conservation constraint - no placement - egress node (16)
   for(int scID : scUsed){
	  for(int scCopyID : scCopies.get(scID)){ 
	    for(Map.Entry<MpCstr9and13,IloRange> entry : this.flow_nplace_egr_constraint.entrySet()){
	    	if(entry.getKey().chainIndex == scCopyID){			    	
		    	 TrafficNodes tn = entry.getKey().tn; //traffic nodes
		    	 //function ID of the first function in the service chain
		    	 int lastVNF = ChainSet.get(scID).chain_size-1;	
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
		   				col = col.and(this.master_problem.column(entry.getValue(), 1.0));		   		   		        	
		   				//update the HashMap
		   			    var_y_l_sigma_sd.put(temp_elem, col);
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
		   				col = col.and(this.master_problem.column(entry.getValue(), -1.0));		   		   		        	
		   				//update the HashMap
		   			    var_y_l_sigma_sd.put(temp_elem, col);
			    	}
	    		 }
			 }				     
	    }
	  }
   }	
//   System.out.println("\tY variables added to constraint (16)");
   if(capCstr){
		//flow capacity constraint	(6)	    	
		//declare the counter
		int lnum = 0;	
	    for(BaseVertex s_vert : g._vertex_list){ 
			for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){									
			    //iterating over the set of traffic nodes and their associated service chain (c)
				for(int scID : scUsed){
					//function ID for first function in the service chain
		    		int firstVNF = 0;
		    		//function ID for last function in the service chain
		    		int lastVNF = ChainSet.get(scID).chain_size-1; 
					for(int scCopyID : scCopies.get(scID)){
						//iterate over the traffic nodes that use that traffic chain
						for(TrafficNodes tn : serviceChainTN.get(scCopyID)){	    	
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
	//			    	    	System.out.println("Y_firstVNF variable not added in link constraints");
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
	//							System.out.println("Y_lastVNF variable not added in link constraints");
								}					    
				    	}
					}
				}
				//increment the link_num
				lnum++;
			} 
		 }
  }
//  System.out.println("\tY variables added to constraint (6)");
   //destination incoming link constraint (14)
   for(int scID : scUsed){
	   //function ID of the first function in the service chain
   	   int lastVNF = ChainSet.get(scID).chain_size-1;
	   for(int scCopyID : scCopies.get(scID)){
	     for(Map.Entry<MpCstr81012and14,IloRange> entry : this.dest_outgoing_constraint.entrySet()){
	     	if(entry.getKey().chainIndex == scCopyID){
	    	 	TrafficNodes tn = entry.getKey().tn;		    	 
		    	//node at which the VNF has been placed
		    	BaseVertex nfviNode = entry.getKey().nfvi_node;
			   	//iterating over the outgoing links from the last VNF placement
		    	//these links will take you to the destination
//		    	System.out.println("\t\t"+scCopyID+", ("+tn.v1.get_id()+","+tn.v2.get_id()+"), "+nfviNode.get_id());
//		    	System.out.print("\t\t\tAdjacent vertices: ");
		    	for(BaseVertex t_vert :  g.get_adjacent_vertices(nfviNode)){
		    		//System.out.print(t_vert.get_id()+",");
				    //add the column for var_y_l_sigma1_sd
					MpVarY temp_elem_N = new MpVarY(tn, lastVNF, nfviNode, t_vert);	   					
					//add the column for var_y_l_sigma1_sd
					IloColumn col = var_y_l_sigma_sd.get(temp_elem_N);
				    //check if key exists
		    	    if(col != null){
  		   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
  		   				col = col.and(this.master_problem.column(entry.getValue(), 1.0));		   		   		        	
  		   				//update the HashMap
  		   			    var_y_l_sigma_sd.put(temp_elem_N, col);  		   			    
		    	    }		   					
				}
//		    	System.out.print("\n"); 
		    }
	     }
    	}
	}   
//    System.out.println("\tY variables added to constraint (14)");
    
    //check if there are any source and destination are same on the links
   /* for(MpVarY objY : var_y_l_sigma_sd.keySet()){
    	if(objY.s_vrt.get_id()==objY.t_vrt.get_id()){
    		System.out.println("\t\tY_SC"+ objY.tn.chain_index + "_Ind" + objY.f_id + "_Src" +  objY.tn.v1.get_id() + "_Dst" + objY.tn.v2.get_id() + "_Ls" + objY.s_vrt.get_id() + "_Ld" + objY.t_vrt.get_id() + " is wrong!");
    	}
    }*/
   
    //add the variable
	//include the range for var_y_l_sigma_sd variables
	for(Map.Entry<MpVarY, IloColumn> entry : var_y_l_sigma_sd.entrySet()){
		String yName = "Y_SC"+ entry.getKey().tn.chain_index + "_Ind" + entry.getKey().f_id + "_Src" +  entry.getKey().tn.v1.get_id() + "_Dst" + entry.getKey().tn.v2.get_id() + "_Ls" + entry.getKey().s_vrt.get_id() + "_Ld" + entry.getKey().t_vrt.get_id();
		this.usedVarY.put(entry.getKey(), this.master_problem.numVar(entry.getValue(),0.0,1.0,yName));
	}
	//deallocate Y variables
	var_y_l_sigma_sd.clear();
	System.out.println("######## Variable Y has been added to RMP model! ##########");
   }


	
	//create the Master Problem Object
	//remembering previous columns
	public MasterProblem(boolean coreCstr, boolean capCstr, Map<BaseVertex,Double> cpuCoreCount, Map<NodePair,Double> linkCapacity,
				List<FuncPt> func_list, List<Integer> scUsed,  Map<Integer,ArrayList<TrafficNodes>> serviceChainTN, Map<Integer,ArrayList<HuerVarZ>> configsPerSC, 
				Map<TrafficNodes,SdDetails> configPerSD, Graph g, ArrayList<BaseVertex> nodesNFVI, ArrayList<BaseVertex> nfv_nodes, 
				Map<Integer, ServiceChain> ChainSet, List<TrafficNodes> pair_list, ArrayList<BaseVertex> vertex_list_without_nfvi_nodes,  
				Map<Integer,ArrayList<Integer>> scCopies,  Map<Integer,Integer> SCcopyToSC, Map<Integer,Integer> CountMaxVNF, Map<Integer,Integer> replicaPerVNF,
				Integer kNodeCount,  Map<Integer, HashSet<HuerVarZ>> generatedColumns) throws Exception{
			
			
			
			 //Columns for variable z_gamma			   
		     Map<MpVarZ,IloColumn> var_z_gamma;
		     //Columns for variable x_vf
		     Map<MpVarX, IloColumn> var_x_vf;
		     //Columns for variable h_v
		     Map<MpVarH, IloColumn> var_h_v;
		     //Columns for variable x_v^ci
		     Map<MpVarXc, IloColumn> var_x_civ;
		     //Columns for var_y_l_sigma_sd
			 Map<MpVarY, IloColumn> var_y_l_sigma_sd;
			
			
			 //model for the master problem
		     this.master_problem = new IloCplex();
		     //objective for master problem
		     //trying to minimize the bandwidth used in routing the service requests
		     //(2) Objective for the master problem
		     this.BwUsed = this.master_problem.addMinimize(); 	   
		     //returns the LP Matrix associated with the master problem
			 IloLPMatrix matrixDummyColumns = this.master_problem.addLPMatrix();	
		     
			 		 
		     int cstrNum=0;//reset the constraint no. counter 
		     //Add the RANGED CONSTRAINTS
		 	 //configuration selection constraint(3)		
		     this.config_selection_constraint = new HashMap<Integer,IloRange>();
		     for(int scID : scUsed){
		    	 for(int scCopyID : scCopies.get(scID)){
			    	 String constraint = "cstr3" + "_" + cstrNum;
			    	 IloRange rng = this.master_problem.addRange(1.0, 1.0, constraint);	
			    	 cstrNum++;	    	
			    	 //keep track of constraint
			    	 //the chain number corresponds to the service chain ID
			    	 this.config_selection_constraint.put( scCopyID, rng);
			    	 //add the range to the matrix dummy column
				     matrixDummyColumns.addRow(rng);
		    	 }
		     }
		     System.out.println("Constraint (3) generated!");
		     /*cstrNum=0;//reset the constraint no. counter
		     //configuration constraints (4)
		     this.place_config_constraint = new ArrayList<IloRange>();
		     for(int f=0; f<func_list.size(); f++){
		    	String constraint = "cstr4" + "_" + cstrNum;
		    	IloRange rng = this.master_problem.addRange(1.0, Double.MAX_VALUE, constraint);
		    	cstrNum++;
		    	//keep track of constraint
		    	this.place_config_constraint.add(rng);
		    	//add the range to the matrix dummy column
		    	matrixDummyColumns.addRow(rng);
		     }	
		     System.out.println("Constraint (4) generated!");*/
		     cstrNum=0;//reset the constraint no. counter
		     //core capacity constraint	(5)		  
		     this.core_capacity_constraint = new ArrayList<IloRange>();
		     for(int vnf_node=0; vnf_node < nfv_nodes.size(); vnf_node++){
		    	BaseVertex nfvNode = nfv_nodes.get(vnf_node);
		    	double coreCount = 0.0;
		    	if(cpuCoreCount.get(nfvNode) != null){
		    		coreCount = cpuCoreCount.get(nfvNode);
		    	}
		    	String constraint = "cstr5" + "_" + cstrNum;
		    	IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, coreCount, constraint);
		    	cstrNum++;
		    	//keep track of constraint
		    	this.core_capacity_constraint.add(rng);
		    	//add dummy variable if core capacity constraint is imposed
		    	if(coreCstr){
			    	//add the range to the matrix dummy column
			    	matrixDummyColumns.addRow(rng);
		    	}
		     }
		     System.out.println("Constraint (5) generated!");
		     //flow capacity constraint (6)
		     this.flow_capacity_constraint = new ArrayList<IloRange>();
		     int link_num = 0;
		     for(BaseVertex s_vert : g._vertex_list){ 
				for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){
					NodePair link = new NodePair(s_vert,t_vert);
					double linkBw = 0.0;
					if(linkCapacity.get(link)!=null){
						linkBw = linkCapacity.get(link);
					}
					String constraint = "cstr6" + "_" + s_vert.get_id() + "_" + t_vert.get_id();
					IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, linkBw, constraint);				
					//keep track of constraint
					this.flow_capacity_constraint.add(link_num,  rng);
					//add dummy variable if the capacity constraint is enforced
					if(capCstr){
						//add the range to the matrix dummy column
				    	matrixDummyColumns.addRow(rng);
					}
					//increment the link number
					link_num++;
				} 					
			 }
		     System.out.println("Constraint (6) generated!");
		     cstrNum=0;//reset the constraint no. counter
		     //function placement relating to configuration selection constraint - 1 (7)
		     this.rel_place_conf_constraint_1 = new HashMap<NwMpCstr7and8,IloRange>();
		     for(int scID : scUsed){
		    	 for(int scCopyID : scCopies.get(scID)){
			    	 for(int f_in_C=0; f_in_C < ChainSet.get(scID).chain_seq.size(); f_in_C++ ){	    		
			    		 for(BaseVertex nfviNode : nodesNFVI){	    		
				    		 String constraint = "cstr7" + "_" + cstrNum;
//				    		 IloRange rng = this.master_problem.addRange(0.0, Double.MAX_VALUE, constraint);
				    		 IloRange rng = this.master_problem.addRange(0.0, 0.0, constraint);
				    		 cstrNum++;
				    		 //create the constraint index object
				    		 NwMpCstr7and8 cstr7 = new NwMpCstr7and8(scCopyID, f_in_C, nfviNode);
				    		 //keep track of constraint
				    		 this.rel_place_conf_constraint_1.put(cstr7, rng );
				    		 //add the range to the matrix dummy column
						     matrixDummyColumns.addRow(rng);
			    		 }
				     }	
		    	 }
			 }
		     System.out.println("Constraint (7) generated!");	 
		     /*cstrNum=0;//reset the constraint no. counter
		     //function placement relating to configuration selection constraint - 2 (8)
		     this.rel_place_conf_constraint_2 = new HashMap<NwMpCstr7and8,IloRange>();
		     for(int scID : scUsed){
		    	 for(int scCopyID : scCopies.get(scID)){
			    	 for(int f_in_C=0; f_in_C < ChainSet.get(scID).chain_seq.size(); f_in_C++ ){	    	
			    		 for(BaseVertex nfviNode : nodesNFVI){	    		 
				    		 String constraint = "cstr8" + "_" + cstrNum;
				    		 IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, 0.0, constraint);
				    		 cstrNum++;
				    		 //create the constraint index object
				    		 NwMpCstr7and8 cstr8 = new NwMpCstr7and8(scCopyID, f_in_C, nfviNode);
				    		 //create the constraint
				    		 this.rel_place_conf_constraint_2.put(cstr8, rng);
				    		 //add the range to the matrix dummy column
						     matrixDummyColumns.addRow(rng);
			    		 }
				     }
		    	 }
			 }
		     System.out.println("Constraint (8) generated!");*/	 
		     //VNF replica constraints
		     cstrNum=0;//reset the constraint no. counter
		     this.vnf_replica_constraint_1 = new HashMap<MpVarX,IloRange>();
		     for(BaseVertex nfviNode : nodesNFVI){
		    	 for(FuncPt fpt : func_list){
		    		 MpVarX tempCstr = new MpVarX(nfviNode,fpt.getid());
		    		 String constraint = "cstrVnfReplica1_" + cstrNum;
		    		 IloRange rng = this.master_problem.addRange(0.0, Double.MAX_VALUE, constraint);
		    		 cstrNum++;
		    		 //keep track of constraint
		    		 this.vnf_replica_constraint_1.put(tempCstr,rng);
		    	 }
		     }
		     System.out.println("##### Constraints for vnf replica constraint 1 generated! #####");
		     cstrNum=0;//reset the constraint no. counter
		     this.vnf_replica_constraint_2 = new ArrayList<IloRange>();
		     for(int f=0; f<func_list.size(); f++){
		    	int fID = func_list.get(f).getid();
		    	String constraint = "vnfID" + fID + "_cstrVnfReplica2_" + "_" + cstrNum;
		    	IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, replicaPerVNF.get(fID), constraint);
		    	cstrNum++;
		    	//keep track of constraint
		    	this.vnf_replica_constraint_2.add(rng);	    	
		     }
		     System.out.println("##### Constraints for vnf replica constraint 2 generated! #####");
		     cstrNum=0;//reset the constraint no. counter
		     this.vnf_replica_constraint_3 = new HashMap<MpVarX,IloRange>();
		     for(BaseVertex nfviNode : nodesNFVI){
		    	 for(FuncPt fpt : func_list){
		    		 MpVarX tempCstr = new MpVarX(nfviNode,fpt.getid());
		    		 String constraint = "cstrVnfReplica3_" + cstrNum;
		    		 IloRange rng = this.master_problem.addRange(0.0, Double.MAX_VALUE, constraint);
		    		 cstrNum++;
		    		 //keep track of constraint
		    		 this.vnf_replica_constraint_3.put(tempCstr,rng);
		    	 }
		     }
		     System.out.println("##### Constraints for vnf replica constraint 3 generated! #####");
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
		    /* for(Map.Entry<BaseVertex,IloRange> entry : this.vnf_location_constraint_2.entrySet()){
		    	 System.out.println("Node: " + entry.getKey().get_id() + ", Range:" + entry.getValue().toString());
		     }*/
		     System.out.println("##### Constraints for vnf location constraint 2 generated! #####");
		     //location constraint 3
		     this.vnf_location_constraint_3 = this.master_problem.addRange(-Double.MAX_VALUE,kNodeCount,"cstrVnfLocation3");
		     System.out.println("##### Constraints for vnf location constraint 3 generated! #####");
		     
		     
		     
		     /*cstrNum=0;//reset the constraint no. counter
		     //function replica constraint (4)
		     //changed the replica constraint!!!!!
		     ArrayList<IloRange> replica_constraint = new ArrayList<IloRange>();
		     for(int f=0; f<func_list.size(); f++){
		    	 String constraint = "cstr4" + "_" + cstrNum;
		    	 IloRange rng = this.master_problem.addRange(-Double.MAX_VALUE, InputConstants.replicaLimit, constraint);
//		    	 IloRange rng = this.master_problem.addRange(InputConstants.replicaLimit, Double.MAX_VALUE,  constraint);
		    	 cstrNum++;
		    	 //keep track of constraint
		    	 replica_constraint.add(rng);				    	
			 }*/
		     cstrNum=0;//reset the constraint no. counter
		     //source outgoing link constraint (9)
		     this.src_outgoing_constraint = new HashMap<MpCstr7and11,IloRange>();
		     //iterate through the list of service chain indices			 			    	
		     for(int sd_count=0; sd_count<pair_list.size(); sd_count++){
		    	//create the traffic node
		    	TrafficNodes tn = pair_list.get(sd_count);
		    	//create the constraint object
		    	MpCstr7and11 cstr9 = new MpCstr7and11(tn.chain_index,tn);
		    	//create the range to be considered
		    	String constraint = "cstr9" + "_" + cstrNum;
		    	IloRange rng = this.master_problem.addRange(1.0, 1.0, constraint);
		    	cstrNum++;
		    	//keep track of constraint
		    	this.src_outgoing_constraint.put(cstr9, rng);			    	
		     }
		     System.out.println("Constraint (9) generated!");
		     cstrNum=0;//reset the constraint no. counter
		     //source incoming link constraint (10)
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
		    		//create the range to be considered
			    	String constraint = "cstr10" + "_" + cstrNum;
		    		IloRange rng = this.master_problem.addRange(0.0, Double.MAX_VALUE, constraint);
		    		cstrNum++;
		    		//create the constraint
		    		MpCstr81012and14 cstr10 = new MpCstr81012and14(tn.chain_index, tn, nfvi_node); 
		    		//add range to the constraint 
		    		this.src_incoming_constraint.put(cstr10, rng); 
			     }			    	
		     }
		     System.out.println("Constraint (10) generated!");
		     cstrNum=0;//reset the constraint no. counter
		     //flow conservation constraint - placement - ingress node (11)
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
		    		MpCstr81012and14 cstr11 = new MpCstr81012and14(tn.chain_index, tn, nfvi_node); 
		    		//create the range to be considered
			    	String constraint = "cstr11" + "_" + cstrNum;
		    		IloRange rng = this.master_problem.addRange(0.0, 0.0, constraint);
		    		cstrNum++;
		    		//add range to the constraint 
		    		this.flow_place_ing_constraint.put(cstr11, rng); 
			     }			    	
		     }
		     System.out.println("Constraint (11) generated!");
		     cstrNum=0;//reset the constraint no. counter
		     //flow conservation constraint - no placement - ingress node (12)
		     this.flow_noplace_ing_constraint = new HashMap<MpCstr9and13,IloRange>();
		     //only sd-pairs where the source node is not a NFV-capable node
		     for(TrafficNodes tn : pair_list){
		    	ArrayList<BaseVertex> temp_non_nfvi_nodes = new ArrayList<BaseVertex>(vertex_list_without_nfvi_nodes);
		    	//remove the source vertex if in the non-NFV node list
		    	if(temp_non_nfvi_nodes.contains(tn.v1)){
		    		temp_non_nfvi_nodes.remove(tn.v1);
		    	}
		    	for(BaseVertex non_nfv_node : temp_non_nfvi_nodes){
		    		//create the range to be considered
			    	String constraint = "cstr12" + "_" + cstrNum;
		    		IloRange rng = this.master_problem.addRange(0.0, 0.0, constraint);
		    		cstrNum++;
		    		//create the constraint
		    		MpCstr9and13 cstr12 = new MpCstr9and13(tn.chain_index, tn, non_nfv_node); 
		    		//add range to the constraint 
		    		this.flow_noplace_ing_constraint.put(cstr12, rng);
			     }			    	
		     }
		     System.out.println("Constraint (12) generated!");	
		     cstrNum=0;//reset the constraint no. counter
		     //destination incoming link constraint (13)
		     this.dest_incoming_constraint = new HashMap<MpCstr7and11,IloRange>();			   
		     for(int sd_count=0; sd_count < pair_list.size(); sd_count++){
			    	//create the traffic node
			    	TrafficNodes tn = pair_list.get(sd_count);
			    	//create the constraint object
			    	MpCstr7and11 cstr13 = new MpCstr7and11(tn.chain_index,tn);
			    	//create the range to be considered
			    	String constraint = "cstr13" + "_" + cstrNum;
		    		IloRange rng = this.master_problem.addRange(1.0, 1.0, constraint);
		    		cstrNum++;
		    		//keep track of constraint
		    		this.dest_incoming_constraint.put(cstr13, rng);			    	
			 }
		     System.out.println("Constraint (13) generated!");	
		     cstrNum=0;//reset the constraint no. counter			    
		     //destination outgoing link constraint (14)
		     this.dest_outgoing_constraint = new HashMap<MpCstr81012and14,IloRange>();
		     for(TrafficNodes tn : pair_list){
		    	 //add the nfv nodes
		    	 ArrayList<BaseVertex> temp_nfvi_nodes = new ArrayList<BaseVertex>(nodesNFVI);	    	
		    	 //remove the destinatio node vertex if in the NFV node list
		    	 if(temp_nfvi_nodes.contains(tn.v2)){
		    		 temp_nfvi_nodes.remove(tn.v2);
		    	 }
			     for(BaseVertex nfvi_node : temp_nfvi_nodes){
			    	 //create the range to be considered
				     String constraint = "cstr14" + "_" + cstrNum;
			    	 IloRange rng = this.master_problem.addRange(0.0, Double.MAX_VALUE, constraint);
			    	 cstrNum++;
			    	 //create the constraint
			    	 MpCstr81012and14 cstr14 = new MpCstr81012and14(tn.chain_index, tn, nfvi_node); 
			    	 //keep track of constraint
			    	 this.dest_outgoing_constraint.put( cstr14 , rng);
			     }
		     }
		     System.out.println("Constraint (14) generated!");
		     cstrNum=0;//reset the constraint no. counter
		     //flow conservation constraint - placement - egress node (15)
		     this.flow_place_egr_constraint = new HashMap<MpCstr81012and14,IloRange>();
		     for(TrafficNodes tn : pair_list){
		    	//add the set of NFV nodes
		    	ArrayList<BaseVertex> temp_nfvi_nodes = new ArrayList<BaseVertex>(nodesNFVI);	    	
		    	//remove the destination vertex if it is a NFV node
		    	if(temp_nfvi_nodes.contains(tn.v2)){
		    		temp_nfvi_nodes.remove(tn.v2);
		    	}
		    	for(BaseVertex nfvi_node : temp_nfvi_nodes){
		    		//create the range to be considered
			    	String constraint = "cstr15" + "_" + cstrNum;
		    		IloRange rng = this.master_problem.addRange(0.0, 0.0, constraint);
		    		cstrNum++;
		    		//create the constraint
		    		MpCstr81012and14 cstr15 = new MpCstr81012and14(tn.chain_index, tn, nfvi_node); 
		    		//add range to the constraint 
		    		this.flow_place_egr_constraint.put(cstr15, rng); 
			     }			    	
			 }
		     System.out.println("Constraint (15) generated!");
		     cstrNum=0;//reset the constraint no. counter
		     //flow conservation constraint - no placement - egress node (16)
		     this.flow_nplace_egr_constraint = new HashMap<MpCstr9and13,IloRange>();
		     //only sd-pairs where the source node is not a NFV-capable node
		     for(TrafficNodes tn : pair_list){
		    	ArrayList<BaseVertex> temp_non_nfvi_nodes = new ArrayList<BaseVertex>(vertex_list_without_nfvi_nodes);
		    	//remove the destination vertex if in the non-NFV node list
		    	if(temp_non_nfvi_nodes.contains(tn.v2)){
		    		temp_non_nfvi_nodes.remove(tn.v2);
		    	}
		    	for(BaseVertex non_nfv_node : temp_non_nfvi_nodes){	
		    		//create the range to be considered
			    	String constraint = "cstr16" + "_" + cstrNum;
		    		IloRange rng = this.master_problem.addRange(0.0, 0.0, constraint);
		    		cstrNum++;
		    		//create the constraint
		    		MpCstr9and13 cstr16 = new MpCstr9and13(tn.chain_index, tn, non_nfv_node); 
		    		//add range to the constraint 
		    		this.flow_nplace_egr_constraint.put(cstr16, rng); 
			     }			    	
		     }	 
		     System.out.println("Constraint (16) generated!");
		     /*System.out.println("#### Number of Rows before adding diagonal matrix : " + this.master_problem.getNrows() + " #####");
		     System.out.println("#### Number of Columns before adding diagonal matrix : " + this.master_problem.getNcols() + " #####");
		     System.out.println("#### Number of Non-zero's before adding diagonal matrix : " + this.master_problem.getNNZs() + " #####");*/
		     
		     
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
		    	 	 //add the variable to the set of master problem variables
		    	     this.slackVariables.put(slackVar, tempVar);	    	    
		    	     //add the variable to the linear expression			    	     
		    	     test.addTerm(tempVar, coeffSV);
		    	 	 //add the temp variable to the matrix
		    		 matrixDummyColumns.addColumn(tempVar, new int[]{rowIndex}, new double[]{1.0}); 	    	    
		     }
		     //add the linear expression to the Objective
		     this.BwUsed.setExpr(test);
		     System.out.println("Slack variables added to objective!");
		     /*System.out.println("#### Number of Rows after adding diagonal matrix : " + this.master_problem.getNrows() + " #####");
		     System.out.println("#### Number of Columns after adding diagonal matrix : " + this.master_problem.getNcols() + " #####");
		     System.out.println("#### Number of Non-zero's after adding diagonal matrix : " + this.master_problem.getNNZs() + " #####");*/
		   
		 
		    
		     
		     
		     
		    
		     
		     
		     //Add the VARIABLES
		     //Columns for variable z_gamma			   
		     var_z_gamma = new HashMap<MpVarZ,IloColumn>();
		     //Handles for variable z_gamma
		     this.usedVarZ = new HashMap<MpVarZ,IloNumVar>();		     
		     //including the columns for the objective - (2)
		     //iertating over the set of service chains
		     for(int scID : scUsed){
	    	    for(int scCopyID : scCopies.get(scID)){
	    	    	//there are no generated columns for the number of clusters
	    	    	if(generatedColumns.get(scCopies.get(scID).size()) == null){
		    	    	for(HuerVarZ config : configsPerSC.get(scID)){ 
		    	    		if(config.sCiD == scCopyID){
				    	    	double coeffZObj = 0.0;
				    	    	for(Pp2VarDelta varD : config.DeltaVarSet){
				    	    		coeffZObj += varD.sd.flow_traffic*config.BVarSet.size();
				    	    	}				    		
					    		//add the column for var_z_gamma 	//coefficient for var_z_gamma is the bandwidth multiplied by the number of links used by the configuration			    	
					    		var_z_gamma.put(config, this.master_problem.column( this.BwUsed, coeffZObj));	
				//	    		System.out.println("Coefficient for VarZGamma in this.BwUsed : " + entry.getKey().flow_traffic*link_included );
		    	    		}
		    	    	}
	    	    	}else{//use the previously generated columns
	    	    		for(HuerVarZ config : generatedColumns.get(scCopies.get(scID).size())){ 
		    	    		if(config.sCiD == scCopyID){
				    	    	double coeffZObj = 0.0;
				    	    	for(Pp2VarDelta varD : config.DeltaVarSet){
				    	    		coeffZObj += varD.sd.flow_traffic*config.BVarSet.size();
				    	    	}				    		
					    		//add the column for var_z_gamma 	//coefficient for var_z_gamma is the bandwidth multiplied by the number of links used by the configuration			    	
					    		var_z_gamma.put(config, this.master_problem.column( this.BwUsed, coeffZObj));	
				//	    		System.out.println("Coefficient for VarZGamma in this.BwUsed : " + entry.getKey().flow_traffic*link_included );
		    	    		}
		    	    	}
	    	    	}
	    	    }
		    }
			//configuration selection constraint (3) 
		     for(int scID : scUsed){
		    	 for(int scCopyID : scCopies.get(scID)){
			    	 for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){				 			
						 //check if Z has same service chain
						 if(entry.getKey().sCiD == scCopyID){
							 //add the coefficient for the var_z_gamma variable					
							 var_z_gamma.put(entry.getKey(),entry.getValue().and(this.master_problem.column( this.config_selection_constraint.get(scCopyID), 1.0)));
						 }					 
					 }
		    	 }
		    }
		   if(coreCstr){
			   //including the columns for the core capacity constraints - (5)
			   //iterating over the set of NFV-capable nodes
			   for(int vnf_node_index=0; vnf_node_index < nfv_nodes.size(); vnf_node_index++){	
				   BaseVertex nfvNode = nfv_nodes.get(vnf_node_index);
				   //iterating over the set of service chains
				   for(int scID : scUsed){//Service Chain corresponding to service chain ID 
					     ServiceChain scTemp = ChainSet.get(scID);
					     int scSize = scTemp.chain_seq.size();
					     for(int scCopyID : scCopies.get(scID)){
						     //iterating over the set of traffic nodes and their associated service chain (c) //check if the configuration is for the same chain
					    	 for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){
					    		HuerVarZ config = (HuerVarZ) entry.getKey();					    		 
					    		if(config.sCiD == scCopyID){				    			
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
		   }
		  if(capCstr){
			  //including the columns for the flow capacity constraints - (6)
			  //iterating over all the links
			  int link_counter = 0; 
			  for(BaseVertex s_vert : g._vertex_list){ 
				for(BaseVertex t_vert : g.get_adjacent_vertices(s_vert)){					 
					for(int scID : scUsed){
				 	   ServiceChain scTemp = ChainSet.get(scID);
				 	   for(int scCopyID : scCopies.get(scID)){
						   for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){
							   	 HuerVarZ config = (HuerVarZ) entry.getKey();	
							   	 if(config.sCiD == scCopyID){
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
					 }
			    	 //increment the link counter
			    	 //this is done to depict the next link 
			    	 link_counter++; 
				}				
			 }
		 }
		 //including the columns for function placement relating to configuration selection constraint - 1 - (7)
		 for(Map.Entry<NwMpCstr7and8, IloRange> entryCstr : this.rel_place_conf_constraint_1.entrySet()){
			 	 //the required function
				 int VNF_SEQ = entryCstr.getKey().fSeq;
				 //the NFV_node under consideration
				 BaseVertex NFVI_node = entryCstr.getKey().nfviNode;
				 //variable A
			 	  PpVarA tempVarA = new PpVarA(NFVI_node,VNF_SEQ);
			     //iterating over the set of traffic nodes and their associated service chain (c)
		    	 for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){
		    		 	HuerVarZ config = (HuerVarZ) entry.getKey();				    
					    //match the constraint scID to MpVarZ scID
					    if( (entryCstr.getKey().scID==config.sCiD) && (config.AVarSet.contains(tempVarA)) ){	   					
							//add the coefficient for the this.var_z_gamma variable
		    				var_z_gamma.put(entry.getKey(),entry.getValue().and(this.master_problem.column(entryCstr.getValue(), 1.0)));	
					    }
		    	}				  
		 }		
		 //include the range for var_z_gamma variables
		 //add the variable to the set of variables		 
//		 this.configurationCounter = 0;
	     for(Map.Entry<MpVarZ, IloColumn> entry : var_z_gamma.entrySet()){
	    	String zName = "Z_No." + entry.getKey().cgConfig + "_SC" + entry.getKey().sCiD + "_Config" + entry.getKey().configDsc;
	    	this.usedVarZ.put(entry.getKey(), this.master_problem.numVar(entry.getValue(), 0.0, Double.MAX_VALUE, zName));		    	
	     }  
	     //deallocate var_z_gamma
	     var_z_gamma.clear();
	     System.out.println("######## Variable Z has been added to RMP model! ##########");
	     
	     
	     
	     
	   //Columns for variable x_vf
	   var_x_vf = new HashMap<MpVarX, IloColumn>();
	   //Handles for variable x_vf  
	   this.usedVarX = new HashMap<MpVarX,IloNumVar>();
	   //vnf replica constraint - (1)
	   for(Map.Entry<MpVarX,IloRange> entry : this.vnf_replica_constraint_1.entrySet()){
		   //get VNF Id
		   int vnfId = entry.getKey().f_id;
		   //add column to constraint
		   IloColumn col = this.master_problem.column(entry.getValue(), CountMaxVNF.get(vnfId));
		   //populate the HashMap
		   var_x_vf.put(entry.getKey(),col);
	   }
	   //vnf replica constraint - (3)
	   for(Map.Entry<MpVarX,IloRange> entry : this.vnf_replica_constraint_3.entrySet()){
		   //get the column
		   IloColumn col = var_x_vf.get(entry.getKey());
		   //add the column to the constraint
		   col = col.and(this.master_problem.column(entry.getValue(),-1.0));
		   //populate the HashMap
		   var_x_vf.put(entry.getKey(),col);
	   }
	   //vnf replica constraint - (2)
	   for(int f=0; f<func_list.size(); f++){
		   int fID = func_list.get(f).getid();
		   for(BaseVertex nfviNode : nodesNFVI){
			   //create the key //the object MpVarX
			   MpVarX varX = new MpVarX(nfviNode,fID);
			   //get the column
			   IloColumn col = var_x_vf.get(varX);
			   //add the column to the constraint
			   col = col.and(this.master_problem.column(this.vnf_replica_constraint_2.get(f), 1.0));
			   //populate the HashMap
			   var_x_vf.put(varX,col);
		   }
	   }
	   //vnf feasibility
	  /* for(int f=0; f<func_list.size(); f++){
		   int fID = func_list.get(f).getid();
		   for(BaseVertex nfviNode : nodesNFVI){
			   //create the key //the object MpVarX
			   MpVarX varX = new MpVarX(nfviNode,fID);
			   //get the column
			   IloColumn col = var_x_vf.get(varX);
			   //add the column to the constraint
			   col = col.and(this.master_problem.column(this.vnf_feasibility.get(f), 1.0));
			   //populate the HashMap
			   var_x_vf.put(varX,col);
		   }
	   }*/
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
	  	 String xName = "X_Nod" + entry.getKey().v.get_id() + "_Vnf" + entry.getKey().f_id;
	  	 this.usedVarX.put(entry.getKey(),this.master_problem.numVar(entry.getValue(), 0.0, 1.0, xName));
	   }
	   //deallocate X variables
	   var_x_vf.clear();
	   System.out.println("######## Variable X has been added to RMP model! ##########");  
	   
	   
	   
	   
	   
	   
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
	  /* for(BaseVertex node : nodesNFVI){
		   if(this.vnf_location_constraint_2.get(node) == null){
			   System.out.println("####### Node does not exist! #######");
		   }else{
			   System.out.println("Node: " + node.get_id() + " = " + this.vnf_location_constraint_2.get(node).toString());
		   }
	   }*/
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
	  	 this.usedVarH.put(entry.getKey(),this.master_problem.numVar(entry.getValue(), 0.0, 1.0, hName));
	   }
	   //deallocate X variables
	   var_h_v.clear();
	   System.out.println("######## Variable X has been added to RMP model! ##########");  
	     
	     
	     
	     
	   //Columns for variable x_v^ci
	   var_x_civ = new HashMap<MpVarXc, IloColumn>();
	   //column for replica y
	// IloColumn replicaY = this.master_problem.column(this.BwUsed, InputConstants.Big_M);
	   //Handles for variable x_v^ci
	   this.usedVarXc = new HashMap<MpVarXc,IloNumVar>();
	   //configuration selection constraint - 1 (7)		      
	   for(Map.Entry<NwMpCstr7and8, IloRange> entryCstr : this.rel_place_conf_constraint_1.entrySet()){      
		   //create the key //the object MpVarX
		   MpVarXc temp_elem = new MpVarXc(entryCstr.getKey().scID, entryCstr.getKey().nfviNode, entryCstr.getKey().fSeq) ;
		   //add the column to the constraint
		   IloColumn col = this.master_problem.column(entryCstr.getValue(), -1.0);				 
		   //populate the HashMap
		   var_x_civ.put(temp_elem, col);				  
	   }
	   //configuration selection constraint - 2 (8)	
	   /*for(Map.Entry<NwMpCstr7and8, IloRange> entryCstr : this.rel_place_conf_constraint_2.entrySet()){	   
		  //create the key // the object MpVarX
		  MpVarXc temp_elem = new MpVarXc(entryCstr.getKey().scID, entryCstr.getKey().nfviNode, entryCstr.getKey().fSeq);
		  //get the column
		  IloColumn col = var_x_civ.get(temp_elem);	
		  //add the column to the constraint				
		  col = col.and(this.master_problem.column(entryCstr.getValue(), -InputConstants.Big_M));
		  //populate the HashMap
		  var_x_civ.put(temp_elem, col);	 
	   }*/
	   //vnf replica constraint - (1)
	   for(Map.Entry<MpVarX,IloRange> entry : this.vnf_replica_constraint_1.entrySet()){
		   int fID = entry.getKey().f_id;
		   BaseVertex nfviNode = entry.getKey().v;
		   for(int scID : scUsed){
			 ServiceChain scTemp = ChainSet.get(scID);
			 for(int scCopyID : scCopies.get(scID)){
				 for(int f_seq=0; f_seq<scTemp.chain_size; f_seq++){
					 int tempID=scTemp.chain_seq.get(f_seq);
					 //the temp ID matches with VNF ID
					 if(fID==tempID){			
						  //create the key // the object MpVarX
						  MpVarXc temp_elem = new MpVarXc(scCopyID, nfviNode, f_seq);
						  //get the column
						  IloColumn col = var_x_civ.get(temp_elem);	
						  //add the column to the constraint				
						  col = col.and(this.master_problem.column(entry.getValue(), -1.0));
						  //populate the HashMap
						  var_x_civ.put(temp_elem, col);					 
					 }
				 }
			 }
		   }
	   }
	   //vnf replica constraint - (3)
	   for(Map.Entry<MpVarX,IloRange> entry : this.vnf_replica_constraint_3.entrySet()){
		   int fID = entry.getKey().f_id;
		   BaseVertex nfviNode = entry.getKey().v;
		   for(int scID : scUsed){
			 ServiceChain scTemp = ChainSet.get(scID);
			 for(int scCopyID : scCopies.get(scID)){
				 for(int f_seq=0; f_seq<scTemp.chain_size; f_seq++){
					 int tempID=scTemp.chain_seq.get(f_seq);
					 //the temp ID matches with VNF ID
					 if(fID==tempID){			
						  //create the key // the object MpVarX
						  MpVarXc temp_elem = new MpVarXc(scCopyID, nfviNode, f_seq);
						  //get the column
						  IloColumn col = var_x_civ.get(temp_elem);	
						  //add the column to the constraint				
						  col = col.and(this.master_problem.column(entry.getValue(), 1.0));
						  //populate the HashMap
						  var_x_civ.put(temp_elem, col);					 
					 }
				 }
			 }
		   }
	   }
	   //vnf replica constraint - (2)
	//   for(int f=0; f<func_list.size(); f++){
//		   int fID = func_list.get(f).getid();
//		   for(int scID : scUsed){
//			 ServiceChain scTemp = ChainSet.get(scID);
//			 for(int scCopyID : scCopies.get(scID)){
//				 for(int f_seq=0; f_seq<scTemp.chain_size; f_seq++){
//					 int tempID=scTemp.chain_seq.get(f_seq);
//					 //the temp ID matches with VNF ID
//					 if(fID==tempID){
//						 for(BaseVertex nfviNode : nodesNFVI){
//							 //create the key // the object MpVarX
//							  MpVarXc temp_elem = new MpVarXc(scCopyID, nfviNode, f_seq);
//							  //get the column
//							  IloColumn col = var_x_civ.get(temp_elem);	
//							  //add the column to the constraint				
//							  col = col.and(this.master_problem.column(this.vnf_replica_constraint_2.get(f), 1.0));
//							  //populate the HashMap
//							  var_x_civ.put(temp_elem, col);
//						 }
//					 }
//				 }
//			 }
//		   }
	//   }
	   //function replica constraint (4)
	   //adding columns for x_vf to this constraint	
	//   for(int f=0; f<func_list.size(); f++){
//	  	  //iterate through the list of vnf_nodes
//			  for(int vnf_node_index=0; vnf_node_index < nodesNFVI.size(); vnf_node_index++){
//				  //create the key // the object MpVarX
//				  MpVarX temp_elem = new MpVarX( nfv_nodes.get(vnf_node_index), func_list.get(f).getid() );	
//				  //add the column to the constraint
//				  IloColumn col = this.var_x_vf.get(temp_elem);
//				  //add the column to the constraint //modify the object
//				  col = col.and(this.master_problem.column(replica_constraint.get(f),1.0));
//				  //add the replica y to the same constraint
////				  replicaY = replicaY.and(this.master_problem.column(replica_constraint.get(f),-1.0));
//				  //update the HashMap
//				  this.var_x_vf.put(temp_elem, col);
//			  }		 	    	
//		 }	
	   //add replicaY to the list of variables of the master problem
	//   this.master_problem.numVar(replicaY, 0.0, Double.MAX_VALUE, "replicaY");
	   //source outgoing link constraint (9)
	   //adding columns for x_vf to this constraint
	   for(int scID : scUsed){
		 for(int scCopyID : scCopies.get(scID)){
			 for(Map.Entry<MpCstr7and11,IloRange> entry : this.src_outgoing_constraint.entrySet()){
	  		 	//traffic node
		    	TrafficNodes tn = entry.getKey().tn;
		    	//all the traffic nodes that use that service chain
		    	if(tn.chain_index == scCopyID){
			    	BaseVertex v = tn.v1;//Vertex where the function is placed
			    	int f_seq_index_in_C = 0;//sequence index of the first function in the service chain		    	
			    	MpVarXc temp_elem = new MpVarXc(scCopyID, v, f_seq_index_in_C);		    	
			    	//check if source node is a VNF capable node
			    	if(nodesNFVI.contains(tn.v1)){
		    		    //get the column object 
			    	 	IloColumn col = var_x_civ.get(temp_elem);
			    	 	//add the column to the constraint //modify the object
			    		col = col.and(this.master_problem.column(entry.getValue(),1.0));
			    		//update the HashMap
			    		var_x_civ.put(temp_elem, col);
			     	}
		    	}
		     }
		   }
	   }
	   //source incoming link constraint (10)
	   for(int scID : scUsed){
	 	  for(int scCopyID : scCopies.get(scID)){
	 	    for(Map.Entry<MpCstr81012and14,IloRange> entry : this.src_incoming_constraint.entrySet()){
	 	    	if(entry.getKey().chainIndex == scCopyID){
	 	    		//sequence index of the first function in the service chain
	 	    		int f_seq_index_in_C = 0; 
	 		    	//create an object
	 		    	MpVarXc temp_elem = new MpVarXc(scCopyID, entry.getKey().nfvi_node,f_seq_index_in_C);	
	 		    	//get the column object 
	 		    	IloColumn col = var_x_civ.get(temp_elem);
	 		    	//check if key exists
	 		    	if(col != null){
	 			    	 //add the column to the constraint //modify the object
	 			    	 col = col.and(this.master_problem.column(entry.getValue(), -1.0));
	 			    	 //update the HashMap
	 			    	 var_x_civ.put(temp_elem, col);
	 		    	}
	 		    }
	 	    }
	 	  }
	   }	
	 //flow conservation constraint - placement - ingress node (11)
	   for(int scID : scUsed){ 
	 	  for(int scCopyID : scCopies.get(scID)){
	 	    for(Map.Entry<MpCstr81012and14,IloRange> entry : this.flow_place_ing_constraint.entrySet()){
	 	    	if(entry.getKey().chainIndex == scCopyID){
	 		    	 //sequence index of the first function in the service chain
	 	    		 int f_seq_index_in_C = 0; 
	 		    	 //create an object
	 		    	 MpVarXc temp_elem = new MpVarXc(scCopyID, entry.getKey().nfvi_node,f_seq_index_in_C);		    	
	 		    	 //get the column object 
	 		    	 IloColumn col = var_x_civ.get(temp_elem);
	 		    	 //check if key exists
	 		    	 if(col != null){
	 			    	 //add the column to the constraint //modify the object
	 			    	 col = col.and(this.master_problem.column(entry.getValue(),1.0));
	 			    	 //update the HashMap
	 			    	 var_x_civ.put(temp_elem, col);
	 		    	 }
	 	    	}
	 	    }
	 	  }
	   }
	   //destination incoming link constraint (13)
	   for(int scID : scUsed){
	 	  for(int scCopyID : scCopies.get(scID)){
	 	    for(Map.Entry<MpCstr7and11,IloRange> entry : this.dest_incoming_constraint.entrySet()){
	 	    	if(entry.getKey().chainIndex == scCopyID){
	 	    		 //Traffic Node
	 	    		 TrafficNodes tn = entry.getKey().tn;
	 		    	 //Service chain
	 		    	 ServiceChain sc = ChainSet.get(scID);
	 		    	 //sequence index of the last function in the service chain	
	 			     int f_seq_index_in_C = sc.chain_size-1;
	 		    	 //check if destination node is an NFV node
	 		    	 if(nodesNFVI.contains(tn.v2)){
	 			    	 //create an object
	 			    	 MpVarXc temp_elem = new MpVarXc(scCopyID, tn.v2,f_seq_index_in_C);
	 			    	 //get the column object 
	 			    	 IloColumn col = var_x_civ.get(temp_elem);
	 			    	 //check if key exists
	 			    	 if(col != null){
	 				    	 //add the column to the constraint //modify the object
	 				    	 col = col.and(this.master_problem.column(entry.getValue(), 1.0));
	 				    	 //update the HashMap
	 				    	 var_x_civ.put(temp_elem, col);
	 			    	 }
	 		    	 }
	 		    }
	 	    }
	 	  }
	   }	
	   //destination outgoing link constraint (14)
	   for(int scID : scUsed){
		   for(int scCopyID : scCopies.get(scID)){
		     for(Map.Entry<MpCstr81012and14,IloRange> entry : this.dest_outgoing_constraint.entrySet()){
		     	if(entry.getKey().chainIndex == scCopyID){		    		
			    	ServiceChain sc = ChainSet.get(scID);
			        //sequence index of the last function in the service chain	
			    	int f_seq_index_in_C = sc.chain_size-1;
			    	//create an object
			    	MpVarXc temp_elem = new MpVarXc(scCopyID, entry.getKey().nfvi_node, f_seq_index_in_C);
			    	//get the column object 
			    	IloColumn col = var_x_civ.get(temp_elem);
			    	//check if destination node is a VNF capable node
			    	if(col != null){
				    	 //add the column to the constraint //modify the object
				    	 col = col.and(this.master_problem.column(entry.getValue(), -1.0));
				    	 //update the HashMap
				    	 var_x_civ.put(temp_elem, col);
			     	}
			    }				 
		     }
		   }
		}	    
		  //flow conservation constraint - placement - egress node (15)
		  for(int scID : scUsed){
			  for(int scCopyID : scCopies.get(scID)){
			    for(Map.Entry<MpCstr81012and14,IloRange> entry : this.flow_place_egr_constraint.entrySet()){
			    	if(entry.getKey().chainIndex == scCopyID){
				    	 //service chain
				    	 ServiceChain sc = ChainSet.get(scID);
				    	 //function identifier for the last function in the service chain
		//		    	 int f_id = sc.chain_seq.get(sc.chain_seq.size() - 1);
				    	 //sequence index of the last function in the service chain	
					     int f_seq_index_in_C = sc.chain_size-1;
				    	 //create an object
				    	 MpVarXc temp_elem = new MpVarXc(scCopyID, entry.getKey().nfvi_node,f_seq_index_in_C);	
				    	 //get the column object 
				    	 IloColumn col = var_x_civ.get(temp_elem);
				    	 //check if key exists
				    	 if(col != null){
					    	 //add the column to the constraint //modify the object
					    	 col = col.and(this.master_problem.column(entry.getValue(), -1.0));
					    	 //update the HashMap
					    	 var_x_civ.put(temp_elem, col);
				    	 }
				    }	 
			    }
			  }
		  }		   
		  //add the variable
		  //include the range for var_x_vf variables
		  for(Map.Entry<MpVarXc, IloColumn> entry : var_x_civ.entrySet()){
		  	String xName = "X_SC" + entry.getKey().scID + "_Nod" + entry.getKey().v.get_id() + "_VnfSQ" + entry.getKey().f_seq;
		  	this.usedVarXc.put(entry.getKey(),this.master_problem.numVar(entry.getValue(), 0.0, 1.0, xName));
		  }
		  //deallocate X variables
		  var_x_civ.clear();
		  System.out.println("######## Variable Xc has been added to RMP model! ##########");
		
		
		
		//Columns for var_y_l_sigma_sd
		var_y_l_sigma_sd = new HashMap<MpVarY, IloColumn>();
		//Handles for var_y_l_sigma_sd
		this.usedVarY = new HashMap<MpVarY, IloNumVar>();
		//including the columns for the objective - (2)
		//iterating over the set of service chains
		for(int scID : scUsed){
			for(int scCopyID : scCopies.get(scID)){
				//iterating over the set of traffic nodes and their associated service chain (c)
				for(TrafficNodes tn : serviceChainTN.get(scCopyID)){	    	
			   		//function ID for first function in the service chain
			   		int firstVNF = 0;
			   		//function ID for last function in the service chain
			   		int lastVNF = ChainSet.get(scID).chain_size-1;
					for(BaseVertex s_vert : g._vertex_list){ 
						for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){ 						
							//add the column for var_y_l_sigma1_sd
							MpVarY temp_elem_1 = new MpVarY(tn, firstVNF, s_vert, t_vert);
							//the column
							IloColumn col1 = this.master_problem.column( this.BwUsed, tn.flow_traffic);
			   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
							var_y_l_sigma_sd.put(temp_elem_1, col1);
			   		        //add the column for var_y_l_sigmaN_sd
							MpVarY temp_elem_N = new MpVarY(tn, lastVNF, s_vert, t_vert);
							//the column
							IloColumn colN = this.master_problem.column( this.BwUsed, tn.flow_traffic);
			   				//coefficient for var_y_l_sigmaN_sd is the bandwidth 
							var_y_l_sigma_sd.put(temp_elem_N, colN);						
					 	} 
					}   				
				}
			}		
		}
//		System.out.println("\tY variables added to objective (2)");
		//source outgoing link constraint - (9)	
		for(int scID : scUsed){
			for(int scCopyID : scCopies.get(scID)){
			    for(Map.Entry<MpCstr7and11,IloRange> entry : this.src_outgoing_constraint.entrySet()){	   	
			        //traffic nodes
			    	TrafficNodes tn = entry.getKey().tn;
			        //for all the traffic nodes using that service chain
			    	if(tn.chain_index == scCopyID){
				    	//function ID for first function in the service chain
				    	int firstVNF = 0;			    
		  				//iterating over all the links   				
						for(BaseVertex t_vert : g.get_adjacent_vertices(tn.v1)){
						    //add the column for var_y_l_sigma1_sd
							MpVarY temp_elem_1 = new MpVarY(tn, firstVNF, tn.v1, t_vert);					
							//add the column for var_y_l_sigma1_sd
							IloColumn col = var_y_l_sigma_sd.get(temp_elem_1);
						    //check if key exists
				    	    if(col != null){
		  		   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
		  		   				col = col.and(this.master_problem.column( entry.getValue(), 1.0));		   		   		        	
		  		   				//update the HashMap
		  		   			    var_y_l_sigma_sd.put(temp_elem_1, col);
				    	    }
				    	    else{
				    	    	//print out key
					    		/*System.out.println("SD pair: (" + tn.v1.get_id() + ", "+ tn.v2.get_id() + "); Link: (" + tn.v1.get_id() + ", "+ t_vert.get_id() + "); VNF: " + firstVNF);
				    	    	System.out.println("the column was empty : ");*/
				    	    }
						}
			    	}
			    }	    
			}
		}
//		System.out.println("\tY variables added to constraint (9)");
	   //flow conservation constraint - placement - ingress node (11)	
	   for(int scID : scUsed){ 
		   for(int scCopyID : scCopies.get(scID)){
		    for(Map.Entry<MpCstr81012and14,IloRange> entry : this.flow_place_ing_constraint.entrySet()){	
		         if(entry.getKey().chainIndex == scCopyID){ 		    
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
			   				col = col.and(this.master_problem.column(entry.getValue(), -1.0));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);
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
			   				col = col.and(this.master_problem.column(entry.getValue(), 1.0));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);
				    	}				    	
			    	 }
			    }
			     
		    }
		   }
		}
//	   System.out.println("\tY variables added to constraint (11)");
	   //flow conservation constraint - no placement - ingress node (12)
	   for(int scID : scUsed){	   
		  for(int scCopyID : scCopies.get(scID)){
			for(Map.Entry<MpCstr9and13,IloRange> entry : this.flow_noplace_ing_constraint.entrySet()){	
				if(entry.getKey().chainIndex == scCopyID){		    	
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
			   				col = col.and(this.master_problem.column(entry.getValue(), 1.0));		   		   		        	
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
			   				col = col.and(this.master_problem.column(entry.getValue(), -1.0));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);
				    	}
		    		}
				} 				     
			}
		  }
		}
//	   System.out.println("\tY variables added to constraint (12)");
	   //source incoming link constraint (10)
	   for(int scID : scUsed){
		 for(int scCopyID : scCopies.get(scID)){
		    for(Map.Entry<MpCstr81012and14,IloRange> entry : this.src_incoming_constraint.entrySet()){
		        if(entry.getKey().chainIndex == scCopyID){			    	
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
			   				col = col.and(this.master_problem.column(entry.getValue(), 1.0));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);
				    	}
			    	 }
			    }			     
		    }
		 }
	   }
//	   System.out.println("\tY variables added to constraint (10)");
	   //destination incoming link constraint (13)	
	   for(int scID : scUsed){
		  for(int scCopyID : scCopies.get(scID)){ 
		    for(Map.Entry<MpCstr7and11,IloRange> entry : this.dest_incoming_constraint.entrySet()){
		    	if(entry.getKey().chainIndex == scCopyID){			    	
			    	 TrafficNodes tn = entry.getKey().tn;		    	
			    	 int lastVNF = ChainSet.get(scID).chain_size-1;				    	    		 
		    		 //outgoing links
			    	 for(BaseVertex s_vrt : g.get_precedent_vertices(tn.v2)){
			    		//add the column for var_y_l_sigma1_sd
							MpVarY temp_elem = new MpVarY(tn, lastVNF, s_vrt, tn.v2);
							//add the column for var_y_l_sigma1_sd
							IloColumn col = var_y_l_sigma_sd.get(temp_elem);
							//check if key exists
					    	if(col != null){
				   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
				   				col = col.and(this.master_problem.column(entry.getValue(), 1.0));		   		   		        	
				   				//update the HashMap
				   			    var_y_l_sigma_sd.put(temp_elem, col);
					    	}
			    	 }
			    }				     			    	 
		    }
		  }
	   }
//	   System.out.println("\tY variables added to constraint (13)");
	   int count15 = 0;
	   //flow conservation constraint - placement - egress node - outgoing links (15)	 
	   for(int scID : scUsed){
		 for(int scCopyID : scCopies.get(scID)){ 
		    for(Map.Entry<MpCstr81012and14,IloRange> entry : this.flow_place_egr_constraint.entrySet()){
		    	if(entry.getKey().chainIndex == scCopyID){	    		
			    	 //SD pair
	  			     TrafficNodes tn = entry.getKey().tn; // the traffic nodes
	  			     //function ID for first function in the service chain
	  			     int lastVNF = ChainSet.get(scID).chain_size-1;	
			    	 BaseVertex nfvi_node = entry.getKey().nfvi_node;
			    	/* System.out.println("\t\t"+scCopyID+", ("+tn.v1.get_id()+","+tn.v2.get_id()+"), "+nfvi_node.get_id());
			    	 System.out.print("\t\t\tPrecedent vertices: ");*/
		    		 //incoming links
			    	 for(BaseVertex s_vrt : g.get_precedent_vertices(nfvi_node)){
			    		//System.out.print(s_vrt.get_id()+","); 
		    		    //add the column for var_y_l_sigma1_sd
						MpVarY temp_elem = new MpVarY(tn, lastVNF, s_vrt, nfvi_node);	
						//add the column for var_y_l_sigma1_sd
						IloColumn col = var_y_l_sigma_sd.get(temp_elem);
						//check if key exists
				    	if(col != null){
			   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
			   				col = col.and(this.master_problem.column(entry.getValue(), -1.0));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);
				    	}
			    	 }
			    	/* System.out.print("\n"); 
			    	 System.out.print("\t\t\tAdjacent vertices: ");*/
			    	 //outgoing links
			    	 for(BaseVertex t_vrt : g.get_adjacent_vertices(nfvi_node)){
			    		 //System.out.print(t_vrt.get_id()+","); 
			    		//add the column for var_y_l_sigma1_sd
						MpVarY temp_elem = new MpVarY(tn, lastVNF, nfvi_node, t_vrt);
						//add the column for var_y_l_sigma1_sd
						IloColumn col = var_y_l_sigma_sd.get(temp_elem);
						//check if key exists
				    	if(col != null){
			   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
			   				col = col.and(this.master_problem.column(entry.getValue(), 1.0));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);
				    	}
			    	 }
			    	 //System.out.print("\n"); 
			    }
		    	//increment the constraint number
		    	count15++;
		    }
		 }
	   }
//	   System.out.println("\tY variables added to constraint (15)");
	   //flow conservation constraint - no placement - egress node (16)
	   for(int scID : scUsed){
		  for(int scCopyID : scCopies.get(scID)){ 
		    for(Map.Entry<MpCstr9and13,IloRange> entry : this.flow_nplace_egr_constraint.entrySet()){
		    	if(entry.getKey().chainIndex == scCopyID){			    	
			    	 TrafficNodes tn = entry.getKey().tn; //traffic nodes
			    	 //function ID of the first function in the service chain
			    	 int lastVNF = ChainSet.get(scID).chain_size-1;	
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
			   				col = col.and(this.master_problem.column(entry.getValue(), 1.0));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);
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
			   				col = col.and(this.master_problem.column(entry.getValue(), -1.0));		   		   		        	
			   				//update the HashMap
			   			    var_y_l_sigma_sd.put(temp_elem, col);
				    	}
		    		 }
				 }				     
		    }
		  }
	   }	
//	   System.out.println("\tY variables added to constraint (16)");
	   if(capCstr){
			//flow capacity constraint	(6)	    	
			//declare the counter
			int lnum = 0;	
		    for(BaseVertex s_vert : g._vertex_list){ 
				for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){									
				    //iterating over the set of traffic nodes and their associated service chain (c)
					for(int scID : scUsed){
						//function ID for first function in the service chain
			    		int firstVNF = 0;
			    		//function ID for last function in the service chain
			    		int lastVNF = ChainSet.get(scID).chain_size-1; 
						for(int scCopyID : scCopies.get(scID)){
							//iterate over the traffic nodes that use that traffic chain
							for(TrafficNodes tn : serviceChainTN.get(scCopyID)){	    	
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
		//			    	    	System.out.println("Y_firstVNF variable not added in link constraints");
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
		//							System.out.println("Y_lastVNF variable not added in link constraints");
									}					    
					    	}
						}
					}
					//increment the link_num
					lnum++;
				} 
			 }
	  }
//	  System.out.println("\tY variables added to constraint (6)");
	   //destination incoming link constraint (14)
	   for(int scID : scUsed){
		   //function ID of the first function in the service chain
	   	   int lastVNF = ChainSet.get(scID).chain_size-1;
		   for(int scCopyID : scCopies.get(scID)){
		     for(Map.Entry<MpCstr81012and14,IloRange> entry : this.dest_outgoing_constraint.entrySet()){
		     	if(entry.getKey().chainIndex == scCopyID){
		    	 	TrafficNodes tn = entry.getKey().tn;		    	 
			    	//node at which the VNF has been placed
			    	BaseVertex nfviNode = entry.getKey().nfvi_node;
				   	//iterating over the outgoing links from the last VNF placement
			    	//these links will take you to the destination
//			    	System.out.println("\t\t"+scCopyID+", ("+tn.v1.get_id()+","+tn.v2.get_id()+"), "+nfviNode.get_id());
//			    	System.out.print("\t\t\tAdjacent vertices: ");
			    	for(BaseVertex t_vert :  g.get_adjacent_vertices(nfviNode)){
			    		//System.out.print(t_vert.get_id()+",");
					    //add the column for var_y_l_sigma1_sd
						MpVarY temp_elem_N = new MpVarY(tn, lastVNF, nfviNode, t_vert);	   					
						//add the column for var_y_l_sigma1_sd
						IloColumn col = var_y_l_sigma_sd.get(temp_elem_N);
					    //check if key exists
			    	    if(col != null){
	  		   				//coefficient for var_y_l_sigma1_sd is the bandwidth 
	  		   				col = col.and(this.master_problem.column(entry.getValue(), 1.0));		   		   		        	
	  		   				//update the HashMap
	  		   			    var_y_l_sigma_sd.put(temp_elem_N, col);  		   			    
			    	    }		   					
					}
//			    	System.out.print("\n"); 
			    }
		     }
	    	}
		}   
//	    System.out.println("\tY variables added to constraint (14)");
	    
	    //check if there are any source and destination are same on the links
	   /* for(MpVarY objY : var_y_l_sigma_sd.keySet()){
	    	if(objY.s_vrt.get_id()==objY.t_vrt.get_id()){
	    		System.out.println("\t\tY_SC"+ objY.tn.chain_index + "_Ind" + objY.f_id + "_Src" +  objY.tn.v1.get_id() + "_Dst" + objY.tn.v2.get_id() + "_Ls" + objY.s_vrt.get_id() + "_Ld" + objY.t_vrt.get_id() + " is wrong!");
	    	}
	    }*/
	   
	    //add the variable
		//include the range for var_y_l_sigma_sd variables
		for(Map.Entry<MpVarY, IloColumn> entry : var_y_l_sigma_sd.entrySet()){
			String yName = "Y_SC"+ entry.getKey().tn.chain_index + "_Ind" + entry.getKey().f_id + "_Src" +  entry.getKey().tn.v1.get_id() + "_Dst" + entry.getKey().tn.v2.get_id() + "_Ls" + entry.getKey().s_vrt.get_id() + "_Ld" + entry.getKey().t_vrt.get_id();
			this.usedVarY.put(entry.getKey(), this.master_problem.numVar(entry.getValue(),0.0,1.0,yName));
		}
		//deallocate Y variables
		var_y_l_sigma_sd.clear();
//		System.out.println("######## Variable Y has been added to RMP model! ##########");
  	}
	
	
}
