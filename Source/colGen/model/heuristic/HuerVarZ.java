package colGen.model.heuristic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ILP.NodePair;
import ILP.TrafficNodes;
import colGen.model.ver1.MpVarZ;
import colGen.model.ver1.PpVarA;
import colGen.model.ver1.PpVarB;
import colGen.model.ver2.Pp2VarDelta;
import edu.asu.emit.qyan.alg.model.Vertex;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class HuerVarZ extends MpVarZ{
	
	//Keep track of first VNF //Keep track of last VNF
	public BaseVertex firstVNF;
	public BaseVertex lastVNF;
	//keeps track of A variables
    public Set<PpVarA> AVarSet;
    //keeps track of bandwidth used by each (s,d) pair
    public Map<TrafficNodes,Double> bwPerSD;    
    
    //declare the constructor
    public HuerVarZ(){
    	this.AVarSet = new HashSet<PpVarA>();
    	this.bwPerSD = new HashMap<TrafficNodes,Double>();
    }
    
    //copy a Z object to create a new one
    public HuerVarZ(MpVarZ config){
    	//Z object fields //inherited fields
    	this.sCiD = config.sCiD;
    	//copy Delta variables
		this.DeltaVarSet = new HashSet<Pp2VarDelta>(config.DeltaVarSet);	
		//copy B variables
		this.BVarSet = new HashSet<PpVarB>(config.BVarSet);
		//get configuration description
		this.configDsc = config.configDsc;
		//declare new set of 
		this.AVarSet = new HashSet<PpVarA>();
		//add the set of A variables
		String [] vnfLocations = configDsc.split("_");
		//make the A objects
		for(int i=1; i<vnfLocations.length; i++){
			//make the vertex
			BaseVertex vrt = new Vertex(Integer.valueOf(vnfLocations[i]));
			//make the A object
			PpVarA tempA = new PpVarA(vrt,i);
			//add to A VarSet
			this.AVarSet.add(tempA);
		}
		//copy Maps
		this.nfvNodeCpuCores = new HashMap<BaseVertex,Double>(config.nfvNodeCpuCores);
		this.linkBandwidth = new HashMap<NodePair,Double>(config.linkBandwidth);
    }
    
    //copy old object to create a new one
    public HuerVarZ(HuerVarZ config, Set<Pp2VarDelta> varDGroups){
    	//Z object fields //inherited fields
	    	this.sCiD = config.sCiD;
			this.cgConfig = config.cgConfig;
			//copy Delta variables
			this.DeltaVarSet = new HashSet<Pp2VarDelta>(varDGroups);
			//get configuration description
			this.configDsc = config.configDsc;
			//copy B variables
			this.BVarSet = new HashSet<PpVarB>(config.BVarSet);
			//copy Maps
			this.nfvNodeCpuCores = new HashMap<BaseVertex,Double>(config.nfvNodeCpuCores);
			this.linkBandwidth = new HashMap<NodePair,Double>(config.linkBandwidth);
    	//copy first and last VNF location
    	this.firstVNF = config.firstVNF;
    	this.lastVNF = config.lastVNF;		
    	//copy set
    	this.AVarSet = new HashSet<PpVarA>(config.AVarSet);
    	//copy map
    	this.bwPerSD = new HashMap<TrafficNodes,Double>(config.bwPerSD);
    }
    
    
    //copy old object to create a new one
    public HuerVarZ(HuerVarZ config){
    	//Z object fields //inherited fields
	    	this.sCiD = config.sCiD;
			this.cgConfig = config.cgConfig;
			//copy Delta variables
			this.DeltaVarSet = new HashSet<Pp2VarDelta>(config.DeltaVarSet);
			//get configuration description
			this.configDsc = config.configDsc;
			//copy B variables
			this.BVarSet = new HashSet<PpVarB>(config.BVarSet);
			//copy Maps
			this.nfvNodeCpuCores = new HashMap<BaseVertex,Double>(config.nfvNodeCpuCores);
			this.linkBandwidth = new HashMap<NodePair,Double>(config.linkBandwidth);
    	//copy first and last VNF location
    	this.firstVNF = config.firstVNF;
    	this.lastVNF = config.lastVNF;		
    	//copy set
    	this.AVarSet = new HashSet<PpVarA>(config.AVarSet);
    	//copy map
    	this.bwPerSD = new HashMap<TrafficNodes,Double>(config.bwPerSD);
    }
    
    //add A variables
    public void addAVar(PpVarA varA){
    	this.AVarSet.add(varA);
    }
    
    //update first VNF
    public void updateFirstVNF(BaseVertex firstVNF){
    	this.firstVNF = firstVNF;
    }
    
    //update last VNF
    public void updateLastVNF(BaseVertex lastVNF){
    	this.lastVNF = lastVNF;
    }
    
    //update the configuration number
    public void updateConfigNum(int configNum){
    	this.cgConfig = configNum;
    }
    
    @Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			MpVarZ o = (MpVarZ) obj;
			if(  (this.sCiD==o.sCiD) && (this.configDsc.equals(o.configDsc)) && 
					this.DeltaVarSet.equals(o.DeltaVarSet) && this.BVarSet.equals(o.BVarSet) ){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.sCiD + this.configDsc.hashCode() +  
	    		this.DeltaVarSet.hashCode() + this.BVarSet.hashCode();
	}
    
}
