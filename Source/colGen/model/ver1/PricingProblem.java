package colGen.model.ver1;

import ilog.concert.IloColumn;
import ilog.concert.IloIntVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class PricingProblem{
	//pricing problem belongs to which service chain
	public int scIDpP;
	//model for the pricing problem
    public IloCplex pricing_problem;
    //declare the handle for the reduced cost - (21)
    public IloObjective reducedCost;
    //core capacity constraint - (22)
    public ArrayList<IloRange> core_capacity;
    //link capacity constraint - (23)
    public ArrayList<IloRange> link_capacity;
    //vnf placement constraint - (24)
    public ArrayList<IloRange> vnf_placement;
    //placement of initial 2 VNFs outgoing - (25)
//  public ArrayList<IloRange> vnf_placement_first_2_sc_outgoing = new ArrayList<IloRange>();
    //placement of 2 VNF outgoing - (26)  
//  public ArrayList<IloRange> vnf_placement_2nd_vnf_outgoing = new ArrayList<IloRange>();
    //placement of initial 2 VNFs incoming - (27)
    public ArrayList<IloRange> vnf_placement_first_2_sc_incoming;
    //placement of sequential VNFs in a service chain - (28)
    public ArrayList<IloRange> vnf_placement_seq_2_nfv_nodes;
    //placement of sequential VNFs in a service chain - (29) in non-NFV nodes
    public ArrayList<IloRange> vnf_placement_seq_2_non_nfv_nodes;
    //placement of last 2 VNFs incoming - (30) 
//  public ArrayList<IloRange> vnf_placement_last_2_sc_incoming = new ArrayList<IloRange>();
    //placement of last 2 VNFs outgoing - (31)
    public ArrayList<IloRange> vnf_placement_last_2_sc_outgoing;
    //add A variables to the model
    public Map<PpVarA, IloIntVar> Used_var_a_vi;      
    //add B variables to the model
    public Map<PpVarB, IloIntVar> Used_var_b_l_sigma_i_i1;
   	
	

	//creating the Pricing Problem object
	public PricingProblem(boolean coreCstr, boolean capCstr, Map<BaseVertex,Double> cpuCoreCount, Map<NodePair,Double> linkCapacity, Map<Integer, ArrayList<TrafficNodes>> serviceChainTN, List<FuncPt> vnfList, int serviceChainID, 
			Map<Integer, ServiceChain> ChainSet, ArrayList<BaseVertex> nodesNFVI,  ArrayList<BaseVertex> nfv_nodes,
			ArrayList<BaseVertex> vertex_list_without_nfvi_nodes, Graph g, Map<Integer,Integer> scCopyToSC) throws Exception{
		
		 //add the columns for the variable var_a_v_sigma
	    Map<PpVarA, IloColumn> var_a_vi;
	    //add the columns for the variable b_l_sigma_i_sigma_i+1
	    Map<PpVarB, IloColumn> var_b_l_sigma_i_i1;
		
		//service chain for the pricing problem
		this.scIDpP = serviceChainID;
		//get the functions associated with this service chain		
		ServiceChain sc = ChainSet.get(scCopyToSC.get(serviceChainID));
		//chain size
		int scSize = sc.chain_size;
		//get the list of functions for this particular service chain
		ArrayList<FuncPt> scVNF = new ArrayList<FuncPt>();
		//iterate through the service chain //find the VNFs
		for(int vnfID : sc.chain_seq){
			for(FuncPt fp : vnfList){
				if(fp.getid() == vnfID){
					if(!scVNF.contains(fp))
						scVNF.add(fp);
				}
			}
		}		
		//get the total traffic used by the service chain
		double totalTraffic = 0.0;
		for(TrafficNodes tn : serviceChainTN.get(serviceChainID)){
			totalTraffic += tn.flow_traffic;
		}		
		
		//model for the pricing problem
		this.pricing_problem = new IloCplex();
		//declare the handle for the reduced cost - (17)
		this.reducedCost = this.pricing_problem.addMinimize();
	    		    
	    //add the constraints to the model 
		int cstrNum=0;//reset the constraint no. counter 
		this.core_capacity = new ArrayList<IloRange>();
		//core capacity constraint - (18)
		for(int nfvNodeCount = 0; nfvNodeCount < nfv_nodes.size(); nfvNodeCount++){
			BaseVertex nfvNode = nfv_nodes.get(nfvNodeCount);
	    	double coreCount = 0.0;
	    	if(cpuCoreCount.get(nfvNode) != null){
	    		coreCount = cpuCoreCount.get(nfvNode);
	    	}
			String constraint = "cstr22" + "_" + cstrNum;
			this.core_capacity.add(nfvNodeCount,this.pricing_problem.addRange(-Double.MAX_VALUE, coreCount,constraint));
			cstrNum++;
		}
		//link capacity constraint - (19)
		cstrNum=0;//reset the constraint no. counter
		this.link_capacity = new ArrayList<IloRange>();
		int link_num = 0;
		for(BaseVertex s_vert : g._vertex_list){ 
			for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){
				NodePair link = new NodePair(s_vert,t_vert);
				double linkBw = 0.0;
				if(linkCapacity.get(link)!=null){
					linkBw = linkCapacity.get(link);
				}
				String constraint = "cstr23" + "_" + cstrNum;
				this.link_capacity.add(link_num,this.pricing_problem.addRange(-Double.MAX_VALUE, linkBw, constraint));
				cstrNum++;
				//increment the link counter
				link_num++;
			}
		}
		//vnf placement constraint - (20)
		cstrNum=0;//reset the constraint no. counter
		this.vnf_placement = new ArrayList<IloRange>();
	    for(int vnf_index = 0 ; vnf_index < scSize; vnf_index++){
	    	String constraint = "cstr24" + "_" + cstrNum;
	    	this.vnf_placement.add(this.pricing_problem.addRange(1.0, 1.0, constraint));
	    	cstrNum++;
	    }	 
	    //placement of initial 2 VNFs outgoing - (25)
//	    cstrNum=0;//reset the constraint no. counter
//	    for(int vnf_node_index=0; vnf_node_index < nodesNFVI.size(); vnf_node_index++){
//	    	String constraint = "cstr25" + "_" + cstrNum;
//	    	this.vnf_placement_first_2_sc_outgoing.add(this.pricing_problem.addRange(0.0, Double.MAX_VALUE, constraint));
//	    	cstrNum++;
//	    }	
	    //placement of 2 VNF outgoing - (26)  
//	    cstrNum=0;//reset the constraint no. counter
//	    //select the chain with 5 VNFs
//	    for(int vnf_node_index=0; vnf_node_index < nodesNFVI.size(); vnf_node_index++){
//	    	String constraint = "cstr26" + "_" + cstrNum;
//	    	this.vnf_placement_2nd_vnf_outgoing.add(this.pricing_problem.addRange(-Double.MAX_VALUE, 1.0, constraint));
//	    	cstrNum++;
//	    }
	    //placement of initial 2 VNFs incoming - (21)	
	    cstrNum=0;//reset the constraint no. counter
	    this.vnf_placement_first_2_sc_incoming = new ArrayList<IloRange>();
	    //select the chain with 5 VNFs
	    for(int vnf_node_index=0; vnf_node_index < nodesNFVI.size(); vnf_node_index++){
	    	String constraint = "cstr27" + "_" + cstrNum;
	    	this.vnf_placement_first_2_sc_incoming.add(this.pricing_problem.addRange(-Double.MAX_VALUE, 1.0, constraint));
	    	cstrNum++;
	    }
	    //placement of sequential VNFs in a service chain - (28)
	    cstrNum=0;//reset the constraint no. counter
	    this.vnf_placement_seq_2_nfv_nodes = new ArrayList<IloRange>();
	    for(int nfvi_node_index=0; nfvi_node_index<nodesNFVI.size(); nfvi_node_index++){
	    	//select the chain with 5 VNFs
		    for(int vnf_index=0; vnf_index < (scSize-1); vnf_index++){
		    	String constraint = "cstr28" + "_" + cstrNum;
		    	this.vnf_placement_seq_2_nfv_nodes.add(this.pricing_problem.addRange(0.0, 0.0, constraint));
		    	cstrNum++;
		    }
	    }
	    //placement of sequential VNFs in a service chain - (29) in non-NFV nodes
	    cstrNum=0;//reset the constraint no. counter
	    this.vnf_placement_seq_2_non_nfv_nodes= new ArrayList<IloRange>();
	    for(int vrt_crt=0; vrt_crt<vertex_list_without_nfvi_nodes.size(); vrt_crt++){
	    	//select the chain with 5 VNFs
		    for(int vnf_index=0; vnf_index < (scSize-1); vnf_index++){
		    	String constraint = "cstr29" + "_" + cstrNum;
		    	this.vnf_placement_seq_2_non_nfv_nodes.add(this.pricing_problem.addRange(0.0, 0.0, constraint));
		    	cstrNum++;
		    }
	    }
	    //placement of last 2 VNFs incoming - (30) in SDN_NFV
//	    cstrNum=0;//reset the constraint no. counter
//	    //select the chain with 5 VNFs
//	    for(int vnf_node_index=0; vnf_node_index < nodesNFVI.size(); vnf_node_index++){
//	    	String constraint = "cstr30" + "_" + cstrNum;
//	    	this.vnf_placement_last_2_sc_incoming.add(this.pricing_problem.addRange(0.0, Double.MAX_VALUE, constraint));
//	    	cstrNum++;
//	    }		    
	    //placement of last 2 VNFs outgoing - (31)
	    cstrNum=0;//reset the constraint no. counter
	    this.vnf_placement_last_2_sc_outgoing = new ArrayList<IloRange>();
	    //select the chain with 5 VNFs
	    for(int vnf_node_index=0; vnf_node_index < nodesNFVI.size(); vnf_node_index++){
	    	String constraint = "cstr31" + "_" + cstrNum;
	    	this.vnf_placement_last_2_sc_outgoing.add(this.pricing_problem.addRange(-Double.MAX_VALUE, 1.0, constraint));
	    	cstrNum++;
	    }
	    //outgoing degree 1 for all nodes in the network - (32)
//	    ArrayList<IloRange> node_outgoing_degree = new ArrayList<IloRange>();
//	    cstrNum=0;//reset the constraint no. counter
//	    for(int vrt_index=0; vrt_index < g.get_vertex_list().size(); vrt_index++){
//	    	//select the chain with 5 VNFs
//		    for(int vnf_index= 0 ; vnf_index < (scSize-1); vnf_index++){
//	    		String constraint = "cstr32" + "_" + cstrNum;
//		    	node_outgoing_degree.add(this.pricing_problem.addRange(-Double.MAX_VALUE, 1.0, constraint));
//	    		cstrNum++;
//		    }
//	    }
	    //incoming degree 1 for all nodes in the network - (33)
//	    ArrayList<IloRange> node_incoming_degree = new ArrayList<IloRange>();
//	    cstrNum=0;//reset the constraint no. counter
//	    for(int vrt_index=0; vrt_index < g.get_vertex_list().size(); vrt_index++){
//	    	//select the chain with 5 VNFs
//		    for(int vnf_index= 0 ; vnf_index < (scSize-1); vnf_index++){
//	    		String constraint = "cstr33" + "_" + cstrNum;
//		    	node_incoming_degree.add(this.pricing_problem.addRange(-Double.MAX_VALUE, 1.0, constraint));
//		    }
//	    }


	    //add A variables to the model
	    this.Used_var_a_vi = new HashMap<PpVarA, IloIntVar>();
	    //add the columns for the variable var_a_v_sigma
	    var_a_vi = new HashMap<PpVarA, IloColumn>();   
	    //add B variables to the model
	    this.Used_var_b_l_sigma_i_i1 = new HashMap<PpVarB, IloIntVar>();
	    //add the columns for the variable b_l_sigma_i_sigma_i+1
	    var_b_l_sigma_i_i1 = new HashMap<PpVarB, IloColumn>();
	    

	    //add the column for var_a_v_sigma to the placement of sequential VNFs in a service chain - (28) in NFV nodes
	    //modifying the for loop to account for consecutive VNFs
	    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
	    	BaseVertex nfvi_node = nodesNFVI.get(nfvi_node_index);	
	    	for(int vnf_index = 0 ; vnf_index < (scSize-1) ; vnf_index++){		
	    		 int f_i = vnf_index;
	    		 int f_i1 = vnf_index+1;
	    		 //create the new object
	    		 PpVarA temp_elem_1 = new PpVarA(nfvi_node,f_i);
	    		 //get the column from the hashmap
		    	 IloColumn col1  = var_a_vi.get(temp_elem_1);
	    		 //create the new object
	    		 PpVarA temp_elem_2 = new PpVarA(nfvi_node,f_i1);
	    		 //get the column from the hashmap
		    	 IloColumn col2  = var_a_vi.get(temp_elem_2);
		    	 if(col1 != null){
		    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
			    	col1 = col1.and(this.pricing_problem.column(this.vnf_placement_seq_2_nfv_nodes.get(nfvi_node_index*(scSize - 1) + vnf_index), -1.0));
			    	//update the hashmap
			    	var_a_vi.put(temp_elem_1, col1);
		    	 }else{
		    		//coefficient for var_a_v_sigma_i 
		    		var_a_vi.put(temp_elem_1, this.pricing_problem.column(this.vnf_placement_seq_2_nfv_nodes.get(nfvi_node_index*(scSize - 1) + vnf_index), -1.0));
		    	 }
		    	 if(col2 != null){
		    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
		    		col2 = col2.and(this.pricing_problem.column(this.vnf_placement_seq_2_nfv_nodes.get(nfvi_node_index*(scSize - 1) + vnf_index), 1.0));
		    		//update the hashmap
		    		var_a_vi.put(temp_elem_2, col2);
		    	 }else{
		    		//coefficient for var_a_v_sigma_i+1
		    		var_a_vi.put(temp_elem_2, this.pricing_problem.column(this.vnf_placement_seq_2_nfv_nodes.get(nfvi_node_index*(scSize - 1) + vnf_index), 1.0));
		    	 }		    		
	    	 }
	    }
	    if(coreCstr){
		    //add the column for var_a_v_sigma to the core capacity constraint - (22)
		    for(int nfvNodeCount = 0; nfvNodeCount < nfv_nodes.size(); nfvNodeCount++){	   
		    	BaseVertex nfvNode = nfv_nodes.get(nfvNodeCount);
		    	//iterate through the list of functions
		    	for(FuncPt fp : scVNF){
		    		//Function ID
		    		int fID = fp.getid();
		    		double fCore = fp.getcore();
		    		//Function Sequence Index
		    		int f_seq_index = sc.chain_seq.indexOf(fID);
		    		//create the new object
		    		PpVarA temp_elem = new PpVarA(nfvNode,f_seq_index);
		    		//get the column from the hashmap
		    		IloColumn col = var_a_vi.get(temp_elem);
		    		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
			    		col = col.and(this.pricing_problem.column(this.core_capacity.get(nfvNodeCount), fCore*totalTraffic)); 
			    		//update the hashmap
			    		var_a_vi.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		var_a_vi.put(temp_elem, this.pricing_problem.column(this.core_capacity.get(nfvNodeCount), fCore*totalTraffic)); 
			    	}
		    	}
		    }
	    }
	    //add the column for var_a_v_sigma to the VNF placement constraint - (24)
	    for(int vnf_index = 0 ; vnf_index < scSize; vnf_index++){
	    	 for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
	    		//Network node
	    		BaseVertex nfv_node = nodesNFVI.get(nfvi_node_index);	    		
	    		//create the new object
	    		PpVarA temp_elem = new PpVarA(nfv_node,vnf_index);
	    		//get the column from the hashmap
	    		IloColumn col = var_a_vi.get(temp_elem);
	    		//check if key exists
		    	if(col != null){
		    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
		    		col = col.and(this.pricing_problem.column(this.vnf_placement.get(vnf_index), 1.0)); 
		    		//update the hashmap
		    		var_a_vi.put(temp_elem, col);
		    	}else{
		    		//coefficient for var_a_v_sigma_i 
		    		var_a_vi.put(temp_elem, this.pricing_problem.column(this.vnf_placement.get(vnf_index), 1.0)); 
		    	}
	    	 }
	    }
	    //add the column for var_a_v_sigma to the placement of initial 2 VNFs outgoing - (25)
//	    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
//	    	//since the first VNF in the service chain
//	    	int first_vnf_index = 0;	    
//	    	//Network node
//    		BaseVertex nfvi_node = nodesNFVI.get(nfvi_node_index);    		
//    		//create the new object
//    		PpVarA temp_elem = new PpVarA(nfvi_node,first_vnf_index);
//    		//get the column from the hashmap
//    		IloColumn col = this.var_a_vi.get(temp_elem);
//    		//check if key exists
//	    	if(col != null){
//	    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
//	    		col = col.and(this.pricing_problem.column(this.vnf_placement_first_2_sc_outgoing.get(nfvi_node_index), -1.0));
//	    		//update the hashmap
//	    		this.var_a_vi.put(temp_elem, col);
//	    	}else{
//	    		//coefficient for var_a_v_sigma_i 
//	    		this.var_a_vi.put(temp_elem, this.pricing_problem.column(this.vnf_placement_first_2_sc_outgoing.get(nfvi_node_index), -1.0));
//	    	}    		
//	    	//only if the chain size is greater than 1
//	    	if(scSize > 1){
//		    	//second VNF in the chain
//		    	int second_vnf_index = 1;	    		  	
//	    		//create the new object
//	    		PpVarA temp_elem_nxt = new PpVarA(nfvi_node,second_vnf_index);
//	    		//get the column from the hashmap
//	    		IloColumn col_nxt = this.var_a_vi.get(temp_elem_nxt);
//		    	//check if key exists
//		    	if(col_nxt != null){
//		    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
//		    		col_nxt = col_nxt.and(this.pricing_problem.column(this.vnf_placement_first_2_sc_outgoing.get(nfvi_node_index), 1.0));
//		    		//update the hashmap
//		    		this.var_a_vi.put(temp_elem_nxt, col_nxt);
//		    	}else{
//		    		//coefficient for var_a_v_sigma_i 
//		    		this.var_a_vi.put(temp_elem, this.pricing_problem.column(this.vnf_placement_first_2_sc_outgoing.get(nfvi_node_index), 1.0));
//		    	}
//	    	}
//	    }
	    //add the column for var_a_v_sigma to the placement of 2nd VNF incoming - (26)
//	    if(scSize > 1){
//		    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
//		    	//since the second VNF in the service chain
//		    	int second_vnf_index = 1;
//		    	//Network node
//	    		BaseVertex nfvi_node = nodesNFVI.get(nfvi_node_index);
//	    		//Function sequence ID
//	    		int second_VNF_f_id = sc.chain_seq.get(second_vnf_index);
//	    		//create the new object
//	    		PpVarA temp_elem = new PpVarA(nfvi_node,second_VNF_f_id);
//	    		//get the column from the hashmap
//	    		IloColumn col = this.var_a_vi.get(temp_elem);
//	    		//check if key exists
//		    	if(col != null){
//		    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
//			    	col = col.and(this.pricing_problem.column(this.vnf_placement_2nd_vnf_outgoing.get(nfvi_node_index), 1.0));
//			    	//update the hashmap
//			    	this.var_a_vi.put(temp_elem, col);
//		    	}else{
//		    		//coefficient for var_a_v_sigma_i 
//		    		this.var_a_vi.put(temp_elem, this.pricing_problem.column(this.vnf_placement_2nd_vnf_outgoing.get(nfvi_node_index), 1.0));
//		    	}
//		    }
//	    }
	    //add the column for var_a_v_sigma to the placement of initial 2 VNFs incoming - (27)
	    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
	    	//since the first VNF in the service chain
	    	int first_vnf_index = 0;
	    	//Network node
    		BaseVertex nfvi_node = nodesNFVI.get(nfvi_node_index);    		
    		//create the new object
    		PpVarA temp_elem = new PpVarA(nfvi_node,first_vnf_index);
    		//get the column from the hashmap
    		IloColumn col = var_a_vi.get(temp_elem);
    		//check if key exists
	    	if(col != null){
	    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
		    	col = col.and(this.pricing_problem.column(this.vnf_placement_first_2_sc_incoming.get(nfvi_node_index), 1.0));
		    	//update the hashmap
		    	var_a_vi.put(temp_elem, col);
	    	}else{
	    		//coefficient for var_a_v_sigma_i 
	    		var_a_vi.put(temp_elem, this.pricing_problem.column(this.vnf_placement_first_2_sc_incoming.get(nfvi_node_index), 1.0));		    		
	    	}
	    }
	    //add the column for var_a_v_sigma to the placement of last 2 VNFs incoming - (30)
//	    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
//	    	//since the last VNF in the service chain
//	    	int last_vnf_index = scSize - 1;
//	    	//Network node
//    		BaseVertex nfvi_node = nodesNFVI.get(nfvi_node_index);    	
//    		//create the new object
//    		PpVarA temp_elem = new PpVarA(nfvi_node,last_vnf_index);
//    		//get the column from the hashmap
//    		IloColumn col = this.var_a_vi.get(temp_elem);
//      		//check if key exists
//	    	if(col != null){
//	    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
//		    	col = col.and(this.pricing_problem.column(this.vnf_placement_last_2_sc_incoming.get(nfvi_node_index), -1.0));
//		    	//update the hashmap
//		    	this.var_a_vi.put(temp_elem, col);
//	    	}else{
//	    		//coefficient for var_a_v_sigma_i 
//	    		this.var_a_vi.put(temp_elem, this.pricing_problem.column(this.vnf_placement_last_2_sc_incoming.get(nfvi_node_index), -1.0));
//	    	}
//    		
//	    	//if the service chain size is greater than 1
//	    	if(scSize > 1){
//	    		//since the last but one VNF in the service chain
//		    	int penultimate_vnf_index = last_vnf_index - 1;		    		
//	    		//create the new object
//	    		PpVarA temp_elem_prev = new PpVarA(nfvi_node,penultimate_vnf_index);
//	    		//get the column from the hashmap
//	    		IloColumn col_prev = this.var_a_vi.get(temp_elem_prev);  
//		    	//check if key exists
//		    	if(col_prev != null){
//		    		//coefficient for var_a_v_sigma_i-1 to the constraint //modify the column
//			    	col_prev = col_prev.and(this.pricing_problem.column(this.vnf_placement_last_2_sc_incoming.get(nfvi_node_index), 1.0));
//			    	//update the hashmap
//			    	this.var_a_vi.put(temp_elem_prev, col_prev);
//		    	}else{
//		    		//coefficient for var_a_v_sigma_i 
//		    		this.var_a_vi.put(temp_elem, this.pricing_problem.column(this.vnf_placement_last_2_sc_incoming.get(nfvi_node_index), 1.0));
//		    	}
//	    	}
//	    }
	    //add the column for var_a_v_sigma to the placement of last VNF outgoing - (31)
	    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
	    	//since the last VNF in the service chain
	    	int last_vnf_index = scSize - 1;
	    	//Network node
    		BaseVertex nfvi_node = nodesNFVI.get(nfvi_node_index);    	
    		//create the new object
    		PpVarA temp_elem = new PpVarA(nfvi_node,last_vnf_index);
    		//get the column from the hashmap
    		IloColumn col = var_a_vi.get(temp_elem);
    		//check if key exists
	    	if(col != null){
	    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
	    		col = col.and(this.pricing_problem.column(this.vnf_placement_last_2_sc_outgoing.get(nfvi_node_index), 1.0));
	    		//update the hashmap
	    		var_a_vi.put(temp_elem, col);
	    	}else{
	    		//coefficient for var_a_v_sigma_i 
	    		var_a_vi.put(temp_elem, this.pricing_problem.column(this.vnf_placement_last_2_sc_outgoing.get(nfvi_node_index), 1.0));
	    	}
	    }		   
	    //add the variable to the set of variables
	    for(Map.Entry<PpVarA,IloColumn> entry : var_a_vi.entrySet()){
	    	String aName = "A_Node" + entry.getKey().node.get_id() + "_VnfSQ" + entry.getKey().f_seq;
	    	this.Used_var_a_vi.put(entry.getKey(),this.pricing_problem.intVar(entry.getValue(), 0, 1, aName));
	    }
	    //deallocate A variables
	    var_a_vi.clear();
	    
	    
	    
	    
	    if(capCstr){
		    //link capacity constraint - (23)		
	  		link_num = 0;
	  		for(BaseVertex s_vert : g._vertex_list){ 
	  			for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){
	  				for(int vnf_index = 0 ; vnf_index < (scSize - 1); vnf_index++){
	  					//create the key
			   			PpVarB temp_elem = new PpVarB(serviceChainID,vnf_index,vnf_index+1,s_vert,t_vert);
			   			//get the column object if its exists
			   			IloColumn col = var_b_l_sigma_i_i1.get(temp_elem);	
			   		    //check if key exists
			    		if(col != null){
			    			//add the column object if it exists
			    			col = col.and(this.pricing_problem.column(this.link_capacity.get(link_num), totalTraffic));
			    			//update the value in the hashmap
				    		var_b_l_sigma_i_i1.put(temp_elem, col);
			    		}else{
			    			//add the elements to the HashMap
			    			var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.link_capacity.get(link_num), totalTraffic));
			    		}
	  				} 
	  				//increment the link counter
	  				link_num++;
	  			}
	  		}
	    }
	    //add the column for var_b_l_sigma_i_i1 to the placement of initial 2 VNFs outgoing - (25)
//	    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
//	    	//source vertex for the link
//	    	BaseVertex s_vrt = nodesNFVI.get(nfvi_node_index);
//	    	//for outgoing links
//	    	for(BaseVertex t_vrt : g.get_adjacent_vertices(s_vrt)){
//	    		//considering the first 2 functions in a service chain
//	    		int s_f_index = 0;
//	    		int t_f_index = 1;	    	
//	    		//create the key
//	    		PpVarB temp_elem = new PpVarB(serviceChainID,s_f_index,t_f_index,s_vrt,t_vrt);
//	    		//get the column object if its exists
//	   			IloColumn col = this.var_b_l_sigma_i_i1.get(temp_elem);	
//	   		    //check if key exists
//	    		if(col != null){
//	    			//add the column to the constraint
//	    			col=col.and(this.pricing_problem.column(this.vnf_placement_first_2_sc_outgoing.get(nfvi_node_index), 1.0));
//	    			//update the value in the Hashmap
//		    		this.var_b_l_sigma_i_i1.put(temp_elem, col);
//	    		}else{		    			
//		    		//add the elements to the Hashmap
//		    		this.var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_first_2_sc_outgoing.get(nfvi_node_index), 1.0));
//	    		}		    		
//	    	}		    
//	    }
	    //add the column for var_b_l_sigma_i_i1 to the placement of 2nd VNF outgoing - (26) in SDN_NFV
	    //adjacent edges //outgoing
//	    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
//	    	//source vertex for the link
//	    	BaseVertex s_vrt = nodesNFVI.get(nfvi_node_index);
//	    	//for outgoing links
//	    	for(BaseVertex t_vrt : g.get_adjacent_vertices(s_vrt)){
//	    		//considering the first 2 functions in a service chain
//	    		int s_f_index = 0;
//	    		int t_f_index = 1;
//	    		//create the key
//	    		PpVarB temp_elem = new PpVarB(serviceChainID,s_f_index,t_f_index,s_vrt,t_vrt);
//	    		//get the column object if its exists
//	   			IloColumn col = this.var_b_l_sigma_i_i1.get(temp_elem);	
//	   		    //check if key exists
//	    		if(col != null){
//	    			//add the column to the constraint
//	    			col=col.and(this.pricing_problem.column(this.vnf_placement_2nd_vnf_outgoing.get(nfvi_node_index), 1.0));
//	    			//update the value in the hashmap
//		    		this.var_b_l_sigma_i_i1.put(temp_elem, col);
//	    		}else{		    			
//		    		//add the elements to the Hashmap
//		    		this.var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_2nd_vnf_outgoing.get(nfvi_node_index), 1.0));
//	    		}		    		
//	    	}		    
//	    }
	    //add the column for var_b_l_sigma_i_i1 to the placement of initial 2 VNFs incoming - (27)
	    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
	    	//target vertex for the link
	    	BaseVertex t_vrt = nodesNFVI.get(nfvi_node_index);
	    	//for incoming links
		   	for(BaseVertex s_vrt : g.get_precedent_vertices(t_vrt)){
	    		//considering the first 2 functions in a service chain
	    		int s_f_index = 0;
	    		int t_f_index = 1;
	    		//create the key
	    		PpVarB temp_elem = new PpVarB(serviceChainID,s_f_index,t_f_index,s_vrt,t_vrt);
	    		//get the column object if its exists
	   			IloColumn col = var_b_l_sigma_i_i1.get(temp_elem);		    		
	    		//check if key exists
	    		if(col != null){
	    			//add the column to the constraint
	    			col = col.and(this.pricing_problem.column(this.vnf_placement_first_2_sc_incoming.get(nfvi_node_index), 1.0));
	    			//update the hashmap
	    			var_b_l_sigma_i_i1.put(temp_elem, col);  
	    		}else{				    		
		    	 	//add the elements to the HashMap
		    		var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_first_2_sc_incoming.get(nfvi_node_index), 1.0));
	    		}			    	
		   	}			   
	    }	    
	    //add the column for var_b_l_sigma_i_i1 to the placement of sequential VNFs in a service chain - (28) in NFV nodes - outgoing links from NFV node 
	    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
	    	//NFV node
	    	BaseVertex nfvi_node = nodesNFVI.get(nfvi_node_index);;
	    	//select the chain with 5 VNFs
		    for(int vnf_index = 0 ; vnf_index < (scSize - 1); vnf_index++){ // -1 because we are accounting for sequential VNFs in a service chain			    	
		    	//outoging links from NFV node   	
			   	for(BaseVertex t_vrt : g.get_adjacent_vertices(nfvi_node)){				    		
		   			//create the key
		   			PpVarB temp_elem = new PpVarB(serviceChainID,vnf_index,vnf_index+1,nfvi_node,t_vrt);
		   			//get the column object if its exists
		   			IloColumn col = var_b_l_sigma_i_i1.get(temp_elem);			    					    			
		    		//check if key exists
		    		if( col != null){
		    			//add the column to the constraint
		    			col = col.and(this.pricing_problem.column(this.vnf_placement_seq_2_nfv_nodes.get(nfvi_node_index*(scSize - 1) + vnf_index), 1.0));
		    			//update the HashMap
		    			var_b_l_sigma_i_i1.put(temp_elem, col);  
		    		}else{					    		
			    		//add the elements to the HashMap
			    		var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_seq_2_nfv_nodes.get(nfvi_node_index*(scSize - 1) + vnf_index), 1.0));
		    		}				    		
			   	}
			   	//incoming links to NFV node
				for(BaseVertex s_vrt : g.get_precedent_vertices(nfvi_node)){				    		
		   			//create the key
		   			PpVarB temp_elem = new PpVarB(serviceChainID,vnf_index,vnf_index+1,s_vrt,nfvi_node);
		   		    //get the column object if its exists
		   			IloColumn col = var_b_l_sigma_i_i1.get(temp_elem);
		    		//check if key exists
		    		if( col != null){
		    			//add the column to the constraint
		    			col = col.and(this.pricing_problem.column(this.vnf_placement_seq_2_nfv_nodes.get(nfvi_node_index*(scSize - 1) + vnf_index), -1.0));
		    			//update the HashMap
		    			var_b_l_sigma_i_i1.put(temp_elem, col);  
		    		}else{				    		
			    		//add the elements to the HashMap
			    		var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_seq_2_nfv_nodes.get(nfvi_node_index*(scSize - 1) + vnf_index), -1.0));
		    		}					    						    	
			   	}			    
		    }
	    }
	    //add the column for var_b_l_sigma_i_i1 to the placement of sequential VNFs in a service chain - (29) in non-NFV nodes
	    for(int non_nfv_node_index = 0; non_nfv_node_index < vertex_list_without_nfvi_nodes.size(); non_nfv_node_index++){		    	
	    	//non NFV node
	    	BaseVertex nonNFVnode = vertex_list_without_nfvi_nodes.get(non_nfv_node_index);		    	
	    	//select the chain with 5 VNFs
		    for(int vnf_index = 0 ; vnf_index < (scSize - 1); vnf_index++){ // -1 because we are accounting for sequential VNFs in a service chain	  	
	    	    //outgoing links
			   	for(BaseVertex t_vrt : g.get_adjacent_vertices(nonNFVnode)){				    		
		   			//create the key
		   			PpVarB temp_elem = new PpVarB(serviceChainID,vnf_index,vnf_index+1,nonNFVnode,t_vrt);
		   		    //get the column object if its exists
		   			IloColumn col = var_b_l_sigma_i_i1.get(temp_elem);			    			
		    		//check if key exists
		    		if(col != null){
		    			//add the column to the constraint
		    			col = col.and(this.pricing_problem.column(this.vnf_placement_seq_2_non_nfv_nodes.get(non_nfv_node_index*(scSize - 1) + vnf_index), 1.0));
		    			//update the HashMap
		    			var_b_l_sigma_i_i1.put(temp_elem, col);  
		    		}else{				    	
			    		//add the elements to the HashMap
			    		var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_seq_2_non_nfv_nodes.get(non_nfv_node_index*(scSize - 1) + vnf_index), 1.0));
		    		}
			   	}
			   	//incoming links
				for(BaseVertex s_vrt : g.get_precedent_vertices(nonNFVnode)){				    		
		   			//create the key
		   			PpVarB temp_elem = new PpVarB(serviceChainID,vnf_index,vnf_index+1,s_vrt,nonNFVnode);
		   		    //get the column object if its exists
		   			IloColumn col = var_b_l_sigma_i_i1.get(temp_elem);				    	
		    		//check if key exists
		    		if(col != null){
		    			//add the column to the constraint
		    			col = col.and(this.pricing_problem.column(this.vnf_placement_seq_2_non_nfv_nodes.get(non_nfv_node_index*(scSize - 1) + vnf_index), -1.0));
		    			//update the HashMap
		    			var_b_l_sigma_i_i1.put(temp_elem, col);  
		    		}else{					    		
			    		//add the elements to the HashMap
			    		var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_seq_2_non_nfv_nodes.get(non_nfv_node_index*(scSize - 1) + vnf_index), -1.0));
		    		}
			    						    	
			   	}			    
		    }
	    }
	    //add the column for var_b_l_sigma_i_i1 to the placement of last 2 VNFs incoming - (30) in SDN_NFV
//	    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
//	    	//NFV node
//	    	BaseVertex t_vrt = nodesNFVI.get(nfvi_node_index);;
//		   	//incoming links    	
//		   	for(BaseVertex s_vrt : g.get_precedent_vertices(t_vrt)){
//	    		//considering the last 2 functions in a service chain
//	    		int s_f_index = sc.chain_size - 2;
//	    		int t_f_index = sc.chain_size - 1;
//	    		//create the key
//	    		PpVarB temp_elem = new PpVarB(serviceChainID, s_f_index, t_f_index, s_vrt, t_vrt);
//	    		//get the column object if its exists
//	   			IloColumn col = this.var_b_l_sigma_i_i1.get(temp_elem);		    		
//	    		//check if key exists
//	    		if(col != null){
//	    			//add the column to the constraint
//	    			col = col.and(this.pricing_problem.column(this.vnf_placement_last_2_sc_incoming.get(nfvi_node_index), 1.0));
//	    			//update the HashMap
//	    			this.var_b_l_sigma_i_i1.put(temp_elem, col);  
//	    		}else{			    		
//		    		//add the elements to the HashMap
//		    		this.var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_last_2_sc_incoming.get(nfvi_node_index), 1.0));
//	    		}			    	
//		   	}	    
//	    }	    
	    //add the column for var_b_l_sigma_i_i1 to the placement of last VNF outgoing - (31)
	    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
	    	//NFV node
	    	BaseVertex s_vrt = nodesNFVI.get(nfvi_node_index);;
	    	//outgoing links
	    	for(BaseVertex t_vrt : g.get_adjacent_vertices(s_vrt)){
	    		//considering the last 2 functions in a service chain
	    		int s_f_index = scSize - 2;
	    		int t_f_index = scSize - 1;
	    		//create the key
	    		PpVarB temp_elem = new PpVarB(serviceChainID,s_f_index,t_f_index,s_vrt,t_vrt);
	    		//get the column object if its exists
	   			IloColumn col = var_b_l_sigma_i_i1.get(temp_elem);		    		
	    		//check if key exists
	    		if(col != null){
	    			//add the column to the constraint
	    			col = col.and(this.pricing_problem.column(this.vnf_placement_last_2_sc_outgoing.get(nfvi_node_index), 1.0));
	    			//update the HashMap
	    			var_b_l_sigma_i_i1.put(temp_elem, col);  
	    		}else{		    		
		    		//add the elements to the HashMap
		    		var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_last_2_sc_outgoing.get(nfvi_node_index), 1.0));
	    		}		    	
	    	}		    
	    }
	    //outgoing degree 1 for all nodes in the network - (32)		    
//	    for(int vrt_index=0; vrt_index < g.get_vertex_list().size(); vrt_index++){
//	    	//node
//	    	BaseVertex s_vrt = g.get_vertex_list().get(vrt_index);
//	    	//select the chain with 5 VNFs
//		    for(int vnf_index=0; vnf_index < (sc.chain_size-1); vnf_index++){
//		    	//outgoing links
//		    	for(BaseVertex t_vrt : g.get_adjacent_vertices(s_vrt)){
//		    		//create the key
//		    		PpVarB temp_elem = new PpVarB(serviceChainID,vnf_index,vnf_index+1,s_vrt,t_vrt);
//		    		//get the column object if its exists
//		   			IloColumn col = this.var_b_l_sigma_i_i1.get(temp_elem);
//		   		    //check if key exists
//		    		if(col != null){
//		    			//add the column to the constraint
//		    			col = col.and(this.pricing_problem.column(node_outgoing_degree.get(vrt_index*(sc.chain_size - 1) + vnf_index), 1.0));
//		    			//update the HashMap
//		    			this.var_b_l_sigma_i_i1.put(temp_elem, col);  
//		    		}else{		    		
//			    		//add the elements to the HashMap
//			    		this.var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(node_outgoing_degree.get(vrt_index*(sc.chain_size - 1) + vnf_index), 1.0));
//		    		}
//		    	}
//		    }
//		}
//	    //incoming degree 1 for all nodes in the network - (33)		    
//	    for(int vrt_index=0; vrt_index < g.get_vertex_list().size(); vrt_index++){
//	    	//node
//	    	BaseVertex t_vrt = g.get_vertex_list().get(vrt_index);
//	    	//select the chain with 5 VNFs
//		    for(int vnf_index=0; vnf_index < (sc.chain_size-1); vnf_index++){
//		    	//incoming links
//		    	for(BaseVertex s_vrt : g.get_precedent_vertices(t_vrt)){
//		    		//create the key
//		    		PpVarB temp_elem = new PpVarB(serviceChainID,vnf_index,vnf_index+1,s_vrt,t_vrt);
//		    		//get the column object if its exists
//		   			IloColumn col = this.var_b_l_sigma_i_i1.get(temp_elem);
//		   		    //check if key exists
//		    		if(col != null){
//		    			//add the column to the constraint
//		    			col = col.and(this.pricing_problem.column(node_incoming_degree.get(vrt_index*(sc.chain_size - 1) + vnf_index), 1.0));
//		    			//update the HashMap
//		    			this.var_b_l_sigma_i_i1.put(temp_elem, col);  
//		    		}else{		    		
//			    		//add the elements to the HashMap
//			    		this.var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(node_incoming_degree.get(vrt_index*(sc.chain_size - 1) + vnf_index), 1.0));
//		    		}
//		    	}
//		    }
//		}
	    //add the variable to the set of variables
	    for(Map.Entry<PpVarB, IloColumn> entry : var_b_l_sigma_i_i1.entrySet()){
	    	String bName = "B_Ls" + entry.getKey().s_vrt.get_id() + "_Ld" + entry.getKey().t_vrt.get_id() + "_vP" + entry.getKey().s_f_index + "_vN" + entry.getKey().t_f_index + "_SC" + entry.getKey().sc_index;
	    	this.Used_var_b_l_sigma_i_i1.put(entry.getKey(), this.pricing_problem.intVar(entry.getValue(), 0, 1, bName));
	    }
	    //deallocate B variables
	    var_b_l_sigma_i_i1.clear();
	}
	
	
}
