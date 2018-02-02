package colGen.model.ver1;

import ILP.IngressEgressConfiguration;
import ILP.TrafficNodes;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class MpVarY {

	public TrafficNodes tn; // sd pair for traffic	
	public int f_id; // function identifier //whether first or last VNF
	public BaseVertex s_vrt; // source vertex
	public BaseVertex t_vrt; // target vertex
	
	public MpVarY(TrafficNodes tn, int f_id, BaseVertex s_vrt, BaseVertex t_vrt){
		this.tn = tn;		
		this.f_id = f_id;
		this.s_vrt = s_vrt;
		this.t_vrt = t_vrt;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		}else{
			MpVarY o = (MpVarY) obj;
			if( this.tn.equals(o.tn)&&(this.f_id==o.f_id)&&(this.s_vrt.get_id()==o.s_vrt.get_id())&&(this.t_vrt.get_id()==o.t_vrt.get_id())){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.tn.hashCode() + this.f_id + this.s_vrt.hashCode() + this.t_vrt.hashCode();
	}
	
	
	
	
		
	// to check for objects that have incoming links	
	public boolean incomingLink(MpVarY obj){
		//if traffic nodes are same
		//if service chains are same
		//if target vertices are same
		if(this.tn.equals(obj.tn) && this.t_vrt.equals(obj.t_vrt) && (this.f_id == obj.f_id) ){		
		   return true;
		}else{
		   return false;
		}
	}
	
	// to check for objects that have outgoing links
	public boolean outgoingLink(MpVarY obj){
		//if traffic nodes are same
		//if service chains are same
		//if source vertices are same
		if(this.tn.equals(obj.tn) && this.s_vrt.equals(obj.s_vrt) && (this.f_id == obj.f_id) ){		
		   return true;
		}else{
		   return false;
		}
	}
}
