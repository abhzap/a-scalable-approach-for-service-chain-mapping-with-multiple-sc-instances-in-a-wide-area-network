package colGen.model.ver1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ILP.NodePair;
import ILP.TrafficNodes;
import colGen.model.ver2.Pp2VarDelta;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class MpVarZ {

	public int sCiD;
	public NewIngEgConfigs completeConfig;
	public int cgConfig = 0;
	public static int totalNewConfigs = 0;
	//keeps track of Delta variables
	public Set<Pp2VarDelta> DeltaVarSet;
	//keeps track of A variables
	public String configDsc;
	//keeps track of B variables
	public Set<PpVarB> BVarSet;
	//keeps track of nodes and CPU cores required 
	public HashMap<BaseVertex,Double> nfvNodeCpuCores;
	//keeps track of links and bandwidth required
	public HashMap<NodePair,Double> linkBandwidth;
	
	//default constructor
	public MpVarZ(){
		this.completeConfig = null;
		//increment total number of new configurations
		totalNewConfigs = totalNewConfigs + 1;
		//set the cgConfig number 
		this.cgConfig = totalNewConfigs;
		//get Delta variables
		this.DeltaVarSet = new HashSet<Pp2VarDelta>();
		//get B variables
		this.BVarSet = new HashSet<PpVarB>();
		//create the new HashMap
		this.nfvNodeCpuCores = new HashMap<BaseVertex,Double>();
		//create the new HashMap
		this.linkBandwidth = new HashMap<NodePair,Double>();
	}
	
	//constructor for making a new MpVarZ from an old one
	/*public MpVarZ(MpVarZ varZ){
		this.sCiD = varZ.sCiD;
		this.cgConfig = varZ.cgConfig;
		//copy Delta variables
		this.DeltaVarSet = new HashSet<Pp2VarDelta>(varZ.DeltaVarSet);
		//get configuration description
		this.configDsc = varZ.configDsc;
		//copy B variables
		this.BVarSet = new HashSet<PpVarB>(varZ.BVarSet);
		//copy Maps
		this.nfvNodeCpuCores = new HashMap<BaseVertex,Double>(varZ.nfvNodeCpuCores);
		this.linkBandwidth = new HashMap<NodePair,Double>(varZ.linkBandwidth);
	}*/
	
	public MpVarZ(int serviceChainID, String configDsc, Set<Pp2VarDelta> DeltaVarSet, Set<PpVarB> BvarSet){
		this.sCiD = serviceChainID;
		this.completeConfig = null;
		//increment total number of new configurations
		totalNewConfigs = totalNewConfigs + 1;
		//set the cgConfig number 
		this.cgConfig = totalNewConfigs;
		//get Delta variables
		this.DeltaVarSet = new HashSet<Pp2VarDelta>(DeltaVarSet);
		//get A variables
		this.configDsc = configDsc;	
		//get B variables
		this.BVarSet = new HashSet<PpVarB>(BvarSet);
		//create the new HashMap
		this.nfvNodeCpuCores = new HashMap<BaseVertex,Double>();
		//create the new HashMap
		this.linkBandwidth = new HashMap<NodePair, Double>();
	}
	
	public MpVarZ(int serviceChainID, NewIngEgConfigs completeConfig, Set<Pp2VarDelta> DeltaVarSet, Set<PpVarB> BvarSet){
		this.sCiD = serviceChainID;
		this.completeConfig = completeConfig;
		//increment total number of new configurations
		totalNewConfigs = totalNewConfigs + 1;
		//set the cgConfig number 
		this.cgConfig = totalNewConfigs;
		//get Delta variables
		this.DeltaVarSet = new HashSet<Pp2VarDelta>(DeltaVarSet);
		//get A variables
		this.configDsc = "_" + completeConfig.ingeg.v1.get_id() + "_" ;
		for(BaseVertex vrt : this.completeConfig.cfg.config){
			this.configDsc = this.configDsc + vrt.get_id() + "_";
		}
		this.configDsc = this.configDsc + completeConfig.ingeg.v2.get_id();
		//get B variables
		this.BVarSet = new HashSet<PpVarB>(BvarSet);
		//create the new HashMap
		this.nfvNodeCpuCores = new HashMap<BaseVertex,Double>();
		//create the new HashMap
		this.linkBandwidth = new HashMap<NodePair, Double>();
	}
	
	public MpVarZ(int serviceChainID, NewIngEgConfigs completeConfig){
		this.sCiD = serviceChainID;
		this.completeConfig = completeConfig;
		//increment total number of new configurations
		totalNewConfigs = totalNewConfigs + 1;
		//set the cgConfig number 
		this.cgConfig = totalNewConfigs;
		this.configDsc = "_" + completeConfig.ingeg.v1.get_id() + "_" ;
		for(BaseVertex vrt : this.completeConfig.cfg.config){
			this.configDsc = this.configDsc + vrt.get_id() + "_";
		}
		this.configDsc = this.configDsc + completeConfig.ingeg.v2.get_id();
		//create the new HashMap
		this.nfvNodeCpuCores = new HashMap<BaseVertex,Double>();
		//create the new HashMap
		this.linkBandwidth = new HashMap<NodePair, Double>();
	}
	
	public MpVarZ(int serviceChainID, String configDsc){
		this.sCiD = serviceChainID;
		this.completeConfig = null;
		//increment total number of new configurations
		totalNewConfigs = totalNewConfigs + 1;
		//set the cgConfig number 
		this.cgConfig = totalNewConfigs;
		this.configDsc = configDsc;		
		//create the new HashMap
		this.nfvNodeCpuCores = new HashMap<BaseVertex,Double>();
		//create the new HashMap
		this.linkBandwidth = new HashMap<NodePair, Double>();
	}
	
	
	//repeat check
	public boolean configRepetition(int scID, Set<Pp2VarDelta> DeltaVarSet, String configDsc, Set<PpVarB> BvarSet){		
		boolean repetition = false;
		//check
		if( (this.sCiD==scID) && this.DeltaVarSet.equals(DeltaVarSet) &&
				(this.configDsc.equals(configDsc)) && this.BVarSet.equals(BVarSet) ){
			repetition = true;
		}
		return repetition;
	}
	
	//update the configuration description
	public void updateConfigDesc(String configDsc){
		this.configDsc = configDsc;
	}
	
	//update the core count HashMap
	public void addCoreCount(BaseVertex vrt, Double coreCount){
		if(this.nfvNodeCpuCores.get(vrt)!=null){
			Double nwCoreCount = this.nfvNodeCpuCores.get(vrt)+coreCount;
			this.nfvNodeCpuCores.put(vrt, nwCoreCount);
		}else{
			this.nfvNodeCpuCores.put(vrt, coreCount);
		}
	}	
	
	//update the link count HashMap
	public void addLinkCapacity(NodePair link, Double bwOccupation){
		if(this.linkBandwidth.get(link)!=null){
			this.linkBandwidth.put(link, this.linkBandwidth.get(link)+bwOccupation);
		}else{
			this.linkBandwidth.put(link, bwOccupation);
		}
	}	
	
	//update service chain ID
	public void updateSCId(int sCid){
		this.sCiD = sCid;
	}
	
	//add B variables
	public void addBVars(Set<PpVarB> varBCollection){
		this.BVarSet.addAll(varBCollection);
	}
	
	//add Delta variable
	public void addDeltaVar(TrafficNodes sd){
		this.DeltaVarSet.add(new Pp2VarDelta(sd));
	}
	
	
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			MpVarZ o = (MpVarZ) obj;
			if(  (this.sCiD==o.sCiD) && (this.configDsc.equals(o.configDsc)) && (this.cgConfig==o.cgConfig) && 
					this.DeltaVarSet.equals(o.DeltaVarSet) && this.BVarSet.equals(o.BVarSet) ){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.sCiD + this.cgConfig + this.configDsc.hashCode() +  
	    		this.DeltaVarSet.hashCode() + this.BVarSet.hashCode();
	}
}
