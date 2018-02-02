package FileOps;

import java.io.FileWriter;
import java.util.List;

import Given.InputConstants;
import ILP.TrafficNodes;

public class WriteFile {
	
	//use to generate the kind of traffic profile required
	public static void TrafficDetails(List<TrafficNodes> pair_list){
		try{			
			java.io.BufferedWriter reqWriter = new java.io.BufferedWriter(
					new FileWriter(InputConstants.FILE_READ_PATH + InputConstants.TRAFFIC_FILE));
			reqWriter.write("\n" + " source " + " destination "  + " hour " );
			reqWriter.write("\n" + "**************START OF FILE****************************");	
			for(TrafficNodes pair : pair_list ){
				reqWriter.write("\n" + pair.v1.get_id() + "\t" + pair.v2.get_id() + "\t" + pair.flow_traffic);
			}
			reqWriter.write("\n" + "**************END OF FILE****************************");	
			reqWriter.close();
			System.out.println("************Traffic File created***************");
		}catch(Exception exp){
			exp.printStackTrace();
			System.exit(1);
		}		
		
	}
	
	//use to generate the kind of traffic profile required
	

}
