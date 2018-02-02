package colGen.model.ver1.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.heuristic.BaseHeuristic2;
import colGen.model.heuristic.HuerVarZ;
import colGen.model.heuristic.BaseHeuristic2.SdDetails;
import colGen.model.preprocess.PreProcVer1;
import colGen.model.preprocess.placeNFVI;
import colGen.model.preprocess.preProcFunctions;
import colGen.model.ver1.CG;
import colGen.model.ver1.VertexRank;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class SdGroups {	
	
	public static void groupCount2(Map<Integer,ArrayList<Integer>> scCopies, Map<Integer,Integer> scCopyToSC, 
			ArrayList<Integer> scCopyUsed, List<Integer> scUsed, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN){		
		 
  		//2 configs
  		ArrayList<Integer> srcVrtGrp1 = new ArrayList<Integer>();//create 1st source-vertex group
  		srcVrtGrp1.add(1);srcVrtGrp1.add(2);srcVrtGrp1.add(3);srcVrtGrp1.add(4);
  		srcVrtGrp1.add(5);srcVrtGrp1.add(6);srcVrtGrp1.add(7);srcVrtGrp1.add(8);
  		srcVrtGrp1.add(9);srcVrtGrp1.add(10);srcVrtGrp1.add(13);srcVrtGrp1.add(14);
  		ArrayList<Integer> srcVrtGrp2 = new ArrayList<Integer>();//create 2nd source-vertex group
  		srcVrtGrp2.add(11);srcVrtGrp2.add(12);srcVrtGrp2.add(15);srcVrtGrp2.add(16);
  		srcVrtGrp2.add(17);srcVrtGrp2.add(18);srcVrtGrp2.add(19);srcVrtGrp2.add(20);
  		srcVrtGrp2.add(21);srcVrtGrp2.add(22);srcVrtGrp2.add(23);srcVrtGrp2.add(24);	  	
	  	
	  	//add the SC copy Id's to data-structure's
	    //add to map and list's
	  	ArrayList<Integer> scCopyList = new ArrayList<Integer>();
    	scCopyToSC.put(1001,0);
    	scCopyToSC.put(1002,0);
    	scCopyUsed.add(1001);
    	scCopyUsed.add(1002);
    	scCopyList.add(1001);
    	scCopyList.add(1002);
    	 //add all scCopyId's in map
	    scCopies.put(0, scCopyList);
	  	
	    //cluster each (s,d) pair as a separate service chain
	    for(int scID : scUsed){	   
		    for(TrafficNodes tn : serviceChainTN.get(scID)){		    
		    	//update chain index for (s,d)
		    	if(srcVrtGrp1.contains(tn.v1.get_id())){
		    		tn.updateChainIndex(1001);	 
		    	}
		    	if(srcVrtGrp2.contains(tn.v1.get_id())){
		    		tn.updateChainIndex(1002);	 
		    	}
		    }		   
	    }
		
	}
	
	public static void groupCount3(Map<Integer,ArrayList<Integer>> scCopies, Map<Integer,Integer> scCopyToSC, 
			ArrayList<Integer> scCopyUsed, List<Integer> scUsed, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN){
		
		//3 configs
  		ArrayList<Integer> srcVrtGrp1 = new ArrayList<Integer>();//create 1st source-vertex group
  		srcVrtGrp1.add(1);srcVrtGrp1.add(2);srcVrtGrp1.add(3);srcVrtGrp1.add(4);
  		srcVrtGrp1.add(5);srcVrtGrp1.add(6);srcVrtGrp1.add(7);srcVrtGrp1.add(8); 		
  		ArrayList<Integer> srcVrtGrp2 = new ArrayList<Integer>();//create 2nd source-vertex group
  		srcVrtGrp2.add(9);srcVrtGrp2.add(11);srcVrtGrp2.add(12);srcVrtGrp2.add(15);
  		srcVrtGrp2.add(16);srcVrtGrp2.add(19);srcVrtGrp2.add(20);srcVrtGrp2.add(21);
  		ArrayList<Integer> srcVrtGrp3 = new ArrayList<Integer>();//create 3rd source-vertex group
  		srcVrtGrp3.add(10); srcVrtGrp3.add(13);srcVrtGrp3.add(14);srcVrtGrp3.add(17);	
  		srcVrtGrp3.add(18);srcVrtGrp3.add(22);srcVrtGrp3.add(23);srcVrtGrp3.add(24);
	  	
	  	//add the SC copy Id's to data-structure's
	    //add to map and list's
	  	ArrayList<Integer> scCopyList = new ArrayList<Integer>();
    	scCopyToSC.put(1001,0);
    	scCopyToSC.put(1002,0);
    	scCopyToSC.put(1003,0);
    	scCopyUsed.add(1001);
    	scCopyUsed.add(1002);
    	scCopyUsed.add(1003);
    	scCopyList.add(1001);
    	scCopyList.add(1002);
    	scCopyList.add(1003);
    	 //add all scCopyId's in map
	    scCopies.put(0, scCopyList);
	  	
	    //cluster each (s,d) pair as a separate service chain
	    for(int scID : scUsed){	   
		    for(TrafficNodes tn : serviceChainTN.get(scID)){		    
		    	//update chain index for (s,d)
		    	if(srcVrtGrp1.contains(tn.v1.get_id())){
		    		tn.updateChainIndex(1001);	 
		    	}
		    	if(srcVrtGrp2.contains(tn.v1.get_id())){
		    		tn.updateChainIndex(1002);	 
		    	}
		    	if(srcVrtGrp3.contains(tn.v1.get_id())){
		    		tn.updateChainIndex(1003);	 
		    	}
		    }		   
	    }
		
	}

	public static void groupCount4(Map<Integer,ArrayList<Integer>> scCopies, Map<Integer,Integer> scCopyToSC, 
			ArrayList<Integer> scCopyUsed, List<Integer> scUsed, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN){
		
		//2 configs
  		ArrayList<Integer> srcVrtGrp1 = new ArrayList<Integer>();//create 1st source-vertex group
  		srcVrtGrp1.add(1);srcVrtGrp1.add(2);srcVrtGrp1.add(3);
  		srcVrtGrp1.add(4);srcVrtGrp1.add(6);srcVrtGrp1.add(7);		
  		ArrayList<Integer> srcVrtGrp2 = new ArrayList<Integer>();//create 2nd source-vertex group
  		srcVrtGrp2.add(5);srcVrtGrp2.add(8);srcVrtGrp2.add(9);
  		srcVrtGrp2.add(10);srcVrtGrp2.add(11);srcVrtGrp2.add(12);  		
  		ArrayList<Integer> srcVrtGrp3 = new ArrayList<Integer>();//create 3rd source-vertex group
  		srcVrtGrp3.add(15);srcVrtGrp3.add(16);srcVrtGrp3.add(19);
  		srcVrtGrp3.add(20);srcVrtGrp3.add(21);srcVrtGrp3.add(22);  		
  		ArrayList<Integer> srcVrtGrp4 = new ArrayList<Integer>();//create 4th source-vertex group
  		srcVrtGrp4.add(13);srcVrtGrp4.add(14);srcVrtGrp4.add(17);	
   		srcVrtGrp4.add(18);srcVrtGrp4.add(23);srcVrtGrp4.add(24);
	  	
	  	//add the SC copy Id's to data-structure's
	    //add to map and list's
	  	ArrayList<Integer> scCopyList = new ArrayList<Integer>();
    	scCopyToSC.put(1001,0);
    	scCopyToSC.put(1002,0);
    	scCopyToSC.put(1003,0);
    	scCopyToSC.put(1004,0);
    	scCopyUsed.add(1001);
    	scCopyUsed.add(1002);
    	scCopyUsed.add(1003);
    	scCopyUsed.add(1004);
    	scCopyList.add(1001);
    	scCopyList.add(1002);
    	scCopyList.add(1003);
    	scCopyList.add(1004);
    	//add all scCopyId's in map
	    scCopies.put(0, scCopyList);
	  	
	    //cluster each (s,d) pair as a separate service chain
	    for(int scID : scUsed){	   
		    for(TrafficNodes tn : serviceChainTN.get(scID)){		    
		    	//update chain index for (s,d)
		    	if(srcVrtGrp1.contains(tn.v1.get_id())){
		    		tn.updateChainIndex(1001);	 
		    	}
		    	if(srcVrtGrp2.contains(tn.v1.get_id())){
		    		tn.updateChainIndex(1002);	 
		    	}
		    	if(srcVrtGrp3.contains(tn.v1.get_id())){
		    		tn.updateChainIndex(1003);	 
		    	}
		    	if(srcVrtGrp4.contains(tn.v1.get_id())){
		    		tn.updateChainIndex(1004);	 
		    	}
		    }		   
	    }
		
	}
	
	public static void groupCount5(Map<Integer,ArrayList<Integer>> scCopies, Map<Integer,Integer> scCopyToSC, 
			ArrayList<Integer> scCopyUsed, List<Integer> scUsed, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN){
		
		//2 configs
  		ArrayList<Integer> srcVrtGrp1 = new ArrayList<Integer>();//create 1st source-vertex group
  		srcVrtGrp1.add(1);srcVrtGrp1.add(2);srcVrtGrp1.add(6);srcVrtGrp1.add(11);		
  		ArrayList<Integer> srcVrtGrp2 = new ArrayList<Integer>();//create 2nd source-vertex group
  		srcVrtGrp2.add(15);srcVrtGrp2.add(16);srcVrtGrp2.add(19);srcVrtGrp2.add(20);srcVrtGrp2.add(21);  		
  		ArrayList<Integer> srcVrtGrp3 = new ArrayList<Integer>();//create 3rd source-vertex group
  		srcVrtGrp3.add(17);srcVrtGrp3.add(18);srcVrtGrp3.add(22);srcVrtGrp3.add(23);srcVrtGrp3.add(24);  		
  		ArrayList<Integer> srcVrtGrp4 = new ArrayList<Integer>();//create 4th source-vertex group
  		srcVrtGrp4.add(9);srcVrtGrp4.add(10);srcVrtGrp4.add(12);srcVrtGrp4.add(13);srcVrtGrp4.add(14);
   		ArrayList<Integer> srcVrtGrp5 = new ArrayList<Integer>();//create 4th source-vertex group
   		srcVrtGrp5.add(3);srcVrtGrp5.add(4);srcVrtGrp5.add(5);srcVrtGrp5.add(7);srcVrtGrp5.add(8);
	  	
	  	//add the SC copy Id's to data-structure's
	    //add to map and list's
	  	ArrayList<Integer> scCopyList = new ArrayList<Integer>();
    	scCopyToSC.put(1001,0);
    	scCopyToSC.put(1002,0);
    	scCopyToSC.put(1003,0);
    	scCopyToSC.put(1004,0);
    	scCopyToSC.put(1005,0);
    	scCopyUsed.add(1001);
    	scCopyUsed.add(1002);
    	scCopyUsed.add(1003);
    	scCopyUsed.add(1004);
    	scCopyUsed.add(1005);
    	scCopyList.add(1001);
    	scCopyList.add(1002);
    	scCopyList.add(1003);
    	scCopyList.add(1004);
    	scCopyList.add(1005);
    	//add all scCopyId's in map
	    scCopies.put(0, scCopyList);
	  	
	    //cluster each (s,d) pair as a separate service chain
	    for(int scID : scUsed){	   
		    for(TrafficNodes tn : serviceChainTN.get(scID)){		    
		    	//update chain index for (s,d)
		    	if(srcVrtGrp1.contains(tn.v1.get_id())){
		    		tn.updateChainIndex(1001);	 
		    	}
		    	if(srcVrtGrp2.contains(tn.v1.get_id())){
		    		tn.updateChainIndex(1002);	 
		    	}
		    	if(srcVrtGrp3.contains(tn.v1.get_id())){
		    		tn.updateChainIndex(1003);	 
		    	}
		    	if(srcVrtGrp4.contains(tn.v1.get_id())){
		    		tn.updateChainIndex(1004);	 
		    	}
		    	if(srcVrtGrp5.contains(tn.v1.get_id())){
		    		tn.updateChainIndex(1005);	 
		    	}
		    }		   
	    }		
	}
	
	public static void groupCount6(Map<Integer,ArrayList<Integer>> scCopies, Map<Integer,Integer> scCopyToSC, 
			ArrayList<Integer> scCopyUsed, List<Integer> scUsed, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN){
		
		//6 configs
		//source clusters
		List<ArrayList<Integer>> srcVrtGrp = new ArrayList<ArrayList<Integer>>();
  		ArrayList<Integer> srcVrtGrp1 = new ArrayList<Integer>();//create 1st source-vertex group
  		srcVrtGrp1.add(1);srcVrtGrp1.add(2);srcVrtGrp1.add(3);srcVrtGrp1.add(4);
  		srcVrtGrp1.add(5);srcVrtGrp1.add(6);srcVrtGrp1.add(7);srcVrtGrp1.add(8);  		
  		ArrayList<Integer> srcVrtGrp2 = new ArrayList<Integer>();//create 2nd source-vertex group
  		srcVrtGrp2.add(9);srcVrtGrp2.add(11);srcVrtGrp2.add(12);srcVrtGrp2.add(15);
  		srcVrtGrp2.add(16);srcVrtGrp2.add(19);srcVrtGrp2.add(20);srcVrtGrp2.add(21);  	
  		ArrayList<Integer> srcVrtGrp3 = new ArrayList<Integer>();//create 3rd source-vertex group
  		srcVrtGrp3.add(10); srcVrtGrp3.add(13);srcVrtGrp3.add(14);srcVrtGrp3.add(17);	
  		srcVrtGrp3.add(18);srcVrtGrp3.add(22);srcVrtGrp3.add(23);srcVrtGrp3.add(24);
  	
  		
  		//destination clusters
  		List<ArrayList<Integer>> dstVrtGrp = new ArrayList<ArrayList<Integer>>();
  		ArrayList<Integer> dstVrtGrp1 = new ArrayList<Integer>();//create 1st source-vertex group
  		dstVrtGrp1.add(1);dstVrtGrp1.add(2);dstVrtGrp1.add(3);dstVrtGrp1.add(4);
  		dstVrtGrp1.add(5);dstVrtGrp1.add(6);dstVrtGrp1.add(7);dstVrtGrp1.add(8);
  		dstVrtGrp1.add(9);dstVrtGrp1.add(10);dstVrtGrp1.add(13);dstVrtGrp1.add(14);  	
  		ArrayList<Integer> dstVrtGrp2 = new ArrayList<Integer>();//create 2nd source-vertex group
  		dstVrtGrp2.add(11);dstVrtGrp2.add(12);dstVrtGrp2.add(15);dstVrtGrp2.add(16);
  		dstVrtGrp2.add(17);dstVrtGrp2.add(18);dstVrtGrp2.add(19);dstVrtGrp2.add(20);
  		dstVrtGrp2.add(21);dstVrtGrp2.add(22);dstVrtGrp2.add(23);dstVrtGrp2.add(24);
  		
  	    //add the SC copy Id's to data-structure's
	    //add to map and list's
	  	ArrayList<Integer> scCopyList = new ArrayList<Integer>();
    	scCopyToSC.put(1001,0);
    	scCopyToSC.put(1002,0);
    	scCopyToSC.put(1003,0);
    	scCopyToSC.put(1004,0);
    	scCopyToSC.put(1005,0);
    	scCopyToSC.put(1006,0);
    	scCopyUsed.add(1001);
    	scCopyUsed.add(1002);
    	scCopyUsed.add(1003);
    	scCopyUsed.add(1004);
    	scCopyUsed.add(1005);
    	scCopyUsed.add(1006);
    	scCopyList.add(1001);
    	scCopyList.add(1002);
    	scCopyList.add(1003);
    	scCopyList.add(1004);
    	scCopyList.add(1005);
    	scCopyList.add(1006);
    	//add all scCopyId's in map
	    scCopies.put(0, scCopyList);
	  	
	    //cluster each (s,d) pair as a separate service chain
	    for(int scID : scUsed){	   
		    for(TrafficNodes tn : serviceChainTN.get(scID)){		    
		    	//update chain index for (s,d)
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1001);	 
		    	}
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1002);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1003);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1004);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1005);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1006);	 
		    	}
		    }		   
	    }
  		
  		
	}
	
	public static void groupCount8(Map<Integer,ArrayList<Integer>> scCopies, Map<Integer,Integer> scCopyToSC, 
			ArrayList<Integer> scCopyUsed, List<Integer> scUsed, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN){
		//8 configs
		//source clusters
  		ArrayList<Integer> srcVrtGrp1 = new ArrayList<Integer>();//create 1st source-vertex group
  		srcVrtGrp1.add(1);srcVrtGrp1.add(2);srcVrtGrp1.add(3);
  		srcVrtGrp1.add(4);srcVrtGrp1.add(6);srcVrtGrp1.add(7);		
  		ArrayList<Integer> srcVrtGrp2 = new ArrayList<Integer>();//create 2nd source-vertex group
  		srcVrtGrp2.add(5);srcVrtGrp2.add(8);srcVrtGrp2.add(9);
  		srcVrtGrp2.add(10);srcVrtGrp2.add(11);srcVrtGrp2.add(12);  		
  		ArrayList<Integer> srcVrtGrp3 = new ArrayList<Integer>();//create 3rd source-vertex group
  		srcVrtGrp3.add(15);srcVrtGrp3.add(16);srcVrtGrp3.add(19);
  		srcVrtGrp3.add(20);srcVrtGrp3.add(21);srcVrtGrp3.add(22);  		
  		ArrayList<Integer> srcVrtGrp4 = new ArrayList<Integer>();//create 4th source-vertex group
  		srcVrtGrp4.add(13);srcVrtGrp4.add(14);srcVrtGrp4.add(17);	
   		srcVrtGrp4.add(18);srcVrtGrp4.add(23);srcVrtGrp4.add(24);
   		
   		//destination clusters
  		List<ArrayList<Integer>> dstVrtGrp = new ArrayList<ArrayList<Integer>>();
  		ArrayList<Integer> dstVrtGrp1 = new ArrayList<Integer>();//create 1st source-vertex group
  		dstVrtGrp1.add(1);dstVrtGrp1.add(2);dstVrtGrp1.add(3);dstVrtGrp1.add(4);
  		dstVrtGrp1.add(5);dstVrtGrp1.add(6);dstVrtGrp1.add(7);dstVrtGrp1.add(8);
  		dstVrtGrp1.add(9);dstVrtGrp1.add(10);dstVrtGrp1.add(13);dstVrtGrp1.add(14);  	
  		ArrayList<Integer> dstVrtGrp2 = new ArrayList<Integer>();//create 2nd source-vertex group
  		dstVrtGrp2.add(11);dstVrtGrp2.add(12);dstVrtGrp2.add(15);dstVrtGrp2.add(16);
  		dstVrtGrp2.add(17);dstVrtGrp2.add(18);dstVrtGrp2.add(19);dstVrtGrp2.add(20);
  		dstVrtGrp2.add(21);dstVrtGrp2.add(22);dstVrtGrp2.add(23);dstVrtGrp2.add(24);
  		
  		//add the SC copy Id's to data-structure's
	    //add to map and list's
	  	ArrayList<Integer> scCopyList = new ArrayList<Integer>();
    	scCopyToSC.put(1001,0);
    	scCopyToSC.put(1002,0);
    	scCopyToSC.put(1003,0);
    	scCopyToSC.put(1004,0);
    	scCopyToSC.put(1005,0);
    	scCopyToSC.put(1006,0);
    	scCopyToSC.put(1007,0);
    	scCopyToSC.put(1008,0);
    	scCopyUsed.add(1001);
    	scCopyUsed.add(1002);
    	scCopyUsed.add(1003);
    	scCopyUsed.add(1004);
    	scCopyUsed.add(1005);
    	scCopyUsed.add(1006);
    	scCopyUsed.add(1007);
    	scCopyUsed.add(1008);
    	scCopyList.add(1001);
    	scCopyList.add(1002);
    	scCopyList.add(1003);
    	scCopyList.add(1004);
    	scCopyList.add(1005);
    	scCopyList.add(1006);
    	scCopyList.add(1007);
    	scCopyList.add(1008);
    	//add all scCopyId's in map
	    scCopies.put(0, scCopyList);
	  	
	    //cluster each (s,d) pair as a separate service chain
	    for(int scID : scUsed){	   
		    for(TrafficNodes tn : serviceChainTN.get(scID)){		    
		    	//update chain index for (s,d)
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1001);	 
		    	}
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1002);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1003);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1004);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1005);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1006);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1007);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1008);	 
		    	}
		    }		   
	    }	
	}
	
	public static void groupCount9(Map<Integer,ArrayList<Integer>> scCopies, Map<Integer,Integer> scCopyToSC, 
			ArrayList<Integer> scCopyUsed, List<Integer> scUsed, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN){
		
		//9 configs
		//source clusters
  		ArrayList<Integer> srcVrtGrp1 = new ArrayList<Integer>();//create 1st source-vertex group
  		srcVrtGrp1.add(1);srcVrtGrp1.add(2);srcVrtGrp1.add(3);srcVrtGrp1.add(4);
  		srcVrtGrp1.add(5);srcVrtGrp1.add(6);srcVrtGrp1.add(7);srcVrtGrp1.add(8); 		
  		ArrayList<Integer> srcVrtGrp2 = new ArrayList<Integer>();//create 2nd source-vertex group
  		srcVrtGrp2.add(9);srcVrtGrp2.add(11);srcVrtGrp2.add(12);srcVrtGrp2.add(15);
  		srcVrtGrp2.add(16);srcVrtGrp2.add(19);srcVrtGrp2.add(20);srcVrtGrp2.add(21);
  		ArrayList<Integer> srcVrtGrp3 = new ArrayList<Integer>();//create 3rd source-vertex group
  		srcVrtGrp3.add(10); srcVrtGrp3.add(13);srcVrtGrp3.add(14);srcVrtGrp3.add(17);	
  		srcVrtGrp3.add(18);srcVrtGrp3.add(22);srcVrtGrp3.add(23);srcVrtGrp3.add(24);
  		
  		//destination clusters
  		ArrayList<Integer> dstVrtGrp1 = new ArrayList<Integer>();//create 1st destination-vertex group
  		dstVrtGrp1.add(1);dstVrtGrp1.add(2);dstVrtGrp1.add(3);dstVrtGrp1.add(4);
  		dstVrtGrp1.add(5);dstVrtGrp1.add(6);dstVrtGrp1.add(7);dstVrtGrp1.add(8); 		
  		ArrayList<Integer> dstVrtGrp2 = new ArrayList<Integer>();//create 2nd destination-vertex group
  		dstVrtGrp2.add(9);dstVrtGrp2.add(11);dstVrtGrp2.add(12);dstVrtGrp2.add(15);
  		dstVrtGrp2.add(16);dstVrtGrp2.add(19);dstVrtGrp2.add(20);dstVrtGrp2.add(21);
  		ArrayList<Integer> dstVrtGrp3 = new ArrayList<Integer>();//create 3rd destination-vertex group
  		dstVrtGrp3.add(10);dstVrtGrp3.add(13);dstVrtGrp3.add(14);dstVrtGrp3.add(17);
  		dstVrtGrp3.add(18);dstVrtGrp3.add(22);dstVrtGrp3.add(23);dstVrtGrp3.add(24);
  		
  		//add the SC copy Id's to data-structure's
	    //add to map and list's
	  	ArrayList<Integer> scCopyList = new ArrayList<Integer>();
    	scCopyToSC.put(1001,0);
    	scCopyToSC.put(1002,0);
    	scCopyToSC.put(1003,0);
    	scCopyToSC.put(1004,0);
    	scCopyToSC.put(1005,0);
    	scCopyToSC.put(1006,0);
    	scCopyToSC.put(1007,0);
    	scCopyToSC.put(1008,0);
    	scCopyToSC.put(1009,0);
    	scCopyUsed.add(1001);
    	scCopyUsed.add(1002);
    	scCopyUsed.add(1003);
    	scCopyUsed.add(1004);
    	scCopyUsed.add(1005);
    	scCopyUsed.add(1006);
    	scCopyUsed.add(1007);
    	scCopyUsed.add(1008);
    	scCopyUsed.add(1009);
    	scCopyList.add(1001);
    	scCopyList.add(1002);
    	scCopyList.add(1003);
    	scCopyList.add(1004);
    	scCopyList.add(1005);
    	scCopyList.add(1006);
    	scCopyList.add(1007);
    	scCopyList.add(1008);
    	scCopyList.add(1009);
    	//add all scCopyId's in map
	    scCopies.put(0, scCopyList);
	    
	  //cluster each (s,d) pair as a separate service chain
	    for(int scID : scUsed){	   
		    for(TrafficNodes tn : serviceChainTN.get(scID)){		    
		    	//update chain index for (s,d)
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1001);	 
		    	}
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1002);	 
		    	}
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1003);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1004);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1005);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1006);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1007);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1008);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1009);	 
		    	}		    	
		    }		   
	    }
  		
	}
	
	public static void groupCount12(Map<Integer,ArrayList<Integer>> scCopies, Map<Integer,Integer> scCopyToSC, 
			ArrayList<Integer> scCopyUsed, List<Integer> scUsed, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN){
		//12
		//source clusters
  		ArrayList<Integer> srcVrtGrp1 = new ArrayList<Integer>();//create 1st source-vertex group
  		srcVrtGrp1.add(1);srcVrtGrp1.add(2);srcVrtGrp1.add(3);
  		srcVrtGrp1.add(4);srcVrtGrp1.add(6);srcVrtGrp1.add(7);		
  		ArrayList<Integer> srcVrtGrp2 = new ArrayList<Integer>();//create 2nd source-vertex group
  		srcVrtGrp2.add(5);srcVrtGrp2.add(8);srcVrtGrp2.add(9);
  		srcVrtGrp2.add(10);srcVrtGrp2.add(11);srcVrtGrp2.add(12);  		
  		ArrayList<Integer> srcVrtGrp3 = new ArrayList<Integer>();//create 3rd source-vertex group
  		srcVrtGrp3.add(15);srcVrtGrp3.add(16);srcVrtGrp3.add(19);
  		srcVrtGrp3.add(20);srcVrtGrp3.add(21);srcVrtGrp3.add(22);  		
  		ArrayList<Integer> srcVrtGrp4 = new ArrayList<Integer>();//create 4th source-vertex group
  		srcVrtGrp4.add(13);srcVrtGrp4.add(14);srcVrtGrp4.add(17);	
   		srcVrtGrp4.add(18);srcVrtGrp4.add(23);srcVrtGrp4.add(24);
		
		//destination clusters
  		ArrayList<Integer> dstVrtGrp1 = new ArrayList<Integer>();//create 1st destination-vertex group
  		dstVrtGrp1.add(1);dstVrtGrp1.add(2);dstVrtGrp1.add(3);dstVrtGrp1.add(4);
  		dstVrtGrp1.add(5);dstVrtGrp1.add(6);dstVrtGrp1.add(7);dstVrtGrp1.add(8); 		
  		ArrayList<Integer> dstVrtGrp2 = new ArrayList<Integer>();//create 2nd destination-vertex group
  		dstVrtGrp2.add(9);dstVrtGrp2.add(11);dstVrtGrp2.add(12);dstVrtGrp2.add(15);
  		dstVrtGrp2.add(16);dstVrtGrp2.add(19);dstVrtGrp2.add(20);dstVrtGrp2.add(21);
  		ArrayList<Integer> dstVrtGrp3 = new ArrayList<Integer>();//create 3rd destination-vertex group
  		dstVrtGrp3.add(10);dstVrtGrp3.add(13);dstVrtGrp3.add(14);dstVrtGrp3.add(17);
  		dstVrtGrp3.add(18);dstVrtGrp3.add(22);dstVrtGrp3.add(23);dstVrtGrp3.add(24);
  		
  	    //add the SC copy Id's to data-structure's
	    //add to map and list's
	  	ArrayList<Integer> scCopyList = new ArrayList<Integer>();
    	scCopyToSC.put(1001,0);
    	scCopyToSC.put(1002,0);
    	scCopyToSC.put(1003,0);
    	scCopyToSC.put(1004,0);
    	scCopyToSC.put(1005,0);
    	scCopyToSC.put(1006,0);
    	scCopyToSC.put(1007,0);
    	scCopyToSC.put(1008,0);
    	scCopyToSC.put(1009,0);
    	scCopyToSC.put(1010,0);
    	scCopyToSC.put(1011,0);
    	scCopyToSC.put(1012,0);
    	scCopyUsed.add(1001);
    	scCopyUsed.add(1002);
    	scCopyUsed.add(1003);
    	scCopyUsed.add(1004);
    	scCopyUsed.add(1005);
    	scCopyUsed.add(1006);
    	scCopyUsed.add(1007);
    	scCopyUsed.add(1008);
    	scCopyUsed.add(1009);
    	scCopyUsed.add(1010);
    	scCopyUsed.add(1011);
    	scCopyUsed.add(1012);
    	scCopyList.add(1001);
    	scCopyList.add(1002);
    	scCopyList.add(1003);
    	scCopyList.add(1004);
    	scCopyList.add(1005);
    	scCopyList.add(1006);
    	scCopyList.add(1007);
    	scCopyList.add(1008);
    	scCopyList.add(1009);
    	scCopyList.add(1010);
    	scCopyList.add(1011);
    	scCopyList.add(1012);
    	//add all scCopyId's in map
	    scCopies.put(0, scCopyList);
	    
	    //cluster each (s,d) pair as a separate service chain
	    for(int scID : scUsed){	   
		    for(TrafficNodes tn : serviceChainTN.get(scID)){		    
		    	//update chain index for (s,d)
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1001);	 
		    	}
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1002);	 
		    	}
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1003);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1004);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1005);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1006);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1007);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1008);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1009);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1010);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1011);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1012);	 
		    	}
		    }		   
	    }
	    
	}
	
	public static void groupCount16(Map<Integer,ArrayList<Integer>> scCopies, Map<Integer,Integer> scCopyToSC, 
			ArrayList<Integer> scCopyUsed, List<Integer> scUsed, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN){
		//16
		//source clusters
  		ArrayList<Integer> srcVrtGrp1 = new ArrayList<Integer>();//create 1st source-vertex group
  		srcVrtGrp1.add(1);srcVrtGrp1.add(2);srcVrtGrp1.add(3);
  		srcVrtGrp1.add(4);srcVrtGrp1.add(6);srcVrtGrp1.add(7);		
  		ArrayList<Integer> srcVrtGrp2 = new ArrayList<Integer>();//create 2nd source-vertex group
  		srcVrtGrp2.add(5);srcVrtGrp2.add(8);srcVrtGrp2.add(9);
  		srcVrtGrp2.add(10);srcVrtGrp2.add(11);srcVrtGrp2.add(12);  		
  		ArrayList<Integer> srcVrtGrp3 = new ArrayList<Integer>();//create 3rd source-vertex group
  		srcVrtGrp3.add(15);srcVrtGrp3.add(16);srcVrtGrp3.add(19);
  		srcVrtGrp3.add(20);srcVrtGrp3.add(21);srcVrtGrp3.add(22);  		
  		ArrayList<Integer> srcVrtGrp4 = new ArrayList<Integer>();//create 4th source-vertex group
  		srcVrtGrp4.add(13);srcVrtGrp4.add(14);srcVrtGrp4.add(17);	
   		srcVrtGrp4.add(18);srcVrtGrp4.add(23);srcVrtGrp4.add(24);
   		
   		//destination clusters
   		ArrayList<Integer> dstVrtGrp1 = new ArrayList<Integer>();//create 1st source-vertex group
   		dstVrtGrp1.add(1);dstVrtGrp1.add(2);dstVrtGrp1.add(3);
   		dstVrtGrp1.add(4);dstVrtGrp1.add(6);dstVrtGrp1.add(7);		
  		ArrayList<Integer> dstVrtGrp2 = new ArrayList<Integer>();//create 2nd source-vertex group
  		dstVrtGrp2.add(5);dstVrtGrp2.add(8);dstVrtGrp2.add(9);
  		dstVrtGrp2.add(10);dstVrtGrp2.add(11);dstVrtGrp2.add(12);  		
  		ArrayList<Integer> dstVrtGrp3 = new ArrayList<Integer>();//create 3rd source-vertex group
  		dstVrtGrp3.add(15);dstVrtGrp3.add(16);dstVrtGrp3.add(19);
  		dstVrtGrp3.add(20);dstVrtGrp3.add(21);dstVrtGrp3.add(22);  		
  		ArrayList<Integer> dstVrtGrp4 = new ArrayList<Integer>();//create 4th source-vertex group
  		dstVrtGrp4.add(13);dstVrtGrp4.add(14);dstVrtGrp4.add(17);	
  		dstVrtGrp4.add(18);dstVrtGrp4.add(23);dstVrtGrp4.add(24);
  		
  		//add the SC copy Id's to data-structure's
	    //add to map and list's
	  	ArrayList<Integer> scCopyList = new ArrayList<Integer>();
    	scCopyToSC.put(1001,0);
    	scCopyToSC.put(1002,0);
    	scCopyToSC.put(1003,0);
    	scCopyToSC.put(1004,0);
    	scCopyToSC.put(1005,0);
    	scCopyToSC.put(1006,0);
    	scCopyToSC.put(1007,0);
    	scCopyToSC.put(1008,0);
    	scCopyToSC.put(1009,0);
    	scCopyToSC.put(1010,0);
    	scCopyToSC.put(1011,0);
    	scCopyToSC.put(1012,0);
    	scCopyToSC.put(1013,0);
    	scCopyToSC.put(1014,0);
    	scCopyToSC.put(1015,0);
    	scCopyToSC.put(1016,0);
    	scCopyUsed.add(1001);
    	scCopyUsed.add(1002);
    	scCopyUsed.add(1003);
    	scCopyUsed.add(1004);
    	scCopyUsed.add(1005);
    	scCopyUsed.add(1006);
    	scCopyUsed.add(1007);
    	scCopyUsed.add(1008);
    	scCopyUsed.add(1009);
    	scCopyUsed.add(1010);
    	scCopyUsed.add(1011);
    	scCopyUsed.add(1012);
    	scCopyUsed.add(1013);
    	scCopyUsed.add(1014);
    	scCopyUsed.add(1015);
    	scCopyUsed.add(1016);
    	scCopyList.add(1001);
    	scCopyList.add(1002);
    	scCopyList.add(1003);
    	scCopyList.add(1004);
    	scCopyList.add(1005);
    	scCopyList.add(1006);
    	scCopyList.add(1007);
    	scCopyList.add(1008);
    	scCopyList.add(1009);
    	scCopyList.add(1010);
    	scCopyList.add(1011);
    	scCopyList.add(1012);
    	scCopyList.add(1013);
    	scCopyList.add(1014);
    	scCopyList.add(1015);
    	scCopyList.add(1016);
    	//add all scCopyId's in map
	    scCopies.put(0, scCopyList);
	    
	    //cluster each (s,d) pair as a separate service chain
	    for(int scID : scUsed){	   
		    for(TrafficNodes tn : serviceChainTN.get(scID)){		    
		    	//update chain index for (s,d)
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1001);	 
		    	}
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1002);	 
		    	}
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1003);	 
		    	}
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp4.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1004);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1005);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1006);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1007);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp4.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1008);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1009);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1010);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1011);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp4.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1012);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1013);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1014);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1015);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp4.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1016);	 
		    	}
		    }		   
	    }
	    
	}
	
	public static void groupCount20(Map<Integer,ArrayList<Integer>> scCopies, Map<Integer,Integer> scCopyToSC, 
			ArrayList<Integer> scCopyUsed, List<Integer> scUsed, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN){
		//20 configs
		//source clusters
		ArrayList<Integer> srcVrtGrp1 = new ArrayList<Integer>();//create 1st source-vertex group
  		srcVrtGrp1.add(1);srcVrtGrp1.add(2);srcVrtGrp1.add(6);srcVrtGrp1.add(11);		
  		ArrayList<Integer> srcVrtGrp2 = new ArrayList<Integer>();//create 2nd source-vertex group
  		srcVrtGrp2.add(15);srcVrtGrp2.add(16);srcVrtGrp2.add(19);srcVrtGrp2.add(20);srcVrtGrp2.add(21);  		
  		ArrayList<Integer> srcVrtGrp3 = new ArrayList<Integer>();//create 3rd source-vertex group
  		srcVrtGrp3.add(17);srcVrtGrp3.add(18);srcVrtGrp3.add(22);srcVrtGrp3.add(23);srcVrtGrp3.add(24);  		
  		ArrayList<Integer> srcVrtGrp4 = new ArrayList<Integer>();//create 4th source-vertex group
  		srcVrtGrp4.add(9);srcVrtGrp4.add(10);srcVrtGrp4.add(12);srcVrtGrp4.add(13);srcVrtGrp4.add(14);
   		ArrayList<Integer> srcVrtGrp5 = new ArrayList<Integer>();//create 4th source-vertex group
   		srcVrtGrp5.add(3);srcVrtGrp5.add(4);srcVrtGrp5.add(5);srcVrtGrp5.add(7);srcVrtGrp5.add(8);
		
		
		//destination clusters
   		ArrayList<Integer> dstVrtGrp1 = new ArrayList<Integer>();//create 1st source-vertex group
   		dstVrtGrp1.add(1);dstVrtGrp1.add(2);dstVrtGrp1.add(3);
   		dstVrtGrp1.add(4);dstVrtGrp1.add(6);dstVrtGrp1.add(7);		
  		ArrayList<Integer> dstVrtGrp2 = new ArrayList<Integer>();//create 2nd source-vertex group
  		dstVrtGrp2.add(5);dstVrtGrp2.add(8);dstVrtGrp2.add(9);
  		dstVrtGrp2.add(10);dstVrtGrp2.add(11);dstVrtGrp2.add(12);  		
  		ArrayList<Integer> dstVrtGrp3 = new ArrayList<Integer>();//create 3rd source-vertex group
  		dstVrtGrp3.add(15);dstVrtGrp3.add(16);dstVrtGrp3.add(19);
  		dstVrtGrp3.add(20);dstVrtGrp3.add(21);dstVrtGrp3.add(22);  		
  		ArrayList<Integer> dstVrtGrp4 = new ArrayList<Integer>();//create 4th source-vertex group
  		dstVrtGrp4.add(13);dstVrtGrp4.add(14);dstVrtGrp4.add(17);	
  		dstVrtGrp4.add(18);dstVrtGrp4.add(23);dstVrtGrp4.add(24);
  		
  		//add the SC copy Id's to data-structure's
	    //add to map and list's
	  	ArrayList<Integer> scCopyList = new ArrayList<Integer>();
    	scCopyToSC.put(1001,0);
    	scCopyToSC.put(1002,0);
    	scCopyToSC.put(1003,0);
    	scCopyToSC.put(1004,0);
    	scCopyToSC.put(1005,0);
    	scCopyToSC.put(1006,0);
    	scCopyToSC.put(1007,0);
    	scCopyToSC.put(1008,0);
    	scCopyToSC.put(1009,0);
    	scCopyToSC.put(1010,0);
    	scCopyToSC.put(1011,0);
    	scCopyToSC.put(1012,0);
    	scCopyToSC.put(1013,0);
    	scCopyToSC.put(1014,0);
    	scCopyToSC.put(1015,0);
    	scCopyToSC.put(1016,0);
    	scCopyToSC.put(1017,0);
    	scCopyToSC.put(1018,0);
    	scCopyToSC.put(1019,0);
    	scCopyToSC.put(1020,0);
    	scCopyUsed.add(1001);
    	scCopyUsed.add(1002);
    	scCopyUsed.add(1003);
    	scCopyUsed.add(1004);
    	scCopyUsed.add(1005);
    	scCopyUsed.add(1006);
    	scCopyUsed.add(1007);
    	scCopyUsed.add(1008);
    	scCopyUsed.add(1009);
    	scCopyUsed.add(1010);
    	scCopyUsed.add(1011);
    	scCopyUsed.add(1012);
    	scCopyUsed.add(1013);
    	scCopyUsed.add(1014);
    	scCopyUsed.add(1015);
    	scCopyUsed.add(1016);
    	scCopyUsed.add(1017);
    	scCopyUsed.add(1018);
    	scCopyUsed.add(1019);
    	scCopyUsed.add(1020);
    	scCopyList.add(1001);
    	scCopyList.add(1002);
    	scCopyList.add(1003);
    	scCopyList.add(1004);
    	scCopyList.add(1005);
    	scCopyList.add(1006);
    	scCopyList.add(1007);
    	scCopyList.add(1008);
    	scCopyList.add(1009);
    	scCopyList.add(1010);
    	scCopyList.add(1011);
    	scCopyList.add(1012);
    	scCopyList.add(1013);
    	scCopyList.add(1014);
    	scCopyList.add(1015);
    	scCopyList.add(1016);
    	scCopyList.add(1017);
    	scCopyList.add(1018);
    	scCopyList.add(1019);
    	scCopyList.add(1020);
    	//add all scCopyId's in map
	    scCopies.put(0, scCopyList);
	    
	    //cluster each (s,d) pair as a separate service chain
	    for(int scID : scUsed){	   
		    for(TrafficNodes tn : serviceChainTN.get(scID)){		    
		    	//update chain index for (s,d)
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1001);	 
		    	}
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1002);	 
		    	}
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1003);	 
		    	}
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp4.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1004);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1005);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1006);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1007);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp4.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1008);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1009);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1010);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1011);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp4.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1012);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1013);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1014);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1015);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp4.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1016);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1013);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1014);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1015);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp4.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1016);	 
		    	}
		    	if( srcVrtGrp5.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1017);	 
		    	}
		    	if( srcVrtGrp5.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1018);	 
		    	}
		    	if( srcVrtGrp5.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1019);	 
		    	}
		    	if( srcVrtGrp5.contains(tn.v1.get_id()) && dstVrtGrp4.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1020);	 
		    	}
		    }		   
	    }
	}
	
	public static void groupCount25(Map<Integer,ArrayList<Integer>> scCopies, Map<Integer,Integer> scCopyToSC, 
			ArrayList<Integer> scCopyUsed, List<Integer> scUsed, Map<Integer,ArrayList<TrafficNodes>> serviceChainTN){
		//25 clusters
		//source clusters
		ArrayList<Integer> srcVrtGrp1 = new ArrayList<Integer>();//create 1st source-vertex group
  		srcVrtGrp1.add(1);srcVrtGrp1.add(2);srcVrtGrp1.add(6);srcVrtGrp1.add(11);		
  		ArrayList<Integer> srcVrtGrp2 = new ArrayList<Integer>();//create 2nd source-vertex group
  		srcVrtGrp2.add(15);srcVrtGrp2.add(16);srcVrtGrp2.add(19);srcVrtGrp2.add(20);srcVrtGrp2.add(21);  		
  		ArrayList<Integer> srcVrtGrp3 = new ArrayList<Integer>();//create 3rd source-vertex group
  		srcVrtGrp3.add(17);srcVrtGrp3.add(18);srcVrtGrp3.add(22);srcVrtGrp3.add(23);srcVrtGrp3.add(24);  		
  		ArrayList<Integer> srcVrtGrp4 = new ArrayList<Integer>();//create 4th source-vertex group
  		srcVrtGrp4.add(9);srcVrtGrp4.add(10);srcVrtGrp4.add(12);srcVrtGrp4.add(13);srcVrtGrp4.add(14);
   		ArrayList<Integer> srcVrtGrp5 = new ArrayList<Integer>();//create 4th source-vertex group
   		srcVrtGrp5.add(3);srcVrtGrp5.add(4);srcVrtGrp5.add(5);srcVrtGrp5.add(7);srcVrtGrp5.add(8);
		
		
		//destination clusters
   		ArrayList<Integer> dstVrtGrp1 = new ArrayList<Integer>();//create 1st destination-vertex group
  		dstVrtGrp1.add(1);dstVrtGrp1.add(2);dstVrtGrp1.add(6);dstVrtGrp1.add(11);		
  		ArrayList<Integer> dstVrtGrp2 = new ArrayList<Integer>();//create 2nd destination-vertex group
  		dstVrtGrp2.add(15);dstVrtGrp2.add(16);dstVrtGrp2.add(19);dstVrtGrp2.add(20);dstVrtGrp2.add(21);  		
  		ArrayList<Integer> dstVrtGrp3 = new ArrayList<Integer>();//create 3rd destination-vertex group
  		dstVrtGrp3.add(17);dstVrtGrp3.add(18);dstVrtGrp3.add(22);dstVrtGrp3.add(23);dstVrtGrp3.add(24);  		
  		ArrayList<Integer> dstVrtGrp4 = new ArrayList<Integer>();//create 4th destination-vertex group
  		dstVrtGrp4.add(9);dstVrtGrp4.add(10);dstVrtGrp4.add(12);dstVrtGrp4.add(13);dstVrtGrp4.add(14);
   		ArrayList<Integer> dstVrtGrp5 = new ArrayList<Integer>();//create 5th destination-vertex group
   		dstVrtGrp5.add(3);dstVrtGrp5.add(4);dstVrtGrp5.add(5);dstVrtGrp5.add(7);dstVrtGrp5.add(8);
  		
  		//add the SC copy Id's to data-structure's
	    //add to map and list's
	  	ArrayList<Integer> scCopyList = new ArrayList<Integer>();
    	scCopyToSC.put(1001,0);
    	scCopyToSC.put(1002,0);
    	scCopyToSC.put(1003,0);
    	scCopyToSC.put(1004,0);
    	scCopyToSC.put(1005,0);
    	scCopyToSC.put(1006,0);
    	scCopyToSC.put(1007,0);
    	scCopyToSC.put(1008,0);
    	scCopyToSC.put(1009,0);
    	scCopyToSC.put(1010,0);
    	scCopyToSC.put(1011,0);
    	scCopyToSC.put(1012,0);
    	scCopyToSC.put(1013,0);
    	scCopyToSC.put(1014,0);
    	scCopyToSC.put(1015,0);
    	scCopyToSC.put(1016,0);
    	scCopyToSC.put(1017,0);
    	scCopyToSC.put(1018,0);
    	scCopyToSC.put(1019,0);
    	scCopyToSC.put(1020,0);
    	scCopyToSC.put(1021,0);
    	scCopyToSC.put(1022,0);
    	scCopyToSC.put(1023,0);
    	scCopyToSC.put(1024,0);
    	scCopyToSC.put(1025,0);
    	scCopyUsed.add(1001);
    	scCopyUsed.add(1002);
    	scCopyUsed.add(1003);
    	scCopyUsed.add(1004);
    	scCopyUsed.add(1005);
    	scCopyUsed.add(1006);
    	scCopyUsed.add(1007);
    	scCopyUsed.add(1008);
    	scCopyUsed.add(1009);
    	scCopyUsed.add(1010);
    	scCopyUsed.add(1011);
    	scCopyUsed.add(1012);
    	scCopyUsed.add(1013);
    	scCopyUsed.add(1014);
    	scCopyUsed.add(1015);
    	scCopyUsed.add(1016);
    	scCopyUsed.add(1017);
    	scCopyUsed.add(1018);
    	scCopyUsed.add(1019);
    	scCopyUsed.add(1020);
    	scCopyUsed.add(1021);
    	scCopyUsed.add(1022);
    	scCopyUsed.add(1023);
    	scCopyUsed.add(1024);
    	scCopyUsed.add(1025);
    	scCopyList.add(1001);
    	scCopyList.add(1002);
    	scCopyList.add(1003);
    	scCopyList.add(1004);
    	scCopyList.add(1005);
    	scCopyList.add(1006);
    	scCopyList.add(1007);
    	scCopyList.add(1008);
    	scCopyList.add(1009);
    	scCopyList.add(1010);
    	scCopyList.add(1011);
    	scCopyList.add(1012);
    	scCopyList.add(1013);
    	scCopyList.add(1014);
    	scCopyList.add(1015);
    	scCopyList.add(1016);
    	scCopyList.add(1017);
    	scCopyList.add(1018);
    	scCopyList.add(1019);
    	scCopyList.add(1020);
    	scCopyList.add(1021);
    	scCopyList.add(1022);
    	scCopyList.add(1023);
    	scCopyList.add(1024);
    	scCopyList.add(1025);
    	//add all scCopyId's in map
	    scCopies.put(0, scCopyList);
	    
	    //cluster each (s,d) pair as a separate service chain
	    for(int scID : scUsed){	   
		    for(TrafficNodes tn : serviceChainTN.get(scID)){		    
		    	//update chain index for (s,d)
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1001);	 
		    	}
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1002);	 
		    	}
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1003);	 
		    	}
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp4.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1004);	 
		    	}
		    	if( srcVrtGrp1.contains(tn.v1.get_id()) && dstVrtGrp5.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1005);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1006);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1007);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1008);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp4.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1009);	 
		    	}
		    	if( srcVrtGrp2.contains(tn.v1.get_id()) && dstVrtGrp5.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1010);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1011);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1012);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1013);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp4.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1014);	 
		    	}
		    	if( srcVrtGrp3.contains(tn.v1.get_id()) && dstVrtGrp5.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1015);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1016);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1017);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1018);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp4.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1019);	 
		    	}
		    	if( srcVrtGrp4.contains(tn.v1.get_id()) && dstVrtGrp5.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1020);	 
		    	}		    	
		    	if( srcVrtGrp5.contains(tn.v1.get_id()) && dstVrtGrp1.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1021);	 
		    	}
		    	if( srcVrtGrp5.contains(tn.v1.get_id()) && dstVrtGrp2.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1022);	 
		    	}
		    	if( srcVrtGrp5.contains(tn.v1.get_id()) && dstVrtGrp3.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1023);	 
		    	}
		    	if( srcVrtGrp5.contains(tn.v1.get_id()) && dstVrtGrp4.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1024);	 
		    	}
		    	if( srcVrtGrp5.contains(tn.v1.get_id()) && dstVrtGrp5.contains(tn.v2.get_id()) ){
		    		tn.updateChainIndex(1025);	 
		    	}
		    }		   
	    }
	}
	
	public static void runTest() throws Exception{
		//generate the graph object
	    Graph g = PreProcVer1.makeGraphObject();
	    //set all nodes to type switches
	    preProcFunctions.makeAllVrtSwitches(g);
	    //populate ChainSet details
	    InputConstants.populateServices();
	    //print the graph object
	    preProcFunctions.printGraph(g);
	    
	    //calculate betweenness centrality
	    Map<BaseVertex,Double> bcOfVertex = preProcFunctions.calculateBetweenessCentrality(g);
	    
	    //calculate ranking of vertices based on 
	    //product of betweeness-centrality and degree centrality
	    Map<BaseVertex,Double> vertexRank = preProcFunctions.calProductOfBCandDeg(g,bcOfVertex);
	    //list of vertex ranks
	    List<VertexRank> rankList = new ArrayList<VertexRank>();
	    //make list
	    for(Map.Entry<BaseVertex, Double> entry : vertexRank.entrySet()){
	    	VertexRank obj = new VertexRank(entry.getKey(),entry.getValue());
	    	rankList.add(obj);
	    }	  
	    //sort list in descending order
	    Collections.sort(rankList);
	    //print out the vertex Ranking
	    preProcFunctions.printVertexRanking(rankList);
	    
	    //generate the routes for the traffic pairs
	    HashMap<NodePair,List<Path>> sdpaths = preProcFunctions.findRoutesForSDpairs(g);
	    
	    //get the set of service chains
	  	Map<Integer,ServiceChain> ChainSet = PreProcVer1.populateChainSetBasedOnScenario1();
		// print out the Set of Service Chains
		preProcFunctions.printServiceChains(ChainSet);
		
		//total list of VNF available
		List<FuncPt> vnf_list = PreProcVer1.totalListOfVNFs();
		
		//NFV nodes 
		//All nodes are NFV capable
		int numOfNfvNodes = 24;
				
		// SD pairs between which we desire traffic to be
		// Store each s-d pair
		List<TrafficNodes> pair_list = new ArrayList<TrafficNodes>();
		//generate all (s,d) pairs
		for(BaseVertex srcVrt: g.get_vertex_list()){
			for(BaseVertex destVrt: g.get_vertex_list()){
				if(srcVrt.get_id() != destVrt.get_id()){					
					pair_list.add(new TrafficNodes(srcVrt,destVrt,0,1000));					
				}
			}			
		}	
		
		//List of the service chains to be deployed
		List<Integer> scUsed = preProcFunctions.serviceChainsUsed(pair_list);		
		//print out the pair lists
		preProcFunctions.printSDpairs(pair_list, scUsed);	    
		
		//VNFs used across the service chains deployed
		List<FuncPt> func_list = preProcFunctions.listOfVNFsUsed(vnf_list, ChainSet, scUsed);		
		//print out the function list
		preProcFunctions.printListOfVNFsUsed(func_list);
		
		//traffic pairs for each service chain deployed	
		Map<Integer,ArrayList<TrafficNodes>> serviceChainTN = preProcFunctions.sdPairsforServiceChain(scUsed, pair_list);
	    //print out the traffic nodes available for each service chain
	    preProcFunctions.printTrafficNodesForServiceChains(scUsed, serviceChainTN);	
	    
	    //split a single service chain into multiple service chains
	  	Map<Integer,ArrayList<Integer>> scCopies = new HashMap<Integer,ArrayList<Integer>>();		
	  	//new service to old service
	  	Map<Integer,Integer> scCopyToSC = new HashMap<Integer,Integer>();
	  	//create list of service chains
	  	ArrayList<Integer> scCopyUsed = new ArrayList<Integer>();
	  	
	  	//divide (s,d) pairs in group of 2
//	  	groupCount2(scCopies,scCopyToSC,scCopyUsed,scUsed,serviceChainTN);
//	  	groupCount3(scCopies,scCopyToSC,scCopyUsed,scUsed,serviceChainTN);
//		groupCount4(scCopies,scCopyToSC,scCopyUsed,scUsed,serviceChainTN);
//		groupCount5(scCopies,scCopyToSC,scCopyUsed,scUsed,serviceChainTN);
//		groupCount6(scCopies,scCopyToSC,scCopyUsed,scUsed,serviceChainTN);
//		groupCount8(scCopies,scCopyToSC,scCopyUsed,scUsed,serviceChainTN);
//		groupCount9(scCopies,scCopyToSC,scCopyUsed,scUsed,serviceChainTN);
//		groupCount12(scCopies,scCopyToSC,scCopyUsed,scUsed,serviceChainTN);
//	  	groupCount16(scCopies,scCopyToSC,scCopyUsed,scUsed,serviceChainTN);
//	  	groupCount20(scCopies,scCopyToSC,scCopyUsed,scUsed,serviceChainTN);
	  	groupCount25(scCopies,scCopyToSC,scCopyUsed,scUsed,serviceChainTN);
	    
	    //max number of VNFs
	    Map<Integer, ArrayList<Integer>> funcInSC = preProcFunctions.vnfInSCs(scUsed, func_list, ChainSet);
	    Map<Integer,Integer> CountMaxVNF = preProcFunctions.countMaxVnfBasedOnSdPairs(ChainSet, funcInSC, serviceChainTN);
	    
	    //replica constraint per VNF
	    Map<Integer,Integer> replicaPerVNF = new HashMap<Integer,Integer>(CountMaxVNF);
	    
	    //DC node placement		  
  		ArrayList<Integer> dcNodes = new ArrayList<Integer>();  
  		  
  		//place the DC nodes
  		placeNFVI.placeDC(g, dcNodes);
  		//place the NFV nodes
  		placeNFVI.placeNFVPoP(g, rankList, numOfNfvNodes);
  		//create the list of NFV-capable nodes
  		ArrayList<BaseVertex> nfv_nodes = new ArrayList<BaseVertex>();
  		placeNFVI.makeNFVList(g, nfv_nodes);
  		
  		//create the list of NFVI nodes
  		//add the set of DC nodes to the set of nfv nodes
  		ArrayList<BaseVertex> nodesNFVI = new ArrayList<BaseVertex>();
  		//add the set of NFV nodes
  		nodesNFVI.addAll(nfv_nodes);
  		//print the nodes with NFV capability
  		placeNFVI.printNFVINodes(nodesNFVI);
  		
  		//list of vertices without the NFV nodes
  		ArrayList<BaseVertex> vertex_list_without_nfvi_nodes = new ArrayList<BaseVertex>(g._vertex_list);
  		//assuming that the NFV and DC node sets are exclusive				 
  		vertex_list_without_nfvi_nodes.removeAll(nodesNFVI);		    
  		
  	  	  
		//valid configurations for each service chain //each (s,d) selects a valid configuration
		Map<Integer,ArrayList<HuerVarZ>> configsPerSC = new HashMap<Integer,ArrayList<HuerVarZ>>();
		Map<TrafficNodes,SdDetails> configPerSD = new HashMap<TrafficNodes,SdDetails>();
		
	
		//cluster traffic pairs according to service chains
		serviceChainTN = preProcFunctions.sdPairsforServiceChain(scCopyUsed, pair_list);
		 //print out the traffic nodes available for each service chain
	    preProcFunctions.printTrafficNodesForServiceChains(scCopyUsed, serviceChainTN);
		//get configuration per SC
		configsPerSC = BaseHeuristic2.singleConfigBasedOnAdj(scUsed, ChainSet, nodesNFVI, scCopyUsed, scCopyToSC, sdpaths, serviceChainTN, scCopies, configPerSD);
		
		//print the configurations for each SC
	  	preProcFunctions.printConfigsPerSCforBH2(scUsed, configsPerSC);					  
	  	//print the configuration for each (s,d)
	  	preProcFunctions.printConfigForSD(configPerSD);
	  	
	    //calculate the core and link constraints
  		boolean coreCstr = false;
  		boolean capCstr = false;
  		Map<BaseVertex,Double> cpuCoreCount = new HashMap<BaseVertex,Double>();
  		Map<NodePair,Double> linkCapacity = new HashMap<NodePair,Double>();
  		CG.runCG(coreCstr,capCstr,cpuCoreCount,linkCapacity,g, ChainSet, pair_list, scUsed, vnf_list, func_list, serviceChainTN, nfv_nodes, 
  				  nodesNFVI, vertex_list_without_nfvi_nodes, scCopies, scCopyToSC, configsPerSC, configPerSD, CountMaxVNF, replicaPerVNF, numOfNfvNodes);
		
	}

}
