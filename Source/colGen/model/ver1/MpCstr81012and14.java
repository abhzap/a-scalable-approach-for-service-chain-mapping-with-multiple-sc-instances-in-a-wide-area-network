package colGen.model.ver1;

import colGen.model.ver2.Pp2VarP;
import ILP.TrafficNodes;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class MpCstr81012and14 {
	
	 public int cstrNum;//constraint number
	 public int chainIndex;
	 public TrafficNodes tn;
	 public BaseVertex nfvi_node;// if source/destination (sd pair) is NFV node it cannot be assigned as nfv_node
	 
	 public MpCstr81012and14(int chainIndex, TrafficNodes tn, BaseVertex nfvi_node){
		this.chainIndex = chainIndex;
		this.tn = tn;
		this.nfvi_node = nfvi_node;
	 }
	 
	 public MpCstr81012and14(int cstrNum, int chainIndex, TrafficNodes tn, BaseVertex nfvi_node){
		this.cstrNum = cstrNum;
		this.chainIndex = chainIndex;
		this.tn = tn;
		this.nfvi_node = nfvi_node;
	 }
	 
	 @Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			MpCstr81012and14 o = (MpCstr81012and14) obj;
			if( (this.tn.v1.get_id()==o.tn.v1.get_id()) && (this.tn.v2.get_id()==o.tn.v2.get_id()) && (this.cstrNum==o.cstrNum) 
					&& (this.nfvi_node.get_id()==o.nfvi_node.get_id()) && (this.tn.chain_index==o.tn.chain_index)){
				result = true;
			}
		}
	    return result;
	}	

	@Override
	public int hashCode()
	{
	    return this.tn.hashCode() + this.nfvi_node.hashCode() + this.chainIndex + this.cstrNum;
	}
}
