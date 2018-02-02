package colGen.model.simulation;

import colGen.model.mpV2AsIlp.RunMpIlp;

public class Sim {

	public static void main(String args[]) throws Exception{
		
		//print the console output to the "consoleOutput.txt" file		
	    /*PrintStream out = new PrintStream(new FileOutputStream("consoleOutput.txt"));
		System.setOut(out);*/
		
        //run model for version1
		Ver1.runVer1();
		//run model for version2
//		Ver2.runVer2();
		
		//run the ILP
//		RunMpIlp.runIlp();
		
	}
}
