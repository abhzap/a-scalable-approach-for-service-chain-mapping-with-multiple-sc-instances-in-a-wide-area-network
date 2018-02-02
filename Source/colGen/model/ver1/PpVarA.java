package colGen.model.ver1;

import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class PpVarA {

	public BaseVertex node;//NFV node
//	public int f_id;//function id
	public int f_seq;//function seq
	
	public PpVarA(BaseVertex node, int f_seq){
		this.node = node;//assign the node
		this.f_seq = f_seq;//assign the function ID
	}
	
  	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			PpVarA o = (PpVarA) obj;
			if( (this.f_seq==o.f_seq) && (this.node.get_id()==o.node.get_id()) ){
				result = true;
			}
		}
	    return result;
	}	

	@Override
	public int hashCode()
	{
	    return this.node.hashCode() + this.f_seq;
	}
	
	
	public boolean checkEquality(BaseVertex node, int f_seq){
    	if( (this.node.get_id()==node.get_id()) && (this.f_seq==f_seq) ){
    		return true;
    	}else{
    		return false;
    	}
	}
}
