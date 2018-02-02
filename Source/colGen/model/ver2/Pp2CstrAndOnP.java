package colGen.model.ver2;

import ILP.TrafficNodes;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class Pp2CstrAndOnP {
	
	public TrafficNodes tn;
	public BaseVertex nfviNode;
    public int f_seq;
    
    //create the AND operation constraint on X
    public Pp2CstrAndOnP(TrafficNodes tn, BaseVertex nfviNode, int f_seq){
    	this.tn = tn;
    	this.nfviNode = nfviNode;
    	this.f_seq = f_seq;
    }
}
