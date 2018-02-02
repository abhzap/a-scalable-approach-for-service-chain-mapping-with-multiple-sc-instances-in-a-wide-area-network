package ILP;

import java.util.ArrayList;
import java.util.List;

import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class IngressEgressConfiguration {	
	
	public NodePair ingeg;
	public ArrayList<BaseVertex> config;
	public List<Path> config_routes;
	public int shortest_path_length;
	
	public IngressEgressConfiguration(BaseVertex ingress_node, BaseVertex egress_node){
		this.ingeg = new NodePair(ingress_node,egress_node);		
		this.config = new ArrayList<BaseVertex>();
		this.config_routes = new ArrayList<Path>();
	}
	
	public IngressEgressConfiguration(BaseVertex ingress_node, BaseVertex egress_node, ArrayList<BaseVertex> config){
		this.ingeg = new NodePair(ingress_node,egress_node);		
		this.config = new ArrayList<BaseVertex>(config);
		this.config_routes = new ArrayList<Path>();
	}
	
	public boolean equals(IngressEgressConfiguration ingegconfig){
		boolean ingeg_flag = false;
		boolean config_flag = true;
		if(this.ingeg.v1.get_id()==ingegconfig.ingeg.v1.get_id() && this.ingeg.v2.get_id()==ingegconfig.ingeg.v2.get_id()){
			ingeg_flag = true;
		}
		for(int index=0; index < this.config.size(); index++){			
			if(this.config.get(index).get_id() != ingegconfig.config.get(index).get_id()){
				config_flag = false;
				break;
			}			
		}
		if(ingeg_flag && config_flag){
			return true;
		}else{
			return false;
		}
	}
	
	
	public void setShortestPathLength(){
		this.shortest_path_length = this.config_routes.get(0).get_vertices().size()-1;
	}
}
