package colGen.model.ver1;

import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class MpVarXc {
	   public int scID;
	   public BaseVertex v;  
//	   public int f_id; //function identifier
	   public int f_seq;
	   
	   public MpVarXc(int scID, BaseVertex v, int f_seq){
		   this.scID = scID;
		   this.v = v;	  
//		   this.f_id = f_id;
		   this.f_seq = f_seq;
	   }
	   
	   @Override
		public boolean equals(Object obj)
		{
			boolean result = false;
			if(obj == null || obj.getClass() != getClass()){
				result = false;
			} else {
				MpVarXc o = (MpVarXc) obj;
				if( (this.scID==o.scID) && (this.f_seq==o.f_seq) && (this.v.get_id()==o.v.get_id()) ){
					result = true;
				}
			}
		    return result;
		}	
		
		@Override
		public int hashCode()
		{
		    return this.v.hashCode() + this.f_seq;
		}

}
