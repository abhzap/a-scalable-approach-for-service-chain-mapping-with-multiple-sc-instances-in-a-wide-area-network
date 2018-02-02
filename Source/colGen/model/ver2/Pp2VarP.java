package colGen.model.ver2;

import ILP.TrafficNodes;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class Pp2VarP {
	
	public TrafficNodes sd;	
	public int f_seq;
	public BaseVertex nfviNode;
	
	public Pp2VarP(TrafficNodes sd, int f_seq, BaseVertex nfviNode){
		this.sd = sd;
		this.f_seq = f_seq;
		this.nfviNode = nfviNode;		
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			Pp2VarP o = (Pp2VarP) obj;
			if( (this.sd.v1.get_id()==o.sd.v1.get_id()) && (this.sd.v2.get_id()==o.sd.v2.get_id()) && (this.f_seq==o.f_seq) 
					&& (this.nfviNode.get_id()==o.nfviNode.get_id()) && (this.sd.chain_index==o.sd.chain_index)){
				result = true;
			}
		}
	    return result;
	}	

	@Override
	public int hashCode()
	{
	    return this.sd.hashCode() + this.nfviNode.hashCode() + this.f_seq;
	}	

}
