package ILP;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class TrafficNodes extends NodePair implements java.io.Serializable{
	
	public BaseVertex v1;
	public BaseVertex v2;	
	public int chain_index;
	public double flow_traffic; //expressed in Mbps
	
	public TrafficNodes(){
		
	}	
	
	public TrafficNodes(int chain_index){
		this.chain_index = chain_index;
	}
	
	public TrafficNodes(BaseVertex v1, BaseVertex v2){
		   this.v1 = v1;
		   this.v2 = v2;		  
		   this.flow_traffic = 0;
	}
	
	public TrafficNodes(BaseVertex v1, BaseVertex v2, int chain_index){
		   this.v1 = v1;
		   this.v2 = v2;
		   this.chain_index = chain_index;
		   this.flow_traffic = 0;
	}
	
	public TrafficNodes(BaseVertex v1, BaseVertex v2, int chain_index, double flow_traffic){
		   this.v1 = v1;
		   this.v2 = v2;
		   this.chain_index = chain_index;
		   this.flow_traffic = flow_traffic;
	}
	
	
	
	//since TrafficNodes is used as a key in a HashMap
	@Override
	public boolean equals(Object obj){
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			TrafficNodes o = (TrafficNodes) obj;
			if( (this.v1.get_id()==o.v1.get_id()) && (this.v2.get_id()==o.v2.get_id()) && (this.chain_index==o.chain_index)){
				result = true;
			}
		}
	    return result;		
	}	
	@Override
	public int hashCode(){
		return this.v1.hashCode() + this.v2.hashCode() + this.chain_index;			
	}
	
	public boolean equals(NodePair np){
		return (this.v1.get_id()==np.v1.get_id())&&(this.v2.get_id()==np.v2.get_id());
	}
	
	public boolean equals(TrafficNodes tn){
		return (this.v1.get_id()==tn.v1.get_id())&&(this.v2.get_id()==tn.v2.get_id())&&(this.chain_index==tn.chain_index);
	}
	
	public Integer getChainIndex(){
		   return this.chain_index;
	}
	
	public void updateChainIndex(int chain_index){
		   this.chain_index = chain_index;
	}
	
	public void updateTraffic(double flow_traffic){
		   this.flow_traffic=flow_traffic;
	}
	
	public void addTraffic(double flowTraffic){
		this.flow_traffic += flowTraffic;
	}
	
	public double getTraffic(){
		   return this.flow_traffic;
	}
	
	public String toString(){
		   return String.format(this.v1.get_id() + ", " + this.v2.get_id() + " : " + this.chain_index) ;				   
	}	
	
}
