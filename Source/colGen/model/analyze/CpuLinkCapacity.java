package colGen.model.analyze;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.HashMap;
import java.util.Map;

import ILP.NodePair;
import colGen.model.ver1.MpVarY;
import colGen.model.ver1.MpVarZ;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class CpuLinkCapacity {
	
	//calculate CPU cores required
	public static void calCpuCapacity(IloCplex masterProblem, Map<MpVarZ, IloNumVar> usedVarZ, Map<BaseVertex,Double> cpuCoreCount){
		 
		//iterate through the generated configurations
		try{
			System.out.println("##### calCpuCapacity() function has been run! #####");
			for(Map.Entry<MpVarZ, IloNumVar> entryZ : usedVarZ.entrySet()){
				if(masterProblem.getValue(entryZ.getValue()) != 0){
					System.out.println("SC = " + entryZ.getKey().sCiD);
					//iterate through configuration core counts
					for(Map.Entry<BaseVertex,Double> entryCPU : entryZ.getKey().nfvNodeCpuCores.entrySet()){
						System.out.println("\tVertex = " + entryCPU.getKey().get_id() + " ; CPU cores = " + entryCPU.getValue());
						//add it to the parent HashMap
						if(cpuCoreCount.get(entryCPU.getKey()) != null){							
							double val = entryCPU.getValue() + cpuCoreCount.get(entryCPU.getKey());
							cpuCoreCount.put(entryCPU.getKey(), val);
						}else{
							cpuCoreCount.put(entryCPU.getKey(), entryCPU.getValue());
						}
					}
				}
			}
		}catch(IloException exc){
			System.err.println("Catch CPLEX exception");
		}		
	}
	
	//calculate link capacities required
	public static void calLinkCapacity(IloCplex masterProblem, Map<MpVarZ, IloNumVar> usedVarZ, Map<MpVarY, IloNumVar> usedVarY, Map<NodePair,Double> linkCapacity){
		//iterate through the generated configurations
		try{
			for(Map.Entry<MpVarZ, IloNumVar> entryZ : usedVarZ.entrySet()){
				//check if the variable has been set
				if(masterProblem.getValue(entryZ.getValue()) != 0){
					//iterate through configuration link usages
					for(Map.Entry<NodePair,Double> entryLink : entryZ.getKey().linkBandwidth.entrySet()){
						//add it to the parent HashMap
						if(linkCapacity.get(entryLink.getKey()) != null){
							double val = entryLink.getValue() + linkCapacity.get(entryLink.getKey());
							linkCapacity.put(entryLink.getKey(), val);
						}else{
							linkCapacity.put(entryLink.getKey(), entryLink.getValue());
						}
					}					
				}
			}
		}catch(IloException exc){
			System.err.println("Caught CPLEX exception : Link capacity caculation from Z");
		}
		//iterate through the links coming to the 1st VNF location and going from last VNF location
		//iterate through the generated configurations
		try{
			for(Map.Entry<MpVarY, IloNumVar> entryY : usedVarY.entrySet()){
				//check if the variable has been set
				if(masterProblem.getValue(entryY.getValue()) != 0){
					double trafficOnLink = entryY.getKey().tn.flow_traffic;
					NodePair link = new NodePair(entryY.getKey().s_vrt,entryY.getKey().t_vrt);
					//add it to the parent HashMap
					if(linkCapacity.get(link) != null){
						double val = linkCapacity.get(link) + trafficOnLink;
						linkCapacity.put(link, val);
					}else{
						//add traffic on the link
						linkCapacity.put(link, trafficOnLink);
					}
				}			
			}
		}catch(IloException exc){
			System.err.println("Caught CPLEX exception : Link capacity calculation from Y");
		}		
	}

}
