package colGen.model.ver2;

import ILP.TrafficNodes;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class Pp2CstrAndOnQ {

	public TrafficNodes sd;	
    public int f_seq;
	public BaseVertex srcL;
	public BaseVertex tarL;	
    
    //create the AND operation constraint on Y
    public Pp2CstrAndOnQ(TrafficNodes sd, int f_seq, BaseVertex srcL, BaseVertex tarL){
    	this.sd = sd;
    	this.f_seq = f_seq;
    	this.srcL = srcL;    	
    	this.tarL = tarL;   	
    }
    
}
