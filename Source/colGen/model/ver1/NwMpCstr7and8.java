package colGen.model.ver1;

import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class NwMpCstr7and8 {
	
	public int scID;
	public int fSeq;
	public BaseVertex nfviNode;
	
	//constructor for making Constraint 16 and 17 object
	public NwMpCstr7and8(int scID, int fSeq, BaseVertex nfvNode){
		this.scID = scID;
		this.fSeq = fSeq;
		this.nfviNode = nfvNode;
	}
	
}
