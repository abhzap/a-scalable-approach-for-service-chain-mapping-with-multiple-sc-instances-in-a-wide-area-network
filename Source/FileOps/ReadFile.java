package FileOps;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Given.InputConstants;
import ILP.ChainConfig;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;


public class ReadFile {
	
		
	//read the given s-d pairs and add the traffic flows to them
	//stream reading function
	public static List<TrafficNodes> readSDPairs(InputStream fileStream, Map<NodePair, List<Path>> sdpaths){
		ArrayList<TrafficNodes> sd_traffic = new ArrayList<TrafficNodes>();
		int lineNum = 0;
		BufferedReader br = null;		
		String line_data;
		boolean read_vars = false;
	
				try{
					br = new BufferedReader(new InputStreamReader(fileStream));	
					//skip the comments in the file
					do{
						line_data = br.readLine();
						lineNum++;						
					}while(line_data != null
							&& !line_data.contains(InputConstants.START_OF_FILE_DELIMETER));
					//Error while reading file
					if (line_data == null) {
						System.err.println("ERROR: Incorrect file syntax at line number:"
										+ lineNum);
						
					}
					//Check that the end of file has not reached        	
					//Check that the end of file has not reached        	
					while((line_data=br.readLine()) != null && !line_data.contains(InputConstants.END_OF_FILE_DELIMETER)){
						read_vars = true;							
						if(read_vars){
						    //value separated from variable
							String[] parts = line_data.split("\t");	
							int src_index = Integer.valueOf(parts[0]);							
							int dest_index = Integer.valueOf(parts[1]);	
							int chain_index = Integer.valueOf(parts[2]);
							TrafficNodes temp = null;
							for( Map.Entry<NodePair,List<Path>> entry : sdpaths.entrySet()){
								if(entry.getKey().v1.get_id()==src_index && entry.getKey().v2.get_id()==dest_index){
									temp = new TrafficNodes(entry.getKey().v1, entry.getKey().v2, chain_index);									
								}
							}											
							//add the TrafficNodes object to the list
							sd_traffic.add(temp);
						}
					}		
					//close the buffered writer from writing the file
					br.close();				
				}catch(Exception exp){
					System.err.println("Error in reading the file");				
				}
		System.out.println("Source Destination pairs have been read");
		return sd_traffic;
	}	
	
	//string reading function
	public static List<TrafficNodes> readSDPairs(String filename, Map<TrafficNodes,List<Path>> sdpaths){
		ArrayList<TrafficNodes> sd_traffic = new ArrayList<TrafficNodes>();
		int lineNum = 0;
		BufferedReader br = null;		
		String line_data;
		boolean read_vars = false;
	
				try{
					br = new BufferedReader(new FileReader(filename));					
					//skip the comments in the file
					do{
						line_data = br.readLine();
						lineNum++;						
					}while(line_data != null
							&& !line_data.contains(InputConstants.START_OF_FILE_DELIMETER));
					//Error while reading file
					if (line_data == null) {
						System.err.println("ERROR: Incorrect file syntax at line number:"
										+ lineNum);
						
					}
					//Check that the end of file has not reached        	
					while((line_data=br.readLine()) != null && !line_data.contains(InputConstants.END_OF_FILE_DELIMETER)){
						read_vars = true;							
						if(read_vars){
						    //value separated from variable
							String[] parts = line_data.split("\t");	
							int src_index = Integer.valueOf(parts[0]);							
							int dest_index = Integer.valueOf(parts[1]);	
							int chain_index = Integer.valueOf(parts[2]);
							TrafficNodes temp = new TrafficNodes();
							for( Map.Entry<TrafficNodes,List<Path>> entry : sdpaths.entrySet()){
								if(entry.getKey().v1.get_id()==src_index && entry.getKey().v2.get_id()==dest_index){
									temp = entry.getKey();
									temp.updateChainIndex(chain_index);
								}
							}											
							//add the TrafficNodes object to the list
							sd_traffic.add(temp);
						}
					}		
					//close the buffered writer from writing the file
					br.close();				
				}catch(Exception exp){
					System.err.println("Error in reading the file");				
				}
		System.out.println("Source Destination pairs have been read");
		return sd_traffic;
	}
		
		
		
		
	
	//read the Set of Service Chain Configurations
	//stream reading functions
	public static ArrayList<ChainConfig> readChainConfig(InputStream fileStream, Graph g){			
		ArrayList<ChainConfig> ConfigList = new ArrayList<ChainConfig>();			
		int lineNum = 0;
		BufferedReader br = null;		
		String line_data;
		boolean read_vars = false;
	
				try{
					br = new BufferedReader(new InputStreamReader(fileStream));					
					//skip the comments in the file
					do{
						line_data = br.readLine();
						lineNum++;						
					}while(line_data != null
							&& !line_data.contains(InputConstants.START_OF_FILE_DELIMETER));
					//Error while reading file
					if (line_data == null) {
						System.err.println("ERROR: Incorrect file syntax at line number:"
										+ lineNum);
						
					}
					//Check that the end of file has not reached        	
					while((line_data=br.readLine()) != null && !line_data.contains(InputConstants.END_OF_FILE_DELIMETER)){
						read_vars = true;							
						if(read_vars){
						    //index separated from VNF sequence
							String[] parts = line_data.split("\t");	
							//store the sequence of VNFs
						    ArrayList<BaseVertex> temp = new ArrayList<BaseVertex>();
						    for(String x : Arrays.asList(parts[1].split(","))){
							  temp.add(g.get_vertex(Integer.parseInt(x)));
						    }
						   //add the new configuration to the list of configurations
						   ConfigList.add(new ChainConfig(Integer.parseInt(parts[0]), temp));  
						}
					}						
					//close the buffered writer from writing the file
					br.close();				
				}catch(Exception exp){
					System.err.println("Error in reading the file");				
				}
		System.out.println("Configurations of service chains have been read");
		return ConfigList;			
	}	
	//string reading functions
	public static ArrayList<ChainConfig> readChainConfig(String filename, Graph g){			
		ArrayList<ChainConfig> ConfigList = new ArrayList<ChainConfig>();			
		int lineNum = 0;
		BufferedReader br = null;		
		String line_data;
		boolean read_vars = false;
	
				try{
					br = new BufferedReader(new FileReader(filename));					
					//skip the comments in the file
					do{
						line_data = br.readLine();
						lineNum++;						
					}while(line_data != null
							&& !line_data.contains(InputConstants.START_OF_FILE_DELIMETER));
					//Error while reading file
					if (line_data == null) {
						System.err.println("ERROR: Incorrect file syntax at line number:"
										+ lineNum);
						
					}
					//Check that the end of file has not reached        	
					while((line_data=br.readLine()) != null && !line_data.contains(InputConstants.END_OF_FILE_DELIMETER)){
						read_vars = true;							
						if(read_vars){
						    //index separated from VNF sequence
							String[] parts = line_data.split("\t");	
							//store the sequence of VNFs
						    ArrayList<BaseVertex> temp = new ArrayList<BaseVertex>();
						    for(String x : Arrays.asList(parts[1].split(","))){
							  temp.add(g.get_vertex(Integer.parseInt(x)));
						    }
						   //add the new configuration to the list of configurations
						   ConfigList.add(new ChainConfig(Integer.parseInt(parts[0]), temp));  
						}
					}						
					//close the buffered writer from writing the file
					br.close();				
				}catch(Exception exp){
					System.err.println("Error in reading the file");				
				}
		System.out.println("Configurations of service chains have been read");
		return ConfigList;			
	}
		
		
	
	
	//read the Set of Service Chains
	//stream reading functions
	public static Map<Integer,ServiceChain> readChainSet(InputStream fileStream){		
		Map<Integer,ServiceChain> ChainSet = new HashMap<Integer,ServiceChain>();
		List<ServiceChain> checkSet = new ArrayList<ServiceChain>();
		int lineNum = 0;
		BufferedReader br = null;		
		String line_data;
		boolean read_vars = false;
	
				try{
					br = new BufferedReader(new InputStreamReader(fileStream));							
					//skip the comments in the file
					do{
						line_data = br.readLine();
						lineNum++;						
					}while(line_data != null
							&& !line_data.contains(InputConstants.START_OF_FILE_DELIMETER));
					//Error while reading file
					if (line_data == null) {
						System.err.println("ERROR: Incorrect file syntax at line number:"
										+ lineNum);
						
					}
					//Check that the end of file has not reached        	
					while((line_data=br.readLine()) != null && !line_data.contains(InputConstants.END_OF_FILE_DELIMETER)){
						read_vars = true;							
						if(read_vars){
						    //index separated from VNF sequence
							String[] parts = line_data.split("\t");	
							//store the sequence of VNFs
						    ArrayList<Integer> temp = new ArrayList<Integer>();
						    for(String x : Arrays.asList(parts[1].split(","))){
							   temp.add(Integer.parseInt(x));
						    }
						   /* for(Integer x : temp){
						    	System.out.print(x + ", ");
						    }
						    System.out.println();
						    //add element to checker
						    checkSet.add(new ServiceChain ( Integer.parseInt(parts[0]) , temp ));*/
						    //add element to Map
						    if(parts.length > 2){
						    	ChainSet.put( Integer.parseInt(parts[0]), new ServiceChain ( Integer.parseInt(parts[0]) , temp , Double.parseDouble(parts[2])) );
						    }else{
						    	ChainSet.put( Integer.parseInt(parts[0]), new ServiceChain ( Integer.parseInt(parts[0]) , temp ) );
						    }
						}
					}	
					/*for(ServiceChain c : checkSet){
						for(Integer VNF : c.getChainSeq()){
							System.out.print(VNF + ", ");
						}
						System.out.println();
					}*/
					//close the buffered writer from writing the file
					br.close();				
				}catch(Exception exp){
					System.err.println("Error in reading the file");				
				}
		System.out.println("Set of service chains have been read");
		return ChainSet;		
	}
	//string reading functions
	public static Map<Integer,ServiceChain> readChainSet(String filename){		
		Map<Integer,ServiceChain> ChainSet = new HashMap<Integer,ServiceChain>();
		List<ServiceChain> checkSet = new ArrayList<ServiceChain>();
		int lineNum = 0;
		BufferedReader br = null;		
		String line_data;
		boolean read_vars = false;
	
				try{
					br = new BufferedReader(new FileReader(filename));					
					//skip the comments in the file
					do{
						line_data = br.readLine();
						lineNum++;						
					}while(line_data != null
							&& !line_data.contains(InputConstants.START_OF_FILE_DELIMETER));
					//Error while reading file
					if (line_data == null) {
						System.err.println("ERROR: Incorrect file syntax at line number:"
										+ lineNum);
						
					}
					//Check that the end of file has not reached        	
					while((line_data=br.readLine()) != null && !line_data.contains(InputConstants.END_OF_FILE_DELIMETER)){
						read_vars = true;							
						if(read_vars){
						    //index separated from VNF sequence
							String[] parts = line_data.split("\t");	
							//store the sequence of VNFs
						    ArrayList<Integer> temp = new ArrayList<Integer>();
						    for(String x : Arrays.asList(parts[1].split(","))){
							   temp.add(Integer.parseInt(x));
						    }
						   /* for(Integer x : temp){
						    	System.out.print(x + ", ");
						    }
						    System.out.println();
						    //add element to checker
						    checkSet.add(new ServiceChain ( Integer.parseInt(parts[0]) , temp ));*/
						    //add element to Map
						    ChainSet.put( Integer.parseInt(parts[0]), new ServiceChain ( Integer.parseInt(parts[0]) , temp ) );
						}
					}	
					/*for(ServiceChain c : checkSet){
						for(Integer VNF : c.getChainSeq()){
							System.out.print(VNF + ", ");
						}
						System.out.println();
					}*/
					//close the buffered writer from writing the file
					br.close();				
				}catch(Exception exp){
					System.err.println("Error in reading the file");				
				}
		System.out.println("Set of service chains have been read");
		return ChainSet;		
	}
	
	
	
	
	//read file with core-throughput details
	//stream reading functions
	public static  List<FuncPt> readFnPt(InputStream fileStream){
		List<FuncPt> fdet = new ArrayList<FuncPt>();
		int lineNum = 0;
		BufferedReader br = null;		
		String line_data;
		boolean read_vars = false;
	
				try{
					br = new BufferedReader(new InputStreamReader(fileStream));						
					//skip the comments in the file
					do{
						line_data = br.readLine();
						lineNum++;						
					}while(line_data != null
							&& !line_data.contains(InputConstants.START_OF_FILE_DELIMETER));
					//Error while reading file
					if (line_data == null) {
						System.err.println("ERROR: Incorrect file syntax at line number:"
										+ lineNum);
						
					}
					//Check that the end of file has not reached        	
					while((line_data=br.readLine()) != null && !line_data.contains(InputConstants.END_OF_FILE_DELIMETER)){
						read_vars = true;							
						if(read_vars){
						    //value separated from variable
							String[] parts = line_data.split("\t");	
//							System.out.println(parts[0] + "," + parts[1]);							;							
							//add the TrafficNodes object to the list
							if(parts.length > 2){
								fdet.add(new FuncPt(Integer.valueOf(parts[0]),Double.valueOf(parts[1]),Double.valueOf(parts[2])));
							}else{
								fdet.add(new FuncPt(Integer.valueOf(parts[0]),Double.valueOf(parts[1])));
							}
						}
					}		
					//close the buffered writer from writing the file
					br.close();				
				}catch(Exception exp){
					System.err.println("Error in reading the file");				
				}
		System.out.println("Function details have been read");
		return fdet;
		
	}
	//string reading functions
	public static  List<FuncPt> readFnPt(String filename){
		List<FuncPt> fdet = new ArrayList<FuncPt>();
		int lineNum = 0;
		BufferedReader br = null;		
		String line_data;
		boolean read_vars = false;
	
				try{
					br = new BufferedReader(new FileReader(filename));					
					//skip the comments in the file
					do{
						line_data = br.readLine();
						lineNum++;						
					}while(line_data != null
							&& !line_data.contains(InputConstants.START_OF_FILE_DELIMETER));
					//Error while reading file
					if (line_data == null) {
						System.err.println("ERROR: Incorrect file syntax at line number:"
										+ lineNum);
						
					}
					//Check that the end of file has not reached        	
					while((line_data=br.readLine()) != null && !line_data.contains(InputConstants.END_OF_FILE_DELIMETER)){
						read_vars = true;							
						if(read_vars){
						    //value separated from variable
							String[] parts = line_data.split("\t");	
//							System.out.println(parts[0] + "," + parts[1]);							;							
							//add the TrafficNodes object to the list
							fdet.add(new FuncPt(Integer.valueOf(parts[0]),Double.valueOf(parts[1])));
						}
					}		
					//close the buffered writer from writing the file
					br.close();				
				}catch(Exception exp){
					System.err.println("Error in reading the file");				
				}
		System.out.println("Function details have been read");
		return fdet;
		
	}
	
	
	//read the given s-d pairs and add the traffic flows to them	
	public static List<TrafficNodes> readSDPairs(Map<TrafficNodes,List<Path>> sdpaths){
		ArrayList<TrafficNodes> sd_traffic = new ArrayList<TrafficNodes>();
		int lineNum = 0;
		BufferedReader br = null;		
		String line_data;
		boolean read_vars = false;
	
				try{
					br = new BufferedReader(new FileReader(InputConstants.FILE_READ_PATH + InputConstants.SD_PAIRS ));					
					//skip the comments in the file
					do{
						line_data = br.readLine();
						lineNum++;						
					}while(line_data != null
							&& !line_data.contains(InputConstants.START_OF_FILE_DELIMETER));
					//Error while reading file
					if (line_data == null) {
						System.err.println("ERROR: Incorrect file syntax at line number:"
										+ lineNum);
						
					}
					//Check that the end of file has not reached        	
					while((line_data=br.readLine()) != null && !line_data.contains(InputConstants.END_OF_FILE_DELIMETER)){
						read_vars = true;							
						if(read_vars){
						    //value separated from variable
							String[] parts = line_data.split("\t");	
							int src_index = Integer.valueOf(parts[0]);							
							int dest_index = Integer.valueOf(parts[1]);						
							TrafficNodes temp = new TrafficNodes();
							for( Map.Entry<TrafficNodes,List<Path>> entry : sdpaths.entrySet()){
								if(entry.getKey().v1.get_id()==src_index && entry.getKey().v2.get_id()==dest_index){
									temp = entry.getKey();
								}
							}						
							//add the TrafficNodes object to the list
							sd_traffic.add(temp);
						}
					}		
					//close the buffered writer from writing the file
					br.close();				
				}catch(Exception exp){
					System.err.println("Error in reading the file");				
				}
		System.out.println("Source Destination pairs have been read");
		return sd_traffic;
	}
	
	
	public static void readTraffic(List<TrafficNodes> pair_list){		
		int lineNum = 0;
		BufferedReader br = null;		
		String line_data;
		boolean read_vars = false;
	
				try{
					br = new BufferedReader(new FileReader(InputConstants.FILE_READ_PATH + InputConstants.TRAFFIC_FILE ));					
					//skip the comments in the file
					do{
						line_data = br.readLine();
						lineNum++;
						System.out.println(lineNum + " : " + line_data);
					}while(line_data != null
							&& !line_data.contains(InputConstants.START_OF_FILE_DELIMETER));
					//Error while reading file
					if (line_data == null) {
						System.err.println("ERROR: Incorrect file syntax at line number:"
										+ lineNum);
						
					}
					//Check that the end of file has not reached        	
					while((line_data=br.readLine()) != null && !line_data.contains(InputConstants.END_OF_FILE_DELIMETER)){
						read_vars = true;							
						if(read_vars){
						    //value separated from variable
							String[] parts = line_data.split("\t");		
							int src_index = Integer.valueOf(parts[0]);
							int dest_index = Integer.valueOf(parts[1]);
							System.out.println(src_index + "," + dest_index);
							for(TrafficNodes pr : pair_list){
								if(pr.v1.get_id() == src_index && pr.v2.get_id() == dest_index){
									pr.flow_traffic = Integer.valueOf(parts[2]);
								}
							}
							System.out.println("source : " + parts[0] + " destination : " + parts[1] + " traffic : " + parts[2] + " , " + parts[3] + " , " + parts[4]);							
						}
					}		
					//close the buffered writer from writing the file
					br.close();				
				}catch(Exception exp){
					System.out.println("Error in reading the file");				
				}
		System.out.println("Traffic File has been read");
//		return pair_list;
	}
	
	
	
	public static void outputVariables(){		
		int lineNum = 0;
		BufferedReader br = null;		
		String line_data;
		boolean read_vars = false;			
				try{
					br = new BufferedReader(new FileReader(InputConstants.FILE_READ_PATH + InputConstants.OUTPUT_VAR +  ".txt"));
					System.out.println(InputConstants.FILE_READ_PATH + InputConstants.OUTPUT_VAR +  ".txt");
					//skip the comments in the file
					do{
						line_data = br.readLine();
						lineNum++;
						System.out.println(lineNum + " : " + line_data);
					}while(line_data != null
							&& !line_data.contains(InputConstants.START_OF_FILE_DELIMETER));
					//Error while reading file
					if (line_data == null) {
						System.err.println("ERROR: Incorrect file syntax at line number:"
										+ lineNum);
						
					}
					//Check that the end of file has not reached        	
					while((line_data=br.readLine()) != null && !line_data.contains(InputConstants.END_OF_FILE_DELIMETER)){
						read_vars = true;							
						if(read_vars){
						    //value separated from variable
							String[] parts = line_data.split("\\s+");
							String[] gvar = parts[0].split("_");		
							System.out.println("func_id : " + gvar[1] + ", " + gvar[2] + ", " + gvar[3]);							
						}
					}		
					//close the buffered writer from writing the file
					br.close();				
				}catch(Exception exp){
					System.out.println("Error in reading the file");				
				}		
	}
	
				
}
