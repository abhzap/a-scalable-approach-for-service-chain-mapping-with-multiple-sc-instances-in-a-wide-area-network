package colGen.model.heuristic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.heuristic.ClusterHeuristic.ClusterTrafficPairs;
import colGen.model.preprocess.PreProcVer1;
import colGen.model.preprocess.placeNFVI;
import colGen.model.preprocess.preProcFunctions;
import colGen.model.trafficGen.TrafficGenerator;
import edu.asu.emit.qyan.alg.control.YenTopKShortestPathsAlg;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class TestHeuristic {
	
	public static void runTest() throws Exception{	
		
		//generate the graph object
	    Graph g = PreProcVer1.makeGraphObject();
	    int totalNumberOfSdPairs = g._vertex_list.size()*(g._vertex_list.size() - 1);
	    //population of US cities
 		Map<BaseVertex, Double> popUsCities = new HashMap<BaseVertex, Double>();
 		popUsCities.put(g.get_vertex(1),660000.0);
 		popUsCities.put(g.get_vertex(2),850000.0);
 		popUsCities.put(g.get_vertex(3),1400000.0);
 		popUsCities.put(g.get_vertex(4),191000.0);
 		popUsCities.put(g.get_vertex(5),660000.0);
 		popUsCities.put(g.get_vertex(6),2200000.0);
 		popUsCities.put(g.get_vertex(7),450000.0); 	
 		popUsCities.put(g.get_vertex(8),2700000.0);//chicago
 		popUsCities.put(g.get_vertex(9),300000.0);
 		popUsCities.put(g.get_vertex(10),460000.0);
 		popUsCities.put(g.get_vertex(11),680000.0); 		
 		popUsCities.put(g.get_vertex(12),8500000.0);//new york
 		popUsCities.put(g.get_vertex(13),1600000.0);
 		popUsCities.put(g.get_vertex(14),670000.0);
 		
 		//traffic matrix percentages 		
 		ArrayList<Double> trafMatPrcnt = new ArrayList<Double>();
 		trafMatPrcnt.add(0.05); trafMatPrcnt.add(0.1); trafMatPrcnt.add(0.15);trafMatPrcnt.add(0.2);	
 		trafMatPrcnt.add(0.25); trafMatPrcnt.add(0.50); trafMatPrcnt.add(0.75);trafMatPrcnt.add(1.0);
 		
 		
 		//Traffic Load //Scenario 1 Load where there is a full-mesh traffic
		int trafficLoad = 1000*totalNumberOfSdPairs;
		System.out.println("Total Traffic Load = " + trafficLoad);
		
		//Iterate multiple times
		for(int iterationNo=0; iterationNo<=1; iterationNo++){
			System.out.println("\n\n\n##### Iteration No. = " + iterationNo + " #####");
			
			//existing node pairs
			ArrayList<NodePair> pairListTotal = new ArrayList<NodePair>();
			//traffic node pairs for each traffic percentage
			Map<Double,ArrayList<NodePair>> trafPairs = new TreeMap<Double,ArrayList<NodePair>>();
			
			//iterate through traffic percentages
			for(Double trafPrcnt : trafMatPrcnt){
				//the number of node pairs to be added
				int reqNumOfNodePairs = (int) Math.ceil(trafPrcnt*totalNumberOfSdPairs) - pairListTotal.size();		
				//selected node pairs
				ArrayList<NodePair> selPairs = TrafficGenerator.generateNodePairs(pairListTotal, reqNumOfNodePairs, g._vertex_list);	
				//add the node pairs to existing node pairs
				pairListTotal.addAll(selPairs);			
				//add the selected node pairs
				ArrayList<NodePair> pairsForTrafPrcnt = new ArrayList<NodePair>(pairListTotal);
				//add the list of node pairs to the map
				trafPairs.put(trafPrcnt, pairsForTrafPrcnt);
			}
			
			//iterate through the traf percentages
			for(Map.Entry<Double, ArrayList<NodePair>> entryTraf : trafPairs.entrySet()){
				// SD pairs between which we desire traffic to be
				// Store each s-d pair
				List<TrafficNodes> pair_list = new ArrayList<TrafficNodes>();			
				//iterate through the node pairs and generate the vertex list
				ArrayList<BaseVertex> usedVerts = new ArrayList<BaseVertex>();
				//total population besides current vertex
				Map<BaseVertex, Double> remPopulation = new HashMap<BaseVertex, Double>();
				double totalPopulation = 0;
				for(NodePair np : entryTraf.getValue()){					
					if(!usedVerts.contains(np.v1)){
						usedVerts.add(np.v1);
						System.out.println("Population of City " + np.v1.get_id() + " = " + popUsCities.get(np.v1));
						totalPopulation += popUsCities.get(np.v1);						
					}
					if(remPopulation.get(np.v1) != null){
						double nwPop = remPopulation.get(np.v1) + popUsCities.get(np.v2);
						remPopulation.put(np.v1,nwPop);
					}else{
						remPopulation.put(np.v1,popUsCities.get(np.v2));
					}
				}
				System.out.println("Total Population = " + totalPopulation);
				//weight of each vertex based on population
				Map<BaseVertex, Double> relWtOfEachVert = new HashMap<BaseVertex, Double>();
				
				for(BaseVertex usedVert : usedVerts){
					relWtOfEachVert.put(usedVert, (popUsCities.get(usedVert)/totalPopulation));					
				}
				System.out.println("Relative Weight for each vertex");
				double totalRelWt = 0.0;
				for(Map.Entry<BaseVertex, Double> entryVrt : relWtOfEachVert.entrySet()){
					System.out.println("\t" + entryVrt.getKey().get_id() + " = " + entryVrt.getValue());
					totalRelWt += entryVrt.getValue();
				}
				System.out.println("Total relative weight = " + totalRelWt);
				System.out.println("Remaining Population for each vertex");
				for(Map.Entry<BaseVertex, Double> entryPop : remPopulation.entrySet()){
					System.out.println("\t" + entryPop.getKey().get_id() + " = " + entryPop.getValue());
				}
				
				//assign the connection Bw to traffic flows
				for(NodePair np : entryTraf.getValue()){
					System.out.println("(" + np.v1.get_id() + "," + np.v2.get_id() + ") = " + popUsCities.get(np.v2)/remPopulation.get(np.v1));
					double connBw = relWtOfEachVert.get(np.v1)*(popUsCities.get(np.v2)/remPopulation.get(np.v1))*trafficLoad;
					pair_list.add(new TrafficNodes(np.v1,np.v2,2,connBw));
				}
				
				System.out.println("\nTraffic Percentage: " + entryTraf.getKey() + " ; No. of Node Pairs: " + entryTraf.getValue().size());
				double totalFlow = 0.0;
				for(TrafficNodes tn : pair_list){
					if(tn.flow_traffic < 0){
						totalFlow += tn.flow_traffic;
						System.out.println("\t\t\t(" + tn.v1.get_id() + "," + tn.v2.get_id() + "," + tn.chain_index + "," + tn.flow_traffic + ")");
					}else{
						totalFlow += tn.flow_traffic;
						System.out.println("\t\t(" + tn.v1.get_id() + "," + tn.v2.get_id() + "," + tn.chain_index + "," + tn.flow_traffic + ")");
					}
				}
				System.out.println("Total Flow = " + totalFlow);
			}
		
		}
	   
		
	}

}
