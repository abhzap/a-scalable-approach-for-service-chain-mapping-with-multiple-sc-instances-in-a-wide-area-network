package colGen.model.simulation;

import colGen.model.ver1.test.TestClusterV2AllVnfDetailAllSc;
import colGen.model.ver1.test.TestClusterV2PerAllSc;

public class Ver1 {
	
	public static void runVer1() throws Exception{	
		
		//Motivation tests
//		PmCompareWithILP.runTest();
		
		//Test Traffic Pair Generation
//		TestNodePairGeneration.runTest();
		
		//Test Heuristic Details
//		TestHeuristic.runTest();
		
		//Globecom tests
//		TestClusterV2All.runTest();
//		TestClusterV2Per.runTest();
		TestClusterV2PerAllSc.runTest();
		
		
		//TON tests
//		TestClusterV2Uniform.runTest();
//		TestClusterV2CompareUniform.runTest();
//		TestClusterV2CompareSkew.runTest();
		
		//Link tests
//		TestClusterV2AllLink.runTest(); 
		
		//CPU core tests
		
		//Replica Tests
//		TestClusterV2AllVnfReplica.runTest();
//		TestClusterV2AllVnfDetail.runTest();
//		TestClusterV2AllVnfDetailAllSc.runTest();
//		TestClusterV2AllVnfDetailSelected.runTest();
//		TestClusterV2AllVnfDetailAllScSelected.runTest();
//		TestClusterV2AllVnfDetailAllScSelectedNetworks.runTest();
		
		//test the cluster generation
//		TestCluster.runTest();
		
		//test the cluster generation 
		//with cluster heuristic version 2
//		TestClusterV2.runTest();
		
		//test when using prev cols in memory
//		TestClusterPrevCols.runTest();
		
		//test the kNode tuning
//		TestKNodeCluster.runTest();		
		
		//Instances that I run		
//		Test.runTest(); 
		
		//Instances given by Nicholas
//		Instance.runInstances();
		
		//Latest instance to run and test
//		boolean cluster = true;
//		Test2.runTest(cluster);
		
		//clustering flag
//		boolean cluster = false;
//		int noOfSdPairsPerService = 10;	
//		VariableNFVInodes.runSim(cluster, noOfSdPairsPerService);
		
		//scenario 1
//		Scenario1.runTest();
		
        //scenario 3
//		Scenario3.runTest();
		//scenario 3.1
//		Scenario3sub1.runTest();
		
		//scenario 4
//		Scenario4.runTest();
		//scenario 4.2
//		Scenario4sub2.runTest();
		
		//scenario 5
//		Scenario5.runTest();
		//scenario 5.1
//		Scenario5sub1.runTest();
		
		//run test for SD Groups
		//divide the graph nodes
//		SdGroups.runTest();
		//cluster the (s,d) pairs
//		SdGroups2.runTest();		
		
	}

}
