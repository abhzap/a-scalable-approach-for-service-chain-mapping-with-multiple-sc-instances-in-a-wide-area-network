package colGen.model.ver2;

import ILP.TrafficNodes;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class Mp2CstrE {
	
	 public int chainIndex;
	 public TrafficNodes tn;
	 public BaseVertex vrt;
	 public int vnfPos;
	 
	 public Mp2CstrE(int chainIndex, TrafficNodes tn, BaseVertex vrt, int vnfPos){
		this.chainIndex = chainIndex;
		this.tn = tn;
		this.vrt = vrt;
		this.vnfPos = vnfPos;
	 }

}
