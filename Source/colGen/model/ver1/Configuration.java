package colGen.model.ver1;

import java.util.ArrayList;
import java.util.List;

import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class Configuration {
	
	public ArrayList<BaseVertex> config;
	public ArrayList<Path> config_routes;
	
	public Configuration(ArrayList<BaseVertex> config, List<Path> config_routes){		
		this.config = config;
		this.config_routes = (ArrayList<Path>)config_routes;
	}
	
	public Configuration(ArrayList<BaseVertex> config){		
		this.config = new ArrayList<BaseVertex>(config);
		this.config_routes = null;
	}
	
	public ArrayList<BaseVertex> getConfiguration(){
		return this.config;
	}
	
	public ArrayList<Path> getPaths(){
		return this.getPaths();
	}

}
