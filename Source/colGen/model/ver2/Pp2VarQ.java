package colGen.model.ver2;

import ILP.TrafficNodes;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class Pp2VarQ {
	
	public TrafficNodes sd;
	public int f_seq;
	public BaseVertex srcL;
	public BaseVertex tarL;
	public int scID;
	
	public Pp2VarQ(TrafficNodes sd, int f_seq, BaseVertex srcL, BaseVertex tarL, int scID){
		this.sd = sd;		
		this.f_seq = f_seq;
		this.srcL = srcL;
		this.tarL = tarL;
		this.scID = scID;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			Pp2VarQ o = (Pp2VarQ) obj;
			if( (this.sd.v1.get_id()==o.sd.v1.get_id()) && (this.sd.v2.get_id()==o.sd.v2.get_id()) 
					&& (this.f_seq==o.f_seq) && (this.srcL.get_id()==o.srcL.get_id()) && (this.tarL.get_id()==o.tarL.get_id()) && (this.scID==o.scID) ){
				result = true;
			}
		}
	    return result;
	}	

	@Override
	public int hashCode()
	{
	    return this.sd.hashCode() + this.srcL.hashCode()  + this.tarL.hashCode() + this.f_seq + this.scID;
	}

}
