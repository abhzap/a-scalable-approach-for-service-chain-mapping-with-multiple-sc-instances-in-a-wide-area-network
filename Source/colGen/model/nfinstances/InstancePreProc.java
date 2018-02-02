package colGen.model.nfinstances;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Given.InputConstants;
import ILP.NodePair;
import ILP.TrafficNodes;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;

public class InstancePreProc {
	
	public static Graph makeGraphObject(String fileName) throws Exception{		
		//build graph object from given network file
	    Class<?> cls = Class.forName("colGen.model.simulation.Sim");		
	    //returns the ClassLoader
	    ClassLoader cLoader = cls.getClassLoader();
	    //print out the class name
	    System.out.println("Class Name : " + cLoader.getClass());
	    //finds the resource with the given name
	    InputStream networkFileStream = cLoader.getResourceAsStream(fileName);
	    //return the Graph Object
	    return new Graph(networkFileStream);
	}
	
	public static List<TrafficNodes> readSDPairs(String sc0, String sc1, String sc2, String sc3, InputStream fileStream, Map<NodePair, List<Path>> sdpaths){
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
							double traffic = Double.valueOf(parts[2])*1000;//to convert to Mbps
							String sc = parts[3]+parts[4]+parts[5]+parts[6]+parts[7];
							int chain_index = 0;
							if(sc.equals(sc1)){
								chain_index=1;
							}else if(sc.equals(sc2)){
								chain_index=2;
							}else if(sc.equals(sc3)){
								chain_index=3;
							}
							TrafficNodes temp = null;
							for( Map.Entry<NodePair,List<Path>> entry : sdpaths.entrySet()){
								if(entry.getKey().v1.get_id()==src_index && entry.getKey().v2.get_id()==dest_index){
									temp = new TrafficNodes(entry.getKey().v1, entry.getKey().v2, chain_index, traffic);									
								}
							}							
							//check if traffic node is already in list
							if(sd_traffic.contains(temp)){
								int indexOfObject = sd_traffic.indexOf(temp);
								sd_traffic.get(indexOfObject).addTraffic(traffic);
							}else{
								sd_traffic.add(temp);
							}
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
	
	public static List<TrafficNodes> setOfSDpairs(String sc0, String sc1, String sc2, String sc3, String filename,HashMap<NodePair, List<Path>> sdpaths) throws Exception{
		//build graph object from given network file
	    Class<?> cls = Class.forName("colGen.model.simulation.Sim");		
	    //returns the ClassLoader
	    ClassLoader cLoader = cls.getClassLoader();
		InputStream sdPairStream = cLoader.getResourceAsStream(filename);
		return readSDPairs(sc0, sc1, sc2, sc3, sdPairStream, sdpaths);
	}
	
	

}
