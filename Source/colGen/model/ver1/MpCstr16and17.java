package colGen.model.ver1;

import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class MpCstr16and17 {

	public int functionID;
	public BaseVertex nfviNode;
	
	//constructor for making Constraint 16 and 17 object
	public MpCstr16and17(int functionID, BaseVertex nfvNode){
		this.functionID = functionID;
		this.nfviNode = nfvNode;
	}
}
