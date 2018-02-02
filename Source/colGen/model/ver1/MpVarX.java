package colGen.model.ver1;

import ILP.FuncPt;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class MpVarX {  
	
   public int cstrNum;
   public BaseVertex v;  
   public int f_id; //function identifier
   
   public MpVarX(BaseVertex v, int f_id){
	   this.v = v;	  
	   this.f_id = f_id;
   }
   
   public MpVarX(int cstrNum, BaseVertex v, int f_id){
	   this.cstrNum = cstrNum;
	   this.v = v;	  
	   this.f_id = f_id;
   }
   
   @Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			MpVarX o = (MpVarX) obj;
			if( (this.f_id==o.f_id)&&(this.v.get_id()==o.v.get_id()) ){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.v.hashCode() + this.f_id;
	}
}
