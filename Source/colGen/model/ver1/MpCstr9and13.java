package colGen.model.ver1;

import ILP.TrafficNodes;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class MpCstr9and13 {
	
	public int cstrNum;
	public int chainIndex;
	public TrafficNodes tn;
	public BaseVertex node; //node cannot be a NFV node and the source(9)/destination(13) 
	
	public MpCstr9and13(int chainIndex, TrafficNodes tn, BaseVertex node){
		this.chainIndex = chainIndex;
		this.tn = tn;
		this.node = node;
	}
	
	public MpCstr9and13(int cstrNum, int chainIndex, TrafficNodes tn, BaseVertex node){
		this.cstrNum = cstrNum;
		this.chainIndex = chainIndex;
		this.tn = tn;
		this.node = node;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			MpCstr9and13 o = (MpCstr9and13) obj;
			if( (this.tn.v1.get_id()==o.tn.v1.get_id()) && (this.tn.v2.get_id()==o.tn.v2.get_id()) && (this.cstrNum==o.cstrNum) 
					&& (this.node.get_id()==o.node.get_id()) && (this.chainIndex==o.chainIndex) && (this.tn.chain_index==o.tn.chain_index)){
				result = true;
			}
		}
	    return result;
	}	

	@Override
	public int hashCode()
	{
	    return this.tn.hashCode() + this.node.hashCode() + this.chainIndex + this.cstrNum;
	}
}
