package colGen.model.simulation;

import colGen.model.ver2.test.CgCompareWithILP;
import colGen.model.ver2.test.Scenario1;
import colGen.model.ver2.test.Scenario3;
import colGen.model.ver2.test.TestForCorrectness;

public class Ver2 {
	
	public static void runVer2() throws Exception{
		//motivation tests		
		CgCompareWithILP.runTest();
		
		//scenario 1
//		Scenario1.runTest();
		
		//scenario 3
//		Scenario3.runTest();
		
		//test whether model ver2 works correctly or not
//		TestForCorrectness.runTest();
	}
	
}
