package colGen.model.ver2;

import ILP.TrafficNodes;

public class Pp2VarDelta {
	
	public TrafficNodes sd;

	public Pp2VarDelta(TrafficNodes sd){
		this.sd = sd;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			Pp2VarDelta o = (Pp2VarDelta) obj;
			if( (this.sd.v1.get_id()==o.sd.v1.get_id()) && (this.sd.v2.get_id()==o.sd.v2.get_id()) && (this.sd.chain_index==o.sd.chain_index)){
				result = true;
			}
		}
	    return result;
	}	

	@Override
	public int hashCode()
	{
	    return this.sd.hashCode();
	}
}
