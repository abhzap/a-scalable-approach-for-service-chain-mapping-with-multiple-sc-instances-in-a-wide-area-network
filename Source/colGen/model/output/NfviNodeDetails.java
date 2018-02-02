package colGen.model.output;

import java.io.FileWriter;
import java.util.Collection;

public class NfviNodeDetails {
	
	public static java.io.BufferedWriter makeBwWriter(String str){
		java.io.BufferedWriter reqWriter = null;
		try{	
			reqWriter = new java.io.BufferedWriter(new FileWriter(str));
			
		}catch(Exception exp){
			exp.printStackTrace();
			System.exit(1);
		}
		return reqWriter;
	}
	
	public static void writeBW(java.io.BufferedWriter reqWriter, double bwValue){			
		try{	
			reqWriter.write("\n" + bwValue);
		}catch(Exception exp){
			exp.printStackTrace();
			System.exit(1);
		}
			
	}
	
	public static java.io.BufferedWriter makeTimeWriter(String str){
		java.io.BufferedWriter reqWriter = null;
		try{	
			reqWriter = new java.io.BufferedWriter(new FileWriter(str));			
		}catch(Exception exp){
			exp.printStackTrace();
			System.exit(1);
		}
		return reqWriter;
	}
	
	public static void writeRunTime(java.io.BufferedWriter reqWriter, double cgTime, double ilpTime, double cplexTime, double totalTime){
		try{	
			reqWriter.write("\n" + cgTime + "\t" + ilpTime + "\t" + cplexTime + "\t" + totalTime);
		}catch(Exception exp){
			exp.printStackTrace();
			System.exit(1);
		}
	}
	
	public static java.io.BufferedWriter makeCpuWriter(String str){
		java.io.BufferedWriter reqWriter = null;
		try{	
			reqWriter = new java.io.BufferedWriter(new FileWriter(str));			
		}catch(Exception exp){
			exp.printStackTrace();
			System.exit(1);
		}
		return reqWriter;
	}
	
	public static void writeCpuCoreCount(java.io.BufferedWriter reqWriter, Collection<Double> cpuValues){
		try{	
			//actual number of CPUs per GB
			for(Double cpuValue : cpuValues){
				reqWriter.write("\n" + cpuValue/1000.0);
			}
		}catch(Exception exp){
			exp.printStackTrace();
			System.exit(1);
		}
	}
	
	public static java.io.BufferedWriter makeEoptWriter(String str){
		java.io.BufferedWriter reqWriter = null;
		try{	
			reqWriter = new java.io.BufferedWriter(new FileWriter(str));			
		}catch(Exception exp){
			exp.printStackTrace();
			System.exit(1);
		}
		return reqWriter;
	}
	
	public static void writeEoptimality(java.io.BufferedWriter reqWriter,double eOptimal){
		try{	
			reqWriter.write("\n" + eOptimal);
		}catch(Exception exp){
			exp.printStackTrace();
			System.exit(1);
		}
	}
	
	public static java.io.BufferedWriter makeClusterCountWriter(String str){
		java.io.BufferedWriter reqWriter = null;
		try{	
			reqWriter = new java.io.BufferedWriter(new FileWriter(str));
			
		}catch(Exception exp){
			exp.printStackTrace();
			System.exit(1);
		}
		return reqWriter;
	}
	
	public static void writeClusterCount(java.io.BufferedWriter reqWriter, int clusterCount){			
		try{	
			reqWriter.write("\n" + clusterCount);
		}catch(Exception exp){
			exp.printStackTrace();
			System.exit(1);
		}
			
	}
	
	
}
