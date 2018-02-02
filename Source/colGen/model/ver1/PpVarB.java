package colGen.model.ver1;

import edu.asu.emit.qyan.alg.model.Vertex;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class PpVarB {
	// source and target vertex define a link
	public BaseVertex s_vrt; // source vertex
    public BaseVertex t_vrt; // target vertex
    public int s_f_index; // source function index // index of function on the source vertex in the service chain  
    public int t_f_index; // target function index // index of function on the target vertex in the service chain
    public int sc_index;//to indicate which service chain index is represented
    
    public PpVarB(int sc_index, int s_f_index, int t_f_index, BaseVertex s_vrt, BaseVertex t_vrt ){
    	this.sc_index = sc_index;
    	this.s_f_index = s_f_index;
    	this.t_f_index = t_f_index;
    	this.s_vrt = s_vrt;
    	this.t_vrt = t_vrt;    	
    }
    
    @Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			PpVarB o = (PpVarB) obj;
			if( (this.s_vrt.get_id()==o.s_vrt.get_id()) && (this.t_vrt.get_id()==o.t_vrt.get_id()) && (this.s_f_index==o.s_f_index) && (this.t_f_index==o.t_f_index) && (this.sc_index==o.sc_index)  ){
				result = true;
			}
		}
	    return result;
	}	

	@Override
	public int hashCode()
	{
	    return this.s_vrt.hashCode() + this.t_vrt.hashCode() + this.s_f_index + this.t_f_index + this.sc_index;
	}
    
    public boolean checkEquality(int sc_index, int s_f_index, int t_f_index, BaseVertex s_vrt, BaseVertex t_vrt){
    	if( (this.sc_index == sc_index) && (this.s_f_index==s_f_index) && (this.t_f_index==t_f_index) && (this.s_vrt.get_id()==s_vrt.get_id()) && (this.t_vrt.get_id()==t_vrt.get_id()) ){
    		return true;
    	}else{
    		return false;
    	}
    }
}
