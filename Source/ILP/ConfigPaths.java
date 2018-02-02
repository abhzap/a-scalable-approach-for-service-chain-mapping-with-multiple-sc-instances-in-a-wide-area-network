package ILP;

import java.util.ArrayList;
import java.util.List;

import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class ConfigPaths {
	public ArrayList<BaseVertex> node_seq;
	public List<Path> config_routes;
	
	public ConfigPaths(ArrayList<BaseVertex> node_seq){
		  this.node_seq = node_seq;
		  this.config_routes = new ArrayList<Path>();
	}
	
}
