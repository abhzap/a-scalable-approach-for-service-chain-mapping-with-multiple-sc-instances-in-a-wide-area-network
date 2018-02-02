package Given;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputConstants {	
	
	public static final int simeplexInterationLimit = 1;
	public static final int masterProblemConstraintCount = 3;
	public static final int pricingProblemConstraintCount = 21;
	public static final int configCountPerServiceChain = 30;
	public static final String NFV_Strategy = "NFV_ALL";
	public static final double trafficLoad = 100;//1Tb in Mbps
	public static final int k_paths = 40;//atleast 40 paths to satisfy the connections	
	public static final boolean slackVars = true;	
	public static final boolean coreCstr = false;
	public static final boolean capacityCstr = false;
	public static final boolean latencyCstr = false;
	public static final int coreCount = 128000;
	public static final int BANDWIDTH_PER_LAMBDA = 40000;//40 Gbps
//	public static final boolean checkMode = false;
	public static final int numOfDCnodes = 0;	
//	public static final int BigM_x_vf = 100;
	public static final int functionInstances = 100;
	public static final String SD_PAIRS = "sd4nodenet.txt";//"scenario2.txt";//"testICC2.txt";//"testICC.txt";	
	public static final String NETWORK_FILE_NAME = "cost239.txt";//"4nodenet.txt";//"6nodenet.txt";//"cost239.txt";//"nsf_14_network.txt";//"eon16.txt";//"japan19.txt";//"japan23.txt";//"us24network.txt";//"3nodenet.txt";//"germany.txt";	
	public static final double Big_M = 99999.0;//seems to be the optimal value for M	
	//to define the permissible negative values	
	public static final double RC_EPS = 1.0e-6;
	public static final String CHAIN_SET = "ChainSetTON.txt";//"ChainSet.txt"
	public static final String FUNCTION_DETAILS = "function_core_latency_throughput.txt";//"function_core_throughput.txt";
	public static final List<Integer> NOT_DC_SET = Arrays.asList(4,5,7,11,12,14);  
	public static final List<Double> TRAFFIC = Arrays.asList(1000.0);
	public static final List<Integer> CORE_NUM_LIST = Arrays.asList(4000);
	//Arrays.asList(4000,8000,16000,32000,64000,128000,256000,512000,1024000,2048000);
	public static final List<Integer> NFV_4 = Arrays.asList(3,8,10);
	public static final List<Integer> NFV_3 = Arrays.asList(1,5,6,7,9,13);
	public static final List<Integer> NFV_SL = Arrays.asList(1,2,3,4);
	public static final List<Integer> NFV_SR = Arrays.asList(9,11,12,13,14);
	public static final List<Integer> NFV_SC = Arrays.asList(5,6,7,8,10);	
	public static final List<String> DC_Strategy_List = Arrays.asList("DC_1","DC_2","DC_3","DC_4");	
	public static final List<String> NFV_Strategy_List = Arrays.asList("NFV_ALL","NFV_4","NFV_3");	
	public static final int shortest_path = 1;//finds the shortest path between any 2 points
	public static final int allowed_config_paths = 1;	
	public static final String CONFIG_SET = "Configuration.txt";		
//	public static final int NFV_NODE_CORE_COUNT = 20000;//1,2,4,8,12,16,20,24,48
	//epsilon values for making delta change
	//public static final double epsValue = 1.0e-6;
	//non-zero value tolerance //default value = 1.0e-5
	//can take values from 1.0e-5 to 1.0e-1
	public static final double integerTolerance = 1.0e-5;	
	//set the optimal tolerance for simplex method //default value = 1.0e-6
	//can take values from 1.0e-9 to 1.0e-1
	//public static final double simplexOptimalityTolerance = 1.0e-6;
	//set the feasibility tolerance //default value = 1.0e-6
	//can take values from 1.0e-9 to 1.0e-1
	//public static final double feasbilityTolerance = 1.0e-6;
	//VNF replica constraint
//	public static final int replicaLimit = 1;
	//number of initial configurations added by the heuristic
	public static final int configCount = 1;
	//the service chain for which the pricing problem is being solved
//	public static final int SC_ID = 7;
	public static final boolean allPossiblePlcmnts = true;
    public static final int placementCount = 1;
	public static final double SEC_TO_MICROSEC = 1000000;	
	public static final double SPEED_OF_LIGHT = 300000;//in kilometers/second
	public static final String FILE_READ_PATH = "Data/";	
	public static final String START_OF_FILE_DELIMETER = "START OF FILE";
	public static final String END_OF_FILE_DELIMETER = "END OF FILE";
	public static final String START_OF_NODES_DELIMETER = "START OF NODE";
	public static final String START_OF_LINKS_DELIMETER = "START OF LINK";
	
	
	public static final List<Integer> HQ_Nodes = Arrays.asList(2,8,10);//Arrays.asList(2,8,10);
	public static final List<Integer> TRAF_INT = Arrays.asList(20);//Arrays.asList(1,2,5,10,15,20,25,30,35,40,45,50,55,60,65,70);	
	public static final String TRAFFIC_FILE = "traffic.txt";
	public static final String OUTPUT_VAR = "output_var";
	public static final String ILP_FILE_NAME = "14node_1_nfv";//"14node_dc_all_nfv","14node_dc_1_nfv"
	public static final String LaptopUserName = "Abhishek Gupta";
	public static final String PCUserName = "abgupta";
	public static final String ILP_WRITE_PATH = "C:/Users/" + PCUserName + "/Box Sync/Luna - Eclipse/PlaceVNF/FileLP/";	
	public static final List<Integer> FUNC_REQ = Arrays.asList(3);//5 function chain Arrays.asList(3,7,6,2,4);// 4 function chain Arrays.asList(1,5,2,8)
	//4 function chain Arrays.asList(3,7,6,2) //3 function chain Arrays.asList(3,7,6) //2 function chain Arrays.asList(3,7) //1 function chain Arrays.asList(3)
	
	public static class ServiceDetails{		
		public Double connectionBandwidth;//in Mbps
		public Double totalTrafficPercentage;
		public ArrayList<Integer> setOfIDs;
		
		public ServiceDetails(Double connectionBandwidth, Double totalTrafficPercentage, int [] scIDs){			
			this.connectionBandwidth = connectionBandwidth;
			this.totalTrafficPercentage = totalTrafficPercentage;
			this.setOfIDs = new ArrayList<Integer>();
			for(int scID : scIDs){
				setOfIDs.add(scID);
			}
		}		
	}
	public static final Map<String,ServiceDetails> services = new HashMap<String,ServiceDetails>();
	public static void populateServices(){
		//connection bandwidth in Mbps
		String service = new String("web");
		int scIDs[] = new int[]{0,15,5,4};
		ServiceDetails detServ = new ServiceDetails(0.1,0.182,scIDs);
		services.put(service, detServ);
		
		service = new String("voip");
		scIDs = new int[]{1,16,7,6};
		detServ = new ServiceDetails(.064,.118,scIDs);
		services.put(service, detServ);
		
		service = new String("videostream");
		scIDs = new int[]{2,17,9,8};
		detServ = new ServiceDetails(4.0,0.698,scIDs);
		services.put(service, detServ);
		
		service = new String("cloudgame");
		scIDs = new int[]{3,18,11,10};
		detServ = new ServiceDetails(4.0,0.002,scIDs);
		services.put(service, detServ);
	}
	
}
