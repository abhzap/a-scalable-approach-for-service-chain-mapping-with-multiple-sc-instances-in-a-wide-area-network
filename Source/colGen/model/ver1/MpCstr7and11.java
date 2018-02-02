package colGen.model.ver1;

import colGen.model.ver2.Pp2VarP;
import ILP.TrafficNodes;

public class MpCstr7and11 {
	 
	 public int cstrNum;
	 public int chainIndex;
	 public TrafficNodes tn;
	 
	 public MpCstr7and11(int chainIndex, TrafficNodes tn){		 
		 this.chainIndex = chainIndex;
		 this.tn = tn;
	 }
	 
	 public MpCstr7and11(int cstrNum, int chainIndex, TrafficNodes tn){
		 this.cstrNum = cstrNum;
		 this.chainIndex = chainIndex;
		 this.tn = tn;
	 }
	 
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			MpCstr7and11 o = (MpCstr7and11) obj;
			if( (this.tn.v1.get_id()==o.tn.v1.get_id()) && (this.tn.v2.get_id()==o.tn.v2.get_id()) && (this.cstrNum==o.cstrNum) 
					&& (this.chainIndex==o.chainIndex) && (this.tn.chain_index==o.tn.chain_index)){
				result = true;
			}
		}
	    return result;
	}	

	@Override
	public int hashCode()
	{
	    return this.tn.hashCode() + this.chainIndex + this.cstrNum;
	}
}
