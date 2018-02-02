package colGen.model.ver2;

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
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.ver1.MpCstr7and11;
import colGen.model.ver1.PpVarA;
import colGen.model.ver1.PpVarB;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class PricingProblem2 {
		//pricing problem belongs to which service chain
		public int scIDpP;
		//model for the pricing problem
	    public IloCplex pricing_problem;
	    //declare the handle for the reduced cost - (66)
	    public IloObjective reducedCost;
	    //core capacity constraint - (21)
	    public ArrayList<IloRange> core_capacity;
	    //link capacity constraint - (22)
	    public ArrayList<IloRange> link_capacity;
	    //latency constraint (23)
	    public ArrayList<IloRange> latency_constraint;
	    //vnf placement constraint - (24)
	    public ArrayList<IloRange> vnf_placement;
	    //P with A constraint - (25)
	    public Map<Pp2CstrAndOnP,IloRange> pAnd1WithA;
	    //P with Delta constraint - (26)
	    public Map<Pp2CstrAndOnP,IloRange> pAnd2WithDelta;
	    //P with A and Delta constraint - (27)
	    public Map<Pp2CstrAndOnP,IloRange> pAnd3WithDeltaAndA;
	    //Q with B constraint - (28)
	    public Map<Pp2CstrAndOnQ,IloRange> qAnd1WithB;
	    //Q with Delta constraint - (29)
	    public Map<Pp2CstrAndOnQ,IloRange> qAnd2WithDelta;
	    //Q with B and Delta constraint - (30)
	    public Map<Pp2CstrAndOnQ,IloRange> qAnd3WithDeltaAndB;
	    //placement of initial 2 VNFs incoming - (31)
	    public ArrayList<IloRange> vnf_placement_first_2_sc_incoming;
	    //placement of last 2 VNFs outgoing - (32)
	    public ArrayList<IloRange> vnf_placement_last_2_sc_outgoing;
	    //placement of sequential VNFs in a service chain - (33)
	    public ArrayList<IloRange> vnf_placement_seq_2_nfv_nodes;
	    //placement of sequential VNFs in a service chain - (34) in non-NFV nodes
	    public ArrayList<IloRange> vnf_placement_seq_2_non_nfv_nodes;	   
	    //add Delta variables to the model
	    public Map<Pp2VarDelta, IloIntVar> Used_var_d_sd;
	    //add the columns for the variable var_delta_sd
	    public Map<Pp2VarDelta, IloColumn> var_d_sd;
	    //add A variables to the model
	    public Map<PpVarA, IloIntVar> Used_var_a_vi;
	    //add the columns for the variable var_a_v_sigma
	    public Map<PpVarA, IloColumn> var_a_vi;   
	    //add B variables to the model
	    public Map<PpVarB, IloIntVar> Used_var_b_l_sigma_i_i1;
	    //add the columns for the variable b_l_sigma_i_sigma_i+1
	    public Map<PpVarB, IloColumn> var_b_l_sigma_i_i1;
	    //add X variables to the model
	    public Map<Pp2VarP, IloIntVar> Used_var_p_sd_vi;
	    //add the columns for the variable x_sd_vi
	    public Map<Pp2VarP, IloColumn> var_p_sd_vi;
	    //add Y variables to the model
	    public Map<Pp2VarQ, IloIntVar> Used_var_q_sd_il;
	    //add the columns for the variable y_sd_il
	    public Map<Pp2VarQ, IloColumn> var_q_sd_il;
	    /*//placement of initial 2 VNFs outgoing - (25)
	    public ArrayList<IloRange> vnf_placement_first_2_sc_outgoing = new ArrayList<IloRange>();
	    //placement of 2 VNF outgoing - (26)  
	    public ArrayList<IloRange> vnf_placement_2nd_vnf_outgoing = new ArrayList<IloRange>();
	    //placement of last 2 VNFs incoming - (30) 
	    public ArrayList<IloRange> vnf_placement_last_2_sc_incoming = new ArrayList<IloRange>();*/
	    
	    
	    
	    
	    //creating the Pricing Problem object
		public PricingProblem2(int coreCount, Map<Integer, ArrayList<TrafficNodes>> serviceChainTN, List<FuncPt> vnfList, int serviceChainID, 
				Map<Integer, ServiceChain> ChainSet, ArrayList<BaseVertex> nodesNFVI,  ArrayList<BaseVertex> nfv_nodes,
				ArrayList<BaseVertex> vertex_list_without_nfvi_nodes, Graph g) throws Exception{
			
				//service chain for the pricing problem
				this.scIDpP = serviceChainID;
				//get the functions associated with this service chain
				ServiceChain sc = ChainSet.get(serviceChainID);
				//chain size
				int scSize = sc.chain_size;
				//get the list of functions for this particular service chain
				ArrayList<FuncPt> scVNF = new ArrayList<FuncPt>();			
				//iterate through the service chain //find the VNFs
				for(int vnfID : sc.chain_seq){
					for(FuncPt fp : vnfList){
						if(fp.getid() == vnfID){
							scVNF.add(fp);								
						}
					}
				}
				//print out scVNFs
				int indVNF = 0;
				for(FuncPt fp : scVNF){
					System.out.println("Function ID: " + fp.getid() + " ; Seq.No: " + indVNF);
					indVNF++;
				}
				//set of SD pairs using a service chain 'c'
				ArrayList<TrafficNodes> sdPairsForSC = serviceChainTN.get(serviceChainID);
						
				
				//model for the pricing problem
				this.pricing_problem = new IloCplex();
				//declare the handle for the reduced cost - (66)
				this.reducedCost = this.pricing_problem.addMinimize();
			    		    
			    //add the constraints to the model 
				int cstrNum=0;//reset the constraint no. counter 
				//starting constraint number
		     	int cstrIndicator = InputConstants.pricingProblemConstraintCount;
				if(InputConstants.coreCstr){
					this.core_capacity = new ArrayList<IloRange>();
					//core capacity constraint - (21)
					for(int nfvNodeCount = 0; nfvNodeCount < nfv_nodes.size(); nfvNodeCount++){
						String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
						this.core_capacity.add(nfvNodeCount,this.pricing_problem.addRange(-Double.MAX_VALUE, coreCount, constraint));
						cstrNum++;
					}
				}
				//link capacity constraint - (22)
				cstrNum=0;//reset the constraint no. counter
				cstrIndicator++;//increase constraint number
				this.link_capacity = new ArrayList<IloRange>();
				int link_num = 0;
				if(InputConstants.capacityCstr){
					for(BaseVertex s_vert : g._vertex_list){ 
						for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){
							String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
							this.link_capacity.add(link_num,this.pricing_problem.addRange(-Double.MAX_VALUE, InputConstants.BANDWIDTH_PER_LAMBDA, constraint));
							cstrNum++;
							//increment the link counter
							link_num++;
						}
					}
				}
				//latency constraint - (23)
				cstrNum=0;//reset the constraint no. counter
				cstrIndicator++;//increase constraint number
				this.latency_constraint = new ArrayList<IloRange>();
				//iterate through the list of service chain indices			 			    	
			    for(int sd_count=0; sd_count < sdPairsForSC.size(); sd_count++){			    	
			    	//create the traffic node
			    	TrafficNodes tn = sdPairsForSC.get(sd_count);			    	
			    	double latencyReq = ChainSet.get(serviceChainID).getLatReq();
			    	double totalProcTime = 0.0;
			    	double sdTraffic = tn.flow_traffic;			    	
					//iterate through the service chain //find the VNFs
					for(int vnfID : ChainSet.get(serviceChainID).chain_seq){
						for(FuncPt fp : vnfList){
							if(fp.getid() == vnfID){
								totalProcTime += sdTraffic*fp.getProcDelay();						
							}
						}
					}
			    	double upperBound = latencyReq - totalProcTime;			    	
			    	//create the range to be considered
			    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
			    	IloRange rng = this.pricing_problem.addRange(-Double.MAX_VALUE,upperBound,constraint);
			    	cstrNum++;
			    	//keep track of constraint
			    	this.latency_constraint.add(sd_count,rng);			    	
			    }
				//vnf placement constraint - (24)
				cstrNum=0;//reset the constraint no. counter
				cstrIndicator++;//increase constraint number
				this.vnf_placement = new ArrayList<IloRange>();
			    for(int vnf_index = 0 ; vnf_index < scSize; vnf_index++){
			    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
			    	this.vnf_placement.add(this.pricing_problem.addRange(1.0, 1.0, constraint));
			    	cstrNum++;
			    }
			    //P with A constraint - (25)
			    cstrNum=0;//reset the constraint no. counter
			    cstrIndicator++;//increase constraint number
			    this.pAnd1WithA = new HashMap<Pp2CstrAndOnP, IloRange>();
			    //iterate through the list of traffic nodes
			    for(TrafficNodes tn : sdPairsForSC ){
			    	//iterate through the list of NFVI nodes
			    	for(BaseVertex nfviNode : nodesNFVI){
			    		//iterate through the sequence of VNF functions
			    		for(int f_seq = 0 ; f_seq < scSize; f_seq++){
			    			String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
			    			Pp2CstrAndOnP cstr = new Pp2CstrAndOnP(tn, nfviNode, f_seq);
			    			this.pAnd1WithA.put(cstr, this.pricing_problem.addRange(-Double.MAX_VALUE, 0.0, constraint));
			    			cstrNum++;
			    		}
			    	}
			    }
			    //P with Delta constraint - (26)
			    cstrNum=0;//reset the constraint no. counter
			    cstrIndicator++;//increase constraint number
			    this.pAnd2WithDelta = new HashMap<Pp2CstrAndOnP, IloRange>();
			    //iterate through the list of traffic nodes
			    for(TrafficNodes tn : sdPairsForSC ){
			    	//iterate through the list of NFVI nodes
			    	for(BaseVertex nfviNode : nodesNFVI){
			    		//iterate through the sequence of VNF functions
			    		for(int f_seq = 0 ; f_seq < scSize; f_seq++){
			    			String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
			    			Pp2CstrAndOnP cstr = new Pp2CstrAndOnP(tn, nfviNode, f_seq);
			    			this.pAnd2WithDelta.put(cstr, this.pricing_problem.addRange(-Double.MAX_VALUE, 0.0, constraint));
			    			cstrNum++;
			    		}
			    	}
			    }
			    //P with A and Delta constraint - (27)
			    cstrNum=0;//reset the constraint no. counter
			    cstrIndicator++;//increase constraint number
			    this.pAnd3WithDeltaAndA = new HashMap<Pp2CstrAndOnP, IloRange>();
			    //iterate through the list of traffic nodes
			    for(TrafficNodes tn : sdPairsForSC ){
			    	//iterate through the list of NFVI nodes
			    	for(BaseVertex nfviNode : nodesNFVI){
			    		//iterate through the sequence of VNF functions
			    		for(int f_seq = 0 ; f_seq < scSize; f_seq++){
			    			String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
			    			Pp2CstrAndOnP cstr = new Pp2CstrAndOnP(tn, nfviNode, f_seq);
			    			this.pAnd3WithDeltaAndA.put(cstr, this.pricing_problem.addRange(-Double.MAX_VALUE, 1.0, constraint));
			    			cstrNum++;
			    		}
			    	}
			    }
			    //Q with B constraint - (28)			   
			    cstrNum=0;//reset the constraint no. counter
			    cstrIndicator++;//increase constraint number
			    this.qAnd1WithB = new HashMap<Pp2CstrAndOnQ,IloRange>();
			    link_num = 0;
				for(BaseVertex s_vert : g._vertex_list){ 
					for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){
						//iterate through the list of traffic nodes
					    for(TrafficNodes tn : sdPairsForSC ){
				    		//iterate through the sequence of VNF functions
				    		for(int f_seq = 0 ; f_seq < scSize - 1; f_seq++){
								String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
								Pp2CstrAndOnQ cstr = new Pp2CstrAndOnQ(tn, f_seq, s_vert, t_vert);
				    			this.qAnd1WithB.put(cstr, this.pricing_problem.addRange(-Double.MAX_VALUE, 0.0, constraint));
								cstrNum++;								
				    		}
				    	}
				    	//increment the link counter
						link_num++;
					}
				}
			    //Q with Delta constraint - (29)
			    cstrNum=0;//reset the constraint no. counter
			    cstrIndicator++;//increase constraint number
			    this.qAnd2WithDelta = new HashMap<Pp2CstrAndOnQ,IloRange>();
			    link_num = 0;
				for(BaseVertex s_vert : g._vertex_list){ 
					for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){
						//iterate through the list of traffic nodes
					    for(TrafficNodes tn : sdPairsForSC ){
				    		//iterate through the sequence of VNF functions
				    		for(int f_seq = 0 ; f_seq < scSize - 1; f_seq++){
								String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
								Pp2CstrAndOnQ cstr = new Pp2CstrAndOnQ(tn, f_seq, s_vert, t_vert);
				    			this.qAnd2WithDelta.put(cstr, this.pricing_problem.addRange(-Double.MAX_VALUE, 0.0, constraint));
								cstrNum++;								
				    		}
				    	}
				    	//increment the link counter
						link_num++;
					}
				}
			    //Q with B and Delta constraint - (30)
			    cstrNum=0;//reset the constraint no. counter
			    cstrIndicator++;//increase constraint number
			    this.qAnd3WithDeltaAndB = new HashMap<Pp2CstrAndOnQ,IloRange>();
			    link_num = 0;
				for(BaseVertex s_vert : g._vertex_list){ 
					for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){
						//iterate through the list of traffic nodes
					    for(TrafficNodes tn : sdPairsForSC ){
				    		//iterate through the sequence of VNF functions
				    		for(int f_seq = 0 ; f_seq < scSize - 1; f_seq++){
								String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
								Pp2CstrAndOnQ cstr = new Pp2CstrAndOnQ(tn, f_seq, s_vert, t_vert);
				    			this.qAnd3WithDeltaAndB.put(cstr, this.pricing_problem.addRange(-Double.MAX_VALUE, 1.0, constraint));
								cstrNum++;								
				    		}
				    	}
				    	//increment the link counter
						link_num++;
					}
				}
			    //placement of initial 2 VNFs incoming - (31)	
			    cstrNum=0;//reset the constraint no. counter
			    cstrIndicator++;//increase constraint number
			    this.vnf_placement_first_2_sc_incoming = new ArrayList<IloRange>();
			    //select the chain with 5 VNFs
			    for(int vnf_node_index=0; vnf_node_index < nodesNFVI.size(); vnf_node_index++){
			    	String constraint ="cstr" + cstrIndicator + "_" + cstrNum;
			    	this.vnf_placement_first_2_sc_incoming.add(this.pricing_problem.addRange(-Double.MAX_VALUE, 1.0, constraint));
			    	cstrNum++;
			    }
			    //placement of last 2 VNFs outgoing - (32)
			    cstrNum=0;//reset the constraint no. counter
			    cstrIndicator++;//increase constraint number
			    this.vnf_placement_last_2_sc_outgoing = new ArrayList<IloRange>();
			    //select the chain with 5 VNFs
			    for(int vnf_node_index=0; vnf_node_index < nodesNFVI.size(); vnf_node_index++){
			    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
			    	this.vnf_placement_last_2_sc_outgoing.add(this.pricing_problem.addRange(-Double.MAX_VALUE, 1.0, constraint));
			    	cstrNum++;
			    }
			    //placement of sequential VNFs in a service chain - (33)
			    cstrNum=0;//reset the constraint no. counter
			    cstrIndicator++;//increase constraint number
			    this.vnf_placement_seq_2_nfv_nodes = new ArrayList<IloRange>();
			    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
			    	//select the chain with 5 VNFs
				    for(int vnf_index = 0 ; vnf_index < (ChainSet.get(serviceChainID).chain_size-1); vnf_index++){
				    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
				    	this.vnf_placement_seq_2_nfv_nodes.add(this.pricing_problem.addRange(0.0, 0.0, constraint));
				    	cstrNum++;
				    }
			    }
			    //placement of sequential VNFs in a service chain - (34) in non-NFV nodes
			    cstrNum=0;//reset the constraint no. counter
			    cstrIndicator++;//increase constraint number
			    this.vnf_placement_seq_2_non_nfv_nodes= new ArrayList<IloRange>();
			    for(int vrt_crt = 0; vrt_crt < vertex_list_without_nfvi_nodes.size(); vrt_crt++){
			    	//select the chain with 5 VNFs
				    for(int vnf_index = 0 ; vnf_index < (ChainSet.get(serviceChainID).chain_size-1); vnf_index++){
				    	String constraint = "cstr" + cstrIndicator + "_" + cstrNum;
				    	this.vnf_placement_seq_2_non_nfv_nodes.add(this.pricing_problem.addRange(0.0, 0.0, constraint));
				    	cstrNum++;
				    }
			    }		    
			  
			   /* //placement of initial 2 VNFs outgoing - (25)
			    cstrNum=0;//reset the constraint no. counter
			    for(int vnf_node_index=0; vnf_node_index < nodesNFVI.size(); vnf_node_index++){
			    	String constraint = "cstr25" + "_" + cstrNum;
			    	this.vnf_placement_first_2_sc_outgoing.add(this.pricing_problem.addRange(0.0, Double.MAX_VALUE, constraint));
			    	cstrNum++;
			    }	
			    //placement of 2 VNF outgoing - (26)  
			    cstrNum=0;//reset the constraint no. counter
			    //select the chain with 5 VNFs
			    for(int vnf_node_index=0; vnf_node_index < nodesNFVI.size(); vnf_node_index++){
			    	String constraint = "cstr26" + "_" + cstrNum;
			    	this.vnf_placement_2nd_vnf_outgoing.add(this.pricing_problem.addRange(-Double.MAX_VALUE, 1.0, constraint));
			    	cstrNum++;
			    }
			    //placement of last 2 VNFs incoming - (30) in SDN_NFV
			    cstrNum=0;//reset the constraint no. counter
			    //select the chain with 5 VNFs
			    for(int vnf_node_index=0; vnf_node_index < nodesNFVI.size(); vnf_node_index++){
			    	String constraint = "cstr30" + "_" + cstrNum;
			    	this.vnf_placement_last_2_sc_incoming.add(this.pricing_problem.addRange(0.0, Double.MAX_VALUE, constraint));
			    	cstrNum++;
			    }*/
			    
			    
			    
			    
			    //add P variables to the model
			    this.Used_var_p_sd_vi = new HashMap<Pp2VarP, IloIntVar>();
			    //add the columns for the variable x_sd_vi
			    this.var_p_sd_vi = new HashMap<Pp2VarP, IloColumn>();
			    //to impose or not the core capacity constraints
			    if(InputConstants.coreCstr){
				    //add the column for var_a_v_sigma to the core capacity constraint - (67)
				    for(int nfvNodeCount = 0; nfvNodeCount < nfv_nodes.size(); nfvNodeCount++){	   
				    	BaseVertex nfvNode = nfv_nodes.get(nfvNodeCount);
				    	//iterating through the list of traffic nodes
				    	for(TrafficNodes tn : sdPairsForSC){
					    	//iterate through the list of functions
					    	for(int f_seq_index = 0; f_seq_index < scSize; f_seq_index++){
					    		//get the function point
					    		FuncPt fp = scVNF.get(f_seq_index);
					    		//Function ID
					    		int fID = fp.getid();
					    		double fCore = fp.getcore();				    		
					    		//create the new object
					    		Pp2VarP temp_elem = new Pp2VarP(tn,f_seq_index,nfvNode);
					    		//get the column from the hashmap
					    		IloColumn col = this.var_p_sd_vi.get(temp_elem);
					    		//check if key exists
						    	if(col != null){
						    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
						    		col = col.and(this.pricing_problem.column(this.core_capacity.get(nfvNodeCount), fCore*tn.flow_traffic)); 
						    		//update the hashmap
						    		this.var_p_sd_vi.put(temp_elem, col);
						    	}else{
						    		//coefficient for var_a_v_sigma_i 
						    		this.var_p_sd_vi.put(temp_elem, this.pricing_problem.column(this.core_capacity.get(nfvNodeCount), fCore*tn.flow_traffic)); 
						    	}
					    	}
				    	}
				    }
			    }
			    //P with A constraint - (70)
			    for(Map.Entry<Pp2CstrAndOnP, IloRange> entryCstr : this.pAnd1WithA.entrySet()){
			    	TrafficNodes tn = entryCstr.getKey().tn;
			    	int f_seq = entryCstr.getKey().f_seq;
			    	BaseVertex nfviNode = entryCstr.getKey().nfviNode;
			    	//create the new object
			    	Pp2VarP temp_elem = new Pp2VarP(tn,f_seq,nfviNode);			    	
		    		//get the column from the hashmap
		    		IloColumn col = this.var_p_sd_vi.get(temp_elem);
		    		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
			    		col = col.and(this.pricing_problem.column(entryCstr.getValue(), 1.0)); 
			    		//update the hashmap
			    		this.var_p_sd_vi.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		this.var_p_sd_vi.put(temp_elem, this.pricing_problem.column(entryCstr.getValue(), 1.0)); 
			    	}
			    }
			    //P with Delta constraint - (71)
			    for(Map.Entry<Pp2CstrAndOnP, IloRange> entryCstr : this.pAnd2WithDelta.entrySet()){
			    	TrafficNodes tn = entryCstr.getKey().tn;
			    	int f_seq = entryCstr.getKey().f_seq;
			    	BaseVertex nfviNode = entryCstr.getKey().nfviNode;
			    	//create the new object
			    	Pp2VarP temp_elem = new Pp2VarP(tn,f_seq,nfviNode);			    	
		    		//get the column from the hashmap
		    		IloColumn col = this.var_p_sd_vi.get(temp_elem);
		    		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
			    		col = col.and(this.pricing_problem.column(entryCstr.getValue(), 1.0)); 
			    		//update the hashmap
			    		this.var_p_sd_vi.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		this.var_p_sd_vi.put(temp_elem, this.pricing_problem.column(entryCstr.getValue(), 1.0)); 
			    	}			    	
			    }
			    //P with A and Delta constraint - (72)
			    for(Map.Entry<Pp2CstrAndOnP, IloRange> entryCstr : this.pAnd3WithDeltaAndA.entrySet()){
			    	TrafficNodes tn = entryCstr.getKey().tn;
			    	int f_seq = entryCstr.getKey().f_seq;
			    	BaseVertex nfviNode = entryCstr.getKey().nfviNode;
			    	//create the new object
			    	Pp2VarP temp_elem = new Pp2VarP(tn,f_seq,nfviNode);			    	
		    		//get the column from the hashmap
		    		IloColumn col = this.var_p_sd_vi.get(temp_elem);
		    		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
			    		col = col.and(this.pricing_problem.column(entryCstr.getValue(), -1.0)); 
			    		//update the hashmap
			    		this.var_p_sd_vi.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		this.var_p_sd_vi.put(temp_elem, this.pricing_problem.column(entryCstr.getValue(), -1.0)); 
			    	}
			    	
			    }			    
			    //add 'P' variable to the set of variables
			    for(Map.Entry<Pp2VarP,IloColumn> entryP : this.var_p_sd_vi.entrySet()){
			    	String pName = "P_Src" + entryP.getKey().sd.v1.get_id() + "_Dst" + entryP.getKey().sd.v2.get_id() + "_Node" + entryP.getKey().nfviNode.get_id() + "_VnfSQ" + entryP.getKey().f_seq;
			    	this.Used_var_p_sd_vi.put(entryP.getKey(),this.pricing_problem.intVar(entryP.getValue(), 0, 1, pName));
			    }
			    
			    
			    
			    
			    
			    //add Q variables to the model
			    this.Used_var_q_sd_il = new HashMap<Pp2VarQ, IloIntVar>();
			    //add the columns for the variable y_sd_il
			    this.var_q_sd_il = new HashMap<Pp2VarQ, IloColumn>();
			    if(InputConstants.capacityCstr){
				    //link capacity constraint - (68)
				    link_num = 0;
				    //iterating over the links
					for(BaseVertex s_vert : g._vertex_list){ 
						for(BaseVertex t_vert :  g.get_adjacent_vertices(s_vert)){
							//iterating through the list of traffic nodes
					    	for(TrafficNodes tn : sdPairsForSC){
						    	//iterate through the list of functions
						    	for(int f_seq_index = 0; f_seq_index < scSize-1; f_seq_index++){
						    		//create the new object
							    	Pp2VarQ temp_elem = new Pp2VarQ(tn,f_seq_index,s_vert,t_vert,serviceChainID);			    	
						    		//get the column from the hashmap
						    		IloColumn col = this.var_q_sd_il.get(temp_elem);
						    		//check if key exists
							    	if(col != null){
							    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
							    		col = col.and(this.pricing_problem.column(this.link_capacity.get(link_num), tn.flow_traffic)); 
							    		//update the hashmap
							    		this.var_q_sd_il.put(temp_elem, col);
							    	}else{
							    		//coefficient for var_a_v_sigma_i 
							    		this.var_q_sd_il.put(temp_elem, this.pricing_problem.column(this.link_capacity.get(link_num), tn.flow_traffic)); 
							    	}
						    	}
					    	}
					    	//increment the link counter
							link_num++;
						}
					}
			    }
				//latency constraints
				for(int sdCount = 0; sdCount < sdPairsForSC.size(); sdCount++){
					TrafficNodes tn = sdPairsForSC.get(sdCount);
					//iterating over the links
					for(BaseVertex source : g._vertex_list){ 
						for(BaseVertex sink :  g.get_adjacent_vertices(source)){
							double lProp = InputConstants.SEC_TO_MICROSEC*g.get_edge_length(source, sink)/InputConstants.SPEED_OF_LIGHT;
							//iterate through the list of functions
					    	for(int f_seq_index = 0; f_seq_index < scSize-1; f_seq_index++){
					    		//create the new object
						    	Pp2VarQ temp_elem = new Pp2VarQ(tn,f_seq_index,source,sink,serviceChainID);			    	
					    		//get the column from the hashmap
					    		IloColumn col = this.var_q_sd_il.get(temp_elem);
					    		//check if key exists
						    	if(col != null){
						    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
						    		col = col.and(this.pricing_problem.column(this.latency_constraint.get(sdCount), lProp)); 
						    		//update the hashmap
						    		this.var_q_sd_il.put(temp_elem, col);
						    	}else{
						    		//coefficient for var_a_v_sigma_i 
						    		this.var_q_sd_il.put(temp_elem, this.pricing_problem.column(this.latency_constraint.get(sdCount), lProp)); 
						    	}
					    	}
						}
					}
				}
				//Q with B constraint - (73)
				for(Map.Entry<Pp2CstrAndOnQ, IloRange> entryCstr : this.qAnd1WithB.entrySet()){
					//create the new object
			    	Pp2VarQ temp_elem = new Pp2VarQ(entryCstr.getKey().sd,entryCstr.getKey().f_seq,entryCstr.getKey().srcL,entryCstr.getKey().tarL,serviceChainID);			    	
		    		//get the column from the hashmap
		    		IloColumn col = this.var_q_sd_il.get(temp_elem);
		    		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
			    		col = col.and(this.pricing_problem.column(entryCstr.getValue(), 1.0)); 
			    		//update the hashmap
			    		this.var_q_sd_il.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		this.var_q_sd_il.put(temp_elem, this.pricing_problem.column(entryCstr.getValue(), 1.0)); 
			    	}					
				}
				//Q with Delta constraint - (74)
				for(Map.Entry<Pp2CstrAndOnQ, IloRange> entryCstr : this.qAnd2WithDelta.entrySet()){
					//create the new object
			    	Pp2VarQ temp_elem = new Pp2VarQ(entryCstr.getKey().sd,entryCstr.getKey().f_seq,entryCstr.getKey().srcL,entryCstr.getKey().tarL,serviceChainID);			    	
		    		//get the column from the hashmap
		    		IloColumn col = this.var_q_sd_il.get(temp_elem);
		    		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
			    		col = col.and(this.pricing_problem.column(entryCstr.getValue(), 1.0)); 
			    		//update the hashmap
			    		this.var_q_sd_il.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		this.var_q_sd_il.put(temp_elem, this.pricing_problem.column(entryCstr.getValue(), 1.0)); 
			    	}					
				}
				//Q with B and Delta constraint - (75)
				for(Map.Entry<Pp2CstrAndOnQ, IloRange> entryCstr : this.qAnd3WithDeltaAndB.entrySet()){
					//create the new object
			    	Pp2VarQ temp_elem = new Pp2VarQ(entryCstr.getKey().sd,entryCstr.getKey().f_seq,entryCstr.getKey().srcL,entryCstr.getKey().tarL,serviceChainID);			    	
		    		//get the column from the hashmap
		    		IloColumn col = this.var_q_sd_il.get(temp_elem);
		    		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
			    		col = col.and(this.pricing_problem.column(entryCstr.getValue(), -1.0)); 
			    		//update the hashmap
			    		this.var_q_sd_il.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		this.var_q_sd_il.put(temp_elem, this.pricing_problem.column(entryCstr.getValue(), -1.0)); 
			    	}					
				}
				//add 'Q' variable to the set of variables
			    for(Map.Entry<Pp2VarQ,IloColumn> entryQ : this.var_q_sd_il.entrySet()){
			    	String qName = "Q_Src" + entryQ.getKey().sd.v1.get_id() + "_Dst" + entryQ.getKey().sd.v2.get_id() + "_VnfSQ" + entryQ.getKey().f_seq 
			    			+ "_sL" + entryQ.getKey().srcL.get_id() + "_sT" +  entryQ.getKey().tarL.get_id() ;
			    	this.Used_var_q_sd_il.put(entryQ.getKey(),this.pricing_problem.intVar(entryQ.getValue(), 0, 1, qName));
			    }
			    
			    
			    
			    
			    
			    //add A variables to the model
			    this.Used_var_a_vi = new HashMap<PpVarA, IloIntVar>();
			    //add the columns for the variable var_a_v_sigma
			    this.var_a_vi = new HashMap<PpVarA, IloColumn>();
		    	//add the column for var_a_v_sigma to the placement of sequential VNFs in a service chain - (77) in NFV nodes
		    	//modifying the for loop to account for consecutive VNFs
			    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
			    	BaseVertex nfvi_node = nodesNFVI.get(nfvi_node_index);	
			    	for(int vnf_index = 0 ; vnf_index < (ChainSet.get(serviceChainID).chain_size-1) ; vnf_index++){		
			    		 int f_i = vnf_index;
			    		 int f_i1 = vnf_index+1;
			    		 //create the new object
			    		 PpVarA temp_elem_1 = new PpVarA(nfvi_node,f_i);
			    		 //get the column from the hashmap
				    	 IloColumn col1  = this.var_a_vi.get(temp_elem_1);
			    		 //create the new object
			    		 PpVarA temp_elem_2 = new PpVarA(nfvi_node,f_i1);
			    		 //get the column from the hashmap
				    	 IloColumn col2  = this.var_a_vi.get(temp_elem_2);
				    	 if(col1 != null){
				    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
					    	col1 = col1.and(this.pricing_problem.column(this.vnf_placement_seq_2_nfv_nodes.get(nfvi_node_index*(ChainSet.get(serviceChainID).chain_size - 1) + vnf_index), -1.0));
					    	//update the hashmap
					    	this.var_a_vi.put(temp_elem_1, col1);
				    	 }else{
				    		//coefficient for var_a_v_sigma_i 
				    		this.var_a_vi.put(temp_elem_1, this.pricing_problem.column(this.vnf_placement_seq_2_nfv_nodes.get(nfvi_node_index*(ChainSet.get(serviceChainID).chain_size - 1) + vnf_index), -1.0));
				    	 }
				    	 if(col2 != null){
				    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
				    		col2 = col2.and(this.pricing_problem.column(this.vnf_placement_seq_2_nfv_nodes.get(nfvi_node_index*(ChainSet.get(serviceChainID).chain_size - 1) + vnf_index), 1.0));
				    		//update the hashmap
				    		this.var_a_vi.put(temp_elem_2, col2);
				    	 }else{
				    		 //coefficient for var_a_v_sigma_i+1
				    		 this.var_a_vi.put(temp_elem_2, this.pricing_problem.column(this.vnf_placement_seq_2_nfv_nodes.get(nfvi_node_index*(ChainSet.get(serviceChainID).chain_size - 1) + vnf_index), 1.0));
				    	 }		    		
			    	 }
			    }
			    //add the column for var_a_v_sigma to the VNF placement constraint - (69)
			    for(int vnf_index = 0 ; vnf_index < scSize; vnf_index++){
			    	 for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
			    		//Network node
			    		BaseVertex nfviNode = nodesNFVI.get(nfvi_node_index);	    		
			    		//create the new object
			    		PpVarA temp_elem = new PpVarA(nfviNode,vnf_index);
			    		//get the column from the hashmap
			    		IloColumn col = this.var_a_vi.get(temp_elem);
			    		//check if key exists
				    	if(col != null){
				    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
				    		col = col.and(this.pricing_problem.column(this.vnf_placement.get(vnf_index), 1.0)); 
				    		//update the hashmap
				    		this.var_a_vi.put(temp_elem, col);
				    	}else{
				    		//coefficient for var_a_v_sigma_i 
				    		this.var_a_vi.put(temp_elem, this.pricing_problem.column(this.vnf_placement.get(vnf_index), 1.0)); 
				    	}
			    	 }
			    }
			    //P with A constraint - (70)
			    for(Map.Entry<Pp2CstrAndOnP, IloRange> entryCstr : this.pAnd1WithA.entrySet()){
			    	TrafficNodes tn = entryCstr.getKey().tn;
			    	int f_seq = entryCstr.getKey().f_seq;
			    	BaseVertex nfviNode = entryCstr.getKey().nfviNode;
			    	//create the new object
		    		PpVarA temp_elem = new PpVarA(nfviNode,f_seq);
		    		//get the column from the hashmap
		    		IloColumn col = this.var_a_vi.get(temp_elem);
		    		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
			    		col = col.and(this.pricing_problem.column(entryCstr.getValue(), -1.0)); 
			    		//update the hashmap
			    		this.var_a_vi.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		this.var_a_vi.put(temp_elem, this.pricing_problem.column(entryCstr.getValue(), -1.0)); 
			    	}
			    }			  
			    //P with A and Delta constraint - (72)
			    for(Map.Entry<Pp2CstrAndOnP, IloRange> entryCstr : this.pAnd3WithDeltaAndA.entrySet()){
			    	TrafficNodes tn = entryCstr.getKey().tn;
			    	int f_seq = entryCstr.getKey().f_seq;
			    	BaseVertex nfviNode = entryCstr.getKey().nfviNode;
			    	//create the new object
		    		PpVarA temp_elem = new PpVarA(nfviNode,f_seq);
		    		//get the column from the hashmap
		    		IloColumn col = this.var_a_vi.get(temp_elem);
		    		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
			    		col = col.and(this.pricing_problem.column(entryCstr.getValue(), 1.0)); 
			    		//update the hashmap
			    		this.var_a_vi.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		this.var_a_vi.put(temp_elem, this.pricing_problem.column(entryCstr.getValue(), 1.0)); 
			    	}
			    	
			    }
			    //add the column for var_a_v_sigma to the placement of initial 2 VNFs incoming - (76)
			    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
			    	//since the first VNF in the service chain
			    	int first_vnf_index = 0;
			    	//Network node
		    		BaseVertex nfvi_node = nodesNFVI.get(nfvi_node_index);    		
		    		//create the new object
		    		PpVarA temp_elem = new PpVarA(nfvi_node,first_vnf_index);
		    		//get the column from the hashmap
		    		IloColumn col = this.var_a_vi.get(temp_elem);
		    		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
				    	col = col.and(this.pricing_problem.column(this.vnf_placement_first_2_sc_incoming.get(nfvi_node_index), 1.0));
				    	//update the hashmap
				    	this.var_a_vi.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		this.var_a_vi.put(temp_elem, this.pricing_problem.column(this.vnf_placement_first_2_sc_incoming.get(nfvi_node_index), 1.0));		    		
			    	}
			    }
			    //add the column for var_a_v_sigma to the placement of last VNF outgoing - (79)
			    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
			    	//since the last VNF in the service chain
			    	int last_vnf_index = ChainSet.get(serviceChainID).chain_size - 1;
			    	//Network node
		    		BaseVertex nfvi_node = nodesNFVI.get(nfvi_node_index);    	
		    		//create the new object
		    		PpVarA temp_elem = new PpVarA(nfvi_node,last_vnf_index);
		    		//get the column from the hashmap
		    		IloColumn col = this.var_a_vi.get(temp_elem);
		    		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
			    		col = col.and(this.pricing_problem.column(this.vnf_placement_last_2_sc_outgoing.get(nfvi_node_index), 1.0));
			    		//update the hashmap
			    		this.var_a_vi.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		this.var_a_vi.put(temp_elem, this.pricing_problem.column(this.vnf_placement_last_2_sc_outgoing.get(nfvi_node_index), 1.0));
			    	}
			    }
			   /* //add the column for var_a_v_sigma to the placement of initial 2 VNFs outgoing - (25)
			    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
			    	//since the first VNF in the service chain
			    	int first_vnf_index = 0;	    
			    	//Network node
		    		BaseVertex nfvi_node = nodesNFVI.get(nfvi_node_index);    		
		    		//create the new object
		    		PpVarA temp_elem = new PpVarA(nfvi_node,first_vnf_index);
		    		//get the column from the hashmap
		    		IloColumn col = this.var_a_vi.get(temp_elem);
		    		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
			    		col = col.and(this.pricing_problem.column(this.vnf_placement_first_2_sc_outgoing.get(nfvi_node_index), -1.0));
			    		//update the hashmap
			    		this.var_a_vi.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		this.var_a_vi.put(temp_elem, this.pricing_problem.column(this.vnf_placement_first_2_sc_outgoing.get(nfvi_node_index), -1.0));
			    	}    		
			    	//only if the chain size is greater than 1
			    	if(scSize > 1){
				    	//second VNF in the chain
				    	int second_vnf_index = 1;	    		  	
			    		//create the new object
			    		PpVarA temp_elem_nxt = new PpVarA(nfvi_node,second_vnf_index);
			    		//get the column from the hashmap
			    		IloColumn col_nxt = this.var_a_vi.get(temp_elem_nxt);
				    	//check if key exists
				    	if(col_nxt != null){
				    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
				    		col_nxt = col_nxt.and(this.pricing_problem.column(this.vnf_placement_first_2_sc_outgoing.get(nfvi_node_index), 1.0));
				    		//update the hashmap
				    		this.var_a_vi.put(temp_elem_nxt, col_nxt);
				    	}else{
				    		//coefficient for var_a_v_sigma_i 
				    		this.var_a_vi.put(temp_elem, this.pricing_problem.column(this.vnf_placement_first_2_sc_outgoing.get(nfvi_node_index), 1.0));
				    	}
			    	}
			    }
			    //add the column for var_a_v_sigma to the placement of 2nd VNF incoming - (26)
			    if(scSize > 1){
				    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
				    	//since the second VNF in the service chain
				    	int second_vnf_index = 1;
				    	//Network node
			    		BaseVertex nfvi_node = nodesNFVI.get(nfvi_node_index);
			    		//Function sequence ID
			    		int second_VNF_f_id = ChainSet.get(serviceChainID).chain_seq.get(second_vnf_index);
			    		//create the new object
			    		PpVarA temp_elem = new PpVarA(nfvi_node,second_VNF_f_id);
			    		//get the column from the hashmap
			    		IloColumn col = this.var_a_vi.get(temp_elem);
			    		//check if key exists
				    	if(col != null){
				    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
					    	col = col.and(this.pricing_problem.column(this.vnf_placement_2nd_vnf_outgoing.get(nfvi_node_index), 1.0));
					    	//update the hashmap
					    	this.var_a_vi.put(temp_elem, col);
				    	}else{
				    		//coefficient for var_a_v_sigma_i 
				    		this.var_a_vi.put(temp_elem, this.pricing_problem.column(this.vnf_placement_2nd_vnf_outgoing.get(nfvi_node_index), 1.0));
				    	}
				    }
			    }
			    //add the column for var_a_v_sigma to the placement of last 2 VNFs incoming - (30)
			    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
			    	//since the last VNF in the service chain
			    	int last_vnf_index = ChainSet.get(serviceChainID).chain_size - 1;
			    	//Network node
		    		BaseVertex nfvi_node = nodesNFVI.get(nfvi_node_index);    	
		    		//create the new object
		    		PpVarA temp_elem = new PpVarA(nfvi_node,last_vnf_index);
		    		//get the column from the hashmap
		    		IloColumn col = this.var_a_vi.get(temp_elem);
		      		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
				    	col = col.and(this.pricing_problem.column(this.vnf_placement_last_2_sc_incoming.get(nfvi_node_index), -1.0));
				    	//update the hashmap
				    	this.var_a_vi.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		this.var_a_vi.put(temp_elem, this.pricing_problem.column(this.vnf_placement_last_2_sc_incoming.get(nfvi_node_index), -1.0));
			    	}
		    		
			    	//if the service chain size is greater than 1
			    	if(scSize > 1){
			    		//since the last but one VNF in the service chain
				    	int penultimate_vnf_index = last_vnf_index - 1;		    		
			    		//create the new object
			    		PpVarA temp_elem_prev = new PpVarA(nfvi_node,penultimate_vnf_index);
			    		//get the column from the hashmap
			    		IloColumn col_prev = this.var_a_vi.get(temp_elem_prev);  
				    	//check if key exists
				    	if(col_prev != null){
				    		//coefficient for var_a_v_sigma_i-1 to the constraint //modify the column
					    	col_prev = col_prev.and(this.pricing_problem.column(this.vnf_placement_last_2_sc_incoming.get(nfvi_node_index), 1.0));
					    	//update the hashmap
					    	this.var_a_vi.put(temp_elem_prev, col_prev);
				    	}else{
				    		//coefficient for var_a_v_sigma_i 
				    		this.var_a_vi.put(temp_elem, this.pricing_problem.column(this.vnf_placement_last_2_sc_incoming.get(nfvi_node_index), 1.0));
				    	}
			    	}
			    }*/
			    //add the variable to the set of variables
			    for(Map.Entry<PpVarA,IloColumn> entry : this.var_a_vi.entrySet()){
			    	String aName = "A_Node" + entry.getKey().node.get_id() + "_VnfSQ" + entry.getKey().f_seq;
			    	this.Used_var_a_vi.put(entry.getKey(),this.pricing_problem.intVar(entry.getValue(), 0, 1, aName));
			    }
			    
			    
			    
			    
			    
			    
			    
			    
			    //add Delta variables to the model
			    this.Used_var_d_sd = new HashMap<Pp2VarDelta, IloIntVar>();
			    //add the columns for the variable var_delta_sd
			    this.var_d_sd = new HashMap<Pp2VarDelta, IloColumn>();
			    //P with Delta constraint - (71)
			    for(Map.Entry<Pp2CstrAndOnP, IloRange> entryCstr : this.pAnd2WithDelta.entrySet()){
			    	TrafficNodes tn = entryCstr.getKey().tn;			    
			    	//create the new object
			    	Pp2VarDelta temp_elem = new Pp2VarDelta(tn);			    	
		    		//get the column from the hashmap
		    		IloColumn col = this.var_d_sd.get(temp_elem);
		    		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
			    		col = col.and(this.pricing_problem.column(entryCstr.getValue(), -1.0)); 
			    		//update the hashmap
			    		this.var_d_sd.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		this.var_d_sd.put(temp_elem, this.pricing_problem.column(entryCstr.getValue(), -1.0)); 
			    	}			    	
			    }
			    //P with A and Delta constraint - (72)
			    for(Map.Entry<Pp2CstrAndOnP, IloRange> entryCstr : this.pAnd3WithDeltaAndA.entrySet()){
			    	TrafficNodes tn = entryCstr.getKey().tn;
			    	//create the new object
			    	Pp2VarDelta temp_elem = new Pp2VarDelta(tn);			    	
		    		//get the column from the hashmap
		    		IloColumn col = this.var_d_sd.get(temp_elem);
		    		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
			    		col = col.and(this.pricing_problem.column(entryCstr.getValue(), 1.0)); 
			    		//update the hashmap
			    		this.var_d_sd.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		this.var_d_sd.put(temp_elem, this.pricing_problem.column(entryCstr.getValue(), 1.0)); 
			    	}			    	
			    }
			    //Q with Delta constraint - (74)
				for(Map.Entry<Pp2CstrAndOnQ, IloRange> entryCstr : this.qAnd2WithDelta.entrySet()){
					//Traffic Node
					TrafficNodes tn = entryCstr.getKey().sd;			    
			    	//create the new object
			    	Pp2VarDelta temp_elem = new Pp2VarDelta(tn);			    	
		    		//get the column from the hashmap
		    		IloColumn col = this.var_d_sd.get(temp_elem);
		    		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
			    		col = col.and(this.pricing_problem.column(entryCstr.getValue(), -1.0)); 
			    		//update the hashmap
			    		this.var_d_sd.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		this.var_d_sd.put(temp_elem, this.pricing_problem.column(entryCstr.getValue(), -1.0)); 
			    	}					
				}
				//Q with B and Delta constraint - (75)
				for(Map.Entry<Pp2CstrAndOnQ, IloRange> entryCstr : this.qAnd3WithDeltaAndB.entrySet()){
					//Traffic Node
					TrafficNodes tn = entryCstr.getKey().sd;
			    	//create the new object
			    	Pp2VarDelta temp_elem = new Pp2VarDelta(tn);			    	
		    		//get the column from the hashmap
		    		IloColumn col = this.var_d_sd.get(temp_elem);
		    		//check if key exists
			    	if(col != null){
			    		//coefficient for var_a_v_sigma_i to the constraint //modify the column
			    		col = col.and(this.pricing_problem.column(entryCstr.getValue(), 1.0)); 
			    		//update the hashmap
			    		this.var_d_sd.put(temp_elem, col);
			    	}else{
			    		//coefficient for var_a_v_sigma_i 
			    		this.var_d_sd.put(temp_elem, this.pricing_problem.column(entryCstr.getValue(), 1.0)); 
			    	}					
				}
			    //add "Delta" variable to the set of variables			
			    for(Map.Entry<Pp2VarDelta,IloColumn> entryD : this.var_d_sd.entrySet()){
			    	String dName = "Delta_Src" + entryD.getKey().sd.v1.get_id() + "_Dst" + entryD.getKey().sd.v2.get_id();
			    	this.Used_var_d_sd.put(entryD.getKey(),this.pricing_problem.intVar(entryD.getValue(), 0, 1, dName));
			    }
			    
			    
			    
			    
			    
			    
			    //add B variables to the model
			    this.Used_var_b_l_sigma_i_i1 = new HashMap<PpVarB, IloIntVar>();
			    //add the columns for the variable b_l_sigma_i_sigma_i+1
			    this.var_b_l_sigma_i_i1 = new HashMap<PpVarB, IloColumn>();
				//Q with B constraint - (73)
				for(Map.Entry<Pp2CstrAndOnQ, IloRange> entryCstr : this.qAnd1WithB.entrySet()){
					int vnf_index = entryCstr.getKey().f_seq;
					BaseVertex sVrt = entryCstr.getKey().srcL;
					BaseVertex tVrt = entryCstr.getKey().tarL;
					//create the key
		   			PpVarB temp_elem = new PpVarB(serviceChainID,vnf_index,vnf_index+1,sVrt,tVrt);
		   			//get the column object if its exists
		   			IloColumn col = this.var_b_l_sigma_i_i1.get(temp_elem);	
		   		    //check if key exists
		    		if(col != null){
		    			//add the column object if it exists
		    			col = col.and(this.pricing_problem.column(entryCstr.getValue(), -1.0));
		    			//update the value in the hashmap
			    		this.var_b_l_sigma_i_i1.put(temp_elem, col);
		    		}else{
		    			//add the elements to the HashMap
		    			this.var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(entryCstr.getValue(), -1.0));
		    		}					
				}
				//Q with B and Delta constraint - (75)
				for(Map.Entry<Pp2CstrAndOnQ, IloRange> entryCstr : this.qAnd3WithDeltaAndB.entrySet()){
					int vnf_index = entryCstr.getKey().f_seq;
					BaseVertex sVrt = entryCstr.getKey().srcL;
					BaseVertex tVrt = entryCstr.getKey().tarL;
					//create the key
		   			PpVarB temp_elem = new PpVarB(serviceChainID,vnf_index,vnf_index+1,sVrt,tVrt);
		   			//get the column object if its exists
		   			IloColumn col = this.var_b_l_sigma_i_i1.get(temp_elem);	
		   		    //check if key exists
		    		if(col != null){
		    			//add the column object if it exists
		    			col = col.and(this.pricing_problem.column(entryCstr.getValue(), 1.0));
		    			//update the value in the hashmap
			    		this.var_b_l_sigma_i_i1.put(temp_elem, col);
		    		}else{
		    			//add the elements to the HashMap
		    			this.var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(entryCstr.getValue(), 1.0));
		    		}					
				}
			    //add the column for var_b_l_sigma_i_i1 to the placement of initial 2 VNFs incoming - (76)
			    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
			    	//target vertex for the link
			    	BaseVertex t_vrt = nodesNFVI.get(nfvi_node_index);;
			    	//for incoming links
				   	for(BaseVertex s_vrt : g.get_precedent_vertices(t_vrt)){
			    		//considering the first 2 functions in a service chain
			    		int s_f_index = 0;
			    		int t_f_index = 1;
			    		//create the key
			    		PpVarB temp_elem = new PpVarB(serviceChainID,s_f_index,t_f_index,s_vrt,t_vrt);
			    		//get the column object if its exists
			   			IloColumn col = this.var_b_l_sigma_i_i1.get(temp_elem);		    		
			    		//check if key exists
			    		if(col != null){
			    			//add the column to the constraint
			    			col = col.and(this.pricing_problem.column(this.vnf_placement_first_2_sc_incoming.get(nfvi_node_index), 1.0));
			    			//update the hashmap
			    			this.var_b_l_sigma_i_i1.put(temp_elem, col);  
			    		}else{				    		
				    	 	//add the elements to the HashMap
				    		this.var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_first_2_sc_incoming.get(nfvi_node_index), 1.0));
			    		}			    	
				   	}			   
			    }	    
			    //add the column for var_b_l_sigma_i_i1 to the placement of sequential VNFs in a service chain - (77) in NFV nodes - outgoing links from NFV node 
			    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
			    	//NFV node
			    	BaseVertex nfvi_node = nodesNFVI.get(nfvi_node_index);;
			    	//select the chain with 5 VNFs
				    for(int vnf_index = 0 ; vnf_index < (ChainSet.get(serviceChainID).chain_size - 1); vnf_index++){ // -1 because we are accounting for sequential VNFs in a service chain			    	
				    	//outoging links from NFV node   	
					   	for(BaseVertex t_vrt : g.get_adjacent_vertices(nfvi_node)){				    		
				   			//create the key
				   			PpVarB temp_elem = new PpVarB(serviceChainID,vnf_index,vnf_index+1,nfvi_node,t_vrt);
				   			//get the column object if its exists
				   			IloColumn col = this.var_b_l_sigma_i_i1.get(temp_elem);			    					    			
				    		//check if key exists
				    		if( col != null){
				    			//add the column to the constraint
				    			col = col.and(this.pricing_problem.column(this.vnf_placement_seq_2_nfv_nodes.get(nfvi_node_index*(ChainSet.get(serviceChainID).chain_size - 1) + vnf_index), 1.0));
				    			//update the HashMap
				    			this.var_b_l_sigma_i_i1.put(temp_elem, col);  
				    		}else{					    		
					    		//add the elements to the HashMap
					    		this.var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_seq_2_nfv_nodes.get(nfvi_node_index*(ChainSet.get(serviceChainID).chain_size - 1) + vnf_index), 1.0));
				    		}				    		
					   	}
					   	//incoming links to NFV node
						for(BaseVertex s_vrt : g.get_precedent_vertices(nfvi_node)){				    		
				   			//create the key
				   			PpVarB temp_elem = new PpVarB(serviceChainID,vnf_index,vnf_index+1,s_vrt,nfvi_node);
				   		    //get the column object if its exists
				   			IloColumn col = this.var_b_l_sigma_i_i1.get(temp_elem);
				    		//check if key exists
				    		if( col != null){
				    			//add the column to the constraint
				    			col = col.and(this.pricing_problem.column(this.vnf_placement_seq_2_nfv_nodes.get(nfvi_node_index*(ChainSet.get(serviceChainID).chain_size - 1) + vnf_index), -1.0));
				    			//update the HashMap
				    			this.var_b_l_sigma_i_i1.put(temp_elem, col);  
				    		}else{				    		
					    		//add the elements to the HashMap
					    		this.var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_seq_2_nfv_nodes.get(nfvi_node_index*(ChainSet.get(serviceChainID).chain_size - 1) + vnf_index), -1.0));
				    		}					    						    	
					   	}			    
				    }
			    }
			    //add the column for var_b_l_sigma_i_i1 to the placement of sequential VNFs in a service chain - (78) in non-NFV nodes
			    for(int non_nfv_node_index = 0; non_nfv_node_index < vertex_list_without_nfvi_nodes.size(); non_nfv_node_index++){		    	
			    	//non NFV node
			    	BaseVertex nonNFVnode = vertex_list_without_nfvi_nodes.get(non_nfv_node_index);		    	
			    	//select the chain with 5 VNFs
				    for(int vnf_index = 0 ; vnf_index < (ChainSet.get(serviceChainID).chain_size - 1); vnf_index++){ // -1 because we are accounting for sequential VNFs in a service chain	  	
			    	    //outgoing links
					   	for(BaseVertex t_vrt : g.get_adjacent_vertices(nonNFVnode)){				    		
				   			//create the key
				   			PpVarB temp_elem = new PpVarB(serviceChainID,vnf_index,vnf_index+1,nonNFVnode,t_vrt);
				   		    //get the column object if its exists
				   			IloColumn col = this.var_b_l_sigma_i_i1.get(temp_elem);			    			
				    		//check if key exists
				    		if(col != null){
				    			//add the column to the constraint
				    			col = col.and(this.pricing_problem.column(this.vnf_placement_seq_2_non_nfv_nodes.get(non_nfv_node_index*(ChainSet.get(serviceChainID).chain_size - 1) + vnf_index), 1.0));
				    			//update the HashMap
				    			this.var_b_l_sigma_i_i1.put(temp_elem, col);  
				    		}else{				    	
					    		//add the elements to the HashMap
					    		this.var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_seq_2_non_nfv_nodes.get(non_nfv_node_index*(ChainSet.get(serviceChainID).chain_size - 1) + vnf_index), 1.0));
				    		}
					   	}
					   	//incoming links
						for(BaseVertex s_vrt : g.get_precedent_vertices(nonNFVnode)){				    		
				   			//create the key
				   			PpVarB temp_elem = new PpVarB(serviceChainID,vnf_index,vnf_index+1,s_vrt,nonNFVnode);
				   		    //get the column object if its exists
				   			IloColumn col = this.var_b_l_sigma_i_i1.get(temp_elem);				    	
				    		//check if key exists
				    		if(col != null){
				    			//add the column to the constraint
				    			col = col.and(this.pricing_problem.column(this.vnf_placement_seq_2_non_nfv_nodes.get(non_nfv_node_index*(ChainSet.get(serviceChainID).chain_size - 1) + vnf_index), -1.0));
				    			//update the HashMap
				    			this.var_b_l_sigma_i_i1.put(temp_elem, col);  
				    		}else{					    		
					    		//add the elements to the HashMap
					    		this.var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_seq_2_non_nfv_nodes.get(non_nfv_node_index*(ChainSet.get(serviceChainID).chain_size - 1) + vnf_index), -1.0));
				    		}
					    						    	
					   	}			    
				    }
			    }
			    //add the column for var_b_l_sigma_i_i1 to the placement of last VNF outgoing - (79)
			    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
			    	//NFV node
			    	BaseVertex s_vrt = nodesNFVI.get(nfvi_node_index);;
			    	//outgoing links
			    	for(BaseVertex t_vrt : g.get_adjacent_vertices(s_vrt)){
			    		//considering the last 2 functions in a service chain
			    		int s_f_index = ChainSet.get(serviceChainID).chain_size - 2;
			    		int t_f_index = ChainSet.get(serviceChainID).chain_size - 1;
			    		//create the key
			    		PpVarB temp_elem = new PpVarB(serviceChainID,s_f_index,t_f_index,s_vrt,t_vrt);
			    		//get the column object if its exists
			   			IloColumn col = this.var_b_l_sigma_i_i1.get(temp_elem);		    		
			    		//check if key exists
			    		if(col != null){
			    			//add the column to the constraint
			    			col = col.and(this.pricing_problem.column(this.vnf_placement_last_2_sc_outgoing.get(nfvi_node_index), 1.0));
			    			//update the HashMap
			    			this.var_b_l_sigma_i_i1.put(temp_elem, col);  
			    		}else{		    		
				    		//add the elements to the HashMap
				    		this.var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_last_2_sc_outgoing.get(nfvi_node_index), 1.0));
			    		}		    	
			    	}		    
			    }
			    /*//add the column for var_b_l_sigma_i_i1 to the placement of initial 2 VNFs outgoing - (25)
			    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
			    	//source vertex for the link
			    	BaseVertex s_vrt = nodesNFVI.get(nfvi_node_index);
			    	//for outgoing links
			    	for(BaseVertex t_vrt : g.get_adjacent_vertices(s_vrt)){
			    		//considering the first 2 functions in a service chain
			    		int s_f_index = 0;
			    		int t_f_index = 1;	    	
			    		//create the key
			    		PpVarB temp_elem = new PpVarB(serviceChainID,s_f_index,t_f_index,s_vrt,t_vrt);
			    		//get the column object if its exists
			   			IloColumn col = this.var_b_l_sigma_i_i1.get(temp_elem);	
			   		    //check if key exists
			    		if(col != null){
			    			//add the column to the constraint
			    			col=col.and(this.pricing_problem.column(this.vnf_placement_first_2_sc_outgoing.get(nfvi_node_index), 1.0));
			    			//update the value in the Hashmap
				    		this.var_b_l_sigma_i_i1.put(temp_elem, col);
			    		}else{		    			
				    		//add the elements to the Hashmap
				    		this.var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_first_2_sc_outgoing.get(nfvi_node_index), 1.0));
			    		}		    		
			    	}		    
			    }
			    //add the column for var_b_l_sigma_i_i1 to the placement of 2nd VNF outgoing - (26) in SDN_NFV
			    //adjacent edges //outgoing
			    for(int nfvi_node_index = 0; nfvi_node_index < nodesNFVI.size(); nfvi_node_index++){
			    	//source vertex for the link
			    	BaseVertex s_vrt = nodesNFVI.get(nfvi_node_index);
			    	//for outgoing links
			    	for(BaseVertex t_vrt : g.get_adjacent_vertices(s_vrt)){
			    		//considering the first 2 functions in a service chain
			    		int s_f_index = 0;
			    		int t_f_index = 1;
			    		//create the key
			    		PpVarB temp_elem = new PpVarB(serviceChainID,s_f_index,t_f_index,s_vrt,t_vrt);
			    		//get the column object if its exists
			   			IloColumn col = this.var_b_l_sigma_i_i1.get(temp_elem);	
			   		    //check if key exists
			    		if(col != null){
			    			//add the column to the constraint
			    			col=col.and(this.pricing_problem.column(this.vnf_placement_2nd_vnf_outgoing.get(nfvi_node_index), 1.0));
			    			//update the value in the hashmap
				    		this.var_b_l_sigma_i_i1.put(temp_elem, col);
			    		}else{		    			
				    		//add the elements to the Hashmap
				    		this.var_b_l_sigma_i_i1.put(temp_elem, this.pricing_problem.column(this.vnf_placement_2nd_vnf_outgoing.get(nfvi_node_index), 1.0));
			    		}		    		
			    	}		    
			    }*/
			    //add "B" variable to the set of variables
			    for(Map.Entry<PpVarB, IloColumn> entry : this.var_b_l_sigma_i_i1.entrySet()){
			    	String bName = "B_Ls" + entry.getKey().s_vrt.get_id() + "_Ld" + entry.getKey().t_vrt.get_id() + "_vP" + entry.getKey().s_f_index + "_vN" + entry.getKey().t_f_index + "_SC" + entry.getKey().sc_index;
			    	this.Used_var_b_l_sigma_i_i1.put(entry.getKey(), this.pricing_problem.intVar(entry.getValue(), 0, 1, bName));
			    }
			    
		}

}
