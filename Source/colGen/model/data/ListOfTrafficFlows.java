package colGen.model.data;

import java.util.ArrayList;

import ILP.TrafficNodes;

public class ListOfTrafficFlows implements java.io.Serializable {
	
	public ArrayList<TrafficNodes> trafficFlows;
	
	public ListOfTrafficFlows(ArrayList<TrafficNodes> trafficFlows){
		this.trafficFlows = new ArrayList<TrafficNodes>();
		for(TrafficNodes tn : trafficFlows){
			this.trafficFlows.add(new TrafficNodes(tn.v1,tn.v2,tn.chain_index,tn.flow_traffic));
		}
	}

}
