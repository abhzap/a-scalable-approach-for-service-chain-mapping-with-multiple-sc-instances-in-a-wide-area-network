package colGen.model.data;

import java.util.List;

import Given.InputConstants;
import colGen.model.heuristic.ClusterHeuristic;
import colGen.model.heuristic.ClusterHeuristic.ClusterTrafficPairs;
import colGen.model.preprocess.PreProcVer1;
import colGen.model.preprocess.preProcFunctions;
import edu.asu.emit.qyan.alg.model.Graph;

public class MakeTrafficGroups {
	
	public static void generateData() throws Exception{
		//generate the graph object
	    Graph g = PreProcVer1.makeGraphObject();
	    //set all nodes to type switches
	    preProcFunctions.makeAllVrtSwitches(g);
	    //populate ChainSet details
	    InputConstants.populateServices();
	    //print the graph object
	    preProcFunctions.printGraph(g);
	    //store the generated traffic groups
	    TrafficGroups dataGen = new TrafficGroups();	    
	    //generate the number of clusters
	    for(int numOfClusters=1;numOfClusters<=ClusterHeuristic.formClustersWhenAllSdPairs(g).size();numOfClusters++){	    	  	
		    //form clusters
		    List<ClusterTrafficPairs> clusterGroups = ClusterHeuristic.formClusters(g, numOfClusters);
		    //store in the data object
		    dataGen.addGroup(numOfClusters,clusterGroups);
		    //print clusters
		    ClusterHeuristic.printClusters(clusterGroups);
	    }
	    
	}

}
