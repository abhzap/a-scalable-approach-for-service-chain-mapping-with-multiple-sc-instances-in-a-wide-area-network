package colGen.model.preprocess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import Given.InputConstants;
import colGen.model.ver1.VertexRank;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class placeNFVI {
    
	//find the DC locations
	public static List<ArrayList<Integer>> generateLocationsForDC(Graph g){		
	    //assign the DC nodes
	    ArrayList<Integer> vrtID = new ArrayList<Integer>();
        //add all the vertex ID to this list
	    for(BaseVertex vrt : g._vertex_list){
	    	vrtID.add(vrt.get_id());
	    }
	    if(!vrtID.isEmpty()){
	    	System.out.println("Vertex list not empty!");
	    }
	    //return the k DC node placements
	    return KCombination.findCombinations(vrtID, InputConstants.numOfDCnodes);	
	}	
	
	//place the DC nodes
	public static void placeDC(Graph g, ArrayList<Integer> dcNodes){
		for(BaseVertex vrt : g._vertex_list){
			if(dcNodes.contains(vrt.get_id())){
				System.out.println("Vertex : " + vrt.get_id() + " set to type DC ");
				vrt.set_type("dc");
			}
		}
	}
	
	
	//make the list of DC nodes
	public static void makeDCList(Graph g, ArrayList<BaseVertex> dc_nodes){
		System.out.println("Making List of DCs!");
		for (BaseVertex tmp_vrt : g._vertex_list) {			
			if(tmp_vrt.get_type().equalsIgnoreCase("dc")) {
				dc_nodes.add(tmp_vrt);
			}
		}
	}
	
	
	//place the NFV nodes
	public static void placeNFVPoP(Graph g, ArrayList<Integer> dcNodes){		
		//select how many NFV-capable nodes are required
		if(InputConstants.NFV_Strategy.equals("NFV_4")){
			for(BaseVertex vrt : g._vertex_list){
				if( InputConstants.NFV_4.contains(vrt.get_id()) && !dcNodes.contains(vrt.get_id()) ){
//					System.out.println("Vertex : " + vrt.get_id() + " set to type nfv ");
					vrt.set_type("nfv");
				}else if(!dcNodes.contains(vrt.get_id())){
					vrt.set_type("sw");
				}
			}
		}else if(InputConstants.NFV_Strategy.equals("NFV_3")){
			for(BaseVertex vrt : g._vertex_list){
				if( InputConstants.NFV_3.contains(vrt.get_id()) && !dcNodes.contains(vrt.get_id()) ){
					vrt.set_type("nfv");
				}
				else if(!dcNodes.contains(vrt.get_id())){
					vrt.set_type("sw");
				}
			}			
		}else if(InputConstants.NFV_Strategy.equals("NFV_ALL")){
			for(BaseVertex vrt : g._vertex_list){	
				if( !dcNodes.contains(vrt.get_id()) ){
					vrt.set_type("nfv");
				}
			}
		}else if(InputConstants.NFV_Strategy.equals("NFV_SL")){
			for(BaseVertex vrt : g._vertex_list){	
				if( InputConstants.NFV_SL.contains(vrt.get_id()) && !dcNodes.contains(vrt.get_id()) ){
					vrt.set_type("nfv");
				}
				else if(!dcNodes.contains(vrt.get_id())){
					vrt.set_type("sw");
				}
			}
		}else if(InputConstants.NFV_Strategy.equals("NFV_SR")){
			for(BaseVertex vrt : g._vertex_list){	
				if( InputConstants.NFV_SR.contains(vrt.get_id()) && !dcNodes.contains(vrt.get_id()) ){
					vrt.set_type("nfv");
				}
				else if(!dcNodes.contains(vrt.get_id())){
					vrt.set_type("sw");
				}
			}
		}else if(InputConstants.NFV_Strategy.equals("NFV_SC")){
			for(BaseVertex vrt : g._vertex_list){	
				if( InputConstants.NFV_SC.contains(vrt.get_id()) && !dcNodes.contains(vrt.get_id()) ){
					vrt.set_type("nfv");
				}
				else if(!dcNodes.contains(vrt.get_id())){
					vrt.set_type("sw");
				}
			}
		}		
	}	
	
	//place the NFV nodes
	public static void placeNFVPoP(Graph g, List<VertexRank> rankList, int numOfNfvNodes){
		int nfvCount=0;	
		Iterator<VertexRank> itrList = rankList.iterator();
		while(nfvCount<numOfNfvNodes){
			BaseVertex vrt = itrList.next().vertex;	
			vrt.set_type("nfv");
			System.out.println("Vertex " + vrt.get_id() + " set to type nfv!");
			nfvCount++;		
		}
	}
	
	//make all nodes NFV capable
	public static void allNFV(Graph g){
		for(BaseVertex vrt : g._vertex_list) {			
			vrt.set_type("nfv");
			System.out.println("Vertex " + vrt.get_id() + " set to type nfv!");
		}
	}
	
	//public static void make list of NFV nodes
	public static void makeNFVList(Graph g, ArrayList<BaseVertex> nfv_nodes){
		System.out.println("Making list of NFV nodes!");
		for(BaseVertex tmp_vrt : g._vertex_list) {			
			if(tmp_vrt.get_type().equalsIgnoreCase("nfv")){
				nfv_nodes.add(tmp_vrt);
			}
		}		
	}
	
	//print list of NFVI nodes
	public static void printNFVINodes(ArrayList<BaseVertex> nodesNFVI){
		for(BaseVertex vrt : nodesNFVI){
			System.out.println("Vertex : " + vrt.get_id() + " ; Type : " +  vrt.get_type());
		}
	}
	
}
