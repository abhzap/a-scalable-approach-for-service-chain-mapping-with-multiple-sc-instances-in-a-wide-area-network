package colGen.model.ver1;

import java.util.ArrayList;

import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;
import ILP.NodePair;

public class IngressEgressConfigs {	
	NodePair ingeg;
	ArrayList<Configuration> configs;
	
	public IngressEgressConfigs(){		
	}
	
	public IngressEgressConfigs(NodePair ingeg){
		this.ingeg = ingeg;
		this.configs = new ArrayList<Configuration>();		
	}	

}
